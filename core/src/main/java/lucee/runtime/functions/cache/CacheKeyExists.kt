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
class CacheKeyExists : BIF() {
    @Override
    @Throws(PageException::class)
    operator fun invoke(pc: PageContext?, args: Array<Object?>?): Object? {
        if (args!!.size == 1) return call(pc, Caster.toString(args[0]))
        if (args.size == 2) return call(pc, Caster.toString(args[0]), Caster.toString(args[1]))
        throw FunctionException(pc, "CacheKeyExists", 1, 2, args.size)
    }

    companion object {
        private const val serialVersionUID = -5656876871645994195L
        @Throws(PageException::class)
        fun call(pc: PageContext?, key: String?): Boolean {
            return call(pc, key, null)
        }

        @Throws(PageException::class)
        fun call(pc: PageContext?, key: String?, cacheName: String?): Boolean {
            return try {
                CacheUtil.getCache(pc, cacheName, Config.CACHE_TYPE_OBJECT).contains(CacheUtil.key(key))
            } catch (e: IOException) {
                throw Caster.toPageException(e)
            }
        }
    }
}