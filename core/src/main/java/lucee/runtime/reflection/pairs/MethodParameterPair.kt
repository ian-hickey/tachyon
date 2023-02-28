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
package lucee.runtime.reflection.pairs

import java.lang.reflect.Method

/**
 * Hold a pair of method and parameter to invoke
 */
class MethodParameterPair(method: Method?, parameters: Array<Object?>?) {
    private val method: Method?
    private val parameters: Array<Object?>?

    /**
     * returns the Method
     *
     * @return returns the Method
     */
    fun getMethod(): Method? {
        return method
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
     * @param method
     * @param parameters
     */
    init {
        this.method = method
        this.parameters = parameters
        method.setAccessible(true)
    }
}