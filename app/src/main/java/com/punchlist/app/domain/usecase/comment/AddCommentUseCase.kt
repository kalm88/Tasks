package com.punchlist.app.domain.usecase.comment

import com.punchlist.app.data.model.Comment
import com.punchlist.app.data.repository.CommentRepository
import com.punchlist.app.util.Result
import javax.inject.Inject

class AddCommentUseCase @Inject constructor(
    private val commentRepository: CommentRepository
) {
    suspend operator fun invoke(projectId: String, comment: Comment): Result<String> {
        if (comment.text.isBlank()) return Result.Error("Comment cannot be empty")
        return commentRepository.addComment(projectId, comment)
    }
}
