package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entitas Produk untuk Ashabi Waroeng
 * Menyimpan data barang dagangan, barcode, stok, harga modal, dan harga jual.
 */
@Entity(tableName = "products")
data class Product(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val barcode: String = "",
    val name: String,
    val category: String,
    val costPrice: Double,     // Harga Modal / Kulak
    val sellingPrice: Double,  // Harga Jual ke Konsumen
    val stock: Int,            // Jumlah Stok Tersedia
    val minStock: Int = 5,     // Batas peringatan stok menipis
    val unit: String = "pcs",  // Satuan (pcs, bks, botol, kg, renceng)
    val updatedAt: Long = System.currentTimeMillis()
) {
    val isLowStock: Boolean
        get() = stock <= minStock

    val isOutOfStock: Boolean
        get() = stock <= 0
}
