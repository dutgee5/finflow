package com.thedone.finflow_client.data.repo

import com.thedone.finflow_client.data.local.dao.TransactionDao
import com.thedone.finflow_client.data.mapper.toEntity
import com.thedone.finflow_client.data.mapper.toTransaction
import com.thedone.finflow_client.data.remote.FinflowApi
import com.thedone.finflow_client.data.remote.dto.TransactionRequestDto
import com.thedone.finflow_client.data.remote.dto.TransactionResponseDto
import com.thedone.finflow_client.domain.model.Transaction
import com.thedone.finflow_client.domain.repo.TransactionRepository
import com.thedone.finflow_client.util.Resource
import kotlinx.coroutines.flow.first
import java.io.IOException
import javax.inject.Inject

class TransactionRepositoryImpl @Inject constructor(
    private val api: FinflowApi,
    private val dao: TransactionDao,
) : TransactionRepository {
    override suspend fun getTransactions(): Resource<List<Transaction>> {
        val localData = dao.getAllTransactions().first().map { it.toTransaction() }
        return try {
            val response = api.getTransactions()
            if (response.isSuccessful && response.body() != null) {
                val remoteTransactions = response.body()!!
                dao.clearAll()
                dao.insertTransactions(remoteTransactions.map { it.toEntity() })
                val updatedLocalData = dao.getAllTransactions().first().map { it.toTransaction() }
                Resource.Success(updatedLocalData)
            } else {
                Resource.Error("Sunucu hatası, çevrimdışı veriler gösteriliyor.:", localData)
            }
        } catch (e: IOException) {
            if (localData.isNotEmpty()) {
                Resource.Success(localData)
            } else {
                Resource.Error("İnternet bağlantısı yok ve yerel veri bulunamadı.")
            }
        } catch (e: Exception) {
            Resource.Error("Bilinmeyen hata: ${e.localizedMessage}", localData)
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
                val remoteData = response.body()!!
                dao.insertTransaction(remoteData.toEntity())
                Resource.Success(remoteData.toTransaction())
            } else {
                Resource.Error("İşlem eklenemedi: ${response.message()}")
            }
        } catch (e: IOException) {
            Resource.Error("Sunucuya ulaşılamadı. İnterneti kontrol edin.")
        } catch (e: Exception) {
            Resource.Error("Bilinmeyen hata: ${e.localizedMessage}")
        }
    }

    override suspend fun updateTransaction(
        id: Int,
        type: String,
        amount: Double,
        description: String,
    ): Resource<Transaction> {
        return try {
            val request = TransactionRequestDto(type, amount, description)
            val response = api.updateTransaction(id, request)

            if (response.isSuccessful && response.body() != null) {
                val remoteData = response.body()!!
                dao.insertTransaction(remoteData.toEntity())
                Resource.Success(remoteData.toTransaction())
            } else {
                Resource.Error("İşlem güncellenemedi: ${response.message()}")
            }
        } catch (e: IOException) {
            Resource.Error("Sunucuya ulaşılamadı. İnterneti kontrol edin.")
        } catch (e: Exception) {
            Resource.Error("Bilinmeyen hata : ${e.localizedMessage}")
        }
    }

    override suspend fun deleteTransaction(id: Int): Resource<Unit> {
        return try {
            val response = api.deleteTransaction(id)
            if (response.isSuccessful) {
                dao.deleteTransactionById(id)
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