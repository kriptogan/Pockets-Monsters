package com.kriptogan.pocketsmonsters.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
fun EnergySlotsScreen(
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
                text = "Energy Slots",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Description
        Text(
            text = "D&D full caster spell slot progression for Pokémon energy slots",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Energy Slots Table
        EnergySlotsTable()
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // How It Works Section
        HowItWorksSection()
    }
}

@Composable
private fun EnergySlotsTable() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Energy Slots by D&D Level",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            // Table Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(8.dp)
            ) {
                Text(
                    text = "Level",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "1st",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "2nd",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "3rd",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "4th",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "5th",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
            }
            
            // Table Rows
            val levels = listOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20)
            val spellSlots = listOf(
                listOf(2, 0, 0, 0, 0, 0, 0, 0, 0), // Level 1
                listOf(3, 0, 0, 0, 0, 0, 0, 0, 0), // Level 2
                listOf(4, 2, 0, 0, 0, 0, 0, 0, 0), // Level 3
                listOf(4, 3, 0, 0, 0, 0, 0, 0, 0), // Level 4
                listOf(4, 3, 2, 0, 0, 0, 0, 0, 0), // Level 5
                listOf(4, 3, 3, 0, 0, 0, 0, 0, 0), // Level 6
                listOf(4, 3, 3, 1, 0, 0, 0, 0, 0), // Level 7
                listOf(4, 3, 3, 2, 0, 0, 0, 0, 0), // Level 8
                listOf(4, 3, 3, 3, 1, 0, 0, 0, 0), // Level 9
                listOf(4, 3, 3, 3, 2, 0, 0, 0, 0), // Level 10
                listOf(4, 3, 3, 3, 2, 1, 0, 0, 0), // Level 11
                listOf(4, 3, 3, 3, 2, 1, 0, 0, 0), // Level 12
                listOf(4, 3, 3, 3, 2, 1, 1, 0, 0), // Level 13
                listOf(4, 3, 3, 3, 2, 1, 1, 0, 0), // Level 14
                listOf(4, 3, 3, 3, 2, 1, 1, 1, 0), // Level 15
                listOf(4, 3, 3, 3, 2, 1, 1, 1, 0), // Level 16
                listOf(4, 3, 3, 3, 2, 1, 1, 1, 1), // Level 17
                listOf(4, 3, 3, 3, 2, 1, 1, 1, 1), // Level 18
                listOf(4, 3, 3, 3, 2, 1, 1, 1, 1), // Level 19
                listOf(4, 3, 3, 3, 2, 1, 1, 1, 1)  // Level 20
            )
            
            levels.forEachIndexed { index, level ->
                val slots = spellSlots[index]
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            if (level % 2 == 0) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                            else Color.Transparent
                        )
                        .padding(8.dp)
                ) {
                    Text(
                        text = level.toString(),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )
                    slots.take(5).forEach { slot ->
                        Text(
                            text = if (slot > 0) slot.toString() else "-",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center,
                            color = if (slot > 0) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HowItWorksSection() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "How Energy Slots Work",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            Text(
                text = "• After a long rest, prepare 4 moves that require energy slots",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "• Free moves (0 slots) can be used at will",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "• Basic moves require 1+ energy slots",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "• Power moves require 2+ energy slots",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "• Ultimate moves require 3+ energy slots",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "• Energy slots refresh after each long rest",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
