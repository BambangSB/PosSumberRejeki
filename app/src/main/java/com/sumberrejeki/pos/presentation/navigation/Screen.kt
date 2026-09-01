package com.sumberrejeki.pos.presentation.navigation

sealed class Screen(val route: String) {
    data object Welcome : Screen("welcome")
    data object RegisterAdmin : Screen("register_admin")
    data object Home : Screen("home")
    data object Catalog : Screen("catalog")
    data object AddEditProduct : Screen("product_form?productId={productId}&barcode={barcode}") {
        fun createRoute(productId: Long? = null, barcode: String? = null): String {
            val pId = productId ?: 0L
            val bCode = barcode ?: ""
            return "product_form?productId=$pId&barcode=$bCode"
        }
    }
    data object BarcodeScanner : Screen("barcode_scanner")
    data object Checkout : Screen("checkout")
    data object PaymentSuccess : Screen("payment_success/{orderId}") {
        fun createRoute(orderId: String): String = "payment_success/$orderId"
    }
    data object History : Screen("history")
    data object FinancialReport : Screen("financial_report")
    data object Profile : Screen("profile")
    data object StaffManagement : Screen("staff_management")
    data object UpgradeLicense : Screen("upgrade_license")
}
