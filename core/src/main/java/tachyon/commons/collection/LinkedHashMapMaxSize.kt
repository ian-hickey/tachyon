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
package tachyon.commons.collection

import java.util.LinkedHashMap

class LinkedHashMapMaxSize<K, V> @JvmOverloads constructor(private val maxSize: Int, initCapacity: Int = Math.ceil(maxSize / 0.75f), accessOrder: Boolean = false) : LinkedHashMap<K, V>(initCapacity, 0.75f, accessOrder) {
    @Override
    protected fun removeEldestEntry(eldest: Map.Entry<K, V>?): Boolean {
        return size() > maxSize
    }
}