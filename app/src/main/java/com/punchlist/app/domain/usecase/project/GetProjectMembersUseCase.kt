package com.punchlist.app.domain.usecase.project

import com.punchlist.app.data.model.User
import com.punchlist.app.data.repository.ProjectRepository
import com.punchlist.app.util.Result
import javax.inject.Inject

class GetProjectMembersUseCase @Inject constructor(
    private val projectRepository: ProjectRepository
) {
    suspend operator fun invoke(projectId: String): Result<List<User>> =
        projectRepository.getMembers(projectId)
}
