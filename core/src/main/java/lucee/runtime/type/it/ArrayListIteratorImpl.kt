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
package lucee.runtime.type.it

import java.util.ListIterator

class ArrayListIteratorImpl(array: Array?, index: Int) : ListIterator {
    private val array: Array?
    private var index = -1
    private var current = UNDEFINED
    @Override
    fun add(o: Object?) {
        array.setEL(++index + 1, o)
    }

    @Override
    fun remove() {
        if (current == UNDEFINED) throw IllegalStateException()
        array.removeEL(current + 1)
        current = UNDEFINED
    }

    @Override
    fun set(o: Object?) {
        if (current == UNDEFINED) throw IllegalStateException()
        array.setEL(current + 1, o)
    }

    /////////////
    @Override
    operator fun hasNext(): Boolean {
        return array.size() > index + 1
    }

    @Override
    fun hasPrevious(): Boolean {
        return index > -1
    }

    @Override
    fun previousIndex(): Int {
        return index
    }

    @Override
    fun nextIndex(): Int {
        return index + 1
    }

    @Override
    fun previous(): Object? {
        if (!hasPrevious()) throw NoSuchElementException()
        current = index
        return array.get(index-- + 1, null)
    }

    @Override
    operator fun next(): Object? {
        if (!hasNext()) throw NoSuchElementException()
        return array.get(++index.also { current = it } + 1, null)
    }

    companion object {
        private val UNDEFINED: Int = Integer.MIN_VALUE
    }

    /**
     * Constructor of the class
     *
     * @param arr
     * @param index
     */
    init {
        this.array = array
        this.index = index - 1
    }
}