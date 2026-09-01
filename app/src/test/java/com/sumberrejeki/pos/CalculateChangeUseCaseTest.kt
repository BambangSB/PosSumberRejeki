package com.sumberrejeki.pos

import com.sumberrejeki.pos.domain.model.PaymentMethod
import com.sumberrejeki.pos.domain.usecase.checkout.CalculateChangeUseCase
import org.junit.Assert.assertEquals
import org.junit.Test

class CalculateChangeUseCaseTest {

    private val calculateChangeUseCase = CalculateChangeUseCase()

    @Test
    fun `cash payment with excess cash returns correct change`() {
        val total = 37500.0
        val paid = 50000.0
        val change = calculateChangeUseCase(total, paid, PaymentMethod.CASH)

        assertEquals(12500.0, change, 0.001)
    }

    @Test
    fun `cash payment with exact money returns zero change`() {
        val total = 50000.0
        val paid = 50000.0
        val change = calculateChangeUseCase(total, paid, PaymentMethod.CASH)

        assertEquals(0.0, change, 0.001)
    }

    @Test
    fun `card payment always returns zero change`() {
        val total = 100000.0
        val paid = 100000.0
        val change = calculateChangeUseCase(total, paid, PaymentMethod.CARD)

        assertEquals(0.0, change, 0.001)
    }
}
