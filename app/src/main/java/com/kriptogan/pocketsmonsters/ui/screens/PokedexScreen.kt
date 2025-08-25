package com.kriptogan.pocketsmonsters.ui.screens

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.kriptogan.pocketsmonsters.data.models.Pokemon
import com.kriptogan.pocketsmonsters.data.models.PokemonListItem
import com.kriptogan.pocketsmonsters.ui.components.PokemonDetailScreen
import com.kriptogan.pocketsmonsters.ui.components.PokemonListScreen
import com.kriptogan.pocketsmonsters.ui.viewmodel.PokemonUiState

@Composable
fun PokedexScreen(
    uiState: PokemonUiState,
    pokemonList: List<PokemonListItem>,
    searchQuery: String,
    lastViewedPokemonIndex: Int,
    selectedPokemon: Pokemon?,
    isLocalDataAvailable: Boolean,
    lastUpdateTime: String,
    isDetailedDataAvailable: Boolean,
    downloadProgress: Pair<Int, Int>,
    isDownloading: Boolean,
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
            downloadProgress = downloadProgress,
            isDownloading = isDownloading,
            onPokemonClick = onPokemonClick,
            onSearchQueryChange = onSearchQueryChange,
            onRefreshClick = onRefreshClick
        )
    }
}
