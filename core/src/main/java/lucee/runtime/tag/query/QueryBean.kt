package lucee.runtime.tag.query

import java.io.Serializable

class QueryBean : Serializable {
    /** If specified, password overrides the password value specified in the data source setup.  */
    var password: String? = null

    /** The name of the data source from which this query should retrieve data.  */
    var datasource: DataSource? = null

    /**
     * The maximum number of milliseconds for the query to execute before returning an error indicating
     * that the query has timed-out. This attribute is not supported by most ODBC drivers. timeout is
     * supported by the SQL Server 6.x or above driver. The minimum and maximum allowable values vary,
     * depending on the driver.
     */
    var timeout: TimeSpan? = null

    /** This is the age of which the query data can be  */
    var cachedWithin: Object? = null

    /**
     * Specifies the maximum number of rows to fetch at a time from the server. The range is 1, default
     * to 100. This parameter applies to ORACLE native database drivers and to ODBC drivers. Certain
     * ODBC drivers may dynamically reduce the block factor at runtime.
     */
    var blockfactor = -1

    /** The database driver type.  */
    var dbtype: String? = null

    /**
     * Used for debugging queries. Specifying this attribute causes the SQL statement submitted to the
     * data source and the number of records returned from the query to be returned.
     */
    var debug = true
    /* This is specific to JTags, and allows you to give the cache a specific name */ // public String cachename;
    /** Specifies the maximum number of rows to return in the record set.  */
    var maxrows = -1

    /** If specified, username overrides the username value specified in the data source setup.  */
    var username: String? = null

    /**   */
    var cachedAfter: DateTime? = null

    /**
     * The name query. Must begin with a letter and may consist of letters, numbers, and the underscore
     * character, spaces are not allowed. The query name is used later in the page to reference the
     * query's record set.
     */
    var name: String? = null
    var result: String? = null
    var indexName: Collection.Key? = null

    // public static HSQLDBHandler hsql=new HSQLDBHandler();
    var items: ArrayList<SQLItem?>? = ArrayList<SQLItem?>()
    var unique = false
    var ormoptions: Struct? = null
    var returntype: Int = Query.RETURN_TYPE_UNDEFINED
    var timezone: TimeZone? = null
    var tmpTZ: TimeZone? = null
    var lazy = false
    var params: Object? = null
    var nestingLevel = 0
    var setReturnVariable = false
    var rtn: Object? = null
    var columnName: Key? = null
    var literalTimestampWithTSOffset = false
    var previousLiteralTimestampWithTSOffset = false
    var tags: Array<String?>? = null
    var sql: String? = null
    var hasBody = false
    var listener: TagListener? = null
    var rawDatasource: Object? = null
    var async = false
    fun release() {
        items.clear()
        password = null
        datasource = null
        rawDatasource = null
        timeout = null
        cachedWithin = null
        cachedAfter = null
        // cachename="";
        blockfactor = -1
        dbtype = null
        debug = true
        maxrows = -1
        username = null
        name = ""
        result = null
        unique = false
        ormoptions = null
        returntype = Query.RETURN_TYPE_UNDEFINED
        timezone = null
        tmpTZ = null
        lazy = false
        params = null
        nestingLevel = 0
        rtn = null
        setReturnVariable = false
        columnName = null
        literalTimestampWithTSOffset = false
        previousLiteralTimestampWithTSOffset = false
        tags = null
        sql = null
        hasBody = false
        listener = null
        async = false
        indexName = null
    }
}