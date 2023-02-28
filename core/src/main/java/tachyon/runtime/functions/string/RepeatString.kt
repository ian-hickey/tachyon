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
 * Implements the CFML Function repeatstring
 */
package tachyon.runtime.functions.string

import tachyon.commons.lang.StringUtil

class RepeatString : BIF() {
    @Override
    @Throws(PageException::class)
    operator fun invoke(pc: PageContext?, args: Array<Object?>?): Object? {
        if (args!!.size == 2) return call(pc, Caster.toString(args[0]), Caster.toDoubleValue(args[1]))
        throw FunctionException(pc, "RepeatString", 2, 2, args.size)
    }

    companion object {
        private const val serialVersionUID = 6041471441971348584L
        @Throws(ExpressionException::class)
        fun call(pc: PageContext?, str: String?, count: Double): String? {
            if (count < 0) throw ExpressionException("Parameter 2 of function repeatString which is now [" + Caster.toString(count).toString() + "] must be a non-negative integer")
            return StringUtil.repeatString(str!!, count.toInt())
        }

        @Throws(ExpressionException::class)
        fun _call(pc: PageContext?, str: String?, count: Double): String? {
            val len = count.toInt()
            if (len < 0) throw ExpressionException("Parameter 2 of function repeatString which is now [$len] must be a non-negative integer")
            val chars: CharArray = str.toCharArray()
            val cb = StringBuilder(chars.size * len)
            for (i in 0 until len) cb.append(chars)
            return cb.toString()
        }

        @Throws(ExpressionException::class)
        fun call(sb: StringBuilder?, str: String?, count: Double): StringBuilder? {
            val len = count.toInt()
            if (len < 0) throw ExpressionException("Parameter 1 of function repeatString which is now [$len] must be a non-negative integer")
            for (i in 0 until len) sb.append(str)
            return sb
        }

        @Throws(ExpressionException::class)
        fun call(sb: StringBuilder?, c: Char, count: Double): StringBuilder? {
            val len = count.toInt()
            if (len < 0) throw ExpressionException("Parameter 1 of function repeatString which is now [$len] must be a non-negative integer")
            for (i in 0 until len) sb.append(c)
            return sb
        }
    }
}