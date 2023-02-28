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
package tachyon.runtime.functions.international

import java.text.DateFormat

/**
 * Implements the CFML Function dateformat
 */
object LSTimeFormat : Function {
    private const val serialVersionUID = -35357762883021790L

    /**
     * @param pc
     * @param o
     * @return
     * @throws PageException
     */
    @Throws(PageException::class)
    fun call(pc: PageContext?, o: Object?): String? {
        return _call(pc, o, "short", pc.getLocale(), pc.getTimeZone())
    }

    @Throws(PageException::class)
    fun call(pc: PageContext?, o: Object?, mask: String?): String? {
        return _call(pc, o, mask, pc.getLocale(), pc.getTimeZone())
    }

    @Throws(PageException::class)
    fun call(pc: PageContext?, o: Object?, mask: String?, locale: Locale?): String? {
        return _call(pc, o, mask, if (locale == null) pc.getLocale() else locale, pc.getTimeZone())
    }

    @Throws(PageException::class)
    fun call(pc: PageContext?, o: Object?, mask: String?, locale: Locale?, tz: TimeZone?): String? {
        return _call(pc, o, mask, if (locale == null) pc.getLocale() else locale, if (tz == null) pc.getTimeZone() else tz)
    }

    @Throws(PageException::class)
    private fun _call(pc: PageContext?, o: Object?, mask: String?, locale: Locale?, tz: TimeZone?): String? {
        return if (o is String && StringUtil.isEmpty(o as String?, true)) "" else TimeFormat(locale).format(toTimeLS(locale, tz, o), mask, tz)
    }

    @Throws(PageException::class)
    private fun toTimeLS(locale: Locale?, timeZone: TimeZone?, `object`: Object?): DateTime? {
        if (`object` is DateTime) return `object` as DateTime?
        if (`object` is CharSequence) {
            val str: String = Caster.toString(`object`)
            val formats: Array<DateFormat?> = FormatUtil.getTimeFormats(locale, timeZone, true)
            for (i in formats.indices) {
                try {
                    return DateTimeImpl(formats[i].parse(str).getTime(), false)
                } catch (e: ParseException) {
                }
            }
        }
        return DateCaster.toDateAdvanced(`object`, timeZone)
    }
}