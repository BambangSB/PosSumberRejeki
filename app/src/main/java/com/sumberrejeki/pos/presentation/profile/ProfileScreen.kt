package com.sumberrejeki.pos.presentation.profile

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.HeadsetMic
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sumberrejeki.pos.domain.model.UserRole
import com.sumberrejeki.pos.presentation.theme.PremiumGold
import com.sumberrejeki.pos.presentation.theme.RoleAdminBadge
import com.sumberrejeki.pos.presentation.theme.RoleCashierBadge
import com.sumberrejeki.pos.presentation.theme.SuccessGreen

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    onLogoutSuccess: () -> Unit,
    onNavigateToManageProducts: () -> Unit = {},
    onNavigateToSubscription: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentUser by viewModel.currentUser.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    var showUpgradeModal by remember { mutableStateOf(false) }
    var showLogoutConfirmDialog by remember { mutableStateOf(false) }

    // Dialog state for Edit Store Name
    var showEditStoreDialog by remember { mutableStateOf(false) }
    var inputStoreName by remember { mutableStateOf("") }
    var showConfirmStoreNameDialog by remember { mutableStateOf(false) }
    var showUpgradeRequiredForStoreNameDialog by remember { mutableStateOf(false) }

    // Dialog state for Edit Admin Name
    var showEditAdminDialog by remember { mutableStateOf(false) }
    var inputAdminName by remember { mutableStateOf("") }
    var showConfirmAdminNameDialog by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }

    // File picker for restoring database JSON
    val restoreFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.prepareRestoreFromUri(uri)
        }
    }

    LaunchedEffect(uiState.message) {
        uiState.message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. User Profile Info Card
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(
                                if (currentUser?.role == UserRole.ADMIN) RoleAdminBadge.copy(alpha = 0.15f)
                                else RoleCashierBadge.copy(alpha = 0.15f)
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = null,
                            tint = if (currentUser?.role == UserRole.ADMIN) RoleAdminBadge else RoleCashierBadge,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = currentUser?.username ?: "Pengguna",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            if (currentUser?.role == UserRole.ADMIN) {
                                IconButton(
                                    onClick = {
                                        inputAdminName = currentUser?.username ?: ""
                                        showEditAdminDialog = true
                                    },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Ubah Nama Admin",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (currentUser?.role == UserRole.ADMIN) RoleAdminBadge.copy(alpha = 0.15f)
                            else RoleCashierBadge.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = if (currentUser?.role == UserRole.ADMIN) "ROLE: ADMIN TOKO" else "ROLE: KASIR",
                                color = if (currentUser?.role == UserRole.ADMIN) RoleAdminBadge else RoleCashierBadge,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            // 2. Store Info & License Status Card
            val isPro = uiState.licenseInfo.isProActive
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isPro) MaterialTheme.colorScheme.primaryContainer
                    else MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Store Name Header with Edit Option
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Store,
                                contentDescription = null,
                                tint = if (isPro) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = uiState.licenseInfo.licensedTo.ifBlank { "Toko Sumber Rejeki" },
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = if (isPro) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                            )
                        }

                        if (currentUser?.role == UserRole.ADMIN) {
                            IconButton(
                                onClick = {
                                    if (!uiState.licenseInfo.canEditStoreName) {
                                        showUpgradeRequiredForStoreNameDialog = true
                                    } else {
                                        inputStoreName = uiState.licenseInfo.licensedTo.ifBlank { "Sumber Rejeki POS" }
                                        showEditStoreDialog = true
                                    }
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Ubah Nama Toko",
                                    tint = if (isPro) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    HorizontalDivider(
                        color = (if (isPro) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.outlineVariant).copy(
                            alpha = 0.2f
                        ),
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    // License Details
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isPro) Icons.Default.Star else Icons.Default.Shield,
                                contentDescription = null,
                                tint = if (isPro) PremiumGold else MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            val tierTitle = when {
                                uiState.licenseInfo.isTrialActive -> "Lisensi: PRO (Trial - ${uiState.licenseInfo.trialDaysRemaining} hari)"
                                uiState.licenseInfo.isProActive -> "Lisensi: PRO (Aktif)"
                                else -> "Lisensi: GRATIS (Terbatas)"
                            }
                            Text(
                                text = tierTitle,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = if (isPro) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Button(
                            onClick = onNavigateToSubscription,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            contentPadding = ButtonDefaults.ContentPadding
                        ) {
                            Text(
                                text = if (isPro) "Paket Pro" else "Upgrade",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    val descText = when {
                        uiState.licenseInfo.isTrialActive -> "Semua fitur Pro aktif selama masa percobaan 3 hari. Nikmati akses tanpa batas!"
                        uiState.licenseInfo.isProActive -> "Fitur Pro Aktif: Akses rincian laba rugi, ekspor spreadsheet, tanpa batas produk, dan multi-kasir."
                        else -> "Versi Gratis: Terbatas 30 produk, 1 admin, tanpa rincian laba rugi & ekspor. Upgrade ke Pro mulai Rp 2.000/hari."
                    }
                    Text(
                        text = descText,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isPro) MaterialTheme.colorScheme.onPrimaryContainer.copy(
                            alpha = 0.8f
                        ) else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // 3. Dedicated Manage & Delete Products Card (Admin Only)
            if (currentUser?.role == UserRole.ADMIN) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Inventory2,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Kelola & Hapus Produk",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Text(
                                    text = "${uiState.products.size} Produk",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Button(
                            onClick = onNavigateToManageProducts,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Inventory2, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Buka Kelola & Hapus Produk", fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.weight(1f))
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                // 4. Staff Management Section (Admin Only)
                StaffManagementSection(
                    staffUsers = uiState.staffUsers,
                    licenseInfo = uiState.licenseInfo,
                    currentUserId = currentUser?.userId ?: 0L,
                    onAddStaff = { username, pin, role ->
                        viewModel.addStaffUser(username, pin, role) {}
                    },
                    onDeleteStaff = { user ->
                        viewModel.deleteStaffUser(user)
                    },
                    onUpgradeLicenseClick = onNavigateToSubscription
                )
            }

            // 5. Customer Service WhatsApp Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = SuccessGreen.copy(alpha = 0.12f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.HeadsetMic,
                            contentDescription = null,
                            tint = SuccessGreen,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Bantuan & Layanan Pelanggan",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Text(
                        text = "Ada pertanyaan seputar aktivasi, penggunaan kasir, atau kendala teknis? Hubungi tim support kami via WhatsApp.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Button(
                        onClick = {
                            try {
                                val url =
                                    "https://wa.me/6283832060902?text=" + Uri.encode("Halo Admin Support POS Sumber Rejeki, saya membutuhkan bantuan.")
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                Toast.makeText(
                                    context,
                                    "Tidak dapat membuka WhatsApp: ${e.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Chat Sekarang", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }

            // 6. Data & Cadangan (Backup & Restore Tools)
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Backup,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Data & Cadangan (Backup & Restore)",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Text(
                        text = "Amankan database produk, pengguna, dan riwayat transaksi ke folder Download atau pulihkan dari file cadangan sebelumnya.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Export Button
                    Button(
                        onClick = { viewModel.exportDatabase() },
                        enabled = !uiState.isProcessingBackup,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (uiState.isProcessingBackup) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Sedang Memproses...")
                        } else {
                            Icon(Icons.Default.Backup, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Export Backup Database (JSON)", fontWeight = FontWeight.Bold)
                        }
                    }

                    // Restore Button
                    OutlinedButton(
                        onClick = {
                            restoreFileLauncher.launch("*/*")
                        },
                        enabled = !uiState.isProcessingBackup,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Restore, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Restore Database dari File (JSON)", fontWeight = FontWeight.Bold)
                    }
                }
            }

            // 7. Logout Button
            OutlinedButton(
                onClick = { showLogoutConfirmDialog = true },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Logout,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Keluar dari Sesi Kasir", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp)
        )
    }

    // Modal Edit Nama Toko
    if (showEditStoreDialog) {
        AlertDialog(
            onDismissRequest = { showEditStoreDialog = false },
            title = { Text("Ubah Nama Toko", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Masukkan nama toko baru yang akan ditampilkan di struk dan aplikasi:")
                    OutlinedTextField(
                        value = inputStoreName,
                        onValueChange = { inputStoreName = it },
                        label = { Text("Nama Toko") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (inputStoreName.isNotBlank()) {
                            showEditStoreDialog = false
                            showConfirmStoreNameDialog = true
                        }
                    }
                ) {
                    Text("Lanjut")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditStoreDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }

    // Dialog Persetujuan Ubah Nama Toko
    if (showConfirmStoreNameDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmStoreNameDialog = false },
            title = { Text("Konfirmasi Ubah Nama Toko", fontWeight = FontWeight.Bold) },
            text = {
                Text("Apakah Anda yakin ingin mengubah nama toko menjadi '${inputStoreName.trim()}'?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showConfirmStoreNameDialog = false
                        viewModel.updateStoreName(inputStoreName.trim())
                    }
                ) {
                    Text("Ya, Simpan")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmStoreNameDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }

    // Modal Edit Nama Admin
    if (showEditAdminDialog) {
        AlertDialog(
            onDismissRequest = { showEditAdminDialog = false },
            title = { Text("Ubah Nama Admin", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Masukkan nama baru untuk akun admin Anda:")
                    OutlinedTextField(
                        value = inputAdminName,
                        onValueChange = { inputAdminName = it },
                        label = { Text("Nama Admin") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (inputAdminName.isNotBlank()) {
                            showEditAdminDialog = false
                            showConfirmAdminNameDialog = true
                        }
                    }
                ) {
                    Text("Lanjut")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditAdminDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }

    // Dialog Persetujuan Ubah Nama Admin
    if (showConfirmAdminNameDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmAdminNameDialog = false },
            title = { Text("Konfirmasi Ubah Nama Admin", fontWeight = FontWeight.Bold) },
            text = {
                Text("Apakah Anda yakin ingin mengubah nama akun admin menjadi '${inputAdminName.trim()}'?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showConfirmAdminNameDialog = false
                        viewModel.updateAdminUsername(inputAdminName.trim())
                    }
                ) {
                    Text("Ya, Simpan")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmAdminNameDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }

    // Modal Upgrade License
    if (showUpgradeModal) {
        UpgradeLicenseModal(
            installationId = uiState.installationId,
            licensedTo = uiState.licenseInfo.licensedTo.ifBlank { "Toko Sumber Rejeki" },
            licenseKeyInput = uiState.licenseKeyInput,
            isActivating = uiState.isActivatingLicense,
            errorMessage = uiState.errorMessage,
            onLicenseKeyChange = { viewModel.onLicenseKeyInputChanged(it) },
            onActivateClick = {
                viewModel.activateLicense {
                    showUpgradeModal = false
                }
            },
            onRequestKeyToCS = {
                try {
                    val storeName = uiState.licenseInfo.licensedTo.ifBlank { "Toko Sumber Rejeki" }
                    val message = "Halo CS Sumber Rejeki,\nSaya ingin request key PREMIUM untuk aplikasi kasir saya.\nBerikut datanya:\n- ID Instalasi: ${uiState.installationId}\n- Nama Toko: $storeName\n\nMohon bantuannya, terima kasih."
                    val url = "https://wa.me/6283832060902?text=" + Uri.encode(message)
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    context.startActivity(intent)
                } catch (e: Exception) {
                    Toast.makeText(
                        context,
                        "Tidak dapat membuka WhatsApp: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            },
            onDismiss = { showUpgradeModal = false }
        )
    }

    // Logout Confirm Dialog
    if (showLogoutConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutConfirmDialog = false },
            title = { Text("Konfirmasi Keluar") },
            text = { Text("Apakah Anda yakin ingin keluar dari sesi kasir aktif?") },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutConfirmDialog = false
                        viewModel.logout(onLogoutSuccess)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Keluar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutConfirmDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }

    // Dialog Hasil Export Database (Lokasi File & Sharing)
    if (uiState.backupExportResult != null) {
        val exportResult = uiState.backupExportResult!!
        AlertDialog(
            onDismissRequest = { viewModel.dismissBackupResult() },
            icon = {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = SuccessGreen,
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text("Backup Database Berhasil!", fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "File cadangan telah berhasil dibuat dan disimpan di perangkat Anda.",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Folder,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Nama File:",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                            Text(
                                text = exportResult.fileName,
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.primary
                            )

                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                            Text(
                                text = "Lokasi Penyimpanan:",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = exportResult.filePath,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                            Text(
                                text = "Rincian Data Tersimpan:",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "• ${exportResult.productCount} Produk\n• ${exportResult.userCount} Pengguna\n• ${exportResult.orderCount} Riwayat Transaksi",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Text(
                        text = "Anda dapat menyalin file ini atau membagikannya ke Google Drive, WhatsApp, atau email untuk cadangan jangka panjang.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        try {
                            val shareIntent = viewModel.createShareIntent(exportResult.file)
                            context.startActivity(Intent.createChooser(shareIntent, "Bagikan File Backup"))
                        } catch (e: Exception) {
                            Toast.makeText(context, "Gagal membagikan file: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Bagikan / Kirim")
                }
            },
            dismissButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    TextButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("Lokasi Backup POS", exportResult.filePath)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "Path file disalin ke clipboard!", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Salin Lokasi")
                    }

                    TextButton(onClick = { viewModel.dismissBackupResult() }) {
                        Text("Tutup")
                    }
                }
            }
        )
    }

    // Dialog Konfirmasi Restore Database
    if (uiState.backupRestorePreview != null) {
        val preview = uiState.backupRestorePreview!!
        AlertDialog(
            onDismissRequest = { viewModel.dismissRestorePreview() },
            icon = {
                Icon(
                    imageVector = Icons.Default.Restore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text("Konfirmasi Restore Database", fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "File cadangan valid dan siap dipulihkan ke aplikasi:",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "Tanggal Backup: ${preview.backupDateFormatted}",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.primary
                            )
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                            Text(
                                text = "Data yang akan dipulihkan:\n• ${preview.productCount} Produk\n• ${preview.userCount} Pengguna\n• ${preview.orderCount} Riwayat Transaksi",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.WarningAmber,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Data database yang ada saat ini akan diperbarui dengan data cadangan ini.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.confirmRestore(preview)
                    }
                ) {
                    Text("Ya, Pulihkan Sekarang")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissRestorePreview() }) {
                    Text("Batal")
                }
            }
        )
    }

    if (showUpgradeRequiredForStoreNameDialog) {
        AlertDialog(
            onDismissRequest = { showUpgradeRequiredForStoreNameDialog = false },
            icon = { Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFFD97706)) },
            title = { Text("Ubah Nama Toko Terkunci", fontWeight = FontWeight.Bold) },
            text = {
                Text("Fitur kustomisasi nama toko pada struk belanja dan nota hanya tersedia untuk pelanggan paket Pro. Upgrade sekarang mulai dari Rp 2.000/hari!")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showUpgradeRequiredForStoreNameDialog = false
                        onNavigateToSubscription()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706))
                ) {
                    Text("Upgrade ke Pro", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showUpgradeRequiredForStoreNameDialog = false }) {
                    Text("Nanti Saja")
                }
            }
        )
    }
}
