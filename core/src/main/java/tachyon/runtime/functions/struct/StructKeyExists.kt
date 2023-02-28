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
 * Implements the CFML Function structkeyexists
 */
package tachyon.runtime.functions.struct

import tachyon.runtime.PageContext

class StructKeyExists : BIF() {
    @Override
    @Throws(PageException::class)
    operator fun invoke(pc: PageContext?, args: Array<Object?>?): Object? {
        if (args!!.size == 2) return call(pc, Caster.toStruct(args[0]), Caster.toKey(args[1]))
        throw FunctionException(pc, "StructKeyExists", 2, 2, args.size)
    }

    companion object {
        private const val serialVersionUID = 7659087310641834209L
        fun call(pc: PageContext?, struct: tachyon.runtime.type.Struct?, key: String?): Boolean {
            return call(pc, struct, KeyImpl.init(key))
        }

        fun call(pc: PageContext?, struct: tachyon.runtime.type.Struct?, key: Collection.Key?): Boolean {
            if (struct is CollectionStruct) {
                val c: Collection = (struct as CollectionStruct?).getCollection()
                if (c is Query) {
                    return QueryColumnExists.call(pc, c as Query, key)
                }
            }
            if (struct is StructSupport) { // FUTURE make available in Struct
                if (!(struct as StructSupport?).containsKey(pc, key)) return false
            } else {
                if (!struct.containsKey(key)) return false
            }
            return if (NullSupportHelper.full(pc)) true else struct.get(key, null) != null
            // do not change, this has do be this way
        }
    }
}