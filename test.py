import requests
import time

API_KEY = "12464cb1-d83b-4d98-a212-a680284f1189"
CARD_ID = "sv01-15"  # Charizard Base Set
URL = f"https://api.pokemontcg.io/v2/cards/{CARD_ID}"

HEADERS = {
    "X-Api-Key": API_KEY,
    "User-Agent": "Mozilla/5.0 (Debug Script; +https://example.com)",
    "Accept": "application/json"
}

MAX_RETRIES = 3
TIMEOUT_SECONDS = 30

for attempt in range(1, MAX_RETRIES + 1):
    try:
        print(f"Attempt {attempt}...")
        response = requests.get(
            URL,
            headers=HEADERS,
            timeout=TIMEOUT_SECONDS
        )
        response.raise_for_status()

        data = response.json()["data"]

        print("SUCCESS!")
        print("ID:", data["id"])
        print("Name:", data["name"])
        print("Set:", data["set"]["name"])
        print("Image:", data["images"]["large"])
        break

    except requests.exceptions.RequestException as e:
        print("ERROR:", e)

        if attempt < MAX_RETRIES:
            time.sleep(3)
        else:
            print("FAILED after retries")
