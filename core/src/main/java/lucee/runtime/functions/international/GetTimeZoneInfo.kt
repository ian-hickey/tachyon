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
 * Implements the CFML Function gettimezoneinfo
 */
package lucee.runtime.functions.international

import java.util.Calendar

object GetTimeZoneInfo : Function {
    private const val serialVersionUID = -5462276373169138909L
    fun call(pc: PageContext?): lucee.runtime.type.Struct? {
        return call(pc, null, null)
    }

    fun call(pc: PageContext?, tz: TimeZone?): lucee.runtime.type.Struct? {
        return call(pc, tz, null)
    }

    fun call(pc: PageContext?, tz: TimeZone?, dspLocale: Locale?): lucee.runtime.type.Struct? {
        var tz: TimeZone? = tz
        var dspLocale: Locale? = dspLocale
        if (tz == null) tz = pc.getTimeZone()
        if (dspLocale == null) dspLocale = pc.getLocale()
        // Date date = ;
        val c: Calendar = JREDateTimeUtil.getThreadCalendar(tz)
        c.setTimeInMillis(System.currentTimeMillis())
        val dstOffset: Int = c.get(Calendar.DST_OFFSET)
        var total: Int = c.get(Calendar.ZONE_OFFSET) / 1000 + dstOffset / 1000
        total *= -1
        val j = total / 60
        val hour = total / 60 / 60
        val minutes = j % 60
        val struct: Struct = StructImpl()
        struct.setEL("utcTotalOffset", Double.valueOf(total))
        struct.setEL("utcHourOffset", Double.valueOf(hour))
        struct.setEL("utcMinuteOffset", Double.valueOf(minutes))
        struct.setEL("isDSTon", if (dstOffset > 0) Boolean.TRUE else Boolean.FALSE)
        struct.setEL(KeyConstants._name, tz.getDisplayName(dspLocale))
        struct.setEL("nameDST", tz.getDisplayName(Boolean.TRUE, TimeZone.LONG, dspLocale))
        struct.setEL(KeyConstants._shortName, tz.getDisplayName(Boolean.FALSE, TimeZone.SHORT, dspLocale))
        struct.setEL("shortNameDST", tz.getDisplayName(Boolean.TRUE, TimeZone.SHORT, dspLocale))
        struct.setEL(KeyConstants._id, tz.getID())
        struct.setEL(KeyConstants._timezone, tz.getID())
        struct.setEL(KeyConstants._offset, Double.valueOf(-total))
        struct.setEL("DSTOffset", Double.valueOf(dstOffset / 1000))
        return struct

        // return new StructImpl();
    }
}