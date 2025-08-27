package com.kriptogan.pocketsmonsters.data.models

/**
 * Represents a Pokemon nature that affects stats and personality
 */
data class Nature(
    val name: String,
    val increasedStat: String?,
    val decreasedStat: String?,
    val description: String
)
