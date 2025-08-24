package com.kriptogan.pocketsmonsters.data.repository

import com.kriptogan.pocketsmonsters.data.api.PokeApiService
import com.kriptogan.pocketsmonsters.data.local.LocalStorage
import com.kriptogan.pocketsmonsters.data.models.Pokemon
import com.kriptogan.pocketsmonsters.data.models.PokemonListResponse
import com.kriptogan.pocketsmonsters.data.network.NetworkModule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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
     * Get detailed information about a specific Pokémon by name
     * @param name The name of the Pokémon
     * @return Pokemon or null if error occurs
     */
    suspend fun getPokemon(name: String): Result<Pokemon> = withContext(Dispatchers.IO) {
        try {
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
                val pokemon = apiService.getPokemon(name.lowercase())
                pokemonList.add(pokemon)
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
     * Get formatted last update time
     */
    fun getLastUpdateTime(): String {
        return localStorage.getFormattedLastUpdateTime()
    }
}
