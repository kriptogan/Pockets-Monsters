package com.kriptogan.pocketsmonsters.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kriptogan.pocketsmonsters.ui.viewmodel.DiceRollingViewModel
import com.kriptogan.pocketsmonsters.ui.viewmodel.diceTypes
import com.kriptogan.pocketsmonsters.ui.viewmodel.DiceType
import com.kriptogan.pocketsmonsters.ui.viewmodel.RollResult

@Composable
fun DiceRollingScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val viewModel: DiceRollingViewModel = viewModel()
    val currentRoll by viewModel.currentRoll.collectAsState()
    val isRolling by viewModel.isRolling.collectAsState()
    val rollHistory by viewModel.rollHistory.collectAsState()
    val currentDiceType by viewModel.currentDiceType.collectAsState()
    
    // Handle dice rolling animation
    LaunchedEffect(isRolling) {
        if (isRolling) {
            viewModel.performRoll()
        }
    }
    
    // Clear current roll when leaving screen
    DisposableEffect(Unit) {
        onDispose {
            viewModel.clearCurrentRoll()
        }
    }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color(0xFFD32F2F)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Text(
                text = "Dice Rolling",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A1A)
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Top Section: Dice Grid (Left) + Rolling Tray (Right)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Section 1: Top Left - Dice Selection Grid (2x3)
            Column(
                modifier = Modifier.weight(1f)
            ) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(diceTypes) { diceType ->
                        DiceButton(
                            diceType = diceType,
                            onClick = {
                                viewModel.startRoll(diceType)
                            },
                            isRolling = isRolling
                        )
                    }
                }
            }
            
            // Section 2: Top Right - Rolling Tray
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            color = Color(0xFF87CEEB).copy(alpha = 0.1f),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .border(
                            width = 2.dp,
                            color = Color(0xFF87CEEB),
                            shape = RoundedCornerShape(16.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isRolling) {
                        RollingDice()
                    } else if (currentRoll != null) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = currentRoll.toString(),
                                style = MaterialTheme.typography.displayLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFD32F2F),
                                fontSize = 64.sp
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Text(
                                text = currentDiceType?.name ?: "",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color(0xFF666666)
                            )
                        }
                    } else {
                        Text(
                            text = "Roll the dice!",
                            style = MaterialTheme.typography.headlineSmall,
                            color = Color(0xFF666666),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Section 3: Bottom Center - Roll History
        if (rollHistory.isNotEmpty()) {
            LazyColumn(
                modifier = Modifier.height(200.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(rollHistory) { roll ->
                    RollHistoryItem(roll = roll)
                }
            }
        }
    }
}

@Composable
private fun RollingDice() {
    val infiniteTransition = rememberInfiniteTransition(label = "dice_roll")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )
    
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(300, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )
    
    Icon(
        imageVector = Icons.Default.Refresh,
        contentDescription = "Rolling dice",
        modifier = Modifier
            .size(60.dp)
            .rotate(rotation)
            .scale(scale),
        tint = Color(0xFFD32F2F)
    )
}

@Composable
private fun DiceButton(
    diceType: DiceType,
    onClick: () -> Unit,
    isRolling: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(RoundedCornerShape(10.dp))
            .background(
                color = if (isRolling) Color(0xFFE0E0E0) else Color(0xFF87CEEB).copy(alpha = 0.2f)
            )
            .border(
                width = 2.dp,
                color = Color(0xFF87CEEB),
                shape = RoundedCornerShape(10.dp)
            )
            .clickable(enabled = !isRolling, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = diceType.name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = if (isRolling) Color(0xFF999999) else Color(0xFF1A1A1A),
                fontSize = 14.sp
            )
            
            Spacer(modifier = Modifier.height(2.dp))
            
            Text(
                text = "d${diceType.sides}",
                style = MaterialTheme.typography.bodySmall,
                color = if (isRolling) Color(0xFF999999) else Color(0xFF666666),
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun RollHistoryItem(roll: RollResult) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = Color(0xFFF5F5F5),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = roll.diceType,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF1A1A1A),
            modifier = Modifier.weight(1f)
        )
        
        Text(
            text = roll.result.toString(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFD32F2F)
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        Text(
            text = formatTime(roll.timestamp),
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF999999)
        )
    }
}

private fun formatTime(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    
    return when {
        diff < 60000 -> "Just now"
        diff < 3600000 -> "${diff / 60000}m ago"
        diff < 86400000 -> "${diff / 3600000}h ago"
        else -> "${diff / 86400000}d ago"
    }
}
