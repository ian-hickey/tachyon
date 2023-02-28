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
//import tachyon.runtime.cache.eh.EHCache;
import tachyon.runtime.PageContext

/**
 * implements BIF CacheRegionNew. This function only exists for compatibility with other CFML
 * Engines and should be avoided where possible. The preferred method to manipulate Cache
 * connections is via the Administrator interface or in Application.
 */
class CacheRegionNew : BIF() {
    @Override
    @Throws(PageException::class)
    operator fun invoke(pc: PageContext?, args: Array<Object?>?): Object? {
        if (args!!.size == 1) return call(pc, Caster.toString(args[0]))
        if (args.size == 2) return call(pc, Caster.toString(args[0]), args[1])
        if (args.size == 3) return call(pc, Caster.toString(args[0]), args[1], args[2])
        if (args.size == 4) return call(pc, Caster.toString(args[0]), args[1], args[2], Caster.toString(args[3]))
        throw FunctionException(pc, "CacheRegionNew", 1, 4, args.size)
    }

    companion object {
        @Throws(PageException::class)
        fun call(pc: PageContext?, cacheName: String?, arg2: Object?, arg3: Object?, arg4: String?): String? { // used Object for args 2 & 3 to match fld
            return _call(pc, cacheName, arg2 as Struct?, arg3 as Boolean?, arg4)
        }

        @Throws(PageException::class)
        fun call(pc: PageContext?, cacheName: String?, properties: Object?, arg3: Object?): String? {
            if (arg3 is Boolean) // name, properties, throwOnError
                return _call(pc, cacheName, properties as Struct?, arg3 as Boolean?, null)
            if (arg3 is String) // name, properties, password
                return _call(pc, cacheName, properties as Struct?, true, arg3 as String?)
            throw FunctionException(pc, "CacheRegionNew", 3, "throwOnError",
                    "when calling this function with 3 arguments the 3rd argument must be either throwOnError (Boolean), or webAdminPassword (String)")
        }

        @Throws(PageException::class)
        fun call(pc: PageContext?, cacheName: String?, arg2: Object?): String? {
            if (arg2 is Struct) // name, properties
                return _call(pc, cacheName, arg2 as Struct?, true, null)
            if (arg2 is String) // name, password
                return _call(pc, cacheName, StructImpl(), true, arg2 as String?)
            throw FunctionException(pc, "CacheRegionNew", 2, "properties",
                    "when calling this function with 2 arguments the 2nd argument must be either properties (Struct), or webAdminPassword (String)")
        }

        @Throws(PageException::class)
        fun call(pc: PageContext?, cacheName: String?): String? {
            return _call(pc, cacheName, StructImpl(), true, null) // name
        }

        @Throws(PageException::class)
        fun _call(pc: PageContext?, cacheName: String?, properties: Struct?, throwOnError: Boolean?, strWebAdminPassword: String?): String? {
            val webAdminPassword: Password = CacheUtil.getPassword(pc, strWebAdminPassword, false)
            try {
                val adminConfig: ConfigAdmin = ConfigAdmin.newInstance(pc.getConfig(), webAdminPassword) // TODO why we have here EHCache?
                adminConfig.updateCacheConnection(cacheName, ClassDefinitionImpl("org.tachyon.extension.cache.eh.EHCache", null, null, pc.getConfig().getIdentification()),
                        Config.CACHE_TYPE_NONE, properties, false, false)
                adminConfig.storeAndReload()
            } catch (e: Exception) {
                if (throwOnError!!) throw Caster.toPageException(e)
            }
            return null
        }
    }
}