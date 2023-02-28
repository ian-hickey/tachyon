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

import tachyon.runtime.PageContext

/**
 *
 */
class CacheGetDefaultCacheName : BIF() {
    @Override
    @Throws(PageException::class)
    operator fun invoke(pc: PageContext?, args: Array<Object?>?): Object? {
        if (args!!.size == 1) return call(pc, Caster.toString(args[0]))
        throw FunctionException(pc, "CacheGetDefaultCacheName", 1, 1, args.size)
    }

    companion object {
        private const val serialVersionUID = 6115589794465960484L
        @Throws(PageException::class)
        fun call(pc: PageContext?, strType: String?): String? {
            val type: Int = CacheUtil.toType(strType, Config.CACHE_TYPE_NONE)
            if (type == Config.CACHE_TYPE_NONE) throw FunctionException(pc, "CacheGetDefaultCacheName", 1, "type", "invalid type definition [$strType], valid types are [object, template, query, resource, function, include, http, file, webservice]")
            val config: ConfigPro = pc.getConfig() as ConfigPro
            val conn: CacheConnection = config.getCacheDefaultConnection(type)
                    ?: throw ExpressionException("there is no default cache defined for type [$strType]")
            return conn.getName()
        }
    }
}