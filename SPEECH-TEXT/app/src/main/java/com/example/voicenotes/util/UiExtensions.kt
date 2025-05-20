package com.example.voicenotes.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.annotation.IdRes
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.commit
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.example.voicenotes.R
import com.google.android.material.snackbar.Snackbar

/**
 * Extension function to show a toast message
 */
fun Context.toast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, duration).show()
}

/**
 * Extension function to show a toast message from a string resource
 */
fun Context.toast(@androidx.annotation.StringRes messageRes: Int, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, messageRes, duration).show()
}

/**
 * Extension function to show a snackbar with an optional action
 */
fun View.snackbar(
    message: String,
    duration: Int = Snackbar.LENGTH_LONG,
    actionText: String? = null,
    action: (() -> Unit)? = null
) {
    val snackbar = Snackbar.make(this, message, duration)
    actionText?.let {
        snackbar.setAction(it) { action?.invoke() }
    }
    snackbar.show()
}

/**
 * Extension function to show a snackbar with a string resource
 */
fun View.snackbar(
    @androidx.annotation.StringRes messageRes: Int,
    duration: Int = Snackbar.LENGTH_LONG,
    @androidx.annotation.StringRes actionTextRes: Int? = null,
    action: (() -> Unit)? = null
) {
    val snackbar = Snackbar.make(this, messageRes, duration)
    actionTextRes?.let {
        snackbar.setAction(it) { action?.invoke() }
    }
    snackbar.show()
}

/**
 * Extension function to hide the keyboard
 */
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

/**
 * Extension function to show the keyboard
 */
fun View.showKeyboard() {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    requestFocus()
    imm.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
}

/**
 * Extension function to set visibility with optional animation
 */
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

/**
 * Extension function to toggle visibility with optional animation
 */
fun View.toggleVisibility(animate: Boolean = true) {
    setVisible(visibility != View.VISIBLE, animate)
}

/**
 * Extension function to load an image with Glide
 */
fun ImageView.loadImage(
    url: String?,
    @DrawableRes placeholder: Int = R.drawable.ic_image_placeholder,
    @DrawableRes error: Int = R.drawable.ic_broken_image,
    centerCrop: Boolean = true,
    circleCrop: Boolean = false,
    onSuccess: (() -> Unit)? = null,
    onError: ((Exception?) -> Unit)? = null
) {
    val request = Glide.with(context)
        .load(url)
        .placeholder(placeholder)
        .error(error)
        .listener(object : RequestListener<Drawable> {
            override fun onLoadFailed(
                e: GlideException?,
                model: Any?,
                target: Target<Drawable>,
                isFirstResource: Boolean
            ): Boolean {
                onError?.invoke(e)
                return false
            }

            override fun onResourceReady(
                resource: Drawable?,
                model: Any?,
                target: Target<Drawable>?,
                dataSource: DataSource?,
                isFirstResource: Boolean
            ): Boolean {
                onSuccess?.invoke()
                return false
            }
        })


    if (centerCrop) {
        request.centerCrop()
    }
    
    if (circleCrop) {
        request.circleCrop()
    }
    
    request.into(this)
}

/**
 * Extension function to observe LiveData with a simpler syntax
 */
fun <T> LiveData<T>.observeOnce(lifecycleOwner: LifecycleOwner, observer: (T) -> Unit) {
    observe(lifecycleOwner, object : Observer<T> {
        override fun onChanged(value: T) {
            removeObserver(this)
            observer(value)
        }
    })
}

/**
 * Extension function to safely navigate to a fragment
 */
fun Fragment.navigateTo(
    @IdRes containerViewId: Int,
    fragment: Fragment,
    addToBackStack: Boolean = true,
    tag: String? = fragment::class.java.simpleName,
    sharedElements: Pair<View, String>? = null
) {
    parentFragmentManager.commit {
        if (sharedElements != null) {
            addSharedElement(sharedElements.first, sharedElements.second)
        }
        
        if (addToBackStack) {
            addToBackStack(tag)
        }
        
        replace(containerViewId, fragment, tag)
    }
}

/**
 * Extension function to show a dialog fragment
 */
fun Fragment.showDialog(
    dialog: androidx.fragment.app.DialogFragment,
    tag: String = dialog::class.java.simpleName
) {
    dialog.show(childFragmentManager, tag)
}

/**
 * Extension function to get a color from theme attributes
 */
@ColorInt
fun Context.getColorFromAttr(
    @AttrRes attrColor: Int,
    typedArray: android.content.res.TypedArray? = null,
    resolveRefs: Boolean = true
): Int {
    val array = typedArray ?: theme.obtainStyledAttributes(intArrayOf(attrColor))
    try {
        return if (resolveRefs) {
            val colorResId = array.getResourceId(0, 0)
            if (colorResId != 0) {
                ContextCompat.getColor(this, colorResId)
            } else {
                array.getColor(0, 0)
            }
        } else {
            array.getColor(0, 0)
        }
    } finally {
        if (typedArray == null) {
            array.recycle()
        }
    }
}

/**
 * Extension function to set light status bar icons (dark icons on light background)
 */
fun Activity.setLightStatusBar(light: Boolean) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.isAppearanceLightStatusBars = light
    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        var flags = window.decorView.systemUiVisibility
        flags = if (light) {
            flags or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        } else {
            flags and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
        }
        window.decorView.systemUiVisibility = flags
    }
}

/**
 * Extension function to set light navigation bar icons (dark icons on light background)
 */
fun Activity.setLightNavigationBar(light: Boolean) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.isAppearanceLightNavigationBars = light
    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        var flags = window.decorView.systemUiVisibility
        flags = if (light) {
            flags or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
        } else {
            flags and View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR.inv()
        }
        window.decorView.systemUiVisibility = flags
    }
}

/**
 * Extension function to set fullscreen mode
 */
fun Activity.setFullScreen(fullScreen: Boolean) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        if (fullScreen) {
            window.setDecorFitsSystemWindows(false)
            val controller = window.insetsController
            controller?.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
            controller?.systemBarsBehavior =
                WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        } else {
            window.setDecorFitsSystemWindows(true)
            val controller = window.insetsController
            controller?.show(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
        }
    } else {
        @Suppress("DEPRECATION")
        if (fullScreen) {
            window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_IMMERSIVE
                        or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_FULLSCREEN
                )
        } else {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
        }
    }
}

/**
 * Extension function to set margin to a view programmatically
 */
fun View.setMargin(
    left: Int = marginLeft,
    top: Int = marginTop,
    right: Int = marginRight,
    bottom: Int = marginBottom
) {
    val params = layoutParams as? ViewGroup.MarginLayoutParams
        ?: return // Return if the view's layout params are not MarginLayoutParams
        
    params.setMargins(left, top, right, bottom)
    layoutParams = params
}

/**
 * Extension function to set padding to a view programmatically
 */
fun View.setPadding(
    left: Int = paddingLeft,
    top: Int = paddingTop,
    right: Int = paddingRight,
    bottom: Int = paddingBottom
) {
    setPadding(left, top, right, bottom)
}

/**
 * Extension function to open a URL in a browser
 */
fun Context.openUrl(url: String) {
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
    } catch (e: Exception) {
        toast("Couldn't open URL")
    }
}

/**
 * Extension function to share text
 */
fun Context.shareText(text: String, subject: String = "") {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, subject)
        putExtra(Intent.EXTRA_TEXT, text)
    }
    startActivity(Intent.createChooser(intent, "Share via"))
}

/**
 * Extension function to get a drawable with tint
 */
fun Context.getTintedDrawable(
    @androidx.annotation.DrawableRes drawableRes: Int,
    @androidx.annotation.ColorRes colorRes: Int
): Drawable? {
    return ContextCompat.getDrawable(this, drawableRes)?.apply {
        setTint(ContextCompat.getColor(this@getTintedDrawable, colorRes))
    }
}
