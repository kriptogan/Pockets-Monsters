#!/usr/bin/env python3
"""
Script to clean up pokemons.json by removing unnecessary data
Keeps only essential information for D&D conversion including level-up moves

Usage: py scripts/clean_pokemon_data.py
"""

import json
import os
from pathlib import Path
from typing import Dict, List, Any

class PokemonDataCleaner:
    def __init__(self):
        self.assets_dir = "app/src/main/assets"
        self.pokemon_data_file = "pokemons.json"
        self.cleaned_file = "pokemons_cleaned.json"
        
        # Most recent generation (Gen 8 - Sword/Shield)
        self.target_version_groups = [
            "sword-shield",
            "brilliant-diamond-and-shining-pearl",
            "legends-arceus",
            "scarlet-violet"
        ]
    
    def clean_data(self):
        """Clean the pokemons.json file by removing unnecessary data"""
        try:
            print("🧹 Starting Pokémon data cleanup...")
            
            # Read the current data file
            pokemon_data_path = Path(self.assets_dir) / self.pokemon_data_file
            if not pokemon_data_path.exists():
                print(f"❌ Pokemon data file not found: {pokemon_data_path}")
                return
            
            print(f"📖 Reading {pokemon_data_path}...")
            with open(pokemon_data_path, 'r', encoding='utf-8') as f:
                all_data = json.load(f)
            
            print(f"✅ Found {len(all_data)} Pokémon entries")
            
            # Clean each Pokémon
            cleaned_pokemons = []
            for pokemon in all_data:
                cleaned_pokemon = self._clean_pokemon(pokemon)
                cleaned_pokemons.append(cleaned_pokemon)
            
            # Save cleaned data
            self._save_cleaned_file(cleaned_pokemons)
            
            print("\n🎉 Data cleanup completed successfully!")
            
        except Exception as e:
            print(f"💥 Data cleanup failed: {str(e)}")
            raise
    
    def _clean_pokemon(self, pokemon: Dict[str, Any]) -> Dict[str, Any]:
        """Clean a single Pokémon entry"""
        cleaned = {
            "id": pokemon.get("id"),
            "name": pokemon.get("name"),
            "height": pokemon.get("height"),
            "weight": pokemon.get("weight"),
            "base_experience": pokemon.get("base_experience"),
            "types": pokemon.get("types", []),
            "stats": pokemon.get("stats", []),
            "abilities": pokemon.get("abilities", []),
            "level_up_moves": self._extract_level_up_moves(pokemon),
            "sprite_path": f"sprites/{pokemon.get('name', '').lower()}.png"
        }
        return cleaned
    
    def _extract_level_up_moves(self, pokemon: Dict[str, Any]) -> List[Dict[str, Any]]:
        """Extract only level-up moves from the most recent generation"""
        level_up_moves = []
        moves = pokemon.get("moves", [])
        
        for move_info in moves:
            move = move_info.get("move", {})
            version_group_details = move_info.get("version_group_details", [])
            
            # Filter for level-up moves only
            level_up_details = []
            for detail in version_group_details:
                move_learn_method = detail.get("move_learn_method", {})
                if move_learn_method.get("name") == "level-up":
                    # Check if it's from our target generation
                    version_group = detail.get("version_group", {})
                    if version_group.get("name") in self.target_version_groups:
                        level_up_details.append({
                            "level_learned_at": detail.get("level_learned_at"),
                            "move_learn_method": {
                                "name": "level-up"
                            },
                            "version_group": {
                                "name": version_group.get("name")
                            }
                        })
            
            # Only add moves that have level-up data
            if level_up_details:
                level_up_moves.append({
                    "move": {
                        "name": move.get("name")
                    },
                    "version_group_details": level_up_details
                })
        
        return level_up_moves
    
    def _save_cleaned_file(self, cleaned_data: List[Dict[str, Any]]):
        """Save the cleaned data to a new file"""
        # Save as cleaned file first
        cleaned_path = Path(self.assets_dir) / self.cleaned_file
        
        with open(cleaned_path, 'w', encoding='utf-8') as f:
            json.dump(cleaned_data, f, indent=2, ensure_ascii=False)
        
        # Get file size
        file_size = cleaned_path.stat().st_size
        size_mb = file_size / (1024 * 1024)
        print(f"📁 {self.cleaned_file}: {size_mb:.2f} MB")
        
        # Backup original and replace
        original_path = Path(self.assets_dir) / self.pokemon_data_file
        backup_path = Path(self.assets_dir) / "pokemons_backup.json"
        
        # Create backup
        if original_path.exists():
            import shutil
            shutil.copy2(original_path, backup_path)
            print(f"📋 Created backup: {backup_path}")
        
        # Replace original with cleaned version
        shutil.move(cleaned_path, original_path)
        print(f"✅ Replaced {self.pokemon_data_file} with cleaned version")
        
        # Show size reduction
        if backup_path.exists():
            original_size = backup_path.stat().st_size
            original_mb = original_size / (1024 * 1024)
            reduction = ((original_size - file_size) / original_size) * 100
            print(f"📉 Size reduction: {reduction:.1f}% ({original_mb:.2f} MB → {size_mb:.2f} MB)")

def main():
    """Main execution"""
    cleaner = PokemonDataCleaner()
    cleaner.clean_data()

if __name__ == "__main__":
    main()
