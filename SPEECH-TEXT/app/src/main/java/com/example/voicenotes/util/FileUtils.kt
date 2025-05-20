package com.example.voicenotes.util

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import androidx.core.content.FileProvider
import com.example.voicenotes.BuildConfig
import java.io.*
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FileUtils @Inject constructor(private val context: Context) {

    companion object {
        private const val AUTHORITY = "${BuildConfig.APPLICATION_ID}.fileprovider"
        private const val FILES_DIRECTORY = "files"
        private const val IMAGES_DIRECTORY = "images"
        private const val AUDIO_DIRECTORY = "audio"
        private const val DOCUMENTS_DIRECTORY = "documents"
        private const val CACHE_DIRECTORY = "cache"
        private const val TEMP_DIRECTORY = "temp"
        
        private const val FILE_PREFIX = "file_"
        private const val IMAGE_PREFIX = "IMG_"
        private const val AUDIO_PREFIX = "AUD_"
        private const val TEMP_FILE_PREFIX = "TEMP_"
        
        private const val IMAGE_EXTENSION = ".jpg"
        private const val AUDIO_EXTENSION = ".m4a"
        private const val TEMP_EXTENSION = ".tmp"
        
        private const val BUFFER_SIZE = 8192 // 8KB buffer size
        
        /**
         * Get the MIME type of a file
         */
        fun getMimeType(url: String?): String {
            var type: String? = null
            val extension = MimeTypeMap.getFileExtensionFromUrl(url)
            if (extension != null) {
                type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.lowercase())
            }
            return type ?: "*/*"
        }
        
        /**
         * Get the file extension from a URL
         */
        fun getFileExtension(url: String?): String {
            if (url == null || url.isEmpty()) return ""
            val dot = url.lastIndexOf('.')
            return if (dot >= 0) {
                url.substring(dot + 1).lowercase()
            } else ""
        }
        
        /**
         * Get the file name from a URL
         */
        fun getFileName(url: String?): String {
            if (url == null || url.isEmpty()) return ""
            val filename = url.substringAfterLast('/')
            return if (filename.contains('?')) {
                filename.substring(0, filename.indexOf('?'))
            } else {
                filename
            }
        }
    }
    
    // Directory getters
    
    fun getFilesDirectory(): File = getOrCreateDirectory(FILES_DIRECTORY)
    
    fun getImagesDirectory(): File = getOrCreateDirectory(IMAGES_DIRECTORY)
    
    fun getAudioDirectory(): File = getOrCreateDirectory(AUDIO_DIRECTORY)
    
    fun getDocumentsDirectory(): File = getOrCreateDirectory(DOCUMENTS_DIRECTORY)
    
    fun getCacheDirectory(): File = getOrCreateDirectory(CACHE_DIRECTORY)
    
    fun getTempDirectory(): File = getOrCreateDirectory(TEMP_DIRECTORY)
    
    // File creation methods
    
    fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = "${IMAGE_PREFIX}${timeStamp}_"
        val storageDir = getImagesDirectory()
        return File.createTempFile(
            imageFileName, /* prefix */
            IMAGE_EXTENSION, /* suffix */
            storageDir      /* directory */
        )
    }
    
    fun createAudioFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val audioFileName = "${AUDIO_PREFIX}${timeStamp}_"
        val storageDir = getAudioDirectory()
        return File.createTempFile(
            audioFileName,  /* prefix */
            AUDIO_EXTENSION, /* suffix */
            storageDir      /* directory */
        )
    }
    
    fun createTempFile(prefix: String = TEMP_FILE_PREFIX, suffix: String = TEMP_EXTENSION): File {
        val storageDir = getTempDirectory()
        return File.createTempFile(prefix, suffix, storageDir)
    }
    
    // File operations
    
    fun copyFile(source: File, destination: File): Boolean {
        return try {
            source.inputStream().use { input ->
                destination.outputStream().use { output ->
                    input.copyTo(output, BUFFER_SIZE)
                }
            }
            true
        } catch (e: IOException) {
            e.printStackTrace()
            false
        }
    }
    
    fun copyFile(uri: Uri, destination: File): Boolean {
        return try {
            context.contentResolver.openInputStream(uri)?.use { input ->
                destination.outputStream().use { output ->
                    input.copyTo(output, BUFFER_SIZE)
                }
            }
            true
        } catch (e: IOException) {
            e.printStackTrace()
            false
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    fun moveFile(source: File, destination: File): Boolean {
        return if (source.renameTo(destination)) {
            true
        } else {
            // If rename fails, try copy and delete
            if (copyFile(source, destination)) {
                source.delete()
                true
            } else {
                false
            }
        }
    }
    
    fun deleteFile(file: File): Boolean {
        return file.delete()
    }
    
    fun deleteFile(path: String): Boolean {
        return File(path).delete()
    }
    
    fun deleteFiles(files: List<File>): Boolean {
        var success = true
        for (file in files) {
            if (!file.delete()) {
                success = false
            }
        }
        return success
    }
    
    fun clearDirectory(directory: File): Boolean {
        if (!directory.exists() || !directory.isDirectory) {
            return false
        }
        
        var success = true
        val files = directory.listFiles()
        if (files != null) {
            for (file in files) {
                if (file.isDirectory) {
                    success = success and clearDirectory(file)
                } else {
                    success = success and file.delete()
                }
            }
        }
        return success
    }
    
    // URI and path resolution
    
    fun getFileUri(file: File): Uri {
        return FileProvider.getUriForFile(context, AUTHORITY, file)
    }
    
    fun getPath(uri: Uri): String? {
        // DocumentProvider
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val type = split[0]
                
                if ("primary".equals(type, ignoreCase = true)) {
                    return "${Environment.getExternalStorageDirectory()}/${split[1]}"
                }
                
                // Handle non-primary volumes
                return "/storage/$type/${split[1]}"
            } else if (isDownloadsDocument(uri)) {
                // DownloadsProvider
                val id = DocumentsContract.getDocumentId(uri)
                val contentUri = ContentUris.withAppendedId(
                    Uri.parse("content://downloads/public_downloads"),
                    id.toLong()
                )
                return getDataColumn(context, contentUri, null, null)
            } else if (isMediaDocument(uri)) {
                // MediaProvider
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val type = split[0]
                
                var contentUri: Uri? = null
                when (type.lowercase()) {
                    "image" -> contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    "video" -> contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                    "audio" -> contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                }
                
                val selection = "_id=?"
                val selectionArgs = arrayOf(split[1])
                
                return contentUri?.let {
                    getDataColumn(context, it, selection, selectionArgs)
                }
            }
        } else if ("content".equals(uri.scheme, ignoreCase = true)) {
            // MediaStore (and general)
            return getDataColumn(context, uri, null, null)
        } else if ("file".equals(uri.scheme, ignoreCase = true)) {
            // File
            return uri.path
        }
        
        return null
    }
    
    fun getFileName(uri: Uri): String? {
        var result: String? = null
        
        if (uri.scheme == "content") {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (index != -1) {
                        result = cursor.getString(index)
                    }
                }
            }
        }
        
        if (result == null) {
            result = uri.path
            val cut = result?.lastIndexOf('/') ?: -1
            if (cut != -1) {
                result = result?.substring(cut + 1)
            }
        }
        
        return result
    }
    
    fun getFileSize(uri: Uri): Long {
        var size: Long = 0
        
        if (uri.scheme == "content") {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                    if (sizeIndex != -1) {
                        size = cursor.getLong(sizeIndex)
                    }
                }
            }
        } else {
            val file = File(uri.path ?: return 0)
            size = file.length()
        }
        
        return size
    }
    
    // Private helper methods
    
    private fun getOrCreateDirectory(directoryName: String): File {
        val dir = File(context.getExternalFilesDir(null), directoryName)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }
    
    private fun getDataColumn(
        context: Context,
        uri: Uri,
        selection: String?,
        selectionArgs: Array<String>?
    ): String? {
        var cursor: Cursor? = null
        val column = "_data"
        val projection = arrayOf(column)
        
        try {
            cursor = context.contentResolver.query(
                uri, projection, selection, selectionArgs, null
            )
            
            cursor?.let {
                if (it.moveToFirst()) {
                    val columnIndex = it.getColumnIndexOrThrow(column)
                    return it.getString(columnIndex)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            cursor?.close()
        }
        
        return null
    }
    
    private fun isExternalStorageDocument(uri: Uri): Boolean {
        return "com.android.externalstorage.documents" == uri.authority
    }
    
    private fun isDownloadsDocument(uri: Uri): Boolean {
        return "com.android.providers.downloads.documents" == uri.authority
    }
    
    private fun isMediaDocument(uri: Uri): Boolean {
        return "com.android.providers.media.documents" == uri.authority
    }
}
