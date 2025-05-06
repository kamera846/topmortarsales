package com.topmortar.topmortarsales.commons.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import kotlin.math.roundToInt

object CompressImageUtil {

    fun compressImageOptimized(
        context: Context,
        imageUri: Uri,
        maxWidth: Int = 1024,
        maxHeight: Int = 1024,
        quality: Int = 80
    ): Uri {
        val inputStream = context.contentResolver.openInputStream(imageUri) ?: return imageUri

        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        BitmapFactory.decodeStream(inputStream, null, options)

        // Hitung sample size
        options.inSampleSize = calculateInSampleSize(options, maxWidth, maxHeight)
        options.inJustDecodeBounds = false

        val inputStream2 = context.contentResolver.openInputStream(imageUri) ?: return imageUri
        var bitmap = BitmapFactory.decodeStream(inputStream2, null, options)

        if (bitmap != null) {
            bitmap = resizeBitmapKeepingAspectRatio(bitmap, maxWidth, maxHeight)
        }

        val compressedFile = File(context.cacheDir, "compressed_${System.currentTimeMillis()}.jpg")
        val fos = FileOutputStream(compressedFile)

        bitmap?.compress(Bitmap.CompressFormat.JPEG, quality, fos)

        fos.flush()
        fos.close()
        bitmap?.recycle()

        return Uri.fromFile(compressedFile)
    }

    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val (height: Int, width: Int) = options.run { outHeight to outWidth }
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight: Int = height / 2
            val halfWidth: Int = width / 2

            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

    private fun resizeBitmapKeepingAspectRatio(bitmap: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height

        val widthRatio = maxWidth.toFloat() / width
        val heightRatio = maxHeight.toFloat() / height
        val ratio = minOf(widthRatio, heightRatio)

        val newWidth = (width * ratio).roundToInt()
        val newHeight = (height * ratio).roundToInt()

        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }
}
