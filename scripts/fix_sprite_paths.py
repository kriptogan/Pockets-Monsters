#!/usr/bin/env python3
"""
Fix sprite paths in pokemons.json by removing the 'sprites/' prefix
"""

import json
import os

def fix_sprite_paths():
    # Path to the pokemons.json file
    pokemons_file = "app/src/main/assets/pokemons.json"
    
    if not os.path.exists(pokemons_file):
        print(f"❌ File not found: {pokemons_file}")
        return
    
    print("🔧 Fixing sprite paths in pokemons.json...")
    
    try:
        # Read the current file
        with open(pokemons_file, 'r', encoding='utf-8') as f:
            pokemons = json.load(f)
        
        print(f"📖 Loaded {len(pokemons)} Pokémon")
        
        # Fix sprite paths
        fixed_count = 0
        for pokemon in pokemons:
            if 'sprite_path' in pokemon:
                old_path = pokemon['sprite_path']
                if old_path.startswith('sprites/'):
                    new_path = old_path.replace('sprites/', '')
                    pokemon['sprite_path'] = new_path
                    fixed_count += 1
                    print(f"  ✅ {pokemon['name']}: {old_path} → {new_path}")
        
        print(f"🔧 Fixed {fixed_count} sprite paths")
        
        # Write the updated file
        with open(pokemons_file, 'w', encoding='utf-8') as f:
            json.dump(pokemons, f, indent=2, ensure_ascii=False)
        
        print("💾 Updated pokemons.json successfully!")
        
        # Verify the fix
        print("\n🔍 Verifying first few entries:")
        for i, pokemon in enumerate(pokemons[:5]):
            print(f"  {pokemon['name']}: {pokemon['sprite_path']}")
        
    except Exception as e:
        print(f"❌ Error: {e}")
        return False
    
    return True

if __name__ == "__main__":
    success = fix_sprite_paths()
    if success:
        print("\n🎉 Sprite path fixing completed successfully!")
    else:
        print("\n💥 Failed to fix sprite paths!")
