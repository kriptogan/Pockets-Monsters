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
    val movesData: List<MoveData> = emptyList(), // Full move data from moves_database.json
    
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
    
    // Energy slots (maximum available slots for each tier based on level)
    val energySlots: List<Int> = listOf(-1, -1, -1, -1, -1, -1, -1, -1, -1), // 1st through 9th level slots
    
    // Current energy slots (how many slots are currently available for each tier)
    val currentEnergySlots: List<Int> = listOf(-1, -1, -1, -1, -1, -1, -1, -1, -1), // 1st through 9th level slots
    
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
                            
                                                         // Handle level field - it can be null for special evolutions
                             // Convert Pokemon game level to D&D level using ceil(level/5) formula
                             val level = if (evolution.has("level") && !evolution.isNull("level")) {
                                 val pokemonGameLevel = evolution.getInt("level")
                                 val dndLevel = kotlin.math.ceil(pokemonGameLevel / 5.0).toInt()
                                 dndLevel
                             } else {
                                 null
                             }
                            val evolutionId = evolution.getInt("evolutionId")
                            
                            return EvolutionDetails(
                                level = level,
                                evolutionId = evolutionId
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
        
        /**
         * Search for moves in moves_database.json file
         * @param context Android context to access assets
         * @param moves List of LevelUpMove to search for
         * @return List of MoveData with full move information
         */
        fun findMoves(context: android.content.Context, moves: List<LevelUpMove>): List<MoveData> {
            return try {
                val inputStream = context.assets.open("moves_database.json")
                val jsonString = inputStream.bufferedReader().use { it.readText() }
                val jsonArray = org.json.JSONArray(jsonString)
                
                val foundMoves = mutableListOf<MoveData>()
                
                for (i in 0 until jsonArray.length()) {
                    val moveJson = jsonArray.getJSONObject(i)
                    val moveName = moveJson.getString("name")
                    
                    // Check if this move exists in our moves list
                    if (moves.any { it.name == moveName }) {
                        val moveData = MoveData(
                            name = moveName,
                            tier = moveJson.getInt("tier"),
                            power = if (moveJson.has("power") && !moveJson.isNull("power")) moveJson.getInt("power") else null,
                            accuracy = if (moveJson.has("accuracy") && !moveJson.isNull("accuracy")) moveJson.getInt("accuracy") else null,
                            type = moveJson.getString("type"),
                            damage_class = moveJson.getString("damage_class"),
                            description = moveJson.getString("description"),
                            effect = moveJson.getString("effect"),
                            stat_changes = if (moveJson.has("stat_changes") && !moveJson.isNull("stat_changes")) {
                                val statChangesArray = moveJson.getJSONArray("stat_changes")
                                val statChangesList = mutableListOf<Any>()
                                for (j in 0 until statChangesArray.length()) {
                                    statChangesList.add(statChangesArray.get(j))
                                }
                                statChangesList
                            } else {
                                emptyList()
                            }
                        )
                        foundMoves.add(moveData)
                    }
                }
                
                foundMoves
            } catch (e: Exception) {
                e.printStackTrace()
                emptyList()
            }
        }
        
        /**
         * Calculate energy slots for a given D&D level according to Wizard spell slot progression
         * @param dndLevel The D&D level (1-20)
         * @return List of energy slots (-1 for unavailable tiers, Int for available slots)
         */
        fun calculateEnergySlots(dndLevel: Int): List<Int> {
            return when (dndLevel) {
                1 -> listOf(2, -1, -1, -1, -1, -1, -1, -1, -1)
                2 -> listOf(3, -1, -1, -1, -1, -1, -1, -1, -1)
                3 -> listOf(4, 2, -1, -1, -1, -1, -1, -1, -1)
                4 -> listOf(4, 3, -1, -1, -1, -1, -1, -1, -1)
                5 -> listOf(4, 3, 2, -1, -1, -1, -1, -1, -1)
                6 -> listOf(4, 3, 3, -1, -1, -1, -1, -1, -1)
                7 -> listOf(4, 3, 3, 1, -1, -1, -1, -1, -1)
                8 -> listOf(4, 3, 3, 2, -1, -1, -1, -1, -1)
                9 -> listOf(4, 3, 3, 3, 1, -1, -1, -1, -1)
                10 -> listOf(4, 3, 3, 3, 2, -1, -1, -1, -1)
                11 -> listOf(4, 3, 3, 3, 2, 1, -1, -1, -1)
                12 -> listOf(4, 3, 3, 3, 2, 1, -1, -1, -1)
                13 -> listOf(4, 3, 3, 3, 2, 1, 1, -1, -1)
                14 -> listOf(4, 3, 3, 3, 2, 1, 1, -1, -1)
                15 -> listOf(4, 3, 3, 3, 2, 1, 1, 1, -1)
                16 -> listOf(4, 3, 3, 3, 2, 1, 1, 1, -1)
                17 -> listOf(4, 3, 3, 3, 2, 1, 1, 1, 1)
                18 -> listOf(4, 3, 3, 3, 3, 1, 1, 1, 1)
                19 -> listOf(4, 3, 3, 3, 3, 2, 1, 1, 1)
                20 -> listOf(4, 3, 3, 3, 3, 2, 2, 1, 1)
                else -> if (dndLevel > 20) {
                    listOf(4, 3, 3, 3, 3, 2, 2, 1, 1) // Cap at level 20
                } else {
                    listOf(-1, -1, -1, -1, -1, -1, -1, -1, -1) // Level 0 or below
                }
            }
        }
    }
    
    /**
     * Set energy slots based on current Pokemon level using D&D Wizard spell slot progression
     * @return Updated PartyPokemon with calculated energy slots
     */
    fun setEnergySlots(): PartyPokemon {
        val calculatedEnergySlots = calculateEnergySlots(level)
        val calculatedCurrentEnergySlots = calculatedEnergySlots // Start with same values as max slots
        
        return this.copy(
            energySlots = calculatedEnergySlots,
            currentEnergySlots = calculatedCurrentEnergySlots
        )
    }
    
    /**
     * Reset current energy slots to match the maximum available slots
     * @return Updated PartyPokemon with reset current energy slots
     */
    fun resetCurrentEnergySlots(): PartyPokemon {
        return this.copy(
            currentEnergySlots = energySlots
        )
    }
    
    /**
     * Gain experience and check for level change
     * @param expAmount Amount of experience to gain (can be negative for level down)
     * @return Pair of (message, updated Pokemon instance)
     */
    fun gainExp(expAmount: Int): Pair<String, PartyPokemon> {
        val newCurrentExp = currentExp + expAmount
        val newLevel = calculateLevelFromExp(newCurrentExp)
        
        return when {
            newLevel > level -> {
                // Level up occurred
                val (message, updatedPokemon) = levelUp(newLevel, newCurrentExp)
                message to updatedPokemon
            }
            newLevel < level -> {
                // Level down occurred
                val updatedPokemon = levelDown(newLevel, newCurrentExp)
                "Level Down!" to updatedPokemon
            }
            else -> {
                // No level change, just update experience
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
       /* Log.d("PartyPokemon", "Current DnD stats for $name: $currentDnDStats")
        Log.d("PartyPokemon", "Stats breakdown for $name:")
        currentDnDStats.forEach { (statName, value) ->
            val modifier = floor((value - 10) / 2.0).toInt()
            val modifierText = if (modifier >= 0) "+$modifier" else "$modifier"
            Log.d("PartyPokemon", "  $statName: $value (modifier: $modifierText)")
        }*/
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
        // Convert current level to D&D level for proper comparison
        val currentDnDLevel = level
        
        // Filter moves based on D&D level conversion
        val newAvailableMoves = basePokemon.levelUpMoves.filter { move ->
            val moveDnDLevel = kotlin.math.ceil(move.levelLearnedAt / 5.0).toInt()
            val isAvailable = moveDnDLevel <= currentDnDLevel
            isAvailable
        }
        
        // Remove moves from currentMoveSet that are no longer available
        val cleanedCurrentMoveSet = currentMoveSet.filter { moveName ->
            newAvailableMoves.any { it.name == moveName }
        }
        
        val result = this.copy(
            availableMoves = newAvailableMoves,
            currentMoveSet = cleanedCurrentMoveSet
        )
        
        return result
    }

    /**
     * Simple level up - update level, proficiency, and experience
     * @param newLevel The new level
     * @param newExp The new experience total
     * @return Pair of (message, updated Pokemon instance)
     */
    private fun levelUp(newLevel: Int, newExp: Int): Pair<String, PartyPokemon> {
        Log.d("evolution test", "=== LEVEL UP PROCESS STARTED ===")
        Log.d("evolution test", "Pokemon: ${this.name} (ID: ${this.id})")
        Log.d("evolution test", "Current level: ${this.level} -> New level: $newLevel")
        Log.d("evolution test", "Current EXP: ${this.currentExp} -> New EXP: $newExp")
        Log.d("evolution test", "Current proficiency: ${this.proficiency}")
        
        val newProficiency = calculateProficiencyBonus(newLevel)
        Log.d("evolution test", "New proficiency bonus: $newProficiency")
        
        // Check if evolution level is reached
        // Evolution level is already in D&D level format, or null for special evolutions
        Log.d("evolution test", "Checking evolution requirements...")
        Log.d("evolution test", "Evolution data: ${if (evolution != null) "Present" else "None"}")
        
        val evolutionMessage = if (evolution != null) {
            if (evolution.level != null) {
                // Level-based evolution
                val evolutionDnDLevel = evolution.level
                Log.d("evolution test", "Level-based evolution: Required level = $evolutionDnDLevel")
                Log.d("evolution test", "Current level ($newLevel) >= Evolution level ($evolutionDnDLevel): ${newLevel >= evolutionDnDLevel}")
                
                if (newLevel >= evolutionDnDLevel) {
                    Log.d("evolution test", "EVOLUTION LEVEL REACHED! Pokemon can now evolve!")
                    "Reached evolution!"
                } else {
                    Log.d("evolution test", "Level up achieved, but evolution level not yet reached")
                    "Level Up!"
                }
            } else {
                // Special evolution (stone, trade, happiness, etc.) - no automatic evolution
                Log.d("evolution test", "Special evolution type detected (level = null)")
                Log.d("evolution test", "Evolution requires special method (stone, trade, happiness, etc.)")
                "Level Up!"
            }
        } else {
            Log.d("evolution test", "No evolution data available for this Pokemon")
            "Level Up!"
        }
        
        Log.d("evolution test", "Evolution message: $evolutionMessage")
        
        Log.d("evolution test", "Creating updated Pokemon with new level and EXP...")
        val updatedPokemon = this.copy(
            level = newLevel,
            currentExp = newExp,
            proficiency = newProficiency
        )
        Log.d("evolution test", "Updated Pokemon created: ${updatedPokemon.name} (Level: ${updatedPokemon.level}, EXP: ${updatedPokemon.currentExp}, Proficiency: ${updatedPokemon.proficiency})")
        
        // Recalculate available moves and clean up current move set
        Log.d("evolution test", "Recalculating available moves for new level...")
        val finalPokemon = updatedPokemon.recalculateAvailableMoves()
        Log.d("evolution test", "Final Pokemon moves recalculated: ${finalPokemon.availableMoves.size} available moves")
        Log.d("evolution test", "Final Pokemon current move set: ${finalPokemon.currentMoveSet.size} moves")
        
        // Update energy slots for new level
        Log.d("evolution test", "Updating energy slots for new level...")
        val pokemonWithEnergySlots = finalPokemon.setEnergySlots().resetCurrentEnergySlots()
        Log.d("evolution test", "Energy slots updated for level ${pokemonWithEnergySlots.level}")
        
        Log.d("evolution test", "=== LEVEL UP PROCESS COMPLETED ===")
        Log.d("evolution test", "Returning: $evolutionMessage for ${pokemonWithEnergySlots.name}")
        
        return evolutionMessage to pokemonWithEnergySlots
    }
    
    /**
     * Simple level down - update level, proficiency, and experience
     * @param newLevel The new level
     * @param newExp The new experience total
     * @return Updated Pokemon instance
     */
    private fun levelDown(newLevel: Int, newExp: Int): PartyPokemon {
        Log.d("evolution test", "=== LEVEL DOWN PROCESS STARTED ===")
        Log.d("evolution test", "Pokemon: ${this.name} (ID: ${this.id})")
        Log.d("evolution test", "Current level: ${this.level} -> New level: $newLevel")
        Log.d("evolution test", "Current EXP: ${this.currentExp} -> New EXP: $newExp")
        Log.d("evolution test", "Current proficiency: ${this.proficiency}")
        
        val newProficiency = calculateProficiencyBonus(newLevel)
        Log.d("evolution test", "New proficiency bonus: $newProficiency")
        
        Log.d("evolution test", "Creating updated Pokemon with new level and EXP...")
        val updatedPokemon = this.copy(
            level = newLevel,
            currentExp = newExp,
            proficiency = newProficiency
        )
        Log.d("evolution test", "Updated Pokemon created: ${updatedPokemon.name} (Level: ${updatedPokemon.level}, EXP: ${updatedPokemon.currentExp}, Proficiency: ${updatedPokemon.proficiency})")
        
        // Recalculate available moves and clean up current move set
        Log.d("evolution test", "Recalculating available moves for new level...")
        val finalPokemon = updatedPokemon.recalculateAvailableMoves()
        Log.d("evolution test", "Final Pokemon moves recalculated: ${finalPokemon.availableMoves.size} available moves")
        Log.d("evolution test", "Final Pokemon current move set: ${finalPokemon.currentMoveSet.size} moves")
        
        // Update energy slots for new level
        Log.d("evolution test", "Updating energy slots for new level...")
        val pokemonWithEnergySlots = finalPokemon.setEnergySlots().resetCurrentEnergySlots()
        Log.d("evolution test", "Energy slots updated for level ${pokemonWithEnergySlots.level}")
        
        Log.d("evolution test", "=== LEVEL DOWN PROCESS COMPLETED ===")
        Log.d("evolution test", "Returning updated Pokemon: ${pokemonWithEnergySlots.name}")
        
        return pokemonWithEnergySlots
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
 * level can be null for special evolutions (stone, trade, happiness, etc.)
 */
data class EvolutionDetails(
    val level: Int?,
    val evolutionId: Int
)

/**
 * Represents move data from moves_database.json
 */
data class MoveData(
    val name: String,
    val tier: Int,
    val power: Int?,
    val accuracy: Int?,
    val type: String,
    val damage_class: String,
    val description: String,
    val effect: String,
    val stat_changes: List<Any> // This can be empty list or contain stat change objects
)
