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

import java.util.ArrayList

/**
 * Passes SQL statements to a data source. Not limited to queries.
 */
class Query : BodyTagTryCatchFinallyImpl() {
    var orgPSQ = false
    var hasChangedPSQ = false
    private var data: QueryBean? = QueryBean()

    private class ResMeta {
        var res: Object? = null
        var meta: Object? = null
        fun asQueryResult(): QueryResult? {
            return if (res is QueryResult) res as QueryResult? else null
        }
    }

    @Override
    fun release() {
        super.release()
        if (data.async) data = QueryBean() else data.release()
        orgPSQ = false
        hasChangedPSQ = false
    }

    @Throws(PageException::class)
    fun setTags(oTags: Object?) {
        if (StringUtil.isEmpty(oTags)) return
        // to Array
        val arr: Array
        arr = if (Decision.isArray(oTags)) Caster.toArray(oTags) else ListUtil.listToArrayRemoveEmpty(Caster.toString(oTags), ',')

        // to String[]
        val it: Iterator<Object?> = arr.valueIterator()
        val list: List<String?> = ArrayList<String?>()
        var str: String
        while (it.hasNext()) {
            str = Caster.toString(it.next())
            if (!StringUtil.isEmpty(str)) list.add(str)
        }
        data.tags = list.toArray(arrayOfNulls<String?>(list.size()))
    }

    fun setOrmoptions(ormoptions: Struct?) {
        data.ormoptions = ormoptions
    }

    @Throws(CasterException::class)
    fun setIndexname(indexName: String?) {
        data.indexName = KeyImpl.toKey(indexName)
    }

    @Throws(ApplicationException::class)
    fun setReturntype(strReturntype: String?) {
        if (StringUtil.isEmpty(strReturntype)) return
        data.returntype = toReturnType(strReturntype)
    }

    fun setUnique(unique: Boolean) {
        data.unique = unique
    }

    /**
     * @param result the result to set
     */
    fun setResult(result: String?) {
        data.result = result
    }

    /**
     * @param psq set preserver single quote
     */
    fun setPsq(psq: Boolean) {
        orgPSQ = pageContext.getPsq()
        if (orgPSQ != psq) {
            pageContext.setPsq(psq)
            hasChangedPSQ = true
        }
    }

    /**
     * set the value password If specified, password overrides the password value specified in the data
     * source setup.
     *
     * @param password value to set
     */
    fun setPassword(password: String?) {
        data.password = password
    }

    /**
     * set the value datasource The name of the data source from which this query should retrieve data.
     *
     * @param datasource value to set
     * @throws ClassException
     * @throws BundleException
     */
    @Throws(PageException::class, ClassException::class, BundleException::class)
    fun setDatasource(datasource: Object?) {
        if (datasource == null) return
        data.rawDatasource = datasource
        data.datasource = toDatasource(pageContext, datasource)
    }

    /**
     * set the value timeout The maximum number of milliseconds for the query to execute before
     * returning an error indicating that the query has timed-out. This attribute is not supported by
     * most ODBC drivers. timeout is supported by the SQL Server 6.x or above driver. The minimum and
     * maximum allowable values vary, depending on the driver.
     *
     * @param timeout value to set
     * @throws PageException
     */
    @Throws(PageException::class)
    fun setTimeout(timeout: Object?) {
        if (timeout is TimeSpan) data.timeout = timeout as TimeSpan? else {
            val i: Int = Caster.toIntValue(timeout)
            if (i < 0) throw ApplicationException("Invalid value [$i] for attribute [timeout], value must be a positive integer greater or equal than 0")
            data.timeout = TimeSpanImpl(0, 0, 0, i)
        }
    }

    /**
     * set the value cachedafter This is the age of which the query data can be
     *
     * @param cachedafter value to set
     */
    fun setCachedafter(cachedafter: DateTime?) {
        data.cachedAfter = cachedafter
    }

    /**
     * set the value cachename This is specific to JTags, and allows you to give the cache a specific
     * name
     *
     * @param cachename value to set
     */
    fun setCachename(cachename: String?) {
        // DeprecatedUtil.tagAttribute(pageContext,"query", "cachename");
        // this.cachename=cachename;
    }

    fun setColumnkey(columnKey: String?) {
        if (StringUtil.isEmpty(columnKey, true)) return
        data.columnName = KeyImpl.init(columnKey)
    }

    fun setCachedwithin(cachedwithin: Object?) {
        if (StringUtil.isEmpty(cachedwithin)) return
        data.cachedWithin = cachedwithin
    }

    fun setLazy(lazy: Boolean) {
        data.lazy = lazy
    }

    /**
     * set the value providerdsn Data source name for the COM provider, OLE-DB only.
     *
     * @param providerdsn value to set
     * @throws ApplicationException
     */
    @Throws(ApplicationException::class)
    fun setProviderdsn(providerdsn: String?) {
        // DeprecatedUtil.tagAttribute(pageContext,"Query", "providerdsn");
    }

    /**
     * set the value connectstring
     *
     * @param connectstring value to set
     * @throws ApplicationException
     */
    @Throws(ApplicationException::class)
    fun setConnectstring(connectstring: String?) {
        // DeprecatedUtil.tagAttribute(pageContext,"Query", "connectstring");
    }

    fun setTimezone(tz: TimeZone?) {
        if (tz == null) return
        data.timezone = tz
    }

    /**
     * set the value blockfactor Specifies the maximum number of rows to fetch at a time from the
     * server. The range is 1, default to 100. This parameter applies to ORACLE native database drivers
     * and to ODBC drivers. Certain ODBC drivers may dynamically reduce the block factor at runtime.
     *
     * @param blockfactor value to set
     */
    fun setBlockfactor(blockfactor: Double) {
        data.blockfactor = blockfactor.toInt()
    }

    /**
     * set the value dbtype The database driver type.
     *
     * @param dbtype value to set
     */
    fun setDbtype(dbtype: String?) {
        data.dbtype = dbtype.toLowerCase()
    }

    /**
     * set the value debug Used for debugging queries. Specifying this attribute causes the SQL
     * statement submitted to the data source and the number of records returned from the query to be
     * returned.
     *
     * @param debug value to set
     */
    fun setDebug(debug: Boolean) {
        data.debug = debug
    }

    /**
     * set the value dbname The database name, Sybase System 11 driver and SQLOLEDB provider only. If
     * specified, dbName overrides the default database specified in the data source.
     *
     * @param dbname value to set
     * @throws ApplicationException
     */
    fun setDbname(dbname: String?) {
        // DeprecatedUtil.tagAttribute(pageContext,"Query", "dbname");
    }

    /**
     * set the value maxrows Specifies the maximum number of rows to return in the record set.
     *
     * @param maxrows value to set
     */
    fun setMaxrows(maxrows: Double) {
        data.maxrows = maxrows.toInt()
    }

    /**
     * set the value username If specified, username overrides the username value specified in the data
     * source setup.
     *
     * @param username value to set
     */
    fun setUsername(username: String?) {
        if (!StringUtil.isEmpty(username)) data.username = username
    }

    /**
     * set the value provider COM provider, OLE-DB only.
     *
     * @param provider value to set
     * @throws ApplicationException
     */
    fun setProvider(provider: String?) {
        // DeprecatedUtil.tagAttribute(pageContext,"Query", "provider");
    }

    /**
     * set the value dbserver For native database drivers and the SQLOLEDB provider, specifies the name
     * of the database server computer. If specified, dbServer overrides the server specified in the
     * data source.
     *
     * @param dbserver value to set
     * @throws ApplicationException
     */
    fun setDbserver(dbserver: String?) {
        // DeprecatedUtil.tagAttribute(pageContext,"Query", "dbserver");
    }

    /**
     * set the value name The name query. Must begin with a letter and may consist of letters, numbers,
     * and the underscore character, spaces are not allowed. The query name is used later in the page to
     * reference the query's record set.
     *
     * @param name value to set
     */
    var name: String?
        get() = if (data.name == null) "query" else data.name
        set(name) {
            data.name = name
        }

    /**
     * @param item
     */
    fun setParam(item: SQLItem?) {
        data.items.add(item)
    }

    fun setParams(params: Object?) {
        data.params = params
    }

    fun setNestinglevel(nestingLevel: Double) {
        data.nestingLevel = nestingLevel.toInt()
    }

    @Throws(ApplicationException::class)
    fun setListener(listener: Object?) {
        if (listener == null) return
        data.listener = toTagListener(listener)
    }

    fun setAsync(async: Boolean) {
        data.async = async
    }

    fun setSql(sql: String?) {
        data.sql = sql
    }

    @Override
    @Throws(PageException::class)
    fun doStartTag(): Int {

        // default datasource
        if (data.datasource == null && (data.dbtype == null || !data.dbtype.equals("query"))) {
            val obj: Object = pageContext.getApplicationContext().getDefDataSource()
            if (StringUtil.isEmpty(obj)) {
                val isCFML = pageContext.getRequestDialect() === CFMLEngine.DIALECT_CFML
                throw ApplicationException("Attribute [datasource] is required when attribute [dbtype] is not [query] and no default datasource is defined",
                        ("you can define a default datasource as attribute [defaultdatasource] of the tag "
                                + if (isCFML) Constants.CFML_APPLICATION_TAG_NAME else Constants.LUCEE_APPLICATION_TAG_NAME) + " or as data member of the "
                                + (if (isCFML) Constants.CFML_APPLICATION_EVENT_HANDLER else Constants.LUCEE_APPLICATION_EVENT_HANDLER).toString() + " (this.defaultdatasource=\"mydatasource\";)")
            }
            data.datasource = if (obj is DataSource) obj as DataSource else pageContext.getDataSource(Caster.toString(obj))
        }
        // timeout
        if (data.datasource is DataSourceImpl && (data.datasource as DataSourceImpl).getAlwaysSetTimeout()) {
            val remaining: TimeSpan = PageContextUtil.remainingTime(pageContext, true)
            if (data.timeout == null || data.timeout.getSeconds() as Int <= 0 || data.timeout.getSeconds() > remaining.getSeconds()) { // not set
                data.timeout = remaining
            }
        }

        // timezone
        if (data.timezone != null || data.datasource != null && data.datasource.getTimeZone().also { data.timezone = it } != null) {
            data.tmpTZ = pageContext.getTimeZone()
            pageContext.setTimeZone(data.timezone)
        }
        val pci: PageContextImpl? = pageContext as PageContextImpl?

        // cache within
        if (StringUtil.isEmpty(data.cachedWithin)) {
            val tmp: Object = pageContext.getCachedWithin(ConfigWeb.CACHEDWITHIN_QUERY)
            if (tmp != null) setCachedwithin(tmp)
        }

        // literal timestamp with TSOffset
        if (data.datasource is DataSourceSupport) data.literalTimestampWithTSOffset = (data.datasource as DataSourceSupport).getLiteralTimestampWithTSOffset() else data.literalTimestampWithTSOffset = false
        data.previousLiteralTimestampWithTSOffset = pci.getTimestampWithTSOffset()
        pci.setTimestampWithTSOffset(data.literalTimestampWithTSOffset)
        return EVAL_BODY_BUFFERED
    }

    @Override
    @Throws(PageException::class)
    fun doEndTag(): Int {
        if (hasChangedPSQ) pageContext.setPsq(orgPSQ)

        // listener
        if (data.listener == null && data.datasource != null) { // if no datasource we have dbtype query, otherwise we
            // would have an exception in doStartTag
            // does the datasource define a listener?
            var listener: TagListener = (data.datasource as DataSourceSupport).getListener()
            if (listener != null) {
                data.listener = listener
            } else {
                val ac: ApplicationContext = pageContext.getApplicationContext()
                if (ac is ApplicationContextSupport) {
                    val acs: ApplicationContextSupport = ac as ApplicationContextSupport
                    listener = acs.getQueryListener()
                    if (listener != null) {
                        data.listener = listener
                    }
                }
            }
        }
        var strSQL: String?
        if (data.hasBody && !StringUtil.isEmpty(bodyContent.getString().trim().also { strSQL = it }, true)) { // we have a body
            if (!StringUtil.isEmpty(data.sql, true)) { // sql in attr and body
                if (!strSQL!!.equals(data.sql.trim())) throw DatabaseException("Defining SQL in the body and as an attribute at the same time is not permitted [" + strSQL + "," + data.sql + "]", null, null,
                        null)
            }
        } else {
            if (StringUtil.isEmpty(data.sql, true)) throw DatabaseException("The required sql string is not defined in the body of the query tag, and not in a sql attribute", null, null, null)
            strSQL = data.sql.trim()
        }
        if (!data.items.isEmpty() && data.params != null) throw DatabaseException("You cannot use the attribute [params] and sub tags queryparam at the same time", null, null, null)
        if (data.async) {
            val ps: PageSource? = pageSource
            ((pageContext.getConfig() as ConfigPro).getSpoolerEngine() as SpoolerEngineImpl).add(pageContext.getConfig(),
                    QuerySpoolerTask(pageContext, data, strSQL, toTemplateLine(pageContext.getConfig(), sourceTemplate, ps), ps))
        } else {
            _doEndTag(pageContext, data, strSQL, toTemplateLine(pageContext.getConfig(), sourceTemplate, pageSource), true) // when
            // sourceTemplate
            // exists
            // getPageSource
            // call was not
            // necessary
        }
        return EVAL_PAGE
    }

    private val pageSource: PageSource?
        private get() {
            if (data.nestingLevel > 0) {
                val pci: PageContextImpl? = pageContext as PageContextImpl?
                val ps: PageSource = pci.getPageSource(-data.nestingLevel)
                if (ps != null) return ps
            }
            return (pageContext as PageContextImpl?).getCurrentPageSource(null)
        }

    @Override
    fun doInitBody() {
    }

    @Override
    fun doAfterBody(): Int {
        return SKIP_BODY
    }

    var returnVariable: Object?
        get() = data.rtn
        set(setReturnVariable) {
            data.setReturnVariable = setReturnVariable
        }

    /**
     * sets if tag has a body or not
     *
     * @param hasBody
     */
    fun hasBody(hasBody: Boolean) {
        data.hasBody = hasBody
    }

    companion object {
        private val SQL_PARAMETERS: Collection.Key? = KeyImpl.getInstance("sqlparameters")
        private val CFQUERY: Collection.Key? = KeyImpl.getInstance("cfquery")
        private val GENERATEDKEY: Collection.Key? = KeyImpl.getInstance("generatedKey")
        private val MAX_RESULTS: Collection.Key? = KeyImpl.getInstance("maxResults")
        private val TIMEOUT: Collection.Key? = KeyConstants._timeout
        const val RETURN_TYPE_UNDEFINED = 0
        const val RETURN_TYPE_QUERY = 1
        const val RETURN_TYPE_ARRAY = 2
        const val RETURN_TYPE_STRUCT = 3
        const val RETURN_TYPE_STORED_PROC = 4
        @Throws(ApplicationException::class)
        private fun toReturnType(strReturntype: String?): Int {
            var strReturntype = strReturntype
            strReturntype = strReturntype.toLowerCase().trim()
            if (strReturntype.equals("query")) return RETURN_TYPE_QUERY
            if (strReturntype.equals("struct")) return RETURN_TYPE_STRUCT
            if (strReturntype.equals("array") || strReturntype.equals("array_of_struct") || strReturntype.equals("array-of-struct") || strReturntype.equals("arrayofstruct")
                    || strReturntype.equals("array_of_entity") || strReturntype.equals("array-of-entity") || strReturntype.equals("arrayofentities")
                    || strReturntype.equals("array_of_entities") || strReturntype.equals("array-of-entities") || strReturntype.equals("arrayofentities")) {
                return RETURN_TYPE_ARRAY
            }
            throw ApplicationException("Attribute [returntype] of tag [query] has an invalid value", "valid values are [query,array] but value was [$strReturntype]")
        }

        @Throws(ApplicationException::class)
        fun toReturnType(rt: Int): String? {
            if (RETURN_TYPE_QUERY == rt) return "query"
            if (RETURN_TYPE_STRUCT == rt) return "struct"
            return if (RETURN_TYPE_ARRAY == rt) "array" else "undefined"
        }

        @Throws(PageException::class)
        fun toDatasource(pageContext: PageContext?, datasource: Object?): DataSource? {
            return if (Decision.isStruct(datasource)) {
                AppListenerUtil.toDataSource(pageContext.getConfig(), "__temp__", Caster.toStruct(datasource), ThreadLocalPageContext.getLog(pageContext, "application"))
            } else if (Decision.isString(datasource)) {
                pageContext.getDataSource(Caster.toString(datasource))
            } else {
                throw ApplicationException("Attribute [datasource] must be datasource name or a datasource definition(struct)")
            }
        }

        private fun getName(data: QueryBean?): String? {
            return if (data.name == null) "query" else data.name
        }

        @Throws(PageException::class)
        fun _doEndTag(pageContext: PageContext?, data: QueryBean?, strSQL: String?, tl: TemplateLine?, setVars: Boolean): Int {

            // listener before
            var strSQL = strSQL
            if (data.listener != null) {
                val res = writeBackArgs(pageContext, data, data.listener.before(pageContext, createArgStruct(data, strSQL, tl)))
                if (!StringUtil.isEmpty(res)) strSQL = res
            }
            var sqlQuery: SQL? = null
            var exe: Long = 0
            try {
                // cannot use attribute params and queryparam tag

                // create SQL
                sqlQuery = if (data.params != null) {
                    if (data.params is Argument) QueryParamConverter.convert(strSQL, data.params as Argument) else if (Decision.isArray(data.params)) QueryParamConverter.convert(strSQL, Caster.toArray(data.params)) else if (Decision.isStruct(data.params)) QueryParamConverter.convert(strSQL, Caster.toStruct(data.params)) else throw DatabaseException("Value of the attribute [params] has to be a Struct or an Array", null, null, null)
                } else {
                    if (data.items.isEmpty()) SQLImpl(strSQL) else SQLImpl(strSQL, data.items.toArray(arrayOfNulls<SQLItem?>(data.items.size())))
                }
                validate(sqlQuery)

                // tachyon.runtime.type.Query query=null;
                var queryResult: QueryResult? = null
                var cacheHandlerId: String? = null
                var cacheId: String? = null
                val now: Long = System.currentTimeMillis()
                if (data.cachedAfter != null) {
                    // not yet
                    if (data.cachedAfter.getTime() > now) data.cachedWithin = null else if (data.cachedWithin == null) {
                        data.cachedWithin = (pageContext as PageContextImpl?).getCachedAfterTimeRange()
                    }
                }
                val useCache = data.cachedWithin != null || data.cachedAfter != null
                var cacheHandler: CacheHandler? = null
                if (useCache) {
                    cacheId = CacheHandlerCollectionImpl.createId(sqlQuery, if (data.datasource != null) data.datasource.getName() else null, data.username, data.password, data.returntype,
                            data.maxrows)
                    val coll: CacheHandlerCollectionImpl = pageContext.getConfig().getCacheHandlerCollection(Config.CACHE_TYPE_QUERY, null) as CacheHandlerCollectionImpl
                    cacheHandler = coll.getInstanceMatchingObject(data.cachedWithin, null)
                    if (cacheHandler == null && data.cachedAfter != null) cacheHandler = coll.getTimespanInstance(null)
                    if (cacheHandler != null) {
                        cacheHandlerId = cacheHandler.id() // cacheHandlerId specifies to queryResult the cacheType and
                        // therefore whether the query is cached or not
                        if (cacheHandler is CacheHandlerPro) {
                            var cacheItem: CacheItem?
                            try {
                                cacheItem = (cacheHandler as CacheHandlerPro?).get(pageContext, cacheId, if (data.cachedWithin != null) data.cachedWithin else data.cachedAfter)
                            } catch (pe: PageException) {
                                cacheItem = null
                                LogUtil.log(pageContext, "query", pe)
                            }
                            if (cacheItem is QueryResultCacheItem) queryResult = (cacheItem as QueryResultCacheItem?).getQueryResult()
                        } else { // FUTURE this else block can be removed when all cache handlers implement
                            // CacheHandlerPro
                            var cacheItem: CacheItem?
                            try {
                                cacheItem = cacheHandler.get(pageContext, cacheId)
                            } catch (pe: PageException) {
                                cacheItem = null
                                LogUtil.log(pageContext, "query", pe)
                            }
                            if (cacheItem is QueryResultCacheItem) {
                                val queryCachedItem: QueryResultCacheItem? = cacheItem as QueryResultCacheItem?
                                val cacheLimit: Date = data.cachedAfter
                                /*
							 * if(cacheLimit == null && cacheHandler in) { TimeSpan ts = Caster.toTimespan(cachedWithin,null);
							 * cacheLimit = new Date(System.currentTimeMillis() - Caster.toTimeSpan(cachedWithin).getMillis());
							 * }
							 */if (cacheLimit == null || queryCachedItem.isCachedAfter(cacheLimit)) queryResult = queryCachedItem.getQueryResult()
                            }
                        }
                    } else {
                        val patterns: List<String?> = pageContext.getConfig().getCacheHandlerCollection(Config.CACHE_TYPE_QUERY, null).getPatterns()
                        throw ApplicationException("Cachedwithin value [" + data.cachedWithin.toString() + "] is invalid, valid values are for example [" + ListUtil.listToList(patterns, ", ").toString() + "]")
                    }
                    // query=pageContext.getQueryCache().getQuery(pageContext,sql,datasource!=null?datasource.getName():null,username,password,cachedafter);
                }

                // cache not found, process and cache result if needed
                if (queryResult == null) {
                    // QoQ
                    if ("query".equals(data.dbtype)) {
                        val q: QueryImpl? = executeQoQ(pageContext, data, sqlQuery, tl)
                        q.setTemplateLine(tl)
                        queryResult = if (data.returntype === RETURN_TYPE_ARRAY) QueryArray.toQueryArray(q) // TODO this should be done in queryExecute
                        else if (data.returntype === RETURN_TYPE_STRUCT) {
                            if (data.columnName == null) throw ApplicationException("Attribute [columnKey] is required when return type is set to struct")
                            QueryStruct.toQueryStruct(q, data.columnName) // TODO this should be done in
                            // queryExecute itself so we not
                            // have to convert // afterwards
                        } else q
                    } else {
                        val start: Long = System.nanoTime()
                        val obj: Object
                        obj = if ("orm".equals(data.dbtype) || "hql".equals(data.dbtype)) executeORM(pageContext, data, sqlQuery, data.returntype, data.ormoptions) else executeDatasoure(pageContext, data, sqlQuery, data.result != null, pageContext.getTimeZone(), tl)
                        if (obj is QueryResult) {
                            queryResult = obj as QueryResult
                        } else {
                            if (data.setReturnVariable) {
                                data.rtn = obj
                            } else if (!StringUtil.isEmpty(data.name)) {
                                if (setVars) pageContext.setVariable(data.name, obj)
                            }
                            if (data.result != null) {
                                val time: Long = System.nanoTime() - start
                                val sct: Struct = StructImpl()
                                sct.setEL(KeyConstants._cached, Boolean.FALSE)
                                sct.setEL(KeyConstants._executionTime, Caster.toDouble(time / 1000000))
                                sct.setEL(KeyConstants._executionTimeNano, Caster.toDouble(time))
                                sct.setEL(KeyConstants._SQL, sqlQuery.getSQLString())
                                if (!Decision.isArray(obj)) sct.setEL(KeyConstants._RECORDCOUNT, Caster.toDouble(1))
                                if (setVars) pageContext.setVariable(data.result, sct)
                            } else setExecutionTime(pageContext, (System.nanoTime() - start) / 1000000)
                            return EVAL_PAGE
                        }
                    }
                    // else query=executeDatasoure(sql,result!=null,pageContext.getTimeZone());
                    if (data.cachedWithin != null && (data.cachedAfter == null || data.cachedAfter.getTime() <= now)) {
                        val cacheItem: CacheItem = QueryResultCacheItem.newInstance(queryResult, data.tags, data.datasource, null)
                        if (cacheItem != null) {
                            try {
                                cacheHandler.set(pageContext, cacheId, data.cachedWithin, cacheItem)
                            } catch (pe: PageException) {
                                LogUtil.log(pageContext, "query", pe)
                            }
                        }
                    }
                    exe = queryResult.getExecutionTime()
                } else {
                    queryResult.setCacheType(cacheHandlerId)
                }
                if (pageContext.getConfig().debug() && data.debug) {
                    val di: DebuggerImpl = pageContext.getDebugger() as DebuggerImpl
                    val logdb: Boolean = (pageContext.getConfig() as ConfigPro).hasDebugOptions(ConfigPro.DEBUG_DATABASE)
                    if (logdb) {
                        val debugUsage: Boolean = DebuggerImpl.debugQueryUsage(pageContext, queryResult)
                        di.addQuery(if (debugUsage) queryResult else null, if (data.datasource != null) data.datasource.getName() else null, data.name, sqlQuery, queryResult.getRecordcount(), tl,
                                exe)
                    } else {
                        di.addQuery(exe)
                    }
                }
                var setResult = false
                if (data.setReturnVariable) {
                    data.rtn = queryResult
                } else if (queryResult.getColumncount() + queryResult.getRecordcount() > 0 && !StringUtil.isEmpty(data.name)) {
                    if (setVars) pageContext.setVariable(data.name, queryResult)
                    setResult = true
                }

                // Result
                val meta: Struct? = createMetaData(pageContext, data, queryResult, sqlQuery, setVars, exe)

                // listener
                (pageContext.getConfig() as ConfigWebPro).getActionMonitorCollector().log(pageContext, "query", "Query", exe, queryResult)
                if (data.listener != null) {
                    callAfter(pageContext, data, strSQL, tl, setResult, queryResult, meta, setVars)
                }

                // log
                val log: Log = ThreadLocalPageContext.getLog(pageContext, "datasource")
                if (log.getLogLevel() >= Log.LEVEL_INFO) {
                    log.info("query tag", "executed [" + sqlQuery.toString().trim().toString() + "] in " + DecimalFormat.call(pageContext, exe / 1000000.0).toString() + " ms")
                }
            } catch (pe: PageException) {
                if (data.listener != null && data.listener.hasError()) {
                    val addExe: Long = System.nanoTime()
                    val args: Struct? = createArgStruct(data, strSQL, tl)
                    args.set(KeyConstants._exception, CatchBlockImpl(pe))
                    val rm = writeBackResult(pageContext, data, data.listener.error(pageContext, args), setVars)
                    if (data.result == null || rm!!.meta == null && rm.asQueryResult() != null) rm!!.meta = createMetaData(pageContext, data, rm.asQueryResult(), null, setVars, exe + (System.nanoTime() - addExe))
                    callAfter(pageContext, data, strSQL, tl, true, rm.res, rm.meta, setVars)
                } else throw pe
            } finally {
                (pageContext as PageContextImpl?).setTimestampWithTSOffset(data.previousLiteralTimestampWithTSOffset)
                if (data.tmpTZ != null) {
                    pageContext.setTimeZone(data.tmpTZ)
                }
            }
            return EVAL_PAGE
        }

        @Throws(PageException::class)
        private fun validate(sql: SQL?) {
            val items: Array<SQLItem?> = sql.getItems() ?: return
            for (item in items) {
                val _item: SQLItemImpl? = item as SQLItemImpl?
                QueryParam.check(item.getValue(), item.getType(), _item.getMaxlength(), _item.getCharset())
            }
        }

        @Throws(PageException::class)
        private fun createMetaData(pageContext: PageContext?, data: QueryBean?, queryResult: QueryResult?, sqlQuery: SQL?, setVars: Boolean, exe: Long): Struct? {
            val meta: Struct?
            if (data.result != null && queryResult != null) {
                meta = StructImpl()
                meta.setEL(KeyConstants._cached, Caster.toBoolean(queryResult.isCached()))
                if (queryResult.getColumncount() + queryResult.getRecordcount() > 0) {
                    val list: String = ListUtil.arrayToList(if (queryResult is tachyon.runtime.type.Query) (queryResult as tachyon.runtime.type.Query?).getColumnNamesAsString() else CollectionUtil.toString(queryResult.getColumnNames(), false), ",")
                    meta.setEL(KeyConstants._COLUMNLIST, list)
                }
                var rc: Int = queryResult.getRecordcount()
                if (rc == 0) rc = queryResult.getUpdateCount()
                meta.setEL(KeyConstants._RECORDCOUNT, Caster.toDouble(rc))
                meta.setEL(KeyConstants._executionTime, Caster.toDouble(queryResult.getExecutionTime() / 1000000))
                meta.setEL(KeyConstants._executionTimeNano, Caster.toDouble(queryResult.getExecutionTime()))
                if (sqlQuery != null) meta.setEL(KeyConstants._SQL, sqlQuery.getSQLString())

                // GENERATED KEYS
                val qi: tachyon.runtime.type.Query = Caster.toQuery(queryResult, null)
                if (qi != null) {
                    val qryKeys: tachyon.runtime.type.Query = qi.getGeneratedKeys()
                    if (qryKeys != null) {
                        val generatedKey = StringBuilder()
                        var sb: StringBuilder?
                        val columnNames: Array<Collection.Key?> = qryKeys.getColumnNames()
                        var column: QueryColumn
                        for (c in columnNames.indices) {
                            column = qryKeys.getColumn(columnNames[c])
                            sb = StringBuilder()
                            val size: Int = column.size()
                            for (row in 1..size) {
                                if (row > 1) sb.append(',')
                                sb.append(Caster.toString(column.get(row, null)))
                            }
                            if (sb.length() > 0) {
                                meta.setEL(columnNames[c], sb.toString())
                                if (generatedKey.length() > 0) generatedKey.append(',')
                                generatedKey.append(sb)
                            }
                        }
                        if (generatedKey.length() > 0) meta.setEL(GENERATEDKEY, generatedKey.toString())
                    }
                }

                // sqlparameters
                if (sqlQuery != null) {
                    val params: Array<SQLItem?> = sqlQuery.getItems()
                    if (params != null && params.size > 0) {
                        val arr: Array = ArrayImpl()
                        meta.setEL(SQL_PARAMETERS, arr)
                        for (i in params.indices) {
                            arr.append(params[i].getValue())
                        }
                    }
                }
                if (setVars) pageContext.setVariable(data.result, meta)
            } else {
                meta = setExecutionTime(pageContext, exe / 1000000)
            }
            return meta
        }

        @Throws(PageException::class)
        private fun callAfter(pc: PageContext?, data: QueryBean?, strSQL: String?, tl: TemplateLine?, setResult: Boolean, queryResult: Object?, meta: Object?, setVars: Boolean) {
            val args: Struct? = createArgStruct(data, strSQL, tl)
            if (setResult && queryResult != null) args.set(KeyConstants._result, queryResult)
            if (meta != null) args.set(KeyConstants._meta, meta)
            writeBackResult(pc, data, data.listener.after(pc, args), setVars)
        }

        @Throws(PageException::class)
        private fun createArgStruct(data: QueryBean?, strSQL: String?, tl: TemplateLine?): Struct? {
            val rtn: Struct = StructImpl(Struct.TYPE_LINKED)
            val args: Struct = StructImpl(Struct.TYPE_LINKED)

            // TODO add missing attrs
            /*
		 * TagLibTag tlt = TagUtil.getTagLibTag(pageContext, CFMLEngine.DIALECT_CFML, "cf", "query");
		 * Iterator<Entry<String, TagLibTagAttr>> it = tlt.getAttributes().entrySet().iterator();
		 * Entry<String, TagLibTagAttr> e; while(it.hasNext()) { e=it.next(); e.getValue().get(this); }
		 */set(args, "cachedAfter", data.cachedAfter)
            set(args, "cachedWithin", data.cachedWithin)
            if (data.columnName != null) set(args, "columnName", data.columnName.getString())
            set(args, KeyConstants._datasource, data.rawDatasource)
            set(args, "dbtype", data.dbtype)
            set(args, KeyConstants._debug, data.debug)
            if (data.maxrows >= 0) set(args, "maxrows", data.maxrows)
            set(args, KeyConstants._name, data.name)
            set(args, "ormoptions", data.ormoptions)
            set(args, KeyConstants._username, data.username)
            set(args, KeyConstants._password, data.password)
            set(args, KeyConstants._result, data.result)
            set(args, KeyConstants._returntype, toReturnType(data.returntype))
            set(args, KeyConstants._timeout, data.timeout)
            set(args, KeyConstants._timezone, data.timezone)
            set(args, "unique", data.unique)
            set(args, KeyConstants._sql, strSQL)
            rtn.setEL(KeyConstants._args, args)

            // params
            if (data.params != null) {
                set(args, "params", data.params)
            } else if (data.items != null) {
                val params: Array = ArrayImpl()
                val it: Iterator<SQLItem?> = data.items.iterator()
                var item: SQLItem?
                while (it.hasNext()) {
                    item = it.next()
                    params.appendEL(QueryParamConverter.toStruct(item))
                }
                set(args, KeyConstants._params, params)
            }
            rtn.setEL(KeyConstants._caller, tl.toStruct())
            return rtn
        }

        @Throws(PageException::class)
        private fun writeBackArgs(pageContext: PageContext?, data: QueryBean?, args: Struct?): String? {
            var args: Struct = args ?: return null

            // maybe they send the hole arguments scope, we handle this here
            if (args.size() === 2 && args.containsKey("caller") && args.containsKey("args")) args = Caster.toStruct(args.get("args"))

            // cachedAfter
            val dt: DateTime = Caster.toDate(args.get("cachedAfter", null), true, pageContext.getTimeZone(), null)
            if (dt != null && dt !== data.cachedAfter) data.cachedAfter = dt

            // cachedWithin
            var obj: Object = args.get("cachedWithin", null)
            if (obj != null && obj !== data.cachedWithin) data.cachedWithin = obj

            // columnName
            val k: Key = Caster.toKey(args.get("columnName", null), null)
            if (k != null && k !== data.columnName) data.columnName = k

            // datasource
            obj = args.get("datasource", null)
            if (obj != null && obj !== data.datasource) {
                data.rawDatasource = obj
                data.datasource = toDatasource(pageContext, obj)
            }

            // dbtype
            var str: String = Caster.toString(args.get("dbtype", null), null)
            if (str != null && str !== data.dbtype && !StringUtil.isEmpty(str)) data.dbtype = str

            // debug
            var b: Boolean = Caster.toBoolean(args.get("debug", null), null)
            if (b != null && b !== data.debug) data.debug = b.booleanValue()

            // lazy
            b = Caster.toBoolean(args.get("lazy", null), null)
            if (b != null && b !== data.lazy) data.lazy = b.booleanValue()

            // maxrows
            var i: Integer = Caster.toInteger(args.get("maxrows", null), null)
            if (i != null && i !== data.maxrows) {
                if (i.intValue() >= 0) data.maxrows = i.intValue()
            }

            // name
            str = Caster.toString(args.get("name", null), null)
            if (str != null && str !== data.name && !str.equals(data.name) && !StringUtil.isEmpty(str)) data.name = str

            // ormoptions
            val sct: Struct = Caster.toStruct(args.get("ormoptions", null), null)
            if (sct != null && sct !== data.ormoptions) data.ormoptions = sct

            // username
            str = Caster.toString(args.get("username", null), null)
            if (str != null && str !== data.username && !StringUtil.isEmpty(str)) data.username = str

            // password
            str = Caster.toString(args.get("password", null), null)
            if (str != null && str !== data.password && !StringUtil.isEmpty(str)) data.password = str

            // result
            str = Caster.toString(args.get("result", null), null)
            if (str != null && str !== data.result && !StringUtil.isEmpty(str)) data.result = str

            // returntype
            i = Caster.toInteger(args.get("returntype", null), null)
            if (i != null && i !== data.returntype) data.returntype = i.intValue()

            // timeout
            val ts: TimeSpan = Caster.toTimespan(args.get("timeout", null), null)
            if (ts != null && ts !== data.timeout) data.timeout = ts

            // timezone
            val tz: TimeZone = Caster.toTimeZone(args.get("timezone", null), null)
            if (tz != null && tz !== data.timeout) data.timezone = tz

            // params
            obj = args.get("params", null)
            if (obj != null && obj !== data.params) {
                data.params = obj
                data.items.clear()
            }

            // sql
            var sql: String? = null
            str = Caster.toString(args.get("sql", null), null)
            if (str != null && !StringUtil.isEmpty(str)) sql = str
            return sql
        }

        @Throws(PageException::class)
        private fun writeBackResult(pageContext: PageContext?, data: QueryBean?, args: Struct?, setVars: Boolean): ResMeta? {
            val rm = ResMeta()
            if (args == null) return rm

            // result
            rm.res = args.get(KeyConstants._result, null)
            if (rm.res != null) {
                if (!StringUtil.isEmpty(data.name) && setVars) pageContext.setVariable(data.name, rm.res)
            }
            // meta
            rm.meta = args.get(KeyConstants._meta, null)
            if (rm.meta != null) {
                if (StringUtil.isEmpty(data.result)) pageContext.undefinedScope().setEL(CFQUERY, rm.meta) else {
                    if (setVars) pageContext.setVariable(data.result, rm.meta)
                }
            }
            return rm
        }

        @Throws(PageException::class)
        private operator fun set(args: Struct?, name: String?, value: Object?) {
            if (value != null) args.set(name, value)
        }

        @Throws(PageException::class)
        private operator fun set(args: Struct?, name: Key?, value: Object?) {
            if (value != null) args.set(name, value)
        }

        private fun setExecutionTime(pc: PageContext?, exe: Long): Struct? {
            val sct: Struct = StructImpl()
            sct.setEL(KeyConstants._executionTime, Double.valueOf(exe))
            pc.undefinedScope().setEL(CFQUERY, sct)
            return sct
        }

        @Throws(PageException::class)
        private fun executeORM(pageContext: PageContext?, data: QueryBean?, sql: SQL?, returnType: Int, ormoptions: Struct?): Object? {
            var ormoptions: Struct? = ormoptions
            val session: ORMSession = ORMUtil.getSession(pageContext)
            if (ormoptions == null) ormoptions = StructImpl()
            var dsn: String? = null
            if (ormoptions != null) dsn = Caster.toString(ormoptions.get(KeyConstants._datasource, null), null)
            if (StringUtil.isEmpty(dsn, true)) dsn = ORMUtil.getDefaultDataSource(pageContext).getName()

            // params
            val _items: Array<SQLItem?> = sql.getItems()
            val params: Array = ArrayImpl()
            for (i in _items.indices) {
                params.appendEL(_items[i])
            }

            // query options
            if (data.maxrows !== -1 && !ormoptions.containsKey(MAX_RESULTS)) ormoptions.setEL(MAX_RESULTS, Double.valueOf(data.maxrows))
            if (data.timeout != null && data.timeout.getSeconds() as Int > 0 && !ormoptions.containsKey(TIMEOUT)) ormoptions.setEL(TIMEOUT, Double.valueOf(data.timeout.getSeconds()))
            /*
		 * MUST offset: Specifies the start index of the resultset from where it has to start the retrieval.
		 * cacheable: Whether the result of this query is to be cached in the secondary cache. Default is
		 * false. cachename: Name of the cache in secondary cache.
		 */
            val res: Object = session.executeQuery(pageContext, dsn, sql.getSQLString(), params, data.unique, ormoptions)
            return if (returnType == RETURN_TYPE_ARRAY || returnType == RETURN_TYPE_UNDEFINED) res else session.toQuery(pageContext, res, null)
        }

        @Throws(PageException::class)
        fun _call(pc: PageContext?, hql: String?, params: Object?, unique: Boolean, queryOptions: Struct?): Object? {
            val session: ORMSession = ORMUtil.getSession(pc)
            var dsn: String = Caster.toString(queryOptions.get(KeyConstants._datasource, null), null)
            if (StringUtil.isEmpty(dsn, true)) dsn = ORMUtil.getDefaultDataSource(pc).getName()
            return if (Decision.isCastableToArray(params)) session.executeQuery(pc, dsn, hql, Caster.toArray(params), unique, queryOptions) else if (Decision.isCastableToStruct(params)) session.executeQuery(pc, dsn, hql, Caster.toStruct(params), unique, queryOptions) else session.executeQuery(pc, dsn, hql, params as Array?, unique, queryOptions)
        }

        @Throws(PageException::class)
        private fun executeQoQ(pc: PageContext?, data: QueryBean?, sql: SQL?, tl: TemplateLine?): tachyon.runtime.type.QueryImpl? {
            return try {
                HSQLDBHandler().execute(pc, sql, data.maxrows, data.blockfactor, data.timeout)
            } catch (e: Exception) {
                throw Caster.toPageException(e)
            }
        }

        @Throws(PageException::class)
        private fun executeDatasoure(pageContext: PageContext?, data: QueryBean?, sql: SQL?, createUpdateData: Boolean, tz: TimeZone?, tl: TemplateLine?): QueryResult? {
            val manager: DatasourceManagerImpl = pageContext.getDataSourceManager() as DatasourceManagerImpl
            val dc: DatasourceConnection = manager.getConnection(pageContext, data.datasource, data.username, data.password)
            return try {
                if (data.lazy && !createUpdateData && data.cachedWithin == null && data.cachedAfter == null && data.result == null) {
                    if (data.returntype !== RETURN_TYPE_QUERY && data.returntype !== RETURN_TYPE_UNDEFINED) throw DatabaseException("Only return type [query] is allowed when [lazy] is set to true", null, sql, dc)
                    return SimpleQuery(pageContext, dc, sql, data.maxrows, data.blockfactor, data.timeout, getName(data), tl, tz)
                }
                if (data.returntype === RETURN_TYPE_ARRAY) {
                    return QueryImpl.toArray(pageContext, dc, sql, data.maxrows, data.blockfactor, data.timeout, getName(data), tl, createUpdateData, true)
                }
                if (data.returntype === RETURN_TYPE_STRUCT) {
                    QueryImpl.toStruct(pageContext, dc, sql, data.columnName, data.maxrows, data.blockfactor, data.timeout, getName(data), tl, createUpdateData, true)
                } else QueryImpl(pageContext, dc, sql, data.maxrows, data.blockfactor, data.timeout, getName(data), tl, createUpdateData, true, data.indexName)
            } finally {
                manager.releaseConnection(pageContext, dc)
            }
        }

        fun toTagListener(listener: Object?, defaultValue: TagListener?): TagListener? {
            if (listener is TagListener) return listener as TagListener?
            if (listener is Component) return ComponentTagListener(listener as Component?)
            if (listener is UDF) return UDFTagListener(null, listener as UDF?, null)
            if (listener is Struct) {
                val before: UDF = Caster.toFunction((listener as Struct?).get("before", null), null)
                val after: UDF = Caster.toFunction((listener as Struct?).get("after", null), null)
                val error: UDF = Caster.toFunction((listener as Struct?).get("error", null), null)
                return UDFTagListener(before, after, error)
            }
            return defaultValue
        }

        fun toTemplateLine(config: Config?, sourceTemplate: String?, ps: PageSource?): TemplateLine? {
            if (!StringUtil.isEmpty(sourceTemplate)) {
                return TemplateLine(sourceTemplate)
            }
            if (config.debug() || ps == null) {
                val rtn: TemplateLine = SystemUtil.getCurrentContext(null)
                if (rtn != null) return rtn
            }
            return TemplateLine(ps.getDisplayPath())
        }

        @Throws(ApplicationException::class)
        fun toTagListener(listener: Object?): TagListener? {
            val ql: TagListener? = toTagListener(listener, null)
            if (ql != null) return ql
            throw ApplicationException("Cannot convert [" + Caster.toTypeName(listener).toString() + "] to a listener")
        }
    }
}