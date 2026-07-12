package com.thedone.finflow_client.data.repo

import com.thedone.finflow_client.data.mapper.toTransaction
import com.thedone.finflow_client.data.remote.FinflowApi
import com.thedone.finflow_client.data.remote.dto.TransactionRequestDto
import com.thedone.finflow_client.data.remote.dto.TransactionResponseDto
import com.thedone.finflow_client.domain.model.Transaction
import com.thedone.finflow_client.domain.repo.TransactionRepository
import com.thedone.finflow_client.util.Resource
import java.io.IOException
import javax.inject.Inject

class TransactionRepositoryImpl @Inject constructor(
    private val api: FinflowApi,
) : TransactionRepository {
    override suspend fun getTransactions(): Resource<List<Transaction>> {
        return try {
            val response = api.getTransactions()
            if (response.isSuccessful && response.body() != null) {
                // map ile dönerek transaction listesine çevirme
                val domainTransactions = response.body()!!.map { it.toTransaction() }
                Resource.Success(domainTransactions)
            } else {
                Resource.Error("İşlemler alınamadı: ${response.message()}")
            }
        } catch (e: IOException) {
            Resource.Error("Sunucuya ulaşılamadı. İnterneti kontrol edin.")
        } catch (e: Exception) {
            Resource.Error("Bilinmeyen hata: ${e.localizedMessage}")
        }
    }

    override suspend fun addTransaction(
        type: String,
        amount: Double,
        description: String,
    ): Resource<Transaction> {
        return try {
            val request = TransactionRequestDto(type, amount, description)
            val response = api.addTransaction(request)

            if (response.isSuccessful && response.body() != null) {
                // ekleneni saf modele çevir
                Resource.Success(response.body()!!.toTransaction())
            } else {
                Resource.Error("İşlem eklenemedi: ${response.message()}")
            }
        } catch (e: IOException) {
            Resource.Error("Sunucuya ulaşılamadı. İnterneti kontrol edin.")
        } catch (e: Exception) {
            Resource.Error("Bilinmeyen hata: ${e.localizedMessage}")
        }
    }

    override suspend fun deleteTransaction(id: Int): Resource<Unit> {
        return try {
            val response = api.deleteTransaction(id)
            if (response.isSuccessful) {
                Resource.Success(Unit)
            } else {
                Resource.Error("İşlem silinemedi: ${response.message()}")
            }
        } catch (e: IOException) {
            Resource.Error("Sunucuya ulaşılamadı. İnterneti kontrol edin.")
        } catch (e: Exception) {
            Resource.Error("Bilinmeyen hata: ${e.localizedMessage}")
        }
    }
}