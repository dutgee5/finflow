package com.thedone.finflow_client.domain.model


enum class TransactionType {
    INCOME, EXPENSE
}

data class Transaction(
    val id: Int,
    val type: TransactionType,
    val amount: Double,
    val description: String,
    val date: String,
)

