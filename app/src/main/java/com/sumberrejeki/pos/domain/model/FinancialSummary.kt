package com.sumberrejeki.pos.domain.model

data class TopProductSales(
    val productId: Long,
    val productName: String,
    val quantitySold: Int,
    val totalRevenue: Double
)

data class FinancialSummary(
    val totalRevenue: Double = 0.0,
    val totalCostOfGoods: Double = 0.0,
    val grossProfit: Double = 0.0,
    val totalTransactions: Int = 0,
    val cashRevenue: Double = 0.0,
    val cardRevenue: Double = 0.0,
    val topSellingProducts: List<TopProductSales> = emptyList(),
    val averageTransactionValue: Double = if (totalTransactions > 0) totalRevenue / totalTransactions else 0.0
)
