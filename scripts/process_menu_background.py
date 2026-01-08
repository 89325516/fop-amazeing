#!/usr/bin/env python3
"""
处理主菜单背景图片 - 激进裁剪版：
1. 更激进地裁剪四周留白
2. 裁剪底部水印区域
3. 保存处理后的图片
"""

from PIL import Image
import os

def get_content_bounds_aggressive(img, threshold=230, edge_sample_depth=50):
    """
    更激进地检测图片内容边界。
    使用边缘采样来判断留白区域。
    """
    if img.mode != 'RGB':
        img_rgb = img.convert('RGB')
    else:
        img_rgb = img
    
    pixels = img_rgb.load()
    width, height = img_rgb.size
    
    def is_whitish_pixel(r, g, b):
        """判断像素是否为浅色/留白"""
        return r >= threshold and g >= threshold and b >= threshold
    
    def is_edge_whitish(pixels, width, height, edge, position, depth=edge_sample_depth):
        """检查边缘的一定深度内是否大部分是留白"""
        whitish_count = 0
        total = 0
        
        if edge == 'left':
            x = position
            for y in range(0, height, 5):  # 采样
                r, g, b = pixels[x, y]
                if is_whitish_pixel(r, g, b):
                    whitish_count += 1
                total += 1
        elif edge == 'right':
            x = position
            for y in range(0, height, 5):
                r, g, b = pixels[x, y]
                if is_whitish_pixel(r, g, b):
                    whitish_count += 1
                total += 1
        elif edge == 'top':
            y = position
            for x in range(0, width, 5):
                r, g, b = pixels[x, y]
                if is_whitish_pixel(r, g, b):
                    whitish_count += 1
                total += 1
        elif edge == 'bottom':
            y = position
            for x in range(0, width, 5):
                r, g, b = pixels[x, y]
                if is_whitish_pixel(r, g, b):
                    whitish_count += 1
                total += 1
        
        # 如果超过80%是留白，认为这行/列是留白
        return (whitish_count / total) > 0.8 if total > 0 else False
    
    # 从左边开始找
    left = 0
    for x in range(width // 4):  # 最多搜索25%
        if not is_edge_whitish(pixels, width, height, 'left', x):
            left = x
            break
    
    # 从右边开始找
    right = width
    for x in range(width - 1, width * 3 // 4, -1):
        if not is_edge_whitish(pixels, width, height, 'right', x):
            right = x + 1
            break
    
    # 从上边开始找
    top = 0
    for y in range(height // 4):
        if not is_edge_whitish(pixels, width, height, 'top', y):
            top = y
            break
    
    # 从下边开始找
    bottom = height
    for y in range(height - 1, height * 3 // 4, -1):
        if not is_edge_whitish(pixels, width, height, 'bottom', y):
            bottom = y + 1
            break
    
    return left, top, right, bottom


def process_menu_background(input_path, output_path, extra_crop_pixels=15, bottom_crop_percent=10):
    """
    处理主菜单背景图片 - 激进版。
    
    Args:
        input_path: 输入图片路径
        output_path: 输出图片路径
        extra_crop_pixels: 额外向内裁剪的像素数
        bottom_crop_percent: 底部裁剪百分比（去除水印）
    """
    print(f"正在处理图片 (激进裁剪): {input_path}")
    
    # 打开图片
    img = Image.open(input_path)
    original_size = img.size
    print(f"原始尺寸: {original_size[0]} x {original_size[1]}")
    
    # 步骤1: 激进检测并裁剪留白
    left, top, right, bottom = get_content_bounds_aggressive(img, threshold=230)
    print(f"检测到内容边界: left={left}, top={top}, right={right}, bottom={bottom}")
    
    # 额外向内裁剪以确保没有留白残留
    left += extra_crop_pixels
    top += extra_crop_pixels
    right -= extra_crop_pixels
    bottom -= extra_crop_pixels
    
    print(f"额外裁剪后边界: left={left}, top={top}, right={right}, bottom={bottom}")
    
    # 裁剪掉留白
    img_cropped = img.crop((left, top, right, bottom))
    cropped_size = img_cropped.size
    print(f"去除留白后尺寸: {cropped_size[0]} x {cropped_size[1]}")
    
    # 步骤2: 裁剪底部区域（去除水印）
    bottom_pixels_to_crop = int(cropped_size[1] * bottom_crop_percent / 100)
    print(f"底部裁剪像素: {bottom_pixels_to_crop} ({bottom_crop_percent}%)")
    
    # 执行底部裁剪
    final_img = img_cropped.crop((0, 0, cropped_size[0], cropped_size[1] - bottom_pixels_to_crop))
    final_size = final_img.size
    print(f"最终尺寸: {final_size[0]} x {final_size[1]}")
    print(f"最终宽高比: {final_size[0] / final_size[1]:.2f}:1")
    
    # 确保输出目录存在
    os.makedirs(os.path.dirname(output_path), exist_ok=True)
    
    # 保存处理后的图片
    final_img.save(output_path, 'PNG')
    print(f"已保存到: {output_path}")
    
    return final_img


if __name__ == "__main__":
    input_path = "/Users/y.h/fopws2526projectfop-amazeing/raw_assets/ai_generated_raw/主页背景图.png"
    output_path = "/Users/y.h/fopws2526projectfop-amazeing/assets/images/menu_background.png"
    
    # 更激进的裁剪：额外向内裁剪20像素，底部裁剪10%
    process_menu_background(input_path, output_path, extra_crop_pixels=20, bottom_crop_percent=10)
    
    print("\n✅ 处理完成!")
