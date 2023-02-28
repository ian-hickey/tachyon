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

class QueryDeleteColumn : BIF() {
    @Override
    @Throws(PageException::class)
    operator fun invoke(pc: PageContext?, args: Array<Object?>?): Object? {
        return call(pc, Caster.toQuery(args!![0]), Caster.toString(args[1]))
    }

    companion object {
        private const val serialVersionUID = 5363459913899891827L
        @Throws(PageException::class)
        fun call(pc: PageContext?, query: Query?, strColumn: String?): Array? {
            return toArray(query.removeColumn(KeyImpl.init(strColumn)))
        }

        @Throws(PageException::class)
        fun toArray(column: QueryColumn?): Array? {
            val clone: Array = ArrayImpl()
            val len: Int = column.size()
            clone.resize(len)
            for (i in 1..len) {
                clone.setE(i, QueryUtil.getValue(column, i))
            }
            return clone
        }
    }
}