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
 * Implements the CFML Function cjustify
 */
package lucee.runtime.functions.string

import lucee.runtime.PageContext

class CJustify : BIF() {
    @Override
    @Throws(PageException::class)
    operator fun invoke(pc: PageContext?, args: Array<Object?>?): Object? {
        if (args!!.size == 2) return call(pc, Caster.toString(args[0]), Caster.toDoubleValue(args[1]))
        throw FunctionException(pc, "CJustify", 2, 2, args.size)
    }

    companion object {
        private const val serialVersionUID = -4145093552477680411L
        @Throws(ExpressionException::class)
        fun call(pc: PageContext?, string: String?, length: Double): String? {
            var len = length.toInt()
            return if (len < 1) throw ExpressionException("Parameter 2 of function cJustify which is now [$len] must be a non-negative integer") else if (string!!.length().let { len -= it; len } <= 0) string else {
                val chrs = CharArray(string.length() + len)
                val part = len / 2
                for (i in 0 until part) chrs[i] = ' '
                for (i in string.length() - 1 downTo 0) chrs[part + i] = string.charAt(i)
                for (i in part + string.length() until chrs.size) chrs[i] = ' '
                String(chrs)
            }
        }
    }
}