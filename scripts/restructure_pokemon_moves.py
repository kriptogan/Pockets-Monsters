#!/usr/bin/env python3
"""
Script to restructure pokemons.json by consolidating version group details
Moves version group info directly into the move property for cleaner data

Usage: py scripts/restructure_pokemon_moves.py
"""

import json
from pathlib import Path
from typing import Dict, List, Any

class PokemonMoveRestructurer:
    def __init__(self):
        self.assets_dir = "app/src/main/assets"
        self.pokemon_data_file = "pokemons.json"
        self.restructured_file = "pokemons_restructured.json"
        
        # Priority order for version groups (most recent first)
        self.version_priority = [
            "scarlet-violet",
            "legends-arceus", 
            "brilliant-diamond-and-shining-pearl",
            "sword-shield"
        ]
    
    def restructure_data(self):
        """Restructure the pokemons.json file by consolidating move data"""
        try:
            print("🔄 Starting Pokémon move restructuring...")
            
            # Read the current data file
            pokemon_data_path = Path(self.assets_dir) / self.pokemon_data_file
            if not pokemon_data_path.exists():
                print(f"❌ Pokemon data file not found: {pokemon_data_path}")
                return
            
            print(f"📖 Reading {pokemon_data_path}...")
            with open(pokemon_data_path, 'r', encoding='utf-8') as f:
                all_data = json.load(f)
            
            print(f"✅ Found {len(all_data)} Pokémon entries")
            
            # Restructure each Pokémon
            restructured_pokemons = []
            for pokemon in all_data:
                restructured_pokemon = self._restructure_pokemon(pokemon)
                restructured_pokemons.append(restructured_pokemon)
            
            # Save restructured data
            self._save_restructured_file(restructured_pokemons)
            
            print("\n🎉 Move restructuring completed successfully!")
            
        except Exception as e:
            print(f"💥 Move restructuring failed: {str(e)}")
            raise
    
    def _restructure_pokemon(self, pokemon: Dict[str, Any]) -> Dict[str, Any]:
        """Restructure a single Pokémon entry"""
        restructured = pokemon.copy()
        
        # Restructure level_up_moves
        level_up_moves = pokemon.get("level_up_moves", [])
        restructured_moves = []
        
        for move_info in level_up_moves:
            restructured_move = self._restructure_move(move_info)
            if restructured_move:  # Only add if we have valid data
                restructured_moves.append(restructured_move)
        
        restructured["level_up_moves"] = restructured_moves
        return restructured
    
    def _restructure_move(self, move_info: Dict[str, Any]) -> Dict[str, Any]:
        """Restructure a single move entry"""
        move = move_info.get("move", {})
        version_group_details = move_info.get("version_group_details", [])
        
        if not version_group_details:
            return None
        
        # Find the best version group based on priority
        best_version_detail = self._find_best_version_detail(version_group_details)
        
        if not best_version_detail:
            return None
        
        # Create restructured move
        restructured_move = {
            "name": move.get("name"),
            "level_learned_at": best_version_detail.get("level_learned_at"),
            "version_group": best_version_detail.get("version_group", {}).get("name")
        }
        
        return restructured_move
    
    def _find_best_version_detail(self, version_details: List[Dict[str, Any]]) -> Dict[str, Any]:
        """Find the best version detail based on priority order"""
        if not version_details:
            return None
        
        # Sort by priority (lower index = higher priority)
        for priority_version in self.version_priority:
            for detail in version_details:
                version_group = detail.get("version_group", {}).get("name")
                if version_group == priority_version:
                    return detail
        
        # If no priority version found, return the first one
        return version_details[0] if version_details else None
    
    def _save_restructured_file(self, restructured_data: List[Dict[str, Any]]):
        """Save the restructured data to a new file"""
        # Save as restructured file first
        restructured_path = Path(self.assets_dir) / self.restructured_file
        
        with open(restructured_path, 'w', encoding='utf-8') as f:
            json.dump(restructured_data, f, indent=2, ensure_ascii=False)
        
        # Get file size
        file_size = restructured_path.stat().st_size
        size_mb = file_size / (1024 * 1024)
        print(f"📁 {self.restructured_file}: {size_mb:.2f} MB")
        
        # Backup original and replace
        original_path = Path(self.assets_dir) / self.pokemon_data_file
        backup_path = Path(self.assets_dir) / "pokemons_backup_restructure.json"
        
        # Create backup
        if original_path.exists():
            import shutil
            shutil.copy2(original_path, backup_path)
            print(f"📋 Created backup: {backup_path}")
        
        # Replace original with restructured version
        shutil.move(restructured_path, original_path)
        print(f"✅ Replaced {self.pokemon_data_file} with restructured version")
        
        # Show size comparison
        if backup_path.exists():
            original_size = backup_path.stat().st_size
            original_mb = original_size / (1024 * 1024)
            reduction = ((original_size - file_size) / original_size) * 100
            print(f"📉 Size reduction: {reduction:.1f}% ({original_mb:.2f} MB → {size_mb:.2f} MB)")
        
        # Show example of restructured data
        if restructured_data:
            first_pokemon = restructured_data[0]
            level_up_moves = first_pokemon.get("level_up_moves", [])
            if level_up_moves:
                print(f"\n📝 Example of restructured move:")
                example_move = level_up_moves[0]
                print(f"  {json.dumps(example_move, indent=2)}")

def main():
    """Main execution"""
    restructurer = PokemonMoveRestructurer()
    restructurer.restructure_data()

if __name__ == "__main__":
    main()
