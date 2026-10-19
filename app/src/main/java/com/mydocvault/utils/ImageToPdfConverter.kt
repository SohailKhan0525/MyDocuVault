package com.mydocvault.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import kotlin.math.min

/**
 * Converts up to 100 selected images into a single, paginated A4 PDF.
 * Images are decoded one at a time and downsampled to limit peak memory use.
 */
class ImageToPdfConverter(private val context: Context) {
    companion object {
        const val MAX_IMAGES = 100
        private const val PAGE_WIDTH = 595
        private const val PAGE_HEIGHT = 842
        private const val PAGE_MARGIN = 24f
        private const val MAX_DECODE_DIMENSION = 2400
    }

    suspend fun convert(uris: List<Uri>): File = withContext(Dispatchers.IO) {
        require(uris.isNotEmpty()) { "Select at least one image." }
        require(uris.size <= MAX_IMAGES) { "Select no more than $MAX_IMAGES images at a time." }

        val output = File(context.cacheDir, "images_${System.currentTimeMillis()}.pdf")
        val document = PdfDocument()
        try {
            uris.forEachIndexed { index, uri ->
                val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    BitmapFactory.decodeStream(stream, null, bounds)
                } ?: throw IOException("Could not read image ${index + 1}.")

                if (bounds.outWidth <= 0 || bounds.outHeight <= 0) {
                    throw IOException("File ${index + 1} is not a supported image.")
                }

                val options = BitmapFactory.Options().apply {
                    inSampleSize = calculateSampleSize(bounds.outWidth, bounds.outHeight)
                    inPreferredConfig = Bitmap.Config.ARGB_8888
                }
                val bitmap = context.contentResolver.openInputStream(uri)?.use { stream ->
                    BitmapFactory.decodeStream(stream, null, options)
                } ?: throw IOException("Could not decode image ${index + 1}.")

                try {
                    val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, index + 1).create()
                    val page = document.startPage(pageInfo)
                    val canvas: Canvas = page.canvas
                    canvas.drawColor(Color.WHITE)

                    val availableWidth = PAGE_WIDTH - PAGE_MARGIN * 2
                    val availableHeight = PAGE_HEIGHT - PAGE_MARGIN * 2
                    val scale = min(
                        availableWidth / bitmap.width.toFloat(),
                        availableHeight / bitmap.height.toFloat()
                    )
                    val targetWidth = bitmap.width * scale
                    val targetHeight = bitmap.height * scale
                    val left = (PAGE_WIDTH - targetWidth) / 2f
                    val top = (PAGE_HEIGHT - targetHeight) / 2f
                    val destination = RectF(left, top, left + targetWidth, top + targetHeight)
                    val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
                    canvas.drawBitmap(bitmap, null, destination, paint)
                    document.finishPage(page)
                } finally {
                    bitmap.recycle()
                }
            }

            FileOutputStream(output).use { document.writeTo(it) }
            output
        } catch (exception: Exception) {
            output.delete()
            throw exception
        } finally {
            document.close()
        }
    }

    private fun calculateSampleSize(width: Int, height: Int): Int {
        var sample = 1
        while (width / sample > MAX_DECODE_DIMENSION || height / sample > MAX_DECODE_DIMENSION) {
            sample *= 2
        }
        return sample
    }
}
