package com.kriptogan.pocketsmonsters.data.converter

import com.kriptogan.pocketsmonsters.data.models.Pokemon
import com.kriptogan.pocketsmonsters.data.models.Stat
import kotlin.math.*

class DnDConverter {
    
    /**
     * Convert a Pokémon to D&D view with converted stats
     */
    fun convertPokemonToDnD(pokemon: Pokemon): DnDView {
        val convertedStats = convertStats(pokemon.stats)
        val modifiers = calculateModifiers(convertedStats)
        val hitDice = calculateHitDice(pokemon.stats.find { it.stat.name == "hp" }?.baseStat ?: 0)
        val movement = calculateMovement(pokemon.stats, pokemon.weight)
        val speedModifier = modifiers["Speed"] ?: 0
        val ac = 10 + min(speedModifier, 5) // Capped at +5 as per rules
        val initiative = speedModifier
        
        return DnDView(
            pokemon = pokemon,
            convertedStats = convertedStats,
            modifiers = modifiers,
            hitDice = hitDice,
            movement = movement,
            ac = ac,
            initiative = initiative
        )
    }
    
    /**
     * Convert Pokémon stats using formula: (Base Stat ÷ 10) + 5, rounded down
     */
    private fun convertStats(stats: List<Stat>): Map<String, Int> {
        return stats.associate { stat ->
            val statName = when(stat.stat.name) {
                "attack" -> "Attack"
                "defense" -> "Defense"
                "special-attack" -> "Sp.Atk"
                "special-defense" -> "Sp.Def"
                "speed" -> "Speed"
                "hp" -> "HP"
                else -> stat.stat.name.replaceFirstChar { it.uppercase() }
            }
            val convertedValue = floor((stat.baseStat / 10.0) + 5).toInt()
            statName to convertedValue
        }
    }
    
    /**
     * Calculate D&D modifiers: floor((Stat - 10) ÷ 2)
     */
    private fun calculateModifiers(convertedStats: Map<String, Int>): Map<String, Int> {
        return convertedStats.mapValues { (_, value) ->
            floor((value - 10) / 2.0).toInt()
        }
    }
    
    /**
     * Calculate Hit Dice based on HP ranges from rules
     */
    private fun calculateHitDice(hp: Int): String {
        return when {
            hp <= 50 -> "d6"
            hp <= 100 -> "d8"
            hp <= 150 -> "d10"
            else -> "d12"
        }
    }
    
    /**
     * Calculate movement using the complex weight-based formula from rules
     */
    private fun calculateMovement(stats: List<Stat>, weight: Int): Int {
        val speedStat = stats.find { it.stat.name == "speed" }?.baseStat ?: 0
        
        // Step 1: Base Score
        val baseScore = floor(speedStat / 10.0 + 5).toInt()
        
        // Step 2: Weight Modifier
        val weightModifier = when {
            weight < 10 -> 1
            weight < 50 -> 0
            weight < 150 -> -1
            weight < 300 -> -2
            else -> -3
        }
        
        // Step 3: Adjusted Movement Score
        val adjustedScore = maxOf(1, baseScore + weightModifier)
        
        // Step 4: Convert to Feet
        val movementInFeet = roundToNearest5(adjustedScore * 2.5)
        
        return movementInFeet.toInt()
    }
    
    /**
     * Round to nearest 5 as specified in the rules
     */
    private fun roundToNearest5(value: Double): Double {
        return round(value / 5.0) * 5.0
    }
}

/**
 * Data class for D&D view information
 */
data class DnDView(
    val pokemon: Pokemon,
    val convertedStats: Map<String, Int>,  // "Attack" -> 14
    val modifiers: Map<String, Int>,      // "Attack" -> +2
    val hitDice: String,                  // "d8"
    val movement: Int,                    // 25 feet
    val ac: Int,                          // 13
    val initiative: Int                   // +3
)
