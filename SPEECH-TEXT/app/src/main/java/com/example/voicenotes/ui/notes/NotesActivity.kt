package com.example.voicenotes.ui.notes

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.example.voicenotes.R
import com.example.voicenotes.data.model.Note
import com.example.voicenotes.data.repository.AuthRepository
import com.example.voicenotes.databinding.ActivityNotesBinding
import com.example.voicenotes.ui.auth.LoginActivity
import com.example.voicenotes.util.showSnackbar
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import java.io.File
import java.io.IOException
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class NotesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNotesBinding
    private val viewModel: NotesViewModel by viewModels()
    
    @Inject
    lateinit var authRepository: AuthRepository
    
    private lateinit var notesAdapter: NotesAdapter
    private var currentNote: Note? = null
    private var isRecording = false
    private var outputFile: String = ""
    private var mediaRecorder: MediaRecorder? = null
    private var mediaPlayer: MediaPlayer? = null
    private var speechRecognizer: SpeechRecognizer? = null
    private var isListening = false
    
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startVoiceRecognition()
        } else {
            showSnackbar(binding.root, getString(R.string.microphone_permission_denied))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNotesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupToolbar()
        setupRecyclerView()
        setupSearchView()
        setupClickListeners()
        observeViewModel()
        
        // Initialize speech recognizer
        if (SpeechRecognizer.isRecognitionAvailable(this)) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this).apply {
                setRecognitionListener(createRecognitionListener())
            }
        } else {
            binding.addNoteFab.isVisible = false
            showSnackbar(binding.root, getString(R.string.speech_recognition_not_available))
        }
        
        // Setup output directory for recordings
        outputFile = "${externalCacheDir?.absolutePath}/audiorecord.3gp"
    }
    
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(true)
    }
    
    private fun setupRecyclerView() {
        notesAdapter = NotesAdapter(
            onNoteClick = { note ->
                showNoteDialog(note)
            },
            onNoteDelete = { note ->
                showDeleteConfirmation(note)
            },
            onNoteEdit = { note ->
                showNoteDialog(note)
            }
        )
        
        binding.notesRecyclerView.apply {
            adapter = notesAdapter
            setHasFixedSize(true)
        }
        
        // Set up swipe refresh
        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.refresh()
        }
    }
    
    private fun setupSearchView() {
        binding.searchView.editText?.setTextColor(getColor(R.color.on_surface))
        binding.searchView.editText?.setHintTextColor(getColor(R.color.on_surface_secondary))
        
        binding.searchBar.setOnClickListener {
            binding.searchView.show()
        }
        
        binding.searchView.editText?.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH) {
                val query = binding.searchView.text.toString()
                viewModel.onSearchQueryChanged(query)
                true
            } else {
                false
            }
        }
    }
    
    private fun setupClickListeners() {
        binding.addNoteFab.setOnClickListener {
            checkAudioPermission()
        }
        
        binding.startRecordingFab.setOnClickListener {
            if (isRecording) {
                stopRecording()
            } else {
                startRecording()
            }
        }
    }
    
    private fun observeViewModel() {
        lifecycleScope.launchWhenStarted {
            viewModel.uiState.collect { state ->
                when (state) {
                    is NotesUiState.Loading -> showLoading(true)
                    is NotesUiState.Empty -> showEmptyState()
                    is NotesUiState.EmptySearch -> showEmptySearchState()
                    is NotesUiState.Success -> showNotes(state.notes)
                    is NotesUiState.Error -> showError(state.message)
                }
            }
        }
        
        // Observe search query changes
        viewModel.searchQuery.collectWhenStarted(this) { query ->
            binding.searchView.setText(query)
            if (query.isNotEmpty()) {
                binding.searchView.show()
            }
        }
    }
    
    private fun showLoading(show: Boolean) {
        binding.swipeRefreshLayout.isRefreshing = show
    }
    
    private fun showNotes(notes: List<Note>) {
        binding.notesRecyclerView.isVisible = true
        binding.emptyStateText.isVisible = false
        binding.emptySearchResultsText.isVisible = false
        
        notesAdapter.submitList(notes)
    }
    
    private fun showEmptyState() {
        binding.notesRecyclerView.isVisible = false
        binding.emptyStateText.isVisible = true
        binding.emptySearchResultsText.isVisible = false
        binding.emptyStateText.text = getString(R.string.no_notes_yet)
    }
    
    private fun showEmptySearchState() {
        binding.notesRecyclerView.isVisible = false
        binding.emptyStateText.isVisible = false
        binding.emptySearchResultsText.isVisible = true
        binding.emptySearchResultsText.text = getString(R.string.no_notes_found)
    }
    
    private fun showError(message: String) {
        showLoading(false)
        showSnackbar(binding.root, message)
    }
    
    private fun checkAudioPermission() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED -> {
                startVoiceRecognition()
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
        }
    }
    
    private fun startVoiceRecognition() {
        if (isListening) return
        
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.speak_now))
        }
        
        try {
            speechRecognizer?.startListening(intent)
            isListening = true
            showRecordingUI(true)
        } catch (e: Exception) {
            showSnackbar(binding.root, getString(R.string.speech_error, e.message ?: "Unknown error"))
        }
    }
    
    private fun createRecognitionListener() = object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {}
        override fun onBeginningOfSpeech() {}
        override fun onRmsChanged(rmsdB: Float) {}
        override fun onBufferReceived(buffer: ByteArray?) {}
        override fun onEndOfSpeech() {}
        override fun onError(error: Int) {
            isListening = false
            showRecordingUI(false)
            
            val errorMessage = when (error) {
                SpeechRecognizer.ERROR_AUDIO -> getString(R.string.audio_recording_error)
                SpeechRecognizer.ERROR_CLIENT -> return
                SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> getString(R.string.insufficient_permissions)
                SpeechRecognizer.ERROR_NETWORK_TIMEOUT, SpeechRecognizer.ERROR_NETWORK -> getString(R.string.network_error)
                SpeechRecognizer.ERROR_NO_MATCH -> getString(R.string.no_speech_recognized)
                SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> getString(R.string.recognizer_busy)
                SpeechRecognizer.ERROR_SERVER -> getString(R.string.server_error)
                SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> getString(R.string.no_speech_input)
                else -> getString(R.string.speech_error_unknown)
            }
            
            showSnackbar(binding.root, errorMessage)
        }

        override fun onResults(results: Bundle?) {
            isListening = false
            showRecordingUI(false)
            
            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            if (!matches.isNullOrEmpty()) {
                val spokenText = matches[0]
                showNoteDialog(Note(content = spokenText))
            }
        }

        override fun onPartialResults(partialResults: Bundle?) {}
        override fun onEvent(eventType: Int, params: Bundle?) {}
    }
    
    private fun showRecordingUI(show: Boolean) {
        binding.addNoteFab.isVisible = !show
        binding.startRecordingFab.isVisible = show
    }
    
    private fun startRecording() {
        if (isRecording) return
        
        mediaRecorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            setOutputFile(outputFile)
            
            try {
                prepare()
                start()
                isRecording = true
                binding.startRecordingFab.text = getString(R.string.stop_recording)
            } catch (e: IOException) {
                Log.e("NotesActivity", "prepare() failed: ${e.message}")
                showSnackbar(binding.root, getString(R.string.recording_failed))
            }
        }
    }
    
    private fun stopRecording() {
        if (!isRecording) return
        
        mediaRecorder?.apply {
            try {
                stop()
                release()
            } catch (e: Exception) {
                Log.e("NotesActivity", "stop() failed: ${e.message}")
            }
        }
        mediaRecorder = null
        isRecording = false
        binding.startRecordingFab.text = getString(R.string.start_recording)
        
        // Show dialog to save the recorded note
        showNoteDialog(Note())
    }
    
    private fun showNoteDialog(note: Note) {
        currentNote = note
        
        val dialogView = layoutInflater.inflate(R.layout.dialog_note, null)
        val dialog = MaterialAlertDialogBuilder(this, R.style.ThemeOverlay_MaterialComponents_Dialog)
            .setView(dialogView)
            .setTitle(if (note.id.isEmpty()) R.string.add_note else R.string.edit_note)
            .setCancelable(false)
            .create()
            
        val titleInput = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.titleInput)
        val contentInput = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.contentInput)
        val saveButton = dialogView.findViewById<com.google.android.material.button.MaterialButton>(R.id.saveButton)
        val cancelButton = dialogView.findViewById<com.google.android.material.button.MaterialButton>(R.id.cancelButton)
        
        // Set current note data
        titleInput.setText(note.title)
        contentInput.setText(note.content)
        
        // Set focus on content if title is empty, otherwise focus on title
        if (note.title.isEmpty() && note.content.isEmpty()) {
            titleInput.requestFocus()
        } else if (note.content.isEmpty()) {
            contentInput.requestFocus()
        }
        
        saveButton.setOnClickListener {
            val title = titleInput.text.toString().trim()
            val content = contentInput.text.toString().trim()
            
            if (content.isEmpty()) {
                contentInput.error = getString(R.string.content_cannot_be_empty)
                return@setOnClickListener
            }
            
            val updatedNote = note.copy(
                title = title,
                content = content,
                updatedAt = Date()
            )
            
            viewModel.saveNote(updatedNote) { result ->
                result.onSuccess {
                    dialog.dismiss()
                    showSnackbar(binding.root, getString(R.string.note_saved))
                }.onFailure { exception ->
                    showSnackbar(binding.root, exception.message ?: getString(R.string.error_saving_note))
                }
            }
        }
        
        cancelButton.setOnClickListener {
            dialog.dismiss()
        }
        
        dialog.show()
    }
    
    private fun showDeleteConfirmation(note: Note) {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.delete_note)
            .setMessage(R.string.are_you_sure_delete_note)
            .setPositiveButton(R.string.delete) { _, _ ->
                viewModel.deleteNote(note) { result ->
                    result.onSuccess {
                        showSnackbar(binding.root, getString(R.string.note_deleted))
                    }.onFailure { exception ->
                        showSnackbar(binding.root, exception.message ?: getString(R.string.error_deleting_note))
                    }
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }
    
    private fun signOut() {
        authRepository.signOut()
        startActivity(Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
        finish()
    }
    
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_notes, menu)
        return true
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_sign_out -> {
                signOut()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        speechRecognizer?.destroy()
        mediaRecorder?.release()
        mediaPlayer?.release()
    }
    
    companion object {
        private const val TAG = "NotesActivity"
    }
}
