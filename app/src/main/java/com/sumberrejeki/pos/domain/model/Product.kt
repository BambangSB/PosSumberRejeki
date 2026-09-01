package com.sumberrejeki.pos.domain.model

data class Product(
    val productId: Long = 0,
    val barcode: String,
    val name: String,
    val category: String,
    val sellPrice: Double,
    val costPrice: Double,
    val stock: Int
)
