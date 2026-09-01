package com.sumberrejeki.pos.utils.backup

import android.content.Context
import com.sumberrejeki.pos.data.local.AppDatabase
import com.sumberrejeki.pos.data.local.entity.OrderItemEntity
import com.sumberrejeki.pos.data.local.entity.ProductEntity
import com.sumberrejeki.pos.data.local.entity.UserEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

class DatabaseBackupManager(
    private val context: Context,
    private val db: AppDatabase
) {

    suspend fun exportToJson(): String = withContext(Dispatchers.IO) {
        val root = JSONObject()

        // 1. Export Products
        val products = db.productDao().getAllProductsFlow()
        val allProducts = db.openHelper.readableDatabase.query("SELECT * FROM products")
        val prodArray = JSONArray()
        while (allProducts.moveToNext()) {
            val p = JSONObject().apply {
                put("productId", allProducts.getLong(allProducts.getColumnIndexOrThrow("productId")))
                put("barcode", allProducts.getString(allProducts.getColumnIndexOrThrow("barcode")))
                put("name", allProducts.getString(allProducts.getColumnIndexOrThrow("name")))
                put("category", allProducts.getString(allProducts.getColumnIndexOrThrow("category")))
                put("sellPrice", allProducts.getDouble(allProducts.getColumnIndexOrThrow("sellPrice")))
                put("costPrice", allProducts.getDouble(allProducts.getColumnIndexOrThrow("costPrice")))
                put("stock", allProducts.getInt(allProducts.getColumnIndexOrThrow("stock")))
            }
            prodArray.put(p)
        }
        allProducts.close()
        root.put("products", prodArray)

        // 2. Export Users
        val users = db.userDao().getAllUsers()
        val userArray = JSONArray()
        for (u in users) {
            val uObj = JSONObject().apply {
                put("userId", u.userId)
                put("username", u.username)
                put("pinHash", u.pinHash)
                put("role", u.role)
                put("createdAt", u.createdAt)
            }
            userArray.put(uObj)
        }
        root.put("users", userArray)

        root.put("backupDate", System.currentTimeMillis())
        root.put("version", 1)

        root.toString(2)
    }

    suspend fun importFromJson(jsonString: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val root = JSONObject(jsonString)

            if (root.has("products")) {
                val prodArray = root.getJSONArray("products")
                val productList = mutableListOf<ProductEntity>()
                for (i in 0 until prodArray.length()) {
                    val p = prodArray.getJSONObject(i)
                    productList.add(
                        ProductEntity(
                            productId = p.optLong("productId", 0L),
                            barcode = p.getString("barcode"),
                            name = p.getString("name"),
                            category = p.getString("category"),
                            sellPrice = p.getDouble("sellPrice"),
                            costPrice = p.getDouble("costPrice"),
                            stock = p.getInt("stock")
                        )
                    )
                }
                db.productDao().insertProducts(productList)
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
