package com.punchlist.app.data.repository

import com.punchlist.app.data.model.Project
import com.punchlist.app.data.model.User
import com.punchlist.app.data.remote.FirestoreService
import com.punchlist.app.util.Result
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProjectRepositoryImpl @Inject constructor(
    private val firestoreService: FirestoreService
) : ProjectRepository {

    override fun observeProjects(userId: String): Flow<List<Project>> =
        firestoreService.observeProjects(userId)

    override suspend fun createProject(project: Project): Result<String> = try {
        val id = firestoreService.createProject(project)
        Result.Success(id)
    } catch (e: Exception) {
        Result.Error(e.message ?: "Failed to create project", e)
    }

    override suspend fun addMember(projectId: String, userId: String, role: String): Result<Unit> = try {
        firestoreService.addProjectMember(projectId, userId, role)
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(e.message ?: "Failed to add member", e)
    }

    override suspend fun getMembers(projectId: String): Result<List<User>> = try {
        val members = firestoreService.getProjectMembers(projectId)
        Result.Success(members)
    } catch (e: Exception) {
        Result.Error(e.message ?: "Failed to fetch members", e)
    }
}
