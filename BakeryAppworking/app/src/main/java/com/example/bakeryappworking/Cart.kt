package com.example.bakeryappworking

import android.util.Log

data class CartItem(
    val product: Product = Product(),
    var quantity: Int = 1
) {
    constructor() : this(Product(), 1) // No-arg constructor for Firebase
    val totalPrice: Double
        get() = product.price * quantity
}

object Cart {
    private val items = mutableListOf<CartItem>()
    private val listeners = mutableListOf<() -> Unit>()
    private val firebaseManager = FirebaseManager.getInstance()

    fun addListener(listener: () -> Unit) {
        listeners.add(listener)
    }

    fun removeListener(listener: () -> Unit) {
        listeners.remove(listener)
    }

    private fun notifyListeners() {
        listeners.forEach { it() }
    }

    fun addItem(item: CartItem) {
        try {
            // Check if item already exists in cart
            val existingItem = items.find { it.product.id == item.product.id }
            if (existingItem != null) {
                existingItem.quantity += item.quantity
                Log.d("Cart", "Updated existing item: ${item.product.title}, new quantity: ${existingItem.quantity}")
            } else {
                items.add(item)
                Log.d("Cart", "Added new item: ${item.product.title}, quantity: ${item.quantity}")
            }
            Log.d("Cart", "Total items in cart: ${items.size}")
            
            // Save to Firebase
            val userId = firebaseManager.getCurrentUserId()
            if (userId != null) {
                firebaseManager.saveCartItems(userId, items) { success ->
                    if (!success) {
                        Log.e("Cart", "Failed to save cart items to Firebase")
                    }
                }
            } else {
                Log.w("Cart", "Cannot save to Firebase: User not logged in")
            }
            
            notifyListeners()
        } catch (e: Exception) {
            Log.e("Cart", "Error adding item to cart", e)
        }
    }

    fun removeItem(item: CartItem) {
        try {
            items.remove(item)
            Log.d("Cart", "Removed item: ${item.product.title}")
            
            // Save to Firebase
            val userId = firebaseManager.getCurrentUserId()
            if (userId != null) {
                firebaseManager.saveCartItems(userId, items) { success ->
                    if (!success) {
                        Log.e("Cart", "Failed to save cart items to Firebase")
                    }
                }
            } else {
                Log.w("Cart", "Cannot save to Firebase: User not logged in")
            }
            
            notifyListeners()
        } catch (e: Exception) {
            Log.e("Cart", "Error removing item from cart", e)
        }
    }

    fun getItems(): List<CartItem> {
        Log.d("Cart", "Getting items, total: ${items.size}")
        return items.toList()
    }

    fun clear() {
        try {
            items.clear()
            Log.d("Cart", "Cart cleared")
            
            // Save to Firebase
            val userId = firebaseManager.getCurrentUserId()
            if (userId != null) {
                firebaseManager.saveCartItems(userId, items) { success ->
                    if (!success) {
                        Log.e("Cart", "Failed to save cart items to Firebase")
                    }
                }
            } else {
                Log.w("Cart", "Cannot save to Firebase: User not logged in")
            }
            
            notifyListeners()
        } catch (e: Exception) {
            Log.e("Cart", "Error clearing cart", e)
        }
    }

    fun getTotalPrice(): Double = items.sumOf { it.totalPrice }
    
    fun loadItems() {
        try {
            val userId = firebaseManager.getCurrentUserId()
            if (userId != null) {
                Log.d("Cart", "Loading cart items for user: $userId")
                firebaseManager.getCartItems(userId) { loadedItems ->
                    try {
                        items.clear()
                        items.addAll(loadedItems)
                        Log.d("Cart", "Loaded ${loadedItems.size} items from Firebase")
                        notifyListeners()
                    } catch (e: Exception) {
                        Log.e("Cart", "Error processing loaded items", e)
                    }
                }
            } else {
                Log.w("Cart", "Cannot load items: User not logged in")
            }
        } catch (e: Exception) {
            Log.e("Cart", "Error loading cart items", e)
        }
    }
} 