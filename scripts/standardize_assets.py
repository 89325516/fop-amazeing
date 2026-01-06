from PIL import Image
import os
import re

input_dir = "raw_assets/ai_processed_transparent"
output_dir = "raw_assets/ai_ready_optimized"
UNIT_SIZE = 64

def get_asset_config(filename):
    """
    Parses filename to determine target grid size.
    Returns: (width_units, height_units, is_wall)
    """
    w_units = 2  # Minimum 2x2 now
    h_units = 2
    is_wall = filename.startswith("wall_")

    # Parse Dimensions from Filename (e.g., "_2x2", "_3x3")
    dim_match = re.search(r'[_\-](\d+)x(\d+)', filename)
    if dim_match:
        w_units = int(dim_match.group(1))
        h_units = int(dim_match.group(2))

    return w_units, h_units, is_wall

def process_asset(img, target_w, target_h, stretch=True):
    """
    Process asset to target size.
    """
    bbox = img.getbbox()
    if not bbox:
        return Image.new("RGBA", (target_w, target_h), (0, 0, 0, 0))
    
    content = img.crop(bbox)
    
    if stretch:
        return content.resize((target_w, target_h), Image.LANCZOS)
    else:
        content_w, content_h = content.size
        scale = min(target_w / content_w, target_h / content_h)
        if scale > 10: scale = 1.0
        
        new_w = int(content_w * scale)
        new_h = int(content_h * scale)
        if new_w <= 0 or new_h <= 0:
            return Image.new("RGBA", (target_w, target_h))
        
        resized = content.resize((new_w, new_h), Image.LANCZOS)
        final = Image.new("RGBA", (target_w, target_h), (0, 0, 0, 0))
        paste_x = (target_w - new_w) // 2
        paste_y = (target_h - new_h) // 2
        final.paste(resized, (paste_x, paste_y))
        return final

def standardize_file(filename):
    input_path = os.path.join(input_dir, filename)
    output_path = os.path.join(output_dir, filename)

    try:
        img = Image.open(input_path).convert("RGBA")
        w_units, h_units, is_wall = get_asset_config(filename)
        
        if is_wall:
            # WALL: Visual height = logical height + 0.5
            target_w = w_units * UNIT_SIZE
            target_h = int((h_units + 0.5) * UNIT_SIZE)
            std_img = process_asset(img, target_w, target_h, stretch=True)
            mode = f"Wall ({w_units}x{h_units} -> {target_w}x{target_h}px)"
        elif filename.startswith("tile_"):
            target_w = w_units * UNIT_SIZE
            target_h = h_units * UNIT_SIZE
            std_img = process_asset(img, target_w, target_h, stretch=True)
            mode = "Tile (100% fill)"
        elif filename.startswith("item_") or filename.startswith("ui_"):
            target_w = UNIT_SIZE
            target_h = UNIT_SIZE
            std_img = process_asset(img, target_w, target_h, stretch=False)
            mode = "Item (centered)"
        else:
            target_w = w_units * UNIT_SIZE
            target_h = h_units * UNIT_SIZE
            std_img = process_asset(img, target_w, target_h, stretch=True)
            mode = "Default"
            
        std_img.save(output_path)
        print(f"[{mode}] {filename}")

    except Exception as e:
        print(f"Error: {filename} - {e}")

print("Starting asset standardization (v4 - new wall sizes)...")
if not os.path.exists(output_dir):
    os.makedirs(output_dir)

for filename in os.listdir(input_dir):
    if filename.endswith(".png"):
        standardize_file(filename)
print("Done.")
