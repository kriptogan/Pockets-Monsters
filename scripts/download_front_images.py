#!/usr/bin/env python3
"""
Download front images (official artwork) of all Pokémon for use in detail pages
"""

import json
import os
import requests
import time
from pathlib import Path

def download_front_images():
    # Paths
    pokemons_file = "app/src/main/assets/pokemons.json"
    front_images_dir = "app/src/main/assets/front_images"
    
    # Create front_images directory if it doesn't exist
    Path(front_images_dir).mkdir(parents=True, exist_ok=True)
    
    if not os.path.exists(pokemons_file):
        print(f"❌ File not found: {pokemons_file}")
        return False
    
    print("🖼️  Downloading front images (official artwork) for all Pokémon...")
    
    try:
        # Load Pokémon data
        with open(pokemons_file, 'r', encoding='utf-8') as f:
            pokemons = json.load(f)
        
        print(f"📖 Found {len(pokemons)} Pokémon")
        
        # Download front images
        success_count = 0
        failed_count = 0
        
        for i, pokemon in enumerate(pokemons):
            pokemon_id = pokemon['id']
            pokemon_name = pokemon['name']
            
            # Front image URL (official artwork)
            front_image_url = f"https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/{pokemon_id}.png"
            
            # Output file path
            output_file = os.path.join(front_images_dir, f"{pokemon_name}.png")
            
            print(f"📥 [{i+1}/{len(pokemons)}] Downloading {pokemon_name} (ID: {pokemon_id})...")
            
            try:
                # Download the image
                response = requests.get(front_image_url, timeout=30)
                
                if response.status_code == 200:
                    # Save the image
                    with open(output_file, 'wb') as f:
                        f.write(response.content)
                    
                    # Get file size
                    file_size = len(response.content) / 1024  # KB
                    print(f"  ✅ Saved {pokemon_name}.png ({file_size:.1f} KB)")
                    success_count += 1
                else:
                    print(f"  ❌ Failed to download {pokemon_name}: HTTP {response.status_code}")
                    failed_count += 1
                
                # Small delay to be respectful to the API
                time.sleep(0.1)
                
            except Exception as e:
                print(f"  ❌ Error downloading {pokemon_name}: {e}")
                failed_count += 1
        
        # Summary
        print(f"\n🎉 Download completed!")
        print(f"✅ Successfully downloaded: {success_count}")
        print(f"❌ Failed: {failed_count}")
        print(f"📁 Front images saved to: {front_images_dir}")
        
        # Check total size
        total_size = 0
        for file in os.listdir(front_images_dir):
            if file.endswith('.png'):
                file_path = os.path.join(front_images_dir, file)
                total_size += os.path.getsize(file_path)
        
        total_size_mb = total_size / (1024 * 1024)
        print(f"💾 Total size: {total_size_mb:.1f} MB")
        
        return True
        
    except Exception as e:
        print(f"❌ Error: {e}")
        return False

if __name__ == "__main__":
    success = download_front_images()
    if success:
        print("\n🎉 Front image download completed successfully!")
    else:
        print("\n💥 Failed to download front images!")
