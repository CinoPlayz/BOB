


import json
import matplotlib.pyplot as plt
from matplotlib.patches import Rectangle
import os
import torch
from PIL import Image
import concurrent.futures
from tqdm import tqdm
import shutil



human_category_id = 0  
relevant_category_ids = {0, 1, 2}

def process_annotations(input_path, output_path):

    if not os.path.exists(input_path):
        print(f"Input file not found: {input_path}")
        return
    
    with open(input_path, "r") as f:
        data = json.load(f)
    
    data["categories"] = [{"id": human_category_id, "name": "human", "supercategory": "none"}]
    
    for annotation in data["annotations"]:
        if annotation["category_id"] in relevant_category_ids:
            annotation["category_id"] = human_category_id
    
    with open(output_path, "w") as f:
        json.dump(data, f)
    
    category_ids = {annotation["category_id"] for annotation in data["annotations"]}
    print(f"Processed {input_path}. Unique category IDs: {category_ids}")



def load_annotations_and_images(images_dir, annotations_json_path):
    with open(annotations_json_path, "r") as f:
        data = json.load(f)
    
    available_files = set(os.listdir(images_dir))
    print(f"Datoteke v direktoriju: {len(available_files)}")
    
    # ID -> filename mapping samo za obstoječe datoteke
    image_id_to_filename = {}
    for img in data['images']:
        if img['file_name'] in available_files:
            image_id_to_filename[str(img['id'])] = img['file_name']
    
    print(f"Najdenih ujemajočih slik: {len(image_id_to_filename)}")
    
    annotations_dict = {}
    for annotation in data["annotations"]:
        image_id = str(annotation["image_id"])
        if image_id in image_id_to_filename: 
            bbox = annotation["bbox"]
            if image_id not in annotations_dict:
                annotations_dict[image_id] = []
            annotations_dict[image_id].append(bbox)
    
    images_dict = {}
    for image_id, file_name in image_id_to_filename.items():
        if image_id in annotations_dict:  # Samo slike z anotacijami
            images_dict[image_id] = os.path.join(images_dir, file_name)
    
    return annotations_dict, images_dict







def calculate_new_bbox(bbox, original_size, target_size):
    """
    Preračuna koordinate bounding boxa za novo velikost slike.
    
    Args:
        bbox (list): [x, y, width, height]
        original_size (tuple): (width, height) originalne slike
        target_size (int): ciljna velikost (širina in višina)
    """
    scale_x = target_size / original_size[0]
    scale_y = target_size / original_size[1]
    scale = min(scale_x, scale_y)
    
    # padding
    pad_x = (target_size - original_size[0] * scale) / 2
    pad_y = (target_size - original_size[1] * scale) / 2
    
    # Prilagodi bbox koordinate
    new_x = bbox[0] * scale + pad_x
    new_y = bbox[1] * scale + pad_y
    new_width = bbox[2] * scale
    new_height = bbox[3] * scale
    
    return [new_x, new_y, new_width, new_height]


def resize_image_with_info(args):
    input_path, output_path, target_size = args
    try:
        with Image.open(input_path) as img:
            original_size = img.size
            
            # Ohrani razmerje stranic in doda padding
            img.thumbnail((target_size, target_size), Image.Resampling.LANCZOS)
            
            new_img = Image.new("RGB", (target_size, target_size), color="black")
            
            x = (target_size - img.size[0]) // 2
            y = (target_size - img.size[1]) // 2
            
            new_img.paste(img, (x, y))
            new_img.save(output_path, "JPEG", quality=95)
            
            return True, original_size
    except Exception as e:
        print(f"Napaka pri obdelavi {input_path}: {str(e)}")
        return False, None


def resize_dataset_with_annotations(input_dir, output_dir, annotations_path, target_size):
    """
    Zmanjša vse slike in posodobi anotacije.
    
    Args:
        input_dir (str): Direktorij z originalnimi slikami
        output_dir (str): Direktorij kam se shranijo zmanjšane slike
        annotations_path (str): Pot do COCO annotations datoteke
        target_size (int): Ciljna velikost slike
    """

    os.makedirs(output_dir, exist_ok=True)
    

    with open(annotations_path, 'r') as f:
        annotations_data = json.load(f)
    
    # za original_size podatke
    image_sizes = {}
    
    # Zbere vse poti do slik in njihove ID-je
    image_files = []
    id_to_filename = {}
    filename_to_id = {}
    
    for img_info in annotations_data['images']:
        filename = img_info['file_name']
        img_id = img_info['id']
        input_path = os.path.join(input_dir, filename)
        output_path = os.path.join(output_dir, filename)
        
        if os.path.exists(input_path):
            image_files.append((input_path, output_path, target_size))
            id_to_filename[img_id] = filename
            filename_to_id[filename] = img_id
    
    print(f"Najdenih {len(image_files)} slik za obdelavo")
    
    # Resize slike in zberi original sizes
    with concurrent.futures.ThreadPoolExecutor(max_workers=os.cpu_count()) as executor:
        results = list(tqdm(executor.map(resize_image_with_info, image_files),
                          total=len(image_files),
                          desc="Obdelava slik"))
    
    # Zbere original sizes
    for (success, size), (input_path, _, _) in zip(results, image_files):
        if success and size:
            filename = os.path.basename(input_path)
            if filename in filename_to_id:
                img_id = filename_to_id[filename]
                image_sizes[img_id] = size
    
    # Posodobi anotacije
    for img in annotations_data['images']:
        img['width'] = target_size
        img['height'] = target_size
    
    for ann in annotations_data['annotations']:
        img_id = ann['image_id']
        if img_id in image_sizes:
            original_size = image_sizes[img_id]
            ann['bbox'] = calculate_new_bbox(ann['bbox'], original_size, target_size)
    

    output_annotations_path = os.path.join('annotations_resized.coco.json')
    print(f"Izračunana pot za anotacije: {output_annotations_path}")

    try:
        with open(output_annotations_path, 'w') as f:
            json.dump(annotations_data, f)
        print(f"Anotacije uspešno shranjene v: {output_annotations_path}")
    except Exception as e:
        print(f"Napaka pri shranjevanju anotacij: {str(e)}")

    
    successful = sum(1 for success, _ in results if success)
    print(f"Uspešno obdelanih {successful} od {len(image_files)} slik")
    print(f"Nove anotacije shranjene v: {output_annotations_path}")






