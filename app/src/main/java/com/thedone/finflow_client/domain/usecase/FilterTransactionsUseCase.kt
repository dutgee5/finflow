package com.thedone.finflow_client.domain.usecase

import com.thedone.finflow_client.domain.model.Transaction
import com.thedone.finflow_client.domain.model.TransactionType
import javax.inject.Inject

class FilterTransactionsUseCase @Inject constructor() {
    operator fun invoke(
        transactions: List<Transaction>,
        searchQuery: String,
        filterType: String,
    ): List<Transaction> {
        return transactions.filter { tx ->
            // büyük-küçük
            val matchesSearch = tx.description.contains(searchQuery, ignoreCase = true)

            //tip
            val matchesType = when (filterType) {
                "INCOME" -> tx.type == TransactionType.INCOME
                "EXPENSE" -> tx.type == TransactionType.EXPENSE
                else -> true
            }
            matchesSearch && matchesType
        }
    }
}