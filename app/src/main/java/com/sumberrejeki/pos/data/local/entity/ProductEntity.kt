package com.sumberrejeki.pos.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.sumberrejeki.pos.domain.model.Product

@Entity(
    tableName = "products",
    indices = [Index(value = ["barcode"], unique = true)]
)
data class ProductEntity(
    @PrimaryKey(autoGenerate = true)
    val productId: Long = 0,
    val barcode: String,
    val name: String,
    val category: String,
    val sellPrice: Double,
    val costPrice: Double,
    val stock: Int
) {
    fun toDomain(): Product = Product(
        productId = productId,
        barcode = barcode,
        name = name,
        category = category,
        sellPrice = sellPrice,
        costPrice = costPrice,
        stock = stock
    )

    companion object {
        fun fromDomain(product: Product): ProductEntity = ProductEntity(
            productId = product.productId,
            barcode = product.barcode,
            name = product.name,
            category = product.category,
            sellPrice = product.sellPrice,
            costPrice = product.costPrice,
            stock = product.stock
        )
    }
}
