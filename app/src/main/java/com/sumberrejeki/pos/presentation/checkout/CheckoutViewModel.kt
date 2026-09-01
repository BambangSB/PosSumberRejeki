package com.sumberrejeki.pos.presentation.checkout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sumberrejeki.pos.domain.model.CartItem
import com.sumberrejeki.pos.domain.model.Order
import com.sumberrejeki.pos.domain.model.OrderItem
import com.sumberrejeki.pos.domain.model.PaymentMethod
import com.sumberrejeki.pos.domain.model.User
import com.sumberrejeki.pos.domain.usecase.auth.GetCurrentSessionUseCase
import com.sumberrejeki.pos.domain.usecase.cart.ClearCartDraftUseCase
import com.sumberrejeki.pos.domain.usecase.cart.GetCartDraftsUseCase
import com.sumberrejeki.pos.domain.usecase.cart.RemoveFromCartDraftUseCase
import com.sumberrejeki.pos.domain.usecase.cart.UpdateCartQtyUseCase
import com.sumberrejeki.pos.domain.usecase.checkout.CalculateChangeUseCase
import com.sumberrejeki.pos.domain.usecase.checkout.ProcessCheckoutUseCase
import com.sumberrejeki.pos.domain.usecase.report.GetOrdersHistoryUseCase
import com.sumberrejeki.pos.utils.printer.BluetoothPrinterDevice
import com.sumberrejeki.pos.utils.printer.BluetoothPrinterManager
import com.sumberrejeki.pos.utils.printer.ReceiptFormatter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class CheckoutUiState(
    val customerName: String = "",
    val paymentMethod: PaymentMethod = PaymentMethod.CASH,
    val amountPaidInput: String = "",
    val changeAmount: Double = 0.0,
    val isProcessing: Boolean = false,
    val errorMessage: String? = null,
    val completedOrder: Order? = null,
    val pairedPrinters: List<BluetoothPrinterDevice> = emptyList(),
    val isPrinting: Boolean = false,
    val printMessage: String? = null
)

class CheckoutViewModel(
    private val getCartDraftsUseCase: GetCartDraftsUseCase,
    private val updateCartQtyUseCase: UpdateCartQtyUseCase,
    private val removeFromCartDraftUseCase: RemoveFromCartDraftUseCase,
    private val clearCartDraftUseCase: ClearCartDraftUseCase,
    private val processCheckoutUseCase: ProcessCheckoutUseCase,
    private val calculateChangeUseCase: CalculateChangeUseCase,
    private val getOrdersHistoryUseCase: GetOrdersHistoryUseCase,
    private val printerManager: BluetoothPrinterManager,
    getCurrentSessionUseCase: GetCurrentSessionUseCase
) : ViewModel() {

    val currentUser: StateFlow<User?> = getCurrentSessionUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val cartItems: StateFlow<List<CartItem>> = getCartDraftsUseCase.cartItemsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalCartPrice: StateFlow<Double> = getCartDraftsUseCase.totalCartPriceFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalItemCount: StateFlow<Int> = getCartDraftsUseCase.totalItemCountFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    private val _uiState = MutableStateFlow(CheckoutUiState())
    val uiState: StateFlow<CheckoutUiState> = _uiState.asStateFlow()

    fun onCustomerNameChanged(name: String) {
        _uiState.value = _uiState.value.copy(customerName = name)
    }

    fun onPaymentMethodChanged(method: PaymentMethod) {
        _uiState.value = _uiState.value.copy(paymentMethod = method)
        recalculateChange()
    }

    fun onAmountPaidChanged(amountStr: String) {
        _uiState.value = _uiState.value.copy(
            amountPaidInput = amountStr,
            errorMessage = null
        )
        recalculateChange()
    }

    fun setQuickAmount(amount: Double) {
        _uiState.value = _uiState.value.copy(
            amountPaidInput = amount.toLong().toString(),
            errorMessage = null
        )
        recalculateChange()
    }

    private fun recalculateChange() {
        val total = totalCartPrice.value
        val paid = _uiState.value.amountPaidInput.toDoubleOrNull() ?: 0.0
        val change = calculateChangeUseCase(total, paid, _uiState.value.paymentMethod)
        _uiState.value = _uiState.value.copy(changeAmount = change)
    }

    fun updateCartQuantity(draftId: Long, newQty: Int) {
        viewModelScope.launch {
            updateCartQtyUseCase(draftId, newQty)
            recalculateChange()
        }
    }

    fun removeCartItem(draftId: Long) {
        viewModelScope.launch {
            removeFromCartDraftUseCase(draftId)
            recalculateChange()
        }
    }

    fun clearCart() {
        viewModelScope.launch {
            clearCartDraftUseCase()
        }
    }

    fun processOrder(onSuccess: (Order) -> Unit) {
        val user = currentUser.value
        val items = cartItems.value
        val total = totalCartPrice.value

        if (items.isEmpty()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Keranjang belanja kosong")
            return
        }

        val method = _uiState.value.paymentMethod
        val paid = if (method == PaymentMethod.CARD) total else (_uiState.value.amountPaidInput.toDoubleOrNull() ?: 0.0)

        if (method == PaymentMethod.CASH && paid < total) {
            _uiState.value = _uiState.value.copy(errorMessage = "Uang pembayaran kurang dari total belanja")
            return
        }

        val orderItems = items.map {
            OrderItem(
                orderId = "",
                productId = it.product.productId,
                productName = it.product.name,
                priceAtPurchase = it.product.sellPrice,
                quantity = it.quantity
            )
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isProcessing = true, errorMessage = null)
            val result = processCheckoutUseCase(
                customerName = _uiState.value.customerName,
                items = orderItems,
                totalAmount = total,
                paymentMethod = method,
                amountPaid = paid,
                cashierId = user?.userId ?: 0L,
                cashierName = user?.username ?: "Kasir"
            )

            result.onSuccess { order ->
                _uiState.value = _uiState.value.copy(
                    isProcessing = false,
                    completedOrder = order
                )
                onSuccess(order)
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isProcessing = false,
                    errorMessage = error.message ?: "Gagal memproses transaksi"
                )
            }
        }
    }

    suspend fun loadOrderById(orderId: String): Order? {
        return getOrdersHistoryUseCase.getOrderById(orderId)
    }

    fun loadPairedPrinters() {
        val printers = printerManager.getPairedPrinters()
        _uiState.value = _uiState.value.copy(pairedPrinters = printers)
    }

    fun printReceipt(order: Order, deviceAddress: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isPrinting = true, printMessage = null)
            val receiptBytes = ReceiptFormatter.formatReceiptEscPos(order)
            val result = printerManager.printData(deviceAddress, receiptBytes)
            result.onSuccess {
                _uiState.value = _uiState.value.copy(
                    isPrinting = false,
                    printMessage = "Struk berhasil dicetak!"
                )
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isPrinting = false,
                    printMessage = "Gagal mencetak: ${error.message ?: "Koneksi printer bermasalah"}"
                )
            }
        }
    }

    fun resetCheckoutState() {
        _uiState.value = CheckoutUiState()
    }
}
