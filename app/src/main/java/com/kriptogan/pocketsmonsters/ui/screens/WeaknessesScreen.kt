package com.kriptogan.pocketsmonsters.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun WeaknessesScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
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
                text = "Weaknesses & Resistance",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Description
        Text(
            text = "Type effectiveness chart showing how each type performs against others",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Type effectiveness table
        TypeEffectivenessTable()
    }
}

@Composable
private fun TypeEffectivenessTable() {
    val types = listOf(
        "Normal", "Fire", "Water", "Electric", "Grass", "Ice",
        "Fighting", "Poison", "Ground", "Flying", "Psychic", "Bug",
        "Rock", "Ghost", "Dragon", "Dark", "Steel", "Fairy"
    )
    
    Column(
        modifier = Modifier.padding(horizontal = 16.dp)
    ) {
        types.forEach { type ->
            TypeCard(type = type)
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun TypeCard(type: String) {
    val weaknesses = getWeaknesses(type)
    val resistances = getResistances(type)
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Type header with color
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(getTypeColor(type)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = type.take(1),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Text(
                    text = type,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Weaknesses section
            if (weaknesses.isNotEmpty()) {
                Text(
                    text = "Weaknesses (2× damage)",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.Red
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(weaknesses) { weaknessType ->
                        TypeChip(
                            type = weaknessType,
                            backgroundColor = Color.Red.copy(alpha = 0.2f),
                            textColor = Color.Red
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // Resistances section
            if (resistances.isNotEmpty()) {
                Text(
                    text = "Resistances (½× damage)",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.Green
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(resistances) { resistanceType ->
                        TypeChip(
                            type = resistanceType,
                            backgroundColor = Color.Green.copy(alpha = 0.2f),
                            textColor = Color.Green
                        )
                    }
                }
            }
            
            // No effect section (if any)
            val noEffect = getNoEffect(type)
            if (noEffect.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "No Effect (0× damage)",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(noEffect) { noEffectType ->
                        TypeChip(
                            type = noEffectType,
                            backgroundColor = Color.Gray.copy(alpha = 0.2f),
                            textColor = Color.Gray
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TypeChip(
    type: String,
    backgroundColor: Color,
    textColor: Color
) {
    Card(
        modifier = Modifier.height(32.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        )
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = type,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        }
    }
}

@Composable
private fun LegendItem(
    symbol: String,
    color: Color,
    description: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .background(color),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = symbol,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = if (color == Color.Transparent) Color.Gray else Color.White
            )
        }
        
        Spacer(modifier = Modifier.width(4.dp))
        
        Text(
            text = description,
            style = MaterialTheme.typography.labelSmall
        )
    }
}

@Composable
private fun getTypeColor(type: String): Color {
    return when (type.lowercase()) {
        "normal" -> Color(0.66f, 0.66f, 0.47f)
        "fire" -> Color(0.93f, 0.51f, 0.18f)
        "water" -> Color(0.39f, 0.56f, 0.94f)
        "electric" -> Color(0.97f, 0.82f, 0.18f)
        "grass" -> Color(0.48f, 0.78f, 0.30f)
        "ice" -> Color(0.59f, 0.85f, 0.85f)
        "fighting" -> Color(0.76f, 0.35f, 0.24f)
        "poison" -> Color(0.64f, 0.24f, 0.63f)
        "ground" -> Color(0.89f, 0.75f, 0.40f)
        "flying" -> Color(0.66f, 0.56f, 0.95f)
        "psychic" -> Color(0.98f, 0.33f, 0.56f)
        "bug" -> Color(0.65f, 0.73f, 0.10f)
        "rock" -> Color(0.71f, 0.65f, 0.21f)
        "ghost" -> Color(0.45f, 0.34f, 0.59f)
        "dragon" -> Color(0.44f, 0.21f, 0.95f)
        "dark" -> Color(0.44f, 0.34f, 0.27f)
        "steel" -> Color(0.72f, 0.72f, 0.81f)
        "fairy" -> Color(0.84f, 0.52f, 0.68f)
        else -> MaterialTheme.colorScheme.primary
    }
}

private fun getWeaknesses(type: String): List<String> {
    return when (type.lowercase()) {
        "normal" -> listOf("Fighting")
        "fire" -> listOf("Water", "Ground", "Rock")
        "water" -> listOf("Electric", "Grass")
        "electric" -> listOf("Ground")
        "grass" -> listOf("Fire", "Ice", "Poison", "Flying", "Bug")
        "ice" -> listOf("Fire", "Fighting", "Rock", "Steel")
        "fighting" -> listOf("Flying", "Psychic", "Fairy")
        "poison" -> listOf("Ground", "Psychic")
        "ground" -> listOf("Water", "Grass", "Ice")
        "flying" -> listOf("Electric", "Ice", "Rock")
        "psychic" -> listOf("Bug", "Ghost", "Dark")
        "bug" -> listOf("Fire", "Flying", "Rock")
        "rock" -> listOf("Water", "Grass", "Fighting", "Ground", "Steel")
        "ghost" -> listOf("Ghost", "Dark")
        "dragon" -> listOf("Ice", "Dragon", "Fairy")
        "dark" -> listOf("Fighting", "Bug", "Fairy")
        "steel" -> listOf("Fire", "Fighting", "Ground")
        "fairy" -> listOf("Poison", "Steel")
        else -> emptyList()
    }
}

private fun getResistances(type: String): List<String> {
    return when (type.lowercase()) {
        "normal" -> emptyList()
        "fire" -> listOf("Fire", "Grass", "Ice", "Bug", "Steel")
        "water" -> listOf("Fire", "Water", "Ice", "Steel")
        "electric" -> listOf("Electric", "Flying", "Steel")
        "grass" -> listOf("Water", "Electric", "Grass", "Ground")
        "ice" -> listOf("Ice")
        "fighting" -> listOf("Rock", "Bug", "Dark")
        "poison" -> listOf("Grass", "Fighting", "Poison", "Bug", "Fairy")
        "ground" -> listOf("Poison", "Rock")
        "flying" -> listOf("Grass", "Fighting", "Bug")
        "psychic" -> listOf("Fighting", "Psychic")
        "bug" -> listOf("Grass", "Fighting", "Ground")
        "rock" -> listOf("Normal", "Fire", "Poison", "Flying")
        "ghost" -> listOf("Poison", "Bug")
        "dragon" -> listOf("Fire", "Water", "Electric", "Grass")
        "dark" -> listOf("Ghost", "Dark")
        "steel" -> listOf("Normal", "Grass", "Ice", "Flying", "Psychic", "Bug", "Rock", "Dragon", "Steel", "Fairy")
        "fairy" -> listOf("Fighting", "Bug", "Dark")
        else -> emptyList()
    }
}

private fun getNoEffect(type: String): List<String> {
    return when (type.lowercase()) {
        "normal" -> listOf("Ghost")
        "electric" -> listOf("Ground")
        "fighting" -> listOf("Ghost")
        "poison" -> listOf("Steel")
        "ground" -> listOf("Flying")
        "psychic" -> listOf("Dark")
        "ghost" -> listOf("Normal")
        "dragon" -> listOf("Fairy")
        else -> emptyList()
    }
}
