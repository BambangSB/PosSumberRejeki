package com.sumberrejeki.pos.domain.repository

import com.sumberrejeki.pos.domain.model.FinancialSummary
import com.sumberrejeki.pos.domain.model.Order
import kotlinx.coroutines.flow.Flow

interface OrderRepository {
    val allOrdersFlow: Flow<List<Order>>
    val totalOrdersCountFlow: Flow<Int>
    val todayRevenueFlow: Flow<Double>
    val todayOrdersCountFlow: Flow<Int>

    suspend fun getOrderById(orderId: String): Order?
    suspend fun processCheckout(order: Order): Result<Order>
    suspend fun getOrdersBetween(startTime: Long, endTime: Long): List<Order>
    suspend fun getFinancialSummary(startTime: Long, endTime: Long): FinancialSummary
}
