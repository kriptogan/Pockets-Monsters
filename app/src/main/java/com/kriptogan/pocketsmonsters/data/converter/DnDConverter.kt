package com.kriptogan.pocketsmonsters.data.converter

import android.util.Log
import com.kriptogan.pocketsmonsters.data.models.Pokemon
import com.kriptogan.pocketsmonsters.data.models.Stat
import com.kriptogan.pocketsmonsters.data.models.LevelUpMove
import kotlin.math.*

class DnDConverter {
    
    companion object {
        private const val TAG = "DnDConverter"
    }
    
    /**
     * Convert a Pokémon to D&D view with converted stats
     */
    fun convertPokemonToDnD(pokemon: Pokemon): DnDView {
        Log.d(TAG, "=== Starting conversion for ${pokemon.name} ===")
        Log.d(TAG, "Pokemon ID: ${pokemon.id}")
        Log.d(TAG, "Pokemon Weight: ${pokemon.weight} (raw value)")
        
        val convertedStats = convertStats(pokemon.stats)
        val modifiers = calculateModifiers(convertedStats)
        val hitDice = calculateHitDice(pokemon.stats.find { it.stat.name == "hp" }?.baseStat ?: 0)
        val movement = calculateMovement(pokemon.stats, pokemon.weight)
        val speedModifier = modifiers["Speed"] ?: 0
        val ac = 10 + min(speedModifier, 5) // Capped at +5 as per rules
        val initiative = speedModifier
        
        // Group moves by D&D level
        val movesByDnDLevel = groupMovesByDnDLevel(pokemon.levelUpMoves)
        
        Log.d(TAG, "=== Final Results for ${pokemon.name} ===")
        Log.d(TAG, "Movement: $movement feet")
        Log.d(TAG, "AC: $ac")
        Log.d(TAG, "Initiative: $initiative")
        Log.d(TAG, "Hit Dice: $hitDice")
        Log.d(TAG, "Converted Stats: $convertedStats")
        Log.d(TAG, "Modifiers: $modifiers")
        Log.d(TAG, "Moves by D&D Level: $movesByDnDLevel")
        
        return DnDView(
            pokemon = pokemon,
            convertedStats = convertedStats,
            modifiers = modifiers,
            hitDice = hitDice,
            movement = movement,
            ac = ac,
            initiative = initiative,
            movesByDnDLevel = movesByDnDLevel
        )
    }
    
    /**
     * Convert Pokémon stats using formula: (Base Stat ÷ 10) + 5, rounded down
     */
    private fun convertStats(stats: List<Stat>): Map<String, Int> {
        Log.d(TAG, "=== Converting Stats ===")
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
            Log.d(TAG, "${stat.stat.name}: ${stat.baseStat} → $convertedValue (formula: floor((${stat.baseStat} ÷ 10) + 5) = floor(${stat.baseStat / 10.0 + 5}) = $convertedValue)")
            statName to convertedValue
        }
    }
    
    /**
     * Calculate D&D modifiers: floor((Stat - 10) ÷ 2)
     */
    private fun calculateModifiers(convertedStats: Map<String, Int>): Map<String, Int> {
        Log.d(TAG, "=== Calculating Modifiers ===")
        return convertedStats.mapValues { (statName, value) ->
            val modifier = floor((value - 10) / 2.0).toInt()
            Log.d(TAG, "$statName: $value → $modifier (formula: floor(($value - 10) ÷ 2) = floor(${(value - 10) / 2.0}) = $modifier)")
            modifier
        }
    }
    
    /**
     * Calculate Hit Dice based on HP ranges from rules
     */
    private fun calculateHitDice(hp: Int): String {
        val hitDice = when {
            hp <= 50 -> "d6"
            hp <= 100 -> "d8"
            hp <= 150 -> "d10"
            else -> "d12"
        }
        Log.d(TAG, "=== Hit Dice Calculation ===")
        Log.d(TAG, "HP: $hp → Hit Dice: $hitDice")
        return hitDice
    }
    
    /**
     * Calculate movement using the complex weight-based formula from rules
     */
    private fun calculateMovement(stats: List<Stat>, weight: Int): Int {
        Log.d(TAG, "=== Movement Calculation ===")
        Log.d(TAG, "Input - Weight: $weight (API value), Stats: ${stats.map { "${it.stat.name}: ${it.baseStat}" }}")
        
        // Convert weight from API value to kilograms (division by 10)
        // PokéAPI provides weight in a format where division by 10 gives us kg
        val weightInKg = weight / 10
        Log.d(TAG, "Weight Conversion: $weight ÷ 10 = $weightInKg kg")
        
        val speedStat = stats.find { it.stat.name == "speed" }?.baseStat ?: 0
        Log.d(TAG, "Speed Stat Found: $speedStat")
        
        // Step 1: Base Score
        val baseScore = floor(speedStat / 10.0 + 5).toInt()
        Log.d(TAG, "Step 1 - Base Score: floor($speedStat ÷ 10 + 5) = floor(${speedStat / 10.0 + 5}) = $baseScore")
        
        // Step 2: Weight Modifier (using correct ranges)
        val weightModifier = when {
            weightInKg < 10 -> 1       // 0-9.9 kg: +1
            weightInKg < 50 -> 0       // 10-49.9 kg: +0
            weightInKg < 150 -> -1     // 50-149.9 kg: -1
            weightInKg < 300 -> -2     // 150-299.9 kg: -2
            else -> -3                 // ≥ 300 kg: -3
        }
        Log.d(TAG, "Step 2 - Weight Modifier: Weight $weightInKg kg falls in range → Modifier: $weightModifier")
        
        // Step 3: Adjusted Movement Score
        val adjustedScore = maxOf(1, baseScore + weightModifier)
        Log.d(TAG, "Step 3 - Adjusted Score: $baseScore + $weightModifier = ${baseScore + weightModifier} → maxOf(1, ${baseScore + weightModifier}) = $adjustedScore")
        
        // Step 4: Convert to Feet
        val rawMovementInFeet = adjustedScore * 2.5
        Log.d(TAG, "Step 4 - Raw Movement: $adjustedScore × 2.5 = $rawMovementInFeet feet")
        
        // Step 5: Round to Nearest 5
        val movementInFeet = roundToNearest5(rawMovementInFeet)
        Log.d(TAG, "Step 5 - Rounded Movement: roundToNearest5($rawMovementInFeet) = $movementInFeet feet")
        
        val finalResult = movementInFeet.toInt()
        Log.d(TAG, "Final Movement Result: $finalResult feet")
        
        return finalResult
    }
    
    /**
     * Round to nearest 5 as specified in the rules
     */
    private fun roundToNearest5(value: Double): Double {
        Log.d(TAG, "=== Rounding to Nearest 5 ===")
        Log.d(TAG, "Input value: $value")
        
        val dividedBy5 = value / 5.0
        Log.d(TAG, "Divided by 5: $value ÷ 5 = $dividedBy5")
        
        val rounded = round(dividedBy5)
        Log.d(TAG, "Rounded: round($dividedBy5) = $rounded")
        
        val result = rounded * 5.0
        Log.d(TAG, "Final rounded value: $result")
        return result
    }
    
    /**
     * Group moves by D&D level using formula: ceil(Pokemon Level ÷ 5)
     */
    private fun groupMovesByDnDLevel(levelUpMoves: List<LevelUpMove>): Map<Int, List<String>> {
        Log.d(TAG, "=== Grouping Moves by D&D Level ===")
        
        val groupedMoves = levelUpMoves.groupBy { move ->
            val dndLevel = ceil(move.levelLearnedAt / 5.0).toInt()
            Log.d(TAG, "Move: ${move.name}, Pokemon Level: ${move.levelLearnedAt} → D&D Level: ceil(${move.levelLearnedAt} ÷ 5) = ceil(${move.levelLearnedAt / 5.0}) = $dndLevel")
            dndLevel
        }.mapValues { (dndLevel, moves) ->
            val moveNames = moves.map { it.name }
            Log.d(TAG, "D&D Level $dndLevel: ${moveNames.joinToString(", ")}")
            moveNames
        }
        
        Log.d(TAG, "Final grouped moves: $groupedMoves")
        return groupedMoves
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
    val initiative: Int,                  // +3
    val movesByDnDLevel: Map<Int, List<String>>  // D&D Level -> List of move names
)
