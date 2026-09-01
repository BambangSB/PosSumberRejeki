package com.sumberrejeki.pos.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sumberrejeki.pos.domain.model.Order
import com.sumberrejeki.pos.presentation.history.OrderDetailDialog

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToCatalog: () -> Unit,
    onNavigateToScanner: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToReport: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedOrderForDetail by remember { mutableStateOf<Order?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // 1. Sales Metrics
        MetricsSection(
            todayRevenue = uiState.todayRevenue,
            todayOrdersCount = uiState.todayOrdersCount,
            totalOrdersCount = uiState.totalOrdersCount
        )

        // 2. Shortcut Grid
        ShortcutGrid(
            userRole = uiState.currentUser?.role,
            onNavigateToCatalog = onNavigateToCatalog,
            onNavigateToScanner = onNavigateToScanner,
            onNavigateToHistory = onNavigateToHistory,
            onNavigateToReport = onNavigateToReport
        )

        // 3. Recent Orders
        RecentOrdersSection(
            recentOrders = uiState.recentOrders,
            onOrderClick = { selectedOrderForDetail = it },
            onSeeAllClick = onNavigateToHistory
        )

        Spacer(modifier = Modifier.height(60.dp))
    }

    if (selectedOrderForDetail != null) {
        OrderDetailDialog(
            order = selectedOrderForDetail!!,
            onDismiss = { selectedOrderForDetail = null },
            onPrintReceipt = { /* trigger print via history or dialog */ }
        )
    }
}
