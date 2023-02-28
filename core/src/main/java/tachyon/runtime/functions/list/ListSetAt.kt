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
 * Implements the CFML Function listsetat
 */
package tachyon.runtime.functions.list

import tachyon.runtime.PageContext

class ListSetAt : BIF() {
    @Override
    @Throws(PageException::class)
    operator fun invoke(pc: PageContext?, args: Array<Object?>?): Object? {
        if (args!!.size == 3) return call(pc, Caster.toString(args[0]), Caster.toDoubleValue(args[1]), Caster.toString(args[2]))
        if (args.size == 4) return call(pc, Caster.toString(args[0]), Caster.toDoubleValue(args[1]), Caster.toString(args[2]), Caster.toString(args[3]))
        if (args.size == 5) return call(pc, Caster.toString(args[0]), Caster.toDoubleValue(args[1]), Caster.toString(args[2]), Caster.toString(args[3]), Caster.toBooleanValue(args[4]))
        throw FunctionException(pc, "ListSetAt", 3, 5, args.size)
    }

    companion object {
        private const val serialVersionUID = -105782799713547552L
        @Throws(ExpressionException::class)
        fun call(pc: PageContext?, list: String?, posNumber: Double, value: String?): String? {
            return call(pc, list, posNumber, value, ",", false)
        }

        @Throws(ExpressionException::class)
        fun call(pc: PageContext?, list: String?, posNumber: Double, value: String?, delimiter: String?): String? {
            return call(pc, list, posNumber, value, delimiter, false)
        }

        @Throws(ExpressionException::class)
        fun call(pc: PageContext?, list: String?, posNumber: Double, value: String?, delimiter: String?, includeEmptyFields: Boolean): String? {
            if (list!!.length() === 0) throw FunctionException(pc, "listSetAt", 1, "list", "can't be empty")
            val pos = posNumber.toInt()
            // int[] removedInfo=new int[2];
            val arr: Array = ListUtil.listToArray(list, delimiter)
            val len: Int = arr.size()

            // invalid index
            if (pos < 1) throw FunctionException(pc, "listSetAt", 2, "position", "invalid string list index [$pos]") else if (len < pos) {
                throw FunctionException(pc, "listSetAt", 2, "position", "invalid string list index [$pos], indexes go from 1 to $len")
            }
            val sb = StringBuilder() // RepeatString.call(new StringBuffer(),delimiter,removedInfo[0]);
            var hasStart = false
            var hasSet = false
            var v: String
            var count = 0
            for (i in 1..len) {
                v = arr.get(i, "")
                if (hasStart) {
                    sb.append(delimiter)
                } else hasStart = true
                if (includeEmptyFields || v.length() > 0) count++
                if (!hasSet && pos == count) {
                    sb.append(value)
                    hasSet = true
                } else sb.append(arr.get(i, ""))
            }
            if (!hasSet) {
                throw FunctionException(pc, "listSetAt", 2, "position", "invalid string list index [$pos]")
            }
            return sb.toString()
        }
    }
}