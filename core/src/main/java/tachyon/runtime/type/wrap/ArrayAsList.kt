/**
 * Copyright (c) 2014, the Railo Company Ltd.
 * Copyright (c) 2015, Tachyon Assosication Switzerland
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
package tachyon.runtime.type.wrap

import java.util.ArrayList

class ArrayAsList private constructor(array: Array?) : List {
    var array: Array?
    @Override
    fun add(o: Object?): Boolean {
        try {
            array.append(o)
        } catch (e: PageException) {
            return false
        }
        return true
    }

    @Override
    fun add(index: Int, element: Object?) {
        try {
            array.insert(index + 1, element)
        } catch (e: PageException) {
            throw IndexOutOfBoundsException(e.getMessage())
        }
    }

    @Override
    fun addAll(c: Collection?): Boolean {
        val it: Iterator = c.iterator()
        while (it.hasNext()) {
            add(it.next())
        }
        return !c.isEmpty()
    }

    @Override
    fun addAll(index: Int, c: Collection?): Boolean {
        var index = index
        val it: Iterator = c.iterator()
        while (it.hasNext()) {
            add(index++, it.next())
        }
        return !c.isEmpty()
    }

    @Override
    fun clear() {
        array.clear()
    }

    @Override
    operator fun contains(o: Object?): Boolean {
        return indexOf(o) != -1
    }

    @Override
    fun containsAll(c: Collection?): Boolean {
        val it: Iterator = c.iterator()
        while (it.hasNext()) {
            if (!contains(it.next())) return false
        }
        return true
    }

    @Override
    operator fun get(index: Int): Object? {
        return try {
            array.getE(index + 1)
        } catch (e: PageException) {
            throw IndexOutOfBoundsException(e.getMessage())
        }
    }

    @Override
    fun indexOf(o: Object?): Int {
        val it: Iterator<Object?> = array.valueIterator()
        var index = 0
        while (it.hasNext()) {
            if (it.next().equals(o)) return index
            index++
        }
        return -1
    }

    @get:Override
    val isEmpty: Boolean
        get() = array.size() === 0

    @Override
    operator fun iterator(): Iterator? {
        return array.valueIterator()
    }

    @Override
    fun lastIndexOf(o: Object?): Int {
        val it: Iterator<Object?> = array.valueIterator()
        var index = 0
        var rtn = -1
        while (it.hasNext()) {
            if (it.next().equals(o)) rtn = index
            index++
        }
        return rtn
    }

    @Override
    fun listIterator(): ListIterator? {
        return listIterator(0)
    }

    @Override
    fun listIterator(index: Int): ListIterator? {
        return ArrayListIteratorImpl(array, index)
        // return array.toList().listIterator(index);
    }

    @Override
    fun remove(o: Object?): Boolean {
        val index = indexOf(o)
        if (index == -1) return false
        try {
            array.removeE(index + 1)
        } catch (e: PageException) {
            return false
        }
        return true
    }

    @Override
    fun remove(index: Int): Object? {
        return try {
            array.removeE(index + 1)
        } catch (e: PageException) {
            throw IndexOutOfBoundsException(e.getMessage())
        }
    }

    @Override
    fun removeAll(c: Collection?): Boolean {
        val it: Iterator = c.iterator()
        var rtn = false
        while (it.hasNext()) {
            if (remove(it.next())) rtn = true
        }
        return rtn
    }

    @Override
    fun retainAll(c: Collection?): Boolean {
        ArrayList().retainAll(c)
        var modified = false
        val it: Iterator? = iterator()
        while (it.hasNext()) {
            if (!c.contains(it.next())) {
                it.remove()
                modified = true
            }
        }
        return modified
    }

    @Override
    operator fun set(index: Int, element: Object?): Object? {
        return try {
            if (!array.containsKey(index + 1)) throw IndexOutOfBoundsException("Index: " + (index + 1) + ", Size: " + size())
            array.setE(index + 1, element)
        } catch (e: PageException) {
            throw PageRuntimeException(e)
        }
    }

    @Override
    fun size(): Int {
        return array.size()
    }

    @Override
    fun subList(fromIndex: Int, toIndex: Int): List? {
        return array.toList().subList(fromIndex, toIndex)
    }

    @Override
    fun toArray(): Array<Object?>? {
        return array.toArray()
    }

    @Override
    fun toArray(a: Array<Object?>?): Array<Object?>? {
        return array.toArray()
    }

    companion object {
        fun toList(array: Array?): List? {
            if (array is ListAsArray) return (array as ListAsArray?)!!.list
            return if (array is List) array else ArrayAsList(array)
        }
    }

    init {
        this.array = array
    }
}