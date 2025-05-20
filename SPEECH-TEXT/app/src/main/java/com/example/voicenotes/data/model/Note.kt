package com.example.voicenotes.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import java.util.*

data class Note(
    @DocumentId
    val id: String = "",
    val title: String = "",
    val content: String = "",
    val createdAt: Date = Date(),
    val updatedAt: Date = Date(),
    val userId: String = ""
) {
    // Convert to a map for Firestore
    fun toMap(): Map<String, Any> = hashMapOf(
        "title" to title,
        "content" to content,
        "createdAt" to Timestamp(createdAt),
        "updatedAt" to Timestamp(updatedAt),
        "userId" to userId
    )
    
    companion object {
        // Create a Note from a Firestore document
        fun fromMap(id: String, map: Map<String, Any>): Note {
            return Note(
                id = id,
                title = map["title"] as? String ?: "",
                content = map["content"] as? String ?: "",
                createdAt = (map["createdAt"] as? Timestamp)?.toDate() ?: Date(),
                updatedAt = (map["updatedAt"] as? Timestamp)?.toDate() ?: Date(),
                userId = map["userId"] as? String ?: ""
            )
        }
    }
}

// Extension function to format date
data class TimeAgo(val date: Date) {
    fun format(): String {
        val now = Date()
        val diffInMs = now.time - date.time
        val diffInSec = diffInMs / 1000
        val diffInMin = diffInSec / 60
        val diffInHours = diffInMin / 60
        val diffInDays = diffInHours / 24

        return when {
            diffInSec < 60 -> "Just now"
            diffInMin < 60 -> "$diffInMin min ago"
            diffInMin < 120 -> "1 hour ago"
            diffInHours < 24 -> "$diffInHours hours ago"
            diffInDays == 1L -> "Yesterday"
            diffInDays < 7 -> "$diffInDays days ago"
            else -> date.toString("MMM d, yyyy")
        }
    }
    
    private fun Date.toString(format: String, locale: Locale = Locale.getDefault()): String {
        val formatter = java.text.SimpleDateFormat(format, locale)
        return formatter.format(this)
    }
}
