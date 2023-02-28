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
package tachyon.runtime.cache.tag

import java.util.Iterator

abstract class MapCacheHandler : CacheHandler {
    private var cacheType = 0
    private var id: String? = null
    @Override
    fun init(cw: ConfigWeb?, id: String?, cacheType: Int) {
        this.id = id
        this.cacheType = cacheType
    }

    @Override
    operator fun get(pc: PageContext?, id: String?): CacheItem? {
        return duplicate(map()!![id])
    }

    @Override
    fun remove(pc: PageContext?, id: String?): Boolean {
        return map().remove(id) != null
    }

    @Override
    operator fun set(pc: PageContext?, id: String?, cachedwithin: Object?, value: CacheItem?) {
        // cachedwithin is ignored in this cache, it should be "request"
        map().put(id, duplicate(value))
    }

    private fun duplicate(value: CacheItem?): CacheItem? {
        return if (value == null) null else Duplicator.duplicate(value, true) as CacheItem
    }

    @Override
    fun clear(pc: PageContext?) {
        map().clear()
    }

    @Override
    fun clear(pc: PageContext?, filter: CacheHandlerFilter?) {
        val it: Iterator<Entry<String?, CacheItem?>?> = map().entrySet().iterator()
        var e: Entry<String?, CacheItem?>?
        while (it.hasNext()) {
            e = it.next()
            if (filter == null || filter.accept(e.getValue())) it.remove()
        }
    }

    @Override
    fun size(pc: PageContext?): Int {
        return map()!!.size()
    }

    @Override
    fun clean(pc: PageContext?) {
        // not necessary
    }

    @Override
    fun id(): String? {
        return id
    }

    @Override
    fun release(pc: PageContext?) {
        clear(pc)
    }

    protected abstract fun map(): Map<String?, CacheItem?>?
}