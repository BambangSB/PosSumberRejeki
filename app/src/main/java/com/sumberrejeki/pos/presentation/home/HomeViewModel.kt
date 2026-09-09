package com.sumberrejeki.pos.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sumberrejeki.pos.domain.model.Order
import com.sumberrejeki.pos.domain.model.Product
import com.sumberrejeki.pos.domain.model.User
import com.sumberrejeki.pos.domain.usecase.auth.GetCurrentSessionUseCase
import com.sumberrejeki.pos.domain.usecase.cart.GetCartDraftsUseCase
import com.sumberrejeki.pos.domain.usecase.product.GetProductsUseCase
import com.sumberrejeki.pos.domain.usecase.report.GetDashboardMetricsUseCase
import com.sumberrejeki.pos.domain.usecase.report.GetOrdersHistoryUseCase
import com.sumberrejeki.pos.presentation.components.CurrencyFormatter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

enum class NotificationType {
    LOW_STOCK,
    OUT_OF_STOCK,
    SALES_RECORD,
    SYSTEM_INFO
}

data class HomeNotification(
    val id: String,
    val title: String,
    val message: String,
    val type: NotificationType,
    val timestamp: Long = System.currentTimeMillis(),
    val targetProductId: Long? = null
)

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
    val recentOrders: List<Order> = emptyList(),
    val notifications: List<HomeNotification> = emptyList(),
    val licenseInfo: com.sumberrejeki.pos.domain.model.LicenseInfo = com.sumberrejeki.pos.domain.model.LicenseInfo()
)

class HomeViewModel(
    getCurrentSessionUseCase: GetCurrentSessionUseCase,
    getDashboardMetricsUseCase: GetDashboardMetricsUseCase,
    getCartDraftsUseCase: GetCartDraftsUseCase,
    getOrdersHistoryUseCase: GetOrdersHistoryUseCase,
    getProductsUseCase: GetProductsUseCase,
    getLicenseStatusUseCase: com.sumberrejeki.pos.domain.usecase.license.GetLicenseStatusUseCase? = null
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

    private val licenseFlow = getLicenseStatusUseCase?.licenseInfoFlow
        ?: kotlinx.coroutines.flow.flowOf(com.sumberrejeki.pos.domain.model.LicenseInfo())

    val uiState: StateFlow<HomeUiState> = combine(
        combine(
            getCurrentSessionUseCase(),
            metricsFlow,
            cartSummaryFlow
        ) { user, metrics, cart -> Triple(user, metrics, cart) },
        getOrdersHistoryUseCase.allOrdersFlow,
        getProductsUseCase.allProductsFlow,
        licenseFlow
    ) { (user, metrics, cart), orders, products, license ->
        val notifs = buildNotifications(products, metrics, orders)
        HomeUiState(
            currentUser = user,
            todayRevenue = metrics.todayRevenue,
            todayOrdersCount = metrics.todayOrdersCount,
            totalOrdersCount = metrics.totalOrdersCount,
            cartItemCount = cart.totalItemCount,
            cartTotalPrice = cart.totalCartPrice,
            recentOrders = orders.take(5),
            notifications = notifs,
            licenseInfo = license
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState()
    )

    private fun buildNotifications(
        products: List<Product>,
        metrics: HomeMetrics,
        recentOrders: List<Order>
    ): List<HomeNotification> {
        val list = mutableListOf<HomeNotification>()

        // 1. Out of stock (stock <= 0)
        val outOfStockProducts = products.filter { it.stock <= 0 }
        if (outOfStockProducts.isNotEmpty()) {
            if (outOfStockProducts.size == 1) {
                val p = outOfStockProducts.first()
                list.add(
                    HomeNotification(
                        id = "oos_${p.productId}",
                        title = "Stok Habis: ${p.name}",
                        message = "Produk '${p.name}' telah habis (0 pcs). Segera lakukan restok produk.",
                        type = NotificationType.OUT_OF_STOCK,
                        targetProductId = p.productId
                    )
                )
            } else {
                val sampleNames = outOfStockProducts.take(2).joinToString { it.name }
                list.add(
                    HomeNotification(
                        id = "oos_multiple",
                        title = "${outOfStockProducts.size} Produk Stok Habis!",
                        message = "$sampleNames dan ${outOfStockProducts.size - 2} produk lainnya telah kehabisan stok.",
                        type = NotificationType.OUT_OF_STOCK
                    )
                )
            }
        }

        // 2. Low stock (1..5)
        val lowStockProducts = products.filter { it.stock in 1..5 }
        if (lowStockProducts.isNotEmpty()) {
            if (lowStockProducts.size <= 3) {
                lowStockProducts.forEach { p ->
                    list.add(
                        HomeNotification(
                            id = "low_${p.productId}",
                            title = "Stok Menipis: ${p.name}",
                            message = "Tersisa hanya ${p.stock} pcs di toko/gudang.",
                            type = NotificationType.LOW_STOCK,
                            targetProductId = p.productId
                        )
                    )
                }
            } else {
                val sampleNames = lowStockProducts.take(2).joinToString { "${it.name} (${it.stock} pcs)" }
                list.add(
                    HomeNotification(
                        id = "low_multiple",
                        title = "${lowStockProducts.size} Produk Stok Menipis (<= 5 pcs)",
                        message = "Termasuk $sampleNames. Segera periksa dan tambah stok.",
                        type = NotificationType.LOW_STOCK
                    )
                )
            }
        }

        // 3. Sales Milestones / Highest Record Today
        if (metrics.todayOrdersCount > 0) {
            val milestoneMsg = when {
                metrics.todayOrdersCount >= 20 -> "Luar biasa! Toko mencapai rekor ${metrics.todayOrdersCount} transaksi hari ini dengan omzet ${CurrencyFormatter.formatRupiah(metrics.todayRevenue)}."
                metrics.todayOrdersCount >= 10 -> "Performa Hebat! Hari ini sudah tembus ${metrics.todayOrdersCount} transaksi (${CurrencyFormatter.formatRupiah(metrics.todayRevenue)})."
                else -> "Aktivitas Hari Ini: ${metrics.todayOrdersCount} transaksi berhasil tercatat (Total Omzet: ${CurrencyFormatter.formatRupiah(metrics.todayRevenue)})."
            }
            list.add(
                HomeNotification(
                    id = "sales_today",
                    title = "Pencapaian Transaksi Hari Ini",
                    message = milestoneMsg,
                    type = NotificationType.SALES_RECORD
                )
            )
        }

        // 4. System / Backup Reminder
        list.add(
            HomeNotification(
                id = "sys_backup_reminder",
                title = "Pengingat Cadangan Database",
                message = "Lakukan export backup database secara berkala di menu Pengaturan / Profil agar data penjualan tetap aman.",
                type = NotificationType.SYSTEM_INFO
            )
        )

        return list
    }
}
