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
package tachyon.runtime.type

import java.io.Serializable

/**
 * CFML array object
 */
class ArrayInt : Serializable {
    private var arr: IntArray?
    private val cap = 32
    private var size = 0
    private var offset = 0
    private var offCount = 0

    /**
     * constructor with default dimesnion (1)
     */
    constructor() {
        arr = IntArray(offset + cap)
    }

    /**
     * constructor with to data to fill
     *
     * @param objects Objects array data to fill
     */
    constructor(objects: IntArray?) {
        arr = objects
        size = arr!!.size
        offset = 0
    }

    operator fun get(key: Int, defaultValue: Int): Int {
        if (key > size || key < 1) {
            return defaultValue
        }
        val o = arr!![offset + key - 1]
        return if (o == NULL) defaultValue else o
    }

    @Throws(ExpressionException::class)
    operator fun get(key: Int): Int {
        if (key < 1 || key > size) {
            throw invalidPosition(key)
        }
        val o = arr!![offset + key - 1]
        if (o == NULL) {
            throw invalidPosition(key)
        }
        return o
    }

    /**
     * Exception method if key doesn't exist at given position
     *
     * @param pos
     * @return exception
     */
    private fun invalidPosition(pos: Int): ExpressionException? {
        return ExpressionException("Element at position [$pos] doesn't exist in array")
    }

    operator fun set(key: Int, value: Int): Int {
        if (offset + key > arr!!.size) enlargeCapacity(key)
        if (key > size) size = key
        return value.also { arr!![offset + key - 1] = it }
    }

    /**
     * !!! all methods that use this method must be sync enlarge the inner array to given size
     *
     * @param key min size of the array
     */
    @Synchronized
    private fun enlargeCapacity(key: Int) {
        val diff = offCount - offset
        var newSize = arr!!.size
        if (newSize < 1) newSize = 1
        while (newSize < key + offset + diff) {
            newSize *= 2
        }
        if (newSize > arr!!.size) {
            val na = IntArray(newSize)
            for (i in offset until offset + size) {
                na[i + diff] = arr!![i]
            }
            arr = na
            offset += diff
        }
    }

    /*
	 * * !!! all methods that use this method must be sync enlarge the offset if 0 / private void
	 * enlargeOffset() { if(offset==0) { offCount=offCount==0?1:offCount*2; offset=offCount; int[]
	 * narr=new int[arr.length+offset]; for(int i=0;i<size;i++) { narr[offset+i]=arr[i]; } arr=narr; } }
	 */
    fun size(): Int {
        return size
    }

    fun keys(): IntArray? {
        val lst = ArrayList()
        var count = 0
        for (i in offset until offset + size) {
            val o = arr!![i]
            count++
            if (o != NULL) lst.add(Integer.valueOf(count))
        }
        val ints = IntArray(lst.size())
        for (i in ints.indices) {
            ints[i] = (lst.get(i) as Integer).intValue()
        }
        return ints
    }

    @Throws(ExpressionException::class)
    fun remove(key: Int): Int {
        if (key > size || key < 1) throw invalidPosition(key)
        val obj = get(key, NULL)
        for (i in offset + key - 1 until offset + size - 1) {
            arr!![i] = arr!![i + 1]
        }
        size--
        return obj
    }

    fun removeEL(key: Int): Int {
        if (key > size || key < 1) return NULL
        val obj = get(key, NULL)
        for (i in offset + key - 1 until offset + size - 1) {
            arr!![i] = arr!![i + 1]
        }
        size--
        return obj
    }

    fun clear() {
        if (size() > 0) {
            arr = IntArray(cap)
            size = 0
            offCount = 1
            offset = 0
        }
    }

    fun add(o: Int): Int {
        if (offset + size + 1 > arr!!.size) enlargeCapacity(size + 1)
        arr!![offset + size] = o
        size++
        return o
    }

    fun toArray(): IntArray? {
        val rtn = IntArray(size)
        var count = 0
        for (i in offset until offset + size) {
            rtn[count++] = arr!![i]
        }
        return rtn
    }

    operator fun contains(key: Int): Boolean {
        return get(key, NULL) != NULL
    }

    companion object {
        private const val NULL = 0
    }
}