package com.punchlist.app.data.model

import com.google.firebase.Timestamp

data class Project(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val createdByUserId: String = "",
    val createdAt: Timestamp? = null,
    val memberCount: Int = 0
) {
    fun toFirestoreMap(): Map<String, Any?> = mapOf(
        "name" to name,
        "description" to description,
        "createdByUserId" to createdByUserId,
        "createdAt" to (createdAt ?: Timestamp.now()),
        "memberCount" to memberCount
    )

    companion object {
        fun fromMap(id: String, map: Map<String, Any?>): Project = Project(
            id = id,
            name = map["name"] as? String ?: "",
            description = map["description"] as? String ?: "",
            createdByUserId = map["createdByUserId"] as? String ?: "",
            createdAt = map["createdAt"] as? Timestamp,
            memberCount = (map["memberCount"] as? Long)?.toInt() ?: 0
        )
    }
}
