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

import java.util.Calendar

class DateTimeFormat  // private final Calendar calendar;
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
        val calendar: Calendar = JREDateTimeUtil.getThreadCalendar(getLocale(), tz)
        calendar.setTimeInMillis(date.getTime())
        val lcMask: String = StringUtil.toLowerCase(mask)
        if (lcMask.equals("short")) return getAsString(calendar, java.text.DateFormat.SHORT, tz) else if (lcMask.equals("medium")) return getAsString(calendar, java.text.DateFormat.MEDIUM, tz) else if (lcMask.equals("long")) return getAsString(calendar, java.text.DateFormat.LONG, tz) else if (lcMask.equals("full")) return getAsString(calendar, java.text.DateFormat.FULL, tz)
        val len: Int = mask!!.length()
        var pos = 0
        if (len == 0) return ""
        val formated = StringBuilder()
        while (pos < len) {
            val c: Char = mask.charAt(pos)
            val next = if (len > pos + 1) mask.charAt(pos + 1) else 0.toChar()
            when (c) {
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
                'y', 'Y' -> {
                    val next__2 = if (len > pos + 2) mask.charAt(pos + 2) else 0.toChar()
                    val next__3 = if (len > pos + 3) mask.charAt(pos + 3) else 0.toChar()
                    val year4: Int = calendar.get(Calendar.YEAR)
                    val year2 = year4 % 100
                    if (next == 'y' || next == 'Y') {
                        if ((next__2 == 'y' || next__2 == 'Y') && (next__3 == 'y' || next__3 == 'Y')) {
                            formated.append(year4)
                            pos += 3
                        } else {
                            formated.append(if (year2 < 10) "0$year2" else "" + year2)
                            pos++
                        }
                    } else {
                        formated.append(year2)
                    }
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
                'N', 'n' -> {
                    val minute: Int = calendar.get(Calendar.MINUTE)
                    if (next == 'N' || next == 'n') {
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
                'z', 'Z' -> {
                    // count next z and jump to last z (max 6)
                    val start = pos
                    while (pos + 1 < len && Character.toLowerCase(mask.charAt(pos + 1)) === 'z') {
                        pos++
                        if (pos - start > 4) break
                    }
                    if (pos - start > 2) formated.append(tz.getDisplayName(getLocale())) else formated.append(tz.getID())
                }
                else -> formated.append(c)
            }
            pos++
        }
        return formated.toString()
    }

    private fun getAsString(c: Calendar?, style: Int, tz: TimeZone?): String? {
        val df: java.text.DateFormat = java.text.DateFormat.getDateTimeInstance(style, style, getLocale())
        df.setTimeZone(tz)
        return df.format(c.getTime())
    }
}