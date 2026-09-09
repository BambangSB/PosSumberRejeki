package com.sumberrejeki.pos

import android.content.Context
import com.sumberrejeki.pos.data.local.AppDatabase
import com.sumberrejeki.pos.data.preferences.LicensePreferences
import com.sumberrejeki.pos.data.preferences.UserSessionDataStore
import com.sumberrejeki.pos.data.repository.AuthRepositoryImpl
import com.sumberrejeki.pos.data.repository.CartRepositoryImpl
import com.sumberrejeki.pos.data.repository.LicenseRepositoryImpl
import com.sumberrejeki.pos.data.repository.OrderRepositoryImpl
import com.sumberrejeki.pos.data.repository.ProductRepositoryImpl
import com.sumberrejeki.pos.domain.repository.AuthRepository
import com.sumberrejeki.pos.domain.repository.CartRepository
import com.sumberrejeki.pos.domain.repository.LicenseRepository
import com.sumberrejeki.pos.domain.repository.OrderRepository
import com.sumberrejeki.pos.domain.repository.ProductRepository
import com.sumberrejeki.pos.domain.usecase.auth.AddStaffUserUseCase
import com.sumberrejeki.pos.domain.usecase.auth.DeleteStaffUserUseCase
import com.sumberrejeki.pos.domain.usecase.auth.GetCurrentSessionUseCase
import com.sumberrejeki.pos.domain.usecase.auth.GetStaffUsersUseCase
import com.sumberrejeki.pos.domain.usecase.auth.LoginWithPinUseCase
import com.sumberrejeki.pos.domain.usecase.auth.RegisterAdminUseCase
import com.sumberrejeki.pos.domain.usecase.auth.ValidateUserLimitUseCase
import com.sumberrejeki.pos.domain.usecase.auth.LogoutUseCase
import com.sumberrejeki.pos.domain.usecase.cart.AddToCartDraftUseCase
import com.sumberrejeki.pos.domain.usecase.cart.ClearCartDraftUseCase
import com.sumberrejeki.pos.domain.usecase.cart.GetCartDraftsUseCase
import com.sumberrejeki.pos.domain.usecase.cart.RemoveFromCartDraftUseCase
import com.sumberrejeki.pos.domain.usecase.cart.UpdateCartQtyUseCase
import com.sumberrejeki.pos.domain.usecase.checkout.CalculateChangeUseCase
import com.sumberrejeki.pos.domain.usecase.checkout.ProcessCheckoutUseCase
import com.sumberrejeki.pos.domain.usecase.license.GetLicenseStatusUseCase
import com.sumberrejeki.pos.domain.usecase.license.VerifyRsaLicenseUseCase
import com.sumberrejeki.pos.domain.usecase.product.DeleteProductUseCase
import com.sumberrejeki.pos.domain.usecase.product.GetProductByBarcodeUseCase
import com.sumberrejeki.pos.domain.usecase.product.GetProductsUseCase
import com.sumberrejeki.pos.domain.usecase.product.SaveProductUseCase
import com.sumberrejeki.pos.domain.usecase.product.SearchProductsUseCase
import com.sumberrejeki.pos.domain.usecase.report.GetDashboardMetricsUseCase
import com.sumberrejeki.pos.domain.usecase.report.GetFinancialReportUseCase
import com.sumberrejeki.pos.domain.usecase.report.GetOrdersHistoryUseCase
import com.sumberrejeki.pos.utils.backup.DatabaseBackupManager
import com.sumberrejeki.pos.utils.printer.BluetoothPrinterManager
import com.sumberrejeki.pos.utils.security.InstallationIdProvider

interface AppContainer {
    val database: AppDatabase
    val authRepository: AuthRepository
    val productRepository: ProductRepository
    val cartRepository: CartRepository
    val orderRepository: OrderRepository
    val licenseRepository: LicenseRepository
    val printerManager: BluetoothPrinterManager
    val backupManager: DatabaseBackupManager

    // Auth Use Cases
    val loginWithPinUseCase: LoginWithPinUseCase
    val registerAdminUseCase: RegisterAdminUseCase
    val validateUserLimitUseCase: ValidateUserLimitUseCase
    val getStaffUsersUseCase: GetStaffUsersUseCase
    val addStaffUserUseCase: AddStaffUserUseCase
    val deleteStaffUserUseCase: DeleteStaffUserUseCase
    val getCurrentSessionUseCase: GetCurrentSessionUseCase
    val logoutUseCase: LogoutUseCase

    // Cart Use Cases
    val addToCartDraftUseCase: AddToCartDraftUseCase
    val updateCartQtyUseCase: UpdateCartQtyUseCase
    val removeFromCartDraftUseCase: RemoveFromCartDraftUseCase
    val clearCartDraftUseCase: ClearCartDraftUseCase
    val getCartDraftsUseCase: GetCartDraftsUseCase

    // Checkout Use Cases
    val calculateChangeUseCase: CalculateChangeUseCase
    val processCheckoutUseCase: ProcessCheckoutUseCase

    // Product Use Cases
    val getProductsUseCase: GetProductsUseCase
    val searchProductsUseCase: SearchProductsUseCase
    val getProductByBarcodeUseCase: GetProductByBarcodeUseCase
    val saveProductUseCase: SaveProductUseCase
    val deleteProductUseCase: DeleteProductUseCase

    // License Use Cases
    val verifyRsaLicenseUseCase: VerifyRsaLicenseUseCase
    val getLicenseStatusUseCase: GetLicenseStatusUseCase

    // Report Use Cases
    val getFinancialReportUseCase: GetFinancialReportUseCase
    val getDashboardMetricsUseCase: GetDashboardMetricsUseCase
    val getOrdersHistoryUseCase: GetOrdersHistoryUseCase
}

class DefaultAppContainer(private val context: Context) : AppContainer {

    override val database: AppDatabase by lazy {
        AppDatabase.getInstance(context)
    }

    private val sessionDataStore: UserSessionDataStore by lazy {
        UserSessionDataStore(context)
    }

    private val licensePreferences: LicensePreferences by lazy {
        LicensePreferences(context)
    }

    private val installationIdProvider: InstallationIdProvider by lazy {
        InstallationIdProvider(context)
    }

    override val printerManager: BluetoothPrinterManager by lazy {
        BluetoothPrinterManager(context)
    }

    override val backupManager: DatabaseBackupManager by lazy {
        DatabaseBackupManager(context, database)
    }

    override val authRepository: AuthRepository by lazy {
        AuthRepositoryImpl(database.userDao(), sessionDataStore)
    }

    override val productRepository: ProductRepository by lazy {
        ProductRepositoryImpl(database.productDao())
    }

    override val cartRepository: CartRepository by lazy {
        CartRepositoryImpl(database.cartDraftDao())
    }

    override val orderRepository: OrderRepository by lazy {
        OrderRepositoryImpl(
            db = database,
            orderDao = database.orderDao(),
            productDao = database.productDao(),
            cartDraftDao = database.cartDraftDao(),
            userDao = database.userDao()
        )
    }

    override val licenseRepository: LicenseRepository by lazy {
        LicenseRepositoryImpl(context, licensePreferences, installationIdProvider)
    }

    // Auth Use Cases
    override val loginWithPinUseCase: LoginWithPinUseCase by lazy {
        LoginWithPinUseCase(authRepository)
    }

    override val registerAdminUseCase: RegisterAdminUseCase by lazy {
        RegisterAdminUseCase(authRepository)
    }

    override val validateUserLimitUseCase: ValidateUserLimitUseCase by lazy {
        ValidateUserLimitUseCase(authRepository, licenseRepository)
    }

    override val getStaffUsersUseCase: GetStaffUsersUseCase by lazy {
        GetStaffUsersUseCase(authRepository)
    }

    override val addStaffUserUseCase: AddStaffUserUseCase by lazy {
        AddStaffUserUseCase(authRepository, validateUserLimitUseCase)
    }

    override val deleteStaffUserUseCase: DeleteStaffUserUseCase by lazy {
        DeleteStaffUserUseCase(authRepository)
    }

    override val getCurrentSessionUseCase: GetCurrentSessionUseCase by lazy {
        GetCurrentSessionUseCase(authRepository)
    }

    override val logoutUseCase: LogoutUseCase by lazy {
        LogoutUseCase(authRepository)
    }

    // Cart Use Cases
    override val addToCartDraftUseCase: AddToCartDraftUseCase by lazy {
        AddToCartDraftUseCase(cartRepository)
    }

    override val updateCartQtyUseCase: UpdateCartQtyUseCase by lazy {
        UpdateCartQtyUseCase(cartRepository)
    }

    override val removeFromCartDraftUseCase: RemoveFromCartDraftUseCase by lazy {
        RemoveFromCartDraftUseCase(cartRepository)
    }

    override val clearCartDraftUseCase: ClearCartDraftUseCase by lazy {
        ClearCartDraftUseCase(cartRepository)
    }

    override val getCartDraftsUseCase: GetCartDraftsUseCase by lazy {
        GetCartDraftsUseCase(cartRepository)
    }

    // Checkout Use Cases
    override val calculateChangeUseCase: CalculateChangeUseCase by lazy {
        CalculateChangeUseCase()
    }

    override val processCheckoutUseCase: ProcessCheckoutUseCase by lazy {
        ProcessCheckoutUseCase(orderRepository, calculateChangeUseCase)
    }

    // Product Use Cases
    override val getProductsUseCase: GetProductsUseCase by lazy {
        GetProductsUseCase(productRepository)
    }

    override val searchProductsUseCase: SearchProductsUseCase by lazy {
        SearchProductsUseCase(productRepository)
    }

    override val getProductByBarcodeUseCase: GetProductByBarcodeUseCase by lazy {
        GetProductByBarcodeUseCase(productRepository)
    }

    override val saveProductUseCase: SaveProductUseCase by lazy {
        SaveProductUseCase(productRepository, licenseRepository)
    }

    override val deleteProductUseCase: DeleteProductUseCase by lazy {
        DeleteProductUseCase(productRepository)
    }

    // License Use Cases
    override val verifyRsaLicenseUseCase: VerifyRsaLicenseUseCase by lazy {
        VerifyRsaLicenseUseCase(licenseRepository)
    }

    override val getLicenseStatusUseCase: GetLicenseStatusUseCase by lazy {
        GetLicenseStatusUseCase(licenseRepository)
    }

    // Report Use Cases
    override val getFinancialReportUseCase: GetFinancialReportUseCase by lazy {
        GetFinancialReportUseCase(orderRepository)
    }

    override val getDashboardMetricsUseCase: GetDashboardMetricsUseCase by lazy {
        GetDashboardMetricsUseCase(orderRepository)
    }

    override val getOrdersHistoryUseCase: GetOrdersHistoryUseCase by lazy {
        GetOrdersHistoryUseCase(orderRepository)
    }
}
