package com.kriptogan.pocketsmonsters.ui.viewmodel

import android.app.Application
import androidx.compose.runtime.*
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.kriptogan.pocketsmonsters.data.models.PartyPokemon
import com.kriptogan.pocketsmonsters.data.party.PartyManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PartyViewModel(application: Application) : AndroidViewModel(application) {
    
    private val partyManager = PartyManager(application)
    
    // Party state
    private val _party = MutableStateFlow<List<PartyPokemon>>(emptyList())
    val party: StateFlow<List<PartyPokemon>> = _party.asStateFlow()
    
    // Selected party Pokemon for detail view
    private val _selectedPartyPokemon = MutableStateFlow<PartyPokemon?>(null)
    val selectedPartyPokemon: StateFlow<PartyPokemon?> = _selectedPartyPokemon.asStateFlow()
    
    // Scroll position preservation
    private val _lastViewedPartyPokemonIndex = MutableStateFlow<Int>(-1)
    val lastViewedPartyPokemonIndex: StateFlow<Int> = _lastViewedPartyPokemonIndex.asStateFlow()
    
    // Current screen state
    private val _currentScreen = MutableStateFlow<PartyScreen>(PartyScreen.List)
    val currentScreen: StateFlow<PartyScreen> = _currentScreen.asStateFlow()
    
    init {
        loadParty()
    }
    
    /**
     * Load the current party
     */
    fun loadParty() {
        viewModelScope.launch {
            val currentParty = partyManager.getParty()
            _party.value = currentParty
        }
    }
    
    /**
     * Save the index of the clicked party Pokemon for scroll position restoration
     */
    fun saveClickedPartyPokemonIndex(partyPokemonId: Int) {
        val index = _party.value.indexOfFirst { it.id == partyPokemonId }
        _lastViewedPartyPokemonIndex.value = index
    }
    
    /**
     * Select a party Pokemon for detail view
     */
    fun selectPartyPokemon(partyPokemon: PartyPokemon) {
        _selectedPartyPokemon.value = partyPokemon
        _currentScreen.value = PartyScreen.Detail
    }
    
    /**
     * Navigate back to the party list view
     */
    fun navigateToList() {
        _currentScreen.value = PartyScreen.List
        _selectedPartyPokemon.value = null
    }
    
    /**
     * Remove a Pokemon from the party
     */
    fun removeFromParty(partyPokemonId: Int) {
        viewModelScope.launch {
            partyManager.removeFromParty(partyPokemonId)
            loadParty() // Reload party after removal
        }
    }
    
    /**
     * Clear the selected party Pokemon
     */
    fun clearSelectedPartyPokemon() {
        _selectedPartyPokemon.value = null
    }
}

/**
 * Navigation screens for party
 */
sealed class PartyScreen {
    object List : PartyScreen()
    object Detail : PartyScreen()
}
