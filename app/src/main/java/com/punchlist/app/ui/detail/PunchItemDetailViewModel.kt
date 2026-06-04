package com.punchlist.app.ui.detail

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.punchlist.app.data.model.Comment
import com.punchlist.app.data.model.PunchItem
import com.punchlist.app.data.model.Status
import com.punchlist.app.domain.usecase.comment.AddCommentUseCase
import com.punchlist.app.domain.usecase.comment.GetCommentsUseCase
import com.punchlist.app.domain.usecase.punchitem.GetPunchItemDetailUseCase
import com.punchlist.app.domain.usecase.punchitem.UpdatePunchItemUseCase
import com.punchlist.app.domain.usecase.punchitem.UpdateStatusUseCase
import com.punchlist.app.domain.usecase.storage.UploadPhotoUseCase
import com.punchlist.app.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DetailUiState(
    val item: PunchItem? = null,
    val comments: List<Comment> = emptyList(),
    val projectName: String = "",
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class PunchItemDetailViewModel @Inject constructor(
    private val getPunchItemDetailUseCase: GetPunchItemDetailUseCase,
    private val updateStatusUseCase: UpdateStatusUseCase,
    private val updatePunchItemUseCase: UpdatePunchItemUseCase,
    private val getCommentsUseCase: GetCommentsUseCase,
    private val addCommentUseCase: AddCommentUseCase,
    private val uploadPhotoUseCase: UploadPhotoUseCase,
    private val auth: FirebaseAuth,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(DetailUiState())
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    fun load(projectId: String, itemId: String, projectName: String = "") {
        _uiState.value = _uiState.value.copy(projectName = projectName)
        viewModelScope.launch {
            when (val result = getPunchItemDetailUseCase(projectId, itemId)) {
                is Result.Success -> _uiState.value = _uiState.value.copy(item = result.data, isLoading = false)
                is Result.Error -> _uiState.value = _uiState.value.copy(error = result.message, isLoading = false)
                Result.Loading -> {}
            }
        }

        viewModelScope.launch {
            getCommentsUseCase(projectId, itemId)
                .catch { _uiState.value = _uiState.value.copy(error = it.message) }
                .collect { _uiState.value = _uiState.value.copy(comments = it) }
        }
    }

    fun updateStatus(projectId: String, itemId: String, status: Status) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true)
            when (val result = updateStatusUseCase(projectId, itemId, status)) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isSaving = false,
                        item = _uiState.value.item?.copy(status = status)
                    )
                }
                is Result.Error -> _uiState.value = _uiState.value.copy(isSaving = false, error = result.message)
                Result.Loading -> {}
            }
        }
    }

    fun addComment(projectId: String, itemId: String, text: String) {
        val uid = auth.currentUser?.uid ?: return
        val name = auth.currentUser?.displayName ?: "Unknown"
        viewModelScope.launch {
            val comment = Comment(itemId = itemId, userId = uid, userName = name, text = text)
            addCommentUseCase(projectId, comment)
        }
    }

    fun addCompletionPhoto(projectId: String, itemId: String, uri: Uri) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true)
            when (val result = uploadPhotoUseCase(uri, projectId, itemId, isCompletion = true)) {
                is Result.Success -> {
                    val current = _uiState.value.item ?: return@launch
                    val updated = current.copy(completionPhotoUrls = current.completionPhotoUrls + result.data)
                    updatePunchItemUseCase(updated)
                    _uiState.value = _uiState.value.copy(isSaving = false, item = updated)
                }
                is Result.Error -> _uiState.value = _uiState.value.copy(isSaving = false, error = result.message)
                Result.Loading -> {}
            }
        }
    }

    fun observeCompletionPhoto(projectId: String, itemId: String) {
        savedStateHandle.getLiveData<String>("captured_photo_uri").observeForever { uriString ->
            if (uriString != null) {
                addCompletionPhoto(projectId, itemId, Uri.parse(uriString))
                savedStateHandle.remove<String>("captured_photo_uri")
            }
        }
    }
}
