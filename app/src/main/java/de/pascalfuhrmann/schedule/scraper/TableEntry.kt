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
package de.pascalfuhrmann.schedule.scraper

import org.jsoup.select.Elements

class TableEntry {
    var schoolClass: String = ""
    var lesson: String = ""
    private var room: String = ""
    var teacher: String = ""
    private var subject: String = ""
    private var reason: String = ""
    private var changedLesson: String = ""
    private var changedRoom: String = ""
    var changedTeacher: String = ""
    private var changedSubject: String = ""
    var date: String = ""

    val essentialInfo: String
        get() {
            var baseEntry = "$subject $reason "

            baseEntry = when {
                reason.equals("Vertretung", true) || reason.contains("vorgezogen") -> {
                    baseEntry.plus("bei $changedTeacher in $changedRoom unterrichtet $subject\n")
                }
                reason.equals("Raumänderung", true) -> {
                    baseEntry.plus(" in $changedRoom\n")
                }
                reason.equals("s. oben", true) -> {
                    baseEntry.plus("Siehe Titelzeile auf der Website.")
                }
                else -> return baseEntry
            }
            return baseEntry
        }

    constructor(titleRow: Elements, detailRow: Elements, table_date: String) {
        schoolClass = titleRow.eachText()[0]
        this.date = table_date
        lesson = titleRow.eachText()[2]

        if (titleRow.eachText().size > 7) {
            room = titleRow.eachText()[3]
            teacher = titleRow.eachText()[4]
            subject = titleRow.eachText()[5]
            reason = titleRow.eachText()[8]
            if (reason.isEmpty())
                reason = "Vertretung"
        } else {
            room = ""
            teacher = titleRow.eachText()[3]
            reason = titleRow.eachText()[6]
            if (reason.isEmpty())
                reason = "Vertretung"
        }

        if (!detailRow.eachText().isEmpty()) {
            changedLesson = detailRow.eachText()[1]
            changedRoom = detailRow.eachText()[2]
            changedTeacher = detailRow.eachText()[3]
            changedSubject = detailRow.eachText()[4]
        }

        if (changedTeacher.isEmpty()) {
            changedTeacher = ""
        }
    }

    /**
     * Constructor mostly used for displaying errors in the list.
     * @param reason
     */
    constructor(reason: String) {
        this.reason = reason
    }
}
