package com.kriptogan.pocketsmonsters.data.local

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.kriptogan.pocketsmonsters.data.models.Pokemon
import com.kriptogan.pocketsmonsters.data.models.PokemonListItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LocalStorage(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(
        "pokemon_data",
        Context.MODE_PRIVATE
    )
    private val gson = Gson()
    
    companion object {
        private const val KEY_POKEMON_LIST = "pokemon_list"
        private const val KEY_POKEMON_DETAILS = "pokemon_details"
        private const val KEY_LAST_UPDATE = "last_update"
        private const val KEY_IS_DATA_AVAILABLE = "is_data_available"
        private const val KEY_DOWNLOAD_PROGRESS = "download_progress"
        private const val KEY_TOTAL_POKEMON = "total_pokemon"
    }
    
    suspend fun savePokemonList(pokemonList: List<PokemonListItem>) = withContext(Dispatchers.IO) {
        val json = gson.toJson(pokemonList)
        prefs.edit()
            .putString(KEY_POKEMON_LIST, json)
            .putLong(KEY_LAST_UPDATE, System.currentTimeMillis())
            .putBoolean(KEY_IS_DATA_AVAILABLE, true)
            .apply()
    }
    
    suspend fun getPokemonList(): List<PokemonListItem>? = withContext(Dispatchers.IO) {
        val json = prefs.getString(KEY_POKEMON_LIST, null)
        if (json != null) {
            val type = object : TypeToken<List<PokemonListItem>>() {}.type
            gson.fromJson(json, type)
        } else {
            null
        }
    }
    
    suspend fun savePokemonDetails(pokemonDetails: Map<String, Pokemon>) = withContext(Dispatchers.IO) {
        val json = gson.toJson(pokemonDetails)
        prefs.edit()
            .putString(KEY_POKEMON_DETAILS, json)
            .putLong(KEY_LAST_UPDATE, System.currentTimeMillis())
            .putBoolean(KEY_IS_DATA_AVAILABLE, true)
            .apply()
    }
    
    suspend fun getPokemonDetails(): Map<String, Pokemon>? = withContext(Dispatchers.IO) {
        val json = prefs.getString(KEY_POKEMON_DETAILS, null)
        if (json != null) {
            val type = object : TypeToken<Map<String, Pokemon>>() {}.type
            gson.fromJson(json, type)
        } else {
            null
        }
    }
    
    suspend fun getPokemonDetail(name: String): Pokemon? = withContext(Dispatchers.IO) {
        val details = getPokemonDetails()
        details?.get(name.lowercase())
    }
    
    suspend fun saveDownloadProgress(current: Int, total: Int) = withContext(Dispatchers.IO) {
        prefs.edit()
            .putInt(KEY_DOWNLOAD_PROGRESS, current)
            .putInt(KEY_TOTAL_POKEMON, total)
            .apply()
    }
    
    fun getDownloadProgress(): Pair<Int, Int> {
        val current = prefs.getInt(KEY_DOWNLOAD_PROGRESS, 0)
        val total = prefs.getInt(KEY_TOTAL_POKEMON, 0)
        return Pair(current, total)
    }
    
    fun isDataAvailable(): Boolean {
        return prefs.getBoolean(KEY_IS_DATA_AVAILABLE, false)
    }
    
    fun isDetailedDataAvailable(): Boolean {
        val details = prefs.getString(KEY_POKEMON_DETAILS, null)
        return details != null
    }
    
    fun getLastUpdateTime(): Long {
        return prefs.getLong(KEY_LAST_UPDATE, 0L)
    }
    
    fun clearData() {
        prefs.edit()
            .remove(KEY_POKEMON_LIST)
            .remove(KEY_POKEMON_DETAILS)
            .remove(KEY_LAST_UPDATE)
            .remove(KEY_DOWNLOAD_PROGRESS)
            .remove(KEY_TOTAL_POKEMON)
            .putBoolean(KEY_IS_DATA_AVAILABLE, false)
            .apply()
    }
    
    fun getFormattedLastUpdateTime(): String {
        val timestamp = getLastUpdateTime()
        if (timestamp == 0L) return "Never"
        
        val date = java.util.Date(timestamp)
        val formatter = java.text.SimpleDateFormat("MMM dd, yyyy 'at' HH:mm", java.util.Locale.getDefault())
        return formatter.format(date)
    }
}
