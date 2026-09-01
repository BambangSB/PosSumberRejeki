package com.sumberrejeki.pos.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.content.Intent
import android.net.Uri
import com.sumberrejeki.pos.domain.model.LicenseInfo
import com.sumberrejeki.pos.domain.model.Product
import com.sumberrejeki.pos.domain.model.User
import com.sumberrejeki.pos.domain.model.UserRole
import com.sumberrejeki.pos.domain.repository.AuthRepository
import com.sumberrejeki.pos.domain.repository.LicenseRepository
import com.sumberrejeki.pos.domain.usecase.auth.AddStaffUserUseCase
import com.sumberrejeki.pos.domain.usecase.auth.DeleteStaffUserUseCase
import com.sumberrejeki.pos.domain.usecase.auth.GetCurrentSessionUseCase
import com.sumberrejeki.pos.domain.usecase.auth.GetStaffUsersUseCase
import com.sumberrejeki.pos.domain.usecase.auth.LogoutUseCase
import com.sumberrejeki.pos.domain.usecase.license.GenerateLicenseHelper
import com.sumberrejeki.pos.domain.usecase.license.GetLicenseStatusUseCase
import com.sumberrejeki.pos.domain.usecase.license.VerifyRsaLicenseUseCase
import com.sumberrejeki.pos.domain.usecase.product.DeleteProductUseCase
import com.sumberrejeki.pos.domain.usecase.product.GetProductsUseCase
import com.sumberrejeki.pos.utils.backup.BackupExportResult
import com.sumberrejeki.pos.utils.backup.BackupPreview
import com.sumberrejeki.pos.utils.backup.DatabaseBackupManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

data class ProfileUiState(
    val licenseInfo: LicenseInfo = LicenseInfo(),
    val installationId: String = "",
    val staffUsers: List<User> = emptyList(),
    val products: List<Product> = emptyList(),
    val productSearchQuery: String = "",
    val licenseKeyInput: String = "",
    val isActivatingLicense: Boolean = false,
    val isProcessingBackup: Boolean = false,
    val message: String? = null,
    val errorMessage: String? = null,
    val backupExportResult: BackupExportResult? = null,
    val backupRestorePreview: BackupPreview? = null
)

class ProfileViewModel(
    private val logoutUseCase: LogoutUseCase,
    private val getStaffUsersUseCase: GetStaffUsersUseCase,
    private val addStaffUserUseCase: AddStaffUserUseCase,
    private val deleteStaffUserUseCase: DeleteStaffUserUseCase,
    private val verifyRsaLicenseUseCase: VerifyRsaLicenseUseCase,
    private val getLicenseStatusUseCase: GetLicenseStatusUseCase,
    private val getProductsUseCase: GetProductsUseCase,
    private val deleteProductUseCase: DeleteProductUseCase,
    private val authRepository: AuthRepository,
    private val licenseRepository: LicenseRepository,
    private val backupManager: DatabaseBackupManager,
    getCurrentSessionUseCase: GetCurrentSessionUseCase
) : ViewModel() {

    val currentUser: StateFlow<User?> = getCurrentSessionUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val id = getLicenseStatusUseCase.getInstallationId()
            _uiState.value = _uiState.value.copy(installationId = id)
        }

        viewModelScope.launch {
            getLicenseStatusUseCase.licenseInfoFlow.collect { info ->
                _uiState.value = _uiState.value.copy(licenseInfo = info)
            }
        }

        viewModelScope.launch {
            getStaffUsersUseCase().collect { users ->
                _uiState.value = _uiState.value.copy(staffUsers = users)
            }
        }

        viewModelScope.launch {
            getProductsUseCase.allProductsFlow.collect { list ->
                _uiState.value = _uiState.value.copy(products = list)
            }
        }
    }

    fun onLicenseKeyInputChanged(key: String) {
        _uiState.value = _uiState.value.copy(
            licenseKeyInput = key,
            errorMessage = null
        )
    }

    fun activateLicense(onSuccess: () -> Unit) {
        val key = _uiState.value.licenseKeyInput
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isActivatingLicense = true, errorMessage = null)
            val result = verifyRsaLicenseUseCase(key)
            result.onSuccess { info ->
                _uiState.value = _uiState.value.copy(
                    isActivatingLicense = false,
                    licenseInfo = info,
                    message = "Selamat! Lisensi Premium Berhasil Diaktifkan!"
                )
                onSuccess()
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isActivatingLicense = false,
                    errorMessage = error.message ?: "Verifikasi lisensi gagal"
                )
            }
        }
    }

    fun generateDemoLicenseForTesting() {
        val id = _uiState.value.installationId
        if (id.isNotBlank()) {
            val demoKey = GenerateLicenseHelper.generateOfflineLicenseForDevice(id, "Toko Sumber Rejeki")
            _uiState.value = _uiState.value.copy(licenseKeyInput = demoKey)
        }
    }

    fun addStaffUser(username: String, pin: String, role: UserRole, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(errorMessage = null)
            val result = addStaffUserUseCase(username, pin, role)
            result.onSuccess {
                _uiState.value = _uiState.value.copy(message = "Pengguna '${it.username}' berhasil ditambahkan")
                onSuccess()
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    errorMessage = error.message ?: "Gagal menambahkan pengguna"
                )
            }
        }
    }

    fun deleteStaffUser(user: User) {
        val adminId = currentUser.value?.userId ?: 0L
        viewModelScope.launch {
            val result = deleteStaffUserUseCase(user.userId, adminId)
            result.onSuccess {
                _uiState.value = _uiState.value.copy(message = "Pengguna '${user.username}' telah dihapus")
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    errorMessage = error.message ?: "Gagal menghapus pengguna"
                )
            }
        }
    }

    fun onProductSearchQueryChanged(query: String) {
        _uiState.value = _uiState.value.copy(productSearchQuery = query)
    }

    fun deleteProduct(product: Product, onSuccess: () -> Unit = {}) {
        val role = currentUser.value?.role ?: UserRole.CASHIER
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(errorMessage = null)
            val result = deleteProductUseCase(product.productId, role)
            result.onSuccess {
                _uiState.value = _uiState.value.copy(
                    message = "Produk '${product.name}' berhasil dihapus dari database"
                )
                onSuccess()
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    errorMessage = error.message ?: "Gagal menghapus produk '${product.name}'"
                )
            }
        }
    }

    fun updateStoreName(newName: String, onSuccess: () -> Unit = {}) {
        val trimmed = newName.trim()
        if (trimmed.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Nama toko tidak boleh kosong")
            return
        }
        viewModelScope.launch {
            try {
                licenseRepository.updateStoreName(trimmed)
                _uiState.value = _uiState.value.copy(
                    message = "Nama toko berhasil diubah menjadi '$trimmed'"
                )
                onSuccess()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = e.message ?: "Gagal mengubah nama toko"
                )
            }
        }
    }

    fun updateAdminUsername(newUsername: String, onSuccess: () -> Unit = {}) {
        val currentUserId = currentUser.value?.userId ?: 0L
        val trimmed = newUsername.trim()
        if (trimmed.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Nama admin tidak boleh kosong")
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(errorMessage = null)
            val result = authRepository.updateUsername(currentUserId, trimmed)
            result.onSuccess {
                _uiState.value = _uiState.value.copy(
                    message = "Nama akun admin berhasil diubah menjadi '$trimmed'"
                )
                onSuccess()
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    errorMessage = error.message ?: "Gagal mengubah nama akun"
                )
            }
        }
    }

    fun exportDatabase(onSuccess: (BackupExportResult) -> Unit = {}) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isProcessingBackup = true, errorMessage = null)
            try {
                val result = backupManager.exportDatabase()
                _uiState.value = _uiState.value.copy(
                    isProcessingBackup = false,
                    backupExportResult = result,
                    message = "Cadangan database berhasil dibuat: ${result.fileName}"
                )
                onSuccess(result)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isProcessingBackup = false,
                    errorMessage = "Gagal membuat cadangan database: ${e.message}"
                )
            }
        }
    }

    fun prepareRestoreFromUri(uri: Uri, onPreviewReady: (BackupPreview) -> Unit = {}) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isProcessingBackup = true, errorMessage = null)
            try {
                val jsonString = backupManager.readJsonFromUri(uri)
                val preview = backupManager.previewBackupJson(jsonString)
                _uiState.value = _uiState.value.copy(
                    isProcessingBackup = false,
                    backupRestorePreview = if (preview.isValid) preview else null,
                    errorMessage = if (!preview.isValid) preview.errorMessage else null
                )
                if (preview.isValid) {
                    onPreviewReady(preview)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isProcessingBackup = false,
                    errorMessage = "Gagal membaca file cadangan: ${e.message}"
                )
            }
        }
    }

    fun confirmRestore(preview: BackupPreview, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isProcessingBackup = true, errorMessage = null)
            val result = backupManager.restoreFromJson(preview.jsonContent)
            result.onSuccess { restoredPreview ->
                _uiState.value = _uiState.value.copy(
                    isProcessingBackup = false,
                    backupRestorePreview = null,
                    message = "Pemulihan database berhasil! (${restoredPreview.productCount} Produk, ${restoredPreview.userCount} Pengguna, ${restoredPreview.orderCount} Transaksi)"
                )
                onSuccess()
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isProcessingBackup = false,
                    errorMessage = "Gagal memulihkan database: ${error.message}"
                )
            }
        }
    }

    fun dismissBackupResult() {
        _uiState.value = _uiState.value.copy(backupExportResult = null)
    }

    fun dismissRestorePreview() {
        _uiState.value = _uiState.value.copy(backupRestorePreview = null)
    }

    fun createShareIntent(file: File): Intent {
        return backupManager.createShareIntent(file)
    }

    fun logout(onLoggedOut: () -> Unit) {
        viewModelScope.launch {
            logoutUseCase()
            onLoggedOut()
        }
    }

    fun clearMessage() {
        _uiState.value = _uiState.value.copy(message = null, errorMessage = null)
    }
}
