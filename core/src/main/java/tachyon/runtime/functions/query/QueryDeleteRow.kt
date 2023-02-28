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

class QueryDeleteRow : BIF() {
    @Override
    @Throws(PageException::class)
    operator fun invoke(pc: PageContext?, args: Array<Object?>?): Object? {
        return if (args!!.size == 1) call(pc, Caster.toQuery(args[0])) else call(pc, Caster.toQuery(args[0]), Caster.toDoubleValue(args[1]))
    }

    companion object {
        private const val serialVersionUID = 7610413135885802876L
        @Throws(PageException::class)
        fun call(pc: PageContext?, query: Query?): Boolean {
            return call(pc, query, query.getRowCount())
        }

        @Throws(PageException::class)
        fun call(pc: PageContext?, query: Query?, row: Double): Boolean {
            var row = row
            if (row == -9999.0) row = query.getRowCount() // used for named arguments
            query.removeRow(row.toInt())
            return true
        }
    }
}