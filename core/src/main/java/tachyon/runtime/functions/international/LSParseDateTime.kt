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
 * Implements the CFML Function lsparsedatetime
 */
package tachyon.runtime.functions.international

import java.text.ParseException

object LSParseDateTime : Function {
    private const val serialVersionUID = 7808039073301229473L
    @Throws(PageException::class)
    fun call(pc: PageContext?, oDate: Object?): tachyon.runtime.type.dt.DateTime? {
        return _call(pc, oDate, pc.getLocale(), pc.getTimeZone(), null)
    }

    @Throws(PageException::class)
    fun call(pc: PageContext?, oDate: Object?, locale: Locale?): tachyon.runtime.type.dt.DateTime? {
        return _call(pc, oDate, if (locale == null) pc.getLocale() else locale, pc.getTimeZone(), null)
    }

    @Throws(PageException::class)
    fun call(pc: PageContext?, oDate: Object?, locale: Locale?, strTimezoneOrFormat: String?): tachyon.runtime.type.dt.DateTime? {
        var locale: Locale? = locale
        if (locale == null) locale = pc.getLocale()
        if (strTimezoneOrFormat == null) {
            return _call(pc, oDate, locale, pc.getTimeZone(), null)
        }
        val tz: TimeZone = TimeZoneUtil.toTimeZone(strTimezoneOrFormat, null)
        return if (tz != null) _call(pc, oDate, locale, tz, null) else _call(pc, oDate, locale, pc.getTimeZone(), strTimezoneOrFormat)
    }

    @Throws(PageException::class)
    fun call(pc: PageContext?, oDate: Object?, locale: Locale?, strTimezone: String?, strFormat: String?): tachyon.runtime.type.dt.DateTime? {
        return _call(pc, oDate, if (locale == null) pc.getLocale() else locale, if (strTimezone == null) pc.getTimeZone() else TimeZoneUtil.toTimeZone(strTimezone), strFormat)
    }

    @Throws(PageException::class)
    private fun _call(pc: PageContext?, oDate: Object?, locale: Locale?, tz: TimeZone?, format: String?): tachyon.runtime.type.dt.DateTime? {
        var locale: Locale? = locale
        var tz: TimeZone? = tz
        if (oDate is Date) return Caster.toDate(oDate, tz)
        val strDate: String = Caster.toString(oDate)

        // regular parse date time
        if (StringUtil.isEmpty(format, true)) return DateCaster.toDateTime(locale, strDate, tz, locale.equals(Locale.US))

        // with java based format
        tz = ThreadLocalPageContext.getTimeZone(tz)
        if (locale == null) locale = pc.getLocale()
        val df = SimpleDateFormat(format, locale)
        df.setTimeZone(tz)
        return try {
            DateTimeImpl(df.parse(strDate))
        } catch (e: ParseException) {
            throw Caster.toPageException(e)
        }
    }
}