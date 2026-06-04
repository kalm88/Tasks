package com.punchlist.app.data.remote

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

// Firebase Storage paths:
// projects/{projectId}/punchItems/{itemId}/photos/{uuid}.jpg
// projects/{projectId}/punchItems/{itemId}/completionPhotos/{uuid}.jpg

@Singleton
class StorageService @Inject constructor(
    private val storage: FirebaseStorage
) {
    suspend fun uploadPhoto(localUri: Uri, projectId: String, itemId: String, isCompletion: Boolean = false): String {
        val folder = if (isCompletion) "completionPhotos" else "photos"
        val fileName = "${UUID.randomUUID()}.jpg"
        val ref = storage.reference
            .child("projects/$projectId/punchItems/$itemId/$folder/$fileName")
        ref.putFile(localUri).await()
        return ref.downloadUrl.await().toString()
    }

    suspend fun deletePhoto(downloadUrl: String) {
        try {
            storage.getReferenceFromUrl(downloadUrl).delete().await()
        } catch (e: Exception) {
            // Swallow if already deleted
        }
    }
}
