package com.punchlist.app.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.punchlist.app.data.model.User
import com.punchlist.app.data.remote.FirestoreService
import com.punchlist.app.util.Result
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val firestoreService: FirestoreService,
    private val auth: FirebaseAuth
) : UserRepository {

    override suspend fun getUser(userId: String): Result<User> = try {
        val user = firestoreService.getUser(userId)
        if (user != null) Result.Success(user)
        else Result.Error("User not found")
    } catch (e: Exception) {
        Result.Error(e.message ?: "Failed to fetch user", e)
    }

    override suspend fun createUser(user: User): Result<Unit> = try {
        firestoreService.createUser(user)
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(e.message ?: "Failed to create user", e)
    }

    override suspend fun updateFcmToken(userId: String, token: String): Result<Unit> = try {
        firestoreService.updateFcmToken(userId, token)
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(e.message ?: "Failed to update FCM token", e)
    }

    override suspend fun getCurrentUser(): User? {
        val uid = auth.currentUser?.uid ?: return null
        return firestoreService.getUser(uid)
    }
}
