package com.example.voicenotes.util

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.transition.Transition
import android.transition.TransitionInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.annotation.AnimRes
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityOptionsCompat
import androidx.core.util.Pair
import androidx.fragment.app.Fragment
import com.example.voicenotes.R

object TransitionUtils {

    /**
     * Create a scene transition animation for an activity start
     */
    fun createActivityTransition(activity: Activity, vararg sharedElements: Pair<View, String>): Bundle? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                activity,
                *sharedElements
            )
            options.toBundle()
        } else {
            null
        }
    }

    /**
     * Set up shared element enter transition for a fragment
     */
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun setupSharedElementEnterTransition(
        fragment: Fragment,
        @AnimRes enterAnimation: Int = R.anim.fade_in,
        @AnimRes exitAnimation: Int = R.anim.fade_out,
        @AnimRes popEnterAnimation: Int = R.anim.fade_in,
        @AnimRes popExitAnimation: Int = R.anim.fade_out,
        sharedElementCallback: (() -> Unit)? = null
    ) {
        val transition = TransitionInflater.from(fragment.requireContext())
            .inflateTransition(android.R.transition.move)

        transition.addListener(object : Transition.TransitionListener {
            override fun onTransitionStart(transition: Transition) {}
            override fun onTransitionCancel(transition: Transition) {}
            override fun onTransitionPause(transition: Transition) {}
            override fun onTransitionResume(transition: Transition) {}
            override fun onTransitionEnd(transition: Transition) {
                sharedElementCallback?.invoke()
                transition.removeListener(this)
            }
        })

        // Set the shared element enter transition
        fragment.sharedElementEnterTransition = transition
        
        // Set the shared element return transition
        fragment.sharedElementReturnTransition = transition
        
        // Set the enter and exit transitions
        fragment.enterTransition = android.transition.Fade().apply {
            duration = fragment.resources.getInteger(android.R.integer.config_shortAnimTime).toLong()
        }
        
        fragment.exitTransition = android.transition.Fade().apply {
            duration = fragment.resources.getInteger(android.R.integer.config_shortAnimTime).toLong()
        }
        
        // Set the pop enter and pop exit transitions
        fragment.enterTransition = android.transition.Fade().apply {
            duration = fragment.resources.getInteger(android.R.integer.config_shortAnimTime).toLong()
        }
        
        fragment.exitTransition = android.transition.Fade().apply {
            duration = fragment.resources.getInteger(android.R.integer.config_shortAnimTime).toLong()
        }
    }

    /**
     * Set up shared element return transition for an activity
     */
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun setupReturnTransition(activity: Activity) {
        // This makes sure that the system knows that we want to participate in the return transition
        activity.window.requestFeature(android.view.Window.FEATURE_ACTIVITY_TRANSITIONS)
        activity.window.allowEnterTransitionOverlap = true
        activity.window.allowReturnTransitionOverlap = true
    }

    /**
     * Start an activity with a shared element transition
     */
    fun startActivityWithTransition(
        activity: Activity,
        intent: Intent,
        sharedView: View,
        transitionName: String
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                activity,
                sharedView,
                transitionName
            )
            activity.startActivity(intent, options.toBundle())
        } else {
            activity.startActivity(intent)
        }
    }

    /**
     * Start an activity with multiple shared elements
     */
    fun startActivityWithMultipleSharedElements(
        activity: Activity,
        intent: Intent,
        vararg sharedElements: Pair<View, String>
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                activity,
                *sharedElements
            )
            activity.startActivity(intent, options.toBundle())
        } else {
            activity.startActivity(intent)
        }
    }

    /**
     * Set up a staggered animation for a RecyclerView item
     */
    fun setupStaggeredAnimation(
        view: View,
        position: Int,
        @AnimRes animationRes: Int = R.anim.item_animation_fall_down
    ) {
        view.animation = AnimationUtils.loadAnimation(view.context, animationRes).apply {
            startOffset = (position * 100).toLong()
        }
    }

    /**
     * Set up a circular reveal animation for a view
     */
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun setupCircularReveal(
        view: View,
        cx: Int = view.width / 2,
        cy: Int = view.height / 2,
        startRadius: Float = 0f,
        endRadius: Float = Math.hypot(view.width.toDouble(), view.height.toDouble()).toFloat(),
        duration: Long = view.resources.getInteger(android.R.integer.config_mediumAnimTime).toLong()
    ) {
        view.post {
            val anim = android.view.ViewAnimationUtils.createCircularReveal(
                view,
                cx,
                cy,
                startRadius,
                endRadius
            )
            anim.duration = duration
            view.visibility = View.VISIBLE
            anim.start()
        }
    }

    /**
     * Set up a circular hide animation for a view
     */
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun setupCircularHide(
        view: View,
        cx: Int = view.width / 2,
        cy: Int = view.height / 2,
        endRadius: Float = 0f,
        startRadius: Float = Math.hypot(view.width.toDouble(), view.height.toDouble()).toFloat(),
        duration: Long = view.resources.getInteger(android.R.integer.config_mediumAnimTime).toLong(),
        onEnd: () -> Unit = {}
    ) {
        val anim = android.view.ViewAnimationUtils.createCircularReveal(
            view,
            cx,
            cy,
            startRadius,
            endRadius
        )
        anim.duration = duration
        anim.addListener(object : android.animation.AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: android.animation.Animator) {
                view.visibility = View.INVISIBLE
                onEnd()
            }
        })
        anim.start()
    }

    /**
     * Set up a fade in animation for a view
     */
    fun fadeInView(
        view: View,
        duration: Long = view.resources.getInteger(android.R.integer.config_mediumAnimTime).toLong(),
        startDelay: Long = 0,
        onEnd: () -> Unit = {}
    ) {
        view.apply {
            alpha = 0f
            visibility = View.VISIBLE
            animate()
                .alpha(1f)
                .setDuration(duration)
                .setStartDelay(startDelay)
                .withEndAction { onEnd() }
                .start()
        }
    }

    /**
     * Set up a fade out animation for a view
     */
    fun fadeOutView(
        view: View,
        duration: Long = view.resources.getInteger(android.R.integer.config_mediumAnimTime).toLong(),
        startDelay: Long = 0,
        onEnd: () -> Unit = {}
    ) {
        view.animate()
            .alpha(0f)
            .setDuration(duration)
            .setStartDelay(startDelay)
            .withEndAction {
                view.visibility = View.GONE
                onEnd()
            }
            .start()
    }

    /**
     * Set up a slide in animation for a view
     */
    fun slideInView(
        view: View,
        from: Int = View.TRANSLATION_Y,
        distance: Float = view.height.toFloat(),
        duration: Long = view.resources.getInteger(android.R.integer.config_mediumAnimTime).toLong(),
        startDelay: Long = 0,
        onEnd: () -> Unit = {}
    ) {
        view.apply {
            when (from) {
                View.TRANSLATION_Y -> translationY = distance
                View.TRANSLATION_X -> translationX = distance
            }
            alpha = 0f
            visibility = View.VISIBLE
            animate()
                .translationY(0f)
                .translationX(0f)
                .alpha(1f)
                .setDuration(duration)
                .setStartDelay(startDelay)
                .withEndAction { onEnd() }
                .start()
        }
    }

    /**
     * Set up a slide out animation for a view
     */
    fun slideOutView(
        view: View,
        to: Int = View.TRANSLATION_Y,
        distance: Float = view.height.toFloat(),
        duration: Long = view.resources.getInteger(android.R.integer.config_mediumAnimTime).toLong(),
        startDelay: Long = 0,
        onEnd: () -> Unit = {}
    ) {
        view.animate()
            .translationY(if (to == View.TRANSLATION_Y) distance else 0f)
            .translationX(if (to == View.TRANSLATION_X) distance else 0f)
            .alpha(0f)
            .setDuration(duration)
            .setStartDelay(startDelay)
            .withEndAction {
                view.visibility = View.GONE
                // Reset view properties
                view.translationY = 0f
                view.translationX = 0f
                view.alpha = 1f
                onEnd()
            }
            .start()
    }
}
