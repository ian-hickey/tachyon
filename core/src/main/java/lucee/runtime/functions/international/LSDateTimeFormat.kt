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
package lucee.runtime.functions.international

import java.util.Locale

/**
 * Implements the CFML Function dateformat
 */
object LSDateTimeFormat : Function {
    private const val serialVersionUID = -1677384484943178492L
    @Throws(ExpressionException::class)
    fun call(pc: PageContext?, `object`: Object?): String? {
        return DateTimeFormat.invoke(pc, `object`, null, pc.getLocale(), ThreadLocalPageContext.getTimeZone(pc))
    }

    @Throws(ExpressionException::class)
    fun call(pc: PageContext?, `object`: Object?, mask: String?): String? {
        return DateTimeFormat.invoke(pc, `object`, mask, pc.getLocale(), ThreadLocalPageContext.getTimeZone(pc))
    }

    @Throws(ExpressionException::class)
    fun call(pc: PageContext?, `object`: Object?, mask: String?, locale: Locale?): String? {
        return DateTimeFormat.invoke(pc, `object`, mask, locale, ThreadLocalPageContext.getTimeZone(pc))
    }

    @Throws(ExpressionException::class)
    fun call(pc: PageContext?, `object`: Object?, mask: String?, locale: Locale?, tz: TimeZone?): String? {
        return DateTimeFormat.invoke(pc, `object`, mask, if (locale == null) pc.getLocale() else locale, if (tz == null) ThreadLocalPageContext.getTimeZone(pc) else tz)
    }
}