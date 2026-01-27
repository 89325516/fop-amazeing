from PIL import Image
import sys
import os

def stitch_2x2_to_1x4(input_path, output_path):
    print(f"Stitching {input_path} -> {output_path}")
    try:
        img = Image.open(input_path)
        width, height = img.size
        
        # Assume 2x2 grid
        cell_w = width // 2
        cell_h = height // 2
        
        # Extract 4 frames
        frames = []
        # Row 0
        frames.append(img.crop((0, 0, cell_w, cell_h)))
        frames.append(img.crop((cell_w, 0, width, cell_h)))
        # Row 1
        frames.append(img.crop((0, cell_h, cell_w, height)))
        frames.append(img.crop((cell_w, cell_h, width, height)))
        
        # Create new image (1x4)
        new_w = cell_w * 4
        new_h = cell_h
        result = Image.new("RGBA", (new_w, new_h))
        
        for i, frame in enumerate(frames):
            result.paste(frame, (i * cell_w, 0))
            
        result.save(output_path)
        print(f"Saved {output_path}")
        
    except Exception as e:
        print(f"Error processing {input_path}: {e}")

if __name__ == "__main__":
    if len(sys.argv) < 3:
        print("Usage: python3 stitch_2x2.py <input> <output>")
        sys.exit(1)
        
    stitch_2x2_to_1x4(sys.argv[1], sys.argv[2])
