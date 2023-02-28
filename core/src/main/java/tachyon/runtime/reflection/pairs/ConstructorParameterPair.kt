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
package tachyon.runtime.reflection.pairs

import java.lang.reflect.Constructor

/**
 * Hold a pair of method and parameter to invoke
 */
class ConstructorParameterPair(constructor: Constructor?, parameters: Array<Object?>?) {
    private val constructor: Constructor?
    private val parameters: Array<Object?>?

    /**
     * returns the Constructor
     *
     * @return returns the Constructor
     */
    fun getConstructor(): Constructor? {
        return constructor
    }

    /**
     * returns the Parameters
     *
     * @return returns the Parameters
     */
    fun getParameters(): Array<Object?>? {
        return parameters
    }

    /**
     * constructor of the pair Object
     *
     * @param constructor
     * @param parameters
     */
    init {
        this.constructor = constructor
        this.parameters = parameters
        constructor.setAccessible(true)
    }
}