/**
 * Copyright (c) 2014, the Railo Company Ltd.
 * Copyright (c) 2015, Tachyon Assosication Switzerland
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

import java.time.DayOfWeek

class JREDateTimeUtil  // Calendar string;
internal constructor() : DateTimeUtil() {
    @Override
    override fun _toTime(tz: TimeZone?, year: Int, month: Int, day: Int, hour: Int, minute: Int, second: Int, milliSecond: Int): Long {
        var tz: TimeZone? = tz
        if (tz == null) tz = ThreadLocalPageContext.getTimeZone(tz)
        val time: Calendar = _getThreadCalendar(null as PageContext?, tz)
        time.set(year, month - 1, day, hour, minute, second)
        time.set(Calendar.MILLISECOND, milliSecond)
        return time.getTimeInMillis()
    }

    @Override
    override fun getYear(tz: TimeZone, dt: DateTime): Int {
        return _get(tz, dt, Calendar.YEAR)
    }

    @Override
    override fun setYear(tz: TimeZone, dt: DateTime, value: Int) {
        _set(tz, dt, value, Calendar.YEAR)
    }

    @Override
    override fun getMonth(tz: TimeZone, dt: DateTime): Int {
        return _get(tz, dt, Calendar.MONTH) + 1
    }

    @Override
    override fun setMonth(tz: TimeZone, dt: DateTime, value: Int) {
        _set(tz, dt, value - 1, Calendar.MONTH)
    }

    @Override
    override fun getDay(tz: TimeZone, dt: DateTime): Int {
        return _get(tz, dt, Calendar.DAY_OF_MONTH)
    }

    @Override
    override fun setDay(tz: TimeZone, dt: DateTime, value: Int) {
        _set(tz, dt, value, Calendar.DAY_OF_MONTH)
    }

    @Override
    override fun getHour(tz: TimeZone, dt: DateTime): Int {
        return _get(tz, dt, Calendar.HOUR_OF_DAY)
    }

    @Override
    override fun setHour(tz: TimeZone, dt: DateTime, value: Int) {
        _set(tz, dt, value, Calendar.HOUR_OF_DAY)
    }

    @Override
    override fun getMinute(tz: TimeZone, dt: DateTime): Int {
        return _get(tz, dt, Calendar.MINUTE)
    }

    @Override
    override fun setMinute(tz: TimeZone, dt: DateTime, value: Int) {
        _set(tz, dt, value, Calendar.MINUTE)
    }

    @Override
    override fun getSecond(tz: TimeZone, dt: DateTime): Int {
        return _get(tz, dt, Calendar.SECOND)
    }

    @Override
    override fun setSecond(tz: TimeZone, dt: DateTime, value: Int) {
        _set(tz, dt, value, Calendar.SECOND)
    }

    @Override
    override fun getMilliSecond(tz: TimeZone, dt: DateTime): Int {
        return _get(tz, dt, Calendar.MILLISECOND)
    }

    @Override
    override fun setMilliSecond(tz: TimeZone, dt: DateTime, value: Int) {
        _set(tz, dt, value, Calendar.MILLISECOND)
    }

    @Override
    @Synchronized
    override fun getDayOfYear(locale: Locale, tz: TimeZone, dt: DateTime): Int {
        return _get(locale, tz, dt, Calendar.DAY_OF_YEAR)
    }

    @Override
    @Synchronized
    override fun getDayOfWeek(locale: Locale, tz: TimeZone, dt: DateTime): Int {
        // TODO improve for locale not starting the week with Sunday or Monday
        val weekFields: WeekFields = WeekFields.of(locale)
        val firstDay: DayOfWeek = weekFields.getFirstDayOfWeek()
        var fd: Int = firstDay.getValue()
        if (fd == 7) fd = 0
        val raw = _get(locale, tz, dt, Calendar.DAY_OF_WEEK)
        val result = raw - fd
        return if (result < 1) 7 + result else result
    }

    @Override
    @Synchronized
    override fun getFirstDayOfMonth(tz: TimeZone?, dt: DateTime): Int {
        val c: Calendar = _getThreadCalendar(null as PageContext?, tz)
        c.setTimeInMillis(dt.getTime())
        c.set(Calendar.DATE, 1)
        return c.get(Calendar.DAY_OF_YEAR)
    }

    @Override
    @Synchronized
    override fun getWeekOfYear(locale: Locale?, tz: TimeZone?, dt: DateTime): Int {
        val c: Calendar = _getThreadCalendar(locale, tz)
        c.setTimeInMillis(dt.getTime())
        val week: Int = c.get(Calendar.WEEK_OF_YEAR)
        // alreay counted as week of next year
        if (week == 1 && c.get(Calendar.MONTH) === Calendar.DECEMBER) {
            // seven days before plus one
            c.setTimeInMillis(dt.getTime() - SEVEN_DAYS)
            return c.get(Calendar.WEEK_OF_YEAR) + 1
        }
        return week
    }

    @Override
    @Synchronized
    override fun getMilliSecondsInDay(tz: TimeZone?, time: Long): Long {
        val c: Calendar = _getThreadCalendar(null as PageContext?, tz)
        c.setTimeInMillis(time)
        return c.get(Calendar.HOUR_OF_DAY) * 3600000 + c.get(Calendar.MINUTE) * 60000 + c.get(Calendar.SECOND) * 1000 + c.get(Calendar.MILLISECOND)
    }

    @Override
    @Synchronized
    override fun getDaysInMonth(tz: TimeZone?, dt: DateTime): Int {
        val c: Calendar = _getThreadCalendar(null as PageContext?, tz)
        c.setTimeInMillis(dt.getTime())
        return daysInMonth(c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1)
    }

    @Override
    override fun toString(pc: PageContext?, dt: DateTime, tz: TimeZone?, addTimeZoneOffset: Boolean?): String {
        var addTimeZoneOffset = addTimeZoneOffset
        val c: Calendar = _getThreadCalendar(pc, tz)
        c.setTimeInMillis(dt.getTime())
        // "HH:mm:ss"
        val sb = StringBuilder()
        sb.append("{ts '")
        toString(sb, c.get(Calendar.YEAR), 4)
        sb.append("-")
        toString(sb, c.get(Calendar.MONTH) + 1, 2)
        sb.append("-")
        toString(sb, c.get(Calendar.DATE), 2)
        sb.append(" ")
        toString(sb, c.get(Calendar.HOUR_OF_DAY), 2)
        sb.append(":")
        toString(sb, c.get(Calendar.MINUTE), 2)
        sb.append(":")
        toString(sb, c.get(Calendar.SECOND), 2)
        if (addTimeZoneOffset !== Boolean.FALSE) {
            if (addTimeZoneOffset == null && pc != null) addTimeZoneOffset = (pc as PageContextImpl?).getTimestampWithTSOffset()
            if (addTimeZoneOffset === Boolean.TRUE) addTimeZoneOffset(c, sb)
        }
        sb.append("'}")
        return sb.toString()
    }

    /*
	 * public static void main(String[] args) { Calendar c =
	 * Calendar.getInstance(TimeZone.getTimeZone("Pacific/Marquesas")); //c =
	 * Calendar.getInstance(TimeZoneConstants.AUSTRALIA_DARWIN);
	 * 
	 * c.setTimeInMillis(0); print.e(c.getTimeZone()); print.e(toTimeZoneOffset(c));
	 * 
	 * print.e(c.get(Calendar.ZONE_OFFSET)+c.get(Calendar.DST_OFFSET) );
	 * print.e(c.getTimeZone().getOffset(c.getTimeInMillis()));
	 * 
	 * c.set(Calendar.MONTH,7); print.e(c.get(Calendar.ZONE_OFFSET)+c.get(Calendar.DST_OFFSET) );
	 * print.e(c.getTimeZone().getOffset(c.getTimeInMillis())); }
	 */
    private fun addTimeZoneOffset(c: Calendar, sb: StringBuilder) {
        var min: Int = (c.get(Calendar.ZONE_OFFSET) + c.get(Calendar.DST_OFFSET)) / 60000
        val op: Char
        if (min < 0) {
            op = '-'
            min = min - min - min
        } else op = '+'
        val hours = min / 60
        min = min - hours * 60
        sb.append(op)
        toString(sb, hours, 2)
        sb.append(':')
        toString(sb, min, 2)
    }

    companion object {
        private const val SEVEN_DAYS = 604800000L
        private val _calendar = CalendarThreadLocal()
        private val calendar = CalendarThreadLocal()
        private val _localeCalendar = LocaleCalendarThreadLocal()
        private val localeCalendar = LocaleCalendarThreadLocal()
        private fun _get(tz: TimeZone, dt: DateTime, field: Int): Int {
            val c: Calendar = _getThreadCalendar(null as PageContext?, tz)
            c.setTimeInMillis(dt.getTime())
            return c.get(field)
        }

        private fun _set(tz: TimeZone, dt: DateTime, value: Int, field: Int) {
            val c: Calendar = _getThreadCalendar(null as PageContext?, tz)
            c.setTimeInMillis(dt.getTime())
            c.set(field, value)
            dt.setTime(c.getTimeInMillis())
        }

        private fun _get(l: Locale, tz: TimeZone, dt: DateTime, field: Int): Int {
            val c: Calendar = _getThreadCalendar(l, tz)
            c.setTimeInMillis(dt.getTime())
            return c.get(field)
        }

        fun newInstance(tz: TimeZone?, l: Locale?): Calendar {
            var tz: TimeZone? = tz
            if (tz == null) tz = ThreadLocalPageContext.getTimeZone()
            return Calendar.getInstance(tz, l)
        }

        /**
         * important:this function returns always the same instance for a specific thread, so make sure only
         * use one thread calendar instance at time.
         *
         * @return calendar instance
         */
        val threadCalendar: Calendar
            get() {
                val c: Calendar = calendar.get()
                c.clear()
                return c
            }

        /**
         * important:this function returns always the same instance for a specific thread, so make sure only
         * use one thread calendar instance at time.
         *
         * @return calendar instance
         */
        fun getThreadCalendar(tz: TimeZone?): Calendar {
            var tz: TimeZone? = tz
            val c: Calendar = calendar.get()
            c.clear()
            if (tz == null) tz = ThreadLocalPageContext.getTimeZone()
            c.setTimeZone(tz)
            return c
        }

        /**
         * important:this function returns always the same instance for a specific thread, so make sure only
         * use one thread calendar instance at time.
         *
         * @return calendar instance
         */
        fun getThreadCalendar(l: Locale, tz: TimeZone?): Calendar {
            var tz: TimeZone? = tz
            if (tz == null) tz = ThreadLocalPageContext.getTimeZone()
            val c: Calendar = localeCalendar[tz, l]
            c.setTimeZone(tz)
            return c
        }

        /*
	 * internally we use another instance to avoid conflicts
	 */
        private fun _getThreadCalendar(pc: PageContext, tz: TimeZone): Calendar {
            var tz: TimeZone? = tz
            val c: Calendar = _calendar.get()
            c.clear()
            if (tz == null) tz = ThreadLocalPageContext.getTimeZone(pc)
            c.setTimeZone(tz)
            return c
        }

        /*
	 * internally we use another instance to avoid conflicts
	 */
        private fun _getThreadCalendar(l: Locale, tz: TimeZone): Calendar {
            var tz: TimeZone? = tz
            val c: Calendar = _localeCalendar[tz, l]
            if (tz == null) tz = ThreadLocalPageContext.getTimeZone()
            c.setTimeZone(tz)
            return c
        }

        fun toString(sb: StringBuilder, i: Int, amount: Int) {
            var amount = amount
            val str: String = Caster.toString(i)
            amount = amount - str.length()
            while (amount-- > 0) {
                sb.append('0')
            }
            sb.append(str)
        }
    }
}

internal class CalendarThreadLocal : ThreadLocal<Calendar?>() {
    @Override
    @Synchronized
    protected fun initialValue(): Calendar {
        return Calendar.getInstance()
    }
}

internal class LocaleCalendarThreadLocal : ThreadLocal<Map<String?, Calendar?>?>() {
    @Override
    @Synchronized
    protected fun initialValue(): Map<String, Calendar> {
        return HashMap<String, Calendar>()
    }

    operator fun get(tz: TimeZone?, l: Locale): Calendar {
        val map: Map<String, Calendar> = get()
        var c: Calendar? = map[l.toString() + ":" + tz]
        if (c == null) {
            c = JREDateTimeUtil.newInstance(tz, l)
            map.put(l.toString() + ":" + tz, c)
        } else c.clear()
        return c
    }
}