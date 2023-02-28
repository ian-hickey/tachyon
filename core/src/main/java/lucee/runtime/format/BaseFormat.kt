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
package lucee.runtime.format

import java.text.DateFormatSymbols

abstract class BaseFormat(locale: Locale?) : Format {
    private val locale: Locale?
    protected fun getMonthAsString(month: Int): String? {
        return if (getLocale().equals(Locale.US)) {
            when (month) {
                1 -> "January"
                2 -> "February"
                3 -> "March"
                4 -> "April"
                5 -> "May"
                6 -> "June"
                7 -> "July"
                8 -> "August"
                9 -> "September"
                10 -> "October"
                11 -> "November"
                12 -> "December"
                else -> null
            }
        } else DateFormatSymbols(locale).getMonths().get(month - 1)
    }

    protected fun getMonthShortAsString(month: Int): String? {
        return if (getLocale().equals(Locale.US)) {
            when (month) {
                1 -> "Jan"
                2 -> "Feb"
                3 -> "Mar"
                4 -> "Apr"
                5 -> "May"
                6 -> "Jun"
                7 -> "Jul"
                8 -> "Aug"
                9 -> "Sep"
                10 -> "Oct"
                11 -> "Nov"
                12 -> "Dec"
                else -> null
            }
        } else clean(DateFormatSymbols(locale).getShortMonths().get(month - 1))
    }

    protected fun getDayOfWeekAsString(dayOfWeek: Int): String? {
        return if (getLocale().equals(Locale.US)) {
            when (dayOfWeek) {
                1 -> "Sunday"
                2 -> "Monday"
                3 -> "Tuesday"
                4 -> "Wednesday"
                5 -> "Thursday"
                6 -> "Friday"
                7 -> "Saturday"
                else -> null
            }
        } else DateFormatSymbols(locale).getWeekdays().get(dayOfWeek)
    }

    protected fun getDayOfWeekShortAsString(dayOfWeek: Int): String? {
        return if (getLocale().equals(Locale.US)) {
            when (dayOfWeek) {
                1 -> "Sun"
                2 -> "Mon"
                3 -> "Tue"
                4 -> "Wed"
                5 -> "Thu"
                6 -> "Fri"
                7 -> "Sat"
                else -> null
            }
        } else clean(DateFormatSymbols(locale).getShortWeekdays().get(dayOfWeek))
    }

    private fun clean(str: String?): String? {
        // Java 10 added a dot in case it abbreviate a name
        return if (str.charAt(str!!.length() - 1) === '.') str.substring(0, str!!.length() - 1) else str
    }

    protected fun getLocale(): Locale? {
        return if (locale == null) Locale.US else locale
    }

    init {
        this.locale = locale
    }
}