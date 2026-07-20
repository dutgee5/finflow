package com.thedone.finflow_client.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.thedone.finflow_client.data.local.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow


@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY date DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    // Backend'den gelen listeyi tek seferde içeri atar (Var olanları ezer/günceller)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactions(transactions: List<TransactionEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteTransactionById(id: Int)

    // Kullanıcı çıkış yaptığında veritabanını temizlemek için
    @Query("DELETE FROM transactions")
    suspend fun clearAll()
}