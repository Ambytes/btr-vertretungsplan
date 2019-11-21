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

    Contributors: Pascal Fuhrmann, Jonas Lauschke
  */
package de.pascalfuhrmann.schedule.scraper

import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import java.util.*
import java.util.regex.Pattern

/**
 * Returns a list of strings which only contains searched elements.
 * Checks for class shorthands/teacher shorthands.
 */
fun searchSort(entryList: ArrayList<TableEntry>, search: String): ArrayList<TableEntry> {
    val parsedList = ArrayList<TableEntry>()

    //comparing all school class shorthands to the search input
    for (entry in entryList) {
        if (entry.schoolClass.equals(search, ignoreCase = true)) {
            parsedList.add(entry)
        }
    }

    //comparing all teacher shorthands to the search input
    for (entry in entryList) {
        val teacher = entry.teacher.substring(0, 2).equals(search, ignoreCase = true)
        var changedTeacher = false
        if (!entry.changedTeacher.isEmpty())
            changedTeacher = entry.changedTeacher.substring(0, 2).equals(search, ignoreCase = true)


        if (teacher || changedTeacher) {
            parsedList.add(entry)
        }
    }

    if (parsedList.isEmpty()) {
        parsedList.add(TableEntry("Keine Einträge für '$search' gefunden."))
    }
    return parsedList
}

/**
 * Return a list of strings which prioritizes entrys of the school class
 * defined in the settings  activity.
 */
fun sortByClass(entryList: ArrayList<TableEntry>, schoolClass: String): ArrayList<TableEntry> {
    val parsedList = ArrayList<TableEntry>()
    val schoolClasses = HashSet<String>()

    //if there was an error we cant sort the list and don't have to
    if (entryList.size == 1) {
        parsedList.add(entryList[0])
        return parsedList
    }

    for (entry in entryList) {
        //we are creating a list that contains all school classes excluding duplicates
        schoolClasses.add(entry.schoolClass)

        //if the momentary entry equals the passed argument we add it to the sorted list
        //the passed argument is handled as our prioritized school class (favored by user in settings)
        if (entry.schoolClass.equals(schoolClass, ignoreCase = true)) {
            parsedList.add(entry)
        }
    }

    //adding all other TableEntry information sorted by school class
    for (sortedClass in schoolClasses) {
        for (i in entryList.indices) {
            //sort by class
            if (sortedClass.equals(entryList[i].schoolClass, ignoreCase = true) && !sortedClass.equals(schoolClass, ignoreCase = true)) {
                parsedList.add(entryList[i])
            }
        }
    }

    return parsedList
}

/**
 * Parses the whole HTML file so only the relevant table content is contained
 * in the returned List of TableEntry's.
 * If the passed string is empty the program assumes that something went wrong
 * while fetching it from the website.
 * If there is no table content the program assumes that there is no representation plan.
 * @param html
 * @return
 */
fun parse(html: String): ArrayList<TableEntry> {
    val entryList = ArrayList<TableEntry>()
    val document = Jsoup.parse(html)

    //if the html string is empty return an error to the user
    if (html.isEmpty()) {
        entryList.add(TableEntry(
                "Es konnte kein HTML Content gefetched werden."))
        return entryList
    }

    //using the document to filter all table rows
    val tables = document.getElementsByTag("table")

    if (tables.eachText().isEmpty()) {
        entryList.add(TableEntry(
                "Es liegen keine aktuellen Vertretungspläne vor."))
        return entryList
    }

    var entry: TableEntry
    var firstField: String
    var firstRow: Elements
    var secondRow: Elements
    var tableRows: Elements
    var e: Element
    //iterating through each table
    tables.indices.forEach { i ->
        //equally hacky way to receive the right date as all the other html parsing shit happening here
        val tableText = tables[i].parent().parent().text()
        val regexPattern = Pattern.compile("((\\w+), (\\d{2}).(\\d{2}).(\\d{4}))")
        val dateMatcher = regexPattern.matcher(tableText)
        var tableDate: String? = ""

        if (dateMatcher.find()) {
            tableDate = dateMatcher.group(0)
        }

        tableRows = tables[i].getElementsByTag("tr")

        val element = tableRows.iterator()
        while (element.hasNext()) {
            e = element.next()
            if (!e.children().hasText()) {
                element.remove()
                continue
            }

            firstField = e.children().eachText()[0]
            // TODO("Add a way to display the first row of the table intelligently")
            if (firstField.contains("Allgemeine") || firstField.contains("Klasse")) {
                element.remove()
                continue
            }

            firstRow = e.getElementsByTag("td")
            e = element.next()
            secondRow = e.getElementsByTag("td")
            entry = TableEntry(firstRow, secondRow, tableDate!!)
            entryList.add(entry)
        }
    }

    return entryList
}