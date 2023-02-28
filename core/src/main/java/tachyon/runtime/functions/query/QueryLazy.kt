package tachyon.runtime.functions.query

import java.io.IOException

class QueryLazy : BIF() {
    @Override
    @Throws(PageException::class)
    operator fun invoke(pc: PageContext?, args: Array<Object?>?): Object? {
        if (args!!.size < 2 || args.size > 4) throw FunctionException(pc, "QueryLazy", 2, 4, args.size)
        if (args.size == 4) return call(pc, Caster.toString(args[0]), Caster.toFunction(args[1]), args[2], Caster.toStruct(args[3]))
        return if (args.size == 3) call(pc, Caster.toString(args[0]), Caster.toFunction(args[1]), args[2]) else call(pc, Caster.toString(args[0]), Caster.toFunction(args[1]))
    }

    class Col(res: ResultSet?, key: Key?, type: Int, index: Int, tz: TimeZone?, fullNullSupport: Boolean) {
        private val res: ResultSet?
        private val key: Key?
        private val index: Int
        private var cast: Cast? = null
        private val tz: TimeZone?
        private val fullNullSupport: Boolean
        @Throws(SQLException::class, IOException::class)
        fun get(): Object? {
            return cast.toCFType(tz, res, index)
        }

        @Throws(SQLException::class, IOException::class, PageException::class)
        fun set(sct: Struct?) {
            sct.set(key, if (fullNullSupport) cast.toCFType(tz, res, index) else emptyIfNull(cast.toCFType(tz, res, index)))
        }

        @Throws(PageException::class, SQLException::class, IOException::class)
        operator fun set(qry: QueryImpl?, row: Int) {
            qry.setAt(key, row, if (fullNullSupport) cast.toCFType(tz, res, index) else emptyIfNull(cast.toCFType(tz, res, index)))
        }

        companion object {
            fun emptyIfNull(obj: Object?): Object? {
                return if (obj == null) "" else obj
            }
        }

        init {
            this.res = res
            this.key = key
            this.index = index
            this.tz = tz
            this.fullNullSupport = fullNullSupport
            cast = try {
                QueryUtil.toCast(res, type)
            } catch (e: Exception) {
                throw SimpleQuery.toRuntimeExc(e)
            }
        }
    }

    companion object {
        private const val RETURN_TYPE_QUERY = 1
        private const val RETURN_TYPE_ARRAY = 2
        private const val RETURN_TYPE_STRUCT = 3
        private const val serialVersionUID = 2886504786460447165L
        private val BLOCKFACTOR: Key? = KeyImpl.getInstance("blockfactor")
        private val MAXROWS: Key? = KeyImpl.getInstance("maxrows")
        private val COLUMNKEY: Key? = KeyImpl.getInstance("columnkey")
        @Throws(PageException::class)
        fun call(pc: PageContext?, sql: String?, listener: UDF?): String? {
            return call(pc, sql, listener, null, null)
        }

        @Throws(PageException::class)
        fun call(pc: PageContext?, sql: String?, listener: UDF?, params: Object?): String? {
            return call(pc, sql, listener, params, null)
        }

        // name is set by evaluator
        @Throws(PageException::class)
        fun call(pc: PageContext?, strSQL: String?, listener: UDF?, params: Object?, options: Struct?): String? {
            val ds: DataSource? = getDatasource(pc, options)
            // credentials
            val user = getString(pc, options, KeyConstants._username, null)
            var pass = getString(pc, options, KeyConstants._password, null)
            val returntype = getReturntype(pc, options)
            var columnKey: Collection.Key? = null
            if (returntype == RETURN_TYPE_STRUCT) {
                columnKey = getKey(pc, options, COLUMNKEY, null)
                if (StringUtil.isEmpty(columnKey)) throw ApplicationException("attribute columnKey is required when return type is set to struct")
            } else {
            }
            val maxrows = getInt(pc, options, MAXROWS, Integer.MIN_VALUE)
            val blockfactor = getInt(pc, options, BLOCKFACTOR, Integer.MIN_VALUE)
            if (user == null) pass = null
            val sql: SQL? = getSQL(pc, strSQL, params)
            val timeout: TimeSpan? = getTimeout(pc, options)
            val tz: TimeZone? = getTimeZone(pc, options, ds)
            val manager: DatasourceManagerImpl = pc.getDataSourceManager() as DatasourceManagerImpl
            val dc: DatasourceConnection = manager.getConnection(pc, ds, user, pass) // TODO username and password
            val isMySQL: Boolean = DataSourceUtil.isMySQL(dc)
            // check SQL Restrictions
            if (dc.getDatasource().hasSQLRestriction()) { // deprecated
                QueryUtil.checkSQLRestriction(dc, sql)
            }

            // execute
            var stat: Statement? = null
            var res: ResultSet? = null
            var hasResult = false
            try {
                val items: Array<SQLItem?> = sql.getItems()
                if (items.size == 0) {
                    stat = dc.getConnection().createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)
                    setAttributes(stat, maxrows, blockfactor, timeout, isMySQL)
                    // some driver do not support second argument
                    hasResult = stat.execute(sql.getSQLString())
                } else {
                    // some driver do not support second argument
                    val preStat: PreparedStatement = dc.getPreparedStatement(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)
                    stat = preStat
                    setAttributes(preStat, maxrows, blockfactor, timeout, isMySQL)
                    setItems(pc, tz, preStat, items)
                    hasResult = preStat.execute()
                }
                res = null
                do {
                    if (hasResult) {
                        res = stat.getResultSet()
                        exe(pc, res, tz, listener, blockfactor, returntype, columnKey)
                        break
                    }
                    throw ApplicationException("the function QueryLazy can only be used for queries returning a resultset")
                } while (true)
            } catch (e: SQLException) {
                throw DatabaseException(e, sql, dc)
            } catch (e: Throwable) {
                ExceptionUtil.rethrowIfNecessary(e)
                throw Caster.toPageException(e)
            } finally {
                DBUtil.closeEL(res)
                DBUtil.closeEL(stat)
                manager.releaseConnection(pc, dc)
            }
            return null
        }

        @Throws(SQLException::class, PageException::class, IOException::class)
        private fun exe(pc: PageContext?, res: ResultSet?, tz: TimeZone?, listener: UDF?, blockfactor: Int, returntype: Int, columnKey: Collection.Key?) {
            val meta: ResultSetMetaData = res.getMetaData()

            // init columns
            val columncount: Int = meta.getColumnCount()
            val tmpKeys: List<Key?> = ArrayList<Key?>()
            // List<Integer> tmpTypes=new ArrayList<Integer>();
            // int count=0;
            var key: Collection.Key
            var columnName: String
            var type: Int
            val fns: Boolean = NullSupportHelper.full(pc)
            val columns = arrayOfNulls<Col?>(columncount)
            for (i in 0 until columncount) {
                columnName = QueryUtil.getColumnName(meta, i + 1)
                if (StringUtil.isEmpty(columnName)) columnName = "column_$i"
                type = meta.getColumnType(i + 1)
                key = KeyImpl.init(columnName)
                val index = tmpKeys.indexOf(key)
                if (index == -1) {
                    // mappings.put(key.getLowerString(), Caster.toInteger(i+1));
                    tmpKeys.add(key)
                    // tmpTypes.add(type);
                    columns[i] = Col(res, key, type, i + 1, tz, fns)
                }
            }

            // loop data
            val blocks = blockfactor > 1
            var isQuery = false
            var isArray = false
            var isStruct = false
            var row: Struct?
            var _arrRows: Array? = null
            var sctRows: Struct? = null
            var qryRows: QueryImpl? = null
            if (blocks) {
                if (returntype == RETURN_TYPE_ARRAY) {
                    _arrRows = ArrayImpl()
                    isArray = true
                } else if (returntype == RETURN_TYPE_STRUCT) {
                    sctRows = StructImpl()
                    isStruct = true
                } else {
                    qryRows = QueryImpl(tmpKeys.toArray(arrayOfNulls<Collection.Key?>(tmpKeys.size())), blockfactor, "queryLazy")
                    isQuery = true
                }
            }
            var rownbr = 0
            while (res.next()) {
                rownbr++
                // create row
                row = StructImpl()
                for (col in columns) {
                    if (isQuery) col!![qryRows] = rownbr else col!!.set(row)
                }
                if (blocks) {
                    if (isArray) {
                        _arrRows.appendEL(row)
                        if (blockfactor == rownbr) {
                            if (!Caster.toBooleanValue(listener.call(pc, arrayOf<Object?>(_arrRows), true), true)) {
                                rownbr = 0
                                break
                            }
                            _arrRows = ArrayImpl()
                            rownbr = 0
                        }
                    } else if (isStruct) {
                        sctRows.set(KeyImpl.toKey(row.get(columnKey)), row)
                        if (blockfactor == rownbr) {
                            if (!Caster.toBooleanValue(listener.call(pc, arrayOf<Object?>(sctRows), true), true)) {
                                rownbr = 0
                                break
                            }
                            sctRows = StructImpl()
                            rownbr = 0
                        }
                    } else if (isQuery) {
                        if (blockfactor == rownbr) {
                            if (!Caster.toBooleanValue(listener.call(pc, arrayOf<Object?>(qryRows), true), true)) {
                                rownbr = 0
                                break
                            }
                            qryRows = QueryImpl(tmpKeys.toArray(arrayOfNulls<Collection.Key?>(tmpKeys.size())), blockfactor, "queryLazy")
                            rownbr = 0
                        }
                    }
                } else {
                    if (!Caster.toBooleanValue(listener.call(pc, arrayOf<Object?>(row), true), true)) break
                }
            }
            // send the remaing to the UDF
            if (blocks && rownbr > 0) {
                if (isArray) {
                    listener.call(pc, arrayOf<Object?>(_arrRows), true)
                } else if (isStruct) {
                    listener.call(pc, arrayOf<Object?>(sctRows), true)
                } else if (isQuery) {
                    if (rownbr < blockfactor) {
                        // shrink
                        qryRows.removeRows(rownbr, blockfactor - rownbr)
                    }
                    listener.call(pc, arrayOf<Object?>(qryRows), true)
                }
            }
        }

        @Throws(SQLException::class)
        private fun setAttributes(stat: Statement?, maxrow: Int, fetchsize: Int, timeout: TimeSpan?, isMySQL: Boolean) {
            if (maxrow > -1) stat.setMaxRows(maxrow)
            if (isMySQL) stat.setFetchSize(Integer.MIN_VALUE) // this is necessary for mysql otherwise all data are loaded into memory
            else if (fetchsize > 0) stat.setFetchSize(fetchsize)
            val to: Int = QueryImpl.getSeconds(timeout)
            if (to > 0) DataSourceUtil.setQueryTimeoutSilent(stat, to)
        }

        @Throws(DatabaseException::class, PageException::class, SQLException::class)
        private fun setItems(pc: PageContext?, tz: TimeZone?, preStat: PreparedStatement?, items: Array<SQLItem?>?) {
            for (i in items.indices) {
                SQLCaster.setValue(pc, tz, preStat, i + 1, items!![i])
            }
        }

        @Throws(PageException::class)
        private fun getString(pc: PageContext?, options: Struct?, key: Collection.Key?, defaultValue: String?): String? {
            if (options == null) return defaultValue
            val str: String = Caster.toString(options.get(key, null), null)
            return if (StringUtil.isEmpty(str)) defaultValue else str
        }

        @Throws(PageException::class)
        private fun getKey(pc: PageContext?, options: Struct?, key: Collection.Key?, defaultValue: Collection.Key?): Collection.Key? {
            if (options == null) return defaultValue
            val str: Collection.Key = Caster.toKey(options.get(key, null), null)
            return if (StringUtil.isEmpty(str)) defaultValue else str
        }

        private fun getInt(pc: PageContext?, options: Struct?, key: Collection.Key?, defaultValue: Int): Int {
            return if (options == null) defaultValue else Caster.toIntValue(options.get(key, null), defaultValue)
        }

        @Throws(PageException::class)
        fun getReturntype(pc: PageContext?, options: Struct?): Int {
            var strReturntype = getString(pc, options, KeyConstants._returntype, null)
            if (StringUtil.isEmpty(strReturntype)) return RETURN_TYPE_QUERY
            strReturntype = strReturntype.toLowerCase().trim()
            return if (strReturntype.equals("query")) RETURN_TYPE_QUERY else if (strReturntype.equals("struct")) RETURN_TYPE_STRUCT else if (strReturntype.equals("array") || strReturntype.equals("array_of_struct") || strReturntype.equals("array-of-struct") || strReturntype.equals("arrayofstruct")
                    || strReturntype.equals("array_of_entity") || strReturntype.equals("array-of-entity") || strReturntype.equals("arrayofentities")
                    || strReturntype.equals("array_of_entities") || strReturntype.equals("array-of-entities") || strReturntype.equals("arrayofentities")) RETURN_TYPE_ARRAY else throw ApplicationException("option returntype for function QueryLazy invalid value",
                    "valid values are [query,array,struct] but value is now [$strReturntype]")
        }

        @Throws(PageException::class)
        private fun getTimeZone(pc: PageContext?, options: Struct?, ds: DataSource?): TimeZone? {
            var obj: Object? = if (options == null) null else options.get(KeyConstants._timezone, null)
            if (StringUtil.isEmpty(obj)) obj = null
            if (obj != null) return Caster.toTimeZone(obj) else if (ds.getTimeZone() != null) return ds.getTimeZone()
            return pc.getTimeZone()
        }

        @Throws(PageException::class)
        fun getTimeout(pc: PageContext?, options: Struct?): TimeSpan? {
            val obj: Object? = if (options == null) null else options.get(KeyConstants._timeout, null)
            if (obj == null || StringUtil.isEmpty(obj)) return null
            return if (obj is TimeSpan) obj as TimeSpan? else {
                val i: Int = Caster.toIntValue(obj)
                if (i < 0) throw ApplicationException("invalid value [$i] for attribute timeout, value must be a positive integer greater or equal than 0")
                TimeSpanImpl(0, 0, 0, i)
            }
        }

        @Throws(PageException::class)
        private fun getSQL(pc: PageContext?, strSQL: String?, params: Object?): SQL? {
            if (params != null) {
                if (params is Argument) return QueryParamConverter.convert(strSQL, params as Argument?)
                if (Decision.isArray(params)) return QueryParamConverter.convert(strSQL, Caster.toArray(params))
                if (Decision.isStruct(params)) return QueryParamConverter.convert(strSQL, Caster.toStruct(params))
                throw DatabaseException("value of the attribute [params] has to be a struct or an array", null, null, null)
            }
            return SQLImpl(strSQL)
        }

        @Throws(PageException::class)
        private fun getDatasource(pc: PageContext?, options: Struct?): DataSource? {
            var ds: DataSource? = null
            var obj: Object? = if (options == null) null else options.get(KeyConstants._datasource, null)
            if (obj != null) ds = Query.toDatasource(pc, obj)

            // no datasource definition
            if (ds == null) {
                obj = pc.getApplicationContext().getDefDataSource()
                if (StringUtil.isEmpty(obj)) {
                    val isCFML = pc.getRequestDialect() === CFMLEngine.DIALECT_CFML
                    throw ApplicationException("option [datasource] is required when option [dbtype] is not [query] and no default datasource is defined",
                            ("you can define a default datasource as attribute [defaultdatasource] of the tag "
                                    + if (isCFML) Constants.CFML_APPLICATION_TAG_NAME else Constants.LUCEE_APPLICATION_TAG_NAME) + " or as data member of the "
                                    + (if (isCFML) Constants.CFML_APPLICATION_EVENT_HANDLER else Constants.LUCEE_APPLICATION_EVENT_HANDLER).toString() + " (this.defaultdatasource=\"mydatasource\";)")
                }
                ds = if (obj is DataSource) obj as DataSource? else Query.toDatasource(pc, obj)
            }
            return ds
        }
    }
}