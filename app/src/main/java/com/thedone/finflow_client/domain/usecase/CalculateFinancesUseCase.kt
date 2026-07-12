package com.thedone.finflow_client.domain.usecase

import com.thedone.finflow_client.data.remote.dto.TransactionResponseDto
import com.thedone.finflow_client.domain.model.FinanceSummary
import com.thedone.finflow_client.domain.model.Transaction
import com.thedone.finflow_client.domain.model.TransactionType
import javax.inject.Inject


class CalculateFinancesUseCase @Inject constructor() {
    operator fun invoke(transactions: List<Transaction>): FinanceSummary {
        val income = transactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
        val expense = transactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }

        return FinanceSummary(
            balance = income - expense,
            totalIncome = income,
            totalExpense = expense
        )
    }
}