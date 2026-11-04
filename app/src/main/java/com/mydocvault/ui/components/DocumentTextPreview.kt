package com.mydocvault.ui.components

import android.graphics.Color
import android.widget.TextView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.mydocvault.utils.FileType
import io.noties.markwon.Markwon

@Composable
fun DocumentTextPreview(
    text: String,
    fileName: String,
    fileType: FileType,
    modifier: Modifier = Modifier
) {
    val isMarkdown = fileName.substringAfterLast('.', "").equals("md", ignoreCase = true)
    val isCode = fileType == FileType.CODE && !isMarkdown
    val sections = text.split("\u0000").filter { it.isNotBlank() }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        PreviewHeader(fileName = fileName, fileType = fileType, itemCount = sections.size)
        if (isMarkdown) {
            MarkdownBlock(text = text)
        } else if (isCode) {
            CodeBlock(text = text)
        } else {
            sections.forEachIndexed { index, section ->
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        if (sections.size > 1) {
                            Text(
                                text = section.substringBefore('\n').takeIf { it.isNotBlank() }
                                    ?: "Section ${index + 1}",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Text(
                            text = if (sections.size > 1) section.substringAfter('\n', "") else section,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PreviewHeader(fileName: String, fileType: FileType, itemCount: Int) {
    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)) {
        Text(
            text = fileName,
            style = MaterialTheme.typography.titleMedium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = "${fileType.name.lowercase().replace('_', ' ').replaceFirstChar { it.uppercase() }}${if (itemCount > 1) " • $itemCount sections" else ""}",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun CodeBlock(text: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Text(
            text = text.ifBlank { "This file is empty." },
            modifier = Modifier.padding(14.dp),
            style = MaterialTheme.typography.bodySmall.copy(
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun MarkdownBlock(text: String) {
    val context = LocalContext.current
    val background = MaterialTheme.colorScheme.surface.toArgb()
    val foreground = MaterialTheme.colorScheme.onSurface.toArgb()
    AndroidView(
        modifier = Modifier.fillMaxWidth().background(
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(12.dp)
        ),
        factory = {
            TextView(context).apply {
                setTextColor(foreground)
                setBackgroundColor(Color.TRANSPARENT)
                setPadding(16, 12, 16, 12)
                setTextIsSelectable(true)
                val markwon = Markwon.builder(context).build()
                tag = markwon
            }
        },
        update = { view ->
            view.setTextColor(foreground)
            view.setBackgroundColor(background)
            (view.tag as Markwon).setMarkdown(view, text.ifBlank { "This file is empty." })
        }
    )
}
