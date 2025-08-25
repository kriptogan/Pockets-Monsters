#!/usr/bin/env python3
"""
Script to download all Pokémon data and save to assets directory
Run this script to populate the assets folder with offline data

Usage: py scripts/download_pokemon_data.py
"""

import json
import os
import time
import urllib.request
from pathlib import Path
from typing import Dict, List, Any

class PokemonDataDownloader:
    def __init__(self):
        self.base_url = "https://pokeapi.co/api/v2/"
        
    def download_all_data(self, assets_dir: str):
        """Download all Pokémon data and save to assets directory"""
        try:
            print("🚀 Starting Pokémon data download...")
            
            # Create assets directory if it doesn't exist
            assets_path = Path(assets_dir)
            assets_path.mkdir(parents=True, exist_ok=True)
            print(f"📁 Created assets directory: {assets_path.absolute()}")
            
            # Step 1: Download basic list
            print("📋 Downloading Pokémon list...")
            pokemon_list = self.download_pokemon_list()
            total = len(pokemon_list["results"])
            print(f"✅ Found {total} Pokémon to download")
            
            # Step 2: Download detailed data for each Pokémon
            pokemon_details = []
            sprite_urls = {}
            
            for index, pokemon_item in enumerate(pokemon_list["results"]):
                try:
                    print(f"📥 Downloading {pokemon_item['name']} ({index + 1}/{total})")
                    
                    pokemon = self.download_pokemon(pokemon_item["name"].lower())
                    pokemon_details.append(pokemon)
                    
                    # Extract front sprite URL
                    front_sprite_url = pokemon.get("sprites", {}).get("front_default")
                    if front_sprite_url:
                        sprite_urls[pokemon["name"]] = front_sprite_url
                    
                    # Small delay to be respectful to the API
                    time.sleep(0.1)
                    
                except Exception as e:
                    print(f"❌ Failed to download {pokemon_item['name']}: {str(e)}")
            
            # Step 3: Save Pokémon data to JSON
            print("💾 Saving Pokémon data...")
            pokemon_data_file = assets_path / "pokemon_data.json"
            with open(pokemon_data_file, 'w', encoding='utf-8') as f:
                json.dump(pokemon_details, f, indent=2, ensure_ascii=False)
            print(f"✅ Saved Pokémon data to: {pokemon_data_file}")
            
            # Step 4: Save sprite URLs mapping
            print("🔗 Saving sprite URLs...")
            sprite_urls_file = assets_path / "sprite_urls.json"
            with open(sprite_urls_file, 'w', encoding='utf-8') as f:
                json.dump(sprite_urls, f, indent=2, ensure_ascii=False)
            print(f"✅ Saved sprite URLs to: {sprite_urls_file}")
            
            # Step 5: Download and save sprites
            print("🖼️ Downloading sprites...")
            sprites_dir = assets_path / "sprites"
            sprites_dir.mkdir(exist_ok=True)
            
            successful_sprites = 0
            for pokemon_name, url in sprite_urls.items():
                try:
                    self.download_sprite(url, sprites_dir / f"{pokemon_name.lower()}.png")
                    successful_sprites += 1
                except Exception as e:
                    print(f"❌ Failed to download sprite for {pokemon_name}: {str(e)}")
            print(f"✅ Downloaded {successful_sprites} sprites")
            
            # Step 6: Save metadata
            timestamp = int(time.time() * 1000)
            formatted_date = time.strftime("%Y-%m-%d %H:%M:%S", time.localtime(timestamp / 1000))
            
            metadata = {
                "total_pokemon": total,
                "successful_sprites": successful_sprites,
                "download_timestamp": timestamp,
                "download_date": formatted_date,
                "version": "1.0.0",
                "description": "Offline Pokémon data for Pockets & Monsters app"
            }
            
            metadata_file = assets_path / "metadata.json"
            with open(metadata_file, 'w', encoding='utf-8') as f:
                json.dump(metadata, f, indent=2, ensure_ascii=False)
            print(f"✅ Saved metadata to: {metadata_file}")
            
            # Summary
            print("\n🎉 Data download completed successfully!")
            print("📊 Summary:")
            print(f"   • Total Pokémon: {total}")
            print(f"   • Sprites downloaded: {successful_sprites}")
            print(f"   • Download date: {formatted_date}")
            print(f"   • Assets directory: {assets_path.absolute()}")
            print("\n📱 You can now build your app with offline data!")
            
        except Exception as e:
            print(f"💥 Data download failed: {str(e)}")
            raise
    
    def download_pokemon_list(self) -> Dict[str, Any]:
        """Download the list of all Pokémon"""
        url = f"{self.base_url}pokemon?limit=1008"
        with urllib.request.urlopen(url) as response:
            if response.status != 200:
                raise Exception(f"Failed to download Pokémon list: {response.status}")
            data = response.read()
            return json.loads(data.decode('utf-8'))
    
    def download_pokemon(self, name: str) -> Dict[str, Any]:
        """Download detailed information about a specific Pokémon"""
        url = f"{self.base_url}pokemon/{name}"
        with urllib.request.urlopen(url) as response:
            if response.status != 200:
                raise Exception(f"Failed to download Pokémon {name}: {response.status}")
            data = response.read()
            return json.loads(data.decode('utf-8'))
    
    def download_sprite(self, url: str, output_file: Path):
        """Download a single sprite image"""
        with urllib.request.urlopen(url) as response:
            if response.status != 200:
                raise Exception(f"Failed to download sprite from {url}: {response.status}")
            with open(output_file, 'wb') as f:
                f.write(response.read())

def main():
    """Main execution"""
    assets_dir = "app/src/main/assets"
    
    downloader = PokemonDataDownloader()
    downloader.download_all_data(assets_dir)

if __name__ == "__main__":
    main()
