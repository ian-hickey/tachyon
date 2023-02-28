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
 * Implements the CFML Function firstdayofmonth
 */
package tachyon.runtime.functions.dateTime

import java.util.TimeZone

class FirstDayOfMonth : BIF() {
    @Override
    @Throws(PageException::class)
    operator fun invoke(pc: PageContext?, args: Array<Object?>?): Object? {
        return if (args!!.size == 1) call(pc, Caster.toDatetime(args[0], pc.getTimeZone())) else call(pc, Caster.toDatetime(args[0], pc.getTimeZone()), Caster.toTimeZone(args[1]))
    }

    companion object {
        private const val serialVersionUID = 2771139908016254661L
        fun call(pc: PageContext?, date: DateTime?): Double {
            return _call(pc, date, pc.getTimeZone())
        }

        fun call(pc: PageContext?, date: DateTime?, tz: TimeZone?): Double {
            return _call(pc, date, if (tz == null) pc.getTimeZone() else tz)
        }

        private fun _call(pc: PageContext?, date: DateTime?, tz: TimeZone?): Double {
            return DateTimeUtil.getInstance().getFirstDayOfMonth(tz, date)
        }
    }
}