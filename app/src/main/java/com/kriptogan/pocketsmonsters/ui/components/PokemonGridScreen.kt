package com.kriptogan.pocketsmonsters.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.GridItemSpan
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
import com.kriptogan.pocketsmonsters.data.models.Pokemon
import com.kriptogan.pocketsmonsters.ui.viewmodel.PokemonUiState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.ui.draw.scale
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

/**
 * Get the generation number for a Pokémon ID
 */
fun getPokemonGeneration(pokemonId: Int): Int {
    return when {
        pokemonId <= 151 -> 1
        pokemonId <= 251 -> 2
        pokemonId <= 386 -> 3
        pokemonId <= 493 -> 4
        pokemonId <= 649 -> 5
        pokemonId <= 721 -> 6
        pokemonId <= 809 -> 7
        pokemonId <= 905 -> 8
        else -> 9
    }
}

/**
 * Get the start ID for a generation
 */
fun getGenerationStartId(generation: Int): Int {
    return when (generation) {
        1 -> 1
        2 -> 152
        3 -> 252
        4 -> 387
        5 -> 494
        6 -> 650
        7 -> 722
        8 -> 810
        9 -> 906
        else -> 1
    }
}

/**
 * TCG type to digital type mapping
 */
val TCG_TYPE_MAPPING = mapOf(
    "Colorless" to listOf("normal", "flying"),
    "Fire" to listOf("fire"),
    "Water" to listOf("water", "ice"),
    "Lightning" to listOf("electric"),
    "Grass" to listOf("grass", "bug"),
    "Fighting" to listOf("fighting", "ground", "rock"),
    "Psychic" to listOf("psychic", "ghost", "fairy"),
    "Darkness" to listOf("dark", "poison"),
    "Metal" to listOf("steel"),
    "Dragon" to listOf("dragon")
)

/**
 * Get all TCG types
 */
fun getAllTCGTypes(): List<String> {
    return TCG_TYPE_MAPPING.keys.sorted()
}

/**
 * Check if a Pokémon matches a TCG type
 */
fun pokemonMatchesTCGType(pokemon: Pokemon, tcgType: String): Boolean {
    val digitalTypes = TCG_TYPE_MAPPING[tcgType] ?: return false
    return pokemon.types.any { typeSlot ->
        digitalTypes.contains(typeSlot.type.name.lowercase())
    }
}

@Composable
fun PokemonGridScreen(
    uiState: PokemonUiState,
    pokemonList: List<Pokemon>,
    searchQuery: String,
    lastViewedPokemonIndex: Int,
    onPokemonClick: (String) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onPokemonLongPress: ((Int, String) -> Unit)? = null,
    ownedPokemonIds: Set<Int> = emptySet()
) {
    val gridState = rememberLazyGridState()
    
    // Filter states
    var showMissingOnly by remember { mutableStateOf(false) }
    var selectedTCGType by remember { mutableStateOf<String?>(null) }
    
    // Get all TCG types
    val allTCGTypes = remember { getAllTCGTypes() }
    
    // Filter Pokémon list based on "Missing" and "Type" filters
    val filteredPokemonList = remember(pokemonList, ownedPokemonIds, showMissingOnly, selectedTCGType) {
        val currentTCGType = selectedTCGType // Store in local variable to avoid smart cast issue
        pokemonList
            .filter { pokemon ->
                // Apply "Missing" filter
                val passesMissingFilter = !showMissingOnly || !ownedPokemonIds.contains(pokemon.id)
                
                // Apply "Type" filter (TCG format)
                val passesTypeFilter = currentTCGType == null || 
                    pokemonMatchesTCGType(pokemon, currentTCGType)
                
                passesMissingFilter && passesTypeFilter
            }
    }
    
    // Group Pokémon by generation - ensure all are included and sorted by ID
    // Use the filtered list to respect the "Missing" filter
    val pokemonByGeneration = remember(filteredPokemonList.size, filteredPokemonList.firstOrNull()?.id) {
        filteredPokemonList
            .sortedBy { it.id } // Sort by ID first to ensure proper ordering
            .groupBy { getPokemonGeneration(it.id) }
            .also { grouped ->
                // Debug: Log if any generation is missing Pokémon
                val totalGrouped = grouped.values.sumOf { it.size }
                if (totalGrouped != filteredPokemonList.size) {
                    android.util.Log.w("PokemonGridScreen", "Warning: Grouped ${totalGrouped} Pokémon but list has ${filteredPokemonList.size}")
                }
            }
    }
    
    // Find the grid index (accounting for headers) of each generation header
    val generationIndices = remember(filteredPokemonList, searchQuery) {
        if (searchQuery.isNotEmpty()) {
            // When searching, no headers, return empty map (generation selector hidden)
            emptyMap<Int, Int>()
        } else {
            // When not searching, calculate indices accounting for headers
            val indices = mutableMapOf<Int, Int>()
            var currentIndex = 0
            pokemonByGeneration.keys.sorted().forEach { generation ->
                indices[generation] = currentIndex // Header index
                currentIndex += 1 // Skip header
                val generationPokemon = pokemonByGeneration[generation] ?: emptyList()
                currentIndex += generationPokemon.size // Skip Pokémon in this generation
            }
            indices
        }
    }
    
    // Restore scroll position to the last viewed Pokémon index
    LaunchedEffect(lastViewedPokemonIndex) {
        if (lastViewedPokemonIndex >= 0 && lastViewedPokemonIndex < pokemonList.size) {
            gridState.animateScrollToItem(lastViewedPokemonIndex)
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Search Bar - positioned at top of content area
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            label = { Text("Search Pokémon...") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFFD32F2F), // Pokedex red
                unfocusedBorderColor = Color(0xFF666666), // Dark gray
                focusedLabelColor = Color(0xFFD32F2F), // Pokedex red
                unfocusedLabelColor = Color(0xFF666666) // Dark gray
            )
        )
        
        // Filter buttons row
        if (pokemonList.isNotEmpty()) {
            var showGenerationDialog by remember { mutableStateOf(false) }
            var showTypeDialog by remember { mutableStateOf(false) }
            var showDataDialog by remember { mutableStateOf(false) }
            var scrollToGeneration by remember { mutableStateOf<Int?>(null) }
            
            // Handle scroll when generation is selected
            LaunchedEffect(scrollToGeneration) {
                scrollToGeneration?.let { gen ->
                    generationIndices[gen]?.let { index ->
                        gridState.animateScrollToItem(index)
                    }
                    scrollToGeneration = null
                }
            }
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.Start
            ) {
                // Gen button - only show when not searching
                if (searchQuery.isEmpty()) {
                    Button(
                        onClick = { showGenerationDialog = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFD32F2F) // Pokedex red
                        ),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text(
                            text = "Gen",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                // Type button - show always
                Button(
                    onClick = { showTypeDialog = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedTCGType != null) Color(0xFF4CAF50) else Color(0xFFD32F2F) // Green when active, red when inactive
                    ),
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Text(
                        text = selectedTCGType ?: "Type",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                // Missing button - show always
                Button(
                    onClick = { showMissingOnly = !showMissingOnly },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (showMissingOnly) Color(0xFF4CAF50) else Color(0xFFD32F2F) // Green when active, red when inactive
                    ),
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Text(
                        text = "Missing",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                // Data button - show always
                Button(
                    onClick = { showDataDialog = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFD32F2F) // Pokedex red
                    ),
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Text(
                        text = "%",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            // Generation selection dialog
            if (showGenerationDialog) {
                AlertDialog(
                    onDismissRequest = { showGenerationDialog = false },
                    title = {
                        Text(
                            text = "Select Generation",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFD32F2F)
                        )
                    },
                    text = {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            (1..9).forEach { gen ->
                                val generationPokemon = pokemonByGeneration[gen] ?: emptyList()
                                val ownedInGen = generationPokemon.count { ownedPokemonIds.contains(it.id) }
                                
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            scrollToGeneration = gen
                                            showGenerationDialog = false
                                        },
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp, vertical = 12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Generation $gen",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF1A1A1A)
                                        )
                                        Text(
                                            text = "$ownedInGen/${generationPokemon.size}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = Color(0xFF666666)
                                        )
                                    }
                                }
                            }
                        }
                    },
                    confirmButton = {},
                    dismissButton = {
                        TextButton(
                            onClick = { showGenerationDialog = false }
                        ) {
                            Text("Cancel")
                        }
                    },
                    containerColor = Color.White,
                    shape = RoundedCornerShape(16.dp)
                )
            }
            
            // Type selection dialog
            if (showTypeDialog) {
                AlertDialog(
                    onDismissRequest = { showTypeDialog = false },
                    title = {
                        Text(
                            text = "Select Type",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFD32F2F)
                        )
                    },
                    text = {
                        val scrollState = rememberScrollState()
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .verticalScroll(scrollState),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Option to clear type filter
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedTCGType = null
                                        showTypeDialog = false
                                    },
                                colors = CardDefaults.cardColors(
                                    containerColor = if (selectedTCGType == null) Color(0xFF4CAF50).copy(alpha = 0.2f) else Color.White
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "All Types",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1A1A1A)
                                    )
                                    val totalCount = if (showMissingOnly) {
                                        pokemonList.count { !ownedPokemonIds.contains(it.id) }
                                    } else {
                                        pokemonList.size
                                    }
                                    Text(
                                        text = "$totalCount",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color(0xFF666666)
                                    )
                                }
                            }
                            
                            // TCG Type options
                            allTCGTypes.forEach { tcgType ->
                                val tcgTypePokemon = pokemonList.filter { pokemon ->
                                    pokemonMatchesTCGType(pokemon, tcgType)
                                }
                                val filteredTCGTypePokemon = if (showMissingOnly) {
                                    tcgTypePokemon.filter { !ownedPokemonIds.contains(it.id) }
                                } else {
                                    tcgTypePokemon
                                }
                                
                                // Get the digital types included in this TCG type
                                val digitalTypes = TCG_TYPE_MAPPING[tcgType] ?: emptyList()
                                val typeLabel = if (digitalTypes.size > 1) {
                                    "$tcgType (${digitalTypes.joinToString(", ") { it.replaceFirstChar { char -> char.uppercaseChar() } }})"
                                } else {
                                    tcgType
                                }
                                
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            selectedTCGType = tcgType
                                            showTypeDialog = false
                                        },
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (selectedTCGType == tcgType) Color(0xFF4CAF50).copy(alpha = 0.2f) else Color.White
                                    ),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp, vertical = 12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = tcgType,
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF1A1A1A)
                                            )
                                            if (digitalTypes.size > 1) {
                                                Text(
                                                    text = digitalTypes.joinToString(", ") { it.replaceFirstChar { char -> char.uppercaseChar() } },
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = Color(0xFF666666),
                                                    fontSize = 12.sp
                                                )
                                            }
                                        }
                                        Text(
                                            text = "${filteredTCGTypePokemon.size}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = Color(0xFF666666)
                                        )
                                    }
                                }
                            }
                        }
                    },
                    confirmButton = {},
                    dismissButton = {
                        TextButton(
                            onClick = { showTypeDialog = false }
                        ) {
                            Text("Cancel")
                        }
                    },
                    containerColor = Color.White,
                    shape = RoundedCornerShape(16.dp)
                )
            }
            
            // Data statistics dialog
            if (showDataDialog) {
                // Calculate statistics
                val totalPokemon = pokemonList.size
                val totalOwned = ownedPokemonIds.size
                val totalOwnedPercentage = if (totalPokemon > 0) {
                    (totalOwned.toFloat() / totalPokemon * 100).toInt()
                } else 0
                
                // Calculate by generation
                val ownedByGen = remember(pokemonList, ownedPokemonIds) {
                    (1..9).map { gen ->
                        val genPokemon = pokemonList.filter { getPokemonGeneration(it.id) == gen }
                        val genOwned = genPokemon.count { ownedPokemonIds.contains(it.id) }
                        val genPercentage = if (genPokemon.isNotEmpty()) {
                            (genOwned.toFloat() / genPokemon.size * 100).toInt()
                        } else 0
                        gen to Triple(genOwned, genPokemon.size, genPercentage)
                    }.toMap()
                }
                
                // Calculate by TCG type
                val ownedByTCGType = remember(pokemonList, ownedPokemonIds) {
                    getAllTCGTypes().map { tcgType ->
                        val typePokemon = pokemonList.filter { pokemonMatchesTCGType(it, tcgType) }
                        val typeOwned = typePokemon.count { ownedPokemonIds.contains(it.id) }
                        val typePercentage = if (typePokemon.isNotEmpty()) {
                            (typeOwned.toFloat() / typePokemon.size * 100).toInt()
                        } else 0
                        tcgType to Triple(typeOwned, typePokemon.size, typePercentage)
                    }.toMap()
                }
                
                val scrollState = rememberScrollState()
                
                AlertDialog(
                    onDismissRequest = { showDataDialog = false },
                    title = {
                        Text(
                            text = "Collection Statistics",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFD32F2F)
                        )
                    },
                    text = {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .verticalScroll(scrollState),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Total owned percentage
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xFFD32F2F).copy(alpha = 0.1f)
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = "Total Collection",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFD32F2F)
                                    )
                                    Text(
                                        text = "$totalOwned / $totalPokemon",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = Color(0xFF1A1A1A)
                                    )
                                    Text(
                                        text = "$totalOwnedPercentage%",
                                        style = MaterialTheme.typography.headlineMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF4CAF50)
                                    )
                                }
                            }
                            
                            // By generation
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color.White
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Text(
                                        text = "By Generation",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1A1A1A)
                                    )
                                    (1..9).forEach { gen ->
                                        val (owned, total, percentage) = ownedByGen[gen] ?: Triple(0, 0, 0)
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "Gen $gen",
                                                style = MaterialTheme.typography.bodyLarge,
                                                fontWeight = FontWeight.Medium,
                                                color = Color(0xFF1A1A1A)
                                            )
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "$owned/$total",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = Color(0xFF666666)
                                                )
                                                Text(
                                                    text = "$percentage%",
                                                    style = MaterialTheme.typography.bodyLarge,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF4CAF50)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                            
                            // By TCG type
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color.White
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Text(
                                        text = "By TCG Type",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1A1A1A)
                                    )
                                    getAllTCGTypes().forEach { tcgType ->
                                        val (owned, total, percentage) = ownedByTCGType[tcgType] ?: Triple(0, 0, 0)
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = tcgType,
                                                style = MaterialTheme.typography.bodyLarge,
                                                fontWeight = FontWeight.Medium,
                                                color = Color(0xFF1A1A1A)
                                            )
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "$owned/$total",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = Color(0xFF666666)
                                                )
                                                Text(
                                                    text = "$percentage%",
                                                    style = MaterialTheme.typography.bodyLarge,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF4CAF50)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    },
                    confirmButton = {},
                    dismissButton = {
                        TextButton(
                            onClick = { showDataDialog = false }
                        ) {
                            Text("Close")
                        }
                    },
                    containerColor = Color.White,
                    shape = RoundedCornerShape(16.dp)
                )
            }
        }
        
        when (uiState) {
            is PokemonUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = Color(0xFFD32F2F) // Pokedex red
                    )
                }
            }
            
            is PokemonUiState.Success -> {
                if (filteredPokemonList.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "No Pokémon found",
                                style = MaterialTheme.typography.titleLarge,
                                textAlign = TextAlign.Center,
                                color = Color(0xFF1A1A1A),
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Try adjusting your search",
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center,
                                color = Color(0xFF666666)
                            )
                        }
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(4), // 4 columns like you requested
                        state = gridState,
                        horizontalArrangement = Arrangement.spacedBy(8.dp), // Reduced spacing for 4 columns
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        // Only show generation headers if not searching
                        if (searchQuery.isEmpty()) {
                            // Use the pre-computed grouping to ensure consistency
                            // Verify all Pokémon are included (debug check)
                            val totalInGroups = pokemonByGeneration.values.sumOf { it.size }
                            if (totalInGroups != filteredPokemonList.size) {
                                android.util.Log.e("PokemonGridScreen", "Mismatch: ${filteredPokemonList.size} total Pokémon but ${totalInGroups} in groups. Missing: ${filteredPokemonList.size - totalInGroups}")
                            }
                            
                            pokemonByGeneration.keys.sorted().forEach { generation ->
                                val generationPokemon = pokemonByGeneration[generation] ?: emptyList()
                                
                                // Generation header with owned/total counter
                                item(
                                    key = "header_gen_$generation",
                                    span = { GridItemSpan(4) }
                                ) {
                                    val ownedInGen = generationPokemon.count { ownedPokemonIds.contains(it.id) }
                                    GenerationHeader(
                                        generation = generation,
                                        totalCount = generationPokemon.size,
                                        ownedCount = ownedInGen,
                                        modifier = Modifier.padding(vertical = 16.dp, horizontal = 4.dp)
                                    )
                                }
                                
                                // Pokémon in this generation - already sorted by ID from grouping
                                items(
                                    items = generationPokemon,
                                    key = { pokemon -> "${pokemon.id}_${ownedPokemonIds.contains(pokemon.id)}" }
                                ) { pokemon ->
                                    val isOwned = ownedPokemonIds.contains(pokemon.id)
                                    PokemonGridCard(
                                        pokemon = pokemon,
                                        onClick = { 
                                            // Tap toggles ownership
                                            onPokemonLongPress?.invoke(pokemon.id, pokemon.name)
                                        },
                                        onLongPress = { 
                                            // Long press opens details
                                            onPokemonClick(pokemon.name)
                                        },
                                        isOwned = isOwned
                                    )
                                }
                            }
                        } else {
                            // When searching, show filtered results without generation headers
                            items(
                                items = filteredPokemonList,
                                key = { pokemon -> "${pokemon.id}_${ownedPokemonIds.contains(pokemon.id)}" }
                            ) { pokemon ->
                                val isOwned = ownedPokemonIds.contains(pokemon.id)
                                PokemonGridCard(
                                    pokemon = pokemon,
                                    onClick = { 
                                        // Tap toggles ownership
                                        onPokemonLongPress?.invoke(pokemon.id, pokemon.name)
                                    },
                                    onLongPress = { 
                                        // Long press opens details
                                        onPokemonClick(pokemon.name)
                                    },
                                    isOwned = isOwned
                                )
                            }
                        }
                    }
                }
            }
            
            is PokemonUiState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = uiState.message ?: "An error occurred",
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.error
                        )
                        
                        Button(
                            onClick = { /* TODO: Implement retry functionality */ },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFD32F2F) // Pokedex red
                            )
                        ) {
                            Text("Retry")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PokemonGridCard(
    pokemon: Pokemon,
    onClick: () -> Unit, // Now toggles ownership
    onLongPress: (() -> Unit)? = null, // Now opens details
    isOwned: Boolean = false,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    
    // Animation for card scale on press
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1f,
        animationSpec = tween(durationMillis = 150),
        label = "gridCardScale"
    )
    
    // Green border for owned Pokémon, white border for unowned with animation
    val borderColor = if (isOwned) Color(0xFF4CAF50) else Color.White
    val borderWidth by animateDpAsState(
        targetValue = if (isOwned) 3.dp else 2.dp,
        animationSpec = tween(durationMillis = 300),
        label = "gridBorderWidth"
    )
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(0.8f) // Vertical rectangle like in your image
            .clip(RoundedCornerShape(16.dp))
            .background(Color.Transparent) // Fully transparent background
            .border(
                width = borderWidth,
                color = borderColor,
                shape = RoundedCornerShape(16.dp)
            )
            .scale(scale)
            .pointerInput(pokemon.id) {
                detectTapGestures(
                    onLongPress = {
                        // Long press opens details
                        if (onLongPress != null) {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onLongPress()
                        }
                    },
                    onTap = { 
                        // Tap toggles ownership
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onClick() 
                    },
                    onPress = {
                        isPressed = true
                        tryAwaitRelease()
                        isPressed = false
                    }
                )
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize(), // No padding
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center // Center content vertically
        ) {
            // Pokémon front image from front_images directory
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data("file:///android_asset/front_images/${pokemon.name}.png")
                    .crossfade(true)
                    .build(),
                contentDescription = "Front image of ${pokemon.name}",
                modifier = Modifier
                    .size(64.dp) // 80% of 80dp = 64dp
                    .clip(RoundedCornerShape(8.dp)), // No background
                contentScale = ContentScale.Fit
            )
            
            Spacer(modifier = Modifier.height(8.dp)) // Small spacing between image and text
            
            // Pokémon ID number (4 digits)
            Text(
                text = String.format("%04d", pokemon.id), // Format as 4 digits (e.g., "0001", "0025")
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A1A), // Dark text for readability
                textAlign = TextAlign.Center,
                fontSize = 16.sp
            )
        }
    }
}

/**
 * Generation header component with owned/total counter
 */
@Composable
private fun GenerationHeader(
    generation: Int,
    totalCount: Int,
    ownedCount: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFD32F2F).copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Generation $generation",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFD32F2F)
            )
            Text(
                text = "$ownedCount/$totalCount",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF666666)
            )
        }
    }
}
