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
 * Implements the CFML Function structnew
 */
package lucee.runtime.functions.struct

import java.util.Collections

class StructNew : BIF() {
    @Override
    @Throws(PageException::class)
    operator fun invoke(pc: PageContext?, args: Array<Object?>?): Object? {
        if (args!!.size == 1) return call(pc, Caster.toString(args[0]))
        if (args.size == 0) return call(pc)
        throw FunctionException(pc, "StructNew", 0, 1, args.size)
    }

    companion object {
        private const val serialVersionUID = 2439168907287957648L
        fun call(pc: PageContext?): Struct? {
            return StructImpl()
        }

        @Throws(ApplicationException::class)
        fun call(pc: PageContext?, type: String?): Struct? {
            return call(pc, type, null)
        }

        @Throws(ApplicationException::class)
        fun call(pc: PageContext?, type: String?, onMissingKey: UDF?): Struct? {
            val t = toType(type)
            if (t == StructImpl.TYPE_LINKED_CASESENSITIVE || t == StructImpl.TYPE_CASESENSITIVE) {
                if (onMissingKey != null) throw ApplicationException("type [$type] is not supported in combination with onMissingKey listener")
                return MapAsStruct.toStruct(if (t == StructImpl.TYPE_LINKED_CASESENSITIVE) Collections.synchronizedMap(LinkedHashMap()) else ConcurrentHashMap(), true)
            }
            return if (onMissingKey != null) {
                StructListenerImpl(t, onMissingKey)
            } else StructImpl(t)
        }

        @Throws(ApplicationException::class)
        fun toType(type: String?): Int {
            var type = type
            type = type.toLowerCase()
            return if (type!!.equals("linked")) Struct.TYPE_LINKED else if (type.equals("ordered")) Struct.TYPE_LINKED else if (type.equals("weaked")) Struct.TYPE_WEAKED else if (type.equals("weak")) Struct.TYPE_WEAKED else if (type.equals("syncronized")) Struct.TYPE_SYNC else if (type.equals("synchronized")) Struct.TYPE_SYNC else if (type.equals("sync")) Struct.TYPE_SYNC else if (type.equals("soft")) Struct.TYPE_SOFT else if (type.equals("normal")) Struct.TYPE_REGULAR else if (type.equals("regular")) Struct.TYPE_REGULAR else if (type.equals("ordered-casesensitive")) StructImpl.TYPE_LINKED_CASESENSITIVE else if (type.equals("casesensitive")) StructImpl.TYPE_CASESENSITIVE else throw ApplicationException("valid struct types are [normal, weak, linked, soft, synchronized,ordered-casesensitive,casesensitive]")
        }
    }
}