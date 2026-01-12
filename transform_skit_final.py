import os
import re

# Get the full path
file_path = r'd:\my projects\Pockets & Monsters\app\src\main\assets\test_fetch\skit.txt'

# Read the file - try different methods
try:
    with open(file_path, 'r', encoding='utf-8') as f:
        lines = f.readlines()
    print(f"Read {len(lines)} lines using readlines()")
except Exception as e:
    print(f"Error with readlines(): {e}")
    try:
        with open(file_path, 'r', encoding='utf-8') as f:
            content = f.read()
        lines = content.splitlines(keepends=False)
        print(f"Read {len(lines)} lines using splitlines()")
    except Exception as e2:
        print(f"Error with splitlines(): {e2}")
        lines = []

if len(lines) == 0:
    print("File appears empty. Exiting.")
    exit(1)

# Remove newlines and process
lines = [line.rstrip('\n\r') for line in lines]

result = []
i = 0
processed = 0
while i < len(lines):
    line = lines[i]
    stripped = line.strip()
    
    # Skip empty lines
    if not stripped:
        i += 1
        continue
    
    # Check if this line is a card number (starts with a digit, contains "/")
    if re.match(r'^\d+', stripped) and '/' in stripped:
        processed += 1
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

print(f"Processed {processed} card number lines")
print(f"Created {len(result)} entries")

# Write back to file
if len(result) > 0:
    with open(file_path, 'w', encoding='utf-8') as f:
        f.writelines(result)
    print(f"Transformed file: {len(result)} entries created")
    print(f"First 10 entries:")
    for entry in result[:10]:
        print(f"  {entry.strip()}")
    if len(result) > 10:
        print(f"Last 5 entries:")
        for entry in result[-5:]:
            print(f"  {entry.strip()}")
else:
    print("No entries created. File may be empty or in wrong format.")

