package com.kriptogan.pocketsmonsters.data.inventory

import android.content.Context
import android.content.SharedPreferences
import com.kriptogan.pocketsmonsters.data.models.InventoryItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

class InventoryManager(private val context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("inventory", Context.MODE_PRIVATE)
    private val inventoryKey = "inventory_items"
    
    private val _inventory = MutableStateFlow<List<InventoryItem>>(emptyList())
    val inventory: StateFlow<List<InventoryItem>> = _inventory.asStateFlow()
    
    init {
        loadInventory()
    }
    
    fun addItem(item: InventoryItem) {
        val newItem = if (item.id.isEmpty()) {
            item.copy(id = UUID.randomUUID().toString())
        } else {
            item
        }
        
        val currentList = _inventory.value.toMutableList()
        currentList.add(newItem)
        _inventory.value = currentList
        saveInventory()
    }
    
    fun updateItem(updatedItem: InventoryItem) {
        val currentList = _inventory.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == updatedItem.id }
        if (index != -1) {
            currentList[index] = updatedItem
            _inventory.value = currentList
            saveInventory()
        }
    }
    
    fun deleteItem(itemId: String) {
        val currentList = _inventory.value.toMutableList()
        currentList.removeAll { it.id == itemId }
        _inventory.value = currentList
        saveInventory()
    }
    
    fun useItem(itemId: String) {
        val currentList = _inventory.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == itemId }
        if (index != -1) {
            currentList[index] = currentList[index].useItem()
            _inventory.value = currentList
            saveInventory()
        }
    }
    
    private fun loadInventory() {
        val jsonString = prefs.getString(inventoryKey, null)
        if (jsonString != null) {
            try {
                val jsonArray = JSONArray(jsonString)
                val items = mutableListOf<InventoryItem>()
                
                for (i in 0 until jsonArray.length()) {
                    val jsonObject = jsonArray.getJSONObject(i)
                    val item = InventoryItem(
                        id = jsonObject.getString("id"),
                        name = jsonObject.getString("name"),
                        quantity = jsonObject.getInt("quantity"),
                        description = jsonObject.getString("description")
                    )
                    items.add(item)
                }
                
                _inventory.value = items
            } catch (e: Exception) {
                // If loading fails, start with empty list
                _inventory.value = emptyList()
            }
        }
    }
    
    private fun saveInventory() {
        val jsonArray = JSONArray()
        _inventory.value.forEach { item ->
            val jsonObject = JSONObject().apply {
                put("id", item.id)
                put("name", item.name)
                put("quantity", item.quantity)
                put("description", item.description)
            }
            jsonArray.put(jsonObject)
        }
        
        prefs.edit()
            .putString(inventoryKey, jsonArray.toString())
            .apply()
    }
}
