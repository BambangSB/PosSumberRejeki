package com.sumberrejeki.pos.presentation.scan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sumberrejeki.pos.domain.model.Product
import com.sumberrejeki.pos.domain.usecase.cart.AddToCartDraftUseCase
import com.sumberrejeki.pos.domain.usecase.cart.GetCartDraftsUseCase
import com.sumberrejeki.pos.domain.usecase.product.GetProductByBarcodeUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class ScannerNavigationEvent {
    data class NavigateToAddProduct(val barcode: String) : ScannerNavigationEvent()
}

data class ScannerUiState(
    val foundProduct: Product? = null,
    val selectedQuantity: Int = 1,
    val isFlashOn: Boolean = false,
    val message: String? = null
)

class ScannerViewModel(
    private val getProductByBarcodeUseCase: GetProductByBarcodeUseCase,
    private val addToCartDraftUseCase: AddToCartDraftUseCase,
    getCartDraftsUseCase: GetCartDraftsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ScannerUiState())
    val uiState: StateFlow<ScannerUiState> = _uiState.asStateFlow()

    private val _navigationEvent = MutableSharedFlow<ScannerNavigationEvent>()
    val navigationEvent: SharedFlow<ScannerNavigationEvent> = _navigationEvent.asSharedFlow()

    val totalCartItems: StateFlow<Int> = getCartDraftsUseCase.totalItemCountFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val totalCartPrice: StateFlow<Double> = getCartDraftsUseCase.totalCartPriceFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    fun onBarcodeScanned(barcode: String) {
        viewModelScope.launch {
            val product = getProductByBarcodeUseCase(barcode)
            if (product != null) {
                _uiState.value = _uiState.value.copy(
                    foundProduct = product,
                    selectedQuantity = 1,
                    message = null
                )
            } else {
                _navigationEvent.emit(ScannerNavigationEvent.NavigateToAddProduct(barcode))
            }
        }
    }

    fun updateQuantity(quantity: Int) {
        _uiState.value = _uiState.value.copy(selectedQuantity = quantity)
    }

    fun addFoundProductToCart() {
        val product = _uiState.value.foundProduct ?: return
        val quantity = _uiState.value.selectedQuantity

        viewModelScope.launch {
            val result = addToCartDraftUseCase(product, quantity)
            result.onSuccess {
                _uiState.value = _uiState.value.copy(
                    foundProduct = null,
                    selectedQuantity = 1,
                    message = "Berhasil menambahkan ${product.name} (x$quantity)"
                )
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    message = error.message ?: "Gagal menambahkan ke keranjang"
                )
            }
        }
    }

    fun dismissBottomSheet() {
        _uiState.value = _uiState.value.copy(foundProduct = null)
    }

    fun toggleFlash() {
        _uiState.value = _uiState.value.copy(isFlashOn = !_uiState.value.isFlashOn)
    }

    fun clearMessage() {
        _uiState.value = _uiState.value.copy(message = null)
    }
}
