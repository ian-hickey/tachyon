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
 * Implements the CFML Function removechars
 */
package lucee.runtime.functions.string

import lucee.runtime.PageContext

class RemoveChars : BIF() {
    @Override
    @Throws(PageException::class)
    operator fun invoke(pc: PageContext?, args: Array<Object?>?): Object? {
        if (args!!.size == 3) return call(pc, Caster.toString(args[0]), Caster.toDoubleValue(args[1]), Caster.toDoubleValue(args[2]))
        throw FunctionException(pc, "RemoveChars", 3, 3, args.size)
    }

    companion object {
        @Throws(ExpressionException::class)
        fun call(pc: PageContext?, str: String?, s: Double, l: Double): String? {
            val start = s.toInt()
            val length = l.toInt()
            val strLength: Int = str!!.length()

            // check param 2
            if (start < 1 || start > strLength) throw ExpressionException("Parameter 2 of function removeChars which is now [$start] must be a greater 0 and less than the length of the first parameter")

            // check param 3
            if (length < 0) throw ExpressionException("Parameter 3 of function removeChars which is now [$length] must be a non-negative integer")
            if (strLength == 0) return ""
            var rtn: String? = str.substring(0, start - 1)
            if (start + length <= strLength) rtn += str.substring(start + length - 1)
            return rtn
        }
    }
}