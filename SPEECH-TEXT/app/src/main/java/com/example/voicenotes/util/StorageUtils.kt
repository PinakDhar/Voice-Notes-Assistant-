package com.example.voicenotes.util

import android.net.Uri
import com.example.voicenotes.data.model.Result
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.tasks.await
import java.io.File
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StorageUtils @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firebaseStorage: FirebaseStorage
) {
    
    companion object {
        private const val VOICE_NOTES_FOLDER = "voice_notes"
        private const val AUDIO_EXTENSION = "m4a"
    }
    
    private val currentUserId: String
        get() = firebaseAuth.currentUser?.uid ?: throw IllegalStateException("User not authenticated")
    
    private val userStorageRef: StorageReference
        get() = firebaseStorage.reference
            .child(currentUserId)
            .child(VOICE_NOTES_FOLDER)
    
    /**
     * Upload a file to Firebase Storage
     * @param fileUri The URI of the file to upload
     * @return Result with the download URL on success or an error on failure
     */
    suspend fun uploadFile(fileUri: Uri): Result<String> {
        return try {
            // Create a reference to the file in Firebase Storage
            val fileRef = userStorageRef.child("${UUID.randomUUID()}.$AUDIO_EXTENSION")
            
            // Upload the file
            val uploadTask = fileRef.putFile(fileUri).await()
            
            // Get the download URL
            val downloadUrl = fileRef.downloadUrl.await().toString()
            
            Result.Success(downloadUrl)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    /**
     * Delete a file from Firebase Storage
     * @param fileUrl The URL of the file to delete
     * @return Result with success or an error on failure
     */
    suspend fun deleteFile(fileUrl: String): Result<Unit> {
        return try {
            // Get a reference to the file from the URL
            val fileRef = firebaseStorage.getReferenceFromUrl(fileUrl)
            
            // Delete the file
            fileRef.delete().await()
            
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    /**
     * Get a temporary download URL for a file
     * @param fileUrl The URL of the file
     * @return Result with the temporary download URL or an error on failure
     */
    suspend fun getFileUrl(fileUrl: String): Result<String> {
        return try {
            // Get a reference to the file from the URL
            val fileRef = firebaseStorage.getReferenceFromUrl(fileUrl)
            
            // Get the download URL
            val downloadUrl = fileRef.downloadUrl.await().toString()
            
            Result.Success(downloadUrl)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    /**
     * Get a file from local storage
     * @param context The application context
     * @param fileName The name of the file to get
     * @return The file if it exists, or null if it doesn't
     */
    fun getLocalFile(context: android.content.Context, fileName: String): File? {
        val file = File(context.filesDir, fileName)
        return if (file.exists()) file else null
    }
    
    /**
     * Save a file to local storage
     * @param context The application context
     * @param inputStream The input stream of the file to save
     * @param fileName The name to give the saved file
     * @return The saved file, or null if there was an error
     */
    fun saveFileLocally(
        context: android.content.Context,
        inputStream: java.io.InputStream,
        fileName: String
    ): File? {
        return try {
            val file = File(context.filesDir, fileName)
            file.outputStream().use { output ->
                inputStream.copyTo(output)
            }
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
