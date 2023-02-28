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
 * Implements the CFML Function valuelist
 */
package lucee.runtime.functions.query

import lucee.commons.lang.ExceptionUtil

class ValueArray : BIF() {
    @Override
    @Throws(PageException::class)
    operator fun invoke(pc: PageContext?, args: Array<Object?>?): Object? {
        return if (args!![0] is QueryColumn) call(pc, args[0] as QueryColumn?) else call(pc, Caster.toString(args[0]))
    }

    companion object {
        private const val serialVersionUID = -1810991362001086246L
        @Throws(PageException::class)
        fun call(pc: PageContext?, column: QueryColumn?): Array? {
            val arr: Array = ArrayImpl()
            val size: Int = column.size()
            var obj: Object
            val type: Short = SQLCaster.toCFType(column.getType(), lucee.commons.lang.CFTypes.TYPE_UNDEFINED)
            for (i in 1..size) {
                obj = column.get(i, null)
                try {
                    obj = Caster.castTo(pc, type, column.getTypeAsString(), obj)
                } catch (t: Throwable) {
                    ExceptionUtil.rethrowIfNecessary(t)
                }
                arr.append(obj)
            }
            return arr
        }

        @Throws(PageException::class)
        fun call(pc: PageContext?, strQueryColumn: String?): Array? {
            return call(pc, ValueList.toColumn(pc, strQueryColumn))
        }
    }
}