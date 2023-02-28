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
 * Implements the CFML Function monthasstring
 */
package lucee.runtime.functions.string

import java.text.DateFormatSymbols

object MonthAsString : Function {
    @Throws(ExpressionException::class)
    fun call(pc: PageContext?, month: Double): String? {
        return call(month, pc.getLocale(), false)
    }

    @Throws(ExpressionException::class)
    fun call(pc: PageContext?, month: Double, locale: Locale?): String? {
        return call(month, if (locale == null) pc.getLocale() else locale, false)
    }

    @Throws(ExpressionException::class)
    internal fun call(month: Double, locale: Locale?, _short: Boolean): String? {
        val m = month.toInt()
        if (m >= 1 && m <= 12) {
            val dfs = DateFormatSymbols(locale)
            val months: Array<String?> = if (_short) dfs.getShortMonths() else dfs.getMonths()
            return months[m - 1]
        }
        throw ExpressionException("invalid month definition in function monthAsString, must be between 1 and 12 now [$month]")
    }
}