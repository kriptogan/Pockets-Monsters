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
import com.kriptogan.pocketsmonsters.data.models.TypeSlot
import com.kriptogan.pocketsmonsters.data.models.TypeInfo
import com.kriptogan.pocketsmonsters.data.models.Stat
import com.kriptogan.pocketsmonsters.data.models.StatInfo
import com.kriptogan.pocketsmonsters.data.models.LevelUpMove
import com.kriptogan.pocketsmonsters.data.models.AbilitySlot
import com.kriptogan.pocketsmonsters.data.models.Ability
import kotlin.random.Random

class PartyManager(context: Context) {
    
    companion object {
        private const val TAG = "PartyManager"
        private const val PREFS_NAME = "party_prefs"
        private const val KEY_PARTY = "party_pokemon"
        private const val MAX_PARTY_SIZE = 6
    }
    
    private val context: Context = context
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
     * Execute evolution for a Pokemon
     * @param pokemonId The ID of the Pokemon to evolve
     * @return Result containing the evolved Pokemon or error message
     */
    fun executeEvolution(pokemonId: Int): Result<PartyPokemon> {
        return try {
            android.util.Log.d(TAG, "Evolution started for Pokemon ID: $pokemonId")
            
            // 1. Find the current Pokemon in the party
            val currentPokemon = getParty().find { it.id == pokemonId }
                ?: return Result.failure(Exception("Pokemon not found in party"))
            
            // 2. Save the current values to preserve
            val natureToPreserve = currentPokemon.nature
            val levelToPreserve = currentPokemon.level
            val currentExpToPreserve = currentPokemon.currentExp
            
            android.util.Log.d(TAG, "Preserving: Nature=${natureToPreserve.name}, Level=$levelToPreserve, Exp=$currentExpToPreserve")
            
            // 3. Search for the evolution form in pokemons.json
            val evolutionData = currentPokemon.evolution
                ?: return Result.failure(Exception("No evolution data found"))
            
            val evolvedBasePokemon = findPokemonById(evolutionData.evolutionId)
                ?: return Result.failure(Exception("Evolution form not found in pokemons.json"))
            
                    android.util.Log.d(TAG, "Found evolution form: ${evolvedBasePokemon.name}")
        android.util.Log.d(TAG, "Evolution form stats: ${evolvedBasePokemon.stats.map { "${it.stat.name}=${it.baseStat}" }}")
        android.util.Log.d(TAG, "Evolution form height: ${evolvedBasePokemon.height}, weight: ${evolvedBasePokemon.weight}")
            
            // 4. Create new PartyPokemon with evolved form but preserved values
            val evolvedPartyPokemon = createPartyPokemon(
                pokemon = evolvedBasePokemon,
                level = levelToPreserve,
                currentExp = currentExpToPreserve,
                nature = natureToPreserve
            )
            
            android.util.Log.d(TAG, "Created evolved PartyPokemon: maxHP=${evolvedPartyPokemon.maxHP}, currentDnDStats=${evolvedPartyPokemon.currentDnDStats}")
            
            // 5. Replace the old Pokemon with the evolved one
            val currentParty = getParty().toMutableList()
            val pokemonIndex = currentParty.indexOfFirst { it.id == pokemonId }
            if (pokemonIndex != -1) {
                currentParty[pokemonIndex] = evolvedPartyPokemon
                saveParty(currentParty)
                android.util.Log.d(TAG, "Evolution completed successfully!")
                Result.success(evolvedPartyPokemon)
            } else {
                Result.failure(Exception("Failed to replace Pokemon in party"))
            }
            
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Evolution failed", e)
            Result.failure(e)
        }
    }
    
    /**
     * Find Pokemon by ID in pokemons.json
     * @param pokemonId The ID of the Pokemon to find
     * @return Pokemon if found, null otherwise
     */
    private fun findPokemonById(pokemonId: Int): Pokemon? {
        return try {
            val inputStream = context.assets.open("pokemons.json")
            val jsonString = inputStream.bufferedReader().use { it.readText() }
            val jsonArray = org.json.JSONArray(jsonString)
            
            for (i in 0 until jsonArray.length()) {
                val pokemon = jsonArray.getJSONObject(i)
                if (pokemon.getInt("id") == pokemonId) {
                    // Parse the Pokemon data
                    val name = pokemon.getString("name")
                    val types = mutableListOf<TypeSlot>()
                    val stats = mutableListOf<Stat>()
                    val moves = mutableListOf<LevelUpMove>()
                    val abilities = mutableListOf<AbilitySlot>()
                    
                    // Parse types
                    if (pokemon.has("types")) {
                        val typesArray = pokemon.getJSONArray("types")
                        for (j in 0 until typesArray.length()) {
                            val type = typesArray.getJSONObject(j)
                            val slot = type.getInt("slot")
                            val typeName = type.getJSONObject("type").getString("name")
                                                         types.add(TypeSlot(slot, TypeInfo(typeName, "")))
                        }
                    }
                    
                    // Parse stats
                    if (pokemon.has("stats")) {
                        val statsArray = pokemon.getJSONArray("stats")
                        for (j in 0 until statsArray.length()) {
                            val stat = statsArray.getJSONObject(j)
                            val baseStat = stat.getInt("base_stat")
                            val statName = stat.getJSONObject("stat").getString("name")
                                                         stats.add(Stat(baseStat, 0, StatInfo(statName, "")))
                        }
                    }
                    
                    // Parse moves
                    if (pokemon.has("moves")) {
                        val movesArray = pokemon.getJSONArray("moves")
                        for (j in 0 until movesArray.length()) {
                            val move = movesArray.getJSONObject(j)
                            val levelLearnedAt = move.getJSONArray("version_group_details")
                                .getJSONObject(0)
                                .getJSONObject("level_learned_at")
                                .getInt("level")
                            val moveName = move.getJSONObject("move").getString("name")
                                                         moves.add(LevelUpMove(moveName, levelLearnedAt, "red-blue"))
                        }
                    }
                    
                    // Parse abilities
                    if (pokemon.has("abilities")) {
                        val abilitiesArray = pokemon.getJSONArray("abilities")
                        for (j in 0 until abilitiesArray.length()) {
                            val ability = abilitiesArray.getJSONObject(j)
                            val slot = ability.getInt("slot")
                            val abilityName = ability.getJSONObject("ability").getString("name")
                                                         abilities.add(AbilitySlot(Ability(abilityName), false, slot))
                        }
                    }
                    
                    // Parse height and weight
                    val height = if (pokemon.has("height")) pokemon.getInt("height") else 1
                    val weight = if (pokemon.has("weight")) pokemon.getInt("weight") else 1
                    val baseExperience = if (pokemon.has("base_experience")) pokemon.getInt("base_experience") else 0
                    val spritePath = if (pokemon.has("sprites") && pokemon.getJSONObject("sprites").has("front_default")) {
                        pokemon.getJSONObject("sprites").getString("front_default")
                    } else ""
                    
                    return Pokemon(
                        id = pokemonId,
                        name = name,
                        types = types,
                        stats = stats,
                        abilities = abilities,
                        height = height,
                        weight = weight,
                        baseExperience = baseExperience,
                        levelUpMoves = moves, // Use the parsed moves
                        spritePath = spritePath
                    )
                }
            }
            null
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Error finding Pokemon by ID", e)
            null
        }
    }
    
    /**
     * Create PartyPokemon with specific level, experience, and nature
     * @param pokemon The base Pokemon
     * @param level The desired level
     * @param currentExp The desired current experience
     * @param nature The desired nature
     * @return New PartyPokemon instance
     */
    private fun createPartyPokemon(
        pokemon: Pokemon,
        level: Int,
        currentExp: Int,
        nature: Nature
    ): PartyPokemon {
        android.util.Log.d(TAG, "=== Creating PartyPokemon for ${pokemon.name} ===")
        android.util.Log.d(TAG, "Input parameters: level=$level, currentExp=$currentExp, nature=${nature.name}")
        
        // Convert to D&D stats using DnDConverter
        val dndView = dndConverter.convertPokemonToDnD(pokemon)
        android.util.Log.d(TAG, "DnD conversion for ${pokemon.name}: convertedStats=${dndView.convertedStats}")
        
        // Calculate proficiency bonus for the specified level
        val proficiency = calculateProficiencyBonus(level)
        android.util.Log.d(TAG, "Proficiency bonus for level $level: $proficiency")
        
        // Calculate HP based on level and base stats using proper DnD conversion
        val baseHP = pokemon.stats.find { it.stat.name == "hp" }?.baseStat ?: 10
        val adjustedBaseHP = kotlin.math.floor(baseHP / 3.0).toInt() // New rule: floor(Base HP ÷ 3)
        
        // Calculate HP using proper Hit Dice rules
        val hitDiceValue = when {
            baseHP <= 50 -> 4  // d6 average
            baseHP <= 100 -> 5 // d8 average
            baseHP <= 150 -> 6 // d10 average
            else -> 7          // d12 average
        }
        val maxHP = adjustedBaseHP
        
        android.util.Log.d(TAG, "HP calculation: baseHP=$baseHP, adjustedBaseHP=$adjustedBaseHP, hitDiceValue=$hitDiceValue, level=$level, maxHP=$maxHP")
        
        // Generate size and weight variations (5% variation)
        val sizeVariation = Random.nextDouble(-0.05, 0.06) // -5% to +5%
        val weightVariation = Random.nextDouble(-0.05, 0.06) // -5% to +5%
        val actualSize = maxOf(1, (pokemon.height * (1 + sizeVariation)).toInt())
        val actualWeight = maxOf(1, (pokemon.weight * (1 + weightVariation)).toInt())
        
        android.util.Log.d(TAG, "Size/Weight calculation: baseHeight=${pokemon.height}, baseWeight=${pokemon.weight}, actualSize=$actualSize, actualWeight=$actualWeight")
        
        // Keep base DnD stats without nature modifiers - they will be applied during calculations
        val currentDnDStats = dndView.convertedStats.toMutableMap()
        android.util.Log.d(TAG, "Base DnD stats (no nature modifiers): $currentDnDStats")
        
        // Search for evolution data for the evolved form
        val evolutionData = PartyPokemon.findEvolutionData(context, pokemon.id)
        android.util.Log.d(TAG, "Evolution data found: $evolutionData")
        
        android.util.Log.d("lvlup process", "=== Creating PartyPokemon ===")
        android.util.Log.d("lvlup process", "Base Pokemon levelUpMoves count: ${pokemon.levelUpMoves.size}")
        android.util.Log.d("lvlup process", "Base Pokemon levelUpMoves: ${pokemon.levelUpMoves.map { "${it.name} (Lv${it.levelLearnedAt})" }}")
        android.util.Log.d("lvlup process", "Creating for level: $level")
        
        // Convert level to D&D level for proper move filtering
        val currentDnDLevel = kotlin.math.ceil(level / 5.0).toInt()
        android.util.Log.d("lvlup process", "Current D&D level: $currentDnDLevel (from Pokemon level $level)")
        
        // Filter moves based on D&D level conversion
        val availableMoves = pokemon.levelUpMoves.filter { move ->
            val moveDnDLevel = kotlin.math.ceil(move.levelLearnedAt / 5.0).toInt()
            val isAvailable = moveDnDLevel <= currentDnDLevel
            android.util.Log.d("lvlup process", "Move ${move.name}: Pokemon Lv${move.levelLearnedAt} → D&D Lv$moveDnDLevel, available=$isAvailable")
            isAvailable
        }
        
        android.util.Log.d("lvlup process", "Filtered availableMoves count: ${availableMoves.size}")
        
        val partyPokemon = PartyPokemon(
            id = pokemon.id,
            name = pokemon.name,
            basePokemon = pokemon,
            level = level,
            currentExp = currentExp,
            maxHP = maxHP,
            currentHP = maxHP, // Start with full HP
            nature = nature,
            proficiency = proficiency,
            currentMoveSet = emptyList(),
            conditions = emptyList(),
            weaknesses = calculateWeaknesses(pokemon.types.map { it.type.name }),
            resistances = calculateResistances(pokemon.types.map { it.type.name }),
            actualSize = actualSize,
            actualWeight = actualWeight,
            availableMoves = availableMoves,
            convertedDnDStats = dndView.convertedStats,
            currentDnDStats = currentDnDStats,
            movementSpeed = 30, // This will be recalculated by the Pokemon
            evolution = evolutionData
        )
        
        android.util.Log.d(TAG, "=== PartyPokemon created successfully ===")
        android.util.Log.d(TAG, "Final PartyPokemon: id=${partyPokemon.id}, name=${partyPokemon.name}, level=${partyPokemon.level}, maxHP=${partyPokemon.maxHP}, currentDnDStats=${partyPokemon.currentDnDStats}")
        
        return partyPokemon
    }
    
    /**
     * Calculate proficiency bonus based on level
     */
    private fun calculateProficiencyBonus(level: Int): Int {
        return when {
            level <= 4 -> 2
            level <= 8 -> 3
            level <= 12 -> 4
            level <= 16 -> 5
            else -> 6
        }
    }
    
    /**
     * Calculate max HP based on base HP and level using proper Hit Dice rules
     */
    private fun calculateMaxHP(baseHP: Int, level: Int): Int {
        val dndHP = kotlin.math.floor(baseHP / 3.0).toInt()
        val hitDiceValue = when {
            baseHP <= 50 -> 4  // d6 average
            baseHP <= 100 -> 5 // d8 average
            baseHP <= 150 -> 6 // d10 average
            else -> 7          // d12 average
        }
        return dndHP + (level - 1) * hitDiceValue
    }
    

    
    /**
     * Create a new PartyPokemon from base Pokemon
     */
    private fun createPartyPokemon(pokemon: Pokemon): PartyPokemon {
        android.util.Log.d(TAG, "=== Creating initial PartyPokemon for ${pokemon.name} ===")
        android.util.Log.d(TAG, "Input parameters: level=1, currentExp=0, random nature")
        
        // Convert to D&D stats
        val dndView = dndConverter.convertPokemonToDnD(pokemon)
        android.util.Log.d(TAG, "DnD conversion for ${pokemon.name}: convertedStats=${dndView.convertedStats}")
        
        // Calculate HP based on current level (level 1) using new rules
        val baseHP = pokemon.stats.find { it.stat.name == "hp" }?.baseStat ?: 0
        val adjustedBaseHP = kotlin.math.floor(baseHP / 3.0).toInt() // New rule: floor(Base HP ÷ 3)
        val maxHP = adjustedBaseHP // For level 1, HP equals adjusted base HP
        
        android.util.Log.d(TAG, "HP calculation: baseHP=$baseHP, adjustedBaseHP=$adjustedBaseHP, maxHP=$maxHP")
        
        // Generate size and weight variations (5% variation)
        val sizeVariation = Random.nextDouble(-0.05, 0.06) // -5% to +5%
        val weightVariation = Random.nextDouble(-0.05, 0.06) // -5% to +5%
        val actualSize = maxOf(1, (pokemon.height * (1 + sizeVariation)).toInt())
        val actualWeight = maxOf(1, (pokemon.weight * (1 + weightVariation)).toInt())
        
        android.util.Log.d(TAG, "Size/Weight calculation: baseHeight=${pokemon.height}, baseWeight=${pokemon.weight}, actualSize=$actualSize, actualWeight=$actualWeight")
        
        // Get available moves for level 1 using D&D level conversion
        val currentDnDLevel = kotlin.math.ceil(1 / 5.0).toInt() // Always 1 for level 1
        android.util.Log.d("lvlup process", "Creating level 1 Pokemon - D&D level: $currentDnDLevel")
        
        val availableMoves = pokemon.levelUpMoves.filter { move ->
            val moveDnDLevel = kotlin.math.ceil(move.levelLearnedAt / 5.0).toInt()
            val isAvailable = moveDnDLevel <= currentDnDLevel
            android.util.Log.d("lvlup process", "Move ${move.name}: Pokemon Lv${move.levelLearnedAt} → D&D Lv$moveDnDLevel, available=$isAvailable")
            isAvailable
        }
        
        // Calculate weaknesses and resistances (simplified - you can expand this)
        val weaknesses = calculateWeaknesses(pokemon.types.map { it.type.name })
        val resistances = calculateResistances(pokemon.types.map { it.type.name })
        
        // Assign a random nature
        val randomNature = getRandomNature()
        android.util.Log.d(TAG, "Random nature assigned: ${randomNature.name}")
        
        // Keep base DnD stats without nature modifiers - they will be applied during calculations
        val currentDnDStats = dndView.convertedStats.toMutableMap()
        android.util.Log.d(TAG, "Base DnD stats (no nature modifiers): $currentDnDStats")
        
        // Search for evolution data
        val evolutionData = PartyPokemon.findEvolutionData(context, pokemon.id)
        android.util.Log.d(TAG, "Evolution data found: $evolutionData")
        
        val partyPokemon = PartyPokemon(
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
            currentDnDStats = currentDnDStats,
            weaknesses = weaknesses,
            resistances = resistances,
            conditions = emptyList(),
            nature = randomNature,
            // Experience properties with default values
            currentExp = 0,
            expToLevelUp = 300,
            proficiency = 2,
            
            // D&D derived stats (calculated from currentDnDStats and actualWeight)
            movementSpeed = 30,
            
            // Evolution data
            evolution = evolutionData
        )
        
        android.util.Log.d(TAG, "=== Initial PartyPokemon created successfully ===")
        android.util.Log.d(TAG, "Final PartyPokemon: id=${partyPokemon.id}, name=${partyPokemon.name}, level=${partyPokemon.level}, maxHP=${partyPokemon.maxHP}, currentDnDStats=${partyPokemon.currentDnDStats}")
        
        return partyPokemon
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
     * Calculate weaknesses based on types with proper dual type support
     */
    private fun calculateWeaknesses(types: List<String>): List<String> {
        if (types.isEmpty()) return emptyList()
        if (types.size == 1) return getSingleTypeWeaknesses(types[0])
        
        // For dual types, we need to calculate combined effectiveness
        val type1 = types[0]
        val type2 = types[1]
        
        // Get individual type weaknesses
        val weaknesses1 = getSingleTypeWeaknesses(type1)
        val weaknesses2 = getSingleTypeWeaknesses(type2)
        
        // Get individual type resistances
        val resistances1 = getSingleTypeResistances(type1)
        val resistances2 = getSingleTypeResistances(type2)
        
        // Get individual type immunities
        val immunities1 = getSingleTypeImmunities(type1)
        val immunities2 = getSingleTypeImmunities(type2)
        
        // Calculate combined effectiveness
        val allTypes = (weaknesses1 + weaknesses2 + resistances1 + resistances2 + immunities1 + immunities2).distinct()
        
        return allTypes.filter { type ->
            val effectiveness1 = getTypeEffectiveness(type, type1)
            val effectiveness2 = getTypeEffectiveness(type, type2)
            val totalEffectiveness = effectiveness1 * effectiveness2
            
            // Return true if this type is weak (2x or 4x effective) AND not immune
            totalEffectiveness > 1.0 && totalEffectiveness > 0.0
        }
    }
    
    /**
     * Calculate resistances based on types with proper dual type support
     */
    private fun calculateResistances(types: List<String>): List<String> {
        if (types.isEmpty()) return emptyList()
        if (types.size == 1) return getSingleTypeResistances(types[0])
        
        // For dual types, calculate combined effectiveness
        val type1 = types[0]
        val type2 = types[1]
        
        // Get all types that could potentially be resisted
        val allTypes = listOf("normal", "fire", "water", "electric", "grass", "ice", "fighting", 
                              "poison", "ground", "flying", "psychic", "bug", "rock", "ghost", 
                              "dragon", "dark", "steel", "fairy")
        
        return allTypes.filter { type ->
            val effectiveness1 = getTypeEffectiveness(type, type1)
            val effectiveness2 = getTypeEffectiveness(type, type2)
            val totalEffectiveness = effectiveness1 * effectiveness2
            
            // Return true if this type is resisted (0.5x or 0.25x effective)
            totalEffectiveness < 1.0 && totalEffectiveness > 0.0
        }
    }
    
    /**
     * Get single type weaknesses
     */
    private fun getSingleTypeWeaknesses(type: String): List<String> {
        return when (type.lowercase()) {
            "normal" -> listOf("fighting")
            "fire" -> listOf("water", "ground", "rock")
            "water" -> listOf("electric", "grass")
            "electric" -> listOf("ground")
            "grass" -> listOf("fire", "ice", "poison", "flying", "bug")
            "ice" -> listOf("fire", "fighting", "rock", "steel")
            "fighting" -> listOf("flying", "psychic", "fairy")
            "poison" -> listOf("ground", "psychic")
            "ground" -> listOf("water", "grass", "ice")
            "flying" -> listOf("electric", "ice", "rock")
            "psychic" -> listOf("bug", "ghost", "dark")
            "bug" -> listOf("fire", "flying", "rock")
            "rock" -> listOf("water", "grass", "fighting", "ground", "steel")
            "ghost" -> listOf("ghost", "dark")
            "dragon" -> listOf("ice", "dragon", "fairy")
            "dark" -> listOf("fighting", "bug", "fairy")
            "steel" -> listOf("fire", "fighting", "ground")
            "fairy" -> listOf("poison", "steel")
            else -> emptyList()
        }
    }
    
    /**
     * Get single type resistances
     */
    private fun getSingleTypeResistances(type: String): List<String> {
        return when (type.lowercase()) {
            "normal" -> emptyList()
            "fire" -> listOf("fire", "grass", "ice", "bug", "steel")
            "water" -> listOf("fire", "water", "ice", "steel")
            "electric" -> listOf("electric", "flying", "steel")
            "grass" -> listOf("water", "electric", "grass", "ground")
            "ice" -> listOf("ice")
            "fighting" -> listOf("bug", "rock", "dark")
            "poison" -> listOf("grass", "fighting", "poison", "bug", "fairy")
            "ground" -> listOf("poison", "rock")
            "flying" -> listOf("grass", "fighting", "bug")
            "psychic" -> listOf("fighting", "psychic")
            "bug" -> listOf("grass", "fighting", "ground")
            "rock" -> listOf("normal", "fire", "poison", "flying")
            "ghost" -> listOf("poison", "bug")
            "dragon" -> listOf("fire", "water", "electric", "grass")
            "dark" -> listOf("ghost", "dark")
            "steel" -> listOf("normal", "grass", "ice", "flying", "psychic", "bug", "rock", "dragon", "steel", "fairy")
            "fairy" -> listOf("fighting", "bug", "dark")
            else -> emptyList()
        }
    }
    
    /**
     * Get single type immunities
     */
    private fun getSingleTypeImmunities(type: String): List<String> {
        return when (type.lowercase()) {
            "normal" -> listOf("ghost")
            "electric" -> listOf("ground")
            "fighting" -> listOf("ghost")
            "poison" -> listOf("steel")
            "ground" -> listOf("flying")
            "psychic" -> listOf("dark")
            "ghost" -> listOf("normal")
            "dragon" -> listOf("fairy")
            else -> emptyList()
        }
    }
    
    /**
     * Get type effectiveness multiplier for a specific type combination
     */
    private fun getTypeEffectiveness(attackingType: String, defendingType: String): Double {
        return when (attackingType) {
            "normal" -> when (defendingType) {
                "rock", "steel" -> 0.5
                "ghost" -> 0.0
                else -> 1.0
            }
            "fire" -> when (defendingType) {
                "fire", "water", "rock", "dragon" -> 0.5
                "grass", "ice", "bug", "steel" -> 2.0
                else -> 1.0
            }
            "water" -> when (defendingType) {
                "water", "grass", "dragon" -> 0.5
                "fire", "ground", "rock" -> 2.0
                else -> 1.0
            }
            "electric" -> when (defendingType) {
                "electric", "grass", "dragon" -> 0.5
                "water", "flying" -> 2.0
                "ground" -> 0.0
                else -> 1.0
            }
            "grass" -> when (defendingType) {
                "fire", "grass", "poison", "flying", "bug", "dragon", "steel" -> 0.5
                "water", "ground", "rock" -> 2.0
                else -> 1.0
            }
            "ice" -> when (defendingType) {
                "fire", "water", "ice", "steel" -> 0.5
                "grass", "ground", "flying", "dragon" -> 2.0
                else -> 1.0
            }
            "fighting" -> when (defendingType) {
                "normal", "ice", "rock", "dark", "steel" -> 2.0
                "poison", "flying", "psychic", "bug", "fairy" -> 0.5
                "ghost" -> 0.0
                else -> 1.0
            }
            "poison" -> when (defendingType) {
                "grass", "fairy" -> 2.0
                "poison", "ground", "rock", "ghost" -> 0.5
                "steel" -> 0.0
                else -> 1.0
            }
            "ground" -> when (defendingType) {
                "fire", "electric", "poison", "rock", "steel" -> 2.0
                "grass", "bug" -> 0.5
                "flying" -> 0.0
                else -> 1.0
            }
            "flying" -> when (defendingType) {
                "grass", "fighting", "bug" -> 2.0
                "electric", "rock", "steel" -> 0.5
                else -> 1.0
            }
            "psychic" -> when (defendingType) {
                "fighting", "poison" -> 2.0
                "psychic", "steel" -> 0.5
                "dark" -> 0.0
                else -> 1.0
            }
            "bug" -> when (defendingType) {
                "grass", "psychic", "dark" -> 2.0
                "fire", "fighting", "poison", "flying", "ghost", "steel", "fairy" -> 0.5
                else -> 1.0
            }
            "rock" -> when (defendingType) {
                "fire", "ice", "flying", "bug" -> 2.0
                "fighting", "ground", "steel" -> 0.5
                else -> 1.0
            }
            "ghost" -> when (defendingType) {
                "psychic", "ghost" -> 2.0
                "dark" -> 0.5
                "normal" -> 0.0
                else -> 1.0
            }
            "dragon" -> when (defendingType) {
                "dragon" -> 2.0
                "steel" -> 0.5
                "fairy" -> 0.0
                else -> 1.0
            }
            "dark" -> when (defendingType) {
                "psychic", "ghost" -> 2.0
                "fighting", "dark", "fairy" -> 0.5
                else -> 1.0
            }
            "steel" -> when (defendingType) {
                "ice", "rock", "fairy" -> 2.0
                "fire", "water", "electric", "steel" -> 0.5
                else -> 1.0
            }
            "fairy" -> when (defendingType) {
                "fighting", "dragon", "dark" -> 2.0
                "fire", "poison", "steel" -> 0.5
                else -> 1.0
            }
            else -> 1.0
        }
    }
    
    /**
     * Debug function to test type effectiveness calculations
     */
    fun debugTypeEffectiveness(types: List<String>, attackingType: String): String {
        if (types.isEmpty()) return "No types"
        if (types.size == 1) {
            val effectiveness = getTypeEffectiveness(attackingType, types[0])
            return "${types[0]} vs $attackingType: ${effectiveness}x"
        }
        
        val type1 = types[0]
        val type2 = types[1]
        val effectiveness1 = getTypeEffectiveness(attackingType, type1)
        val effectiveness2 = getTypeEffectiveness(attackingType, type2)
        val total = effectiveness1 * effectiveness2
        
        return "$type1/$type2 vs $attackingType: ${effectiveness1}x × ${effectiveness2}x = ${total}x"
    }
}
