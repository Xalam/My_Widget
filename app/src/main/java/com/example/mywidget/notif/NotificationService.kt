package com.example.mywidget.notif

import android.app.*
import android.content.Intent
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.example.mywidget.R
import com.example.mywidget.ReplyActivity

class NotificationService : IntentService("NotificationService") {
    companion object {
        private const val KEY_REPLY = "key_reply_message"
        const val REPLY_ACTION = "com.dicoding.notification.directreply.REPLY_ACTION"
        const val CHANNEL_ID = "channel_01"
        val CHANNEL_NAME: CharSequence = "dicoding channel"

        @RequiresApi(Build.VERSION_CODES.KITKAT_WATCH)
        fun getReplyMessage(intent: Intent): CharSequence? {
            val remoteInput = RemoteInput.getResultsFromIntent(intent)
            return remoteInput?.getCharSequence(KEY_REPLY)
        }
    }

    private var mNotificationId: Int = 0
    private var mMessageId: Int = 0

    override fun onHandleIntent(intent: Intent?) {
        if(intent != null){
            showNotification()
        }
    }

    @RequiresApi(Build.VERSION_CODES.KITKAT_WATCH)
    private fun showNotification() {
        mNotificationId = 1
        mMessageId = 1

        val replyLabel = getString(R.string.notif_action_reply)
        val remoteInput = androidx.core.app.RemoteInput.Builder(KEY_REPLY)
            .setLabel(replyLabel)
            .build()

        val replyAction = NotificationCompat.Action.Builder(R.drawable.ic_send, replyLabel, getReplyPendingIntent())
            .addRemoteInput(remoteInput)
            .setAllowGeneratedReplies(true)
            .build()

        val mBuilder = NotificationCompat.Builder(this,
            CHANNEL_ID
        )
            .setSmallIcon(R.drawable.ic_assistant)
            .setContentTitle(getString(R.string.notif_title))
            .setContentText(getString(R.string.notif_content))
            .setShowWhen(true)
            .addAction(replyAction)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            /* Create or update. */
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT)

            mBuilder.setChannelId(CHANNEL_ID)
            notificationManager.createNotificationChannel(channel)
        }

        val notification = mBuilder.build()
        notificationManager.notify(mNotificationId, notification)
    }

    private fun getReplyPendingIntent(): PendingIntent {
        val intent: Intent
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent = NotificationBroadcastReceiver.getReplyMessageIntent(this, mNotificationId, mMessageId)
            PendingIntent.getBroadcast(applicationContext, 100, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        } else {
            intent =
                ReplyActivity.getReplyMessageIntent(
                    this,
                    mNotificationId,
                    mMessageId
                )
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            PendingIntent.getActivity(this, 100, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        }
    }

}
