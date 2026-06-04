package com.punchlist.app.data.repository

import android.net.Uri
import com.punchlist.app.data.remote.StorageService
import com.punchlist.app.util.Result
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StorageRepositoryImpl @Inject constructor(
    private val storageService: StorageService
) : StorageRepository {

    override suspend fun uploadPhoto(localUri: Uri, projectId: String, itemId: String, isCompletion: Boolean): Result<String> = try {
        val url = storageService.uploadPhoto(localUri, projectId, itemId, isCompletion)
        Result.Success(url)
    } catch (e: Exception) {
        Result.Error(e.message ?: "Upload failed", e)
    }

    override suspend fun deletePhoto(downloadUrl: String): Result<Unit> = try {
        storageService.deletePhoto(downloadUrl)
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(e.message ?: "Delete failed", e)
    }
}
