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
 * Implements the CFML Function dayofweekasstring
 */
package tachyon.runtime.functions.string

import java.util.Date

class DayOfWeekAsString : BIF() {
    @Override
    @Throws(PageException::class)
    operator fun invoke(pc: PageContext?, args: Array<Object?>?): Object? {
        if (args!!.size == 1) return call(pc, Caster.toDoubleValue(args[0]))
        if (args.size == 2) return call(pc, Caster.toDoubleValue(args[0]), Caster.toLocale(args[1]))
        throw FunctionException(pc, "DayOfWeekAsString", 1, 2, args.size)
    }

    companion object {
        private const val serialVersionUID = 4067032942689404733L
        private const val DAY = 1000 * 60 * 60 * 24
        private val dates: Array<Date?>? = arrayOf<Date?>(Date(0 + 3 * DAY), Date(0 + 4 * DAY), Date(0 + 5 * DAY), Date(0 + 6 * DAY), Date(0),
                Date(0 + 1 * DAY), Date(0 + 2 * DAY))

        @Throws(ExpressionException::class)
        fun call(pc: PageContext?, dow: Double): String? {
            return call(pc, dow, pc.getLocale(), true)
        }

        @Throws(ExpressionException::class)
        fun call(pc: PageContext?, dow: Double, locale: Locale?): String? {
            return call(pc, dow, if (locale == null) pc.getLocale() else locale, true)
        }

        @Throws(ExpressionException::class)
        fun call(pc: PageContext?, dow: Double, locale: Locale?, _long: Boolean): String? {
            val dayOfWeek = dow.toInt()
            if (dayOfWeek >= 1 && dayOfWeek <= 7) {
                return DateFormatPool.format(locale, TimeZoneConstants.GMT0, if (_long) "EEEE" else "EEE", dates!![dayOfWeek - 1])
            }
            throw FunctionException(pc, if (_long) "DayOfWeekAsString" else "DayOfWeekShortAsString", 1, "dayOfWeek", "must be between 1 and 7 now [$dayOfWeek]")
            // throw new ExpressionException("invalid dayOfWeek definition in function DayOfWeekAsString, must
            // be between 1 and 7 now ["+dayOfWeek+"]");
        }
    }
}