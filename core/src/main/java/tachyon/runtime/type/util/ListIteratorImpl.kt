/**
 * Copyright (c) 2014, the Railo Company Ltd.
 * Copyright (c) 2016, Tachyon Assosication Switzerland
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
package tachyon.runtime.type.util

import java.util.List

class ListIteratorImpl<T>(private val list: List<T?>?, index: Int) : ListIterator<T?> {
    private var index = -1
    private var current = UNDEFINED

    /**
     * Constructor of the class
     *
     * @param arr
     */
    constructor(list: List<T?>?) : this(list, 0) {}

    @Override
    fun add(o: T?) {
        list.add(++index, o)
    }

    @Override
    fun remove() {
        if (current == UNDEFINED) throw IllegalStateException()
        list.remove(current)
        current = UNDEFINED
    }

    @Override
    fun set(o: T?) {
        if (current == UNDEFINED) throw IllegalStateException()
        list.set(current, o)
    }

    /////////////
    @Override
    override fun hasNext(): Boolean {
        return list!!.size() > index + 1
    }

    @Override
    override fun hasPrevious(): Boolean {
        return index > -1
    }

    @Override
    override fun previousIndex(): Int {
        return index
    }

    @Override
    override fun nextIndex(): Int {
        return index + 1
    }

    @Override
    override fun previous(): T? {
        if (!hasPrevious()) throw NoSuchElementException()
        current = index
        return list!![index--]
    }

    @Override
    override fun next(): T? {
        if (!hasNext()) throw NoSuchElementException()
        return list!![++index.also { current = it }]
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
        this.index = index - 1
    }
}