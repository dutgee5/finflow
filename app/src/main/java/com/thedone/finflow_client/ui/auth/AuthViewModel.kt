package com.thedone.finflow_client.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thedone.finflow_client.domain.repo.AuthRepository
import com.thedone.finflow_client.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


data class AuthState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false,
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: AuthRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(AuthState())
    val state = _state.asStateFlow()

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _state.value = AuthState(error = "Email and password cannot be blank!")
            return
        }

        viewModelScope.launch {
            _state.value = AuthState(isLoading = true)

            when (val result = repository.login(email, password)) {
                is Resource.Success -> _state.value = AuthState(isSuccess = true)
                is Resource.Error -> _state.value = AuthState(error = result.message)
                else -> Unit
            }
        }
    }

    fun register(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _state.value = AuthState(error = "Email and password cannot be blank!")
            return
        }

        viewModelScope.launch {
            _state.value = AuthState(isLoading = true)
            when (val result = repository.register(email, password)) {
                is Resource.Success -> _state.value = AuthState(isSuccess = true)
                is Resource.Error -> _state.value = AuthState(error = result.message)
                else -> Unit
            }
        }
    }
}