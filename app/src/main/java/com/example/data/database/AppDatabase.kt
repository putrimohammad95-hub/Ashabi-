package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.ProductDao
import com.example.data.dao.TransactionDao
import com.example.data.model.Product
import com.example.data.model.Transaction
import com.example.data.model.TransactionItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        Product::class,
        Transaction::class,
        TransactionItem::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun transactionDao(): TransactionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope = CoroutineScope(Dispatchers.IO)): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "ashabi_waroeng_db"
                )
                .addCallback(DatabaseCallback(scope))
                .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch {
                        populateInitialProducts(database.productDao())
                    }
                }
            }
        }

        suspend fun populateInitialProducts(productDao: ProductDao) {
            val sampleProducts = listOf(
                Product(
                    barcode = "89999990001",
                    name = "Beras Ramos Wangi 5kg",
                    category = "Sembako",
                    costPrice = 64000.0,
                    sellingPrice = 72000.0,
                    stock = 25,
                    minStock = 5,
                    unit = "karung"
                ),
                Product(
                    barcode = "89999990002",
                    name = "Minyak Goreng Bimoli 2L",
                    category = "Sembako",
                    costPrice = 33000.0,
                    sellingPrice = 37500.0,
                    stock = 30,
                    minStock = 6,
                    unit = "pouch"
                ),
                Product(
                    barcode = "89999990003",
                    name = "Gula Pasir Gulaku 1kg",
                    category = "Sembako",
                    costPrice = 15500.0,
                    sellingPrice = 18000.0,
                    stock = 40,
                    minStock = 8,
                    unit = "bks"
                ),
                Product(
                    barcode = "89999990004",
                    name = "Telur Ayam Negeri 1kg",
                    category = "Sembako",
                    costPrice = 25000.0,
                    sellingPrice = 28500.0,
                    stock = 18,
                    minStock = 5,
                    unit = "kg"
                ),
                Product(
                    barcode = "89999990005",
                    name = "Indomie Goreng Spesial",
                    category = "Mie & Makanan",
                    costPrice = 3000.0,
                    sellingPrice = 3500.0,
                    stock = 120,
                    minStock = 20,
                    unit = "bks"
                ),
                Product(
                    barcode = "89999990006",
                    name = "Indomie Ayam Bawang",
                    category = "Mie & Makanan",
                    costPrice = 3000.0,
                    sellingPrice = 3500.0,
                    stock = 95,
                    minStock = 20,
                    unit = "bks"
                ),
                Product(
                    barcode = "89999990007",
                    name = "Teh Pucuk Harum 350ml",
                    category = "Minuman",
                    costPrice = 3100.0,
                    sellingPrice = 4000.0,
                    stock = 48,
                    minStock = 12,
                    unit = "botol"
                ),
                Product(
                    barcode = "89999990008",
                    name = "Le Minerale 600ml",
                    category = "Minuman",
                    costPrice = 2500.0,
                    sellingPrice = 3500.0,
                    stock = 54,
                    minStock = 12,
                    unit = "botol"
                ),
                Product(
                    barcode = "89999990009",
                    name = "Kopi Kapal Api Mix 10s",
                    category = "Minuman",
                    costPrice = 12000.0,
                    sellingPrice = 15000.0,
                    stock = 30,
                    minStock = 6,
                    unit = "renceng"
                ),
                Product(
                    barcode = "89999990010",
                    name = "Ultra Milk Cokelat 250ml",
                    category = "Minuman",
                    costPrice = 5800.0,
                    sellingPrice = 7000.0,
                    stock = 24,
                    minStock = 6,
                    unit = "ktk"
                ),
                Product(
                    barcode = "89999990011",
                    name = "Chitato Sapi Panggang 68g",
                    category = "Camilan / Snack",
                    costPrice = 9800.0,
                    sellingPrice = 12000.0,
                    stock = 20,
                    minStock = 5,
                    unit = "bks"
                ),
                Product(
                    barcode = "89999990012",
                    name = "Biskuit Roma Kelapa 300g",
                    category = "Camilan / Snack",
                    costPrice = 8500.0,
                    sellingPrice = 10500.0,
                    stock = 15,
                    minStock = 4,
                    unit = "bks"
                ),
                Product(
                    barcode = "89999990013",
                    name = "Sunlight Jeruk Nipis 700ml",
                    category = "Kebutuhan Rumah",
                    costPrice = 13500.0,
                    sellingPrice = 16000.0,
                    stock = 16,
                    minStock = 4,
                    unit = "pouch"
                ),
                Product(
                    barcode = "89999990014",
                    name = "Rinso Anti Noda 770g",
                    category = "Kebutuhan Rumah",
                    costPrice = 19500.0,
                    sellingPrice = 23000.0,
                    stock = 12,
                    minStock = 3,
                    unit = "bks"
                ),
                Product(
                    barcode = "89999990015",
                    name = "Pasta Gigi Pepsodent 190g",
                    category = "Kebutuhan Rumah",
                    costPrice = 12000.0,
                    sellingPrice = 14500.0,
                    stock = 18,
                    minStock = 4,
                    unit = "tube"
                ),
                Product(
                    barcode = "89999990016",
                    name = "Sabun Lifebuoy Total 10",
                    category = "Kebutuhan Rumah",
                    costPrice = 3800.0,
                    sellingPrice = 4800.0,
                    stock = 4,
                    minStock = 5,
                    unit = "pcs"
                )
            )
            productDao.insertAll(sampleProducts)
        }
    }
}
