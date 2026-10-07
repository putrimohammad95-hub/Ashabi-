package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entitas Transaksi Kasir Ashabi Waroeng
 * Menyimpan data ringkasan transaksi penjualan, nota faktur, pembayaran, dan laba.
 */
@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val invoiceNumber: String,      // Nomor faktur nota unik, contoh: AW-20261006-0001
    val timestamp: Long = System.currentTimeMillis(),
    val totalAmount: Double,        // Total tagihan akhir yang harus dibayar
    val subtotalAmount: Double,     // Subtotal sebelum diskon
    val discountAmount: Double = 0.0, // Diskon belanja
    val paidAmount: Double,         // Uang tunai/nominal yang dibayarkan konsumen
    val changeAmount: Double,       // Kembalian yang diberikan
    val paymentMethod: String = "Tunai", // Tunai, QRIS, Transfer
    val cashierName: String = "Kasir Ashabi Waroeng",
    val totalProfit: Double = 0.0   // Total estimasi keuntungan (SellingPrice - CostPrice)
)
