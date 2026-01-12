file_path = r'd:\my projects\Pockets & Monsters\app\src\main\assets\test_fetch\skit.txt'

with open(file_path, 'rb') as f:
    raw_bytes = f.read(200)  # First 200 bytes
    print(f"First 200 bytes (hex): {raw_bytes[:200].hex()}")

with open(file_path, 'r', encoding='utf-8') as f:
    lines = f.readlines()
    print(f"\nTotal lines: {len(lines)}")
    print(f"First 5 lines:")
    for i, line in enumerate(lines[:5], 1):
        print(f"  {i}: {repr(line)}")
        stripped = line.strip()
        print(f"      Stripped: {repr(stripped)}")
        print(f"      Starts with digit: {bool(__import__('re').match(r'^\d+', stripped))}")
        print(f"      Contains '/': {'/' in stripped}")

