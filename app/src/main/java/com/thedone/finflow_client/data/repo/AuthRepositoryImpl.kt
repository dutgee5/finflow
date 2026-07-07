package com.thedone.finflow_client.data.repo

import com.thedone.finflow_client.data.local.TokenManager
import com.thedone.finflow_client.data.remote.FinflowApi
import com.thedone.finflow_client.data.remote.dto.AuthRequestDto
import com.thedone.finflow_client.domain.repo.AuthRepository
import com.thedone.finflow_client.util.Resource
import java.io.IOException
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val api: FinflowApi,
    private val tokenManager: TokenManager,
) : AuthRepository {
    override suspend fun register(
        email: String,
        password: String,
    ): Resource<String> {
        return try {
            val request = AuthRequestDto(email, password)
            val response = api.register(request)

            if (response.isSuccessful) {
                Resource.Success(response.body() ?: "Registration successful.")
            } else {
                Resource.Error("Registration failed: ${response.message()}")
            }
        } catch (e: IOException) {
            Resource.Error("The server could not be reached. Please check your internet connection.")
        } catch (e: Exception) {
            Resource.Error("An unknown error occurred.: ${e.localizedMessage}")
        }
    }

    override suspend fun login(
        email: String,
        password: String,
    ): Resource<Unit> {
        return try {
            val request = AuthRequestDto(email, password)
            val response = api.login(request)

            if (response.isSuccessful && response.body() != null) {
                val token = response.body()!!.token
                tokenManager.saveToken(token)

                Resource.Success(Unit)
            } else {
                Resource.Error("Email or password is incorrect!")
            }
        } catch (e: IOException) {
            Resource.Error("The server could not be reached. Please check your internet connection.")
        } catch (e: Exception) {
            Resource.Error("An unknown error occurred: ${e.localizedMessage}")
        }
    }
}