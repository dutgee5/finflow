package com.thedone.finflow_client.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thedone.finflow_client.data.local.TokenManager
import com.thedone.finflow_client.domain.model.Transaction
import com.thedone.finflow_client.domain.model.TransactionType
import com.thedone.finflow_client.domain.repo.TransactionRepository
import com.thedone.finflow_client.domain.usecase.CalculateFinancesUseCase
import com.thedone.finflow_client.domain.usecase.FormatTransactionsUseCase
import com.thedone.finflow_client.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: TransactionRepository,
    private val tokenManager: TokenManager,
    private val calculateFinancesUseCase: CalculateFinancesUseCase,
    private val formatTransactionsUseCase: FormatTransactionsUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(HomeUiState())
    val state = _state.asStateFlow()

    init {
        getTransactions()
    }

    private fun getTransactions() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = "") }

            when (val result = repository.getTransactions()) {
                is Resource.Success -> {
                    updateStateWithNewData(result.data ?: emptyList())
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

    fun addTransaction(type: TransactionType, amount: Double, description: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = "") }

            when (val result = repository.addTransaction(type.name, amount, description)) {
                is Resource.Success -> {
                    val newTransaction = result.data
                    if (newTransaction != null) {
                        val currentRawList = _state.value.groupedTransactions.values.flatten()
                        val updatedList = currentRawList + newTransaction
                        updateStateWithNewData(updatedList, "İşlem başarıyla eklendi.")
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
                    val updatedList =
                        _state.value.groupedTransactions.values.flatten().filter { it.id != id }
                    updateStateWithNewData(updatedList, "İşlem silindi.")
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

    private fun updateStateWithNewData(rawList: List<Transaction>, successMsg: String = "") {
        val finances = calculateFinancesUseCase(rawList)
        val grouped = formatTransactionsUseCase(rawList)

        _state.update {
            it.copy(
                isLoading = false,
                groupedTransactions = grouped,
                balance = finances.balance,
                totalIncome = finances.totalIncome,
                totalExpense = finances.totalExpense,
                successMessage = successMsg
            )
        }
    }
}