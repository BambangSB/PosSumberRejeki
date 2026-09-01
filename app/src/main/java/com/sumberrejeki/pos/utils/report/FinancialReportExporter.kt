package com.sumberrejeki.pos.utils.report

import android.content.Context
import android.content.Intent
import android.os.Environment
import androidx.core.content.FileProvider
import com.sumberrejeki.pos.domain.model.FinancialSummary
import com.sumberrejeki.pos.domain.model.Order
import com.sumberrejeki.pos.presentation.components.CurrencyFormatter
import com.sumberrejeki.pos.presentation.history.ReportPeriod
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class FinancialExportResult(
    val file: File,
    val fileName: String,
    val filePath: String,
    val periodName: String,
    val totalRevenue: Double,
    val grossProfit: Double,
    val totalTransactions: Int
)

object FinancialReportExporter {

    private val dateTimeFormat = SimpleDateFormat("dd MMMM yyyy, HH:mm:ss", Locale("id", "ID"))
    private val fileTimestampFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)
    private val orderDateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("id", "ID"))

    suspend fun exportToSpreadsheet(
        context: Context,
        summary: FinancialSummary,
        orders: List<Order>,
        period: ReportPeriod,
        storeName: String
    ): FinancialExportResult = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        val periodLabel = when (period) {
            ReportPeriod.TODAY -> "Hari Ini"
            ReportPeriod.THIS_MONTH -> "Bulan Ini"
            ReportPeriod.ALL_TIME -> "Semua Waktu"
        }

        val sb = StringBuilder()

        // UTF-8 BOM so Microsoft Excel directly opens Indonesian accented/symbolic characters properly
        sb.append("\uFEFF")

        // 1. Header Section
        sb.append("\"SUMBER REJEKI POS - LAPORAN KEUANGAN & LABA\"\n")
        sb.append("\"Nama Toko:\",\"${escapeCsv(storeName)}\"\n")
        sb.append("\"Periode:\",\"$periodLabel\"\n")
        sb.append("\"Tanggal Dibuat:\",\"${dateTimeFormat.format(Date(now))}\"\n")
        sb.append("\n")

        // 2. Financial Metrics Summary
        val profitMargin = if (summary.totalRevenue > 0) (summary.grossProfit / summary.totalRevenue) * 100 else 0.0
        sb.append("\"=== RINGKASAN FINANSIAL & LABA KOTOR ===\"\n")
        sb.append("\"Indikator Keuangan\",\"Nominal / Nilai\"\n")
        sb.append("\"Total Omzet (Penjualan Kotor)\",\"${CurrencyFormatter.formatRupiah(summary.totalRevenue)}\"\n")
        sb.append("\"Total Modal Pokok (HPP)\",\"${CurrencyFormatter.formatRupiah(summary.totalCostOfGoods)}\"\n")
        sb.append("\"Estimasi Laba Kotor (Gross Profit)\",\"${CurrencyFormatter.formatRupiah(summary.grossProfit)}\"\n")
        sb.append("\"Margin Keuntungan\",\"${String.format(Locale.US, "%.2f", profitMargin)}%\"\n")
        sb.append("\"Total Transaksi Berhasil\",\"${summary.totalTransactions} Transaksi\"\n")
        sb.append("\"Rata-rata Transaksi (AOV)\",\"${CurrencyFormatter.formatRupiah(summary.averageTransactionValue)}\"\n")
        sb.append("\n")

        // 3. Payment Method Breakdown
        sb.append("\"=== RINCIAN METODE PEMBAYARAN ===\"\n")
        sb.append("\"Metode Pembayaran\",\"Total Penerimaan\"\n")
        sb.append("\"Pembayaran Tunai (Cash)\",\"${CurrencyFormatter.formatRupiah(summary.cashRevenue)}\"\n")
        sb.append("\"Pembayaran Non-Tunai (Card / QRIS)\",\"${CurrencyFormatter.formatRupiah(summary.cardRevenue)}\"\n")
        sb.append("\n")

        // 4. Top Selling Products
        if (summary.topSellingProducts.isNotEmpty()) {
            sb.append("\"=== PRODUK TERLARIS (TOP SELLING) ===\"\n")
            sb.append("\"No\",\"Nama Produk\",\"Jumlah Terjual (Qty)\",\"Total Omzet Produk\"\n")
            summary.topSellingProducts.forEachIndexed { index, topProd ->
                sb.append("${index + 1},\"${escapeCsv(topProd.productName)}\",${topProd.quantitySold},\"${CurrencyFormatter.formatRupiah(topProd.totalRevenue)}\"\n")
            }
            sb.append("\n")
        }

        // 5. Detailed Orders Log
        sb.append("\"=== DAFTAR DETAIL TRANSAKSI ===\"\n")
        sb.append("\"No\",\"ID Transaksi\",\"Tanggal & Waktu\",\"Metode Pembayaran\",\"Total Belanja\",\"Jumlah Bayar\",\"Kembalian\"\n")
        orders.forEachIndexed { index, order ->
            val dateStr = orderDateFormat.format(Date(order.timestamp))
            sb.append("${index + 1},\"${order.orderId}\",\"$dateStr\",\"${order.paymentMethod.name}\",\"${CurrencyFormatter.formatRupiah(order.totalAmount)}\",\"${CurrencyFormatter.formatRupiah(order.amountPaid)}\",\"${CurrencyFormatter.formatRupiah(order.changeAmount)}\"\n")
        }

        val csvContent = sb.toString()
        val timeStampStr = fileTimestampFormat.format(Date(now))
        val fileName = "Laporan_Keuangan_SumberRejeki_${period.name.lowercase()}_$timeStampStr.csv"

        val targetDir = try {
            val publicDownloads = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            if (publicDownloads.exists() || publicDownloads.mkdirs()) {
                publicDownloads
            } else {
                context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) ?: context.filesDir
            }
        } catch (e: Exception) {
            context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) ?: context.filesDir
        }

        val exportFile = File(targetDir, fileName)
        FileOutputStream(exportFile).use { fos ->
            fos.write(csvContent.toByteArray(Charsets.UTF_8))
            fos.flush()
        }

        FinancialExportResult(
            file = exportFile,
            fileName = fileName,
            filePath = exportFile.absolutePath,
            periodName = periodLabel,
            totalRevenue = summary.totalRevenue,
            grossProfit = summary.grossProfit,
            totalTransactions = summary.totalTransactions
        )
    }

    fun createShareIntent(context: Context, file: File): Intent {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        return Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Laporan Keuangan POS Sumber Rejeki")
            putExtra(Intent.EXTRA_TEXT, "Berikut file laporan keuangan dan laba: ${file.name}")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

    private fun escapeCsv(value: String): String {
        return value.replace("\"", "\"\"")
    }
}
