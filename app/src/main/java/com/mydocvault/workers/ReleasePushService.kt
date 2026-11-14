package com.mydocvault.workers

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class ReleasePushService : FirebaseMessagingService() {
    override fun onMessageReceived(message: RemoteMessage) {
        if (message.from == "/topics/stable-releases" && !message.data["version"].isNullOrBlank())
            UpdateWorker.checkNow(this)
    }
    override fun onDeletedMessages() { UpdateWorker.checkNow(this) }

    companion object {
        fun subscribe(context: Context) {
            if (FirebaseApp.getApps(context).isNotEmpty()) {
                FirebaseMessaging.getInstance().apply {
                    isAutoInitEnabled = true
                    subscribeToTopic("stable-releases")
                }
            }
        }
    }
}
