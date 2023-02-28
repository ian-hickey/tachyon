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
 * Implements the CFML Function arraymin
 */
package lucee.runtime.functions.arrays

import lucee.runtime.PageContext

class ArraySlice : BIF() {
    @Override
    @Throws(PageException::class)
    operator fun invoke(pc: PageContext?, args: Array<Object?>?): Object? {
        return if (args!!.size == 2) call(pc, Caster.toArray(args[0]), Caster.toDoubleValue(args[1])) else if (args.size == 3) call(pc, Caster.toArray(args[0]), Caster.toDoubleValue(args[1]), Caster.toDoubleValue(args[2])) else throw FunctionException(pc, "ArraySlice", 2, 3, args.size)
    }

    companion object {
        private const val serialVersionUID = 7309769117464009924L
        @Throws(PageException::class)
        fun call(pc: PageContext?, arr: Array?, offset: Double): Array? {
            return call(pc, arr, offset, 0.0)
        }

        @Throws(PageException::class)
        fun call(pc: PageContext?, arr: Array?, offset: Double, length: Double): Array? {
            val len: Int = arr.size()
            if (len == 0) throw FunctionException(pc, "arraySlice", 1, "array", "Array cannot be empty")
            if (offset > 0) {
                if (len < offset) throw FunctionException(pc, "arraySlice", 2, "offset", "Offset cannot be greater than size of the array")
                var to = 0
                if (length > 0) to = (offset + length - 1).toInt() else if (length < 0) to = (len + length).toInt()
                if (len < to) throw FunctionException(pc, "arraySlice", 3, "length", "Offset+length cannot be greater than size of the array")
                return Companion[arr, offset.toInt(), to]
            }
            return call(pc, arr, len + offset, length)
        }

        @Throws(PageException::class)
        operator fun get(arr: Array?, from: Int, to: Int): Array? {
            val rtn: Array = ArrayUtil.getInstance(arr.getDimension())
            val keys: IntArray = arr.intKeys()
            for (i in keys.indices) {
                val key = keys[i]
                if (key < from) continue
                if (to > 0 && key > to) break
                rtn.append(arr.getE(key))
            }
            return rtn
        }
    }
}