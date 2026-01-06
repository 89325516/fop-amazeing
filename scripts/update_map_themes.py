import os

MAPS_DIR = "assets/maps"

def get_theme(level_num):
    if 1 <= level_num <= 4:
        return "Grassland"
    elif 5 <= level_num <= 8:
        return "Desert"
    elif 9 <= level_num <= 12:
        return "Ice"
    elif 13 <= level_num <= 16:
        return "Jungle"
    elif 17 <= level_num <= 20:
        return "Space"
    return "Grassland"

def update_map_file(filename):
    filepath = os.path.join(MAPS_DIR, filename)
    if not os.path.exists(filepath):
        print(f"Skipping {filename}, does not exist.")
        return

    try:
        level_num = int(filename.replace("level-", "").replace(".properties", ""))
        new_theme = get_theme(level_num)
    except ValueError:
        print(f"Skipping {filename}, cannot parse level number.")
        return

    print(f"Updating {filename} to theme {new_theme}...")
    
    with open(filepath, 'r') as f:
        lines = f.readlines()

    new_lines = []
    theme_found = False
    
    # Check if lines start with comments or metadata
    # We want to insert/replace the theme line
    
    for line in lines:
        if line.strip().startswith("theme="):
            new_lines.append(f"theme={new_theme}\n")
            theme_found = True
        else:
            new_lines.append(line)
    
    if not theme_found:
        # Insert theme at the top, after comments if any
        inserted = False
        final_lines = []
        for line in new_lines:
            if not inserted and not line.strip().startswith("#"):
                 # Insert before the first non-comment line, or just at the top
                 final_lines.append(f"theme={new_theme}\n")
                 inserted = True
            final_lines.append(line)
        
        if not inserted: # Empty file or all comments
            final_lines.append(f"theme={new_theme}\n")
        
        new_lines = final_lines

    with open(filepath, 'w') as f:
        f.writelines(new_lines)

def main():
    if not os.path.exists(MAPS_DIR):
        print(f"Directory {MAPS_DIR} not found.")
        return

    files = os.listdir(MAPS_DIR)
    for f in files:
        if f.startswith("level-") and f.endswith(".properties"):
            update_map_file(f)

if __name__ == "__main__":
    main()
