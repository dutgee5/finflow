package com.thedone.finflow_client.data.mapper

import com.thedone.finflow_client.data.remote.dto.TransactionResponseDto
import com.thedone.finflow_client.domain.model.Transaction
import com.thedone.finflow_client.domain.model.TransactionType

fun TransactionResponseDto.toTransaction(): Transaction {
    return Transaction(
        id = this.id,
        type = if (this.type == "INCOME") TransactionType.INCOME else TransactionType.EXPENSE,
        amount = this.amount,
        description = this.description,
        date = this.date
    )
}