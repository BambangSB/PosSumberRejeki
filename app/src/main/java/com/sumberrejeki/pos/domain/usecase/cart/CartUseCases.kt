package com.sumberrejeki.pos.domain.usecase.cart

import com.sumberrejeki.pos.domain.model.CartItem
import com.sumberrejeki.pos.domain.model.Product
import com.sumberrejeki.pos.domain.repository.CartRepository
import kotlinx.coroutines.flow.Flow

class AddToCartDraftUseCase(
    private val cartRepository: CartRepository
) {
    suspend operator fun invoke(product: Product, quantity: Int = 1, notes: String? = null): Result<Unit> {
        if (quantity <= 0) {
            return Result.failure(IllegalArgumentException("Jumlah harus lebih dari 0"))
        }
        if (product.stock < quantity) {
            return Result.failure(IllegalStateException("Stok '${product.name}' tidak mencukupi (Tersedia: ${product.stock})"))
        }
        cartRepository.addToCart(product, quantity, notes)
        return Result.success(Unit)
    }
}

class UpdateCartQtyUseCase(
    private val cartRepository: CartRepository
) {
    suspend operator fun invoke(draftId: Long, newQuantity: Int) {
        cartRepository.updateQuantity(draftId, newQuantity)
    }
}

class RemoveFromCartDraftUseCase(
    private val cartRepository: CartRepository
) {
    suspend operator fun invoke(draftId: Long) {
        cartRepository.removeFromCart(draftId)
    }
}

class ClearCartDraftUseCase(
    private val cartRepository: CartRepository
) {
    suspend operator fun invoke() {
        cartRepository.clearCart()
    }
}

class GetCartDraftsUseCase(
    private val cartRepository: CartRepository
) {
    val cartItemsFlow: Flow<List<CartItem>> = cartRepository.cartItemsFlow
    val totalItemCountFlow: Flow<Int> = cartRepository.totalItemCountFlow
    val totalCartPriceFlow: Flow<Double> = cartRepository.totalCartPriceFlow

    suspend fun getCartItems(): List<CartItem> = cartRepository.getCartItems()
}
