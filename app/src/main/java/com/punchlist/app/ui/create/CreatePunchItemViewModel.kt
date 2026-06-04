package com.punchlist.app.ui.create

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.punchlist.app.data.model.Priority
import com.punchlist.app.data.repository.UserRepository
import com.punchlist.app.domain.usecase.punchitem.CreatePunchItemUseCase
import com.punchlist.app.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CreatePunchItemUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val createdItemId: String? = null,
    val localPhotoUris: List<Uri> = emptyList(),
    val sku: String = ""
)

@HiltViewModel
class CreatePunchItemViewModel @Inject constructor(
    private val createPunchItemUseCase: CreatePunchItemUseCase,
    private val userRepository: UserRepository,
    private val auth: FirebaseAuth,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreatePunchItemUiState())
    val uiState: StateFlow<CreatePunchItemUiState> = _uiState.asStateFlow()

    // Watch for photo URI passed back from CameraScreen via SavedStateHandle
    fun observeCapturedPhoto() {
        savedStateHandle.getLiveData<String>("captured_photo_uri").observeForever { uriString ->
            if (uriString != null) {
                val uri = Uri.parse(uriString)
                if (uri != null && !_uiState.value.localPhotoUris.contains(uri)) {
                    _uiState.value = _uiState.value.copy(
                        localPhotoUris = _uiState.value.localPhotoUris + uri
                    )
                    savedStateHandle.remove<String>("captured_photo_uri")
                }
            }
        }
    }

    // Watch for SKU value passed back from BarcodeScannerScreen via SavedStateHandle
    fun observeScannedSku() {
        savedStateHandle.getLiveData<String>("scanned_sku").observeForever { sku ->
            if (sku != null) {
                _uiState.value = _uiState.value.copy(sku = sku)
                savedStateHandle.remove<String>("scanned_sku")
            }
        }
    }

    fun setSku(value: String) {
        _uiState.value = _uiState.value.copy(sku = value)
    }

    fun removePhoto(uri: Uri) {
        _uiState.value = _uiState.value.copy(
            localPhotoUris = _uiState.value.localPhotoUris - uri
        )
    }

    fun createItem(
        projectId: String,
        title: String,
        issueDescription: String,
        workRequired: String,
        location: String,
        priority: Priority,
        assignedToUserId: String,
        assignedToUserName: String
    ) {
        val uid = auth.currentUser?.uid ?: return
        val displayName = auth.currentUser?.displayName ?: "Unknown"

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            when (val result = createPunchItemUseCase(
                projectId = projectId,
                title = title,
                issueDescription = issueDescription,
                workRequired = workRequired,
                location = location,
                priority = priority,
                assignedToUserId = assignedToUserId,
                assignedToUserName = assignedToUserName,
                createdByUserId = uid,
                createdByUserName = displayName,
                localPhotoUris = _uiState.value.localPhotoUris,
                sku = _uiState.value.sku
            )) {
                is Result.Success -> _uiState.value = CreatePunchItemUiState(createdItemId = result.data)
                is Result.Error -> _uiState.value = _uiState.value.copy(isLoading = false, error = result.message)
                Result.Loading -> {}
            }
        }
    }
}
