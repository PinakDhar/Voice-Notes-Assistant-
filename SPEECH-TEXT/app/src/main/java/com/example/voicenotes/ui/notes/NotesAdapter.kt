package com.example.voicenotes.ui.notes

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.voicenotes.R
import com.example.voicenotes.data.model.Note
import com.example.voicenotes.data.model.TimeAgo
import com.example.voicenotes.databinding.ItemNoteBinding

class NotesAdapter(
    private val onNoteClick: (Note) -> Unit,
    private val onNoteDelete: (Note) -> Unit,
    private val onNoteEdit: (Note) -> Unit
) : ListAdapter<Note, NotesAdapter.NoteViewHolder>(NoteDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val binding = ItemNoteBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return NoteViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        val note = getItem(position)
        holder.bind(note)
    }

    inner class NoteViewHolder(
        private val binding: ItemNoteBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(note: Note) {
            binding.apply {
                // Set note title (show "Untitled" if empty)
                noteTitleText.text = note.title.ifEmpty {
                    root.context.getString(R.string.untitled)
                }

                // Set note content
                noteContentText.text = note.content

                // Set formatted time
                noteTimeText.text = TimeAgo(note.updatedAt).format()

                // Set click listeners
                noteCard.setOnClickListener { onNoteClick(note) }

                // Set up menu button
                noteMenuButton.setOnClickListener { view ->
                    showPopupMenu(view, note)
                }
            }
        }

        private fun showPopupMenu(view: View, note: Note) {
            val popup = PopupMenu(view.context, view)
            popup.menuInflater.inflate(R.menu.menu_note_item, popup.menu)

            popup.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.action_edit -> {
                        onNoteEdit(note)
                        true
                    }
                    R.id.action_delete -> {
                        onNoteDelete(note)
                        true
                    }
                    else -> false
                }
            }

            popup.show()
        }
    }
}

class NoteDiffCallback : DiffUtil.ItemCallback<Note>() {
    override fun areItemsTheSame(oldItem: Note, newItem: Note): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Note, newItem: Note): Boolean {
        return oldItem == newItem
    }
}
