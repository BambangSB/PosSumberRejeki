package com.sumberrejeki.pos.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "cart_drafts",
    foreignKeys = [
        ForeignKey(
            entity = ProductEntity::class,
            parentColumns = ["productId"],
            childColumns = ["productId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["productId"])]
)
data class CartDraftEntity(
    @PrimaryKey(autoGenerate = true)
    val draftId: Long = 0,
    val productId: Long,
    val quantity: Int,
    val notes: String? = null
)
