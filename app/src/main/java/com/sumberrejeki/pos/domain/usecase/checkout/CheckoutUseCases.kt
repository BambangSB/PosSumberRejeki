package com.sumberrejeki.pos.domain.usecase.checkout

import com.sumberrejeki.pos.domain.model.Order
import com.sumberrejeki.pos.domain.model.PaymentMethod
import com.sumberrejeki.pos.domain.repository.OrderRepository
import java.util.UUID

class CalculateChangeUseCase {
    operator fun invoke(totalAmount: Double, amountPaid: Double, paymentMethod: PaymentMethod): Double {
        return when (paymentMethod) {
            PaymentMethod.CARD -> 0.0
            PaymentMethod.CASH -> {
                val change = amountPaid - totalAmount
                if (change < 0.0) 0.0 else change
            }
        }
    }
}

class ProcessCheckoutUseCase(
    private val orderRepository: OrderRepository,
    private val calculateChangeUseCase: CalculateChangeUseCase
) {
    suspend operator fun invoke(
        customerName: String?,
        items: List<com.sumberrejeki.pos.domain.model.OrderItem>,
        totalAmount: Double,
        paymentMethod: PaymentMethod,
        amountPaid: Double,
        cashierId: Long,
        cashierName: String = ""
    ): Result<Order> {
        if (items.isEmpty()) {
            return Result.failure(IllegalStateException("Keranjang belanja kosong"))
        }

        if (paymentMethod == PaymentMethod.CASH && amountPaid < totalAmount) {
            return Result.failure(IllegalArgumentException("Nominal pembayaran kurang dari total belanja"))
        }

        val finalAmountPaid = if (paymentMethod == PaymentMethod.CARD) totalAmount else amountPaid
        val changeAmount = calculateChangeUseCase(totalAmount, finalAmountPaid, paymentMethod)

        val newOrderId = UUID.randomUUID().toString()
        val assignedItems = items.map { it.copy(orderId = newOrderId) }

        val order = Order(
            orderId = newOrderId,
            customerName = customerName?.trim()?.ifEmpty { null },
            totalAmount = totalAmount,
            paymentMethod = paymentMethod,
            amountPaid = finalAmountPaid,
            changeAmount = changeAmount,
            cashierId = cashierId,
            cashierName = cashierName,
            timestamp = System.currentTimeMillis(),
            items = assignedItems
        )

        return orderRepository.processCheckout(order)
    }
}
