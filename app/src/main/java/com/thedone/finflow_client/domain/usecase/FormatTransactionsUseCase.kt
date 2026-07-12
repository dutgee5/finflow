package com.thedone.finflow_client.domain.usecase

import com.thedone.finflow_client.domain.model.Transaction
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

class FormatTransactionsUseCase @Inject constructor() {
    operator fun invoke(transactions: List<Transaction>): Map<String, List<Transaction>> {
        val formatter = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale("tr"))

        return transactions
            .sortedByDescending { it.id }
            .groupBy { tx ->
                try {
                    val datePart = tx.date.substringBefore("T")
                    val parsed = LocalDate.parse(datePart)
                    parsed.format(formatter)
                } catch (e: Exception) {
                    "Geçmiş İşlemler"
                }
            }
    }
}