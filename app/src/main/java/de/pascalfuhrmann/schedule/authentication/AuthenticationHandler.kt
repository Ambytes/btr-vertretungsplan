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
package de.pascalfuhrmann.schedule.authentication

private const val BASE_URL = "https://btr-rs.de"
private const val LOGIN_PATH = "/service-vertretungsplan.php"
private const val LOGOUT_PATH = "/service-logout.php"

/**
 * Issues a login post request with the given login data.
 * @param username
 * @param password
 * @return a pair which contains a success boolean and the post request response body
 */

fun login(username: String, password: String): Pair<Boolean, String> {
    val headers = mapOf(
            "login" to username,
            "passwd" to password,
            "submit" to "Anmelden"
    )

    val response = khttp.post(
            url = "$BASE_URL$LOGIN_PATH",
            data = headers)

    return when {
        response.text.contains("fehlgeschlagen") -> false to ""
        else -> true to response.text
    }
}

/**
 * Issues a logout post request with the given username.
 * @param username
 */
fun logout(username: String) {
    khttp.post(
            url = "$BASE_URL$LOGOUT_PATH",
            data = mapOf("login" to username, "submit" to "Abmelden")
    )
}

/** Handles receiving a post response from the web server (containing html)
 * @param username
 * @param password
 * @return a pair containing a success boolean and the post request response body
 */
fun requestData(username: String, password: String): Pair<Boolean, String> {
    logout(username)
    return login(username, password)
}
