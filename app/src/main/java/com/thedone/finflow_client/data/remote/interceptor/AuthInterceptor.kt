package com.thedone.finflow_client.data.remote.interceptor

import com.thedone.finflow_client.data.local.TokenManager
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class AuthInterceptor @Inject constructor(
    private val tokenManager: TokenManager,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val requestBuilder = chain.request().newBuilder()

        // datastore asenkron olduğu için OkHttp'nin senkron yapısına uyarla
        val token = runBlocking {
            tokenManager.getToken().firstOrNull()
        }

        // Eğer token varsa header'a ekle
        token?.let {
            requestBuilder.addHeader("Authorization", "Bearer $it")
        }

        return chain.proceed(requestBuilder.build())
    }
}