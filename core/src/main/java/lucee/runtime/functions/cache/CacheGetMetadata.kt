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
package lucee.runtime.functions.cache

import java.io.IOException

/**
 *
 */
class CacheGetMetadata : BIF() {
    @Override
    @Throws(PageException::class)
    operator fun invoke(pc: PageContext?, args: Array<Object?>?): Object? {
        if (args!!.size == 1) return call(pc, Caster.toString(args[0]))
        if (args.size == 2) return call(pc, Caster.toString(args[0]), Caster.toString(args[1]))
        throw FunctionException(pc, "CacheGetMetadata", 1, 2, args.size)
    }

    companion object {
        private const val serialVersionUID = -470089623854482521L
        private val CACHE_HITCOUNT: Collection.Key? = KeyImpl.getInstance("cache_hitcount")
        private val CACHE_MISSCOUNT: Collection.Key? = KeyImpl.getInstance("cache_misscount")
        private val CACHE_CUSTOM: Collection.Key? = KeyImpl.getInstance("cache_custom")
        private val CREATED_TIME: Collection.Key? = KeyImpl.getInstance("createdtime")
        private val IDLE_TIME: Collection.Key? = KeyImpl.getInstance("idletime")
        private val LAST_HIT: Collection.Key? = KeyImpl.getInstance("lasthit")
        private val LAST_UPDATED: Collection.Key? = KeyImpl.getInstance("lastupdated")
        @Throws(PageException::class)
        fun call(pc: PageContext?, id: String?): Struct? {
            return call(pc, id, null)
        }

        @Throws(PageException::class)
        fun call(pc: PageContext?, id: String?, cacheName: String?): Struct? {
            return try {
                val cache: Cache = CacheUtil.getCache(pc, cacheName, Config.CACHE_TYPE_OBJECT)
                val entry: CacheEntry = cache.getCacheEntry(CacheUtil.key(id))
                val info: Struct = StructImpl()
                info.set(CACHE_HITCOUNT, Double.valueOf(cache.hitCount()))
                info.set(CACHE_MISSCOUNT, Double.valueOf(cache.missCount()))
                info.set(CACHE_CUSTOM, cache.getCustomInfo())
                info.set(KeyConstants._custom, entry.getCustomInfo())
                info.set(CREATED_TIME, entry.created())
                info.set(KeyConstants._hitcount, Double.valueOf(entry.hitCount()))
                info.set(IDLE_TIME, Double.valueOf(entry.idleTimeSpan()))
                info.set(LAST_HIT, entry.lastHit())
                info.set(LAST_UPDATED, entry.lastModified())
                info.set(KeyConstants._size, Double.valueOf(entry.size()))
                info.set(KeyConstants._timespan, Double.valueOf(entry.liveTimeSpan()))
                info
            } catch (e: IOException) {
                throw Caster.toPageException(e)
            }
        }
    }
}