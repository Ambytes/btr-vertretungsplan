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

import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.evernote.android.job.JobManager
import com.google.android.material.snackbar.Snackbar
import de.pascalfuhrmann.schedule.adapter.ScheduleAdapter
import de.pascalfuhrmann.schedule.R
import de.pascalfuhrmann.schedule.notifications.DeputizeNotificationJob
import de.pascalfuhrmann.schedule.notifications.DeputizeNotificationJobCreator
import de.pascalfuhrmann.schedule.scraper.TableEntry
import de.pascalfuhrmann.schedule.scraper.parse
import de.pascalfuhrmann.schedule.scraper.searchSort
import de.pascalfuhrmann.schedule.scraper.sortByClass
import de.pascalfuhrmann.schedule.utility.PreferenceManager
import java.util.*

class MainActivity : AppCompatActivity() {
    private var mRecyclerView: RecyclerView? = null
    private var mEditText: EditText? = null
    private var entryList: ArrayList<TableEntry> = ArrayList()
    private var sortedList: ArrayList<TableEntry> = ArrayList()
    private var schoolClass = "DI71"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)

        mEditText = findViewById(R.id.search_bar)
        mEditText!!.setOnKeyListener(DoneOnEditorActionListener())

        val preferences = PreferenceManager.instance

        if (!preferences.schoolClass!!.isBlank()) {
            mEditText!!.setText(schoolClass)
            schoolClass = preferences.schoolClass!!
        }

        if (preferences.notifications) {
            val notificationIntent = Intent(this, LoginActivity::class.java)
            JobManager.create(this).addJobCreator(DeputizeNotificationJobCreator(this, notificationIntent))
            DeputizeNotificationJob.schedule(preferences.notificationTime)
        } else {
            JobManager.create(this)
            DeputizeNotificationJob.unschedule()
        }

        //get table data which was already requested
        @Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS", "NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
        entryList = parse(intent.extras!!.getString("htmlContent")!!)
        sortedList = sortByClass(entryList, schoolClass)
        entryList = sortedList

        mRecyclerView = findViewById(R.id.rv)
        mRecyclerView!!.setHasFixedSize(true)
        val llm = LinearLayoutManager(baseContext)
        mRecyclerView!!.layoutManager = llm
        mRecyclerView!!.adapter = ScheduleAdapter(entryList)

        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_activity_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.settings -> {
                val intent = Intent(this, SettingsActivity::class.java)
                intent.addCategory(Intent.CATEGORY_ALTERNATIVE)
                startActivity(intent)
                return true
            }
            R.id.about -> {
                val toast = Toast.makeText(this,
                        "Developer: Pascal Fuhrmann",
                        Toast.LENGTH_LONG)
                toast.show()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    /**
     * A custom actionlistener to detect if the user hits enter/done at the
     * edittext-view. Uses the information to update the list view accordingly.
     */
    private inner class DoneOnEditorActionListener : View.OnKeyListener {
        override fun onKey(v: View, keyCode: Int, event: KeyEvent): Boolean {
            return if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                when {
                    mEditText!!.text.toString().isEmpty() -> {
                        sortedList = entryList
                        updateAdapter()
                    }
                    else -> {
                        sortedList = searchSort(entryList, mEditText!!.text.toString())
                        when {
                            sortedList[0].essentialInfo.contains("No search") -> {
                                val errorMessage = Snackbar.make(findViewById(R.id.main_activity),
                                        "No search results found.",
                                        Snackbar.LENGTH_LONG)
                                errorMessage.show()
                            }
                            else -> updateAdapter()
                        }
                    }
                }
                true
            } else false
        }


        /**
         * Set's a new list as the adapters content and updates it.
         */
        private fun updateAdapter() {
            mRecyclerView!!.adapter = ScheduleAdapter(entryList)
            mRecyclerView!!.adapter!!.notifyDataSetChanged()
        }

    }
}
