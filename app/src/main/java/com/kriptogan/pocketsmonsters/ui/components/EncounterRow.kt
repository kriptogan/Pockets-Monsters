package com.kriptogan.pocketsmonsters.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
    onRemove: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Remove button (prefix to name)
            IconButton(
                onClick = { showDeleteConfirmation = true },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Remove",
                    tint = Color.Red,
                    modifier = Modifier.size(20.dp)
                )
            }
            
            // Name field
            OutlinedTextField(
                value = creature.name,
                onValueChange = { onUpdate(creature.updateName(it)) },
                modifier = Modifier
                    .weight(2f)
                    .height(45.dp),
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
                onValueChange = { onUpdate(creature.updateCurrentHp(it)) },
                modifier = Modifier
                    .width(45.dp)
                    .height(45.dp),
                singleLine = true,
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number),
                textStyle = TextStyle(fontSize = 10.sp)
            )
        }
    }
    
    // Delete confirmation dialog
    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text("Remove Creature") },
            text = { Text("Are you sure you want to remove '${creature.name.ifBlank { "this creature" }}'?") },
            confirmButton = {
                Button(
                    onClick = {
                        onRemove(creature.id)
                        showDeleteConfirmation = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Red
                    )
                ) {
                    Text("Remove")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
