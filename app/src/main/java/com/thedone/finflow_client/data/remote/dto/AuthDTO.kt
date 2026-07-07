package com.thedone.finflow_client.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class AuthRequestDto(
    val email: String,
    val password: String,
)

@Serializable
data class AuthResponseDto(
    val token: String,
    val message: String,
)