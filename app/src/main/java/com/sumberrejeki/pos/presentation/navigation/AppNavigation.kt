package com.sumberrejeki.pos.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.sumberrejeki.pos.PosApplication
import com.sumberrejeki.pos.presentation.auth.AuthViewModel
import com.sumberrejeki.pos.presentation.auth.RegisterAdminScreen
import com.sumberrejeki.pos.presentation.auth.WelcomeScreen
import com.sumberrejeki.pos.presentation.checkout.CheckoutViewModel
import com.sumberrejeki.pos.presentation.checkout.OrderConfirmationScreen
import com.sumberrejeki.pos.presentation.checkout.SuccessPaymentScreen
import com.sumberrejeki.pos.presentation.components.AdaptiveScaffold
import com.sumberrejeki.pos.presentation.components.FloatingSummaryCard
import com.sumberrejeki.pos.presentation.components.PosTopAppBar
import com.sumberrejeki.pos.presentation.history.FinancialReportScreen
import com.sumberrejeki.pos.presentation.history.HistoryScreen
import com.sumberrejeki.pos.presentation.history.HistoryViewModel
import com.sumberrejeki.pos.presentation.home.HomeScreen
import com.sumberrejeki.pos.presentation.home.HomeViewModel
import com.sumberrejeki.pos.presentation.product.AddEditProductScreen
import com.sumberrejeki.pos.presentation.product.ManageProductsScreen
import com.sumberrejeki.pos.presentation.product.ProductCatalogScreen
import com.sumberrejeki.pos.presentation.product.ProductViewModel
import com.sumberrejeki.pos.presentation.profile.ProfileScreen
import com.sumberrejeki.pos.presentation.profile.ProfileViewModel
import com.sumberrejeki.pos.presentation.scan.BarcodeScannerScreen
import com.sumberrejeki.pos.presentation.scan.ScannerViewModel

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController()
) {
    val context = LocalContext.current
    val app = context.applicationContext as PosApplication
    val container = app.container

    // ViewModels with Container Factory
    val authViewModel: AuthViewModel = viewModel {
        AuthViewModel(
            loginWithPinUseCase = container.loginWithPinUseCase,
            registerAdminUseCase = container.registerAdminUseCase,
            getStaffUsersUseCase = container.getStaffUsersUseCase,
            getCurrentSessionUseCase = container.getCurrentSessionUseCase
        )
    }

    val homeViewModel: HomeViewModel = viewModel {
        HomeViewModel(
            getCurrentSessionUseCase = container.getCurrentSessionUseCase,
            getDashboardMetricsUseCase = container.getDashboardMetricsUseCase,
            getCartDraftsUseCase = container.getCartDraftsUseCase,
            getOrdersHistoryUseCase = container.getOrdersHistoryUseCase,
            getProductsUseCase = container.getProductsUseCase
        )
    }

    val productViewModel: ProductViewModel = viewModel {
        ProductViewModel(
            getProductsUseCase = container.getProductsUseCase,
            searchProductsUseCase = container.searchProductsUseCase,
            getProductByBarcodeUseCase = container.getProductByBarcodeUseCase,
            saveProductUseCase = container.saveProductUseCase,
            deleteProductUseCase = container.deleteProductUseCase,
            addToCartDraftUseCase = container.addToCartDraftUseCase,
            getCartDraftsUseCase = container.getCartDraftsUseCase
        )
    }

    val scannerViewModel: ScannerViewModel = viewModel {
        ScannerViewModel(
            getProductByBarcodeUseCase = container.getProductByBarcodeUseCase,
            addToCartDraftUseCase = container.addToCartDraftUseCase,
            getCartDraftsUseCase = container.getCartDraftsUseCase
        )
    }

    val checkoutViewModel: CheckoutViewModel = viewModel {
        CheckoutViewModel(
            getCartDraftsUseCase = container.getCartDraftsUseCase,
            updateCartQtyUseCase = container.updateCartQtyUseCase,
            removeFromCartDraftUseCase = container.removeFromCartDraftUseCase,
            clearCartDraftUseCase = container.clearCartDraftUseCase,
            processCheckoutUseCase = container.processCheckoutUseCase,
            calculateChangeUseCase = container.calculateChangeUseCase,
            getOrdersHistoryUseCase = container.getOrdersHistoryUseCase,
            printerManager = container.printerManager,
            getCurrentSessionUseCase = container.getCurrentSessionUseCase
        )
    }

    val historyViewModel: HistoryViewModel = viewModel {
        HistoryViewModel(
            getOrdersHistoryUseCase = container.getOrdersHistoryUseCase,
            getFinancialReportUseCase = container.getFinancialReportUseCase,
            printerManager = container.printerManager,
            getCurrentSessionUseCase = container.getCurrentSessionUseCase
        )
    }

    val profileViewModel: ProfileViewModel = viewModel {
        ProfileViewModel(
            logoutUseCase = container.logoutUseCase,
            getStaffUsersUseCase = container.getStaffUsersUseCase,
            addStaffUserUseCase = container.addStaffUserUseCase,
            deleteStaffUserUseCase = container.deleteStaffUserUseCase,
            verifyRsaLicenseUseCase = container.verifyRsaLicenseUseCase,
            getLicenseStatusUseCase = container.getLicenseStatusUseCase,
            getProductsUseCase = container.getProductsUseCase,
            deleteProductUseCase = container.deleteProductUseCase,
            authRepository = container.authRepository,
            licenseRepository = container.licenseRepository,
            backupManager = container.backupManager,
            getCurrentSessionUseCase = container.getCurrentSessionUseCase
        )
    }

    val currentSession by authViewModel.currentSession.collectAsState()
    val totalCartItems by productViewModel.totalCartItems.collectAsState()
    val totalCartPrice by productViewModel.totalCartPrice.collectAsState()
    val homeUiState by homeViewModel.uiState.collectAsState()

    var showNotificationSheet by remember { mutableStateOf(false) }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val startDestination = Screen.Welcome.route

    AdaptiveScaffold(
        currentRoute = currentRoute,
        onNavigateToRoute = { route ->
            navController.navigate(route) {
                popUpTo(Screen.Home.route) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
        },
        userRole = currentSession?.role,
        topBar = {
            val title = when (currentRoute) {
                Screen.Home.route -> "Sumber Rejeki POS"
                Screen.Catalog.route -> "Katalog Produk"
                Screen.BarcodeScanner.route -> "Scan Barcode"
                Screen.History.route -> "Riwayat Transaksi"
                Screen.Profile.route -> "Pengaturan Toko"
                else -> "Sumber Rejeki POS"
            }

            val showTopBar = currentRoute in listOf(
                Screen.Home.route,
                Screen.Catalog.route,
                Screen.History.route,
                Screen.Profile.route
            )

            if (showTopBar) {
                PosTopAppBar(
                    title = title,
                    userRole = currentSession?.role,
                    userName = currentSession?.username,
                    cartItemCount = totalCartItems,
                    onCartClick = { navController.navigate(Screen.Checkout.route) },
                    showCartAction = currentRoute != Screen.Profile.route,
                    notificationCount = homeUiState.notifications.size,
                    showNotificationAction = currentRoute == Screen.Home.route,
                    onNotificationClick = { showNotificationSheet = true }
                )
            }
        },
        floatingSummaryCard = {
            val showFloatingCard = currentRoute in listOf(
                Screen.Home.route,
                Screen.Catalog.route,
                Screen.BarcodeScanner.route
            ) && totalCartItems > 0

            if (showFloatingCard) {
                FloatingSummaryCard(
                    totalItems = totalCartItems,
                    totalPrice = totalCartPrice,
                    onCheckoutClick = { navController.navigate(Screen.Checkout.route) }
                )
            }
        }
    ) { modifier ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = modifier
        ) {
            composable(Screen.Welcome.route) {
                WelcomeScreen(
                    viewModel = authViewModel,
                    onLoginSuccess = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Welcome.route) { inclusive = true }
                        }
                    },
                    onNavigateToRegister = {
                        navController.navigate(Screen.RegisterAdmin.route)
                    }
                )
            }

            composable(Screen.RegisterAdmin.route) {
                RegisterAdminScreen(
                    viewModel = authViewModel,
                    onRegistrationSuccess = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.RegisterAdmin.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Home.route) {
                HomeScreen(
                    viewModel = homeViewModel,
                    onNavigateToCatalog = { navController.navigate(Screen.Catalog.route) },
                    onNavigateToScanner = { navController.navigate(Screen.BarcodeScanner.route) },
                    onNavigateToHistory = { navController.navigate(Screen.History.route) },
                    onNavigateToReport = { navController.navigate(Screen.FinancialReport.route) },
                    showNotificationSheet = showNotificationSheet,
                    onDismissNotificationSheet = { showNotificationSheet = false }
                )
            }

            composable(Screen.Catalog.route) {
                ProductCatalogScreen(
                    viewModel = productViewModel,
                    userRole = currentSession?.role,
                    onNavigateToAddProduct = {
                        navController.navigate(Screen.AddEditProduct.createRoute())
                    },
                    onNavigateToEditProduct = { productId ->
                        navController.navigate(Screen.AddEditProduct.createRoute(productId = productId))
                    }
                )
            }

            composable(
                route = Screen.AddEditProduct.route,
                arguments = listOf(
                    navArgument("productId") {
                        type = NavType.LongType
                        defaultValue = 0L
                    },
                    navArgument("barcode") {
                        type = NavType.StringType
                        defaultValue = ""
                    }
                )
            ) { backStackEntry ->
                val productId = backStackEntry.arguments?.getLong("productId") ?: 0L
                val barcode = backStackEntry.arguments?.getString("barcode")
                AddEditProductScreen(
                    viewModel = productViewModel,
                    productId = productId,
                    prefilledBarcode = barcode,
                    userRole = currentSession?.role,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.BarcodeScanner.route) {
                BarcodeScannerScreen(
                    viewModel = scannerViewModel,
                    onNavigateToAddProduct = { scannedBarcode ->
                        navController.navigate(Screen.AddEditProduct.createRoute(barcode = scannedBarcode))
                    }
                )
            }

            composable(Screen.Checkout.route) {
                OrderConfirmationScreen(
                    viewModel = checkoutViewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onCheckoutSuccess = { orderId ->
                        navController.navigate(Screen.PaymentSuccess.createRoute(orderId)) {
                            popUpTo(Screen.Home.route)
                        }
                    }
                )
            }

            composable(
                route = Screen.PaymentSuccess.route,
                arguments = listOf(
                    navArgument("orderId") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val orderId = backStackEntry.arguments?.getString("orderId") ?: ""
                SuccessPaymentScreen(
                    viewModel = checkoutViewModel,
                    orderId = orderId,
                    onNewTransactionClick = {
                        navController.navigate(Screen.Catalog.route) {
                            popUpTo(Screen.Home.route)
                        }
                    }
                )
            }

            composable(Screen.History.route) {
                HistoryScreen(
                    viewModel = historyViewModel,
                    userRole = currentSession?.role,
                    onNavigateToReport = { navController.navigate(Screen.FinancialReport.route) }
                )
            }

            composable(Screen.FinancialReport.route) {
                FinancialReportScreen(
                    viewModel = historyViewModel,
                    userRole = currentSession?.role,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.ManageProducts.route) {
                ManageProductsScreen(
                    viewModel = productViewModel,
                    userRole = currentSession?.role,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToAddProduct = {
                        navController.navigate(Screen.AddEditProduct.createRoute())
                    },
                    onNavigateToEditProduct = { productId ->
                        navController.navigate(Screen.AddEditProduct.createRoute(productId = productId))
                    }
                )
            }

            composable(Screen.Profile.route) {
                ProfileScreen(
                    viewModel = profileViewModel,
                    onLogoutSuccess = {
                        authViewModel.resetState()
                        navController.navigate(Screen.Welcome.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    onNavigateToManageProducts = {
                        navController.navigate(Screen.ManageProducts.route)
                    }
                )
            }
        }
    }
}
