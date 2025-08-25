package com.kriptogan.pocketsmonsters.data.offline

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.kriptogan.pocketsmonsters.data.models.Pokemon
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Loads all Pokémon data from assets (offline mode)
 * Uses cleaned and optimized data structure
 */
class OfflineDataLoader(private val context: Context) {
    private val gson = Gson()

    companion object {
        private const val POKEMONS_FILE = "pokemons.json"
        private const val MOVES_FILE = "moves.json"
        private const val ABILITIES_FILE = "abilities.json"
        private const val TYPES_FILE = "types.json"
        private const val STATS_FILE = "stats.json"
        private const val SPRITE_URLS_FILE = "sprite_urls.json"
        private const val METADATA_FILE = "metadata.json"
        private const val SPRITES_DIR = "sprites"
    }

    /**
     * Check if offline data is available
     */
    fun isOfflineDataAvailable(): Boolean {
        val result = try {
            val available = context.assets.list("")?.contains(POKEMONS_FILE) == true
            println("🔍 DEBUG: isOfflineDataAvailable() - POKEMONS_FILE exists: $available")
            
            if (available) {
                val spritesDir = context.assets.list("sprites")
                println("🔍 DEBUG: Sprites directory contents: ${spritesDir?.size ?: 0} files")
                if (spritesDir != null && spritesDir.isNotEmpty()) {
                    println("🔍 DEBUG: First 5 sprite files: ${spritesDir.take(5)}")
                }
            }
            
            available
        } catch (e: Exception) {
            println("❌ ERROR in isOfflineDataAvailable(): ${e.message}")
            false
        }
        return result
    }

    /**
     * Load all Pokémon data from assets
     */
    suspend fun loadAllPokemonData(): List<Pokemon> = withContext(Dispatchers.IO) {
        try {
            val json = context.assets.open(POKEMONS_FILE).bufferedReader().use { it.readText() }
            val type = object : TypeToken<List<Pokemon>>() {}.type
            val result = gson.fromJson<List<Pokemon>>(json, type) ?: emptyList()
            
            // Debug logging
            println("🔍 DEBUG: Loaded ${result.size} Pokémon from offline data")
            if (result.isNotEmpty()) {
                val firstPokemon = result.first()
                println("🔍 DEBUG: First Pokémon: ${firstPokemon.name}, ID: ${firstPokemon.id}, Sprite: ${firstPokemon.spritePath}")
                
                // Check if sprite file exists
                val spriteExists = context.assets.list("sprites")?.contains(firstPokemon.spritePath) == true
                println("�� DEBUG: Sprite file exists: $spriteExists")
                
                // List available sprites
                val availableSprites = context.assets.list("sprites")?.take(10) ?: emptyList()
                println("🔍 DEBUG: Available sprites (first 10): $availableSprites")
            }
            
            println("Successfully loaded ${result.size} Pokémon from offline data")
            result
        } catch (e: Exception) {
            println("Failed to load Pokémon data: ${e.message}")
            e.printStackTrace()
            emptyList()
        }
    }

    /**
     * Load a specific Pokémon by name
     */
    suspend fun loadPokemon(name: String): Pokemon? = withContext(Dispatchers.IO) {
        try {
            val allPokemon = loadAllPokemonData()
            allPokemon.find { it.name.lowercase() == name.lowercase() }
        } catch (e: Exception) {
            println("Failed to load Pokémon $name: ${e.message}")
            null
        }
    }

    /**
     * Load a specific Pokémon by ID
     */
    suspend fun loadPokemonById(id: Int): Pokemon? = withContext(Dispatchers.IO) {
        try {
            val allPokemon = loadAllPokemonData()
            allPokemon.find { it.id == id }
        } catch (e: Exception) {
            println("Failed to load Pokémon with ID $id: ${e.message}")
            null
        }
    }
    
    /**
     * Load Pokémon list (basic info) from assets - for backward compatibility
     */
    suspend fun loadPokemonList(): List<Pokemon> = withContext(Dispatchers.IO) {
        try {
            // For offline data, we return all Pokémon since we have full data
            loadAllPokemonData()
        } catch (e: Exception) {
            println("Failed to load Pokémon list: ${e.message}")
            emptyList()
        }
    }

    /**
     * Load moves data
     */
    suspend fun loadMoves(): List<Map<String, Any>> = withContext(Dispatchers.IO) {
        try {
            val json = context.assets.open(MOVES_FILE).bufferedReader().use { it.readText() }
            val type = object : TypeToken<List<Map<String, Any>>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        } catch (e: Exception) {
            println("Failed to load moves: ${e.message}")
            emptyList()
        }
    }

    /**
     * Load abilities data
     */
    suspend fun loadAbilities(): List<Map<String, Any>> = withContext(Dispatchers.IO) {
        try {
            val json = context.assets.open(ABILITIES_FILE).bufferedReader().use { it.readText() }
            val type = object : TypeToken<List<Map<String, Any>>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        } catch (e: Exception) {
            println("Failed to load abilities: ${e.message}")
            emptyList()
        }
    }

    /**
     * Load types data
     */
    suspend fun loadTypes(): List<Map<String, Any>> = withContext(Dispatchers.IO) {
        try {
            val json = context.assets.open(TYPES_FILE).bufferedReader().use { it.readText() }
            val type = object : TypeToken<List<Map<String, Any>>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        } catch (e: Exception) {
            println("Failed to load types: ${e.message}")
            emptyList()
        }
    }

    /**
     * Load stats data
     */
    suspend fun loadStats(): List<Map<String, Any>> = withContext(Dispatchers.IO) {
        try {
            val json = context.assets.open(STATS_FILE).bufferedReader().use { it.readText() }
            val type = object : TypeToken<List<Map<String, Any>>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        } catch (e: Exception) {
            println("Failed to load stats: ${e.message}")
            emptyList()
        }
    }

    /**
     * Load sprite URLs mapping
     */
    suspend fun loadSpriteUrls(): Map<String, String> = withContext(Dispatchers.IO) {
        try {
            val json = context.assets.open(SPRITE_URLS_FILE).bufferedReader().use { it.readText() }
            val type = object : TypeToken<Map<String, String>>() {}.type
            gson.fromJson(json, type) ?: emptyMap()
        } catch (e: Exception) {
            println("Failed to load sprite URLs: ${e.message}")
            emptyMap()
        }
    }

    /**
     * Load metadata about the offline data
     */
    suspend fun loadMetadata(): Map<String, Any> = withContext(Dispatchers.IO) {
        try {
            val json = context.assets.open(METADATA_FILE).bufferedReader().use { it.readText() }
            val type = object : TypeToken<Map<String, Any>>() {}.type
            gson.fromJson(json, type) ?: emptyMap()
        } catch (e: Exception) {
            println("Failed to load metadata: ${e.message}")
            emptyMap()
        }
    }

    /**
     * Get sprite file path for a Pokémon
     */
    fun getSpritePath(pokemonName: String): String? {
        return try {
            val sprites = context.assets.list(SPRITES_DIR)
            sprites?.find { it.lowercase() == "${pokemonName.lowercase()}.png" }
                ?.let { "$SPRITES_DIR/$it" }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Check if sprite exists for a Pokémon
     */
    fun hasSprite(pokemonName: String): Boolean {
        return getSpritePath(pokemonName) != null
    }

    /**
     * Get total number of Pokémon in offline data
     */
    suspend fun getTotalPokemonCount(): Int {
        return try {
            val metadata = loadMetadata()
            metadata["total_pokemon"] as? Int ?: 0
        } catch (e: Exception) {
            0
        }
    }

    /**
     * Get download timestamp of offline data
     */
    suspend fun getDownloadTimestamp(): Long {
        return try {
            val metadata = loadMetadata()
            metadata["download_timestamp"] as? Long ?: 0L
        } catch (e: Exception) {
            0L
        }
    }

    /**
     * Get version of offline data
     */
    suspend fun getDataVersion(): String {
        return try {
            val metadata = loadMetadata()
            metadata["version"] as? String ?: "unknown"
        } catch (e: Exception) {
            "unknown"
        }
    }
}
