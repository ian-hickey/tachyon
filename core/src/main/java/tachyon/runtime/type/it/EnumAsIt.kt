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

class EnumAsIt(e: Enumeration?) : Iterator {
    private val e: Enumeration?

    @Override
    operator fun hasNext(): Boolean {
        return e.hasMoreElements()
    }

    @Override
    operator fun next(): Object? {
        return e.nextElement()
    }

    @Override
    fun remove() {
        throw UnsupportedOperationException("this operation is not suppored")
    }

    init {
        this.e = e
    }
}