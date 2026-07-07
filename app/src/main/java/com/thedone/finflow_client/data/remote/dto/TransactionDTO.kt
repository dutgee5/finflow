package com.thedone.finflow_client.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class TransactionRequestDto(
    val type: String,
    val amount: Double,
    val description: String,
)

@Serializable
data class TransactionResponseDto(
    val id: Int,
    val type: String,
    val amount: Double,
    val description: String,
    val date: String,
)