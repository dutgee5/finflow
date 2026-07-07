package com.thedone.finflow_client.di

import com.thedone.finflow_client.data.repo.AuthRepositoryImpl
import com.thedone.finflow_client.data.repo.TransactionRepositoryImpl
import com.thedone.finflow_client.domain.repo.AuthRepository
import com.thedone.finflow_client.domain.repo.TransactionRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl,
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindTransactionRepository(
        transactionRepositoryImpl: TransactionRepositoryImpl,
    ): TransactionRepository
}
