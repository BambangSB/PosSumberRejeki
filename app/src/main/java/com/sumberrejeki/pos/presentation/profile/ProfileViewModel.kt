package com.sumberrejeki.pos.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sumberrejeki.pos.domain.model.LicenseInfo
import com.sumberrejeki.pos.domain.model.User
import com.sumberrejeki.pos.domain.model.UserRole
import com.sumberrejeki.pos.domain.usecase.auth.AddStaffUserUseCase
import com.sumberrejeki.pos.domain.usecase.auth.DeleteStaffUserUseCase
import com.sumberrejeki.pos.domain.usecase.auth.GetCurrentSessionUseCase
import com.sumberrejeki.pos.domain.usecase.auth.GetStaffUsersUseCase
import com.sumberrejeki.pos.domain.usecase.auth.LogoutUseCase
import com.sumberrejeki.pos.domain.usecase.license.GenerateLicenseHelper
import com.sumberrejeki.pos.domain.usecase.license.GetLicenseStatusUseCase
import com.sumberrejeki.pos.domain.usecase.license.VerifyRsaLicenseUseCase
import com.sumberrejeki.pos.utils.backup.DatabaseBackupManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ProfileUiState(
    val licenseInfo: LicenseInfo = LicenseInfo(),
    val installationId: String = "",
    val staffUsers: List<User> = emptyList(),
    val licenseKeyInput: String = "",
    val isActivatingLicense: Boolean = false,
    val message: String? = null,
    val errorMessage: String? = null,
    val backupJsonString: String? = null
)

class ProfileViewModel(
    private val logoutUseCase: LogoutUseCase,
    private val getStaffUsersUseCase: GetStaffUsersUseCase,
    private val addStaffUserUseCase: AddStaffUserUseCase,
    private val deleteStaffUserUseCase: DeleteStaffUserUseCase,
    private val verifyRsaLicenseUseCase: VerifyRsaLicenseUseCase,
    private val getLicenseStatusUseCase: GetLicenseStatusUseCase,
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

    fun exportBackup(onExported: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val json = backupManager.exportToJson()
                _uiState.value = _uiState.value.copy(
                    backupJsonString = json,
                    message = "Backup database berhasil diexport!"
                )
                onExported(json)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = "Gagal membuat backup: ${e.message}")
            }
        }
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
