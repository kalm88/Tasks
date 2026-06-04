package com.punchlist.app.domain.usecase.project

import com.punchlist.app.data.model.Project
import com.punchlist.app.data.repository.ProjectRepository
import com.punchlist.app.util.Result
import javax.inject.Inject

class CreateProjectUseCase @Inject constructor(
    private val projectRepository: ProjectRepository
) {
    suspend operator fun invoke(name: String, description: String, createdByUserId: String): Result<String> {
        if (name.isBlank()) return Result.Error("Project name is required")
        val project = Project(
            name = name.trim(),
            description = description.trim(),
            createdByUserId = createdByUserId
        )
        return projectRepository.createProject(project)
    }
}
