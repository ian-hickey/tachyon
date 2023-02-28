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
 * Implements the CFML Function replacelist
 */
package tachyon.runtime.functions.string

import java.util.Iterator

class ReplaceList : BIF() {
    @Override
    @Throws(PageException::class)
    operator fun invoke(pc: PageContext?, args: Array<Object?>?): Object? {
        if (args!!.size == 6) return _call(pc, Caster.toString(args[0]), Caster.toString(args[1]), Caster.toString(args[2]), Caster.toString(args[3]), Caster.toString(args[4]),
                false, Caster.toBooleanValue(args[5]))
        if (args.size == 5) {
            return if (Decision.isBoolean(args[4])) _call(pc, Caster.toString(args[0]), Caster.toString(args[1]), Caster.toString(args[2]), Caster.toString(args[3]), Caster.toString(args[3]), false, Caster.toBooleanValue(args[4])) else _call(pc, Caster.toString(args[0]), Caster.toString(args[1]), Caster.toString(args[2]), Caster.toString(args[3]), Caster.toString(args[4]), false, false)
        }
        if (args.size == 4) {
            return if (Decision.isBoolean(args[3])) _call(pc, Caster.toString(args[0]), Caster.toString(args[1]), Caster.toString(args[2]), ",", ",", false, Caster.toBooleanValue(args[3])) else _call(pc, Caster.toString(args[0]), Caster.toString(args[1]), Caster.toString(args[2]), Caster.toString(args[3]), ",", false, false)
        }
        if (args.size == 3) return _call(pc, Caster.toString(args[0]), Caster.toString(args[1]), Caster.toString(args[2]), ",", ",", false, false)
        throw FunctionException(pc, "ReplaceList", 3, 5, args.size)
    }

    companion object {
        private const val serialVersionUID = -3935300433837460732L
        fun call(pc: PageContext?, str: String?, list1: String?, list2: String?): String? {
            return _call(pc, str, list1, list2, ",", ",", false, false)
        }

        @Throws(PageException::class)
        fun call(pc: PageContext?, str: String?, list1: String?, list2: String?, delimiter_list1: String?): String? {
            return if (Decision.isBoolean(delimiter_list1)) _call(pc, str, list1, list2, ",", ",", false, Caster.toBooleanValue(delimiter_list1)) else _call(pc, str, list1, list2, delimiter_list1, delimiter_list1, false, false)
        }

        @Throws(PageException::class)
        fun call(pc: PageContext?, str: String?, list1: String?, list2: String?, delimiter_list1: String?, delimiter_list2: String?): String? {
            return if (Decision.isBoolean(delimiter_list2)) _call(pc, str, list1, list2, delimiter_list1, delimiter_list1, false, Caster.toBooleanValue(delimiter_list2)) else _call(pc, str, list1, list2, delimiter_list1, delimiter_list2, false, false)
        }

        fun call(pc: PageContext?, str: String?, list1: String?, list2: String?, delimiter_list1: String?, delimiter_list2: String?, includeEmptyFields: Boolean): String? {
            return _call(pc, str, list1, list2, delimiter_list1, delimiter_list2, false, includeEmptyFields)
        }

        fun _call(pc: PageContext?, str: String?, list1: String?, list2: String?, delimiter_list1: String?, delimiter_list2: String?, ignoreCase: Boolean, includeEmptyFields: Boolean): String? {
            var str = str
            var delimiter_list1 = delimiter_list1
            var delimiter_list2 = delimiter_list2
            if (delimiter_list1 == null) delimiter_list1 = ","
            if (delimiter_list2 == null) delimiter_list2 = ","
            val arr1: Array = ListUtil.listToArray(list1, delimiter_list1, false, false)
            val arr2: Array = ListUtil.listToArray(list2, delimiter_list2, includeEmptyFields, false)
            val it1: Iterator<Object?> = arr1.valueIterator()
            val it2: Iterator<Object?> = arr2.valueIterator()
            while (it1.hasNext()) {
                str = StringUtil.replace(str, Caster.toString(it1.next(), null), if (it2.hasNext()) Caster.toString(it2.next(), null) else "", false, ignoreCase)
            }
            return str
        }
    }
}