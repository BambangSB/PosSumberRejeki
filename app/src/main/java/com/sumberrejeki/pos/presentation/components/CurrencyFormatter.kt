package com.sumberrejeki.pos.presentation.components

import java.text.NumberFormat
import java.util.Locale

object CurrencyFormatter {
    private val indonesianLocale = Locale("in", "ID")

    fun formatRupiah(amount: Double): String {
        val format = NumberFormat.getCurrencyInstance(indonesianLocale).apply {
            maximumFractionDigits = 0
        }
        return format.format(amount).replace("Rp", "Rp ").trim()
    }

    fun formatNumber(amount: Double): String {
        val format = NumberFormat.getNumberInstance(indonesianLocale).apply {
            maximumFractionDigits = 0
        }
        return format.format(amount)
    }
}
