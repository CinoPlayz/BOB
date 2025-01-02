


import torch
from torchvision import transforms
from PIL import Image
import matplotlib.pyplot as plt
from matplotlib.patches import Rectangle
import torchvision
from torchvision.models.detection import FasterRCNN_ResNet50_FPN_Weights
import os
os.environ["KMP_DUPLICATE_LIB_OK"] = "TRUE"


def calculate_occupancy_rate(image_path, total_seats, confidence_threshold=0.5, visualize=False):
    """
    Izračuna stopnjo zasedenosti prostora na podlagi števila zaznanih oseb in skupnega števila sedežev.

    Args:
        image_path (str): Pot do vhodne slike.
        total_seats (int): Skupno število sedežev v prostoru.
        confidence_threshold (float): Prag zaupanja za zaznavo oseb (privzeto 0.5).
        visualize (bool): Ali naj funkcija prikaže sliko z zaznanimi osebami (privzeto False).

    Returns:
        float: Stopnja zasedenosti v odstotkih.
    """
    num_people = detect_people(image_path, confidence_threshold, visualize)
    occupancy_rate = (num_people / total_seats) * 100
    #print(f"Število sedežev: {total_seats}, Število zaznanih oseb: {num_people}, Stopnja zasedenosti: {occupancy_rate:.2f}%")
    return occupancy_rate


def detect_people(image_path, confidence_threshold=0.5, visualize=False):
    """
    Detekcija oseb na sliki z uporabo Faster R-CNN modela.

    Args:
        image_path (str): Pot do vhodne slike.
        confidence_threshold (float): Prag zaupanja za zaznavo oseb (privzeto 0.5).
        visualize (bool): Ali naj funkcija prikaže sliko z zaznanimi osebami (privzeto False).

    Returns:
        int: Število zaznanih oseb.
    """
    image = Image.open(image_path).convert("RGB")

    # Pretvorba slike v tenzor
    transform = transforms.Compose([
        transforms.ToTensor(),
    ])
    image_tensor = transform(image).unsqueeze(0)

    weights = FasterRCNN_ResNet50_FPN_Weights.DEFAULT
    model = torchvision.models.detection.fasterrcnn_resnet50_fpn(weights=weights)
    model.eval()

    # Izvedba napovedi
    with torch.no_grad():
        predictions = model(image_tensor)

    # Filtriranje napovedi glede na prag zaupanja
    person_boxes = [
        (box, score) for box, score, label in zip(# Box vrne Koordinate bounding boxov za vse zaznane osebe, zip združi elemente iz treh seznamov v seznam (box, score, label) 
            predictions[0]['boxes'], predictions[0]['scores'], predictions[0]['labels']
        ) if score > confidence_threshold and label == 1
    ]

    num_people = len(person_boxes)


    if visualize:
        fig, ax = plt.subplots(1, figsize=(12, 12))
        ax.imshow(image)

        for i, (box, score) in enumerate(person_boxes):
            x1, y1, x2, y2 = box.numpy()
            
            rect = Rectangle((x1, y1), x2 - x1, y2 - y1, linewidth=2, edgecolor='red', facecolor='none')
            ax.add_patch(rect)
            
            ax.text(x1, y1 - 5, f"{i + 1}", color='red', fontsize=12, backgroundcolor='white')

        plt.title(f'Zaznane osebe: {num_people}')
        plt.axis('off')
        plt.show()

    #for i, (box, score) in enumerate(person_boxes):
        #print(f"Oseba {i + 1}: Bounding Box = {box.numpy()}, Zaupanje = {score:.2f}")

    return num_people




#image_path = 'train_resized/8_png.rf.de91d32ce2dcbb4fe2d21e2d7fdf1b0a.jpg'
#image_path = "test_image.jpg"
"""
Primer klica
total_seats = 10
occupancy_rate = calculate_occupancy_rate(image_path, total_seats, confidence_threshold=0.5, visualize=True)

print(f'Zsedenost: {occupancy_rate}')

"""