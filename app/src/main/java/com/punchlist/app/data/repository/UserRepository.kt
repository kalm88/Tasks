package com.punchlist.app.data.repository

import com.punchlist.app.data.model.User
import com.punchlist.app.util.Result

interface UserRepository {
    suspend fun getUser(userId: String): Result<User>
    suspend fun createUser(user: User): Result<Unit>
    suspend fun updateFcmToken(userId: String, token: String): Result<Unit>
    suspend fun getCurrentUser(): User?
}
