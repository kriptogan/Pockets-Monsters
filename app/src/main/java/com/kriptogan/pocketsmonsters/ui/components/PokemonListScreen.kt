package com.kriptogan.pocketsmonsters.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.kriptogan.pocketsmonsters.data.models.PokemonListItem
import com.kriptogan.pocketsmonsters.ui.viewmodel.PokemonUiState

@Composable
fun PokemonListScreen(
    uiState: PokemonUiState,
    pokemonList: List<PokemonListItem>,
    searchQuery: String,
    scrollPosition: Int,
    onPokemonClick: (String) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onScrollPositionChange: (Int) -> Unit
) {
    val listState = rememberLazyListState()
    
    // Restore scroll position when the screen is displayed
    LaunchedEffect(scrollPosition) {
        if (scrollPosition > 0) {
            listState.animateScrollToItem(scrollPosition)
        }
    }
    
    // Update scroll position when user scrolls
    LaunchedEffect(listState.firstVisibleItemIndex) {
        onScrollPositionChange(listState.firstVisibleItemIndex)
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            label = { Text("Search Pokémon...") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            singleLine = true
        )
        
        when (uiState) {
            is PokemonUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            
            is PokemonUiState.Success -> {
                if (pokemonList.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No Pokémon found",
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    LazyColumn(
                        state = listState,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(pokemonList) { pokemon ->
                            PokemonCard(
                                pokemon = pokemon,
                                onClick = { onPokemonClick(pokemon.name) }
                            )
                        }
                    }
                }
            }
            
            is PokemonUiState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = uiState.message ?: "An error occurred",
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.error
                        )
                        
                        Button(
                            onClick = { /* TODO: Implement retry functionality */ }
                        ) {
                            Text("Retry")
                        }
                    }
                }
            }
        }
    }
}
