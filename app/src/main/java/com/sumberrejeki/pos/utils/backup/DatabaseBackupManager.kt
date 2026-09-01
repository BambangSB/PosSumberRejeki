package com.sumberrejeki.pos.utils.backup

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import androidx.core.content.FileProvider
import androidx.room.withTransaction
import com.sumberrejeki.pos.data.local.AppDatabase
import com.sumberrejeki.pos.data.local.entity.OrderEntity
import com.sumberrejeki.pos.data.local.entity.OrderItemEntity
import com.sumberrejeki.pos.data.local.entity.ProductEntity
import com.sumberrejeki.pos.data.local.entity.UserEntity
import com.sumberrejeki.pos.domain.model.PaymentMethod
import com.sumberrejeki.pos.domain.model.UserRole
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.File
import java.io.FileOutputStream
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class BackupExportResult(
    val file: File,
    val fileName: String,
    val filePath: String,
    val productCount: Int,
    val userCount: Int,
    val orderCount: Int,
    val jsonString: String
)

data class BackupPreview(
    val isValid: Boolean,
    val errorMessage: String? = null,
    val productCount: Int = 0,
    val userCount: Int = 0,
    val orderCount: Int = 0,
    val backupDateFormatted: String = "",
    val jsonContent: String = ""
)

class DatabaseBackupManager(
    private val context: Context,
    private val db: AppDatabase
) {

    private val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm:ss", Locale("id", "ID"))
    private val fileTimestampFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)

    suspend fun exportDatabase(): BackupExportResult = withContext(Dispatchers.IO) {
        val root = JSONObject()

        // 1. Export Products
        val allProductsQuery = db.openHelper.readableDatabase.query("SELECT * FROM products ORDER BY productId ASC")
        val prodArray = JSONArray()
        var prodCount = 0
        while (allProductsQuery.moveToNext()) {
            val p = JSONObject().apply {
                put("productId", allProductsQuery.getLong(allProductsQuery.getColumnIndexOrThrow("productId")))
                put("barcode", allProductsQuery.getString(allProductsQuery.getColumnIndexOrThrow("barcode")))
                put("name", allProductsQuery.getString(allProductsQuery.getColumnIndexOrThrow("name")))
                put("category", allProductsQuery.getString(allProductsQuery.getColumnIndexOrThrow("category")))
                put("sellPrice", allProductsQuery.getDouble(allProductsQuery.getColumnIndexOrThrow("sellPrice")))
                put("costPrice", allProductsQuery.getDouble(allProductsQuery.getColumnIndexOrThrow("costPrice")))
                put("stock", allProductsQuery.getInt(allProductsQuery.getColumnIndexOrThrow("stock")))
            }
            prodArray.put(p)
            prodCount++
        }
        allProductsQuery.close()
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
        val userCount = users.size

        // 3. Export Orders and Order Items
        val allOrdersQuery = db.openHelper.readableDatabase.query("SELECT * FROM orders ORDER BY timestamp ASC")
        val orderArray = JSONArray()
        var orderCount = 0
        while (allOrdersQuery.moveToNext()) {
            val o = JSONObject().apply {
                put("orderId", allOrdersQuery.getString(allOrdersQuery.getColumnIndexOrThrow("orderId")))
                put("customerName", if (allOrdersQuery.isNull(allOrdersQuery.getColumnIndexOrThrow("customerName"))) JSONObject.NULL else allOrdersQuery.getString(allOrdersQuery.getColumnIndexOrThrow("customerName")))
                put("totalAmount", allOrdersQuery.getDouble(allOrdersQuery.getColumnIndexOrThrow("totalAmount")))
                put("paymentMethod", allOrdersQuery.getString(allOrdersQuery.getColumnIndexOrThrow("paymentMethod")))
                put("amountPaid", allOrdersQuery.getDouble(allOrdersQuery.getColumnIndexOrThrow("amountPaid")))
                put("changeAmount", allOrdersQuery.getDouble(allOrdersQuery.getColumnIndexOrThrow("changeAmount")))
                put("cashierId", allOrdersQuery.getLong(allOrdersQuery.getColumnIndexOrThrow("cashierId")))
                put("timestamp", allOrdersQuery.getLong(allOrdersQuery.getColumnIndexOrThrow("timestamp")))
            }
            orderArray.put(o)
            orderCount++
        }
        allOrdersQuery.close()
        root.put("orders", orderArray)

        val allOrderItemsQuery = db.openHelper.readableDatabase.query("SELECT * FROM order_items ORDER BY orderItemId ASC")
        val orderItemsArray = JSONArray()
        while (allOrderItemsQuery.moveToNext()) {
            val item = JSONObject().apply {
                put("orderItemId", allOrderItemsQuery.getLong(allOrderItemsQuery.getColumnIndexOrThrow("orderItemId")))
                put("orderId", allOrderItemsQuery.getString(allOrderItemsQuery.getColumnIndexOrThrow("orderId")))
                put("productId", allOrderItemsQuery.getLong(allOrderItemsQuery.getColumnIndexOrThrow("productId")))
                put("productName", allOrderItemsQuery.getString(allOrderItemsQuery.getColumnIndexOrThrow("productName")))
                put("priceAtPurchase", allOrderItemsQuery.getDouble(allOrderItemsQuery.getColumnIndexOrThrow("priceAtPurchase")))
                put("quantity", allOrderItemsQuery.getInt(allOrderItemsQuery.getColumnIndexOrThrow("quantity")))
            }
            orderItemsArray.put(item)
        }
        allOrderItemsQuery.close()
        root.put("order_items", orderItemsArray)

        val now = System.currentTimeMillis()
        root.put("appName", "Sumber Rejeki POS")
        root.put("version", 1)
        root.put("backupDate", now)
        root.put("backupDateFormatted", dateFormat.format(Date(now)))

        val jsonString = root.toString(2)

        // Save to Downloads directory (with fallback to app's external files directory)
        val timeStampStr = fileTimestampFormat.format(Date(now))
        val fileName = "POS_SumberRejeki_Backup_$timeStampStr.json"

        val targetDir = try {
            val publicDownloads = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            if (publicDownloads.exists() || publicDownloads.mkdirs()) {
                publicDownloads
            } else {
                context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) ?: context.filesDir
            }
        } catch (e: Exception) {
            context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) ?: context.filesDir
        }

        val backupFile = File(targetDir, fileName)
        FileOutputStream(backupFile).use { fos ->
            fos.write(jsonString.toByteArray(Charsets.UTF_8))
            fos.flush()
        }

        BackupExportResult(
            file = backupFile,
            fileName = fileName,
            filePath = backupFile.absolutePath,
            productCount = prodCount,
            userCount = userCount,
            orderCount = orderCount,
            jsonString = jsonString
        )
    }

    fun previewBackupJson(jsonString: String): BackupPreview {
        return try {
            val root = JSONObject(jsonString)
            val productCount = if (root.has("products")) root.getJSONArray("products").length() else 0
            val userCount = if (root.has("users")) root.getJSONArray("users").length() else 0
            val orderCount = if (root.has("orders")) root.getJSONArray("orders").length() else 0
            val backupDate = if (root.has("backupDate")) root.getLong("backupDate") else System.currentTimeMillis()
            val formattedDate = dateFormat.format(Date(backupDate))

            if (productCount == 0 && userCount == 0 && orderCount == 0) {
                BackupPreview(
                    isValid = false,
                    errorMessage = "File cadangan tidak berisi data produk, pengguna, atau transaksi yang valid."
                )
            } else {
                BackupPreview(
                    isValid = true,
                    productCount = productCount,
                    userCount = userCount,
                    orderCount = orderCount,
                    backupDateFormatted = formattedDate,
                    jsonContent = jsonString
                )
            }
        } catch (e: Exception) {
            BackupPreview(
                isValid = false,
                errorMessage = "Format file tidak valid: ${e.message}"
            )
        }
    }

    suspend fun restoreFromJson(jsonString: String): Result<BackupPreview> = withContext(Dispatchers.IO) {
        val preview = previewBackupJson(jsonString)
        if (!preview.isValid) {
            return@withContext Result.failure(IllegalArgumentException(preview.errorMessage ?: "Format cadangan tidak valid"))
        }

        try {
            val root = JSONObject(jsonString)

            db.withTransaction {
                // 1. Restore Products
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
                    if (productList.isNotEmpty()) {
                        db.productDao().insertProducts(productList)
                    }
                }

                // 2. Restore Users
                if (root.has("users")) {
                    val userArray = root.getJSONArray("users")
                    val userList = mutableListOf<UserEntity>()
                    for (i in 0 until userArray.length()) {
                        val u = userArray.getJSONObject(i)
                        userList.add(
                            UserEntity(
                                userId = u.optLong("userId", 0L),
                                username = u.getString("username"),
                                pinHash = u.getString("pinHash"),
                                role = u.getString("role"),
                                createdAt = u.optLong("createdAt", System.currentTimeMillis())
                            )
                        )
                    }
                    if (userList.isNotEmpty()) {
                        db.userDao().insertUsers(userList)
                    }
                }

                // 3. Restore Orders & Items
                if (root.has("orders")) {
                    val orderArray = root.getJSONArray("orders")
                    for (i in 0 until orderArray.length()) {
                        val o = orderArray.getJSONObject(i)
                        db.orderDao().insertOrder(
                            OrderEntity(
                                orderId = o.getString("orderId"),
                                customerName = if (o.isNull("customerName")) null else o.optString("customerName").takeIf { it.isNotBlank() },
                                totalAmount = o.getDouble("totalAmount"),
                                paymentMethod = o.getString("paymentMethod"),
                                amountPaid = o.getDouble("amountPaid"),
                                changeAmount = o.optDouble("changeAmount", o.optDouble("changeGiven", 0.0)),
                                cashierId = o.optLong("cashierId", 0L),
                                timestamp = o.optLong("timestamp", System.currentTimeMillis())
                            )
                        )
                    }
                }

                if (root.has("order_items")) {
                    val orderItemsArray = root.getJSONArray("order_items")
                    val itemsList = mutableListOf<OrderItemEntity>()
                    for (i in 0 until orderItemsArray.length()) {
                        val item = orderItemsArray.getJSONObject(i)
                        itemsList.add(
                            OrderItemEntity(
                                orderItemId = item.optLong("orderItemId", 0L),
                                orderId = item.getString("orderId"),
                                productId = item.getLong("productId"),
                                productName = item.getString("productName"),
                                priceAtPurchase = item.getDouble("priceAtPurchase"),
                                quantity = item.getInt("quantity")
                            )
                        )
                    }
                    if (itemsList.isNotEmpty()) {
                        db.orderDao().insertOrderItems(itemsList)
                    }
                }
            }

            Result.success(preview)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun readJsonFromUri(uri: Uri): String = withContext(Dispatchers.IO) {
        val stringBuilder = StringBuilder()
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8)).use { reader ->
                var line: String? = reader.readLine()
                while (line != null) {
                    stringBuilder.append(line).append('\n')
                    line = reader.readLine()
                }
            }
        }
        stringBuilder.toString()
    }

    fun createShareIntent(file: File): Intent {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        return Intent(Intent.ACTION_SEND).apply {
            type = "application/json"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Backup Database POS Sumber Rejeki")
            putExtra(Intent.EXTRA_TEXT, "File cadangan database POS Sumber Rejeki: ${file.name}")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }
}
