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
package tachyon.runtime.type.comparator

import java.util.Comparator

class ArrayOfStructComparator(key: Collection.Key?) : Comparator<Struct?> {
    private val key: Key?
    @Override
    fun compare(s1: Struct?, s2: Struct?): Int {
        return compareObjects(s1.get(key, ""), s2.get(key, ""))
    }

    private fun compareObjects(oLeft: Object?, oRight: Object?): Int {
        return Caster.toString(oLeft, "").compareToIgnoreCase(Caster.toString(oRight, ""))
        // return Caster.toString(oLeft).compareTo(Caster.toString(oRight));
    }

    /**
     * Constructor of the class
     *
     * @param key key used from struct
     */
    init {
        this.key = key
    }
}