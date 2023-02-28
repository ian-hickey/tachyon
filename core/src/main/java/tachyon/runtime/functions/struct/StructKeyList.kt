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
 * Implements the CFML Function structkeylist
 */
package tachyon.runtime.functions.struct

import java.util.Iterator

class StructKeyList : BIF() {
    @Override
    @Throws(PageException::class)
    operator fun invoke(pc: PageContext?, args: Array<Object?>?): Object? {
        if (args!!.size == 2) return call(pc, Caster.toStruct(args[0]), Caster.toString(args[1]))
        if (args.size == 1) return call(pc, Caster.toStruct(args[0]))
        throw FunctionException(pc, "StructKeyList", 1, 2, args.size)
    }

    companion object {
        private const val serialVersionUID = 6256709521354910213L
        fun call(pc: PageContext?, struct: Struct?): String? {
            return call(pc, struct, ",") // KeyImpl.toUpperCaseList(struct.keys(), ",");
        }

        fun call(pc: PageContext?, struct: Struct?, delimiter: String?): String? {
            // return KeyImpl.toList(CollectionUtil.keys(struct), delimiter);
            if (struct == null) return ""
            val it: Iterator<Key?> = struct.keyIterator()

            // first
            if (!it.hasNext()) return ""
            val sb = StringBuilder()
            sb.append(it.next().getString())

            // rest
            if (delimiter!!.length() === 1) {
                val c: Char = delimiter.charAt(0)
                while (it.hasNext()) {
                    sb.append(c)
                    sb.append(it.next().getString())
                }
            } else {
                while (it.hasNext()) {
                    sb.append(delimiter)
                    sb.append(it.next().getString())
                }
            }
            return sb.toString()
        }
    }
}