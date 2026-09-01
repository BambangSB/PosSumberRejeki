package com.sumberrejeki.pos.domain.repository

import com.sumberrejeki.pos.domain.model.CartItem
import com.sumberrejeki.pos.domain.model.Product
import kotlinx.coroutines.flow.Flow

interface CartRepository {
    val cartItemsFlow: Flow<List<CartItem>>
    val totalItemCountFlow: Flow<Int>
    val totalCartPriceFlow: Flow<Double>

    suspend fun addToCart(product: Product, quantity: Int = 1, notes: String? = null)
    suspend fun updateQuantity(draftId: Long, quantity: Int)
    suspend fun removeFromCart(draftId: Long)
    suspend fun clearCart()
    suspend fun getCartItems(): List<CartItem>
}
