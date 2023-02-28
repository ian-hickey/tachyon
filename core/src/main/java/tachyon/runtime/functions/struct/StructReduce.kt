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
 * Implements the CFML Function ArrayFilter
 */
package tachyon.runtime.functions.struct

import tachyon.runtime.PageContext

class StructReduce : BIF() {
    @Override
    @Throws(PageException::class)
    operator fun invoke(pc: PageContext?, args: Array<Object?>?): Object? {
        if (args!!.size == 2) return call(pc, Caster.toStruct(args[0]), Caster.toFunction(args[1]))
        if (args.size == 3) return call(pc, Caster.toStruct(args[0]), Caster.toFunction(args[1]), args[2])
        throw FunctionException(pc, "StructReduce", 2, 3, args.size)
    }

    companion object {
        private const val serialVersionUID = 2268062574022295144L
        @Throws(PageException::class)
        fun call(pc: PageContext?, sct: Struct?, udf: UDF?): Object? {
            return Reduce.call(pc, sct, udf, null, ClosureFunc.TYPE_STRUCT)
        }

        @Throws(PageException::class)
        fun call(pc: PageContext?, sct: Struct?, udf: UDF?, initValue: Object?): Object? {
            return Reduce.call(pc, sct, udf, initValue, ClosureFunc.TYPE_STRUCT)
        }
    }
}