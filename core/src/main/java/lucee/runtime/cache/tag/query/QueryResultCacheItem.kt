package lucee.runtime.cache.tag.query

import java.io.Serializable

abstract class QueryResultCacheItem protected constructor(qr: QueryResult?, tags: Array<String?>?, datasourceName: String?, creationDate: Long) : CacheItem, Dumpable, Serializable, Duplicable {
    private val creationDate: Long
    private val queryResult: QueryResult?
    private val tags: Array<String?>?
    private val dsn: String?
    fun getQueryResult(): QueryResult? {
        return queryResult
    }

    @Override
    fun getName(): String? {
        return queryResult.getName()
    }

    @Override
    fun getPayload(): Long {
        return queryResult.getRecordcount()
    }

    @Override
    fun getMeta(): String? {
        return queryResult.getSql().getSQLString()
    }

    @Override
    fun getExecutionTime(): Long {
        return queryResult.getExecutionTime()
    }

    fun getTags(): Array<String?>? {
        return tags
    }

    fun getDatasourceName(): String? {
        return dsn
    }

    fun getCreationDate(): Long {
        return creationDate
    }

    @Override
    fun toDumpData(pageContext: PageContext?, maxlevel: Int, properties: DumpProperties?): DumpData? {
        return queryResult.toDumpData(pageContext, maxlevel, properties)
    }

    fun isCachedAfter(cacheAfter: Date?): Boolean {
        if (cacheAfter == null) return true
        return if (creationDate >= cacheAfter.getTime()) {
            true
        } else false
    }

    companion object {
        private const val serialVersionUID = -2322582053856364084L
        private val EMPTY: Array<String?>? = arrayOfNulls<String?>(0)
        fun newInstance(qr: QueryResult?, tags: Array<String?>?, ds: DataSource?, defaultValue: CacheItem?): CacheItem? {
            val dsn: String? = if (ds == null) null else ds.getName()
            if (qr is Query) return QueryCacheItem(qr as Query?, tags, dsn) else if (qr is QueryArray) return QueryArrayItem(qr as QueryArray?, tags, dsn) else if (qr is QueryStruct) return QueryStructItem(qr as QueryStruct?, tags, dsn)
            return defaultValue
        }
    }

    init {
        queryResult = qr
        this.creationDate = creationDate
        this.tags = tags ?: EMPTY
        dsn = datasourceName
    }
}