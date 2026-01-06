from PIL import Image, ImageOps
import os
import re

input_dir = "raw_assets/ai_generated_raw"
output_dir = "raw_assets/ai_processed_transparent"

# Watermark crop settings (bottom-right corner)
WATERMARK_CROP_PERCENT = 0.08  # Crop 8% from bottom and right

def is_white_or_light(r, g, b, threshold=200):
    """
    Checks if a pixel is considered white/light background.
    """
    c_max = max(r, g, b)
    c_min = min(r, g, b)
    delta = c_max - c_min
    
    saturation = 0
    if c_max > 0:
        saturation = delta / c_max
    
    is_grey_white = saturation < 0.15 and c_max > threshold
    is_checkerboard_grey = saturation < 0.1 and 180 < c_max < 230 and abs(r - g) < 15 and abs(g - b) < 15
    
    return is_grey_white or is_checkerboard_grey

def crop_watermark(img):
    """
    Crops the bottom-right corner to remove potential AI watermarks.
    """
    width, height = img.size
    crop_x = int(width * WATERMARK_CROP_PERCENT)
    crop_y = int(height * WATERMARK_CROP_PERCENT)
    
    # Crop from all sides slightly, but more from bottom-right
    left = crop_x // 2
    top = crop_y // 2
    right = width - crop_x
    bottom = height - crop_y
    
    return img.crop((left, top, right, bottom))

def remove_all_white_pixels(image_path, output_path):
    """
    1. Crops watermark area
    2. Removes all white/light background pixels globally
    """
    img = Image.open(image_path)
    img = img.convert("RGBA")
    
    # Step 1: Crop potential watermark
    img = crop_watermark(img)
    
    width, height = img.size
    pixels = img.load()
    
    removed_count = 0
    
    # Step 2: Remove white pixels
    for y in range(height):
        for x in range(width):
            r, g, b, a = pixels[x, y]
            if a == 0:
                continue
            if is_white_or_light(r, g, b):
                pixels[x, y] = (0, 0, 0, 0)
                removed_count += 1
    
    img.save(output_path, "PNG")
    print(f"Processed: {output_path} (cropped watermark, removed {removed_count} bg pixels)")

print("Starting smart background removal (v2 with watermark crop)...")
if not os.path.exists(output_dir):
    os.makedirs(output_dir)

for filename in os.listdir(input_dir):
    if filename.endswith(".png"):
        input_path = os.path.join(input_dir, filename)
        output_path = os.path.join(output_dir, filename)
        remove_all_white_pixels(input_path, output_path)
print("Done.")
