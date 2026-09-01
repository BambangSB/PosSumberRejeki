package com.sumberrejeki.pos.utils.printer

import com.sumberrejeki.pos.domain.model.Order
import java.io.ByteArrayOutputStream
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ReceiptFormatter {

    private const val ESC: Byte = 0x1B
    private const val GS: Byte = 0x1D

    // Commands
    private val CMD_INIT = byteArrayOf(ESC, 0x40)
    private val CMD_ALIGN_LEFT = byteArrayOf(ESC, 0x61, 0x00)
    private val CMD_ALIGN_CENTER = byteArrayOf(ESC, 0x61, 0x01)
    private val CMD_ALIGN_RIGHT = byteArrayOf(ESC, 0x61, 0x02)
    private val CMD_BOLD_ON = byteArrayOf(ESC, 0x45, 0x01)
    private val CMD_BOLD_OFF = byteArrayOf(ESC, 0x45, 0x00)
    private val CMD_DOUBLE_SIZE = byteArrayOf(GS, 0x21, 0x11)
    private val CMD_NORMAL_SIZE = byteArrayOf(GS, 0x21, 0x00)
    private val CMD_FEED_3 = byteArrayOf(ESC, 0x64, 0x03)
    private val CMD_CUT = byteArrayOf(GS, 0x56, 0x41, 0x00)

    fun formatReceiptEscPos(order: Order, storeName: String = "SUMBER REJEKI POS", storeAddress: String = "Jl. Pasar Rejeki No. 88", lineWidth: Int = 32): ByteArray {
        val out = ByteArrayOutputStream()
        val currencyFormat = NumberFormat.getCurrencyInstance(Locale("in", "ID")).apply {
            maximumFractionDigits = 0
        }
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

        fun write(bytes: ByteArray) = out.write(bytes)
        fun writeText(text: String) = out.write(text.toByteArray(Charsets.ISO_8859_1))
        fun writeLine(text: String = "") = writeText("$text\n")

        fun formatTwoColumns(left: String, right: String): String {
            val spaces = lineWidth - left.length - right.length
            return if (spaces > 0) {
                left + " ".repeat(spaces) + right
            } else {
                "$left $right"
            }
        }

        // 1. Initialize
        write(CMD_INIT)

        // 2. Header (Store Name & Address)
        write(CMD_ALIGN_CENTER)
        write(CMD_BOLD_ON)
        write(CMD_DOUBLE_SIZE)
        writeLine(storeName)
        write(CMD_NORMAL_SIZE)
        write(CMD_BOLD_OFF)
        writeLine(storeAddress)
        writeLine("-".repeat(lineWidth))

        // 3. Order Metadata
        write(CMD_ALIGN_LEFT)
        writeLine("No. Struk : #${order.orderId.takeLast(8).uppercase()}")
        writeLine("Waktu     : ${dateFormat.format(Date(order.timestamp))}")
        if (order.cashierName.isNotBlank()) {
            writeLine("Kasir     : ${order.cashierName}")
        }
        if (!order.customerName.isNullOrBlank()) {
            writeLine("Pelanggan : ${order.customerName}")
        }
        writeLine("=".repeat(lineWidth))

        // 4. Line Items
        for (item in order.items) {
            writeLine(item.productName)
            val priceDetail = "${item.quantity} x ${currencyFormat.format(item.priceAtPurchase)}"
            val subtotal = currencyFormat.format(item.subtotal)
            writeLine(formatTwoColumns("  $priceDetail", subtotal))
        }

        writeLine("-".repeat(lineWidth))

        // 5. Totals & Payment
        write(CMD_BOLD_ON)
        writeLine(formatTwoColumns("TOTAL", currencyFormat.format(order.totalAmount)))
        write(CMD_BOLD_OFF)
        writeLine(formatTwoColumns("Metode Bayar", order.paymentMethod.name))
        writeLine(formatTwoColumns("Bayar", currencyFormat.format(order.amountPaid)))
        writeLine(formatTwoColumns("Kembali", currencyFormat.format(order.changeAmount)))
        writeLine("=".repeat(lineWidth))

        // 6. Footer & Thank You
        write(CMD_ALIGN_CENTER)
        writeLine("Terima Kasih Atas Kunjungan Anda")
        writeLine("Barang yang sudah dibeli")
        writeLine("tidak dapat ditukar/dikembalikan")
        write(CMD_FEED_3)
        write(CMD_CUT)

        return out.toByteArray()
    }
}
