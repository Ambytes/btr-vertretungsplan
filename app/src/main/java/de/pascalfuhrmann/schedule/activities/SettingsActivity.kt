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
package de.pascalfuhrmann.schedule.activities

import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.icu.util.Calendar
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.evernote.android.job.JobManager
import de.pascalfuhrmann.schedule.R
import de.pascalfuhrmann.schedule.notifications.DeputizeNotificationJob
import de.pascalfuhrmann.schedule.notifications.DeputizeNotificationJobCreator
import de.pascalfuhrmann.schedule.utility.PreferenceManager
import kotlinx.android.synthetic.main.settings_activity.*
import java.util.concurrent.TimeUnit

class SettingsActivity : AppCompatActivity() {
    private var mEditText: EditText? = null
    private var mEditUser: EditText? = null
    private var mEditPass: EditText? = null
    private var mButton: Button? = null
    private var mCheckNotification: CheckBox? = null
    private var mTimePickerButton: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)

        mEditText = findViewById(R.id.school_class)
        mEditUser = findViewById(R.id.notification_user)
        mEditPass = findViewById(R.id.notification_password)
        mCheckNotification = findViewById(R.id.notifications)

        val preferences = PreferenceManager.instance
        var time: Long = preferences.notificationTime
        mEditText!!.setText(preferences.schoolClass)
        mCheckNotification!!.isChecked = preferences.notifications


        mTimePickerButton = findViewById(R.id.time_picker)
        mTimePickerButton!!.setOnClickListener {
            val mCurrentTime = Calendar.getInstance()
            val hour = mCurrentTime.get(Calendar.HOUR_OF_DAY)
            val minute = mCurrentTime.get(Calendar.MINUTE)
            val mTimePicker: TimePickerDialog
            mTimePicker = TimePickerDialog(
                    this,
                    TimePickerDialog.OnTimeSetListener { _, selectedHour, selectedMinute ->
                        time = TimeUnit.HOURS.toMillis(selectedHour.toLong()) +
                                TimeUnit.MINUTES.toMillis(selectedMinute.toLong() + 16)
                    },
                    hour,
                    minute,
                    true
            )
            mTimePicker.setTitle("Wähle Benachrichtigungsuhrzeit")
            mTimePicker.show()
        }

        mButton = findViewById(R.id.confirm_button)
        mButton!!.setOnClickListener { v ->
            if (mButton != null) {
                //close keyboard
                val imm = v.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(v.windowToken, 0)

                preferences.schoolClass = mEditText!!.text.toString()
                preferences.notifications = mCheckNotification!!.isChecked
                preferences.notificationUser = mEditUser!!.text.toString()
                preferences.notificationPass = mEditUser!!.text.toString()
                preferences.notificationTime = time
                preferences.updateValues(this)

                //schedule notifications so the user doesn't have to restart the app to apply them
                if (mCheckNotification!!.isChecked) {
                    val notificationIntent = Intent(this, LoginActivity::class.java)
                    JobManager.create(this).addJobCreator(DeputizeNotificationJobCreator(this, notificationIntent))

                    //unschedule to override with new time
                    if (DeputizeNotificationJob.isScheduled()) DeputizeNotificationJob.unschedule()

                    DeputizeNotificationJob.schedule(time)
                } else {
                    DeputizeNotificationJob.unschedule()
                }

                finish()
            }
        }

    }

    override fun onBackPressed() {
        finish()
    }
}
