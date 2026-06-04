package com.punchlist.app.domain.usecase.comment

import com.punchlist.app.data.model.Comment
import com.punchlist.app.data.repository.CommentRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCommentsUseCase @Inject constructor(
    private val commentRepository: CommentRepository
) {
    operator fun invoke(projectId: String, itemId: String): Flow<List<Comment>> =
        commentRepository.observeComments(projectId, itemId)
}
