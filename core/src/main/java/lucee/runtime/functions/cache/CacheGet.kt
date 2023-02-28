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
class CacheGet : BIF() {
    @Override
    @Throws(PageException::class)
    operator fun invoke(pc: PageContext?, args: Array<Object?>?): Object? {
        if (args!!.size == 1) return call(pc, Caster.toString(args[0]))
        if (args.size == 2) return call(pc, Caster.toString(args[0]), args[1])
        if (args.size == 3) return call(pc, Caster.toString(args[0]), args[1], Caster.toString(args[2]))
        throw FunctionException(pc, "CacheGet", 1, 3, args.size)
    }

    companion object {
        private const val serialVersionUID = -7164470356423036571L
        @Throws(PageException::class)
        fun call(pc: PageContext?, key: String?): Object? {
            return try {
                _call(pc, key, false, CacheUtil.getDefault(pc, Config.CACHE_TYPE_OBJECT))
            } catch (e: IOException) {
                throw Caster.toPageException(e)
            }
        }

        @Throws(PageException::class)
        fun call(pc: PageContext?, key: String?, objThrowWhenNotExist: Object?): Object? {
            // default behavior, second parameter is a boolean
            val throwWhenNotExist: Boolean = Caster.toBoolean(objThrowWhenNotExist, null)
            if (throwWhenNotExist != null) {
                return try {
                    _call(pc, key, throwWhenNotExist.booleanValue(), CacheUtil.getDefault(pc, Config.CACHE_TYPE_OBJECT))
                } catch (e: IOException) {
                    throw Caster.toPageException(e)
                }
            }

            // compatibility behavior, second parameter is a cacheName
            if (objThrowWhenNotExist is String) {
                val cacheName = objThrowWhenNotExist as String?
                if (!StringUtil.isEmpty(cacheName)) {
                    try {
                        val cache: Cache = CacheUtil.getCache(pc, cacheName, null)
                        if (cache != null) return _call(pc, key, false, cache)
                    } catch (e: IOException) {
                        throw Caster.toPageException(e)
                    }
                }
            }
            throw FunctionException(pc, "cacheGet", 2, "ThrowWhenNotExist",
                    "arguments needs to be a boolean value, but also a valid cacheName is supported for compatibility reasons to other engines")
        }

        @Throws(PageException::class)
        fun call(pc: PageContext?, key: String?, objThrowWhenNotExist: Object?, cacheName: String?): Object? {
            val throwWhenNotExist: Boolean = Caster.toBoolean(objThrowWhenNotExist, null)
                    ?: throw FunctionException(pc, "cacheGet", 2, "ThrowWhenNotExist", "arguments needs to be a boolean value")
            return try {
                val cache: Cache = CacheUtil.getCache(pc, cacheName, Config.CACHE_TYPE_OBJECT)
                _call(pc, key, throwWhenNotExist.booleanValue(), cache)
            } catch (e: IOException) {
                throw Caster.toPageException(e)
            }
        }

        @Throws(IOException::class)
        private fun _call(pc: PageContext?, key: String?, throwWhenNotExist: Boolean, cache: Cache?): Object? {
            return if (throwWhenNotExist) cache.getValue(CacheUtil.key(key)) else cache.getValue(CacheUtil.key(key), null)
        }
    }
}