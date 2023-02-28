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
package lucee.runtime.functions.query

import lucee.runtime.PageContext

class QueryEach : BIF() {
    @Override
    @Throws(PageException::class)
    operator fun invoke(pc: PageContext?, args: Array<Object?>?): Object? {
        if (args!!.size == 2) return call(pc, Caster.toQuery(args[0]), Caster.toFunction(args[1]))
        if (args.size == 3) return call(pc, Caster.toQuery(args[0]), Caster.toFunction(args[1]), Caster.toBooleanValue(args[2]))
        if (args.size == 4) return call(pc, Caster.toQuery(args[0]), Caster.toFunction(args[1]), Caster.toBooleanValue(args[2]), Caster.toDoubleValue(args[3]))
        throw FunctionException(pc, "QueryEach", 2, 4, args.size)
    }

    companion object {
        private const val serialVersionUID = 3004432895753378644L
        @Throws(PageException::class)
        fun call(pc: PageContext?, qry: Query?, udf: UDF?): String? {
            return _call(pc, qry, udf, false, 20)
        }

        @Throws(PageException::class)
        fun call(pc: PageContext?, qry: Query?, udf: UDF?, parallel: Boolean): String? {
            return _call(pc, qry, udf, parallel, 20)
        }

        @Throws(PageException::class)
        fun call(pc: PageContext?, qry: Query?, udf: UDF?, parallel: Boolean, maxThreads: Double): String? {
            return _call(pc, qry, udf, parallel, maxThreads.toInt())
        }

        @Throws(PageException::class)
        private fun _call(pc: PageContext?, qry: Query?, udf: UDF?, parallel: Boolean, maxThreads: Int): String? {
            return Each.call(pc, qry, udf, parallel, maxThreads)
        }
    }
}