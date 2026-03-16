package com.mydocvault.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

data class UpdateInfo(
    val versionName: String,
    val notes: String,
    val apkUrl: String
)

class UpdateChecker @Inject constructor(
    private val client: OkHttpClient
) {
    suspend fun checkForUpdate(context: Context, owner: String, repo: String): UpdateInfo? = withContext(Dispatchers.IO) {
        try {
            val currentVersion = getAppVersionName(context)
            val url = "https://api.github.com/repos/$owner/$repo/releases/latest"
            val request = Request.Builder().url(url).build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext null
                val body = response.body?.string() ?: return@withContext null
                val json = JSONObject(body)
                val tagName = json.optString("tag_name")
                val notes = json.optString("body")
                val assets = json.optJSONArray("assets")
                var apkUrl = ""
                if (assets != null) {
                    for (i in 0 until assets.length()) {
                        val asset = assets.getJSONObject(i)
                        val name = asset.optString("name")
                        val urlStr = asset.optString("browser_download_url")
                        if (name.endsWith(".apk") && !name.endsWith("-debug.apk")) {
                            apkUrl = urlStr
                            break
                        }
                    }
                }
                if (apkUrl.isBlank()) return@withContext null
                if (!VersionComparator.isNewerVersion(tagName, currentVersion)) return@withContext null
                UpdateInfo(tagName, notes, apkUrl)
            }
        } catch (_: Exception) {
            null
        }
    }

    suspend fun downloadApk(
        context: Context,
        apkUrl: String,
        onProgress: (Int) -> Unit
    ): File = withContext(Dispatchers.IO) {
        val request = Request.Builder().url(apkUrl).build()
        val file = File(context.getExternalFilesDir(null), "mydocvault_update.apk")
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) error("Download failed")
            val body = response.body ?: error("Empty response")
            val total = body.contentLength().coerceAtLeast(1)
            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
            var downloaded = 0L
            FileOutputStream(file).use { sink ->
                body.byteStream().use { input ->
                    while (true) {
                        val read = input.read(buffer)
                        if (read <= 0) break
                        sink.write(buffer, 0, read)
                        downloaded += read
                        val percent = ((downloaded * 100) / total).toInt().coerceIn(0, 100)
                        onProgress(percent)
                    }
                }
            }
        }
        file
    }

    fun startInstall(context: Context, apkFile: File) {
        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            apkFile
        )
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    private fun getAppVersionName(context: Context): String {
        val packageManager = context.packageManager
        val packageName = context.packageName
        val info = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            packageManager.getPackageInfo(packageName, android.content.pm.PackageManager.PackageInfoFlags.of(0))
        } else {
            @Suppress("DEPRECATION")
            packageManager.getPackageInfo(packageName, 0)
        }
        return info.versionName ?: "0"
    }
}
