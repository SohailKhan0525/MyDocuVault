package com.mydocvault

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.mydocvault.utils.BackupNotificationHelper
import com.mydocvault.utils.UpdateChecker
import com.mydocvault.utils.UpdateNotificationHelper
import com.mydocvault.workers.BackupWorker
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
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

    @Inject
    lateinit var updateChecker: UpdateChecker

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

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
        checkForUpdatesOnLaunch()
    }

    private fun setupGlobalExceptionHandler() {
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, exception ->
            android.os.Handler(android.os.Looper.getMainLooper()).post {
                android.widget.Toast.makeText(
                    applicationContext,
                    "Error: ${exception.message ?: "Unknown"}",
                    android.widget.Toast.LENGTH_LONG
                ).show()
            }
            // Give time for the toast to be added to the queue
            try { Thread.sleep(1000) } catch (e: InterruptedException) {}
            // Still call default handler to let the OS know it crashed, 
            // but the user will see the toast instead of just disappearing instantly.
            // But user asked "instead of crash". If we swallow, we must loop.
            // Looping main thread after crash is risky. Let's start an Intent to restart and show toast.
            val intent = android.content.Intent(applicationContext, MainActivity::class.java).apply {
                putExtra("global_error", exception.message)
                addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK)
            }
            applicationContext.startActivity(intent)
            android.os.Process.killProcess(android.os.Process.myPid())
            System.exit(1)
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

    private fun checkForUpdatesOnLaunch() {
        appScope.launch {
            try {
                val updateInfo = updateChecker.checkForUpdate(this@MyDocVaultApp, "SohailKhan0525", "MyDocuVault")
                if (updateInfo != null) {
                    updateNotificationHelper.notifyUpdateAvailable(updateInfo.versionName)
                }
            } catch (e: Exception) {
                android.util.Log.w("MyDocVaultApp", "Update check on launch failed: ${e.message}")
            }
        }
    }

    override fun onTerminate() {
        super.onTerminate()
        appScope.coroutineContext[kotlinx.coroutines.Job]?.cancel()
    }
}