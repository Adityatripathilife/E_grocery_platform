package com.example.e_grocery_platform

import androidx.annotation.DrawableRes

data class Item(
    val id: Int,
    val name: String,
    val description: String,
    @DrawableRes val imageResId: Int
)
