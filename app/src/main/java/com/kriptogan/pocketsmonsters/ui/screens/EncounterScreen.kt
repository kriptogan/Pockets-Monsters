package com.kriptogan.pocketsmonsters.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
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
    
    // Pokemon details state
    var selectedPokemon by remember { mutableStateOf<Pokemon?>(null) }
    val repository = remember { NetworkModule.createPokemonRepository(context) }
    val coroutineScope = rememberCoroutineScope()
    
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
                
                // Initiative column
                Text(
                    text = "Init",
                    modifier = Modifier.width(45.dp),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
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
            items(creatures) { creature ->
                EncounterRow(
                    creature = creature,
                    onUpdate = { encounterManager.updateCreature(it) },
                    onRemove = { encounterManager.removeCreature(it) },
                    onInfoClick = { loadPokemonById(creature.pokemonId) }
                )
            }
            
            // Add button row
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Button(
                        onClick = { encounterManager.addCreature() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Red
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Creature"
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add Creature")
                    }
                }
            }
        }
        
            // Creatures count
            Text(
                text = "${creatures.size} creatures",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }
    }
}
