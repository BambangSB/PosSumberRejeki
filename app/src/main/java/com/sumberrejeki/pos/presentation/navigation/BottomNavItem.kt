package com.sumberrejeki.pos.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Store
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(
    val route: String,
    val title: String,
    val icon: ImageVector,
    val adminOnly: Boolean = false
) {
    data object Home : BottomNavItem(
        route = Screen.Home.route,
        title = "Beranda",
        icon = Icons.Default.Home
    )

    data object Catalog : BottomNavItem(
        route = Screen.Catalog.route,
        title = "Katalog",
        icon = Icons.Default.Store
    )

    data object Scanner : BottomNavItem(
        route = Screen.BarcodeScanner.route,
        title = "Scan",
        icon = Icons.Default.QrCodeScanner
    )

    data object History : BottomNavItem(
        route = Screen.History.route,
        title = "Riwayat",
        icon = Icons.Default.History
    )

    data object Profile : BottomNavItem(
        route = Screen.Profile.route,
        title = "Profil",
        icon = Icons.Default.Person
    )

    companion object {
        val items = listOf(Home, Catalog, Scanner, History, Profile)
    }
}
