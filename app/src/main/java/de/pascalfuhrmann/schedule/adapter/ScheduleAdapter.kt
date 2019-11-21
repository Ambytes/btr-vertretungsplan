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
package de.pascalfuhrmann.schedule.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import de.pascalfuhrmann.schedule.R
import de.pascalfuhrmann.schedule.scraper.TableEntry

class ScheduleAdapter(private val tableEntries: ArrayList<TableEntry>) :
        RecyclerView.Adapter<ScheduleAdapter.DataEntryViewHolder>() {

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder.
    // Each data item is just a string in this case that is shown in a TextView.
    class DataEntryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cv: CardView = itemView.findViewById(R.id.cv)
        val classTextView: TextView = itemView.findViewById(R.id.classTextView)
        val dateTextView: TextView = itemView.findViewById(R.id.dateTextView)
        val lessonTextView: TextView = itemView.findViewById(R.id.lessonTextView)
        val descriptionTextView: TextView = itemView.findViewById(R.id.descriptionTextView)
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(parent: ViewGroup,
                                    viewType: Int): DataEntryViewHolder {
        val v: View = LayoutInflater.from(parent.context)
                .inflate(R.layout.list, parent, false)
        return DataEntryViewHolder(v)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: DataEntryViewHolder, position: Int) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        holder.classTextView.text = tableEntries[position].schoolClass
        holder.dateTextView.text = tableEntries[position].date
        holder.lessonTextView.text = tableEntries[position].lesson
        holder.descriptionTextView.text = tableEntries[position].essentialInfo
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = tableEntries.size
}