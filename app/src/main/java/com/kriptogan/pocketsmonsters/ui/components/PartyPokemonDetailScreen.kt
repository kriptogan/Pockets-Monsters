package com.kriptogan.pocketsmonsters.ui.components

import androidx.compose.foundation.background
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
    
    var currentHP by remember { mutableStateOf(partyPokemon.currentHP) }
    var currentMoveSet by remember { mutableStateOf(partyPokemon.currentMoveSet.toMutableList()) }
    var currentConditions by remember { mutableStateOf(partyPokemon.conditions.toMutableList()) }
    var showMoveSelectionDialog by remember { mutableStateOf(false) }
    var showConditionDialog by remember { mutableStateOf(false) }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(top = 20.dp)
    ) {
        // Header with back button
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
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
                    text = partyPokemon.name.replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                
                Spacer(modifier = Modifier.weight(1f))
                
                Text(
                    text = "Level ${partyPokemon.currentLevel}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
        
        // Content
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Pokemon Image and Basic Info
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Pokemon Image
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
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data("file:///android_asset/front_images/${partyPokemon.name}.png")
                                .crossfade(true)
                                .build(),
                            contentDescription = "Official artwork of ${partyPokemon.name}",
                            modifier = Modifier
                                .size(180.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Fit
                        )
                    }
                }
                
                // Basic Info
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Party Status",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // HP Management
                        Text(
                            text = "HP: $currentHP/${partyPokemon.maxHP}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        LinearProgressIndicator(
                            progress = { currentHP.toFloat() / partyPokemon.maxHP.toFloat() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // HP Controls
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            IconButton(
                                onClick = { 
                                    if (currentHP > 0) currentHP-- 
                                },
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(
                                        color = MaterialTheme.colorScheme.errorContainer,
                                        shape = RoundedCornerShape(20.dp)
                                    )
                            ) {
                                                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Decrease HP",
                                tint = MaterialTheme.colorScheme.onErrorContainer
                            )
                            }
                            
                            IconButton(
                                onClick = { 
                                    if (currentHP < partyPokemon.maxHP) currentHP++ 
                                },
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(
                                        color = MaterialTheme.colorScheme.primaryContainer,
                                        shape = RoundedCornerShape(20.dp)
                                    )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Increase HP",
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Current Move Set
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
                            text = "Current Move Set",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        
                        TextButton(
                            onClick = { showMoveSelectionDialog = true }
                        ) {
                            Text("Change Moves")
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    if (currentMoveSet.isNotEmpty()) {
                        currentMoveSet.forEachIndexed { index, move ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${index + 1}. ${move.replaceFirstChar { it.uppercase() }}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                
                                IconButton(
                                    onClick = {
                                        currentMoveSet.removeAt(index)
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                                                    Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Remove move",
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
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
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
            
            // Save Changes Button
            Button(
                onClick = {
                    // Update the party Pokemon with new values
                    val updatedPokemon = partyPokemon.copy(
                        currentHP = currentHP,
                        currentMoveSet = currentMoveSet,
                        conditions = currentConditions
                    )
                    partyManager.updatePartyPokemon(updatedPokemon)
                    onBackClick()
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("Save Changes")
            }
        }
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
