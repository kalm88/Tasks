package com.punchlist.app.data.model

data class User(
    val id: String = "",
    val email: String = "",
    val name: String = "",
    val role: Role = Role.WORKER,
    val companyId: String = "",
    val fcmToken: String = ""
) {
    fun toFirestoreMap(): Map<String, Any> = mapOf(
        "email" to email,
        "name" to name,
        "role" to role.name,
        "companyId" to companyId,
        "fcmToken" to fcmToken
    )

    companion object {
        fun fromMap(id: String, map: Map<String, Any?>): User = User(
            id = id,
            email = map["email"] as? String ?: "",
            name = map["name"] as? String ?: "",
            role = Role.fromString(map["role"] as? String ?: ""),
            companyId = map["companyId"] as? String ?: "",
            fcmToken = map["fcmToken"] as? String ?: ""
        )
    }
}
