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
 * Implements the CFML Function arrayavg
 */
package tachyon.runtime.functions.query

import tachyon.commons.lang.CFTypes

class QueryFilter : BIF() {
    @Override
    @Throws(PageException::class)
    operator fun invoke(pc: PageContext?, args: Array<Object?>?): Object? {
        if (args!!.size == 2) return call(pc, Caster.toQuery(args[0]), Caster.toFunction(args[1]))
        if (args.size == 3) return call(pc, Caster.toQuery(args[0]), Caster.toFunction(args[1]), Caster.toBooleanValue(args[2]))
        if (args.size == 4) return call(pc, Caster.toQuery(args[0]), Caster.toFunction(args[1]), Caster.toBooleanValue(args[2]), Caster.toDoubleValue(args[3]))
        throw FunctionException(pc, "QueryFilter", 2, 4, args.size)
    }

    companion object {
        private const val serialVersionUID = 6823791561366291738L
        @Throws(PageException::class)
        fun call(pc: PageContext?, qry: Query?, udf: UDF?): Query? {
            return _call(pc, qry, udf, false, 20)
        }

        @Throws(PageException::class)
        fun call(pc: PageContext?, qry: Query?, udf: UDF?, parallel: Boolean): Query? {
            return _call(pc, qry, udf, parallel, 20)
        }

        @Throws(PageException::class)
        fun call(pc: PageContext?, qry: Query?, udf: UDF?, parallel: Boolean, maxThreads: Double): Query? {
            return _call(pc, qry, udf, parallel, maxThreads.toInt())
        }

        @Throws(PageException::class)
        fun _call(pc: PageContext?, qry: Query?, filter: UDF?, parallel: Boolean, maxThreads: Int): Query? {

            // check UDF return type
            val type: Int = filter.getReturnType()
            if (type != CFTypes.TYPE_BOOLEAN && type != CFTypes.TYPE_ANY) throw ExpressionException("invalid return type [" + filter.getReturnTypeAsString().toString() + "] for UDF Filter, valid return types are [boolean,any]")
            return Filter.call(pc, qry, filter, parallel, maxThreads, Filter.TYPE_QUERY) as Query
        }
    }
}