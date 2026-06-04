package com.punchlist.app.domain.usecase.auth

import com.google.firebase.auth.FirebaseUser
import com.punchlist.app.data.repository.AuthRepository
import com.punchlist.app.util.Result
import javax.inject.Inject

class SignInUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String): Result<FirebaseUser> {
        if (email.isBlank()) return Result.Error("Email is required")
        if (password.isBlank()) return Result.Error("Password is required")
        return authRepository.signIn(email.trim(), password)
    }
}
