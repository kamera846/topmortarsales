package com.topmortar.topmortarsales.commons.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

object CompressImageUtil {

    fun compressImage(context: Context, uri: Uri, quality: Int): Uri? {
        try {
            val contentResolver = context.contentResolver
            val inputStream = contentResolver.openInputStream(uri)
            val originalBitmap = BitmapFactory.decodeStream(inputStream)

            val outputStream = ByteArrayOutputStream()
            originalBitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)

            val byteArray = outputStream.toByteArray()

            // Save the compressed image to a temporary file
            val tempFile = File.createTempFile("compressed_image", ".jpg")
            val fileOutputStream = FileOutputStream(tempFile)
            fileOutputStream.write(byteArray)
            fileOutputStream.close()

            // Return the Uri of the compressed image
            return Uri.fromFile(tempFile)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

}
