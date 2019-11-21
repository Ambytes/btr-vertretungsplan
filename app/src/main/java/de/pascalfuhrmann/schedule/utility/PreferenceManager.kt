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
package de.pascalfuhrmann.schedule.utility

import android.content.Context
import android.util.Log

class PreferenceManager {
    var settings:               String? = null
    var userData:               String? = null
    var keyStorage:             String? = null
    var schoolClass:            String? = null
    var notifications:          Boolean = false
    var notificationUser:       String? = null
    var notificationPass:       String? = null
    var notificationTime:       Long    = 0

    fun initialize(context: Context) {
        val preferences = context.getSharedPreferences(SETTINGS, Context.MODE_PRIVATE)
        preferences.all.forEach { (key, value) ->
            when (key) {
                SETTINGS            -> settings         = value as String
                USER_DATA           -> userData         = value as String
                KEY_STORAGE         -> keyStorage       = value as String
                SCHOOL_CLASS        -> schoolClass      = value as String
                NOTIFICATIONS       -> notifications    = value as Boolean
                NOTIFICATION_USER   -> notificationUser = value as String
                NOTIFICATION_PASS   -> notificationPass = value as String
                NOTIFICATION_TIME   -> notificationTime = value as Long
            }
        }
    }

    fun updateValues(context: Context) {
        val preferences = context.getSharedPreferences(SETTINGS, Context.MODE_PRIVATE)
        val editor = preferences.edit()

        //editor.putString(KEY_STORAGE, keyStorage) //unnecessary since it is always the same
        editor.putString(SETTINGS, settings)
        editor.putString(USER_DATA, userData)
        editor.putString(SCHOOL_CLASS, schoolClass)
        editor.putBoolean(NOTIFICATIONS, notifications)
        editor.putString(NOTIFICATION_USER, notificationUser)
        editor.putString(NOTIFICATION_PASS, notificationPass)
        editor.putLong(NOTIFICATION_TIME, notificationTime)
        editor.apply()
    }

    //suppressed since I need to be 100% sure it is written before I continue
    fun setUserData(context: Context, messageEncrypted: String) {
        userData = messageEncrypted
        Log.d("HELPINPREFERENCE", this.userData!!)
        val preferences = context.getSharedPreferences(SETTINGS, Context.MODE_PRIVATE)
        val editor = preferences.edit()
        editor.putString(USER_DATA, messageEncrypted)
        editor.apply()
    }

    companion object {
        val instance: PreferenceManager = PreferenceManager()
        const val SETTINGS          = "SettingsActivity"
        const val USER_DATA         = "userdata"
        const val KEY_STORAGE       = "keystorage"
        const val SCHOOL_CLASS      = "schoolClass"
        const val NOTIFICATIONS     = "notifications"
        const val NOTIFICATION_USER = "notifcationUser"
        const val NOTIFICATION_PASS = "notifcationPass"
        const val NOTIFICATION_TIME = "notificationTime"
    }
}