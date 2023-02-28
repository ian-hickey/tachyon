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
package tachyon.runtime.java

import java.util.Calendar

/**
 * creates a Java Proxy for components, so you can use componets as java classes following a certain
 * interface or class
 */
object JavaProxy {
    fun call(config: ConfigWeb?, cfc: Component?, methodName: String?, vararg arguments: Object?): Object? {
        var unregister = false
        var pc: PageContext? = null
        return try {
            pc = ThreadLocalPageContext.get()
            // create PageContext if necessary
            if (pc == null) {
                pc = ThreadUtil.createDummyPageContext(config)
                unregister = true
                pc.addPageSource(cfc.getPageSource(), true)
            }
            cfc.call(pc, methodName, arguments)
        } catch (pe: PageException) {
            throw PageRuntimeException(pe)
        } finally {
            if (unregister) config.getFactory().releaseTachyonPageContext(pc, true)
        }
    }

    fun call(config: ConfigWeb?, udf: UDF?, methodName: String?, arguments: Array<Object?>?): Object? {
        var unregister = false
        var pc: PageContext? = null
        return try {
            pc = ThreadLocalPageContext.get()
            // create PageContext if necessary
            if (pc == null) {
                pc = ThreadUtil.createDummyPageContext(config)
                unregister = true
                // pc.addPageSource(udf.getPageSource(), true);
            }
            udf.call(pc, KeyImpl.init(methodName), arguments, true)
        } catch (pe: PageException) {
            throw PageRuntimeException(pe)
        } finally {
            if (unregister) config.getFactory().releaseTachyonPageContext(pc, true)
        }
    }

    fun toBoolean(obj: Object?): Boolean {
        return try {
            Caster.toBooleanValue(obj)
        } catch (pe: PageException) {
            throw PageRuntimeException(pe)
        }
    }

    fun toFloat(obj: Object?): Float {
        return try {
            Caster.toFloatValue(obj)
        } catch (pe: PageException) {
            throw PageRuntimeException(pe)
        }
    }

    fun toInt(obj: Object?): Int {
        return try {
            Caster.toIntValue(obj)
        } catch (pe: PageException) {
            throw PageRuntimeException(pe)
        }
    }

    fun toDouble(obj: Object?): Double {
        return try {
            Caster.toDoubleValue(obj)
        } catch (pe: PageException) {
            throw PageRuntimeException(pe)
        }
    }

    fun toLong(obj: Object?): Long {
        return try {
            Caster.toLongValue(obj)
        } catch (pe: PageException) {
            throw PageRuntimeException(pe)
        }
    }

    fun toChar(obj: Object?): Char {
        return try {
            Caster.toCharValue(obj)
        } catch (pe: PageException) {
            throw PageRuntimeException(pe)
        }
    }

    fun toByte(obj: Object?): Byte {
        return try {
            Caster.toByteValue(obj)
        } catch (pe: PageException) {
            throw PageRuntimeException(pe)
        }
    }

    fun toShort(obj: Object?): Short {
        return try {
            Caster.toShortValue(obj)
        } catch (pe: PageException) {
            throw PageRuntimeException(pe)
        }
    }

    fun toString(obj: Object?): String? {
        return try {
            Caster.toString(obj)
        } catch (pe: PageException) {
            throw PageRuntimeException(pe)
        }
    }

    fun to(obj: Object?, clazz: Class?): Object? {
        return try {
            Caster.castTo(ThreadLocalPageContext.get(), clazz, obj)
        } catch (pe: PageException) {
            throw PageRuntimeException(pe)
        }
    }

    fun to(obj: Object?, className: String?): Object? {
        return try {
            Caster.castTo(ThreadLocalPageContext.get(), className, obj, false)
        } catch (pe: PageException) {
            throw PageRuntimeException(pe)
        }
    }

    fun toCFML(value: Boolean): Object? {
        return if (value) Boolean.TRUE else Boolean.FALSE
    }

    fun toCFML(value: Byte): Object? {
        return Caster.toDouble(value)
    }

    fun toCFML(value: Char): Object? {
        return String(charArrayOf(value))
    }

    fun toCFML(value: Double): Object? {
        return Caster.toDouble(value)
    }

    fun toCFML(value: Float): Object? {
        return Caster.toDouble(value)
    }

    fun toCFML(value: Int): Object? {
        return Caster.toDouble(value)
    }

    fun toCFML(value: Long): Object? {
        return Caster.toDouble(value)
    }

    fun toCFML(value: Short): Object? {
        return Caster.toDouble(value)
    }

    fun toCFML(value: Object?): Object? {
        return try {
            _toCFML(value)
        } catch (e: PageException) {
            value
        }
    }

    @Throws(PageException::class)
    fun _toCFML(value: Object?): Object? {
        if (value is Date || value is Calendar) { // do not change to caster.isDate
            return Caster.toDate(value, null)
        }
        if (value is Array<Object>) {
            val arr: Array<Object?>? = value
            if (!ArrayUtil.isEmpty(arr)) {
                var allTheSame = true
                // byte
                if (arr!![0] is Byte) {
                    for (i in 1 until arr.size) {
                        if (arr[i] !is Byte) {
                            allTheSame = false
                            break
                        }
                    }
                    if (allTheSame) {
                        val bytes = ByteArray(arr.size)
                        for (i in arr.indices) {
                            bytes[i] = Caster.toByteValue(arr[i])
                        }
                        return bytes
                    }
                }
            }
        }
        if (value is Array<Byte>) {
            val arr = value as Array<Byte?>?
            if (!ArrayUtil.isEmpty(arr)) {
                val bytes = ByteArray(arr!!.size)
                for (i in arr.indices) {
                    bytes[i] = arr[i].byteValue()
                }
                return bytes
            }
        }
        if (value is ByteArray) {
            return value
        }
        if (value !is Collection) {
            if (Decision.isArray(value)) {
                val a: Array = Caster.toArray(value)
                val len: Int = a.size()
                var o: Object
                for (i in 1..len) {
                    o = a.get(i, null)
                    if (o != null) a.setEL(i, toCFML(o))
                }
                return a
            }
            if (value is Map) {
                val sct: Struct = StructImpl()
                val it: Iterator = (value as Map?).entrySet().iterator()
                var entry: Map.Entry
                while (it.hasNext()) {
                    entry = it.next() as Entry
                    sct.setEL(Caster.toString(entry.getKey()), toCFML(entry.getValue()))
                }
                return sct

                // return StructUtil.copyToStruct((Map)value);
            }
            if (Decision.isQuery(value)) {
                val q: Query = Caster.toQuery(value)
                val recorcount: Int = q.getRecordcount()
                val strColumns: Array<String?> = q.getColumns()
                var col: QueryColumn
                var row: Int
                for (i in strColumns.indices) {
                    col = q.getColumn(strColumns[i])
                    row = 1
                    while (row <= recorcount) {
                        col.set(row, toCFML(col.get(row, null)))
                        row++
                    }
                }
                return q
            }
        }
        return value
    }
}