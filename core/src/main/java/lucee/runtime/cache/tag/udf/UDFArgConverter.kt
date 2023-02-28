/**
 * Copyright (c) 2014, the Railo Company Ltd.
 * Copyright (c) 2015, Lucee Assosication Switzerland
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
package lucee.runtime.cache.tag.udf

import java.util.HashSet

object UDFArgConverter {
    /**
     *
     * @param o
     * @param max max elements converted
     * @return
     */
    fun serialize(o: Object?): String? {
        return serialize(o, HashSet<Object?>(), 100)
    }

    fun serialize(o: Object?, max: Int): String? {
        return serialize(o, HashSet<Object?>(), max)
    }

    private fun serialize(oo: Object?, done: Set<Object?>?, max: Int): String? {
        if (oo == null) return "null"
        val raw: Object? = toRaw(oo)
        if (done!!.size() >= max) return "max reached:" + raw.hashCode()
        if (done.contains(raw)) return "parent reference"
        done.add(raw)
        var c: Collection? = null
        var other: Object? = null
        return try {
            if (raw is Array<Object>) {
                return serializeArray(raw as Array<Object?>?, done, max)
            } else if (Caster.toCollection(raw, null).also { c = it } != null) {
                if (raw !== c) {
                    done.add(c)
                    other = c
                }
                return serializeCollection(c, done, max)
            }
            raw.toString()
        } finally {
            if (other != null) done.remove(other)
            done.remove(raw)
        }
    }

    private fun toRaw(o: Object?): Object? {
        return if (o is XMLStruct) (o as XMLStruct?).toNode() else o
    }

    private fun serializeArray(arr: Array<Object?>?, done: Set<Object?>?, max: Int): String? {
        val sb = StringBuilder("[")
        var notFirst = false
        for (o in arr!!) {
            if (notFirst) sb.append(",")
            sb.append(serialize(o, done, max))
            notFirst = true
        }
        return sb.append("]").toString()
    }

    private fun serializeCollection(coll: Collection?, done: Set<Object?>?, max: Int): String? {
        if (coll is Query) {
            val qry: Query? = coll as Query?
            val sb = StringBuilder(8192)
            val it: Iterator<Key?> = qry.keyIterator()
            var k: Key?
            sb.append("{")
            val len: Int = qry.getRecordcount()
            while (it.hasNext()) {
                k = it.next()
                sb.append(',')
                sb.append(k.getLowerString())
                sb.append('[')
                var doIt = false
                for (y in 1..len) {
                    if (doIt) sb.append(',')
                    doIt = true
                    try {
                        sb.append(serialize(qry.getAt(k, y), done, max))
                    } catch (e: PageException) {
                        sb.append(serialize(e.getMessage(), done, max))
                    }
                }
                sb.append(']')
            }
            sb.append('}')
            return sb.toString()
        }
        val sb = StringBuilder("{")
        val it: Iterator<Entry<Key?, Object?>?> = coll.entryIterator()
        var e: Entry<Key?, Object?>?
        var notFirst = false
        while (it.hasNext()) {
            if (notFirst) sb.append(",")
            e = it.next()
            sb.append(e.getKey().getLowerString())
            sb.append(":")
            sb.append(serialize(e.getValue(), done, max))
            notFirst = true
        }
        return sb.append("}").toString()
    }

    private fun escape(str: String?): String? {
        return StringUtil.replace(str, "'", "''", false)
    }
}