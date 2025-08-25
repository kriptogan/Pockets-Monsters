package com.kriptogan.pocketsmonsters.data.repository

import com.kriptogan.pocketsmonsters.data.api.PokeApiService
import com.kriptogan.pocketsmonsters.data.local.LocalStorage
import com.kriptogan.pocketsmonsters.data.models.Pokemon
import com.kriptogan.pocketsmonsters.data.models.PokemonListResponse
import com.kriptogan.pocketsmonsters.data.network.NetworkModule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.delay

class PokemonRepository(private val localStorage: LocalStorage) {
    
    private val apiService: PokeApiService = NetworkModule.createPokeApiService()
    
    /**
     * Get a list of Pokémon with local storage support
     * @return PokemonListResponse or null if error occurs
     */
    suspend fun getPokemonList(): Result<PokemonListResponse> = withContext(Dispatchers.IO) {
        try {
            // First try to get from local storage
            val localData = localStorage.getPokemonList()
            if (localData != null) {
                return@withContext Result.success(PokemonListResponse(localData.size, null, null, localData))
            }
            
            // If no local data, fetch from API and save
            val response = apiService.getPokemonList()
            localStorage.savePokemonList(response.results)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Force refresh Pokémon list from API
     * @return PokemonListResponse or null if error occurs
     */
    suspend fun refreshPokemonList(): Result<PokemonListResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getPokemonList()
            localStorage.savePokemonList(response.results)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Download all detailed Pokémon data and store locally
     * @param onProgressUpdate Callback for progress updates (current, total)
     * @return Result indicating success or failure
     */
    suspend fun downloadAllPokemonDetails(
        onProgressUpdate: (current: Int, total: Int) -> Unit
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // Get the list of all Pokémon first
            val pokemonList = localStorage.getPokemonList()
            if (pokemonList == null) {
                return@withContext Result.failure(Exception("Pokemon list not available"))
            }
            
            val total = pokemonList.size
            val pokemonDetails = mutableMapOf<String, Pokemon>()
            
            // Download each Pokémon's details
            pokemonList.forEachIndexed { index, pokemonItem ->
                try {
                    val pokemon = apiService.getPokemon(pokemonItem.name.lowercase())
                    pokemonDetails[pokemon.name.lowercase()] = pokemon
                    
                    // Update progress
                    val current = index + 1
                    onProgressUpdate(current, total)
                    localStorage.saveDownloadProgress(current, total)
                    
                    // Small delay to avoid overwhelming the API
                    delay(50)
                    
                } catch (e: Exception) {
                    // Log error but continue with other Pokémon
                    println("Failed to fetch Pokémon: ${pokemonItem.name} - ${e.message}")
                }
            }
            
            // Save all details to local storage
            localStorage.savePokemonDetails(pokemonDetails)
            
            // Clear progress
            localStorage.saveDownloadProgress(0, 0)
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get detailed information about a specific Pokémon by name
     * @param name The name of the Pokémon
     * @return Pokemon or null if error occurs
     */
    suspend fun getPokemon(name: String): Result<Pokemon> = withContext(Dispatchers.IO) {
        try {
            // First try to get from local storage
            val localPokemon = localStorage.getPokemonDetail(name)
            if (localPokemon != null) {
                return@withContext Result.success(localPokemon)
            }
            
            // If not in local storage, fetch from API
            val response = apiService.getPokemon(name.lowercase())
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get detailed information about a specific Pokémon by ID
     * @param id The ID of the Pokémon
     * @return Pokemon or null if error occurs
     */
    suspend fun getPokemonById(id: Int): Result<Pokemon> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getPokemonById(id)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get multiple Pokémon by their names
     * @param names List of Pokémon names to fetch
     * @return List of successfully fetched Pokémon
     */
    suspend fun getMultiplePokemon(names: List<String>): List<Pokemon> = withContext(Dispatchers.IO) {
        val pokemonList = mutableListOf<Pokemon>()
        
        names.forEach { name ->
            try {
                val pokemon = getPokemon(name)
                pokemon.fold(
                    onSuccess = { pokemonList.add(it) },
                    onFailure = { println("Failed to fetch Pokémon: $name - ${it.message}") }
                )
            } catch (e: Exception) {
                // Log error but continue with other Pokémon
                println("Failed to fetch Pokémon: $name - ${e.message}")
            }
        }
        
        pokemonList
    }
    
    /**
     * Check if local data is available
     */
    fun isLocalDataAvailable(): Boolean {
        return localStorage.isDataAvailable()
    }
    
    /**
     * Check if detailed Pokémon data is available locally
     */
    fun isDetailedDataAvailable(): Boolean {
        return localStorage.isDetailedDataAvailable()
    }
    
    /**
     * Get formatted last update time
     */
    fun getLastUpdateTime(): String {
        return localStorage.getFormattedLastUpdateTime()
    }
    
    /**
     * Get download progress
     */
    fun getDownloadProgress(): Pair<Int, Int> {
        return localStorage.getDownloadProgress()
    }
}
