package com.punchlist.app.data.model

import com.google.firebase.Timestamp

data class PunchItem(
    val id: String = "",
    val projectId: String = "",
    val title: String = "",
    val issueDescription: String = "",
    val workRequired: String = "",
    val location: String = "",
    val priority: Priority = Priority.MEDIUM,
    val status: Status = Status.OPEN,
    val assignedToUserId: String = "",
    val assignedToUserName: String = "",
    val createdByUserId: String = "",
    val createdByUserName: String = "",
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null,
    val dueDate: Timestamp? = null,
    val photoUrls: List<String> = emptyList(),
    val completionPhotoUrls: List<String> = emptyList(),
    val commentCount: Int = 0,
    val syncPending: Boolean = false
) {
    fun toFirestoreMap(): Map<String, Any?> = mapOf(
        "projectId" to projectId,
        "title" to title,
        "issueDescription" to issueDescription,
        "workRequired" to workRequired,
        "location" to location,
        "priority" to priority.name,
        "status" to status.name,
        "assignedToUserId" to assignedToUserId,
        "assignedToUserName" to assignedToUserName,
        "createdByUserId" to createdByUserId,
        "createdByUserName" to createdByUserName,
        "createdAt" to (createdAt ?: Timestamp.now()),
        "updatedAt" to Timestamp.now(),
        "dueDate" to dueDate,
        "photoUrls" to photoUrls,
        "completionPhotoUrls" to completionPhotoUrls,
        "commentCount" to commentCount
    )

    companion object {
        @Suppress("UNCHECKED_CAST")
        fun fromMap(id: String, map: Map<String, Any?>): PunchItem = PunchItem(
            id = id,
            projectId = map["projectId"] as? String ?: "",
            title = map["title"] as? String ?: "",
            issueDescription = map["issueDescription"] as? String ?: "",
            workRequired = map["workRequired"] as? String ?: "",
            location = map["location"] as? String ?: "",
            priority = Priority.fromString(map["priority"] as? String ?: ""),
            status = Status.fromString(map["status"] as? String ?: ""),
            assignedToUserId = map["assignedToUserId"] as? String ?: "",
            assignedToUserName = map["assignedToUserName"] as? String ?: "",
            createdByUserId = map["createdByUserId"] as? String ?: "",
            createdByUserName = map["createdByUserName"] as? String ?: "",
            createdAt = map["createdAt"] as? Timestamp,
            updatedAt = map["updatedAt"] as? Timestamp,
            dueDate = map["dueDate"] as? Timestamp,
            photoUrls = (map["photoUrls"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
            completionPhotoUrls = (map["completionPhotoUrls"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
            commentCount = (map["commentCount"] as? Long)?.toInt() ?: 0
        )
    }
}
