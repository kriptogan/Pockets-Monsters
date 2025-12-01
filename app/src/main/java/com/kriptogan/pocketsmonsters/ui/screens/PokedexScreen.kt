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
    onPokemonLongPress: ((Int, String) -> Unit)? = null, // Callback for long press
    ownedPokemonIds: Set<Int> = emptySet(), // Set of owned Pokémon IDs
    tcgData: com.kriptogan.pocketsmonsters.data.tcg.PokemonTCGData? = null, // TCG data for selected Pokémon
    isLoadingTCG: Boolean = false, // Loading state for TCG data
    modifier: Modifier = Modifier
) {
    if (selectedPokemon != null) {
        // Show Pokémon detail screen
        PokemonDetailScreen(
            pokemon = selectedPokemon,
            onBackClick = onBackClick,
            onPartyUpdated = onPartyUpdated, // Pass the callback
            tcgData = tcgData,
            isLoadingTCG = isLoadingTCG,
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
            onSearchQueryChange = onSearchQueryChange,
            onPokemonLongPress = onPokemonLongPress,
            ownedPokemonIds = ownedPokemonIds
        )
    }
}
