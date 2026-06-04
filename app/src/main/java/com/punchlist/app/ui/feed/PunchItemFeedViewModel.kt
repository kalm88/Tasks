package com.punchlist.app.ui.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.punchlist.app.data.model.Priority
import com.punchlist.app.data.model.PunchItem
import com.punchlist.app.data.model.Status
import com.punchlist.app.domain.usecase.punchitem.GetPunchItemsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class FeedUiState {
    object Loading : FeedUiState()
    data class Success(val items: List<PunchItem>, val filterState: FeedFilterState) : FeedUiState()
    data class Error(val message: String) : FeedUiState()
}

@HiltViewModel
class PunchItemFeedViewModel @Inject constructor(
    private val getPunchItemsUseCase: GetPunchItemsUseCase
) : ViewModel() {

    private val _allItems = MutableStateFlow<List<PunchItem>>(emptyList())
    private val _filterState = MutableStateFlow(FeedFilterState())
    private val _isLoading = MutableStateFlow(true)
    private val _error = MutableStateFlow<String?>(null)

    val uiState: StateFlow<FeedUiState> = combine(
        _allItems, _filterState, _isLoading, _error
    ) { items, filter, loading, error ->
        when {
            error != null -> FeedUiState.Error(error)
            loading && items.isEmpty() -> FeedUiState.Loading
            else -> FeedUiState.Success(filter.apply(items), filter)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FeedUiState.Loading)

    fun loadItems(projectId: String) {
        viewModelScope.launch {
            getPunchItemsUseCase(projectId)
                .catch { _error.value = it.message ?: "Failed to load items" }
                .collect {
                    _allItems.value = it
                    _isLoading.value = false
                }
        }
    }

    fun setSearchQuery(query: String) {
        _filterState.value = _filterState.value.copy(searchQuery = query)
    }

    fun setStatusFilter(status: Status?) {
        _filterState.value = _filterState.value.copy(statusFilter = status)
    }

    fun setPriorityFilter(priority: Priority?) {
        _filterState.value = _filterState.value.copy(priorityFilter = priority)
    }

    fun clearFilters() {
        _filterState.value = FeedFilterState()
    }
}
