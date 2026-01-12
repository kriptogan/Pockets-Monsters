import os
import requests
from pathlib import Path

# File paths
skit_path = r'd:\my projects\Pockets & Monsters\app\src\main\assets\test_fetch\skit.txt'
output_dir = r'd:\my projects\Pockets & Monsters\app\src\main\assets\tcg_images\4'

# Create output directory if it doesn't exist
os.makedirs(output_dir, exist_ok=True)

# Read skit.txt
with open(skit_path, 'r', encoding='utf-8') as f:
    lines = f.readlines()

total = 0
success = 0
failed = 0
skipped = 0

print(f"Starting download of {len(lines)} card images...")
print(f"Output directory: {output_dir}\n")

for i, line in enumerate(lines, 1):
    line = line.strip()
    if not line:
        continue
    
    # Format: setId/cardNumber
    if '/' in line:
        parts = line.split('/', 1)
        if len(parts) == 2:
            set_id = parts[0].strip()
            card_number = parts[1].strip()
            
            # Construct URL: images.pokemontcg.io/[baseCode]/[cardIndex]_hires.png
            url = f"https://images.pokemontcg.io/{set_id}/{card_number}_hires.png"
            
            # Construct filename: setId_cardNumber.png
            filename = f"{set_id}_{card_number}.png"
            filepath = os.path.join(output_dir, filename)
            
            # Skip if file already exists
            if os.path.exists(filepath):
                print(f"[{i}/{len(lines)}] Skipped (exists): {filename}")
                skipped += 1
                continue
            
            try:
                # Download image
                response = requests.get(url, timeout=30)
                response.raise_for_status()
                
                # Save image
                with open(filepath, 'wb') as f:
                    f.write(response.content)
                
                print(f"[{i}/{len(lines)}] OK Downloaded: {filename}")
                success += 1
                
            except requests.exceptions.RequestException as e:
                print(f"[{i}/{len(lines)}] FAILED: {filename} - {str(e)}")
                failed += 1
            except Exception as e:
                print(f"[{i}/{len(lines)}] ERROR: {filename} - {str(e)}")
                failed += 1
            
            total += 1

print(f"\n{'='*50}")
print(f"Download complete!")
print(f"Total: {total}")
print(f"Success: {success}")
print(f"Skipped: {skipped}")
print(f"Failed: {failed}")
print(f"{'='*50}")

