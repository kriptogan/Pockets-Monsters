package com.kriptogan.pocketsmonsters.ui.viewmodel

import android.app.Application
import androidx.compose.runtime.*
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.random.Random
import kotlinx.coroutines.delay

class DiceRollingViewModel(application: Application) : AndroidViewModel(application) {
    
    // Roll history that persists during navigation
    private val _rollHistory = MutableStateFlow<List<RollResult>>(emptyList())
    val rollHistory: StateFlow<List<RollResult>> = _rollHistory.asStateFlow()
    
    // Current roll state
    private val _currentRoll = MutableStateFlow<Int?>(null)
    val currentRoll: StateFlow<Int?> = _currentRoll.asStateFlow()
    
    // Rolling state
    private val _isRolling = MutableStateFlow(false)
    val isRolling: StateFlow<Boolean> = _isRolling.asStateFlow()
    
    // Current dice type
    private val _currentDiceType = MutableStateFlow<DiceType?>(null)
    val currentDiceType: StateFlow<DiceType?> = _currentDiceType.asStateFlow()
    
    /**
     * Start rolling a specific dice type
     */
    fun startRoll(diceType: DiceType) {
        if (!_isRolling.value) {
            _isRolling.value = true
            _currentDiceType.value = diceType
        }
    }
    
    /**
     * Handle the dice rolling animation and result
     */
    fun performRoll() {
        viewModelScope.launch {
            val diceType = _currentDiceType.value ?: return@launch
            
            // Simulate rolling animation
            repeat(10) {
                val randomResult = Random.nextInt(1, diceType.sides + 1)
                _currentRoll.value = randomResult
                delay(100)
            }
            
            // Final result
            val finalResult = Random.nextInt(1, diceType.sides + 1)
            _currentRoll.value = finalResult
            
            // Add to roll history
            val newRoll = RollResult(diceType.name, finalResult, System.currentTimeMillis())
            _rollHistory.value = listOf(newRoll) + _rollHistory.value.take(9) // Keep last 10 rolls
            
            // Reset rolling state
            _isRolling.value = false
        }
    }
    
    /**
     * Clear current roll (for when returning to screen)
     */
    fun clearCurrentRoll() {
        _currentRoll.value = null
        _currentDiceType.value = null
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

val diceTypes = listOf(
    DiceType("D4", 4),
    DiceType("D6", 6),
    DiceType("D8", 8),
    DiceType("D10", 10),
    DiceType("D12", 12),
    DiceType("D20", 20)
)
