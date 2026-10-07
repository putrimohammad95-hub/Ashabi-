package com.example.data.model

import androidx.room.Embedded
import androidx.room.Relation

/**
 * Hubungan Relasi 1-ke-Banyak antara Transaksi dan Detail Item
 */
data class TransactionWithItems(
    @Embedded
    val transaction: Transaction,

    @Relation(
        parentColumn = "id",
        entityColumn = "transactionId"
    )
    val items: List<TransactionItem>
)
