package com.punchlist.app.data.repository

import com.punchlist.app.data.model.PunchItem
import com.punchlist.app.data.model.Status
import com.punchlist.app.data.remote.FirestoreService
import com.punchlist.app.util.Result
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PunchItemRepositoryImpl @Inject constructor(
    private val firestoreService: FirestoreService
) : PunchItemRepository {

    override fun observePunchItems(projectId: String): Flow<List<PunchItem>> =
        firestoreService.observePunchItems(projectId)

    override suspend fun createPunchItem(item: PunchItem): Result<String> = try {
        val id = firestoreService.createPunchItem(item)
        Result.Success(id)
    } catch (e: Exception) {
        Result.Error(e.message ?: "Failed to create item", e)
    }

    override suspend fun updatePunchItem(item: PunchItem): Result<Unit> = try {
        firestoreService.updatePunchItem(item)
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(e.message ?: "Failed to update item", e)
    }

    override suspend fun updateStatus(projectId: String, itemId: String, status: Status): Result<Unit> = try {
        firestoreService.updateStatus(projectId, itemId, status.name)
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(e.message ?: "Failed to update status", e)
    }

    override suspend fun getPunchItem(projectId: String, itemId: String): Result<PunchItem> = try {
        val item = firestoreService.getPunchItem(projectId, itemId)
        if (item != null) Result.Success(item)
        else Result.Error("Item not found")
    } catch (e: Exception) {
        Result.Error(e.message ?: "Failed to fetch item", e)
    }
}
