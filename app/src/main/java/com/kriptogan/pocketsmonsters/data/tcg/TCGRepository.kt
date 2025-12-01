package com.kriptogan.pocketsmonsters.data.tcg

import android.content.Context
import android.util.Log
import com.kriptogan.pocketsmonsters.data.api.TCGApiService
import com.kriptogan.pocketsmonsters.data.local.LocalStorage
import com.kriptogan.pocketsmonsters.data.offline.OfflineDataLoader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TCGRepository(
    private val context: Context,
    private val apiService: TCGApiService? = null,
    private val offlineDataLoader: OfflineDataLoader? = null
) {
    
    companion object {
        private const val TAG = "TCGRepository"
    }
    
    /**
     * Get TCG data for a specific Pokémon by its national Pokédex number
     * @param pokemonId The national Pokédex number
     * @param pokemonName The name of the Pokémon
     * @return PokemonTCGData containing sets and cards, or null if not found
     */
    suspend fun getTCGDataForPokemon(pokemonId: Int, pokemonName: String): PokemonTCGData? = withContext(Dispatchers.IO) {
        try {
            // First try offline data
            val offlineData = loadTCGDataFromAssets(pokemonId, pokemonName)
            if (offlineData != null && offlineData.cards.isNotEmpty()) {
                Log.d(TAG, "Loaded TCG data from offline assets for $pokemonName")
                return@withContext offlineData
            }
            
            // Fallback to API if available
            if (apiService != null) {
                try {
                    // Query format: "nationalPokedexNumbers:25" for Pikachu
                    val query = "nationalPokedexNumbers:$pokemonId"
                    val response = apiService.searchCards(q = query, pageSize = 250)
                    
                    if (response.isSuccessful && response.body() != null) {
                        val cards = response.body()!!.data
                        val sets = extractUniqueSets(cards)
                        
                        Log.d(TAG, "Loaded ${cards.size} cards from API for $pokemonName")
                        return@withContext PokemonTCGData(
                            pokemonId = pokemonId,
                            pokemonName = pokemonName,
                            sets = sets,
                            cards = cards
                        )
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error fetching TCG data from API: ${e.message}")
                }
            }
            
            // Return empty data if nothing found
            Log.d(TAG, "No TCG data found for $pokemonName")
            PokemonTCGData(
                pokemonId = pokemonId,
                pokemonName = pokemonName,
                sets = emptyList(),
                cards = emptyList()
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error loading TCG data: ${e.message}")
            e.printStackTrace()
            null
        }
    }
    
    /**
     * Get TCG sets for a specific Pokémon
     * @param pokemonId The national Pokédex number
     * @param pokemonName The name of the Pokémon
     * @return List of TCG sets containing this Pokémon
     */
    suspend fun getTCGSetsForPokemon(pokemonId: Int, pokemonName: String): List<TCGSet> = withContext(Dispatchers.IO) {
        val tcgData = getTCGDataForPokemon(pokemonId, pokemonName)
        tcgData?.sets ?: emptyList()
    }
    
    /**
     * Get TCG cards for a specific Pokémon
     * @param pokemonId The national Pokédex number
     * @param pokemonName The name of the Pokémon
     * @return List of TCG cards for this Pokémon
     */
    suspend fun getCardsForPokemon(pokemonId: Int, pokemonName: String): List<TCGCard> = withContext(Dispatchers.IO) {
        val tcgData = getTCGDataForPokemon(pokemonId, pokemonName)
        tcgData?.cards ?: emptyList()
    }
    
    /**
     * Load TCG data from offline assets
     * @param pokemonId The national Pokédex number
     * @param pokemonName The name of the Pokémon
     * @return PokemonTCGData if found in assets, null otherwise
     */
    private suspend fun loadTCGDataFromAssets(pokemonId: Int, pokemonName: String): PokemonTCGData? = withContext(Dispatchers.IO) {
        return@withContext try {
            // Check if TCG data file exists in assets
            // For now, return null - we'll add offline TCG data in a future update
            // This allows the API to be used as fallback
            null
        } catch (e: Exception) {
            Log.e(TAG, "Error loading TCG data from assets: ${e.message}")
            null
        }
    }
    
    /**
     * Extract unique sets from a list of cards
     * @param cards List of TCG cards
     * @return List of unique TCG sets
     */
    private fun extractUniqueSets(cards: List<TCGCard>): List<TCGSet> {
        val setMap = mutableMapOf<String, TCGSet>()
        
        cards.forEach { card ->
            card.set?.let { cardSet ->
                if (cardSet.id != null && !setMap.containsKey(cardSet.id)) {
                    // Convert TCGCardSet to TCGSet
                    setMap[cardSet.id] = TCGSet(
                        id = cardSet.id,
                        name = cardSet.name ?: "",
                        series = cardSet.series,
                        printedTotal = cardSet.printedTotal,
                        total = cardSet.total,
                        legalities = cardSet.legalities,
                        ptcgoCode = cardSet.ptcgoCode,
                        releaseDate = cardSet.releaseDate,
                        updatedAt = cardSet.updatedAt,
                        images = cardSet.images
                    )
                }
            }
        }
        
        return setMap.values.toList().sortedBy { it.releaseDate ?: "" }
    }
}

