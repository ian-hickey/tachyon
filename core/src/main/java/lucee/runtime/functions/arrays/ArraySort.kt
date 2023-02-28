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
 * Implements the CFML Function arraysort
 */
package lucee.runtime.functions.arrays

import java.util.Arrays

class ArraySort : BIF() {
    @Override
    @Throws(PageException::class)
    operator fun invoke(pc: PageContext?, args: Array<Object?>?): Object? {
        return if (args!!.size == 2) call(pc, Caster.toArray(args[0]), args[1]) else if (args.size == 3) call(pc, Caster.toArray(args[0]), args[1], Caster.toString(args[2])) else if (args.size == 4) call(pc, Caster.toArray(args[0]), args[1], Caster.toString(args[2]), Caster.toBooleanValue(args[3])) else throw FunctionException(pc, "ArraySort", 2, 4, args.size)
    }

    companion object {
        private const val serialVersionUID = -747941236369495141L
        @Throws(PageException::class)
        fun call(pc: PageContext?, objArr: Object?, sortTypeOrClosure: Object?): Boolean {
            return call(pc, objArr, sortTypeOrClosure, "asc", false)
        }

        @Throws(PageException::class)
        fun call(pc: PageContext?, objArr: Object?, sortTypeOrClosure: Object?, sortorder: String?): Boolean {
            return call(pc, objArr, sortTypeOrClosure, sortorder, false)
        }

        @Throws(PageException::class)
        fun call(pc: PageContext?, objArr: Object?, sortTypeOrClosure: Object?, sortorder: String?, localeSensitive: Boolean): Boolean {

            // Comparator
            val comp: Comparator
            comp = if (sortTypeOrClosure is UDF) UDFComparator(pc, sortTypeOrClosure as UDF?) else ArrayUtil.toComparator(pc, Caster.toString(sortTypeOrClosure), sortorder, localeSensitive)

            // we always need to convert the original object, because we do not return the result
            if (objArr is Array) (objArr as Array?).sortIt(comp) else if (objArr is List) Collections.sort(objArr as List?, comp) else if (objArr is Array<Object>) Arrays.sort(objArr as Array<Object?>?, comp) else if (objArr is ByteArray) Arrays.sort(objArr as ByteArray?) else if (objArr is CharArray) Arrays.sort(objArr as CharArray?) else if (objArr is ShortArray) Arrays.sort(objArr as ShortArray?) else if (objArr is IntArray) Arrays.sort(objArr as IntArray?) else if (objArr is LongArray) Arrays.sort(objArr as LongArray?) else if (objArr is FloatArray) Arrays.sort(objArr as FloatArray?) else if (objArr is DoubleArray) Arrays.sort(objArr as DoubleArray?) else throw FunctionException(pc, "ArraySort", 1, "array", "cannot sort object from type [" + Caster.toTypeName(objArr).toString() + "]")
            return true
        }

        // used for member function
        @Throws(PageException::class)
        fun call(pc: PageContext?, array: Array?, sortTypeOrClosure: Object?): Boolean {
            return call(pc, array, sortTypeOrClosure, "asc", false)
        }

        @Throws(PageException::class)
        fun call(pc: PageContext?, array: Array?, sortTypeOrClosure: Object?, sortorder: String?): Boolean {
            return call(pc, array, sortTypeOrClosure, sortorder, false)
        }

        @Throws(PageException::class)
        fun call(pc: PageContext?, arr: Array?, sortTypeOrClosure: Object?, sortorder: String?, localeSensitive: Boolean): Boolean {
            // Comparator
            val comp: Comparator
            comp = if (sortTypeOrClosure is UDF) UDFComparator(pc, sortTypeOrClosure as UDF?) else ArrayUtil.toComparator(pc, Caster.toString(sortTypeOrClosure), sortorder, localeSensitive)
            arr.sortIt(comp)
            return true
        }
    }
}

internal class UDFComparator(pc: PageContext?, udf: UDF?) : Comparator<Object?> {
    private val udf: UDF?
    private val args: Array<Object?>? = arrayOfNulls<Object?>(2)
    private val pc: PageContext?
    @Override
    fun compare(oLeft: Object?, oRight: Object?): Int {
        return try {
            args!![0] = oLeft
            args[1] = oRight
            val res: Object = udf.call(pc, args, false)
            val i: Integer = Caster.toInteger(res, null)
                    ?: throw FunctionException(pc, "ArraySort", 2, "function",
                            "return value of the " + (if (udf is Closure) "closure" else "function [" + udf.getFunctionName().toString() + "]") + " cannot be casted to an integer.",
                            CasterException.createMessage(res, "integer"))
            i.intValue()
        } catch (pe: PageException) {
            throw PageRuntimeException(pe)
        }
    }

    init {
        this.pc = pc
        this.udf = udf
    }
}