package com.punchlist.app.data.repository

import com.google.firebase.auth.FirebaseUser
import com.punchlist.app.util.Result
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val currentUser: FirebaseUser?
    val authStateFlow: Flow<FirebaseUser?>
    suspend fun signIn(email: String, password: String): Result<FirebaseUser>
    suspend fun signUp(email: String, password: String, name: String): Result<FirebaseUser>
    suspend fun signOut()
    suspend fun sendPasswordReset(email: String): Result<Unit>
}
