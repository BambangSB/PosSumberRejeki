package com.sumberrejeki.pos.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
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
    showNotificationSheet: Boolean = false,
    onDismissNotificationSheet: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedOrderForDetail by remember { mutableStateOf<Order?>(null) }

    val urgentNotifications = remember(uiState.notifications) {
        uiState.notifications.filter { it.type == NotificationType.OUT_OF_STOCK || it.type == NotificationType.LOW_STOCK }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Urgent Stock Alert Banner (if any)
        if (urgentNotifications.isNotEmpty()) {
            val firstAlert = urgentNotifications.first()
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (firstAlert.type == NotificationType.OUT_OF_STOCK) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.8f)
                    else Color(0xFFFFECB3)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToCatalog() }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(
                                if (firstAlert.type == NotificationType.OUT_OF_STOCK) MaterialTheme.colorScheme.error
                                else Color(0xFFE65100)
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.WarningAmber,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = firstAlert.title,
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = if (firstAlert.type == NotificationType.OUT_OF_STOCK) MaterialTheme.colorScheme.onErrorContainer
                            else Color(0xFF5D4037)
                        )
                        Text(
                            text = firstAlert.message,
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1,
                            color = if (firstAlert.type == NotificationType.OUT_OF_STOCK) MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)
                            else Color(0xFF5D4037).copy(alpha = 0.85f)
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Lihat",
                        tint = if (firstAlert.type == NotificationType.OUT_OF_STOCK) MaterialTheme.colorScheme.onErrorContainer
                        else Color(0xFF5D4037),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        // 1. Promotional Auto-Sliding Banner Carousel
        PromoBannerCarousel(
            onNavigateToCatalog = onNavigateToCatalog,
            onNavigateToScanner = onNavigateToScanner,
            onNavigateToReport = onNavigateToReport
        )

        // 2. Sales Metrics
        MetricsSection(
            todayRevenue = uiState.todayRevenue,
            todayOrdersCount = uiState.todayOrdersCount,
            totalOrdersCount = uiState.totalOrdersCount
        )

        // 3. Shortcut Grid
        ShortcutGrid(
            userRole = uiState.currentUser?.role,
            onNavigateToCatalog = onNavigateToCatalog,
            onNavigateToScanner = onNavigateToScanner,
            onNavigateToHistory = onNavigateToHistory,
            onNavigateToReport = onNavigateToReport
        )

        // 4. Recent Orders
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

    if (showNotificationSheet) {
        NotificationBottomSheet(
            notifications = uiState.notifications,
            onDismiss = onDismissNotificationSheet,
            onNotificationClick = { notif ->
                if (notif.type == NotificationType.LOW_STOCK || notif.type == NotificationType.OUT_OF_STOCK || notif.targetProductId != null) {
                    onNavigateToCatalog()
                }
            }
        )
    }
}