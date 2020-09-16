package com.example.mywidget

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.example.mywidget.notif.NotificationService
import com.example.mywidget.stack_notif.NotificationItem
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), View.OnClickListener {
    private var idNotif = 0
    private val stackNotif = ArrayList<NotificationItem>()

    companion object {
        private const val JOB_ID = 100
        private const val SCHEDULE_OF_PERIOD = 86000L
        val NOTIFICATION_ID = 1
        var CHANNEL_ID = "channel_01"
        var CHANNEL_NAME: CharSequence = "xalam channel"
        private const val CHANNEL_NAME_STACK = "my channel"
        private const val GROUP_KEY_EMAILS = "group_key_emails"
        private const val NOTIFICATION_REQUEST_CODE = 200
        private const val MAX_NOTIFICATION = 2

    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btn_start.setOnClickListener(this)
        btn_stop.setOnClickListener(this)
        button_show_notification.setOnClickListener(this)
        btnSend.setOnClickListener(this)
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onClick(view: View?) {
        when(view?.id){
            R.id.btn_start -> startJob()
            R.id.btn_stop -> stopJob()
            R.id.button_show_notification -> startService(Intent(this, NotificationService::class.java))
            R.id.btnSend -> stackNotif()
        }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun startJob(){
        val mServiceComponent = ComponentName(this, MyWidgetService::class.java)
        val builder = JobInfo.Builder(JOB_ID, mServiceComponent)
        builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_NONE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
            builder.setPeriodic(900000) //15 menit
        } else {
            builder.setPeriodic(86000) //3menit
        }

        val jobScheduler = getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
        jobScheduler.schedule(builder.build())

        Toast.makeText(this, "Job Service started", Toast.LENGTH_SHORT).show()
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun stopJob(){
        val tm = getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
        tm.cancel(JOB_ID)
        Toast.makeText(this, "Job Service canceled", Toast.LENGTH_SHORT).show()
    }

    fun sendNotif(view: View) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("http://dicoding.com")) //Can Also Intent Activity
        val pendingIntent = PendingIntent.getActivity(this,0, intent,0)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentIntent(pendingIntent)
            .setSmallIcon(R.drawable.ic_assistant)
            .setLargeIcon(BitmapFactory.decodeResource(resources, R.drawable.ic_assistant))
            .setContentTitle(resources.getString(R.string.content_title))
            .setContentText(resources.getString(R.string.content_text))
            .setSubText(resources.getString(R.string.subtext))
            .setAutoCancel(true)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT)
            channel.description = CHANNEL_NAME as String?
            builder.setChannelId(CHANNEL_ID)

            notificationManager.createNotificationChannel(channel)
        }
        val notification = builder.build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    fun stackNotif(){
        val sender = edtSender.text.toString()
        val message = edtMessage.text.toString()
        if (sender.isEmpty() || message.isEmpty()){
            Toast.makeText(this@MainActivity, "Data Harus Diisi", Toast.LENGTH_SHORT).show()
        } else {
            stackNotif.add(NotificationItem(idNotif, sender, message))
            sendNotifStack()
            idNotif++
            edtSender.setText("")
            edtMessage.setText("")

            val methodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            methodManager.hideSoftInputFromWindow(edtMessage.windowToken, 0)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        stackNotif.clear()
        idNotif = 0
    }

    private fun sendNotifStack(){
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val largeIcon = BitmapFactory.decodeResource(resources, R.drawable.ic_assistant)
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        val pendingIntent = PendingIntent.getActivity(this, NOTIFICATION_REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        val builder: NotificationCompat.Builder

        val CHANNEL_ID = "channel_01"
        if(idNotif < MAX_NOTIFICATION){
            builder = NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("New Email From " + stackNotif[idNotif].sender)
                .setContentText(stackNotif[idNotif].message)
                .setSmallIcon(R.drawable.ic_assistant)
                .setLargeIcon(largeIcon)
                .setGroup(GROUP_KEY_EMAILS)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
        } else {
            val inboxStyle = NotificationCompat.InboxStyle()
                .addLine("New Email from " + stackNotif[idNotif].sender)
                .addLine("New Email from " + stackNotif[idNotif - 1].sender)
                .setBigContentTitle("$idNotif new emails")
                .setSummaryText("mail@xalam")
            builder = NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("$idNotif new emails")
                .setContentText("mail@dicoding.com")
                .setSmallIcon(R.drawable.ic_mail)
                .setGroup(GROUP_KEY_EMAILS)
                .setGroupSummary(true)
                .setContentIntent(pendingIntent)
                .setStyle(inboxStyle)
                .setAutoCancel(true)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            /* Create or update. */
            val channel = NotificationChannel(CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT)

            builder.setChannelId(CHANNEL_ID)

            notificationManager.createNotificationChannel(channel)
        }

        val notification = builder.build()

        notificationManager.notify(idNotif, notification)


    }
}
