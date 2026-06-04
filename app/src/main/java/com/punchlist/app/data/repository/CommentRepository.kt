package com.punchlist.app.data.repository

import com.punchlist.app.data.model.Comment
import com.punchlist.app.util.Result
import kotlinx.coroutines.flow.Flow

interface CommentRepository {
    fun observeComments(projectId: String, itemId: String): Flow<List<Comment>>
    suspend fun addComment(projectId: String, comment: Comment): Result<String>
}
