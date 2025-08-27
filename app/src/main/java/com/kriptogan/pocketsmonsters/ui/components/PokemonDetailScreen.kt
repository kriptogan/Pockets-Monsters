package com.kriptogan.pocketsmonsters.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
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
import androidx.activity.compose.BackHandler
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.kriptogan.pocketsmonsters.data.converter.DnDConverter
import com.kriptogan.pocketsmonsters.data.models.Pokemon
import com.kriptogan.pocketsmonsters.data.party.PartyManager

@Composable
fun PokemonDetailScreen(
    pokemon: Pokemon?,
    onBackClick: () -> Unit,
    onPartyUpdated: () -> Unit = {}, // Callback to refresh parent screen
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

    val dndConverter = remember { DnDConverter() }
    val dndView = remember(pokemon) { dndConverter.convertPokemonToDnD(pokemon) }
    
    // Get context once at the composable level
    val context = LocalContext.current
    
    // Get party state from ViewModel (passed down from parent)
    val partySize = remember { mutableStateOf(0) }
    val isInParty = remember { mutableStateOf(false) }
    
    // Update party state when Pokemon changes
    LaunchedEffect(pokemon.id) {
        // Get party state from PartyManager
        val partyManager = PartyManager(context)
        val currentPartySize = partyManager.getPartySize()
        val currentIsInParty = partyManager.isInParty(pokemon.id)
        
        // Update state
        partySize.value = currentPartySize
        isInParty.value = currentIsInParty
    }
    
    val canAddToParty = remember { derivedStateOf { partySize.value < 6 && !isInParty.value } }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // 1. Simplified Header - Back button and Pokemon name only
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
        
        // 2. Top Section - 3 panels side by side
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 2.1. Left Panel - Creature Image + Types
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(
                        color = Color.Transparent,
                        shape = RoundedCornerShape(16.dp)
                    )
                    .border(
                        width = 2.dp,
                        color = Color.White,
                        shape = RoundedCornerShape(16.dp)
                    )
            ) {
                // Pokemon image - perfectly centered in the entire area
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data("file:///android_asset/front_images/${pokemon.name}.png")
                        .crossfade(true)
                        .build(),
                    contentDescription = "Official artwork of ${pokemon.name}",
                    modifier = Modifier
                        .size(160.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Fit
                )
                
                // Types positioned at the bottom, may overlap with image
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 8.dp)
                ) {
                    pokemon.types.forEach { type ->
                        Text(
                            text = type.type.name.replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF1A1A1A),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }
                }
            }
            
            // 2.2. Center Panel - Type + Combat Info
            Card(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                border = androidx.compose.foundation.BorderStroke(
                    width = 1.dp,
                    color = Color(0xFF4CAF50).copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Type title and types
                    Text(
                        text = "TYPE",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF666666),
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    
                    Text(
                        text = pokemon.types.joinToString(", ") { it.type.name.replaceFirstChar { char -> char.uppercase() } },
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF1A1A1A),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    // Initiative label above value
                    Text(
                        text = "Initiative",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF666666),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    
                    // Initiative value (same size as type value)
                    Text(
                        text = when {
                            dndView.initiative > 0 -> "+${dndView.initiative}"
                            dndView.initiative < 0 -> "${dndView.initiative}"
                            else -> "+0"
                        },
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF1A1A1A),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    // Movement label above value
                    Text(
                        text = "Movement",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF666666),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    
                    // Movement value (same size as type value)
                    Text(
                        text = "${dndView.movement}ft",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF1A1A1A),
                        textAlign = TextAlign.Center
                    )
                }
            }
            
            // 2.3. Right Panel - HP + Hit Dice
            Card(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                border = androidx.compose.foundation.BorderStroke(
                    width = 1.dp,
                    color = Color(0xFFE91E63).copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // HP label above value
                    Text(
                        text = "HP",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF666666),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    
                    // HP value (same size as type value)
                    Text(
                        text = "${dndView.convertedStats["HP"] ?: 0}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF1A1A1A),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    // Hit Dice label above value
                    Text(
                        text = "Hit Dice",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF666666),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    
                    // Hit Dice value (same size as type value)
                    Text(
                        text = dndView.hitDice,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF1A1A1A),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // 3. Bottom Section - Stats Grid (2x3)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            border = androidx.compose.foundation.BorderStroke(
                width = 1.dp,
                color = Color(0xFFF44336).copy(alpha = 0.3f)
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                // Row 1: Attack, Sp.Attack, Speed
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // Attack (Red)
                    StatGridItem(
                        label = "ATTACK",
                        value = "${dndView.convertedStats["Attack"] ?: 0}",
                        statModifier = "${dndView.modifiers["Attack"] ?: 0}",
                        color = Color(0xFFF44336),
                        modifier = Modifier.weight(1f)
                    )
                    
                    // Sp.Attack (Purple)
                    StatGridItem(
                        label = "SP.ATTACK",
                        value = "${dndView.convertedStats["Sp.Atk"] ?: 0}",
                        statModifier = "${dndView.modifiers["Sp.Atk"] ?: 0}",
                        color = Color(0xFF9C27B0),
                        modifier = Modifier.weight(1f)
                    )
                    
                    // Speed (Green/Blue)
                    StatGridItem(
                        label = "SPEED",
                        value = "${dndView.convertedStats["Speed"] ?: 0}",
                        statModifier = "${dndView.modifiers["Speed"] ?: 0}",
                        color = Color(0xFF4CAF50),
                        modifier = Modifier.weight(1f)
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Row 2: Defense, Sp.Defense, AC
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // Defense (Gray)
                    StatGridItem(
                        label = "DEFENSE",
                        value = "${dndView.convertedStats["Defense"] ?: 0}",
                        statModifier = "${dndView.modifiers["Defense"] ?: 0}",
                        color = Color(0xFF795548),
                        modifier = Modifier.weight(1f)
                    )
                    
                    // Sp.Defense (Teal/Blue)
                    StatGridItem(
                        label = "SP.DEFENSE",
                        value = "${dndView.convertedStats["Sp.Def"] ?: 0}",
                        statModifier = "${dndView.modifiers["Sp.Def"] ?: 0}",
                        color = Color(0xFF00BCD4),
                        modifier = Modifier.weight(1f)
                    )
                    
                    // AC (Red)
                    StatGridItem(
                        label = "AC",
                        value = "${dndView.ac}",
                        statModifier = "", // AC does not have a modifier in D&D
                        color = Color(0xFFD32F2F),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // 4. Moves by Level
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            border = androidx.compose.foundation.BorderStroke(
                width = 1.dp,
                color = Color(0xFFD32F2F).copy(alpha = 0.3f)
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Moves by Level",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A1A),
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                // Display moves organized by level
                dndView.movesByDnDLevel.entries.sortedBy { it.key }.forEach { (dndLevel, moves) ->
                    Column(
                        modifier = Modifier.padding(bottom = 12.dp)
                    ) {
                        Text(
                            text = "Level $dndLevel",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFD32F2F)
                        )
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        Text(
                            text = moves.joinToString(", "),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF1A1A1A)
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // 5. Add to Party Button
        Button(
            onClick = {
                if (canAddToParty.value) {
                    val partyManager = PartyManager(context)
                    val result = partyManager.addToParty(pokemon)
                    
                    if (result.isSuccess) {
                        // Update local state immediately
                        val newPartySize = partyManager.getPartySize()
                        val newIsInParty = partyManager.isInParty(pokemon.id)
                        
                        partySize.value = newPartySize
                        isInParty.value = newIsInParty
                        
                        // Notify parent that party has changed
                        onPartyUpdated()
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (canAddToParty.value) Color(0xFFD32F2F) else Color(0xFFCCCCCC)
            ),
            shape = RoundedCornerShape(12.dp),
            enabled = canAddToParty.value
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Text(
                text = when {
                    isInParty.value -> "Already in Party"
                    partySize.value >= 6 -> "Party is Full"
                    else -> "Add to Party"
                },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun StatGridItem(
    label: String,
    value: String,
    statModifier: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF666666),
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        // Value and modifier on the same line
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.Bottom
        ) {
            // Show modifier first, then value in parentheses
            if (statModifier.isNotEmpty()) {
                Text(
                    text = when {
                        statModifier.toIntOrNull() ?: 0 > 0 -> "+${statModifier}"
                        statModifier.toIntOrNull() ?: 0 < 0 -> "${statModifier}"
                        else -> "+0"
                    },
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A1A),
                    textAlign = TextAlign.Center
                )
                
                Text(
                    text = "(${value})",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF999999),
                    textAlign = TextAlign.Center
                )
            } else {
                // If no modifier, just show the value
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A1A),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
