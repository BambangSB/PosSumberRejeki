package com.sumberrejeki.pos.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sumberrejeki.pos.domain.model.User
import com.sumberrejeki.pos.domain.usecase.auth.GetCurrentSessionUseCase
import com.sumberrejeki.pos.domain.usecase.auth.GetStaffUsersUseCase
import com.sumberrejeki.pos.domain.usecase.auth.LoginWithPinUseCase
import com.sumberrejeki.pos.domain.usecase.auth.RegisterAdminUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class AuthUiState(
    val selectedUser: User? = null,
    val pinInput: String = "",
    val errorMessage: String? = null,
    val isLoading: Boolean = false,
    val isLoginSuccess: Boolean = false
)

class AuthViewModel(
    private val loginWithPinUseCase: LoginWithPinUseCase,
    private val registerAdminUseCase: RegisterAdminUseCase,
    getStaffUsersUseCase: GetStaffUsersUseCase,
    getCurrentSessionUseCase: GetCurrentSessionUseCase
) : ViewModel() {

    val allUsers: StateFlow<List<User>> = getStaffUsersUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val currentSession: StateFlow<User?> = getCurrentSessionUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun selectUser(user: User) {
        _uiState.value = _uiState.value.copy(
            selectedUser = user,
            pinInput = "",
            errorMessage = null
        )
    }

    fun onPinChange(pin: String) {
        _uiState.value = _uiState.value.copy(
            pinInput = pin,
            errorMessage = null
        )
        // Automatically attempt login when PIN reaches 6 digits if user is selected
        if (pin.length == 6 && _uiState.value.selectedUser != null) {
            submitLogin()
        }
    }

    fun submitLogin() {
        val user = _uiState.value.selectedUser ?: return
        val pin = _uiState.value.pinInput

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val result = loginWithPinUseCase(user.username, pin)
            result.onSuccess {
                _uiState.value = _uiState.value.copy(isLoading = false, isLoginSuccess = true)
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    pinInput = "",
                    errorMessage = error.message ?: "PIN tidak valid"
                )
            }
        }
    }

    fun registerAdmin(username: String, pin: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val result = registerAdminUseCase(username, pin)
            result.onSuccess {
                _uiState.value = _uiState.value.copy(isLoading = false, isLoginSuccess = true)
                onSuccess()
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = error.message ?: "Gagal mendaftarkan toko"
                )
            }
        }
    }

    fun resetState() {
        _uiState.value = AuthUiState()
    }
}
