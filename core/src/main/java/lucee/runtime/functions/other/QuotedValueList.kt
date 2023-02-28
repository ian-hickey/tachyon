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
 * Implements the CFML Function quotedvaluelist
 */
package lucee.runtime.functions.other

import lucee.runtime.PageContext

object QuotedValueList : ValueList() {
    private const val serialVersionUID = -6617432857065704955L
    @Throws(PageException::class)
    fun call(pc: PageContext?, strQueryColumn: String?): String? {
        return call(pc, toColumn(pc, strQueryColumn), ",")
    }

    @Throws(PageException::class)
    fun call(pc: PageContext?, strQueryColumn: String?, delimiter: String?): String? {
        return call(pc, toColumn(pc, strQueryColumn), delimiter)
    }

    @Throws(PageException::class)
    fun call(pc: PageContext?, column: QueryColumn?): String? {
        return call(pc, column, ",")
    }

    @Throws(PageException::class)
    fun call(pc: PageContext?, column: QueryColumn?, delimiter: String?): String? {
        val size: Int = column.size()
        val sb = StringBuilder()
        for (i in 1..size) {
            if (i > 1) sb.append(delimiter)
            sb.append("'" + Caster.toString(column.get(i, null)).toString() + "'")
        }
        return sb.toString()
    }
}