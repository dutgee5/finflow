package com.thedone.finflow_client.domain.usecase

import com.thedone.finflow_client.domain.model.Transaction
import com.thedone.finflow_client.domain.model.TransactionType
import javax.inject.Inject

class ExportTransactionsUseCase @Inject constructor() {
    operator fun invoke(transactions: List<Transaction>): String {
        val builder = java.lang.StringBuilder()
        // türkçe karakteri
        builder.append('\uFEFF')

        builder.append("Tarih,Açıklama,Tip,Tutar\n")

        transactions.forEach { transaction ->
            val typeStr = if (transaction.type == TransactionType.INCOME) "Gelir" else "Gider"

            // gereksiz virgülü önlemek için
            val safeDescription = "\"${transaction.description.replace("\"", "\"\"")}\""

            builder.append("${transaction.date},${safeDescription},${typeStr},${transaction.amount}\n")
        }
        return builder.toString()
    }
}