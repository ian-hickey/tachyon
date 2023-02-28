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
package tachyon.runtime.type

import java.util.Iterator
/**
 * interface that define that in a class an iterator is available
 */
interface Iteratorable {
    /**
     * @return return an Iterator for Keys as Collection.Keys
     */
    fun keyIterator(): Iterator<Collection.Key?>?

    /**
     * @return return an Iterator for Keys as String
     */
    fun keysAsStringIterator(): Iterator<String?>?

    /**
     *
     * @return return an Iterator for Values
     */
    fun valueIterator(): Iterator<Object?>?
    fun entryIterator(): Iterator<Entry<Key?, Object?>?>?
}