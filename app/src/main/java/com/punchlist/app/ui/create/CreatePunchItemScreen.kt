package com.punchlist.app.ui.create

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.punchlist.app.data.model.Priority
import com.punchlist.app.ui.components.PhotoGrid

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePunchItemScreen(
    projectId: String,
    onItemCreated: () -> Unit,
    onOpenCamera: () -> Unit,
    onBack: () -> Unit,
    viewModel: CreatePunchItemViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var title by remember { mutableStateOf("") }
    var issueDescription by remember { mutableStateOf("") }
    var workRequired by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf(Priority.MEDIUM) }
    var priorityDropdownExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.observeCapturedPhoto()
    }

    LaunchedEffect(uiState.createdItemId) {
        if (uiState.createdItemId != null) onItemCreated()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("New Issue", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Photos section
            Text("Photos", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)

            PhotoGrid(
                photoUrls = uiState.localPhotoUris.map { it.toString() },
                onAddPhoto = onOpenCamera,
                onRemovePhoto = { uriString ->
                    val uri = android.net.Uri.parse(uriString)
                    viewModel.removePhoto(uri)
                }
            )

            HorizontalDivider()

            // Issue details
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title *") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = issueDescription,
                onValueChange = { issueDescription = it },
                label = { Text("Issue Description") },
                maxLines = 4,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = workRequired,
                onValueChange = { workRequired = it },
                label = { Text("Work Required") },
                maxLines = 3,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = location,
                onValueChange = { location = it },
                label = { Text("Location / Area") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // Priority dropdown
            ExposedDropdownMenuBox(
                expanded = priorityDropdownExpanded,
                onExpandedChange = { priorityDropdownExpanded = it }
            ) {
                OutlinedTextField(
                    value = priority.label,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Priority") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = priorityDropdownExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = priorityDropdownExpanded,
                    onDismissRequest = { priorityDropdownExpanded = false }
                ) {
                    Priority.entries.forEach { p ->
                        DropdownMenuItem(
                            text = { Text(p.label) },
                            onClick = {
                                priority = p
                                priorityDropdownExpanded = false
                            }
                        )
                    }
                }
            }

            uiState.error?.let {
                Text(text = it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    viewModel.createItem(
                        projectId = projectId,
                        title = title,
                        issueDescription = issueDescription,
                        workRequired = workRequired,
                        location = location,
                        priority = priority,
                        assignedToUserId = "",
                        assignedToUserName = ""
                    )
                },
                enabled = title.isNotBlank() && !uiState.isLoading,
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("Submit Issue", fontSize = 16.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
