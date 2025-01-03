import torch
import torchvision
from torch.utils.data import DataLoader
from torch.optim import Adam
from torch.utils.tensorboard import SummaryWriter
import os
import torch.backends.mkldnn as mkldnn
import torch._dynamo
import csv

from data_processing import collate_fn


class FineTunedFasterRCNN(torch.nn.Module):
    def __init__(self, num_classes=2):  # 2 razreda: ozadje in človek
        super().__init__()
        
        self.model = torchvision.models.detection.fasterrcnn_resnet50_fpn(
            weights=torchvision.models.detection.FasterRCNN_ResNet50_FPN_Weights.DEFAULT
        )
        
        # Zamenjamo classification head/ zadnji sloj  z novim za naš primer
        in_features = self.model.roi_heads.box_predictor.cls_score.in_features
        self.model.roi_heads.box_predictor = torchvision.models.detection.faster_rcnn.FastRCNNPredictor(
            in_features, num_classes
        )
    
    def forward(self, images, targets=None):
        # Med učenjem potrebujemo targets, med evalvacijo pa ne
        return self.model(images, targets)

def train_one_epoch(model, dataloader, optimizer, device, scaler):
    model.train()
    total_loss = 0
    total_cls_loss = 0    # Klasifikacijska izguba -> prepoznavanje objektov
    total_box_loss = 0    # Lokalizacijska izguba -> določanje pozicij bounding boxov
    total_rpn_loss = 0    # RPN izguba -> Namesto iskanja vseh možnih regij, RPN uporablja konvolucijsko mrežo za predlaganje omejenega števila regij z visoko verjetnostjo, da vsebujejo objekte.
    num_batches = len(dataloader)
    
    for batch_idx, (images, boxes, labels) in enumerate(dataloader):

        images = [img.to(device) for img in images]
        targets = []
        for box, label in zip(boxes, labels):
            valid_indices = label.nonzero().squeeze(1)#vrne indekse vseh veljavnih bounding boxov
            target = {
                'boxes': box[valid_indices].to(device),
                'labels': label[valid_indices].to(device)
            }
            targets.append(target)
        if device == 'cuda':
            with torch.cuda.amp.autocast():
                loss_dict = model(images, targets)
                cls_loss = loss_dict.get('loss_classifier', 0)
                box_loss = loss_dict.get('loss_box_reg', 0)
                rpn_loss = sum(v for k, v in loss_dict.items() if 'rpn' in k)
                losses = sum(loss for loss in loss_dict.values())
        
            optimizer.zero_grad()
            scaler.scale(losses).backward()
            scaler.step(optimizer)
            scaler.update()
            
        else:
            # CPU način
            loss_dict = model(images, targets)
            cls_loss = loss_dict.get('loss_classifier', 0)
            box_loss = loss_dict.get('loss_box_reg', 0)
            rpn_loss = sum(v for k, v in loss_dict.items() if 'rpn' in k)
            losses = sum(loss for loss in loss_dict.values())
            
            optimizer.zero_grad()
            losses.backward()
            optimizer.step()
        
        total_loss += losses.item()
        if isinstance(cls_loss, torch.Tensor):
            total_cls_loss += cls_loss.item()
        if isinstance(box_loss, torch.Tensor):
            total_box_loss += box_loss.item()
        total_rpn_loss += rpn_loss if isinstance(rpn_loss, float) else rpn_loss.item()
        
        #if (batch_idx + 1) % 10 == 0:
        print(f"\nBatch {batch_idx + 1}/{num_batches}")
        print(f"- Trenutna izguba: {losses.item():.4f}")
        print(f"- Klasifikacijska izguba: {cls_loss.item() if isinstance(cls_loss, torch.Tensor) else 0:.4f}")
        print(f"- Lokalizacijska izguba: {box_loss.item() if isinstance(box_loss, torch.Tensor) else 0:.4f}")
    

    avg_loss = total_loss / num_batches
    avg_cls_loss = total_cls_loss / num_batches
    avg_box_loss = total_box_loss / num_batches
    avg_rpn_loss = total_rpn_loss / num_batches
    
    print(f"- Povprečna skupna izguba: {avg_loss:.4f}")
    print(f"- Povprečna klasifikacijska izguba: {avg_cls_loss:.4f}")
    print(f"- Povprečna lokalizacijska izguba: {avg_box_loss:.4f}")
    print(f"- Povprečna RPN izguba: {avg_rpn_loss:.4f}")
    
    return avg_loss, {
        'cls_loss': avg_cls_loss,
        'box_loss': avg_box_loss,
        'rpn_loss': avg_rpn_loss
    }

def validate(model, dataloader, device):
    model.eval()
    total_loss = 0
    num_batches = len(dataloader)

    with torch.no_grad():
        for batch_idx, (images, boxes, labels) in enumerate(dataloader):

            images = [img.to(device) for img in images]
            targets = []
            for box, label in zip(boxes, labels):
                valid_indices = label.nonzero().squeeze(1)
                target = {
                    'boxes': box[valid_indices].to(device),
                    'labels': label[valid_indices].to(device)
                }
                targets.append(target)
            
            model.train()  
            if device == 'cuda':
                with torch.cuda.amp.autocast():
                    loss_dict = model(images, targets)
            else:
                loss_dict = model(images, targets)
            model.eval()
            
            # Faster R-CNN vrne slovar z izgubami
            losses = sum(loss for loss in loss_dict.values())
            total_loss += losses.item()

            
            #if (batch_idx + 1) % 10 == 0:
            current_avg_loss = total_loss / (batch_idx + 1)
            print(f"\nValidacijski batch {batch_idx + 1}/{num_batches}")
            print(f"- Trenutna povprečna izguba: {current_avg_loss:.4f}")
    
    avg_loss = total_loss / num_batches

    print(f"- Validacijska izguba: {avg_loss:.4f}")
    
    return avg_loss

def train_model(train_dataset, valid_dataset, num_epochs=10, device='cuda'):
    if device == 'cuda':
        torch.backends.cudnn.benchmark = True
        torch.backends.cuda.matmul.allow_tf32 = True

    if device == 'cuda' and not torch.cuda.is_available():
        raise RuntimeError("CUDA ni na voljo")

    
    print(f"Uporabljam napravo: {device}")
    print(f"Velikost učne množice: {len(train_dataset)} slik")
    print(f"Velikost validacijske množice: {len(valid_dataset)} slik")
    
    train_loader = DataLoader(
        train_dataset,
        batch_size=8,  
        shuffle=True,
        collate_fn=collate_fn,
        num_workers=4 if device == 'cuda' else 0, 
        persistent_workers=True if device == 'cuda' else False,
        pin_memory=True if device == 'cuda' else False# pin_memory za hitrejši prenos na GPU
    )
    
    valid_loader = DataLoader(
        valid_dataset,
        batch_size=8,  
        shuffle=False,
        collate_fn=collate_fn,
        num_workers=4 if device == 'cuda' else 0,
        persistent_workers=True if device == 'cuda' else False,
        pin_memory=True if device == 'cuda' else False
    )

    print(f"- Število učnih batch-ev: {len(train_loader)}")
    print(f"- Število validacijskih batch-ev: {len(valid_loader)}")

    # Inicializacija modela in optimizatorja
    model = FineTunedFasterRCNN().to(device)
    optimizer = torch.optim.AdamW(model.parameters(), lr=0.0001)


    # Dodamo gradient scaler za mixed precision training
    scaler = torch.cuda.amp.GradScaler() if device == 'cuda' else None

    if device == 'cuda' and hasattr(torch, 'compile'):
        model = torch.compile(model, mode="reduce-overhead", fullgraph=True)

    
    writer = SummaryWriter('runs/fasterrcnn_finetune')

    csv_file = 'epoch_losses.csv'
    with open(csv_file, mode='w', newline='') as file:
        csv_writer = csv.writer(file)
        csv_writer.writerow(['Epoch', 'Train Loss', 'Validation Loss', 'Classification Loss', 'Box Loss', 'RPN Loss'])
    best_val_loss = float('inf')
    
    for epoch in range(num_epochs):
        print(f"\nZačenjam epoho {epoch + 1}")    
        train_loss_tuple = train_one_epoch(model, train_loader, optimizer, device, scaler)
        train_loss = train_loss_tuple[0]
        
        val_loss = validate(model, valid_loader, device)
        
        writer.add_scalar('Loss/train', train_loss, epoch)
        writer.add_scalar('Loss/validation', val_loss, epoch)

        loss_dict = train_loss_tuple[1]
        writer.add_scalar('Loss/classification', loss_dict['cls_loss'], epoch)
        writer.add_scalar('Loss/box_regression', loss_dict['box_loss'], epoch)
        writer.add_scalar('Loss/rpn', loss_dict['rpn_loss'], epoch)
        
        print(f'Epoch {epoch + 1}/{num_epochs}:')
        print(f'Train Loss: {train_loss:.4f}')
        print(f'Validation Loss: {val_loss:.4f}')


        with open(csv_file, mode='a', newline='') as file:
            csv_writer = csv.writer(file)
            csv_writer.writerow([
                epoch + 1,
                train_loss,
                val_loss,
                loss_dict['cls_loss'],
                loss_dict['box_loss'],
                loss_dict['rpn_loss']
            ])
        
        if val_loss < best_val_loss:
            best_val_loss = val_loss
            torch.save(model.state_dict(), 'fasterrcnn_model.pth')
            print(f'Shranil novi najboljši model z validation loss: {val_loss:.4f}')
    
    writer.close()
    return model

