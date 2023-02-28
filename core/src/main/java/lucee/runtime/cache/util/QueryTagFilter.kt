package lucee.runtime.cache.util

import lucee.commons.io.cache.CacheEntry

class QueryTagFilter(private val tags: Array<String?>?, datasourceName: String?) : CacheEntryFilter {
    private val sct: Struct? = StructImpl()
    private val datasourceName: String?
    @Override
    fun accept(ce: CacheEntry?): Boolean {
        val `val`: Object = ce.getValue()
        if (`val` is QueryResultCacheItem) {
            // need to be same datasource
            if (StringUtil.isEmpty(datasourceName) || datasourceName.equalsIgnoreCase((`val` as QueryResultCacheItem).getDatasourceName())) {
                // does a tag match?
                val _tags: Array<String?> = (`val` as QueryResultCacheItem).getTags()
                if (_tags != null) {
                    for (tag in _tags) {
                        if (sct.containsKey(tag)) {
                            return true
                        }
                    }
                }
            }
        }
        return false
    }

    @Override
    fun toPattern(): String? {
        return "tags:" + ListUtil.arrayToList(tags, ", ")
    }

    init {
        for (tag in tags!!) {
            sct.put(tag, "")
        }
        this.datasourceName = datasourceName
    }
}