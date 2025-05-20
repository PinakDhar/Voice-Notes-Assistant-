package com.example.voicenotes.data.repository

import com.example.voicenotes.data.model.Note
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotesRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val authRepository: AuthRepository
) {
    private val userId: String
        get() = authRepository.getCurrentUserId() ?: throw IllegalStateException("User not authenticated")

    private val notesCollection
        get() = firestore.collection("users").document(userId).collection("notes")

    fun getNotes(): Flow<List<Note>> {
        return notesCollection
            .orderBy("updatedAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, _ ->
                // This will be handled by the Flow
            }
            .map { querySnapshot ->
                querySnapshot.documents.mapNotNull { document ->
                    document.toObject(Note::class.java)?.copy(id = document.id)
                }
            }
    }

    suspend fun getNoteById(noteId: String): Note? {
        return try {
            val document = notesCollection.document(noteId).get().await()
            document.toObject(Note::class.java)?.copy(id = document.id)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun saveNote(note: Note): Result<String> {
        return try {
            val noteToSave = note.copy(
                updatedAt = java.util.Date(),
                userId = userId
            )
            
            // If note has an ID, update it, otherwise create a new one
            val documentReference = if (note.id.isNotEmpty()) {
                notesCollection.document(note.id).set(noteToSave).await()
                notesCollection.document(note.id)
            } else {
                notesCollection.add(noteToSave).await()
            }
            
            Result.success(documentReference.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteNote(noteId: String): Result<Unit> {
        return try {
            notesCollection.document(noteId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    fun searchNotes(query: String): Flow<List<Note>> {
        val searchQuery = query.lowercase()
        return notesCollection
            .orderBy("updatedAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, _ ->
                // This will be handled by the Flow
            }
            .map { querySnapshot ->
                querySnapshot.documents
                    .mapNotNull { document ->
                        document.toObject(Note::class.java)?.copy(id = document.id)
                    }
                    .filter { note ->
                        note.title.lowercase().contains(searchQuery) ||
                        note.content.lowercase().contains(searchQuery)
                    }
            }
    }
}
