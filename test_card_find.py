"""Simple test to find a specific card"""
from pokemontcgsdk import RestClient, Card
import os
import time

# Configure API key - try environment variable first, fall back to hardcoded
API_KEY = os.environ.get("POKEMONTCG_IO_API_KEY") or "12464cb1-d83b-4d98-a212-a680284f1189"
RestClient.configure(API_KEY)

print("Testing Card.find('xy1-1')...")
print("=" * 60)

max_retries = 3
retry_delay = 2

for attempt in range(max_retries):
    try:
        if attempt > 0:
            print(f"\nRetry attempt {attempt + 1}/{max_retries}...")
            time.sleep(retry_delay * attempt)
        
        print(f"Using API key: {API_KEY[:20]}...")
        print("\nFetching card 'xy1-1'...")
        
        card = Card.find('xy1-1')
        
        print("\nCard found successfully!")
        print(f"  Card ID: {card.id}")
        print(f"  Name: {card.name}")
        
        if hasattr(card, 'set') and card.set:
            print(f"  Set: {card.set.name} ({card.set.id})")
        
        if hasattr(card, 'national_pokedex_numbers') and card.national_pokedex_numbers:
            print(f"  National Pokedex Numbers: {card.national_pokedex_numbers}")
        
        if hasattr(card, 'images') and card.images:
            if hasattr(card.images, 'small'):
                print(f"  Small Image: {card.images.small}")
            if hasattr(card.images, 'large'):
                print(f"  Large Image: {card.images.large}")
        
        if hasattr(card, 'hp'):
            print(f"  HP: {card.hp}")
        
        if hasattr(card, 'types'):
            print(f"  Types: {card.types}")
        
        print("\n" + "=" * 60)
        print("Test successful!")
        break  # Success, exit retry loop
        
    except Exception as e:
        if attempt < max_retries - 1:
            error_msg = str(e) if hasattr(e, '__str__') else repr(e)
            print(f"\nAttempt {attempt + 1} failed: {error_msg}")
            if "504" in error_msg or "Timeout" in error_msg:
                print(f"Server timeout. Retrying in {retry_delay * (attempt + 1)} seconds...")
            else:
                print(f"Retrying in {retry_delay * (attempt + 1)} seconds...")
            continue
        else:
            # Last attempt failed
            print("\nAll attempts failed:")
            error_msg = str(e) if hasattr(e, '__str__') else repr(e)
            print(f"  {error_msg}")
            print(f"\nError type: {type(e).__name__}")
            import traceback
            traceback.print_exc()
