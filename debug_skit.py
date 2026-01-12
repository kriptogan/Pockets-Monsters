import os

# Get the full path
base_dir = r'd:\my projects\Pockets & Monsters'
file_path = os.path.join(base_dir, 'app/src/main/assets/test_fetch/skit.txt')

# Read the file
with open(file_path, 'r', encoding='utf-8') as f:
    lines = f.readlines()

print(f"Total lines: {len(lines)}")
print("\nFirst 10 lines with repr:")
for i, line in enumerate(lines[:10], 1):
    print(f"{i}: {repr(line)}")

