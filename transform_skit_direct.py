import re

# Read the content that we know exists
content = """    Charizard	Base
4 / 102 	
	Dark Charizard	Team Rocket
4 / 82 	
	Dark Charizard	Team Rocket
21 / 82 	
	Blaine's Charizard	Gym Challenge
2 / 132 	
	Shining Charizard	Neo Destiny
107 / 105 	
	Lance's Charizard	VS
97 / 141 	
	Charizard	Miscellaneous Promos 1998
	Charizard	Expedition
6 / 165 	
	Charizard	Expedition
39 / 165 	
	Charizard	Expedition
40 / 165 	
	Charizard	Skyridge
146 / 144 	
	Charizard	EX Dragon
100 / 97 	
	Charizard ex	EX FireRed and Leaf Green
105 / 112 	
	Charizard δ	EX Crystal Guardians
4 / 100 	
	Charizard Shining δ	EX Dragon Frontiers
100 / 101 	
	Charizard	EX Power Keepers
6 / 108 	
	Charizard	Secret Wonders
3 / 132 	
	Charizard	Stormfront
103 / 100 	
	Charizard Galactic	Supreme Victors
20 / 147 	
	Charizard Galactic Lv. X	Supreme Victors
143 / 147 	
	Charizard	Arceus
1 / 99 	
	Charizard Galactic Lv. X	DP Promo
DP45 	
	Charizard	Boundaries Crossed
20 / 149 	
	Charizard	Plasma Storm
136 / 135 	
	Charizard	Legendary Treasures
19 / 113 	
	Charizard EX	Flashfire
11 / 106 	
	Charizard EX	Flashfire
12 / 106 	
	M Charizard EX	Flashfire
13 / 106 	
	M Charizard EX	Flashfire
69 / 106 	
	Charizard EX	Flashfire
100 / 106 	
	M Charizard EX	Flashfire
107 / 106 	
	M Charizard EX	Flashfire
108 / 106 	
	Charizard EX	Generations
11 / 83 	
	M Charizard EX	Generations
12 / 83 	
	Charizard	Generations
RC5 / RC32 	
	Charizard	Evolutions
11 / 108 	
	Charizard EX	Evolutions
12 / 108 	
	M Charizard EX	Evolutions
13 / 108 	
	M Charizard EX	Evolutions
101 / 108 	
	Charizard EX	XY Promos
XY17 	
	Charizard EX	XY Promos
XY29 	
	Charizard EX	XY Promos
XY121 	
	Charizard EX	XY Promo
276 / XY-P 	
	Charizard	XY Promo
280 / XY-P 	
	Charizard GX	Burning Shadows
20 / 147 	
	Charizard GX	Burning Shadows
150 / 147 	
	Charizard	Dragon Majesty
3 / 70 	
	Charizard	Team Up
14 / 181 	
	Charizard	Pokemon Detective Pikachu
5 / 18 	
	Reshiram & Charizard GX	Unbroken Bonds
20 / 214 	
	Reshiram & Charizard GX	Unbroken Bonds
194 / 214 	
	Reshiram & Charizard GX	Unbroken Bonds
217 / 214 	
	Charizard GX	Hidden Fates
9 / 68 	
	Charizard GX	Hidden Fates
SV49 / SV94  Alternate Art Card	
	Charizard & Braixen GX	Cosmic Eclipse
22 / 236 	
	Charizard & Braixen GX	Cosmic Eclipse
212 / 236 	
	Charizard & Braixen GX	Cosmic Eclipse
251 / 236 	
	Charizard GX	SM Promos
SM60 	
	Charizard	SM Promos
SM158 	
	Charizard GX	SM Promos
SM195 	
	Reshiram & Charizard GX	SM Promos
SM201 	
	Charizard GX	SM Promos
SM211 	
	Charizard	SM Promos
SM226 	
	Charizard & Braixen GX	SM Promos
SM230 	
	Reshiram & Charizard GX	SM Promos
SM247 	
	Charizard V	Darkness Ablaze
19 / 189 	
	Charizard VMAX	Darkness Ablaze
20 / 189 	
	Charizard VMAX	Champion's Path
74 / 70 	
	Charizard V	Champion's Path
79 / 70 	
	Charizard	Vivid Voltage
25 / 180 	
	Charizard VMAX	Shining Fates
SV107 / SV122 	
	Charizard	Celebrations
4 / 102 	
	Charizard V	Brilliant Stars
17 / 172 	
	Charizard VSTAR	Brilliant Stars
18 / 172 	
	Charizard V	Brilliant Stars
153 / 172 	
	Charizard V	Brilliant Stars
154 / 172 	
	Charizard VSTAR	Brilliant Stars
174 / 172 	
	Charizard	Pokemon GO
10 / 78 	
	Radiant Charizard	Pokemon GO
11 / 78 	
	Charizard	Lost Origin
TG3 / TG30 	
	Charizard V	Crown Zenith
18 / 159 	
	Charizard VSTAR	Crown Zenith
19 / 159 	
	Radiant Charizard	Crown Zenith
20 / 159 	
	Charizard	S Promo
143 / S-P 	
	Charizard V	SWSH Promos
SWSH50 	
	Charizard	SWSH Promos
SWSH66 	
	Special Delivery Charizard	SWSH Promos
SWSH75 	
	Lance's Charizard V	SWSH Promos
SWSH133 	
	Charizard V	SWSH Promos
SWSH260 	
	Charizard VMAX	SWSH Promos
SWSH261 	
	Charizard VSTAR	SWSH Promos
SWSH262 	
	Charizard ex	Obsidian Flames
125 / 197 	
	Charizard ex	Obsidian Flames
215 / 197 	
	Charizard ex	Obsidian Flames
223 / 197 	
	Charizard ex	Obsidian Flames
228 / 197 	
	Charizard ex	151
6 / 165 	
	Charizard ex	151
183 / 165 	
	Charizard ex	151
199 / 165 	
	Charizard ex	Paldean Fates
54 / 91 	
	Charizard ex	Paldean Fates
234 / 91 	
	Charizard ex	Battle Master Deck Terastal Charizard ex
6 / 21 	
	Charizard ex	SV Promos
	Charizard ex	SV Promos
	Charizard ex	SV Promos
	Charizard ex	SV Promos
	Mega Charizard X ex	Phantasmal Flames
13 / 94 	
	Mega Charizard X ex	Phantasmal Flames
109 / 94 	
	Mega Charizard X ex	Phantasmal Flames
125 / 94 	
	Mega Charizard X ex	Phantasmal Flames
130 / 94 	
	Mega Charizard Y ex	Ascended Heroes
22 / 217 	
	Mega Charizard Y ex	Starter Deck 100 Battle Collection
766 / 742 	
	Mega Charizard ex	Mega Promos
23 """

lines = content.splitlines()

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
    if re.match(r'^\d+', stripped) and '/' in stripped:
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

# Write to file
file_path = r'd:\my projects\Pockets & Monsters\app\src\main\assets\test_fetch\skit.txt'
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

