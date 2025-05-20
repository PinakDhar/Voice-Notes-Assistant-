package com.example.voicenotes.ui.notes

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.example.voicenotes.R

class DeleteConfirmationDialog : DialogFragment() {
    
    private var onDeleteConfirmed: (() -> Unit)? = null
    
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(requireContext(), R.style.Theme_VoiceNotes_Dialog)
            .setTitle(R.string.delete_note)
            .setMessage(R.string.are_you_sure_delete_note)
            .setPositiveButton(R.string.delete) { _, _ ->
                onDeleteConfirmed?.invoke()
            }
            .setNegativeButton(R.string.cancel, null)
            .create()
    }
    
    fun setOnDeleteConfirmedListener(listener: () -> Unit) {
        onDeleteConfirmed = listener
    }
    
    companion object {
        const val TAG = "DeleteConfirmationDialog"
    }
}
