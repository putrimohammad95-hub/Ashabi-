package com.example.data.model

/**
 * Model item keranjang belanja kasir sementara (in-memory)
 */
data class CartItem(
    val product: Product,
    val quantity: Int = 1
) {
    val subtotal: Double
        get() = product.sellingPrice * quantity

    val totalProfit: Double
        get() = (product.sellingPrice - product.costPrice) * quantity
}
