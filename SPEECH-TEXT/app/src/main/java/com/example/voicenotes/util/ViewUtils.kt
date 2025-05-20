package com.example.voicenotes.util

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar

object ViewUtils {
    
    // Extension function to show toast from Activity
    fun Activity.toast(message: String, duration: Int = Toast.LENGTH_SHORT) {
        Toast.makeText(this, message, duration).show()
    }
    
    // Extension function to show toast from Fragment
    fun Fragment.toast(message: String, duration: Int = Toast.LENGTH_SHORT) {
        requireContext().toast(message, duration)
    }
    
    // Extension function to show toast with string resource
    fun Context.toast(@StringRes messageRes: Int, duration: Int = Toast.LENGTH_SHORT) {
        Toast.makeText(this, messageRes, duration).show()
    }
    
    // Show snackbar with action
    fun View.snackbar(
        message: String,
        actionText: String? = null,
        duration: Int = Snackbar.LENGTH_LONG,
        action: (() -> Unit)? = null
    ) {
        val snackbar = Snackbar.make(this, message, duration)
        actionText?.let { 
            snackbar.setAction(it) { action?.invoke() }
        }
        snackbar.show()
    }
    
    // Hide keyboard
    fun Activity.hideKeyboard() {
        val imm = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        // Find the currently focused view
        var view = currentFocus
        // If no view currently has focus, create a new one to grab a window token from it
        if (view == null) {
            view = View(this)
        }
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }
    
    // Show keyboard
    fun Activity.showKeyboard(view: View) {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        view.requestFocus()
        imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
    }
    
    // Set visible or gone with animation
    fun View.setVisible(visible: Boolean, animate: Boolean = true) {
        if (animate) {
            if (visible && visibility != View.VISIBLE) {
                alpha = 0f
                visibility = View.VISIBLE
                animate().alpha(1f).setDuration(200).start()
            } else if (!visible && visibility == View.VISIBLE) {
                animate().alpha(0f).setDuration(200).withEndAction {
                    visibility = View.GONE
                }.start()
            }
        } else {
            visibility = if (visible) View.VISIBLE else View.GONE
        }
    }
    
    // Toggle visibility
    fun View.toggleVisibility(animate: Boolean = true) {
        setVisible(visibility != View.VISIBLE, animate)
    }
}
