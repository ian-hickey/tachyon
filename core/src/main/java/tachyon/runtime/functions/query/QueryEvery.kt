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
package tachyon.runtime.functions.query

import tachyon.runtime.PageContext

class QueryEvery : BIF() {
    @Override
    @Throws(PageException::class)
    operator fun invoke(pc: PageContext?, args: Array<Object?>?): Object? {
        if (args!!.size == 2) return call(pc, Caster.toQuery(args[0]), Caster.toFunction(args[1]))
        if (args.size == 3) return call(pc, Caster.toQuery(args[0]), Caster.toFunction(args[1]), Caster.toBooleanValue(args[2]))
        if (args.size == 4) return call(pc, Caster.toQuery(args[0]), Caster.toFunction(args[1]), Caster.toBooleanValue(args[2]), Caster.toDoubleValue(args[3]))
        throw FunctionException(pc, "QueryEvery", 2, 4, args.size)
    }

    companion object {
        private const val serialVersionUID = -9206289776586881074L
        @Throws(PageException::class)
        fun call(pc: PageContext?, qry: Query?, udf: UDF?): Boolean {
            return _call(pc, qry, udf, false, 20)
        }

        @Throws(PageException::class)
        fun call(pc: PageContext?, qry: Query?, udf: UDF?, parallel: Boolean): Boolean {
            return _call(pc, qry, udf, parallel, 20)
        }

        @Throws(PageException::class)
        fun call(pc: PageContext?, qry: Query?, udf: UDF?, parallel: Boolean, maxThreads: Double): Boolean {
            return _call(pc, qry, udf, parallel, maxThreads.toInt())
        }

        @Throws(PageException::class)
        private fun _call(pc: PageContext?, qry: Query?, udf: UDF?, parallel: Boolean, maxThreads: Int): Boolean {
            return Every.call(pc, qry, udf, parallel, maxThreads, ClosureFunc.TYPE_QUERY)
        }
    }
}