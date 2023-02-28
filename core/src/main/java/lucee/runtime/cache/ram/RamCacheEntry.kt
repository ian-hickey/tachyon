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
package lucee.runtime.cache.ram

import java.io.ByteArrayOutputStream

class RamCacheEntry(private val key: String?, value: Object?, idleTime: Long, until: Long) : CacheEntry {
    private var value: Object?
    private val idleTime: Long
    private val until: Long
    private val created: Long
    private var modifed: Long
    private var accessed: Long
    private var hitCount: Int
    @Override
    fun created(): Date? {
        return Date(created)
    }

    @Override
    fun getCustomInfo(): Struct? {
        return CacheUtil.getInfo(this)
    }

    @Override
    fun getKey(): String? {
        return key
    }

    @Override
    fun getValue(): Object? {
        return value
    }

    @Override
    fun hitCount(): Int {
        return hitCount
    }

    @Override
    fun idleTimeSpan(): Long {
        return idleTime
    }

    @Override
    fun lastHit(): Date? {
        return Date(accessed)
    }

    @Override
    fun lastModified(): Date? {
        return Date(modifed)
    }

    @Override
    fun liveTimeSpan(): Long {
        return until
    }

    @Override
    fun size(): Long {
        return sizeOf(value).toLong()
    }

    fun update(value: Object?) {
        this.value = value
        accessed = System.currentTimeMillis()
        modifed = accessed
        hitCount++
    }

    fun read(): RamCacheEntry? {
        accessed = System.currentTimeMillis()
        hitCount++
        return this
    }

    companion object {
        private fun sizeOf(o: Object?): Int {
            val os = ByteArrayOutputStream()
            var oos: ObjectOutputStream? = null
            try {
                oos = ObjectOutputStream(os)
                oos.writeObject(o)
            } catch (t: Throwable) {
                ExceptionUtil.rethrowIfNecessary(t)
            } finally {
                IOUtil.closeEL(oos)
            }
            return os.toByteArray().length
        }
    }

    init {
        this.value = value
        this.idleTime = idleTime
        this.until = until
        accessed = System.currentTimeMillis()
        modifed = accessed
        created = modifed
        hitCount = 1
    }
}