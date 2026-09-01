package com.sumberrejeki.pos.domain.usecase.product

import com.sumberrejeki.pos.domain.model.Product
import com.sumberrejeki.pos.domain.model.UserRole
import com.sumberrejeki.pos.domain.repository.ProductRepository
import kotlinx.coroutines.flow.Flow

class GetProductsUseCase(
    private val productRepository: ProductRepository
) {
    val allProductsFlow: Flow<List<Product>> = productRepository.allProductsFlow
    val allCategoriesFlow: Flow<List<String>> = productRepository.allCategoriesFlow

    suspend fun getProductById(productId: Long): Product? = productRepository.getProductById(productId)
}

class SearchProductsUseCase(
    private val productRepository: ProductRepository
) {
    operator fun invoke(query: String, category: String?): Flow<List<Product>> {
        return productRepository.searchProducts(query, category)
    }
}

class GetProductByBarcodeUseCase(
    private val productRepository: ProductRepository
) {
    suspend operator fun invoke(barcode: String): Product? {
        return productRepository.getProductByBarcode(barcode.trim())
    }
}

class SaveProductUseCase(
    private val productRepository: ProductRepository
) {
    suspend operator fun invoke(product: Product, userRole: UserRole): Result<Long> {
        // RBAC: Check Admin role for creating/updating products
        if (userRole != UserRole.ADMIN) {
            return Result.failure(SecurityException("Hanya Admin yang berwenang menambah atau mengubah produk"))
        }

        if (product.name.isBlank()) {
            return Result.failure(IllegalArgumentException("Nama produk tidak boleh kosong"))
        }
        if (product.barcode.isBlank()) {
            return Result.failure(IllegalArgumentException("Barcode tidak boleh kosong"))
        }
        if (product.sellPrice <= 0) {
            return Result.failure(IllegalArgumentException("Harga jual harus lebih dari 0"))
        }
        if (product.costPrice < 0) {
            return Result.failure(IllegalArgumentException("Harga modal tidak boleh negatif"))
        }
        if (product.stock < 0) {
            return Result.failure(IllegalArgumentException("Stok tidak boleh negatif"))
        }

        val existingWithBarcode = productRepository.getProductByBarcode(product.barcode)
        if (existingWithBarcode != null && existingWithBarcode.productId != product.productId) {
            return Result.failure(IllegalStateException("Barcode '${product.barcode}' sudah digunakan oleh produk '${existingWithBarcode.name}'"))
        }

        val id = productRepository.saveProduct(product)
        return Result.success(id)
    }
}

class DeleteProductUseCase(
    private val productRepository: ProductRepository
) {
    suspend operator fun invoke(productId: Long, userRole: UserRole): Result<Unit> {
        // RBAC: Only Admin can delete products
        if (userRole != UserRole.ADMIN) {
            return Result.failure(SecurityException("Hanya Admin yang berwenang menghapus produk"))
        }

        productRepository.deleteProduct(productId)
        return Result.success(Unit)
    }

    suspend fun deleteMultiple(productIds: List<Long>, userRole: UserRole): Result<Unit> {
        if (userRole != UserRole.ADMIN) {
            return Result.failure(SecurityException("Hanya Admin yang berwenang menghapus produk"))
        }
        if (productIds.isEmpty()) return Result.success(Unit)

        productRepository.deleteProducts(productIds)
        return Result.success(Unit)
    }
}
