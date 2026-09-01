package com.sumberrejeki.pos.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.sumberrejeki.pos.data.local.dao.CartDraftDao
import com.sumberrejeki.pos.data.local.dao.OrderDao
import com.sumberrejeki.pos.data.local.dao.ProductDao
import com.sumberrejeki.pos.data.local.dao.UserDao
import com.sumberrejeki.pos.data.local.entity.CartDraftEntity
import com.sumberrejeki.pos.data.local.entity.OrderEntity
import com.sumberrejeki.pos.data.local.entity.OrderItemEntity
import com.sumberrejeki.pos.data.local.entity.ProductEntity
import com.sumberrejeki.pos.data.local.entity.UserEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        UserEntity::class,
        ProductEntity::class,
        CartDraftEntity::class,
        OrderEntity::class,
        OrderItemEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun productDao(): ProductDao
    abstract fun cartDraftDao(): CartDraftDao
    abstract fun orderDao(): OrderDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "sumber_rejeki_pos.db"
                )
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // Pre-populate sample store products
                            CoroutineScope(Dispatchers.IO).launch {
                                INSTANCE?.let { database ->
                                    populateInitialData(database)
                                }
                            }
                        }
                    })
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private suspend fun populateInitialData(db: AppDatabase) {
            val sampleProducts = listOf(
                ProductEntity(
                    barcode = "8992753112015",
                    name = "Minyak Goreng Sania 2L",
                    category = "Sembako",
                    sellPrice = 36000.0,
                    costPrice = 31500.0,
                    stock = 50
                ),
                ProductEntity(
                    barcode = "8992388111019",
                    name = "Beras Premium Ramos 5kg",
                    category = "Sembako",
                    sellPrice = 72000.0,
                    costPrice = 64000.0,
                    stock = 30
                ),
                ProductEntity(
                    barcode = "8999909001017",
                    name = "Gula Pasir Gulaku 1kg",
                    category = "Sembako",
                    sellPrice = 17500.0,
                    costPrice = 15000.0,
                    stock = 45
                ),
                ProductEntity(
                    barcode = "8992775210010",
                    name = "Kopi Kapal Api Spesial 165g",
                    category = "Minuman",
                    sellPrice = 14000.0,
                    costPrice = 11500.0,
                    stock = 60
                ),
                ProductEntity(
                    barcode = "8998866200227",
                    name = "Indomie Goreng Original 85g",
                    category = "Makanan Instan",
                    sellPrice = 3500.0,
                    costPrice = 2900.0,
                    stock = 120
                ),
                ProductEntity(
                    barcode = "8998866200234",
                    name = "Indomie Kuah Ayam Bawang 75g",
                    category = "Makanan Instan",
                    sellPrice = 3300.0,
                    costPrice = 2750.0,
                    stock = 100
                ),
                ProductEntity(
                    barcode = "8992775001014",
                    name = "Teh Botol Sosro Kotak 250ml",
                    category = "Minuman",
                    sellPrice = 4000.0,
                    costPrice = 3200.0,
                    stock = 80
                ),
                ProductEntity(
                    barcode = "8999999052012",
                    name = "Susu UHT Ultra Milk Cokelat 200ml",
                    category = "Minuman",
                    sellPrice = 6000.0,
                    costPrice = 4900.0,
                    stock = 75
                ),
                ProductEntity(
                    barcode = "8992741982026",
                    name = "Sabun Mandi Lifebuoy Total 10 110g",
                    category = "Perawatan Tubuh",
                    sellPrice = 5500.0,
                    costPrice = 4200.0,
                    stock = 40
                ),
                ProductEntity(
                    barcode = "8992745300116",
                    name = "Pasta Gigi Pepsodent White 120g",
                    category = "Perawatan Tubuh",
                    sellPrice = 8500.0,
                    costPrice = 6800.0,
                    stock = 35
                ),
                ProductEntity(
                    barcode = "8991002101340",
                    name = "Telur Ayam Negeri 1kg",
                    category = "Sembako",
                    sellPrice = 28000.0,
                    costPrice = 24500.0,
                    stock = 25
                ),
                ProductEntity(
                    barcode = "8992388001013",
                    name = "Tepung Terigu Segitiga Biru 1kg",
                    category = "Sembako",
                    sellPrice = 13500.0,
                    costPrice = 11200.0,
                    stock = 40
                )
            )
            db.productDao().insertProducts(sampleProducts)
        }
    }
}
