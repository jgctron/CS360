package com.example.cs340final.model

// Data class representing an inventory item
class InventoryItem(val id: Int, val name: String, var quantity: Int) {

    // Function to update the quantity of the item
    fun updateQuantity(newQuantity: Int) {
        if (newQuantity >= 0) {
            quantity = newQuantity
        }
    }

    // Function to decrease the quantity of the item by a specified amount
    fun decreaseQuantity(amount: Int) {
        if (quantity >= amount) {
            quantity -= amount
        }
    }
}
