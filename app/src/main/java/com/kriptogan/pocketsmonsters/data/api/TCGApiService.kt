package com.kriptogan.pocketsmonsters.data.api

import com.kriptogan.pocketsmonsters.data.tcg.TCGCard
import com.kriptogan.pocketsmonsters.data.tcg.TCGSet
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Pokémon TCG API Service
 * Official API: https://docs.pokemontcg.io/
 * Base URL: https://api.pokemontcg.io/v2/
 */
interface TCGApiService {
    
    /**
     * Get all TCG sets
     * @param page Page number (default: 1)
     * @param pageSize Number of results per page (default: 250)
     * @return Response containing list of TCG sets
     */
    @GET("sets")
    suspend fun getSets(
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 250
    ): Response<TCGApiResponse<TCGSet>>
    
    /**
     * Get a specific TCG set by ID
     * @param setId The ID of the set
     * @return Response containing the TCG set
     */
    @GET("sets/{id}")
    suspend fun getSet(
        @Path("id") setId: String
    ): Response<TCGApiResponseSingle<TCGSet>>
    
    /**
     * Search for cards
     * @param q Query string (e.g., "name:pikachu", "nationalPokedexNumbers:25")
     * @param page Page number (default: 1)
     * @param pageSize Number of results per page (default: 250)
     * @return Response containing list of TCG cards
     */
    @GET("cards")
    suspend fun searchCards(
        @Query("q") q: String,
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 250
    ): Response<TCGApiResponse<TCGCard>>
    
    /**
     * Get cards by Pokémon national Pokédex number
     * @param pokedexNumber The national Pokédex number
     * @param page Page number (default: 1)
     * @param pageSize Number of results per page (default: 250)
     * @return Response containing list of TCG cards
     */
    @GET("cards")
    suspend fun getCardsByPokedexNumber(
        @Query("q") pokedexNumber: Int,
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 250
    ): Response<TCGApiResponse<TCGCard>>
}

/**
 * Generic API response wrapper for lists
 */
data class TCGApiResponse<T>(
    val data: List<T>,
    val page: Int? = null,
    val pageSize: Int? = null,
    val count: Int? = null,
    val totalCount: Int? = null
)

/**
 * Generic API response wrapper for single items
 */
data class TCGApiResponseSingle<T>(
    val data: T
)

