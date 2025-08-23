package com.kriptogan.pocketsmonsters.data.api

import com.kriptogan.pocketsmonsters.data.models.Pokemon
import com.kriptogan.pocketsmonsters.data.models.PokemonListResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface PokeApiService {
    
    /**
     * Get a list of Pokémon with pagination support
     * @param limit Maximum number of Pokémon to return (default 151 for first generation)
     * @param offset Number of Pokémon to skip for pagination
     */
    @GET("pokemon")
    suspend fun getPokemonList(
        @Query("limit") limit: Int = 151,
        @Query("offset") offset: Int = 0
    ): PokemonListResponse
    
    /**
     * Get detailed information about a specific Pokémon by name
     * @param name The name of the Pokémon (e.g., "pikachu", "charizard")
     */
    @GET("pokemon/{name}")
    suspend fun getPokemon(@Path("name") name: String): Pokemon
    
    /**
     * Get detailed information about a specific Pokémon by ID
     * @param id The ID of the Pokémon (e.g., 25 for Pikachu)
     */
    @GET("pokemon/{id}")
    suspend fun getPokemonById(@Path("id") id: Int): Pokemon
}
