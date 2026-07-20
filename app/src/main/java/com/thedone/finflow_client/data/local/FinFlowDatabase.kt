package com.thedone.finflow_client.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.thedone.finflow_client.data.local.dao.TransactionDao
import com.thedone.finflow_client.data.local.entity.TransactionEntity

@Database(
    entities = [TransactionEntity::class],
    version = 1,
    exportSchema = false
)
abstract class FinFlowDatabase : RoomDatabase() {
    abstract val transactionDao: TransactionDao
}