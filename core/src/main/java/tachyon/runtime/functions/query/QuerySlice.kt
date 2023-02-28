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
 * Implements the CFML Function arraymin
 */
package tachyon.runtime.functions.query

import tachyon.runtime.PageContext

class QuerySlice : BIF() {
    @Override
    @Throws(PageException::class)
    operator fun invoke(pc: PageContext?, args: Array<Object?>?): Object? {
        return if (args!!.size == 2) call(pc, Caster.toQuery(args[0]), Caster.toDoubleValue(args[1])) else call(pc, Caster.toQuery(args[0]), Caster.toDoubleValue(args[1]), Caster.toDoubleValue(args[2]))
    }

    companion object {
        private const val serialVersionUID = -2760070317171532995L
        @Throws(PageException::class)
        fun call(pc: PageContext?, qry: Query?, offset: Double): Query? {
            return call(pc, qry, offset, 0.0)
        }

        @Throws(PageException::class)
        fun call(pc: PageContext?, qry: Query?, offset: Double, length: Double): Query? {
            val len: Int = qry.getRecordcount()
            if (len == 0) throw FunctionException(pc, "querySlice", 1, "query", "Query cannot be empty")
            if (offset > 0) {
                if (len < offset) throw FunctionException(pc, "querySlice", 2, "offset", "offset cannot be greater than the recordcount of the query")
                var to = 0
                if (length > 0) to = (offset + length - 1).toInt() else if (length <= 0) to = (len + length).toInt()
                if (len < to) throw FunctionException(pc, "querySlice", 3, "length", "offset+length cannot be greater than the recordcount of the query")
                return Companion[qry, offset.toInt(), to]
            }
            return call(pc, qry, len + offset, length)
        }

        @Throws(PageException::class)
        private operator fun get(qry: Query?, from: Int, to: Int): Query? {
            var columns: Array<Collection.Key?>?
            // print.out(from+"::"+to);
            val nq: Query = QueryImpl(qry.getColumnNames().also { columns = it }, 0, qry.getName())
            var row = 1
            for (i in from..to) {
                nq.addRow()
                for (y in columns.indices) {
                    nq.setAt(columns!![y], row, qry.getAt(columns!![y], i))
                }
                row++
            }
            return nq
        }
    }
}