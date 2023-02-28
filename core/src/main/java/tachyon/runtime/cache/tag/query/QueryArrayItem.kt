package tachyon.runtime.cache.tag.query

import tachyon.commons.digest.HashUtil

class QueryArrayItem(queryArray: QueryArray?, tags: Array<String?>?, datasourceName: String?) : QueryResultCacheItem(queryArray, tags, datasourceName, System.currentTimeMillis()) {
    val queryArray: QueryArray?
    @Override
    fun getHashFromValue(): String? {
        return toString(HashUtil.create64BitHash(UDFArgConverter.serialize(queryArray)))
    }

    fun getQueryArray(): QueryArray? {
        return queryArray
    }

    @Override
    fun duplicate(deepCopy: Boolean): Object? {
        return QueryArrayItem(queryArray.duplicate(true) as QueryArray, getTags(), getDatasourceName())
    }

    companion object {
        private const val serialVersionUID = 7327671003736543783L
    }

    init {
        this.queryArray = queryArray
    }
}