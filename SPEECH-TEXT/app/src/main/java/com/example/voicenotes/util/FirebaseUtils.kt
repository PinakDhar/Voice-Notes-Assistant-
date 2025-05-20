package com.example.voicenotes.util

import com.example.voicenotes.data.model.Note
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.tasks.await

object FirebaseUtils {
    
    /**
     * Convert a Firestore DocumentSnapshot to a Note object
     */
    fun DocumentSnapshot.toNote(): Note? {
        return try {
            val id = id
            val title = getString("title") ?: ""
            val content = getString("content") ?: ""
            val userId = getString("userId") ?: ""
            val createdAt = getTimestamp("createdAt")?.toDate()?.time ?: 0L
            val updatedAt = getTimestamp("updatedAt")?.toDate()?.time ?: 0L
            
            Note(
                id = id,
                title = title,
                content = content,
                userId = userId,
                createdAt = createdAt,
                updatedAt = updatedAt
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    /**
     * Convert a QuerySnapshot to a list of Note objects
     */
    fun QuerySnapshot.toNotes(): List<Note> {
        return documents.mapNotNull { it.toNote() }
    }
    
    /**
     * Convert a Note object to a map for Firestore
     */
    fun Note.toFirestoreMap(): Map<String, Any> {
        return hashMapOf(
            "title" to title,
            "content" to content,
            "userId" to userId,
            "createdAt" to Timestamp(createdAt, 0),
            "updatedAt" to Timestamp(updatedAt, 0)
        )
    }
    
    /**
     * Execute a Firestore query and return a Result
     */
    suspend fun <T> safeFirestoreCall(
        call: suspend () -> T
    ): Result<T> {
        return try {
            Result.Success(call())
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    /**
     * Extension function to await a Firestore Task and return a Result
     */
    suspend fun <T> com.google.android.gms.tasks.Task<T>.awaitResult(): Result<T> {
        return try {
            val result = this.await()
            Result.Success(result)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}
