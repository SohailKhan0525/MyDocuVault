package com.mydocvault.ui.components

import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import java.io.ByteArrayInputStream
import java.io.File
import java.net.URI
import java.util.zip.ZipFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.jsoup.parser.Parser
import org.jsoup.safety.Safelist

private const val BOOK_HOST = "book.invalid"
private const val MAX_CHAPTER = 4L * 1024 * 1024
private const val MAX_IMAGE = 12L * 1024 * 1024

/** Resolve package-relative EPUB paths. Only explicitly referenced ZIP members are read. */
private fun member(parent: String, href: String): String {
    val path = URI(null, null, "/$parent", null).resolve(href).normalize()
    require(path.scheme == null && path.authority == null && path.path.startsWith("/") && !path.path.startsWith("/../"))
    return path.path.removePrefix("/")
}

private fun ZipFile.contents(path: String, limit: Long): ByteArray {
    val entry = getEntry(path) ?: error("Missing book resource")
    require(entry.size in 0..limit)
    return getInputStream(entry).use { it.readBytes() }
}

private fun chapters(file: File): List<String> = ZipFile(file).use { zip ->
    fun xml(path: String) = Jsoup.parse(zip.contents(path, MAX_CHAPTER).inputStream(), null, "", Parser.xmlParser())
    val opf = xml("META-INF/container.xml").selectFirst("rootfile")?.attr("full-path") ?: error("Missing package")
    val manifest = xml(opf)
    val items = manifest.select("manifest > item").associateBy { it.attr("id") }
    manifest.select("spine > itemref").mapNotNull {
        items[it.attr("idref")]?.takeIf { entry -> entry.attr("media-type") in setOf("application/xhtml+xml", "text/html") }
            ?.let { entry -> member(opf, entry.attr("href")) }
    }.also { require(it.isNotEmpty()) }
}

@Composable
internal fun EpubReader(file: File, onOpen: () -> Unit) {
    var pages by remember(file) { mutableStateOf<List<String>>(emptyList()) }
    var index by rememberSaveable(file.path) { mutableIntStateOf(0) }
    var failed by remember(file) { mutableStateOf(false) }
    var html by remember(file, index) { mutableStateOf<String?>(null) }
    LaunchedEffect(file, index) {
        try {
            val spine = pages.ifEmpty { withContext(Dispatchers.IO) { chapters(file) } }
            pages = spine
            val page = spine[index.coerceIn(spine.indices)]
            html = withContext(Dispatchers.IO) {
                val source = ZipFile(file).use { it.contents(page, MAX_CHAPTER) }
                val body = Jsoup.parse(source.inputStream(), null, "").body()
                body.select("img[src]").forEach { img ->
                    val target = runCatching { member(page, img.attr("src")) }.getOrNull()
                    if (target == null) img.remove()
                    else img.attr("src", URI("https", BOOK_HOST, "/$target", null).toASCIIString())
                }
                val safe = Jsoup.clean(body.html(), Safelist.relaxed().removeAttributes("a", "href")
                    .removeProtocols("img", "src", "http").addProtocols("img", "src", "https"))
                """<!doctype html><html><head><meta name="viewport" content="width=device-width,initial-scale=1">
                   <meta http-equiv="Content-Security-Policy" content="default-src 'none'; img-src https://$BOOK_HOST; style-src 'unsafe-inline'">
                   <style>:root{color-scheme:light dark}body{max-width:44em;margin:auto;padding:1em;font:1.13em/1.55 serif;overflow-wrap:anywhere}img,table{max-width:100%;height:auto}</style>
                   </head><body>$safe</body></html>"""
            }
        } catch (e: kotlinx.coroutines.CancellationException) { throw e }
        catch (_: Exception) { failed = true }
    }
    if (failed) { ReaderMessage("This book could not be displayed.", "Choose another app", onOpen); return }
    val chapter = html
    if (chapter == null) { ReaderLoading(); return }
    Column(Modifier.fillMaxSize()) {
        key(index) {
            AndroidView(modifier = Modifier.weight(1f).fillMaxWidth(), factory = { context ->
                WebView(context).apply {
                    settings.allowFileAccess = false
                    settings.allowContentAccess = false
                    settings.blockNetworkLoads = true
                    webViewClient = object : WebViewClient() {
                        override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest) = true
                        override fun shouldInterceptRequest(view: WebView, request: WebResourceRequest): WebResourceResponse {
                            val uri = request.url
                            val empty = WebResourceResponse("text/plain", "utf-8", ByteArrayInputStream(byteArrayOf()))
                            if (uri.host != BOOK_HOST || uri.scheme != "https") return empty
                            val path = uri.path?.removePrefix("/") ?: return empty
                            val mime = when (path.substringAfterLast('.').lowercase()) {
                                "png" -> "image/png"; "jpg", "jpeg" -> "image/jpeg"
                                "gif" -> "image/gif"; "webp" -> "image/webp"; else -> return empty
                            }
                            return runCatching {
                                WebResourceResponse(mime, null, ZipFile(file).use { it.contents(path, MAX_IMAGE) }.inputStream())
                            }.getOrDefault(empty)
                        }
                    }
                    loadDataWithBaseURL("https://$BOOK_HOST/", chapter, "text/html", "utf-8", null)
                }
            }, onRelease = { it.destroy() })
        }
        HorizontalDivider()
        Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween) {
            TextButton(enabled = index > 0, onClick = { index-- }) { Text("Previous") }
            Text("${index + 1} / ${pages.size}", style = MaterialTheme.typography.labelLarge)
            TextButton(enabled = index < pages.lastIndex, onClick = { index++ }) { Text("Next") }
        }
    }
}
