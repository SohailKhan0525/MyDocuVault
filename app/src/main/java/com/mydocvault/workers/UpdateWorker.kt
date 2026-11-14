package com.mydocvault.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.mydocvault.utils.UpdateChecker
import com.mydocvault.utils.UpdateNotificationHelper
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit

@HiltWorker
class UpdateWorker @AssistedInject constructor(
    @Assisted context: Context, @Assisted params: WorkerParameters,
    private val checker: UpdateChecker, private val notifications: UpdateNotificationHelper
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        if (!notifications.canNotify()) return Result.success()
        return try {
            checker.checkForUpdate(applicationContext, "SohailKhan0525", "MyDocuVault", retryNetworkErrors = true)
                ?.let(notifications::notifyUpdateAvailable)
            Result.success()
        } catch (_: java.io.IOException) {
            Result.retry()
        }
    }

    companion object {
        private val network get() = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()

        fun schedule(context: Context) {
            WorkManager.getInstance(context).enqueueUniquePeriodicWork("release-checks",
                ExistingPeriodicWorkPolicy.KEEP, PeriodicWorkRequestBuilder<UpdateWorker>(6, TimeUnit.HOURS)
                    .setConstraints(network).build())
        }

        fun checkNow(context: Context) {
            WorkManager.getInstance(context).enqueueUniqueWork("release-hint", ExistingWorkPolicy.KEEP,
                OneTimeWorkRequestBuilder<UpdateWorker>().setConstraints(network).build())
        }
    }
}
