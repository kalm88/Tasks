package com.punchlist.app.data.model

import com.google.firebase.Timestamp

data class Attachment(
    val id: String = "",
    val url: String = "",
    val type: String = "image",
    val uploadedByUserId: String = "",
    val createdAt: Timestamp? = null
)
