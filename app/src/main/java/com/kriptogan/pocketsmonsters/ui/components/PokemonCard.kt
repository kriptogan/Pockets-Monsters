package com.kriptogan.pocketsmonsters.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import coil.request.ImageRequest
import com.kriptogan.pocketsmonsters.data.models.Pokemon
import android.util.Log
import kotlinx.coroutines.launch

@Composable
fun PokemonCard(
    pokemon: Pokemon,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val spritePath = "file:///android_asset/sprites/${pokemon.spritePath}"
    
    // Debug logging when component is created
    LaunchedEffect(pokemon.name) {
        Log.d("PokemonCard", "Created card for ${pokemon.name} with sprite: $spritePath")
        println("🐛 DEBUG: Created card for ${pokemon.name} with sprite: $spritePath")
    }
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Pokémon sprite from local assets - try different loading approaches
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data("file:///android_asset/sprites/${pokemon.spritePath}")
                    .crossfade(true)
                    .build(),
                contentDescription = "Sprite of ${pokemon.name}",
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentScale = ContentScale.Fit
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Pokémon information
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = pokemon.name.replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Show types if available
                if (pokemon.types.isNotEmpty()) {
                    Row {
                        pokemon.types.forEach { typeSlot ->
                            Text(
                                text = typeSlot.type.name.replaceFirstChar { it.uppercase() },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(end = 4.dp)
                            )
                        }
                    }
                }
            }
            
            // Arrow indicator
            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = "View details",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
