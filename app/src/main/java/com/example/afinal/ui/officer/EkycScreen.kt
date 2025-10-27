package com.example.afinal.ui.officer

import android.Manifest
import android.content.Context
import android.net.Uri
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import coil.compose.rememberAsyncImagePainter
import com.example.afinal.viewmodel.officer.EkycViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executor

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun EkycScreen(viewModel: EkycViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)

    // Yêu cầu quyền camera khi cần
    LaunchedEffect(Unit) {
        if (!cameraPermissionState.status.isGranted) {
            cameraPermissionState.launchPermissionRequest()
        }
    }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Quy trình eKYC", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(24.dp))

        if (cameraPermissionState.status.isGranted) {
            if (uiState.selfieUri == null) {
                // Hiển thị camera để chụp ảnh
                CameraCaptureView(
                    isFaceDetected = uiState.isFaceDetected,
                    onFaceDetected = viewModel::onFaceDetectionResult,
                    onImageCaptured = viewModel::onSelfieCaptured
                )
            } else {
                // Hiển thị ảnh đã chụp
                Image(
                    painter = rememberAsyncImagePainter(uiState.selfieUri),
                    contentDescription = "Ảnh chân dung",
                    modifier = Modifier.fillMaxWidth().height(300.dp)
                )
            }
        } else {
            Text("Vui lòng cấp quyền camera để sử dụng tính năng này.")
        }
    }
}

@Composable
fun CameraCaptureView(
    isFaceDetected: Boolean,
    onFaceDetected: (Boolean) -> Unit,
    onImageCaptured: (Uri) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    var imageCapture: ImageCapture? by remember { mutableStateOf(null) }

    Box(contentAlignment = Alignment.BottomCenter, modifier = Modifier.fillMaxWidth().height(400.dp)) {
        AndroidView(
            factory = { ctx ->
                val previewView = PreviewView(ctx)
                val executor = ContextCompat.getMainExecutor(ctx)
                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()
                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }
                    imageCapture = ImageCapture.Builder().build()
                    val cameraSelector = CameraSelector.Builder()
                        .requireLensFacing(CameraSelector.LENS_FACING_FRONT)
                        .build()

                    // Thiết lập Face Detector
                    val faceDetector = FaceDetection.getClient(
                        FaceDetectorOptions.Builder()
                            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
                            .build()
                    )
                    val imageAnalyzer = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()
                        .also {
                            it.setAnalyzer(executor, FaceAnalyzer(faceDetector, onFaceDetected))
                        }

                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview,
                        imageCapture,
                        imageAnalyzer
                    )
                }, executor)
                previewView
            },
            modifier = Modifier.fillMaxSize()
        )

        Button(
            onClick = {
                imageCapture?.let { capture ->
                    val photoFile = createImageFile(context)
                    val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
                    capture.takePicture(outputOptions, ContextCompat.getMainExecutor(context),
                        object : ImageCapture.OnImageSavedCallback {
                            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                                onImageCaptured(output.savedUri ?: Uri.fromFile(photoFile))
                            }
                            override fun onError(exc: ImageCaptureException) {
                                // Xử lý lỗi
                            }
                        })
                }
            },
            enabled = isFaceDetected // Chỉ cho phép chụp khi có khuôn mặt
        ) {
            Text(if (isFaceDetected) "Chụp ảnh" else "Đưa khuôn mặt vào khung hình")
        }
    }
}

// Lớp phân tích hình ảnh để phát hiện khuôn mặt
private class FaceAnalyzer(
    private val detector: com.google.mlkit.vision.face.FaceDetector,
    private val onFaceDetected: (Boolean) -> Unit
) : ImageAnalysis.Analyzer {
    @androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            detector.process(image)
                .addOnSuccessListener { faces ->
                    onFaceDetected(faces.isNotEmpty())
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        }
    }
}

// Hàm tiện ích tạo file ảnh
private fun createImageFile(context: Context): File {
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
    return File(context.filesDir, "JPEG_${timeStamp}_.jpg")
}