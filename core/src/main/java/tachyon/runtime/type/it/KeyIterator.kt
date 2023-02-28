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
package tachyon.runtime.type.it

import java.util.Enumeration

/**
 * Iterator Implementation for an Object Array
 */
class KeyIterator(arr: Array<Collection.Key?>?) : Iterator<Collection.Key?>, Enumeration<Collection.Key?> {
    private val arr: Array<Collection.Key?>?
    private var pos: Int
    @Override
    fun remove() {
        throw UnsupportedOperationException("this operation is not suppored")
    }

    @Override
    override fun hasNext(): Boolean {
        return arr!!.size > pos
    }

    @Override
    override fun next(): Collection.Key? {
        return arr!![pos++] ?: return null
    }

    @Override
    fun hasMoreElements(): Boolean {
        return hasNext()
    }

    @Override
    fun nextElement(): Collection.Key? {
        return next()
    }

    /**
     * constructor for the class
     *
     * @param arr Base Array
     */
    init {
        this.arr = arr ?: arrayOfNulls<Collection.Key?>(0)
        pos = 0
    }
}