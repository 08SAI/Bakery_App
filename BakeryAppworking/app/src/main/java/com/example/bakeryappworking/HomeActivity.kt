package com.example.bakeryappworking

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.bakeryappworking.databinding.ActivityHomeBinding

class HomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomeBinding
    private lateinit var productAdapter: ProductAdapter
    private lateinit var categoryAdapter: CategoryAdapter
    private val products = mutableListOf<Product>()
    private val categories = mutableListOf<Category>()
    private val firebaseManager = FirebaseManager.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerViews()
        setupBottomNavigation()
        loadCategories()
        loadProducts()
    }

    private fun setupRecyclerViews() {
        // Setup Products RecyclerView
        productAdapter = ProductAdapter(
            products = products,
            onItemClick = { product ->
                val intent = Intent(this, ProductDetailActivity::class.java).apply {
                    putExtra("product_id", product.id)
                }
                startActivity(intent)
            },
            onAddToCart = { product, quantity ->
                val cartItem = CartItem(
                    product = product,
                    quantity = quantity
                )
                Cart.addItem(cartItem)
                Toast.makeText(this, "${product.title} (${quantity}) added to cart", Toast.LENGTH_SHORT).show()
            }
        )
        binding.bakeryRecycler.apply {
            layoutManager = LinearLayoutManager(this@HomeActivity)
            adapter = productAdapter
        }

        // Setup Categories RecyclerView
        setupCategoryAdapter()
    }

    private fun setupCategoryAdapter() {
        val categories = listOf(
            Category("All", R.drawable.ic_all),
            Category("Bread", R.drawable.ic_bread),
            Category("Pastry", R.drawable.ic_pastry),
            Category("Cake", R.drawable.ic_cake),
            Category("Cookie", R.drawable.ic_cookie)
        )

        categoryAdapter = CategoryAdapter(categories) { category ->
            if (category.name == "All") {
                // Show all products
                productAdapter.updateProducts(products)
            } else {
                // Filter products by category and scroll to that section
                val filteredProducts = products.filter { it.category == category.name }
                productAdapter.updateProducts(filteredProducts)
                productAdapter.scrollToCategory(category.name, binding.bakeryRecycler)
            }
        }

        binding.categoryRecycler.apply {
            layoutManager = LinearLayoutManager(this@HomeActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = categoryAdapter
        }
    }

    private fun loadProducts() {
        firebaseManager.getBakeryItems { items ->
            products.clear()
            products.addAll(items)
            // Update the adapter with all products initially
            productAdapter.updateProducts(products)
            Log.d("HomeActivity", "Loaded ${items.size} products")
        }
    }

    private fun loadCategories() {
        // Add your categories here
        categories.addAll(listOf(
            Category("All", R.drawable.ic_all),
            Category("Bread", R.drawable.ic_bread),
            Category("Pastry", R.drawable.ic_pastry),
            Category("Cake", R.drawable.ic_cake),
            Category("Cookie", R.drawable.ic_cookie)
        ))
        categoryAdapter.notifyDataSetChanged()
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.selectedItemId = R.id.navigation_home
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> true
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
                R.id.navigation_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    finish()
                    false
                }
                else -> false
            }
        }
    }
}