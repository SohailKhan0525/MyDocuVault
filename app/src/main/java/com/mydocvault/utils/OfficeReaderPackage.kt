package com.mydocvault.utils

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.IOException

object OfficeReaderPackage {
    const val ID = "com.mydocvault.reader"
    private const val REVISION = 1
    private val client = OkHttpClient()

    fun isInstalled(context: Context): Boolean = context.packageManager.run {
        try {
            @Suppress("DEPRECATION")
            val info = getPackageInfo(ID, 0)
            @Suppress("DEPRECATION")
            info.versionCode >= REVISION && checkSignatures(context.packageName, ID) == PackageManager.SIGNATURE_MATCH
        } catch (_: PackageManager.NameNotFoundException) {
            false
        }
    }

    // Check the signing identity and package before handing the APK to Android's installer.
    @Suppress("DEPRECATION")
    private fun isExpectedPackage(context: Context, file: File): Boolean {
        val pm = context.packageManager
        val archive = pm.getPackageArchiveInfo(file.path, PackageManager.GET_SIGNATURES) ?: return false
        val ours = pm.getPackageInfo(context.packageName, PackageManager.GET_SIGNATURES).signatures
        return archive.packageName == ID && archive.versionCode == REVISION &&
            !ours.isNullOrEmpty() && archive.signatures?.toSet() == ours.toSet()
    }

    suspend fun download(context: Context, progress: (Int) -> Unit): File = withContext(Dispatchers.IO) {
        val abi = Build.SUPPORTED_ABIS.firstOrNull { it in setOf("arm64-v8a", "armeabi-v7a", "x86_64", "x86") }
            ?: throw IOException("This processor is unsupported.")
        val name = "MyDocuVault-Reader-$REVISION-$abi.apk"
        val dest = File(context.cacheDir, name)
        if (dest.isFile && isExpectedPackage(context, dest)) return@withContext dest
        val tmp = File.createTempFile("reader-", ".part", context.cacheDir)
        val request = Request.Builder().url("https://github.com/SohailKhan0525/MyDocuVault/releases/download/v1.17.0/$name").build()
        try {
            client.newCall(request).execute().use { response ->
                val body = response.body
                if (!response.isSuccessful || body == null) throw IOException("Reader download unavailable.")
                val total = body.contentLength()
                var count = 0L
                var last = -1
                val buffer = ByteArray(65536)
                body.byteStream().use { input ->
                    tmp.outputStream().use { output ->
                        while (true) {
                            currentCoroutineContext().ensureActive()
                            val read = input.read(buffer)
                            if (read < 0) break
                            count += read
                            if (count > 180_000_000L) throw IOException("Unexpected reader size.")
                            output.write(buffer, 0, read)
                            if (total > 0) {
                                val value = (count * 100 / total).toInt().coerceIn(0, 99)
                                if (value != last) { last = value; progress(value) }
                            }
                        }
                    }
                }
            }
            if (!isExpectedPackage(context, tmp)) throw IOException("Reader verification failed.")
            java.nio.file.Files.move(tmp.toPath(), dest.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING)
            dest
        } finally {
            tmp.delete()
        }
    }
}
