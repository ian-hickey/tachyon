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
 * Implements the CFML Function insert
 */
package tachyon.runtime.functions.string

import tachyon.runtime.PageContext

class Insert : BIF() {
    @Override
    @Throws(PageException::class)
    operator fun invoke(pc: PageContext?, args: Array<Object?>?): Object? {
        if (args!!.size == 3) return call(pc, Caster.toString(args[0]), Caster.toString(args[1]), Caster.toDoubleValue(args[2]))
        throw FunctionException(pc, "Insert", 2, 3, args.size)
    }

    companion object {
        private const val serialVersionUID = 5926183314989306282L
        @Throws(ExpressionException::class)
        fun call(pc: PageContext?, sub: String?, str: String?, pos: Double): String? {
            val p = pos.toInt()
            if (p < 0 || p > str!!.length()) throw ExpressionException("third parameter of the function insert, must be between 0 and " + str!!.length().toString() + " now [" + p.toString() + "]")
            val sb = StringBuilder(str!!.length() + sub!!.length())
            return sb.append(str.substring(0, p)).append(sub).append(str.substring(p)).toString()
        }
    }
}