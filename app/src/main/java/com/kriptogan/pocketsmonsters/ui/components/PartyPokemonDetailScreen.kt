package com.kriptogan.pocketsmonsters.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardOptions
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
    var showMaxHPDialog by remember { mutableStateOf(false) }
    
    // Local state for current HP and max HP to make UI reactive
    var currentHP by remember { mutableStateOf(partyPokemon.currentHP) }
    var maxHP by remember { mutableStateOf(partyPokemon.maxHP) }
    
    // Sync local HP state with PartyManager data when screen becomes active
    LaunchedEffect(Unit) {
        val updatedParty = partyManager.getParty().find { it.id == partyPokemon.id }
        updatedParty?.let { 
            currentHP = it.currentHP
            maxHP = it.maxHP
        }
    }
    
    // Sync local HP state whenever partyPokemon changes (e.g., when returning from other screens)
    LaunchedEffect(partyPokemon.id) {
        val updatedParty = partyManager.getParty().find { it.id == partyPokemon.id }
        updatedParty?.let { 
            currentHP = it.currentHP
            maxHP = it.maxHP
        }
    }
    
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
                            .data("file:///android_asset/front_images/${partyPokemon.name}.png")
                            .crossfade(true)
                            .build(),
                        contentDescription = "Official artwork of ${partyPokemon.name}",
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
                        partyPokemon.basePokemon.types.forEach { type ->
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
                            text = "AC",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF666666),
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        
                        Text(
                            text = "${partyPokemon.calculateCurrentArmorClass()}",
                            style = MaterialTheme.typography.titleLarge,
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
                            text = when {
                                partyPokemon.calculateCurrentInitiative() > 0 -> "+${partyPokemon.calculateCurrentInitiative()}"
                                partyPokemon.calculateCurrentInitiative() < 0 -> "${partyPokemon.calculateCurrentInitiative()}"
                                else -> "+0"
                            },
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
                        
                                                 // 2. HP value (clickable to edit max HP)
                         Text(
                             text = "${maxHP}",
                             style = MaterialTheme.typography.titleLarge,
                             fontWeight = FontWeight.Medium,
                             color = Color(0xFF1A1A1A),
                             textAlign = TextAlign.Center,
                             modifier = Modifier
                                 .padding(bottom = 16.dp)
                                 .clickable { showMaxHPDialog = true }
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
                                     if (currentHP < maxHP) {
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
                                     tint = if (currentHP < maxHP) Color(0xFF4CAF50) else Color(0xFFCCCCCC),
                                     modifier = Modifier.size(20.dp)
                                 )
                             }
                        }
                        
                        // Reduced spacing to align with movement speed
                        Spacer(modifier = Modifier.height(12.dp))
                        
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
            
            // Nature Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    // Nature display in horizontal format
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Nature name
                        Text(
                            text = partyPokemon.nature.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        
                        // Separator
                        Text(
                            text = "|",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF666666)
                        )
                        
                        // Buffed stat with up arrow (red)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = androidx.compose.material.icons.Icons.Default.KeyboardArrowUp,
                                contentDescription = "Increased",
                                tint = Color.Red,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = partyPokemon.nature.increasedStat ?: "None",
                                style = MaterialTheme.typography.titleMedium,
                                color = if (partyPokemon.nature.increasedStat != null) Color.Red else Color(0xFF666666),
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        // Separator
                        Text(
                            text = "|",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF666666)
                        )
                        
                        // Debuffed stat with down arrow (blue)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = androidx.compose.material.icons.Icons.Default.KeyboardArrowDown,
                                contentDescription = "Decreased",
                                tint = Color.Blue,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = partyPokemon.nature.decreasedStat ?: "None",
                                style = MaterialTheme.typography.titleMedium,
                                color = if (partyPokemon.nature.decreasedStat != null) Color.Blue else Color(0xFF666666),
                                fontWeight = FontWeight.Bold
                            )
                        }
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
                        
                                                 // Empty space where AC was (removed to avoid duplication)
                         Spacer(modifier = Modifier.weight(1f))
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
                        text = "Current Move Set (${currentMoveSet.size}/4)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Display current moves
                    if (currentMoveSet.isNotEmpty()) {
                        currentMoveSet.forEach { moveName ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = moveName.replaceFirstChar { it.uppercase() },
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                
                                IconButton(
                                    onClick = {
                                        currentMoveSet.remove(moveName)
                                        // Save the updated move set to local datastore
                                        val updatedPokemon = partyPokemon.copy(currentMoveSet = currentMoveSet.toList())
                                        partyManager.updatePartyPokemon(updatedPokemon)
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Remove move",
                                        tint = Color(0xFFD32F2F),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    } else {
                        Text(
                            text = "No moves selected",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "Available Moves (Level ${partyPokemon.level})",
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
                                
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (currentMoveSet.contains(move.name)) {
                                        // Show remove button for moves in current set
                                        IconButton(
                                            onClick = {
                                                currentMoveSet.remove(move.name)
                                                // Save the updated move set to local datastore
                                                val updatedPokemon = partyPokemon.copy(currentMoveSet = currentMoveSet.toList())
                                                partyManager.updatePartyPokemon(updatedPokemon)
                                            },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Remove move",
                                                tint = Color(0xFFD32F2F),
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    } else if (currentMoveSet.size < 4) {
                                        // Show add button for available moves
                                        IconButton(
                                            onClick = {
                                                currentMoveSet.add(move.name)
                                                // Save the updated move set to local datastore
                                                val updatedPokemon = partyPokemon.copy(currentMoveSet = currentMoveSet.toList())
                                                partyManager.updatePartyPokemon(updatedPokemon)
                                            },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Add,
                                                contentDescription = "Add move",
                                                tint = Color(0xFF4CAF50),
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
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
                                        // Save the updated conditions to local datastore
                                        val updatedPokemon = partyPokemon.copy(conditions = currentConditions.toList())
                                        partyManager.updatePartyPokemon(updatedPokemon)
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
                    Column {
                        Text("Select a status condition to add to this Pokemon:")
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Show available conditions that aren't already applied
                        val availableConditions = Condition.values().filter { condition ->
                            !currentConditions.contains(condition)
                        }
                        
                        if (availableConditions.isNotEmpty()) {
                            availableConditions.forEach { condition ->
                                TextButton(
                                    onClick = {
                                        currentConditions.add(condition)
                                        // Save the updated conditions to local datastore
                                        val updatedPokemon = partyPokemon.copy(conditions = currentConditions.toList())
                                        partyManager.updatePartyPokemon(updatedPokemon)
                                        showConditionDialog = false
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalAlignment = Alignment.Start
                                    ) {
                                        Text(
                                            text = condition.displayName,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Text(
                                            text = condition.description,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        } else {
                            Text(
                                text = "All conditions are already applied",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showConditionDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
        
        // Max HP Edit Dialog
        if (showMaxHPDialog) {
            var newMaxHP by remember { mutableStateOf(maxHP.toString()) }
            var showError by remember { mutableStateOf(false) }
            
            AlertDialog(
                onDismissRequest = { showMaxHPDialog = false },
                title = { Text("Edit Max HP") },
                text = { 
                    Column {
                        Text("Enter new max HP value:")
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        OutlinedTextField(
                            value = newMaxHP,
                            onValueChange = { 
                                newMaxHP = it
                                showError = false
                            },
                            label = { Text("Max HP") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            isError = showError
                        )
                        
                        if (showError) {
                            Text(
                                text = "Please enter a valid positive number",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            val newMaxHPValue = newMaxHP.toIntOrNull()
                            if (newMaxHPValue != null && newMaxHPValue > 0) {
                                val oldMaxHP = maxHP
                                maxHP = newMaxHPValue
                                
                                // Update current HP only if it equals the old max HP
                                if (currentHP == oldMaxHP) {
                                    currentHP = newMaxHPValue
                                }
                                
                                // Save to PartyManager
                                val updatedPokemon = partyPokemon.copy(
                                    maxHP = newMaxHPValue,
                                    currentHP = currentHP
                                )
                                partyManager.updatePartyPokemon(updatedPokemon)
                                
                                showMaxHPDialog = false
                            } else {
                                showError = true
                            }
                        }
                    ) {
                        Text("Save")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showMaxHPDialog = false }) {
                        Text("Cancel")
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
