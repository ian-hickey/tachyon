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
package lucee.runtime.functions.dateTime

import java.util.TimeZone

object Beat : Function {
    private const val day = 86400000.0
    private val BMD: TimeZone? = TimeZone.getTimeZone("GMT+1")
    @Throws(PageException::class)
    fun call(pc: PageContext?): Double {
        return call(pc, null)
    }

    @Throws(PageException::class)
    fun call(pc: PageContext?, obj: Object?): Double {
        var obj: Object? = obj
        if (obj == null) obj = DateTimeImpl(pc)
        val tz: TimeZone = ThreadLocalPageContext.getTimeZone(pc)
        val date: DateTime = DateCaster.toDateAdvanced(obj, tz)
        return format(date.getTime())
    }

    fun format(time: Long): Double {
        val millisInDay: Long = DateTimeUtil.getInstance().getMilliSecondsInDay(BMD, time)
        val res = millisInDay / day * 1000
        return (res * 1000).toInt() / 1000.0
    }
}