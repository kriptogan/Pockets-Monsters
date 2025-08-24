package com.kriptogan.pocketsmonsters.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.kriptogan.pocketsmonsters.data.converter.DnDView

@Composable
fun DnDView(
    dndView: DnDView?,
    modifier: Modifier = Modifier
) {
    if (dndView == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("No D&D data available")
        }
        return
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
    ) {
        // Row containing image and D&D character sheet
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Large Pokémon sprite
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
                    val spriteUrl = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/${dndView.pokemon.id}.png"
                    
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(spriteUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Official artwork of ${dndView.pokemon.name}",
                        modifier = Modifier
                            .size(180.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Fit
                    )
                }
            }

            // Basic D&D Info Card
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "D&D Character Sheet",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        InfoItem(
                            label = "Hit Dice",
                            value = dndView.hitDice
                        )
                        InfoItem(
                            label = "Movement",
                            value = "${dndView.movement} ft"
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        InfoItem(
                            label = "Armor Class",
                            value = dndView.ac.toString()
                        )
                        InfoItem(
                            label = "Initiative",
                            value = formatModifier(dndView.initiative)
                        )
                    }
                }
            }
        }

        // D&D Stats Cards
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Converted Stats Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Converted Stats",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Display each stat with original and converted values
                    // HP should show original value only (not converted)
                    StatRow(
                        statName = "HP",
                        originalValue = dndView.pokemon.stats.find { it.stat.name == "hp" }?.baseStat ?: 0,
                        convertedValue = dndView.pokemon.stats.find { it.stat.name == "hp" }?.baseStat ?: 0, // Use original HP
                        modifier = null, // No modifier for HP
                        showOriginalInBrackets = false // Don't show [original] for HP since it's the same
                    )
                    
                    StatRow(
                        statName = "Attack",
                        originalValue = dndView.pokemon.stats.find { it.stat.name == "attack" }?.baseStat ?: 0,
                        convertedValue = dndView.convertedStats["Attack"] ?: 0,
                        modifier = dndView.modifiers["Attack"] ?: 0,
                        showOriginalInBrackets = false // Don't show [original] for other stats in DnD view
                    )
                    
                    StatRow(
                        statName = "Defense",
                        originalValue = dndView.pokemon.stats.find { it.stat.name == "defense" }?.baseStat ?: 0,
                        convertedValue = dndView.convertedStats["Defense"] ?: 0,
                        modifier = dndView.modifiers["Defense"] ?: 0,
                        showOriginalInBrackets = false // Don't show [original] for other stats in DnD view
                    )
                    
                    StatRow(
                        statName = "Sp.Atk",
                        originalValue = dndView.pokemon.stats.find { it.stat.name == "special-attack" }?.baseStat ?: 0,
                        convertedValue = dndView.convertedStats["Sp.Atk"] ?: 0,
                        modifier = dndView.modifiers["Sp.Atk"] ?: 0,
                        showOriginalInBrackets = false // Don't show [original] for other stats in DnD view
                    )
                    
                    StatRow(
                        statName = "Sp.Def",
                        originalValue = dndView.pokemon.stats.find { it.stat.name == "special-defense" }?.baseStat ?: 0,
                        convertedValue = dndView.convertedStats["Sp.Def"] ?: 0,
                        modifier = dndView.modifiers["Sp.Def"] ?: 0,
                        showOriginalInBrackets = false // Don't show [original] for other stats in DnD view
                    )
                    
                    StatRow(
                        statName = "Speed",
                        originalValue = dndView.pokemon.stats.find { it.stat.name == "speed" }?.baseStat ?: 0,
                        convertedValue = dndView.convertedStats["Speed"] ?: 0,
                        modifier = dndView.modifiers["Speed"] ?: 0,
                        showOriginalInBrackets = false // Don't show [original] for other stats in DnD view
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Types Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Types",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        dndView.pokemon.types.forEach { typeSlot ->
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
        }
    }
}

@Composable
private fun StatRow(
    statName: String,
    originalValue: Int,
    convertedValue: Int,
    modifier: Int?,
    showOriginalInBrackets: Boolean = true
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = statName,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
        
        Text(
            text = if (modifier != null) "$convertedValue (${formatModifier(modifier)})" else "$convertedValue",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )
        
        if (showOriginalInBrackets) {
            Text(
                text = "[$originalValue]",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
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
 * Format modifier with + or - sign
 */
private fun formatModifier(modifier: Int): String {
    return if (modifier >= 0) "+$modifier" else "$modifier"
}
