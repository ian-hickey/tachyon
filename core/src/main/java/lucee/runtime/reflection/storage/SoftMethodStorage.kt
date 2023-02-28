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
package lucee.runtime.reflection.storage

import java.lang.ref.SoftReference

/**
 * Method Storage Class
 */
class SoftMethodStorage {
    private val tokens: ConcurrentHashMap<String?, Object?>? = ConcurrentHashMap<String?, Object?>()
    private val map: Map<Class?, SoftReference<Map<Key?, Map<Integer?, Array<Method?>?>?>?>?>? = ConcurrentHashMap<Class?, SoftReference<Map<Key?, Map<Integer?, Array<Method?>?>?>?>?>()

    /**
     * returns a methods matching given criteria or null if method doesn't exist
     *
     * @param clazz clazz to get methods from
     * @param methodName Name of the Method to get
     * @param count wished count of arguments
     * @return matching Methods as Array
     */
    fun getMethods(clazz: Class?, methodName: Collection.Key?, count: Int): Array<Method?>? {
        val tmp: SoftReference<Map<Key?, Map<Integer?, Array<Method?>?>?>?>? = map!![clazz]
        var methodsMap: Map<Key?, Map<Integer?, Array<Method?>?>?>? = if (tmp == null) null else tmp.get()
        if (methodsMap == null) methodsMap = store(clazz)
        val methods: Map<Any?, Array<Any?>?> = methodsMap!![methodName] ?: return null
        val arr: Array<Method?>? = methods[count + 1]

        // sort because of LDEV-2430
        if (arr != null && arr.size > 1) {
            // is sorting necessary?
            val str: String = arr[0].getName()
            var needSorting = false
            for (i in 1 until arr.size) {
                if (!str.equals(arr[i].getName())) {
                    needSorting = true
                    break
                }
            }
            if (needSorting) {
                val arrSorted: Array<Method?> = arrayOfNulls<Method?>(arr.size)
                for (i in arr.indices) {
                    arrSorted[i] = arr[i]
                }
                Arrays.sort(arrSorted, object : Comparator<Method?>() {
                    @Override
                    fun compare(l: Method?, r: Method?): Int {
                        if (methodName.getString().equals(l.getName())) return -1
                        return if (methodName.getString().equals(r.getName())) 1 else 0
                    }
                })
                return arrSorted
            }
        }
        return arr
    }

    /**
     * store a class with his methods
     *
     * @param clazz
     * @return returns stored struct
     */
    private fun store(clazz: Class?): Map<Key?, Map<Integer?, Array<Method?>?>?>? {
        synchronized(getToken(clazz)) {
            val methods: Array<Method?> = clazz.getMethods()
            val methodsMap: Map<Key?, Map<Integer?, Array<Method?>?>?> = ConcurrentHashMap<Key?, Map<Integer?, Array<Method?>?>?>()
            for (i in methods.indices) {
                storeMethod(methods[i], methodsMap)
            }
            map.put(clazz, SoftReference<Map<Key?, Map<Integer?, Array<Method?>?>?>?>(methodsMap))
            return methodsMap
        }
    }

    private fun getToken(clazz: Class?): Object? {
        val newLock = Object()
        var lock: Object = tokens.putIfAbsent(clazz.getName(), newLock)
        if (lock == null) {
            lock = newLock
        }
        return lock
    }

    /**
     * stores a single method
     *
     * @param method
     * @param methodsMap
     */
    private fun storeMethod(method: Method?, methodsMap: Map<Key?, Map<Integer?, Array<Method?>?>?>?) {
        val methodName: Key = KeyImpl.init(method.getName())
        var methodArgs: Map<Integer?, Array<Method?>?>? = methodsMap!![methodName]
        if (methodArgs == null) {
            methodArgs = ConcurrentHashMap<Integer?, Array<Method?>?>()
            methodsMap.put(methodName, methodArgs)
        }
        storeArgs(method, methodArgs)
    }

    /**
     * stores arguments of a method
     *
     * @param method
     * @param methodArgs
     */
    private fun storeArgs(method: Method?, methodArgs: Map<Integer?, Array<Method?>?>?) {
        val pmt: Array<Class?> = method.getParameterTypes()
        val args: Array<Method?>?
        val ms: Array<Method?>? = methodArgs!![pmt.size + 1]
        if (ms == null) {
            args = arrayOfNulls<Method?>(1)
            methodArgs.put(pmt.size + 1, args)
        } else {
            args = arrayOfNulls<Method?>(ms.size + 1)
            for (i in ms.indices) {
                args!![i] = ms[i]
            }
            methodArgs.put(pmt.size + 1, args)
        }
        args!![args.size - 1] = method
    }
}