package com.sumberrejeki.pos.data.local.entity

import androidx.room.Embedded
import androidx.room.Relation
import com.sumberrejeki.pos.domain.model.Order

data class OrderWithItems(
    @Embedded
    val order: OrderEntity,

    @Relation(
        parentColumn = "orderId",
        entityColumn = "orderId"
    )
    val items: List<OrderItemEntity>
) {
    fun toDomain(cashierName: String = ""): Order = order.toDomain(
        cashierName = cashierName,
        items = items.map { it.toDomain() }
    )
}
