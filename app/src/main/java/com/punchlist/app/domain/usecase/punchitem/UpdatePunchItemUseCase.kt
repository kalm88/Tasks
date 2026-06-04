package com.punchlist.app.domain.usecase.punchitem

import com.punchlist.app.data.model.PunchItem
import com.punchlist.app.data.repository.PunchItemRepository
import com.punchlist.app.util.Result
import javax.inject.Inject

class UpdatePunchItemUseCase @Inject constructor(
    private val punchItemRepository: PunchItemRepository
) {
    suspend operator fun invoke(item: PunchItem): Result<Unit> =
        punchItemRepository.updatePunchItem(item)
}
