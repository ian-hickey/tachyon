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

class QueryGetCell : BIF() {
    @Override
    @Throws(PageException::class)
    operator fun invoke(pc: PageContext?, args: Array<Object?>?): Object? {
        return if (args!!.size == 2) call(pc, Caster.toQuery(args[0]), Caster.toString(args[1])) else call(pc, Caster.toQuery(args[0]), Caster.toString(args[1]), Caster.toDoubleValue(args[2]))
    }

    companion object {
        private const val serialVersionUID = -6234552570552045133L
        @Throws(PageException::class)
        fun call(pc: PageContext?, query: Query?, columnName: String?): Object? {
            return call(pc, query, columnName, query.getRecordcount())
        }

        @Throws(PageException::class)
        fun call(pc: PageContext?, query: Query?, columnName: String?, rowNumber: Double): Object? {
            var rowNumber = rowNumber
            if (rowNumber == -9999.0) rowNumber = query.getRecordcount() // used for named arguments
            return query.getAt(KeyImpl.init(columnName), rowNumber.toInt())
        }
    }
}