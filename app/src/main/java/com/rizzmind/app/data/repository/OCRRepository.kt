package com.rizzmind.app.data.repository

import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.tasks.await

class OCRRepository {

    private val textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    suspend fun extractTextFromBitmap(bitmap: Bitmap): Result<String> {
        return try {
            val image = InputImage.fromBitmap(bitmap, 0)
            val result = textRecognizer.process(image).await()
            val extractedText = result.text
            
            if (extractedText.isNotBlank()) {
                Result.success(extractedText)
            } else {
                Result.failure(Exception("No text found in image"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun closeRecognizer() {
        textRecognizer.close()
    }
}