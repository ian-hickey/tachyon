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
package tachyon.runtime.functions.arrays

import tachyon.runtime.PageContext

class ArrayMid : BIF() {
    @Override
    @Throws(PageException::class)
    operator fun invoke(pc: PageContext?, args: Array<Object?>?): Object? {
        return if (args!!.size == 2) call(pc, Caster.toArray(args[0]), Caster.toDoubleValue(args[1])) else if (args.size == 3) call(pc, Caster.toArray(args[0]), Caster.toDoubleValue(args[1]), Caster.toDoubleValue(args[2])) else throw FunctionException(pc, "ArrayMid", 2, 3, args.size)
    }

    companion object {
        private const val serialVersionUID = 4996354700884413289L
        @Throws(ExpressionException::class)
        fun call(pc: PageContext?, arr: Array?, start: Double): Array? {
            return call(pc, arr, start, -1.0)
        }

        @Throws(ExpressionException::class)
        fun call(pc: PageContext?, arr: Array?, start: Double, count: Double): Array? {
            val s = start.toInt()
            var c = count.toInt()
            if (s < 1) throw FunctionException(pc, "ArrayMid", 2, "start", "Parameter which is now [$s] must be a positive integer")
            if (c == -1) c = arr.size() else if (c < -1) throw FunctionException(pc, "ArrayMid", 3, "count", "Parameter which is now [$c] must be a non-negative integer or -1 (for string length)")
            c += s - 1
            if (s > arr.size()) return ArrayImpl()
            val rtn = ArrayImpl()
            val len: Int = arr.size()
            var value: Object
            var i = s
            while (i <= c && i <= len) {
                value = arr.get(i, null)
                rtn.appendEL(value)
                i++
            }
            return rtn
        }
    }
}