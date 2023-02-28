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

class ValueIterator(coll: Collection?, keys: Array<Collection.Key?>?) : Iterator<Object?>, Enumeration<Object?> {
    private val coll: Collection?
    protected var keys: Array<Key?>?
    protected var pos = 0

    @Override
    override fun hasNext(): Boolean {
        return keys!!.size > pos
    }

    @Override
    override fun next(): Object? {
        val key: Key = keys!![pos++] ?: return null
        return coll.get(key, null)
    }

    @Override
    fun hasMoreElements(): Boolean {
        return hasNext()
    }

    @Override
    fun nextElement(): Object? {
        return next()
    }

    @Override
    fun remove() {
        throw UnsupportedOperationException("this operation is not suppored")
    }

    init {
        this.coll = coll
        this.keys = keys
    }
}