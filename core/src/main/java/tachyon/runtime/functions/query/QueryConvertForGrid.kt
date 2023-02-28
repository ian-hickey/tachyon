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
 * Implements the CFML Function querysetcell
 */
package tachyon.runtime.functions.query

import tachyon.runtime.PageContext

class QueryConvertForGrid : BIF() {
    @Override
    @Throws(PageException::class)
    operator fun invoke(pc: PageContext?, args: Array<Object?>?): Object? {
        return call(pc, Caster.toQuery(args!![0]), Caster.toDoubleValue(args[1]), Caster.toDoubleValue(args[2]))
    }

    companion object {
        private const val serialVersionUID = 871091293736619034L
        @Throws(PageException::class)
        fun call(pc: PageContext?, src: Query?, dpage: Double, dpageSize: Double): Struct? {
            val page = dpage.toInt()
            val pageSize = dpageSize.toInt()
            if (page < 1) {
                throw FunctionException(pc, "QueryConvertForGrid", 2, "page", "page must be a positive number now ($page)")
            }
            val start = (page - 1) * pageSize + 1
            val end = start + pageSize
            val srcColumns: Array<Collection.Key?> = src.getColumnNames()
            val srcRows: Int = src.getRowCount()
            var trgRows = srcRows - start + 1
            if (trgRows > pageSize) trgRows = pageSize
            if (trgRows < 0) trgRows = 0
            val trg: Query = QueryImpl(srcColumns, trgRows, src.getName())
            var trgRow = 0
            var srcRow = start
            while (srcRow <= end && srcRow <= srcRows) {
                trgRow++
                for (col in srcColumns.indices) {
                    trg.setAtEL(srcColumns[col], trgRow, src.getAt(srcColumns[col], srcRow, null))
                }
                srcRow++
            }
            val sct: Struct = StructImpl()
            sct.setEL(KeyConstants._QUERY, trg)
            sct.setEL("TOTALROWCOUNT", Integer.valueOf(srcRows))
            return sct
        }
    }
}