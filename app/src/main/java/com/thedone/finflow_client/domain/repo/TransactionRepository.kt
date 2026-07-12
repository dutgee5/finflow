package com.thedone.finflow_client.domain.repo

import com.thedone.finflow_client.domain.model.Transaction
import com.thedone.finflow_client.util.Resource


interface TransactionRepository {
    suspend fun getTransactions(): Resource<List<Transaction>>
    suspend fun addTransaction(
        type: String,
        amount: Double,
        description: String,
    ): Resource<Transaction>

    suspend fun deleteTransaction(id: Int): Resource<Unit>

}
