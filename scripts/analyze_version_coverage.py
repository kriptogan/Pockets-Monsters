#!/usr/bin/env python3
"""
Script to analyze version group coverage in pokemons.json
Checks which moves have Scarlet-Violet data and which don't

Usage: py scripts/analyze_version_coverage.py
"""

import json
from pathlib import Path
from collections import defaultdict

def analyze_version_coverage():
    """Analyze the version group coverage in pokemons.json"""
    assets_dir = "app/src/main/assets"
    pokemon_file = "pokemons.json"
    
    pokemon_path = Path(assets_dir) / pokemon_file
    
    if not pokemon_path.exists():
        print(f"❌ Pokemon file not found: {pokemon_path}")
        return
    
    print("🔍 Analyzing version group coverage...")
    
    with open(pokemon_path, 'r', encoding='utf-8') as f:
        pokemons = json.load(f)
    
    print(f"✅ Loaded {len(pokemons)} Pokémon")
    
    # Track version group coverage
    version_groups = defaultdict(int)
    moves_without_scarlet_violet = []
    total_level_up_moves = 0
    
    for pokemon in pokemons:
        level_up_moves = pokemon.get("level_up_moves", [])
        total_level_up_moves += len(level_up_moves)
        
        for move_info in level_up_moves:
            version_details = move_info.get("version_group_details", [])
            
            # Track all version groups
            for detail in version_details:
                version_group = detail.get("version_group", {}).get("name")
                if version_group:
                    version_groups[version_group] += 1
            
            # Check if this move has Scarlet-Violet data
            has_scarlet_violet = any(
                detail.get("version_group", {}).get("name") == "scarlet-violet"
                for detail in version_details
            )
            
            if not has_scarlet_violet:
                moves_without_scarlet_violet.append({
                    "pokemon": pokemon.get("name"),
                    "move": move_info.get("move", {}).get("name"),
                    "versions": [
                        detail.get("version_group", {}).get("name")
                        for detail in version_details
                    ]
                })
    
    print(f"\n📊 Version Group Coverage Analysis:")
    print(f"Total level-up moves: {total_level_up_moves}")
    print(f"Total version group entries: {sum(version_groups.values())}")
    
    print(f"\n🎮 Version Groups Found:")
    for version, count in sorted(version_groups.items()):
        print(f"  {version}: {count} entries")
    
    print(f"\n🔴 Scarlet-Violet Coverage:")
    scarlet_violet_count = version_groups.get("scarlet-violet", 0)
    coverage_percentage = (scarlet_violet_count / total_level_up_moves) * 100 if total_level_up_moves > 0 else 0
    print(f"  Moves with Scarlet-Violet data: {scarlet_violet_count}")
    print(f"  Coverage: {coverage_percentage:.1f}%")
    
    if moves_without_scarlet_violet:
        print(f"\n⚠️  Moves WITHOUT Scarlet-Violet data: {len(moves_without_scarlet_violet)}")
        print("First 10 examples:")
        for i, move_data in enumerate(moves_without_scarlet_violet[:10]):
            print(f"  {i+1}. {move_data['pokemon']} - {move_data['move']}")
            print(f"     Available versions: {', '.join(move_data['versions'])}")
        
        if len(moves_without_scarlet_violet) > 10:
            print(f"  ... and {len(moves_without_scarlet_violet) - 10} more")
    else:
        print(f"\n✅ All moves have Scarlet-Violet data!")

if __name__ == "__main__":
    analyze_version_coverage()
