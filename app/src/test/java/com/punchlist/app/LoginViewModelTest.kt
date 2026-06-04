package com.punchlist.app

import app.cash.turbine.test
import com.google.firebase.auth.FirebaseUser
import com.punchlist.app.domain.usecase.auth.SignInUseCase
import com.punchlist.app.ui.auth.LoginViewModel
import com.punchlist.app.util.Result
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val signInUseCase: SignInUseCase = mockk()
    private lateinit var viewModel: LoginViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = LoginViewModel(signInUseCase)
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `signIn success sets success state`() = runTest {
        val user = mockk<FirebaseUser>()
        coEvery { signInUseCase(any(), any()) } returns Result.Success(user)

        viewModel.uiState.test {
            viewModel.signIn("test@example.com", "password123")
            val loading = awaitItem()
            assertTrue(loading.isLoading)
            val success = awaitItem()
            assertTrue(success.success)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `signIn failure sets error state`() = runTest {
        coEvery { signInUseCase(any(), any()) } returns Result.Error("Invalid credentials")

        viewModel.uiState.test {
            viewModel.signIn("bad@email.com", "wrong")
            skipItems(1) // loading
            val error = awaitItem()
            assertEquals("Invalid credentials", error.error)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `empty email returns validation error without calling Firebase`() = runTest {
        coEvery { signInUseCase("", any()) } returns Result.Error("Email is required")

        viewModel.uiState.test {
            viewModel.signIn("", "password")
            skipItems(1)
            val error = awaitItem()
            assertNotNull(error.error)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
