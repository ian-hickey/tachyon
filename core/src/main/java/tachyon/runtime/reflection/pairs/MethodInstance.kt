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

import java.lang.reflect.InvocationTargetException

/**
 * class holds a Method and the parameter to call it
 */
class MethodInstance(method: Method?, args: Array<Object?>?) {
    private val method: Method?
    private val args: Array<Object?>?

    /**
     * Invokes the method
     *
     * @param o Object to invoke Method on it
     * @return return value of the Method
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * @throws InvocationTargetException
     */
    @Throws(IllegalAccessException::class, InvocationTargetException::class)
    operator fun invoke(o: Object?): Object? {
        return method.invoke(o, args)
    }

    /**
     * @return Returns the args.
     */
    fun getArgs(): Array<Object?>? {
        return args
    }

    /**
     * @return Returns the method.
     */
    fun getMethod(): Method? {
        return method
    }

    fun setAccessible(b: Boolean) {
        method.setAccessible(b)
    }

    /**
     * constructor of the class
     *
     * @param method
     * @param args
     */
    init {
        this.method = method
        this.args = args
        method.setAccessible(true)
    }
}