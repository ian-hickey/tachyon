/**
 * Copyright (c) 2015, Tachyon Assosication Switzerland. All rights reserved.
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
 * implements BIF CacheRegionExists. This function only exists for compatibility with other CFML
 * Engines and should be avoided where possible. The preferred method to manipulate Cache
 * connections is via the Administrator interface or in Application.
 */
class CacheRegionExists : BIF() {
    @Override
    @Throws(PageException::class)
    operator fun invoke(pc: PageContext?, args: Array<Object?>?): Object? {
        if (args!!.size == 1) return call(pc, Caster.toString(args[0]))
        if (args.size == 2) return call(pc, Caster.toString(args[0]), Caster.toString(args[1]))
        throw FunctionException(pc, "CacheRegionExists", 0, 1, args.size)
    }

    companion object {
        private const val serialVersionUID = 5966166102856736134L
        @Throws(PageException::class)
        fun call(pc: PageContext?, cacheName: String?, strWebAdminPassword: String?): Boolean {
            val webAdminPassword: Password = CacheUtil.getPassword(pc, strWebAdminPassword, false)
            return try {
                val adminConfig: ConfigAdmin = ConfigAdmin.newInstance(pc.getConfig(), webAdminPassword)
                adminConfig.cacheConnectionExists(cacheName)
            } catch (e: Exception) {
                throw Caster.toPageException(e)
            }
        }

        @Throws(PageException::class)
        fun call(pc: PageContext?, cacheName: String?): Boolean {
            return call(pc, cacheName, null)
        }
    }
}