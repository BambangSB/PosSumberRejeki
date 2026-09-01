package com.sumberrejeki.pos.domain.model

data class CartItem(
    val draftId: Long = 0,
    val product: Product,
    val quantity: Int,
    val notes: String? = null
) {
    val subtotal: Double
        get() = product.sellPrice * quantity
}
