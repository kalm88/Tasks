package com.punchlist.app.data.repository

import android.net.Uri
import com.punchlist.app.util.Result

interface StorageRepository {
    suspend fun uploadPhoto(localUri: Uri, projectId: String, itemId: String, isCompletion: Boolean): Result<String>
    suspend fun deletePhoto(downloadUrl: String): Result<Unit>
}
