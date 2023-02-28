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

import java.lang.reflect.Constructor

/**
 * class holds a Constructor and the parameter to call it
 */
class ConstructorInstance(constructor: Constructor?, args: Array<Object?>?) {
    private val constructor: Constructor?
    private val args: Array<Object?>?

    /**
     * Invokes the method
     *
     * @return return value of the Method
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     * @throws InstantiationException
     * @throws IllegalArgumentException
     */
    @Throws(IllegalArgumentException::class, InstantiationException::class, IllegalAccessException::class, InvocationTargetException::class)
    operator fun invoke(): Object? {
        return constructor.newInstance(args)
    }

    /**
     * @return Returns the args.
     */
    fun getArgs(): Array<Object?>? {
        return args
    }

    /**
     * @return Returns the constructor.
     */
    fun getConstructor(): Constructor? {
        return constructor
    }

    /**
     * constructor of the class
     *
     * @param constructor
     * @param args
     */
    init {
        this.constructor = constructor
        this.args = args
    }
}