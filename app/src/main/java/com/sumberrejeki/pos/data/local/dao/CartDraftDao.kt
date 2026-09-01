package com.sumberrejeki.pos.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.sumberrejeki.pos.data.local.entity.CartDraftEntity
import com.sumberrejeki.pos.data.local.entity.CartDraftWithProduct
import kotlinx.coroutines.flow.Flow

@Dao
interface CartDraftDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDraft(draft: CartDraftEntity): Long

    @Update
    suspend fun updateDraft(draft: CartDraftEntity)

    @Delete
    suspend fun deleteDraft(draft: CartDraftEntity)

    @Query("DELETE FROM cart_drafts WHERE draftId = :draftId")
    suspend fun deleteDraftById(draftId: Long)

    @Query("DELETE FROM cart_drafts WHERE productId = :productId")
    suspend fun deleteDraftByProductId(productId: Long)

    @Query("DELETE FROM cart_drafts")
    suspend fun clearAllDrafts()

    @Query("SELECT * FROM cart_drafts WHERE productId = :productId LIMIT 1")
    suspend fun getDraftByProductId(productId: Long): CartDraftEntity?

    @Transaction
    @Query("SELECT * FROM cart_drafts ORDER BY draftId DESC")
    fun getAllCartDraftsWithProductFlow(): Flow<List<CartDraftWithProduct>>

    @Transaction
    @Query("SELECT * FROM cart_drafts ORDER BY draftId DESC")
    suspend fun getAllCartDraftsWithProduct(): List<CartDraftWithProduct>

    @Query("SELECT COUNT(*) FROM cart_drafts")
    fun getDraftCountFlow(): Flow<Int>

    @Query("SELECT SUM(quantity) FROM cart_drafts")
    fun getTotalItemCountFlow(): Flow<Int?>
}
