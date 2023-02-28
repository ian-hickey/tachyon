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
package tachyon.runtime.functions.cache

import java.io.IOException

/**
 *
 */
class CacheDelete : BIF() {
    @Override
    @Throws(PageException::class)
    operator fun invoke(pc: PageContext?, args: Array<Object?>?): Object? {
        if (args!!.size == 1) return _call(pc, args[0], false, null)
        if (args.size == 2) return _call(pc, args[0], Caster.toBooleanValue(args[1]), null)
        if (args.size == 3) return _call(pc, args[0], Caster.toBooleanValue(args[1]), Caster.toString(args[2]))
        throw FunctionException(pc, "CacheDelete", 1, 3, args.size)
    }

    companion object {
        private const val serialVersionUID = 4148677299207997607L
        private var remove: Method? = null
        @Deprecated
        @Throws(PageException::class)
        fun call(pc: PageContext?, id: String?): String? {
            return _call(pc, id, false, null)
        }

        @Deprecated
        @Throws(PageException::class)
        fun call(pc: PageContext?, id: String?, throwOnError: Boolean): String? {
            return _call(pc, id, throwOnError, null)
        }

        @Deprecated
        @Throws(PageException::class)
        fun call(pc: PageContext?, id: String?, throwOnError: Boolean, cacheName: String?): String? {
            return _call(pc, id, throwOnError, cacheName)
        }

        @Throws(PageException::class)
        fun call(pc: PageContext?, id: Object?): String? {
            return _call(pc, id, false, null)
        }

        @Throws(PageException::class)
        fun call(pc: PageContext?, id: Object?, throwOnError: Boolean): String? {
            return _call(pc, id, throwOnError, null)
        }

        @Throws(PageException::class)
        fun call(pc: PageContext?, id: Object?, throwOnError: Boolean, cacheName: String?): String? {
            return _call(pc, id, throwOnError, cacheName)
        }

        @Throws(PageException::class)
        fun _call(pc: PageContext?, id: Object?, throwOnError: Boolean, cacheName: String?): String? {
            var id: Object? = id
            try {
                val cache: Cache = CacheUtil.getCache(pc, cacheName, Config.CACHE_TYPE_OBJECT)
                if (Decision.isArray(id)) {
                    val arr: Array = Caster.toArray(id)
                    id = if (arr.size() === 1) {
                        Caster.toString(arr.getE(1))
                    } else {
                        if (!remove(cache, toKeys(arr)) && throwOnError) {
                            throw ApplicationException("can not remove the element with the following ids [" + ListUtil.arrayToList(arr, ", ").toString() + "]")
                        }
                        return null
                    }
                }
                if (!cache.remove(CacheUtil.key(Caster.toString(id))) && throwOnError) {
                    throw ApplicationException("can not remove the element with the following id [$id]")
                }
            } catch (e: IOException) {
                throw Caster.toPageException(e)
            }
            return null
        }

        @Throws(PageException::class)
        private fun toKeys(array: Array?): Array<String?>? {
            val arr = arrayOfNulls<String?>(array.size())
            for (i in arr.indices) {
                arr[i] = CacheUtil.key(Caster.toString(array.get(i + 1, null)))
            }
            return arr
        }

        @Throws(PageException::class, IOException::class)
        private fun remove(cache: Cache?, keys: Array<String?>?): Boolean {
            // // public boolean remove(String[] keys) throws IOException { FUTURE add to interface
            return try {
                Caster.toBooleanValue(getMethod(cache).invoke(cache, arrayOf(keys)))
            } catch (e: NoSuchMethodException) {
                var rtn = true
                for (key in keys!!) {
                    if (!cache.remove(key)) rtn = false
                }
                rtn
            } catch (e: Exception) {
                throw Caster.toPageException(e)
            }
        }

        @Throws(NoSuchMethodException::class, SecurityException::class)
        private fun getMethod(cache: Cache?): Method? {
            if (remove == null || remove.getDeclaringClass() !== cache.getClass()) {
                remove = cache.getClass().getMethod("remove", arrayOf<Class?>(Array<String>::class.java))
            }
            return remove
        }
    }
}