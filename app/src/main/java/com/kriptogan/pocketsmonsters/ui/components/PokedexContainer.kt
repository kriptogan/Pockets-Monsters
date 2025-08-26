package com.kriptogan.pocketsmonsters.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.kriptogan.pocketsmonsters.R

@Composable
fun PokedexContainer(
    content: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFB8E6FF), // Lighter crystal blue at top (like in the image)
                        Color(0xFF87CEEB), // Medium crystal blue in middle
                        Color(0xFF5F9EA0)  // Slightly deeper blue at bottom for subtle gradient
                    )
                )
            )
    ) {
        // Top Pokedex section
        PokedexTop(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .zIndex(2f)
        )
        
        // Bottom Pokedex section
        PokedexBottom(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .zIndex(2f)
        )
        
        // Content area - positioned between borders
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 108.dp, bottom = 108.dp) // Match border heights
                .zIndex(1f)
        ) {
            content()
        }
    }
}
