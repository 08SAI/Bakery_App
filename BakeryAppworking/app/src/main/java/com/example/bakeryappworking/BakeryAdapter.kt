package com.example.bakeryappworking

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.bakeryappworking.databinding.ItemBakeryBinding

class BakeryAdapter(
    private val items: List<BakeryItem>,
    private val onItemClick: (BakeryItem) -> Unit,
    private val onAddToCartClick: (BakeryItem, Int) -> Unit
) : RecyclerView.Adapter<BakeryAdapter.BakeryViewHolder>() {
    
    inner class BakeryViewHolder(
        private val binding: ItemBakeryBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        private var quantity = 0
        
        fun bind(item: BakeryItem) {
            binding.apply {
                itemTitle.text = item.name
                itemRating.text = item.rating.toString()
                itemDistance.text = item.distance
                itemImage.setImageResource(item.imageResId)
                tvQuantity.text = quantity.toString()
                
                root.setOnClickListener { onItemClick(item) }
                
                btnMinus.setOnClickListener {
                    if (quantity > 0) {
                        quantity--
                        tvQuantity.text = quantity.toString()
                    }
                }
                
                btnPlus.setOnClickListener {
                    quantity++
                    tvQuantity.text = quantity.toString()
                }
                
                btnAddToCart.setOnClickListener { 
                    if (quantity > 0) {
                        onAddToCartClick(item, quantity)
                        quantity = 0
                        tvQuantity.text = quantity.toString()
                    }
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BakeryViewHolder {
        val binding = ItemBakeryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return BakeryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BakeryViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size
} 