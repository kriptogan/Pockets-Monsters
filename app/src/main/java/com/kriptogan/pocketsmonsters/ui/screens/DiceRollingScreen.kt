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
import kotlinx.coroutines.delay
import kotlin.random.Random

@Composable
fun DiceRollingScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var currentRoll by remember { mutableStateOf<Int?>(null) }
    var isRolling by remember { mutableStateOf(false) }
    var rollHistory by remember { mutableStateOf<List<RollResult>>(emptyList()) }
    var currentDiceType by remember { mutableStateOf<DiceType?>(null) }
    
    // Handle dice rolling animation
    LaunchedEffect(isRolling) {
        if (isRolling && currentDiceType != null) {
            // Simulate rolling animation
            repeat(10) {
                val randomResult = Random.nextInt(1, currentDiceType!!.sides + 1)
                currentRoll = randomResult
                delay(100)
            }
            
            // Final result
            val finalResult = Random.nextInt(1, currentDiceType!!.sides + 1)
            currentRoll = finalResult
            
            // Add to roll history
            rollHistory = listOf(
                RollResult(currentDiceType!!.name, finalResult, System.currentTimeMillis())
            ) + rollHistory.take(9) // Keep last 10 rolls
            
            // Reset rolling state
            isRolling = false
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
                                if (!isRolling) {
                                    isRolling = true
                                    currentDiceType = diceType
                                }
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

data class RollResult(
    val diceType: String,
    val result: Int,
    val timestamp: Long
)

data class DiceType(
    val name: String,
    val sides: Int
)

private val diceTypes = listOf(
    DiceType("D4", 4),
    DiceType("D6", 6),
    DiceType("D8", 8),
    DiceType("D10", 10),
    DiceType("D12", 12),
    DiceType("D20", 20)
)
