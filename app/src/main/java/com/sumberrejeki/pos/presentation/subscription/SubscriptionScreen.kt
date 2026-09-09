package com.sumberrejeki.pos.presentation.subscription

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sumberrejeki.pos.domain.model.LicenseInfo
import com.sumberrejeki.pos.domain.model.SubscriptionTier
import com.sumberrejeki.pos.presentation.components.CurrencyFormatter
import com.sumberrejeki.pos.presentation.theme.GreenPrimary
import com.sumberrejeki.pos.presentation.theme.PremiumGold
import com.sumberrejeki.pos.presentation.theme.SuccessGreen

data class ProPlanDuration(
    val id: String,
    val title: String,
    val durationDays: Int,
    val price: Double,
    val dailyPriceNote: String,
    val discountPercent: Int = 0,
    val isRecommended: Boolean = false
)

val PRO_PLAN_OPTIONS = listOf(
    ProPlanDuration("1d", "1 Hari", 1, 2000.0, "Rp 2.000 / hari"),
    ProPlanDuration("3d", "3 Hari", 3, 5000.0, "Rp 1.666 / hari"),
    ProPlanDuration("1w", "1 Minggu", 7, 10000.0, "Rp 1.428 / hari"),
    ProPlanDuration("1m", "1 Bulan", 30, 35000.0, "Rp 1.166 / hari", isRecommended = true),
    ProPlanDuration("3m", "3 Bulan", 90, 95000.0, "Rp 1.055 / hari"),
    ProPlanDuration("6m", "6 Bulan", 180, 180000.0, "Rp 1.000 / hari"),
    ProPlanDuration("12m", "1 Tahun", 365, 336000.0, "Rp 920 / hari", discountPercent = 20, isRecommended = true)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionScreen(
    licenseInfo: LicenseInfo,
    installationId: String,
    storeName: String,
    isActivatingKey: Boolean = false,
    keyErrorMessage: String? = null,
    onActivateKey: (String) -> Unit = {},
    onNavigateBack: () -> Unit = {}
) {
    val context = LocalContext.current
    var selectedTabIndex by remember { mutableIntStateOf(0) } // 0: Paket, 1: Riwayat & Kode Aktivasi
    var isYearlyToggle by remember { mutableStateOf(false) }
    var selectedPlan by remember {
        mutableStateOf(PRO_PLAN_OPTIONS.first { it.id == "1m" })
    }
    var licenseKeyInput by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Langganan",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Promo Banner Header: "Mulai dari Rp 1.200/hari — lebih murah dari secangkir kopi"
            Surface(
                color = GreenPrimary.copy(alpha = 0.08f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(GreenPrimary.copy(alpha = 0.16f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalCafe,
                            contentDescription = null,
                            tint = GreenPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Mulai dari Rp 920/hari — lebih murah dari secangkir kopi.",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Tab Switcher: [Paket] vs [Aktivasi Offline]
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.WorkspacePremium, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Pilihan Paket", fontWeight = FontWeight.Bold)
                        }
                    }
                )
                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Key, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Aktivasi Kode RSA", fontWeight = FontWeight.Bold)
                        }
                    }
                )
            }

            if (selectedTabIndex == 0) {
                // Tab 0: Pilihan Paket & Komparasi
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Status Langganan Saat Ini
                    CurrentSubscriptionStatusCard(licenseInfo = licenseInfo)

                    // Card Perbandingan: Dari ribet manual -> otomatis jalan sendiri
                    FeatureComparisonCard()

                    // Toggle Bulanan vs Tahunan (Hemat 20%)
                    BillingToggleBar(
                        isYearly = isYearlyToggle,
                        onToggle = { yearly ->
                            isYearlyToggle = yearly
                            if (yearly) {
                                selectedPlan = PRO_PLAN_OPTIONS.first { it.id == "12m" }
                            } else {
                                selectedPlan = PRO_PLAN_OPTIONS.first { it.id == "1m" }
                            }
                        }
                    )

                    // Kartu Paket Pro (Utama)
                    ProPackageCard(
                        selectedPlan = selectedPlan,
                        onSelectPlan = { selectedPlan = it },
                        installationId = installationId,
                        storeName = storeName,
                        onOrderViaWhatsApp = { plan ->
                            sendWhatsAppOrder(context, plan, installationId, storeName)
                        }
                    )

                    // Kartu Paket Gratis (Free Tier)
                    FreePackageCard(isCurrentTier = !licenseInfo.isProActive)

                    // Kartu Paket Bisnis (Coming Soon)
                    BusinessPackageCard()

                    Spacer(modifier = Modifier.height(32.dp))
                }
            } else {
                // Tab 1: Input Kode Lisensi RSA (Aktivasi Offline)
                OfflineActivationTab(
                    installationId = installationId,
                    storeName = storeName,
                    licenseKeyInput = licenseKeyInput,
                    isActivating = isActivatingKey,
                    errorMessage = keyErrorMessage,
                    onKeyChange = { licenseKeyInput = it },
                    onActivateClick = { onActivateKey(licenseKeyInput.trim()) },
                    onRequestKeyWhatsApp = {
                        sendWhatsAppOrder(context, selectedPlan, installationId, storeName)
                    }
                )
            }
        }
    }
}

@Composable
private fun CurrentSubscriptionStatusCard(licenseInfo: LicenseInfo) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (licenseInfo.isProActive) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
            else MaterialTheme.colorScheme.surfaceVariant
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (licenseInfo.isProActive) GreenPrimary else Color(0xFF64748B)
                    ) {
                        Text(
                            text = if (licenseInfo.isPremium) "Pro" else if (licenseInfo.isTrialActive) "Pro Trial" else "Gratis",
                            color = Color.White,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (licenseInfo.isProActive) SuccessGreen.copy(alpha = 0.15f) else Color(0xFFE2E8F0)
                    ) {
                        Text(
                            text = if (licenseInfo.isProActive) "Aktif" else "Free Tier",
                            color = if (licenseInfo.isProActive) SuccessGreen else Color(0xFF475569),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                if (licenseInfo.isTrialActive) {
                    Text(
                        text = "${licenseInfo.trialDaysRemaining} Hari Tersisa",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (licenseInfo.isProActive) Icons.AutoMirrored.Filled.TrendingUp else Icons.Default.Lock,
                        contentDescription = null,
                        tint = if (licenseInfo.isProActive) SuccessGreen else MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = when {
                                licenseInfo.isPremium -> "Semua fitur Pro aktif tanpa batas"
                                licenseInfo.isTrialActive -> "Masa Promo Pro Aktif (${licenseInfo.trialDaysRemaining} hari tersisa)"
                                else -> "Masa Percobaan Pro Berakhir (Fitur Free Aktif)"
                            },
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = when {
                                licenseInfo.isPremium -> "Nikmati analisa laba, ekspor laporan, dan produk unlimited."
                                licenseInfo.isTrialActive -> "Nikmati semua fitur Pro. Perpanjang sebelum habis biar tidak terputus."
                                else -> "Tingkatkan ke Pro untuk membuka kembali analisa laba, ekspor excel, dan produk tanpa batas."
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FeatureComparisonCard() {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Lightbulb,
                    contentDescription = null,
                    tint = PremiumGold,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Dari ribet manual ➔ otomatis jalan sendiri",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Kolom TANPA Pro
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "TANPA PRO",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                        color = Color(0xFFE53935)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    ComparisonItem(isPro = false, text = "Catat stok di buku tulis")
                    ComparisonItem(isPro = false, text = "Hitung omzet & laba manual")
                    ComparisonItem(isPro = false, text = "Tebak menu/barang paling laku")
                    ComparisonItem(isPro = false, text = "Batas hanya 30 produk")
                    ComparisonItem(isPro = false, text = "Rekap manual sampai malam")
                }

                // Kolom DENGAN Pro
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "DENGAN PRO",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                        color = SuccessGreen
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    ComparisonItem(isPro = true, text = "Stok auto-kurang tiap transaksi")
                    ComparisonItem(isPro = true, text = "Omzet & laba muncul otomatis")
                    ComparisonItem(isPro = true, text = "Top produk terlihat real-time")
                    ComparisonItem(isPro = true, text = "Tambah produk unlimited")
                    ComparisonItem(isPro = true, text = "Export laporan 1 tap ke Excel")
                }
            }
        }
    }
}

@Composable
private fun ComparisonItem(isPro: Boolean, text: String) {
    Row(
        verticalAlignment = Alignment.Top,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Icon(
            imageVector = if (isPro) Icons.Default.Check else Icons.Default.Close,
            contentDescription = null,
            tint = if (isPro) SuccessGreen else Color(0xFFE53935),
            modifier = Modifier
                .size(16.dp)
                .padding(top = 2.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall.copy(
                fontSize = 11.sp,
                lineHeight = 14.sp,
                textDecoration = if (!isPro) TextDecoration.LineThrough else TextDecoration.None
            ),
            color = if (isPro) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun BillingToggleBar(
    isYearly: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Button(
                onClick = { onToggle(false) },
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (!isYearly) GreenPrimary else Color.Transparent,
                    contentColor = if (!isYearly) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                ),
                elevation = null,
                modifier = Modifier.weight(1f)
            ) {
                Text("Pilihan Durasi / Bulanan", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }

            Button(
                onClick = { onToggle(true) },
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isYearly) GreenPrimary else Color.Transparent,
                    contentColor = if (isYearly) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                ),
                elevation = null,
                modifier = Modifier.weight(1f)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Tahunan", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = PremiumGold.copy(alpha = 0.25f)
                    ) {
                        Text(
                            text = "Hemat 20%",
                            color = Color(0xFFD97706),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold, fontSize = 9.sp),
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProPackageCard(
    selectedPlan: ProPlanDuration,
    onSelectPlan: (ProPlanDuration) -> Unit,
    installationId: String,
    storeName: String,
    onOrderViaWhatsApp: (ProPlanDuration) -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(GreenPrimary)),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Header Card
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = GreenPrimary
                ) {
                    Text(
                        text = "Pro",
                        color = Color.White,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = PremiumGold.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "Paling Populer",
                        color = Color(0xFFD97706),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Harga Dinamis
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = CurrencyFormatter.formatRupiah(selectedPlan.price),
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "/ ${selectedPlan.title}",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }

            Text(
                text = "Hitungan hemat: ${selectedPlan.dailyPriceNote}",
                style = MaterialTheme.typography.labelSmall,
                color = GreenPrimary
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Pilih Durasi Paket:",
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Horizontal Selector Pilihan Durasi (1 hari, 3 hari, 1 minggu, 1 bulan, 3 bulan, 6 bulan, 12 bulan)
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(PRO_PLAN_OPTIONS) { plan ->
                    val isSelected = selectedPlan.id == plan.id
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) GreenPrimary else MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .clickable { onSelectPlan(plan) }
                            .border(
                                width = if (isSelected) 1.5.dp else 0.dp,
                                color = if (isSelected) GreenPrimary else Color.Transparent,
                                shape = RoundedCornerShape(10.dp)
                            )
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = plan.title,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = CurrencyFormatter.formatRupiah(plan.price),
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = if (isSelected) Color.White.copy(alpha = 0.9f) else MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Fitur Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FeatureBadge(text = "1 Outlet")
                FeatureBadge(text = "3 Staf Kasir")
                FeatureBadge(text = "Produk Unlimited")
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(12.dp))

            // Daftar Benefit
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                BenefitItem("Semua fitur kasir & cetak struk Bluetooth")
                BenefitItem("Detail Laba Rugi & HPP otomatis")
                BenefitItem("Ekspor Laporan ke Spreadsheet (Excel / CSV)")
                BenefitItem("Kelola produk tanpa batas (Unlimited)")
                BenefitItem("Bebas edit nama toko & detail produk")
                BenefitItem("Tambah hingga 2 staf kasir dengan PIN unik")
            }

            Spacer(modifier = Modifier.height(18.dp))

            // CTA Button: Pesan via WhatsApp
            Button(
                onClick = { onOrderViaWhatsApp(selectedPlan) },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Chat,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Aktifkan Pro (${selectedPlan.title}) • ${CurrencyFormatter.formatRupiah(selectedPlan.price)}",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun FreePackageCard(isCurrentTier: Boolean) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFF64748B)
                ) {
                    Text(
                        text = "Gratis",
                        color = Color.White,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                if (isCurrentTier) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = "Paket Anda Saat Ini",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Mulai jualan tanpa modal. Pas buat warung baru buka.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FeatureBadge(text = "1 Outlet")
                FeatureBadge(text = "1 Staf (Admin)")
                FeatureBadge(text = "Maks 30 Produk")
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(10.dp))

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                BenefitItem("Point of Sale & Transaksi Kasir")
                BenefitItem("Cetak Struk Thermal Bluetooth")
                BenefitItem("Scanner Barcode Kamera")
                BenefitItem("Database SQLite Offline Lokal")
            }
        }
    }
}

@Composable
private fun BusinessPackageCard() {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFF7C3AED)
                ) {
                    Text(
                        text = "Bisnis",
                        color = Color.White,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFFF3E8FF)
                ) {
                    Text(
                        text = "Coming Soon",
                        color = Color(0xFF7C3AED),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Untuk toko skala besar dengan multi-cabang & integrasi terpusat.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FeatureBadge(text = "Multi Outlet")
                FeatureBadge(text = "Staf Unlimited")
                FeatureBadge(text = "Cloud Sync")
            }
        }
    }
}

@Composable
private fun OfflineActivationTab(
    installationId: String,
    storeName: String,
    licenseKeyInput: String,
    isActivating: Boolean,
    errorMessage: String?,
    onKeyChange: (String) -> Unit,
    onActivateClick: () -> Unit,
    onRequestKeyWhatsApp: () -> Unit
) {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "ID Instalasi Perangkat Anda:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = installationId,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )

                    IconButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("ID Instalasi", installationId))
                            Toast.makeText(context, "ID Instalasi disalin!", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Salin ID", tint = MaterialTheme.colorScheme.primary)
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Nama Toko: $storeName",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        OutlinedTextField(
            value = licenseKeyInput,
            onValueChange = onKeyChange,
            label = { Text("Masukkan Kode Lisensi RSA (Payload.Signature)") },
            leadingIcon = { Icon(Icons.Default.Key, contentDescription = null) },
            shape = RoundedCornerShape(12.dp),
            minLines = 3,
            maxLines = 5,
            modifier = Modifier.fillMaxWidth()
        )

        if (errorMessage != null) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold)
            )
        }

        Button(
            onClick = onActivateClick,
            enabled = licenseKeyInput.isNotBlank() && !isActivating,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            if (isActivating) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Shield, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Verifikasi & Aktifkan Lisensi", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }

        OutlinedButton(
            onClick = onRequestKeyWhatsApp,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = SuccessGreen),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = null, tint = SuccessGreen)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Hubungi CS via WhatsApp", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun FeatureBadge(text: String) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

@Composable
private fun BenefitItem(text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 2.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Check,
            contentDescription = null,
            tint = SuccessGreen,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

private fun sendWhatsAppOrder(
    context: Context,
    plan: ProPlanDuration,
    installationId: String,
    storeName: String
) {
    try {
        val message = """
            Halo CS Sumber Rejeki,
            Saya ingin berlangganan Paket Pro untuk Aplikasi Kasir Sumber Rejeki POS.

            📋 Detail Pesanan:
            • Paket: PRO (${plan.title})
            • Harga: ${CurrencyFormatter.formatRupiah(plan.price)}
            • Nama Toko: ${storeName.ifBlank { "Sumber Rejeki POS" }}
            • ID Instalasi: $installationId

            Mohon info nomor rekening pembayaran dan kode aktivasi lisensinya. Terima kasih!
        """.trimIndent()

        val url = "https://wa.me/6283832060902?text=" + Uri.encode(message)
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "Tidak dapat membuka WhatsApp: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}
