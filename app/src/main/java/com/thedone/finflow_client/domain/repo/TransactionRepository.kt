package com.thedone.finflow_client.domain.repo

import com.thedone.finflow_client.data.remote.dto.TransactionResponseDto
import com.thedone.finflow_client.util.Resource


interface TransactionRepository {
    suspend fun getTransactions(): Resource<List<TransactionResponseDto>>
    suspend fun addTransaction(type: String, amount: Double, description: String): Resource<Unit>

}
