package com.example.bakeryappworking

data class FirebaseBakeryItem(
    val id: Int = 0,
    val name: String = "",
    val rating: Double = 0.0,
    val distance: String = "",
    val price: Double = 0.0,
    val imageResId: String = ""
) {
    fun toBakeryItem(): BakeryItem {
        return BakeryItem(
            id = id,
            name = name,
            rating = rating,
            distance = distance,
            price = price,
            imageResId = R.drawable.ic_launcher_background // Default image for now
        )
    }
} 