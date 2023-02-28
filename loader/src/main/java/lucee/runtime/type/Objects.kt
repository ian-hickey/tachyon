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
package lucee.runtime.type

import lucee.runtime.PageContext

/**
 * Hold a native or wild object, to use id inside lucee runtime
 */
interface Objects : Dumpable, Castable {
    /**
     * return property
     *
     * @param pc PageContext
     * @param key Name of the Property
     * @param defaultValue Default Value
     * @return return value of the Property
     */
    operator fun get(pc: PageContext?, key: Collection.Key?, defaultValue: Object?): Object?

    /**
     * return property or getter of the ContextCollection
     *
     * @param pc PageContext
     * @param key Name of the Property
     * @return return value of the Property
     * @throws PageException Page Exception
     */
    @Throws(PageException::class)
    operator fun get(pc: PageContext?, key: Collection.Key?): Object?

    /**
     * sets a property (Data Member) value of the object
     *
     * @param pc page context
     * @param propertyName property name to set
     * @param value value to insert
     * @return value set to property
     * @throws PageException Page Exception
     */
    @Throws(PageException::class)
    operator fun set(pc: PageContext?, propertyName: Collection.Key?, value: Object?): Object?

    /**
     * sets a property (Data Member) value of the object
     *
     * @param pc page context
     * @param propertyName property name to set
     * @param value value to insert
     * @return value set to property
     */
    fun setEL(pc: PageContext?, propertyName: Collection.Key?, value: Object?): Object?

    /**
     * calls a method of the object
     *
     * @param pc page context
     * @param methodName name of the method to call
     * @param arguments arguments to call method with
     * @return return value of the method
     * @throws PageException Page Exception
     */
    @Throws(PageException::class)
    fun call(pc: PageContext?, methodName: Collection.Key?, arguments: Array<Object?>?): Object?

    /**
     * call a method of the Object with named arguments
     *
     * @param pc PageContext
     * @param methodName name of the method
     * @param args Named Arguments for the method
     * @return return result of the method
     * @throws PageException Page Exception
     */
    @Throws(PageException::class)
    fun callWithNamedValues(pc: PageContext?, methodName: Collection.Key?, args: Struct?): Object?
}