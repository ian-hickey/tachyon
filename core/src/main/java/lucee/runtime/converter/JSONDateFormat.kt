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
package lucee.runtime.converter

import java.lang.ref.SoftReference

object JSONDateFormat {
    val PATTERN_CF: String? = "MMMM, dd yyyy HH:mm:ss Z"
    val PATTERN_ISO8601: String? = "yyyy-MM-dd'T'HH:mm:ssZ" // preferred pattern for json
    private val map: Map<String?, SoftReference<DateFormat?>?>? = ConcurrentHashMap<String?, SoftReference<DateFormat?>?>()

    // private static DateFormat format=null;
    private val locale: Locale? = Locale.ENGLISH
    private val sync: Object? = SerializableObject()
    fun format(date: Date?, tz: TimeZone?, pattern: String?): String? {
        var tz: TimeZone? = tz
        tz = ThreadLocalPageContext.getTimeZone(tz)
        val id: String = locale.hashCode().toString() + "-" + tz.getID()
        synchronized(sync) {
            val tmp: SoftReference<DateFormat?>? = map!![id]
            var format: DateFormat? = if (tmp == null) null else tmp.get()
            if (format == null) {
                format = SimpleDateFormat(pattern, locale)
                format.setTimeZone(tz)
                map.put(id, SoftReference<DateFormat?>(format))
            }
            return format.format(date)
        }
    }
}