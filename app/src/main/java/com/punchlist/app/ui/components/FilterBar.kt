package com.punchlist.app.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.punchlist.app.data.model.Priority
import com.punchlist.app.data.model.Status
import com.punchlist.app.ui.feed.FeedFilterState

@Composable
fun FilterBar(
    filterState: FeedFilterState,
    onStatusFilter: (Status?) -> Unit,
    onPriorityFilter: (Priority?) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            Icons.Default.FilterList,
            contentDescription = "Filter",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp)
        )

        // Status filters
        Status.entries.forEach { status ->
            val selected = filterState.statusFilter == status
            FilterChip(
                selected = selected,
                onClick = { onStatusFilter(if (selected) null else status) },
                label = { Text(status.label, style = MaterialTheme.typography.labelSmall) }
            )
        }

        Spacer(modifier = Modifier.width(4.dp))
        Divider(modifier = Modifier.height(24.dp).width(1.dp))
        Spacer(modifier = Modifier.width(4.dp))

        // Priority filters
        Priority.entries.forEach { priority ->
            val selected = filterState.priorityFilter == priority
            FilterChip(
                selected = selected,
                onClick = { onPriorityFilter(if (selected) null else priority) },
                label = { Text(priority.label, style = MaterialTheme.typography.labelSmall) }
            )
        }
    }
}
