package com.example.voicenotes.data.model

import android.content.Context
import com.example.voicenotes.R
import java.util.*
import kotlin.math.abs

class TimeAgo(private val timestamp: Long) {
    
    fun format(context: Context): String {
        val currentTime = System.currentTimeMillis()
        val timeDiff = currentTime - timestamp
        val seconds = timeDiff / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        val days = hours / 24
        
        return when {
            seconds < 60 -> context.getString(R.string.just_now)
            minutes < 60 -> context.resources.getQuantityString(
                R.plurals.minutes_ago, 
                minutes.toInt(), 
                minutes.toInt()
            )
            hours < 24 -> context.resources.getQuantityString(
                R.plurals.hours_ago, 
                hours.toInt(), 
                hours.toInt()
            )
            days == 1L -> context.getString(R.string.yesterday)
            days < 7 -> context.resources.getQuantityString(
                R.plurals.days_ago, 
                days.toInt(), 
                days.toInt()
            )
            else -> {
                val calendar = Calendar.getInstance().apply { timeInMillis = timestamp }
                val dateFormat = android.text.format.DateFormat.getMediumDateFormat(context)
                dateFormat.format(calendar.time)
            }
        }
    }
    
    companion object {
        fun format(timestamp: Long, context: Context): String {
            return TimeAgo(timestamp).format(context)
        }
    }
}
