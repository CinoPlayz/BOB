import torch
import torchvision
import albumentations as A
from PIL import Image
import numpy as np

class HumanDetectionDataset(torch.utils.data.Dataset):
    def __init__(self, images_dict, annotations_dict, is_training=True):
        self.images_dict = images_dict
        self.annotations_dict = annotations_dict
        self.image_ids = list(annotations_dict.keys())
        self.is_training = is_training
        
        print(f"Dataset inicializiran z {len(self.image_ids)} slikami")
        
        self.basic_transform = A.Compose([
            A.Normalize(
                mean=[0.485, 0.456, 0.406],
                std=[0.229, 0.224, 0.225],
                always_apply=True
            )
        ])
        
        if is_training:
            self.augmentation = A.Compose([
                A.ColorJitter(p=0.3),
                A.HorizontalFlip(p=0.5),
                A.GaussianBlur(p=0.3),
                A.RandomShadow(p=0.3),
                A.RandomBrightnessContrast(p=0.3),
                A.GaussNoise(p=0.3)
            ], bbox_params=A.BboxParams(
                format='coco',
                min_visibility=0.3,
                label_fields=['class_labels']
            ))

    def coco_to_xyxy(self, boxes):
        if not boxes:
            return []
            
        boxes = torch.as_tensor(boxes)
        x1 = boxes[..., 0]
        y1 = boxes[..., 1]
        x2 = x1 + boxes[..., 2]
        y2 = y1 + boxes[..., 3]
        
        return torch.stack((x1, y1, x2, y2), dim=-1)

    def __getitem__(self, idx):
        image_id = self.image_ids[idx]
        image_path = self.images_dict[image_id]
        
        # Naloži sliko s PIL in pretvori v numpy uint8
        pil_image = Image.open(image_path).convert('RGB')
        image_np = np.array(pil_image, dtype=np.uint8)  
        
        #  bounding boxi
        boxes = self.annotations_dict[image_id]
        labels = [1] * len(boxes)
        
        
        if self.is_training and self.augmentation:
            transformed = self.augmentation(
                image=image_np,
                bboxes=boxes,
                class_labels=labels
            )
            image_np = transformed['image']
            boxes = transformed['bboxes']
            labels = transformed['class_labels']
        
        #  normalizacija
        transformed = self.basic_transform(image=image_np)
        image_np = transformed['image']
        
        # Pretvori v tensor
        image_tensor = torch.from_numpy(image_np).float().permute(2, 0, 1)
        
        if boxes:
            boxes_tensor = self.coco_to_xyxy(boxes)
            widths = boxes_tensor[:, 2] - boxes_tensor[:, 0]
            heights = boxes_tensor[:, 3] - boxes_tensor[:, 1]
            valid_boxes = (widths > 0) & (heights > 0)
            
            boxes_tensor = boxes_tensor[valid_boxes]
            labels = [l for l, v in zip(labels, valid_boxes) if v]
            
            labels_tensor = torch.as_tensor(labels, dtype=torch.int64)
        else:
            boxes_tensor = torch.zeros((0, 4), dtype=torch.float32)
            labels_tensor = torch.zeros((0,), dtype=torch.int64)
        
        return image_tensor, boxes_tensor, labels_tensor

    def __len__(self):
        return len(self.image_ids)