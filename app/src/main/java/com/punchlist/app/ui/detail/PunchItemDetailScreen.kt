package com.punchlist.app.ui.detail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.punchlist.app.data.model.Status
import com.punchlist.app.ui.components.CommentItem
import com.punchlist.app.ui.components.PhotoGrid
import com.punchlist.app.ui.components.PriorityChip
import com.punchlist.app.ui.components.StatusChip
import com.punchlist.app.util.PrintShareHelper
import com.punchlist.app.util.toDisplayDate
import com.punchlist.app.util.toRelativeString

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PunchItemDetailScreen(
    projectId: String,
    itemId: String,
    projectName: String = "",
    onBack: () -> Unit,
    onOpenCamera: () -> Unit,
    viewModel: PunchItemDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var commentText by remember { mutableStateOf("") }
    var statusDropdownExpanded by remember { mutableStateOf(false) }
    var shareMenuExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(projectId, itemId) {
        viewModel.load(projectId, itemId, projectName)
        viewModel.observeCompletionPhoto(projectId, itemId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.item?.title ?: "Issue Detail", maxLines = 1) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Print button
                    IconButton(
                        onClick = {
                            uiState.item?.let { item ->
                                PrintShareHelper.print(context, item, uiState.comments, uiState.projectName)
                            }
                        },
                        enabled = uiState.item != null
                    ) {
                        Icon(Icons.Default.Print, contentDescription = "Print")
                    }

                    // Share dropdown
                    Box {
                        IconButton(
                            onClick = { shareMenuExpanded = true },
                            enabled = uiState.item != null
                        ) {
                            Icon(Icons.Default.Share, contentDescription = "Share")
                        }
                        DropdownMenu(
                            expanded = shareMenuExpanded,
                            onDismissRequest = { shareMenuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Share PDF") },
                                leadingIcon = { Icon(Icons.Default.Share, contentDescription = null) },
                                onClick = {
                                    shareMenuExpanded = false
                                    uiState.item?.let { item ->
                                        PrintShareHelper.sharePdf(context, item, uiState.comments, uiState.projectName)
                                    }
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Send via Email") },
                                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                                onClick = {
                                    shareMenuExpanded = false
                                    uiState.item?.let { item ->
                                        PrintShareHelper.shareViaEmail(context, item, uiState.comments, uiState.projectName)
                                    }
                                }
                            )
                        }
                    }
                }
            )
        },
        bottomBar = {
            // Comment input fixed at bottom
            Surface(shadowElevation = 8.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .navigationBarsPadding()
                        .imePadding(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = commentText,
                        onValueChange = { commentText = it },
                        placeholder = { Text("Add a comment…") },
                        modifier = Modifier.weight(1f),
                        maxLines = 3
                    )
                    IconButton(
                        onClick = {
                            if (commentText.isNotBlank()) {
                                viewModel.addComment(projectId, itemId, commentText)
                                commentText = ""
                            }
                        },
                        enabled = commentText.isNotBlank()
                    ) {
                        Icon(
                            Icons.Default.Send,
                            contentDescription = "Send",
                            tint = if (commentText.isNotBlank()) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        val item = uiState.item ?: return@Scaffold

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header chips
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatusChip(status = item.status)
                    PriorityChip(priority = item.priority)
                }
            }

            // Status change
            item {
                ExposedDropdownMenuBox(
                    expanded = statusDropdownExpanded,
                    onExpandedChange = { statusDropdownExpanded = it }
                ) {
                    OutlinedTextField(
                        value = item.status.label,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Change Status") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = statusDropdownExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = statusDropdownExpanded,
                        onDismissRequest = { statusDropdownExpanded = false }
                    ) {
                        Status.entries.forEach { s ->
                            DropdownMenuItem(
                                text = { Text(s.label) },
                                onClick = {
                                    viewModel.updateStatus(projectId, itemId, s)
                                    statusDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            // Issue details section
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        if (item.issueDescription.isNotBlank()) {
                            DetailRow(label = "Issue", value = item.issueDescription)
                        }
                        if (item.workRequired.isNotBlank()) {
                            DetailRow(label = "Work Required", value = item.workRequired)
                        }
                        if (item.location.isNotBlank()) {
                            DetailRow(label = "Location", value = item.location)
                        }
                        if (item.sku.isNotBlank()) {
                            DetailRow(label = "SKU", value = item.sku)
                        }
                        if (item.assignedToUserName.isNotBlank()) {
                            DetailRow(label = "Assigned To", value = item.assignedToUserName)
                        }
                        DetailRow(label = "Created By", value = item.createdByUserName)
                        item.createdAt?.let {
                            DetailRow(label = "Created", value = it.toDisplayDate())
                        }
                        item.dueDate?.let {
                            DetailRow(label = "Due", value = it.toDisplayDate())
                        }
                    }
                }
            }

            // Issue photos
            if (item.photoUrls.isNotEmpty()) {
                item {
                    Text("Photos", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(8.dp))
                    PhotoGrid(photoUrls = item.photoUrls)
                }
            }

            // Completion section
            item {
                Text("Completion", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(8.dp))
                if (item.completionPhotoUrls.isNotEmpty()) {
                    PhotoGrid(photoUrls = item.completionPhotoUrls, onAddPhoto = onOpenCamera)
                } else {
                    OutlinedButton(
                        onClick = onOpenCamera,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Add Completion Photo")
                    }
                }

                if (item.status != Status.COMPLETE) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { viewModel.updateStatus(projectId, itemId, Status.COMPLETE) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Status.COMPLETE.color)
                    ) {
                        Text("Mark Complete")
                    }
                }
            }

            // Comments header
            item {
                HorizontalDivider()
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Comments (${uiState.comments.size})",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Comments list
            items(uiState.comments, key = { it.id }) { comment ->
                CommentItem(comment = comment)
            }

            // Bottom spacer for the fixed comment bar
            item { Spacer(modifier = Modifier.height(72.dp)) }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(text = value, style = MaterialTheme.typography.bodyMedium)
    }
}
