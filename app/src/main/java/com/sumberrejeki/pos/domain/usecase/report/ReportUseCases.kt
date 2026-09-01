package com.sumberrejeki.pos.domain.usecase.report

import com.sumberrejeki.pos.domain.model.FinancialSummary
import com.sumberrejeki.pos.domain.model.Order
import com.sumberrejeki.pos.domain.model.UserRole
import com.sumberrejeki.pos.domain.repository.OrderRepository
import kotlinx.coroutines.flow.Flow
import java.util.Calendar

data class DashboardMetrics(
    val todayRevenue: Double,
    val todayOrdersCount: Int,
    val totalOrdersCount: Int
)

class GetDashboardMetricsUseCase(
    private val orderRepository: OrderRepository
) {
    val todayRevenueFlow: Flow<Double> = orderRepository.todayRevenueFlow
    val todayOrdersCountFlow: Flow<Int> = orderRepository.todayOrdersCountFlow
    val totalOrdersCountFlow: Flow<Int> = orderRepository.totalOrdersCountFlow
}

class GetOrdersHistoryUseCase(
    private val orderRepository: OrderRepository
) {
    val allOrdersFlow: Flow<List<Order>> = orderRepository.allOrdersFlow

    suspend fun getOrderById(orderId: String): Order? = orderRepository.getOrderById(orderId)

    suspend fun getOrdersBetween(startTime: Long, endTime: Long): List<Order> {
        return orderRepository.getOrdersBetween(startTime, endTime)
    }
}

class GetFinancialReportUseCase(
    private val orderRepository: OrderRepository
) {
    suspend operator fun invoke(
        userRole: UserRole,
        startTime: Long = getStartOfDay(),
        endTime: Long = System.currentTimeMillis()
    ): Result<FinancialSummary> {
        // RBAC: Restricted ONLY to ADMIN
        if (userRole != UserRole.ADMIN) {
            return Result.failure(SecurityException("Akses Ditolak: Laporan keuangan hanya dapat diakses oleh Admin."))
        }

        return try {
            val summary = orderRepository.getFinancialSummary(startTime, endTime)
            Result.success(summary)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    companion object {
        fun getStartOfDay(): Long {
            val cal = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            return cal.timeInMillis
        }

        fun getStartOfMonth(): Long {
            val cal = Calendar.getInstance().apply {
                set(Calendar.DAY_OF_MONTH, 1)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            return cal.timeInMillis
        }
    }
}
