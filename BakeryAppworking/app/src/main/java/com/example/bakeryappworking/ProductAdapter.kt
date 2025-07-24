package com.example.bakeryappworking

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton

class ProductAdapter(
    private val products: List<Product>,
    private val onItemClick: (Product) -> Unit,
    private val onAddToCart: (Product, Int) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val cartItems = mutableMapOf<String, Int>() // Product ID to quantity
    private val items = mutableListOf<Any>()
    private val categoryPositions = mutableMapOf<String, Int>()

    companion object {
        private const val VIEW_TYPE_HEADER = 0
        private const val VIEW_TYPE_PRODUCT = 1
    }

    init {
        // Group products by category
        val groupedProducts = products.groupBy { it.category }
        var position = 0

        groupedProducts.forEach { (category, products) ->
            // Add category header
            items.add(category)
            categoryPositions[category] = position
            position++

            // Add products for this category
            items.addAll(products)
            position += products.size
        }
    }

    class ProductViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val image: ImageView = view.findViewById(R.id.product_image)
        val title: TextView = view.findViewById(R.id.product_title)
        val category: TextView = view.findViewById(R.id.product_category)
        val price: TextView = view.findViewById(R.id.product_price)
        val quantityText: TextView = view.findViewById(R.id.quantityText)
        val decreaseButton: ImageButton = view.findViewById(R.id.decreaseButton)
        val increaseButton: ImageButton = view.findViewById(R.id.increaseButton)
        val addToCartButton: MaterialButton = view.findViewById(R.id.add_to_cart_button)
    }

    class CategoryHeaderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val headerText: TextView = view.findViewById(R.id.category_header)
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is String -> VIEW_TYPE_HEADER
            is Product -> VIEW_TYPE_PRODUCT
            else -> throw IllegalArgumentException("Invalid item type")
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_HEADER -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_category_header, parent, false)
                CategoryHeaderViewHolder(view)
            }
            VIEW_TYPE_PRODUCT -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_product, parent, false)
                ProductViewHolder(view)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is CategoryHeaderViewHolder -> {
                val category = items[position] as String
                holder.headerText.text = category
            }
            is ProductViewHolder -> {
                val product = items[position] as Product
                
                // Load image using Glide
                Glide.with(holder.image.context)
                    .load(product.imageUrl)
                    .centerCrop()
                    .into(holder.image)
                    
                holder.title.text = product.title
                holder.category.text = product.category
                holder.price.text = "$${String.format("%.2f", product.price)}"

                // Set initial quantity
                val quantity = cartItems[product.id] ?: 0
                holder.quantityText.text = quantity.toString()

                // Set click listeners
                holder.itemView.setOnClickListener { onItemClick(product) }
                
                holder.decreaseButton.setOnClickListener {
                    val currentQuantity = holder.quantityText.text.toString().toInt()
                    if (currentQuantity > 0) {
                        val newQuantity = currentQuantity - 1
                        holder.quantityText.text = newQuantity.toString()
                        cartItems[product.id] = newQuantity
                    }
                }

                holder.increaseButton.setOnClickListener {
                    val currentQuantity = holder.quantityText.text.toString().toInt()
                    val newQuantity = currentQuantity + 1
                    holder.quantityText.text = newQuantity.toString()
                    cartItems[product.id] = newQuantity
                }

                holder.addToCartButton.setOnClickListener {
                    val quantity = holder.quantityText.text.toString().toInt()
                    if (quantity > 0) {
                        onAddToCart(product, quantity)
                        holder.quantityText.text = "0"
                        cartItems[product.id] = 0
                    }
                }
            }
        }
    }

    override fun getItemCount() = items.size

    fun scrollToCategory(category: String, recyclerView: RecyclerView) {
        val position = categoryPositions[category] ?: return
        recyclerView.smoothScrollToPosition(position)
    }

    fun updateProducts(newProducts: List<Product>) {
        items.clear()
        categoryPositions.clear()
        
        // Group products by category
        val groupedProducts = newProducts.groupBy { it.category }
        var position = 0

        groupedProducts.forEach { (category, products) ->
            // Add category header
            items.add(category)
            categoryPositions[category] = position
            position++

            // Add products for this category
            items.addAll(products)
            position += products.size
        }
        
        notifyDataSetChanged()
    }
} 