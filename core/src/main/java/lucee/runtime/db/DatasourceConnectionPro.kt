package lucee.runtime.db

import java.sql.SQLException

// FUTURE add methods to DatasourceConnection and delete this interface
interface DatasourceConnectionPro : DatasourceConnection {
    @get:Throws(SQLException::class)
    @set:Throws(SQLException::class)
    @set:Override
    var isAutoCommit: Boolean
    val defaultTransactionIsolation: Int

    @Throws(PageException::class)
    fun using(): DatasourceConnection?
    fun release()
    fun validate(): Boolean
    var isManaged: Boolean
}