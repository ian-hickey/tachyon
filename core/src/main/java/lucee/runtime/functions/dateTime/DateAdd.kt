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
/**
 * Implements the CFML Function dateadd
 */
package lucee.runtime.functions.dateTime

import java.util.Calendar

class DateAdd : BIF() {
    @Override
    @Throws(PageException::class)
    operator fun invoke(pc: PageContext?, args: Array<Object?>?): Object? {
        if (args!!.size != 3) throw FunctionException(pc, "DateAdd", 3, 3, args.size)
        return call(pc, Caster.toString(args[0]), Caster.toDoubleValue(args[1]), Caster.toDate(args[2], pc.getTimeZone()))
    }

    companion object {
        // do not change this is used in the chart extension
        private const val serialVersionUID = -5827644560609841341L
        @Throws(ExpressionException::class)
        fun call(pc: PageContext?, datepart: String?, number: Double, date: DateTime?): DateTime? {
            return _call(pc, pc.getTimeZone(), datepart, number, date)
        }

        @Throws(ExpressionException::class)
        fun _call(pc: PageContext?, tz: TimeZone?, datepart: String?, number: Double, date: DateTime?): DateTime? {
            var datepart = datepart
            datepart = datepart.toLowerCase()
            val l = number.toLong()
            var n = l.toInt()
            val first = if (datepart!!.length() === 1) datepart.charAt(0) else 0.toChar()
            if (first == 'l') return DateTimeImpl(pc, date.getTime() + l, false) else if (first == 's') return DateTimeImpl(pc, date.getTime() + l * 1000, false) else if (first == 'n') return DateTimeImpl(pc, date.getTime() + l * 60000, false) else if (first == 'h') return DateTimeImpl(pc, date.getTime() + l * 3600000, false)
            val c: Calendar = JREDateTimeUtil.getThreadCalendar()
            // if (c == null)c=JREDateTimeUtil.newInstance();
            // synchronized (c) {
            // c.clear();
            c.setTimeZone(tz)
            c.setTimeInMillis(date.getTime())
            if (datepart!!.equals("yyyy")) {
                c.set(Calendar.YEAR, c.get(Calendar.YEAR) + n)
            } else if (datepart.equals("ww")) c.add(Calendar.WEEK_OF_YEAR, n) else if (first == 'q') c.add(Calendar.MONTH, n * 3) else if (first == 'm') c.add(Calendar.MONTH, n) else if (first == 'y') c.add(Calendar.DAY_OF_YEAR, n) else if (first == 'd') c.add(Calendar.DATE, n) else if (first == 'w') {
                val dow: Int = c.get(Calendar.DAY_OF_WEEK)
                val offset: Int
                // -
                offset = if (n < 0) {
                    if (Calendar.SUNDAY === dow) 2 else -(6 - dow)
                } else {
                    if (Calendar.SATURDAY === dow) -2 else dow - 2
                }
                c.add(Calendar.DAY_OF_WEEK, -offset)
                if (dow == Calendar.SATURDAY || dow == Calendar.SUNDAY) {
                    if (n > 0) n-- else if (n < 0) n++
                } else n += offset
                c.add(Calendar.DAY_OF_WEEK, n / 5 * 7 + n % 5)
            } else {
                throw ExpressionException("invalid datepart identifier [$datepart] for function dateAdd")
            }
            return DateTimeImpl(pc, c.getTimeInMillis(), false)
            // }
        }
    }
}