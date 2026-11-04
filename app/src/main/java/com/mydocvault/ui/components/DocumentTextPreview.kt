package com.mydocvault.ui.components

import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.TextView
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import io.noties.markwon.Markwon
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

@Composable
internal fun SourceReader(file: File, markdown: Boolean, onOpen: () -> Unit) {
    var text by remember(file) { mutableStateOf<String?>(null) }
    var error by remember(file) { mutableStateOf<String?>(null) }
    LaunchedEffect(file) {
        try {
            text = withContext(Dispatchers.IO) {
                require(file.length() <= 512 * 1024) { "This file is too large for the source reader." }
                file.readText()
            }
        } catch (e: java.io.IOException) {
            error = "The file could not be read."
        } catch (e: IllegalArgumentException) {
            error = e.message
        }
    }
    val content = text
    when {
        error != null -> ReaderMessage(error.orEmpty(), "Choose another app", onOpen)
        content == null -> ReaderLoading()
        content.isEmpty() -> ReaderMessage("This file is empty.")
        markdown -> {
            val context = LocalContext.current
            val renderer = remember(context) { Markwon.create(context) }
            val foreground = MaterialTheme.colorScheme.onSurface.toArgb()
            Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp)) {
                AndroidView(factory = { TextView(it).apply { setTextIsSelectable(true) } }, update = {
                    it.setTextColor(foreground)
                    renderer.setMarkdown(it, content)
                })
            }
        }
        else -> AndroidView(modifier = Modifier.fillMaxSize(), factory = { context ->
            WebView(context).apply {
                settings.apply {
                    javaScriptEnabled = true
                    allowContentAccess = false
                    allowFileAccess = false
                    blockNetworkLoads = true
                    builtInZoomControls = true
                    displayZoomControls = false
                }
                webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView, url: String) {
                        if (url != "file:///android_asset/source/index.html") return
                        // The document is only assigned to a text node. It is never parsed as HTML or script.
                        val value = JSONObject.quote(content)
                        view.evaluateJavascript("var e=document.getElementById('source'); e.textContent=$value; hljs.highlightElement(e)", null)
                    }
                }
                loadUrl("file:///android_asset/source/index.html")
            }
        }, onRelease = { it.destroy() })
    }
}
