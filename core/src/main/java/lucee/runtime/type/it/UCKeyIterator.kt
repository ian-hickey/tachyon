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

class UCKeyIterator(it: Iterator?) : Iterator {
    private val it: Iterator?

    @Override
    operator fun hasNext(): Boolean {
        return it.hasNext()
    }

    @Override
    operator fun next(): Object? {
        return nextString()
    }

    fun nextString(): String? {
        return StringUtil.toStringNative(it.next(), "").toUpperCase()
    }

    @Override
    fun remove() {
        it.remove()
    }

    init {
        this.it = it
    }
}