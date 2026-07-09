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
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject


data class HomeState(
    val isLoading: Boolean = false,
    val groupedTransactions: Map<String, List<TransactionResponseDto>> = emptyMap(),
    val transactions: List<TransactionResponseDto> = emptyList(),
    val balance: Double = 0.0,
    val error: String = "",
    val successMessage: String = "",
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
                            groupedTransactions = groupTransactions(txList),
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

            when (val result = repository.addTransaction(type, amount, description)) {
                is Resource.Success -> {
                    val newTransaction = result.data
                    if (newTransaction != null) {
                        _state.update { currenState ->
                            val updatedRaw =
                                currenState.groupedTransactions.values.flatten() + newTransaction
                            currenState.copy(
                                isLoading = false,
                                groupedTransactions = groupTransactions(updatedRaw),
                                balance = calculateBalance(updatedRaw),
                                successMessage = "İşlem başarıyla eklendi."
                            )
                        }
                    }
                }

                is Resource.Error -> {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = result.message ?: "Ekleme hatası"
                        )
                    }
                }

                else -> {}
            }
        }
    }

    fun deleteTransaction(id: Int) {
        viewModelScope.launch {
            when (val result = repository.deleteTransaction(id)) {
                is Resource.Success -> {
                    _state.update { currentState ->
                        val updatedRaw =
                            currentState.groupedTransactions.values.flatten().filter { it.id != id }
                        currentState.copy(
                            groupedTransactions = groupTransactions(updatedRaw),
                            balance = calculateBalance(updatedRaw),
                            successMessage = "İşlem başarıyla silindi."
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

    fun clearMessages() {
        _state.update { it.copy(error = "", successMessage = "") }
    }

    private fun groupTransactions(list: List<TransactionResponseDto>): Map<String, List<TransactionResponseDto>> {
        val formatter = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale("tr"))
        return list
            .sortedByDescending { it.id }
            .groupBy { tx ->
                try {
                    val datePart =
                        tx.date.substringBefore("T") // "2024-07-09T14:30:00" -> "2024-07-09"
                    val parsed = LocalDate.parse(datePart)
                    parsed.format(formatter)
                } catch (e: Exception) {
                    "Geçmiş İşlemler"
                }
            }
    }

    private fun calculateBalance(transactions: List<TransactionResponseDto>): Double {
        return transactions.sumOf {
            if (it.type == "INCOME") it.amount else -it.amount
        }
    }
}