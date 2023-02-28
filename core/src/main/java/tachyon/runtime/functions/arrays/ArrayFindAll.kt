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

class ArrayFindAll : BIF() {
    @Override
    @Throws(PageException::class)
    operator fun invoke(pc: PageContext?, args: Array<Object?>?): Object? {
        return if (args!!.size == 2) call(pc, Caster.toArray(args[0]), args[1]) else throw FunctionException(pc, "ArrayFindAll", 2, 2, args.size)
    }

    companion object {
        private const val serialVersionUID = -1757019034608924098L
        @Throws(PageException::class)
        fun call(pc: PageContext?, array: Array?, value: Object?): Array? {
            return if (value is UDF) find(pc, array, value as UDF?) else find(array, value, true)
        }

        @Throws(PageException::class)
        fun find(pc: PageContext?, array: Array?, udf: UDF?): Array? {
            val rtn: Array = ArrayImpl()
            val len: Int = array.size()
            val arr: Array<Object?> = arrayOfNulls<Object?>(1)
            var res: Object
            var b: Boolean
            for (i in 1..len) {
                arr[0] = array.get(i, null)
                if (arr[0] != null) {
                    res = udf.call(pc, arr, false)
                    b = Caster.toBoolean(res, null)
                    if (b == null) throw FunctionException(pc, "ArrayFindAll", 2, "function",
                            "return value of the " + (if (udf is Closure) "closure" else "function [" + udf.getFunctionName().toString() + "]") + " cannot be casted to a boolean value.",
                            CasterException.createMessage(res, "boolean"))
                    if (b.booleanValue()) {
                        rtn.appendEL(Caster.toDouble(i))
                    }
                }
            }
            return rtn
        }

        @Throws(PageException::class)
        fun find(array: Array?, value: Object?, caseSensitive: Boolean): Array? {
            val rtn: Array = ArrayImpl()
            val len: Int = array.size()
            val valueIsSimple: Boolean = Decision.isSimpleValue(value)
            var o: Object
            for (i in 1..len) {
                o = array.get(i, null)
                if (o != null && OpUtil.equals(ThreadLocalPageContext.get(), o, value, caseSensitive, !valueIsSimple)) {
                    rtn.appendEL(Caster.toDouble(i))
                }
            }
            return rtn
        }
    }
}