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
package tachyon.runtime.functions.query

import tachyon.commons.lang.StringUtil

class QueryAddColumn : BIF() {
    @Override
    @Throws(PageException::class)
    operator fun invoke(pc: PageContext?, args: Array<Object?>?): Object? {
        if (args!!.size == 2) return call(pc, Caster.toQuery(args[0]), Caster.toString(args[1]))
        return if (args.size == 3) call(pc, Caster.toQuery(args[0]), Caster.toString(args[1]), args[2]) else call(pc, Caster.toQuery(args[0]), Caster.toString(args[1]), args[2], args[3])
    }

    companion object {
        private const val serialVersionUID = -242783888553490683L
        @Throws(PageException::class)
        fun call(pc: PageContext?, query: Query?, string: String?): Double {
            return call(pc, query, string, null, ArrayImpl())
        }

        @Throws(PageException::class)
        fun call(pc: PageContext?, query: Query?, string: String?, arrayOrDataType: Object?): Double {
            return if (!Decision.isArray(arrayOrDataType)) call(pc, query, string, Caster.toString(arrayOrDataType), ArrayImpl()) else call(pc, query, string, null, Caster.toArray(arrayOrDataType))
        }

        @Throws(PageException::class)
        fun call(pc: PageContext?, query: Query?, string: String?, datatype: Object?, array: Object?): Double {
            if (StringUtil.isEmpty(datatype)) query.addColumn(KeyImpl.init(string), Caster.toArray(array)) else query.addColumn(KeyImpl.init(string), Caster.toArray(array), SQLCaster.toSQLType(Caster.toString(datatype)))
            return query.size()
        }
    }
}