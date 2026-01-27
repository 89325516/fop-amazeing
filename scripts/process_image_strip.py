#!/usr/bin/env python3
"""
Image Strip Processing Pipeline
图片条处理流水线

Processes a single image containing 4 animation frames arranged horizontally.
Supports guide line detection for precise alignment.

Usage:
    # Basic usage
    python3 scripts/process_image_strip.py \
        --input raw_assets/images/boar_walk_down.png \
        --frames 4 \
        --name mob_boar_walk_down
    
    # With guide line detection
    python3 scripts/process_image_strip.py \
        --input raw_assets/images/boar_walk_down.png \
        --frames 4 \
        --guide-color "#FF00FF" \
        --name mob_boar_walk_down
    
    # Custom output size
    python3 scripts/process_image_strip.py \
        --input raw_assets/images/boar_walk_down.png \
        --frames 4 \
        --output-size 64 \
        --name mob_boar_walk_down
"""

import os
import sys
import argparse
import re
from PIL import Image
import numpy as np
from pathlib import Path

# Configuration
DEFAULT_FRAME_SIZE = 64
OUTPUT_DIR = "raw_assets/ai_ready_optimized"
FINAL_ANIM_DIR = "assets/images/animations"
FINAL_ANIM_DIR = "assets/images/animations"
FINAL_MOB_DIR = "assets/images/mobs"
FINAL_WALL_DIR = "assets/images/walls"

# Default guide line color: Magenta
DEFAULT_GUIDE_COLOR = "#FF00FF"
GUIDE_COLOR_TOLERANCE = 60  # Increased from 30 for better detection

# Background removal settings - lower = more aggressive
BG_THRESHOLD = 180  # Was 200, lowered to catch gray backgrounds

# Pixelation defaults
DEFAULT_PIXEL_SIZE = 4  # Downscale factor for pixelation (higher = blockier)


def ensure_dirs():
    """Create necessary directories if they don't exist."""
    for d in [OUTPUT_DIR, FINAL_ANIM_DIR, FINAL_MOB_DIR, FINAL_WALL_DIR, "raw_assets/images"]:
        os.makedirs(d, exist_ok=True)


def hex_to_rgb(hex_color):
    """Convert hex color to RGB tuple."""
    hex_color = hex_color.lstrip('#')
    return tuple(int(hex_color[i:i+2], 16) for i in (0, 2, 4))


def is_guide_color(r, g, b, guide_rgb, tolerance=GUIDE_COLOR_TOLERANCE):
    """Check if a pixel matches the guide line color."""
    gr, gg, gb = guide_rgb
    return (abs(r - gr) < tolerance and 
            abs(g - gg) < tolerance and 
            abs(b - gb) < tolerance)


def is_white_or_light(r, g, b, threshold=BG_THRESHOLD):
    """Check if a pixel is considered white/light background."""
    c_max = max(r, g, b)
    c_min = min(r, g, b)
    delta = c_max - c_min
    
    saturation = 0
    if c_max > 0:
        saturation = delta / c_max
    
    is_grey_white = saturation < 0.15 and c_max > threshold
    return is_grey_white


def detect_guide_lines(img, guide_rgb):
    """
    Detect vertical guide lines in the image.
    Returns list of x-coordinates where vertical guide lines are found.
    """
    img_array = np.array(img.convert("RGBA"))
    height, width = img_array.shape[:2]
    
    vertical_lines = []
    
    # Scan each column
    for x in range(width):
        guide_pixel_count = 0
        for y in range(height):
            r, g, b = img_array[y, x, :3]
            if is_guide_color(r, g, b, guide_rgb):
                guide_pixel_count += 1
        
        # If most of the column is guide color, it's a vertical line
        if guide_pixel_count > height * 0.5:
            vertical_lines.append(x)
    
    # Cluster nearby x values
    if not vertical_lines:
        return []
    
    clustered = []
    current_cluster = [vertical_lines[0]]
    
    for x in vertical_lines[1:]:
        if x - current_cluster[-1] <= 3:
            current_cluster.append(x)
        else:
            clustered.append(sum(current_cluster) // len(current_cluster))
            current_cluster = [x]
    clustered.append(sum(current_cluster) // len(current_cluster))
    
    return clustered


def get_magenta_mask(img_array, strict=False):
    """
    Create a boolean mask identifying magenta/pink guide line pixels.
    
    Args:
        img_array: RGBA numpy array
        strict: If True, only detect pure #FF00FF. If False, also detect anti-aliased.
    """
    rgb = img_array[:, :, :3].astype(np.float32)
    r, g, b = rgb[:, :, 0], rgb[:, :, 1], rgb[:, :, 2]
    
    if strict:
        # Pure magenta only: R=255, G=0, B=255 (with tiny tolerance)
        is_pure_magenta = (r > 250) & (b > 250) & (g < 5)
        return is_pure_magenta
    else:
        # Detect pure magenta and its anti-aliased halo
        # Pure magenta: R high, B high, G very low
        is_pure_magenta = (r > 200) & (b > 200) & (g < 50)
        
        # Anti-aliased magenta: R and B still dominate, G slightly higher
        is_antialiased = (
            (r > 150) & (b > 150) & (g < 120) &
            (np.abs(r - b) < 40) &  # R and B similar
            ((r + b) / 2 > g * 1.5)  # R+B average much higher than G
        )
        
        # Dark magenta remnants
        is_dark_magenta = (
            (r > 80) & (b > 80) & (g < 60) &
            (np.abs(r - b) < 30) &
            (r > g * 1.3) & (b > g * 1.3)
        )
        
        return is_pure_magenta | is_antialiased | is_dark_magenta


def inpaint_guide_lines_horizontal(img, max_passes=10):
    """
    Inpaint magenta guide line pixels using HORIZONTAL neighbor colors.
    
    Algorithm:
    1. Find all magenta pixels
    2. For each magenta pixel, look LEFT and RIGHT for non-magenta pixels
    3. If both sides have opaque content: fill with average
    4. If one side is transparent: fill with the other side's color
    5. If both sides are transparent: make this pixel transparent
    
    This naturally handles:
    - Interior guide lines: filled with surrounding object colors
    - Exterior guide lines: become transparent (no neighbors)
    """
    img = img.convert("RGBA")
    
    total_inpainted = 0
    total_transparent = 0
    
    for pass_num in range(max_passes):
        img_array = np.array(img)
        height, width = img_array.shape[:2]
        
        # Get magenta mask (including anti-aliased)
        magenta_mask = get_magenta_mask(img_array, strict=False)
        alpha = img_array[:, :, 3]
        
        # Only process magenta pixels that are still opaque
        to_process = magenta_mask & (alpha > 0)
        ys, xs = np.where(to_process)
        
        if len(ys) == 0:
            break  # No more magenta pixels
        
        result = img_array.copy()
        inpainted_this_pass = 0
        transparent_this_pass = 0
        
        for y, x in zip(ys, xs):
            # Find LEFT neighbor (non-magenta, opaque, NOT magenta-colored)
            left_color = None
            for lx in range(x - 1, -1, -1):
                if alpha[y, lx] == 0:
                    break  # Hit transparency, stop
                if not magenta_mask[y, lx]:
                    # Double-check the color is not magenta-tinted
                    r, g, b = img_array[y, lx, :3]
                    if not (r > 150 and b > 150 and g < 100 and abs(int(r) - int(b)) < 50):
                        left_color = img_array[y, lx, :3].copy()
                        break
            
            # Find RIGHT neighbor (non-magenta, opaque, NOT magenta-colored)
            right_color = None
            for rx in range(x + 1, width):
                if alpha[y, rx] == 0:
                    break  # Hit transparency, stop
                if not magenta_mask[y, rx]:
                    # Double-check the color is not magenta-tinted
                    r, g, b = img_array[y, rx, :3]
                    if not (r > 150 and b > 150 and g < 100 and abs(int(r) - int(b)) < 50):
                        right_color = img_array[y, rx, :3].copy()
                        break
            
            # Decide how to fill
            if left_color is not None and right_color is not None:
                # Both sides have content - average
                avg_color = ((left_color.astype(np.int32) + right_color.astype(np.int32)) // 2).astype(np.uint8)
                result[y, x, :3] = avg_color
                result[y, x, 3] = 255
                inpainted_this_pass += 1
            elif left_color is not None:
                # Only left has content
                result[y, x, :3] = left_color
                result[y, x, 3] = 255
                inpainted_this_pass += 1
            elif right_color is not None:
                # Only right has content
                result[y, x, :3] = right_color
                result[y, x, 3] = 255
                inpainted_this_pass += 1
            else:
                # Neither side has content - make transparent
                result[y, x, 3] = 0
                transparent_this_pass += 1
        
        total_inpainted += inpainted_this_pass
        total_transparent += transparent_this_pass
        img = Image.fromarray(result)
        
        if inpainted_this_pass == 0 and transparent_this_pass == 0:
            break
    
    print(f"    Inpainted {total_inpainted} pixels, made {total_transparent} transparent")
    return img, total_inpainted + total_transparent


def inpaint_guide_lines(img, max_passes=5):
    """
    Legacy wrapper - now uses horizontal fill algorithm.
    """
    return inpaint_guide_lines_horizontal(img, max_passes)


def remove_guide_lines_hsv(img):
    """
    Legacy function for backward compatibility.
    Now just wraps inpaint_guide_lines.
    """
    return inpaint_guide_lines(img)


def remove_background(img, threshold=BG_THRESHOLD):
    """
    Remove background using flood fill from corners for cleaner results.
    Also removes isolated pixels (denoising).
    """
    img = img.convert("RGBA")
    width, height = img.size
    
    # 1. Flood fill from all 4 corners
    # This assumes the background touches the corners (standard for AI generations)
    from PIL import ImageDraw
    
    # Create a mask for flood filling
    mask = Image.new('L', (width + 2, height + 2), 0)
    
    # Convert image to grayscale for thresholding
    gray = img.convert("L")
    gray_data = np.array(gray)
    
    # Create binary mask of "light" pixels
    binary = gray_data > threshold
    
    # Pad binary mask to match flood fill mask size
    padded_binary = np.pad(binary, pad_width=1, mode='constant', constant_values=1)
    
    # Mask format for floodfill is tricky in PIL/OpenCV, let's stick to a simpler
    # approach: Flood fill on the alpha channel of the image directly
    
    # Simpler approach:
    # 1. Identify "background" color from corners
    # 2. Flood fill alpha=0 starting from corners if they match background color
    
    # Get corner colors
    corners = [(0, 0), (width-1, 0), (0, height-1), (width-1, height-1)]
    
    ImageDraw.floodfill(img, (0, 0), (0, 0, 0, 0), thresh=50)
    ImageDraw.floodfill(img, (width-1, 0), (0, 0, 0, 0), thresh=50)
    ImageDraw.floodfill(img, (0, height-1), (0, 0, 0, 0), thresh=50)
    ImageDraw.floodfill(img, (width-1, height-1), (0, 0, 0, 0), thresh=50)
    
    # 2. Aggressive threshold cleanup for remaining "white" pixels
    # This catches spots that flood fill missed (island gaps)
    pixels = img.load()
    for y in range(height):
        for x in range(width):
            r, g, b, a = pixels[x, y]
            if a > 0 and is_white_or_light(r, g, b, threshold):
                pixels[x, y] = (0, 0, 0, 0)
                
    return img

def remove_small_islands(img, min_size=10):
    """
    Remove small disconnected islands of pixels (noise) using simple BFS.
    No external dependencies required.
    """
    img_array = np.array(img)
    height, width = img_array.shape[:2]
    alpha = img_array[:, :, 3]
    
    # Binary mask: 1 = opaque, 0 = transparent
    mask = (alpha > 0).astype(np.int32)
    labeled = np.zeros_like(mask)
    current_label = 0
    
    # Find connected components
    component_sizes = {}
    
    # Directions: 8-connectivity (include diagonals)
    directions = [(-1, -1), (-1, 0), (-1, 1), 
                  (0, -1),           (0, 1), 
                  (1, -1),  (1, 0),  (1, 1)]
    
    for y in range(height):
        for x in range(width):
            if mask[y, x] == 1 and labeled[y, x] == 0:
                # Found a new component
                current_label += 1
                count = 0
                
                # BFS
                stack = [(y, x)]
                labeled[y, x] = current_label
                count += 1
                
                while stack:
                    cy, cx = stack.pop()
                    
                    for dy, dx in directions:
                        ny, nx = cy + dy, cx + dx
                        
                        if 0 <= ny < height and 0 <= nx < width:
                            if mask[ny, nx] == 1 and labeled[ny, nx] == 0:
                                labeled[ny, nx] = current_label
                                stack.append((ny, nx))
                                count += 1
                
                component_sizes[current_label] = count
    
    # Filter out small components
    for y in range(height):
        for x in range(width):
            label = labeled[y, x]
            if label > 0:
                if component_sizes[label] < min_size:
                    img_array[y, x, 3] = 0
                    
    return Image.fromarray(img_array)


def clean_artifacts(img, alpha_threshold=10, gray_threshold=50, edge_strip=0):
    """
    Final cleanup pass to remove:
    1. Edge strip artifacts (top/bottom rows often have junk)
    2. Near-transparent pixels (alpha < threshold) - causes dark halos
    3. Gray/dark edge pixels that are isolated
    
    Should be called AFTER background removal and guide line inpainting.
    """
    img = img.convert("RGBA")
    img_array = np.array(img)
    height, width = img_array.shape[:2]
    
    # Pass 0: Clean edge strips (top and bottom rows often have artifacts)
    edge_count = 0
    for y in range(edge_strip):
        for x in range(width):
            if img_array[y, x, 3] > 0:
                img_array[y, x, 3] = 0
                edge_count += 1
    for y in range(height - edge_strip, height):
        for x in range(width):
            if img_array[y, x, 3] > 0:
                img_array[y, x, 3] = 0
                edge_count += 1
    
    # Pass 1: Remove low-alpha pixels
    low_alpha_mask = img_array[:, :, 3] < alpha_threshold
    img_array[low_alpha_mask, 3] = 0
    low_alpha_count = np.sum(low_alpha_mask)
    
    # Pass 2: Remove isolated dark/gray pixels on edges
    # A pixel is "isolated" if most of its neighbors are transparent
    alpha = img_array[:, :, 3]
    gray_count = 0
    
    # Multiple passes for edge erosion
    for cleanup_pass in range(3):
        result = img_array.copy()
        pass_count = 0
        
        for y in range(height):
            for x in range(width):
                if result[y, x, 3] == 0:
                    continue
                
                r, g, b = result[y, x, :3]
                
                # Check if pixel is dark/gray
                brightness = (int(r) + int(g) + int(b)) / 3
                is_dark = brightness < gray_threshold
                is_neutral = abs(int(r) - int(g)) < 30 and abs(int(g) - int(b)) < 30
                
                if is_dark and is_neutral:
                    # Count transparent neighbors
                    transparent_neighbors = 0
                    total_neighbors = 0
                    
                    for dy in [-1, 0, 1]:
                        for dx in [-1, 0, 1]:
                            if dy == 0 and dx == 0:
                                continue
                            ny, nx = y + dy, x + dx
                            if 0 <= ny < height and 0 <= nx < width:
                                total_neighbors += 1
                                if result[ny, nx, 3] == 0:
                                    transparent_neighbors += 1
                    
                    # If any neighbor is transparent and pixel is dark, remove it
                    if total_neighbors > 0 and transparent_neighbors >= 1:
                        result[y, x, 3] = 0
                        pass_count += 1
        
        gray_count += pass_count
        img_array = result
        
        if pass_count == 0:
            break
    
    print(f"    Cleaned {edge_count} edge, {low_alpha_count} low-alpha, {gray_count} gray pixels")
    
    return Image.fromarray(img_array)


def remove_black_grid_lines(img):
    """
    Detect and remove vertical black/dark grid lines from the image.
    These often appear as separators in sprite sheets.
    """
    img_array = np.array(img.convert("RGBA"))
    height, width = img_array.shape[:2]
    
    # Check for vertical black lines
    for x in range(width):
        is_black_line = True
        dark_pixels = 0
        
        for y in range(height):
            r, g, b, a = img_array[y, x]
            if a == 0:
                continue
                
            # Check if pixel is dark (grid line)
            is_dark = (r < 50 and g < 50 and b < 50)
            if not is_dark:
                # If we find a non-dark pixel, it's not a full grid line
                # But sometimes grid lines are dashed or have artifacts
                # Let's count dark pixels
                pass
            else:
                dark_pixels += 1
                
        # If > 80% of opaque pixels in this column are dark, treat as grid line
        opaque_pixel_count = np.sum(img_array[:, x, 3] > 0)
        
        if opaque_pixel_count > height * 0.5 and dark_pixels > opaque_pixel_count * 0.8:
            # It's a black grid line - clear it
            for y in range(height):
                img_array[y, x] = (0, 0, 0, 0)
                
    return Image.fromarray(img_array)

def standardize_frames_unified(frames, target_size, mode='content', align='center', entity_type='stationary', alignment_strategy='independent'):
    """
    Standardize frames with flexible alignment strategies.
    
    Args:
        alignment_strategy: 
            - 'independent': Each frame cropped/centered individually (good for wild size changes)
            - 'unified': All frames share the same bounding box relative to the original image (good for stability)
    """
    # Get original frame size
    orig_w, orig_h = frames[0].size
    
    # Collect all bounding boxes
    all_bboxes = []
    for frame in frames:
        bbox = frame.getbbox()
        if bbox:
            all_bboxes.append(bbox)
        else:
            all_bboxes.append(None)
    
    if not any(all_bboxes):
        return [Image.new("RGBA", (target_size, target_size), (0, 0, 0, 0)) for _ in frames]
    
    processed = []
    
    if mode == 'canvas':
        # ... (Canvas mode logic same as before) ...
        pass # Placeholder to keep logic simple if we were copying full function, but here we rewrite:
        # Re-implementing canvas logic for completeness to avoid copy-paste errors
        scale = min(target_size / orig_w, target_size / orig_h)
        for frame in frames:
            w, h = frame.size
            final_w = int(w * scale)
            final_h = int(h * scale)
            if final_w <= 0 or final_h <= 0:
                processed.append(Image.new("RGBA", (target_size, target_size), (0, 0, 0, 0)))
                continue
            resized = frame.resize((final_w, final_h), Image.LANCZOS)
            canvas = Image.new("RGBA", (target_size, target_size), (0, 0, 0, 0))
            off_x = (target_size - final_w) // 2
            if align == 'bottom': off_y = target_size - final_h
            elif align == 'top': off_y = 0
            else: off_y = (target_size - final_h) // 2
            canvas.paste(resized, (off_x, off_y))
            processed.append(canvas)

    elif alignment_strategy == 'unified' or entity_type == 'stationary':
        # ========== UNIFIED / STATIONARY MODE ==========
        # Use UNIFIED bounding box to preserve relative positions
        
        valid_bboxes = [b for b in all_bboxes if b is not None]
        union_x1 = min(b[0] for b in valid_bboxes)
        union_y1 = min(b[1] for b in valid_bboxes)
        union_x2 = max(b[2] for b in valid_bboxes)
        union_y2 = max(b[3] for b in valid_bboxes)
        
        union_width = union_x2 - union_x1
        union_height = union_y2 - union_y1
        
        # Calculate scale to fit in target_size
        scale = min(target_size / union_width, target_size / union_height) * 0.90  # 90% to leave margin
        unified_scaled_w = int(union_width * scale)
        unified_scaled_h = int(union_height * scale)
        
        for i, frame in enumerate(frames):
            if all_bboxes[i] is None:
                processed.append(Image.new("RGBA", (target_size, target_size), (0, 0, 0, 0)))
                continue
            
            # Crop to the UNIFIED bounding box
            cropped = frame.crop((union_x1, union_y1, union_x2, union_y2))
            
            if unified_scaled_w <= 0 or unified_scaled_h <= 0:
                processed.append(Image.new("RGBA", (target_size, target_size), (0, 0, 0, 0)))
                continue
            
            resized = cropped.resize((unified_scaled_w, unified_scaled_h), Image.LANCZOS)
            
            # Apply alignment
            canvas = Image.new("RGBA", (target_size, target_size), (0, 0, 0, 0))
            x_offset = (target_size - unified_scaled_w) // 2
            
            if align == 'bottom':
                y_offset = target_size - unified_scaled_h
            elif align == 'top':
                y_offset = 0
            else:  # center
                y_offset = (target_size - unified_scaled_h) // 2
            
            canvas.paste(resized, (x_offset, y_offset))
            processed.append(canvas)
    
    else:
        # ========== INDEPENDENT / MOBILE MODE ==========
        # Each frame cropped and centered INDEPENDENTLY
        
        # First pass: find the MAX content size across all frames for uniform scaling
        max_content_w = 0
        max_content_h = 0
        for bbox in all_bboxes:
            if bbox:
                max_content_w = max(max_content_w, bbox[2] - bbox[0])
                max_content_h = max(max_content_h, bbox[3] - bbox[1])
        
        scale = min(target_size / max_content_w, target_size / max_content_h) * 0.90
        
        for i, frame in enumerate(frames):
            bbox = all_bboxes[i]
            if bbox is None:
                processed.append(Image.new("RGBA", (target_size, target_size), (0, 0, 0, 0)))
                continue
            
            cropped = frame.crop(bbox)
            content_w, content_h = cropped.size
            
            scaled_w = int(content_w * scale)
            scaled_h = int(content_h * scale)
            
            if scaled_w <= 0 or scaled_h <= 0:
                processed.append(Image.new("RGBA", (target_size, target_size), (0, 0, 0, 0)))
                continue
            
            resized = cropped.resize((scaled_w, scaled_h), Image.LANCZOS)
            
            canvas = Image.new("RGBA", (target_size, target_size), (0, 0, 0, 0))
            x_offset = (target_size - scaled_w) // 2
            y_offset = (target_size - scaled_h) // 2
            
            canvas.paste(resized, (x_offset, y_offset))
            processed.append(canvas)
    
    return processed


def split_strip_by_guides(img, guide_lines, num_frames):
    """
    Split image strip using detected guide lines.
    Falls back to equal division if guides not detected.
    """
    width, height = img.size
    
    # Filter out edge guide lines (within 5% of edges)
    edge_margin = int(width * 0.05)
    interior_guides = [x for x in guide_lines if edge_margin < x < width - edge_margin]
    
    print(f"  Interior guides (excluding edges): {interior_guides}")
    
    # We need num_frames-1 interior guide lines to split into num_frames
    if len(interior_guides) >= num_frames - 1:
        # Use the first num_frames-1 interior guides as dividers
        dividers = sorted(interior_guides[:num_frames - 1])
        boundaries = [0] + dividers + [width]
        print(f"  Using guide lines for splitting: {boundaries}")
    else:
        # Fall back to equal division
        frame_width = width // num_frames
        boundaries = [i * frame_width for i in range(num_frames + 1)]
        print(f"  Using equal division (no guides): {boundaries}")
    
    frames = []
    for i in range(num_frames):
        x1 = boundaries[i]
        x2 = boundaries[i + 1]
        frame = img.crop((x1, 0, x2, height))
        frames.append(frame)
    
    return frames


def split_strip_equal(img, num_frames):
    """Split image strip into equal-sized frames."""
    width, height = img.size
    frame_width = width // num_frames
    
    frames = []
    for i in range(num_frames):
        x1 = i * frame_width
        x2 = x1 + frame_width
        frame = img.crop((x1, 0, x2, height))
        frames.append(frame)
    
    return frames


def pixelate_frame(frame, factor):
    """
    Apply pixel art stylization by downscaling and upscaling with nearest-neighbor.
    
    Args:
        frame: PIL Image (RGBA)
        factor: How much to pixelate (higher = blockier). E.g., 4 means 64px -> 16px -> 64px.
    
    Returns:
        Pixelated PIL Image
    """
    w, h = frame.size
    small_w = max(1, w // factor)
    small_h = max(1, h // factor)
    
    # Downscale using NEAREST (no averaging)
    small = frame.resize((small_w, small_h), Image.NEAREST)
    # Upscale back using NEAREST to get blocky pixels
    pixelated = small.resize((w, h), Image.NEAREST)
    
    return pixelated


def process_image_strip(input_path, num_frames, guide_color, target_size, name, scale_mode='content', align='bottom', entity_type='stationary', pixelate_factor=None, min_island_size=10, gray_threshold=50, alignment_strategy='independent'):
    """
    Process a 4-frame image strip into a game-ready sprite sheet.
    
    Args:
        input_path: Path to input image strip
        num_frames: Number of frames in the strip
        guide_color: Hex color of guide lines (or None to skip)
        target_size: Target frame size in pixels
        name: Base name for output files
        scale_mode: 'content' or 'canvas'
        align: 'center', 'bottom', or 'top' - vertical alignment of content
        entity_type: 'stationary' (traps, effects) or 'mobile' (enemies, NPCs)
        pixelate_factor: If set, apply pixel art stylization with this factor
        min_island_size: Minimum size for island removal (speckle cleaning)
        min_island_size: Minimum size for island removal (speckle cleaning)
        gray_threshold: Threshold for cleaning gray artifacts
        alignment_strategy: 'independent' (center each frame) or 'unified' (preserve relative positions)
    
    Returns:
        Output file path
    """
    print(f"\nLoading: {input_path}")
    img = Image.open(input_path).convert("RGBA")
    print(f"  Image size: {img.size}")
    
    # Detect and remove guide lines using HSV color space
    guide_rgb = None
    guide_lines = []
    
    if guide_color:
        try:
            guide_rgb = hex_to_rgb(guide_color)
            print(f"  Guide color: {guide_color} → RGB{guide_rgb}")
            
            # Detect vertical guide lines (for splitting reference BEFORE any processing)
            guide_lines = detect_guide_lines(img, guide_rgb)
            print(f"  Detected {len(guide_lines)} vertical guide lines at x={guide_lines}")
        except Exception as e:
            print(f"  Warning: Guide detection failed: {e}")
    
    # Split into frames FIRST (before any pixel manipulation)
    
    # 0. Remove black grid lines if present (common in sprite sheets)
    img = remove_black_grid_lines(img)
    
    if guide_lines:
        frames = split_strip_by_guides(img, guide_lines, num_frames)
    else:
        frames = split_strip_equal(img, num_frames)
    
    print(f"  Split into {len(frames)} frames")
    
    # Process each frame:
    # 1. Remove background FIRST (white becomes transparent)
    # 2. THEN inpaint guide lines (fill with neighbors or make transparent)
    # 3. FINALLY clean up artifacts (low-alpha, gray edges)
    processed_frames_step1 = []
    for i, frame in enumerate(frames):
        # Step 1: Remove white background
        frame = remove_background(frame)
        
        # Step 2: Inpaint guide lines (only if guide_color was specified)
        if guide_color:
            frame, count = inpaint_guide_lines(frame)
        
        # Step 2.5: Remove small islands (de-speckle)
        frame = remove_small_islands(frame, min_size=min_island_size)

        # Step 3: Clean artifacts (always run)
        frame = clean_artifacts(frame, gray_threshold=gray_threshold)
        
        processed_frames_step1.append(frame)
    
    # Standardize ALL frames with appropriate strategy
    processed_frames = standardize_frames_unified(processed_frames_step1, target_size, scale_mode, align, entity_type, alignment_strategy)
    
    # Report content coverage
    for i, frame in enumerate(processed_frames):
        bbox = frame.getbbox()
        if bbox:
            content_w = bbox[2] - bbox[0]
            content_h = bbox[3] - bbox[1]
            coverage = (content_w * content_h) / (target_size * target_size) * 100
        else:
            coverage = 0
        print(f"    Frame {i+1}: content coverage = {coverage:.1f}%")
    
    # Optional pixelation pass
    if pixelate_factor is not None and pixelate_factor > 1:
        print(f"  Applying pixel art stylization (factor={pixelate_factor})...")
        processed_frames = [pixelate_frame(f, pixelate_factor) for f in processed_frames]
    
    # Create output sprite sheet
    strip_width = target_size * num_frames
    strip_height = target_size
    output = Image.new("RGBA", (strip_width, strip_height), (0, 0, 0, 0))
    
    for i, frame in enumerate(processed_frames):
        output.paste(frame, (i * target_size, 0))
    
    # Save output
    output_name = f"{name}_{num_frames}f.png"
    output_path = os.path.join(OUTPUT_DIR, output_name)
    output.save(output_path)
    print(f"  ✅ Saved: {output_path}")
    
    return output_path, output_name


def automatic_config_from_name(name, entity_type_arg, align_arg, scale_mode_arg):
    """
    Automatically deduce configuration based on file/asset name.
    
    Rules:
    - 'stationary' (align=bottom, mode=content): trap, wall, decoration, effect, static, item
    - 'mobile' (align=center, mode=content): mob, enemy, player, character, npc, boss
    """
    name_lower = name.lower()
    
    # 1. Deduce Entity Type
    if entity_type_arg:
        entity_type = entity_type_arg
    else:
        if any(k in name_lower for k in ['trap', 'wall', 'decor', 'effect', 'static', 'item', 'bush', 'tree', 'rock']):
            entity_type = 'stationary'
        else:
            entity_type = 'mobile'  # Default to mobile (safest for characters)
    
    # 2. Deduce Alignment
    if align_arg:
        align = align_arg
    else:
        if entity_type == 'stationary':
            align = 'bottom'
        else:
            align = 'center'
            
    # 3. Deduce Scale Mode
    if scale_mode_arg:
        scale_mode = scale_mode_arg
    else:
        # Default to 'content' for almost everything unless specifically told 'canvas'
        scale_mode = 'content'
        
    print(f"  🤖 Auto-Config: Type={entity_type}, Align={align}, Mode={scale_mode}")
    return entity_type, align, scale_mode


def main():
    parser = argparse.ArgumentParser(
        description='Process image strips containing animation frames',
        formatter_class=argparse.RawDescriptionHelpFormatter,
        epilog=__doc__
    )
    parser.add_argument('--input', '-i', required=True,
                        help='Input image strip file')
    parser.add_argument('--frames', '-f', type=int, default=4,
                        help='Number of frames per row (default: 4)')
    parser.add_argument('--rows', '-r', type=int, default=1,
                        help='Number of rows (default: 1, for grid layouts use 2+)')
    parser.add_argument('--row-names', 
                        help='Comma-separated names for each row (e.g., "walk_down,walk_up")')
    parser.add_argument('--guide-color', '-g', default=None,
                        help=f'Hex color of guide lines (default: None, e.g., "#FF00FF")')
    parser.add_argument('--scale-mode', default=None, choices=['content', 'canvas'],
                        help='Scaling strategy (default: auto, usually "content")')
    parser.add_argument('--type', '-t', dest='entity_type', default=None, choices=['stationary', 'mobile'],
                        help='Entity type (default: auto-detected from name)')
    parser.add_argument('--align', '-a', default=None, choices=['center', 'bottom', 'top'],
                        help='Vertical alignment (default: auto-detected from type)')
    parser.add_argument('--output-size', '-s', type=int, default=DEFAULT_FRAME_SIZE,
                        help=f'Target frame size (default: {DEFAULT_FRAME_SIZE})')
    parser.add_argument('--name', '-n', required=True,
                        help='Base name for output files')
    parser.add_argument('--no-copy', action='store_true',
                        help='Do not copy to final asset directories')
    parser.add_argument('--pixelate', '-p', nargs='?', const=DEFAULT_PIXEL_SIZE, type=int, default=None,
                        help=f'Apply pixel art stylization (optional factor, default: {DEFAULT_PIXEL_SIZE}). Higher = blockier.')
    parser.add_argument('--min-island-size', type=int, default=20,
                        help='Minimum size of isolated pixel islands to keep (default: 20). Reduce for delicate details.')
    parser.add_argument('--gray-threshold', type=int, default=50,
                        help='Brightness threshold for gray artifact cleanup (default: 50). Reduce to 0 to keep gray objects.')
    parser.add_argument('--alignment-strategy', default='independent', choices=['independent', 'unified'],
                        help='Alignment strategy: "independent" (center each frame) or "unified" (preserve relative positions, good for avoiding jitter)')
    
    args = parser.parse_args()
    
    print("=" * 60)
    print("Image Strip Processing Pipeline v1.3 (Auto-Config)")
    print("=" * 60)
    
    # Apply automatic configuration
    args.entity_type, args.align, args.scale_mode = automatic_config_from_name(
        args.name, args.entity_type, args.align, args.scale_mode
    )
    
    print(f"Input: {args.input}")
    print(f"Frames per row: {args.frames}")
    print(f"Rows: {args.rows}")
    print(f"Entity type: {args.entity_type}")
    print(f"Guide color: {args.guide_color or 'None (equal division)'}")
    print(f"Scale mode: {args.scale_mode}")
    print(f"Align: {args.align}")
    print(f"Output size: {args.output_size}×{args.output_size}")
    print(f"Name: {args.name}")
    print("=" * 60)
    
    if not os.path.exists(args.input):
        print(f"Error: Input file not found: {args.input}")
        return 1
    
    ensure_dirs()
    
    # Parse row names
    row_names = None
    if args.row_names:
        row_names = [n.strip() for n in args.row_names.split(',')]
    
    # Load image
    img = Image.open(args.input).convert("RGBA")
    width, height = img.size
    print(f"\nImage size: {width}×{height}")
    
    # ⚠️ Multi-row detection warning
    # For single-row 4-frame strips, expected aspect ratio is 512:128 = 4:1
    # If height > width/3, it's likely a multi-row grid image
    if args.rows == 1:  # Only warn for single-row mode
        aspect_ratio = width / height if height > 0 else float('inf')
        if aspect_ratio < 3.0:
            print("\n" + "=" * 60)
            print("⚠️  WARNING: POSSIBLE MULTI-ROW IMAGE DETECTED! ⚠️")
            print("=" * 60)
            print(f"  Image aspect ratio: {aspect_ratio:.2f}:1 (expected 4:1 for single row)")
            print(f"  This looks like a {int(4 / aspect_ratio)}-row grid, not a single row!")
            print()
            print("  COMMON CAUSE: External AI generated 8 frames (2 rows × 4 columns)")
            print("                instead of 4 frames (1 row × 4 columns).")
            print()
            print("  SOLUTIONS:")
            print("    1. Use --rows 2 to process as multi-row grid")
            print("    2. Regenerate image with stricter Prompt:")
            print("       '512×128 pixels, SINGLE ROW ONLY, 4 frames only'")
            print("    3. Manually crop to keep only one row")
            print("=" * 60 + "\n")
            
            # Ask for confirmation only in interactive mode (if not --no-copy)
            if not args.no_copy:
                print("  Proceeding anyway... (output may be incorrect)")
                print()
    
    # Handle multi-row grid
    if args.rows > 1:
        row_height = height // args.rows
        print(f"Splitting into {args.rows} rows, each {row_height}px tall")
        
        generated_files = []
        
        for row_idx in range(args.rows):
            # Extract this row
            y1 = row_idx * row_height
            y2 = y1 + row_height
            row_img = img.crop((0, y1, width, y2))
            
            # Determine row name
            if row_names and row_idx < len(row_names):
                row_suffix = row_names[row_idx]
            else:
                row_suffix = f"row{row_idx}"
            
            row_name = f"{args.name}_{row_suffix}"
            print(f"\n--- Processing Row {row_idx}: {row_suffix} ---")
            
            # Save temp row image
            temp_path = f"/tmp/row_{row_idx}.png"
            row_img.save(temp_path)
            
            # Process this row
            output_path, output_name = process_image_strip(
                temp_path, args.frames, args.guide_color,
                args.output_size, row_name, args.scale_mode, args.align, args.entity_type, args.pixelate,
                args.min_island_size, args.gray_threshold, args.alignment_strategy
            )
            generated_files.append((row_suffix, output_path, output_name))
        
        # Copy all to final directory
        if not args.no_copy:
            import shutil
            name_lower = args.name.lower()
            is_wall = 'wall' in name_lower
            is_mob = any(x in name_lower for x in ['mob_', 'enemy_', 'walk_', 'attack_', 'boss'])
            
            if is_wall:
                target_dir = FINAL_WALL_DIR
            elif is_mob:
                target_dir = FINAL_MOB_DIR 
            else:
                target_dir = FINAL_ANIM_DIR
            
            for row_suffix, output_path, output_name in generated_files:
                dst = os.path.join(target_dir, output_name)
                shutil.copy(output_path, dst)
                print(f"✅ {row_suffix}: {dst}")
    else:
        # Single row - original behavior
        output_path, output_name = process_image_strip(
            args.input, args.frames, args.guide_color,
            args.output_size, args.name, args.scale_mode, args.align, args.entity_type, args.pixelate,
            args.min_island_size, args.gray_threshold, args.alignment_strategy
        )
        
        # Copy to final directory
        if not args.no_copy:
            import shutil
            name_lower = args.name.lower()
            is_wall = 'wall' in name_lower
            is_mob = any(x in name_lower for x in ['mob_', 'enemy_', 'walk_', 'attack_', 'boss'])
            
            if is_wall:
                target_dir = FINAL_WALL_DIR
            elif is_mob:
                target_dir = FINAL_MOB_DIR 
            else:
                target_dir = FINAL_ANIM_DIR
            
            dst = os.path.join(target_dir, output_name)
            shutil.copy(output_path, dst)
            print(f"\n✅ Copied to: {dst}")
    
    print("\n" + "=" * 60)
    print("Processing complete!")
    print("=" * 60)
    
    return 0


if __name__ == "__main__":
    sys.exit(main())
