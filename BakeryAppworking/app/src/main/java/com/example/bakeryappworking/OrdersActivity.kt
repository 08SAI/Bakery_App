package com.example.bakeryappworking

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bakeryappworking.databinding.ActivityOrdersBinding
import com.example.bakeryappworking.databinding.ItemOrderBinding
import com.example.bakeryappworking.databinding.ItemOrderProductBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class OrdersActivity : AppCompatActivity() {
    private lateinit var binding: ActivityOrdersBinding
    private lateinit var ordersAdapter: OrdersAdapter
    private val firebaseManager = FirebaseManager.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOrdersBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupBottomNavigation()
        loadOrders()
    }

    private fun setupRecyclerView() {
        ordersAdapter = OrdersAdapter()
        binding.ordersRecycler.apply {
            layoutManager = LinearLayoutManager(this@OrdersActivity)
            adapter = ordersAdapter
        }
    }

    private fun loadOrders() {
        try {
            val userId = firebaseManager.getCurrentUserId()
            if (userId != null) {
                Log.d("OrdersActivity", "Loading orders for user: $userId")
                // Show loading state
                binding.ordersRecycler.visibility = android.view.View.GONE
                binding.emptyOrdersText.visibility = android.view.View.GONE
                binding.progressBar.visibility = android.view.View.VISIBLE

                firebaseManager.getOrders(userId) { orders ->
                    runOnUiThread {
                        binding.progressBar.visibility = android.view.View.GONE
                        
                        if (orders.isEmpty()) {
                            Log.d("OrdersActivity", "No orders found")
                            binding.emptyOrdersText.visibility = android.view.View.VISIBLE
                            binding.ordersRecycler.visibility = android.view.View.GONE
                        } else {
                            Log.d("OrdersActivity", "Loaded ${orders.size} orders")
                            binding.emptyOrdersText.visibility = android.view.View.GONE
                            binding.ordersRecycler.visibility = android.view.View.VISIBLE
                            ordersAdapter.submitList(orders)
                        }
                    }
                }
            } else {
                Log.e("OrdersActivity", "User not logged in")
                Toast.makeText(this, "Please log in to view orders", Toast.LENGTH_SHORT).show()
                finish()
            }
        } catch (e: Exception) {
            Log.e("OrdersActivity", "Error loading orders", e)
            Toast.makeText(this, "Error loading orders: ${e.message}", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.selectedItemId = R.id.navigation_orders
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
                R.id.navigation_orders -> true
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

class OrdersAdapter : androidx.recyclerview.widget.ListAdapter<Order, OrdersAdapter.OrderViewHolder>(OrderDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val binding = ItemOrderBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return OrderViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class OrderViewHolder(
        private val binding: ItemOrderBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(order: Order) {
            val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
            val orderDate = Date(order.orderDate)
            
            binding.tvOrderId.text = "Order #${order.id.take(8)}"
            binding.tvOrderDate.text = dateFormat.format(orderDate)
            binding.tvOrderStatus.text = order.status
            binding.tvOrderTotal.text = String.format("$%.2f", order.totalAmount)
            
            // Set up items recycler view
            val itemsAdapter = OrderItemsAdapter()
            binding.rvOrderItems.apply {
                layoutManager = LinearLayoutManager(context)
                adapter = itemsAdapter
            }
            itemsAdapter.submitList(order.items)
        }
    }
}

class OrderDiffCallback : androidx.recyclerview.widget.DiffUtil.ItemCallback<Order>() {
    override fun areItemsTheSame(oldItem: Order, newItem: Order): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Order, newItem: Order): Boolean {
        return oldItem == newItem
    }
}

class OrderItemsAdapter : androidx.recyclerview.widget.ListAdapter<CartItem, OrderItemsAdapter.OrderItemViewHolder>(OrderItemDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderItemViewHolder {
        val binding = ItemOrderProductBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return OrderItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OrderItemViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class OrderItemViewHolder(
        private val binding: ItemOrderProductBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: CartItem) {
            binding.tvProductName.text = item.product.title
            binding.tvQuantity.text = "x${item.quantity}"
            binding.tvPrice.text = String.format("$%.2f", item.product.price * item.quantity)
        }
    }
}

class OrderItemDiffCallback : androidx.recyclerview.widget.DiffUtil.ItemCallback<CartItem>() {
    override fun areItemsTheSame(oldItem: CartItem, newItem: CartItem): Boolean {
        return oldItem.product.id == newItem.product.id
    }

    override fun areContentsTheSame(oldItem: CartItem, newItem: CartItem): Boolean {
        return oldItem == newItem
    }
} 