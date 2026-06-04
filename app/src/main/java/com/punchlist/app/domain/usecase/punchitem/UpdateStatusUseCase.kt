package com.punchlist.app.domain.usecase.punchitem

import com.punchlist.app.data.model.Status
import com.punchlist.app.data.repository.PunchItemRepository
import com.punchlist.app.util.Result
import javax.inject.Inject

class UpdateStatusUseCase @Inject constructor(
    private val punchItemRepository: PunchItemRepository
) {
    suspend operator fun invoke(projectId: String, itemId: String, status: Status): Result<Unit> =
        punchItemRepository.updateStatus(projectId, itemId, status)
}
