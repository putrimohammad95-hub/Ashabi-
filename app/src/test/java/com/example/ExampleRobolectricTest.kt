package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.database.AppDatabase
import com.example.data.model.CartItem
import com.example.data.model.Product
import com.example.data.repository.PosRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    private lateinit var context: Context
    private lateinit var database: AppDatabase
    private lateinit var repository: PosRepository

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        database = AppDatabase.getDatabase(context)
        repository = PosRepository(database.productDao(), database.transactionDao())
    }

    @Test
    fun `read app_name from context matches Ashabi Waroeng`() {
        val appName = context.getString(R.string.app_name)
        assertEquals("Ashabi Waroeng", appName)
    }

    @Test
    fun `test checkout reduces stock and creates transaction`() = runBlocking {
        // Insert sample product
        val testProduct = Product(
            name = "Kopi Uji Coba",
            category = "Minuman",
            costPrice = 2000.0,
            sellingPrice = 3000.0,
            stock = 10,
            unit = "pcs"
        )
        val productId = repository.insertProduct(testProduct)
        val product = testProduct.copy(id = productId)

        val cartItems = listOf(CartItem(product = product, quantity = 3))
        val result = repository.processCheckout(
            cartItems = cartItems,
            discountAmount = 0.0,
            paidAmount = 10000.0,
            paymentMethod = "Tunai",
            cashierName = "Kasir Ashabi"
        )

        assertTrue(result.isSuccess)
        val tx = result.getOrNull()
        assertNotNull(tx)
        assertEquals(9000.0, tx!!.transaction.totalAmount, 0.01)
        assertEquals(1000.0, tx.transaction.changeAmount, 0.01)
        assertTrue(tx.transaction.invoiceNumber.startsWith("AW-"))

        // Check updated stock
        val products = repository.allProducts.first()
        val updatedProduct = products.find { it.id == productId }
        assertNotNull(updatedProduct)
        assertEquals(7, updatedProduct!!.stock) // 10 - 3 = 7
    }
}
