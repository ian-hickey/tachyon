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
package tachyon.runtime.util

import tachyon.runtime.PageContext

object CallerUtil {
    const val TYPE_DATA = 1
    const val TYPE_UDF_ARGS = 2
    const val TYPE_UDF_NAMED_ARGS = 3
    const val TYPE_BIF = 4
    operator fun get(pc: PageContext?, coll: Object?, keys: Array<Key?>?, defaultValue: Object?): Object? {
        var coll: Object = coll ?: return defaultValue
        val to = keys!!.size - 1
        for (i in 0..to) {
            coll = (pc.getVariableUtil() as VariableUtilImpl).getCollection(pc, coll, keys[i], Null.NULL)
            if (coll === Null.NULL || coll == null && i < to) return defaultValue
        }
        return coll
    }

    // TODO work in progress
    @Throws(PageException::class)
    operator fun get(pc: PageContext?, coll: Object?, types: IntArray?, keys: Array<Key?>?, args: Array<Array<Object?>?>?, defaultValue: Object?): Object? {
        var coll: Object? = coll ?: return defaultValue
        val to = keys!!.size - 1
        val vu: VariableUtilImpl = pc.getVariableUtil()
        for (i in 0..to) {
            when (types!![i]) {
                TYPE_DATA -> coll = vu.getCollection(pc, coll, keys[i], Null.NULL)
                TYPE_UDF_ARGS -> coll = vu!!.callFunctionWithoutNamedValues(pc, coll, keys[i], args!![i], false, Null.NULL)
                TYPE_UDF_NAMED_ARGS -> coll = vu!!.callFunctionWithNamedValues(pc, coll, keys[i], args!![i], false, Null.NULL)
                TYPE_BIF -> coll = null // TODO
            }
            if (coll === Null.NULL || coll == null && i < to) return defaultValue
        }
        return coll
    }
}