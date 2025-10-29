package com.example.afinal.viewmodel.officer

import android.net.Uri
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class EkycUiState(
    val idFrontUri: Uri? = null,
    val idBackUri: Uri? = null,
    val selfieUri: Uri? = null,
    val isFaceDetected: Boolean = false // Trạng thái để biết có khuôn mặt trong khung hình không
)

class EkycViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(EkycUiState())
    val uiState = _uiState.asStateFlow()

    fun onIdFrontCaptured(uri: Uri?) {
        _uiState.update { it.copy(idFrontUri = uri) }
    }

    fun onIdBackCaptured(uri: Uri?) {
        _uiState.update { it.copy(idBackUri = uri) }
    }

    fun onSelfieCaptured(uri: Uri?) {
        _uiState.update { it.copy(selfieUri = uri) }
    }

    fun onFaceDetectionResult(detected: Boolean) {
        _uiState.update { it.copy(isFaceDetected = detected) }
    }
}