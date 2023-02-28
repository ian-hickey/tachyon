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
package lucee.runtime.functions.struct

import lucee.runtime.PageContext

class StructSome : BIF() {
    @Override
    @Throws(PageException::class)
    operator fun invoke(pc: PageContext?, args: Array<Object?>?): Object? {
        if (args!!.size == 2) return call(pc, Caster.toStruct(args[0]), Caster.toFunction(args[1]))
        if (args.size == 3) return call(pc, Caster.toStruct(args[0]), Caster.toFunction(args[1]), Caster.toBooleanValue(args[2]))
        if (args.size == 4) return call(pc, Caster.toStruct(args[0]), Caster.toFunction(args[1]), Caster.toBooleanValue(args[2]), Caster.toDoubleValue(args[3]))
        throw FunctionException(pc, "StructSome", 2, 4, args.size)
    }

    companion object {
        private const val serialVersionUID = -7449844630816343951L
        @Throws(PageException::class)
        fun call(pc: PageContext?, sct: Struct?, udf: UDF?): Boolean {
            return _call(pc, sct, udf, false, 20)
        }

        @Throws(PageException::class)
        fun call(pc: PageContext?, sct: Struct?, udf: UDF?, parallel: Boolean): Boolean {
            return _call(pc, sct, udf, parallel, 20)
        }

        @Throws(PageException::class)
        fun call(pc: PageContext?, sct: Struct?, udf: UDF?, parallel: Boolean, maxThreads: Double): Boolean {
            return _call(pc, sct, udf, parallel, maxThreads.toInt())
        }

        @Throws(PageException::class)
        private fun _call(pc: PageContext?, sct: Struct?, udf: UDF?, parallel: Boolean, maxThreads: Int): Boolean {
            return Some.call(pc, sct, udf, parallel, maxThreads, ClosureFunc.TYPE_STRUCT)
        }
    }
}