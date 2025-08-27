package com.kriptogan.pocketsmonsters.data.party

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.kriptogan.pocketsmonsters.data.converter.DnDConverter
import com.kriptogan.pocketsmonsters.data.models.Pokemon
import com.kriptogan.pocketsmonsters.data.models.PartyPokemon
import com.kriptogan.pocketsmonsters.data.models.Condition
import com.kriptogan.pocketsmonsters.data.models.Nature
import kotlin.random.Random

class PartyManager(context: Context) {
    
    companion object {
        private const val TAG = "PartyManager"
        private const val PREFS_NAME = "party_prefs"
        private const val KEY_PARTY = "party_pokemon"
        private const val MAX_PARTY_SIZE = 6
    }
    
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()
    private val dndConverter = DnDConverter()
    
    /**
     * Get current party
     */
    fun getParty(): List<PartyPokemon> {
        val partyJson = sharedPreferences.getString(KEY_PARTY, "[]")
        return try {
            val type = object : TypeToken<List<PartyPokemon>>() {}.type
            val party = gson.fromJson<List<PartyPokemon>>(partyJson, type) ?: emptyList()
            
            // Migrate existing Pokemon to have natures if they don't have one
            val migratedParty = migratePartyNatures(party)
            if (migratedParty != party) {
                saveParty(migratedParty)
            }
            
            migratedParty
        } catch (e: Exception) {
            Log.e(TAG, "Error loading party: ${e.message}")
            emptyList()
        }
    }
    
    /**
     * Add Pokemon to party
     */
    fun addToParty(pokemon: Pokemon): Result<PartyPokemon> {
        val currentParty = getParty()
        
        if (currentParty.size >= MAX_PARTY_SIZE) {
            return Result.failure(IllegalStateException("Party is full"))
        }
        
        if (currentParty.any { it.id == pokemon.id }) {
            return Result.failure(IllegalStateException("Pokemon already in party"))
        }
        
        try {
            val partyPokemon = createPartyPokemon(pokemon)
            val newParty = currentParty + partyPokemon
            saveParty(newParty)
            
            Log.d(TAG, "Added ${pokemon.name} to party. Party size: ${newParty.size}")
            return Result.success(partyPokemon)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error adding Pokemon to party: ${e.message}")
            return Result.failure(e)
        }
    }
    
    /**
     * Remove Pokemon from party
     */
    fun removeFromParty(pokemonId: Int): Result<Unit> {
        val currentParty = getParty()
        val newParty = currentParty.filter { it.id != pokemonId }
        
        if (newParty.size == currentParty.size) {
            return Result.failure(IllegalStateException("Pokemon not found in party"))
        }
        
        saveParty(newParty)
        Log.d(TAG, "Removed Pokemon $pokemonId from party. Party size: ${newParty.size}")
        return Result.success(Unit)
    }
    
    /**
     * Update party Pokemon
     */
    fun updatePartyPokemon(updatedPokemon: PartyPokemon): Result<Unit> {
        val currentParty = getParty()
        val index = currentParty.indexOfFirst { it.id == updatedPokemon.id }
        
        if (index == -1) {
            return Result.failure(IllegalStateException("Pokemon not found in party"))
        }
        
        val newParty = currentParty.toMutableList()
        newParty[index] = updatedPokemon
        saveParty(newParty)
        
        Log.d(TAG, "Updated ${updatedPokemon.name} in party")
        return Result.success(Unit)
    }
    
    /**
     * Update party Pokemon HP
     */
    fun updatePartyPokemonHP(pokemonId: Int, newHP: Int): Result<Unit> {
        val currentParty = getParty()
        val index = currentParty.indexOfFirst { it.id == pokemonId }
        
        if (index == -1) {
            return Result.failure(IllegalStateException("Pokemon not found in party"))
        }
        
        val pokemon = currentParty[index]
        val updatedPokemon = pokemon.copy(currentHP = newHP)
        
        val newParty = currentParty.toMutableList()
        newParty[index] = updatedPokemon
        saveParty(newParty)
        
        Log.d(TAG, "Updated ${pokemon.name} HP to $newHP")
        return Result.success(Unit)
    }
    
    /**
     * Check if party is full
     */
    fun isPartyFull(): Boolean {
        return getParty().size >= MAX_PARTY_SIZE
    }
    
    /**
     * Get party size
     */
    fun getPartySize(): Int {
        return getParty().size
    }
    
    /**
     * Check if Pokemon is in party
     */
    fun isInParty(pokemonId: Int): Boolean {
        return getParty().any { it.id == pokemonId }
    }
    
    /**
     * Create a new PartyPokemon from base Pokemon
     */
    private fun createPartyPokemon(pokemon: Pokemon): PartyPokemon {
        // Convert to D&D stats
        val dndView = dndConverter.convertPokemonToDnD(pokemon)
        
        // Calculate HP based on current level (level 1) using new rules
        val baseHP = pokemon.stats.find { it.stat.name == "hp" }?.baseStat ?: 0
        val adjustedBaseHP = kotlin.math.floor(baseHP / 3.0).toInt() // New rule: floor(Base HP ÷ 3)
        val maxHP = adjustedBaseHP // For level 1, HP equals adjusted base HP
        
        // Generate size and weight variations (5% variation)
        val sizeVariation = Random.nextDouble(-0.05, 0.06) // -5% to +5%
        val weightVariation = Random.nextDouble(-0.05, 0.06) // -5% to +5%
        val actualSize = maxOf(1, (pokemon.height * (1 + sizeVariation)).toInt())
        val actualWeight = maxOf(1, (pokemon.weight * (1 + weightVariation)).toInt())
        
        // Get available moves for level 1
        val availableMoves = pokemon.levelUpMoves.filter { it.levelLearnedAt <= 1 }
        
        // Calculate weaknesses and resistances (simplified - you can expand this)
        val weaknesses = calculateWeaknesses(pokemon.types.map { it.type.name })
        val resistances = calculateResistances(pokemon.types.map { it.type.name })
        
        // Assign a random nature
        val randomNature = getRandomNature()
        
        return PartyPokemon(
            id = pokemon.id,
            name = pokemon.name,
            basePokemon = pokemon,
            level = 1, // Use the new level property
            currentHP = adjustedBaseHP, // Use adjusted base HP
            maxHP = maxHP,
            actualSize = actualSize,
            actualWeight = actualWeight,
            availableMoves = availableMoves,
            currentMoveSet = availableMoves.take(4).map { it.name },
            convertedDnDStats = dndView.convertedStats,
            currentDnDStats = dndView.convertedStats.toMutableMap(),
            weaknesses = weaknesses,
            resistances = resistances,
            conditions = emptyList(),
            nature = randomNature,
            // Experience properties with default values
            currentExp = 0,
            expToLevelUp = 300,
            proficiency = 2,
            
            // D&D derived stats (calculated from currentDnDStats and actualWeight)
            movementSpeed = 30
        )
    }
    
    /**
     * Get a random nature for a Pokemon
     */
    private fun getRandomNature(): Nature {
        val natures = listOf(
            Nature("Hardy", null, null, "Neutral nature"),
            Nature("Lonely", "Attack", "Defense", "Loves to eat"),
            Nature("Brave", "Attack", "Speed", "Often dozes off"),
            Nature("Adamant", "Attack", "Sp. Atk", "Sturdy body"),
            Nature("Naughty", "Attack", "Sp. Def", "Likes to fight"),
            Nature("Bold", "Defense", "Attack", "Proud of its power"),
            Nature("Docile", null, null, "Sturdy body"),
            Nature("Relaxed", "Defense", "Speed", "Likes to relax"),
            Nature("Impish", "Defense", "Sp. Atk", "Proud of its power"),
            Nature("Lax", "Defense", "Sp. Def", "Loves to eat"),
            Nature("Timid", "Speed", "Attack", "Likes to run"),
            Nature("Hasty", "Speed", "Defense", "Somewhat of a clown"),
            Nature("Serious", null, null, "Strong willed"),
            Nature("Jolly", "Speed", "Sp. Atk", "Good perseverance"),
            Nature("Naive", "Speed", "Sp. Def", "Likes to thrash about"),
            Nature("Modest", "Sp. Atk", "Attack", "Loves to eat"),
            Nature("Mild", "Sp. Atk", "Defense", "Proud of its power"),
            Nature("Quiet", "Sp. Atk", "Speed", "Sturdy body"),
            Nature("Bashful", null, null, "Somewhat stubborn"),
            Nature("Rash", "Sp. Atk", "Sp. Def", "Likes to run"),
            Nature("Calm", "Sp. Def", "Attack", "Strong willed"),
            Nature("Gentle", "Sp. Def", "Defense", "Loves to eat"),
            Nature("Careful", "Sp. Def", "Sp. Atk", "Often lost in thought"),
            Nature("Quirky", null, null, "Mischievous"),
            Nature("Sassy", "Sp. Def", "Speed", "Somewhat vain")
        )
        
        return natures.random()
    }
    
    /**
     * Save party to SharedPreferences
     */
    private fun saveParty(party: List<PartyPokemon>) {
        val partyJson = gson.toJson(party)
        sharedPreferences.edit().putString(KEY_PARTY, partyJson).apply()
        Log.d(TAG, "Party saved. Size: ${party.size}")
    }
    
    /**
     * Migrate existing PartyPokemon objects to have natures
     */
    private fun migratePartyNatures(party: List<PartyPokemon>): List<PartyPokemon> {
        val needsMigration = party.any { it.nature.name == "Hardy" && it.nature.description == "Neutral nature" }
        
        if (!needsMigration) {
            return party
        }
        
        Log.d(TAG, "Migrating existing party Pokemon to have random natures")
        
        return party.map { pokemon ->
            if (pokemon.nature.name == "Hardy" && pokemon.nature.description == "Neutral nature") {
                // This is likely a migrated Pokemon, assign a random nature
                pokemon.copy(nature = getRandomNature())
            } else {
                pokemon
            }
        }
    }
    
    /**
     * Calculate weaknesses based on types (simplified)
     */
    private fun calculateWeaknesses(types: List<String>): List<String> {
        // This is a simplified implementation
        // You can expand this with proper type effectiveness tables
        return when {
            types.contains("fire") -> listOf("water", "ground", "rock")
            types.contains("water") -> listOf("electric", "grass")
            types.contains("grass") -> listOf("fire", "ice", "poison", "flying", "bug")
            types.contains("electric") -> listOf("ground")
            types.contains("ice") -> listOf("fire", "fighting", "rock", "steel")
            types.contains("fighting") -> listOf("flying", "psychic", "fairy")
            types.contains("poison") -> listOf("ground", "psychic")
            types.contains("ground") -> listOf("water", "grass", "ice")
            types.contains("flying") -> listOf("electric", "ice", "rock")
            types.contains("psychic") -> listOf("bug", "ghost", "dark")
            types.contains("bug") -> listOf("fire", "flying", "rock")
            types.contains("rock") -> listOf("water", "grass", "fighting", "ground", "steel")
            types.contains("ghost") -> listOf("ghost", "dark")
            types.contains("dragon") -> listOf("ice", "dragon", "fairy")
            types.contains("dark") -> listOf("fighting", "bug", "fairy")
            types.contains("steel") -> listOf("fire", "fighting", "ground")
            types.contains("fairy") -> listOf("poison", "steel")
            else -> emptyList()
        }
    }
    
    /**
     * Calculate resistances based on types (simplified)
     */
    private fun calculateResistances(types: List<String>): List<String> {
        // This is a simplified implementation
        // You can expand this with proper type effectiveness tables
        return when {
            types.contains("fire") -> listOf("fire", "grass", "ice", "bug", "steel")
            types.contains("water") -> listOf("fire", "water", "ice", "steel")
            types.contains("grass") -> listOf("water", "electric", "grass", "ground")
            types.contains("electric") -> listOf("electric", "flying", "steel")
            types.contains("ice") -> listOf("ice")
            types.contains("fighting") -> listOf("bug", "rock", "dark")
            types.contains("poison") -> listOf("grass", "fighting", "poison", "bug", "fairy")
            types.contains("ground") -> listOf("poison", "rock")
            types.contains("flying") -> listOf("grass", "fighting", "bug")
            types.contains("psychic") -> listOf("fighting", "psychic")
            types.contains("bug") -> listOf("grass", "fighting", "ground")
            types.contains("rock") -> listOf("normal", "fire", "poison", "flying")
            types.contains("ghost") -> listOf("poison", "bug")
            types.contains("dragon") -> listOf("fire", "water", "electric", "grass")
            types.contains("dark") -> listOf("ghost", "dark")
            types.contains("steel") -> listOf("normal", "grass", "ice", "flying", "psychic", "bug", "rock", "dragon", "steel", "fairy")
            types.contains("fairy") -> listOf("fighting", "bug", "dark")
            else -> emptyList()
        }
    }
}
