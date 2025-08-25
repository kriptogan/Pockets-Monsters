package com.kriptogan.pocketsmonsters.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding

sealed class BottomNavItem(
    val route: String,
    val title: String,
    val icon: @Composable () -> Unit
) {
    object Pokedex : BottomNavItem(
        route = "pokedex",
        title = "Pokedex",
        icon = { Icon(Icons.Default.List, contentDescription = "Pokedex") }
    )
    
    object Utilities : BottomNavItem(
        route = "utilities",
        title = "Utilities",
        icon = { Icon(Icons.Default.Settings, contentDescription = "Utilities") }
    )
    
    object MyParty : BottomNavItem(
        route = "my_party",
        title = "My Party",
        icon = { Icon(Icons.Default.AccountCircle, contentDescription = "My Party") }
    )
}

@Composable
fun BottomNavigation(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier.height(64.dp)  // Custom height to prevent tab cutoff
    ) {
        NavigationBarItem(
            icon = BottomNavItem.Pokedex.icon,
            label = { Text(BottomNavItem.Pokedex.title) },
            selected = currentRoute == BottomNavItem.Pokedex.route,
            onClick = { onNavigate(BottomNavItem.Pokedex.route) },
            modifier = Modifier
        .padding(top = 25.dp)  // ← Individual item spacing
        )
        
        NavigationBarItem(
            icon = BottomNavItem.Utilities.icon,
            label = { Text(BottomNavItem.Utilities.title) },
            selected = currentRoute == BottomNavItem.Utilities.route,
            onClick = { onNavigate(BottomNavItem.Utilities.route) },
            modifier = Modifier
        .padding(top = 25.dp)  // ← Individual item spacing
        )
        
        NavigationBarItem(
            icon = BottomNavItem.MyParty.icon,
            label = { Text(BottomNavItem.MyParty.title) },
            selected = currentRoute == BottomNavItem.MyParty.route,
            onClick = { onNavigate(BottomNavItem.MyParty.route) },
            modifier = Modifier
        .padding(top = 25.dp)  // ← Individual item spacing
        )
    }
}
