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
 * Implements the CFML Function datepart
 */
package tachyon.runtime.functions.dateTime

import java.util.TimeZone

class DatePart : BIF() {
    @Override
    @Throws(PageException::class)
    operator fun invoke(pc: PageContext?, args: Array<Object?>?): Object? {
        if (args!!.size == 2) return call(pc, Caster.toString(args[0]), Caster.toDateTime(args[1], null))
        if (args.size == 3) return call(pc, Caster.toString(args[0]), Caster.toDateTime(args[1], null), Caster.toTimeZone(args[2]))
        throw FunctionException(pc, "DatePart", 2, 3, args.size)
    }

    companion object {
        private const val serialVersionUID = -4203375459570986511L
        @Throws(ExpressionException::class)
        fun call(pc: PageContext?, datepart: String?, date: DateTime?): Double {
            return call(pc, datepart, date, null)
        }

        @Throws(ExpressionException::class)
        fun call(pc: PageContext?, datepart: String?, date: DateTime?, tz: TimeZone?): Double {
            var datepart = datepart
            datepart = datepart.toLowerCase()
            val first = if (datepart!!.length() === 1) datepart.charAt(0) else 0.toChar()
            if (datepart!!.equals("yyyy")) return Year.call(pc, date, tz) else if (datepart.equals("ww")) return Week.call(pc, date, tz) else if (first == 'w') return DayOfWeek.call(pc, date, tz) else if (first == 'q') return Quarter.call(pc, date, tz) else if (first == 'm') return Month.call(pc, date, tz) else if (first == 'y') return DayOfYear.call(pc, date, tz) else if (first == 'd') return Day.call(pc, date, tz) else if (first == 'h') return Hour.call(pc, date, tz) else if (first == 'n') return Minute.call(pc, date, tz) else if (first == 's') return Second.call(pc, date, tz) else if (first == 'l') return MilliSecond.call(pc, date, tz)
            throw ExpressionException("invalid datepart type [$datepart] for function datePart")
        }
    }
}