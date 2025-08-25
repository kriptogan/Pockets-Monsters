#!/usr/bin/env python3
"""
Script to remove all URL paths from pokemons.json
Keeps only essential data and sprite_path, removes all API URLs

Usage: py scripts/remove_urls.py
"""

import json
from pathlib import Path
from typing import Dict, List, Any

class PokemonUrlRemover:
    def __init__(self):
        self.assets_dir = "app/src/main/assets"
        self.pokemon_data_file = "pokemons.json"
        self.cleaned_file = "pokemons_no_urls.json"
    
    def remove_urls(self):
        """Remove all URL paths from pokemons.json"""
        try:
            print("🧹 Starting URL removal from Pokémon data...")
            
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
            
            print("\n🎉 URL removal completed successfully!")
            
        except Exception as e:
            print(f"💥 URL removal failed: {str(e)}")
            raise
    
    def _clean_pokemon(self, pokemon: Dict[str, Any]) -> Dict[str, Any]:
        """Clean a single Pokémon entry by removing URLs"""
        cleaned = {}
        
        # Copy basic fields
        for key in ["id", "name", "height", "weight", "base_experience", "sprite_path"]:
            if key in pokemon:
                cleaned[key] = pokemon[key]
        
        # Clean types (remove URLs)
        if "types" in pokemon:
            cleaned["types"] = []
            for type_info in pokemon["types"]:
                cleaned_type = {
                    "slot": type_info.get("slot"),
                    "type": {
                        "name": type_info.get("type", {}).get("name")
                    }
                }
                cleaned["types"].append(cleaned_type)
        
        # Clean stats (remove URLs)
        if "stats" in pokemon:
            cleaned["stats"] = []
            for stat_info in pokemon["stats"]:
                cleaned_stat = {
                    "base_stat": stat_info.get("base_stat"),
                    "effort": stat_info.get("effort"),
                    "stat": {
                        "name": stat_info.get("stat", {}).get("name")
                    }
                }
                cleaned["stats"].append(cleaned_stat)
        
        # Clean abilities (remove URLs)
        if "abilities" in pokemon:
            cleaned["abilities"] = []
            for ability_info in pokemon["abilities"]:
                cleaned_ability = {
                    "ability": {
                        "name": ability_info.get("ability", {}).get("name")
                    },
                    "is_hidden": ability_info.get("is_hidden"),
                    "slot": ability_info.get("slot")
                }
                cleaned["abilities"].append(cleaned_ability)
        
        # Clean level_up_moves (remove URLs)
        if "level_up_moves" in pokemon:
            cleaned["level_up_moves"] = []
            for move_info in pokemon["level_up_moves"]:
                cleaned_move = {
                    "name": move_info.get("name"),
                    "level_learned_at": move_info.get("level_learned_at"),
                    "version_group": move_info.get("version_group")
                }
                cleaned["level_up_moves"].append(cleaned_move)
        
        return cleaned
    
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
        backup_path = Path(self.assets_dir) / "pokemons_backup_no_urls.json"
        
        # Create backup
        if original_path.exists():
            import shutil
            shutil.copy2(original_path, backup_path)
            print(f"📋 Created backup: {backup_path}")
        
        # Replace original with cleaned version
        shutil.move(cleaned_path, original_path)
        print(f"✅ Replaced {self.pokemon_data_file} with cleaned version")
        
        # Show size comparison
        if backup_path.exists():
            original_size = backup_path.stat().st_size
            original_mb = original_size / (1024 * 1024)
            reduction = ((original_size - file_size) / original_size) * 100
            print(f"📉 Size reduction: {reduction:.1f}% ({original_mb:.2f} MB → {size_mb:.2f} MB)")
        
        # Show example of cleaned data
        if cleaned_data:
            first_pokemon = cleaned_data[0]
            print(f"\n📝 Example of cleaned Pokémon data:")
            print(f"  Name: {first_pokemon.get('name')}")
            print(f"  Types: {[t['type']['name'] for t in first_pokemon.get('types', [])]}")
            print(f"  Moves: {len(first_pokemon.get('level_up_moves', []))} level-up moves")
            print(f"  Sprite: {first_pokemon.get('sprite_path')}")

def main():
    """Main execution"""
    remover = PokemonUrlRemover()
    remover.remove_urls()

if __name__ == "__main__":
    main()
