package com.kriptogan.pocketsmonsters.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import androidx.compose.ui.graphics.Color
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.kriptogan.pocketsmonsters.data.converter.DnDView
import com.kriptogan.pocketsmonsters.data.party.PartyManager
import kotlin.math.floor

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

    val context = LocalContext.current
    val partyManager = remember { PartyManager(context) }
    
    var showPartyFullAlert by remember { mutableStateOf(false) }
    var showAlreadyInPartyAlert by remember { mutableStateOf(false) }
    var showSuccessAlert by remember { mutableStateOf(false) }
    var isInParty by remember { mutableStateOf(partyManager.isInParty(dndView.pokemon.id)) }
    var isPartyFull by remember { mutableStateOf(partyManager.isPartyFull()) }
    
    // Refresh party status when Pokemon changes
    LaunchedEffect(dndView.pokemon.id) {
        isInParty = partyManager.isInParty(dndView.pokemon.id)
        isPartyFull = partyManager.isPartyFull()
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
                    // Use high-quality front images (official artwork) from assets
                    val frontImagePath = "file:///android_asset/front_images/${dndView.pokemon.name}.png"
                    
                    // Debug logging
                    LaunchedEffect(dndView.pokemon.name) {
                        println("🖼️ DEBUG: DnD View loading front image: $frontImagePath")
                    }
                    
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(frontImagePath)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Official artwork of ${dndView.pokemon.name}",
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
                             dndView.pokemon.types.forEach { typeSlot ->
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
                    // HP should show adjusted value (divided by 3) as per new rules
                    StatRow(
                        statName = "HP",
                        originalValue = dndView.pokemon.stats.find { it.stat.name == "hp" }?.baseStat ?: 0,
                        convertedValue = floor((dndView.pokemon.stats.find { it.stat.name == "hp" }?.baseStat ?: 0) / 3.0).toInt(), // Adjusted HP = floor(Base HP ÷ 3)
                        modifier = null, // No modifier for HP
                        showOriginalInBrackets = true // Show original value in brackets
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
            
            // Moves by D&D Level Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Moves by D&D Level",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    if (dndView.movesByDnDLevel.isNotEmpty()) {
                        dndView.movesByDnDLevel.entries.sortedBy { it.key }.forEach { (dndLevel, moves) ->
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                            ) {
                                Text(
                                    text = "D&D Level $dndLevel",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = moves.sorted().joinToString(", ") { it.replaceFirstChar { char -> char.uppercase() } },
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (dndLevel != dndView.movesByDnDLevel.keys.maxOrNull()) {
                                Divider(
                                    modifier = Modifier.padding(vertical = 4.dp),
                                    color = MaterialTheme.colorScheme.outlineVariant
                                )
                            }
                        }
                    } else {
                        Text(
                            text = "No moves available",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Add to Party Button
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Party Management",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    when {
                        isInParty -> {
                            // Pokemon is already in party
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Already in Party!",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "This Pokemon is already part of your team",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                        isPartyFull -> {
                            // Party is full
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Party Full!",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error
                            )
                            Text(
                                text = "Release a monster to make room",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                        else -> {
                            // Can add to party
                            Button(
                                onClick = {
                                    val result = partyManager.addToParty(dndView.pokemon)
                                    result.fold(
                                        onSuccess = {
                                            isInParty = true
                                            isPartyFull = partyManager.isPartyFull()
                                            showSuccessAlert = true
                                        },
                                        onFailure = { exception ->
                                            when (exception.message) {
                                                "Party is full" -> showPartyFullAlert = true
                                                "Pokemon already in party" -> showAlreadyInPartyAlert = true
                                                else -> showPartyFullAlert = true
                                            }
                                        }
                                    )
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Add to Party")
                            }
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Text(
                                text = "Add this Pokemon to your party (${partyManager.getPartySize()}/6)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
    
    // Alerts
    if (showPartyFullAlert) {
        AlertDialog(
            onDismissRequest = { showPartyFullAlert = false },
            title = { Text("Party Full") },
            text = { Text("You have reached maximum capacity. Release a monster to make room.") },
            confirmButton = {
                TextButton(onClick = { showPartyFullAlert = false }) {
                    Text("OK")
                }
            }
        )
    }
    
    if (showAlreadyInPartyAlert) {
        AlertDialog(
            onDismissRequest = { showAlreadyInPartyAlert = false },
            title = { Text("Already in Party") },
            text = { Text("This Pokemon is already part of your team!") },
            confirmButton = {
                TextButton(onClick = { showAlreadyInPartyAlert = false }) {
                    Text("OK")
                }
            }
        )
    }
    
    if (showSuccessAlert) {
        AlertDialog(
            onDismissRequest = { showSuccessAlert = false },
            title = { Text("Added to Party!") },
            text = { Text("${dndView.pokemon.name.replaceFirstChar { it.uppercase() }} has been successfully added to your party!") },
            confirmButton = {
                TextButton(onClick = { showSuccessAlert = false }) {
                    Text("OK")
                }
            }
        )
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


