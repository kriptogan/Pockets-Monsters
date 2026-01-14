package com.kriptogan.pocketsmonsters.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.activity.compose.BackHandler
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.kriptogan.pocketsmonsters.data.models.Pokemon
import com.kriptogan.pocketsmonsters.data.tcg.PokemonTCGData
import com.kriptogan.pocketsmonsters.data.tcg.TCGSet
import com.kriptogan.pocketsmonsters.data.tcg.TCGCard
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun PokemonDetailScreen(
    pokemon: Pokemon?,
    onBackClick: () -> Unit,
    onPartyUpdated: () -> Unit = {}, // Kept for compatibility, but not used
    tcgData: PokemonTCGData? = null,
    isLoadingTCG: Boolean = false,
    modifier: Modifier = Modifier
) {
    // Handle system back button
    BackHandler {
        onBackClick()
    }
    
    if (pokemon == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("No Pokémon selected")
        }
        return
    }

    // Fade-in animation for content
    val alpha by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(durationMillis = 300),
        label = "detailScreenAlpha"
    )
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .alpha(alpha)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Header - Back button and Pokemon name
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
                text = pokemon.name.replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A1A)
            )
        }
        
        // Pokémon Image with subtle shadow
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White.copy(alpha = 0.1f)
            )
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data("file:///android_asset/front_images/${pokemon.name}.png")
                    .crossfade(true)
                    .build(),
                contentDescription = "Official artwork of ${pokemon.name}",
                modifier = Modifier
                    .size(280.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Fit
            )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Types
        if (pokemon.types.isNotEmpty()) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Types",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A1A),
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    pokemon.types.forEach { type ->
                        Box(
                            modifier = Modifier
                                .width(100.dp)
                                .height(32.dp)
                                .background(
                                    color = getTypeColor(type.type.name),
                                    shape = RoundedCornerShape(16.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = type.type.name.replaceFirstChar { it.uppercase() },
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // TCG Sets Section - Offline Images from assets
        OfflineTCGSetsSection(
            pokemonId = pokemon.id,
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(16.dp))
    }
}

/**
 * Display a TCG set with its cards
 */
@Composable
private fun TCGSetCard(
    set: TCGSet,
    cards: List<TCGCard>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF5F5F5)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            // Set name and info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = set.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1A1A)
                    )
                    
                    if (set.releaseDate != null) {
                        Text(
                            text = "Released: ${set.releaseDate}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF666666),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
                
                // Set symbol/logo if available
                if (set.images?.symbol != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(set.images.symbol)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Set symbol",
                        modifier = Modifier.size(40.dp),
                        contentScale = ContentScale.Fit
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Card images
            if (cards.isNotEmpty()) {
                Text(
                    text = "Cards (${cards.size})",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF1A1A1A),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(cards) { card ->
                        TCGCardImage(
                            card = card,
                            modifier = Modifier.size(120.dp, 168.dp) // Standard card aspect ratio
                        )
                    }
                }
            } else {
                Text(
                    text = "No cards found in this set",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF999999),
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }
    }
}

/**
 * Display a single TCG card image with hover effect
 */
@Composable
private fun TCGCardImage(
    card: TCGCard,
    modifier: Modifier = Modifier
) {
    var isHovered by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isHovered) 1.05f else 1f,
        animationSpec = tween(durationMillis = 200),
        label = "cardHoverScale"
    )
    
    Card(
        modifier = modifier
            .scale(scale),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isHovered) 8.dp else 4.dp
        )
    ) {
        val imageUrl = card.images?.large ?: card.images?.small
        
        if (imageUrl != null) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(imageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = "${card.name} card",
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(card.id) {
                        detectTapGestures(
                            onPress = {
                                isHovered = true
                                tryAwaitRelease()
                                isHovered = false
                            }
                        )
                    },
                contentScale = ContentScale.Fit
            )
        } else {
            // Placeholder if no image
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = card.name,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    color = Color(0xFF666666),
                    modifier = Modifier.padding(8.dp)
                )
            }
        }
    }
}

/**
 * Display offline TCG sets section
 */
@Composable
private fun OfflineTCGSetsSection(
    pokemonId: Int,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val gson = Gson()
    
    // Load set index and sets data
    val imageDir = "tcg_images/$pokemonId"
    
    // Check if directory exists
    val directoryExists = remember(pokemonId) {
        try {
            context.assets.list("tcg_images")?.contains(pokemonId.toString()) == true
        } catch (e: Exception) {
            false
        }
    }
    
    // Load index.json which maps set IDs to set names
    val setIndex = remember(pokemonId) {
        if (!directoryExists) return@remember emptyMap<String, String>()
        try {
            val json = context.assets.open("$imageDir/index.json").bufferedReader().use { it.readText() }
            val type = object : TypeToken<Map<String, String>>() {}.type
            gson.fromJson<Map<String, String>>(json, type) ?: emptyMap()
        } catch (e: Exception) {
            emptyMap<String, String>()
        }
    }
    
    // Load sets_data.json for full set information (name, series, and release date)
    val setsData = remember {
        try {
            val json = context.assets.open("sets_data.json").bufferedReader().use { it.readText() }
            val type = object : TypeToken<Map<String, Any>>() {}.type
            val data = gson.fromJson<Map<String, Any>>(json, type)
            val setsList = (data?.get("data") as? List<Map<String, Any>>) ?: emptyList()
            setsList.associate { set ->
                val setId = set["id"] as? String ?: ""
                val setName = set["name"] as? String ?: ""
                val seriesName = set["series"] as? String ?: ""
                val releaseDate = set["releaseDate"] as? String ?: ""
                setId to Triple(setName, seriesName, releaseDate)
            }
        } catch (e: Exception) {
            emptyMap<String, Triple<String, String, String>>()
        }
    }
    
    // Load cards.json which contains image URLs
    val cardsData = remember(pokemonId) {
        if (!directoryExists) return@remember emptyMap<String, Map<String, Any>>()
        try {
            val json = context.assets.open("$imageDir/cards.json").bufferedReader().use { it.readText() }
            val type = object : TypeToken<Map<String, Map<String, Any>>>() {}.type
            gson.fromJson<Map<String, Map<String, Any>>>(json, type) ?: emptyMap()
        } catch (e: Exception) {
            emptyMap<String, Map<String, Any>>()
        }
    }
    
    // Convert cards data to list of (setId, imageUrl) pairs
    val availableImages = remember(cardsData) {
        cardsData.mapNotNull { (cardKey, cardInfo) ->
            val setId = cardInfo["setId"] as? String
            val imageUrl = cardInfo["imageUrl"] as? String
            if (setId != null && imageUrl != null) {
                Pair(setId, imageUrl)
            } else {
                null
            }
        }
    }
    
    // Group images by set ID and sort by release date (descending - newest first)
    val imagesBySet = remember(availableImages, setIndex, setsData) {
        availableImages.groupBy({ it.first }, { it.second })
            .toList()
            .sortedByDescending { (setId, _) ->
                // Parse release date from setsData (format: "YYYY/MM/DD")
                val releaseDate = setsData[setId]?.third ?: ""
                if (releaseDate.isNotEmpty()) {
                    try {
                        // Parse date string to comparable format (YYYYMMDD)
                        releaseDate.replace("/", "").toLongOrNull() ?: 0L
                    } catch (e: Exception) {
                        0L
                    }
                } else {
                    // If no release date, put at the end (oldest)
                    0L
                }
            }
            .toMap()
    }
    
    // State for enlarged image dialog (stores image URL)
    var selectedImagePath by remember { mutableStateOf<String?>(null) }
    
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = Color(0xFFD32F2F).copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "TCG Sets",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A1A),
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            if (!directoryExists || imagesBySet.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "No TCG card images found",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color(0xFF1A1A1A),
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "This Pokémon may not have any TCG cards downloaded yet.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF666666),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                // Display each set with its cards, sorted by release date (newest first)
                imagesBySet.forEach { (setId, imageUrls) ->
                    // Use sets_data.json first, then index.json, fallback to setId
                    val setName = setsData[setId]?.first ?: setIndex[setId] ?: setId
                    val seriesName = setsData[setId]?.second ?: ""
                    
                    OfflineTCGSetCard(
                        setName = setName,
                        seriesName = seriesName,
                        imageUrls = imageUrls.sorted(), // Sort image URLs for consistent display
                        onImageClick = { imageUrl ->
                            selectedImagePath = imageUrl
                        },
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }
            }
        }
    }
    
    // Enlarged image dialog
    selectedImagePath?.let { imagePath ->
        Dialog(
            onDismissRequest = { selectedImagePath = null },
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                decorFitsSystemWindows = true
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.9f)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Close button
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        IconButton(
                            onClick = { selectedImagePath = null },
                            modifier = Modifier
                                .background(
                                    Color.White.copy(alpha = 0.2f),
                                    RoundedCornerShape(8.dp)
                                )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = Color.White
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.weight(1f))
                    
                    // Enlarged image (load from URL with caching)
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(imagePath) // imagePath is now a URL
                            .diskCachePolicy(CachePolicy.ENABLED)
                            .memoryCachePolicy(CachePolicy.ENABLED)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Enlarged card",
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .fillMaxHeight(0.8f),
                        contentScale = ContentScale.Fit
                    )
                    
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

/**
 * Display a single offline TCG set with its card images
 */
@Composable
private fun OfflineTCGSetCard(
    setName: String,
    seriesName: String,
    imageUrls: List<String>,
    onImageClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF5F5F5)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            // Set name, series name, and card count
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = setName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1A1A1A)
                        )
                        if (seriesName.isNotEmpty()) {
                            Text(
                                text = seriesName,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF666666),
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }
                    Text(
                        text = "(${imageUrls.size})",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF666666),
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Card images
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(imageUrls) { imageUrl ->
                    OfflineTCGCardImage(
                        imageUrl = imageUrl,
                        onClick = { onImageClick(imageUrl) },
                        modifier = Modifier.size(120.dp, 168.dp)
                    )
                }
            }
        }
    }
}

/**
 * Display a single offline TCG card image
 */
@Composable
private fun OfflineTCGCardImage(
    imageUrl: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isHovered by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isHovered) 1.05f else 1f,
        animationSpec = tween(durationMillis = 200),
        label = "cardHoverScale"
    )
    
    Card(
        modifier = modifier
            .scale(scale)
            .pointerInput(imageUrl) {
                detectTapGestures(
                    onTap = {
                        // Only trigger on tap, not on scroll/swipe
                        onClick()
                    },
                    onPress = {
                        // Visual feedback on press
                        isHovered = true
                        tryAwaitRelease()
                        isHovered = false
                    }
                )
            },
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isHovered) 8.dp else 4.dp
        )
    ) {
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(imageUrl) // Load from URL
                .diskCachePolicy(CachePolicy.ENABLED) // Enable disk caching
                .memoryCachePolicy(CachePolicy.ENABLED) // Enable memory caching
                .crossfade(true)
                .build(),
            contentDescription = "TCG card",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit
        )
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
