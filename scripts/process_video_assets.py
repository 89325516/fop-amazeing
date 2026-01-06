import cv2
import os
import sys
from PIL import Image
import numpy as np

# Configuration
INPUT_DIR = "raw_assets/videos"
OUTPUT_DIR = "raw_assets/ai_processed_transparent"
UNIT_SIZE = 64
DEFAULT_FRAMES = 4

def ensure_dirs():
    if not os.path.exists(INPUT_DIR):
        os.makedirs(INPUT_DIR)
        print(f"Created input directory: {INPUT_DIR}")
        print("Place your .mp4 or .gif files there.")
    if not os.path.exists(OUTPUT_DIR):
        os.makedirs(OUTPUT_DIR)

def process_video(filename):
    filepath = os.path.join(INPUT_DIR, filename)
    basename = os.path.splitext(filename)[0]
    
    # Try to parse target frames from name (e.g., "wall_fire_4frames.mp4")
    target_frames = DEFAULT_FRAMES
    if "frames" in basename:
        import re
        match = re.search(r'(\d+)frames', basename)
        if match:
            target_frames = int(match.group(1))
    
    cap = cv2.VideoCapture(filepath)
    if not cap.isOpened():
        print(f"Error opening video parsing: {filename}")
        return

    frame_count = int(cap.get(cv2.CAP_PROP_FRAME_COUNT))
    print(f"Processing {filename}: {frame_count} frames found. Extracting {target_frames} frames...")

    if frame_count < target_frames:
        print(f"Warning: Video has fewer frames ({frame_count}) than requested ({target_frames}). Using all available.")
        target_frames = frame_count

    extracted_images = []
    
    # Strategy: Extract evenly spaced frames
    # e.g. if 24 frames total and we want 4, we take indices 0, 6, 12, 18
    step = frame_count / target_frames
    
    for i in range(target_frames):
        frame_idx = int(i * step)
        cap.set(cv2.CAP_PROP_POS_FRAMES, frame_idx)
        ret, frame = cap.read()
        
        if ret:
            # Convert BGR (OpenCV) to RGBA (PIL)
            frame_rgb = cv2.cvtColor(frame, cv2.COLOR_BGR2RGBA)
            img = Image.fromarray(frame_rgb)
            extracted_images.append(img)
        else:
            print(f"Failed to read frame {frame_idx}")

    cap.release()

    if not extracted_images:
        return

    # Create Sprite Sheet
    # We assume the output should be Standardized immediately? 
    # Or just dumped to "ai_processed_transparent" for the standardize script to pick up?
    # Let's verify standard script conventions.
    # Standard script expects: png image. If "anim" in name, it treats as strip.
    # So we should create a horizontal strip.
    
    # Determine dimensions. 
    # We will resize blindly to a high quality base (e.g. 256x256 per frame) 
    # or let standardize_assets handle the final downscaling?
    # Better to let standardize_assets handle the final pixel-perfect scaling.
    # We just stitch them here.
    
    # But wait, standardize_assets expects a strip.
    # Let's save a strip.
    
    first_w, first_h = extracted_images[0].size
    total_w = first_w * len(extracted_images)
    total_h = first_h
    
    sheet = Image.new("RGBA", (total_w, total_h))
    
    for i, img in enumerate(extracted_images):
        sheet.paste(img, (i * first_w, 0))
    
    # Output name convention
    # e.g., "wall_fire_anim.png"
    # Ensure "_anim" is in name so standardize script picks it up later
    out_name = basename
    if "_anim" not in out_name:
        out_name += "_anim"
    
    out_path = os.path.join(OUTPUT_DIR, out_name + ".png")
    sheet.save(out_path)
    print(f"Saved sprite sheet: {out_path}")
    print("Run 'python3 scripts/standardize_assets.py' to optimize it for the game.")

if __name__ == "__main__":
    ensure_dirs()
    print("Scanning for videos...")
    count = 0
    for f in os.listdir(INPUT_DIR):
        if f.lower().endswith(('.mp4', '.gif', '.mov', '.avi')):
            process_video(f)
            count += 1
            
    if count == 0:
        print("No videos found. Put files in raw_assets/videos")
