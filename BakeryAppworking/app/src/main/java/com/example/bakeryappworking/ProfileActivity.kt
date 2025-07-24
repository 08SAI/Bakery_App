package com.example.bakeryappworking

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.bakeryappworking.databinding.ActivityProfileBinding
import com.google.firebase.auth.FirebaseAuth
import java.text.NumberFormat
import java.util.Locale

class ProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProfileBinding
    private val firebaseManager = FirebaseManager.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupProfile()
        setupSignOutButton()
        setupBottomNavigation()
        loadUserStats()
    }

    private fun setupProfile() {
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            // Set name and email
            binding.tvName.text = user.displayName ?: "User"
            binding.tvEmail.text = user.email

            // Load profile photo
            user.photoUrl?.let { photoUrl ->
                Glide.with(this)
                    .load(photoUrl)
                    .circleCrop()
                    .into(binding.ivProfilePhoto)
            }
        } else {
            // Handle case when user is not logged in
            Toast.makeText(this, "Please sign in to view profile", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun setupSignOutButton() {
        binding.btnSignOut.setOnClickListener {
            try {
                firebaseManager.signOut()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            } catch (e: Exception) {
                Log.e("ProfileActivity", "Error signing out", e)
                Toast.makeText(this, "Error signing out", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadUserStats() {
        val userId = firebaseManager.getCurrentUserId()
        if (userId != null) {
            // Load orders count
            firebaseManager.getOrders(userId) { orders ->
                binding.tvOrdersCount.text = orders.size.toString()
                
                // Calculate total spent
                val totalSpent = orders.sumOf { it.totalAmount }
                val formattedTotal = NumberFormat.getCurrencyInstance(Locale.US).format(totalSpent)
                binding.tvTotalSpent.text = formattedTotal
            }
        }
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.selectedItemId = R.id.navigation_profile
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    startActivity(Intent(this, HomeActivity::class.java))
                    finish()
                    false
                }
                R.id.navigation_cart -> {
                    startActivity(Intent(this, CartActivity::class.java))
                    finish()
                    false
                }
                R.id.navigation_orders -> {
                    startActivity(Intent(this, OrdersActivity::class.java))
                    finish()
                    false
                }
                R.id.navigation_profile -> true
                else -> false
            }
        }
    }
} 