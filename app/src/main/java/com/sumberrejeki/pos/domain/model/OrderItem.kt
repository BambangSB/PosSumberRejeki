package com.sumberrejeki.pos.domain.model

data class OrderItem(
    val orderItemId: Long = 0,
    val orderId: String,
    val productId: Long,
    val productName: String,
    val priceAtPurchase: Double,
    val quantity: Int
) {
    val subtotal: Double
        get() = priceAtPurchase * quantity
}
