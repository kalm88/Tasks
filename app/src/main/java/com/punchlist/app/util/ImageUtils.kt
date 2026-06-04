package com.punchlist.app.util

import android.content.Context
import android.net.Uri
import java.io.File
import java.util.UUID

object ImageUtils {
    fun createTempImageFile(context: Context): File {
        val dir = File(context.cacheDir, "camera_photos").apply { mkdirs() }
        return File(dir, "photo_${UUID.randomUUID()}.jpg")
    }

    fun compressImageFromUri(context: Context, uri: Uri, maxSizeKb: Int = 1024): ByteArray? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val bytes = inputStream.readBytes()
            inputStream.close()
            // Return as-is for now; integrate Bitmap compression if needed
            bytes
        } catch (e: Exception) {
            null
        }
    }
}
