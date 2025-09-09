package com.kriptogan.pocketsmonsters.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.kriptogan.pocketsmonsters.data.models.InventoryItem

@Composable
fun AddEditItemDialog(
    item: InventoryItem? = null,
    onDismiss: () -> Unit,
    onSave: (InventoryItem) -> Unit,
    modifier: Modifier = Modifier
) {
    var name by remember { mutableStateOf(item?.name ?: "") }
    var quantity by remember { mutableStateOf(item?.quantity?.toString() ?: "0") }
    var description by remember { mutableStateOf(item?.description ?: "") }
    
    val isEditMode = item != null
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isEditMode) "Edit Item" else "Add Item",
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Item Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                OutlinedTextField(
                    value = quantity,
                    onValueChange = { 
                        if (it.all { char -> char.isDigit() }) {
                            quantity = it
                        }
                    },
                    label = { Text("Quantity") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        val quantityInt = quantity.toIntOrNull() ?: 0
                        val itemToSave = if (isEditMode) {
                            item!!.copy(
                                name = name.trim(),
                                quantity = quantityInt,
                                description = description.trim()
                            )
                        } else {
                            InventoryItem(
                                name = name.trim(),
                                quantity = quantityInt,
                                description = description.trim()
                            )
                        }
                        onSave(itemToSave)
                        onDismiss()
                    }
                },
                enabled = name.isNotBlank()
            ) {
                Text(if (isEditMode) "Update" else "Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        modifier = modifier
    )
}
