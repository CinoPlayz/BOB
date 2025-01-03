from PIL import Image
import os
import json
from ultralytics import YOLO
os.environ["KMP_DUPLICATE_LIB_OK"] = "TRUE"


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

    model = YOLO("yolo11n.pt")

    results = model(image_path, conf=confidence_threshold)
    
    num_people = 0
    for result in results: 
        if visualize:       
            result.show()
        result_json = result.to_json()
        obj = json.loads(result_json)

        for iter in obj:
            if iter['name'] == 'person' or iter['name'] == 'face':
               num_people += 1               
    return num_people

#detect_people("./test_images/CA_3.jpg")