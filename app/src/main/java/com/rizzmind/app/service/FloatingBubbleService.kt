package com.rizzmind.app.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.rizzmind.app.R
import com.rizzmind.app.data.repository.OCRRepository
import com.rizzmind.app.databinding.FloatingBubbleBinding
import com.rizzmind.app.utils.ScreenCaptureManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class FloatingBubbleService : Service() {

    private lateinit var windowManager: WindowManager
    private lateinit var floatingView: View
    private lateinit var layoutParams: WindowManager.LayoutParams
    private lateinit var binding: FloatingBubbleBinding
    
    private lateinit var screenCaptureManager: ScreenCaptureManager
    private lateinit var ocrRepository: OCRRepository
    
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    private var mediaProjection: MediaProjection? = null

    companion object {
        const val CHANNEL_ID = "floating_bubble_channel"
        const val NOTIFICATION_ID = 1
        const val EXTRA_MEDIA_PROJECTION_DATA = "media_projection_data"
        const val EXTRA_RESULT_CODE = "result_code"
    }

    override fun onCreate() {
        super.onCreate()
        
        screenCaptureManager = ScreenCaptureManager(this)
        ocrRepository = OCRRepository()
        
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification())
        
        setupFloatingBubble()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            val resultCode = it.getIntExtra(EXTRA_RESULT_CODE, -1)
            val data = it.getParcelableExtra<Intent>(EXTRA_MEDIA_PROJECTION_DATA)
            
            if (resultCode != -1 && data != null) {
                val projectionManager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
                mediaProjection = projectionManager.getMediaProjection(resultCode, data)
                screenCaptureManager.setMediaProjection(mediaProjection!!)
            }
        }
        
        return START_STICKY
    }

    private fun setupFloatingBubble() {
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        
        binding = FloatingBubbleBinding.inflate(LayoutInflater.from(this))
        floatingView = binding.root

        layoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams.TYPE_PHONE
            },
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 100
            y = 100
        }

        setupBubbleClickListener()
        setupBubbleDragListener()
        
        windowManager.addView(floatingView, layoutParams)
    }

    private fun setupBubbleClickListener() {
        binding.root.setOnClickListener {
            performOCR()
        }
    }

    private fun setupBubbleDragListener() {
        var initialX = 0
        var initialY = 0
        var initialTouchX = 0f
        var initialTouchY = 0f
        var isDragging = false

        binding.root.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = layoutParams.x
                    initialY = layoutParams.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    isDragging = false
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val deltaX = event.rawX - initialTouchX
                    val deltaY = event.rawY - initialTouchY
                    
                    if (!isDragging && (Math.abs(deltaX) > 10 || Math.abs(deltaY) > 10)) {
                        isDragging = true
                    }
                    
                    if (isDragging) {
                        layoutParams.x = initialX + deltaX.toInt()
                        layoutParams.y = initialY + deltaY.toInt()
                        windowManager.updateViewLayout(floatingView, layoutParams)
                    }
                    true
                }
                MotionEvent.ACTION_UP -> {
                    if (!isDragging) {
                        binding.root.performClick()
                    }
                    false
                }
                else -> false
            }
        }
    }

    private fun performOCR() {
        if (mediaProjection == null) {
            Toast.makeText(this, "Screen capture permission not granted", Toast.LENGTH_SHORT).show()
            return
        }

        Toast.makeText(this, getString(R.string.ocr_processing), Toast.LENGTH_SHORT).show()
        
        screenCaptureManager.captureScreen { bitmap ->
            if (bitmap != null) {
                serviceScope.launch {
                    val result = ocrRepository.extractTextFromBitmap(bitmap)
                    result.fold(
                        onSuccess = { text ->
                            Toast.makeText(this@FloatingBubbleService, 
                                "OCR Result: ${text.take(100)}${if (text.length > 100) "..." else ""}", 
                                Toast.LENGTH_LONG).show()
                        },
                        onFailure = { 
                            Toast.makeText(this@FloatingBubbleService, 
                                getString(R.string.ocr_no_text), 
                                Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            } else {
                Toast.makeText(this@FloatingBubbleService, 
                    "Failed to capture screen", 
                    Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Floating Bubble Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Service for floating OCR bubble"
            }
            
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("RizzMind OCR")
            .setContentText("Floating bubble is active")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(true)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::floatingView.isInitialized) {
            windowManager.removeView(floatingView)
        }
        screenCaptureManager.release()
        ocrRepository.closeRecognizer()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}