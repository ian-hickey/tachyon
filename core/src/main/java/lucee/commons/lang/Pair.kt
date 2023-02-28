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
package lucee.commons.lang

import java.io.Serializable

/**
 * a Simple name value Pair
 */
class Pair<K, V>
/**
 * Constructor of the class
 *
 * @param name
 * @param value
 */(
        /**
         * @param name the name to set
         */
        var name: K,
        /**
         * @param value the value to set
         */
        var value: V) : Serializable {
    /**
     * @return the name
     */
    /**
     * @return the value
     */
    @Override
    override fun toString(): String {
        return name.toString() + ":" + value
    }
}