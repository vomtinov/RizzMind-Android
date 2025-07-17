package com.rizzmind.app.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.Image
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Handler
import android.os.Looper
import android.util.DisplayMetrics
import android.util.Log
import android.view.WindowManager
import java.nio.ByteBuffer

class ScreenCaptureManager(private val context: Context) {

    private var mediaProjection: MediaProjection? = null
    private var virtualDisplay: VirtualDisplay? = null
    private var imageReader: ImageReader? = null
    
    private val displayMetrics = DisplayMetrics()
    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    
    init {
        windowManager.defaultDisplay.getMetrics(displayMetrics)
    }

    fun setMediaProjection(projection: MediaProjection) {
        mediaProjection = projection
    }

    fun captureScreen(onCaptured: (Bitmap?) -> Unit) {
        if (mediaProjection == null) {
            onCaptured(null)
            return
        }

        try {
            setupImageReader(onCaptured)
            setupVirtualDisplay()
        } catch (e: Exception) {
            Log.e("ScreenCapture", "Error capturing screen", e)
            onCaptured(null)
        }
    }

    private fun setupImageReader(onCaptured: (Bitmap?) -> Unit) {
        imageReader = ImageReader.newInstance(
            displayMetrics.widthPixels,
            displayMetrics.heightPixels,
            PixelFormat.RGBA_8888,
            1
        )

        imageReader?.setOnImageAvailableListener({ reader ->
            try {
                val image = reader.acquireLatestImage()
                if (image != null) {
                    val bitmap = imageToBitmap(image)
                    image.close()
                    
                    // Clean up after capture
                    virtualDisplay?.release()
                    virtualDisplay = null
                    
                    onCaptured(bitmap)
                } else {
                    onCaptured(null)
                }
            } catch (e: Exception) {
                Log.e("ScreenCapture", "Error processing captured image", e)
                onCaptured(null)
            }
        }, Handler(Looper.getMainLooper()))
    }

    private fun setupVirtualDisplay() {
        virtualDisplay = mediaProjection?.createVirtualDisplay(
            "ScreenCapture",
            displayMetrics.widthPixels,
            displayMetrics.heightPixels,
            displayMetrics.densityDpi,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            imageReader?.surface,
            null,
            null
        )
    }

    private fun imageToBitmap(image: Image): Bitmap {
        val planes = image.planes
        val buffer = planes[0].buffer
        val pixelStride = planes[0].pixelStride
        val rowStride = planes[0].rowStride
        val rowPadding = rowStride - pixelStride * displayMetrics.widthPixels

        val bitmap = Bitmap.createBitmap(
            displayMetrics.widthPixels + rowPadding / pixelStride,
            displayMetrics.heightPixels,
            Bitmap.Config.ARGB_8888
        )
        
        bitmap.copyPixelsFromBuffer(buffer)
        
        return if (rowPadding == 0) {
            bitmap
        } else {
            Bitmap.createBitmap(bitmap, 0, 0, displayMetrics.widthPixels, displayMetrics.heightPixels)
        }
    }

    fun release() {
        virtualDisplay?.release()
        imageReader?.close()
        mediaProjection?.stop()
        
        virtualDisplay = null
        imageReader = null
        mediaProjection = null
    }
}