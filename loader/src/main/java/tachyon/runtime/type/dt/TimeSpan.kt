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
package tachyon.runtime.type.dt

import java.io.Serializable

/**
 * defines a time span
 */
interface TimeSpan : Castable, Dumpable, Serializable {
    /**
     * @return returns the timespan in milliseconds
     */
    val millis: Long

    /**
     * @return returns the timespan in seconds
     */
    val seconds: Long

    /**
     * @return Returns the day value.
     */
    val day: Int

    /**
     * @return Returns the hour value.
     */
    val hour: Int

    /**
     * @return Returns the minute value.
     */
    val minute: Int

    /**
     * @return Returns the second value.
     */
    val second: Int
}