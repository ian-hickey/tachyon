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
package tachyon.runtime.functions.list

import tachyon.runtime.PageContext

class ListReduce : BIF() {
    @Override
    @Throws(PageException::class)
    operator fun invoke(pc: PageContext?, args: Array<Object?>?): Object? {
        if (args!!.size == 2) return call(pc, Caster.toString(args[0]), Caster.toFunction(args[1]))
        if (args.size == 3) return call(pc, Caster.toString(args[0]), Caster.toFunction(args[1]), args[2])
        if (args.size == 4) return call(pc, Caster.toString(args[0]), Caster.toFunction(args[1]), args[2], Caster.toString(args[3]))
        if (args.size == 5) return call(pc, Caster.toString(args[0]), Caster.toFunction(args[1]), args[2], Caster.toString(args[3]), Caster.toBooleanValue(args[4]))
        if (args.size == 6) return call(pc, Caster.toString(args[0]), Caster.toFunction(args[1]), args[2], Caster.toString(args[3]), Caster.toBooleanValue(args[4]),
                Caster.toBooleanValue(args[5]))
        throw FunctionException(pc, "ListReduce", 2, 6, args.size)
    }

    companion object {
        private const val serialVersionUID = 1857478124366819325L
        @Throws(PageException::class)
        fun call(pc: PageContext?, list: String?, udf: UDF?): Object? {
            return call(pc, list, udf, null, ",", false, true)
        }

        @Throws(PageException::class)
        fun call(pc: PageContext?, list: String?, udf: UDF?, initValue: Object?): Object? {
            return call(pc, list, udf, initValue, ",", false, true)
        }

        @Throws(PageException::class)
        fun call(pc: PageContext?, list: String?, udf: UDF?, initValue: Object?, delimiter: String?): Object? {
            return call(pc, list, udf, initValue, delimiter, false, true)
        }

        @Throws(PageException::class)
        fun call(pc: PageContext?, list: String?, udf: UDF?, initValue: Object?, delimiter: String?, includeEmptyFields: Boolean): Object? {
            return call(pc, list, udf, initValue, delimiter, includeEmptyFields, true)
        }

        @Throws(PageException::class)
        fun call(pc: PageContext?, list: String?, udf: UDF?, initValue: Object?, delimiter: String?, includeEmptyFields: Boolean, multiCharacterDelimiter: Boolean): Object? {
            val data = StringListData(list, delimiter, includeEmptyFields, multiCharacterDelimiter)
            return Reduce.call(pc, data, udf, initValue, ClosureFunc.TYPE_UNDEFINED)
        }
    }
}