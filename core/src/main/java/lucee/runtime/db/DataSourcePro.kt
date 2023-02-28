package lucee.runtime.db

import lucee.runtime.tag.listener.TagListener

// FUTURE move content to loader
interface DataSourcePro : DataSource {
    /**
     * should connections produced from this datasource be exclusive to a request or not?
     *
     * @return
     */
    val isRequestExclusive: Boolean
    val isAlwaysResetConnections: Boolean
    val defaultTransactionIsolation: Int
    val listener: lucee.runtime.tag.listener.TagListener?
    val idleTimeout: Int
    val liveTimeout: Int
    val minIdle: Int
    val maxIdle: Int
    val maxTotal: Int
    var isMSSQL: Boolean
}