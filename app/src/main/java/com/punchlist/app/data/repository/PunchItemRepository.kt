package com.punchlist.app.data.repository

import com.punchlist.app.data.model.PunchItem
import com.punchlist.app.data.model.Status
import com.punchlist.app.util.Result
import kotlinx.coroutines.flow.Flow

interface PunchItemRepository {
    fun observePunchItems(projectId: String): Flow<List<PunchItem>>
    suspend fun createPunchItem(item: PunchItem): Result<String>
    suspend fun updatePunchItem(item: PunchItem): Result<Unit>
    suspend fun updateStatus(projectId: String, itemId: String, status: Status): Result<Unit>
    suspend fun getPunchItem(projectId: String, itemId: String): Result<PunchItem>
}
