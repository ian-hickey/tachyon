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
 * Implements the CFML Function datecompare
 */
package lucee.runtime.functions.dateTime

import java.util.Calendar

class DateCompare : BIF() {
    @Override
    @Throws(PageException::class)
    operator fun invoke(pc: PageContext?, args: Array<Object?>?): Object? {
        return if (args!!.size == 2) call(pc, Caster.toDatetime(args[0], pc.getTimeZone()), Caster.toDatetime(args[1], pc.getTimeZone())) else call(pc, Caster.toDatetime(args[0], pc.getTimeZone()), Caster.toDatetime(args[1], pc.getTimeZone()), Caster.toString(args[2]))
    }

    companion object {
        @Throws(ExpressionException::class)
        fun call(pc: PageContext?, left: DateTime?, right: DateTime?): Double {
            return call(pc, left, right, "s")
        }

        @Throws(ExpressionException::class)
        fun call(pc: PageContext?, left: DateTime?, right: DateTime?, datepart: String?): Double {
            var datepart = datepart
            datepart = datepart.toLowerCase().trim()
            val tz: TimeZone = ThreadLocalPageContext.getTimeZone(pc)
            val cLeft: Calendar = JREDateTimeUtil.getThreadCalendar(tz)
            cLeft.setTime(left)
            val cRight: Calendar = JREDateTimeUtil.newInstance(tz, Locale.US)
            cRight.setTime(right)

            // TODO WEEEK
            var type = 0
            type = if (datepart.equals("s")) Calendar.SECOND else if (datepart.equals("n")) Calendar.MINUTE else if (datepart.equals("h")) Calendar.HOUR else if (datepart.equals("d")) Calendar.DATE else if (datepart.equals("m")) Calendar.MONTH else if (datepart.equals("y")) Calendar.DATE else if (datepart.equals("yyyy")) Calendar.YEAR else {
                throw FunctionException(pc, "dateCompare", 3, "datePart", "invalid value [$datepart], valid values has to be [s,n,h,d,m,y,yyyy]")
            }

            // Year
            var value: Int = cLeft.get(Calendar.YEAR) - cRight.get(Calendar.YEAR)
            if (value != 0) return if (value > 0) 1 else -1
            if (Calendar.YEAR === type) return 0
            if (Calendar.YEAR === type) return 0

            // Month
            value = cLeft.get(Calendar.MONTH) - cRight.get(Calendar.MONTH)
            if (value != 0) return if (value > 0) 1 else -1
            if (Calendar.MONTH === type) return 0

            // Day
            value = cLeft.get(Calendar.DATE) - cRight.get(Calendar.DATE)
            if (value != 0) return if (value > 0) 1 else -1
            if (Calendar.DATE === type) return 0

            // Hour
            // print.out(cLeft.get(Calendar.HOUR_OF_DAY)+"-"+cRight.get(Calendar.HOUR_OF_DAY));
            value = cLeft.get(Calendar.HOUR_OF_DAY) - cRight.get(Calendar.HOUR_OF_DAY)
            if (value != 0) return if (value > 0) 1 else -1
            if (Calendar.HOUR === type) return 0

            // Minute
            value = cLeft.get(Calendar.MINUTE) - cRight.get(Calendar.MINUTE)
            if (value != 0) return if (value > 0) 1 else -1
            if (Calendar.MINUTE === type) return 0

            // Second
            value = cLeft.get(Calendar.SECOND) - cRight.get(Calendar.SECOND)
            return if (value != 0) if (value > 0) 1 else -1 else 0
        }
    }
}