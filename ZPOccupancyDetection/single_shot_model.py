import torch
from torchvision import transforms
from PIL import Image
import matplotlib.pyplot as plt
from matplotlib.patches import Rectangle
import torchvision
from torchvision.models.detection import ssd300_vgg16, SSD300_VGG16_Weights
import os
os.environ["KMP_DUPLICATE_LIB_OK"] = "TRUE"

def detect_people_ssd(image_path, confidence_threshold=0.5, visualize=True):
    """
    Detekcija oseb na sliki z uporabo SSD modela.
    """

    if not os.path.exists(image_path):
        print(f"Napaka: Slika {image_path} ne obstaja.")
        return 0
        
    image = Image.open(image_path).convert("RGB")

    transform = transforms.Compose([
        transforms.ToTensor(),
    ])
    image_tensor = transform(image).unsqueeze(0)

    weights = SSD300_VGG16_Weights.DEFAULT
    model = ssd300_vgg16(weights=weights)
    model.eval()

    with torch.no_grad():
        predictions = model(image_tensor)

    person_boxes = [
        (box, score) for box, score, label in zip(
            predictions[0]['boxes'], 
            predictions[0]['scores'], 
            predictions[0]['labels']
        ) if score > confidence_threshold and label == 1
    ]

    num_people = len(person_boxes)

    if visualize:
        fig, ax = plt.subplots(1, figsize=(12, 12))
        ax.imshow(image)

        for i, (box, score) in enumerate(person_boxes):
            x1, y1, x2, y2 = box.numpy()
            rect = Rectangle((x1, y1), x2 - x1, y2 - y1, 
                           linewidth=2, edgecolor='red', facecolor='none')
            ax.add_patch(rect)
            
            ax.text(x1, y1 - 5, f"{i + 1} ({score:.2f})", 
                   color='red', fontsize=12, backgroundcolor='white')

        plt.title(f'SSD zaznane osebe: {num_people}')
        plt.axis('off')
        plt.show()

    return num_people

if __name__ == "__main__":
    image_path = 'train_resized/8_png.rf.de91d32ce2dcbb4fe2d21e2d7fdf1b0a.jpg'
    num_people = detect_people_ssd(image_path, confidence_threshold=0.5, visualize=True)
    print(f'Število zaznanih ljudi: {num_people}')