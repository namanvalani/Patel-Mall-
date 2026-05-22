package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey val orderId: String, // e.g. "PM-2026-1025"
    val customerName: String,
    val customerPhone: String,
    val deliveryAddress: String,
    val itemsSummary: String, // Stringified description of items or serialised list
    val subtotal: Double,
    val gstAmount: Double, // GST/tax calculation
    val deliveryCharges: Double,
    val couponCode: String? = null,
    val discountAmount: Double = 0.0,
    val totalAmount: Double,
    val paymentMethod: String, // UPI, Google Pay, PhonePe, Paytm, Cards, Cash on Delivery
    val paymentStatus: String, // Paid, Pending
    val orderStatus: String, // Order received, Packing, Out for delivery, Delivered
    val timestamp: Long = System.currentTimeMillis(),
    val estimatedDeliveryTimeMinutes: Int = 15,
    val deliveryBoyName: String? = null,
    val deliveryBoyPhone: String? = null,
    val deliveryBoyId: Int = 0,
    // Live tracking simulation coordinates
    val currentLat: Double = 22.1384, // Base: Kotda Jadodar latitude
    val currentLng: Double = 69.9576, // Base: Kotda Jadodar longitude
    val rating: Int = 0 // customer rating representation
)
