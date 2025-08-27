package com.kriptogan.pocketsmonsters.ui.screens

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.kriptogan.pocketsmonsters.data.models.Pokemon
import com.kriptogan.pocketsmonsters.ui.components.PokemonDetailScreen
import com.kriptogan.pocketsmonsters.ui.components.PokemonGridScreen
import com.kriptogan.pocketsmonsters.ui.viewmodel.PokemonUiState

@Composable
fun PokedexScreen(
    uiState: PokemonUiState,
    pokemonList: List<Pokemon>,
    searchQuery: String,
    lastViewedPokemonIndex: Int,
    selectedPokemon: Pokemon?,
    onPokemonClick: (String) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onBackClick: () -> Unit,
    onPartyUpdated: () -> Unit = {}, // Callback for party updates
    modifier: Modifier = Modifier
) {
    if (selectedPokemon != null) {
        // Show Pokémon detail screen
        PokemonDetailScreen(
            pokemon = selectedPokemon,
            onBackClick = onBackClick,
            onPartyUpdated = onPartyUpdated, // Pass the callback
            modifier = modifier
        )
    } else {
        // Show Pokémon grid screen
        PokemonGridScreen(
            uiState = uiState,
            pokemonList = pokemonList,
            searchQuery = searchQuery,
            lastViewedPokemonIndex = lastViewedPokemonIndex,
            onPokemonClick = onPokemonClick,
            onSearchQueryChange = onSearchQueryChange
        )
    }
}
