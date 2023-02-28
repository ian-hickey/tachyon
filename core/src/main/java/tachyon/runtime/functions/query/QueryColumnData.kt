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

import java.util.Iterator

class QueryColumnData : BIF() {
    @Override
    @Throws(PageException::class)
    operator fun invoke(pc: PageContext?, args: Array<Object?>?): Object? {
        return if (args!!.size == 2) call(pc, Caster.toQuery(args[0]), Caster.toString(args[1])) else call(pc, Caster.toQuery(args[0]), Caster.toString(args[1]), Caster.toFunction(args[2]))
    }

    companion object {
        private const val serialVersionUID = 3915214686428831274L
        @Throws(PageException::class)
        fun call(pc: PageContext?, query: Query?, columnName: String?): Array? {
            return call(pc, query, columnName, null)
        }

        @Throws(PageException::class)
        fun call(pc: PageContext?, query: Query?, columnName: String?, udf: UDF?): Array? {
            val arr: Array = ArrayImpl()
            val column: QueryColumn = query.getColumn(KeyImpl.init(columnName))
            val it: Iterator<Object?> = column.valueIterator()
            var value: Object?
            val type: Short = SQLCaster.toCFType(column.getType(), tachyon.commons.lang.CFTypes.TYPE_UNDEFINED)
            while (it.hasNext()) {
                value = it.next()
                if (!NullSupportHelper.full(pc) && value == null) value = ""

                // callback call
                if (udf != null) value = udf.call(pc, arrayOf<Object?>(value), true)

                // convert (if necessary)
                value = Caster.castTo(pc, type, column.getTypeAsString(), value, value)
                arr.append(value)
            }
            return arr
        }
    }
}