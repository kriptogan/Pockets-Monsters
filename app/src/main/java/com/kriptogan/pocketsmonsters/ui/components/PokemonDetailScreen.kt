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
                            text = "D&D View (Primary)",
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
        // Large Pokémon sprite
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
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
            }
        }

        // Content
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Basic Info Card
            Card(
                modifier = Modifier.fillMaxWidth(),
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
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Types
                    Text(
                        text = "Types",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        pokemon.types.forEach { typeSlot ->
                            Card(
                                modifier = Modifier.padding(4.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                                ),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Text(
                                    text = typeSlot.type.name.replaceFirstChar { it.uppercase() },
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }
            
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
