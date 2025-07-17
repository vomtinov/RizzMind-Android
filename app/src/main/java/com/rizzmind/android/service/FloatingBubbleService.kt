package com.rizzmind.android.service

import android.app.*
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.*
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.rizzmind.android.R
import com.rizzmind.android.suggestor.Suggestion
import com.rizzmind.android.suggestor.SuggestionEngine
import com.rizzmind.android.utils.TimeUtil
import kotlinx.coroutines.*

class FloatingBubbleService : Service() {
    private var windowManager: WindowManager? = null
    private var floatingView: View? = null
    private var suggestionEngine: SuggestionEngine? = null
    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    
    companion object {
        private const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "floating_bubble_channel"
    }
    
    override fun onCreate() {
        super.onCreate()
        suggestionEngine = SuggestionEngine(this)
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification())
        createFloatingBubble()
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        removeFloatingBubble()
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Floating Bubble Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Keeps the floating bubble running"
                setShowBadge(false)
            }
            
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("RizzMind Active")
            .setContentText("Tap the bubble for smart suggestions")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(true)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }
    
    private fun createFloatingBubble() {
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        
        // Create the floating bubble view
        floatingView = LayoutInflater.from(this).inflate(R.layout.floating_bubble, null)
        
        val layoutFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            WindowManager.LayoutParams.TYPE_PHONE
        }
        
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutFlag,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 100
            y = 100
        }
        
        // Set up click listener for the bubble
        val bubble = floatingView?.findViewById<FloatingActionButton>(R.id.floating_bubble)
        bubble?.setOnClickListener {
            showSuggestionBottomSheet()
        }
        
        // Add drag functionality
        setupDragFunctionality(params)
        
        try {
            windowManager?.addView(floatingView, params)
        } catch (e: Exception) {
            // Handle permission issues
            Toast.makeText(this, "Permission required for floating bubble", Toast.LENGTH_LONG).show()
        }
    }
    
    private fun setupDragFunctionality(params: WindowManager.LayoutParams) {
        floatingView?.setOnTouchListener(object : View.OnTouchListener {
            private var initialX = 0
            private var initialY = 0
            private var initialTouchX = 0f
            private var initialTouchY = 0f
            
            override fun onTouch(v: View, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        initialX = params.x
                        initialY = params.y
                        initialTouchX = event.rawX
                        initialTouchY = event.rawY
                        return true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        params.x = initialX + (event.rawX - initialTouchX).toInt()
                        params.y = initialY + (event.rawY - initialTouchY).toInt()
                        windowManager?.updateViewLayout(floatingView, params)
                        return true
                    }
                    MotionEvent.ACTION_UP -> {
                        val deltaX = Math.abs(event.rawX - initialTouchX)
                        val deltaY = Math.abs(event.rawY - initialTouchY)
                        
                        // If it's a tap (small movement), trigger click
                        if (deltaX < 10 && deltaY < 10) {
                            v.performClick()
                        }
                        return true
                    }
                }
                return false
            }
        })
    }
    
    private fun showSuggestionBottomSheet() {
        serviceScope.launch {
            try {
                // Simulate OCR text extraction - in real implementation, this would come from actual OCR
                val mockOcrText = "Hey! What's up? 😄"
                val personIdentifier = "current_chat" // This would be dynamically determined
                val currentTimestamp = TimeUtil.getCurrentTimestamp()
                
                val suggestions = suggestionEngine?.generateSuggestions(
                    mockOcrText,
                    personIdentifier,
                    currentTimestamp
                ) ?: emptyList()
                
                withContext(Dispatchers.Main) {
                    showBottomSheetWithSuggestions(suggestions)
                }
                
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showErrorBottomSheet("Failed to generate suggestions")
                }
            }
        }
    }
    
    private fun showBottomSheetWithSuggestions(suggestions: List<Suggestion>) {
        val dialog = BottomSheetDialog(this, R.style.BottomSheetDialogTheme)
        val view = LayoutInflater.from(this).inflate(R.layout.suggestions_bottom_sheet, null)
        
        val recyclerView = view.findViewById<RecyclerView>(R.id.suggestions_recycler_view)
        val closeButton = view.findViewById<MaterialButton>(R.id.close_button)
        
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = SuggestionsAdapter(suggestions) { suggestion ->
            copyToClipboard(suggestion.text)
            dialog.dismiss()
            Toast.makeText(this, "Copied: ${suggestion.text}", Toast.LENGTH_SHORT).show()
        }
        
        closeButton.setOnClickListener {
            dialog.dismiss()
        }
        
        dialog.setContentView(view)
        
        // Make dialog appear as system alert
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            dialog.window?.setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY)
        } else {
            dialog.window?.setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT)
        }
        
        dialog.show()
    }
    
    private fun showErrorBottomSheet(errorMessage: String) {
        val dialog = BottomSheetDialog(this, R.style.BottomSheetDialogTheme)
        val view = LayoutInflater.from(this).inflate(R.layout.error_bottom_sheet, null)
        
        val closeButton = view.findViewById<MaterialButton>(R.id.close_button)
        
        closeButton.setOnClickListener {
            dialog.dismiss()
        }
        
        dialog.setContentView(view)
        dialog.show()
    }
    
    private fun copyToClipboard(text: String) {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Suggestion", text)
        clipboard.setPrimaryClip(clip)
    }
    
    private fun removeFloatingBubble() {
        floatingView?.let { view ->
            windowManager?.removeView(view)
        }
        floatingView = null
    }
}