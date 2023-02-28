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
 * Implements the CFML Function left
 */
package tachyon.runtime.functions.string

import tachyon.runtime.PageContext

class Left : BIF() {
    @Override
    @Throws(PageException::class)
    operator fun invoke(pc: PageContext?, args: Array<Object?>?): Object? {
        if (args!!.size == 2) return call(pc, Caster.toString(args[0]), Caster.toDoubleValue(args[1]))
        throw FunctionException(pc, "Left", 2, 2, args.size)
    }

    companion object {
        private const val serialVersionUID = 571667661130843970L
        @Throws(ExpressionException::class)
        fun call(pc: PageContext?, str: String?, number: Double): String? {
            var len = number.toInt()
            if (len == 0) throw ExpressionException("parameter 2 of the function left can not be 0")
            if (Math.abs(len) >= str!!.length()) return str
            if (len < 0) len = str!!.length() + len
            return str.substring(0, len)
        }
    }
}