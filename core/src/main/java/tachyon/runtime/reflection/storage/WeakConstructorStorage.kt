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
package tachyon.runtime.reflection.storage

import java.lang.reflect.Constructor

/**
 * Constructor Storage Class
 */
class WeakConstructorStorage {
    private val map: WeakHashMap<Class?, Array?>? = WeakHashMap<Class?, Array?>()

    /**
     * returns a constructor matching given criteria or null if Constructor doesn't exist
     *
     * @param clazz Class to get Constructor for
     * @param count count of arguments for the constructor
     * @return returns the constructors
     */
    fun getConstructors(clazz: Class?, count: Int): Array<Constructor?>? {
        var con: Array
        var o: Object
        synchronized(map) {
            o = map.get(clazz)
            con = if (o == null) {
                store(clazz)
            } else o
        }
        o = con.get(count + 1, null)
        return if (o == null) null else o
    }

    /**
     * stores the constructors for a Class
     *
     * @param clazz
     * @return stored structure
     */
    private fun store(clazz: Class?): Array? {
        val conArr: Array<Constructor?> = clazz.getConstructors()
        val args: Array = ArrayImpl()
        for (i in conArr.indices) {
            storeArgs(conArr[i], args)
        }
        map.put(clazz, args)
        return args
    }

    /**
     * separate and store the different arguments of one constructor
     *
     * @param constructor
     * @param conArgs
     */
    private fun storeArgs(constructor: Constructor?, conArgs: Array?) {
        val pmt: Array<Class?> = constructor.getParameterTypes()
        val o: Object = conArgs.get(pmt.size + 1, null)
        val args: Array<Constructor?>?
        if (o == null) {
            args = arrayOfNulls<Constructor?>(1)
            conArgs.setEL(pmt.size + 1, args)
        } else {
            val cs: Array<Constructor?> = o
            args = arrayOfNulls<Constructor?>(cs.size + 1)
            for (i in cs.indices) {
                args!![i] = cs[i]
            }
            conArgs.setEL(pmt.size + 1, args)
        }
        args!![args.size - 1] = constructor
    }
}