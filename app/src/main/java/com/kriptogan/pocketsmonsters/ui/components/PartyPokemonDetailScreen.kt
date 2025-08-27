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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.activity.compose.BackHandler
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.kriptogan.pocketsmonsters.data.models.PartyPokemon
import com.kriptogan.pocketsmonsters.data.models.Condition
import com.kriptogan.pocketsmonsters.data.party.PartyManager
import androidx.compose.ui.graphics.Color
import com.kriptogan.pocketsmonsters.data.converter.DnDConverter
import com.kriptogan.pocketsmonsters.data.models.Pokemon
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp

@Composable
fun PartyPokemonDetailScreen(
    partyPokemon: PartyPokemon?,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Handle system back button
    BackHandler {
        onBackClick()
    }
    
    if (partyPokemon == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("No party Pokemon selected")
        }
        return
    }
    
    val context = LocalContext.current
    val partyManager = remember { PartyManager(context) }
    
    var currentMoveSet by remember { mutableStateOf(partyPokemon.currentMoveSet.toMutableList()) }
    var currentConditions by remember { mutableStateOf(partyPokemon.conditions.toMutableList()) }
    var showMoveSelectionDialog by remember { mutableStateOf(false) }
    var showConditionDialog by remember { mutableStateOf(false) }
    
    // Local state for current HP to make UI reactive
    var currentHP by remember { mutableStateOf(partyPokemon.currentHP) }
    
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
                text = partyPokemon.name.replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A1A)
            )
        }
        
        // Create DnD view for stats
        val dndConverter = remember { DnDConverter() }
        val dndView = remember(partyPokemon.name) { 
            // Use the base Pokemon data from PartyPokemon for proper DnD conversion
            dndConverter.convertPokemonToDnD(partyPokemon.basePokemon)
        }
        
        // 2. Top Section - 3 panels side by side
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
                // 2.1. Left Panel - Creature Image
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
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data("file:///android_asset/front_images/${partyPokemon.name}.png")
                            .crossfade(true)
                            .build(),
                        contentDescription = "Official artwork of ${partyPokemon.name}",
                        modifier = Modifier
                            .size(160.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Fit
                    )
                }
                
                // 2.2. Center Panel - Type + Combat Info (using actual DnD data)
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
                        Text(
                            text = "TYPE",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF666666),
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        
                        Text(
                            text = partyPokemon.basePokemon.types.joinToString(", ") { it.type.name.replaceFirstChar { it.uppercase() } },
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF1A1A1A),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        
                        Text(
                            text = "Initiative",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF666666),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        
                        // Initiative value
                        Text(
                            text = "+${dndView.initiative}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF1A1A1A),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        
                        Text(
                            text = "Movement",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF666666),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        
                        // Movement value
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
                        // 1. HP label
                        Text(
                            text = "HP",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF666666),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        
                        // 2. HP value
                        Text(
                            text = "${partyPokemon.maxHP}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF1A1A1A),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        
                        // 3. Current HP label
                        Text(
                            text = "Current HP",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF666666),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        
                        // 4. Current HP value with arrow controls
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Down arrow (decrease HP)
                            IconButton(
                                onClick = {
                                    if (currentHP > 0) {
                                        val newHP = currentHP - 1
                                        val result = partyManager.updatePartyPokemonHP(partyPokemon.id, newHP)
                                        if (result.isSuccess) {
                                            // Update local state to refresh UI immediately
                                            // Note: In a real app, this would trigger a ViewModel refresh
                                            // For now, we'll rely on the parent to refresh the data
                                            currentHP = newHP
                                        }
                                    }
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowDown,
                                    contentDescription = "Decrease HP",
                                    tint = if (currentHP > 0) Color(0xFFD32F2F) else Color(0xFFCCCCCC),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            
                            // Current HP value
                            Text(
                                text = "${currentHP}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF1A1A1A),
                                textAlign = TextAlign.Center
                            )
                            
                            // Up arrow (increase HP)
                            IconButton(
                                onClick = {
                                    if (currentHP < partyPokemon.maxHP) {
                                        val newHP = currentHP + 1
                                        val result = partyManager.updatePartyPokemonHP(partyPokemon.id, newHP)
                                        if (result.isSuccess) {
                                            // Update local state to refresh UI immediately
                                            // Note: In a real app, this would trigger a ViewModel refresh
                                            // For now, we'll rely on the parent to refresh the data
                                            currentHP = newHP
                                        }
                                    }
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowUp,
                                    contentDescription = "Increase HP",
                                    tint = if (currentHP < partyPokemon.maxHP) Color(0xFF4CAF50) else Color(0xFFCCCCCC),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        
                        // 5. Hit Dice label
                        Text(
                            text = "Hit Dice",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF666666),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        
                        // 6. Hit Dice value
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
            
            // 3. Bottom Section - Stats Grid (2x3) - same as Pokemon list details
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
                        // Attack (Red) - using DnD converter like Pokemon list view
                        StatGridItem(
                            label = "ATTACK",
                            value = "${dndView.convertedStats["Attack"] ?: 0}",
                            statModifier = "${dndView.modifiers["Attack"] ?: 0}",
                            color = Color(0xFFF44336),
                            modifier = Modifier.weight(1f)
                        )
                        
                        // Sp.Attack (Purple) - using DnD converter like Pokemon list view
                        StatGridItem(
                            label = "SP.ATTACK",
                            value = "${dndView.convertedStats["Sp.Atk"] ?: 0}",
                            statModifier = "${dndView.modifiers["Sp.Atk"] ?: 0}",
                            color = Color(0xFF9C27B0),
                            modifier = Modifier.weight(1f)
                        )
                        
                        // Speed (Green/Blue) - using DnD converter like Pokemon list view
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
                        // Defense (Gray) - using DnD converter like Pokemon list view
                        StatGridItem(
                            label = "DEFENSE",
                            value = "${dndView.convertedStats["Defense"] ?: 0}",
                            statModifier = "${dndView.modifiers["Defense"] ?: 0}",
                            color = Color(0xFF795548),
                            modifier = Modifier.weight(1f)
                        )
                        
                        // Sp.Defense (Teal/Blue) - using DnD converter like Pokemon list view
                        StatGridItem(
                            label = "SP.DEFENSE",
                            value = "${dndView.convertedStats["Sp.Def"] ?: 0}",
                            statModifier = "${dndView.modifiers["Sp.Def"] ?: 0}",
                            color = Color(0xFF00BCD4),
                            modifier = Modifier.weight(1f)
                        )
                        
                        // AC (Red) - using DnD converter like Pokemon list view
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
            
            // Available Moves
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Available Moves (Level ${partyPokemon.currentLevel})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    if (partyPokemon.availableMoves.isNotEmpty()) {
                        partyPokemon.availableMoves.forEach { move ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = move.name.replaceFirstChar { it.uppercase() },
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = "Learned at Level ${move.levelLearnedAt}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                
                                if (!currentMoveSet.contains(move.name) && currentMoveSet.size < 4) {
                                    IconButton(
                                        onClick = {
                                            currentMoveSet.add(move.name)
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = "Add move",
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        Text(
                            text = "No moves available at this level",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Conditions
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Status Conditions",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        
                        TextButton(
                            onClick = { showConditionDialog = true }
                        ) {
                            Text("Manage")
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    if (currentConditions.isNotEmpty()) {
                        currentConditions.forEach { condition ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = condition.displayName,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                    Text(
                                        text = condition.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                
                                IconButton(
                                    onClick = {
                                        currentConditions.remove(condition)
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Remove condition",
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    } else {
                        Text(
                            text = "No conditions",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Metadata Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Party Information",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Added to Party",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = formatDate(partyPokemon.addedToPartyAt),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Pokemon ID",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "#${partyPokemon.id.toString().padStart(3, '0')}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        // Move Selection Dialog
        if (showMoveSelectionDialog) {
            AlertDialog(
                onDismissRequest = { showMoveSelectionDialog = false },
                title = { Text("Select 4 Moves") },
                text = { 
                    Text("Choose up to 4 moves from the available pool. You can also manage moves directly in the main view.")
                },
                confirmButton = {
                    TextButton(onClick = { showMoveSelectionDialog = false }) {
                        Text("OK")
                    }
                }
            )
        }
        
        // Condition Management Dialog
        if (showConditionDialog) {
            AlertDialog(
                onDismissRequest = { showConditionDialog = false },
                title = { Text("Add Status Condition") },
                text = { 
                    Text("Select a status condition to add to this Pokemon. You can remove conditions directly in the main view.")
                },
                confirmButton = {
                    TextButton(onClick = { showConditionDialog = false }) {
                        Text("OK")
                    }
                }
            )
        }
    }

/**
 * Format timestamp to readable date
 */
private fun formatDate(timestamp: Long): String {
    val date = java.util.Date(timestamp)
    val formatter = java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault())
    return formatter.format(date)
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
