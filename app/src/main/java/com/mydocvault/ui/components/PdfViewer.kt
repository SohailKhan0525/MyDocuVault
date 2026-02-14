package com.mydocvault.ui.components

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.Image
import java.io.File

@Composable
fun PdfViewer(filePath: String) {
    var renderer: PdfRenderer? by remember { mutableStateOf(null) }
    var pageCount by remember { mutableIntStateOf(0) }
    var pageIndex by remember { mutableIntStateOf(0) }
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }

    DisposableEffect(filePath) {
        val file = File(filePath)
        val fd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
        val pdf = PdfRenderer(fd)
        renderer = pdf
        pageCount = pdf.pageCount
        onDispose {
            pdf.close()
            fd.close()
        }
    }

    fun renderPage(index: Int) {
        val pdf = renderer ?: return
        val page = pdf.openPage(index)
        val bmp = Bitmap.createBitmap(page.width, page.height, Bitmap.Config.ARGB_8888)
        page.render(bmp, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
        page.close()
        bitmap = bmp
    }

    if (bitmap == null && renderer != null) {
        renderPage(pageIndex)
    }

    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        bitmap?.let { img ->
            Image(bitmap = img.asImageBitmap(), contentDescription = null)
        }
        if (pageCount > 1) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(onClick = {
                    pageIndex = (pageIndex - 1).coerceAtLeast(0)
                    renderPage(pageIndex)
                }, enabled = pageIndex > 0) {
                    Text("Prev")
                }
                Text("${pageIndex + 1} / $pageCount")
                Button(onClick = {
                    pageIndex = (pageIndex + 1).coerceAtMost(pageCount - 1)
                    renderPage(pageIndex)
                }, enabled = pageIndex < pageCount - 1) {
                    Text("Next")
                }
            }
        }
    }
}
