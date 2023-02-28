/**
 * Copyright (c) 2014, the Railo Company Ltd.
 * Copyright (c) 2015, Tachyon Assosication Switzerland
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
package tachyon.runtime.tag

import java.io.IOException

/**
 * Flushes the query cache
 *
 *
 *
 */
class ObjectCache : TagImpl() {
    /** Clears queries from the cache in the Application scope.  */
    private var action: String? = "clear"
    private var filter: CacheHandlerFilter? = null
    private var result: String? = "cfObjectCache"
    private var type = TYPE_QUERY

    /**
     * set the value action Clears queries from the cache in the Application scope.
     *
     * @param action value to set
     */
    fun setAction(action: String?) {
        this.action = action
    }

    fun setResult(result: String?) {
        this.result = result
    }

    @Throws(ApplicationException::class)
    fun setType(strType: String?) {
        var strType = strType
        if (StringUtil.isEmpty(strType, true)) return
        strType = strType.trim().toLowerCase()
        type = if ("function".equals(strType)) TYPE_FUNCTION else if ("include".equals(strType)) TYPE_INCLUDE else if ("object".equals(strType)) TYPE_OBJECT else if ("query".equals(strType)) TYPE_QUERY else if ("resource".equals(strType)) TYPE_RESOURCE else if ("template".equals(strType)) TYPE_TEMPLATE else throw ApplicationException("invalid type [$strType], valid types are [function, include, object, query, resource, template]")
    }

    @Throws(PageException::class)
    fun setFilter(filter: Object?) {
        this.filter = createFilter(filter, false)
    }

    @Throws(PageException::class)
    fun setFilter(filter: String?) {
        this.filter = createFilter(filter, false)
    }

    @Throws(PageException::class)
    fun setFilterignorecase(filter: String?) {
        this.filter = createFilter(filter, true)
    }

    /*
	 * public static CacheHandlerFilter createFilterx(String sql) { if(!StringUtil.isEmpty(sql,true)) {
	 * return new QueryCacheHandlerFilter(sql); } return null; }
	 */
    @Override
    @Throws(PageException::class)
    fun doStartTag(): Int {
        try {
            _doStartTag()
        } catch (e: IOException) {
            throw Caster.toPageException(e)
        }
        return SKIP_BODY
    }

    @Throws(PageException::class, IOException::class)
    fun _doStartTag() {
        var factory: CacheHandlerCollection? = null
        var cache: Cache? = null
        if (type == TYPE_FUNCTION) factory = pageContext.getConfig().getCacheHandlerCollection(Config.CACHE_TYPE_FUNCTION, null) else if (type == TYPE_INCLUDE) factory = pageContext.getConfig().getCacheHandlerCollection(Config.CACHE_TYPE_INCLUDE, null) else if (type == TYPE_QUERY) factory = pageContext.getConfig().getCacheHandlerCollection(Config.CACHE_TYPE_QUERY, null) else if (type == TYPE_RESOURCE) {
            cache = CacheUtil.getDefault(pageContext, Config.CACHE_TYPE_RESOURCE, null)

            // no specific cache is defined, get default default cache
            if (cache == null) {
                // get cache resource provider
                var crp: CacheResourceProvider? = null
                val providers: Array<ResourceProvider?> = ResourcesImpl.getGlobal().getResourceProviders()
                for (i in providers.indices) {
                    if (providers[i].getScheme().equals("ram") && providers[i] is CacheResourceProvider) {
                        crp = providers[i] as CacheResourceProvider?
                    }
                }
                if (crp == null) throw ApplicationException(Constants.NAME.toString() + " was not able to load the Ram Resource Provider")

                // get cache from resource provider
                cache = crp.getCache()
            }
        } else if (type == TYPE_OBJECT) {
            // throws an exception if not explicitly defined
            cache = CacheUtil.getDefault(pageContext, Config.CACHE_TYPE_OBJECT)
        } else if (type == TYPE_TEMPLATE) {
            // throws an exception if not explicitly defined
            cache = CacheUtil.getDefault(pageContext, Config.CACHE_TYPE_TEMPLATE)
        }

        // Clear
        if (action.equalsIgnoreCase("clear")) {
            if (filter == null) {
                if (cache != null) cache.remove(CacheKeyFilterAll.getInstance()) else factory.clear(pageContext)
            } else {
                if (cache != null) CacheHandlerCollectionImpl.clear(pageContext, cache, filter) else factory.clear(pageContext, filter)
            }
        } else if (action.equalsIgnoreCase("size")) {
            var size = 0
            size = if (cache != null) cache.keys().size() else factory.size(pageContext)
            pageContext.setVariable(result, Caster.toDouble(size))
        } else throw ApplicationException("attribute action has an invalid value [$action], valid is only [clear,size]")
    }

    @Override
    fun doEndTag(): Int {
        return EVAL_PAGE
    }

    @Override
    fun release() {
        super.release()
        action = "clear"
        result = "cfObjectCache"
        filter = null
        type = TYPE_QUERY
    }

    companion object {
        private const val TYPE_QUERY = 1
        private const val TYPE_OBJECT = 2
        private const val TYPE_TEMPLATE = 3
        private const val TYPE_RESOURCE = 4
        private const val TYPE_FUNCTION = 5
        private const val TYPE_INCLUDE = 6
        @Throws(PageException::class)
        fun createFilter(filter: Object?, ignoreCase: Boolean): CacheHandlerFilter? {
            if (filter is UDF) return QueryCacheHandlerFilterUDF(filter as UDF?)
            val sql: String = Caster.toString(filter, null)
            return if (!StringUtil.isEmpty(sql, true)) {
                QueryCacheHandlerFilter(sql, ignoreCase)
            } else null
        }
    }
}