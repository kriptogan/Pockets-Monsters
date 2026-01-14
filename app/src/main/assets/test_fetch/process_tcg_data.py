import os
import re
import json
import requests
from pathlib import Path

# File paths
base_dir = r'd:\my projects\Pockets & Monsters'
skit_path = os.path.join(base_dir, 'app/src/main/assets/test_fetch/skit.txt')
sets_data_path = os.path.join(base_dir, 'app/src/main/assets/sets_data.json')
tcg_images_base = os.path.join(base_dir, 'app/src/main/assets/tcg_images')

# Step 1: Get Pokémon ID from user
pokemon_id = input("Enter the Pokémon ID number (e.g., 4 for Charizard): ").strip()
if not pokemon_id.isdigit():
    print("Error: Invalid Pokémon ID. Must be a number.")
    exit(1)

pokemon_id = int(pokemon_id)
image_dir = os.path.join(tcg_images_base, str(pokemon_id))
os.makedirs(image_dir, exist_ok=True)

print(f"\nProcessing TCG data for Pokémon ID: {pokemon_id}")
print(f"Output directory: {image_dir}\n")

# Step 2: Load sets data
print("Step 1: Loading sets data...")
with open(sets_data_path, 'r', encoding='utf-8') as f:
    sets_data = json.load(f)

# Create mappings
name_to_id = {}
id_to_name = {}
for set_item in sets_data['data']:
    name = set_item['name']
    set_id = set_item['id']
    name_to_id[name] = set_id
    id_to_name[set_id] = name

# Name variations for better matching
name_variations = {
    'Expedition': 'Expedition Base Set',
    'EX Dragon': 'Dragon',
    'EX FireRed and Leaf Green': 'FireRed & LeafGreen',
    'EX Crystal Guardians': 'Crystal Guardians',
    'EX Dragon Frontiers': 'Dragon Frontiers',
    'EX Power Keepers': 'Power Keepers',
    'Pokemon Detective Pikachu': 'Detective Pikachu',
    'Pokemon GO': 'Pokémon GO',
    '151': '151',
}

print(f"Loaded {len(name_to_id)} sets from sets_data.json")

# Step 3: Read skit.txt and check format
print("\nStep 2: Reading skit.txt...")
with open(skit_path, 'r', encoding='utf-8') as f:
    lines = f.readlines()

# Check if file is already in processed format (setId/cardNumber)
first_line = lines[0].strip() if lines else ""
is_processed = '/' in first_line and not '\t' in first_line and not 'HP' in first_line

if is_processed:
    print("File appears to be already processed (setId/cardNumber format)")
    print("Skipping cleaning and transformation steps...")
    set_card_pairs = []
    for line in lines:
        line = line.strip()
        if line and '/' in line:
            parts = line.split('/', 1)
            if len(parts) == 2:
                set_id_or_name = parts[0].strip()
                card_number = parts[1].strip()
                # Check if it's already an ID (lowercase/numbers) or a name
                if set_id_or_name in id_to_name:
                    # Already an ID
                    set_card_pairs.append((set_id_or_name, card_number))
                else:
                    # It's a name, we'll convert it in the next step
                    set_card_pairs.append((set_id_or_name, card_number))
    print(f"Found {len(set_card_pairs)} entries in processed format")
else:
    print("File appears to be in raw format, processing...")
    
    # Clean: Remove HP, Weakness, Resistance, Retreat Cost lines
    cleaned_lines = []
    for line in lines:
        stripped = line.strip()
        if not stripped:
            continue
        # Skip lines with HP
        if 'HP' in stripped:
            continue
        # Skip the header line
        if 'Weakness' in stripped and 'Resistance' in stripped and 'Retreat Cost' in stripped:
            continue
        # Skip lines that are just weakness/resistance/retreat cost values
        if re.match(r'^[\s\t]*([-+]?\d+|x\d+|\s)*[\s\t]*$', stripped):
            continue
        cleaned_lines.append(line)
    
    print(f"Cleaned file: removed {len(lines) - len(cleaned_lines)} lines, kept {len(cleaned_lines)} lines")
    
    # Transform to setName/cardNumber format
    print("\nStep 3: Transforming to setName/cardNumber format...")
    set_card_pairs = []
    i = 0
    while i < len(cleaned_lines):
        line = cleaned_lines[i]
        stripped = line.strip()
        
        if not stripped:
            i += 1
            continue
        
        # Check if this line is a card number (starts with a digit, contains "/")
        if re.match(r'^\d+', stripped) and '/' in stripped:
            # Extract the X value (first number before "/")
            match = re.search(r'^(\d+)', stripped)
            if match:
                x_value = match.group(1)
                
                # Look back for the set name (previous non-empty line)
                j = i - 1
                while j >= 0 and not cleaned_lines[j].strip():
                    j -= 1
                
                if j >= 0:
                    prev_line = cleaned_lines[j]
                    # Split by tab - the last non-empty part should be the set name
                    tab_parts = prev_line.split('\t')
                    non_empty_parts = [p.strip() for p in tab_parts if p.strip()]
                    
                    if len(non_empty_parts) >= 2:
                        set_name = non_empty_parts[-1]
                        set_card_pairs.append((set_name, x_value))
                    elif len(non_empty_parts) == 1:
                        if not re.match(r'^\d+', non_empty_parts[0]):
                            set_name = non_empty_parts[0]
                            set_card_pairs.append((set_name, x_value))
        
        i += 1
    
    print(f"Extracted {len(set_card_pairs)} set/card pairs")

# Step 5: Convert set names to IDs
print("\nStep 4: Converting set names to IDs...")
set_id_card_pairs = []
not_found = []
ignored = []

# Find Celebrations set ID(s)
celebration_ids = {set_id for set_id, name in id_to_name.items() if 'Celebration' in name}
celebration_names = {'Celebrations', 'Celebration'}

for set_name_or_id, card_number in set_card_pairs:
    # Skip Celebrations set
    if set_name_or_id in celebration_ids or set_name_or_id in celebration_names:
        ignored.append((set_name_or_id, card_number))
        continue
    
    # Check if it's already an ID
    if set_name_or_id in id_to_name:
        # Already an ID, use it directly
        set_id_card_pairs.append((set_name_or_id, card_number))
    # Try direct name lookup
    elif set_name_or_id in name_to_id:
        set_id = name_to_id[set_name_or_id]
        # Double-check it's not Celebrations
        if set_id not in celebration_ids:
            set_id_card_pairs.append((set_id, card_number))
        else:
            ignored.append((set_name_or_id, card_number))
    # Try name variations
    elif set_name_or_id in name_variations:
        mapped_name = name_variations[set_name_or_id]
        if mapped_name in name_to_id:
            set_id = name_to_id[mapped_name]
            # Double-check it's not Celebrations
            if set_id not in celebration_ids:
                set_id_card_pairs.append((set_id, card_number))
            else:
                ignored.append((set_name_or_id, card_number))
        else:
            not_found.append(set_name_or_id)
    else:
        not_found.append(set_name_or_id)

print(f"Converted {len(set_id_card_pairs)} entries to set IDs")
if ignored:
    print(f"Ignored {len(ignored)} entries from Celebrations set")
if not_found:
    print(f"Removed {len(not_found)} entries with unknown set names: {set(not_found)}")

# Step 6: Save the processed data to skit.txt
print("\nStep 5: Saving processed data to skit.txt...")
with open(skit_path, 'w', encoding='utf-8') as f:
    for set_id, card_number in set_id_card_pairs:
        f.write(f"{set_id}/{card_number}\n")

print(f"Saved {len(set_id_card_pairs)} entries to skit.txt")

# Step 7: Create index.json
print("\nStep 6: Creating index.json...")
set_ids_found = set(set_id for set_id, _ in set_id_card_pairs)
index = {}
for set_id in sorted(set_ids_found):
    if set_id in id_to_name:
        index[set_id] = id_to_name[set_id]

index_path = os.path.join(image_dir, 'index.json')
with open(index_path, 'w', encoding='utf-8') as f:
    json.dump(index, f, indent=2, ensure_ascii=False)

print(f"Created index.json with {len(index)} set entries")

# Step 8: Create cards.json with image URLs
print("\nStep 7: Creating cards.json with image URLs...")
print(f"Total cards to process: {len(set_id_card_pairs)}\n")

cards_data = {}
total = len(set_id_card_pairs)

for i, (set_id, card_number) in enumerate(set_id_card_pairs, 1):
    # Construct URL: images.pokemontcg.io/[baseCode]/[cardIndex]_hires.png
    url = f"https://images.pokemontcg.io/{set_id}/{card_number}_hires.png"
    
    # Use setId_cardNumber as the key
    card_key = f"{set_id}_{card_number}"
    cards_data[card_key] = {
        "setId": set_id,
        "cardNumber": card_number,
        "imageUrl": url
    }
    
    if i % 50 == 0 or i == total:
        print(f"Processed {i}/{total} cards...")

# Save cards.json
cards_json_path = os.path.join(image_dir, 'cards.json')
with open(cards_json_path, 'w', encoding='utf-8') as f:
    json.dump(cards_data, f, indent=2, ensure_ascii=False)

print(f"\n{'='*50}")
print(f"Cards JSON created successfully!")
print(f"Total cards: {total}")
print(f"Saved to: {cards_json_path}")
print(f"{'='*50}")

print(f"\nAll done! Cards data saved to: {image_dir}")

