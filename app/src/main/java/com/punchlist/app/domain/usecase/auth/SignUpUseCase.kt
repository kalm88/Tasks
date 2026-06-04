package com.punchlist.app.domain.usecase.auth

import com.google.firebase.auth.FirebaseUser
import com.punchlist.app.data.model.Role
import com.punchlist.app.data.model.User
import com.punchlist.app.data.repository.AuthRepository
import com.punchlist.app.data.repository.UserRepository
import com.punchlist.app.util.Result
import javax.inject.Inject

class SignUpUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(email: String, password: String, name: String): Result<FirebaseUser> {
        if (email.isBlank()) return Result.Error("Email is required")
        if (password.length < 6) return Result.Error("Password must be at least 6 characters")
        if (name.isBlank()) return Result.Error("Name is required")

        return when (val authResult = authRepository.signUp(email.trim(), password, name.trim())) {
            is Result.Success -> {
                val user = User(
                    id = authResult.data.uid,
                    email = email.trim(),
                    name = name.trim(),
                    role = Role.WORKER
                )
                userRepository.createUser(user)
                authResult
            }
            is Result.Error -> authResult
            Result.Loading -> authResult
        }
    }
}
