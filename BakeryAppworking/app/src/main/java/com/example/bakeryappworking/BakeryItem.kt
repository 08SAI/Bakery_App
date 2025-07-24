package com.example.bakeryappworking

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class BakeryItem(
    val id: Int,
    val name: String,
    val rating: Double,
    val distance: String,
    val price: Double,
    val imageResId: Int
) : Parcelable 