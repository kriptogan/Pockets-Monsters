package com.kriptogan.pocketsmonsters.ui.viewmodel

import android.app.Application
import androidx.compose.runtime.*
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.kriptogan.pocketsmonsters.data.models.Pokemon
import com.kriptogan.pocketsmonsters.data.network.NetworkModule
import com.kriptogan.pocketsmonsters.data.party.PartyManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PokemonViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository = NetworkModule.createPokemonRepository(application)
    
    // UI State
    private val _uiState = MutableStateFlow<PokemonUiState>(PokemonUiState.Loading)
    val uiState: StateFlow<PokemonUiState> = _uiState.asStateFlow()
    
    private val _pokemonList = MutableStateFlow<List<Pokemon>>(emptyList())
    val pokemonList: StateFlow<List<Pokemon>> = _pokemonList.asStateFlow()
    
    private val _filteredPokemonList = MutableStateFlow<List<Pokemon>>(emptyList())
    val filteredPokemonList: StateFlow<List<Pokemon>> = _filteredPokemonList.asStateFlow()
    
    private val _selectedPokemon = MutableStateFlow<Pokemon?>(null)
    val selectedPokemon: StateFlow<Pokemon?> = _selectedPokemon.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    
    // Navigation state
    private val _currentScreen = MutableStateFlow<PokemonScreen>(PokemonScreen.List)
    val currentScreen: StateFlow<PokemonScreen> = _currentScreen.asStateFlow()
    
    // Search and filter state
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    private val _selectedType = MutableStateFlow<String?>(null)
    val selectedType: StateFlow<String?> = _selectedType.asStateFlow()
    
    // Scroll position preservation
    private val _lastViewedPokemonIndex = MutableStateFlow<Int>(-1)
    val lastViewedPokemonIndex: StateFlow<Int> = _lastViewedPokemonIndex.asStateFlow()
    
    // Party state
    private val _partySize = MutableStateFlow(0)
    val partySize: StateFlow<Int> = _partySize.asStateFlow()
    
    private val _partyPokemonIds = MutableStateFlow<Set<Int>>(emptySet())
    val partyPokemonIds: StateFlow<Set<Int>> = _partyPokemonIds.asStateFlow()
    
    // Offline data status
    private val _isOfflineDataAvailable = MutableStateFlow(false)
    val isOfflineDataAvailable: StateFlow<Boolean> = _isOfflineDataAvailable.asStateFlow()
    
    init {
        loadPokemonList()
        refreshPartyState() // Initialize party state
    }
    
    /**
     * Load the initial list of Pokémon from offline data
     */
    fun loadPokemonList() {
        viewModelScope.launch {
            _isLoading.value = true
            _uiState.value = PokemonUiState.Loading
            
            try {
                // Check if offline data is available
                println("🔍 DEBUG: Checking if offline data is available...")
                val isAvailable = repository.isOfflineDataAvailable()
                println("🔍 DEBUG: Offline data available: $isAvailable")
                
                if (isAvailable) {
                    _isOfflineDataAvailable.value = true
                    
                    // Load all Pokémon data from offline assets
                    println("�� DEBUG: Loading all Pokémon data from repository...")
                    val allPokemon = repository.getAllPokemonData()
                    println("🔍 DEBUG: Repository returned ${allPokemon.size} Pokémon")
                    
                    if (allPokemon.isNotEmpty()) {
                        val firstPokemon = allPokemon.first()
                        println("🔍 DEBUG: First Pokémon from repository: ${firstPokemon.name}, Sprite: ${firstPokemon.spritePath}")
                    }
                    
                    _pokemonList.value = allPokemon
                    _filteredPokemonList.value = allPokemon
                    _uiState.value = PokemonUiState.Success(allPokemon)
                    _errorMessage.value = null
                    
                    println("✅ Loaded ${allPokemon.size} Pokémon from offline data")
                } else {
                    // Fallback to API if no offline data
                    _isOfflineDataAvailable.value = false
                    _uiState.value = PokemonUiState.Error("No offline data available")
                    _errorMessage.value = "No offline data available"
                    println("❌ No offline data available")
                }
            } catch (e: Exception) {
                _uiState.value = PokemonUiState.Error(e.message ?: "Unknown error")
                _errorMessage.value = e.message ?: "Failed to load Pokémon list"
                println("❌ Error loading Pokémon: ${e.message}")
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Load a specific Pokémon by name
     */
    fun loadPokemon(name: String) {
        viewModelScope.launch {
            try {
                val result = repository.getPokemon(name)
                result.fold(
                    onSuccess = { pokemon ->
                        _selectedPokemon.value = pokemon
                        _currentScreen.value = PokemonScreen.Detail
                    },
                    onFailure = { exception ->
                        _errorMessage.value = "Pokémon not found: $name"
                    }
                )
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load Pokémon: ${e.message}"
            }
        }
    }
    
    /**
     * Update search query and filter Pokémon list
     */
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        filterPokemonList()
    }
    
    /**
     * Filter Pokémon list based on search query
     */
    private fun filterPokemonList() {
        val query = _searchQuery.value.lowercase()
        val allPokemon = _pokemonList.value
        
        if (query.isEmpty()) {
            _filteredPokemonList.value = allPokemon
        } else {
            val filtered = allPokemon.filter { pokemon ->
                pokemon.name.lowercase().contains(query)
            }
            _filteredPokemonList.value = filtered
        }
    }
    
    /**
     * Update selected type filter
     */
    fun updateTypeFilter(type: String?) {
        _selectedType.value = type
        applyFilters()
    }
    
    /**
     * Apply search and type filters
     */
    private fun applyFilters() {
        val query = _searchQuery.value.lowercase()
        val type = _selectedType.value
        
        if (type == null && query.isEmpty()) {
            // No filters applied, show all Pokémon
            _filteredPokemonList.value = _pokemonList.value
            _uiState.value = PokemonUiState.Success(_pokemonList.value)
            return
        }
        
        // Start with all Pokémon
        var filtered = _pokemonList.value
        
        // Apply search filter if query exists
        if (query.isNotEmpty()) {
            filtered = filtered.filter { pokemon ->
                pokemon.name.contains(query, ignoreCase = true)
            }
        }
        
        // Apply type filter if type is selected
        if (type != null) {
            // For now, we'll filter by Pokémon that commonly have this type
            // This is a simplified approach - in a full implementation, we'd need to
            // fetch each Pokémon's details to check their actual types
            filtered = filtered.filter { pokemon ->
                // Common type associations (this is a simplified approach)
                when (type.lowercase()) {
                    "fire" -> pokemon.name in listOf("charmander", "charmeleon", "charizard", "vulpix", "ninetales", "growlithe", "arcanine", "ponyta", "rapidash", "magmar", "flareon", "moltres")
                    "water" -> pokemon.name in listOf("squirtle", "wartortle", "blastoise", "psyduck", "golduck", "poliwag", "poliwhirl", "poliwrath", "tentacool", "tentacruel", "slowpoke", "slowbro", "seel", "dewgong", "shellder", "cloyster", "krabby", "kingler", "horsea", "seadra", "goldeen", "seaking", "staryu", "starmie", "magikarp", "gyarados", "lapras", "vaporeon", "omanyte", "omastar", "kabuto", "kabutops", "dratini", "dragonair", "dragonite")
                    "grass" -> pokemon.name in listOf("bulbasaur", "ivysaur", "venusaur", "oddish", "gloom", "vileplume", "paras", "parasect", "bellsprout", "weepinbell", "victreebel", "exeggcute", "exeggutor", "tangela", "chikorita", "bayleef", "meganium", "chikorita", "bayleef", "meganium")
                    "electric" -> pokemon.name in listOf("pikachu", "raichu", "magnemite", "magneton", "voltorb", "electrode", "electabuzz", "jolteon", "chinchou", "lanturn", "mareep", "flaaffy", "ampharos")
                    "psychic" -> pokemon.name in listOf("abra", "kadabra", "alakazam", "slowpoke", "slowbro", "drowzee", "hypno", "exeggcute", "exeggutor", "starmie", "mr. mime", "jynx", "mewtwo", "mew", "natu", "xatu", "espeon", "wobbuffet", "girafarig", "smoochum")
                    "ice" -> pokemon.name in listOf("jynx", "lapras", "articuno", "sneasel", "swinub", "piloswine", "delibird", "smoochum", "sneasel", "swinub", "piloswine", "delibird")
                    "dragon" -> pokemon.name in listOf("dratini", "dragonair", "dragonite", "kingdra")
                    else -> true // If type not recognized, show all
                }
            }
        }
        
        _filteredPokemonList.value = filtered
        
        // Update UI state
        if (filtered.isEmpty()) {
            val filterText = when {
                type != null && query.isNotEmpty() -> "No Pokémon found matching '$query' and type '$type'"
                type != null -> "No Pokémon found of type '$type'"
                query.isNotEmpty() -> "No Pokémon found matching '$query'"
                else -> "No Pokémon found"
            }
            _uiState.value = PokemonUiState.Error(filterText)
        } else {
            _uiState.value = PokemonUiState.Success(filtered)
        }
    }
    
    /**
     * Clear all filters
     */
    fun clearFilters() {
        _searchQuery.value = ""
        _selectedType.value = null
        _filteredPokemonList.value = _pokemonList.value
        _uiState.value = PokemonUiState.Success(_pokemonList.value)
    }
    
    /**
     * Save the index of the clicked Pokémon for scroll position restoration
     */
    fun saveClickedPokemonIndex(pokemonName: String) {
        val index = _pokemonList.value.indexOfFirst { it.name == pokemonName }
        _lastViewedPokemonIndex.value = index
    }
    
    /**
     * Navigate back to the list view
     */
    fun navigateToList() {
        _currentScreen.value = PokemonScreen.List
        _selectedPokemon.value = null
    }
    
    /**
     * Clear the selected Pokémon
     */
    fun clearSelectedPokemon() {
        _selectedPokemon.value = null
    }
    
    /**
     * Clear error message
     */
    fun clearError() {
        _errorMessage.value = null
    }
    
    /**
     * Check if local data is available
     */
    fun isLocalDataAvailable(): Boolean {
        return _isOfflineDataAvailable.value
    }
    
    /**
     * Check if detailed data is available
     */
    fun isDetailedDataAvailable(): Boolean {
        return _isOfflineDataAvailable.value
    }
    
    /**
     * Get last update time (for offline data, this is always current)
     */
    fun getLastUpdateTime(): String {
        return "Now (Offline)"
    }
    
    /**
     * Refresh party state from PartyManager
     */
    fun refreshPartyState() {
        val partyManager = PartyManager(getApplication())
        val party = partyManager.getParty()
        _partySize.value = party.size
        _partyPokemonIds.value = party.map { it.id }.toSet()
    }
    
    /**
     * Add Pokemon to party
     */
    fun addPokemonToParty(pokemon: Pokemon) {
        val partyManager = PartyManager(getApplication())
        val result = partyManager.addToParty(pokemon)
        
        if (result.isSuccess) {
            // Refresh party state to update UI
            refreshPartyState()
        }
    }
    
    /**
     * Check if Pokemon is in party
     */
    fun isPokemonInParty(pokemonId: Int): Boolean {
        return _partyPokemonIds.value.contains(pokemonId)
    }
    
    /**
     * Get current party size
     */
    fun getCurrentPartySize(): Int {
        return _partySize.value
    }
}

/**
 * UI State sealed class for different states
 */
sealed class PokemonUiState {
    object Loading : PokemonUiState()
    data class Success(val pokemonList: List<Pokemon>) : PokemonUiState()
    data class Error(val message: String?) : PokemonUiState()
}

/**
 * Navigation screens
 */
sealed class PokemonScreen {
    object List : PokemonScreen()
    object Detail : PokemonScreen()
}
