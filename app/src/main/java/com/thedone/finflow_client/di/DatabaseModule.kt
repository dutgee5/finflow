package com.thedone.finflow_client.di

import android.content.Context
import androidx.room.Room
import com.thedone.finflow_client.data.local.FinFlowDatabase
import com.thedone.finflow_client.data.local.dao.TransactionDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideFinFlowDatabase(@ApplicationContext context: Context): FinFlowDatabase {
        return Room.databaseBuilder(
            context,
            FinFlowDatabase::class.java,
            "finflow_db"
        ).build()
    }

    @Provides
    @Singleton
    fun provideTransactionDao(database: FinFlowDatabase): TransactionDao {
        return database.transactionDao
    }
}