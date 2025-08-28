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
                        Box(
                            modifier = Modifier
                                .padding(vertical = 2.dp)
                                .width(80.dp)
                                .height(24.dp)
                                .background(
                                    color = getTypeColor(type.type.name),
                                    shape = RoundedCornerShape(16.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = type.type.name.replaceFirstChar { it.uppercase() },
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                textAlign = TextAlign.Center
                            )
                        }
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
                                                              // AC title and value
                      Text(
                          text = "AC",
                          style = MaterialTheme.typography.bodySmall,
                          fontWeight = FontWeight.Bold,
                          color = Color(0xFF666666),
                          modifier = Modifier.padding(bottom = 4.dp)
                      )
                      
                      Text(
                          text = "${dndView.ac}",
                          style = MaterialTheme.typography.titleLarge,
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
                    
                                         // HP value (same size as other values)
                     Text(
                         text = "${dndView.convertedStats["HP"] ?: 0}",
                         style = MaterialTheme.typography.titleLarge,
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
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    // Base Experience label above value
                    Text(
                        text = "Base Exp",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF666666),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    
                    // Base Experience value (same size as other values)
                    Text(
                        text = "${pokemon.baseExperience}",
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
                    
                                         // Empty space where AC was (removed to avoid duplication)
                     Spacer(modifier = Modifier.weight(1f))
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
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 6. Type Effectiveness Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Type Effectiveness",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                // Three columns: Weaknesses, Resistances, Immunities
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    // Weaknesses
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Weaknesses",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFD32F2F),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        
                        val weaknesses = getWeaknesses(pokemon.types.map { it.type.name })
                        if (weaknesses.isNotEmpty()) {
                            weaknesses.forEach { type ->
                                Box(
                                    modifier = Modifier
                                        .padding(vertical = 2.dp)
                                        .width(70.dp)
                                        .height(20.dp)
                                        .background(
                                            color = getTypeColor(type),
                                            shape = RoundedCornerShape(12.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = type.replaceFirstChar { it.uppercase() },
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        } else {
                            Text(
                                text = "None",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF999999),
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                            )
                        }
                    }
                    
                    // Resistances
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Resistances",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF4CAF50),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        
                        val resistances = getResistances(pokemon.types.map { it.type.name })
                        if (resistances.isNotEmpty()) {
                            resistances.forEach { type ->
                                Box(
                                    modifier = Modifier
                                        .padding(vertical = 2.dp)
                                        .width(70.dp)
                                        .height(20.dp)
                                        .background(
                                            color = getTypeColor(type),
                                            shape = RoundedCornerShape(12.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = type.replaceFirstChar { it.uppercase() },
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        } else {
                            Text(
                                text = "None",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF999999),
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                            )
                        }
                    }
                    
                    // Immunities
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Immunities",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF424242),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        
                        val immunities = getImmunities(pokemon.types.map { it.type.name })
                        if (immunities.isNotEmpty()) {
                            immunities.forEach { type ->
                                Box(
                                    modifier = Modifier
                                        .padding(vertical = 2.dp)
                                        .width(70.dp)
                                        .height(20.dp)
                                        .background(
                                            color = Color(0xFF424242),
                                            shape = RoundedCornerShape(12.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = type.replaceFirstChar { it.uppercase() },
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        } else {
                            Text(
                                text = "None",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF999999),
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                            )
                        }
                    }
                }
            }
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

/**
 * Calculate weaknesses based on Pokemon types
 */
private fun getWeaknesses(types: List<String>): List<String> {
    if (types.isEmpty()) return emptyList()
    if (types.size == 1) return getSingleTypeWeaknesses(types[0])
    
    // For dual types, calculate combined weaknesses
    val type1 = types[0]
    val type2 = types[1]
    
    // Get all types that could potentially be weaknesses
    val allTypes = listOf("normal", "fire", "water", "electric", "grass", "ice", "fighting", 
                          "poison", "ground", "flying", "psychic", "bug", "rock", "ghost", 
                          "dragon", "dark", "steel", "fairy")
    
    return allTypes.filter { type ->
        val effectiveness1 = getTypeEffectiveness(type, type1)
        val effectiveness2 = getTypeEffectiveness(type, type2)
        val totalEffectiveness = effectiveness1 * effectiveness2
        
        // Return true if this type is weak (2x or 4x effective) AND not immune
        totalEffectiveness > 1.0 && totalEffectiveness > 0.0
    }
}

/**
 * Calculate resistances based on Pokemon types
 */
private fun getResistances(types: List<String>): List<String> {
    if (types.isEmpty()) return emptyList()
    if (types.size == 1) return getSingleTypeResistances(types[0])
    
    // For dual types, calculate combined resistances
    val type1 = types[0]
    val type2 = types[1]
    
    // Get all types that could potentially be resisted
    val allTypes = listOf("normal", "fire", "water", "electric", "grass", "ice", "fighting", 
                          "poison", "ground", "flying", "psychic", "bug", "rock", "ghost", 
                          "dragon", "dark", "steel", "fairy")
    
    return allTypes.filter { type ->
        val effectiveness1 = getTypeEffectiveness(type, type1)
        val effectiveness2 = getTypeEffectiveness(type, type2)
        val totalEffectiveness = effectiveness1 * effectiveness2
        
        // Return true if this type is resisted (0.5x or 0.25x effective)
        totalEffectiveness < 1.0 && totalEffectiveness > 0.0
    }
}

/**
 * Calculate immunities based on Pokemon types
 */
private fun getImmunities(types: List<String>): List<String> {
    if (types.isEmpty()) return emptyList()
    if (types.size == 1) return getSingleTypeImmunities(types[0])
    
    // For dual types, calculate combined immunities
    val type1 = types[0]
    val type2 = types[1]
    
    // Get all attacking types that could potentially be immunities
    val allAttackingTypes = listOf("normal", "fire", "water", "electric", "grass", "ice", "fighting", 
                                   "poison", "ground", "flying", "psychic", "bug", "rock", "ghost", 
                                   "dragon", "dark", "steel", "fairy")
    
    return allAttackingTypes.filter { attackingType ->
        val effectiveness1 = getTypeEffectiveness(attackingType, type1)
        val effectiveness2 = getTypeEffectiveness(attackingType, type2)
        val totalEffectiveness = effectiveness1 * effectiveness2
        
        // Return true if this attacking type has 0x effectiveness (Pokemon is immune)
        totalEffectiveness == 0.0
    }
}

/**
 * Get single type weaknesses
 */
private fun getSingleTypeWeaknesses(type: String): List<String> {
    return when (type.lowercase()) {
        "normal" -> listOf("fighting")
        "fire" -> listOf("water", "ground", "rock")
        "water" -> listOf("electric", "grass")
        "electric" -> listOf("ground")
        "grass" -> listOf("fire", "ice", "poison", "flying", "bug")
        "ice" -> listOf("fire", "fighting", "rock", "steel")
        "fighting" -> listOf("flying", "psychic", "fairy")
        "poison" -> listOf("ground", "psychic")
        "ground" -> listOf("water", "grass", "ice")
        "flying" -> listOf("electric", "ice", "rock")
        "psychic" -> listOf("bug", "ghost", "dark")
        "bug" -> listOf("fire", "flying", "rock")
        "rock" -> listOf("water", "grass", "fighting", "ground", "steel")
        "ghost" -> listOf("ghost", "dark")
        "dragon" -> listOf("ice", "dragon", "fairy")
        "dark" -> listOf("fighting", "bug", "fairy")
        "steel" -> listOf("fire", "fighting", "ground")
        "fairy" -> listOf("poison", "steel")
        else -> emptyList()
    }
}

/**
 * Get single type resistances
 */
private fun getSingleTypeResistances(type: String): List<String> {
    return when (type.lowercase()) {
        "normal" -> emptyList()
        "fire" -> listOf("fire", "grass", "ice", "bug", "steel")
        "water" -> listOf("fire", "water", "ice", "steel")
        "electric" -> listOf("electric", "flying", "steel")
        "grass" -> listOf("water", "electric", "grass", "ground")
        "ice" -> listOf("ice")
        "fighting" -> listOf("bug", "rock", "dark")
        "poison" -> listOf("grass", "fighting", "poison", "bug", "fairy")
        "ground" -> listOf("poison", "rock")
        "flying" -> listOf("grass", "fighting", "bug")
        "psychic" -> listOf("fighting", "psychic")
        "bug" -> listOf("grass", "fighting", "ground")
        "rock" -> listOf("normal", "fire", "poison", "flying")
        "ghost" -> listOf("poison", "bug")
        "dragon" -> listOf("fire", "water", "electric", "grass")
        "dark" -> listOf("ghost", "dark")
        "steel" -> listOf("normal", "grass", "ice", "flying", "psychic", "bug", "rock", "dragon", "steel", "fairy")
        "fairy" -> listOf("fighting", "bug", "dark")
        else -> emptyList()
    }
}

/**
 * Get single type immunities
 */
private fun getSingleTypeImmunities(type: String): List<String> {
    return when (type.lowercase()) {
        "normal" -> listOf("ghost")
        "electric" -> listOf("ground")
        "fighting" -> listOf("ghost")
        "poison" -> listOf("steel")
        "ground" -> listOf("flying")
        "psychic" -> listOf("dark")
        "ghost" -> listOf("normal")
        "dragon" -> listOf("fairy")
        "fairy" -> listOf("dragon")
        else -> emptyList()
    }
}

/**
 * Get type effectiveness multiplier for a specific type combination
 * @param attackingType The type of the attacking move
 * @param defendingType The type of the defending Pokemon
 * @return The effectiveness multiplier (0.0, 0.5, 1.0, or 2.0)
 */
private fun getTypeEffectiveness(attackingType: String, defendingType: String): Double {
    return when (attackingType) {
        "normal" -> when (defendingType) {
            "rock", "steel" -> 0.5
            "ghost" -> 0.0
            else -> 1.0
        }
        "fire" -> when (defendingType) {
            "fire", "water", "rock", "dragon" -> 0.5
            "grass", "ice", "bug", "steel" -> 2.0
            else -> 1.0
        }
        "water" -> when (defendingType) {
            "water", "grass", "dragon" -> 0.5
            "fire", "ground", "rock" -> 2.0
            else -> 1.0
        }
        "electric" -> when (defendingType) {
            "electric", "grass", "dragon" -> 0.5
            "water", "flying" -> 2.0
            "ground" -> 0.0
            else -> 1.0
        }
        "grass" -> when (defendingType) {
            "fire", "grass", "poison", "flying", "bug", "dragon", "steel" -> 0.5
            "water", "ground", "rock" -> 2.0
            else -> 1.0
        }
        "ice" -> when (defendingType) {
            "fire", "water", "ice", "steel" -> 0.5
            "grass", "ground", "flying", "dragon" -> 2.0
            else -> 1.0
        }
        "fighting" -> when (defendingType) {
            "normal", "ice", "rock", "dark", "steel" -> 2.0
            "poison", "flying", "psychic", "bug", "fairy" -> 0.5
            "ghost" -> 0.0
            else -> 1.0
        }
        "poison" -> when (defendingType) {
            "grass", "fairy" -> 2.0
            "poison", "ground", "rock", "ghost" -> 0.5
            "steel" -> 0.0
            else -> 1.0
        }
        "ground" -> when (defendingType) {
            "fire", "electric", "poison", "rock", "steel" -> 2.0
            "grass", "bug" -> 0.5
            "flying" -> 0.0
            else -> 1.0
        }
        "flying" -> when (defendingType) {
            "grass", "fighting", "bug" -> 2.0
            "electric", "rock", "steel" -> 0.5
            else -> 1.0
        }
        "psychic" -> when (defendingType) {
            "fighting", "poison" -> 2.0
            "psychic", "steel" -> 0.5
            "dark" -> 0.0
            else -> 1.0
        }
        "bug" -> when (defendingType) {
            "grass", "psychic", "dark" -> 2.0
            "fire", "fighting", "poison", "flying", "ghost", "steel", "fairy" -> 0.5
            else -> 1.0
        }
        "rock" -> when (defendingType) {
            "fire", "ice", "flying", "bug" -> 2.0
            "fighting", "ground", "steel" -> 0.5
            else -> 1.0
        }
        "ghost" -> when (defendingType) {
            "psychic", "ghost" -> 2.0
            "dark" -> 0.5
            "normal" -> 0.0
            else -> 1.0
        }
        "dragon" -> when (defendingType) {
            "dragon" -> 2.0
            "steel" -> 0.5
            "fairy" -> 0.0
            else -> 1.0
        }
        "dark" -> when (defendingType) {
            "psychic", "ghost" -> 2.0
            "fighting", "dark", "fairy" -> 0.5
            else -> 1.0
        }
        "steel" -> when (defendingType) {
            "ice", "rock", "fairy" -> 2.0
            "fire", "water", "electric", "steel" -> 0.5
            else -> 1.0
        }
        "fairy" -> when (defendingType) {
            "fighting", "dragon", "dark" -> 2.0
            "fire", "poison", "steel" -> 0.5
            else -> 1.0
        }
        else -> 1.0
    }
}
