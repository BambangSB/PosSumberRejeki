package com.sumberrejeki.pos.presentation.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sumberrejeki.pos.domain.model.FinancialSummary
import com.sumberrejeki.pos.domain.model.Order
import com.sumberrejeki.pos.domain.model.UserRole
import com.sumberrejeki.pos.domain.usecase.auth.GetCurrentSessionUseCase
import com.sumberrejeki.pos.domain.usecase.report.GetFinancialReportUseCase
import android.content.Context
import android.content.Intent
import com.sumberrejeki.pos.domain.usecase.report.GetOrdersHistoryUseCase
import com.sumberrejeki.pos.utils.printer.BluetoothPrinterDevice
import com.sumberrejeki.pos.utils.printer.BluetoothPrinterManager
import com.sumberrejeki.pos.utils.printer.ReceiptFormatter
import com.sumberrejeki.pos.utils.report.FinancialExportResult
import com.sumberrejeki.pos.utils.report.FinancialReportExporter
import java.io.File
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

import com.sumberrejeki.pos.domain.model.LicenseInfo
import com.sumberrejeki.pos.domain.usecase.license.GetLicenseStatusUseCase

enum class ReportPeriod {
    TODAY,
    THIS_MONTH,
    ALL_TIME
}

data class HistoryUiState(
    val financialSummary: FinancialSummary = FinancialSummary(),
    val selectedPeriod: ReportPeriod = ReportPeriod.TODAY,
    val isLoadingReport: Boolean = false,
    val reportErrorMessage: String? = null,
    val pairedPrinters: List<BluetoothPrinterDevice> = emptyList(),
    val isPrinting: Boolean = false,
    val printMessage: String? = null,
    val isExportingSpreadsheet: Boolean = false,
    val exportResult: FinancialExportResult? = null
)

class HistoryViewModel(
    private val getOrdersHistoryUseCase: GetOrdersHistoryUseCase,
    private val getFinancialReportUseCase: GetFinancialReportUseCase,
    private val printerManager: BluetoothPrinterManager,
    getCurrentSessionUseCase: GetCurrentSessionUseCase,
    getLicenseStatusUseCase: GetLicenseStatusUseCase? = null
) : ViewModel() {

    val licenseInfo: StateFlow<LicenseInfo> = getLicenseStatusUseCase?.licenseInfoFlow
        ?.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), LicenseInfo())
        ?: MutableStateFlow(LicenseInfo())

    val currentUser = getCurrentSessionUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val allOrders: StateFlow<List<Order>> = getOrdersHistoryUseCase.allOrdersFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    init {
        loadFinancialReport(ReportPeriod.TODAY)
        loadPrinters()
    }

    fun loadPrinters() {
        val printers = printerManager.getPairedPrinters()
        _uiState.value = _uiState.value.copy(pairedPrinters = printers)
    }

    fun onPeriodSelected(period: ReportPeriod) {
        _uiState.value = _uiState.value.copy(selectedPeriod = period)
        loadFinancialReport(period)
    }

    fun loadFinancialReport(period: ReportPeriod) {
        val role = currentUser.value?.role ?: UserRole.ADMIN

        val now = System.currentTimeMillis()
        val startTime = when (period) {
            ReportPeriod.TODAY -> GetFinancialReportUseCase.getStartOfDay()
            ReportPeriod.THIS_MONTH -> GetFinancialReportUseCase.getStartOfMonth()
            ReportPeriod.ALL_TIME -> 0L
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingReport = true, reportErrorMessage = null)
            val result = getFinancialReportUseCase(role, startTime, now)
            result.onSuccess { summary ->
                _uiState.value = _uiState.value.copy(
                    isLoadingReport = false,
                    financialSummary = summary
                )
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isLoadingReport = false,
                    reportErrorMessage = error.message ?: "Gagal memuat laporan keuangan"
                )
            }
        }
    }

    fun printReceipt(order: Order, deviceAddress: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isPrinting = true, printMessage = null)
            val receiptBytes = ReceiptFormatter.formatReceiptEscPos(order)
            val result = printerManager.printData(deviceAddress, receiptBytes)
            result.onSuccess {
                _uiState.value = _uiState.value.copy(
                    isPrinting = false,
                    printMessage = "Struk berhasil dicetak!"
                )
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isPrinting = false,
                    printMessage = "Gagal mencetak: ${error.message ?: "Printer tidak terhubung"}"
                )
            }
        }
    }

    fun clearPrintMessage() {
        _uiState.value = _uiState.value.copy(printMessage = null)
    }

    fun exportFinancialReport(context: Context, storeName: String = "Sumber Rejeki POS") {
        if (!licenseInfo.value.canExportData) {
            _uiState.value = _uiState.value.copy(
                reportErrorMessage = "Fitur Ekspor Spreadsheet terkunci. Silakan upgrade ke Pro untuk mengekspor data."
            )
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isExportingSpreadsheet = true)
            try {
                val orders = allOrders.value
                val result = FinancialReportExporter.exportToSpreadsheet(
                    context = context,
                    summary = _uiState.value.financialSummary,
                    orders = orders,
                    period = _uiState.value.selectedPeriod,
                    storeName = storeName
                )
                _uiState.value = _uiState.value.copy(
                    isExportingSpreadsheet = false,
                    exportResult = result
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isExportingSpreadsheet = false,
                    reportErrorMessage = "Gagal mengekspor laporan: ${e.message}"
                )
            }
        }
    }

    fun dismissExportResult() {
        _uiState.value = _uiState.value.copy(exportResult = null)
    }

    fun createShareIntent(context: Context, file: File): Intent {
        return FinancialReportExporter.createShareIntent(context, file)
    }
}
