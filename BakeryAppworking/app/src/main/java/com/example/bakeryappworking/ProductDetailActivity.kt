package com.example.bakeryappworking

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.bakeryappworking.databinding.ActivityProductDetailBinding

class ProductDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProductDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProductDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val bakeryItem = intent.getParcelableExtra<BakeryItem>("bakery_item")
        bakeryItem?.let { item ->
            binding.apply {
                productName.text = item.name
                productRating.text = item.rating.toString()
                productDistance.text = item.distance
                productImage.setImageResource(item.imageResId)
            }
        }
    }
} 