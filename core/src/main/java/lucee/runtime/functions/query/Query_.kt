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
 * creates a CFML query Column
 */
package lucee.runtime.functions.query

import lucee.runtime.PageContext

class Query_ : BIF() {
    @Override
    @Throws(PageException::class)
    operator fun invoke(pc: PageContext?, args: Array<Object?>?): Object? {
        return call(pc, args!![0] as Array<Object?>?)
    }

    companion object {
        private const val serialVersionUID = -3496695992298284984L

        @Throws(DatabaseException::class)
        fun call(pc: PageContext?, arr: Array<Object?>?): Query? {
            val names = arrayOfNulls<String?>(arr!!.size)
            val columns: Array<Array?> = arrayOfNulls<Array?>(arr.size)
            var count = 0
            for (i in arr.indices) {
                if (arr[i] is FunctionValue) {
                    val vf: FunctionValue? = arr[i] as FunctionValue?
                    if (vf.getValue() is Array) {
                        names[count] = vf.getNameAsString()
                        columns[count] = vf.getValue() as Array
                        count++
                    } else throw DatabaseException("invalid argument for function query, only array as value are allowed", "example: query(column1:array(1,2,3))", null, null)
                } else throw DatabaseException("invalid argument for function query, only named argument are allowed", "example: query(column1:array(1,2,3))", null, null)
            }
            return QueryImpl(CollectionUtil.toKeys(names, true), columns, "query")
        }
    }
}