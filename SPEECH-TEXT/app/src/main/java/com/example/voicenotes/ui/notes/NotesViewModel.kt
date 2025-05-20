package com.example.voicenotes.ui.notes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.voicenotes.data.model.Note
import com.example.voicenotes.data.repository.NotesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotesViewModel @Inject constructor(
    private val notesRepository: NotesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<NotesUiState>(NotesUiState.Loading)
    val uiState: StateFlow<NotesUiState> = _uiState

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private var currentNotes: List<Note> = emptyList()

    init {
        observeNotes()
    }

    private fun observeNotes() {
        viewModelScope.launch {
            combine(
                notesRepository.getNotes(),
                _searchQuery
            ) { notes, query ->
                currentNotes = notes
                if (query.isBlank()) {
                    notes
                } else {
                    notes.filter { note ->
                        note.title.contains(query, ignoreCase = true) ||
                        note.content.contains(query, ignoreCase = true)
                    }
                }
            }.collect { filteredNotes ->
                _uiState.value = if (filteredNotes.isEmpty() && _searchQuery.value.isNotBlank()) {
                    NotesUiState.EmptySearch
                } else if (filteredNotes.isEmpty()) {
                    NotesUiState.Empty
                } else {
                    NotesUiState.Success(filteredNotes)
                }
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun saveNote(title: String, content: String, onComplete: (Result<Unit>) -> Unit) {
        if (content.isBlank()) {
            onComplete(Result.failure(Exception("Note content cannot be empty")))
            return
        }

        viewModelScope.launch {
            val note = Note(
                title = title,
                content = content,
                updatedAt = java.util.Date()
            )
            
            when (val result = notesRepository.saveNote(note)) {
                is Result.Success -> onComplete(Result.success(Unit))
                is Result.Failure -> onComplete(Result.failure(result.exception))
            }
        }
    }

    fun deleteNote(noteId: String, onComplete: (Result<Unit>) -> Unit) {
        viewModelScope.launch {
            when (val result = notesRepository.deleteNote(noteId)) {
                is Result.Success -> onComplete(Result.success(Unit))
                is Result.Failure -> onComplete(Result.failure(result.exception))
            }
        }
    }
}

sealed class NotesUiState {
    object Loading : NotesUiState()
    object Empty : NotesUiState()
    object EmptySearch : NotesUiState()
    data class Success(val notes: List<Note>) : NotesUiState()
    data class Error(val message: String) : NotesUiState()
}
