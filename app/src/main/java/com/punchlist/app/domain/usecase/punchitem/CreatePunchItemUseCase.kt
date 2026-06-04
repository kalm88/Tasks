package com.punchlist.app.domain.usecase.punchitem

import android.net.Uri
import com.punchlist.app.data.model.PunchItem
import com.punchlist.app.data.model.Priority
import com.punchlist.app.data.model.Status
import com.punchlist.app.data.repository.PunchItemRepository
import com.punchlist.app.data.repository.StorageRepository
import com.punchlist.app.util.Result
import javax.inject.Inject

class CreatePunchItemUseCase @Inject constructor(
    private val punchItemRepository: PunchItemRepository,
    private val storageRepository: StorageRepository
) {
    suspend operator fun invoke(
        projectId: String,
        title: String,
        issueDescription: String,
        workRequired: String,
        location: String,
        priority: Priority,
        assignedToUserId: String,
        assignedToUserName: String,
        createdByUserId: String,
        createdByUserName: String,
        localPhotoUris: List<Uri>,
        sku: String = ""
    ): Result<String> {
        if (title.isBlank()) return Result.Error("Title is required")

        // Create item with placeholder ID first so we have a path for Storage upload
        val placeholderId = java.util.UUID.randomUUID().toString()

        // Upload photos to Storage
        val uploadedUrls = mutableListOf<String>()
        for (uri in localPhotoUris) {
            when (val uploadResult = storageRepository.uploadPhoto(uri, projectId, placeholderId, false)) {
                is Result.Success -> uploadedUrls.add(uploadResult.data)
                is Result.Error -> return Result.Error("Photo upload failed: ${uploadResult.message}")
                Result.Loading -> {}
            }
        }

        val item = PunchItem(
            projectId = projectId,
            title = title.trim(),
            issueDescription = issueDescription.trim(),
            workRequired = workRequired.trim(),
            location = location.trim(),
            priority = priority,
            status = Status.OPEN,
            assignedToUserId = assignedToUserId,
            assignedToUserName = assignedToUserName,
            createdByUserId = createdByUserId,
            createdByUserName = createdByUserName,
            photoUrls = uploadedUrls,
            sku = sku.trim()
        )
        return punchItemRepository.createPunchItem(item)
    }
}
