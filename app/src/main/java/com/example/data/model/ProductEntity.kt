package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nameEn: String,
    val nameGu: String,
    val categoryEn: String,
    val categoryGu: String,
    val price: Double,
    val discountPercent: Int = 0,
    val stock: Int = 50,
    val variantUnit: String, // kg, gram, liter, packet, pieces
    val variantSize: String, // e.g. "1", "500", "2"
    val imageUrlPath: String? = null, // Path or fallback icon identifier
    val isDailyEssential: Boolean = false,
    val isFeatured: Boolean = false,
    val isRecentlyOrdered: Boolean = false
) {
    val discountedPrice: Double
        get() = if (discountPercent > 0) {
            price * (1 - discountPercent / 100.0)
        } else {
            price
        }
}
