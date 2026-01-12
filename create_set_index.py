import os
import json
from pathlib import Path

# File paths
sets_data_path = r'd:\my projects\Pockets & Monsters\app\src\main\assets\sets_data.json'
images_dir = r'd:\my projects\Pockets & Monsters\app\src\main\assets\tcg_images\4'
index_file_path = os.path.join(images_dir, 'index.json')

# Load sets data
with open(sets_data_path, 'r', encoding='utf-8') as f:
    sets_data = json.load(f)

# Create a mapping from set id to set name
id_to_name = {}
for set_item in sets_data['data']:
    set_id = set_item['id']
    set_name = set_item['name']
    id_to_name[set_id] = set_name

print(f"Loaded {len(id_to_name)} sets from sets_data.json")

# Scan directory for PNG files and extract set IDs
set_ids_found = set()
for filename in os.listdir(images_dir):
    if filename.endswith('.png'):
        # Extract set ID (part before underscore)
        if '_' in filename:
            set_id = filename.split('_')[0]
            set_ids_found.add(set_id)

print(f"Found {len(set_ids_found)} unique set IDs in directory")

# Create index mapping set ID -> set name
index = {}
missing_ids = []
for set_id in sorted(set_ids_found):
    if set_id in id_to_name:
        index[set_id] = id_to_name[set_id]
    else:
        missing_ids.append(set_id)
        print(f"Warning: Set ID '{set_id}' not found in sets_data.json")

# Save index file
with open(index_file_path, 'w', encoding='utf-8') as f:
    json.dump(index, f, indent=2, ensure_ascii=False)

print(f"\nIndex file created: {index_file_path}")
print(f"Total entries: {len(index)}")
if missing_ids:
    print(f"Missing set IDs (not in index): {missing_ids}")
else:
    print("All set IDs found in sets_data.json")

# Print first few entries as preview
print(f"\nFirst 10 entries:")
for i, (set_id, set_name) in enumerate(list(index.items())[:10], 1):
    print(f"  {i}. {set_id} -> {set_name}")

