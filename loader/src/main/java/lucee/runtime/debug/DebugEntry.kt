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
package lucee.runtime.debug

import java.io.Serializable

/**
 * a single debug entry
 */
interface DebugEntry : Serializable {
    /**
     * @return Returns the exeTime.
     */
    val exeTime: Long

    /**
     * @param exeTime The exeTime to set.
     */
    fun updateExeTime(exeTime: Long)

    /**
     * @return Returns the src.
     */
    val src: String?

    /**
     * @return Returns the count.
     */
    val count: Int

    /**
     * @return Returns the max.
     */
    val max: Long

    /**
     * @return Returns the min.
     */
    val min: Long

    /**
     * @return the file path of this entry
     */
    val path: String?
    val id: String?
}