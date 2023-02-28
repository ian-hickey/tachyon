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
package lucee.runtime.functions.other

import lucee.runtime.PageContext

object ToNumeric {
    @Throws(PageException::class)
    fun call(pc: PageContext?, value: Object?): Double {
        return Caster.toDoubleValue(value)
    }

    @Throws(PageException::class)
    fun call(pc: PageContext?, value: Object?, oRadix: Object?): Double {
        if (oRadix == null) return call(pc, value)
        val radix: Int
        if (Decision.isNumber(oRadix)) {
            radix = Caster.toIntValue(oRadix)
            if (radix < Character.MIN_RADIX || radix > Character.MAX_RADIX) throw invalidRadix(pc, Caster.toString(radix))
        } else {
            val str: String = Caster.toString(oRadix).trim().toLowerCase()
            radix = if ("bin".equals(str)) 2 else if ("oct".equals(str)) 8 else if ("dec".equals(str)) 10 else if ("hex".equals(str)) 16 else throw invalidRadix(pc, str)
        }
        return Long.parseLong(Caster.toString(value), radix)
    }

    private fun invalidRadix(pc: PageContext?, radix: String?): FunctionException? {
        return FunctionException(pc, "ToNumeric", 2, "radix",
                "invalid value [" + radix + "], valid values are [" + Character.MIN_RADIX + "-" + Character.MAX_RADIX + ",bin,oct,dec,hex]")
    }
}