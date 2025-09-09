package com.kriptogan.pocketsmonsters.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kriptogan.pocketsmonsters.data.inventory.InventoryManager
import com.kriptogan.pocketsmonsters.data.models.InventoryItem
import com.kriptogan.pocketsmonsters.ui.components.AddEditItemDialog
import com.kriptogan.pocketsmonsters.ui.components.InventoryItemCard

@Composable
fun InventoryScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val inventoryManager = remember { InventoryManager(context) }
    val inventory by inventoryManager.inventory.collectAsState()
    
    var showAddDialog by remember { mutableStateOf(false) }
    var itemToEdit by remember { mutableStateOf<InventoryItem?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    
    // Filter inventory based on search query
    val filteredInventory = remember(inventory, searchQuery) {
        if (searchQuery.isBlank()) {
            inventory
        } else {
            inventory.filter { item ->
                item.name.contains(searchQuery, ignoreCase = true)
            }
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
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Inventory",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            
            FloatingActionButton(
                onClick = { showAddDialog = true },
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Item"
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Search bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search items by name...") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search"
                )
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Items count
        Text(
            text = if (searchQuery.isBlank()) {
                "${inventory.size} items"
            } else {
                "${filteredInventory.size} of ${inventory.size} items"
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Items list
        if (inventory.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "No items in inventory",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Tap the + button to add your first item",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else if (filteredInventory.isEmpty() && searchQuery.isNotBlank()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "No items found",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Try a different search term",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredInventory) { item ->
                    InventoryItemCard(
                        item = item,
                        onEdit = { itemToEdit = it },
                        onDelete = { inventoryManager.deleteItem(it.id) },
                        onUse = { inventoryManager.useItem(it.id) }
                    )
                }
            }
        }
    }
    
    // Add item dialog
    if (showAddDialog) {
        AddEditItemDialog(
            onDismiss = { showAddDialog = false },
            onSave = { inventoryManager.addItem(it) }
        )
    }
    
    // Edit item dialog
    if (itemToEdit != null) {
        AddEditItemDialog(
            item = itemToEdit,
            onDismiss = { itemToEdit = null },
            onSave = { 
                inventoryManager.updateItem(it)
                itemToEdit = null
            }
        )
    }
}
