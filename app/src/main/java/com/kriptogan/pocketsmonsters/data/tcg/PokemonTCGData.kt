package com.kriptogan.pocketsmonsters.data.tcg

/**
 * Represents TCG data for a specific Pokémon
 */
data class PokemonTCGData(
    val pokemonId: Int,
    val pokemonName: String,
    val sets: List<TCGSet> = emptyList(),
    val cards: List<TCGCard> = emptyList()
)

