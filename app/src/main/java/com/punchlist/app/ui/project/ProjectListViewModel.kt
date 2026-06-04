package com.punchlist.app.ui.project

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.punchlist.app.data.model.Project
import com.punchlist.app.domain.usecase.auth.SignOutUseCase
import com.punchlist.app.domain.usecase.project.GetProjectsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class ProjectListUiState {
    object Loading : ProjectListUiState()
    data class Success(val projects: List<Project>) : ProjectListUiState()
    data class Error(val message: String) : ProjectListUiState()
}

@HiltViewModel
class ProjectListViewModel @Inject constructor(
    private val getProjectsUseCase: GetProjectsUseCase,
    private val signOutUseCase: SignOutUseCase,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProjectListUiState>(ProjectListUiState.Loading)
    val uiState: StateFlow<ProjectListUiState> = _uiState.asStateFlow()

    init {
        loadProjects()
    }

    private fun loadProjects() {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            getProjectsUseCase(uid)
                .catch { _uiState.value = ProjectListUiState.Error(it.message ?: "Failed to load projects") }
                .collect { _uiState.value = ProjectListUiState.Success(it) }
        }
    }

    fun signOut(onComplete: () -> Unit) {
        viewModelScope.launch {
            signOutUseCase()
            onComplete()
        }
    }
}
