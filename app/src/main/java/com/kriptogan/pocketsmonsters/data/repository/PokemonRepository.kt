package com.kriptogan.pocketsmonsters.data.repository

import com.kriptogan.pocketsmonsters.data.api.PokeApiService
import com.kriptogan.pocketsmonsters.data.models.Pokemon
import com.kriptogan.pocketsmonsters.data.models.PokemonListResponse
import com.kriptogan.pocketsmonsters.data.network.NetworkModule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PokemonRepository {
    
    private val apiService: PokeApiService = NetworkModule.createPokeApiService()
    
    /**
     * Get a list of Pokémon with pagination support
     * @param limit Maximum number of Pokémon to return
     * @param offset Number of Pokémon to skip for pagination
     * @return PokemonListResponse or null if error occurs
     */
    suspend fun getPokemonList(
        limit: Int = 151,
        offset: Int = 0
    ): Result<PokemonListResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getPokemonList(limit, offset)
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
}
