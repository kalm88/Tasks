package com.punchlist.app.notification

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

// FCM message payload conventions:
// data["type"]  = "task_assigned" | "task_completed" | "needs_review"
// data["itemId"]
// data["projectId"]
// data["title"]
// data["body"]

@AndroidEntryPoint
class PunchlistFirebaseMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var notificationHelper: NotificationHelper

    override fun onMessageReceived(message: RemoteMessage) {
        val title = message.notification?.title ?: message.data["title"] ?: "Punchlist"
        val body = message.notification?.body ?: message.data["body"] ?: ""
        notificationHelper.showNotification(title, body)
    }

    override fun onNewToken(token: String) {
        // TODO: Update the FCM token in Firestore for the current user
        // val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        // UserRepository.updateFcmToken(userId, token)
    }
}
