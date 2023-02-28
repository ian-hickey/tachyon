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
 * Implements the CFML Function dayofweekasstring
 */
package lucee.runtime.functions.string

import java.util.Locale

class DayOfWeekShortAsString : BIF() {
    @Override
    @Throws(PageException::class)
    operator fun invoke(pc: PageContext?, args: Array<Object?>?): Object? {
        if (args!!.size == 1) return call(pc, Caster.toDoubleValue(args[0]))
        if (args.size == 2) return call(pc, Caster.toDoubleValue(args[0]), Caster.toLocale(args[1]))
        throw FunctionException(pc, "DayOfWeekShortAsString", 1, 2, args.size)
    }

    companion object {
        private const val serialVersionUID = 3088890446888229079L
        @Throws(ExpressionException::class)
        fun call(pc: PageContext?, dow: Double): String? {
            return DayOfWeekAsString.call(pc, dow, pc.getLocale(), false)
        }

        @Throws(ExpressionException::class)
        fun call(pc: PageContext?, dow: Double, locale: Locale?): String? {
            return DayOfWeekAsString.call(pc, dow, if (locale == null) pc.getLocale() else locale, false)
        }
    }
}