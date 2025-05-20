package com.example.voicenotes.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.widget.ImageView
import androidx.annotation.DrawableRes
import androidx.annotation.Px
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.load.resource.bitmap.FitCenter
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.transition.DrawableCrossFadeFactory
import com.example.voicenotes.R
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ImageLoader @Inject constructor(private val context: Context) {

    private val requestOptions = RequestOptions()
        .diskCacheStrategy(DiskCacheStrategy.ALL)
        .priority(Priority.HIGH)
        .dontAnimate()
        .dontTransform()

    private val drawableCrossFadeFactory = DrawableCrossFadeFactory.Builder()
        .setCrossFadeEnabled(true)
        .build()

    fun loadImage(
        imageView: ImageView,
        url: String?,
        @DrawableRes placeholder: Int = R.drawable.ic_image_placeholder,
        @DrawableRes error: Int = R.drawable.ic_broken_image,
        circleCrop: Boolean = false,
        roundedCorners: Int = 0,
        @Px width: Int = 0,
        @Px height: Int = 0,
        skipMemoryCache: Boolean = false,
        skipDiskCache: Boolean = false,
        priority: Priority = Priority.NORMAL,
        crossFade: Boolean = true,
        onSuccess: (() -> Unit)? = null,
        onError: ((Exception?) -> Unit)? = null
    ) {
        if (url.isNullOrEmpty()) {
            imageView.setImageResource(error)
            return
        }

        val requestBuilder = Glide.with(imageView)
            .load(url)
            .apply(requestOptions)
            .placeholder(placeholder)
            .error(error)
            .priority(priority)
            .skipMemoryCache(skipMemoryCache)
            .diskCacheStrategy(if (skipDiskCache) DiskCacheStrategy.NONE else DiskCacheStrategy.ALL)
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

        // Apply transformations
        when {
            circleCrop -> requestBuilder.apply(RequestOptions.circleCropTransform())
            roundedCorners > 0 -> requestBuilder.transform(RoundedCorners(roundedCorners))
            else -> requestBuilder.transform(FitCenter())
        }

        // Set dimensions if specified
        if (width > 0 && height > 0) {
            requestBuilder.override(width, height)
        }

        // Apply crossfade animation
        if (crossFade) {
            requestBuilder.transition(
                DrawableTransitionOptions.withCrossFade(drawableCrossFadeFactory)
            )
        }

        requestBuilder.into(imageView)
    }

    fun loadImage(
        imageView: ImageView,
        @DrawableRes resourceId: Int,
        circleCrop: Boolean = false,
        roundedCorners: Int = 0,
        @Px width: Int = 0,
        @Px height: Int = 0,
        skipMemoryCache: Boolean = false,
        skipDiskCache: Boolean = false,
        priority: Priority = Priority.NORMAL,
        crossFade: Boolean = true
    ) {
        val requestBuilder = Glide.with(imageView)
            .load(resourceId)
            .apply(requestOptions)
            .skipMemoryCache(skipMemoryCache)
            .diskCacheStrategy(if (skipDiskCache) DiskCacheStrategy.NONE else DiskCacheStrategy.ALL)
            .priority(priority)

        // Apply transformations
        when {
            circleCrop -> requestBuilder.apply(RequestOptions.circleCropTransform())
            roundedCorners > 0 -> requestBuilder.transform(RoundedCorners(roundedCorners))
            else -> requestBuilder.transform(FitCenter())
        }

        // Set dimensions if specified
        if (width > 0 && height > 0) {
            requestBuilder.override(width, height)
        }

        // Apply crossfade animation
        if (crossFade) {
            requestBuilder.transition(
                DrawableTransitionOptions.withCrossFade(drawableCrossFadeFactory)
            )
        }

        requestBuilder.into(imageView)
    }


    fun loadImage(
        imageView: ImageView,
        uri: Uri?,
        circleCrop: Boolean = false,
        roundedCorners: Int = 0,
        @Px width: Int = 0,
        @Px height: Int = 0,
        skipMemoryCache: Boolean = false,
        skipDiskCache: Boolean = false,
        priority: Priority = Priority.NORMAL,
        crossFade: Boolean = true,
        onSuccess: (() -> Unit)? = null,
        onError: ((Exception?) -> Unit)? = null
    ) {
        if (uri == null) {
            onError?.invoke(NullPointerException("Uri is null"))
            return
        }

        val requestBuilder = Glide.with(imageView)
            .load(uri)
            .apply(requestOptions)
            .skipMemoryCache(skipMemoryCache)
            .diskCacheStrategy(if (skipDiskCache) DiskCacheStrategy.NONE else DiskCacheStrategy.ALL)
            .priority(priority)
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

        // Apply transformations
        when {
            circleCrop -> requestBuilder.apply(RequestOptions.circleCropTransform())
            roundedCorners > 0 -> requestBuilder.transform(RoundedCorners(roundedCorners))
            else -> requestBuilder.transform(FitCenter())
        }

        // Set dimensions if specified
        if (width > 0 && height > 0) {
            requestBuilder.override(width, height)
        }

        // Apply crossfade animation
        if (crossFade) {
            requestBuilder.transition(
                DrawableTransitionOptions.withCrossFade(drawableCrossFadeFactory)
            )
        }

        requestBuilder.into(imageView)
    }


    fun clearCache() {
        Glide.get(context).clearMemory()
        Glide.get(context).clearDiskCache()
    }

    fun pauseRequests() {
        Glide.with(context).pauseRequests()
    }

    fun resumeRequests() {
        Glide.with(context).resumeRequests()
    }

    fun isPaused(): Boolean {
        return Glide.with(context).isPaused
    }
}
