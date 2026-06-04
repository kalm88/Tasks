package com.punchlist.app.domain.usecase.punchitem

import com.punchlist.app.data.model.PunchItem
import com.punchlist.app.data.repository.PunchItemRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetPunchItemsUseCase @Inject constructor(
    private val punchItemRepository: PunchItemRepository
) {
    operator fun invoke(projectId: String): Flow<List<PunchItem>> =
        punchItemRepository.observePunchItems(projectId)
}
