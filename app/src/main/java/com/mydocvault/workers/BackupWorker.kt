package com.mydocvault.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.mydocvault.utils.BackupManager
import com.mydocvault.utils.BackupNotificationHelper
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class BackupWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val backupManager: BackupManager,
    private val notificationHelper: BackupNotificationHelper
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val backupFile = backupManager.createBackup()
        return if (backupFile != null) {
            notificationHelper.notifyBackupSuccess(backupFile.name)
            Result.success()
        } else {
            notificationHelper.notifyBackupFailure()
            Result.retry()
        }
    }
}
