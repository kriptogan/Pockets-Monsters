package com.kriptogan.pocketsmonsters.ui.viewmodel

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kriptogan.pocketsmonsters.data.models.Pokemon
import com.kriptogan.pocketsmonsters.data.models.PokemonListItem
import com.kriptogan.pocketsmonsters.data.repository.PokemonRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PokemonViewModel : ViewModel() {
    
    private val repository = PokemonRepository()
    
    // UI State
    private val _uiState = MutableStateFlow<PokemonUiState>(PokemonUiState.Loading)
    val uiState: StateFlow<PokemonUiState> = _uiState.asStateFlow()
    
    private val _pokemonList = MutableStateFlow<List<PokemonListItem>>(emptyList())
    val pokemonList: StateFlow<List<PokemonListItem>> = _pokemonList.asStateFlow()
    
    private val _filteredPokemonList = MutableStateFlow<List<PokemonListItem>>(emptyList())
    val filteredPokemonList: StateFlow<List<PokemonListItem>> = _filteredPokemonList.asStateFlow()
    
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
    
    init {
        loadPokemonList()
    }
    
    /**
     * Load the initial list of Pokémon
     */
    fun loadPokemonList() {
        viewModelScope.launch {
            _isLoading.value = true
            _uiState.value = PokemonUiState.Loading
            
            try {
                val result = repository.getPokemonList()
                result.fold(
                    onSuccess = { response ->
                        _pokemonList.value = response.results
                        _filteredPokemonList.value = response.results
                        _uiState.value = PokemonUiState.Success(response.results)
                        _errorMessage.value = null
                    },
                    onFailure = { exception ->
                        _uiState.value = PokemonUiState.Error(exception.message ?: "Unknown error")
                        _errorMessage.value = exception.message ?: "Failed to load Pokémon list"
                    }
                )
            } catch (e: Exception) {
                _uiState.value = PokemonUiState.Error(e.message ?: "Unknown error")
                _errorMessage.value = e.message ?: "Failed to load Pokémon list"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Update search query and filter results
     */
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        applyFilters()
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
        
        val filtered = _pokemonList.value.filter { pokemon ->
            val matchesSearch = pokemon.name.contains(query, ignoreCase = true)
            val matchesType = type == null || pokemon.name.contains(type, ignoreCase = true)
            
            matchesSearch && matchesType
        }
        
        _filteredPokemonList.value = filtered
        
        // Update UI state
        if (filtered.isEmpty() && query.isNotEmpty()) {
            _uiState.value = PokemonUiState.Error("No Pokémon found matching '$query'")
        } else if (filtered.isEmpty()) {
            _uiState.value = PokemonUiState.Error("No Pokémon found")
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
     * Load detailed information about a specific Pokémon
     */
    fun loadPokemon(name: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            
            try {
                val result = repository.getPokemon(name)
                result.fold(
                    onSuccess = { pokemon ->
                        _selectedPokemon.value = pokemon
                        _currentScreen.value = PokemonScreen.Detail
                        _errorMessage.value = null
                    },
                    onFailure = { exception ->
                        _errorMessage.value = exception.message ?: "Failed to load Pokémon"
                    }
                )
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Failed to load Pokémon"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Navigate back to the list
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
     * Refresh the Pokémon list
     */
    fun refreshPokemonList() {
        loadPokemonList()
    }
}

/**
 * UI State sealed class for different states
 */
sealed class PokemonUiState {
    object Loading : PokemonUiState()
    data class Success(val pokemonList: List<PokemonListItem>) : PokemonUiState()
    data class Error(val message: String?) : PokemonUiState()
}

/**
 * Navigation screens
 */
sealed class PokemonScreen {
    object List : PokemonScreen()
    object Detail : PokemonScreen()
}
