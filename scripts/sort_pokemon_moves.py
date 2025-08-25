#!/usr/bin/env python3
"""
Script to sort level_up_moves by level_learned_at in pokemons.json
Sorts moves in ascending order by level for each Pokémon

Usage: py scripts/sort_pokemon_moves.py
"""

import json
from pathlib import Path
from typing import Dict, List, Any

class PokemonMoveSorter:
    def __init__(self):
        self.assets_dir = "app/src/main/assets"
        self.pokemon_data_file = "pokemons.json"
        self.sorted_file = "pokemons_sorted_moves.json"
    
    def sort_moves(self):
        """Sort level_up_moves by level_learned_at for each Pokémon"""
        try:
            print("🔄 Starting Pokémon move sorting...")
            
            # Read the current data file
            pokemon_data_path = Path(self.assets_dir) / self.pokemon_data_file
            if not pokemon_data_path.exists():
                print(f"❌ Pokemon data file not found: {pokemon_data_path}")
                return
            
            print(f"📖 Reading {pokemon_data_path}...")
            with open(pokemon_data_path, 'r', encoding='utf-8') as f:
                all_data = json.load(f)
            
            print(f"✅ Found {len(all_data)} Pokémon entries")
            
            # Sort moves for each Pokémon
            sorted_pokemons = []
            for pokemon in all_data:
                sorted_pokemon = self._sort_pokemon_moves(pokemon)
                sorted_pokemons.append(sorted_pokemon)
            
            # Save sorted data
            self._save_sorted_file(sorted_pokemons)
            
            print("\n🎉 Move sorting completed successfully!")
            
        except Exception as e:
            print(f"💥 Move sorting failed: {str(e)}")
            raise
    
    def _sort_pokemon_moves(self, pokemon: Dict[str, Any]) -> Dict[str, Any]:
        """Sort level_up_moves for a single Pokémon"""
        sorted_pokemon = pokemon.copy()
        
        # Sort level_up_moves by level_learned_at
        level_up_moves = pokemon.get("level_up_moves", [])
        if level_up_moves:
            # Sort by level_learned_at (ascending order)
            sorted_moves = sorted(level_up_moves, key=lambda x: x.get("level_learned_at", 0))
            sorted_pokemon["level_up_moves"] = sorted_moves
            
            # Show sorting info for first few Pokémon
            if pokemon.get("id", 0) <= 3:  # Show first 3 Pokémon as examples
                print(f"  📝 {pokemon.get('name', 'unknown')}: {len(sorted_moves)} moves sorted")
                if sorted_moves:
                    levels = [move.get("level_learned_at", 0) for move in sorted_moves[:5]]
                    print(f"     First 5 levels: {levels}")
        
        return sorted_pokemon
    
    def _save_sorted_file(self, sorted_data: List[Dict[str, Any]]):
        """Save the sorted data to a new file"""
        # Save as sorted file first
        sorted_path = Path(self.assets_dir) / self.sorted_file
        
        with open(sorted_path, 'w', encoding='utf-8') as f:
            json.dump(sorted_data, f, indent=2, ensure_ascii=False)
        
        # Get file size
        file_size = sorted_path.stat().st_size
        size_mb = file_size / (1024 * 1024)
        print(f"📁 {self.sorted_file}: {size_mb:.2f} MB")
        
        # Backup original and replace
        original_path = Path(self.assets_dir) / self.pokemon_data_file
        backup_path = Path(self.assets_dir) / "pokemons_backup_sorted_moves.json"
        
        # Create backup
        if original_path.exists():
            import shutil
            shutil.copy2(original_path, backup_path)
            print(f"📋 Created backup: {backup_path}")
        
        # Replace original with sorted version
        shutil.move(sorted_path, original_path)
        print(f"✅ Replaced {self.pokemon_data_file} with sorted version")
        
        # Show example of sorted moves
        if sorted_data:
            first_pokemon = sorted_data[0]
            level_up_moves = first_pokemon.get("level_up_moves", [])
            if level_up_moves:
                print(f"\n📝 Example of sorted moves for {first_pokemon.get('name')}:")
                for i, move in enumerate(level_up_moves[:10]):  # Show first 10 moves
                    print(f"  {i+1:2d}. Level {move.get('level_learned_at', 0):2d}: {move.get('name', 'unknown')}")
                if len(level_up_moves) > 10:
                    print(f"  ... and {len(level_up_moves) - 10} more moves")

def main():
    """Main execution"""
    sorter = PokemonMoveSorter()
    sorter.sort_moves()

if __name__ == "__main__":
    main()
