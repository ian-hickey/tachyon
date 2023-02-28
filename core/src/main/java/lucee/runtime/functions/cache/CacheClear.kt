/**
 * Copyright (c) 2014, the Railo Company Ltd.
 * Copyright (c) 2015, Lucee Assosication Switzerland
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

import lucee.commons.io.cache.Cache

/**
 *
 */
class CacheClear : BIF(), Function, CacheKeyFilter {
    @Override
    fun accept(key: String?): Boolean {
        return true
    }

    @Override
    fun toPattern(): String? {
        return "*"
    }

    @Override
    @Throws(PageException::class)
    operator fun invoke(pc: PageContext?, args: Array<Object?>?): Object? {
        if (args!!.size == 0) return call(pc)
        if (args.size == 1) return call(pc, args[0])
        if (args.size == 2) return call(pc, args[0], Caster.toString(args[1]))
        throw FunctionException(pc, "CacheClear", 0, 2, args.size)
    }

    companion object {
        private const val serialVersionUID = 6080620551371620016L
        var FILTER: CacheKeyFilter? = CacheClear()
        @Throws(PageException::class)
        fun call(pc: PageContext?): Double {
            return _call(pc, null, null)
        }

        @Throws(PageException::class)
        fun call(pc: PageContext?, filterOrTags: Object?): Double {
            return _call(pc, filterOrTags, null)
        }

        @Throws(PageException::class)
        fun call(pc: PageContext?, filterOrTags: Object?, cacheName: String?): Double {
            return _call(pc, filterOrTags, cacheName)
        }

        @Throws(PageException::class)
        private fun _call(pc: PageContext?, filterOrTags: Object?, cacheName: String?): Double {
            return try {
                var filter: Object? = FILTER
                // tags
                var isArray = false
                var dsn: String? = null
                if (Decision.isArray(filterOrTags).also { isArray = it } || Decision.isStruct(filterOrTags)) {

                    // read tags from collection and datasource (optional)
                    val tags: Array<String?>
                    if (!isArray) {
                        val sct: Struct = Caster.toStruct(filterOrTags)
                        val arr: Array = Caster.toArray(sct.get("tags", null), null)
                                ?: throw FunctionException(pc, "CacheClear", 1, "tags",
                                        "if you pass the tags within a struct, that struct need to have a key [tags] containing the tags in an array.")
                        tags = ListUtil.toStringArray(arr)
                        dsn = Caster.toString(sct.get(KeyConstants._datasource, null), null)
                    } else {
                        tags = ListUtil.toStringArray(Caster.toArray(filterOrTags))
                    }

                    // get default datasource
                    if (StringUtil.isEmpty(dsn)) {
                        val tmp: Object = pc.getApplicationContext().getDefDataSource()
                        dsn = if (tmp is CharSequence) Caster.toString(tmp, null) else null
                    }
                    filter = QueryTagFilter(tags, if (StringUtil.isEmpty(dsn)) null else dsn)
                } else {
                    val strFilter: String = Caster.toString(filterOrTags)
                    if (CacheGetAllIds.isFilter(strFilter)) filter = WildCardFilter(strFilter, true)
                }
                val cache: Cache = CacheUtil.getCache(pc, cacheName, Config.CACHE_TYPE_OBJECT)
                if (filter is CacheKeyFilter) cache.remove(filter as CacheKeyFilter?) else cache.remove(filter as CacheEntryFilter?)
            } catch (e: Exception) {
                throw Caster.toPageException(e)
            }
        }
    }
}