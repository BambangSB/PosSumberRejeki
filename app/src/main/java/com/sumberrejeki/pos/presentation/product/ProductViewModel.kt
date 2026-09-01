package com.sumberrejeki.pos.presentation.product

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sumberrejeki.pos.domain.model.CartItem
import com.sumberrejeki.pos.domain.model.Product
import com.sumberrejeki.pos.domain.model.UserRole
import com.sumberrejeki.pos.domain.usecase.cart.AddToCartDraftUseCase
import com.sumberrejeki.pos.domain.usecase.cart.GetCartDraftsUseCase
import com.sumberrejeki.pos.domain.usecase.product.DeleteProductUseCase
import com.sumberrejeki.pos.domain.usecase.product.GetProductByBarcodeUseCase
import com.sumberrejeki.pos.domain.usecase.product.GetProductsUseCase
import com.sumberrejeki.pos.domain.usecase.product.SaveProductUseCase
import com.sumberrejeki.pos.domain.usecase.product.SearchProductsUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ProductCatalogUiState(
    val searchQuery: String = "",
    val selectedCategory: String = "Semua",
    val cartItems: List<CartItem> = emptyList(),
    val totalCartItems: Int = 0,
    val totalCartPrice: Double = 0.0,
    val snackbarMessage: String? = null,
    val isLoading: Boolean = false
)

@OptIn(ExperimentalCoroutinesApi::class)
class ProductViewModel(
    private val getProductsUseCase: GetProductsUseCase,
    private val searchProductsUseCase: SearchProductsUseCase,
    private val getProductByBarcodeUseCase: GetProductByBarcodeUseCase,
    private val saveProductUseCase: SaveProductUseCase,
    private val deleteProductUseCase: DeleteProductUseCase,
    private val addToCartDraftUseCase: AddToCartDraftUseCase,
    getCartDraftsUseCase: GetCartDraftsUseCase
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("Semua")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    val categories: StateFlow<List<String>> = getProductsUseCase.allCategoriesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val products: StateFlow<List<Product>> = combine(_searchQuery, _selectedCategory) { query, category ->
        Pair(query, category)
    }.flatMapLatest { (query, category) ->
        val cat = if (category == "Semua") null else category
        searchProductsUseCase(query, cat)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val cartItems: StateFlow<List<CartItem>> = getCartDraftsUseCase.cartItemsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalCartItems: StateFlow<Int> = getCartDraftsUseCase.totalItemCountFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val totalCartPrice: StateFlow<Double> = getCartDraftsUseCase.totalCartPriceFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun onCategorySelected(category: String) {
        _selectedCategory.value = category
    }

    fun addToCart(product: Product, quantity: Int = 1) {
        viewModelScope.launch {
            val result = addToCartDraftUseCase(product, quantity)
            result.onSuccess {
                _snackbarMessage.value = "${product.name} ditambahkan ke keranjang"
            }.onFailure { error ->
                _snackbarMessage.value = error.message ?: "Gagal menambahkan produk"
            }
        }
    }

    fun saveProduct(product: Product, userRole: UserRole, onComplete: (Result<Long>) -> Unit) {
        viewModelScope.launch {
            val result = saveProductUseCase(product, userRole)
            onComplete(result)
        }
    }

    fun deleteProduct(productId: Long, userRole: UserRole, onComplete: (Result<Unit>) -> Unit) {
        viewModelScope.launch {
            val result = deleteProductUseCase(productId, userRole)
            onComplete(result)
        }
    }

    suspend fun getProductById(productId: Long): Product? {
        return getProductsUseCase.getProductById(productId)
    }

    fun clearSnackbarMessage() {
        _snackbarMessage.value = null
    }
}
