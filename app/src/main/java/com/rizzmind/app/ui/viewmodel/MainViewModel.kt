package com.rizzmind.app.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rizzmind.app.data.repository.OCRRepository
import kotlinx.coroutines.launch

class MainViewModel(
    private val ocrRepository: OCRRepository
) : ViewModel() {

    private val _serviceRunning = MutableLiveData<Boolean>()
    val serviceRunning: LiveData<Boolean> = _serviceRunning

    private val _permissionGranted = MutableLiveData<Boolean>()
    val permissionGranted: LiveData<Boolean> = _permissionGranted

    private val _ocrResult = MutableLiveData<String>()
    val ocrResult: LiveData<String> = _ocrResult

    private val _isProcessing = MutableLiveData<Boolean>()
    val isProcessing: LiveData<Boolean> = _isProcessing

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    init {
        _serviceRunning.value = false
        _permissionGranted.value = false
        _isProcessing.value = false
    }

    fun setServiceRunning(running: Boolean) {
        _serviceRunning.value = running
    }

    fun setPermissionGranted(granted: Boolean) {
        _permissionGranted.value = granted
    }

    fun processOCR(bitmap: android.graphics.Bitmap) {
        viewModelScope.launch {
            _isProcessing.value = true
            try {
                val result = ocrRepository.extractTextFromBitmap(bitmap)
                result.fold(
                    onSuccess = { text ->
                        _ocrResult.value = text
                    },
                    onFailure = { error ->
                        _errorMessage.value = error.message ?: "OCR processing failed"
                    }
                )
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Unexpected error occurred"
            } finally {
                _isProcessing.value = false
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        ocrRepository.closeRecognizer()
    }
}