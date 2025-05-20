package com.example.voicenotes.util

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferenceManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    private var preferences: SharedPreferences? = null
    
    fun setup(context: Context) {
        if (preferences == null) {
            preferences = context.getSharedPreferences(
                "${context.packageName}_preferences",
                Context.MODE_PRIVATE
            )
        }
    }
    
    private inline fun <reified T> get(key: String, defaultValue: T): T {
        return when (T::class) {
            String::class -> preferences?.getString(key, defaultValue as? String) as T
            Int::class -> preferences?.getInt(key, defaultValue as? Int ?: 0) as T
            Boolean::class -> preferences?.getBoolean(key, defaultValue as? Boolean ?: false) as T
            Float::class -> preferences?.getFloat(key, defaultValue as? Float ?: 0f) as T
            Long::class -> preferences?.getLong(key, defaultValue as? Long ?: 0L) as T
            else -> throw IllegalArgumentException("Unsupported type")
        } ?: defaultValue
    }
    
    private fun <T> put(key: String, value: T) {
        preferences?.edit {
            when (value) {
                is String -> putString(key, value)
                is Int -> putInt(key, value)
                is Boolean -> putBoolean(key, value)
                is Float -> putFloat(key, value)
                is Long -> putLong(key, value)
                else -> throw IllegalArgumentException("Unsupported type")
            }
        }
    }
    
    // App theme
    var appTheme: String
        get() = get(PREF_APP_THEME, THEME_SYSTEM)
        set(value) = put(PREF_APP_THEME, value)
    
    // Sort order
    var sortOrder: String
        get() = get(PREF_SORT_ORDER, SORT_LAST_MODIFIED_DESC)
        set(value) = put(PREF_SORT_ORDER, value)
    
    // First launch
    var isFirstLaunch: Boolean
        get() = get(PREF_FIRST_LAUNCH, true)
        set(value) = put(PREF_FIRST_LAUNCH, value)
    
    // User ID (for multi-user support in the future)
    var currentUserId: String?
        get() = preferences?.getString(PREF_CURRENT_USER_ID, null)
        set(value) = preferences?.edit { putString(PREF_CURRENT_USER_ID, value) }
    
    // Clear all preferences
    fun clear() {
        preferences?.edit()?.clear()?.apply()
    }
    
    companion object {
        // Theme preferences
        const val THEME_SYSTEM = "system"
        const val THEME_LIGHT = "light"
        const val THEME_DARK = "dark"
        
        // Sort order
        const val SORT_LAST_MODIFIED_DESC = "last_modified_desc"
        const val SORT_LAST_MODIFIED_ASC = "last_modified_asc"
        const val SORT_CREATED_DESC = "created_desc"
        const val SORT_CREATED_ASC = "created_asc"
        const val SORT_TITLE_ASC = "title_asc"
        const val SORT_TITLE_DESC = "title_desc"
        
        // Preference keys
        private const val PREF_APP_THEME = "pref_app_theme"
        private const val PREF_SORT_ORDER = "pref_sort_order"
        private const val PREF_FIRST_LAUNCH = "pref_first_launch"
        private const val PREF_CURRENT_USER_ID = "pref_current_user_id"
    }
}
