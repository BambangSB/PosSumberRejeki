package com.sumberrejeki.pos.data.repository

import androidx.room.withTransaction
import com.sumberrejeki.pos.data.local.AppDatabase
import com.sumberrejeki.pos.data.local.dao.CartDraftDao
import com.sumberrejeki.pos.data.local.dao.OrderDao
import com.sumberrejeki.pos.data.local.dao.ProductDao
import com.sumberrejeki.pos.data.local.dao.UserDao
import com.sumberrejeki.pos.data.local.entity.OrderEntity
import com.sumberrejeki.pos.data.local.entity.OrderItemEntity
import com.sumberrejeki.pos.domain.model.FinancialSummary
import com.sumberrejeki.pos.domain.model.Order
import com.sumberrejeki.pos.domain.model.TopProductSales
import com.sumberrejeki.pos.domain.repository.OrderRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Calendar

class OrderRepositoryImpl(
    private val db: AppDatabase,
    private val orderDao: OrderDao,
    private val productDao: ProductDao,
    private val cartDraftDao: CartDraftDao,
    private val userDao: UserDao
) : OrderRepository {

    override val allOrdersFlow: Flow<List<Order>> = orderDao.getAllOrdersWithItemsFlow().map { list ->
        list.map { it.toDomain() }
    }

    override val totalOrdersCountFlow: Flow<Int> = orderDao.getTotalOrdersCountFlow()

    private fun getTodayStartTimestamp(): Long {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return calendar.timeInMillis
    }

    override val todayRevenueFlow: Flow<Double> = orderDao.getRevenueSinceFlow(getTodayStartTimestamp()).map { it ?: 0.0 }

    override val todayOrdersCountFlow: Flow<Int> = orderDao.getOrderCountSinceFlow(getTodayStartTimestamp())

    override suspend fun getOrderById(orderId: String): Order? {
        val orderWithItems = orderDao.getOrderWithItemsById(orderId) ?: return null
        val cashier = userDao.getUserById(orderWithItems.order.cashierId)
        val cashierName = cashier?.username ?: "Kasir"
        return orderWithItems.toDomain(cashierName)
    }

    override suspend fun processCheckout(order: Order): Result<Order> {
        return try {
            db.withTransaction {
                // 1. Validate & decrease stock for all items
                for (item in order.items) {
                    val product = productDao.getProductById(item.productId)
                        ?: throw IllegalStateException("Produk '${item.productName}' tidak ditemukan")
                    if (product.stock < item.quantity) {
                        throw IllegalStateException("Stok tidak mencukupi untuk '${item.productName}' (Sisa: ${product.stock}, Diminta: ${item.quantity})")
                    }
                    val affectedRows = productDao.decreaseStock(item.productId, item.quantity)
                    if (affectedRows <= 0) {
                        throw IllegalStateException("Gagal mengurangi stok untuk '${item.productName}'")
                    }
                }

                // 2. Insert Order entity
                val orderEntity = OrderEntity.fromDomain(order)
                orderDao.insertOrder(orderEntity)

                // 3. Insert Order Items
                val itemEntities = order.items.map { OrderItemEntity.fromDomain(it.copy(orderId = order.orderId)) }
                orderDao.insertOrderItems(itemEntities)

                // 4. Clear cart drafts
                cartDraftDao.clearAllDrafts()

                Result.success(order)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getOrdersBetween(startTime: Long, endTime: Long): List<Order> {
        return orderDao.getOrdersBetweenTimestamps(startTime, endTime).map { orderWithItems ->
            val cashier = userDao.getUserById(orderWithItems.order.cashierId)
            orderWithItems.toDomain(cashier?.username ?: "Kasir")
        }
    }

    override suspend fun getFinancialSummary(startTime: Long, endTime: Long): FinancialSummary {
        val rawMetrics = orderDao.getFinancialMetricsBetween(startTime, endTime)
        val rawTopSales = orderDao.getTopSellingProducts(startTime, endTime)

        val totalRevenue = rawMetrics?.totalRevenue ?: 0.0
        val totalTransactions = rawMetrics?.totalTransactions ?: 0
        val cashRevenue = rawMetrics?.cashRevenue ?: 0.0
        val cardRevenue = rawMetrics?.cardRevenue ?: 0.0

        // Calculate COGS from order items in this date range
        val ordersInPeriod = orderDao.getOrdersBetweenTimestamps(startTime, endTime)
        var totalCostOfGoods = 0.0
        for (orderWithItems in ordersInPeriod) {
            for (item in orderWithItems.items) {
                val product = productDao.getProductById(item.productId)
                val unitCost = product?.costPrice ?: (item.priceAtPurchase * 0.7) // estimate fallback
                totalCostOfGoods += unitCost * item.quantity
            }
        }

        val grossProfit = totalRevenue - totalCostOfGoods

        val topSelling = rawTopSales.map {
            TopProductSales(
                productId = it.productId,
                productName = it.productName,
                quantitySold = it.quantitySold,
                totalRevenue = it.totalRevenue
            )
        }

        return FinancialSummary(
            totalRevenue = totalRevenue,
            totalCostOfGoods = totalCostOfGoods,
            grossProfit = grossProfit,
            totalTransactions = totalTransactions,
            cashRevenue = cashRevenue,
            cardRevenue = cardRevenue,
            topSellingProducts = topSelling
        )
    }
}
