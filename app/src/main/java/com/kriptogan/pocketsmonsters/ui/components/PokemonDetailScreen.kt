package com.kriptogan.pocketsmonsters.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.activity.compose.BackHandler
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.kriptogan.pocketsmonsters.data.converter.DnDConverter
import com.kriptogan.pocketsmonsters.data.models.Pokemon

@Composable
fun PokemonDetailScreen(
    pokemon: Pokemon?,
    onBackClick: () -> Unit,
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

    var showDnDView by remember { mutableStateOf(true) }
    val dndConverter = remember { DnDConverter() }
    val dndView = remember(pokemon) { dndConverter.convertPokemonToDnD(pokemon) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(top = 20.dp)
    ) {
        // Custom header with back button
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    Text(
                        text = pokemon.name.replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                
                // View Toggle Buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(
                        onClick = { showDnDView = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (showDnDView) 
                                MaterialTheme.colorScheme.primary 
                            else 
                                MaterialTheme.colorScheme.surface
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "D&D View",
                            color = if (showDnDView) 
                                MaterialTheme.colorScheme.onPrimary 
                            else 
                                androidx.compose.ui.graphics.Color.Black
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Button(
                        onClick = { showDnDView = false },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (!showDnDView) 
                                MaterialTheme.colorScheme.primary 
                            else 
                                MaterialTheme.colorScheme.surface
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Pokémon View",
                            color = if (!showDnDView) 
                                MaterialTheme.colorScheme.onPrimary 
                            else 
                                androidx.compose.ui.graphics.Color.Black
                        )
                    }
                }
            }
        }

        // Content based on selected view
        if (showDnDView) {
            DnDView(dndView = dndView)
        } else {
            PokemonView(pokemon = pokemon)
        }
    }
}

@Composable
private fun PokemonView(
    pokemon: Pokemon,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // Row containing image and basic info
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Large Pokémon sprite with types
            Card(
                modifier = Modifier
                    .weight(1f)
                    .height(200.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.secondaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    val spriteUrl = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/${pokemon.id}.png"
                    
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(spriteUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Official artwork of ${pokemon.name}",
                        modifier = Modifier
                            .size(180.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Fit
                    )
                    
                    // Types positioned at bottom of image
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = 20.dp),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            pokemon.types.forEach { typeSlot ->
                                val typeColors = getTypeColors(typeSlot.type.name)
                                Card(
                                    modifier = Modifier.padding(4.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = typeColors.background
                                    ),
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Text(
                                        text = typeSlot.type.name.replaceFirstChar { it.uppercase() },
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = typeColors.text,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Basic Info Card
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Basic Information",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        InfoItem(
                            label = "ID",
                            value = "#${pokemon.id.toString().padStart(3, '0')}"
                        )
                        InfoItem(
                            label = "Height",
                            value = "${pokemon.height / 10.0}m"
                        )
                        InfoItem(
                            label = "Weight",
                            value = "${pokemon.weight / 10.0}kg"
                        )
                    }
                }
            }
        }

        // Content
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Stats Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Base Stats",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    pokemon.stats.forEach { stat ->
                        StatRow(
                            statName = stat.stat.name.replaceFirstChar { it.uppercase() },
                            statValue = stat.baseStat
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Experience Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Experience",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    InfoItem(
                        label = "Base Experience",
                        value = pokemon.baseExperience.toString()
                    )
                }
            }
        }
    }
}

@Composable
private fun InfoItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * Data class for type colors
 */
data class TypeColors(
    val background: Color,
    val text: Color
)

/**
 * Get colors for Pokémon types
 */
@Composable
fun getTypeColors(typeName: String): TypeColors {
    return when (typeName.lowercase()) {
        "normal" -> TypeColors(Color(0.66f, 0.66f, 0.47f), Color.White)
        "fire" -> TypeColors(Color(0.94f, 0.50f, 0.19f), Color.White)
        "water" -> TypeColors(Color(0.41f, 0.56f, 0.94f), Color.White)
        "electric" -> TypeColors(Color(0.97f, 0.82f, 0.19f), Color.Black)
        "grass" -> TypeColors(Color(0.47f, 0.78f, 0.31f), Color.White)
        "ice" -> TypeColors(Color(0.60f, 0.85f, 0.85f), Color.Black)
        "fighting" -> TypeColors(Color(0.75f, 0.19f, 0.16f), Color.White)
        "poison" -> TypeColors(Color(0.63f, 0.25f, 0.63f), Color.White)
        "ground" -> TypeColors(Color(0.88f, 0.75f, 0.41f), Color.Black)
        "flying" -> TypeColors(Color(0.66f, 0.56f, 0.94f), Color.White)
        "psychic" -> TypeColors(Color(0.97f, 0.35f, 0.53f), Color.White)
        "bug" -> TypeColors(Color(0.66f, 0.72f, 0.13f), Color.White)
        "rock" -> TypeColors(Color(0.72f, 0.63f, 0.22f), Color.White)
        "ghost" -> TypeColors(Color(0.44f, 0.35f, 0.60f), Color.White)
        "dragon" -> TypeColors(Color(0.44f, 0.22f, 0.97f), Color.White)
        "dark" -> TypeColors(Color(0.44f, 0.35f, 0.28f), Color.White)
        "steel" -> TypeColors(Color(0.72f, 0.72f, 0.82f), Color.Black)
        "fairy" -> TypeColors(Color(0.93f, 0.60f, 0.67f), Color.Black)
        else -> TypeColors(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.onPrimaryContainer)
    }
}

@Composable
private fun StatRow(
    statName: String,
    statValue: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = statName,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
        
        Text(
            text = statValue.toString(),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )
        
        // Visual stat bar
        LinearProgressIndicator(
            progress = { statValue / 255f }, // Max stat value in Pokémon
            modifier = Modifier
                .width(100.dp)
                .height(8.dp),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}
