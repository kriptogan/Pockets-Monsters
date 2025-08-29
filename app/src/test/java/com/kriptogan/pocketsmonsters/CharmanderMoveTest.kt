package com.kriptogan.pocketsmonsters

import com.kriptogan.pocketsmonsters.data.models.*
import org.junit.Test
import org.junit.Assert.*

/**
 * Test class specifically for Charmander's move progression
 * Demonstrates D&D level conversion: ceil(Pokemon Level ÷ 5)
 */
class CharmanderMoveTest {
    
    @Test
    fun `test Charmander moves at different levels`() {
        // Create Charmander with its actual moves
        val charmander = Pokemon(
            id = 4,
            name = "charmander",
            height = 6,
            weight = 85,
            stats = emptyList(),
            types = emptyList(),
            baseExperience = 62,
            abilities = emptyList(),
            levelUpMoves = listOf(
                LevelUpMove("scratch", 1, "red-blue"),        // D&D Level 1: ceil(1/5) = 1
                LevelUpMove("growl", 1, "red-blue"),          // D&D Level 1: ceil(1/5) = 1
                LevelUpMove("ember", 4, "red-blue"),           // D&D Level 1: ceil(4/5) = 1
                LevelUpMove("smokescreen", 8, "red-blue"),     // D&D Level 2: ceil(8/5) = 2
                LevelUpMove("dragon-breath", 12, "red-blue"),  // D&D Level 3: ceil(12/5) = 3
                LevelUpMove("fire-fang", 17, "red-blue"),      // D&D Level 4: ceil(17/5) = 4
                LevelUpMove("slash", 20, "red-blue"),          // D&D Level 4: ceil(20/5) = 4
                LevelUpMove("flamethrower", 24, "red-blue"),   // D&D Level 5: ceil(24/5) = 5
                LevelUpMove("scary-face", 28, "red-blue"),     // D&D Level 6: ceil(28/5) = 6
                LevelUpMove("fire-spin", 32, "red-blue"),      // D&D Level 7: ceil(32/5) = 7
                LevelUpMove("inferno", 36, "red-blue"),        // D&D Level 8: ceil(36/5) = 8
                LevelUpMove("flare-blitz", 40, "red-blue")     // D&D Level 8: ceil(40/5) = 8
            ),
            spritePath = ""
        )
        
        // Test Level 1 (D&D Level 1)
        var partyCharmander = createPartyPokemon(charmander, 1)
        assertEquals(3, partyCharmander.availableMoves.size)
        assertTrue(partyCharmander.availableMoves.any { it.name == "scratch" })
        assertTrue(partyCharmander.availableMoves.any { it.name == "growl" })
        assertTrue(partyCharmander.availableMoves.any { it.name == "ember" })
        assertFalse(partyCharmander.availableMoves.any { it.name == "smokescreen" })
        
        // Test Level 2 (D&D Level 1) - Should have same moves as Level 1
        partyCharmander = createPartyPokemon(charmander, 2)
        assertEquals(3, partyCharmander.availableMoves.size)
        assertTrue(partyCharmander.availableMoves.any { it.name == "scratch" })
        assertTrue(partyCharmander.availableMoves.any { it.name == "growl" })
        assertTrue(partyCharmander.availableMoves.any { it.name == "ember" })
        assertFalse(partyCharmander.availableMoves.any { it.name == "smokescreen" })
        
        // Test Level 5 (D&D Level 1) - Should still have same moves
        partyCharmander = createPartyPokemon(charmander, 5)
        assertEquals(3, partyCharmander.availableMoves.size)
        assertTrue(partyCharmander.availableMoves.any { it.name == "scratch" })
        assertTrue(partyCharmander.availableMoves.any { it.name == "growl" })
        assertTrue(partyCharmander.availableMoves.any { it.name == "ember" })
        assertFalse(partyCharmander.availableMoves.any { it.name == "smokescreen" })
        
        // Test Level 6 (D&D Level 2) - Now Smokescreen should be available!
        partyCharmander = createPartyPokemon(charmander, 6)
        assertEquals(4, partyCharmander.availableMoves.size)
        assertTrue(partyCharmander.availableMoves.any { it.name == "scratch" })
        assertTrue(partyCharmander.availableMoves.any { it.name == "growl" })
        assertTrue(partyCharmander.availableMoves.any { it.name == "ember" })
        assertTrue(partyCharmander.availableMoves.any { it.name == "smokescreen" })
        assertFalse(partyCharmander.availableMoves.any { it.name == "dragon-breath" })
        
        // Test Level 10 (D&D Level 2) - Still same moves as Level 6
        partyCharmander = createPartyPokemon(charmander, 10)
        assertEquals(4, partyCharmander.availableMoves.size)
        assertTrue(partyCharmander.availableMoves.any { it.name == "smokescreen" })
        assertFalse(partyCharmander.availableMoves.any { it.name == "dragon-breath" })
        
        // Test Level 11 (D&D Level 3) - Now Dragon Breath should be available!
        partyCharmander = createPartyPokemon(charmander, 11)
        assertEquals(5, partyCharmander.availableMoves.size)
        assertTrue(partyCharmander.availableMoves.any { it.name == "dragon-breath" })
        assertFalse(partyCharmander.availableMoves.any { it.name == "fire-fang" })
    }
    
    @Test
    fun `test Charmander level up progression`() {
        val charmander = Pokemon(
            id = 4,
            name = "charmander",
            height = 6,
            weight = 85,
            stats = emptyList(),
            types = emptyList(),
            baseExperience = 62,
            abilities = emptyList(),
            levelUpMoves = listOf(
                LevelUpMove("scratch", 1, "red-blue"),
                LevelUpMove("growl", 1, "red-blue"),
                LevelUpMove("ember", 4, "red-blue"),
                LevelUpMove("smokescreen", 8, "red-blue"),
                LevelUpMove("dragon-breath", 12, "red-blue")
            ),
            spritePath = ""
        )
        
        // Start at Level 1
        var partyCharmander = createPartyPokemon(charmander, 1)
        assertEquals(3, partyCharmander.availableMoves.size)
        
        // Simulate gaining enough experience to reach Level 6 (D&D Level 2)
        // This would require 14000 XP total
        val (message, updatedCharmander) = partyCharmander.gainExp(14000)
        
        assertEquals(6, updatedCharmander.level)
        assertEquals(4, updatedCharmander.availableMoves.size)
        assertTrue(updatedCharmander.availableMoves.any { it.name == "smokescreen" })
    }
    
    private fun createPartyPokemon(pokemon: Pokemon, level: Int): PartyPokemon {
        return PartyPokemon(
            id = pokemon.id,
            name = pokemon.name,
            basePokemon = pokemon,
            level = level,
            currentHP = 50,
            maxHP = 50,
            actualSize = 6,
            actualWeight = 85,
            availableMoves = emptyList(), // Will be recalculated
            currentMoveSet = emptyList(),
            convertedDnDStats = emptyMap(),
            currentDnDStats = emptyMap(),
            weaknesses = emptyList(),
            resistances = emptyList()
        ).recalculateAvailableMoves()
    }
}
