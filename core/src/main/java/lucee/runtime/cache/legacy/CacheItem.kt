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
package lucee.runtime.cache.legacy

import java.io.IOException

abstract class CacheItem(pc: PageContext?, req: HttpServletRequest?, id: String?, key: String?, useId: Boolean) {
    protected val fileName: String?
    abstract fun isValid(): Boolean
    abstract fun isValid(timespan: TimeSpan?): Boolean
    @Throws(IOException::class)
    abstract fun writeTo(os: OutputStream?, charset: String?)
    @Throws(IOException::class)
    abstract fun getValue(): String?
    @Throws(IOException::class)
    abstract fun store(result: String?)
    @Throws(IOException::class)
    abstract fun store(barr: ByteArray?, append: Boolean)

    companion object {
        @Throws(IOException::class)
        fun getInstance(pc: PageContext?, id: String?, key: String?, useId: Boolean, dir: Resource?, cacheName: String?, timespan: TimeSpan?): CacheItem? {
            val req: HttpServletRequest = pc.getHttpServletRequest()
            val cache: Cache = CacheUtil.getCache(pc, cacheName, Config.CACHE_TYPE_TEMPLATE, null)
            return if (cache != null) CacheItemCache(pc, req, id, key, useId, cache, timespan) else CacheItemFS(pc, req, id, key, useId, dir)
        }

        // protected abstract void _flushAll(PageContext pc, Resource dir) throws IOException;
        // protected abstract void _flush(PageContext pc, Resource dir, String expireurl) throws
        // IOException;
        @Throws(IOException::class)
        fun flushAll(pc: PageContext?, dir: Resource?, cacheName: String?) {
            val cache: Cache = CacheUtil.getCache(pc, cacheName, Config.CACHE_TYPE_TEMPLATE, null)
            if (cache != null) CacheItemCache._flushAll(pc, cache) else CacheItemFS._flushAll(pc, dir)
        }

        @Throws(IOException::class)
        fun flush(pc: PageContext?, dir: Resource?, cacheName: String?, expireurl: String?) {
            val cache: Cache = CacheUtil.getCache(pc, cacheName, Config.CACHE_TYPE_TEMPLATE, null)
            if (cache != null) CacheItemCache._flush(pc, cache, expireurl) else CacheItemFS._flush(pc, dir, expireurl)
        }
    }

    init {

        // raw
        var filename: String? = req.getServletPath()
        if (!StringUtil.isEmpty(req.getQueryString())) {
            filename += "?" + req.getQueryString()
            if (useId) filename += "&cfcache_id=$id"
        } else {
            if (useId) filename += "?cfcache_id=$id"
        }
        if (useId && !StringUtil.isEmpty(key)) filename = key
        if (!StringUtil.isEmpty(req.getContextPath())) filename = req.getContextPath() + filename
        fileName = filename
    }
}