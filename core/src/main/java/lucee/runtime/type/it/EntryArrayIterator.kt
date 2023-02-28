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
package lucee.runtime.type.it

import java.util.Enumeration

class EntryArrayIterator(coll: Array?, keys: IntArray?) : Iterator<Entry<Integer?, Object?>?>, Enumeration<Entry<Integer?, Object?>?> {
    private val coll: Array?
    protected var keys: IntArray?
    protected var pos = 0

    @Override
    override fun hasNext(): Boolean {
        return keys!!.size > pos
    }

    @Override
    override fun next(): Entry<Integer?, Object?>? {
        val key = keys!![pos++]
        return EntryImpl(coll, key)
    }

    @Override
    fun hasMoreElements(): Boolean {
        return hasNext()
    }

    @Override
    fun nextElement(): Entry<Integer?, Object?>? {
        return next()
    }

    @Override
    fun remove() {
        throw UnsupportedOperationException("this operation is not suppored")
    }

    inner class EntryImpl(coll: Array?, index: Integer?) : Entry<Integer?, Object?> {
        private val arr: Array?
        private val index: Integer?

        @get:Override
        val key: Integer?
            get() = index

        @get:Override
        val value: Object?
            get() = arr.get(index.intValue(), null)

        @Override
        fun setValue(value: Object?): Object? {
            return arr.setEL(index.intValue(), value)
        }

        init {
            arr = coll
            this.index = index
        }
    }

    init {
        this.coll = coll
        this.keys = keys
    }
}