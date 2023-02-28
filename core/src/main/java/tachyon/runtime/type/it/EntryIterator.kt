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

class EntryIterator(coll: Collection?, keys: Array<Collection.Key?>?) : Iterator<Entry<Key?, Object?>?>, Enumeration<Entry<Key?, Object?>?> {
    private val coll: Collection?
    protected var keys: Array<Key?>?
    protected var pos = 0

    @Override
    override fun hasNext(): Boolean {
        return keys!!.size > pos
    }

    @Override
    override fun next(): Entry<Key?, Object?>? {
        val key: Key = keys!![pos++] ?: return null
        return EntryImpl(coll, key)
    }

    @Override
    fun hasMoreElements(): Boolean {
        return hasNext()
    }

    @Override
    fun nextElement(): Entry<Key?, Object?>? {
        return next()
    }

    @Override
    fun remove() {
        throw UnsupportedOperationException("this operation is not suppored")
    }

    inner class EntryImpl(coll: Collection?, key: Key?) : Entry<Key?, Object?> {
        private val coll: Collection?
        protected var key: Key?
        @Override
        fun getKey(): Key? {
            return key
        }

        @get:Override
        val value: Object?
            get() = coll.get(key, null)

        @Override
        fun setValue(value: Object?): Object? {
            return coll.setEL(key, value)
        }

        init {
            this.coll = coll
            this.key = key
        }
    }

    init {
        this.coll = coll
        this.keys = keys
    }
}