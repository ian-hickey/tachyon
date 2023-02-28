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
class CacheRemove : BIF() {
    @Override
    @Throws(PageException::class)
    operator fun invoke(pc: PageContext?, args: Array<Object?>?): Object? {
        if (args!!.size == 1) return call(pc, args[0])
        if (args.size == 2) return call(pc, args[0], Caster.toBooleanValue(args[1]))
        if (args.size == 3) return call(pc, args[0], Caster.toBooleanValue(args[1]), Caster.toString(args[2]))
        throw FunctionException(pc, "CacheRemove", 1, 3, args.size)
    }

    companion object {
        private const val serialVersionUID = -5823359978885018762L
        @Throws(PageException::class)
        fun call(pc: PageContext?, ids: Object?): String? {
            return call(pc, ids, false, null)
        }

        @Throws(PageException::class)
        fun call(pc: PageContext?, ids: Object?, throwOnError: Boolean): String? {
            return call(pc, ids, throwOnError, null)
        }

        @Throws(PageException::class)
        fun call(pc: PageContext?, ids: Object?, throwOnError: Boolean, cacheName: String?): String? {
            val arr: Array? = toArray(ids) //
            val it: Iterator = arr.valueIterator()
            var id: String
            val cache: Cache
            cache = try {
                CacheUtil.getCache(pc, cacheName, Config.CACHE_TYPE_OBJECT)
            } catch (e: IOException) {
                throw Caster.toPageException(e)
            }
            var sb: StringBuilder? = null
            try {
                while (it.hasNext()) {
                    id = CacheUtil.key(Caster.toString(it.next()))
                    if (!cache.remove(id) && throwOnError) {
                        if (sb == null) sb = StringBuilder() else sb.append(',')
                        sb.append(id)
                    }
                }
            } catch (e: IOException) {
            }
            if (throwOnError && sb != null) throw ApplicationException("can not remove the elements with the following id(s) [$sb]")
            return null
        }

        @Throws(PageException::class)
        private fun toArray(oIds: Object?): Array? {
            return if (Decision.isArray(oIds)) {
                Caster.toArray(oIds)
            } else ListUtil.listToArray(Caster.toString(oIds), ',')
        }
    }
}