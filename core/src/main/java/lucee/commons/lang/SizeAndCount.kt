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
package lucee.commons.lang

import java.io.ByteArrayOutputStream

object SizeAndCount {
    private const val OBJECT_GRANULARITY_IN_BYTES = 8
    private val WORD_SIZE = Arch.vMArchitecture.wordSize
    private val HEADER_SIZE = 2 * WORD_SIZE
    private const val DOUBLE_SIZE = 8
    private const val FLOAT_SIZE = 4
    private const val LONG_SIZE = 8
    private const val INT_SIZE = 4
    private const val SHORT_SIZE = 2
    private const val BYTE_SIZE = 1
    private const val BOOLEAN_SIZE = 1
    private const val CHAR_SIZE = 2
    private val REF_SIZE = WORD_SIZE
    @Throws(PageException::class)
    fun sizeOf(obj: Object?): Size {
        val creator: Creation = CFMLEngineFactory.getInstance().getCreationUtil()
        val size = Size(0, 0)
        sizeOf(creator, size, obj, HashSet<Object>())
        return size
    }

    @Throws(PageException::class)
    private fun sizeOf(creator: Creation, size: Size, obj: Object?, parents: Set<Object>) {
        if (obj == null) return
        var raw: Object = obj

        // TODO this is just a patch solution, find a better way to handle this kind of situation (Wrapper
        // classes)
        if (isInstaneOf(obj.getClass(), "lucee.runtime.text.xml.struct.XMLStruct")) {
            try {
                val toNode: Method = raw.getClass().getMethod("toNode", arrayOfNulls<Class>(0))
                raw = toNode.invoke(obj, arrayOfNulls<Object>(0))
            } catch (e: Exception) {
                LogUtil.log("lang", e)
            }
        }
        if (parents.contains(raw)) return
        parents.add(raw)
        try {
            if (obj is Collection) {
                if (obj is Query) sizeOf(creator, size, obj as Query?, parents) else sizeOf(creator, size, (obj as Collection).valueIterator(), parents)
                return
            } else if (obj is Map) {
                sizeOf(creator, size, (obj as Map).values().iterator(), parents)
                return
            } else if (obj is List) {
                sizeOf(creator, size, (obj as List).iterator(), parents)
                return
            } else if (obj is String) {
                size.size += CHAR_SIZE * (obj as String).length() + REF_SIZE
            } else if (obj is Number) {
                if (obj is Double) size.size += DOUBLE_SIZE + REF_SIZE else if (obj is Float) size.size += FLOAT_SIZE + REF_SIZE else if (obj is Long) size.size += LONG_SIZE + REF_SIZE else if (obj is Integer) size.size += INT_SIZE + REF_SIZE else if (obj is Short) size.size += SHORT_SIZE + REF_SIZE else if (obj is Byte) size.size += BYTE_SIZE + REF_SIZE
            } else if (obj is Boolean) size.size += REF_SIZE + BOOLEAN_SIZE else if (obj is Character) size.size += REF_SIZE + CHAR_SIZE else size.size += _sizeOf(obj)
            size.count++
        } finally {
            // parents.remove(raw);// TODO should we not remove, to see if sister is me.
        }
    }

    @Throws(PageException::class)
    private fun sizeOf(creator: Creation, size: Size, it: Iterator, parents: Set<Object>) {
        size.count++
        size.size += REF_SIZE
        while (it.hasNext()) {
            sizeOf(creator, size, it.next(), parents)
        }
    }

    @Throws(PageException::class)
    private fun sizeOf(creator: Creation, size: Size, qry: Query, parents: Set<Object>) {
        size.count++
        size.size += REF_SIZE
        val rows: Int = qry.getRecordcount()
        val strColumns: Array<String> = qry.getColumns()
        val columns: Array<Collection.Key?> = arrayOfNulls<Collection.Key>(strColumns.size)
        for (col in columns.indices) {
            columns[col] = creator.createKey(strColumns[col])
        }
        for (row in 1..rows) {
            for (col in columns.indices) {
                sizeOf(creator, size, qry.getAt(columns[col], row), parents)
            }
        }
    }

    fun isInstaneOf(src: Class?, className: String): Boolean {
        if (src == null) return false
        if (className.equals(src.getName())) return true

        // interfaces
        val interfaces: Array<Class> = src.getInterfaces()
        for (i in interfaces.indices) {
            if (isInstaneOf(interfaces[i], className)) return true
        }
        return isInstaneOf(src.getSuperclass(), className)
    }

    fun _sizeOf(o: Object?): Int {
        val os = ByteArrayOutputStream()
        var oos: ObjectOutputStream? = null
        try {
            oos = ObjectOutputStream(os)
            oos.writeObject(o)
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
        } finally {
            IOUtil.closeEL(oos)
        }
        return os.toByteArray().length
    }

    class Size(var count: Int, var size: Int)
}

internal class Arch private constructor(val bits: Int, val wordSize: Int) {

    companion object {
        private val ARCH_32_BITS = Arch(32, 4)
        private val ARCH_64_BITS = Arch(64, 8)
        private val ARCH_UNKNOWN = Arch(32, 4)
        val vMArchitecture: Arch
            get() {
                val archString: String = System.getProperty("sun.arch.data.model")
                if (archString != null) {
                    if (archString.equals("32")) {
                        return ARCH_32_BITS
                    } else if (archString.equals("64")) {
                        return ARCH_64_BITS
                    }
                }
                return ARCH_UNKNOWN
            }
    }
}