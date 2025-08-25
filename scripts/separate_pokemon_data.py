#!/usr/bin/env python3
"""
Script to separate the large pokemon_data.json into smaller, focused files
This will make data loading easier and more memory efficient

Usage: py scripts/separate_pokemon_data.py
"""

import json
import os
from pathlib import Path
from typing import Dict, List, Any, Set

class PokemonDataSeparator:
    def __init__(self):
        self.assets_dir = "app/src/main/assets"
        self.pokemon_data_file = "pokemon_data.json"
        
    def separate_data(self):
        """Separate the large pokemon_data.json into smaller files"""
        try:
            print("🚀 Starting Pokémon data separation...")
            
            # Read the large data file
            pokemon_data_path = Path(self.assets_dir) / self.pokemon_data_file
            if not pokemon_data_path.exists():
                print(f"❌ Pokemon data file not found: {pokemon_data_path}")
                return
                
            print(f"📖 Reading {pokemon_data_path}...")
            with open(pokemon_data_path, 'r', encoding='utf-8') as f:
                all_data = json.load(f)
            
            print(f"✅ Found {len(all_data)} Pokémon entries")
            
            # Separate data by type
            self._separate_pokemons(all_data)
            self._separate_moves(all_data)
            self._separate_abilities(all_data)
            self._separate_types(all_data)
            self._separate_stats(all_data)
            
            print("\n🎉 Data separation completed successfully!")
            
        except Exception as e:
            print(f"💥 Data separation failed: {str(e)}")
            raise
    
    def _separate_pokemons(self, all_data: List[Dict[str, Any]]):
        """Extract basic Pokémon information"""
        print("📝 Separating Pokémon data...")
        
        pokemons = []
        for pokemon in all_data:
            pokemon_info = {
                "id": pokemon.get("id"),
                "name": pokemon.get("name"),
                "base_experience": pokemon.get("base_experience"),
                "height": pokemon.get("height"),
                "weight": pokemon.get("weight"),
                "order": pokemon.get("order"),
                "is_default": pokemon.get("is_default", False),
                "location_area_encounters": pokemon.get("location_area_encounters"),
                "species": pokemon.get("species", {}),
                "forms": pokemon.get("forms", []),
                "game_indices": pokemon.get("game_indices", []),
                "held_items": pokemon.get("held_items", []),
                "moves": pokemon.get("moves", []),
                "sprites": pokemon.get("sprites", {}),
                "stats": pokemon.get("stats", []),
                "types": pokemon.get("types", []),
                "abilities": pokemon.get("abilities", [])
            }
            pokemons.append(pokemon_info)
        
        self._save_file("pokemons.json", pokemons)
        print(f"✅ Saved {len(pokemons)} Pokémon entries")
    
    def _separate_moves(self, all_data: List[Dict[str, Any]]):
        """Extract unique moves information"""
        print("⚔️ Separating moves data...")
        
        moves_set = set()
        moves_data = []
        
        for pokemon in all_data:
            for move_info in pokemon.get("moves", []):
                move = move_info.get("move", {})
                move_name = move.get("name")
                
                if move_name and move_name not in moves_set:
                    moves_set.add(move_name)
                    
                    # Get detailed move info if available
                    move_detail = {
                        "name": move_name,
                        "url": move.get("url"),
                        "version_group_details": move_info.get("version_group_details", [])
                    }
                    moves_data.append(move_detail)
        
        self._save_file("moves.json", moves_data)
        print(f"✅ Saved {len(moves_data)} unique moves")
    
    def _separate_abilities(self, all_data: List[Dict[str, Any]]):
        """Extract unique abilities information"""
        print("✨ Separating abilities data...")
        
        abilities_set = set()
        abilities_data = []
        
        for pokemon in all_data:
            for ability_info in pokemon.get("abilities", []):
                ability = ability_info.get("ability", {})
                ability_name = ability.get("name")
                
                if ability_name and ability_name not in abilities_set:
                    abilities_set.add(ability_name)
                    
                    ability_detail = {
                        "name": ability_name,
                        "url": ability.get("url"),
                        "is_hidden": ability_info.get("is_hidden", False),
                        "slot": ability_info.get("slot")
                    }
                    abilities_data.append(ability_detail)
        
        self._save_file("abilities.json", abilities_data)
        print(f"✅ Saved {len(abilities_data)} unique abilities")
    
    def _separate_types(self, all_data: List[Dict[str, Any]]):
        """Extract unique types information"""
        print("🎨 Separating types data...")
        
        types_set = set()
        types_data = []
        
        for pokemon in all_data:
            for type_info in pokemon.get("types", []):
                type_data = type_info.get("type", {})
                type_name = type_data.get("name")
                
                if type_name and type_name not in types_set:
                    types_set.add(type_name)
                    
                    type_detail = {
                        "name": type_name,
                        "url": type_data.get("url"),
                        "slot": type_info.get("slot")
                    }
                    types_data.append(type_detail)
        
        self._save_file("types.json", types_data)
        print(f"✅ Saved {len(types_data)} unique types")
    
    def _separate_stats(self, all_data: List[Dict[str, Any]]):
        """Extract unique stats information"""
        print("📊 Separating stats data...")
        
        stats_set = set()
        stats_data = []
        
        for pokemon in all_data:
            for stat_info in pokemon.get("stats", []):
                stat = stat_info.get("stat", {})
                stat_name = stat.get("name")
                
                if stat_name and stat_name not in stats_set:
                    stats_set.add(stat_name)
                    
                    stat_detail = {
                        "name": stat_name,
                        "url": stat.get("url"),
                        "base_stat": stat_info.get("base_stat"),
                        "effort": stat_info.get("effort")
                    }
                    stats_data.append(stat_detail)
        
        self._save_file("stats.json", stats_data)
        print(f"✅ Saved {len(stats_data)} unique stats")
    
    def _save_file(self, filename: str, data: Any):
        """Save data to a JSON file"""
        file_path = Path(self.assets_dir) / filename
        
        with open(file_path, 'w', encoding='utf-8') as f:
            json.dump(data, f, indent=2, ensure_ascii=False)
        
        # Get file size
        file_size = file_path.stat().st_size
        size_mb = file_size / (1024 * 1024)
        print(f"   📁 {filename}: {size_mb:.2f} MB")

def main():
    """Main execution"""
    separator = PokemonDataSeparator()
    separator.separate_data()

if __name__ == "__main__":
    main()
