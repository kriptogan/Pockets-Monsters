package com.kriptogan.pocketsmonsters

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kriptogan.pocketsmonsters.ui.navigation.BottomNavigation
import com.kriptogan.pocketsmonsters.ui.screens.PokedexScreen
import com.kriptogan.pocketsmonsters.ui.screens.UtilitiesScreen
import com.kriptogan.pocketsmonsters.ui.screens.WeaknessesScreen
import com.kriptogan.pocketsmonsters.ui.screens.NaturesScreen
import com.kriptogan.pocketsmonsters.ui.screens.EnergySlotsScreen
import com.kriptogan.pocketsmonsters.ui.screens.MyPartyScreen
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
    var currentTab by remember { mutableStateOf("pokedex") }
    var currentUtilityScreen by remember { mutableStateOf<String?>(null) }
    val currentScreen by viewModel.currentScreen.collectAsState()
    val selectedPokemon by viewModel.selectedPokemon.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val pokemonList by viewModel.filteredPokemonList.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val lastViewedPokemonIndex by viewModel.lastViewedPokemonIndex.collectAsState()
    
    Scaffold(
        modifier = modifier,
        bottomBar = {
            BottomNavigation(
                currentRoute = currentTab,
                onNavigate = { route ->
                    currentTab = route
                    // Reset to list view when switching tabs
                    if (route != "pokedex") {
                        viewModel.navigateToList()
                    }
                    // Reset utility screen when switching tabs
                    currentUtilityScreen = null
                }
            )
        }
    ) { innerPadding ->
        when (currentTab) {
            "pokedex" -> {
                PokedexScreen(
                    uiState = uiState,
                    pokemonList = pokemonList,
                    searchQuery = searchQuery,
                    lastViewedPokemonIndex = lastViewedPokemonIndex,
                    selectedPokemon = selectedPokemon,
                    onPokemonClick = { pokemonName ->
                        viewModel.saveClickedPokemonIndex(pokemonName)
                        viewModel.loadPokemon(pokemonName)
                    },
                    onSearchQueryChange = { query ->
                        viewModel.updateSearchQuery(query)
                    },
                    onBackClick = {
                        viewModel.navigateToList()
                    },
                    modifier = Modifier.padding(innerPadding)
                )
            }
            "utilities" -> {
                when (currentUtilityScreen) {
                    "weaknesses" -> {
                        WeaknessesScreen(
                            onBackClick = { currentUtilityScreen = null },
                            modifier = Modifier.padding(innerPadding)
                        )
                    }
                    "natures" -> {
                        NaturesScreen(
                            onBackClick = { currentUtilityScreen = null },
                            modifier = Modifier.padding(innerPadding)
                        )
                    }
                    "energy_slots" -> {
                        EnergySlotsScreen(
                            onBackClick = { currentUtilityScreen = null },
                            modifier = Modifier.padding(innerPadding)
                        )
                    }
                    else -> {
                        UtilitiesScreen(
                            onWeaknessesClick = { currentUtilityScreen = "weaknesses" },
                            onNaturesClick = { currentUtilityScreen = "natures" },
                            onEnergySlotsClick = { currentUtilityScreen = "energy_slots" },
                            modifier = Modifier.padding(innerPadding)
                        )
                    }
                }
            }
            "my_party" -> {
                MyPartyScreen(
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }
    }
}