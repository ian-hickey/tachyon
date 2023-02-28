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
 * Implements the CFML Function queryaddcolumn
 */
package lucee.runtime.functions.query

import lucee.runtime.PageContext

class QuerySetColumn : BIF() {
    @Override
    @Throws(PageException::class)
    operator fun invoke(pc: PageContext?, args: Array<Object?>?): Object? {
        return call(pc, Caster.toQuery(args!![0]), Caster.toString(args[1]), Caster.toString(args[2]))
    }

    companion object {
        private const val serialVersionUID = -268309857190767441L
        @Throws(PageException::class)
        fun call(pc: PageContext?, query: Query?, columnName: String?, newColumnName: String?): String? {
            var columnName = columnName
            var newColumnName = newColumnName
            columnName = columnName.trim()
            newColumnName = newColumnName.trim()
            val src: Collection.Key = KeyImpl.getInstance(columnName)
            val trg: Collection.Key = KeyImpl.getInstance(newColumnName)
            val qp: Query = Caster.toQuery(query, null)
            if (qp != null) qp.rename(src, trg) else {
                val qc: QueryColumn = query.removeColumn(src)
                val content: Array = ArrayImpl()
                val len: Int = qc.size()
                for (i in 1..len) {
                    content.setE(i, qc.get(i, null))
                }
                query.addColumn(trg, content, qc.getType())
            }
            return null
        }
    }
}