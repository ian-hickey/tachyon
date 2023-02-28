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

import java.util.Iterator

class ObjectsEntryIterator : Iterator<Entry<Key?, Object?>?> {
    private var keys: Iterator<Key?>?
    private var objs: Objects?

    constructor(keys: Array<Key?>?, objs: Objects?) {
        this.keys = KeyIterator(keys)
        this.objs = objs
    }

    constructor(keys: Iterator<Key?>?, objs: Objects?) {
        this.keys = keys
        this.objs = objs
    }

    @Override
    override fun hasNext(): Boolean {
        return keys!!.hasNext()
    }

    @Override
    override fun next(): Entry<Key?, Object?>? {
        val key: Key = KeyImpl.toKey(keys!!.next(), null)
        return EntryImpl(objs, key)
    }

    @Override
    fun remove() {
        throw UnsupportedOperationException("this operation is not suppored")
    }

    inner class EntryImpl(objcts: Objects?, key: Key?) : Entry<Key?, Object?> {
        protected var key: Key?
        private val objcts: Objects?
        @Override
        fun getKey(): Key? {
            return key
        }

        @get:Override
        val value: Object?
            get() = objcts.get(ThreadLocalPageContext.get(), key, null)

        @Override
        fun setValue(value: Object?): Object? {
            return objcts.setEL(ThreadLocalPageContext.get(), key, value)
        }

        init {
            this.key = key
            this.objcts = objcts
        }
    }
}