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
package lucee.runtime.cache.legacy

import java.io.ByteArrayInputStream

class CacheItemCache(pc: PageContext?, req: HttpServletRequest?, id: String?, key: String?, useId: Boolean, cache: Cache?, timespan: TimeSpan?) : CacheItem(pc, req, id, key, useId) {
    private val cache: Cache?
    private val timespan: TimeSpan?
    private val lcFileName: String?

    @Override
    override fun isValid(): Boolean {
        return try {
            cache.getValue(lcFileName) != null
        } catch (e: IOException) {
            false
        }
    }

    @Override
    override fun isValid(timespan: TimeSpan?): Boolean {
        return isValid()
    }

    @Override
    @Throws(IOException::class)
    override fun writeTo(os: OutputStream?, charset: String?) {
        val barr: ByteArray = getValue().getBytes(if (StringUtil.isEmpty(charset, true)) "UTF-8" else charset)
        IOUtil.copy(ByteArrayInputStream(barr), os, true, false)
    }

    @Override
    @Throws(IOException::class)
    override fun getValue(): String? {
        return try {
            Caster.toString(cache.getValue(lcFileName))
        } catch (e: PageException) {
            throw ExceptionUtil.toIOException(e)
        }
    }

    @Override
    @Throws(IOException::class)
    override fun store(value: String?) {
        cache.put(lcFileName, value, null, valueOf(timespan))
    }

    @Override
    @Throws(IOException::class)
    override fun store(barr: ByteArray?, append: Boolean) {
        var value = if (append) getValue() else ""
        value += IOUtil.toString(barr, "UTF-8")
        store(value)
    }

    companion object {
        @Throws(IOException::class)
        fun _flushAll(pc: PageContext?, cache: Cache?) {
            cache.remove(CacheKeyFilterAll.getInstance())
        }

        @Throws(IOException::class)
        fun _flush(pc: PageContext?, cache: Cache?, expireurl: String?) {
            cache.remove(WildCardFilter(expireurl, true))
        }

        private fun valueOf(timeSpan: TimeSpan?): Long? {
            return if (timeSpan == null) null else Long.valueOf(timeSpan.getMillis())
        }
    }

    init {
        this.cache = cache
        this.timespan = timespan
        lcFileName = fileName
    }
}