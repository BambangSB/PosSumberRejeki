package com.sumberrejeki.pos.data.repository

import com.sumberrejeki.pos.data.local.dao.ProductDao
import com.sumberrejeki.pos.data.local.entity.ProductEntity
import com.sumberrejeki.pos.domain.model.Product
import com.sumberrejeki.pos.domain.repository.ProductRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ProductRepositoryImpl(
    private val productDao: ProductDao
) : ProductRepository {

    override val allProductsFlow: Flow<List<Product>> = productDao.getAllProductsFlow().map { list ->
        list.map { it.toDomain() }
    }

    override val allCategoriesFlow: Flow<List<String>> = productDao.getAllCategoriesFlow()

    override fun searchProducts(query: String, category: String?): Flow<List<Product>> {
        val trimmedQuery = query.trim()
        val flow = if (category.isNullOrBlank() || category.equals("Semua", ignoreCase = true)) {
            if (trimmedQuery.isEmpty()) {
                productDao.getAllProductsFlow()
            } else {
                productDao.searchProductsFlow(trimmedQuery)
            }
        } else {
            if (trimmedQuery.isEmpty()) {
                productDao.getProductsByCategoryFlow(category)
            } else {
                productDao.searchProductsWithCategoryFlow(trimmedQuery, category)
            }
        }
        return flow.map { list -> list.map { it.toDomain() } }
    }

    override suspend fun getProductById(productId: Long): Product? {
        return productDao.getProductById(productId)?.toDomain()
    }

    override suspend fun getProductByBarcode(barcode: String): Product? {
        return productDao.getProductByBarcode(barcode.trim())?.toDomain()
    }

    override suspend fun saveProduct(product: Product): Long {
        val entity = ProductEntity.fromDomain(product)
        return productDao.insertProduct(entity)
    }

    override suspend fun deleteProduct(productId: Long) {
        productDao.deleteProductById(productId)
    }

    override suspend fun getProductCount(): Int {
        return productDao.getProductCount()
    }
}
