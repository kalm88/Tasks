package com.punchlist.app.ui.feed

import com.punchlist.app.data.model.Priority
import com.punchlist.app.data.model.PunchItem
import com.punchlist.app.data.model.Status

data class FeedFilterState(
    val searchQuery: String = "",
    val statusFilter: Status? = null,
    val priorityFilter: Priority? = null,
    val assigneeFilter: String? = null
) {
    fun apply(items: List<PunchItem>): List<PunchItem> = items
        .filter { item ->
            (statusFilter == null || item.status == statusFilter) &&
            (priorityFilter == null || item.priority == priorityFilter) &&
            (assigneeFilter == null || item.assignedToUserId == assigneeFilter) &&
            (searchQuery.isBlank() || item.title.contains(searchQuery, ignoreCase = true) ||
                item.issueDescription.contains(searchQuery, ignoreCase = true) ||
                item.location.contains(searchQuery, ignoreCase = true))
        }

    val isActive: Boolean get() = statusFilter != null || priorityFilter != null || assigneeFilter != null || searchQuery.isNotBlank()
}
