package com.thedone.finflow_client.domain.repo

import com.thedone.finflow_client.util.Resource

interface AuthRepository {
    suspend fun register(email: String, password: String): Resource<String>

    suspend fun login(email: String, password: String): Resource<Unit>

}