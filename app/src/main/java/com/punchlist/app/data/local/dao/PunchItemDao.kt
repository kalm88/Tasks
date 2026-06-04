package com.punchlist.app.data.local.dao

import androidx.room.*
import com.punchlist.app.data.local.entity.PunchItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PunchItemDao {
    @Query("SELECT * FROM punch_items WHERE projectId = :projectId ORDER BY createdAtMs DESC")
    fun observeByProject(projectId: String): Flow<List<PunchItemEntity>>

    @Query("SELECT * FROM punch_items WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): PunchItemEntity?

    @Upsert
    suspend fun upsertAll(items: List<PunchItemEntity>)

    @Upsert
    suspend fun upsert(item: PunchItemEntity)

    @Query("DELETE FROM punch_items WHERE projectId = :projectId")
    suspend fun deleteByProject(projectId: String)

    @Query("SELECT * FROM punch_items WHERE syncPending = 1")
    suspend fun getPendingSync(): List<PunchItemEntity>
}
