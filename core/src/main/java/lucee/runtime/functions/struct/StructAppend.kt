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
 * Implements the CFML Function structappend
 */
package lucee.runtime.functions.struct

import java.util.Iterator

class StructAppend : BIF() {
    @Override
    @Throws(PageException::class)
    operator fun invoke(pc: PageContext?, args: Array<Object?>?): Object? {
        if (args!!.size == 3) return call(pc, Caster.toStruct(args[0]), Caster.toStruct(args[1]), Caster.toBooleanValue(args[2]))
        if (args.size == 2) return call(pc, Caster.toStruct(args[0]), Caster.toStruct(args[1]))
        throw FunctionException(pc, "StructAppend", 2, 3, args.size)
    }

    companion object {
        private const val serialVersionUID = 6131382324325758447L
        @Throws(PageException::class)
        fun call(pc: PageContext?, struct1: Struct?, struct2: Struct?): Boolean {
            return call(pc, struct1, struct2, true)
        }

        @Throws(PageException::class)
        fun call(pc: PageContext?, struct1: Struct?, struct2: Struct?, overwrite: Boolean): Boolean {
            val it: Iterator<Key?> = struct2.keyIterator()
            var key: Key
            while (it.hasNext()) {
                key = KeyImpl.toKey(it.next())
                if (overwrite || struct1.get(key, null) == null) struct1.setEL(key, struct2.get(key, null))
            }
            return true
        }
    }
}