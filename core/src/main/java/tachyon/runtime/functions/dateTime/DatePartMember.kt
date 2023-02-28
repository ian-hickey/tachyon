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
package tachyon.runtime.functions.dateTime

import java.util.TimeZone

class DatePartMember : BIF() {
    @Override
    @Throws(PageException::class)
    operator fun invoke(pc: PageContext?, args: Array<Object?>?): Object? {
        return if (args!!.size == 2) call(pc, Caster.toDatetime(args[0], pc.getTimeZone()), Caster.toString(args[1])) else call(pc, Caster.toDatetime(args[0], pc.getTimeZone()), Caster.toString(args[1]), Caster.toTimeZone(args[2]))
    }

    companion object {
        private const val serialVersionUID = 4954080153486127616L
        @Throws(ExpressionException::class)
        fun call(pc: PageContext?, date: DateTime?, datepart: String?): Double {
            return DatePart.call(pc, datepart, date, null)
        }

        @Throws(ExpressionException::class)
        fun call(pc: PageContext?, date: DateTime?, datepart: String?, tz: TimeZone?): Double {
            return DatePart.call(pc, datepart, date, tz)
        }
    }
}