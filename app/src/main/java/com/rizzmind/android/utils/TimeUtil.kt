package com.rizzmind.android.utils

import java.text.SimpleDateFormat
import java.util.*

object TimeUtil {
    private val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
    private val dateTimeFormat = SimpleDateFormat("MMM dd, h:mm a", Locale.getDefault())
    
    fun getCurrentTimestamp(): Long = System.currentTimeMillis()
    
    fun formatTime(timestamp: Long): String {
        return timeFormat.format(Date(timestamp))
    }
    
    fun formatDateTime(timestamp: Long): String {
        return dateTimeFormat.format(Date(timestamp))
    }
    
    fun isWithinLastMinutes(timestamp: Long, minutes: Int): Boolean {
        val currentTime = getCurrentTimestamp()
        val minutesInMs = minutes * 60 * 1000
        return (currentTime - timestamp) <= minutesInMs
    }
    
    fun getRelativeTimeString(timestamp: Long): String {
        val now = getCurrentTimestamp()
        val diff = now - timestamp
        
        return when {
            diff < 60_000 -> "just now"
            diff < 3600_000 -> "${diff / 60_000}m ago"
            diff < 86400_000 -> "${diff / 3600_000}h ago"
            else -> formatDateTime(timestamp)
        }
    }
}