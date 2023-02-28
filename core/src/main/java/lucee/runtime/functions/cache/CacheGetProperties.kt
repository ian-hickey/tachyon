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
class CacheGetProperties : BIF() {
    @Override
    @Throws(PageException::class)
    operator fun invoke(pc: PageContext?, args: Array<Object?>?): Object? {
        if (args!!.size == 0) return call(pc)
        if (args.size == 1) return call(pc, Caster.toString(args[0]))
        throw FunctionException(pc, "CacheGetProperties", 0, 1, args.size)
    }

    companion object {
        private const val serialVersionUID = -8665995702411192700L
        @Throws(PageException::class)
        fun call(pc: PageContext?): Array? {
            return call(pc, null)
        }

        @Throws(PageException::class)
        fun call(pc: PageContext?, cacheName: String?): Array? {
            val arr: Array = ArrayImpl()
            return try {
                if (StringUtil.isEmpty(cacheName)) {
                    addDefault(pc, Config.CACHE_TYPE_OBJECT, arr)
                    addDefault(pc, Config.CACHE_TYPE_TEMPLATE, arr)
                    addDefault(pc, Config.CACHE_TYPE_QUERY, arr)
                    addDefault(pc, Config.CACHE_TYPE_RESOURCE, arr)
                    addDefault(pc, Config.CACHE_TYPE_FUNCTION, arr)
                    addDefault(pc, Config.CACHE_TYPE_INCLUDE, arr)
                    addDefault(pc, Config.CACHE_TYPE_HTTP, arr)
                    addDefault(pc, Config.CACHE_TYPE_FILE, arr)
                    addDefault(pc, Config.CACHE_TYPE_WEBSERVICE, arr)
                    // MUST welcher muss zuers sein
                } else {
                    var name: String
                    val names: Array<String?> = ListUtil.listToStringArray(cacheName, ',')
                    for (i in names.indices) {
                        name = names[i].trim()
                        if (name.equalsIgnoreCase("template")) arr.appendEL(CacheUtil.getDefault(pc, Config.CACHE_TYPE_TEMPLATE).getCustomInfo()) else if (name.equalsIgnoreCase("object")) arr.appendEL(CacheUtil.getDefault(pc, Config.CACHE_TYPE_OBJECT).getCustomInfo()) else if (name.equalsIgnoreCase("query")) arr.appendEL(CacheUtil.getDefault(pc, Config.CACHE_TYPE_QUERY).getCustomInfo()) else if (name.equalsIgnoreCase("resource")) arr.appendEL(CacheUtil.getDefault(pc, Config.CACHE_TYPE_RESOURCE).getCustomInfo()) else if (name.equalsIgnoreCase("function")) arr.appendEL(CacheUtil.getDefault(pc, Config.CACHE_TYPE_FUNCTION).getCustomInfo()) else if (name.equalsIgnoreCase("include")) arr.appendEL(CacheUtil.getDefault(pc, Config.CACHE_TYPE_INCLUDE).getCustomInfo()) else if (name.equalsIgnoreCase("http")) arr.appendEL(CacheUtil.getDefault(pc, Config.CACHE_TYPE_HTTP).getCustomInfo()) else if (name.equalsIgnoreCase("file")) arr.appendEL(CacheUtil.getDefault(pc, Config.CACHE_TYPE_FILE).getCustomInfo()) else if (name.equalsIgnoreCase("webservice")) arr.appendEL(CacheUtil.getDefault(pc, Config.CACHE_TYPE_WEBSERVICE).getCustomInfo()) else arr.appendEL(CacheUtil.getCache(pc, name).getCustomInfo())
                    }
                }
                arr
            } catch (e: IOException) {
                throw Caster.toPageException(e)
            }
        }

        private fun addDefault(pc: PageContext?, type: Int, arr: Array?) {
            try {
                arr.appendEL(CacheUtil.getDefault(pc, type).getCustomInfo())
            } catch (e: IOException) {
            }
        }
    }
}