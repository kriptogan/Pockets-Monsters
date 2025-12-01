package com.kriptogan.pocketsmonsters.data.collection

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class CollectionManager(context: Context) {
    
    companion object {
        private const val TAG = "CollectionManager"
        private const val PREFS_NAME = "collection_prefs"
        private const val KEY_COLLECTION = "collection_status"
    }
    
    private val context: Context = context
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()
    
    /**
     * Get collection status for a specific Pokémon
     * @param pokemonId The ID of the Pokémon
     * @return CollectionStatus if found, null if not in collection
     */
    fun getCollectionStatus(pokemonId: Int): CollectionStatus? {
        val collection = getAllCollectionStatus()
        return collection.find { it.pokemonId == pokemonId }
    }
    
    /**
     * Check if a Pokémon is owned (has at least 1 card)
     * @param pokemonId The ID of the Pokémon
     * @return true if owned, false otherwise
     */
    fun isPokemonOwned(pokemonId: Int): Boolean {
        val status = getCollectionStatus(pokemonId)
        return status?.isOwned ?: false
    }
    
    /**
     * Toggle ownership status for a Pokémon
     * @param pokemonId The ID of the Pokémon
     * @param pokemonName The name of the Pokémon
     * @return The new ownership status (true if now owned, false if now unowned)
     */
    fun toggleOwnership(pokemonId: Int, pokemonName: String): Boolean {
        val collection = getAllCollectionStatus().toMutableList()
        val existingIndex = collection.indexOfFirst { it.pokemonId == pokemonId }
        
        val newStatus = if (existingIndex >= 0) {
            // Toggle existing status
            val existing = collection[existingIndex]
            val newOwnedStatus = !existing.isOwned
            collection[existingIndex] = existing.copy(
                isOwned = newOwnedStatus,
                ownedSince = if (newOwnedStatus) System.currentTimeMillis() else null
            )
            newOwnedStatus
        } else {
            // Add new entry as owned
            collection.add(
                CollectionStatus(
                    pokemonId = pokemonId,
                    pokemonName = pokemonName,
                    isOwned = true,
                    ownedSince = System.currentTimeMillis()
                )
            )
            true
        }
        
        saveCollection(collection)
        Log.d(TAG, "Toggled ownership for $pokemonName (ID: $pokemonId) to: $newStatus")
        return newStatus
    }
    
    /**
     * Get all owned Pokémon IDs
     * @return Set of Pokémon IDs that are owned
     */
    fun getAllOwnedPokemon(): Set<Int> {
        return getAllCollectionStatus()
            .filter { it.isOwned }
            .map { it.pokemonId }
            .toSet()
    }
    
    /**
     * Get all collection status entries
     * @return List of all CollectionStatus entries
     */
    private fun getAllCollectionStatus(): List<CollectionStatus> {
        val collectionJson = sharedPreferences.getString(KEY_COLLECTION, "[]")
        return try {
            val type = object : TypeToken<List<CollectionStatus>>() {}.type
            gson.fromJson<List<CollectionStatus>>(collectionJson, type) ?: emptyList()
        } catch (e: Exception) {
            Log.e(TAG, "Error loading collection status: ${e.message}")
            emptyList()
        }
    }
    
    /**
     * Save collection status to SharedPreferences
     * @param collection List of CollectionStatus to save
     */
    private fun saveCollection(collection: List<CollectionStatus>) {
        try {
            val collectionJson = gson.toJson(collection)
            sharedPreferences.edit()
                .putString(KEY_COLLECTION, collectionJson)
                .apply()
            Log.d(TAG, "Saved ${collection.size} collection entries")
        } catch (e: Exception) {
            Log.e(TAG, "Error saving collection status: ${e.message}")
        }
    }
    
    /**
     * Clear all collection data (for testing/reset purposes)
     */
    fun clearAll() {
        sharedPreferences.edit()
            .remove(KEY_COLLECTION)
            .apply()
        Log.d(TAG, "Cleared all collection data")
    }
}

