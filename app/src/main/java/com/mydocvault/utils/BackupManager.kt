package com.mydocvault.utils

import android.content.Context
import android.os.Environment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackupManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val DB_NAME = "vault.db"
        private const val DOCS_ENTRY_PREFIX = "documents/"
        private const val DB_ENTRY = "db/$DB_NAME"
        private const val DB_WAL_ENTRY = "db/$DB_NAME-wal"
        private const val DB_SHM_ENTRY = "db/$DB_NAME-shm"
        private const val BACKUP_DIR_NAME = "MyDocuVaultBackup"
    }

    private val docsDir: File get() = File(context.filesDir, "documents")

    /** Creates a ZIP backup of all documents and the Room database.
     *  Returns the resulting backup [File], or null on failure. */
    suspend fun createBackup(): File? = withContext(Dispatchers.IO) {
        try {
            val backupDir = getOrCreateBackupDir() ?: return@withContext null
            val timestamp = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.US).format(Date())
            val zipFile = File(backupDir, "MyDocuVault_backup_$timestamp.zip")

            ZipOutputStream(FileOutputStream(zipFile).buffered()).use { zos ->
                // Add documents
                docsDir.walkTopDown()
                    .filter { it.isFile }
                    .forEach { file ->
                        val entryName = DOCS_ENTRY_PREFIX + file.relativeTo(docsDir).path
                        zos.putNextEntry(ZipEntry(entryName))
                        FileInputStream(file).use { it.copyTo(zos) }
                        zos.closeEntry()
                    }

                // Add Room database files
                addDbFileToZip(zos, context.getDatabasePath(DB_NAME), DB_ENTRY)
                addDbFileToZip(zos, File(context.getDatabasePath(DB_NAME).path + "-wal"), DB_WAL_ENTRY)
                addDbFileToZip(zos, File(context.getDatabasePath(DB_NAME).path + "-shm"), DB_SHM_ENTRY)
            }
            zipFile
        } catch (_: Exception) {
            null
        }
    }

    /** Restores documents and the Room database from [zipFile].
     *  Returns true on success. The caller should restart the app after restoration. */
    suspend fun restoreBackup(zipFile: File): Boolean = withContext(Dispatchers.IO) {
        val tempDir = File(context.cacheDir, "restore_tmp_${System.currentTimeMillis()}")
        try {
            tempDir.mkdirs()

            // Extract zip to temp dir
            ZipInputStream(FileInputStream(zipFile).buffered()).use { zis ->
                var entry = zis.nextEntry
                while (entry != null) {
                    val outFile = File(tempDir, entry.name)
                    if (entry.isDirectory) {
                        outFile.mkdirs()
                    } else {
                        outFile.parentFile?.mkdirs()
                        FileOutputStream(outFile).use { out -> copyStream(zis, out) }
                    }
                    zis.closeEntry()
                    entry = zis.nextEntry
                }
            }

            // Restore documents
            val extractedDocs = File(tempDir, "documents")
            if (extractedDocs.exists()) {
                docsDir.deleteRecursively()
                docsDir.mkdirs()
                extractedDocs.copyRecursively(docsDir, overwrite = true)
            }

            // Restore database files
            restoreDbFile(tempDir, "db/$DB_NAME", DB_NAME)
            restoreDbFile(tempDir, "db/$DB_NAME-wal", "$DB_NAME-wal")
            restoreDbFile(tempDir, "db/$DB_NAME-shm", "$DB_NAME-shm")

            true
        } catch (_: Exception) {
            false
        } finally {
            tempDir.deleteRecursively()
        }
    }

    private fun getOrCreateBackupDir(): File? {
        return try {
            File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
                BACKUP_DIR_NAME
            ).apply { mkdirs() }
        } catch (_: Exception) {
            null
        }
    }

    private fun addDbFileToZip(zos: ZipOutputStream, file: File, entryName: String) {
        if (!file.exists()) return
        zos.putNextEntry(ZipEntry(entryName))
        FileInputStream(file).use { it.copyTo(zos) }
        zos.closeEntry()
    }

    private fun restoreDbFile(tempDir: File, srcRelPath: String, dbFileName: String) {
        val src = File(tempDir, srcRelPath)
        if (!src.exists()) return
        val dest = context.getDatabasePath(dbFileName)
        dest.parentFile?.mkdirs()
        src.copyTo(dest, overwrite = true)
    }

    private fun copyStream(input: InputStream, output: FileOutputStream) {
        val buffer = ByteArray(8192)
        var bytesRead: Int
        while (input.read(buffer).also { bytesRead = it } != -1) {
            output.write(buffer, 0, bytesRead)
        }
    }
}
