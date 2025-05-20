package com.example.voicenotes.util

/**
 * A generic class that holds a value with its loading status.
 * @param <T>
 */
sealed class Result<out T : Any> {
    /**
     * Represents a successful result with the [data] received.
     */
    data class Success<out T : Any>(val data: T) : Result<T>() {
        override fun toString() = "Success[data=$data]"
    }

    /**
     * Represents an error with the [exception] that was thrown.
     */
    data class Error(val exception: Exception) : Result<Nothing>() {
        override fun toString() = "Error[exception=$exception]"
    }

    /**
     * Represents a loading state.
     */
    object Loading : Result<Nothing>() {
        override fun toString() = "Loading"
    }

    /**
     * Returns `true` if this result represents a successful outcome.
     */
    val isSuccess: Boolean get() = this is Success

    /**
     * Returns `true` if this result represents a failed outcome.
     */
    val isError: Boolean get() = this is Error

    /**
     * Returns `true` if this result represents a loading state.
     */
    val isLoading: Boolean get() = this is Loading

    /**
     * Returns the encapsulated result if this instance represents [Success] or `null` if it is [Error] or [Loading].
     */
    fun getOrNull(): T? = (this as? Success)?.data

    /**
     * Returns the encapsulated exception if this instance represents [Error] or `null` if it is [Success] or [Loading].
     */
    fun exceptionOrNull(): Exception? = (this as? Error)?.exception

    /**
     * Returns the encapsulated result if this instance represents [Success] or throws the encapsulated exception if it is [Error].
     * 
     * @throws IllegalStateException if the result is [Loading]
     */
    fun getOrThrow(): T = when (this) {
        is Success -> data
        is Error -> throw exception
        is Loading -> throw IllegalStateException("Result is loading")
    }

    /**
     * Returns the encapsulated result of the given [transform] function applied to the encapsulated value
     * if this instance represents [Success] or the original [Error] if it is [Error] or [Loading].
     */
    fun <R : Any> map(transform: (T) -> R): Result<R> = when (this) {
        is Success -> Success(transform(data))
        is Error -> this
        is Loading -> this
    }

    /**
     * Returns the encapsulated result of the given [transform] function applied to the encapsulated value
     * if this instance represents [Success] or the original [Error] if it is [Error] or [Loading].
     */
    suspend fun <R : Any> suspendMap(transform: suspend (T) -> R): Result<R> = when (this) {
        is Success -> try {
            Success(transform(data))
        } catch (e: Exception) {
            Error(e)
        }
        is Error -> this
        is Loading -> this
    }

    /**
     * Performs the given [action] on the encapsulated value if this instance represents [Success].
     * Returns the original `Result` unchanged.
     */
    fun onSuccess(action: (T) -> Unit): Result<T> = apply {
        if (this is Success) action(data)
    }

    /**
     * Performs the given [action] on the encapsulated exception if this instance represents [Error].
     * Returns the original `Result` unchanged.
     */
    fun onError(action: (Exception) -> Unit): Result<T> = apply {
        if (this is Error) action(exception)
    }

    /**
     * Performs the given [action] if this instance represents [Loading].
     * Returns the original `Result` unchanged.
     */
    fun onLoading(action: () -> Unit): Result<T> = apply {
        if (this is Loading) action()
    }
}
