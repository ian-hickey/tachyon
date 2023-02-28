/**
 *
 * Copyright (c) 2016, Lucee Assosication Switzerland. All rights reserved.
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
 * Implements the CFML Function lsnumberformat
 */
package lucee.runtime.functions.international

import java.util.Locale

class LSNumberFormat : BIF() {
    @Override
    @Throws(PageException::class)
    operator fun invoke(pc: PageContext?, args: Array<Object?>?): Object? {
        if (args!!.size == 3) return call(pc, args[0], Caster.toString(args[1]), Caster.toLocale(args[2]))
        if (args.size == 2) return call(pc, args[0], Caster.toString(args[1]))
        if (args.size == 1) return call(pc, args[0])
        throw FunctionException(pc, "LSNumberFormat", 1, 3, args.size)
    }

    companion object {
        private const val serialVersionUID = -7981883050285346336L
        @Throws(PageException::class)
        fun call(pc: PageContext?, `object`: Object?): String? {
            return _call(pc, `object`, null, pc.getLocale())
        }

        @Throws(PageException::class)
        fun call(pc: PageContext?, `object`: Object?, mask: String?): String? {
            return _call(pc, `object`, mask, pc.getLocale())
        }

        @Throws(PageException::class)
        fun call(pc: PageContext?, `object`: Object?, mask: String?, locale: Locale?): String? {
            return _call(pc, `object`, mask, if (locale == null) pc.getLocale() else locale)
        }

        @Throws(PageException::class)
        private fun _call(pc: PageContext?, `object`: Object?, mask: String?, locale: Locale?): String? {
            return try {
                val formatter: lucee.runtime.util.NumberFormat = NumberFormat()
                if (mask == null) return formatter!!.format(locale, lucee.runtime.functions.displayFormatting.NumberFormat.toNumber(pc, `object`, 0))
                val m: Mask = lucee.runtime.util.NumberFormat.convertMask(mask)
                val number: Double = lucee.runtime.functions.displayFormatting.NumberFormat.toNumber(pc, `object`, m.right)
                formatter!!.format(locale, number, m)
            } catch (e: InvalidMaskException) {
                throw FunctionException(pc, "lsnumberFormat", 1, "number", e.getMessage())
            }
        }
    }
}