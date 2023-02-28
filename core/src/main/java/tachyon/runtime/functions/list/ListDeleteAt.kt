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
 * Implements the CFML Function listdeleteat
 */
package tachyon.runtime.functions.list

import tachyon.runtime.PageContext

class ListDeleteAt : BIF() {
    @Override
    @Throws(PageException::class)
    operator fun invoke(pc: PageContext?, args: Array<Object?>?): Object? {
        if (args!!.size == 2) return call(pc, Caster.toString(args[0]), Caster.toDoubleValue(args[1]), ",", false)
        if (args.size == 3) return call(pc, Caster.toString(args[0]), Caster.toDoubleValue(args[1]), Caster.toString(args[2]), false)
        if (args.size == 4) return call(pc, Caster.toString(args[0]), Caster.toDoubleValue(args[1]), Caster.toString(args[2]), Caster.toBooleanValue(args[3]))
        throw FunctionException(pc, "ListDeleteAt", 2, 4, args.size)
    }

    companion object {
        private const val serialVersionUID = 7050644316663288912L
        private val DEFAULT_DELIMITER: CharArray? = charArrayOf(',')
        @Throws(ExpressionException::class)
        fun call(pc: PageContext?, list: String?, posNumber: Double): String? {
            return _call(pc, list, posNumber.toInt(), DEFAULT_DELIMITER, false)
        }

        @Throws(ExpressionException::class)
        fun call(pc: PageContext?, list: String?, posNumber: Double, del: String?): String? {
            return _call(pc, list, posNumber.toInt(), del.toCharArray(), false)
        }

        @Throws(ExpressionException::class)
        fun call(pc: PageContext?, list: String?, posNumber: Double, del: String?, includeEmptyFields: Boolean): String? {
            return _call(pc, list, posNumber.toInt(), del.toCharArray(), includeEmptyFields)
        }

        @Throws(ExpressionException::class)
        fun _call(pc: PageContext?, list: String?, pos: Int, del: CharArray?, includeEmptyFields: Boolean): String? {
            var pos = pos
            val sb = StringBuilder()
            val len: Int = list!!.length()
            var index = 0
            var last = 0.toChar()
            var c: Char
            if (pos < 1) throw FunctionException(pc, "ListDeleteAt", 2, "index", "index must be greater than 0")
            pos--
            var i = 0

            // ignore all delimiter at start
            if (!includeEmptyFields) while (i < len) {
                c = list.charAt(i)
                if (!equal(del, c)) break
                sb.append(c)
                i++
            }

            // before
            while (i < len) {
                c = list.charAt(i)
                if (index == pos && !equal(del, c)) break
                if (equal(del, c)) {
                    if (includeEmptyFields || !equal(del, last)) index++
                }
                sb.append(c)
                last = c
                i++
            }

            // suppress item
            while (i < len) {
                if (equal(del, list.charAt(i))) break
                i++
            }

            // ignore following delimiter
            while (i < len) {
                if (!equal(del, list.charAt(i))) break
                i++
            }
            if (i == len) {
                while (sb.length() > 0 && equal(del, sb.charAt(sb.length() - 1))) {
                    sb.delete(sb.length() - 1, sb.length())
                }
                if (pos > index) throw FunctionException(pc, "ListDeleteAt", 2, "index", "index must be an integer between 1 and $index")
                return sb.toString()
            }

            // fill the rest
            while (i < len) {
                sb.append(list.charAt(i))
                i++
            }
            return sb.toString()
        }

        private fun equal(del: CharArray?, c: Char): Boolean {
            for (i in del.indices) {
                if (del!![i] == c) return true
            }
            return false
        }
    }
}