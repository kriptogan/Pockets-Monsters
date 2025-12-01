package com.kriptogan.pocketsmonsters

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import kotlinx.coroutines.launch

import androidx.lifecycle.viewmodel.compose.viewModel
import com.kriptogan.pocketsmonsters.ui.components.PokedexContainer
import com.kriptogan.pocketsmonsters.ui.screens.PokedexScreen
import com.kriptogan.pocketsmonsters.ui.theme.PocketsMonstersTheme
import com.kriptogan.pocketsmonsters.ui.viewmodel.PokemonScreen
import com.kriptogan.pocketsmonsters.ui.viewmodel.PokemonViewModel
import android.view.View

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Hide status bar completely
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        
        // Hide bottom navigation bar (system buttons)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )
        
        // Additional flags for immersive mode
        window.decorView.systemUiVisibility = (
            View.SYSTEM_UI_FLAG_FULLSCREEN or
            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        )
        
        // Modern approach for Android 11+ (API 30+) - simplified
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            window.insetsController?.let { controller ->
                controller.hide(android.view.WindowInsets.Type.statusBars() or android.view.WindowInsets.Type.navigationBars())
                controller.systemBarsBehavior = android.view.WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        }
        
        enableEdgeToEdge()
        setContent {
            PocketsMonstersTheme {
                // Force LTR layout even in RTL systems like Hebrew
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    MainApp()
                }
            }
        }
    }
}

@Composable
fun MainApp() {
    var currentTab by remember { mutableStateOf("pokedex") }
    
    PokedexContainer(
        modifier = Modifier.fillMaxSize(),
        currentRoute = currentTab,
        onNavigate = { route -> 
            currentTab = route
        },
        content = {
            MainScreen(
                currentTab = currentTab,
                onTabChange = { route -> currentTab = route }
            )
        }
    )
}

@Composable
fun MainScreen(
    currentTab: String,
    onTabChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PokemonViewModel = viewModel()
) {
    var currentUtilityScreen by remember { mutableStateOf<String?>(null) }
    val currentScreen by viewModel.currentScreen.collectAsState()
    val selectedPokemon by viewModel.selectedPokemon.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val pokemonList by viewModel.filteredPokemonList.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val lastViewedPokemonIndex by viewModel.lastViewedPokemonIndex.collectAsState()
    val ownedPokemonIds by viewModel.ownedPokemonIds.collectAsState()
    val tcgData by viewModel.tcgData.collectAsState()
    val isLoadingTCG by viewModel.isLoadingTCG.collectAsState()
    
    // Snackbar state for toast messages
    val snackbarHostState = remember { SnackbarHostState() }
    var lastOwnedCount by remember { mutableStateOf(ownedPokemonIds.size) }
    
    // Show snackbar when collection changes
    LaunchedEffect(ownedPokemonIds.size) {
        if (ownedPokemonIds.size != lastOwnedCount) {
            val message = if (ownedPokemonIds.size > lastOwnedCount) {
                "Added to collection!"
            } else {
                "Removed from collection"
            }
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Short
            )
            lastOwnedCount = ownedPokemonIds.size
        }
    }
    
    // Content area with SnackbarHost
    Scaffold(
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                snackbar = { snackbarData ->
                    Snackbar(
                        snackbarData = snackbarData,
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
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
                    onPartyUpdated = {
                        // No longer needed - kept for compatibility
                    },
                    onPokemonLongPress = { pokemonId, pokemonName ->
                        viewModel.togglePokemonOwnership(pokemonId, pokemonName)
                    },
                    ownedPokemonIds = ownedPokemonIds,
                    tcgData = tcgData,
                    isLoadingTCG = isLoadingTCG,
                    modifier = Modifier
                )
            }
            // Other tabs removed - only Pokédex is needed for TCG collection tracking
            else -> {
                // Default to Pokédex if unknown route
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
                    onPartyUpdated = {
                        // No longer needed - kept for compatibility
                    },
                    onPokemonLongPress = { pokemonId, pokemonName ->
                        viewModel.togglePokemonOwnership(pokemonId, pokemonName)
                    },
                    ownedPokemonIds = ownedPokemonIds,
                    tcgData = tcgData,
                    isLoadingTCG = isLoadingTCG,
                    modifier = Modifier
                )
            }
        }
        }
    }
}