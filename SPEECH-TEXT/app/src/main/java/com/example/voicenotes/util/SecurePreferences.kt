package com.example.voicenotes.util

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.voicenotes.di.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * A wrapper around EncryptedSharedPreferences that provides a type-safe way to store and retrieve
 * encrypted key-value pairs.
 */
@Singleton
class SecurePreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val PREFS_NAME = "secure_prefs"
        
        // Default values
        private const val STRING_DEFAULT = ""
        private const val INT_DEFAULT = 0
        private const val LONG_DEFAULT = 0L
        private const val FLOAT_DEFAULT = 0f
        private const val BOOLEAN_DEFAULT = false
        private const val STRING_SET_DEFAULT = "" to emptySet<String>()
    }
    
    private val masterKeyAlias = MasterKey.Builder(context, MasterKey.DEFAULT_MASTER_KEY_ALIAS)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()
    
    private val sharedPreferences: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        PREFS_NAME,
        masterKeyAlias,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
    
    // String
    fun putString(key: String, value: String) {
        sharedPreferences.edit().putString(key, value).apply()
    }
    
    fun getString(key: String): String? {
        return sharedPreferences.getString(key, null)
    }
    
    fun getString(key: String, defaultValue: String): String {
        return sharedPreferences.getString(key, defaultValue) ?: defaultValue
    }
    
    // Int
    fun putInt(key: String, value: Int) {
        sharedPreferences.edit().putInt(key, value).apply()
    }
    
    fun getInt(key: String): Int {
        return sharedPreferences.getInt(key, INT_DEFAULT)
    }
    
    fun getInt(key: String, defaultValue: Int): Int {
        return sharedPreferences.getInt(key, defaultValue)
    }
    
    // Long
    fun putLong(key: String, value: Long) {
        sharedPreferences.edit().putLong(key, value).apply()
    }
    
    fun getLong(key: String): Long {
        return sharedPreferences.getLong(key, LONG_DEFAULT)
    }
    
    fun getLong(key: String, defaultValue: Long): Long {
        return sharedPreferences.getLong(key, defaultValue)
    }
    
    // Float
    fun putFloat(key: String, value: Float) {
        sharedPreferences.edit().putFloat(key, value).apply()
    }
    
    fun getFloat(key: String): Float {
        return sharedPreferences.getFloat(key, FLOAT_DEFAULT)
    }
    
    fun getFloat(key: String, defaultValue: Float): Float {
        return sharedPreferences.getFloat(key, defaultValue)
    }
    
    // Boolean
    fun putBoolean(key: String, value: Boolean) {
        sharedPreferences.edit().putBoolean(key, value).apply()
    }
    
    fun getBoolean(key: String): Boolean {
        return sharedPreferences.getBoolean(key, BOOLEAN_DEFAULT)
    }
    
    fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        return sharedPreferences.getBoolean(key, defaultValue)
    }
    
    // String Set
    fun putStringSet(key: String, value: Set<String>) {
        sharedPreferences.edit().putStringSet(key, value).apply()
    }
    
    fun getStringSet(key: String): Set<String> {
        return sharedPreferences.getStringSet(key, STRING_SET_DEFAULT.second) ?: emptySet()
    }
    
    fun getStringSet(key: String, defaultValue: Set<String>): Set<String> {
        return sharedPreferences.getStringSet(key, defaultValue) ?: defaultValue
    }
    
    // Remove
    fun remove(key: String) {
        sharedPreferences.edit().remove(key).apply()
    }
    
    // Clear
    fun clear() {
        sharedPreferences.edit().clear().apply()
    }
    
    // Contains
    fun contains(key: String): Boolean {
        return sharedPreferences.contains(key)
    }
    
    // Register/Unregister OnSharedPreferenceChangeListener
    fun registerOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) {
        sharedPreferences.registerOnSharedPreferenceChangeListener(listener)
    }
    
    fun unregisterOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) {
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(listener)
    }
    
    // Get all entries
    fun getAll(): Map<String, *> {
        return sharedPreferences.all
    }
}
