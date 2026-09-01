package com.sumberrejeki.pos.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sumberrejeki.pos.domain.model.Order
import com.sumberrejeki.pos.domain.model.User
import com.sumberrejeki.pos.domain.usecase.auth.GetCurrentSessionUseCase
import com.sumberrejeki.pos.domain.usecase.cart.GetCartDraftsUseCase
import com.sumberrejeki.pos.domain.usecase.report.GetDashboardMetricsUseCase
import com.sumberrejeki.pos.domain.usecase.report.GetOrdersHistoryUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class HomeMetrics(
    val todayRevenue: Double = 0.0,
    val todayOrdersCount: Int = 0,
    val totalOrdersCount: Int = 0
)

data class CartSummary(
    val totalItemCount: Int = 0,
    val totalCartPrice: Double = 0.0
)

data class HomeUiState(
    val currentUser: User? = null,
    val todayRevenue: Double = 0.0,
    val todayOrdersCount: Int = 0,
    val totalOrdersCount: Int = 0,
    val cartItemCount: Int = 0,
    val cartTotalPrice: Double = 0.0,
    val recentOrders: List<Order> = emptyList()
)

class HomeViewModel(
    getCurrentSessionUseCase: GetCurrentSessionUseCase,
    getDashboardMetricsUseCase: GetDashboardMetricsUseCase,
    getCartDraftsUseCase: GetCartDraftsUseCase,
    getOrdersHistoryUseCase: GetOrdersHistoryUseCase
) : ViewModel() {

    private val metricsFlow: Flow<HomeMetrics> = combine(
        getDashboardMetricsUseCase.todayRevenueFlow,
        getDashboardMetricsUseCase.todayOrdersCountFlow,
        getDashboardMetricsUseCase.totalOrdersCountFlow
    ) { todayRev, todayOrders, totalOrders ->
        HomeMetrics(todayRev, todayOrders, totalOrders)
    }

    private val cartSummaryFlow: Flow<CartSummary> = combine(
        getCartDraftsUseCase.totalItemCountFlow,
        getCartDraftsUseCase.totalCartPriceFlow
    ) { itemCount, totalPrice ->
        CartSummary(itemCount, totalPrice)
    }

    val uiState: StateFlow<HomeUiState> = combine(
        getCurrentSessionUseCase(),
        metricsFlow,
        cartSummaryFlow,
        getOrdersHistoryUseCase.allOrdersFlow
    ) { user, metrics, cart, orders ->
        HomeUiState(
            currentUser = user,
            todayRevenue = metrics.todayRevenue,
            todayOrdersCount = metrics.todayOrdersCount,
            totalOrdersCount = metrics.totalOrdersCount,
            cartItemCount = cart.totalItemCount,
            cartTotalPrice = cart.totalCartPrice,
            recentOrders = orders.take(5)
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState()
    )
}
