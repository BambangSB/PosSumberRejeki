package com.sumberrejeki.pos.domain.model

data class Order(
    val orderId: String,
    val customerName: String? = null,
    val totalAmount: Double,
    val paymentMethod: PaymentMethod,
    val amountPaid: Double,
    val changeAmount: Double,
    val cashierId: Long,
    val cashierName: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val items: List<OrderItem> = emptyList()
)
