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
package lucee.runtime.cache

import java.io.IOException

abstract class CacheSupport : CachePro {
    @Override
    @Throws(IOException::class)
    fun keys(filter: CacheKeyFilter?): List<String?>? {
        val all: Boolean = CacheUtil.allowAll(filter)
        val keys: List<String?> = keys()
        val list: List<String?> = ArrayList<String?>()
        val it = keys.iterator()
        var key: String?
        while (it.hasNext()) {
            key = it.next()
            if (all || filter.accept(key)) list.add(key)
        }
        return list
    }

    @Override
    @Throws(CacheException::class)
    fun verify() {
        getCustomInfo()
    }

    @Override
    @Throws(IOException::class)
    fun keys(filter: CacheEntryFilter?): List<String?>? {
        val all: Boolean = CacheUtil.allowAll(filter)
        val keys: List<String?> = keys()
        val list: List<String?> = ArrayList<String?>()
        val it = keys.iterator()
        var key: String?
        var entry: CacheEntry?
        while (it.hasNext()) {
            key = it.next()
            entry = getQuiet(key, null)
            if (all || filter.accept(entry)) list.add(key)
        }
        return list
    }

    @Override
    @Throws(IOException::class)
    fun entries(): List<CacheEntry?>? {
        val keys: List<String?> = keys()
        val list: List<CacheEntry?> = ArrayList<CacheEntry?>()
        val it = keys.iterator()
        while (it.hasNext()) {
            list.add(getQuiet(it.next(), null))
        }
        return list
    }

    @Override
    @Throws(IOException::class)
    fun entries(filter: CacheKeyFilter?): List<CacheEntry?>? {
        val keys: List<String?> = keys()
        val list: List<CacheEntry?> = ArrayList<CacheEntry?>()
        val it = keys.iterator()
        var key: String?
        while (it.hasNext()) {
            key = it.next()
            if (filter.accept(key)) list.add(getQuiet(key, null))
        }
        return list
    }

    @Override
    @Throws(IOException::class)
    fun entries(filter: CacheEntryFilter?): List<CacheEntry?>? {
        val keys: List<String?> = keys()
        val list: List<CacheEntry?> = ArrayList<CacheEntry?>()
        val it = keys.iterator()
        var entry: CacheEntry?
        while (it.hasNext()) {
            entry = getQuiet(it.next(), null)
            if (filter.accept(entry)) list.add(entry)
        }
        return list
    }

    // there was the wrong generic type defined in the older interface, because of that we do not define
    // a generic type at all here, just to be sure
    @Override
    @Throws(IOException::class)
    fun values(): List? {
        val keys: List<String?> = keys()
        val list: List<Object?> = ArrayList<Object?>()
        val it = keys.iterator()
        var key: String?
        while (it.hasNext()) {
            key = it.next()
            list.add(getQuiet(key, null).getValue())
        }
        return list
    }

    // there was the wrong generic type defined in the older interface, because of that we do not define
    // a generic type at all here, just to be sure
    @Override
    @Throws(IOException::class)
    fun values(filter: CacheEntryFilter?): List? {
        if (CacheUtil.allowAll(filter)) return values()
        val keys: List<String?> = keys()
        val list: List<Object?> = ArrayList<Object?>()
        val it = keys.iterator()
        var key: String?
        var entry: CacheEntry?
        while (it.hasNext()) {
            key = it.next()
            entry = getQuiet(key, null)
            if (filter.accept(entry)) list.add(entry.getValue())
        }
        return list
    }

    // there was the wrong generic type defined in the older interface, because of that we do not define
    // a generic type at all here, just to be sure
    @Override
    @Throws(IOException::class)
    fun values(filter: CacheKeyFilter?): List? {
        if (CacheUtil.allowAll(filter)) return values()
        val keys: List<String?> = keys()
        val list: List<Object?> = ArrayList<Object?>()
        val it = keys.iterator()
        var key: String?
        while (it.hasNext()) {
            key = it.next()
            if (filter.accept(key)) {
                val ce: CacheEntry? = getQuiet(key, null)
                if (ce != null) // possible that the entry is gone since keys(); call above
                    list.add(ce.getValue())
            }
        }
        return list
    }

    @Override
    @Throws(IOException::class)
    fun remove(filter: CacheEntryFilter?): Int {
        if (CacheUtil.allowAll(filter)) return clear()
        val keys: List<String?> = keys()
        var count = 0
        val it = keys.iterator()
        var key: String?
        var entry: CacheEntry?
        while (it.hasNext()) {
            key = it.next()
            entry = getQuiet(key, null)
            if (filter == null || filter.accept(entry)) {
                remove(key)
                count++
            }
        }
        return count
    }

    @Override
    @Throws(IOException::class)
    fun remove(filter: CacheKeyFilter?): Int {
        if (CacheUtil.allowAll(filter)) return clear()
        val keys: List<String?> = keys()
        var count = 0
        val it = keys.iterator()
        var key: String?
        while (it.hasNext()) {
            key = it.next()
            if (filter == null || filter.accept(key)) {
                remove(key)
                count++
            }
        }
        return count
    }

    @Override
    fun getCustomInfo(): Struct? {
        return CacheUtil.getInfo(this)
    }

    @Override
    @Throws(IOException::class)
    fun getValue(key: String?): Object? {
        return getCacheEntry(key).getValue()
    }

    @Override
    operator fun getValue(key: String?, defaultValue: Object?): Object? {
        val entry: CacheEntry = getCacheEntry(key, null) ?: return defaultValue
        return entry.getValue()
    }

    @Override
    @Throws(IOException::class)
    fun getCacheEntry(key: String?): CacheEntry? {
        return getCacheEntry(key, null)
                ?: throw CacheException("there is no valid cache entry with key [$key]")
    }

    @Throws(IOException::class)
    fun getQuiet(key: String?): CacheEntry? {
        return getQuiet(key, null) ?: throw CacheException("there is no valid cache entry with key [$key]")
    }

    abstract fun getQuiet(key: String?, defaultValue: CacheEntry?): CacheEntry?

    /**
     * remove all entries
     *
     * @return returns the count of the removal or -1 if this information is not available
     */
    @Throws(IOException::class)
    abstract fun clear(): Int

    companion object {
        protected fun valid(entry: CacheEntry?): Boolean {
            if (entry == null) return false
            val now: Long = System.currentTimeMillis()
            if (entry.liveTimeSpan() > 0 && entry.liveTimeSpan() + getTime(entry.lastModified()) < now) {
                return false
            }
            return if (entry.idleTimeSpan() > 0 && entry.idleTimeSpan() + getTime(entry.lastHit()) < now) {
                false
            } else true
        }

        private fun getTime(date: Date?): Long {
            return if (date == null) 0 else date.getTime()
        }
    }
}