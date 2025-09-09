package com.kriptogan.pocketsmonsters.data.models

data class InventoryItem(
    val id: String = "",
    val name: String = "",
    val quantity: Int = 0,
    val description: String = ""
) {
    fun useItem(): InventoryItem {
        return if (quantity > 0) {
            copy(quantity = quantity - 1)
        } else {
            this
        }
    }
    
    fun updateQuantity(newQuantity: Int): InventoryItem {
        return copy(quantity = maxOf(0, newQuantity))
    }
    
    fun updateName(newName: String): InventoryItem {
        return copy(name = newName.trim())
    }
    
    fun updateDescription(newDescription: String): InventoryItem {
        return copy(description = newDescription.trim())
    }
}
