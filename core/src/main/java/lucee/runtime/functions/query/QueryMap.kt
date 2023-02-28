/**
 *
 * Copyright (c) 2016, Lucee Assosication Switzerland. All rights reserved.
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
package lucee.runtime.functions.query

import lucee.runtime.PageContext

class QueryMap : BIF() {
    @Override
    @Throws(PageException::class)
    operator fun invoke(pc: PageContext?, args: Array<Object?>?): Object? {
        if (args!!.size == 2) return call(pc, Caster.toQuery(args[0]), Caster.toFunction(args[1]))
        if (args.size == 3) return call(pc, Caster.toQuery(args[0]), Caster.toFunction(args[1]), Caster.toQuery(args[2]))
        if (args.size == 4) return call(pc, Caster.toQuery(args[0]), Caster.toFunction(args[1]), Caster.toQuery(args[2]), Caster.toBooleanValue(args[3]))
        if (args.size == 5) return call(pc, Caster.toQuery(args[0]), Caster.toFunction(args[1]), Caster.toQuery(args[2]), Caster.toBooleanValue(args[3]), Caster.toDoubleValue(args[4]))
        throw FunctionException(pc, "QueryMap", 2, 5, args.size)
    }

    companion object {
        private const val serialVersionUID = 5225631181634029456L
        @Throws(PageException::class)
        fun call(pc: PageContext?, qry: Query?, udf: UDF?): Query? {
            return _call(pc, qry, udf, null, false, 20)
        }

        @Throws(PageException::class)
        fun call(pc: PageContext?, qry: Query?, udf: UDF?, resQuery: Query?): Query? {
            return _call(pc, qry, udf, resQuery, false, 20)
        }

        @Throws(PageException::class)
        fun call(pc: PageContext?, qry: Query?, udf: UDF?, resQuery: Query?, parallel: Boolean): Query? {
            return _call(pc, qry, udf, resQuery, parallel, 20)
        }

        @Throws(PageException::class)
        fun call(pc: PageContext?, qry: Query?, udf: UDF?, resQuery: Query?, parallel: Boolean, maxThreads: Double): Query? {
            return _call(pc, qry, udf, resQuery, parallel, maxThreads.toInt())
        }

        @Throws(PageException::class)
        private fun _call(pc: PageContext?, qry: Query?, udf: UDF?, resQuery: Query?, parallel: Boolean, maxThreads: Int): Query? {
            return Map.call(pc, qry, udf, parallel, maxThreads, resQuery, ClosureFunc.TYPE_QUERY) as Query?
        }
    }
}