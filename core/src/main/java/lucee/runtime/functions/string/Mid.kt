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
 * Implements the CFML Function mid
 */
package lucee.runtime.functions.string

import lucee.runtime.PageContext

class Mid : BIF() {
    @Override
    @Throws(PageException::class)
    operator fun invoke(pc: PageContext?, args: Array<Object?>?): Object? {
        if (args!!.size == 2) return call(pc, Caster.toString(args[0]), Caster.toDoubleValue(args[1]))
        if (args.size == 3) return call(pc, Caster.toString(args[0]), Caster.toDoubleValue(args[1]), Caster.toDoubleValue(args[2]))
        throw FunctionException(pc, "Mid", 2, 3, args.size)
    }

    companion object {
        @Throws(ExpressionException::class)
        fun call(pc: PageContext?, str: String?, start: Double): String? {
            return call(pc, str, start, -1.0)
        }

        @Throws(ExpressionException::class)
        fun call(pc: PageContext?, str: String?, start: Double, count: Double): String? {
            val s = (start - 1).toInt()
            var c = count.toInt()
            if (s < 0) throw ExpressionException("Parameter 2 of function mid which is now [" + (s + 1) + "] must be a positive integer")
            if (c == -1) c = str!!.length() else if (c < -1) throw ExpressionException("Parameter 3 of function mid which is now [$c] must be a non-negative integer or -1 (for string length)")
            c += s
            return if (s > str!!.length()) "" else if (c >= str!!.length()) str.substring(s) else {
                str.substring(s, c)
            }
        }
    }
}