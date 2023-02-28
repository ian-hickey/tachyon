/**
 * Copyright (c) 2014, the Railo Company Ltd.
 * Copyright (c) 2015, Lucee Assosication Switzerland
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
package lucee.runtime.tag

import java.lang.ref.SoftReference

class StoredProc : BodyTagTryCatchFinallySupport() {
    companion object {
        // private static final int PROCEDURE_CAT=1;
        // private static final int PROCEDURE_SCHEM=2;
        // private static final int PROCEDURE_NAME=3;
        // private static final int COLUMN_NAME=4;
        private const val COLUMN_TYPE = 5
        private const val DATA_TYPE = 6
        private const val TYPE_NAME = 7

        // |PRECISION|LENGTH|SCALE|RADIX|NULLABLE|REMARKS|SEQUENCE|OVERLOAD|DEFAULT_VALUE
        private val KEY_SC: lucee.runtime.type.Collection.Key? = KeyImpl.getInstance("StatusCode")
        private val COUNT: lucee.runtime.type.Collection.Key? = KeyImpl.getInstance("count_afsdsfgdfgdsfsdfsgsdgsgsdgsasegfwef")
        private val STATUS_CODE: ProcParamBean? = null
        private val STATUSCODE: lucee.runtime.type.Collection.Key? = KeyImpl.getInstance("StatusCode")
        private operator fun get(params: List<ProcParamBean?>?, index: Int): ProcParamBean? {
            return try {
                params!![index]
            } catch (t: Throwable) {
                ExceptionUtil.rethrowIfNecessary(t)
                null
            }
        }

        init {
            STATUS_CODE = ProcParamBean()
            STATUS_CODE.setType(Types.INTEGER)
            STATUS_CODE.setDirection(ProcParamBean.DIRECTION_OUT)
            STATUS_CODE.setVariable("cfstoredproc.statusCode")
        }
    }

    private val params: List<ProcParamBean?>? = ArrayList<ProcParamBean?>()
    private val results: Array? = ArrayImpl()
    private var procedure: String? = null
    private var datasource: DataSource? = null
    private var username: String? = null
    private var password: String? = null
    private var blockfactor = -1
    private var timeout = -1
    private var debug = true
    private var returncode = false
    private var result: String? = "cfstoredproc"
    private var cachedafter: DateTime? = null
    private var returnValue: ProcParamBean? = null
    private var cachedWithin: Object? = null

    // private Map<String,ProcMetaCollection> procedureColumnCache;
    @Override
    fun release() {
        params.clear()
        results.clear()
        returnValue = null
        procedure = null
        datasource = null
        username = null
        password = null
        blockfactor = -1
        timeout = -1
        debug = true
        returncode = false
        result = "cfstoredproc"
        cachedWithin = null
        cachedafter = null
        // cachename="";
        super.release()
    }

    /**
     * set the value cachedafter This is the age of which the query data can be
     *
     * @param cachedafter value to set
     */
    fun setCachedafter(cachedafter: DateTime?) {
        // lucee.print.ln("cachedafter:"+cachedafter);
        this.cachedafter = cachedafter
    }

    /**
     * set the value cachename This is specific to JTags, and allows you to give the cache a specific
     * name
     *
     * @param cachename value to set
     */
    fun setCachename(cachename: String?) {
        // DeprecatedUtil.tagAttribute(pageContext,"StoredProc", "cachename");
    }

    /**
     * set the value cachedwithin
     *
     * @param cachedwithin value to set
     */
    fun setCachedwithin(cachedwithin: Object?) {
        if (StringUtil.isEmpty(cachedwithin)) return
        cachedWithin = cachedwithin
    }

    /**
     * @param blockfactor The blockfactor to set.
     */
    fun setBlockfactor(blockfactor: Double) {
        this.blockfactor = blockfactor.toInt()
    }

    /**
     * @param blockfactor
     */
    @Deprecated
    @Deprecated("replaced with setBlockfactor(double)")
    fun setBlockfactor(blockfactor: Int) {
        // DeprecatedUtil.tagAttribute(pageContext,"storedproc","blockfactor");
        this.blockfactor = blockfactor
    }

    /**
     * @param datasource The datasource to set.
     */
    @Throws(PageException::class)
    fun setDatasource(datasource: String?) {
        this.datasource = Query.toDatasource(pageContext, datasource)
    }

    @Throws(PageException::class)
    fun setDatasource(datasource: Object?) {
        this.datasource = Query.toDatasource(pageContext, datasource)
    }

    /**
     * @param username The username to set.
     */
    fun setUsername(username: String?) {
        this.username = username
    }

    /**
     * @param password The password to set.
     */
    fun setPassword(password: String?) {
        this.password = password
    }

    /**
     * @param debug The debug to set.
     */
    fun setDebug(debug: Boolean) {
        this.debug = debug
    }

    /**
     * @param procedure The procedure to set.
     */
    fun setProcedure(procedure: String?) {
        this.procedure = procedure
    }

    /**
     * @param result The result to set.
     */
    fun setResult(result: String?) {
        this.result = result
    }

    /**
     * @param returncode The returncode to set.
     */
    fun setReturncode(returncode: Boolean) {
        this.returncode = returncode
    }

    /**
     * @param dbvarname the dbvarname to set
     */
    fun setDbvarname(dbvarname: String?) {
        // DeprecatedUtil.tagAttribute(pageContext,"storedproc","dbvarname");
    }

    fun setDbtype(dbtype: String?) {
        // DeprecatedUtil.tagAttribute(pageContext,"storedproc","dbtype");
    }

    fun addProcParam(param: ProcParamBean?) {
        if (log.getLogLevel() >= Log.LEVEL_DEBUG) { // log entry added to troubleshoot LDEV-1147
            log.debug("StoredProc", String.format("  param [%s] %s = %s", SQLCaster.toStringType(param.getType(), "?"), param!!.getVariable(), param!!.getValue()))
        }
        params.add(param)
    }

    fun addProcResult(result: ProcResultBean?) {
        results.setEL(result.getResultset(), result)
    }

    @Override
    @Throws(JspException::class)
    fun doStartTag(): Int {

        // cache within
        if (StringUtil.isEmpty(cachedWithin)) {
            val tmp: Object = (pageContext as PageContextImpl?).getCachedWithin(ConfigWeb.CACHEDWITHIN_QUERY)
            if (tmp != null) setCachedwithin(tmp)
        }
        return EVAL_BODY_INCLUDE
    }

    @Throws(PageException::class)
    private fun createReturnValue(dc: DatasourceConnection?) {
        val conn: Connection = dc.getConnection()
        if (SQLUtil.isOracle(conn)) {
            val proc: String = procedure.trim().toUpperCase()
            /**
             * The procedure name can have 1, 2, or 3 dot delimited parts 1 part might be: PROC.OBJECT_NAME
             * SYNO.SYNONYM_NAME 2 parts might be: PROC.OWNER, PROC.OBJECT_NAME PROC.OBJECT_NAME,
             * PROC.PROCEDURE_NAME SYNO.SYNONYM_NAME, PROC.PROCEDURE_NAME 3 parts is: PROC.OWNER,
             * PROC.OBJECT_NAME, PROC.PROCEDURE_NAME
             */
            try {
                val ds: DataSourceSupport = dc.getDatasource() as DataSourceSupport
                val cacheTimeout: Long = ds.getMetaCacheTimeout()
                val procParamsCache: Map<String?, SoftReference<ProcMetaCollection?>?> = ds.getProcedureColumnCache()
                val numCfProcParams: Int = params!!.size()
                val cacheId: String = procedure.toLowerCase().toString() + "-" + numCfProcParams + "-" + ds.getUsername() // each user might see different procs
                val tmp: SoftReference<ProcMetaCollection?>? = procParamsCache[cacheId]
                var procParams: ProcMetaCollection? = if (tmp == null) null else tmp.get()
                if (procParams == null || cacheTimeout >= 0 && procParams.created + cacheTimeout < System.currentTimeMillis()) {
                    val owner: String? = null
                    val procName: String? = null
                    val name: String? = null
                    var sql: String? = null
                    val parts: Array<String?> = proc.split("\\.")
                    val params: List<String?> = ArrayList(6)
                    if (parts.size == 1) {
                        sql = """SELECT DISTINCT PROC.OWNER, PROC.OBJECT_NAME, PROC.PROCEDURE_NAME, null as SYN_OWNER, PROC.OBJECT_TYPE, PROC.OBJECT_ID 
    ,(SELECT COUNT(ARGS.IN_OUT) FROM ALL_ARGUMENTS ARGS WHERE ARGS.OBJECT_ID=PROC.OBJECT_ID) AS ARGS_COUNT 
    ,CASE PROC.OWNER WHEN USER THEN 1 
        WHEN 'PUBLIC' THEN 2 
        ELSE 3 END AS OWNER_ORDER 
FROM   ALL_PROCEDURES PROC 
WHERE  PROC.OBJECT_NAME = ? OR PROC.PROCEDURE_NAME = ? 
    UNION 
SELECT DISTINCT PROC.OWNER, PROC.OBJECT_NAME, PROC.PROCEDURE_NAME, SYN.OWNER as SYN_OWNER, PROC.OBJECT_TYPE, PROC.OBJECT_ID 
    ,(SELECT COUNT(ARGS.IN_OUT) FROM ALL_ARGUMENTS ARGS WHERE ARGS.OBJECT_ID=PROC.OBJECT_ID) AS ARGS_COUNT 
    ,CASE SYN.OWNER WHEN USER THEN 1 
        WHEN 'PUBLIC' THEN 2 
        ELSE 3 END AS OWNER_ORDER 
FROM ALL_PROCEDURES PROC JOIN ALL_SYNONYMS SYN ON SYN.TABLE_NAME=PROC.OBJECT_NAME 
WHERE SYN.SYNONYM_NAME = ? 
ORDER BY OWNER_ORDER, PROCEDURE_NAME DESC"""
                        params.add(parts[0])
                        params.add(parts[0])
                        params.add(parts[0])
                    } else if (parts.size == 2) {
                        sql = """SELECT DISTINCT	PROC.OWNER, PROC.OBJECT_NAME, PROC.PROCEDURE_NAME, PROC.OBJECT_TYPE, PROC.OBJECT_ID 
	,(SELECT COUNT(ARGS.IN_OUT) FROM ALL_ARGUMENTS ARGS WHERE ARGS.OBJECT_ID=PROC.OBJECT_ID) AS ARGS_COUNT 
FROM ALL_PROCEDURES PROC 
	LEFT JOIN ALL_SYNONYMS SYN ON PROC.OBJECT_NAME = SYN.TABLE_NAME 
WHERE (PROC.OWNER = ? AND PROC.OBJECT_NAME= ? AND PROC.OBJECT_TYPE='PROCEDURE') 
	OR (PROC.OBJECT_NAME = ? AND PROC.PROCEDURE_NAME = ? AND PROC.OBJECT_TYPE='PACKAGE') 
	OR (SYN.SYNONYM_NAME = ? AND PROC.PROCEDURE_NAME = ? AND PROC.OBJECT_TYPE='PACKAGE')"""
                        params.add(parts[0])
                        params.add(parts[1])
                        params.add(parts[0])
                        params.add(parts[1])
                        params.add(parts[0])
                        params.add(parts[1])
                    } else if (parts.size == 3) {
                        sql = """SELECT PROC.OWNER, PROC.OBJECT_NAME, PROC.PROCEDURE_NAME, PROC.OBJECT_TYPE, PROC.OBJECT_ID 
	,(SELECT COUNT(ARGS.IN_OUT) FROM ALL_ARGUMENTS ARGS WHERE ARGS.OBJECT_ID=PROC.OBJECT_ID) AS ARGS_COUNT 
FROM ALL_PROCEDURES PROC 
WHERE PROC.OWNER = ? 
	AND PROC.OBJECT_NAME = ? 
	AND PROC.PROCEDURE_NAME = ? 
	AND PROC.OBJECT_TYPE = 'PACKAGE'"""
                        params.add(parts[0])
                        params.add(parts[1])
                        params.add(parts[2])
                    }
                    val preparedStatement: PreparedStatement = conn.prepareStatement(sql)
                    var ix = 1
                    for (p in params) preparedStatement.setString(ix++, p)
                    val resultSet: ResultSet = preparedStatement.executeQuery()
                    if (resultSet.next()) {
                        val _owner: String = resultSet.getString(1) // OWNER
                        var _objName: String? = resultSet.getString(2) // OBJECT_NAME
                        var _procName: String? = resultSet.getString(3) // PROCEDURE_NAME
                        if (_procName == null && _objName != null) {
                            // when the PROC is not scoped the PROCEDURE_NAME is actually the OBJECT_NAME, see LDEV-1833
                            _procName = _objName
                            _objName = null
                        }
                        val procColumns: ResultSet = conn.getMetaData().getProcedureColumns(_objName, _owner, _procName, null)
                        procParams = getProcMetaCollection(procColumns)
                        if (procParams != null) procParamsCache.put(cacheId, SoftReference<ProcMetaCollection?>(procParams))
                        if (log.getLogLevel() >= Log.LEVEL_DEBUG) { // log entry added to troubleshoot LDEV-1147
                            log.debug("StoredProc", "PROC OBJECT_ID: " + resultSet.getInt("OBJECT_ID"))
                        }
                    } else {
                        if (log.getLogLevel() >= Log.LEVEL_INFO) log.info(StoredProc::class.java.getSimpleName(), "procedure $procedure not found in view ALL_PROCEDURES")
                    }
                }
                var colType: Int
                var index = -1
                if (procParams != null) {
                    val it: Iterator<ProcMeta?> = procParams.metas.iterator()
                    var pm: ProcMeta?
                    while (it.hasNext()) {
                        index++
                        pm = it.next()
                        colType = pm.columnType

                        // Return
                        if (colType == DatabaseMetaData.procedureColumnReturn) {
                            index--
                            val result: ProcResultBean? = firstResult
                            val param = ProcParamBean()
                            param.setType(pm.dataType)
                            param!!.setDirection(ProcParamBean.DIRECTION_OUT)
                            if (result != null) param!!.setVariable(result.getName())
                            returnValue = param
                        } else if (colType == DatabaseMetaData.procedureColumnOut || colType == DatabaseMetaData.procedureColumnInOut) {
                            // review of the code: seems to add an additional column in this case
                            if (pm.dataType === CFTypes.CURSOR) {
                                val result: ProcResultBean? = firstResult
                                val param = ProcParamBean()
                                param.setType(pm.dataType)
                                param!!.setDirection(ProcParamBean.DIRECTION_OUT)
                                if (result != null) param!!.setVariable(result.getName())
                                if (params.size() < index) {
                                    val message = ("Params passed are [" + paramTypesPassed + "] but the procedure/function expects ["
                                            + ProcMetaCollection.getParamTypeList(procParams.metas) + "]")
                                    throw DatabaseException(message, null, null, dc)
                                } else if (params.size() === index) params.add(param) else params.add(index, param)
                            } else {
                                var param: ProcParamBean? = null
                                if (params.size() > index) param = params[index]
                                if (param != null && pm.dataType !== Types.OTHER && pm.dataType !== param.getType()) {
                                    param.setType(pm.dataType)
                                }
                            }
                        } else if (colType == DatabaseMetaData.procedureColumnIn) {
                            val param: ProcParamBean? = Companion[params, index]
                            if (param != null && pm.dataType !== Types.OTHER && pm.dataType !== param.getType()) {
                                param.setType(pm.dataType)
                            }
                        }
                    }
                }
                contractTo(params, index + 1)
                // if(res!=null)print.out(new QueryImpl(res,"columns").toString());
            } catch (e: SQLException) {
                throw DatabaseException(e, dc)
            }
        }
        if (returncode) {
            returnValue = STATUS_CODE
        }
    }

    private fun contractTo(params: List<ProcParamBean?>?, paramCount: Int) {
        if (params!!.size() > paramCount) {
            for (i in params.size() - 1 downTo paramCount) {
                params.remove(i)
            }
        }
    }

    /**
     * @param rsProcColumns the result from DatabaseMetaData.getProcedureColumns()
     */
    @Throws(SQLException::class)
    private fun getProcMetaCollection(rsProcColumns: ResultSet?): ProcMetaCollection? {
        /*
		 * try { print.out(new QueryImpl(rsProcColumns, "q", pageContext.getTimeZone())); } catch
		 * (PageException e) {} //
		 */
        val allProcs: Map<String?, List<ProcMeta?>?> = HashMap()
        try {
            while (rsProcColumns.next()) {
                val schem: String = rsProcColumns.getString("PROCEDURE_SCHEM")
                val cat: String = rsProcColumns.getString("PROCEDURE_CAT")
                val name: String = rsProcColumns.getString("PROCEDURE_NAME")
                val fqProcName = (if (schem == null) "" else "$schem.") + (if (cat == null) "" else "$cat.") + name
                val lpm: List<ProcMeta?> = allProcs.computeIfAbsent(fqProcName) { p -> ArrayList() }
                lpm.add(ProcMeta(rsProcColumns.getInt(COLUMN_TYPE), getDataType(rsProcColumns)))
            }
        } finally {
            IOUtil.close(rsProcColumns)
        }
        if (log.getLogLevel() >= Log.LEVEL_DEBUG) {
            val sb = StringBuilder(64)
            val it: Iterator<Entry<String?, List<ProcMeta?>?>?> = allProcs.entrySet().iterator()
            while (it.hasNext()) {
                val e: Entry<String?, List<ProcMeta?>?>? = it.next()
                sb.append(if (sb.length() === 0) "Identified procedures: " else ", ")
                sb.append('{')
                sb.append(e.getKey())
                sb.append("(")
                sb.append(ProcMetaCollection.getParamTypeList(e.getValue()))
                sb.append(")}")
            }
            log.debug("StoredProc", sb.toString())
        }
        var result: ProcMetaCollection? = null
        val it: Iterator<Entry<String?, List<ProcMeta?>?>?> = allProcs.entrySet().iterator()
        while (it.hasNext()) {
            val e: Entry<String?, List<ProcMeta?>?>? = it.next()
            result = ProcMetaCollection(e.getKey(), e.getValue())
            break // TODO: should we try to find best match according to params if there is more than one match?
        }
        return result
    }

    @Throws(SQLException::class)
    private fun getDataType(res: ResultSet?): Int {
        var dataType: Int = res.getInt(DATA_TYPE)
        if (dataType == Types.OTHER) {
            val strDataType: String = res.getString(TYPE_NAME)
            if ("REF CURSOR".equalsIgnoreCase(strDataType)) dataType = CFTypes.CURSOR
            if ("CLOB".equalsIgnoreCase(strDataType)) dataType = Types.CLOB
            if ("BLOB".equalsIgnoreCase(strDataType)) dataType = Types.BLOB
        }
        return dataType
    }

    private val firstResult: lucee.runtime.tag.ProcResultBean?
        private get() {
            val it: Iterator<Key?> = results.keyIterator()
            return if (!it.hasNext()) null else results.removeEL(it.next())
        }

    @Override
    @Throws(PageException::class)
    fun doEndTag(): Int {
        val startNS: Long = System.nanoTime()
        var ds: Object? = datasource
        if (datasource == null) {
            ds = pageContext.getApplicationContext().getDefDataSource()
            if (StringUtil.isEmpty(ds)) {
                val isCFML = pageContext.getRequestDialect() === CFMLEngine.DIALECT_CFML
                throw ApplicationException("attribute [datasource] is required, when no default datasource is defined",
                        ("you can define a default datasource as attribute [defaultdatasource] of the tag "
                                + if (isCFML) Constants.CFML_APPLICATION_TAG_NAME else Constants.LUCEE_APPLICATION_TAG_NAME) + " or as data member of the "
                                + (if (isCFML) Constants.CFML_APPLICATION_EVENT_HANDLER else Constants.LUCEE_APPLICATION_EVENT_HANDLER).toString() + " (this.defaultdatasource=\"mydatasource\";)")
            }
        }
        val res: Struct = StructImpl()
        val manager: DataSourceManager = pageContext.getDataSourceManager()
        val dc: DatasourceConnection = if (ds is DataSource) manager.getConnection(pageContext, ds as DataSource?, username, password) else manager.getConnection(pageContext, Caster.toString(ds), username, password)
        createReturnValue(dc)
        val sql = createSQL()

        // add returnValue to params
        if (returnValue != null) {
            params.add(0, returnValue)
        }
        val _sql = SQLImpl(sql)
        var callStat: CallableStatement? = null
        try {
            if (log.getLogLevel() >= Log.LEVEL_DEBUG) // log entry added to troubleshoot LDEV-1147
                log.debug("StoredProc", sql.toString() + " [" + params!!.size() + " params]")
            callStat = dc.getConnection().prepareCall(sql)
            if (blockfactor > 0) callStat.setFetchSize(blockfactor)
            if (timeout > 0) DataSourceUtil.setQueryTimeoutSilent(callStat, timeout)

            // set IN register OUT
            var it: Iterator<ProcParamBean?> = params!!.iterator()
            var param: ProcParamBean?
            var index = 1
            while (it.hasNext()) {
                param = it.next()
                param!!.setIndex(index)
                _sql.addItems(SQLItemImpl(param!!.getValue()))
                if (param!!.getDirection() !== ProcParamBean.DIRECTION_OUT) {
                    SQLCaster.setValue(pageContext, pageContext.getTimeZone(), callStat, index, param)
                }
                if (param!!.getDirection() !== ProcParamBean.DIRECTION_IN) {
                    registerOutParameter(callStat, param)
                }
                index++
            }
            val dsn: String = if (ds is DataSource) (ds as DataSource?).getName() else Caster.toString(ds)

            // cache
            var isFromCache = false
            var cacheValue: Object? = null
            val useCache = cachedWithin != null || cachedafter != null
            var cacheId: String? = null
            var cacheHandler: CacheHandler? = null
            if (useCache) {
                cacheId = CacheHandlerCollectionImpl.createId(_sql, dsn, username, password, Query.RETURN_TYPE_STORED_PROC, 0)
                cacheHandler = pageContext.getConfig().getCacheHandlerCollection(Config.CACHE_TYPE_QUERY, null).getInstanceMatchingObject(cachedWithin, null)
                if (cacheHandler is CacheHandlerPro) {
                    val cacheItem: CacheItem = (cacheHandler as CacheHandlerPro?).get(pageContext, cacheId, cachedWithin)
                    if (cacheItem != null) cacheValue = (cacheItem as StoredProcCacheItem).getStruct()
                } else if (cacheHandler != null) { // TODO this else block can be removed when all cache handlers implement CacheHandlerPro
                    val cacheItem: CacheItem = cacheHandler.get(pageContext, cacheId)
                    if (cacheItem != null) cacheValue = (cacheItem as StoredProcCacheItem).getStruct()
                    // cacheValue = pageContext.getQueryCache().get(pageContext,_sql,dsn,username,password,cachedafter);
                }
            }
            var count = 0
            val start: Long = System.currentTimeMillis()
            if (cacheValue == null) {
                // execute
                var isResult: Boolean = callStat.execute()
                val cacheStruct: Struct? = if (useCache) StructImpl() else null

                // resultsets
                var result: ProcResultBean
                index = 1
                do {
                    if (isResult) {
                        val rs: ResultSet = callStat.getResultSet()
                        if (rs != null) {
                            try {
                                result = results.get(index++, null)
                                if (result != null) {
                                    val q: lucee.runtime.type.Query = QueryImpl(rs, result.getMaxrows(), result.getName(), pageContext.getTimeZone())
                                    count += q.getRecordcount()
                                    setVariable(result.getName(), q)
                                    if (useCache) cacheStruct.set(KeyImpl.getInstance(result.getName()), q)
                                }
                            } finally {
                                IOUtil.close(rs)
                            }
                        }
                    }
                } while (callStat.getMoreResults().also { isResult = it } || callStat.getUpdateCount() !== -1)

                // params
                it = params.iterator()
                while (it.hasNext()) {
                    param = it.next()
                    if (param!!.getDirection() !== ProcParamBean.DIRECTION_IN) {
                        var value: Object? = null
                        if (!StringUtil.isEmpty(param!!.getVariable())) {
                            try {
                                value = SQLCaster.toCFType(callStat.getObject(param!!.getIndex()))
                            } catch (t: Throwable) {
                                ExceptionUtil.rethrowIfNecessary(t)
                            }
                            value = emptyIfNull(value)
                            if (param === STATUS_CODE) res.set(STATUSCODE, value) else setVariable(param!!.getVariable(), value)
                            if (useCache) cacheStruct.set(KeyImpl.getInstance(param!!.getVariable()), value)
                        }
                    }
                }
                if (cacheHandler != null) {
                    cacheStruct.set(COUNT, Caster.toDouble(count))
                    cacheHandler.set(pageContext, cacheId, cachedWithin, StoredProcCacheItem(cacheStruct, procedure, System.currentTimeMillis() - start))
                    // pageContext.getQueryCache().set(pageContext,_sql,dsn,username,password,cache,cachedbefore);
                }
            } else if (cacheValue is Struct) {
                val sctCache: Struct? = cacheValue as Struct?
                count = Caster.toIntValue(sctCache.removeEL(COUNT), 0)
                val cit: Iterator<Entry<Key?, Object?>?> = sctCache.entryIterator()
                var ce: Entry<Key?, Object?>?
                while (cit.hasNext()) {
                    ce = cit.next()
                    if (STATUS_CODE!!.getVariable()!!.equals(ce.getKey().getString())) res.set(KEY_SC, ce.getValue()) else setVariable(ce.getKey().getString(), ce.getValue())
                }
                isFromCache = true
            }
            // result
            var exe: Long
            setVariable(result, res)
            res.set(KeyConstants._executionTime, Caster.toDouble((System.nanoTime() - startNS).also { exe = it }))
            res.set(KeyConstants._cached, Caster.toBoolean(isFromCache))
            if (pageContext.getConfig().debug() && debug) {
                val logdb: Boolean = (pageContext.getConfig() as ConfigPro).hasDebugOptions(ConfigPro.DEBUG_DATABASE)
                if (logdb) pageContext.getDebugger().addQuery(null, dsn, procedure, _sql, count, pageContext.getCurrentPageSource(), exe.toInt())
            }
            if (log.getLogLevel() >= Log.LEVEL_INFO) {
                log.info(StoredProc::class.java.getSimpleName(), "executed [" + sql.trim().toString() + "] in " + DecimalFormat.call(pageContext, exe / 1000000.0).toString() + " ms")
            }
        } catch (e: SQLException) {
            log.error(StoredProc::class.java.getSimpleName(), e)
            val dbe = DatabaseException(e, SQLImpl(sql), dc)
            val details: String = String.format("Parameter types passed (%d): %s", params!!.size(), paramTypesPassed)
            dbe.setDetail(details)
            throw dbe
        } catch (pe: PageException) {
            log.error(StoredProc::class.java.getSimpleName(), pe)
            throw pe
        } finally {
            if (callStat != null) {
                try {
                    callStat.close()
                } catch (e: SQLException) {
                }
            }
            manager.releaseConnection(pageContext, dc)
        }
        return EVAL_PAGE
    }

    @Throws(PageException::class)
    private fun setVariable(name: String?, value: Object?) {
        pageContext.setVariable(name, value)
    }

    private fun createSQL(): String? {
        val sb = StringBuilder(64)
        if (returnValue != null) sb.append("{? = call ") else sb.append("{ call ")
        sb.append(procedure)
        sb.append('(')
        val numParams: Int = params!!.size()
        for (i in 0 until numParams) {
            if (i > 0) sb.append(",")
            sb.append('?')
        }
        sb.append(") }")
        return sb.toString()
    }

    private fun emptyIfNull(`object`: Object?): Object? {
        return if (`object` == null) "" else `object`
    }

    @Throws(SQLException::class)
    private fun registerOutParameter(proc: CallableStatement?, param: ProcParamBean?) {
        if (param!!.getScale() === -1) proc.registerOutParameter(param!!.getIndex(), param.getType()) else proc.registerOutParameter(param!!.getIndex(), param.getType(), param!!.getScale())
    }

    /**
     * @param b
     */
    fun hasBody(b: Boolean) {}

    /**
     * @param timeout the timeout to set
     */
    fun setTimeout(timeout: Double) {
        this.timeout = timeout.toInt()
    }

    private val log: lucee.runtime.tag.Log?
        private get() = ThreadLocalPageContext.getLog(pageContext, "datasource")
    private val paramTypesPassed: String?
        private get() = params.stream().map { ppb -> SQLCaster.toStringType(ppb.getType(), "?") }.collect(Collectors.joining(", "))
}