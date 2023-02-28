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
class CacheCount : BIF() {
    @Override
    @Throws(PageException::class)
    operator fun invoke(pc: PageContext?, args: Array<Object?>?): Object? {
        if (args!!.size == 0) return call(pc)
        if (args.size == 1) return call(pc, Caster.toString(args[0]))
        throw FunctionException(pc, "CacheCount", 0, 1, args.size)
    }

    companion object {
        private const val serialVersionUID = 4192649311671009474L
        @Throws(PageException::class)
        fun call(pc: PageContext?): Double {
            return call(pc, null)
        }

        @Throws(PageException::class)
        fun call(pc: PageContext?, cacheName: String?): Double {
            return try {
                CacheUtil.getCache(pc, cacheName, Config.CACHE_TYPE_OBJECT).keys().size()
            } catch (e: IOException) {
                throw Caster.toPageException(e)
            }
        }
    }
}