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
package tachyon.runtime.type.ref

import tachyon.runtime.PageContext

/**
 *
 */
interface Reference {
    /**
     * @return returns the value of the Variable
     * @throws PageException Page Exception
     */
    @get:Throws(PageException::class)
    @get:Deprecated("use instead <code>{@link #getKey()}</code>")
    @get:Deprecated
    val keyAsString: String?

    /**
     * @return returns the value of the Variable
     * @throws PageException Page Exception
     */
    @get:Throws(PageException::class)
    val key: Collection.Key?

    /**
     * @param pc PageContext of the current Request
     * @return returns the value of the Variable
     * @throws PageException Page Exception
     */
    @Throws(PageException::class)
    operator fun get(pc: PageContext?): Object?

    /**
     * @param pc PageContext of the current Request
     * @param defaultValue default value
     * @return returns the value of the Variable
     */
    operator fun get(pc: PageContext?, defaultValue: Object?): Object?

    /**
     * @param pc PageContext of the current Request
     * @param value resets the value of the variable
     * @return new Value set
     * @throws PageException Page Exception
     */
    @Throws(PageException::class)
    operator fun set(pc: PageContext?, value: Object?): Object?

    /**
     * @param pc PageContext of the current Request
     * @param value resets the value of the variable
     * @return new value set
     */
    fun setEL(pc: PageContext?, value: Object?): Object?

    /**
     * clears the variable from collection
     *
     * @param pc Page Context
     * @return removed Object
     * @throws PageException Page Exception
     */
    @Throws(PageException::class)
    fun remove(pc: PageContext?): Object?

    /**
     * clears the variable from collection
     *
     * @param pc Page Context
     * @return removed Object
     */
    fun removeEL(pc: PageContext?): Object?

    /**
     * create it when not exist
     *
     * @param pc Page Context
     * @return removed Object
     * @throws PageException Page Exception
     */
    @Throws(PageException::class)
    fun touch(pc: PageContext?): Object?

    /**
     * create it when not exist
     *
     * @param pc Page Context
     * @return removed Object
     */
    fun touchEL(pc: PageContext?): Object?

    /**
     * @return returns the collection
     */
    val parent: Object?
}