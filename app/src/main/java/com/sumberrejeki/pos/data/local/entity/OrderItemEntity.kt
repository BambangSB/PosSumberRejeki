package com.sumberrejeki.pos.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.sumberrejeki.pos.domain.model.OrderItem

@Entity(
    tableName = "order_items",
    foreignKeys = [
        ForeignKey(
            entity = OrderEntity::class,
            parentColumns = ["orderId"],
            childColumns = ["orderId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["orderId"])]
)
data class OrderItemEntity(
    @PrimaryKey(autoGenerate = true)
    val orderItemId: Long = 0,
    val orderId: String,
    val productId: Long,
    val productName: String,
    val priceAtPurchase: Double,
    val quantity: Int
) {
    fun toDomain(): OrderItem = OrderItem(
        orderItemId = orderItemId,
        orderId = orderId,
        productId = productId,
        productName = productName,
        priceAtPurchase = priceAtPurchase,
        quantity = quantity
    )

    companion object {
        fun fromDomain(item: OrderItem): OrderItemEntity = OrderItemEntity(
            orderItemId = item.orderItemId,
            orderId = item.orderId,
            productId = item.productId,
            productName = item.productName,
            priceAtPurchase = item.priceAtPurchase,
            quantity = item.quantity
        )
    }
}
