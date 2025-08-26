package com.kriptogan.pocketsmonsters.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
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
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.kriptogan.pocketsmonsters.data.models.PartyPokemon
import com.kriptogan.pocketsmonsters.data.party.PartyManager
import com.kriptogan.pocketsmonsters.ui.components.PartyPokemonDetailScreen

@Composable
fun MyPartyScreen(
    modifier: Modifier = Modifier,
    onPokemonClick: (PartyPokemon) -> Unit = {}
) {
    val context = LocalContext.current
    val partyManager = remember { PartyManager(context) }
    
    var party by remember { mutableStateOf(partyManager.getParty()) }
    var showAddPokemonDialog by remember { mutableStateOf(false) }
    var selectedPokemon by remember { mutableStateOf<PartyPokemon?>(null) }
    
    LaunchedEffect(Unit) {
        // Refresh party data when screen is shown
        party = partyManager.getParty()
    }
    
    // If a Pokemon is selected, show its detail screen
    if (selectedPokemon != null) {
        PartyPokemonDetailScreen(
            partyPokemon = selectedPokemon,
            onBackClick = { selectedPokemon = null }
        )
        return
    }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .padding(top = 20.dp)
    ) {
        // Header
        Text(
            text = "My Party",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "${party.size}/6 Pokemon",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Party Grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            // Existing party Pokemon
            items(party) { partyPokemon ->
                PartyPokemonCard(
                    partyPokemon = partyPokemon,
                    onPokemonClick = { selectedPokemon = partyPokemon },
                    onRemoveClick = {
                        partyManager.removeFromParty(partyPokemon.id)
                        party = partyManager.getParty()
                    }
                )
            }
            
            // Empty slots
            items((0 until (6 - party.size)).toList()) { index ->
                EmptyPartySlot(
                    onClick = { showAddPokemonDialog = true }
                )
            }
        }
        
        // Add Pokemon Button (if party not full)
        if (party.size < 6) {
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = { showAddPokemonDialog = true },
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
                Text("Add Pokemon to Party")
            }
        }
    }
    
    // Add Pokemon Dialog
    if (showAddPokemonDialog) {
        AlertDialog(
            onDismissRequest = { showAddPokemonDialog = false },
            title = { Text("Add Pokemon to Party") },
            text = { Text("Navigate to a Pokemon's details page and use the 'Add to Party' button to add them to your party.") },
            confirmButton = {
                TextButton(onClick = { showAddPokemonDialog = false }) {
                    Text("OK")
                }
            }
        )
    }
}

@Composable
private fun PartyPokemonCard(
    partyPokemon: PartyPokemon,
    onPokemonClick: () -> Unit,
    onRemoveClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .clickable { onPokemonClick() },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        border = BorderStroke(
            width = 2.dp,
            color = MaterialTheme.colorScheme.outline
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            // Remove button (top-right corner)
            IconButton(
                onClick = onRemoveClick,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(32.dp)
                    .background(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(16.dp)
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Remove from party",
                    tint = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.size(16.dp)
                )
            }
            
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Pokemon Image
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data("file:///android_asset/front_images/${partyPokemon.name}.png")
                        .crossfade(true)
                        .build(),
                    contentDescription = "Official artwork of ${partyPokemon.name}",
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Fit
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Pokemon Name
                Text(
                    text = partyPokemon.name.replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                
                // Level
                Text(
                    text = "Level ${partyPokemon.currentLevel}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                // HP Bar
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { partyPokemon.currentHP.toFloat() / partyPokemon.maxHP.toFloat() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
                
                Text(
                    text = "${partyPokemon.currentHP}/${partyPokemon.maxHP}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                // Conditions (if any)
                if (partyPokemon.conditions.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = partyPokemon.conditions.first().displayName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyPartySlot(
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        border = BorderStroke(
            width = 2.dp,
            color = MaterialTheme.colorScheme.outline
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Pokemon",
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Empty Slot",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
