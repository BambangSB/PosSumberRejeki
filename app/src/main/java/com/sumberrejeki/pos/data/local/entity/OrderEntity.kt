package com.sumberrejeki.pos.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.sumberrejeki.pos.domain.model.Order
import com.sumberrejeki.pos.domain.model.PaymentMethod

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey
    val orderId: String,
    val customerName: String? = null,
    val totalAmount: Double,
    val paymentMethod: String, // "CASH" | "CARD"
    val amountPaid: Double,
    val changeAmount: Double,
    val cashierId: Long,
    val timestamp: Long = System.currentTimeMillis()
) {
    fun toDomain(cashierName: String = "", items: List<com.sumberrejeki.pos.domain.model.OrderItem> = emptyList()): Order = Order(
        orderId = orderId,
        customerName = customerName,
        totalAmount = totalAmount,
        paymentMethod = if (paymentMethod.equals("CARD", ignoreCase = true)) PaymentMethod.CARD else PaymentMethod.CASH,
        amountPaid = amountPaid,
        changeAmount = changeAmount,
        cashierId = cashierId,
        cashierName = cashierName,
        timestamp = timestamp,
        items = items
    )

    companion object {
        fun fromDomain(order: Order): OrderEntity = OrderEntity(
            orderId = order.orderId,
            customerName = order.customerName,
            totalAmount = order.totalAmount,
            paymentMethod = order.paymentMethod.name,
            amountPaid = order.amountPaid,
            changeAmount = order.changeAmount,
            cashierId = order.cashierId,
            timestamp = order.timestamp
        )
    }
}
