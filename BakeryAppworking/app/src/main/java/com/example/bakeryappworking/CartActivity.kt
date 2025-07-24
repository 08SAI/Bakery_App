package com.example.bakeryappworking

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bakeryappworking.databinding.ActivityCartBinding
import com.google.android.material.button.MaterialButton
import java.text.NumberFormat
import java.util.Locale

class CartActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCartBinding
    private lateinit var cartAdapter: CartAdapter
    private val firebaseManager = FirebaseManager.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            binding = ActivityCartBinding.inflate(layoutInflater)
            setContentView(binding.root)

            setupRecyclerView()
            setupBottomNavigation()
            
            // Add cart listener to update UI when cart changes
            Cart.addListener { updateCart() }
            
            // Load cart items from Firebase
            try {
                Cart.loadItems()
            } catch (e: Exception) {
                Log.e("CartActivity", "Error loading cart items", e)
                Toast.makeText(this, "Error loading cart items", Toast.LENGTH_SHORT).show()
            }
            
            updateCart()
            setupCheckoutButton()
        } catch (e: Exception) {
            Log.e("CartActivity", "Error in onCreate", e)
            Toast.makeText(this, "Error initializing cart", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun setupRecyclerView() {
        try {
            cartAdapter = CartAdapter(
                cartItems = mutableListOf(),
                onQuantityChanged = { updateTotalPrice() },
                onRemoveItem = { item ->
                    try {
                        Cart.removeItem(item)
                        updateTotalPrice()
                    } catch (e: Exception) {
                        Log.e("CartActivity", "Error removing item", e)
                        Toast.makeText(this, "Error removing item", Toast.LENGTH_SHORT).show()
                    }
                }
            )
            binding.cartRecycler.apply {
                layoutManager = LinearLayoutManager(this@CartActivity)
                adapter = cartAdapter
            }
        } catch (e: Exception) {
            Log.e("CartActivity", "Error setting up RecyclerView", e)
            Toast.makeText(this, "Error setting up cart view", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupCheckoutButton() {
        try {
            Log.d("CartActivity", "Setting up checkout button")
            
            binding.btnProceedToCheckout.setOnClickListener {
                Log.d("CartActivity", "Checkout button clicked")
                try {
                    // Check if user is logged in
                    val userId = firebaseManager.getCurrentUserId()
                    Log.d("CartActivity", "Current user ID: $userId")
                    
                    if (userId == null) {
                        Log.w("CartActivity", "User not logged in, redirecting to login")
                        Toast.makeText(this, "Please log in to place an order", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, LoginActivity::class.java))
                        return@setOnClickListener
                    }

                    val items = Cart.getItems()
                    Log.d("CartActivity", "Cart items count: ${items.size}")
                    if (items.isEmpty()) {
                        Log.w("CartActivity", "Cart is empty")
                        Toast.makeText(this, "Your cart is empty", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }

                    // Log cart items details
                    items.forEach { item ->
                        Log.d("CartActivity", "Item in cart: ${item.product.title}, Quantity: ${item.quantity}, Price: ${item.product.price}")
                    }

                    val totalAmount = Cart.getTotalPrice()
                    Log.d("CartActivity", "Total amount: $totalAmount")
                    
                    try {
                        val order = firebaseManager.createOrder(items, totalAmount)
                        Log.d("CartActivity", "Created order with ID: ${order.id}")
                        Log.d("CartActivity", "Order details - User: ${order.userId}, Items: ${order.items.size}, Total: ${order.totalAmount}")
                        
                        // Show loading indicator
                        binding.btnProceedToCheckout.isEnabled = false
                        binding.btnProceedToCheckout.text = "Processing..."
                        
                        firebaseManager.saveOrder(order) { success ->
                            runOnUiThread {
                                binding.btnProceedToCheckout.isEnabled = true
                                binding.btnProceedToCheckout.text = "Proceed to Checkout"
                                
                                if (success) {
                                    Log.d("CartActivity", "Order saved successfully to Firebase")
                                    Cart.clear()
                                    Log.d("CartActivity", "Cart cleared after successful order")
                                    showOrderConfirmation()
                                } else {
                                    Log.e("CartActivity", "Failed to save order to Firebase")
                                    Toast.makeText(this@CartActivity, "Failed to place order", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("CartActivity", "Error creating/saving order", e)
                        Toast.makeText(this, "Error creating order: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Log.e("CartActivity", "Error during checkout", e)
                    Toast.makeText(this, "Error during checkout: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            Log.e("CartActivity", "Error setting up checkout button", e)
            Toast.makeText(this, "Error setting up checkout: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showOrderConfirmation() {
        try {
            Log.d("CartActivity", "Showing order confirmation dialog")
            
            // Create a simple dialog using AlertDialog
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Order Placed Successfully!")
            builder.setMessage("Thank you for your order. You can track your order status in the Orders section.")
            builder.setCancelable(false)
            
            builder.setPositiveButton("Back to Home") { dialog, _ ->
                try {
                    dialog.dismiss()
                    startActivity(Intent(this, HomeActivity::class.java))
                    finish()
                } catch (e: Exception) {
                    Log.e("CartActivity", "Error navigating to home", e)
                }
            }
            
            // Show dialog on the main thread
            runOnUiThread {
                try {
                    builder.create().show()
                } catch (e: Exception) {
                    Log.e("CartActivity", "Error showing dialog", e)
                    Toast.makeText(this, "Error showing confirmation dialog", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            Log.e("CartActivity", "Error in showOrderConfirmation", e)
            Toast.makeText(this, "Error showing confirmation dialog", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateCart() {
        try {
            val items = Cart.getItems()
            cartAdapter.updateItems(items)
            updateTotalPrice()
            
            // Update visibility of views
            if (items.isEmpty()) {
                binding.emptyCartText.visibility = View.VISIBLE
                binding.cartRecycler.visibility = View.GONE
                binding.totalPrice.visibility = View.GONE
                binding.btnProceedToCheckout.visibility = View.GONE
            } else {
                binding.emptyCartText.visibility = View.GONE
                binding.cartRecycler.visibility = View.VISIBLE
                binding.totalPrice.visibility = View.VISIBLE
                binding.btnProceedToCheckout.visibility = View.VISIBLE
            }
        } catch (e: Exception) {
            Log.e("CartActivity", "Error updating cart", e)
            Toast.makeText(this, "Error updating cart", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateTotalPrice() {
        try {
            val total = Cart.getTotalPrice()
            val formattedTotal = NumberFormat.getCurrencyInstance(Locale.US).format(total)
            binding.totalPrice.text = "Total: $formattedTotal"
        } catch (e: Exception) {
            Log.e("CartActivity", "Error updating total price", e)
        }
    }

    private fun setupBottomNavigation() {
        try {
            binding.bottomNavigation.selectedItemId = R.id.navigation_cart
            binding.bottomNavigation.setOnItemSelectedListener { item ->
                when (item.itemId) {
                    R.id.navigation_home -> {
                        startActivity(Intent(this, HomeActivity::class.java))
                        finish()
                        false
                    }
                    R.id.navigation_cart -> true
                    R.id.navigation_orders -> {
                        startActivity(Intent(this, OrdersActivity::class.java))
                        finish()
                        false
                    }
                    R.id.navigation_profile -> {
                        startActivity(Intent(this, ProfileActivity::class.java))
                        finish()
                        false
                    }
                    else -> false
                }
            }
        } catch (e: Exception) {
            Log.e("CartActivity", "Error setting up bottom navigation", e)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Cart.removeListener { updateCart() }
    }
}