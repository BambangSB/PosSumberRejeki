package com.sumberrejeki.pos.domain.repository

import com.sumberrejeki.pos.domain.model.Product
import kotlinx.coroutines.flow.Flow

interface ProductRepository {
    val allProductsFlow: Flow<List<Product>>
    val allCategoriesFlow: Flow<List<String>>

    fun searchProducts(query: String, category: String?): Flow<List<Product>>
    suspend fun getProductById(productId: Long): Product?
    suspend fun getProductByBarcode(barcode: String): Product?
    suspend fun saveProduct(product: Product): Long
    suspend fun deleteProduct(productId: Long)
    suspend fun deleteProducts(productIds: List<Long>)
    suspend fun getProductCount(): Int
}
