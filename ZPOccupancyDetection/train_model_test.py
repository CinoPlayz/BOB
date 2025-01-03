import torch
from torchvision import models, transforms
from PIL import Image
import matplotlib.pyplot as plt
from matplotlib.patches import Rectangle
import torchvision
import os
import albumentations as A
import numpy as np

class FineTunedFasterRCNN(torch.nn.Module):
    def __init__(self, num_classes=2):
        super().__init__()
        self.model = torchvision.models.detection.fasterrcnn_resnet50_fpn(
            weights=None 
        )
        
        in_features = self.model.roi_heads.box_predictor.cls_score.in_features
        self.model.roi_heads.box_predictor = torchvision.models.detection.faster_rcnn.FastRCNNPredictor(
            in_features, num_classes
        )
    
    def forward(self, images, targets=None):
        return self.model(images, targets)

def prepare_image(image_path):

    pil_image = Image.open(image_path).convert('RGB')
    image_np = np.array(pil_image, dtype=np.uint8)
    
    # Normalizacija
    transform = A.Compose([
        A.Normalize(
            mean=[0.485, 0.456, 0.406],
            std=[0.229, 0.224, 0.225],
            always_apply=True
        )
    ])
    
    transformed = transform(image=image_np)
    image_tensor = torch.from_numpy(transformed['image']).float().permute(2, 0, 1)
    return image_tensor, pil_image


"""
Brez poobdelave
def detect_people(image_path, model_path, confidence_threshold=0.5):
    # Inicializacija modela
    model = FineTunedFasterRCNN(num_classes=2)
    model.load_state_dict(torch.load(model_path, map_location='cpu'))
    model.eval()
    
    # Priprava slike
    image_tensor, original_image = prepare_image(image_path)
    image_tensor = image_tensor.unsqueeze(0)  # Dodaj batch dimenzijo
    
    # Detekcija
    with torch.no_grad():
        predictions = model(image_tensor)
    
    # Filtriranje napovedi
    boxes = predictions[0]['boxes']
    scores = predictions[0]['scores']
    labels = predictions[0]['labels']
    
    # Izberi samo zaznave z dovolj visokim zaupanjem in razredom 1 (človek)
    mask = (scores > confidence_threshold) & (labels == 1)
    filtered_boxes = boxes[mask]
    
    # Vizualizacija
    fig, ax = plt.subplots(1, figsize=(12, 12))
    ax.imshow(original_image)
    
    for box in filtered_boxes:
        x1, y1, x2, y2 = box.numpy()
        rect = Rectangle((x1, y1), x2 - x1, y2 - y1, linewidth=2, edgecolor='red', facecolor='none')
        ax.add_patch(rect)
    
    plt.title(f'Zaznane osebe: {len(filtered_boxes)}')
    plt.axis('off')
    plt.show()
    
    return len(filtered_boxes)
"""
def detect_people(image_path, model_path, confidence_threshold=0.5):
    # Računanje deleža prekrivanja
    def calculate_overlap(box1, box2):
        # Izračun površine prekrivanja
        x1 = max(box1[0], box2[0])
        y1 = max(box1[1], box2[1])
        x2 = min(box1[2], box2[2])
        y2 = min(box1[3], box2[3])
        
        if x2 <= x1 or y2 <= y1:
            return 0.0
        
        intersection = (x2 - x1) * (y2 - y1)
        smaller_box_area = min(
            (box1[2] - box1[0]) * (box1[3] - box1[1]),
            (box2[2] - box2[0]) * (box2[3] - box2[1])
        )
        
        return intersection / smaller_box_area

    model = FineTunedFasterRCNN(num_classes=2)
    model.load_state_dict(torch.load(model_path, map_location='cpu'))
    model.eval()
    
    image_tensor, original_image = prepare_image(image_path)
    image_tensor = image_tensor.unsqueeze(0)
    
    with torch.no_grad():
        predictions = model(image_tensor)
    
    boxes = predictions[0]['boxes']
    scores = predictions[0]['scores']
    labels = predictions[0]['labels']
    
    # Izbere samo zaznave z dovolj visokim zaupanjem in razredom 1 (človek)
    mask = (scores > confidence_threshold) & (labels == 1)
    filtered_boxes = boxes[mask].numpy()
    filtered_scores = scores[mask].numpy()
    
    # Združevanje prekrivajočih boxov
    boxes_to_keep = []
    used_indices = set()
    
    # Sortira boxe po scores
    sorted_indices = np.argsort(-filtered_scores)  # Padajoče
    
    for i in sorted_indices:
        if i in used_indices:
            continue
            
        current_box = filtered_boxes[i]
        boxes_to_keep.append(current_box)
        
        # Preveri prekrivanje z ostalimi boxi
        for j in sorted_indices:
            if i != j and j not in used_indices:
                overlap = calculate_overlap(current_box, filtered_boxes[j])
                if overlap > 0.7:  # Več kot 70% prekrivanje
                    used_indices.add(j)
        
        used_indices.add(i)
    
    fig, ax = plt.subplots(1, figsize=(12, 12))
    ax.imshow(original_image)
    
    for box in boxes_to_keep:
        x1, y1, x2, y2 = box
        rect = Rectangle((x1, y1), x2 - x1, y2 - y1, linewidth=2, edgecolor='red', facecolor='none')
        ax.add_patch(rect)
    
    plt.title(f'Zaznane osebe: {len(boxes_to_keep)}')
    plt.axis('off')
    plt.show()
    
    return len(boxes_to_keep)

# Uporaba
#image_path = 'train_resized/8_png.rf.de91d32ce2dcbb4fe2d21e2d7fdf1b0a.jpg'
#image_path = 'train_resized/1_png.rf.d260c8d56929b12bca3683b87597c54d.jpg'
#image_path = 'train_resized/4_png.rf.88e001383a485ee8f983adbb4630dd5c.jpg'
#image_path = 'train_resized/image_1704690827350_png.rf.7427d60bac36dc17960ea15c46d5e60a.jpg'#Samo glava
#image_path = 'train_resized/Orang-48-_png.rf.30f708ce36835e036c6b05009ee0adce.jpg'#LAžja
image_path = 'train_resized/Orang-24-_png.rf.3ec32b8b1634e41814a9b486b799449f.jpg'#Težja




model_path = 'fasterrcnn_model.pth'
num_people = detect_people(image_path, model_path, confidence_threshold=0.5)
print(f'Število zaznanih oseb: {num_people}')