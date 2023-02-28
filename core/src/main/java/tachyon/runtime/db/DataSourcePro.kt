package tachyon.runtime.db

import tachyon.runtime.tag.listener.TagListener

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
    val listener: tachyon.runtime.tag.listener.TagListener?
    val idleTimeout: Int
    val liveTimeout: Int
    val minIdle: Int
    val maxIdle: Int
    val maxTotal: Int
    var isMSSQL: Boolean
}