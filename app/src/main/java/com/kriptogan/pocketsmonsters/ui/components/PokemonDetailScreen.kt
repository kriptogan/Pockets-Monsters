package com.kriptogan.pocketsmonsters.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.activity.compose.BackHandler
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.kriptogan.pocketsmonsters.data.models.Pokemon
import com.kriptogan.pocketsmonsters.data.tcg.PokemonTCGData
import com.kriptogan.pocketsmonsters.data.tcg.TCGSet
import com.kriptogan.pocketsmonsters.data.tcg.TCGCard

@Composable
fun PokemonDetailScreen(
    pokemon: Pokemon?,
    onBackClick: () -> Unit,
    onPartyUpdated: () -> Unit = {}, // Kept for compatibility, but not used
    tcgData: PokemonTCGData? = null,
    isLoadingTCG: Boolean = false,
    modifier: Modifier = Modifier
) {
    // Handle system back button
    BackHandler {
        onBackClick()
    }
    
    if (pokemon == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("No Pokémon selected")
        }
        return
    }

    // Fade-in animation for content
    val alpha by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(durationMillis = 300),
        label = "detailScreenAlpha"
    )
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .alpha(alpha)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Header - Back button and Pokemon name
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color(0xFFD32F2F)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Text(
                text = pokemon.name.replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A1A)
            )
        }
        
        // Pokémon Image with subtle shadow
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White.copy(alpha = 0.1f)
            )
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data("file:///android_asset/front_images/${pokemon.name}.png")
                    .crossfade(true)
                    .build(),
                contentDescription = "Official artwork of ${pokemon.name}",
                modifier = Modifier
                    .size(280.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Fit
            )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Types
        if (pokemon.types.isNotEmpty()) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Types",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A1A),
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    pokemon.types.forEach { type ->
                        Box(
                            modifier = Modifier
                                .width(100.dp)
                                .height(32.dp)
                                .background(
                                    color = getTypeColor(type.type.name),
                                    shape = RoundedCornerShape(16.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = type.type.name.replaceFirstChar { it.uppercase() },
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // TCG Sets Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            border = androidx.compose.foundation.BorderStroke(
                width = 1.dp,
                color = Color(0xFFD32F2F).copy(alpha = 0.3f)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "TCG Sets",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A1A),
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                when {
                    isLoadingTCG -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(32.dp),
                                    color = Color(0xFFD32F2F)
                                )
                                Text(
                                    text = "Loading TCG data...",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF666666)
                                )
                            }
                        }
                    }
                    tcgData == null || tcgData.sets.isEmpty() -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "No TCG sets found",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color(0xFF1A1A1A),
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = "This Pokémon may not have any TCG cards yet.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF666666),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                    else -> {
                        // Display TCG sets
                        tcgData.sets.forEach { set ->
                            TCGSetCard(
                                set = set,
                                cards = tcgData.cards.filter { it.set?.id == set.id },
                                modifier = Modifier.padding(bottom = 16.dp)
                            )
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
    }
}

/**
 * Display a TCG set with its cards
 */
@Composable
private fun TCGSetCard(
    set: TCGSet,
    cards: List<TCGCard>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF5F5F5)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            // Set name and info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = set.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1A1A)
                    )
                    
                    if (set.releaseDate != null) {
                        Text(
                            text = "Released: ${set.releaseDate}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF666666),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
                
                // Set symbol/logo if available
                if (set.images?.symbol != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(set.images.symbol)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Set symbol",
                        modifier = Modifier.size(40.dp),
                        contentScale = ContentScale.Fit
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Card images
            if (cards.isNotEmpty()) {
                Text(
                    text = "Cards (${cards.size})",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF1A1A1A),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(cards) { card ->
                        TCGCardImage(
                            card = card,
                            modifier = Modifier.size(120.dp, 168.dp) // Standard card aspect ratio
                        )
                    }
                }
            } else {
                Text(
                    text = "No cards found in this set",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF999999),
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }
    }
}

/**
 * Display a single TCG card image with hover effect
 */
@Composable
private fun TCGCardImage(
    card: TCGCard,
    modifier: Modifier = Modifier
) {
    var isHovered by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isHovered) 1.05f else 1f,
        animationSpec = tween(durationMillis = 200),
        label = "cardHoverScale"
    )
    
    Card(
        modifier = modifier
            .scale(scale),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isHovered) 8.dp else 4.dp
        )
    ) {
        val imageUrl = card.images?.large ?: card.images?.small
        
        if (imageUrl != null) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(imageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = "${card.name} card",
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(card.id) {
                        detectTapGestures(
                            onPress = {
                                isHovered = true
                                tryAwaitRelease()
                                isHovered = false
                            }
                        )
                    },
                contentScale = ContentScale.Fit
            )
        } else {
            // Placeholder if no image
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = card.name,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    color = Color(0xFF666666),
                    modifier = Modifier.padding(8.dp)
                )
            }
        }
    }
}

/**
 * Get the official Pokemon type color
 */
private fun getTypeColor(typeName: String): Color {
    return when (typeName.lowercase()) {
        "normal" -> Color(0xFFA8A878)
        "fire" -> Color(0xFFF08030)
        "water" -> Color(0xFF6890F0)
        "electric" -> Color(0xFFF8D030)
        "grass" -> Color(0xFF78C850)
        "ice" -> Color(0xFF98D8D8)
        "fighting" -> Color(0xFFC03028)
        "poison" -> Color(0xFFA040A0)
        "ground" -> Color(0xFFE0C068)
        "flying" -> Color(0xFFA890F0)
        "psychic" -> Color(0xFFF85888)
        "bug" -> Color(0xFFA8B820)
        "rock" -> Color(0xFFB8A038)
        "ghost" -> Color(0xFF705898)
        "dragon" -> Color(0xFF7038F8)
        "dark" -> Color(0xFF705848)
        "steel" -> Color(0xFFB8B8D0)
        "fairy" -> Color(0xFFEE99AC)
        else -> Color(0xFFA8A878) // Default to normal
    }
}
