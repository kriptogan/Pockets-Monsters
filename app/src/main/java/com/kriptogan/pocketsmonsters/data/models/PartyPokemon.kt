package com.kriptogan.pocketsmonsters.data.models

import android.util.Log
import com.google.gson.annotations.SerializedName
import kotlin.math.floor
import kotlin.math.round
import kotlin.math.min

/**
 * Represents a Pokemon in the user's party with current stats and status
 */
data class PartyPokemon(
    val id: Int,
    val name: String,
    val basePokemon: Pokemon, // Reference to base Pokemon data
    
    // Current status
    val level: Int = 1,
    val currentHP: Int,
    val maxHP: Int,
    
    // Physical characteristics (base +-5 variation)
    val actualSize: Int,
    val actualWeight: Int,
    
    // Move management
    val availableMoves: List<LevelUpMove>, // Based on current level
    val currentMoveSet: List<String> = emptyList(), // Selected 4 moves
    
    // D&D stats
    val convertedDnDStats: Map<String, Int>, // Base converted stats
    val currentDnDStats: Map<String, Int>, // Current stats (can be modified)
    
    // Type information
    val weaknesses: List<String>,
    val resistances: List<String>,
    
    // Status conditions
    val conditions: List<Condition> = emptyList(),
    
    // Nature (randomly assigned when added to party)
    val nature: Nature = Nature("Hardy", null, null, "Neutral nature"),
    
    // Experience and Leveling
    val currentExp: Int = 0,
    val expToLevelUp: Int = 300,
    
    // Proficiency bonus (follows D&D rules: +2 at levels 1-4, +3 at 5-8, +4 at 9-12, +5 at 13-16, +6 at 17-20)
    val proficiency: Int = 2,
    
    // D&D derived stats (calculated from currentDnDStats and actualWeight)
    val movementSpeed: Int = 30,
    
    // Metadata
    val addedToPartyAt: Long = System.currentTimeMillis(),
    
    // Evolution data
    val evolution: EvolutionDetails? = null
) {
    
    companion object {
        // D&D Experience Table for levels 1-20
        private val EXP_TABLE = mapOf(
            1 to 0,
            2 to 300,
            3 to 900,
            4 to 2700,
            5 to 6500,
            6 to 14000,
            7 to 23000,
            8 to 34000,
            9 to 48000,
            10 to 64000,
            11 to 85000,
            12 to 100000,
            13 to 120000,
            14 to 140000,
            15 to 165000,
            16 to 195000,
            17 to 225000,
            18 to 265000,
            19 to 305000,
            20 to 355000
        )
        
        /**
         * Calculate proficiency bonus for a given level according to D&D rules
         * @param level Pokemon level
         * @return Proficiency bonus value
         */
        fun calculateProficiencyBonus(level: Int): Int {
            return when {
                level <= 4 -> 2   // Levels 1-4: +2
                level <= 8 -> 3   // Levels 5-8: +3
                level <= 12 -> 4  // Levels 9-12: +4
                level <= 16 -> 5  // Levels 13-16: +5
                level <= 20 -> 6  // Levels 17-20: +6
                else -> 6         // Cap at +6 for levels above 20
            }
        }
        
                /**
         * Search for evolution data in pokemons.json file
         * @param context Android context to access assets
         * @param pokemonId The ID of the Pokemon to search for
         * @return EvolutionDetails if found, null otherwise
         */
        fun findEvolutionData(context: android.content.Context, pokemonId: Int): EvolutionDetails? {
            return try {
                val inputStream = context.assets.open("pokemons.json")
                val jsonString = inputStream.bufferedReader().use { it.readText() }
                val jsonArray = org.json.JSONArray(jsonString)

                for (i in 0 until jsonArray.length()) {
                    val pokemon = jsonArray.getJSONObject(i)
                    if (pokemon.getInt("id") == pokemonId) {
                        if (pokemon.has("evolution")) {
                            val evolution = pokemon.getJSONObject("evolution")
                            return EvolutionDetails(
                                level = evolution.getInt("level"),
                                evolutionId = evolution.getInt("evolutionId")
                            )
                        }
                        break
                    }
                }
                null
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }
    
    /**
     * Gain experience and check for level change
     * @param expAmount Amount of experience to gain (can be negative for level down)
     * @return Pair of (message, updated Pokemon instance)
     */
    fun gainExp(expAmount: Int): Pair<String, PartyPokemon> {
        Log.d("lvlup process", "=== gainExp called ===")
        Log.d("lvlup process", "Current level: $level, current exp: $currentExp")
        Log.d("lvlup process", "Exp amount to add: $expAmount")
        
        val newCurrentExp = currentExp + expAmount
        val newLevel = calculateLevelFromExp(newCurrentExp)
        
        Log.d("lvlup process", "New exp: $newCurrentExp, new level: $newLevel")
        
        return when {
            newLevel > level -> {
                // Level up occurred
                Log.d("lvlup process", "Level up detected! Calling levelUp function")
                val (message, updatedPokemon) = levelUp(newLevel, newCurrentExp)
                Log.d("lvlup process", "Level up result - message: $message, new level: ${updatedPokemon.level}, availableMoves: ${updatedPokemon.availableMoves.size}")
                message to updatedPokemon
            }
            newLevel < level -> {
                // Level down occurred
                Log.d("lvlup process", "Level down detected! Calling levelDown function")
                val updatedPokemon = levelDown(newLevel, newCurrentExp)
                Log.d("lvlup process", "Level down result - new level: ${updatedPokemon.level}, availableMoves: ${updatedPokemon.availableMoves.size}")
                "Level Down!" to updatedPokemon
            }
            else -> {
                // No level change, just update experience
                Log.d("lvlup process", "No level change, just updating experience")
                val updatedPokemon = this.copy(currentExp = newCurrentExp)
                "Experience updated" to updatedPokemon
            }
        }
    }
    
    /**
     * Get the total experience required for a specific level
     */
    fun getExpRequiredForLevel(targetLevel: Int): Int? {
        return EXP_TABLE[targetLevel]
    }
    
    /**
     * Get the current proficiency bonus for this Pokemon's level
     */
    fun getCurrentProficiencyBonus(): Int {
        return calculateProficiencyBonus(level)
    }
    
    /**
     * Check which stat the Pokemon is proficient with (gets +proficiency bonus)
     * @return The stat name that gets the proficiency bonus, or null if none
     */
    fun getProficientStat(): String? {
        return nature.increasedStat
    }
    
    /**
     * Check which stat the Pokemon is deficient in (gets -2 penalty)
     * @return The stat name that gets the -2 penalty, or null if none
     */
    fun getDeficientStat(): String? {
        return nature.decreasedStat
    }
    
    /**
     * Check if a specific stat is proficient (gets +proficiency bonus)
     * @param statName The stat to check (e.g., "Attack", "Speed", "Defense")
     * @return true if the stat is proficient, false otherwise
     */
    fun isStatProficient(statName: String): Boolean {
        return nature.increasedStat == statName
    }
    
    /**
     * Check if a specific stat is deficient (gets -2 penalty)
     * @param statName The stat to check (e.g., "Attack", "Speed", "Defense")
     * @return true if the stat is deficient, false otherwise
     */
    fun isStatDeficient(statName: String): Boolean {
        return nature.decreasedStat == statName
    }
    
    /**
     * Get the proficiency bonus for a specific stat
     * @param statName The stat to check
     * @return The proficiency bonus (+proficiency if proficient, -2 if deficient, 0 if neutral)
     */
    fun getStatProficiencyBonus(statName: String): Int {
        return when {
            isStatProficient(statName) -> getCurrentProficiencyBonus()
            isStatDeficient(statName) -> -2
            else -> 0
        }
    }
    
    /**
     * Calculate current armor class based on currentDnDStats and proficiency/deficiency
     * Formula: 10 + speedModifier + proficiencyBonus where:
     * - speedModifier = floor((speed - 10) / 2)
     * - proficiencyBonus = +proficiency if Speed is proficient, -2 if Speed is deficient, 0 if neutral
     */
    fun calculateCurrentArmorClass(): Int {
        // Log current DnD stats for debugging
        logCurrentDnDStats()
        
        val speedStat = currentDnDStats["Speed"] ?: 10
        val speedModifier = floor((speedStat - 10) / 2.0).toInt()
        
        // Check if Speed stat is proficient or deficient
        val proficiencyBonus = getStatProficiencyBonus("Speed")
        
        val ac = 10 + speedModifier + proficiencyBonus
        
        Log.d("PartyPokemon", "AC calculation for $name: speedStat=$speedStat, speedModifier=$speedModifier, proficiencyBonus=$proficiencyBonus, AC=$ac")
        
        return ac
    }
    
    /**
     * Log current DnD stats for debugging evolution
     */
    fun logCurrentDnDStats() {
        Log.d("PartyPokemon", "Current DnD stats for $name: $currentDnDStats")
        Log.d("PartyPokemon", "Stats breakdown for $name:")
        currentDnDStats.forEach { (statName, value) ->
            val modifier = floor((value - 10) / 2.0).toInt()
            val modifierText = if (modifier >= 0) "+$modifier" else "$modifier"
            Log.d("PartyPokemon", "  $statName: $value (modifier: $modifierText)")
        }
    }
    
    /**
     * Calculate current initiative based on currentDnDStats and proficiency/deficiency
     * Formula: speedModifier + proficiencyBonus where:
     * - speedModifier = floor((speed - 10) / 2)
     * - proficiencyBonus = +proficiency if Speed is proficient, -2 if Speed is deficient, 0 if neutral
     */
    fun calculateCurrentInitiative(): Int {
        // Log current DnD stats for debugging
        logCurrentDnDStats()
        
        val speedStat = currentDnDStats["Speed"] ?: 10
        val speedModifier = floor((speedStat - 10) / 2.0).toInt()
        
        // Check if Speed stat is proficient or deficient
        val proficiencyBonus = getStatProficiencyBonus("Speed")
        
        val initiative = speedModifier + proficiencyBonus
        
        Log.d("PartyPokemon", "Initiative calculation for $name: speedStat=$speedStat, speedModifier=$speedModifier, proficiencyBonus=$proficiencyBonus, Initiative=$initiative")
        
        return initiative
    }
    
    /**
     * Calculate current movement speed based on currentDnDStats and actualWeight
     * Uses the same complex formula from DnDConverter
     */
    fun calculateCurrentMovementSpeed(): Int {
        val speedStat = currentDnDStats["Speed"] ?: 10
        
        // Step 1: Base Score
        val baseScore = speedStat
        
        // Step 2: Weight Modifier (convert actualWeight to kg)
        val weightInKg = actualWeight / 10
        
        val weightModifier = when {
            weightInKg < 10 -> 1       // 0-9.9 kg: +1
            weightInKg < 50 -> 0       // 10-49.9 kg: +0
            weightInKg < 150 -> -1     // 50-149.9 kg: -1
            weightInKg < 300 -> -2     // 150-299.9 kg: -2
            else -> -3                 // ≥ 300 kg: -3
        }
        
        // Step 3: Adjusted Score (using kotlin.math.max instead of maxOf)
        val adjustedScore = kotlin.math.max(1, baseScore + weightModifier)
        
        // Step 4: Convert to Feet
        val rawMovementInFeet = adjustedScore * 2.5
        
        // Step 5: Round to Nearest 5
        val movementInFeet = roundToNearest5(rawMovementInFeet)
        
        val finalMovement = movementInFeet.toInt()
        
        return finalMovement
    }
    
    /**
     * Round to nearest 5 as specified in the D&D rules
     */
    private fun roundToNearest5(value: Double): Double {
        val dividedBy5 = value / 5.0
        val rounded = round(dividedBy5)
        return rounded * 5.0
    }
    
    /**
     * Check if the Pokemon can level up
     */
    fun canLevelUp(): Boolean = level < 20 && currentExp >= expToLevelUp
    
    /**
     * Calculate the level based on total experience
     * @param exp Total experience points
     * @return The level corresponding to the given experience
     */
    private fun calculateLevelFromExp(exp: Int): Int {
        for (level in 20 downTo 1) {
            val requiredExp = EXP_TABLE[level] ?: 0
            if (exp >= requiredExp) {
                return level
            }
        }
        return 1
    }
    
    /**
     * Recalculate available moves based on current level and base Pokemon's levelUpMoves
     * Also cleans up currentMoveSet to remove moves that are no longer available
     * Uses D&D level conversion: ceil(Pokemon Level ÷ 5) for proper move filtering
     * @return Updated PartyPokemon with recalculated moves
     */
    fun recalculateAvailableMoves(): PartyPokemon {
        Log.d("lvlup process", "=== recalculateAvailableMoves called ===")
        Log.d("lvlup process", "Current level: $level")
        Log.d("lvlup process", "Base Pokemon levelUpMoves count: ${basePokemon.levelUpMoves.size}")
        Log.d("lvlup process", "Base Pokemon levelUpMoves: ${basePokemon.levelUpMoves.map { "${it.name} (Lv${it.levelLearnedAt})" }}")
        
        // Convert current level to D&D level for proper comparison
        val currentDnDLevel = level
        Log.d("lvlup process", "Current D&D level: $currentDnDLevel (from Pokemon level $level)")
        Log.d("lvlup process", "D&D level calculation: ceil($level / 5.0) = ceil(${level / 5.0}) = $currentDnDLevel")
        
        // Filter moves based on D&D level conversion
        val newAvailableMoves = basePokemon.levelUpMoves.filter { move ->
            val moveDnDLevel = kotlin.math.ceil(move.levelLearnedAt / 5.0).toInt()
            val isAvailable = moveDnDLevel <= currentDnDLevel
            Log.d("lvlup process", "Move ${move.name}: Pokemon Lv${move.levelLearnedAt} → D&D Lv$moveDnDLevel, available=$isAvailable")
            Log.d("lvlup process", "  Move D&D calculation: ceil(${move.levelLearnedAt} / 5.0) = ceil(${move.levelLearnedAt / 5.0}) = $moveDnDLevel")
            Log.d("lvlup process", "  Comparison: $moveDnDLevel <= $currentDnDLevel = $isAvailable")
            isAvailable
        }
        
        Log.d("lvlup process", "Filtered available moves for D&D level $currentDnDLevel (Pokemon level $level): ${newAvailableMoves.map { "${it.name} (Lv${it.levelLearnedAt})" }}")
        Log.d("lvlup process", "Total moves available: ${newAvailableMoves.size}")
        
        // Remove moves from currentMoveSet that are no longer available
        val cleanedCurrentMoveSet = currentMoveSet.filter { moveName ->
            newAvailableMoves.any { it.name == moveName }
        }
        
        Log.d("lvlup process", "Current move set before cleanup: $currentMoveSet")
        Log.d("lvlup process", "Current move set after cleanup: $cleanedCurrentMoveSet")
        
        Log.d("lvlup process", "Recalculated moves for $name (Pokemon Level $level, D&D Level $currentDnDLevel): available=${newAvailableMoves.size}, currentSet=${cleanedCurrentMoveSet.size}")
        
        val result = this.copy(
            availableMoves = newAvailableMoves,
            currentMoveSet = cleanedCurrentMoveSet
        )
        
        Log.d("lvlup process", "Result Pokemon availableMoves count: ${result.availableMoves.size}")
        Log.d("lvlup process", "Result Pokemon availableMoves: ${result.availableMoves.map { it.name }}")
        
        return result
    }

    /**
     * Simple level up - update level, proficiency, and experience
     * @param newLevel The new level
     * @param newExp The new experience total
     * @return Pair of (message, updated Pokemon instance)
     */
    private fun levelUp(newLevel: Int, newExp: Int): Pair<String, PartyPokemon> {
        Log.d("lvlup process", "=== private levelUp called ===")
        Log.d("lvlup process", "Old level: $level, new level: $newLevel")
        
        val newProficiency = calculateProficiencyBonus(newLevel)
        Log.d("lvlup process", "New proficiency: $newProficiency")
        
        // Check if evolution level is reached
        // Convert Pokemon game level to DnD level: ceil(level/5)
        val evolutionMessage = if (evolution != null) {
            val evolutionDnDLevel = kotlin.math.ceil(evolution.level / 5.0).toInt()
            Log.d("lvlup process", "Evolution check for $name: level=$newLevel, evolutionDnDLevel=$evolutionDnDLevel")
            if (newLevel >= evolutionDnDLevel) {
                "Reached evolution!"
            } else {
                "Level Up!"
            }
        } else {
            "Level Up!"
        }
        
        Log.d("lvlup process", "Evolution message: $evolutionMessage")
        
        val updatedPokemon = this.copy(
            level = newLevel,
            currentExp = newExp,
            proficiency = newProficiency
        )
        
        Log.d("lvlup process", "Updated Pokemon before recalculateAvailableMoves - level: ${updatedPokemon.level}, availableMoves: ${updatedPokemon.availableMoves.size}")
        
        // Recalculate available moves and clean up current move set
        val finalPokemon = updatedPokemon.recalculateAvailableMoves()
        
        Log.d("lvlup process", "Final Pokemon after recalculateAvailableMoves - level: ${finalPokemon.level}, availableMoves: ${finalPokemon.availableMoves.size}")
        
        return evolutionMessage to finalPokemon
    }
    
    /**
     * Simple level down - update level, proficiency, and experience
     * @param newLevel The new level
     * @param newExp The new experience total
     * @return Updated Pokemon instance
     */
    private fun levelDown(newLevel: Int, newExp: Int): PartyPokemon {
        Log.d("lvlup process", "=== private levelDown called ===")
        Log.d("lvlup process", "Old level: $level, new level: $newLevel")
        
        val newProficiency = calculateProficiencyBonus(newLevel)
        Log.d("lvlup process", "New proficiency: $newProficiency")
        
        val updatedPokemon = this.copy(
            level = newLevel,
            currentExp = newExp,
            proficiency = newProficiency
        )
        
        Log.d("lvlup process", "Updated Pokemon before recalculateAvailableMoves - level: ${updatedPokemon.level}, availableMoves: ${updatedPokemon.availableMoves.size}")
        
        // Recalculate available moves and clean up current move set
        val finalPokemon = updatedPokemon.recalculateAvailableMoves()
        
        Log.d("lvlup process", "Final Pokemon after recalculateAvailableMoves - level: ${finalPokemon.level}, availableMoves: ${finalPokemon.availableMoves.size}")
        
        return finalPokemon
    }
}



/**
 * Represents status conditions that can affect a Pokemon
 */
enum class Condition(val displayName: String, val description: String) {
    POISONED("Poisoned", "Takes damage over time"),
    PARALYZED("Paralyzed", "May not act, reduced speed"),
    BURNED("Burned", "Takes damage over time, reduced attack"),
    FROZEN("Frozen", "Cannot act until thawed"),
    ASLEEP("Asleep", "Cannot act until awakened"),
    CONFUSED("Confused", "May attack self or miss"),
    BOUND("Bound", "Cannot move, takes damage"),
    BLINDED("Blinded", "Disadvantage on attacks"),
    DEAFENED("Deafened", "Cannot hear, may miss verbal cues"),
    EXHAUSTED("Exhausted", "Disadvantage on ability checks"),
    FRIGHTENED("Frightened", "Disadvantage on attacks and ability checks"),
    INCAPACITATED("Incapacitated", "Cannot take actions or reactions"),
    INVISIBLE("Invisible", "Advantage on attacks, others have disadvantage"),
    PETRIFIED("Petrified", "Turned to stone, cannot act"),
    PRONE("Prone", "Disadvantage on attacks, others have advantage"),
    RESTRAINED("Restrained", "Speed 0, disadvantage on attacks"),
    STUNNED("Stunned", "Cannot act, others have advantage"),
    UNCONSCIOUS("Unconscious", "Cannot act, others have advantage")
}

/**
 * Represents evolution details for a Pokemon
 */
data class EvolutionDetails(
    val level: Int,
    val evolutionId: Int
)
