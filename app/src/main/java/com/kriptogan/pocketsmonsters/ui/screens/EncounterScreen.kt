package com.kriptogan.pocketsmonsters.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.random.Random
import kotlinx.coroutines.delay
import com.kriptogan.pocketsmonsters.data.encounter.EncounterManager
import com.kriptogan.pocketsmonsters.data.models.EncounterCreature
import com.kriptogan.pocketsmonsters.data.models.Pokemon
import com.kriptogan.pocketsmonsters.data.network.NetworkModule
import com.kriptogan.pocketsmonsters.ui.components.EncounterRow
import com.kriptogan.pocketsmonsters.ui.components.PokemonDetailScreen
import kotlinx.coroutines.launch

@Composable
fun EncounterScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val encounterManager = remember { EncounterManager(context) }
    val creatures by encounterManager.creatures.collectAsState()
    val focusedCreatureId by encounterManager.focusedCreatureId.collectAsState()
    
    // Pokemon details state
    var selectedPokemon by remember { mutableStateOf<Pokemon?>(null) }
    val repository = remember { NetworkModule.createPokemonRepository(context) }
    val coroutineScope = rememberCoroutineScope()
    
    // Dice rolling state
    var currentRoll by remember { mutableStateOf<Int?>(null) }
    var isRolling by remember { mutableStateOf(false) }
    var currentDiceType by remember { mutableStateOf<String?>(null) }
    
    // Computed list - always sorted by initiative (descending)
    val sortedCreatures = remember(creatures) {
        creatures.sortedByDescending { creature ->
            creature.initiative.toIntOrNull() ?: 0
        }
    }
    
    // Function to load Pokemon by ID
    fun loadPokemonById(pokemonId: Int) {
        coroutineScope.launch {
            try {
                val result = repository.getPokemonById(pokemonId)
                result.fold(
                    onSuccess = { pokemon ->
                        selectedPokemon = pokemon
                    },
                    onFailure = { exception ->
                        // Handle error - could show a toast or error message
                        println("Failed to load Pokemon: ${exception.message}")
                    }
                )
            } catch (e: Exception) {
                println("Error loading Pokemon: ${e.message}")
            }
        }
    }
    
    // Function to close Pokemon details
    fun closePokemonDetails() {
        selectedPokemon = null
    }
    
    // Function to roll dice
    fun rollDice(diceType: String, sides: Int) {
        if (!isRolling) {
            isRolling = true
            currentDiceType = diceType
            
            coroutineScope.launch {
                // Simulate rolling animation
                repeat(10) {
                    val randomResult = Random.nextInt(1, sides + 1)
                    currentRoll = randomResult
                    delay(100)
                }
                
                // Final result
                val finalResult = Random.nextInt(1, sides + 1)
                currentRoll = finalResult
                
                // Reset rolling state
                isRolling = false
            }
        }
    }
    
    if (selectedPokemon != null) {
        // Show Pokemon detail screen
        PokemonDetailScreen(
            pokemon = selectedPokemon,
            onBackClick = { closePokemonDetails() },
            modifier = modifier
        )
    } else {
        // Show encounter table
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
        // Header
        Text(
            text = "Encounter",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Table header
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Remove column header (for delete button)
                Text(
                    text = "",
                    modifier = Modifier.weight(0.3f),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                
                // Info column header (for information icon)
                Text(
                    text = "",
                    modifier = Modifier.width(15.dp),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                
                // Name column
                Text(
                    text = "Name",
                    modifier = Modifier.weight(2f),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                
                // Initiative column (always sorted descending)
                Text(
                    text = "Init ↓",
                    modifier = Modifier.width(45.dp),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4CAF50)
                )
                
                // AC column
                Text(
                    text = "AC",
                    modifier = Modifier.width(45.dp),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                
                // Max HP column
                Text(
                    text = "m.HP",
                    modifier = Modifier.width(45.dp),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                
                // Current HP column
                Text(
                    text = "HP",
                    modifier = Modifier.width(45.dp),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Creatures list
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(sortedCreatures) { creature ->
                EncounterRow(
                    creature = creature,
                    onUpdate = { encounterManager.updateCreature(it) },
                    onRemove = { encounterManager.removeCreature(it) },
                    onInfoClick = { loadPokemonById(creature.pokemonId) },
                    onFocusChange = { isFocused ->
                        if (isFocused) {
                            encounterManager.setFocusedCreature(creature.id)
                        }
                        // Don't clear focus when unfocusing - only when another row gets focus
                    },
                    isFocused = focusedCreatureId == creature.id
                )
            }
        }
        
            // Dice buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // d4 button
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .border(
                            width = 1.dp,
                            color = Color.Gray,
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp)
                        )
                        .clickable(enabled = !isRolling) { rollDice("d4", 4) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "d4",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 14.sp),
                        fontWeight = FontWeight.Bold,
                        color = if (isRolling) Color.Gray else Color.Black
                    )
                }
                
                // d6 button
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .border(
                            width = 1.dp,
                            color = Color.Gray,
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp)
                        )
                        .clickable(enabled = !isRolling) { rollDice("d6", 6) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "d6",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 14.sp),
                        fontWeight = FontWeight.Bold,
                        color = if (isRolling) Color.Gray else Color.Black
                    )
                }
                
                // d8 button
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .border(
                            width = 1.dp,
                            color = Color.Gray,
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp)
                        )
                        .clickable(enabled = !isRolling) { rollDice("d8", 8) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "d8",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 14.sp),
                        fontWeight = FontWeight.Bold,
                        color = if (isRolling) Color.Gray else Color.Black
                    )
                }
                
                // d10 button
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .border(
                            width = 1.dp,
                            color = Color.Gray,
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp)
                        )
                        .clickable(enabled = !isRolling) { rollDice("d10", 10) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "d10",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 14.sp),
                        fontWeight = FontWeight.Bold,
                        color = if (isRolling) Color.Gray else Color.Black
                    )
                }
                
                // d12 button
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .border(
                            width = 1.dp,
                            color = Color.Gray,
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp)
                        )
                        .clickable(enabled = !isRolling) { rollDice("d12", 12) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "d12",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 14.sp),
                        fontWeight = FontWeight.Bold,
                        color = if (isRolling) Color.Gray else Color.Black
                    )
                }
                
                // d20 button
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .border(
                            width = 1.dp,
                            color = Color.Gray,
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp)
                        )
                        .clickable(enabled = !isRolling) { rollDice("d20", 20) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "d20",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 14.sp),
                        fontWeight = FontWeight.Bold,
                        color = if (isRolling) Color.Gray else Color.Black
                    )
                }
                
                // X button (shows result)
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .border(
                            width = 1.dp,
                            color = Color.Gray,
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isRolling) {
                        Text(
                            text = "?",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 14.sp),
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray
                        )
                    } else if (currentRoll != null) {
                        Text(
                            text = currentRoll.toString(),
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 14.sp),
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFD32F2F)
                        )
                    } else {
                        Text(
                            text = "X",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 14.sp),
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }
                }
            }
        }
    }
}
