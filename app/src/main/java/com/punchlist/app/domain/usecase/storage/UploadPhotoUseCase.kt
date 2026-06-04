package com.punchlist.app.domain.usecase.storage

import android.net.Uri
import com.punchlist.app.data.repository.StorageRepository
import com.punchlist.app.util.Result
import javax.inject.Inject

class UploadPhotoUseCase @Inject constructor(
    private val storageRepository: StorageRepository
) {
    suspend operator fun invoke(
        localUri: Uri,
        projectId: String,
        itemId: String,
        isCompletion: Boolean = false
    ): Result<String> = storageRepository.uploadPhoto(localUri, projectId, itemId, isCompletion)
}
