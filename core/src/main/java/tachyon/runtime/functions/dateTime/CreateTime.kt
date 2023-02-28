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
 * Implements the CFML Function createtime
 */
package tachyon.runtime.functions.dateTime

import java.util.TimeZone

object CreateTime : Function {
    private const val serialVersionUID = -5887770689991548576L
    fun call(pc: PageContext?, hour: Double): DateTime? {
        return _call(pc, hour, 0.0, 0.0, 0.0, pc.getTimeZone())
    }

    fun call(pc: PageContext?, hour: Double, minute: Double): DateTime? {
        return _call(pc, hour, minute, 0.0, 0.0, pc.getTimeZone())
    }

    fun call(pc: PageContext?, hour: Double, minute: Double, second: Double): DateTime? {
        return _call(pc, hour, minute, second, 0.0, pc.getTimeZone())
    }

    fun call(pc: PageContext?, hour: Double, minute: Double, second: Double, millis: Double): DateTime? {
        return _call(pc, hour, minute, second, millis, pc.getTimeZone())
    }

    fun call(pc: PageContext?, hour: Double, minute: Double, second: Double, millis: Double, tz: TimeZone?): DateTime? {
        return _call(pc, hour, minute, second, millis, if (tz == null) pc.getTimeZone() else tz)
    }

    private fun _call(pc: PageContext?, hour: Double, minute: Double, second: Double, millis: Double, tz: TimeZone?): DateTime? {
        // TODO check this looks wrong
        var tz: TimeZone? = tz
        if (tz == null) tz = ThreadLocalPageContext.getTimeZone(pc)
        return TimeImpl(DateTimeUtil.getInstance().toTime(tz, 1899, 12, 30, hour.toInt(), minute.toInt(), second.toInt(), millis.toInt(), 0), false)
    }
}