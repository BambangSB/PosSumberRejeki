package com.sumberrejeki.pos

import com.sumberrejeki.pos.domain.model.LicenseInfo
import com.sumberrejeki.pos.domain.model.Product
import com.sumberrejeki.pos.domain.model.SubscriptionTier
import com.sumberrejeki.pos.domain.model.UserRole
import com.sumberrejeki.pos.domain.repository.LicenseRepository
import com.sumberrejeki.pos.domain.repository.ProductRepository
import com.sumberrejeki.pos.domain.usecase.product.SaveProductUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SubscriptionLicenseTest {

    @Test
    fun `trial active tier grants full pro access`() {
        val trialInfo = LicenseInfo(
            isTrialActive = true,
            trialDaysRemaining = 3
        )

        assertEquals(SubscriptionTier.PRO_TRIAL, trialInfo.tier)
        assertTrue(trialInfo.isProActive)
        assertEquals(Int.MAX_VALUE, trialInfo.maxProductsAllowed)
        assertTrue(trialInfo.canAddProduct(100))
        assertTrue(trialInfo.canEditProductDetails)
        assertTrue(trialInfo.canViewProfitDetails)
        assertTrue(trialInfo.canExportData)
        assertTrue(trialInfo.canEditStoreName)
        assertTrue(trialInfo.canAddCashier)
    }

    @Test
    fun `expired trial and free tier strictly enforces free limits`() {
        val freeInfo = LicenseInfo(
            isPremium = false,
            isTrialActive = false,
            trialDaysRemaining = 0
        )

        assertEquals(SubscriptionTier.FREE, freeInfo.tier)
        assertFalse(freeInfo.isProActive)
        assertEquals(30, freeInfo.maxProductsAllowed)
        assertTrue(freeInfo.canAddProduct(29))
        assertFalse(freeInfo.canAddProduct(30))
        assertFalse(freeInfo.canEditProductDetails)
        assertFalse(freeInfo.canViewProfitDetails)
        assertFalse(freeInfo.canExportData)
        assertFalse(freeInfo.canEditStoreName)
        assertFalse(freeInfo.canAddCashier)
    }

    @Test
    fun `active pro subscription tier grants full access`() {
        val proInfo = LicenseInfo(
            isPremium = true,
            expiresAt = System.currentTimeMillis() + 86400000L
        )

        assertEquals(SubscriptionTier.PRO, proInfo.tier)
        assertTrue(proInfo.isProActive)
        assertTrue(proInfo.canAddProduct(500))
        assertTrue(proInfo.canEditProductDetails)
        assertTrue(proInfo.canViewProfitDetails)
        assertTrue(proInfo.canExportData)
        assertTrue(proInfo.canEditStoreName)
        assertTrue(proInfo.canAddCashier)
    }

    private class FakeProductRepository(initialProducts: List<Product> = emptyList()) : ProductRepository {
        val savedProducts = initialProducts.toMutableList()
        override val allProductsFlow: Flow<List<Product>> get() = flowOf(savedProducts)
        override val allCategoriesFlow: Flow<List<String>> = flowOf(emptyList())

        override fun searchProducts(query: String, category: String?): Flow<List<Product>> = flowOf(emptyList())
        override suspend fun getProductById(productId: Long): Product? = savedProducts.find { it.productId == productId }
        override suspend fun getProductByBarcode(barcode: String): Product? = savedProducts.find { it.barcode == barcode }
        override suspend fun saveProduct(product: Product): Long {
            val id = if (product.productId == 0L) (savedProducts.size + 1).toLong() else product.productId
            val existingIndex = savedProducts.indexOfFirst { it.productId == id }
            val newP = product.copy(productId = id)
            if (existingIndex >= 0) {
                savedProducts[existingIndex] = newP
            } else {
                savedProducts.add(newP)
            }
            return id
        }
        override suspend fun deleteProduct(productId: Long) {
            savedProducts.removeAll { it.productId == productId }
        }
        override suspend fun deleteProducts(productIds: List<Long>) {
            savedProducts.removeAll { it.productId in productIds }
        }
        override suspend fun getProductCount(): Int = savedProducts.size
    }

    private class FakeTestLicenseRepository(private val licenseInfo: LicenseInfo) : LicenseRepository {
        override val licenseInfoFlow: Flow<LicenseInfo> = flowOf(licenseInfo)
        override suspend fun getInstallationId(): String = "TEST-ID"
        override suspend fun verifyAndActivateLicense(licenseKeyString: String): Result<LicenseInfo> = Result.success(licenseInfo)
        override suspend fun deactivateLicense() {}
        override suspend fun updateStoreName(newStoreName: String) {}
    }

    @Test
    fun `saveProduct blocks adding product beyond 30 on free tier`() = runTest {
        val dummy30 = (1..30).map { i ->
            Product(productId = i.toLong(), barcode = "BC$i", name = "Prod $i", category = "Cat", sellPrice = 1000.0, costPrice = 500.0, stock = 10)
        }
        val prodRepo = FakeProductRepository(dummy30)
        val licenseRepo = FakeTestLicenseRepository(LicenseInfo(isTrialActive = false, isPremium = false))
        val useCase = SaveProductUseCase(prodRepo, licenseRepo)

        val newProduct = Product(
            productId = 0L,
            barcode = "12345",
            name = "Barang Baru",
            category = "Sembako",
            sellPrice = 5000.0,
            costPrice = 4000.0,
            stock = 10
        )

        val result = useCase(newProduct, UserRole.ADMIN)
        assertFalse(result.isSuccess)
        assertTrue(result.exceptionOrNull()?.message?.contains("30 produk") == true)
    }

    @Test
    fun `saveProduct blocks editing existing product on free tier`() = runTest {
        val dummy1 = listOf(Product(productId = 1L, barcode = "12345", name = "Barang Asli", category = "Sembako", sellPrice = 5000.0, costPrice = 4000.0, stock = 10))
        val prodRepo = FakeProductRepository(dummy1)
        val licenseRepo = FakeTestLicenseRepository(LicenseInfo(isTrialActive = false, isPremium = false))
        val useCase = SaveProductUseCase(prodRepo, licenseRepo)

        val editProduct = Product(
            productId = 1L,
            barcode = "12345",
            name = "Barang Edit",
            category = "Sembako",
            sellPrice = 6000.0,
            costPrice = 4000.0,
            stock = 15
        )

        val result = useCase(editProduct, UserRole.ADMIN)
        assertFalse(result.isSuccess)
        assertTrue(result.exceptionOrNull()?.message?.contains("edit detail produk") == true)
    }

    @Test
    fun `saveProduct allows adding 31st product when trial is active`() = runTest {
        val dummy30 = (1..30).map { i ->
            Product(productId = i.toLong(), barcode = "BC$i", name = "Prod $i", category = "Cat", sellPrice = 1000.0, costPrice = 500.0, stock = 10)
        }
        val prodRepo = FakeProductRepository(dummy30)
        val licenseRepo = FakeTestLicenseRepository(LicenseInfo(isTrialActive = true, trialDaysRemaining = 3))
        val useCase = SaveProductUseCase(prodRepo, licenseRepo)

        val newProduct = Product(
            productId = 0L,
            barcode = "99999",
            name = "Barang Trial",
            category = "Sembako",
            sellPrice = 10000.0,
            costPrice = 8000.0,
            stock = 20
        )

        val result = useCase(newProduct, UserRole.ADMIN)
        assertTrue(result.isSuccess)
    }
}
