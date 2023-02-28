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
 * Implements the CFML Function createdate
 */
package tachyon.runtime.functions.dateTime

import java.util.TimeZone

object CreateDate : Function {
    private const val serialVersionUID = -8116641467358905335L
    @Throws(ExpressionException::class)
    fun call(pc: PageContext?, year: Double): DateTime? {
        return _call(pc, year, 1.0, 1.0, pc.getTimeZone())
    }

    @Throws(ExpressionException::class)
    fun call(pc: PageContext?, year: Double, month: Double): DateTime? {
        return _call(pc, year, month, 1.0, pc.getTimeZone())
    }

    @Throws(ExpressionException::class)
    fun call(pc: PageContext?, year: Double, month: Double, day: Double): DateTime? {
        return _call(pc, year, month, day, pc.getTimeZone())
    }

    @Throws(ExpressionException::class)
    fun call(pc: PageContext?, year: Double, month: Double, day: Double, tz: TimeZone?): DateTime? {
        return _call(pc, year, month, day, if (tz == null) pc.getTimeZone() else tz)
    }

    @Throws(ExpressionException::class)
    private fun _call(pc: PageContext?, year: Double, month: Double, day: Double, tz: TimeZone?): DateTime? {
        return DateTimeUtil.getInstance().toDateTime(tz, year.toInt(), month.toInt(), day.toInt(), 0, 0, 0, 0)
    }
}