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
package lucee.runtime.op

import java.io.Serializable

/**
 * Interface to define an Object as Castable, for Lucee Type Casts
 */
interface Castable : Serializable {
    /**
     * cast the castable value to a string, other than the Method toString, this Method can throw a
     * Exception
     *
     * @return String representation of the Object
     * @throws PageException thrown when fail to convert to a string
     */
    @Throws(PageException::class)
    fun castToString(): String?

    /**
     * cast the castable value to a string, return the default value, when the method is not castable
     *
     * @param defaultValue default value returned in case not able to convert to a string
     * @return String representation of the Object
     */
    fun castToString(defaultValue: String?): String?

    /**
     * cast the castable value to a boolean value
     *
     * @return boolean Value representation of the Object
     * @throws PageException thrown when fail to convert to a boolean
     */
    @Throws(PageException::class)
    fun castToBooleanValue(): Boolean

    /**
     * cast the castable value to a boolean value
     *
     * @param defaultValue default value returned in case not able to convert to a boolean
     * @return boolean Value representation of the Object
     */
    fun castToBoolean(defaultValue: Boolean?): Boolean?

    /**
     * cast the castable value to a double value
     *
     * @return double Value representation of the Object
     * @throws PageException thrown when fail to convert to a double value
     */
    @Throws(PageException::class)
    fun castToDoubleValue(): Double

    /**
     * cast the castable value to a double value
     *
     * @param defaultValue default value returned in case not able to convert to a date object
     * @return double Value representation of the Object
     */
    fun castToDoubleValue(defaultValue: Double): Double

    /**
     * cast the castable value to a date time object
     *
     * @return date time representation of the Object
     * @throws PageException thrown when fails to convert to a date object
     */
    @Throws(PageException::class)
    fun castToDateTime(): DateTime?

    /**
     * cast the castable value to a date time object
     *
     * @param defaultValue returned when it is not possible to cast to a dateTime object
     * @return date time representation of the Object
     */
    fun castToDateTime(defaultValue: DateTime?): DateTime?

    @Throws(PageException::class)
    operator fun compareTo(str: String?): Int

    @Throws(PageException::class)
    operator fun compareTo(b: Boolean): Int

    @Throws(PageException::class)
    operator fun compareTo(d: Double): Int

    @Throws(PageException::class)
    operator fun compareTo(dt: DateTime?): Int
}