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

import android.content.Context
import android.content.Intent

import com.evernote.android.job.Job
import com.evernote.android.job.JobCreator

class DeputizeNotificationJobCreator(private val context: Context, private val intent: Intent) : JobCreator {
    override fun create(tag: String): Job? {
        return when (tag) {
            DeputizeNotificationJob.TAG -> DeputizeNotificationJob(this.context, this.intent)
            else -> null
        }
    }
}
