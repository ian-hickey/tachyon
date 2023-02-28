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
package tachyon.runtime.functions.displayFormatting

import java.util.Locale

/**
 * Implements the CFML Function dateformat
 */
object TimeFormat : Function {
    private const val serialVersionUID = -1335780260277678959L

    /**
     * @param pc
     * @param object
     * @return Formated Time Object as String
     * @throws ExpressionException
     */
    @Throws(ExpressionException::class)
    fun call(pc: PageContext?, `object`: Object?): String? {
        return _call(pc, `object`, "hh:mm tt", ThreadLocalPageContext.getTimeZone(pc))
    }

    /**
     * @param pc
     * @param object
     * @param mask Characters that show how CFML displays a date:
     * @return Formated Time Object as String
     * @throws ExpressionException
     */
    @Throws(ExpressionException::class)
    fun call(pc: PageContext?, `object`: Object?, mask: String?): String? {
        return _call(pc, `object`, mask, ThreadLocalPageContext.getTimeZone(pc))
    }

    @Throws(ExpressionException::class)
    fun call(pc: PageContext?, `object`: Object?, mask: String?, tz: TimeZone?): String? {
        return _call(pc, `object`, mask, if (tz == null) ThreadLocalPageContext.getTimeZone(pc) else tz)
    }

    @Throws(ExpressionException::class)
    private fun _call(pc: PageContext?, `object`: Object?, mask: String?, tz: TimeZone?): String? {
        val locale: Locale = Locale.US // :pc.getConfig().getLocale();
        val datetime: DateTime = Caster.toDate(`object`, true, tz, null)
        if (datetime == null) {
            if (StringUtil.isEmpty(`object`, true)) return ""
            throw ExpressionException("Can't convert value [$`object`] to a datetime value")
        }
        return TimeFormat(locale).format(datetime, mask, tz)
        // return new tachyon.runtime.text.TimeFormat(locale).format(datetime,mask);
    }
}