package com.punchlist.app.data.remote

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.punchlist.app.data.model.Comment
import com.punchlist.app.data.model.Project
import com.punchlist.app.data.model.PunchItem
import com.punchlist.app.data.model.User
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

// Firestore collection paths mirror the planned structure:
// users/{userId}
// projects/{projectId}
// projects/{projectId}/members/{userId}
// projects/{projectId}/punchItems/{itemId}
// projects/{projectId}/punchItems/{itemId}/comments/{commentId}

@Singleton
class FirestoreService @Inject constructor(
    private val db: FirebaseFirestore
) {
    // ── Users ───────────────────────────────────────────────────────────────

    suspend fun createUser(user: User) {
        db.collection("users").document(user.id).set(user.toFirestoreMap()).await()
    }

    suspend fun getUser(userId: String): User? {
        val doc = db.collection("users").document(userId).get().await()
        return if (doc.exists()) User.fromMap(doc.id, doc.data ?: emptyMap()) else null
    }

    suspend fun updateFcmToken(userId: String, token: String) {
        db.collection("users").document(userId).update("fcmToken", token).await()
    }

    // ── Projects ────────────────────────────────────────────────────────────

    fun observeProjects(userId: String): Flow<List<Project>> = callbackFlow {
        // Return projects where the current user is a member
        val listener = db.collectionGroup("members")
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                // We get member docs; resolve their parent project IDs
                val projectIds = snapshot.documents.map { it.reference.parent.parent!!.id }
                if (projectIds.isEmpty()) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                // Fetch projects by IDs (Firestore whereIn limit is 30)
                db.collection("projects")
                    .whereIn("__name__", projectIds.take(30))
                    .get()
                    .addOnSuccessListener { projSnapshot ->
                        val projects = projSnapshot.documents.mapNotNull { doc ->
                            doc.data?.let { Project.fromMap(doc.id, it) }
                        }
                        trySend(projects)
                    }
            }
        awaitClose { listener.remove() }
    }

    suspend fun createProject(project: Project): String {
        val ref = db.collection("projects").document()
        val withId = project.copy(id = ref.id)
        ref.set(withId.toFirestoreMap()).await()
        // Add creator as member with Admin role
        ref.collection("members").document(project.createdByUserId)
            .set(mapOf("userId" to project.createdByUserId, "role" to "ADMIN")).await()
        return ref.id
    }

    suspend fun addProjectMember(projectId: String, userId: String, role: String) {
        db.collection("projects").document(projectId)
            .collection("members").document(userId)
            .set(mapOf("userId" to userId, "role" to role)).await()
    }

    suspend fun getProjectMembers(projectId: String): List<User> {
        val memberDocs = db.collection("projects").document(projectId)
            .collection("members").get().await()
        return memberDocs.documents.mapNotNull { doc ->
            val uid = doc.getString("userId") ?: return@mapNotNull null
            getUser(uid)
        }
    }

    // ── PunchItems ──────────────────────────────────────────────────────────

    fun observePunchItems(projectId: String): Flow<List<PunchItem>> = callbackFlow {
        val listener = db.collection("projects").document(projectId)
            .collection("punchItems")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                val items = snapshot.documents.mapNotNull { doc ->
                    doc.data?.let { PunchItem.fromMap(doc.id, it) }
                }
                trySend(items)
            }
        awaitClose { listener.remove() }
    }

    suspend fun createPunchItem(item: PunchItem): String {
        val ref = db.collection("projects").document(item.projectId)
            .collection("punchItems").document()
        ref.set(item.copy(id = ref.id).toFirestoreMap()).await()
        return ref.id
    }

    suspend fun updatePunchItem(item: PunchItem) {
        db.collection("projects").document(item.projectId)
            .collection("punchItems").document(item.id)
            .update(item.toFirestoreMap()).await()
    }

    suspend fun updateStatus(projectId: String, itemId: String, status: String) {
        db.collection("projects").document(projectId)
            .collection("punchItems").document(itemId)
            .update(mapOf("status" to status, "updatedAt" to com.google.firebase.Timestamp.now()))
            .await()
    }

    suspend fun getPunchItem(projectId: String, itemId: String): PunchItem? {
        val doc = db.collection("projects").document(projectId)
            .collection("punchItems").document(itemId).get().await()
        return if (doc.exists()) doc.data?.let { PunchItem.fromMap(doc.id, it) } else null
    }

    // ── Comments ────────────────────────────────────────────────────────────

    fun observeComments(projectId: String, itemId: String): Flow<List<Comment>> = callbackFlow {
        val listener = db.collection("projects").document(projectId)
            .collection("punchItems").document(itemId)
            .collection("comments")
            .orderBy("createdAt", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                val comments = snapshot.documents.mapNotNull { doc ->
                    doc.data?.let { Comment.fromMap(doc.id, it) }
                }
                trySend(comments)
            }
        awaitClose { listener.remove() }
    }

    suspend fun addComment(projectId: String, comment: Comment): String {
        val ref = db.collection("projects").document(projectId)
            .collection("punchItems").document(comment.itemId)
            .collection("comments").document()
        ref.set(comment.copy(id = ref.id).toFirestoreMap()).await()
        // Increment comment count on the parent item
        db.collection("projects").document(projectId)
            .collection("punchItems").document(comment.itemId)
            .update("commentCount", com.google.firebase.firestore.FieldValue.increment(1))
            .await()
        return ref.id
    }
}
