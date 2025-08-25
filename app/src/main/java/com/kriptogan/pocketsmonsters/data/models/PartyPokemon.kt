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
    val currentLevel: Int = 1,
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
    
    // Metadata
    val addedToPartyAt: Long = System.currentTimeMillis()
)

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
