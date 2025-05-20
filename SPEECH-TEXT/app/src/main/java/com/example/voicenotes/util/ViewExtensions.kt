package com.example.voicenotes.util

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar

fun View.visible() {
    visibility = View.VISIBLE
}

fun View.gone() {
    visibility = View.GONE
}

fun View.invisible() {
    visibility = View.INVISIBLE
}

fun View.showIf(show: Boolean) {
    visibility = if (show) View.VISIBLE else View.GONE
}

fun View.enable() {
    isEnabled = true
}

fun View.disable() {
    isEnabled = false
}

fun View.hideKeyboard() {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(windowToken, 0)
}

fun Activity.hideKeyboard() {
    currentFocus?.let { view ->
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }
}

fun Fragment.hideKeyboard() {
    view?.let { activity?.hideKeyboard() }
}

fun View.showSnackbar(
    message: String,
    duration: Int = Snackbar.LENGTH_SHORT,
    actionText: String? = null,
    action: ((View) -> Unit)? = null
) {
    val snackbar = Snackbar.make(this, message, duration)
    actionText?.let {
        snackbar.setAction(it) { view -> action?.invoke(view) }
    }
    snackbar.show()
}

fun View.showSnackbar(
    @StringRes messageRes: Int,
    duration: Int = Snackbar.LENGTH_SHORT,
    @StringRes actionTextRes: Int? = null,
    action: ((View) -> Unit)? = null
) {
    val message = context.getString(messageRes)
    val actionText = actionTextRes?.let { context.getString(it) }
    showSnackbar(message, duration, actionText, action)
}

fun Context.showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, duration).show()
}

fun Context.showToast(@StringRes messageRes: Int, duration: Int = Toast.LENGTH_SHORT) {
    showToast(getString(messageRes), duration)
}

fun Fragment.showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    context?.showToast(message, duration)
}

fun Fragment.showToast(@StringRes messageRes: Int, duration: Int = Toast.LENGTH_SHORT) {
    context?.showToast(messageRes, duration)
}

fun View.showError(message: String) {
    when (this) {
        is com.google.android.material.textfield.TextInputLayout -> {
            error = message
            requestFocus()
        }
        else -> showSnackbar(message)
    }
}

fun View.showError(@StringRes messageRes: Int) {
    showError(context.getString(messageRes))
}

// Extension property to get the display metrics
val Context.displayMetrics: android.util.DisplayMetrics
    get() = resources.displayMetrics

// Extension property to get the screen width in pixels
val Context.screenWidth: Int
    get() = displayMetrics.widthPixels

// Extension property to get the screen height in pixels
val Context.screenHeight: Int
    get() = displayMetrics.heightPixels

// Convert dp to pixels
fun Context.dpToPx(dp: Float): Int {
    return (dp * resources.displayMetrics.density).toInt()
}

// Convert pixels to dp
fun Context.pxToDp(px: Float): Int {
    return (px / resources.displayMetrics.density).toInt()
}

// Extension function to set click with debounce
fun View.setDebouncedOnClickListener(debounceTime: Long = 600L, action: (view: View) -> Unit) {
    val actionWithDebounce: (View) -> Unit = {
        if (it.isClickable) {
            it.isClickable = false
            action(it)
            it.postDelayed({ it.isClickable = true }, debounceTime)
        }
    }
    setOnClickListener(actionWithDebounce)
}
