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

import java.util.ArrayList

class CacheSetProperties : BIF() {
    @Override
    @Throws(PageException::class)
    operator fun invoke(pc: PageContext?, args: Array<Object?>?): Object? {
        if (args!!.size == 1) return call(pc, Caster.toStruct(args[0]))
        throw FunctionException(pc, "CacheSetProperties", 1, 1, args.size)
    }

    companion object {
        private const val serialVersionUID = -5700264673510261084L
        private val OBJECT_TYPE: Key? = KeyImpl.getInstance("objecttype")
        @Throws(PageException::class)
        fun call(pc: PageContext?, properties: Struct?): Object? {
            try {
                val obj: Object = properties.removeEL(OBJECT_TYPE)
                val objectType: String = Caster.toString(obj)
                val conns: Array<CacheConnection?>? = getCaches(pc, objectType)
                for (i in conns.indices) {
                    setProperties(conns!![i], properties)
                }
            } catch (e: CacheException) {
                throw Caster.toPageException(e)
            }
            return call(pc, null)
        }

        @Throws(SecurityException::class)
        private fun setProperties(cc: CacheConnection?, properties: Struct?) {
            throw SecurityException("it is not allowed to change cache connection setting this way, please use the tag cfadmin or the Lucee administrator frontend instead ")
        }

        @Throws(CacheException::class)
        private fun getCaches(pc: PageContext?, cacheName: String?): Array<CacheConnection?>? {
            val config: ConfigPro = pc.getConfig() as ConfigPro
            if (StringUtil.isEmpty(cacheName)) {
                return arrayOf<CacheConnection?>(config.getCacheDefaultConnection(Config.CACHE_TYPE_OBJECT), config.getCacheDefaultConnection(Config.CACHE_TYPE_TEMPLATE))
                // MUST which one is first
            }
            val list: ArrayList<CacheConnection?> = ArrayList<CacheConnection?>()
            var name: String
            val names: Array<String?> = ListUtil.listToStringArray(cacheName, ',')
            for (i in names.indices) {
                name = names[i].trim().toLowerCase()
                if (name.equalsIgnoreCase("template")) list.add(config.getCacheDefaultConnection(Config.CACHE_TYPE_TEMPLATE)) else if (name.equalsIgnoreCase("object")) list.add(config.getCacheDefaultConnection(Config.CACHE_TYPE_OBJECT)) else {
                    val cc: CacheConnection = config.getCacheConnections().get(name)
                            ?: throw CacheException("there is no cache defined with name [$name]")
                    list.add(cc)
                }
            }
            return list.toArray(arrayOfNulls<CacheConnection?>(list.size()))
        }
    }
}