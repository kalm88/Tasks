package com.punchlist.app.data.repository

import com.punchlist.app.data.model.Comment
import com.punchlist.app.data.remote.FirestoreService
import com.punchlist.app.util.Result
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CommentRepositoryImpl @Inject constructor(
    private val firestoreService: FirestoreService
) : CommentRepository {

    override fun observeComments(projectId: String, itemId: String): Flow<List<Comment>> =
        firestoreService.observeComments(projectId, itemId)

    override suspend fun addComment(projectId: String, comment: Comment): Result<String> = try {
        val id = firestoreService.addComment(projectId, comment)
        Result.Success(id)
    } catch (e: Exception) {
        Result.Error(e.message ?: "Failed to add comment", e)
    }
}
