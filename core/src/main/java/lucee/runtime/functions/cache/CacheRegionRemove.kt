/**
 * Copyright (c) 2015, Lucee Assosication Switzerland. All rights reserved.
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

import lucee.runtime.PageContext

/**
 * implements BIF CacheRegionRemove. This function only exists for compatibility with other CFML
 * Engines and should be avoided where possible. The preferred method to manipulate Cache
 * connections is via the Administrator interface or in Application.
 */
class CacheRegionRemove : BIF() {
    @Override
    @Throws(PageException::class)
    operator fun invoke(pc: PageContext?, args: Array<Object?>?): Object? {
        if (args!!.size == 1) return call(pc, Caster.toString(args[0]))
        if (args.size == 2) return call(pc, Caster.toString(args[0]), Caster.toString(args[1]))
        throw FunctionException(pc, "CacheRegionRemove", 1, 2, args.size)
    }

    companion object {
        @Throws(PageException::class)
        fun call(pc: PageContext?, cacheName: String?, webAdminPassword: String?): String? {
            return _call(pc, cacheName, webAdminPassword)
        }

        @Throws(PageException::class)
        fun call(pc: PageContext?, cacheName: String?): String? {
            return _call(pc, cacheName, null)
        }

        @Throws(PageException::class)
        fun _call(pc: PageContext?, cacheName: String?, strWebAdminPassword: String?): String? {
            val webAdminPassword: Password = CacheUtil.getPassword(pc, strWebAdminPassword, false)
            try {
                val adminConfig: ConfigAdmin = ConfigAdmin.newInstance(pc.getConfig(), webAdminPassword)
                adminConfig.removeCacheConnection(cacheName)
                adminConfig.storeAndReload()
            } catch (e: Exception) {
                throw Caster.toPageException(e)
            }
            return null
        }
    }
}