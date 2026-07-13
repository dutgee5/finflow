package com.thedone.finflow_client.ui.home

import com.thedone.finflow_client.domain.model.Transaction

data class HomeUiState(
    val isLoading: Boolean = false,
    val groupedTransactions: Map<String, List<Transaction>> = emptyMap(),
    val balance: Double = 0.0,
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val error: String = "",
    val successMessage: String = "",

    val searchQuery: String = "",
    val selectedFilter: String = "ALL",
)