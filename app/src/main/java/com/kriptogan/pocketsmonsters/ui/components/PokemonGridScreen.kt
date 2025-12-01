package com.kriptogan.pocketsmonsters.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.GridItemSpan
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
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.ui.draw.scale
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

/**
 * Get the generation number for a Pokémon ID
 */
fun getPokemonGeneration(pokemonId: Int): Int {
    return when {
        pokemonId <= 151 -> 1
        pokemonId <= 251 -> 2
        pokemonId <= 386 -> 3
        pokemonId <= 493 -> 4
        pokemonId <= 649 -> 5
        pokemonId <= 721 -> 6
        pokemonId <= 809 -> 7
        pokemonId <= 905 -> 8
        else -> 9
    }
}

/**
 * Get the start ID for a generation
 */
fun getGenerationStartId(generation: Int): Int {
    return when (generation) {
        1 -> 1
        2 -> 152
        3 -> 252
        4 -> 387
        5 -> 494
        6 -> 650
        7 -> 722
        8 -> 810
        9 -> 906
        else -> 1
    }
}

@Composable
fun PokemonGridScreen(
    uiState: PokemonUiState,
    pokemonList: List<Pokemon>,
    searchQuery: String,
    lastViewedPokemonIndex: Int,
    onPokemonClick: (String) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onPokemonLongPress: ((Int, String) -> Unit)? = null,
    ownedPokemonIds: Set<Int> = emptySet()
) {
    val gridState = rememberLazyGridState()
    
    // Group Pokémon by generation
    val pokemonByGeneration = remember(pokemonList) {
        pokemonList.groupBy { getPokemonGeneration(it.id) }
    }
    
    // Find the grid index (accounting for headers) of each generation header
    val generationIndices = remember(pokemonList, searchQuery) {
        if (searchQuery.isNotEmpty()) {
            // When searching, no headers, return empty map (generation selector hidden)
            emptyMap<Int, Int>()
        } else {
            // When not searching, calculate indices accounting for headers
            val indices = mutableMapOf<Int, Int>()
            var currentIndex = 0
            pokemonByGeneration.keys.sorted().forEach { generation ->
                indices[generation] = currentIndex // Header index
                currentIndex += 1 // Skip header
                val generationPokemon = pokemonByGeneration[generation] ?: emptyList()
                currentIndex += generationPokemon.size // Skip Pokémon in this generation
            }
            indices
        }
    }
    
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
                .padding(bottom = 8.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFFD32F2F), // Pokedex red
                unfocusedBorderColor = Color(0xFF666666), // Dark gray
                focusedLabelColor = Color(0xFFD32F2F), // Pokedex red
                unfocusedLabelColor = Color(0xFF666666) // Dark gray
            )
        )
        
        // Generation selector row
        if (pokemonList.isNotEmpty() && searchQuery.isEmpty()) {
            var scrollToGeneration by remember { mutableStateOf<Int?>(null) }
            
            // Handle scroll when generation is selected
            LaunchedEffect(scrollToGeneration) {
                scrollToGeneration?.let { gen ->
                    generationIndices[gen]?.let { index ->
                        gridState.animateScrollToItem(index)
                    }
                    scrollToGeneration = null
                }
            }
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                (1..9).forEach { gen ->
                    FilterChip(
                        selected = false,
                        onClick = {
                            scrollToGeneration = gen
                        },
                        label = {
                            Text(
                                text = gen.toString(),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        modifier = Modifier.weight(1f),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFFD32F2F),
                            selectedLabelColor = Color.White,
                            containerColor = Color(0xFFF5F5F5),
                            labelColor = Color(0xFF1A1A1A)
                        )
                    )
                }
            }
        }
        
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
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "No Pokémon found",
                                style = MaterialTheme.typography.titleLarge,
                                textAlign = TextAlign.Center,
                                color = Color(0xFF1A1A1A),
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Try adjusting your search",
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center,
                                color = Color(0xFF666666)
                            )
                        }
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(4), // 4 columns like you requested
                        state = gridState,
                        horizontalArrangement = Arrangement.spacedBy(8.dp), // Reduced spacing for 4 columns
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        // Only show generation headers if not searching
                        if (searchQuery.isEmpty()) {
                            // Group and display by generation
                            pokemonByGeneration.keys.sorted().forEach { generation ->
                                val generationPokemon = pokemonByGeneration[generation] ?: emptyList()
                                
                                // Generation header
                                item(
                                    key = "header_gen_$generation",
                                    span = { GridItemSpan(4) }
                                ) {
                                    GenerationHeader(
                                        generation = generation,
                                        modifier = Modifier.padding(vertical = 16.dp, horizontal = 4.dp)
                                    )
                                }
                                
                                // Pokémon in this generation
                                items(
                                    items = generationPokemon,
                                    key = { pokemon -> "${pokemon.id}_${ownedPokemonIds.contains(pokemon.id)}" }
                                ) { pokemon ->
                                    val isOwned = ownedPokemonIds.contains(pokemon.id)
                                    PokemonGridCard(
                                        pokemon = pokemon,
                                        onClick = { onPokemonClick(pokemon.name) },
                                        onLongPress = onPokemonLongPress?.let { 
                                            { it(pokemon.id, pokemon.name) }
                                        },
                                        isOwned = isOwned
                                    )
                                }
                            }
                        } else {
                            // When searching, show all results without generation headers
                            items(
                                items = pokemonList,
                                key = { pokemon -> "${pokemon.id}_${ownedPokemonIds.contains(pokemon.id)}" }
                            ) { pokemon ->
                                val isOwned = ownedPokemonIds.contains(pokemon.id)
                                PokemonGridCard(
                                    pokemon = pokemon,
                                    onClick = { onPokemonClick(pokemon.name) },
                                    onLongPress = onPokemonLongPress?.let { 
                                        { it(pokemon.id, pokemon.name) }
                                    },
                                    isOwned = isOwned
                                )
                            }
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
    onLongPress: (() -> Unit)? = null,
    isOwned: Boolean = false,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    
    // Animation for card scale on press
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1f,
        animationSpec = tween(durationMillis = 150),
        label = "gridCardScale"
    )
    
    // Green border for owned Pokémon, white border for unowned with animation
    val borderColor = if (isOwned) Color(0xFF4CAF50) else Color.White
    val borderWidth by animateDpAsState(
        targetValue = if (isOwned) 3.dp else 2.dp,
        animationSpec = tween(durationMillis = 300),
        label = "gridBorderWidth"
    )
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(0.8f) // Vertical rectangle like in your image
            .clip(RoundedCornerShape(16.dp))
            .background(Color.Transparent) // Fully transparent background
            .border(
                width = borderWidth,
                color = borderColor,
                shape = RoundedCornerShape(16.dp)
            )
            .scale(scale)
            .pointerInput(pokemon.id) {
                detectTapGestures(
                    onLongPress = {
                        if (onLongPress != null) {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onLongPress()
                        }
                    },
                    onTap = { 
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onClick() 
                    },
                    onPress = {
                        isPressed = true
                        tryAwaitRelease()
                        isPressed = false
                    }
                )
            }
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

/**
 * Generation header component
 */
@Composable
private fun GenerationHeader(
    generation: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFD32F2F).copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = "Generation $generation",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFD32F2F),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        )
    }
}
