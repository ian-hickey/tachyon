/**
 * Copyright (c) 2015, Lucee Assosication Switzerland. All rights reserved.
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
package lucee.runtime.component

import java.util.concurrent.ConcurrentHashMap

class StaticStruct : ConcurrentHashMap<Key?, Member?>() {
    private var index: Long = 0
    fun isInit(): Boolean {
        return index != 0L
    }

    fun index(): Long {
        return index
    }

    fun setInit(init: Boolean) {
        if (init) index = createIndex() else index = 0
    }

    companion object {
        private const val serialVersionUID = 4964717564860928637L
        private var counter: Long = 1
        @Synchronized
        fun createIndex(): Long {
            counter++
            if (counter < 0) counter = 1
            return counter
        }
    }
}