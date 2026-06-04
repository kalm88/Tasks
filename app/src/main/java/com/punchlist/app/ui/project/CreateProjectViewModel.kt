package com.punchlist.app.ui.project

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.punchlist.app.domain.usecase.project.CreateProjectUseCase
import com.punchlist.app.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CreateProjectUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val createdProjectId: String? = null
)

@HiltViewModel
class CreateProjectViewModel @Inject constructor(
    private val createProjectUseCase: CreateProjectUseCase,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateProjectUiState())
    val uiState: StateFlow<CreateProjectUiState> = _uiState.asStateFlow()

    fun createProject(name: String, description: String) {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            _uiState.value = CreateProjectUiState(isLoading = true)
            when (val result = createProjectUseCase(name, description, uid)) {
                is Result.Success -> _uiState.value = CreateProjectUiState(createdProjectId = result.data)
                is Result.Error -> _uiState.value = CreateProjectUiState(error = result.message)
                Result.Loading -> {}
            }
        }
    }
}
