package com.sumberrejeki.pos.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.sumberrejeki.pos.data.local.entity.OrderEntity
import com.sumberrejeki.pos.data.local.entity.OrderItemEntity
import com.sumberrejeki.pos.data.local.entity.OrderWithItems
import kotlinx.coroutines.flow.Flow

data class TopProductSalesRaw(
    val productId: Long,
    val productName: String,
    val quantitySold: Int,
    val totalRevenue: Double
)

data class FinancialMetricsRaw(
    val totalRevenue: Double?,
    val totalTransactions: Int,
    val cashRevenue: Double?,
    val cardRevenue: Double?
)

@Dao
interface OrderDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: OrderEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrderItems(items: List<OrderItemEntity>)

    @Transaction
    @Query("SELECT * FROM orders ORDER BY timestamp DESC")
    fun getAllOrdersWithItemsFlow(): Flow<List<OrderWithItems>>

    @Transaction
    @Query("SELECT * FROM orders WHERE timestamp >= :startTime AND timestamp <= :endTime ORDER BY timestamp DESC")
    fun getOrdersBetweenTimestampsFlow(startTime: Long, endTime: Long): Flow<List<OrderWithItems>>

    @Transaction
    @Query("SELECT * FROM orders WHERE timestamp >= :startTime AND timestamp <= :endTime ORDER BY timestamp DESC")
    suspend fun getOrdersBetweenTimestamps(startTime: Long, endTime: Long): List<OrderWithItems>

    @Transaction
    @Query("SELECT * FROM orders WHERE orderId = :orderId LIMIT 1")
    suspend fun getOrderWithItemsById(orderId: String): OrderWithItems?

    @Query("SELECT * FROM order_items WHERE orderId = :orderId")
    suspend fun getOrderItemsForOrder(orderId: String): List<OrderItemEntity>

    @Query("SELECT COUNT(*) FROM orders")
    fun getTotalOrdersCountFlow(): Flow<Int>

    @Query("SELECT SUM(totalAmount) FROM orders WHERE timestamp >= :startTime")
    fun getRevenueSinceFlow(startTime: Long): Flow<Double?>

    @Query("SELECT COUNT(*) FROM orders WHERE timestamp >= :startTime")
    fun getOrderCountSinceFlow(startTime: Long): Flow<Int>

    @Query("""
        SELECT 
            SUM(totalAmount) as totalRevenue,
            COUNT(*) as totalTransactions,
            SUM(CASE WHEN paymentMethod = 'CASH' THEN totalAmount ELSE 0 END) as cashRevenue,
            SUM(CASE WHEN paymentMethod = 'CARD' THEN totalAmount ELSE 0 END) as cardRevenue
        FROM orders
        WHERE timestamp >= :startTime AND timestamp <= :endTime
    """)
    suspend fun getFinancialMetricsBetween(startTime: Long, endTime: Long): FinancialMetricsRaw?

    @Query("""
        SELECT 
            oi.productId,
            oi.productName,
            SUM(oi.quantity) as quantitySold,
            SUM(oi.priceAtPurchase * oi.quantity) as totalRevenue
        FROM order_items oi
        INNER JOIN orders o ON oi.orderId = o.orderId
        WHERE o.timestamp >= :startTime AND o.timestamp <= :endTime
        GROUP BY oi.productId, oi.productName
        ORDER BY quantitySold DESC
        LIMIT :limit
    """)
    suspend fun getTopSellingProducts(startTime: Long, endTime: Long, limit: Int = 5): List<TopProductSalesRaw>
}
