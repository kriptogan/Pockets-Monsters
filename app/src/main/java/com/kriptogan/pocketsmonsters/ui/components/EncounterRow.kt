package com.kriptogan.pocketsmonsters.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kriptogan.pocketsmonsters.data.models.EncounterCreature

@Composable
fun EncounterRow(
    creature: EncounterCreature,
    onUpdate: (EncounterCreature) -> Unit,
    onRemove: (Int) -> Unit,
    onInfoClick: () -> Unit = {},
    onFocusChange: (Boolean) -> Unit = {},
    isFocused: Boolean = false,
    modifier: Modifier = Modifier
) {
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isFocused) Color(0xFFE3F2FD) else Color.Unspecified
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Info icon
            IconButton(
                onClick = onInfoClick,
                modifier = Modifier.width(15.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Information",
                    tint = Color(0xFF666666),
                    modifier = Modifier.size(16.dp)
                )
            }
            
            // Name field
            OutlinedTextField(
                value = creature.name,
                onValueChange = { onUpdate(creature.updateName(it)) },
                modifier = Modifier
                    .weight(2f)
                    .height(45.dp)
                    .onFocusChanged { focusState ->
                        onFocusChange(focusState.isFocused)
                    },
                singleLine = true,
                textStyle = TextStyle(fontSize = 10.sp)
            )
            
            // Initiative field
            OutlinedTextField(
                value = creature.initiative,
                onValueChange = { onUpdate(creature.updateInitiative(it)) },
                modifier = Modifier
                    .width(45.dp)
                    .height(45.dp),
                singleLine = true,
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number),
                textStyle = TextStyle(fontSize = 10.sp)
            )
            
            // AC field
            OutlinedTextField(
                value = creature.ac,
                onValueChange = { onUpdate(creature.updateAc(it)) },
                modifier = Modifier
                    .width(45.dp)
                    .height(45.dp),
                singleLine = true,
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number),
                textStyle = TextStyle(fontSize = 10.sp)
            )
            
            // Max HP field
            OutlinedTextField(
                value = creature.maxHp,
                onValueChange = { onUpdate(creature.updateMaxHp(it)) },
                modifier = Modifier
                    .width(45.dp)
                    .height(45.dp),
                singleLine = true,
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number),
                textStyle = TextStyle(fontSize = 10.sp)
            )
            
            // Current HP field
            OutlinedTextField(
                value = creature.currentHp,
                onValueChange = { newHp ->
                    val updatedCreature = creature.updateCurrentHp(newHp)
                    onUpdate(updatedCreature)
                    
                    // Auto-remove if HP reaches -1
                    if (newHp == "-1") {
                        onRemove(creature.id)
                    }
                },
                modifier = Modifier
                    .width(45.dp)
                    .height(45.dp),
                singleLine = true,
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number),
                textStyle = TextStyle(fontSize = 10.sp)
            )
        }
    }
}
