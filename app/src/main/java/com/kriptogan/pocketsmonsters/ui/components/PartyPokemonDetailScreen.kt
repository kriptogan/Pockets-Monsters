package com.kriptogan.pocketsmonsters.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
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
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers

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
    val coroutineScope = rememberCoroutineScope()
    
         var currentMoveSet by remember { mutableStateOf(partyPokemon.currentMoveSet.toMutableList()) }
     var currentConditions by remember { mutableStateOf(partyPokemon.conditions.toMutableList()) }
     var showMoveSelectionDialog by remember { mutableStateOf(false) }
     var showConditionDialog by remember { mutableStateOf(false) }
          var showMaxHPDialog by remember { mutableStateOf(false) }
      var showNatureDescriptionDialog by remember { mutableStateOf(false) }
      var showFullRestDialog by remember { mutableStateOf(false) }
      var showExpDialog by remember { mutableStateOf(false) }
      
     // Local state for current HP and max HP to make UI reactive
     var currentHP by remember { mutableStateOf(partyPokemon.currentHP) }
     var maxHP by remember { mutableStateOf(partyPokemon.maxHP) }
     
     // Local state for current experience and level to make UI reactive
     var currentExp by remember { mutableStateOf(partyPokemon.currentExp) }
     var currentLevel by remember { mutableStateOf(partyPokemon.level) }
    
    // Local state for experience input
    var expInput by remember { mutableStateOf("") }
    
    // Local state for current Pokemon instance to track changes
    var currentPokemon by remember { mutableStateOf(partyPokemon) }
    
         // Local state for level change messages
     var levelChangeMessage by remember { mutableStateOf<String?>(null) }
     
     // Local state for evolution confirmation dialog
     var showEvolutionDialog by remember { mutableStateOf(false) }
     
     // Local state for evolution progress
     var isEvolutionInProgress by remember { mutableStateOf(false) }
    
         // Sync local HP state with PartyManager data when screen becomes active
     LaunchedEffect(Unit) {
         val updatedParty = partyManager.getParty().find { it.id == currentPokemon.id }
         updatedParty?.let { 
             currentHP = it.currentHP
             maxHP = it.maxHP
             currentExp = it.currentExp
             currentLevel = it.level
             currentPokemon = it
         }
     }
    
         // Sync local HP state whenever partyPokemon changes (e.g., when returning from other screens)
     LaunchedEffect(partyPokemon.id) {
         val updatedParty = partyManager.getParty().find { it.id == currentPokemon.id }
        updatedParty?.let { 
            currentHP = it.currentHP
            maxHP = it.maxHP
            currentExp = it.currentExp
            currentLevel = it.level
            currentPokemon = it
        }
    }
    
         // Show evolution progress screen if evolution is in progress
     if (isEvolutionInProgress) {
         Box(
             modifier = Modifier.fillMaxSize(),
             contentAlignment = Alignment.Center
         ) {
             Column(
                 horizontalAlignment = Alignment.CenterHorizontally,
                 verticalArrangement = Arrangement.spacedBy(24.dp)
             ) {
                 // Evolution animation placeholder (you can add a proper animation later)
                 Box(
                     modifier = Modifier
                         .size(120.dp)
                         .background(
                             color = Color(0xFF9C27B0).copy(alpha = 0.1f),
                             shape = RoundedCornerShape(60.dp)
                         )
                         .border(
                             width = 3.dp,
                             color = Color(0xFF9C27B0),
                             shape = RoundedCornerShape(60.dp)
                         ),
                     contentAlignment = Alignment.Center
                 ) {
                     Text(
                         text = "✨",
                         style = MaterialTheme.typography.displayMedium
                     )
                 }
                 
                 Text(
                     text = "Evolution in Progress...",
                     style = MaterialTheme.typography.headlineMedium,
                     fontWeight = FontWeight.Bold,
                     color = Color(0xFF9C27B0),
                     textAlign = TextAlign.Center
                 )
                 
                 Text(
                     text = "Your ${currentPokemon.name.replaceFirstChar { it.uppercase() }} is evolving!",
                     style = MaterialTheme.typography.bodyLarge,
                     color = Color(0xFF666666),
                     textAlign = TextAlign.Center
                 )
                 
                 CircularProgressIndicator(
                     color = Color(0xFF9C27B0),
                     modifier = Modifier.size(48.dp)
                 )
             }
         }
         return
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
                             text = currentPokemon.name.replaceFirstChar { it.uppercase() },
                             style = MaterialTheme.typography.headlineMedium,
                             fontWeight = FontWeight.Bold,
                             color = Color(0xFF1A1A1A),
                             modifier = Modifier.weight(1f)
                         )
             
             Text(
                 text = "Level $currentLevel",
                 style = MaterialTheme.typography.titleMedium,
                 fontWeight = FontWeight.Medium,
                 color = Color.Black,
                 modifier = Modifier.clickable { showExpDialog = true }
             )
        }
        
                 // Create DnD view for stats
         val dndConverter = remember { DnDConverter() }
         val dndView = remember(currentPokemon.name, currentPokemon.basePokemon.id) { 
             // Use the base Pokemon data from PartyPokemon for proper DnD conversion
             dndConverter.convertPokemonToDnD(currentPokemon.basePokemon)
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
                             .data("file:///android_asset/front_images/${currentPokemon.name}.png")
                             .crossfade(true)
                             .build(),
                         contentDescription = "Official artwork of ${currentPokemon.name}",
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
                                                 currentPokemon.basePokemon.types.forEach { type ->
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
                             text = "${currentPokemon.calculateCurrentArmorClass()}",
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
                                 currentPokemon.calculateCurrentInitiative() > 0 -> "+${currentPokemon.calculateCurrentInitiative()}"
                                 currentPokemon.calculateCurrentInitiative() < 0 -> "${currentPokemon.calculateCurrentInitiative()}"
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
                             text = "${currentPokemon.calculateCurrentMovementSpeed()}ft",
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
                                                                                 val result = partyManager.updatePartyPokemonHP(currentPokemon.id, newHP)
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
                                textAlign = TextAlign.Center,
                                modifier = Modifier.pointerInput(Unit) {
                                    detectTapGestures(
                                        onLongPress = { showFullRestDialog = true }
                                    )
                                }
                            )
                            
                                                         // Up arrow (increase HP)
                             IconButton(
                                 onClick = {
                                     if (currentHP < maxHP) {
                                         val newHP = currentHP + 1
                                         val result = partyManager.updatePartyPokemonHP(currentPokemon.id, newHP)
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
                            text = currentPokemon.nature.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.clickable { showNatureDescriptionDialog = true }
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
                                 text = if (currentPokemon.nature.increasedStat != null) 
                                     "${currentPokemon.nature.increasedStat} (+${currentPokemon.proficiency})" 
                                 else "None",
                                 style = MaterialTheme.typography.titleMedium,
                                 color = Color.Red,
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
                                                                 text = if (currentPokemon.nature.decreasedStat != null) 
                                     "${currentPokemon.nature.decreasedStat} (-2)" 
                                 else "None",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.Blue,
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
                             isProficient = false,
                             isDeficient = false,
                             modifier = Modifier.weight(1f)
                         )
                         
                         // Sp.Attack (Purple) - using DnD converter like Pokemon list view
                         StatGridItem(
                             label = "SP.ATTACK",
                             value = "${dndView.convertedStats["Sp.Atk"] ?: 0}",
                             statModifier = "${dndView.modifiers["Sp.Atk"] ?: 0}",
                             color = Color(0xFF9C27B0),
                             isProficient = false,
                             isDeficient = false,
                             modifier = Modifier.weight(1f)
                         )
                         
                         // Speed (Green/Blue) - using DnD converter like Pokemon list view
                         StatGridItem(
                             label = "SPEED",
                             value = "${dndView.convertedStats["Speed"] ?: 0}",
                             statModifier = "${dndView.modifiers["Speed"] ?: 0}",
                             color = Color(0xFF4CAF50),
                             isProficient = false,
                             isDeficient = false,
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
                             isProficient = false,
                             isDeficient = false,
                             modifier = Modifier.weight(1f)
                         )
                         
                         // Sp.Defense (Teal/Blue) - using DnD converter like Pokemon list view
                         StatGridItem(
                             label = "SP.DEFENSE",
                             value = "${dndView.convertedStats["Sp.Def"] ?: 0}",
                             statModifier = "${dndView.modifiers["Sp.Def"] ?: 0}",
                             color = Color(0xFF00BCD4),
                             isProficient = false,
                             isDeficient = false,
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
                                         val updatedPokemon = currentPokemon.copy(currentMoveSet = currentMoveSet.toList())
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
                         text = "Available Moves (Level ${currentPokemon.level})",
                         style = MaterialTheme.typography.titleMedium,
                         fontWeight = FontWeight.Bold
                     )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                                         if (currentPokemon.availableMoves.isNotEmpty()) {
                         currentPokemon.availableMoves.forEach { move ->
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
                                                 val updatedPokemon = currentPokemon.copy(currentMoveSet = currentMoveSet.toList())
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
                                                 val updatedPokemon = currentPokemon.copy(currentMoveSet = currentMoveSet.toList())
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
                                         val updatedPokemon = currentPokemon.copy(conditions = currentConditions.toList())
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
            
            // Weaknesses and Resistances Card
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
                            
                                                         if (currentPokemon.weaknesses.isNotEmpty()) {
                                 currentPokemon.weaknesses.forEach { type ->
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
                            
                                                         if (currentPokemon.resistances.isNotEmpty()) {
                                 currentPokemon.resistances.forEach { type ->
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
                            
                                                         val immunities = getImmunities(currentPokemon.basePokemon.types.map { it.type.name })
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
                                         val updatedPokemon = currentPokemon.copy(conditions = currentConditions.toList())
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
                                 val updatedPokemon = currentPokemon.copy(
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
        
        // Nature Description Dialog
        if (showNatureDescriptionDialog) {
            AlertDialog(
                onDismissRequest = { showNatureDescriptionDialog = false },
                title = { 
                                             Text(
                             text = currentPokemon.nature.name,
                             style = MaterialTheme.typography.titleLarge,
                             fontWeight = FontWeight.Bold,
                             color = MaterialTheme.colorScheme.primary
                         )
                },
                text = { 
                    Column {
                                                 Text(
                             text = currentPokemon.nature.description,
                             style = MaterialTheme.typography.bodyMedium,
                             color = Color(0xFF1A1A1A)
                         )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Show stat effects
                                                 if (currentPokemon.nature.increasedStat != null || currentPokemon.nature.decreasedStat != null) {
                            Text(
                                text = "Stat Effects:",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF666666),
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            
                                                         if (currentPokemon.nature.increasedStat != null) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = androidx.compose.material.icons.Icons.Default.KeyboardArrowUp,
                                        contentDescription = "Increased",
                                        tint = Color.Red,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text = "${currentPokemon.nature.increasedStat} (+${currentPokemon.proficiency})",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color.Red,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                            
                                                         if (currentPokemon.nature.decreasedStat != null) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.padding(top = 4.dp)
                                ) {
                                    Icon(
                                        imageVector = androidx.compose.material.icons.Icons.Default.KeyboardArrowDown,
                                        contentDescription = "Decreased",
                                        tint = Color.Blue,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text = "${currentPokemon.nature.decreasedStat} (-2)",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color.Blue,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        } else {
                            Text(
                                text = "This nature has no stat effects.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF666666),
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                            )
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showNatureDescriptionDialog = false }) {
                        Text("Close")
                    }
                }
            )
        }
        
        // Full Rest Confirmation Dialog
        if (showFullRestDialog) {
            AlertDialog(
                onDismissRequest = { showFullRestDialog = false },
                title = { 
                    Text(
                        text = "Execute Full Rest?",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                text = { 
                    Column {
                                                 Text(
                             text = "This will restore ${currentPokemon.name}'s HP to maximum (${maxHP}).",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF1A1A1A)
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = "Current HP: ${currentHP}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF666666)
                        )
                        
                        Text(
                            text = "Max HP: ${maxHP}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF666666)
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                                                         // Set current HP to max HP
                             val result = partyManager.updatePartyPokemonHP(currentPokemon.id, maxHP)
                            if (result.isSuccess) {
                                currentHP = maxHP
                            }
                            showFullRestDialog = false
                        }
                    ) {
                        Text("Rest")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showFullRestDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
        
        // Experience Dialog
        if (showExpDialog) {
            AlertDialog(
                onDismissRequest = { 
                    showExpDialog = false
                    levelChangeMessage = null
                },
                title = { 
                    Text(
                        text = "Experience Management",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                text = { 
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Level
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Level:",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "$currentLevel",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        
                        // Current EXP
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Current EXP:",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "$currentExp",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        
                        // Next level EXP
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Next level:",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                                                 text = if (currentLevel < 20) {
                                     "${currentPokemon.getExpRequiredForLevel(currentLevel + 1)} EXP"
                                 } else {
                                    "Max level reached"
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (currentLevel < 20) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Experience Input Section
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Add/Remove Experience:",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = expInput,
                                    onValueChange = { expInput = it },
                                    label = { Text("EXP Amount") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )
                                
                                Button(
                                    onClick = {
                                        val expAmount = expInput.toIntOrNull() ?: 0
                                        if (expAmount != 0) {
                                            val (message, updatedPokemon) = currentPokemon.gainExp(expAmount)
                                            
                                            // Update the party with the new Pokemon instance
                                            partyManager.updatePartyPokemon(updatedPokemon)
                                            
                                            // Update local state to reflect changes immediately
                                            currentExp = updatedPokemon.currentExp
                                            currentLevel = updatedPokemon.level
                                            currentPokemon = updatedPokemon
                                            
                                            // Capture the level change message
                                            levelChangeMessage = message
                                            
                                            // Clear input
                                            expInput = ""
                                        }
                                    },
                                    enabled = expInput.isNotEmpty() && expInput.toIntOrNull() != null
                                ) {
                                    Text("Gain")
                                }
                            }
                            
                            Text(
                                text = "Enter positive numbers to gain EXP, negative numbers to lose EXP",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            
                            // Display level change messages
                            if (levelChangeMessage != null) {
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(
                                            color = when {
                                                levelChangeMessage!!.contains("Level Up") -> Color(0xFF4CAF50)
                                                levelChangeMessage!!.contains("Level Down") -> Color(0xFFFF9800)
                                                levelChangeMessage!!.contains("Reached evolution") -> Color(0xFF9C27B0)
                                                else -> Color(0xFF2196F3)
                                            }.copy(alpha = 0.1f),
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .border(
                                            width = 1.dp,
                                            color = when {
                                                levelChangeMessage!!.contains("Level Up") -> Color(0xFF4CAF50)
                                                levelChangeMessage!!.contains("Level Down") -> Color(0xFFFF9800)
                                                levelChangeMessage!!.contains("Reached evolution") -> Color(0xFF9C27B0)
                                                else -> Color(0xFF2196F3)
                                            },
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .padding(12.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = levelChangeMessage!!,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = when {
                                                levelChangeMessage!!.contains("Level Up") -> Color(0xFF4CAF50)
                                                levelChangeMessage!!.contains("Level Down") -> Color(0xFFFF9800)
                                                levelChangeMessage!!.contains("Reached evolution") -> Color(0xFF9C27B0)
                                                else -> Color(0xFF2196F3)
                                            },
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                        
                                                                                 // Automatically show evolution dialog when evolution is available
                                         if (levelChangeMessage!!.contains("Reached evolution")) {
                                             LaunchedEffect(Unit) {
                                                 showEvolutionDialog = true
                                                 // Close the experience dialog since we're showing evolution directly
                                                 showExpDialog = false
                                             }
                                         }
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { 
                        showExpDialog = false
                        levelChangeMessage = null
                    }) {
                        Text("Close")
                    }
                                 }
             )
         }
         
         // Evolution Confirmation Dialog
         if (showEvolutionDialog) {
             AlertDialog(
                 onDismissRequest = { showEvolutionDialog = false },
                 title = { 
                     Text(
                         text = "Evolution Confirmation",
                         style = MaterialTheme.typography.titleLarge,
                         fontWeight = FontWeight.Bold,
                         color = Color(0xFF9C27B0)
                     )
                 },
                 text = { 
                     Text(
                         text = "Pokemon reached evolution!",
                         style = MaterialTheme.typography.bodyLarge,
                         color = Color(0xFF1A1A1A),
                         textAlign = TextAlign.Center
                     )
                 },
                                   confirmButton = {
                      Button(
                          onClick = {
                              // Start evolution process
                              isEvolutionInProgress = true
                              showEvolutionDialog = false
                              showExpDialog = false
                              levelChangeMessage = null
                              
                              // Execute evolution in background
                              coroutineScope.launch {
                                  val result = partyManager.executeEvolution(currentPokemon.id)
                                  
                                  // Switch back to main thread to update UI
                                  withContext(Dispatchers.Main) {
                                      if (result.isSuccess) {
                                          val evolvedPokemon = result.getOrNull()
                                          if (evolvedPokemon != null) {
                                              // Update local state with evolved Pokemon
                                              currentPokemon = evolvedPokemon
                                              currentHP = evolvedPokemon.currentHP
                                              maxHP = evolvedPokemon.maxHP
                                              currentExp = evolvedPokemon.currentExp
                                              currentLevel = evolvedPokemon.level
                                              currentMoveSet = evolvedPokemon.currentMoveSet.toMutableList()
                                              currentConditions = evolvedPokemon.conditions.toMutableList()
                                          }
                                      } else {
                                          // Evolution failed - show error and return to normal view
                                          val error = result.exceptionOrNull()?.message ?: "Evolution failed"
                                          android.util.Log.e("Evolution", "Failed: $error")
                                      }
                                      
                                      // Hide evolution progress screen
                                      isEvolutionInProgress = false
                                  }
                              }
                          },
                          colors = ButtonDefaults.buttonColors(
                              containerColor = Color(0xFF9C27B0)
                          )
                      ) {
                          Text("Confirm Evolution")
                      }
                  },
                 dismissButton = {
                     TextButton(onClick = { showEvolutionDialog = false }) {
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
    isProficient: Boolean = false,
    isDeficient: Boolean = false,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Label with arrow indicator
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Show up arrow for proficient stats
            if (isProficient) {
                Icon(
                    imageVector = androidx.compose.material.icons.Icons.Default.KeyboardArrowUp,
                    contentDescription = "Proficient",
                    tint = Color.Red,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
            
            // Show down arrow for deficient stats
            if (isDeficient) {
                Icon(
                    imageVector = androidx.compose.material.icons.Icons.Default.KeyboardArrowDown,
                    contentDescription = "Deficient",
                    tint = Color.Blue,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
            
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = when {
                    isProficient -> Color.Red
                    isDeficient -> Color.Blue
                    else -> Color(0xFF666666)
                },
                textAlign = TextAlign.Center
            )
        }
        
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
 * Get single type immunities - returns attacking types that have 0x effectiveness
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
