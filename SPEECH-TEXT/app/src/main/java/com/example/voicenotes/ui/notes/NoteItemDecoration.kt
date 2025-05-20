package com.example.voicenotes.ui.notes

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class NoteItemDecoration(
    private val spacing: Int,
    private val spanCount: Int = 1
) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val position = parent.getChildAdapterPosition(view)
        val column = position % spanCount

        with(outRect) {
            // Left spacing for first column, half spacing for others
            left = if (column == 0) spacing else spacing / 2
            
            // Right spacing for last column, half spacing for others
            right = if (column == spanCount - 1) spacing else spacing / 2
            
            // Add top spacing for all items
            top = if (position < spanCount) spacing else 0
            
            // Add bottom spacing for all items
            bottom = spacing
        }
    }
}
