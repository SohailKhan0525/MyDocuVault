package com.mydocvault

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.mydocvault.utils.BackupNotificationHelper
import com.mydocvault.utils.UpdateNotificationHelper
import com.mydocvault.workers.BackupWorker
import dagger.hilt.android.HiltAndroidApp
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltAndroidApp
class MyDocVaultApp : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var backupNotificationHelper: BackupNotificationHelper

    @Inject
    lateinit var updateNotificationHelper: UpdateNotificationHelper

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        setupGlobalExceptionHandler()
        backupNotificationHelper.createNotificationChannel()
        updateNotificationHelper.createNotificationChannel()
        scheduleAutoBackup()
        com.mydocvault.workers.UpdateWorker.schedule(this)
    }

    private fun setupGlobalExceptionHandler() {
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, exception ->
            // Keep sensitive paths and provider details out of user-facing crash messages.
            android.util.Log.e("MyDocVaultApp", "Unhandled application error", exception)
            android.os.Handler(android.os.Looper.getMainLooper()).post {
                android.widget.Toast.makeText(
                    applicationContext,
                    "Something went wrong. Please reopen MyDocVault.",
                    android.widget.Toast.LENGTH_LONG
                ).show()
            }
            // Allow the OS default handler to report the crash; do not relaunch into
            // the same failing state or swallow the exception in an unsafe loop.
            try {
                Thread.sleep(250)
            } catch (_: InterruptedException) {
                Thread.currentThread().interrupt()
            }
            if (defaultHandler != null) {
                defaultHandler.uncaughtException(thread, exception)
            } else {
                android.os.Process.killProcess(android.os.Process.myPid())
                kotlin.system.exitProcess(1)
            }
        }
    }

    private fun scheduleAutoBackup() {
        val constraints = Constraints.Builder()
            .setRequiresBatteryNotLow(true)
            .build()

        val request = PeriodicWorkRequestBuilder<BackupWorker>(8, TimeUnit.HOURS)
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "auto_backup",
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }

}
