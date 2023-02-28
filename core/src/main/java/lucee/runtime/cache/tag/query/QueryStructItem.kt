package lucee.runtime.cache.tag.query

import lucee.commons.digest.HashUtil

class QueryStructItem(queryStruct: QueryStruct?, tags: Array<String?>?, datasourceName: String?) : QueryResultCacheItem(queryStruct, tags, datasourceName, System.currentTimeMillis()) {
    val queryStruct: QueryStruct?
    @Override
    fun getHashFromValue(): String? {
        return toString(HashUtil.create64BitHash(UDFArgConverter.serialize(queryStruct)))
    }

    fun getQueryStruct(): QueryStruct? {
        return queryStruct
    }

    @Override
    fun duplicate(deepCopy: Boolean): Object? {
        return QueryStructItem(queryStruct.duplicate(true) as QueryStruct, getTags(), getDatasourceName())
    }

    companion object {
        private const val serialVersionUID = 7327671003736543783L
    }

    init {
        this.queryStruct = queryStruct
    }
}