package com.thedone.finflow_client.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.thedone.finflow_client.domain.model.Transaction
import com.thedone.finflow_client.domain.model.TransactionType

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = false)
    val id: Int,
    val type: TransactionType,
    val amount: Double,
    val description: String,
    val date: String,
) {
    // // Data katmanından Domain katmanına geçerken kullanacağımız Mapper fonksiyonu
    fun toTransaction(): Transaction {
        return Transaction(
            id = id,
            type = type,
            amount = amount,
            description = description,
            date = date
        )
    }
}
