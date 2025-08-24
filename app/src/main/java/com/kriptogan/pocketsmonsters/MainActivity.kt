package com.kriptogan.pocketsmonsters

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kriptogan.pocketsmonsters.ui.components.PokemonDetailScreen
import com.kriptogan.pocketsmonsters.ui.components.PokemonListScreen
import com.kriptogan.pocketsmonsters.ui.theme.PocketsMonstersTheme
import com.kriptogan.pocketsmonsters.ui.viewmodel.PokemonScreen
import com.kriptogan.pocketsmonsters.ui.viewmodel.PokemonViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PocketsMonstersTheme {
                // Force LTR layout even in RTL systems like Hebrew
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                        MainScreen(
                            modifier = Modifier.padding(innerPadding)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    viewModel: PokemonViewModel = viewModel()
) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val selectedPokemon by viewModel.selectedPokemon.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val pokemonList by viewModel.filteredPokemonList.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val lastViewedPokemonIndex by viewModel.lastViewedPokemonIndex.collectAsState()
    
    when (currentScreen) {
        is PokemonScreen.List -> {
            PokemonListScreen(
                uiState = uiState,
                pokemonList = pokemonList,
                searchQuery = searchQuery,
                lastViewedPokemonIndex = lastViewedPokemonIndex,
                onPokemonClick = { pokemonName ->
                    viewModel.saveClickedPokemonIndex(pokemonName)
                    viewModel.loadPokemon(pokemonName)
                },
                onSearchQueryChange = { query ->
                    viewModel.updateSearchQuery(query)
                }
            )
        }
        is PokemonScreen.Detail -> {
            PokemonDetailScreen(
                pokemon = selectedPokemon,
                onBackClick = {
                    viewModel.navigateToList()
                }
            )
        }
    }
}