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
package tachyon.commons.date

import java.text.SimpleDateFormat

abstract class DateTimeUtil {
    companion object {
        private val HTTP_TIME_STRING_FORMAT_OLD: SimpleDateFormat? = null
        private val HTTP_TIME_STRING_FORMAT: SimpleDateFormat? = null
        private const val DAY_MILLIS = 86400000.0
        private const val CF_UNIX_OFFSET = 2209161600000L
        const val SECOND = 0
        const val MINUTE = 1
        const val HOUR = 2
        const val DAY = 3
        const val YEAR = 10
        const val MONTH = 11
        const val WEEK = 12
        const val QUARTER = 20
        const val MILLISECOND = 30

        // try to load jar Date TimeUtil
        var instance: DateTimeUtil? = null
            get() {
                if (field == null) {
                    // try to load jar Date TimeUtil
                    field = JREDateTimeUtil()
                }
                return field
            }
            private set

        private fun getLocalTimeZoneOffset(time: Long): Long {
            return ThreadLocalPageContext.getTimeZone().getOffset(time)
        }

        fun toHTTPTimeString(time: Long, oldFormat: Boolean): String {
            return toHTTPTimeString(Date(time), oldFormat)
        }

        /**
         * converts a date to a http time String
         *
         * @param date date to convert
         * @param oldFormat "old" in that context means the format support the existing functionality in
         * CFML like the function getHTTPTimeString, in that format the date parts are separated
         * by a space (like "EE, dd MMM yyyy HH:mm:ss zz"), in the "new" format, the date part is
         * separated by "-" (like "EE, dd-MMM-yyyy HH:mm:ss zz")
         * @return
         */
        fun toHTTPTimeString(date: Date?, oldFormat: Boolean): String {
            if (oldFormat) {
                synchronized(HTTP_TIME_STRING_FORMAT_OLD) { return StringUtil.replace(HTTP_TIME_STRING_FORMAT_OLD.format(date), "+00:00", "", true) }
            }
            synchronized(HTTP_TIME_STRING_FORMAT) { return StringUtil.replace(HTTP_TIME_STRING_FORMAT.format(date), "+00:00", "", true) }
        }

        fun format(time: Long, l: Locale?, tz: TimeZone?): String {
            return DateTimeFormat.invoke(DateTimeImpl(time, false), null, ThreadLocalPageContext.getLocale(l), ThreadLocalPageContext.getTimeZone(tz))
        }

        // public final static SimpleDateFormat DATETIME_FORMAT_LOCAL;
        init {
            // DATETIME_FORMAT_LOCAL = new SimpleDateFormat("yyyy-MMM-dd HH:mm:ss");
            HTTP_TIME_STRING_FORMAT_OLD = SimpleDateFormat("EE, dd MMM yyyy HH:mm:ss zz", Locale.ENGLISH)
            HTTP_TIME_STRING_FORMAT_OLD.setTimeZone(TimeZone.getTimeZone("GMT"))
            HTTP_TIME_STRING_FORMAT = SimpleDateFormat("EE, dd-MMM-yyyy HH:mm:ss zz", Locale.ENGLISH)
            HTTP_TIME_STRING_FORMAT.setTimeZone(TimeZoneConstants.UTC)
        }
    }

    @Throws(DateTimeException::class)
    fun toDateTime(tz: TimeZone?, year: Int, month: Int, day: Int, hour: Int, minute: Int, second: Int, milliSecond: Int): DateTime {
        return DateTimeImpl(toTime(tz, year, month, day, hour, minute, second, milliSecond), false)
    }

    fun toDateTime(tz: TimeZone?, year: Int, month: Int, day: Int, hour: Int, minute: Int, second: Int, milliSecond: Int, defaultValue: DateTime): DateTime {
        val time = toTime(tz, year, month, day, hour, minute, second, milliSecond, Long.MIN_VALUE)
        return if (time == Long.MIN_VALUE) defaultValue else DateTimeImpl(time, false)
    }

    /**
     * returns a date time instance by a number, the conversion from the double to date is o the base of
     * the CFML rules.
     *
     * @param days double value to convert to a number
     * @return DateTime Instance
     */
    fun toDateTime(days: Double): DateTime {
        var utc: Long = Math.round(days * DAY_MILLIS)
        utc -= CF_UNIX_OFFSET
        utc -= getLocalTimeZoneOffset(utc)
        return DateTimeImpl(utc, false)
    }

    fun toTime(tz: TimeZone?, year: Int, month: Int, day: Int, hour: Int, minute: Int, second: Int, milliSecond: Int, defaultValue: Long): Long {
        var tz: TimeZone? = tz
        var year = year
        tz = ThreadLocalPageContext.getTimeZone(tz)
        year = toYear(year)
        if (month < 1) return defaultValue
        if (month > 12) return defaultValue
        if (day < 1) return defaultValue
        if (hour < 0) return defaultValue
        if (minute < 0) return defaultValue
        if (second < 0) return defaultValue
        if (milliSecond < 0) return defaultValue
        if (hour > 24) return defaultValue
        if (minute > 59) return defaultValue
        if (second > 59) return defaultValue
        return if (daysInMonth(year, month) < day) defaultValue else _toTime(tz, year, month, day, hour, minute, second, milliSecond)
    }

    @Throws(DateTimeException::class)
    fun toTime(tz: TimeZone?, year: Int, month: Int, day: Int, hour: Int, minute: Int, second: Int, milliSecond: Int): Long {
        var tz: TimeZone? = tz
        var year = year
        tz = ThreadLocalPageContext.getTimeZone(tz)
        year = toYear(year)
        if (month < 1) throw DateTimeException("Month number [$month] must be at least 1")
        if (month > 12) throw DateTimeException("Month number [$month] can not be greater than 12")
        if (day < 1) throw DateTimeException("Day number [$day] must be at least 1")
        if (hour < 0) throw DateTimeException("Hour number [$hour] must be at least 0")
        if (minute < 0) throw DateTimeException("Minute number [$minute] must be at least 0")
        if (second < 0) throw DateTimeException("Second number [$second] must be at least 0")
        if (milliSecond < 0) throw DateTimeException("Milli second number [$milliSecond] must be at least 0")
        if (hour > 24) throw DateTimeException("Hour number [$hour] can not be greater than 24")
        if (minute > 59) throw DateTimeException("Minute number [$minute] can not be greater than 59")
        if (second > 59) throw DateTimeException("Second number [$second] can not be greater than 59")
        if (daysInMonth(year, month) < day) throw DateTimeException("Day number [" + day + "] can not be greater than " + daysInMonth(year, month) + " when month is " + month + " and year " + year)
        return _toTime(tz, year, month, day, hour, minute, second, milliSecond)
    }

    /**
     * return how much days given month in given year has
     *
     * @param year
     * @param month
     * @return
     */
    fun daysInMonth(year: Int, month: Int): Int {
        when (month) {
            1, 3, 5, 7, 8, 10, 12 -> return 31
            4, 6, 9, 11 -> return 30
            2 -> return if (isLeapYear(year)) 29 else 28
        }
        return -1
    }

    /**
     * translate 2 digit numbers to a year; for example 10 to 2010 or 50 to 1950
     *
     * @param year
     * @return year matching number
     */
    fun toYear(year: Int): Int {
        var year = year
        if (year < 100) {
            if (year < 30) {
                year += 2000
                year = year
            } else {
                year += 1900
                year = year
            }
        }
        return year
    }

    /**
     * return if given is is a leap year or not
     *
     * @param year
     * @return is leap year
     */
    fun isLeapYear(year: Int): Boolean {
        return year % 4 == 0 && (year % 100 != 0 || year % 400 == 0)
    }

    /**
     * cast boolean value
     *
     * @param dateTime
     * @return boolean value
     * @throws ExpressionException
     */
    @Throws(DateTimeException::class)
    fun toBooleanValue(dateTime: DateTime?): Boolean {
        throw DateTimeException("Can't cast Date [" + toHTTPTimeString(dateTime, false) + "] to boolean value")
    }

    fun toDoubleValue(dateTime: DateTime): Double {
        return toDoubleValue(dateTime.getTime())
    }

    fun toDoubleValue(time: Long): Double {
        var time = time
        time += getLocalTimeZoneOffset(time)
        time += CF_UNIX_OFFSET
        return time / DAY_MILLIS
    }

    fun getMilliSecondsAdMidnight(timeZone: TimeZone?, time: Long): Long {
        return time - getMilliSecondsInDay(timeZone, time)
    }

    abstract fun _toTime(tz: TimeZone?, year: Int, month: Int, day: Int, hour: Int, minute: Int, second: Int, milliSecond: Int): Long
    abstract fun getYear(tz: TimeZone?, dt: tachyon.runtime.type.dt.DateTime?): Int
    abstract fun setYear(tz: TimeZone?, dt: tachyon.runtime.type.dt.DateTime?, value: Int)
    abstract fun getMonth(tz: TimeZone?, dt: DateTime?): Int
    abstract fun setMonth(tz: TimeZone?, dt: DateTime?, value: Int)
    abstract fun getDay(tz: TimeZone?, dt: DateTime?): Int
    abstract fun setDay(tz: TimeZone?, dt: DateTime?, value: Int)
    abstract fun getHour(tz: TimeZone?, dt: DateTime?): Int
    abstract fun setHour(tz: TimeZone?, dt: DateTime?, value: Int)
    abstract fun getMinute(tz: TimeZone?, dt: DateTime?): Int
    abstract fun setMinute(tz: TimeZone?, dt: DateTime?, value: Int)
    abstract fun getSecond(tz: TimeZone?, dt: DateTime?): Int
    abstract fun setSecond(tz: TimeZone?, dt: DateTime?, value: Int)
    abstract fun getMilliSecond(tz: TimeZone?, dt: DateTime?): Int
    abstract fun setMilliSecond(tz: TimeZone?, dt: DateTime?, value: Int)
    abstract fun getMilliSecondsInDay(tz: TimeZone?, time: Long): Long
    abstract fun getDaysInMonth(tz: TimeZone?, dt: DateTime?): Int
    abstract fun getDayOfYear(locale: Locale?, tz: TimeZone?, dt: DateTime?): Int
    abstract fun getDayOfWeek(locale: Locale?, tz: TimeZone?, dt: DateTime?): Int
    abstract fun getWeekOfYear(locale: Locale?, tz: TimeZone?, dt: DateTime?): Int
    abstract fun getFirstDayOfMonth(tz: TimeZone?, dt: DateTime?): Int
    abstract fun toString(pc: PageContext?, dt: DateTime?, tz: TimeZone?, addTimeZoneOffset: Boolean?): String?
}