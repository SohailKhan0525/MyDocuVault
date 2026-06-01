package com.mydocvault.utils

import android.content.Context
import com.mydocvault.data.db.VaultDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackupManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val db: VaultDatabase
) {
    companion object {
        private const val DB_NAME = "vault.db"
        private const val DOCS_ENTRY_PREFIX = "documents/"
        private const val DB_ENTRY = "db/$DB_NAME"
        private const val DB_WAL_ENTRY = "db/$DB_NAME-wal"
        private const val DB_SHM_ENTRY = "db/$DB_NAME-shm"
        private const val BACKUP_DIR_NAME = "MyDocuVaultBackup"
        private const val BACKUP_FILE_PREFIX = "MyDocuVault_backup_"
        private const val BACKUP_FILE_SUFFIX = ".zip"
    }

    private val docsDir: File get() = File(context.filesDir, "documents")

    /** Deletes all existing backup ZIP files in provided backup directories. */
    private fun deleteOldBackups(vararg backupDirs: File) {
        backupDirs.forEach { backupDir ->
            backupDir.listFiles { file ->
                file.isFile && file.name.endsWith(BACKUP_FILE_SUFFIX)
            }?.forEach { file ->
            if (!file.delete()) {
                android.util.Log.w("BackupManager", "Failed to delete old backup: ${file.name}")
            }
        }
        }
    }

    /** Creates a ZIP backup of all documents and the Room database.
     *  Deletes any previous backups and creates a new timestamped backup file.
     *  Returns the resulting backup [File], or null on failure. */
    suspend fun createBackup(): File? = withContext(Dispatchers.IO) {
        try {
            val backupDir = getOrCreateBackupDir() ?: return@withContext null
            val legacyDir = getLegacyBackupDir()

            // Remove old backups before creating a fresh one
            deleteOldBackups(backupDir, legacyDir)

            val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
            val zipFile = File(backupDir, "$BACKUP_FILE_PREFIX$timestamp$BACKUP_FILE_SUFFIX")

            // Checkpoint WAL so all committed data is flushed into the main DB file
            try {
                db.openHelper.writableDatabase.execSQL("PRAGMA wal_checkpoint(FULL)")
            } catch (_: Exception) { /* best-effort; proceed even if checkpoint fails */ }

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
            var hasDbEntry = false
            ZipInputStream(FileInputStream(zipFile).buffered()).use { zis ->
                var entry = zis.nextEntry
                while (entry != null) {
                    val outFile = secureZipTarget(tempDir, entry.name)
                    if (entry.name == DB_ENTRY) hasDbEntry = true
                    if (entry.isDirectory) {
                        outFile.mkdirs()
                    } else {
                        outFile.parentFile?.mkdirs()
                        BufferedOutputStream(FileOutputStream(outFile)).use { out -> copyStream(zis, out) }
                    }
                    zis.closeEntry()
                    entry = zis.nextEntry
                }
            }
            if (!hasDbEntry) return@withContext false

            // Restore documents
            val extractedDocs = File(tempDir, "documents")
            if (extractedDocs.exists()) {
                docsDir.deleteRecursively()
                docsDir.mkdirs()
                extractedDocs.copyRecursively(docsDir, overwrite = true)
            }

            // Checkpoint WAL so all pending writes are flushed before closing
            try {
                db.openHelper.writableDatabase.execSQL("PRAGMA wal_checkpoint(FULL)")
            } catch (_: Exception) { /* best-effort */ }

            // Close the database before replacing its files so Room releases all file locks
            db.close()

            // Remove stale WAL / SHM files before restoring to guarantee a clean SQLite state
            context.getDatabasePath("$DB_NAME-wal").delete()
            context.getDatabasePath("$DB_NAME-shm").delete()

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
            // Use app-specific Documents directory for better visibility in file managers.
            // Falls back to internal storage if external storage is unavailable.
            val docsBaseDir = context.getExternalFilesDir(android.os.Environment.DIRECTORY_DOCUMENTS)
            val dir = docsBaseDir?.let { File(it, BACKUP_DIR_NAME) }
                ?: File(context.filesDir, BACKUP_DIR_NAME)
            dir.apply { mkdirs() }
        } catch (_: Exception) {
            null
        }
    }

    private fun getLegacyBackupDir(): File {
        return context.getExternalFilesDir(BACKUP_DIR_NAME) ?: File(context.filesDir, BACKUP_DIR_NAME)
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

    private fun secureZipTarget(rootDir: File, entryName: String): File {
        val outFile = File(rootDir, entryName)
        val canonicalRoot = rootDir.canonicalPath + File.separator
        val canonicalOut = outFile.canonicalPath
        require(canonicalOut.startsWith(canonicalRoot)) { "Invalid backup entry path: $entryName" }
        return outFile
    }

    private fun copyStream(input: InputStream, output: java.io.OutputStream) {
        val buffer = ByteArray(8192)
        var bytesRead: Int
        while (input.read(buffer).also { bytesRead = it } != -1) {
            output.write(buffer, 0, bytesRead)
        }
    }
}
