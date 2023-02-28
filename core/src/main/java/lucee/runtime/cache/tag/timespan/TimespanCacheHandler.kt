/**
 * Copyright (c) 2014, the Railo Company Ltd.
 * Copyright (c) 2015, Lucee Assosication Switzerland
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
package lucee.runtime.cache.tag.timespan

import java.io.IOException

class TimespanCacheHandler : CacheHandlerPro {
    private var cacheType = 0
    private var defaultCache: Cache? = null
    private var id: String? = null
    @Override
    fun init(cw: ConfigWeb?, id: String?, cacheType: Int) {
        this.cacheType = cacheType
        this.id = id
    }

    fun setDefaultCache(defaultCache: Cache?) {
        this.defaultCache = defaultCache
    }

    @Override
    operator fun get(pc: PageContext?, id: String?): CacheItem? {
        val cachedValue: Object = getCache(pc).getValue(id, null)
        return CacheHandlerCollectionImpl.toCacheItem(cachedValue, null)
    }

    @Override
    fun remove(pc: PageContext?, id: String?): Boolean {
        try {
            return getCache(pc).remove(id)
        } catch (e: IOException) {
        }
        return false
    }

    @Override
    @Throws(PageException::class)
    operator fun set(pc: PageContext?, id: String?, cachedWithin: Object?, value: CacheItem?) {
        val cachedWithinMillis: Long
        cachedWithinMillis = if (Decision.isDate(cachedWithin, false) && cachedWithin !is TimeSpan) Caster.toDate(cachedWithin, null).getTime() - System.currentTimeMillis() else Caster.toTimespan(cachedWithin).getMillis()
        if (cachedWithinMillis == 0L) return
        try {
            getCache(pc).put(id, value, Long.valueOf(cachedWithinMillis), Long.valueOf(cachedWithinMillis))
        } catch (e: IOException) {
            throw Caster.toPageException(e)
        }
    }

    @Override
    fun clean(pc: PageContext?) {
        try {
            val c: Cache? = getCache(pc)
            val entries: List<CacheEntry?> = c.entries()
            if (entries.size() < 100) return
            val it: Iterator<CacheEntry?> = entries.iterator()
            while (it.hasNext()) {
                it.next() // touch them to makes sure the cache remove them, not really good, cache must do this by itself
            }
        } catch (ioe: IOException) {
        }
    }

    @Override
    fun clear(pc: PageContext?) {
        try {
            getCache(pc).remove(CacheKeyFilterAll.getInstance())
        } catch (e: IOException) {
        }
    }

    @Override
    fun clear(pc: PageContext?, filter: CacheHandlerFilter?) {
        CacheHandlerCollectionImpl.clear(pc, getCache(pc), filter)
    }

    @Override
    fun size(pc: PageContext?): Int {
        return try {
            getCache(pc).keys().size()
        } catch (e: IOException) {
            0
        }
    }

    private fun getCache(pc: PageContext?): Cache? {
        val cache: Cache = CacheUtil.getDefault(pc, cacheType, null)
        if (cache == null) {
            if (defaultCache == null) {
                val rm: RamCache = RamCache().init(0, 0, RamCache.DEFAULT_CONTROL_INTERVAL)
                rm.decouple()
                defaultCache = rm
            }
            return defaultCache
        }
        return if (cache is CachePro) (cache as CachePro).decouple() else cache
    }

    @Override
    fun id(): String? {
        return id
    }

    @Override
    fun release(pc: PageContext?) {
        // to nothing
    }

    @Override
    fun acceptCachedWithin(cachedWithin: Object?): Boolean {
        return Caster.toTimespan(cachedWithin, null) != null
    }

    @Override
    fun pattern(): String? {
        // TODO Auto-generated method stub
        return "#createTimespan(0,0,0,10)#"
    }

    @Override
    @Throws(PageException::class)
    operator fun get(pc: PageContext?, cacheId: String?, cachePolicy: Object?): CacheItem? {
        val cachedAfter: Date?
        if (Decision.isDate(cachePolicy, false) && cachePolicy !is TimeSpan) {
            // cachedAfter was passed
            cachedAfter = Caster.toDate(cachePolicy, null)
        } else {
            val cachedWithinMillis: Long = Caster.toTimeSpan(cachePolicy).getMillis()
            if (cachedWithinMillis == 0L) {
                remove(pc, cacheId)
                return null
            }
            cachedAfter = Date(System.currentTimeMillis() - cachedWithinMillis)
        }
        val cacheItem: CacheItem? = this[pc, cacheId]
        return if (cacheItem is QueryResultCacheItem) {
            if ((cacheItem as QueryResultCacheItem?).isCachedAfter(cachedAfter)) cacheItem else null
            // cacheItem is from before cachedAfter, discard it so that it can be refreshed
        } else cacheItem
    }
}