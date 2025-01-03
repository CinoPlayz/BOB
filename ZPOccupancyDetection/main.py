
import torch
import os
os.environ['KMP_DUPLICATE_LIB_OK'] = 'TRUE'
os.environ['CUDA_LAUNCH_BLOCKING'] = '1'


from human_detection_dataset import HumanDetectionDataset
from data_processing import load_annotations_and_images
from model_training import train_model 


train_annotations_output_path = "train_resized/annotations_resized.coco.json"
valid_annotations_output_path = "valid_resized/annotations_resized.coco.json"

train_images = "train_resized"
valid_images = "valid_resized"

def main():
    #Optimizacije za CUDA
    torch.backends.cuda.matmul.allow_tf32 = True  # Omogočimo TF32 za hitrejše računanje
    torch.backends.cudnn.benchmark = True  # Omogočimo CUDNN benchmark
    
    print("Nalagam podatke...")
    train_annotations_dict, train_image_files = load_annotations_and_images(train_images, train_annotations_output_path)
    valid_annotations_dict, valid_image_files = load_annotations_and_images(valid_images, valid_annotations_output_path)
    
    
    print("Pripravljam dataset...")
    train_dataset = HumanDetectionDataset(train_image_files, train_annotations_dict, is_training=True)
    valid_dataset = HumanDetectionDataset(valid_image_files, valid_annotations_dict, is_training=False)
    
    
    print(f"Število učnih primerov: {len(train_dataset)}")
    print(f"Število validacijskih primerov: {len(valid_dataset)}")
    
    if torch.cuda.is_available():
        device = torch.device('cuda')
        print(f"Uporabljam GPU: {torch.cuda.get_device_name(0)}")
        print(f"Količina GPU spomina: {torch.cuda.get_device_properties(0).total_memory / 1024**3:.1f} GB")
    else:
        device = torch.device('cpu')
        print("GPU ni na voljo, uporabljam CPU")
    

    print("Začenjam učenje...")
    try:
        model = train_model(
            train_dataset=train_dataset,
            valid_dataset=valid_dataset,
            num_epochs=10,  
            #device=device # za gpu
            device='cpu' 
        )
        print("Učenje uspešno zaključeno!")
        
    except Exception as e:
        print(f"Prišlo je do napake med učenjem: {str(e)}")
        raise e

if __name__ == "__main__":
    main()