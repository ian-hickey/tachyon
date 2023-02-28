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
package tachyon.runtime.type.scope

import java.io.Serializable

interface ClusterEntry : Serializable {
    /**
     * @return the key
     */
    /**
     * @param key the key to set
     */
    var key: Collection.Key?

    /**
     * @return the time as Long reference
     */
    val timeRef: Long?
    /**
     * @return the time
     */
    /**
     * @param time the time to set
     */
    var time: Long
    /**
     * @return the value
     */
    /**
     * @param value the value to set
     */
    var value: Serializable?
}