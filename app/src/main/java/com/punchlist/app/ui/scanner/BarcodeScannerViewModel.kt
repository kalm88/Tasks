package com.punchlist.app.ui.scanner

import androidx.annotation.OptIn
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class ScannerUiState(
    val scannedValue: String? = null,
    val isScanning: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class BarcodeScannerViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(ScannerUiState())
    val uiState: StateFlow<ScannerUiState> = _uiState.asStateFlow()

    // Prevent firing the callback multiple times for the same scan
    private var hasCaptured = false

    fun bindScanner(
        context: android.content.Context,
        lifecycleOwner: LifecycleOwner,
        previewView: PreviewView,
        onScanned: (String) -> Unit
    ) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().apply {
                surfaceProvider = previewView.surfaceProvider
            }

            val barcodeScanner = BarcodeScanning.getClient()

            val analyzer = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()

            analyzer.setAnalyzer(ContextCompat.getMainExecutor(context)) { imageProxy ->
                processImage(imageProxy, barcodeScanner, onScanned)
            }

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    analyzer
                )
            } catch (e: Exception) {
                _uiState.value = ScannerUiState(error = "Camera error: ${e.message}", isScanning = false)
            }
        }, ContextCompat.getMainExecutor(context))
    }

    @OptIn(ExperimentalGetImage::class)
    private fun processImage(
        imageProxy: ImageProxy,
        scanner: com.google.mlkit.vision.barcode.BarcodeScanner,
        onScanned: (String) -> Unit
    ) {
        val mediaImage = imageProxy.image
        if (mediaImage == null || hasCaptured) {
            imageProxy.close()
            return
        }

        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
        scanner.process(image)
            .addOnSuccessListener { barcodes ->
                val value = barcodes
                    .firstOrNull { it.valueType == Barcode.TYPE_PRODUCT || it.rawValue != null }
                    ?.rawValue
                if (value != null && !hasCaptured) {
                    hasCaptured = true
                    _uiState.value = ScannerUiState(scannedValue = value, isScanning = false)
                    onScanned(value)
                }
            }
            .addOnCompleteListener { imageProxy.close() }
    }

    fun reset() {
        hasCaptured = false
        _uiState.value = ScannerUiState(isScanning = true)
    }
}
