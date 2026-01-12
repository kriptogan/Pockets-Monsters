import re

import os

# Get the full path
base_dir = r'd:\my projects\Pockets & Monsters'
file_path = os.path.join(base_dir, 'app/src/main/assets/test_fetch/skit.txt')

# Read the file
with open(file_path, 'r', encoding='utf-8') as f:
    lines = f.readlines()

# Filter out lines containing:
# - HP (like "120HP")
# - "Weakness	Resistance	Retreat Cost"
# - Lines that are just weakness/resistance/retreat cost values (like "	 -30	", "x2	 -20	", etc.)
cleaned_lines = []
for line in lines:
    stripped = line.strip()
    # Skip empty lines
    if not stripped:
        continue
    # Skip lines with HP
    if 'HP' in stripped:
        continue
    # Skip the header line
    if 'Weakness' in stripped and 'Resistance' in stripped and 'Retreat Cost' in stripped:
        continue
    # Skip lines that are just weakness/resistance/retreat cost values
    # These are typically lines with just tabs, numbers, x2, +, -, etc.
    if re.match(r'^[\s\t]*([-+]?\d+|x\d+|\s)*[\s\t]*$', stripped):
        continue
    # Keep all other lines
    cleaned_lines.append(line)

# Write back to file
with open(file_path, 'w', encoding='utf-8') as f:
    f.writelines(cleaned_lines)

print(f"Cleaned file: removed {len(lines) - len(cleaned_lines)} lines, kept {len(cleaned_lines)} lines")

