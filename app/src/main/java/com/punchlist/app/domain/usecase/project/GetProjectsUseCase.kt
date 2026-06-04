package com.punchlist.app.domain.usecase.project

import com.punchlist.app.data.model.Project
import com.punchlist.app.data.repository.ProjectRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetProjectsUseCase @Inject constructor(
    private val projectRepository: ProjectRepository
) {
    operator fun invoke(userId: String): Flow<List<Project>> =
        projectRepository.observeProjects(userId)
}
