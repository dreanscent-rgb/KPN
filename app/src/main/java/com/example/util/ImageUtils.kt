package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.media.ExifInterface
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.UUID

object ImageUtils {

    fun createTempCameraUri(context: Context): Uri {
        val storageDir = File(context.cacheDir, "camera_photos").apply { mkdirs() }
        val file = File(storageDir, "capture_${System.currentTimeMillis()}.jpg")
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    }

    suspend fun processAndSaveImage(
        context: Context,
        sourceUri: Uri,
        maxDimension: Int = 1600,
        quality: Int = 80
    ): String = withContext(Dispatchers.IO) {
        try {
            val contentResolver = context.contentResolver
            var inputStream: InputStream? = contentResolver.openInputStream(sourceUri)
            val boundsOptions = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeStream(inputStream, null, boundsOptions)
            inputStream?.close()

            val srcWidth = boundsOptions.outWidth
            val srcHeight = boundsOptions.outHeight
            if (srcWidth <= 0 || srcHeight <= 0) return@withContext sourceUri.toString()

            var inSampleSize = 1
            if (srcWidth > maxDimension || srcHeight > maxDimension) {
                val halfWidth = srcWidth / 2
                val halfHeight = srcHeight / 2
                while (halfWidth / inSampleSize >= maxDimension || halfHeight / inSampleSize >= maxDimension) {
                    inSampleSize *= 2
                }
            }

            val decodeOptions = BitmapFactory.Options().apply {
                this.inSampleSize = inSampleSize
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }

            inputStream = contentResolver.openInputStream(sourceUri)
            var bitmap = BitmapFactory.decodeStream(inputStream, null, decodeOptions)
            inputStream?.close()

            if (bitmap == null) return@withContext sourceUri.toString()

            // Rotate according to EXIF if available
            bitmap = correctOrientation(context, sourceUri, bitmap)

            // Scale down if still larger than maxDimension
            val currentMax = maxOf(bitmap.width, bitmap.height)
            if (currentMax > maxDimension) {
                val scale = maxDimension.toFloat() / currentMax
                val targetWidth = (bitmap.width * scale).toInt()
                val targetHeight = (bitmap.height * scale).toInt()
                val scaledBitmap = Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, true)
                if (scaledBitmap != bitmap) {
                    bitmap.recycle()
                    bitmap = scaledBitmap
                }
            }

            // Save to app internal files directory
            val photosDir = File(context.filesDir, "warehouse_photos").apply { mkdirs() }
            val destFile = File(photosDir, "img_${UUID.randomUUID()}.jpg")
            FileOutputStream(destFile).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, out)
            }
            bitmap.recycle()

            destFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            sourceUri.toString()
        }
    }

    private fun correctOrientation(context: Context, uri: Uri, bitmap: Bitmap): Bitmap {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return bitmap
            val exif = ExifInterface(inputStream)
            val orientation = exif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )
            inputStream.close()

            val rotationDegrees = when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                else -> 0f
            }

            if (rotationDegrees != 0f) {
                val matrix = Matrix().apply { postRotate(rotationDegrees) }
                Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
            } else {
                bitmap
            }
        } catch (_: Exception) {
            bitmap
        }
    }
}
