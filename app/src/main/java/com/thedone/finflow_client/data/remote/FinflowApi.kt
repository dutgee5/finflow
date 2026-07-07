package com.thedone.finflow_client.data.remote

import com.thedone.finflow_client.data.remote.dto.AuthRequestDto
import com.thedone.finflow_client.data.remote.dto.AuthResponseDto
import com.thedone.finflow_client.data.remote.dto.TransactionRequestDto
import com.thedone.finflow_client.data.remote.dto.TransactionResponseDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface FinflowApi {

    @POST("/register")
    suspend fun register(
        @Body request: AuthRequestDto,
    ): Response<String>

    @POST("/login")
    suspend fun login(
        @Body request: AuthRequestDto,
    ): Response<AuthResponseDto>

    @GET("/transactions")
    suspend fun getTransactions(): Response<List<TransactionResponseDto>>

    @POST("/transactions")
    suspend fun addTransaction(
        @Body request: TransactionRequestDto,
    ): Response<TransactionResponseDto>
}