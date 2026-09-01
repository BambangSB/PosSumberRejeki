package com.sumberrejeki.pos

import com.sumberrejeki.pos.domain.model.Product
import com.sumberrejeki.pos.domain.model.UserRole
import com.sumberrejeki.pos.domain.repository.ProductRepository
import com.sumberrejeki.pos.domain.usecase.product.DeleteProductUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DeleteProductUseCaseTest {

    private class FakeProductRepository : ProductRepository {
        val deletedIds = mutableListOf<Long>()
        override val allProductsFlow: Flow<List<Product>> = flowOf(emptyList())
        override val allCategoriesFlow: Flow<List<String>> = flowOf(emptyList())

        override fun searchProducts(query: String, category: String?): Flow<List<Product>> = flowOf(emptyList())
        override suspend fun getProductById(productId: Long): Product? = null
        override suspend fun getProductByBarcode(barcode: String): Product? = null
        override suspend fun saveProduct(product: Product): Long = 1L
        override suspend fun deleteProduct(productId: Long) {
            deletedIds.add(productId)
        }
        override suspend fun deleteProducts(productIds: List<Long>) {
            deletedIds.addAll(productIds)
        }
        override suspend fun getProductCount(): Int = 0
    }

    @Test
    fun `admin user can delete product successfully`() = runTest {
        val fakeRepo = FakeProductRepository()
        val useCase = DeleteProductUseCase(fakeRepo)

        val result = useCase(productId = 42L, userRole = UserRole.ADMIN)

        assertTrue(result.isSuccess)
        assertEquals(listOf(42L), fakeRepo.deletedIds)
    }

    @Test
    fun `admin user can delete multiple products successfully`() = runTest {
        val fakeRepo = FakeProductRepository()
        val useCase = DeleteProductUseCase(fakeRepo)

        val result = useCase.deleteMultiple(listOf(10L, 20L, 30L), UserRole.ADMIN)

        assertTrue(result.isSuccess)
        assertEquals(listOf(10L, 20L, 30L), fakeRepo.deletedIds)
    }

    @Test
    fun `cashier user is rejected from deleting product`() = runTest {
        val fakeRepo = FakeProductRepository()
        val useCase = DeleteProductUseCase(fakeRepo)

        val result = useCase(productId = 42L, userRole = UserRole.CASHIER)

        assertFalse(result.isSuccess)
        assertTrue(result.exceptionOrNull() is SecurityException)
        assertTrue(result.exceptionOrNull()?.message?.contains("Hanya Admin") == true)
        assertTrue(fakeRepo.deletedIds.isEmpty())
    }

    @Test
    fun `cashier user is rejected from batch deleting products`() = runTest {
        val fakeRepo = FakeProductRepository()
        val useCase = DeleteProductUseCase(fakeRepo)

        val result = useCase.deleteMultiple(listOf(1L, 2L), UserRole.CASHIER)

        assertFalse(result.isSuccess)
        assertTrue(result.exceptionOrNull() is SecurityException)
        assertTrue(fakeRepo.deletedIds.isEmpty())
    }
}
