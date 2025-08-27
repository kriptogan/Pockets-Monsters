package com.kriptogan.pocketsmonsters.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.kriptogan.pocketsmonsters.data.models.Pokemon
import com.kriptogan.pocketsmonsters.ui.viewmodel.PokemonUiState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border

@Composable
fun PokemonGridScreen(
    uiState: PokemonUiState,
    pokemonList: List<Pokemon>,
    searchQuery: String,
    lastViewedPokemonIndex: Int,
    onPokemonClick: (String) -> Unit,
    onSearchQueryChange: (String) -> Unit
) {
    val gridState = rememberLazyGridState()
    
    // Restore scroll position to the last viewed Pokémon index
    LaunchedEffect(lastViewedPokemonIndex) {
        if (lastViewedPokemonIndex >= 0 && lastViewedPokemonIndex < pokemonList.size) {
            gridState.animateScrollToItem(lastViewedPokemonIndex)
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Search Bar - positioned at top of content area
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            label = { Text("Search Pokémon...") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFFD32F2F), // Pokedex red
                unfocusedBorderColor = Color(0xFF666666), // Dark gray
                focusedLabelColor = Color(0xFFD32F2F), // Pokedex red
                unfocusedLabelColor = Color(0xFF666666) // Dark gray
            )
        )
        
        when (uiState) {
            is PokemonUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = Color(0xFFD32F2F) // Pokedex red
                    )
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
                            textAlign = TextAlign.Center,
                            color = Color(0xFF1A1A1A) // Dark text for better readability on gradient
                        )
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(4), // 4 columns like you requested
                        state = gridState,
                        horizontalArrangement = Arrangement.spacedBy(8.dp), // Reduced spacing for 4 columns
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        items(pokemonList) { pokemon ->
                            PokemonGridCard(
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
                            onClick = { /* TODO: Implement retry functionality */ },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFD32F2F) // Pokedex red
                            )
                        ) {
                            Text("Retry")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PokemonGridCard(
    pokemon: Pokemon,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(0.8f) // Vertical rectangle like in your image
            .clip(RoundedCornerShape(16.dp))
            .background(Color.Transparent) // Fully transparent background
            .border(
                width = 2.dp,
                color = Color.White, // White border
                shape = RoundedCornerShape(16.dp)
            )
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize(), // No padding
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center // Center content vertically
        ) {
            // Pokémon front image from front_images directory
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data("file:///android_asset/front_images/${pokemon.name}.png")
                    .crossfade(true)
                    .build(),
                contentDescription = "Front image of ${pokemon.name}",
                modifier = Modifier
                    .size(64.dp) // 80% of 80dp = 64dp
                    .clip(RoundedCornerShape(8.dp)), // No background
                contentScale = ContentScale.Fit
            )
            
            Spacer(modifier = Modifier.height(8.dp)) // Small spacing between image and text
            
            // Pokémon ID number (4 digits)
            Text(
                text = String.format("%04d", pokemon.id), // Format as 4 digits (e.g., "0001", "0025")
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A1A), // Dark text for readability
                textAlign = TextAlign.Center,
                fontSize = 16.sp
            )
        }
    }
}
