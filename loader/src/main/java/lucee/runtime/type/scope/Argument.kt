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
package lucee.runtime.type.scope

import java.util.Set

/**
 * interface for Argument scope
 */
interface Argument : Scope, Array, BindScope {
    /**
     * @return returns if scope is bound to another variable for using outside of a udf
     */
    /**
     * sets if scope is bound to another variable for using outside of a udf
     *
     * @param bind bind
     */
    @get:Override
    @set:Override
    abstract override var isBind: Boolean

    /**
     * insert a key in argument scope at defined position
     *
     * @param index index
     * @param key key
     * @param value value
     * @return boolean
     * @throws PageException Page Exception
     */
    @Throws(PageException::class)
    fun insert(index: Int, key: String?, value: Object?): Boolean

    @Throws(PageException::class)
    fun setArgument(obj: Object?): Object?
    fun getFunctionArgument(key: String?, defaultValue: Object?): Object?
    fun getFunctionArgument(key: Collection.Key?, defaultValue: Object?): Object?
    fun setFunctionArgumentNames(functionArgumentNames: Set<Collection.Key?>?)
    fun containsFunctionArgumentKey(key: Key?): Boolean

    companion object {
        val NULL: Object? = null
    }
}