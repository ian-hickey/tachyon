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
package tachyon.runtime.util

import java.util.Enumeration

/**
 * class to make an enumaration from a ser, map or iterator
 */
class EnumerationWrapper<E>(it: Iterator<E?>?) : Enumeration<E?> {
    private val it: Iterator<E?>? = null

    /**
     * @param map Constructor with a Map
     */
    constructor(map: Map<E?, *>?) : this(map.keySet().iterator()) {}

    /**
     * @param set Constructor with a Set
     */
    constructor(set: Set<E?>?) : this(set!!.iterator()) {}

    /**
     * Constructor of the class
     *
     * @param objs
     */
    constructor(objs: Array<E?>?) : this(ArrayIterator(objs)) {}

    /**
     * @see java.util.Enumeration.hasMoreElements
     */
    @Override
    fun hasMoreElements(): Boolean {
        return it!!.hasNext()
    }

    /**
     * @see java.util.Enumeration.nextElement
     */
    @Override
    fun nextElement(): E? {
        return it!!.next()
    }

    /**
     * @param it Constructor with an iterator
     */
    init {
        this.it = it
    }
}