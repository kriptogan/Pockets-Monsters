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
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MainScreen(
                        modifier = Modifier.padding(innerPadding)
                    )
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
    val scrollPosition by viewModel.scrollPosition.collectAsState()
    
    when (currentScreen) {
        is PokemonScreen.List -> {
            PokemonListScreen(
                uiState = uiState,
                pokemonList = pokemonList,
                searchQuery = searchQuery,
                scrollPosition = scrollPosition,
                onPokemonClick = { pokemonName ->
                    viewModel.loadPokemon(pokemonName)
                },
                onSearchQueryChange = { query ->
                    viewModel.updateSearchQuery(query)
                },
                onScrollPositionChange = { position ->
                    viewModel.updateScrollPosition(position)
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