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
package tachyon.runtime.op

import java.io.Serializable

/**
 *
 *
 * To change the template for this generated type comment go to Window - Preferences - Java - Code
 * Generation - Code and Comments
 */
object Duplicator {
    /**
     * primitive value duplication (do nothing, value type must not be duplicated)
     *
     * @param _boolean boolean value to duplicate
     * @return duplicated value
     */
    fun duplicate(_boolean: Boolean): Boolean {
        return _boolean
    }

    /**
     * primitive value duplication (do nothing, value type must not be duplicated)
     *
     * @param _byte byte value to duplicate
     * @return duplicated value
     */
    fun duplicate(_byte: Byte): Byte {
        return _byte
    }

    /**
     * primitive value duplication (do nothing, value type must not be duplicated)
     *
     * @param _short byte value to duplicate
     * @return duplicated value
     */
    fun duplicate(_short: Short): Short {
        return _short
    }

    /**
     * primitive value duplication (do nothing, value type must not be duplicated)
     *
     * @param _int byte value to duplicate
     * @return duplicated value
     */
    fun duplicate(_int: Int): Int {
        return _int
    }

    /**
     * primitive value duplication (do nothing, value type must not be duplicated)
     *
     * @param _long byte value to duplicate
     * @return duplicated value
     */
    fun duplicate(_long: Long): Long {
        return _long
    }

    /**
     * primitive value duplication (do nothing, value type must not be duplicated)
     *
     * @param _double byte value to duplicate
     * @return duplicated value
     */
    fun duplicate(_double: Double): Double {
        return _double
    }

    /**
     * reference type value duplication
     *
     * @param object object to duplicate
     * @return duplicated value
     */
    fun duplicate(`object`: Object?, deepCopy: Boolean): Object? {
        if (`object` == null) return null
        if (`object` is Number) return `object`
        if (`object` is String) return `object`
        if (`object` is Date) return (`object` as Date?).clone()
        if (`object` is Boolean) return `object`
        val before: RefBoolean = RefBooleanImpl()
        try {
            val copy: Object = ThreadLocalDuplication.get(`object`, before)
            if (copy != null) {
                return copy
            }
            if (`object` is Collection) return (`object` as Collection?).duplicate(deepCopy)
            if (`object` is Duplicable) return (`object` as Duplicable?).duplicate(deepCopy)
            if (`object` is UDF) return (`object` as UDF?).duplicate()
            if (`object` is List) return duplicateList(`object` as List?, deepCopy)
            if (`object` is Map) return duplicateMap(`object` as Map?, deepCopy)
            if (`object` is Serializable) {
                try {
                    val ser: String = JavaConverter.serialize(`object` as Serializable?)
                    return JavaConverter.deserialize(ser)
                } catch (t: Throwable) {
                    ExceptionUtil.rethrowIfNecessary(t)
                }
            }
        } finally {
            if (!before.toBooleanValue()) ThreadLocalDuplication.reset()
        }
        return `object`
    }

    fun duplicateList(list: List?, deepCopy: Boolean): List? {
        var newList: List?
        try {
            newList = ClassUtil.loadInstance(list.getClass())
        } catch (e: ClassException) {
            newList = ArrayList()
        }
        return duplicateList(list, newList, deepCopy)
    }

    fun duplicateList(list: List?, newList: List?, deepCopy: Boolean): List? {
        val it: ListIterator = list.listIterator()
        while (it.hasNext()) {
            if (deepCopy) newList.add(duplicate(it.next(), deepCopy)) else newList.add(it.next())
        }
        return newList
    }

    /**
     * duplicate a map
     *
     * @param map
     * @param doKeysLower
     * @return duplicated Map
     * @throws PageException
     */
    @Throws(PageException::class)
    fun duplicateMap(map: Map?, doKeysLower: Boolean, deepCopy: Boolean): Map? {
        if (doKeysLower) {
            var newMap: Map?
            try {
                newMap = ClassUtil.loadInstance(map.getClass())
            } catch (e: ClassException) {
                newMap = HashMap()
            }
            val inside: Boolean = ThreadLocalDuplication.set(map, newMap)
            try {
                val it: Iterator = map.keySet().iterator()
                while (it.hasNext()) {
                    val key: Object = it.next()
                    if (deepCopy) newMap.put(StringUtil.toLowerCase(Caster.toString(key)), duplicate(map.get(key), deepCopy)) else newMap.put(StringUtil.toLowerCase(Caster.toString(key)), map.get(key))
                }
            } finally {
                if (!inside) ThreadLocalDuplication.reset()
            }
            //
            return newMap
        }
        return duplicateMap(map, deepCopy)
    }

    fun duplicateMap(map: Map?, deepCopy: Boolean): Map? {
        var other: Map?
        try {
            other = ClassUtil.loadInstance(map.getClass())
        } catch (e: ClassException) {
            other = HashMap()
        }
        val inside: Boolean = ThreadLocalDuplication.set(map, other)
        try {
            duplicateMap(map, other, deepCopy)
        } finally {
            if (!inside) ThreadLocalDuplication.reset()
        }
        return other
    }

    fun duplicateMap(map: Map?, newMap: Map?, deepCopy: Boolean): Map? {
        val it: Iterator = map.keySet().iterator()
        while (it.hasNext()) {
            val key: Object = it.next()
            if (deepCopy) newMap.put(key, duplicate(map.get(key), deepCopy)) else newMap.put(key, map.get(key))
        }
        return newMap
    }
}