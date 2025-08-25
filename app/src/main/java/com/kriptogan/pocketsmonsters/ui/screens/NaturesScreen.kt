package com.kriptogan.pocketsmonsters.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun NaturesScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Handle system back button
    BackHandler {
        onBackClick()
    }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(top = 20.dp)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back"
                )
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Text(
                text = "Natures",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Description
        Text(
            text = "Personality traits that affect Pokémon stats in battle",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Natures list
        NaturesList()
    }
}

@Composable
private fun NaturesList() {
    val natures = listOf(
        Nature("Hardy", null, null, "Neutral nature"),
        Nature("Lonely", "Attack", "Defense", "Loves to eat"),
        Nature("Brave", "Attack", "Speed", "Often dozes off"),
        Nature("Adamant", "Attack", "Sp. Atk", "Sturdy body"),
        Nature("Naughty", "Attack", "Sp. Def", "Likes to fight"),
        Nature("Bold", "Defense", "Attack", "Proud of its power"),
        Nature("Docile", null, null, "Sturdy body"),
        Nature("Relaxed", "Defense", "Speed", "Likes to relax"),
        Nature("Impish", "Defense", "Sp. Atk", "Proud of its power"),
        Nature("Lax", "Defense", "Sp. Def", "Loves to eat"),
        Nature("Timid", "Speed", "Attack", "Likes to run"),
        Nature("Hasty", "Speed", "Defense", "Somewhat of a clown"),
        Nature("Serious", null, null, "Strong willed"),
        Nature("Jolly", "Speed", "Sp. Atk", "Good perseverance"),
        Nature("Naive", "Speed", "Sp. Def", "Likes to thrash about"),
        Nature("Modest", "Sp. Atk", "Attack", "Loves to eat"),
        Nature("Mild", "Sp. Atk", "Defense", "Proud of its power"),
        Nature("Quiet", "Sp. Atk", "Speed", "Sturdy body"),
        Nature("Bashful", null, null, "Somewhat stubborn"),
        Nature("Rash", "Sp. Atk", "Sp. Def", "Likes to run"),
        Nature("Calm", "Sp. Def", "Attack", "Strong willed"),
        Nature("Gentle", "Sp. Def", "Defense", "Loves to eat"),
        Nature("Careful", "Sp. Def", "Sp. Atk", "Often lost in thought"),
        Nature("Quirky", null, null, "Mischievous"),
        Nature("Sassy", "Sp. Def", "Speed", "Somewhat vain")
    )
    
    Column(
        modifier = Modifier.padding(horizontal = 16.dp)
    ) {
        natures.forEach { nature ->
            NatureCard(nature = nature)
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun NatureCard(nature: Nature) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (nature.increasedStat != null || nature.decreasedStat != null) 
                MaterialTheme.colorScheme.primaryContainer 
            else 
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = nature.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                if (nature.increasedStat != null && nature.decreasedStat != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowUp,
                            contentDescription = "Increased",
                            tint = Color.Green,
                            modifier = Modifier.size(16.dp)
                        )
                        
                        Spacer(modifier = Modifier.width(4.dp))
                        
                        Text(
                            text = nature.increasedStat,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Green,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "Decreased",
                            tint = Color.Red,
                            modifier = Modifier.size(16.dp)
                        )
                        
                        Spacer(modifier = Modifier.width(4.dp))
                        
                        Text(
                            text = nature.decreasedStat,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Red,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    Text(
                        text = "Neutral nature",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = nature.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
            }
        }
    }
}

// Legend card
@Composable
private fun NatureLegend() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "How Natures Work",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "• Natures increase one stat by 10% and decrease another by 10%",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "• Neutral natures have no stat changes",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "• The increased stat is shown in green, decreased in red",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "• Natures also affect which flavors of Pokéblocks/Poffins a Pokémon likes/dislikes",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

data class Nature(
    val name: String,
    val increasedStat: String?,
    val decreasedStat: String?,
    val description: String
)
