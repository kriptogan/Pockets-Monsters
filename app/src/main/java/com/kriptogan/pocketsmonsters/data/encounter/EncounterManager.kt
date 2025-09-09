package com.kriptogan.pocketsmonsters.data.encounter

import android.content.Context
import android.content.SharedPreferences
import com.kriptogan.pocketsmonsters.data.models.EncounterCreature
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

class EncounterManager(private val context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("encounter", Context.MODE_PRIVATE)
    private val encounterKey = "encounter_creatures"
    
    private val _creatures = MutableStateFlow<List<EncounterCreature>>(emptyList())
    val creatures: StateFlow<List<EncounterCreature>> = _creatures.asStateFlow()
    
    init {
        loadEncounter()
    }
    
    fun addCreature() {
        val newCreature = EncounterCreature(id = System.currentTimeMillis().toInt())
        val currentList = _creatures.value.toMutableList()
        currentList.add(newCreature)
        _creatures.value = currentList
        saveEncounter()
    }
    
    fun addCreature(creature: EncounterCreature) {
        val currentList = _creatures.value.toMutableList()
        currentList.add(creature)
        _creatures.value = currentList
        saveEncounter()
    }
    
    fun updateCreature(updatedCreature: EncounterCreature) {
        val currentList = _creatures.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == updatedCreature.id }
        if (index != -1) {
            currentList[index] = updatedCreature
            _creatures.value = currentList
            saveEncounter()
        }
    }
    
    fun removeCreature(creatureId: Int) {
        val currentList = _creatures.value.toMutableList()
        currentList.removeAll { it.id == creatureId }
        _creatures.value = currentList
        saveEncounter()
    }
    
    private fun loadEncounter() {
        val jsonString = prefs.getString(encounterKey, null)
        if (jsonString != null) {
            try {
                val jsonArray = JSONArray(jsonString)
                val creatures = mutableListOf<EncounterCreature>()
                
                for (i in 0 until jsonArray.length()) {
                    val jsonObject = jsonArray.getJSONObject(i)
                    val creature = EncounterCreature(
                        id = jsonObject.getInt("id"),
                        pokemonId = jsonObject.getInt("pokemonId"),
                        name = jsonObject.getString("name"),
                        initiative = jsonObject.getString("initiative"),
                        ac = jsonObject.getString("ac"),
                        maxHp = jsonObject.getString("maxHp"),
                        currentHp = jsonObject.getString("currentHp")
                    )
                    creatures.add(creature)
                }
                
                _creatures.value = creatures
            } catch (e: Exception) {
                // If loading fails, start with empty list
                _creatures.value = emptyList()
            }
        }
    }
    
    private fun saveEncounter() {
        val jsonArray = JSONArray()
        _creatures.value.forEach { creature ->
            val jsonObject = JSONObject().apply {
                put("id", creature.id)
                put("pokemonId", creature.pokemonId)
                put("name", creature.name)
                put("initiative", creature.initiative)
                put("ac", creature.ac)
                put("maxHp", creature.maxHp)
                put("currentHp", creature.currentHp)
            }
            jsonArray.put(jsonObject)
        }
        
        prefs.edit()
            .putString(encounterKey, jsonArray.toString())
            .apply()
    }
}
