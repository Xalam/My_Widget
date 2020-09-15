package com.example.mywidget

import android.app.job.JobParameters
import android.app.job.JobService
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.os.Build
import android.widget.RemoteViews
import androidx.annotation.RequiresApi

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
class MyWidgetService: JobService() {
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onStartJob(job: JobParameters?): Boolean {
        val manager = AppWidgetManager.getInstance(this)
        val view = RemoteViews(packageName, R.layout.random_number_widget)
        val theWidget = ComponentName(this, RandomNumberWidget::class.java)
        val lastUpdate = "Random : " + NumberGenerator.generate(100)
        view.setTextViewText(R.id.appwidget_text, lastUpdate)
        manager.updateAppWidget(theWidget, view)
        jobFinished(job, false)
        return true
    }

    override fun onStopJob(job: JobParameters?): Boolean = false
}