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
package tachyon.runtime.util

import java.util.Iterator

/**
 * Iterator Implementation for an Object Array
 */
class ArrayIterator : Iterator {
    private var arr: Array<Object?>?
    private var offset: Int
    private var length: Int

    /**
     * constructor for the class
     *
     * @param arr Base Array
     */
    constructor(arr: Array<Object?>?) {
        this.arr = arr
        offset = 0
        length = arr!!.size
    }

    constructor(arr: Array<Object?>?, offset: Int, length: Int) {
        this.arr = arr
        this.offset = offset
        this.length = offset + length
        if (this.length > arr!!.size) this.length = arr.size
    }

    constructor(arr: Array<Object?>?, offset: Int) {
        this.arr = arr
        this.offset = offset
        length = arr!!.size
    }

    @Override
    fun remove() {
        throw UnsupportedOperationException("this operation is not suppored")
    }

    @Override
    operator fun hasNext(): Boolean {
        return length > offset
    }

    @Override
    operator fun next(): Object? {
        return arr!![offset++]
    }
}