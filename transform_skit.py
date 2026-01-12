import os
import re

# Get the full path
file_path = r'd:\my projects\Pockets & Monsters\app\src\main\assets\test_fetch\skit.txt'

# Read the file line by line
with open(file_path, 'r', encoding='utf-8') as f:
    lines = f.readlines()

# Remove newlines
lines = [line.rstrip('\n\r') for line in lines]

result = []
i = 0
while i < len(lines):
    line = lines[i]
    stripped = line.strip()
    
    # Skip empty lines
    if not stripped:
        i += 1
        continue
    
    # Check if this line is a card number (starts with a digit, contains "/")
    # Format: "X / Y" or "X/Y"
    if stripped and re.match(r'^\d+', stripped) and '/' in stripped:
        # Extract the X value (first number before "/")
        match = re.search(r'^(\d+)', stripped)
        if match:
            x_value = match.group(1)
            
            # Look back for the set name (previous non-empty line)
            j = i - 1
            while j >= 0 and not lines[j].strip():
                j -= 1
            
            if j >= 0:
                prev_line = lines[j]
                # Split by tab - the last non-empty part should be the set name
                tab_parts = prev_line.split('\t')
                # Get all non-empty parts (strip each)
                non_empty_parts = [p.strip() for p in tab_parts if p.strip()]
                
                if len(non_empty_parts) >= 2:
                    # Last part should be the set name
                    set_name = non_empty_parts[-1]
                    result.append(f"{set_name}/{x_value}\n")
                elif len(non_empty_parts) == 1:
                    # Only one part - if it doesn't start with a digit, it's likely the set name
                    if not re.match(r'^\d+', non_empty_parts[0]):
                        set_name = non_empty_parts[0]
                        result.append(f"{set_name}/{x_value}\n")
    
    i += 1

# Write back to file
with open(file_path, 'w', encoding='utf-8') as f:
    f.writelines(result)

print(f"Transformed file: {len(result)} entries created")
if len(result) > 0:
    print(f"First 10 entries:")
    for entry in result[:10]:
        print(f"  {entry.strip()}")
    if len(result) > 10:
        print(f"Last 5 entries:")
        for entry in result[-5:]:
            print(f"  {entry.strip()}")
