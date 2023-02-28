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
class CacheGetAll : BIF() {
    @Override
    @Throws(PageException::class)
    operator fun invoke(pc: PageContext?, args: Array<Object?>?): Object? {
        if (args!!.size == 0) return call(pc)
        if (args.size == 1) return call(pc, Caster.toString(args[0]))
        if (args.size == 2) return call(pc, Caster.toString(args[0]), Caster.toString(args[1]))
        throw FunctionException(pc, "CacheGetAll", 0, 2, args.size)
    }

    companion object {
        private const val serialVersionUID = 6395709569356486777L
        @Throws(PageException::class)
        fun call(pc: PageContext?): Struct? {
            return call(pc, null, null)
        }

        @Throws(PageException::class)
        fun call(pc: PageContext?, filter: String?): Struct? {
            return call(pc, filter, null)
        }

        @Throws(PageException::class)
        fun call(pc: PageContext?, filter: String?, cacheName: String?): Struct? {
            return try {
                val cache: Cache = CacheUtil.getCache(pc, cacheName, Config.CACHE_TYPE_OBJECT)
                val entries: List<CacheEntry?> = if (CacheGetAllIds.isFilter(filter)) cache.entries(WildCardFilter(filter, true)) else cache.entries()
                val it: Iterator<CacheEntry?> = entries.iterator()
                val sct: Struct = StructImpl()
                var entry: CacheEntry?
                while (it.hasNext()) {
                    entry = it.next()
                    sct.setEL(KeyImpl.init(entry.getKey()), entry.getValue())
                }
                sct
            } catch (e: Exception) {
                throw Caster.toPageException(e)
            }
        }
    }
}