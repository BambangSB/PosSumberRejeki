package com.sumberrejeki.pos.data.repository

import com.sumberrejeki.pos.data.local.dao.CartDraftDao
import com.sumberrejeki.pos.data.local.entity.CartDraftEntity
import com.sumberrejeki.pos.domain.model.CartItem
import com.sumberrejeki.pos.domain.model.Product
import com.sumberrejeki.pos.domain.repository.CartRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CartRepositoryImpl(
    private val cartDraftDao: CartDraftDao
) : CartRepository {

    override val cartItemsFlow: Flow<List<CartItem>> = cartDraftDao.getAllCartDraftsWithProductFlow().map { list ->
        list.map { it.toDomain() }
    }

    override val totalItemCountFlow: Flow<Int> = cartDraftDao.getTotalItemCountFlow().map { it ?: 0 }

    override val totalCartPriceFlow: Flow<Double> = cartItemsFlow.map { items ->
        items.sumOf { it.subtotal }
    }

    override suspend fun addToCart(product: Product, quantity: Int, notes: String?) {
        val existing = cartDraftDao.getDraftByProductId(product.productId)
        if (existing != null) {
            val newQty = existing.quantity + quantity
            cartDraftDao.updateDraft(existing.copy(quantity = newQty, notes = notes ?: existing.notes))
        } else {
            cartDraftDao.insertDraft(
                CartDraftEntity(
                    productId = product.productId,
                    quantity = quantity,
                    notes = notes
                )
            )
        }
    }

    override suspend fun updateQuantity(draftId: Long, quantity: Int) {
        if (quantity <= 0) {
            cartDraftDao.deleteDraftById(draftId)
        } else {
            val all = cartDraftDao.getAllCartDraftsWithProduct()
            val target = all.find { it.draft.draftId == draftId }
            if (target != null) {
                cartDraftDao.updateDraft(target.draft.copy(quantity = quantity))
            }
        }
    }

    override suspend fun removeFromCart(draftId: Long) {
        cartDraftDao.deleteDraftById(draftId)
    }

    override suspend fun clearCart() {
        cartDraftDao.clearAllDrafts()
    }

    override suspend fun getCartItems(): List<CartItem> {
        return cartDraftDao.getAllCartDraftsWithProduct().map { it.toDomain() }
    }
}
