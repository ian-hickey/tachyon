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

class QuerySetCell : BIF() {
    @Override
    @Throws(PageException::class)
    operator fun invoke(pc: PageContext?, args: Array<Object?>?): Object? {
        return if (args!!.size == 3) call(pc, Caster.toQuery(args[0]), Caster.toString(args[1]), args[2]) else call(pc, Caster.toQuery(args[0]), Caster.toString(args[1]), args[2], Caster.toDoubleValue(args[3]))
    }

    companion object {
        private const val serialVersionUID = -5234853923691806118L
        @Throws(PageException::class)
        fun call(pc: PageContext?, query: Query?, columnName: String?, value: Object?): Boolean {
            return call(pc, query, columnName, value, query.getRecordcount())
        }

        @Throws(PageException::class)
        fun call(pc: PageContext?, query: Query?, columnName: String?, value: Object?, rowNumber: Double): Boolean {
            var rowNumber = rowNumber
            if (rowNumber == -9999.0) rowNumber = query.getRecordcount() // used for named arguments
            query.setAt(KeyImpl.init(columnName.trim()), rowNumber.toInt(), value)
            return true
        }
    }
}