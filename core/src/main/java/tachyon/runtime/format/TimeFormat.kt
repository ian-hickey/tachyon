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
package tachyon.runtime.format

import java.text.DateFormat

class TimeFormat
/**
 * constructor of the class
 *
 * @param locale
 */
(locale: Locale?) : BaseFormat(locale), Format {
    /**
     * formats a date to a cfml date format (short)
     *
     * @param date
     * @return formated date
     */
    @Override
    override fun format(date: Date?): String? {
        return format(date, "short")
    }

    /**
     * formats a date to a cfml date format
     *
     * @param date
     * @param mask
     * @return formated date
     */
    @Override
    override fun format(date: Date?, mask: String?): String? {
        val dt: DateTime = if (date is DateTime) date as DateTime? else DateTimeImpl(date.getTime(), false)
        return format(dt, mask, null)
    }

    fun format(date: DateTime?, mask: String?, tz: TimeZone?): String? {
        return format(date.getTime(), mask, tz)
    }

    fun format(time: Long, mask: String?, tz: TimeZone?): String? {
        var def: TimeZone? = null
        return try {
            val calendar: Calendar = JREDateTimeUtil.getThreadCalendar(getLocale(), tz)
            calendar.setTimeInMillis(time)
            val lcMask: String = StringUtil.toLowerCase(mask)
            if (lcMask.equals("short")) return getAsString(calendar, DateFormat.SHORT, tz) else if (lcMask.equals("medium")) return getAsString(calendar, DateFormat.MEDIUM, tz) else if (lcMask.equals("long")) return getAsString(calendar, DateFormat.LONG, tz) else if (lcMask.equals("full")) return getAsString(calendar, DateFormat.FULL, tz) else if (lcMask.equals("beat")) {
                return Caster.toString(Beat.format(time))
            }
            val len: Int = mask!!.length()
            var pos = 0
            if (len == 0) return ""
            val formated = StringBuilder()
            while (pos < len) {
                val c: Char = mask.charAt(pos)
                val next = if (len > pos + 1) mask.charAt(pos + 1) else 0.toChar()
                when (c) {
                    'z' -> {
                        var count = 1
                        while (mask.length() > pos + 1 && mask.charAt(pos + 1) === 'z') {
                            pos++
                            count++
                        }
                        formated.append(tachyon.runtime.format.DateFormat.z(time, tz, count))
                    }
                    'Z' -> {
                        var count = 1
                        while (mask.length() > pos + 1 && mask.charAt(pos + 1) === 'Z') {
                            pos++
                            count++
                        }
                        formated.append(tachyon.runtime.format.DateFormat.Z(time, tz))
                    }
                    'X' -> {
                        var count = 1
                        while (mask.length() > pos + 1 && mask.charAt(pos + 1) === 'X') {
                            pos++
                            count++
                        }
                        if (def == null) def = TimeZone.getDefault()
                        TimeZone.setDefault(TimeZone.getTimeZone("CET"))
                        formated.append(tachyon.runtime.format.DateFormat.X(time, tz, count))
                    }
                    'h' -> {
                        var hour1: Int = calendar.get(Calendar.HOUR_OF_DAY)
                        if (hour1 == 0) hour1 = 12
                        if (hour1 > 12) hour1 = hour1 - 12
                        if (next == 'h') {
                            formated.append(if (hour1 < 10) "0$hour1" else "" + hour1)
                            pos++
                        } else {
                            formated.append(hour1)
                        }
                    }
                    'H' -> {
                        val hour2: Int = calendar.get(Calendar.HOUR_OF_DAY)
                        if (next == 'H') {
                            formated.append(if (hour2 < 10) "0$hour2" else "" + hour2)
                            pos++
                        } else {
                            formated.append(hour2)
                        }
                    }
                    'N', 'n', 'M', 'm' -> {
                        val minute: Int = calendar.get(Calendar.MINUTE)
                        if (next == 'M' || next == 'm' || next == 'N' || next == 'n') {
                            formated.append(if (minute < 10) "0$minute" else "" + minute)
                            pos++
                        } else {
                            formated.append(minute)
                        }
                    }
                    's', 'S' -> {
                        val second: Int = calendar.get(Calendar.SECOND)
                        if (next == 'S' || next == 's') {
                            formated.append(if (second < 10) "0$second" else "" + second)
                            pos++
                        } else {
                            formated.append(second)
                        }
                    }
                    'l', 'L' -> {
                        val nextnext = if (len > pos + 2) mask.charAt(pos + 2) else 0.toChar()
                        var millis: String = Caster.toString(calendar.get(Calendar.MILLISECOND))
                        if (next == 'L' || next == 'l') {
                            if (millis.length() === 1) millis = "0$millis"
                            pos++
                        }
                        if (nextnext == 'L' || nextnext == 'l') {
                            if (millis.length() === 2) millis = "0$millis"
                            pos++
                        }
                        formated.append(millis)
                    }
                    't', 'T' -> {
                        val isAm: Boolean = calendar.get(Calendar.HOUR_OF_DAY) < 12
                        if (next == 'T' || next == 't') {
                            formated.append(if (isAm) "AM" else "PM")
                            pos++
                        } else {
                            formated.append(if (isAm) "A" else "P")
                        }
                    }
                    else -> formated.append(c)
                }
                pos++
            }
            formated.toString()
        } finally {
            if (def != null) TimeZone.setDefault(def)
        }
    }

    private fun getAsString(c: Calendar?, style: Int, tz: TimeZone?): String? {
        val df: DateFormat = DateFormat.getTimeInstance(style, getLocale())
        df.setTimeZone(tz)
        return df!!.format(c.getTime())
    }
}