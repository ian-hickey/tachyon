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
package lucee.runtime.functions.query

import lucee.runtime.PageContext

class ValueListMember : BIF() {
    @Override
    @Throws(PageException::class)
    operator fun invoke(pc: PageContext?, args: Array<Object?>?): Object? {
        return if (args!!.size == 2) call(pc, Caster.toQuery(args[0]), Caster.toString(args[1])) else call(pc, Caster.toQuery(args[0]), Caster.toString(args[1]), Caster.toString(args[2]))
    }

    companion object {
        @Throws(PageException::class)
        fun call(pc: PageContext?, query: Query?, columnName: String?): String? {
            return call(pc, query, columnName, ",")
        }

        @Throws(PageException::class)
        fun call(pc: PageContext?, query: Query?, columnName: String?, delimiter: String?): String? {
            var delimiter = delimiter
            if (delimiter == null) delimiter = ","
            return ListUtil.arrayToList(QueryColumnData.call(pc, query, columnName, null), delimiter)
        }
    }
}