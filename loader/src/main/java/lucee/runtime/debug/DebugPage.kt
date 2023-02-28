/**
 * Copyright (c) 2014, the Railo Company Ltd.
 * Copyright (c) 2015, Lucee Assosication Switzerland
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

import lucee.commons.io.res.Resource

/**
 * debug page
 */
interface DebugPage {
    /**
     * sets the execution time of the page
     *
     * @param t execution time of the page
     */
    fun set(t: Long)

    /**
     * return the minimum execution time of the page
     *
     * @return minimum execution time
     */
    val minimalExecutionTime: Int

    /**
     * return the maximum execution time of the page
     *
     * @return maximum execution time
     */
    val maximalExecutionTime: Int

    /**
     * return the average execution time of the page
     *
     * @return average execution time
     */
    val averageExecutionTime: Int

    /**
     * return count of call the page
     *
     * @return average execution time
     */
    val count: Int

    /**
     * return file represetati9on of the debug page
     *
     * @return file object
     */
    val file: lucee.commons.io.res.Resource?
}