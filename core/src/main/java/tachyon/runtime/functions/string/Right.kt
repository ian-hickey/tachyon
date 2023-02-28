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
 * Implements the CFML Function right
 */
package tachyon.runtime.functions.string

import tachyon.runtime.PageContext

class Right : BIF() {
    @Override
    @Throws(PageException::class)
    operator fun invoke(pc: PageContext?, args: Array<Object?>?): Object? {
        if (args!!.size == 2) return call(pc, Caster.toString(args[0]), Caster.toDoubleValue(args[1]))
        throw FunctionException(pc, "Right", 2, 2, args.size)
    }

    companion object {
        private const val serialVersionUID = 2270997683293984478L
        @Throws(ExpressionException::class)
        fun call(pc: PageContext?, str: String?, number: Double): String? {
            var len = number.toInt()
            if (len == 0) throw ExpressionException("parameter 2 of the function right can not be 0")
            val l: Int = str!!.length()
            if (Math.abs(len) >= l) return str
            if (len < 0) len = l + len
            return str.substring(l - len, l)
        }
    }
}