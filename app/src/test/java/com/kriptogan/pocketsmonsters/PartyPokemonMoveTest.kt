package com.kriptogan.pocketsmonsters

import com.kriptogan.pocketsmonsters.data.models.*
import org.junit.Test
import org.junit.Assert.*

/**
 * Test class for PartyPokemon move management functionality
 */
class PartyPokemonMoveTest {
    
    @Test
    fun `test recalculateAvailableMoves filters moves by DnD level`() {
        // Create a base Pokemon with moves at different levels
        val basePokemon = Pokemon(
            id = 1,
            name = "TestPokemon",
            height = 10,
            weight = 10,
            stats = emptyList(),
            types = emptyList(),
            baseExperience = 100,
            abilities = emptyList(),
            levelUpMoves = listOf(
                LevelUpMove("Tackle", 1, "red-blue"),      // D&D Level 1: ceil(1/5) = 1
                LevelUpMove("Scratch", 5, "red-blue"),     // D&D Level 1: ceil(5/5) = 1
                LevelUpMove("Ember", 10, "red-blue"),      // D&D Level 2: ceil(10/5) = 2
                LevelUpMove("Fire Blast", 15, "red-blue")  // D&D Level 3: ceil(15/5) = 3
            ),
            spritePath = ""
        )
        
        // Create a PartyPokemon at level 8 (D&D Level 2: ceil(8/5) = 2)
        val partyPokemon = PartyPokemon(
            id = 1,
            name = "TestPokemon",
            basePokemon = basePokemon,
            level = 8,
            currentHP = 50,
            maxHP = 50,
            actualSize = 10,
            actualWeight = 10,
            availableMoves = emptyList(), // Will be recalculated
            currentMoveSet = listOf("Tackle", "Scratch", "Ember"),
            convertedDnDStats = emptyMap(),
            currentDnDStats = emptyMap(),
            weaknesses = emptyList(),
            resistances = emptyList()
        )
        
        // Test recalculating available moves
        val updatedPokemon = partyPokemon.recalculateAvailableMoves()
        
        // Should have 3 moves available:
        // - Tackle (Pokemon Lv1 → D&D Lv1) ✅
        // - Scratch (Pokemon Lv5 → D&D Lv1) ✅  
        // - Ember (Pokemon Lv10 → D&D Lv2) ✅
        // - Fire Blast (Pokemon Lv15 → D&D Lv3) ❌ (Pokemon Lv8 = D&D Lv2)
        assertEquals(3, updatedPokemon.availableMoves.size)
        assertTrue(updatedPokemon.availableMoves.any { it.name == "Tackle" })
        assertTrue(updatedPokemon.availableMoves.any { it.name == "Scratch" })
        assertTrue(updatedPokemon.availableMoves.any { it.name == "Ember" })
        assertFalse(updatedPokemon.availableMoves.any { it.name == "Fire Blast" })
        
        // Current move set should remain the same since all moves are still available
        assertEquals(3, updatedPokemon.currentMoveSet.size)
    }
    
    @Test
    fun `test recalculateAvailableMoves removes unavailable moves from currentMoveSet`() {
        // Create a base Pokemon with moves at different levels
        val basePokemon = Pokemon(
            id = 1,
            name = "TestPokemon",
            height = 10,
            weight = 10,
            stats = emptyList(),
            types = emptyList(),
            baseExperience = 100,
            abilities = emptyList(),
            levelUpMoves = listOf(
                LevelUpMove("Tackle", 1, "red-blue"),      // D&D Level 1: ceil(1/5) = 1
                LevelUpMove("Scratch", 5, "red-blue"),     // D&D Level 1: ceil(5/5) = 1
                LevelUpMove("Ember", 10, "red-blue")       // D&D Level 2: ceil(10/5) = 2
            ),
            spritePath = ""
        )
        
        // Create a PartyPokemon at level 3 (D&D Level 1: ceil(3/5) = 1) with moves that won't be available
        val partyPokemon = PartyPokemon(
            id = 1,
            name = "TestPokemon",
            basePokemon = basePokemon,
            level = 3,
            currentHP = 50,
            maxHP = 50,
            actualSize = 10,
            actualWeight = 10,
            availableMoves = emptyList(),
            currentMoveSet = listOf("Tackle", "Scratch", "Ember"), // Ember not available at D&D level 1
            convertedDnDStats = emptyMap(),
            currentDnDStats = emptyMap(),
            weaknesses = emptyList(),
            resistances = emptyList()
        )
        
        // Test recalculating available moves
        val updatedPokemon = partyPokemon.recalculateAvailableMoves()
        
        // Should have 2 moves available:
        // - Tackle (Pokemon Lv1 → D&D Lv1) ✅
        // - Scratch (Pokemon Lv5 → D&D Lv1) ✅
        // - Ember (Pokemon Lv10 → D&D Lv2) ❌ (Pokemon Lv3 = D&D Lv1)
        assertEquals(2, updatedPokemon.availableMoves.size)
        assertTrue(updatedPokemon.availableMoves.any { it.name == "Tackle" })
        assertTrue(updatedPokemon.availableMoves.any { it.name == "Scratch" })
        assertFalse(updatedPokemon.availableMoves.any { it.name == "Ember" })
        
        // Current move set should be cleaned up to remove unavailable moves
        assertEquals(2, updatedPokemon.currentMoveSet.size)
        assertTrue(updatedPokemon.currentMoveSet.contains("Tackle"))
        assertTrue(updatedPokemon.currentMoveSet.contains("Scratch"))
        assertFalse(updatedPokemon.currentMoveSet.contains("Ember"))
    }
    
    @Test
    fun `test levelUp recalculates available moves`() {
        // Create a base Pokemon with moves at different levels
        val basePokemon = Pokemon(
            id = 1,
            name = "TestPokemon",
            height = 10,
            weight = 10,
            stats = emptyList(),
            types = emptyList(),
            baseExperience = 100,
            abilities = emptyList(),
            levelUpMoves = listOf(
                LevelUpMove("Tackle", 1, "red-blue"),
                LevelUpMove("Scratch", 5, "red-blue"),
                LevelUpMove("Ember", 10, "red-blue")
            ),
            spritePath = ""
        )
        
        // Create a PartyPokemon at level 1
        val partyPokemon = PartyPokemon(
            id = 1,
            name = "TestPokemon",
            basePokemon = basePokemon,
            level = 1,
            currentHP = 50,
            maxHP = 50,
            actualSize = 10,
            actualWeight = 10,
            availableMoves = emptyList(),
            currentMoveSet = listOf("Tackle"),
            convertedDnDStats = emptyMap(),
            currentDnDStats = emptyMap(),
            weaknesses = emptyList(),
            resistances = emptyList()
        )
        
        // Test level up to level 5
        val (message, updatedPokemon) = partyPokemon.gainExp(300) // Should level up to level 2
        
        // Should have leveled up
        assertTrue(message.contains("Level Up"))
        assertEquals(2, updatedPokemon.level)
        
        // Should have more moves available
        assertTrue(updatedPokemon.availableMoves.size > 1)
        assertTrue(updatedPokemon.availableMoves.any { it.name == "Tackle" })
        assertTrue(updatedPokemon.availableMoves.any { it.name == "Scratch" })
    }
    
    @Test
    fun `test levelDown recalculates available moves`() {
        // Create a base Pokemon with moves at different levels
        val basePokemon = Pokemon(
            id = 1,
            name = "TestPokemon",
            height = 10,
            weight = 10,
            stats = emptyList(),
            types = emptyList(),
            baseExperience = 100,
            abilities = emptyList(),
            levelUpMoves = listOf(
                LevelUpMove("Tackle", 1, "red-blue"),
                LevelUpMove("Scratch", 5, "red-blue"),
                LevelUpMove("Ember", 10, "red-blue")
            ),
            spritePath = ""
        )
        
        // Create a PartyPokemon at level 5
        val partyPokemon = PartyPokemon(
            id = 1,
            name = "TestPokemon",
            basePokemon = basePokemon,
            level = 5,
            currentHP = 50,
            maxHP = 50,
            actualSize = 10,
            actualWeight = 10,
            availableMoves = emptyList(),
            currentMoveSet = listOf("Tackle", "Scratch"),
            convertedDnDStats = emptyMap(),
            currentDnDStats = emptyMap(),
            weaknesses = emptyList(),
            resistances = emptyList()
        )
        
        // Test level down to level 1
        val (message, updatedPokemon) = partyPokemon.gainExp(-300) // Should level down to level 1
        
        // Should have leveled down
        assertTrue(message.contains("Level Down"))
        assertEquals(1, updatedPokemon.level)
        
        // Should have fewer moves available
        assertEquals(1, updatedPokemon.availableMoves.size)
        assertTrue(updatedPokemon.availableMoves.any { it.name == "Tackle" })
        assertFalse(updatedPokemon.availableMoves.any { it.name == "Scratch" })
        
        // Current move set should be cleaned up
        assertEquals(1, updatedPokemon.currentMoveSet.size)
        assertTrue(updatedPokemon.currentMoveSet.contains("Tackle"))
        assertFalse(updatedPokemon.currentMoveSet.contains("Scratch"))
    }
}
