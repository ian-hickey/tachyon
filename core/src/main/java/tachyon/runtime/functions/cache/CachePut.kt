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

import tachyon.commons.io.cache.Cache

/**
 *
 */
class CachePut : BIF() {
    @Override
    @Throws(PageException::class)
    operator fun invoke(pc: PageContext?, args: Array<Object?>?): Object? {
        if (args!!.size == 2) return call(pc, Caster.toString(args[0]), args[1])
        if (args.size == 3) return call(pc, Caster.toString(args[0]), args[1], Caster.toTimespan(args[2]))
        if (args.size == 4) return call(pc, Caster.toString(args[0]), args[1], Caster.toTimespan(args[2]), Caster.toTimespan(args[3]))
        if (args.size == 5) return call(pc, Caster.toString(args[0]), args[1], Caster.toTimespan(args[2]), Caster.toTimespan(args[3]), Caster.toString(args[4]))
        throw FunctionException(pc, "CacheKeyExists", 1, 2, args.size)
    }

    companion object {
        private const val serialVersionUID = -8636947330333269874L
        @Throws(PageException::class)
        fun call(pc: PageContext?, key: String?, value: Object?): String? {
            return _call(pc, key, value, null, null, null)
        }

        @Throws(PageException::class)
        fun call(pc: PageContext?, key: String?, value: Object?, timeSpan: TimeSpan?): String? {
            return _call(pc, key, value, valueOf(timeSpan), null, null)
        }

        @Throws(PageException::class)
        fun call(pc: PageContext?, key: String?, value: Object?, timeSpan: TimeSpan?, idleTime: TimeSpan?): String? {
            return _call(pc, key, value, valueOf(timeSpan), valueOf(idleTime), null)
        }

        @Throws(PageException::class)
        fun call(pc: PageContext?, key: String?, value: Object?, timeSpan: TimeSpan?, idleTime: TimeSpan?, cacheName: String?): String? {
            return _call(pc, key, value, valueOf(timeSpan), valueOf(idleTime), cacheName)
        }

        @Throws(PageException::class)
        private fun _call(pc: PageContext?, key: String?, value: Object?, timeSpan: Long?, idleTime: Long?, cacheName: String?): String? {
            try {
                val cache: Cache = CacheUtil.getCache(pc, cacheName, Config.CACHE_TYPE_OBJECT)
                cache.put(CacheUtil.key(key), value, idleTime, timeSpan)
            } catch (e: Exception) {
                throw Caster.toPageException(e)
            }
            return ""
        }

        private fun valueOf(timeSpan: TimeSpan?): Long? {
            return if (timeSpan == null) null else Long.valueOf(timeSpan.getMillis())
        }
    }
}