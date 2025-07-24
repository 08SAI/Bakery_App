package com.example.bakeryappworking

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.bakeryappworking.databinding.ItemCartBinding

class CartAdapter(
    private var cartItems: MutableList<CartItem>,
    private val onQuantityChanged: (CartItem) -> Unit,
    private val onRemoveItem: (CartItem) -> Unit
) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    inner class CartViewHolder(
        private val binding: ItemCartBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(cartItem: CartItem) {
            binding.apply {
                productName.text = cartItem.product.title
                productPrice.text = "$${String.format("%.2f", cartItem.product.price)}"
                quantityText.text = cartItem.quantity.toString()
                
                // Load image using Glide
                Glide.with(productImage.context)
                    .load(cartItem.product.imageUrl)
                    .centerCrop()
                    .into(productImage)

                decreaseButton.setOnClickListener {
                    if (cartItem.quantity > 1) {
                        cartItem.quantity--
                        quantityText.text = cartItem.quantity.toString()
                        onQuantityChanged(cartItem)
                    }
                }

                increaseButton.setOnClickListener {
                    cartItem.quantity++
                    quantityText.text = cartItem.quantity.toString()
                    onQuantityChanged(cartItem)
                }

                removeButton.setOnClickListener {
                    onRemoveItem(cartItem)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val binding = ItemCartBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CartViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        holder.bind(cartItems[position])
    }

    override fun getItemCount(): Int = cartItems.size

    fun updateItems(newItems: List<CartItem>) {
        cartItems.clear()
        cartItems.addAll(newItems)
        notifyDataSetChanged()
    }
} 