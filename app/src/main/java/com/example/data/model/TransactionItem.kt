package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entitas Detail Barang dalam Transaksi Penjualan Ashabi Waroeng
 */
@Entity(
    tableName = "transaction_items",
    foreignKeys = [
        ForeignKey(
            entity = Transaction::class,
            parentColumns = ["id"],
            childColumns = ["transactionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["transactionId"])]
)
data class TransactionItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val transactionId: Long,
    val productId: Long,
    val productName: String,
    val category: String = "",
    val price: Double,          // Harga jual saat transaksi
    val costPrice: Double,      // Harga modal saat transaksi (untuk kalkulasi laba)
    val quantity: Int,          // Kuantitas item
    val subtotal: Double        // price * quantity
)
