package com.kriptogan.pocketsmonsters.ui.screens

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.kriptogan.pocketsmonsters.data.models.Pokemon
import com.kriptogan.pocketsmonsters.ui.components.PokemonDetailScreen
import com.kriptogan.pocketsmonsters.ui.components.PokemonListScreen
import com.kriptogan.pocketsmonsters.ui.viewmodel.PokemonUiState

@Composable
fun PokedexScreen(
    uiState: PokemonUiState,
    pokemonList: List<Pokemon>,
    searchQuery: String,
    lastViewedPokemonIndex: Int,
    selectedPokemon: Pokemon?,
    isLocalDataAvailable: Boolean,
    lastUpdateTime: String,
    isDetailedDataAvailable: Boolean,
    onPokemonClick: (String) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onRefreshClick: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (selectedPokemon != null) {
        // Show Pokémon detail screen
        PokemonDetailScreen(
            pokemon = selectedPokemon,
            onBackClick = onBackClick,
            modifier = modifier
        )
    } else {
        // Show Pokémon list screen
        PokemonListScreen(
            uiState = uiState,
            pokemonList = pokemonList,
            searchQuery = searchQuery,
            lastViewedPokemonIndex = lastViewedPokemonIndex,
            isLocalDataAvailable = isLocalDataAvailable,
            lastUpdateTime = lastUpdateTime,
            isDetailedDataAvailable = isDetailedDataAvailable,
            onPokemonClick = onPokemonClick,
            onSearchQueryChange = onSearchQueryChange,
            onRefreshClick = onRefreshClick
        )
    }
}
