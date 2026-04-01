package com.mydocvault.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.github.barteksc.pdfviewer.PDFView
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle
import java.io.File

@Composable
fun PdfViewer(filePath: String) {
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var needsPassword by remember { mutableStateOf(false) }
    var wrongPassword by remember { mutableStateOf(false) }
    var password by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        key(filePath, password) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { context ->
                    PDFView(context, null).also { pdfView ->
                        isLoading = true
                        errorMessage = null
                        pdfView.fromFile(File(filePath))
                            .password(password.ifEmpty { null })
                            .scrollHandle(DefaultScrollHandle(context))
                            .enableSwipe(true)
                            .swipeHorizontal(false)
                            .enableDoubletap(true)
                            .defaultPage(0)
                            .onLoad {
                                isLoading = false
                                needsPassword = false
                                wrongPassword = false
                            }
                            .onError { t ->
                                isLoading = false
                                if (isPdfPasswordError(t)) {
                                    if (password.isNotEmpty()) {
                                        wrongPassword = true
                                    }
                                    needsPassword = true
                                } else {
                                    errorMessage = t?.localizedMessage ?: "Failed to load PDF"
                                }
                            }
                            .load()
                    }
                }
            )
        }

        if (isLoading && errorMessage == null && !needsPassword) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }

        if (errorMessage != null) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp)
                    .align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.errorContainer,
                    modifier = Modifier.size(64.dp)
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Icon(
                            imageVector = Icons.Default.BrokenImage,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
                Text(
                    text = errorMessage ?: "Failed to load PDF",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center
                )
            }
        }
    }

    if (needsPassword) {
        PdfPasswordDialog(
            isWrongPassword = wrongPassword,
            onConfirm = { enteredPassword ->
                password = enteredPassword
                needsPassword = false
                wrongPassword = false
                isLoading = true
            },
            onDismiss = {
                needsPassword = false
                errorMessage = "PDF is password-protected."
            }
        )
    }
}

/**
 * Returns true when the throwable indicates the PDF requires (or has an incorrect) password.
 * Checks the full cause chain and both the message text and exception class name so that
 * the detection works across PdfiumAndroid versions and locales.
 */
private fun isPdfPasswordError(t: Throwable?): Boolean {
    var cause: Throwable? = t
    while (cause != null) {
        val className = cause.javaClass.simpleName.lowercase()
        val message = cause.message?.lowercase() ?: ""
        if (className.contains("password") ||
            message.contains("password") ||
            message.contains("security") ||
            message.contains("encrypted")
        ) return true
        cause = cause.cause
    }
    return false
}
