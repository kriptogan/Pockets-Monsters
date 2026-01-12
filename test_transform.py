import re

# Test with actual lines from the file
test_lines = [
    "    Charizard	Base",
    "4 / 102 	",
    "	Dark Charizard	Team Rocket",
    "4 / 82 	"
]

print("Testing pattern matching:")
for i, line in enumerate(test_lines):
    stripped = line.strip()
    print(f"Line {i}: '{stripped}'")
    print(f"  Starts with digit: {bool(re.match(r'^\d+', stripped))}")
    print(f"  Contains '/': {'/' in stripped}")
    if re.match(r'^\d+', stripped) and '/' in stripped:
        match = re.search(r'^(\d+)', stripped)
        if match:
            print(f"  X value: {match.group(1)}")
    print()

