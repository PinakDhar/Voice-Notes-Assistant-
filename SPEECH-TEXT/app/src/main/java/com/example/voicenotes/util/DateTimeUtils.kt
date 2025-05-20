package com.example.voicenotes.util

import android.content.Context
import com.example.voicenotes.R
import java.text.SimpleDateFormat
import java.util.*

object DateTimeUtils {
    
    private const val SECOND_MILLIS = 1000L
    private const val MINUTE_MILLIS = 60 * SECOND_MILLIS
    private const val HOUR_MILLIS = 60 * MINUTE_MILLIS
    private const val DAY_MILLIS = 24 * HOUR_MILLIS
    
    /**
     * Format a timestamp to a relative time string (e.g., "2 hours ago", "yesterday")
     */
    fun formatRelativeTime(context: Context, timestamp: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp
        
        return when {
            diff < MINUTE_MILLIS -> context.getString(R.string.just_now)
            diff < 2 * MINUTE_MILLIS -> "1 ${context.getString(R.string.minute_ago)}".replace("%d", "1")
            diff < HOUR_MILLIS -> {
                val minutes = (diff / MINUTE_MILLIS).toInt()
                context.resources.getQuantityString(R.plurals.minutes_ago, minutes, minutes)
            }
            diff < 2 * HOUR_MILLIS -> "1 ${context.getString(R.string.hour_ago)}".replace("%d", "1")
            diff < DAY_MILLIS -> {
                val hours = (diff / HOUR_MILLIS).toInt()
                context.resources.getQuantityString(R.plurals.hours_ago, hours, hours)
            }
            diff < 2 * DAY_MILLIS -> context.getString(R.string.yesterday)
            diff < 7 * DAY_MILLIS -> {
                val days = (diff / DAY_MILLIS).toInt()
                context.resources.getQuantityString(R.plurals.days_ago, days, days)
            }
            else -> formatDate(timestamp, context.getString(R.string.date_format))
        }
    }
    
    /**
     * Format a timestamp to the given date format
     */
    fun formatDate(timestamp: Long, format: String): String {
        return SimpleDateFormat(format, Locale.getDefault()).format(Date(timestamp))
    }
    
    /**
     * Format a timestamp to a readable date and time string
     */
    fun formatDateTime(timestamp: Long): String {
        val dateFormat = SimpleDateFormat("MMM d, yyyy h:mm a", Locale.getDefault())
        return dateFormat.format(Date(timestamp))
    }
    
    /**
     * Get current timestamp in milliseconds
     */
    fun currentTimeMillis(): Long = System.currentTimeMillis()
    
    /**
     * Check if two timestamps are on the same day
     */
    fun isSameDay(timestamp1: Long, timestamp2: Long): Boolean {
        val cal1 = Calendar.getInstance().apply { timeInMillis = timestamp1 }
        val cal2 = Calendar.getInstance().apply { timeInMillis = timestamp2 }
        
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
               cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }
}
