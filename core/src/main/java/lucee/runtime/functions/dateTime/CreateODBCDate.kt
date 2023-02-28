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
 * Implements the CFML Function createodbcdate
 */
package lucee.runtime.functions.dateTime

import java.util.Calendar

object CreateODBCDate : Function {
    private const val serialVersionUID = -380258240258117961L
    fun call(pc: PageContext?, datetime: DateTime?): DateTime? {
        return call(pc, datetime, null)
    }

    fun call(pc: PageContext?, datetime: DateTime?, tz: TimeZone?): DateTime? {
        var tz: TimeZone? = tz
        if (tz == null) tz = (pc as PageContextImpl?).getTimeZone()
        val c: Calendar = Calendar.getInstance(tz)
        c.setTime(datetime)
        c.set(Calendar.HOUR, 0)
        c.set(Calendar.MINUTE, 0)
        c.set(Calendar.SECOND, 0)
        c.set(Calendar.MILLISECOND, 0)
        c.set(Calendar.AM_PM, 0)
        return DateImpl(c.getTime())
    }
}