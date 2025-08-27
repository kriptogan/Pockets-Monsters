package com.kriptogan.pocketsmonsters.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
fun CustomBottomNavigation(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = Color(0xFF87CEEB).copy(alpha = 0.5f), // Crystal blue with 50% opacity
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
            )
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Pokedex Tab - Square icon
        CustomNavigationButton(
            icon = Icons.Default.Menu,
            isSelected = currentRoute == "pokedex",
            onClick = { onNavigate("pokedex") }
        )
        
        // Utilities Tab - Star icon
        CustomNavigationButton(
            icon = Icons.Default.Star,
            isSelected = currentRoute == "utilities",
            onClick = { onNavigate("utilities") }
        )
        
        // My Party Tab - Circle icon
        CustomNavigationButton(
            icon = Icons.Default.Favorite,
            isSelected = currentRoute == "my_party",
            onClick = { onNavigate("my_party") }
        )
    }
}

@Composable
private fun CustomNavigationButton(
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(48.dp)
            .background(
                color = if (isSelected) {
                    Color(0xFFD32F2F).copy(alpha = 0.8f) // Red when selected
                } else {
                    Color(0xFF2A2A2A).copy(alpha = 0.6f) // Dark when not selected
                },
                shape = RoundedCornerShape(12.dp)
            )
    ) {
        Icon(
            imageVector = icon,
            contentDescription = "Navigation button",
            tint = if (isSelected) Color.White else Color(0xFFDDDDDD),
            modifier = Modifier.size(24.dp)
        )
    }
}
