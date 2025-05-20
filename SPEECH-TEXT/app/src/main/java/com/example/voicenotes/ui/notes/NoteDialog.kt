package com.example.voicenotes.ui.notes

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.view.LayoutInflater
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.example.voicenotes.R
import com.example.voicenotes.data.model.Note
import com.example.voicenotes.databinding.DialogNoteBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.util.*

class NoteDialog : DialogFragment() {
    
    private var _binding: DialogNoteBinding? = null
    private val binding get() = _binding!!
    
    private var note: Note? = null
    private var isRecording = false
    private var speechRecognizer: SpeechRecognizer? = null
    private var onNoteSaved: ((Note) -> Unit)? = null
    
    private val recognitionListener = object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {
            binding.recordingStatus.text = getString(R.string.listening)
        }

        override fun onBeginningOfSpeech() {
            binding.recordingStatus.text = getString(R.string.speak_now)
            binding.noteContent.hint = ""
        }

        override fun onRmsChanged(rmsdB: Float) {}

        override fun onBufferReceived(buffer: ByteArray?) {}

        override fun onEndOfSpeech() {
            binding.recordingStatus.text = getString(R.string.processing)
        }

        override fun onError(error: Int) {
            isRecording = false
            updateRecordingUI()
            
            val errorMessage = when (error) {
                SpeechRecognizer.ERROR_AUDIO -> getString(R.string.audio_recording_error)
                SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> getString(R.string.insufficient_permissions)
                SpeechRecognizer.ERROR_NETWORK, SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> 
                    getString(R.string.network_error)
                SpeechRecognizer.ERROR_NO_MATCH -> getString(R.string.no_speech_recognized)
                SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> getString(R.string.recognizer_busy)
                SpeechRecognizer.ERROR_SERVER -> getString(R.string.server_error)
                SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> getString(R.string.no_speech_input)
                else -> getString(R.string.speech_error_unknown)
            }
            
            binding.recordingStatus.text = errorMessage
        }

        override fun onResults(results: Bundle?) {
            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            if (!matches.isNullOrEmpty()) {
                val currentText = binding.noteContent.text.toString()
                val newText = if (currentText.isNotEmpty()) "$currentText\n${matches[0]}" else matches[0]
                binding.noteContent.setText(newText)
                binding.noteContent.setSelection(newText.length)
            }
            isRecording = false
            updateRecordingUI()
        }

        override fun onPartialResults(partialResults: Bundle?) {}
        override fun onEvent(eventType: Int, params: Bundle?) {}
    }
    
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogNoteBinding.inflate(LayoutInflater.from(context))
        
        note = arguments?.getParcelable(ARG_NOTE)
        
        setupViews()
        setupSpeechRecognition()
        
        return MaterialAlertDialogBuilder(requireContext(), R.style.Theme_VoiceNotes_Dialog)
            .setView(binding.root)
            .setTitle(if (note == null) R.string.add_note else R.string.edit_note)
            .setPositiveButton(R.string.save) { _, _ -> saveNote() }
            .setNegativeButton(R.string.cancel, null)
            .create()
            .apply {
                // Show keyboard when dialog is shown
                window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
            }
    }
    
    private fun setupViews() {
        note?.let { note ->
            if (note.title.isNotEmpty()) {
                binding.noteTitle.setText(note.title)
            }
            binding.noteContent.setText(note.content)
            binding.noteContent.setSelection(note.content.length)
        }
        
        binding.recordButton.setOnClickListener {
            toggleRecording()
        }
        
        updateRecordingUI()
    }
    
    private fun setupSpeechRecognition() {
        if (SpeechRecognizer.isRecognitionAvailable(requireContext())) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(requireContext()).apply {
                setRecognitionListener(recognitionListener)
            }
        } else {
            binding.recordButton.isEnabled = false
            binding.recordingStatus.text = getString(R.string.speech_not_available)
        }
    }
    
    private fun toggleRecording() {
        if (isRecording) {
            stopRecording()
        } else {
            startRecording()
        }
        isRecording = !isRecording
        updateRecordingUI()
    }
    
    private fun startRecording() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.speak_now))
        }
        
        try {
            speechRecognizer?.startListening(intent)
        } catch (e: Exception) {
            binding.recordingStatus.text = getString(R.string.recording_failed)
            isRecording = false
            updateRecordingUI()
        }
    }
    
    private fun stopRecording() {
        speechRecognizer?.stopListening()
    }
    
    private fun updateRecordingUI() {
        if (isRecording) {
            binding.recordButton.setImageResource(R.drawable.ic_stop)
            binding.recordButton.contentDescription = getString(R.string.stop_recording)
            binding.recordingStatus.visibility = android.view.View.VISIBLE
        } else {
            binding.recordButton.setImageResource(R.drawable.ic_mic)
            binding.recordButton.contentDescription = getString(R.string.start_recording)
            binding.recordingStatus.visibility = android.view.View.GONE
        }
    }
    
    private fun saveNote() {
        val title = binding.noteTitle.text.toString().trim()
        val content = binding.noteContent.text.toString().trim()
        
        if (content.isEmpty()) {
            binding.noteContent.error = getString(R.string.content_cannot_be_empty)
            return
        }
        
        val currentTime = System.currentTimeMillis()
        val updatedNote = note?.copy(
            title = title,
            content = content,
            updatedAt = currentTime
        ) ?: Note(
            id = "",
            title = title,
            content = content,
            createdAt = currentTime,
            updatedAt = currentTime
        )
        
        onNoteSaved?.invoke(updatedNote)
    }
    
    fun setOnNoteSavedListener(listener: (Note) -> Unit) {
        onNoteSaved = listener
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        speechRecognizer?.destroy()
        _binding = null
    }
    
    companion object {
        private const val ARG_NOTE = "note"
        
        fun newInstance(note: Note? = null): NoteDialog {
            return NoteDialog().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_NOTE, note)
                }
            }
        }
    }
}
