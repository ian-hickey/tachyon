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
package tachyon.runtime.converter

import java.io.IOException

class LazyConverter : ConverterSupport() {
    @Override
    @Throws(ConverterException::class, IOException::class)
    fun writeOut(pc: PageContext?, source: Object?, writer: Writer?) {
        writer.write(serialize(source))
        writer.flush()
    }

    companion object {
        fun serialize(o: Object?): String? {
            return serialize(o, HashSet<Object?>())
        }

        private fun serialize(o: Object?, done: Set<Object?>?): String? {
            if (o == null) return "null"
            val raw: Object? = toRaw(o)
            if (done!!.contains(raw)) return "parent reference"
            done.add(raw)
            return try {
                if (o is Array) return serializeArray(o as Array?, done)
                if (o is Struct) {
                    return serializeStruct(o as Struct?, done)
                }
                if (o is SimpleValue || o is Number || o is Boolean) Caster.toString(o, null) else o.toString()
            } finally {
                done.remove(raw)
            }
        }

        fun toRaw(o: Object?): Object? {
            return if (o is XMLStruct) (o as XMLStruct?).toNode() else o
        }

        private fun serializeStruct(struct: Struct?, done: Set<Object?>?): String? {
            val sb = StringBuilder("{")
            val it: Iterator<Key?> = struct.keyIterator()
            var key: Key?
            var notFirst = false
            while (it.hasNext()) {
                if (notFirst) sb.append(", ")
                key = it.next()
                sb.append(key)
                sb.append("={")
                sb.append(serialize(struct.get(key, null), done))
                sb.append("}")
                notFirst = true
            }
            return sb.append("}").toString()
        }

        private fun serializeArray(array: Array?, done: Set<Object?>?): String? {
            val sb = StringBuilder("[")
            val len: Int = array.size()
            for (i in 1..len) {
                if (i > 1) sb.append(", ")
                sb.append(serialize(array.get(i, null), done))
            }
            return sb.append("]").toString()
        }
    }
}