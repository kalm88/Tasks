package com.punchlist.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.punchlist.app.data.model.Priority
import com.punchlist.app.data.model.PunchItem
import com.punchlist.app.data.model.Status

@Entity(tableName = "punch_items")
data class PunchItemEntity(
    @PrimaryKey val id: String,
    val projectId: String,
    val title: String,
    val issueDescription: String,
    val workRequired: String,
    val location: String,
    val priority: String,
    val status: String,
    val assignedToUserId: String,
    val assignedToUserName: String,
    val createdByUserId: String,
    val createdByUserName: String,
    val createdAtMs: Long?,
    val dueDateMs: Long?,
    val photoUrls: String, // JSON array stored as string
    val completionPhotoUrls: String,
    val commentCount: Int,
    val sku: String = "",
    val syncPending: Boolean = false
) {
    fun toDomain(): PunchItem = PunchItem(
        id = id,
        projectId = projectId,
        title = title,
        issueDescription = issueDescription,
        workRequired = workRequired,
        location = location,
        priority = Priority.fromString(priority),
        status = Status.fromString(status),
        assignedToUserId = assignedToUserId,
        assignedToUserName = assignedToUserName,
        createdByUserId = createdByUserId,
        createdByUserName = createdByUserName,
        photoUrls = photoUrls.split(",").filter { it.isNotEmpty() },
        completionPhotoUrls = completionPhotoUrls.split(",").filter { it.isNotEmpty() },
        commentCount = commentCount,
        sku = sku,
        syncPending = syncPending
    )
}

fun PunchItem.toEntity(): PunchItemEntity = PunchItemEntity(
    id = id,
    projectId = projectId,
    title = title,
    issueDescription = issueDescription,
    workRequired = workRequired,
    location = location,
    priority = priority.name,
    status = status.name,
    assignedToUserId = assignedToUserId,
    assignedToUserName = assignedToUserName,
    createdByUserId = createdByUserId,
    createdByUserName = createdByUserName,
    createdAtMs = createdAt?.toDate()?.time,
    dueDateMs = dueDate?.toDate()?.time,
    photoUrls = photoUrls.joinToString(","),
    completionPhotoUrls = completionPhotoUrls.joinToString(","),
    commentCount = commentCount,
    sku = sku,
    syncPending = syncPending
)
