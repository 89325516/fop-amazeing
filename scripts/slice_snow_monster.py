from PIL import Image
import os
import shutil

def process_snow_monster():
    source_path = "/Users/papersiii/fopws2526projectfop-amazeing/assets/custom_images/9d8e7f6a/spritesheet_gen.png"
    output_dir = "/Users/papersiii/fopws2526projectfop-amazeing/assets/custom_images/9d8e7f6a"
    
    if not os.path.exists(source_path):
        print(f"Source not found: {source_path}")
        return

    # Backup original before overwriting
    if not os.path.exists(output_dir):
        os.makedirs(output_dir)

    img = Image.open(source_path).convert("RGBA")
    print(f"Source Image Size: {img.size}")
    
    # Background Removal Threshold
    # If pixel is very bright (near white), make it transparent
    datas = img.getdata()
    new_data = []
    for item in datas:
        # Check if R, G, B are all > 240 (Near white)
        # You can adjust this threshold
        if item[0] > 240 and item[1] > 240 and item[2] > 240:
            new_data.append((255, 255, 255, 0)) # Transparent
        else:
            new_data.append(item)
    
    img.putdata(new_data)
    
    # Grid is 4x4
    fw = img.width // 4
    fh = img.height // 4
    
    target_size = (64, 64)
    
    # New Mapping for Directional Support
    # Input Layout:
    # Row 0: Right (Frames 4-7)
    # Row 1: Left  (Frames 12-15) -> Standard LibGDX usually expects Left at 3
    # Row 2: Down (Frames 0-3)  -> Standard LibGDX usually expects Down at 0
    # Row 3: Up   (Frames 8-11) -> Standard LibGDX usually expects Up at 2
    
    # We will output individual files sequence 0..15 
    # But we will also be mindful of which block corresponds to what direction
    # Block 0 (0-3): Down
    # Block 1 (4-7): Right
    # Block 2 (8-11): Up
    # Block 3 (12-15): Left
    
    # Updated Mapping with Flip Logic
    # Row 0: Right -> Save as Block 1 (4-7) AND Flip for Block 3 (12-15)
    # Row 2: Down  -> Save as Block 0 (0-3)
    # Row 3: Up    -> Save as Block 2 (8-11)
    
    # We ignore Row 1 from source because user reported it looks like Right
    
    for row_idx in range(4):
        # We only process Row 0, 2, 3 from source
        if row_idx == 1:
            continue
            
        for col_idx in range(4):
            # Crop frame
            left = col_idx * fw
            top = row_idx * fh
            right = left + fw
            bottom = top + fh
            
            frame = img.crop((left, top, right, bottom))
            
            # Resize if needed
            if frame.size != target_size:
                frame = frame.resize(target_size, Image.NEAREST)
            
            # Logic based on source row (Assuming Standard Layout: 0=Down, 1=Left, 2=Right, 3=Up)
            
            if row_idx == 0: # Down Row (Standard)
                # Save as Down (Block 0)
                down_idx = 0 * 4 + col_idx # 0, 1, 2, 3
                down_path = os.path.join(output_dir, f"move_{down_idx}.png")
                frame.save(down_path)
                print(f"Saved Down frame move_{down_idx}.png from Row {row_idx} Col {col_idx}")

            elif row_idx == 2: # Right Row (Standard)
                # 1. Save as Right (Block 1)
                right_idx = 1 * 4 + col_idx # 4, 5, 6, 7
                right_path = os.path.join(output_dir, f"move_{right_idx}.png")
                frame.save(right_path)
                print(f"Saved Right frame move_{right_idx}.png from Row {row_idx} Col {col_idx}")
                
                # 2. Flip and Save as Left (Block 3)
                flipped_frame = frame.transpose(Image.FLIP_LEFT_RIGHT)
                left_idx = 3 * 4 + col_idx # 12, 13, 14, 15
                left_path = os.path.join(output_dir, f"move_{left_idx}.png")
                flipped_frame.save(left_path)
                print(f"Saved Left (Flipped) frame move_{left_idx}.png from Row {row_idx} Col {col_idx}")
                
            elif row_idx == 3: # Up Row
                # Save as Up (Block 2)
                up_idx = 2 * 4 + col_idx # 8, 9, 10, 11
                up_path = os.path.join(output_dir, f"move_{up_idx}.png")
                frame.save(up_path)
                print(f"Saved Up frame move_{up_idx}.png from Row {row_idx} Col {col_idx}")

if __name__ == "__main__":
    process_snow_monster()
