package com.punchlist.app.data.repository

import com.punchlist.app.data.model.Project
import com.punchlist.app.data.model.User
import com.punchlist.app.util.Result
import kotlinx.coroutines.flow.Flow

interface ProjectRepository {
    fun observeProjects(userId: String): Flow<List<Project>>
    suspend fun createProject(project: Project): Result<String>
    suspend fun addMember(projectId: String, userId: String, role: String): Result<Unit>
    suspend fun getMembers(projectId: String): Result<List<User>>
}
