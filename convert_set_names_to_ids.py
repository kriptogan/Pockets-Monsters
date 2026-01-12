import json
import os

# File paths
sets_data_path = r'd:\my projects\Pockets & Monsters\app\src\main\assets\sets_data.json'
skit_path = r'd:\my projects\Pockets & Monsters\app\src\main\assets\test_fetch\skit.txt'

# Load sets data
with open(sets_data_path, 'r', encoding='utf-8') as f:
    sets_data = json.load(f)

# Create a mapping from set name to set id
name_to_id = {}
for set_item in sets_data['data']:
    name = set_item['name']
    set_id = set_item['id']
    name_to_id[name] = set_id

# Add name variations for better matching
name_variations = {
    'Expedition': 'Expedition Base Set',
    'EX Dragon': 'Dragon',
    'EX FireRed and Leaf Green': 'FireRed & LeafGreen',
    'EX Crystal Guardians': 'Crystal Guardians',
    'EX Dragon Frontiers': 'Dragon Frontiers',
    'EX Power Keepers': 'Power Keepers',
    'Pokemon Detective Pikachu': 'Detective Pikachu',
    'Pokemon GO': 'Pokémon GO',
    '151': '151',  # This is already the name in JSON
}

print(f"Loaded {len(name_to_id)} sets from sets_data.json")

# Read skit.txt
with open(skit_path, 'r', encoding='utf-8') as f:
    lines = f.readlines()

result = []
not_found = []
for line in lines:
    line = line.strip()
    if not line:
        continue
    
    # Format: setName/X or setId/X
    if '/' in line:
        parts = line.split('/', 1)
        if len(parts) == 2:
            set_name = parts[0].strip()
            card_number = parts[1].strip()
            
            # Check if it's already an ID (starts with lowercase letters/numbers)
            if set_name in name_to_id.values():
                # Already an ID
                result.append(f"{set_name}/{card_number}\n")
                continue
            
            # Try direct lookup
            if set_name in name_to_id:
                set_id = name_to_id[set_name]
                result.append(f"{set_id}/{card_number}\n")
            # Try name variations
            elif set_name in name_variations:
                mapped_name = name_variations[set_name]
                if mapped_name in name_to_id:
                    set_id = name_to_id[mapped_name]
                    result.append(f"{set_id}/{card_number}\n")
                else:
                    not_found.append(set_name)
                    print(f"Set name not found (after variation): '{set_name}'")
            else:
                not_found.append(set_name)
                print(f"Set name not found: '{set_name}'")

print(f"\nFound {len(result)} matching sets")
print(f"Removed {len(not_found)} entries with unknown set names")
if not_found:
    print(f"Unknown set names: {set(not_found)}")

# Write back to file
with open(skit_path, 'w', encoding='utf-8') as f:
    f.writelines(result)

print(f"\nConverted file saved with {len(result)} entries")
if len(result) > 0:
    print(f"First 10 entries:")
    for entry in result[:10]:
        print(f"  {entry.strip()}")
