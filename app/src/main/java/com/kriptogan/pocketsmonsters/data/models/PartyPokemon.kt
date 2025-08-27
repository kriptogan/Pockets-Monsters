package com.kriptogan.pocketsmonsters.data.models

import com.google.gson.annotations.SerializedName

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
    
    // Metadata
    val addedToPartyAt: Long = System.currentTimeMillis()
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
    }
    
    /**
     * Gain experience and check for level up and proficiency increase
     * @param expAmount Amount of experience to gain
     * @return Triple of (newLevel, leveledUp, proficiencyIncreased) where:
     *         - newLevel: the new level after gaining experience
     *         - leveledUp: true if a level up occurred
     *         - proficiencyIncreased: true if proficiency bonus increased
     */
    fun gainExp(expAmount: Int): Triple<Int, Boolean, Boolean> {
        val newCurrentExp = currentExp + expAmount
        var newLevel = level
        var leveledUp = false
        var proficiencyIncreased = false
        
        // Check if we can level up
        while (newLevel < 20 && newCurrentExp >= EXP_TABLE[newLevel + 1]!!) {
            newLevel++
            leveledUp = true
            
            // Check if proficiency bonus increased
            val oldProficiency = calculateProficiencyBonus(newLevel - 1)
            val newProficiency = calculateProficiencyBonus(newLevel)
            if (newProficiency > oldProficiency) {
                proficiencyIncreased = true
            }
        }
        
        // Calculate new exp to next level
        val newExpToLevelUp = if (newLevel < 20) {
            EXP_TABLE[newLevel + 1]!! - newCurrentExp
        } else {
            0 // Max level reached
        }
        
        return Triple(newLevel, leveledUp, proficiencyIncreased)
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
     * Check if the Pokemon can level up
     */
    fun canLevelUp(): Boolean = level < 20 && currentExp >= expToLevelUp
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
