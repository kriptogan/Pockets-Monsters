package com.kriptogan.pocketsmonsters.data.collection

/**
 * Represents the collection status of a Pokémon card
 */
data class CollectionStatus(
    val pokemonId: Int,
    val pokemonName: String,
    val isOwned: Boolean,
    val ownedSince: Long? = null, // Timestamp when marked as owned
    val notes: String? = null // Optional user notes
)

