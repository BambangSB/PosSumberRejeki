package com.sumberrejeki.pos.data.local.entity

import androidx.room.Embedded
import androidx.room.Relation
import com.sumberrejeki.pos.domain.model.CartItem

data class CartDraftWithProduct(
    @Embedded
    val draft: CartDraftEntity,

    @Relation(
        parentColumn = "productId",
        entityColumn = "productId"
    )
    val product: ProductEntity
) {
    fun toDomain(): CartItem = CartItem(
        draftId = draft.draftId,
        product = product.toDomain(),
        quantity = draft.quantity,
        notes = draft.notes
    )
}
