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

import java.util.Iterator

/**
 *
 */
class CacheGetAllIds : BIF() {
    @Override
    @Throws(PageException::class)
    operator fun invoke(pc: PageContext?, args: Array<Object?>?): Object? {
        if (args!!.size == 0) return call(pc)
        if (args.size == 1) return call(pc, Caster.toString(args[0]))
        if (args.size == 2) return call(pc, Caster.toString(args[0]), Caster.toString(args[1]))
        throw FunctionException(pc, "CacheGetAllIds", 0, 2, args.size)
    }

    companion object {
        private const val serialVersionUID = 4831944874663718056L
        @Throws(PageException::class)
        fun call(pc: PageContext?): Array? {
            return call(pc, null, null)
        }

        @Throws(PageException::class)
        fun call(pc: PageContext?, filter: String?): Array? {
            return call(pc, filter, null)
        }

        @Throws(PageException::class)
        fun call(pc: PageContext?, filter: String?, cacheName: String?): Array? {
            return try {
                val cache: Cache = CacheUtil.getCache(pc, cacheName, Config.CACHE_TYPE_OBJECT)
                val keys: List<String?> = if (isFilter(filter)) cache.keys(WildCardFilter(filter, true)) else cache.keys()
                val arr: Array = ArrayImpl()
                if (keys != null) {
                    val it = keys.iterator()
                    while (it.hasNext()) {
                        arr.append(it.next())
                    }
                }
                arr
            } catch (e: Exception) {
                throw Caster.toPageException(e)
            }
        }

        fun isFilter(filter: String?): Boolean {
            return filter != null && filter.length() > 0 && !filter.equals("*")
        }
    }
}