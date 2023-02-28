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
 * Implements the CFML Function structsort
 */
package tachyon.runtime.functions.struct

import java.util.Arrays

class StructSort : BIF() {
    @Override
    @Throws(PageException::class)
    operator fun invoke(pc: PageContext?, args: Array<Object?>?): Object? {
        if (args!!.size == 4) return call(pc, Caster.toStruct(args[0]), Caster.toString(args[1]), Caster.toString(args[2]), Caster.toString(args[3]))
        if (args.size == 3) return call(pc, Caster.toStruct(args[0]), Caster.toString(args[1]), Caster.toString(args[2]))
        if (args.size == 2) return call(pc, Caster.toStruct(args[0]), Caster.toString(args[1]))
        if (args.size == 1) return call(pc, Caster.toStruct(args[0]))
        throw FunctionException(pc, "StructSort", 1, 4, args.size)
    }

    companion object {
        private const val serialVersionUID = -7945612992641626477L
        @Throws(PageException::class)
        fun call(pc: PageContext?, base: Struct?): Array? {
            return call(pc, base, "text", "asc", null)
        }

        @Throws(PageException::class)
        fun call(pc: PageContext?, base: Struct?, sortType: String?): Array? {
            return call(pc, base, sortType, "asc", null)
        }

        @Throws(PageException::class)
        fun call(pc: PageContext?, base: Struct?, sortType: String?, sortOrder: String?): Array? {
            return call(pc, base, sortType, sortOrder, null)
        }

        @Throws(PageException::class)
        fun call(pc: PageContext?, base: Struct?, sortType: String?, sortOrder: String?, pathToSubElement: String?): Array? {
            var isAsc = true
            var ee: PageException? = null
            isAsc = if (sortOrder.equalsIgnoreCase("asc")) true else if (sortOrder.equalsIgnoreCase("desc")) false else throw ExpressionException("invalid sort order type [$sortOrder], sort order types are [asc and desc]")
            val keys: Array<Collection.Key?> = CollectionUtil.keys(base)
            val arr: Array<SortRegister?> = arrayOfNulls<SortRegister?>(keys.size)
            val hasSubDef = pathToSubElement != null
            for (i in keys.indices) {
                var value: Object = base.get(keys[i], null)
                if (hasSubDef) {
                    value = VariableInterpreter.getVariable(pc, Caster.toCollection(value), pathToSubElement)
                }
                arr[i] = SortRegister(i, value)
            }
            var comp: ExceptionComparator? = null
            // text
            if (sortType.equalsIgnoreCase("text")) comp = SortRegisterComparator(pc, isAsc, false, false) else if (sortType.equalsIgnoreCase("textnocase")) comp = SortRegisterComparator(pc, isAsc, true, false) else if (sortType.equalsIgnoreCase("numeric")) comp = NumberSortRegisterComparator(isAsc) else {
                throw ExpressionException("invalid sort type [$sortType], sort types are [text, textNoCase, numeric]")
            }
            Arrays.sort(arr, 0, arr.size, comp)
            ee = comp.getPageException()
            if (ee != null) {
                throw ee
            }
            val rtn: Array = ArrayImpl()
            for (i in arr.indices) {
                rtn.append(keys[arr[i].getOldPosition()].getString())
            }
            return rtn
        }
    }
}