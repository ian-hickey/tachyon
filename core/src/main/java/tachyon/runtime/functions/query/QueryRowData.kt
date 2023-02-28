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

import tachyon.commons.lang.StringUtil

/**
 * implements BIF QueryRowData
 */
class QueryRowData : BIF() {
    @Override
    @Throws(PageException::class)
    operator fun invoke(pc: PageContext?, args: Array<Object?>?): Object? {
        return if (args!!.size == 2) call(pc, Caster.toQuery(args[0]), Caster.toDouble(args[1])) else call(pc, Caster.toQuery(args[0]), Caster.toInteger(args[1]), Caster.toString(args[2]))
    }

    companion object {
        // is this needed?
        private const val serialVersionUID = -5234853923691806118L
        @Throws(PageException::class)
        fun call(pc: PageContext?, query: Query?, rowNumber: Double): Object? {
            return call(pc, query, rowNumber, "struct")
        }

        @Throws(PageException::class)
        fun call(pc: PageContext?, query: Query?, rowNumber: Double, returnFormat: String?): Object? {
            val row: Int = Caster.toInteger(rowNumber)
            if (row < 1 || row > query.getRecordcount()) throw FunctionException(pc, QueryRowData::class.java.getSimpleName(), 2, "rowNumber",
                    "The argument rowNumber [" + row + "] must be between 1 and the query's record count [" + query.getRecordcount() + "]")
            val colNames: Array<Collection.Key?> = query.getColumnNames()
            if (!StringUtil.isEmpty(returnFormat, true)) {
                if ("array".equalsIgnoreCase(returnFormat.trim())) {
                    val resultArray: Array = ArrayImpl()
                    for (col in colNames.indices) {
                        resultArray.append(query.getAt(colNames[col], row, NullSupportHelper.empty(pc)))
                    }
                    return resultArray
                }
            }
            val result: Struct = StructImpl()
            for (col in colNames.indices) result.setEL(colNames[col], query.getAt(colNames[col], row, NullSupportHelper.empty(pc)))
            return result
        }
    }
}