import os
import secrets
import json
from PIL import Image

SOURCE_DIR = "assets/images/mobs"
DEST_BASE = "assets/custom_images"
ELEMENTS_JSON = "assets/custom_elements/elements.json"

# Mobs Configuration
MOBS = [
    {
        "name": "Jungle Boar",
        "type": "ENEMY",
        "files": {
            "Move": "mob_boar_walk_right_4f.png",
            "MoveDown": "mob_boar_walk_down_4f.png",
            "MoveUp": "mob_boar_walk_up_4f.png",
            "Death": "mob_boar_walk_down_4f.png" 
        },
        "stats": {"health": 25, "damage": 5, "speed": 3}
    },
    {
        "name": "Sand Scorpion",
        "type": "ENEMY",
        "files": {
            "Move": "mob_scorpion_walk_right_4f.png",
            "MoveDown": "mob_scorpion_walk_down_4f.png",
            "MoveUp": "mob_scorpion_walk_up_4f.png",
            "Death": "mob_scorpion_walk_down_4f.png"
        },
        "stats": {"health": 15, "damage": 8, "speed": 2}
    }
]

def process_mob(mob):
    # Generate ID (8 chars hex)
    mob_id = secrets.token_hex(4)
    print(f"Processing {mob['name']} -> ID: {mob_id}")
    
    out_dir = os.path.join(DEST_BASE, mob_id)
    os.makedirs(out_dir, exist_ok=True)
    
    sprite_paths = {}
    
    for action, filename in mob['files'].items():
        src_path = os.path.join(SOURCE_DIR, filename)
        if not os.path.exists(src_path):
            print(f"Warning: {src_path} not found")
            continue
            
        try:
            img = Image.open(src_path)
            width, height = img.size
            # Assuming 4 frames horizontal strip
            frame_width = width // 4
            frame_height = height
            
            frame_paths = []
            for i in range(4):
                # Crop
                left = i * frame_width
                right = left + frame_width
                box = (left, 0, right, frame_height)
                frame = img.crop(box)
                
                # Naming convention: action_frame.png
                # JSON Logic often uses lowercase for file paths, but logic keys are CamelCase
                # We will save files as lowercase
                action_lower = action.lower()
                out_name = f"{action_lower}_{i}.png"
                out_path = os.path.join(out_dir, out_name)
                frame.save(out_path)
                
                # relative path for JSON
                rel_path = f"custom_images/{mob_id}/{out_name}"
                frame_paths.append(rel_path)
            
            # Use specific HashMap structure required by Java deserialization
            # The structure in JSON seems to be just a Map, but some entries had "class": "java.util.HashMap".
            # The JSON array format for lists is just ["path1", "path2"...]
            sprite_paths[action] = frame_paths
            
        except Exception as e:
            print(f"Error processing {src_path}: {e}")

    # Construct JSON Entry
    entry = {
        "id": mob_id,
        "name": mob['name'],
        "type": mob['type'],
        "frameCount": 4,
        "spritePaths": {
            "class": "java.util.HashMap",
            **sprite_paths
        },
        "properties": {
            "class": "java.util.HashMap",
            "health": {
                "class": "java.lang.Integer",
                "value": mob['stats']['health']
            },
            "attackDamage": {
                "class": "java.lang.Integer",
                "value": mob['stats']['damage']
            },
            "moveSpeed": {
                "class": "java.lang.Float",
                "value": float(mob['stats']['speed'])
            },
            "defense": {
                "class": "java.lang.Integer",
                "value": 0
            }
        },
        "assignedLevels": {
            "class": "java.util.HashSet",
            "items": [1, 2, 3, 4, 5, 20] 
        },
        "levelProbabilities": {
            "class": "java.util.HashMap",
            "1": 1, "2": 1, "3": 1, "4": 1, "5": 1
        },
        "spawnCount": 15
    }
    return entry

def main():
    if not os.path.exists(DEST_BASE):
        os.makedirs(DEST_BASE)
        
    new_entries = []
    for mob in MOBS:
        new_entries.append(process_mob(mob))
        
    # Read existing JSON
    if os.path.exists(ELEMENTS_JSON):
        with open(ELEMENTS_JSON, 'r') as f:
            try:
                data = json.load(f)
            except json.JSONDecodeError:
                data = []
    else:
        data = []
        
    # Append new entries
    data.extend(new_entries)
    
    # Write back
    with open(ELEMENTS_JSON, 'w') as f:
        json.dump(data, f, indent=4)
        
    print(f"Successfully added {len(new_entries)} mobs to elements.json")

if __name__ == "__main__":
    main()
