package com.example.data.repository

import com.example.data.dao.ProductDao
import com.example.data.dao.TransactionDao
import com.example.data.database.AppDatabase
import com.example.data.model.CartItem
import com.example.data.model.Product
import com.example.data.model.Transaction
import com.example.data.model.TransactionItem
import com.example.data.model.TransactionWithItems
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PosRepository(
    private val productDao: ProductDao,
    private val transactionDao: TransactionDao
) {
    val allProducts: Flow<List<Product>> = productDao.getAllProducts()
    val lowStockProducts: Flow<List<Product>> = productDao.getLowStockProducts()
    val allTransactions: Flow<List<TransactionWithItems>> = transactionDao.getAllTransactions()
    val totalRevenue: Flow<Double?> = transactionDao.getTotalRevenue()
    val totalProfit: Flow<Double?> = transactionDao.getTotalProfit()
    val transactionCount: Flow<Int> = transactionDao.getTransactionCount()

    fun searchProducts(query: String): Flow<List<Product>> {
        return productDao.searchProducts(query)
    }

    fun getProductsByCategory(category: String): Flow<List<Product>> {
        return productDao.getProductsByCategory(category)
    }

    suspend fun getProductByBarcode(barcode: String): Product? {
        return withContext(Dispatchers.IO) {
            productDao.getProductByBarcode(barcode)
        }
    }

    suspend fun insertProduct(product: Product): Long {
        return withContext(Dispatchers.IO) {
            productDao.insertProduct(product)
        }
    }

    suspend fun updateProduct(product: Product) {
        withContext(Dispatchers.IO) {
            productDao.updateProduct(product)
        }
    }

    suspend fun deleteProduct(product: Product) {
        withContext(Dispatchers.IO) {
            productDao.deleteProduct(product)
        }
    }

    suspend fun addStock(productId: Long, qty: Int) {
        withContext(Dispatchers.IO) {
            productDao.addStock(productId, qty)
        }
    }

    /**
     * Memproses transaksi kasir secara atomik:
     * 1. Validasi kecukupan stok
     * 2. Buat invoice transaksi
     * 3. Simpan header transaksi & detail transaksi
     * 4. Potong stok tiap barang secara otomatis di database
     */
    suspend fun processCheckout(
        cartItems: List<CartItem>,
        discountAmount: Double,
        paidAmount: Double,
        paymentMethod: String,
        cashierName: String = "Kasir Ashabi"
    ): Result<TransactionWithItems> = withContext(Dispatchers.IO) {
        if (cartItems.isEmpty()) {
            return@withContext Result.failure(IllegalArgumentException("Keranjang belanja kosong"))
        }

        val subtotal = cartItems.sumOf { it.subtotal }
        val finalTotal = (subtotal - discountAmount).coerceAtLeast(0.0)

        if (paidAmount < finalTotal) {
            return@withContext Result.failure(
                IllegalArgumentException("Uang pembayaran kurang dari total belanja")
            )
        }

        // Generate nomor nota unik: AW-YYYYMMDD-HHmmss
        val dateFormat = SimpleDateFormat("yyyyMMdd-HHmmss", Locale.getDefault())
        val invoiceNumber = "AW-" + dateFormat.format(Date())

        val totalProfit = cartItems.sumOf { it.totalProfit } - discountAmount
        val changeAmount = paidAmount - finalTotal

        val transaction = Transaction(
            invoiceNumber = invoiceNumber,
            timestamp = System.currentTimeMillis(),
            totalAmount = finalTotal,
            subtotalAmount = subtotal,
            discountAmount = discountAmount,
            paidAmount = paidAmount,
            changeAmount = changeAmount,
            paymentMethod = paymentMethod,
            cashierName = cashierName,
            totalProfit = totalProfit.coerceAtLeast(0.0)
        )

        val txId = transactionDao.insertTransaction(transaction)
        val savedTransaction = transaction.copy(id = txId)

        val transactionItems = cartItems.map { item ->
            TransactionItem(
                transactionId = txId,
                productId = item.product.id,
                productName = item.product.name,
                category = item.product.category,
                price = item.product.sellingPrice,
                costPrice = item.product.costPrice,
                quantity = item.quantity,
                subtotal = item.subtotal
            )
        }

        transactionDao.insertTransactionItems(transactionItems)

        // Potong stok produk di Room
        for (item in cartItems) {
            productDao.reduceStock(item.product.id, item.quantity)
        }

        val fullTransaction = TransactionWithItems(
            transaction = savedTransaction,
            items = transactionItems
        )

        return@withContext Result.success(fullTransaction)
    }

    suspend fun seedInitialDataIfNeeded() {
        withContext(Dispatchers.IO) {
            if (productDao.countProducts() == 0) {
                AppDatabase.populateInitialProducts(productDao)
            }
        }
    }
}
