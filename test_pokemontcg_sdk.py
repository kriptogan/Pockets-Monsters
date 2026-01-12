"""Test script for pokemontcg Python SDK"""
from pokemontcgsdk import RestClient, Card
import time
import os

# Configure API key - try environment variable first, fall back to hardcoded
API_KEY = os.environ.get("POKEMONTCG_IO_API_KEY") or "12464cb1-d83b-4d98-a212-a680284f1189"
RestClient.configure(API_KEY)

print("Testing pokemontcg Python SDK...")
print("=" * 60)

# Test 1: Search for cards (simpler query)
print("\n1. Testing Card.where() - Getting first 5 cards:")
try:
    cards = Card.where(pageSize=5)
    print(f"   ✓ Retrieved {len(cards)} cards")
    for i, card in enumerate(cards, 1):
        print(f"   {i}. {card.name} (Set: {card.set.name if hasattr(card.set, 'name') else 'N/A'})")
        if hasattr(card, 'national_pokedex_numbers') and card.national_pokedex_numbers:
            print(f"      Pokemon IDs: {card.national_pokedex_numbers}")
except Exception as e:
    error_msg = str(e) if hasattr(e, '__str__') else repr(e)
    print(f"   ✗ Error: {error_msg}")

# Test 2: Search for Pokemon cards only
print("\n2. Testing Card.where() - Getting Pokemon cards (first 3):")
try:
    cards = Card.where(q='supertype:pokemon', pageSize=3)
    print(f"   ✓ Retrieved {len(cards)} Pokemon cards")
    for i, card in enumerate(cards, 1):
        print(f"   {i}. {card.name}")
        if hasattr(card, 'national_pokedex_numbers') and card.national_pokedex_numbers:
            print(f"      Pokemon IDs: {card.national_pokedex_numbers}")
except Exception as e:
    error_msg = str(e) if hasattr(e, '__str__') else repr(e)
    print(f"   ✗ Error: {error_msg}")

# Test 3: Check pagination
print("\n3. Testing pagination - Page 1 vs Page 2:")
try:
    page1 = Card.where(page=1, pageSize=3)
    print(f"   ✓ Page 1: {len(page1)} cards")
    if len(page1) > 0:
        print(f"      First card: {page1[0].name}")
    
    time.sleep(1)  # Small delay
    
    page2 = Card.where(page=2, pageSize=3)
    print(f"   ✓ Page 2: {len(page2)} cards")
    if len(page2) > 0:
        print(f"      First card: {page2[0].name}")
except Exception as e:
    error_msg = str(e) if hasattr(e, '__str__') else repr(e)
    print(f"   ✗ Error: {error_msg}")

print("\n" + "=" * 60)
print("SDK test complete!")
