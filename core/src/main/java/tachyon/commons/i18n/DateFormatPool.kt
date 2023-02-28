/**
 *
 * Copyright (c) 2014, the Railo Company Ltd. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.  If not, see <http:></http:>//www.gnu.org/licenses/>.
 *
 */
package tachyon.commons.i18n

import java.text.SimpleDateFormat

/**
 *
 */
object DateFormatPool {
    private val data: Map<String, SimpleDateFormat> = WeakHashMap()

    /**
     * pool for formated dates
     *
     * @param locale
     * @param timeZone
     * @param pattern
     * @param date
     * @return date matching given values
     */
    fun format(locale: Locale, timeZone: TimeZone, pattern: String, date: Date?): String {
        synchronized(data) {
            val key: String = locale.toString() + '-' + timeZone.getID() + '-' + pattern
            val obj: Object? = data[key]
            if (obj != null) {
                return (obj as SimpleDateFormat).format(date)
            }
            val sdf = SimpleDateFormat(pattern, locale)
            sdf.setTimeZone(timeZone)
            data.put(key, sdf)
            return sdf.format(date)
        }
    }

    /**
     * pool for formated dates
     *
     * @param locale
     * @param pattern
     * @param date
     * @return date matching given values
     */
    fun format(locale: Locale, pattern: String, date: Date?): String {
        synchronized(data) {
            val key: String = locale.toString() + '-' + pattern
            val obj: Object? = data[key]
            if (obj != null) {
                return (obj as SimpleDateFormat).format(date)
            } // print.ln(key);
            val sdf = SimpleDateFormat(pattern, locale)
            data.put(key, sdf)
            return sdf.format(date)
        }
    }

    /**
     * pool for formated dates
     *
     * @param pattern
     * @param date
     * @return date matching given values
     */
    fun format(pattern: String, date: Date?): String {
        synchronized(data) {
            val obj: Object? = data[pattern]
            if (obj != null) {
                return (obj as SimpleDateFormat).format(date)
            } // print.ln(pattern);
            val sdf = SimpleDateFormat(pattern)
            data.put(pattern, sdf)
            return sdf.format(date)
        }
    }
}