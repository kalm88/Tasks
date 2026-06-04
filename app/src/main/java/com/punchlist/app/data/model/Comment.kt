package com.punchlist.app.data.model

import com.google.firebase.Timestamp

data class Comment(
    val id: String = "",
    val itemId: String = "",
    val userId: String = "",
    val userName: String = "",
    val text: String = "",
    val createdAt: Timestamp? = null,
    val photoUrl: String = ""
) {
    fun toFirestoreMap(): Map<String, Any?> = mapOf(
        "itemId" to itemId,
        "userId" to userId,
        "userName" to userName,
        "text" to text,
        "createdAt" to (createdAt ?: Timestamp.now()),
        "photoUrl" to photoUrl
    )

    companion object {
        fun fromMap(id: String, map: Map<String, Any?>): Comment = Comment(
            id = id,
            itemId = map["itemId"] as? String ?: "",
            userId = map["userId"] as? String ?: "",
            userName = map["userName"] as? String ?: "",
            text = map["text"] as? String ?: "",
            createdAt = map["createdAt"] as? Timestamp,
            photoUrl = map["photoUrl"] as? String ?: ""
        )
    }
}
