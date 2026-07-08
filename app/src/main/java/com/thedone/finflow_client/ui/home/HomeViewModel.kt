package com.thedone.finflow_client.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thedone.finflow_client.data.local.TokenManager
import com.thedone.finflow_client.data.remote.dto.TransactionResponseDto
import com.thedone.finflow_client.domain.repo.TransactionRepository
import com.thedone.finflow_client.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject


data class HomeState(
    val isLoading: Boolean = false,
    val transactions: List<TransactionResponseDto> = emptyList(),
    val balance: Double = 0.0,
    val error: String = "",
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: TransactionRepository,
    private val tokenManager: TokenManager,
) : ViewModel() {

    private val _state = MutableStateFlow(HomeState())
    val state = _state.asStateFlow()

    init {
        getTransactions()
    }

    private fun getTransactions() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = "") }

            when (val result = repository.getTransactions()) {
                is Resource.Success -> {
                    val txList = result.data ?: emptyList()
                    _state.update {
                        it.copy(
                            isLoading = false,
                            transactions = txList,
                            balance = calculateBalance(txList)
                        )
                    }
                }

                is Resource.Error -> {
                    _state.update {
                        it.copy(isLoading = false, error = result.message ?: "Bilinmeyen Hata")
                    }
                }

                else -> Unit
            }
        }
    }

    fun addTransaction(type: String, amount: Double, description: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = "") }

            val result = repository.addTransaction(type, amount, description)

            if (result is Resource.Success && result.data != null) {
                // Tipi açıkça belirterek derleyicinin kafasının karışmasını kesin olarak önlüyoruz
                val newTransaction: TransactionResponseDto = result.data

                _state.update { currentState ->
                    // Eski listeye (+) operatörü ile yeni nesneyi ekleyip yeni bir liste oluşturuyoruz (Çok daha güvenli)
                    val updatedList = currentState.transactions + newTransaction

                    currentState.copy(
                        isLoading = false,
                        transactions = updatedList,
                        balance = calculateBalance(updatedList)
                    )
                }
            } else if (result is Resource.Error) {
                _state.update {
                    it.copy(isLoading = false, error = result.message ?: "Ekleme hatası")
                }
            }
        }
    }

    fun deleteTransaction(id: Int) {
        viewModelScope.launch {
            when (val result = repository.deleteTransaction(id)) {
                is Resource.Success -> {
                    val updatedList = _state.value.transactions.filter { it.id != id }
                    _state.update {
                        it.copy(
                            transactions = updatedList,
                            balance = calculateBalance(updatedList)
                        )
                    }
                }

                is Resource.Error -> {
                    _state.update { it.copy(error = result.message ?: "Silme başarısız oldu") }
                }

                else -> {}
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            tokenManager.clearToken()
        }
    }

    private fun calculateBalance(transactions: List<TransactionResponseDto>): Double {
        return transactions.sumOf {
            if (it.type == "INCOME") it.amount else -it.amount
        }
    }
}