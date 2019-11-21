/* Copyright 2019 Pascal Fuhrmann

    This file is part of substitution_schedule.

    substitution_schedule is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    substitution_schedule is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with substitution_schedule.  If not, see <http://www.gnu.org/licenses/>.
  */

package de.pascalfuhrmann.schedule.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.evernote.android.job.DailyJob
import com.evernote.android.job.JobManager
import com.evernote.android.job.JobRequest
import de.pascalfuhrmann.schedule.R
import de.pascalfuhrmann.schedule.scraper.TableEntry
import de.pascalfuhrmann.schedule.scraper.parse
import de.pascalfuhrmann.schedule.authentication.requestData
import de.pascalfuhrmann.schedule.scraper.searchSort
import de.pascalfuhrmann.schedule.utility.PreferenceManager
import java.util.concurrent.TimeUnit
import java.util.function.Consumer

class DeputizeNotificationJob(private val con: Context, private val intent: Intent) : DailyJob() {
    private var result: Pair<Boolean, String> = Pair(false, "")
    private var entryList: ArrayList<TableEntry> = ArrayList()
    private var schoolClass = "DI71"
    private var user = ""
    private var pass = ""

    override fun onRunDailyJob(params: Params): DailyJobResult {
        val preferences = PreferenceManager.instance
        preferences.initialize(con) //let's be sure there is data
        schoolClass = preferences.schoolClass!!
        user = preferences.notificationUser!!
        pass = preferences.notificationPass!!

        if (schoolClass.isBlank()) {
            return DailyJobResult.CANCEL
        }

        receiveNotificationData()
        if (!result.first) {
            //no login success
            return DailyJobResult.CANCEL
        }
        sendNotification()
        return DailyJobResult.SUCCESS
    }

    private fun receiveNotificationData() {
        if (user.isBlank() || pass.isBlank()) return
        result = requestData(user, pass)
        if (result.first) entryList = parse(result.second)
    }

    private fun sendNotification() {
        createNotificationChannel(con)
        val s = searchSort(entryList, schoolClass).withIndex()
        if (s.first().value.essentialInfo.contains("Keine Einträge")) return

        for ((c, n) in s) {
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            val mBuilder = NotificationCompat.Builder(con, CHANNEL_ID)
                    .setSmallIcon(R.drawable.baseline_calendar_today_white_18)
                    .setContentTitle(n.schoolClass)
                    .setContentText(n.date)
                    .setStyle(NotificationCompat.BigTextStyle()
                            .bigText(n.essentialInfo))
                    .setPriority(NotificationCompat.PRIORITY_MAX)
                    .setShowWhen(true)
                    .setColorized(true)
                    .setColor(1100)
            val notificationManager = NotificationManagerCompat.from(con)
            notificationManager.notify(c, mBuilder.build())
        }
    }

    private fun createNotificationChannel(context: Context) {
        val description = "Spawns notification about the substitution schedule."
        val channel = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH)
        } else {
            TODO("VERSION.SDK_INT < O")
        }
        channel.description = description
        val notificationManager = context.getSystemService<NotificationManager>(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
    }

    companion object {
        private const val CHANNEL_NAME = "deputize_notification_channel"
        private const val CHANNEL_ID = "deputize_notification_btr"
        const val TAG = "deputize_notification_job"

        fun schedule(time: Long) {
            if (isScheduled()) return

            //start time window for the job 15min before the user defined time
            val timeBefore = time - TimeUnit.MINUTES.toMillis(10)

            schedule(JobRequest.Builder(TAG), timeBefore, time)
        }

        fun unschedule() {
            val jobs = JobManager.instance().getAllJobRequestsForTag(TAG)
            if (jobs.isNotEmpty()) {
                jobs.forEach(Consumer<JobRequest> { it.cancelAndEdit() })
            }
        }

        fun isScheduled(): Boolean {
            return !JobManager.instance().getAllJobRequestsForTag(TAG).isEmpty()
        }

        fun executeOnce() {
            startNowOnce(JobRequest.Builder(TAG))
        }
    }
}
