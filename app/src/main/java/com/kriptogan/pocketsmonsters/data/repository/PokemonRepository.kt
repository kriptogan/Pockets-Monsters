package com.kriptogan.pocketsmonsters.data.repository

import com.kriptogan.pocketsmonsters.data.api.PokeApiService
import com.kriptogan.pocketsmonsters.data.local.LocalStorage
import com.kriptogan.pocketsmonsters.data.offline.OfflineDataLoader
import com.kriptogan.pocketsmonsters.data.models.Pokemon
import com.kriptogan.pocketsmonsters.data.network.NetworkModule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PokemonRepository(
    private val localStorage: LocalStorage,
    private val offlineDataLoader: OfflineDataLoader
) {
    
    private val apiService: PokeApiService = NetworkModule.createPokeApiService()
    
    /**
     * Get a list of Pokémon with offline data priority
     * @return List of Pokemon or empty list if error occurs
     */
    suspend fun getPokemonList(): List<Pokemon> = withContext(Dispatchers.IO) {
        try {
            // First try offline data
            if (offlineDataLoader.isOfflineDataAvailable()) {
                offlineDataLoader.loadAllPokemonData()
            } else {
                // Fallback to local storage if available
                val localData = localStorage.getPokemonDetails()
                if (localData != null && localData.isNotEmpty()) {
                    localData.values.toList()
                } else {
                    emptyList()
                }
            }
        } catch (e: Exception) {
            println("Error loading Pokémon list: ${e.message}")
            emptyList()
        }
    }
    
    /**
     * Get a specific Pokémon by name
     * @return Result containing the Pokémon or failure
     */
    suspend fun getPokemon(name: String): Result<Pokemon> = withContext(Dispatchers.IO) {
        try {
            // First try offline data
            if (offlineDataLoader.isOfflineDataAvailable()) {
                val pokemon = offlineDataLoader.loadPokemon(name)
                if (pokemon != null) {
                    Result.success(pokemon)
                } else {
                    Result.failure(Exception("Pokémon not found: $name"))
                }
            } else {
                // Fallback to local storage
                val localData = localStorage.getPokemonDetails()
                val pokemon = localData?.get(name.lowercase())
                if (pokemon != null) {
                    Result.success(pokemon)
                } else {
                    // Last resort: try API
                    val apiPokemon = apiService.getPokemon(name.lowercase())
                    Result.success(apiPokemon)
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get a specific Pokémon by ID
     * @return Result containing the Pokémon or failure
     */
    suspend fun getPokemonById(id: Int): Result<Pokemon> = withContext(Dispatchers.IO) {
        try {
            // First try offline data
            if (offlineDataLoader.isOfflineDataAvailable()) {
                val pokemon = offlineDataLoader.loadPokemonById(id)
                if (pokemon != null) {
                    Result.success(pokemon)
                } else {
                    Result.failure(Exception("Pokémon not found with ID: $id"))
                }
            } else {
                // Fallback to local storage
                val localData = localStorage.getPokemonDetails()
                val pokemon = localData?.values?.find { it.id == id }
                if (pokemon != null) {
                    Result.success(pokemon)
                } else {
                    // Last resort: try API
                    val apiPokemon = apiService.getPokemon(id.toString())
                    Result.success(apiPokemon)
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get all Pokémon data from offline assets
     * @return List of all Pokémon with full details
     */
    suspend fun getAllPokemonData(): List<Pokemon> = withContext(Dispatchers.IO) {
        try {
            println("🔍 DEBUG: Repository.getAllPokemonData() called")
            println("🔍 DEBUG: Offline data available: ${offlineDataLoader.isOfflineDataAvailable()}")
            
            if (offlineDataLoader.isOfflineDataAvailable()) {
                println("🔍 DEBUG: Calling offlineDataLoader.loadAllPokemonData()")
                val result = offlineDataLoader.loadAllPokemonData()
                println("🔍 DEBUG: OfflineDataLoader returned ${result.size} Pokémon")
                if (result.isNotEmpty()) {
                    val firstPokemon = result.first()
                    println("🔍 DEBUG: First Pokémon: ${firstPokemon.name}, Sprite: ${firstPokemon.spritePath}")
                }
                result
            } else {
                println("🔍 DEBUG: No offline data available, returning empty list")
                emptyList()
            }
        } catch (e: Exception) {
            println("❌ Repository.getAllPokemonData() failed: ${e.message}")
            e.printStackTrace()
            emptyList()
        }
    }
    
    /**
     * Check if offline data is available
     */
    fun isOfflineDataAvailable(): Boolean {
        return offlineDataLoader.isOfflineDataAvailable()
    }
    
    /**
     * Check if local data is available
     */
    fun isLocalDataAvailable(): Boolean {
        return offlineDataLoader.isOfflineDataAvailable() || localStorage.isDataAvailable()
    }
    
    /**
     * Check if detailed Pokémon data is available locally
     */
    fun isDetailedDataAvailable(): Boolean {
        return offlineDataLoader.isOfflineDataAvailable() || localStorage.isDetailedDataAvailable()
    }
    
    /**
     * Get formatted last update time
     */
    fun getLastUpdateTime(): String {
        return if (offlineDataLoader.isOfflineDataAvailable()) {
            "Offline data available"
        } else {
            localStorage.getFormattedLastUpdateTime()
        }
    }
    
    /**
     * Get download progress
     */
    fun getDownloadProgress(): Pair<Int, Int> {
        return localStorage.getDownloadProgress()
    }
    
    /**
     * Get offline data metadata
     */
    suspend fun getOfflineDataMetadata(): Map<String, Any>? {
        return if (offlineDataLoader.isOfflineDataAvailable()) {
            try {
                offlineDataLoader.loadMetadata()
            } catch (e: Exception) {
                null
            }
        } else {
            null
        }
    }
}
