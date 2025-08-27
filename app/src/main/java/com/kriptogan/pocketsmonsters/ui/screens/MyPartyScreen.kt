package com.kriptogan.pocketsmonsters.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.sp
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
            color = Color(0xFF666666),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Party Grid - 2 rows, 3 Pokemon per row
        LazyVerticalGrid(
            columns = GridCells.Fixed(3), // 3 columns for 2x3 grid
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            // Existing party Pokemon
            items(party) { partyPokemon ->
                PartyPokemonGridCard(
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
                EmptyPartySlotGrid()
            }
        }
    }
}

@Composable
private fun PartyPokemonGridCard(
    partyPokemon: PartyPokemon,
    onPokemonClick: () -> Unit,
    onRemoveClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.8f) // Vertical rectangle like Pokemon grid
            .clip(RoundedCornerShape(16.dp))
            .background(Color.Transparent) // Fully transparent background
            .border(
                width = 2.dp,
                color = Color.White, // White border
                shape = RoundedCornerShape(16.dp)
            )
            .clickable { onPokemonClick() }
    ) {
        // Remove button (top-right corner)
        IconButton(
            onClick = onRemoveClick,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(28.dp)
                .background(
                    color = Color(0xFFD32F2F).copy(alpha = 0.9f), // Pokedex red
                    shape = RoundedCornerShape(14.dp)
                )
        ) {
            Text(
                text = "×",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Pokemon Image
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data("file:///android_asset/front_images/${partyPokemon.name}.png")
                    .crossfade(true)
                    .build(),
                contentDescription = "Official artwork of ${partyPokemon.name}",
                modifier = Modifier
                    .size(64.dp) // Same size as Pokemon grid
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Fit
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Level
            Text(
                text = "Lv.${partyPokemon.currentLevel}",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A1A),
                textAlign = TextAlign.Center,
                fontSize = 14.sp
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // HP Bar
            LinearProgressIndicator(
                progress = { partyPokemon.currentHP.toFloat() / partyPokemon.maxHP.toFloat() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp),
                color = Color(0xFF4CAF50), // Green for HP
                trackColor = Color(0xFFE0E0E0) // Light gray track
            )
            
            // HP Text
            Text(
                text = "${partyPokemon.currentHP}/${partyPokemon.maxHP}",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF666666),
                textAlign = TextAlign.Center,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun EmptyPartySlotGrid() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.8f) // Same aspect ratio as Pokemon cards
            .clip(RoundedCornerShape(16.dp))
            .background(Color.Transparent) // Fully transparent background
            .border(
                width = 2.dp,
                color = Color.White.copy(alpha = 0.5f), // Semi-transparent white border
                shape = RoundedCornerShape(16.dp)
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Empty",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF999999),
                textAlign = TextAlign.Center,
                fontSize = 14.sp
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = "Slot",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF999999),
                textAlign = TextAlign.Center,
                fontSize = 12.sp
            )
        }
    }
}
