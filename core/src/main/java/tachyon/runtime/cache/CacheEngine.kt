/**
 * Copyright (c) 2014, the Railo Company Ltd.
 * Copyright (c) 2015, Tachyon Assosication Switzerland
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
package tachyon.runtime.cache

import java.io.IOException

// MUST this must be come from configuration
class CacheEngine(cache: Cache?) {
    private val cache: Cache?
    @Throws(IOException::class)
    fun delete(key: String?, throwWhenNotExists: Boolean) {
        if (!cache.remove(key) && throwWhenNotExists) throw CacheException("there is no entry in cache with key [$key]")
    }

    @Throws(IOException::class)
    fun exists(key: String?): Boolean {
        return cache.contains(key)
    }

    @Throws(IOException::class)
    fun flush(key: String?, filter: String?): Int {
        if (!Util.isEmpty(key)) return if (cache.remove(key)) 1 else 0
        return if (!Util.isEmpty(filter)) cache.remove(WildCardFilter(filter, false)) else cache.remove(CacheKeyFilterAll.getInstance())
    }

    operator fun get(key: String?, defaultValue: Object?): Object? {
        return cache.getValue(key, defaultValue)
    }

    @Throws(IOException::class)
    operator fun get(key: String?): Object? {
        return cache.getValue(key)
    }

    fun keys(filter: String?): Array? {
        try {
            val keys: List
            keys = if (Util.isEmpty(filter)) cache.keys() else cache.keys(WildCardFilter(filter, false))
            return Caster.toArray(keys)
        } catch (e: Exception) {
        }
        return ArrayImpl()
    }

    fun list(filter: String?): Struct? {
        val sct: Struct = StructImpl()
        try {
            val entries: List
            entries = if (Util.isEmpty(filter)) cache.entries() else cache.entries(WildCardFilter(filter, false))
            val it: Iterator = entries.iterator()
            var entry: CacheEntry
            while (it.hasNext()) {
                entry = it.next() as CacheEntry
                sct.setEL(entry.getKey(), entry.getValue())
            }
        } catch (e: Exception) {
            LogUtil.log("cache", e)
        }
        return sct
    }

    @Throws(IOException::class)
    operator fun set(key: String?, value: Object?, timespan: TimeSpan?) {
        val until: Long? = if (timespan == null) null else Long.valueOf(timespan.getMillis())
        cache.put(key, value, null, until)
    }

    @Throws(IOException::class)
    fun info(): Struct? {
        return cache.getCustomInfo()
    }

    @Throws(IOException::class)
    fun info(key: String?): Struct? {
        if (key == null) return info()
        val entry: CacheEntry = cache.getCacheEntry(key)
        return entry.getCustomInfo()
    }

    fun getCache(): Cache? {
        return cache
    }

    companion object {
        private val caches: Map? = HashMap()
    }

    init {
        this.cache = cache
    }
}