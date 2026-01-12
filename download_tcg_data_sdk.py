"""Download Pokémon TCG card data using pokemontcg Python SDK"""
import json
import time
from pathlib import Path
from collections import defaultdict
import sys
import os
from pokemontcgsdk import RestClient, Card

# API Configuration
# Try to get API key from environment variable first, fall back to hardcoded
API_KEY = os.environ.get("POKEMONTCG_IO_API_KEY") or "12464cb1-d83b-4d98-a212-a680284f1189"
RestClient.configure(API_KEY)

# Output configuration
OUTPUT_DIR = "app/src/main/assets/tcg_data1"
CHECKPOINT_FILE = "tcg_download_checkpoint_sdk.json"
PAGE_SIZE = 100  # Cards per API page request
SAVE_INTERVAL = 100  # Save progress every N cards processed

def save_checkpoint(checkpoint_file, all_cards, last_page, stats):
    """Save progress checkpoint"""
    try:
        checkpoint_data = {
            "all_cards": all_cards,
            "last_page": last_page,
            "stats": stats
        }
        with open(checkpoint_file, 'w', encoding='utf-8') as f:
            json.dump(checkpoint_data, f, indent=2, ensure_ascii=False)
        return True
    except Exception as e:
        print(f"  Warning: Could not save checkpoint: {e}", flush=True)
        sys.stdout.flush()
        return False

def load_checkpoint(checkpoint_file):
    """Load progress checkpoint if it exists"""
    if not os.path.exists(checkpoint_file):
        return None
    
    try:
        with open(checkpoint_file, 'r', encoding='utf-8') as f:
            checkpoint_data = json.load(f)
        return checkpoint_data
    except Exception as e:
        print(f"  Warning: Could not load checkpoint: {e}", flush=True)
        sys.stdout.flush()
        return None

def card_to_dict(card):
    """Convert SDK Card object to dictionary"""
    try:
        card_dict = {
            "id": card.id if hasattr(card, 'id') else "",
            "name": card.name if hasattr(card, 'name') else "",
            "nationalPokedexNumbers": card.national_pokedex_numbers if hasattr(card, 'national_pokedex_numbers') else [],
            "set": {
                "id": card.set.id if hasattr(card, 'set') and hasattr(card.set, 'id') else "",
                "name": card.set.name if hasattr(card, 'set') and hasattr(card.set, 'name') else ""
            },
            "images": {
                "small": card.images.small if hasattr(card, 'images') and hasattr(card.images, 'small') else "",
                "large": card.images.large if hasattr(card, 'images') and hasattr(card.images, 'large') else ""
            }
        }
        return card_dict
    except Exception as e:
        print(f"  Error converting card to dict: {e}", flush=True)
        sys.stdout.flush()
        return None

def fetch_all_cards():
    """Fetch all cards from pokemontcg.io using SDK"""
    print("Starting to fetch all TCG cards using pokemontcg SDK...", flush=True)
    print("STEP 1: Cards Data Only (NO images will be downloaded)", flush=True)
    sys.stdout.flush()
    
    # Try to load checkpoint
    checkpoint = load_checkpoint(CHECKPOINT_FILE)
    if checkpoint:
        print(f"\nFound checkpoint file. Resuming from previous session...", flush=True)
        sys.stdout.flush()
        all_cards = checkpoint.get("all_cards", [])
        last_page = checkpoint.get("last_page", 0)
        stats = checkpoint.get("stats", {})
        processed = stats.get("processed", 0)
        failed = stats.get("failed", 0)
        print(f"Resuming: {len(all_cards)} cards already fetched, last page: {last_page}", flush=True)
        sys.stdout.flush()
    else:
        print(f"\nNo checkpoint found. Starting fresh...", flush=True)
        sys.stdout.flush()
        all_cards = []
        last_page = 0
        processed = 0
        failed = 0
    
    print(f"\nFetching cards (page size: {PAGE_SIZE})...", flush=True)
    sys.stdout.flush()
    
    page_num = last_page + 1
    max_retries = 3
    retry_delay = 2
    
    while True:
        # Skip if already processed
        if page_num <= last_page:
            page_num += 1
            continue
        
        retries = 0
        cards_fetched = False
        
        while retries < max_retries:
            try:
                print(f"  Fetching page {page_num}...", flush=True)
                sys.stdout.flush()
                
                # Fetch page using SDK
                cards = Card.where(page=page_num, pageSize=PAGE_SIZE)
                
                if not cards or len(cards) == 0:
                    print(f"  No more cards found. Finished at page {page_num - 1}", flush=True)
                    sys.stdout.flush()
                    cards_fetched = True
                    break
                
                # Process cards
                page_cards = []
                for card in cards:
                    card_dict = card_to_dict(card)
                    if card_dict:
                        # Only include Pokemon cards (those with nationalPokedexNumbers)
                        if card_dict.get("nationalPokedexNumbers"):
                            page_cards.append(card_dict)
                            processed += 1
                        else:
                            # Still count non-Pokemon cards but don't include them
                            pass
                
                all_cards.extend(page_cards)
                print(f"  Page {page_num}: Got {len(cards)} cards, {len(page_cards)} Pokemon cards", flush=True)
                sys.stdout.flush()
                
                cards_fetched = True
                break
                
            except Exception as e:
                retries += 1
                error_msg = str(e) if hasattr(e, '__str__') else repr(e)
                if retries < max_retries:
                    wait_time = retry_delay * (2 ** (retries - 1))
                    print(f"  Error fetching page {page_num} (attempt {retries}/{max_retries}): {error_msg}", flush=True)
                    print(f"  Retrying in {wait_time} seconds...", flush=True)
                    sys.stdout.flush()
                    time.sleep(wait_time)
                else:
                    print(f"  Failed to fetch page {page_num} after {max_retries} attempts: {error_msg}", flush=True)
                    sys.stdout.flush()
                    failed += 1
                    break
        
        if not cards_fetched or (cards and len(cards) == 0):
            # No more cards or failed to fetch
            break
        
        # Save checkpoint and intermediate data every SAVE_INTERVAL cards
        if processed % SAVE_INTERVAL == 0:
            stats = {
                "processed": processed,
                "failed": failed
            }
            if save_checkpoint(CHECKPOINT_FILE, all_cards, page_num, stats):
                print(f"  ✓ Checkpoint saved ({processed} Pokemon cards processed)", flush=True)
                sys.stdout.flush()
            
            # Save intermediate paged data
            if len(all_cards) > 0:
                try:
                    pokemon_data = group_cards_by_pokemon(all_cards)
                    tcg_data = transform_to_tcg_data_format(pokemon_data)
                    save_tcg_data_paged(tcg_data, OUTPUT_DIR, page_size=50)
                    print(f"  ✓ Intermediate data saved to {OUTPUT_DIR}/ ({len(tcg_data)} Pokémon)", flush=True)
                    sys.stdout.flush()
                except Exception as e:
                    print(f"  Warning: Could not save intermediate data: {e}", flush=True)
                    sys.stdout.flush()
        
        # Show progress
        if processed % 100 == 0:
            print(f"Progress: {processed} Pokemon cards processed, {failed} failed", flush=True)
            sys.stdout.flush()
        
        page_num += 1
        
        # Delay between pages to avoid overwhelming the API
        time.sleep(1)
    
    print(f"\nTotal Pokemon cards fetched: {len(all_cards)}", flush=True)
    print(f"Failed pages: {failed}", flush=True)
    sys.stdout.flush()
    
    # Save final checkpoint
    stats = {
        "processed": processed,
        "failed": failed
    }
    save_checkpoint(CHECKPOINT_FILE, all_cards, page_num - 1, stats)
    
    # Remove checkpoint file on successful completion
    if os.path.exists(CHECKPOINT_FILE):
        try:
            os.remove(CHECKPOINT_FILE)
            print(f"Checkpoint file removed (download complete)", flush=True)
            sys.stdout.flush()
        except:
            pass
    
    return all_cards

def group_cards_by_pokemon(cards):
    """Group cards by their national Pokédex numbers"""
    pokemon_data = defaultdict(lambda: {
        "pokemonId": None,
        "pokemonName": None,
        "sets": {},
        "cards": []
    })
    
    print("\nGrouping cards by Pokémon...", flush=True)
    sys.stdout.flush()
    
    for card in cards:
        pokedex_numbers = card.get("nationalPokedexNumbers", [])
        pokemon_name = card.get("name", "").lower()
        
        if not pokedex_numbers:
            continue
        
        for pokedex_num in pokedex_numbers:
            if pokedex_num not in pokemon_data:
                pokemon_data[pokedex_num]["pokemonId"] = pokedex_num
                pokemon_data[pokedex_num]["pokemonName"] = pokemon_name.split()[0] if pokemon_name else f"pokemon_{pokedex_num}"
            
            pokemon_data[pokedex_num]["cards"].append(card)
            
            card_set = card.get("set", {})
            if card_set and card_set.get("id"):
                set_id = card_set["id"]
                if set_id not in pokemon_data[pokedex_num]["sets"]:
                    pokemon_data[pokedex_num]["sets"][set_id] = card_set
    
    print(f"Grouped cards for {len(pokemon_data)} Pokémon", flush=True)
    sys.stdout.flush()
    return pokemon_data

def extract_unique_sets(cards):
    """Extract unique sets from a list of cards"""
    sets_map = {}
    
    for card in cards:
        card_set = card.get("set", {})
        if card_set and card_set.get("id"):
            set_id = card_set["id"]
            if set_id not in sets_map:
                sets_map[set_id] = card_set
    
    return list(sets_map.values())

def transform_to_tcg_data_format(pokemon_data):
    """Transform the grouped data to match PokemonTCGData structure"""
    tcg_data_by_pokemon = {}
    
    print("\nTransforming data to app format...", flush=True)
    sys.stdout.flush()
    
    for pokemon_id, data in pokemon_data.items():
        sets = extract_unique_sets(data["cards"])
        sets.sort(key=lambda s: s.get("releaseDate", s.get("id", "")))
        
        tcg_data_by_pokemon[pokemon_id] = {
            "pokemonId": pokemon_id,
            "pokemonName": data["pokemonName"],
            "sets": sets,
            "cards": data["cards"]
        }
    
    print(f"Transformed data for {len(tcg_data_by_pokemon)} Pokémon", flush=True)
    sys.stdout.flush()
    return tcg_data_by_pokemon

def save_tcg_data_paged(tcg_data, output_dir, page_size=50):
    """Save TCG data to paged JSON files with an index"""
    print(f"\nSaving TCG data to paged structure in {output_dir}...", flush=True)
    sys.stdout.flush()
    
    output_path = Path(output_dir)
    output_path.mkdir(parents=True, exist_ok=True)
    
    pokemon_list = [(str(pokemon_id), data) for pokemon_id, data in sorted(tcg_data.items(), key=lambda x: int(x[0]))]
    
    index = {}
    page_num = 1
    current_page_data = {}
    
    for pokemon_id_str, pokemon_data in pokemon_list:
        current_page_data[pokemon_id_str] = pokemon_data
        
        if len(current_page_data) >= page_size:
            page_file = output_path / f"page_{page_num}.json"
            with open(page_file, 'w', encoding='utf-8') as f:
                json.dump(current_page_data, f, indent=2, ensure_ascii=False)
            
            for pid in current_page_data.keys():
                index[pid] = f"page_{page_num}.json"
            
            print(f"  Saved page {page_num} ({len(current_page_data)} Pokémon) to {page_file.name}", flush=True)
            sys.stdout.flush()
            
            page_num += 1
            current_page_data = {}
    
    if current_page_data:
        page_file = output_path / f"page_{page_num}.json"
        with open(page_file, 'w', encoding='utf-8') as f:
            json.dump(current_page_data, f, indent=2, ensure_ascii=False)
        
        for pid in current_page_data.keys():
            index[pid] = f"page_{page_num}.json"
        
        print(f"  Saved page {page_num} ({len(current_page_data)} Pokémon) to {page_file.name}", flush=True)
        sys.stdout.flush()
    
    index_file = output_path / "index.json"
    with open(index_file, 'w', encoding='utf-8') as f:
        json.dump(index, f, indent=2, ensure_ascii=False)
    
    total_size_mb = sum(page_file.stat().st_size / (1024 * 1024) for page_file in output_path.glob("page_*.json"))
    index_size_mb = index_file.stat().st_size / (1024 * 1024)
    
    print(f"\nSaved {len(pokemon_list)} Pokémon TCG data entries across {page_num} pages", flush=True)
    print(f"Index file size: {index_size_mb:.2f} MB", flush=True)
    print(f"Total data size: {total_size_mb:.2f} MB", flush=True)
    sys.stdout.flush()

def main():
    print("=" * 60)
    print("Pokémon TCG Data Downloader (pokemontcg SDK)")
    print("STEP 1: Cards Data Only (No Images)")
    print("=" * 60)
    
    print(f"Output directory: {OUTPUT_DIR}", flush=True)
    print(f"Checkpoint file: {CHECKPOINT_FILE}", flush=True)
    print(f"Progress will be saved every {SAVE_INTERVAL} cards", flush=True)
    sys.stdout.flush()
    
    # Fetch all cards
    all_cards = fetch_all_cards()
    
    if not all_cards:
        print("No cards fetched. Exiting.", flush=True)
        sys.stdout.flush()
        return
    
    # Group cards by Pokémon
    pokemon_data = group_cards_by_pokemon(all_cards)
    
    # Transform to app format
    tcg_data = transform_to_tcg_data_format(pokemon_data)
    
    # Final save
    print("\n" + "=" * 60)
    print("Finalizing data...")
    print("=" * 60, flush=True)
    sys.stdout.flush()
    
    save_tcg_data_paged(tcg_data, OUTPUT_DIR, page_size=50)
    
    print("\n" + "=" * 60)
    print("Download complete!")
    print("=" * 60, flush=True)
    sys.stdout.flush()

if __name__ == "__main__":
    main()

