package com.punchlist.app.domain.usecase.punchitem

import com.punchlist.app.data.model.PunchItem
import com.punchlist.app.data.repository.PunchItemRepository
import com.punchlist.app.util.Result
import javax.inject.Inject

class GetPunchItemDetailUseCase @Inject constructor(
    private val punchItemRepository: PunchItemRepository
) {
    suspend operator fun invoke(projectId: String, itemId: String): Result<PunchItem> =
        punchItemRepository.getPunchItem(projectId, itemId)
}
