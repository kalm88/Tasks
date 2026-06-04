package com.punchlist.app.ui.scanner

import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun BarcodeScannerScreen(
    onScanned: (String) -> Unit,
    onBack: () -> Unit,
    viewModel: BarcodeScannerViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val cameraPermission = rememberPermissionState(android.Manifest.permission.CAMERA)

    // Propagate confirmed scan back to caller
    LaunchedEffect(uiState.scannedValue) {
        uiState.scannedValue?.let { onScanned(it) }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        if (!cameraPermission.status.isGranted) {
            Column(
                modifier = Modifier.fillMaxSize().padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = if (cameraPermission.status.shouldShowRationale)
                        "Camera access is needed to scan barcodes and QR codes."
                    else "Camera permission required to scan SKUs.",
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { cameraPermission.launchPermissionRequest() }) {
                    Text("Grant Permission")
                }
                TextButton(onClick = onBack) {
                    Text("Go Back", color = Color.White)
                }
            }
        } else {
            // Camera preview
            AndroidView(
                factory = { ctx ->
                    PreviewView(ctx).also { preview ->
                        viewModel.bindScanner(ctx, lifecycleOwner, preview, onScanned = { _ ->
                            // Handled via uiState LaunchedEffect above
                        })
                    }
                },
                modifier = Modifier.fillMaxSize()
            )

            // Scanning overlay
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.background(Color.Black.copy(alpha = 0.5f), CircleShape)
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Scan SKU Barcode",
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 18.sp
                    )
                }

                // Viewfinder reticle
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    // Semi-dark overlay with a clear window
                    Box(
                        modifier = Modifier
                            .size(260.dp, 160.dp)
                            .border(
                                width = 3.dp,
                                color = if (uiState.scannedValue != null) Color(0xFF4CAF50) else Color.White,
                                shape = RoundedCornerShape(12.dp)
                            )
                    ) {
                        // Corner accents
                        CornerAccent(Alignment.TopStart)
                        CornerAccent(Alignment.TopEnd)
                        CornerAccent(Alignment.BottomStart)
                        CornerAccent(Alignment.BottomEnd)
                    }
                }

                // Bottom hint
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp, vertical = 48.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (uiState.scannedValue != null) {
                        Surface(
                            color = Color(0xFF4CAF50).copy(alpha = 0.9f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "Scanned: ${uiState.scannedValue}",
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                    } else {
                        Text(
                            text = "Point the camera at a barcode or QR code",
                            color = Color.White.copy(alpha = 0.85f),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Supports UPC, EAN, QR, Code 128 and more",
                            color = Color.White.copy(alpha = 0.55f),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }

            uiState.error?.let {
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                ) { Text(it) }
            }
        }
    }
}

@Composable
private fun BoxScope.CornerAccent(alignment: Alignment) {
    // Draws a small L-shaped corner marker inside the reticle box
    Box(
        modifier = Modifier
            .size(20.dp)
            .align(alignment)
    )
}
