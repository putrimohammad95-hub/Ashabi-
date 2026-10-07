package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.CartItem
import com.example.data.model.Product
import com.example.data.model.TransactionWithItems
import com.example.data.repository.PosRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PosViewModel(private val repository: PosRepository) : ViewModel() {

    init {
        viewModelScope.launch {
            repository.seedInitialDataIfNeeded()
        }
    }

    // Filter & Pencarian
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("Semua")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    // Produk
    val allProducts: StateFlow<List<Product>> = repository.allProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val lowStockProducts: StateFlow<List<Product>> = repository.lowStockProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Produk yang terfilter berdasarkan pencarian dan kategori
    val filteredProducts: StateFlow<List<Product>> = combine(
        allProducts,
        searchQuery,
        selectedCategory
    ) { list, query, category ->
        list.filter { product ->
            val matchQuery = query.isBlank() ||
                product.name.contains(query, ignoreCase = true) ||
                product.barcode.contains(query, ignoreCase = true)
            val matchCategory = category == "Semua" || product.category.equals(category, ignoreCase = true)
            matchQuery && matchCategory
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Keranjang Belanja POS
    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()

    // Diskon
    private val _discountAmount = MutableStateFlow(0.0)
    val discountAmount: StateFlow<Double> = _discountAmount.asStateFlow()

    // Input Pembayaran
    private val _paidAmountInput = MutableStateFlow("")
    val paidAmountInput: StateFlow<String> = _paidAmountInput.asStateFlow()

    private val _paymentMethod = MutableStateFlow("Tunai")
    val paymentMethod: StateFlow<String> = _paymentMethod.asStateFlow()

    // Dialog & Feedback
    private val _showPaymentDialog = MutableStateFlow(false)
    val showPaymentDialog: StateFlow<Boolean> = _showPaymentDialog.asStateFlow()

    private val _activeReceiptTransaction = MutableStateFlow<TransactionWithItems?>(null)
    val activeReceiptTransaction: StateFlow<TransactionWithItems?> = _activeReceiptTransaction.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    // Laporan & Riwayat
    val transactions: StateFlow<List<TransactionWithItems>> = repository.allTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalRevenue: StateFlow<Double?> = repository.totalRevenue
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalProfit: StateFlow<Double?> = repository.totalProfit
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val transactionCount: StateFlow<Int> = repository.transactionCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Perhitungan Keranjang
    val cartSubtotal: StateFlow<Double> = combine(_cartItems) { items ->
        items[0].sumOf { it.subtotal }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val cartTotal: StateFlow<Double> = combine(cartSubtotal, _discountAmount) { subtotal, discount ->
        (subtotal - discount).coerceAtLeast(0.0)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalItemsCount: StateFlow<Int> = combine(_cartItems) { items ->
        items[0].sumOf { it.quantity }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Logika Cart
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedCategory(category: String) {
        _selectedCategory.value = category
    }

    fun addToCart(product: Product) {
        if (product.stock <= 0) {
            _errorMessage.value = "Stok ${product.name} telah habis!"
            return
        }

        val currentList = _cartItems.value.toMutableList()
        val existingIndex = currentList.indexOfFirst { it.product.id == product.id }

        if (existingIndex >= 0) {
            val currentItem = currentList[existingIndex]
            if (currentItem.quantity >= product.stock) {
                _errorMessage.value = "Stok tersedia hanya ${product.stock} ${product.unit}"
                return
            }
            currentList[existingIndex] = currentItem.copy(quantity = currentItem.quantity + 1)
        } else {
            currentList.add(CartItem(product = product, quantity = 1))
        }

        _cartItems.value = currentList
        _errorMessage.value = null
    }

    fun decreaseCartItem(product: Product) {
        val currentList = _cartItems.value.toMutableList()
        val existingIndex = currentList.indexOfFirst { it.product.id == product.id }
        if (existingIndex >= 0) {
            val currentItem = currentList[existingIndex]
            if (currentItem.quantity > 1) {
                currentList[existingIndex] = currentItem.copy(quantity = currentItem.quantity - 1)
            } else {
                currentList.removeAt(existingIndex)
            }
            _cartItems.value = currentList
        }
    }

    fun removeFromCart(product: Product) {
        val currentList = _cartItems.value.toMutableList()
        currentList.removeAll { it.product.id == product.id }
        _cartItems.value = currentList
    }

    fun clearCart() {
        _cartItems.value = emptyList()
        _discountAmount.value = 0.0
        _paidAmountInput.value = ""
        _errorMessage.value = null
    }

    fun setDiscount(amount: Double) {
        _discountAmount.value = amount.coerceAtLeast(0.0)
    }

    fun setPaidAmount(input: String) {
        _paidAmountInput.value = input.filter { it.isDigit() }
    }

    fun setQuickCash(amount: Double) {
        _paidAmountInput.value = amount.toLong().toString()
    }

    fun setPaymentMethod(method: String) {
        _paymentMethod.value = method
        if (method == "QRIS" || method == "Transfer") {
            // QRIS / Transfer otomatis uang pas
            _paidAmountInput.value = cartTotal.value.toLong().toString()
        }
    }

    fun openPaymentDialog() {
        if (_cartItems.value.isEmpty()) {
            _errorMessage.value = "Keranjang masih kosong"
            return
        }
        _paidAmountInput.value = ""
        _paymentMethod.value = "Tunai"
        _showPaymentDialog.value = true
    }

    fun closePaymentDialog() {
        _showPaymentDialog.value = false
    }

    fun simulateBarcodeScan(barcode: String) {
        viewModelScope.launch {
            val product = repository.getProductByBarcode(barcode)
            if (product != null) {
                addToCart(product)
                _successMessage.value = "Berhasil scan: ${product.name}"
            } else {
                _errorMessage.value = "Produk barcode $barcode tidak ditemukan"
            }
        }
    }

    fun processPayment() {
        viewModelScope.launch {
            val paid = _paidAmountInput.value.toDoubleOrNull() ?: 0.0
            val total = cartTotal.value

            if (paid < total) {
                _errorMessage.value = "Nominal pembayaran kurang dari total belanja"
                return@launch
            }

            val result = repository.processCheckout(
                cartItems = _cartItems.value,
                discountAmount = _discountAmount.value,
                paidAmount = paid,
                paymentMethod = _paymentMethod.value,
                cashierName = "Kasir Ashabi Waroeng"
            )

            result.onSuccess { txWithItems ->
                _showPaymentDialog.value = false
                _activeReceiptTransaction.value = txWithItems
                clearCart()
                _successMessage.value = "Transaksi ${txWithItems.transaction.invoiceNumber} berhasil!"
            }.onFailure { err ->
                _errorMessage.value = err.message ?: "Gagal memproses transaksi"
            }
        }
    }

    fun dismissReceipt() {
        _activeReceiptTransaction.value = null
    }

    fun viewReceipt(transaction: TransactionWithItems) {
        _activeReceiptTransaction.value = transaction
    }

    fun clearFeedbackMessages() {
        _errorMessage.value = null
        _successMessage.value = null
    }

    // CRUD Produk
    fun saveProduct(product: Product, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            if (product.id == 0L) {
                repository.insertProduct(product)
                _successMessage.value = "Produk ${product.name} berhasil ditambahkan"
            } else {
                repository.updateProduct(product)
                _successMessage.value = "Produk ${product.name} berhasil diperbarui"
            }
            onComplete()
        }
    }

    fun deleteProduct(product: Product) {
        viewModelScope.launch {
            repository.deleteProduct(product)
            _successMessage.value = "Produk ${product.name} berhasil dihapus"
        }
    }

    fun quickAddStock(productId: Long, qty: Int) {
        viewModelScope.launch {
            repository.addStock(productId, qty)
            _successMessage.value = "Stok berhasil ditambah (+$qty)"
        }
    }
}
