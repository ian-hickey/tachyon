package tachyon.runtime.type.query

import tachyon.commons.io.SystemUtil.TemplateLine

interface QueryResult : Dumpable, Duplicable {
    fun getSql(): SQL?
    fun isCached(): Boolean
    fun setCacheType(cacheType: String?)
    fun getCacheType(): String?
    fun getExecutionTime(): Long
    fun setExecutionTime(time: Long)
    fun getTemplate(): String?
    fun getName(): String?
    fun getColumncount(): Int
    fun getRecordcount(): Int
    fun getUpdateCount(): Int
    fun setUpdateCount(updateCount: Int)
    fun getColumnNames(): Array<Key?>?

    @Throws(PageException::class)
    fun setColumnNames(columnNames: Array<Key?>?)
    fun getTemplateLine(): TemplateLine?
}