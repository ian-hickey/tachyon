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
 * Implements the CFML Function querysetrow
 */
package tachyon.runtime.functions.query

import java.util.Iterator

class QuerySetRow : BIF() {
    @Override
    @Throws(PageException::class)
    operator fun invoke(pc: PageContext?, args: Array<Object?>?): Object? {
        if (args!!.size == 2) return call(pc, Caster.toQuery(args[0]), 0.0, args[1]) else if (args.size == 3) return call(pc, Caster.toQuery(args[0]), Caster.toDouble(args[1]), args[2])
        throw FunctionException(pc, "QuerySetRow", 2, 3, args.size)
    }

    companion object {
        private const val serialVersionUID = -5234853923691806118L
        @Throws(PageException::class)
        fun call(pc: PageContext?, query: Query?, rowNumber: Double, rowData: Object?): Boolean {
            var rowNumber = rowNumber
            if (rowNumber < 1) {
                query.addRow(1)
                rowNumber = query.getRecordcount()
            }
            val rn = rowNumber.toInt()
            val colNames: Array<Collection.Key?> = query.getColumnNames()
            if (Decision.isStruct(rowData)) {
                val it: Iterator<Entry<Key?, Object?>?> = Caster.toStruct(rowData).entryIterator()
                var e: Entry<Key?, Object?>?
                while (it.hasNext()) {
                    e = it.next()
                    query.setAt(e.getKey(), rn, e.getValue())
                }
            } else if (Decision.isArray(rowData)) {
                val data: Array = Caster.toArray(rowData)
                val dataLen: Int = data.size()
                for (col in colNames.indices) {
                    if (col == dataLen) break
                    query.setAt(colNames[col], rn, data.getE(col + 1))
                }
            } else {
                throw FunctionException(pc, QuerySetRow::class.java.getSimpleName(), 2, "rowData", "The argument [rowData] must be either a Struct or Array")
            }
            return true
        }
    }
}