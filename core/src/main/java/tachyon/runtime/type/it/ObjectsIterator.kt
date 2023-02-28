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

import java.util.Iterator

class ObjectsIterator : Iterator<Object?> {
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
    override fun next(): Object? {
        return objs.get(ThreadLocalPageContext.get(), KeyImpl.toKey(keys!!.next(), null), null)
    }

    @Override
    fun remove() {
        throw UnsupportedOperationException("this operation is not suppored")
    }
}