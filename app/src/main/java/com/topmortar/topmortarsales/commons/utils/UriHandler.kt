package com.topmortar.topmortarsales.commons.utils

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream

object UriHandler {

    fun uriToMultiPart(context: Context, uri: Uri, partName: String): MultipartBody.Part? {
        val inputStream = context.contentResolver.openInputStream(uri)
        val file = File(context.cacheDir, "upload_temp")
        val outputStream = FileOutputStream(file)
        inputStream?.copyTo(outputStream)
        inputStream?.close()
        outputStream.close()

        val requestFile = file.asRequestBody(context.contentResolver.getType(uri)?.toMediaTypeOrNull())
        val (name, ext) = getFileNameAndExtension(context, uri)
        return MultipartBody.Part.createFormData(partName, "$name.$ext", requestFile)
    }

    private fun getFileNameAndExtension(context: Context, uri: Uri): Pair<String, String> {
        var name = "image"
        var extension = "jpg" // default

        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (it.moveToFirst() && nameIndex != -1) {
                val fullName = it.getString(nameIndex)
                val dotIndex = fullName.lastIndexOf('.')
                if (dotIndex != -1) {
                    name = fullName.substring(0, dotIndex)
                    extension = fullName.substring(dotIndex + 1)
                } else {
                    name = fullName
                }
            }
        }
        val sanitizedName = sanitizeFileName(name)
        return Pair(sanitizedName, extension)
    }

    private fun sanitizeFileName(filename: String): String {
        // Karakter yang tidak diizinkan dalam nama file
        val regex = Regex("[!&\$@=;/:+,?%\\[\\]<>\\\\~^*#|()\\s]")
        return filename.replace(regex, "_")
    }
}