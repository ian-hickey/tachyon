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

import java.text.SimpleDateFormat

class DateFormat
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
        return format(date, "medium")
    }

    /**
     * formats a date to a cfml date format
     *
     * @param date
     * @param mask
     * @return formated date as string
     */
    @Override
    override fun format(date: Date?, mask: String?): String? {
        return format(date, mask, null)
    }

    fun format(date: Date?, mask: String?, tz: TimeZone?): String? {
        return format(date.getTime(), mask, tz)
    }

    fun format(time: Long, mask: String?, tz: TimeZone?): String? {
        var tz: TimeZone? = tz
        var def: TimeZone? = null
        return try {
            tz = ThreadLocalPageContext.getTimeZone(tz)
            val calendar: Calendar = JREDateTimeUtil.getThreadCalendar(getLocale(), tz)
            calendar.setTimeInMillis(time)
            val lcMask: String = StringUtil.toLowerCase(mask)
            if (lcMask.equals("short")) return getAsString(calendar, java.text.DateFormat.SHORT, tz) else if (lcMask.equals("medium")) return getAsString(calendar, java.text.DateFormat.MEDIUM, tz) else if (lcMask.equals("long")) return getAsString(calendar, java.text.DateFormat.LONG, tz) else if (lcMask.equals("full")) return getAsString(calendar, java.text.DateFormat.FULL, tz) else if ("iso8601".equals(lcMask) || "iso".equals(lcMask)) {
                val formatter = SimpleDateFormat("yyyy-MM-dd")
                return formatter.format(calendar.getTime())
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
                        formated.append(z(time, tz, count))
                    }
                    'Z' -> {
                        while (mask.length() > pos + 1 && mask.charAt(pos + 1) === 'Z') {
                            pos++
                        }
                        formated.append(Z(time, tz))
                    }
                    'X' -> {
                        var count = 1
                        while (mask.length() > pos + 1 && mask.charAt(pos + 1) === 'X') {
                            pos++
                            count++
                        }
                        if (def == null) def = TimeZone.getDefault()
                        TimeZone.setDefault(TimeZone.getTimeZone("CET"))
                        formated.append(X(time, tz, count))
                    }
                    'g', 'G' -> {
                        val era = toEra(calendar.get(Calendar.ERA), "")
                        while (mask.length() > pos + 1 && Character.toLowerCase(mask.charAt(pos + 1)) === 'g') {
                            pos++
                        }
                        formated.append(era)
                    }
                    'd', 'D' -> {
                        val next2 = if (len > pos + 2) mask.charAt(pos + 2) else 0.toChar()
                        val next3 = if (len > pos + 3) mask.charAt(pos + 3) else 0.toChar()
                        val day: Int = calendar.get(Calendar.DATE)
                        if (next == 'd' || next == 'D') {
                            if (next2 == 'd' || next2 == 'D') {
                                pos += if (next3 == 'd' || next3 == 'D') {
                                    formated.append(getDayOfWeekAsString(calendar.get(Calendar.DAY_OF_WEEK)))
                                    3
                                } else {
                                    formated.append(getDayOfWeekShortAsString(calendar.get(Calendar.DAY_OF_WEEK)))
                                    2
                                }
                            } else {
                                formated.append(if (day < 10) "0$day" else "" + day)
                                pos++
                            }
                        } else {
                            formated.append(day)
                        }
                    }
                    'm', 'M' -> {
                        val next_2 = if (len > pos + 2) mask.charAt(pos + 2) else 0.toChar()
                        val next_3 = if (len > pos + 3) mask.charAt(pos + 3) else 0.toChar()
                        val month: Int = calendar.get(Calendar.MONTH) + 1
                        if (next == 'm' || next == 'M') {
                            if (next_2 == 'm' || next_2 == 'M') {
                                pos += if (next_3 == 'm' || next_3 == 'M') {
                                    formated.append(getMonthAsString(month))
                                    3
                                } else {
                                    formated.append(getMonthShortAsString(month))
                                    2
                                }
                            } else {
                                formated.append(if (month < 10) "0$month" else "" + month)
                                pos++
                            }
                        } else {
                            formated.append(month)
                        }
                    }
                    'w', 'W' -> {
                        var week = 0
                        if (c == 'W' || next == 'W') week = calendar.get(Calendar.WEEK_OF_MONTH)
                        if (c == 'w' || next == 'w') week = calendar.get(Calendar.WEEK_OF_YEAR)
                        val next_1 = if (len > pos + 1) mask.charAt(pos + 1) else 0.toChar()
                        if (next == 'w' || next == 'W' && next_1 == 'w' || next_1 == 'W') {
                            formated.append(if (week < 10) "0$week" else "" + week)
                            pos++
                        } else {
                            formated.append(week)
                        }
                    }
                    'y', 'Y' -> {
                        val next__2 = if (len > pos + 2) mask.charAt(pos + 2) else 0.toChar()
                        val next__3 = if (len > pos + 3) mask.charAt(pos + 3) else 0.toChar()
                        val year4: Int = calendar.get(Calendar.YEAR)
                        val year2 = year4 % 100
                        if (next == 'y' || next == 'Y') {
                            if ((next__2 == 'y' || next__2 == 'Y') && (next__3 == 'y' || next__3 == 'Y')) {
                                formated.append(year4)
                                pos += 3
                            } else if (next__2 == 'y' || next__2 == 'Y') {
                                formated.append(year4)
                                pos += 2
                            } else {
                                formated.append(if (year2 < 10) "0$year2" else "" + year2)
                                pos++
                            }
                        } else {
                            formated.append(year4)
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

    private fun toEra(era: Int, defaultValue: String?): String? {
        if (GregorianCalendar.AD === era) return "AD"
        return if (GregorianCalendar.BC === era) "BC" else defaultValue
    }

    private fun getAsString(c: Calendar?, style: Int, tz: TimeZone?): String? {
        val df: java.text.DateFormat = java.text.DateFormat.getDateInstance(style, getLocale())
        df.setTimeZone(tz)
        return df.format(c.getTime())
    }

    companion object {
        fun X(time: Long, tz: TimeZone?, count: Int): Object? {
            if (tz.equals(TimeZoneConstants.UTC)) return "Z"
            val res = Z(time, tz)
            if (count == 1) return res.substring(0, 3)
            return if (count == 2) res else res.substring(0, 1) + res.substring(1, 3).toString() + ":" + res.substring(3)

            // String h=(res.charAt(1)=='0')? h=res.substring(2, 3):res.substring(1, 3);
        }

        fun z(time: Long, tz: TimeZone?, count: Int): String? {
            val c: Calendar = Calendar.getInstance(tz, Locale.US)
            c.setTimeInMillis(time)
            val daylight = c.get(Calendar.DST_OFFSET) !== 0
            val style: Int = if (count < 4) TimeZone.SHORT else TimeZone.LONG
            return tz.getDisplayName(daylight, style, Locale.US)
        }

        fun Z(time: Long, tz: TimeZone?): String? {
            val sb = StringBuilder()
            val c: Calendar = Calendar.getInstance(tz, Locale.US)
            c.setTimeInMillis(time)
            val value: Int = (c.get(Calendar.ZONE_OFFSET) + c.get(Calendar.DST_OFFSET)) / 60000
            var width = 4
            if (value >= 0) sb.append('+') else width++
            val num = value / 60 * 100 + value % 60
            sprintf0d(sb, num, width)
            return sb.toString()
        }

        /**
         * Mimics sprintf(buf, "%0*d", decaimal, width).
         */
        private fun sprintf0d(sb: StringBuilder?, value: Int, width: Int): StringBuilder? {
            var width = width
            var d = value.toLong()
            if (d < 0) {
                sb.append('-')
                d = -d
                --width
            }
            var n = 10
            for (i in 2 until width) {
                n *= 10
            }
            var i = 1
            while (i < width && d < n) {
                sb.append('0')
                n /= 10
                i++
            }
            sb.append(d)
            return sb
        }
    }
}