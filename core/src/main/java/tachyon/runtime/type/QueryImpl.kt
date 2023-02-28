/**
 *
 * Copyright (c) 2014, the Railo Company Ltd. All rights reserved.
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
package tachyon.runtime.type

import java.io.ByteArrayInputStream

/**
 * implementation of the query interface
 */
/**
 *
 */
class QueryImpl : Query, Objects, QueryResult, Cloneable {
    private var populating = false
    private var columns: Array<QueryColumnImpl?>?
    private var columnNames: Array<Collection.Key?>?
    private var sql: SQL? = null
    private var currRow: Map<Integer?, Integer?>? = ConcurrentHashMap<Integer?, Integer?>()
    private var recordcount: AtomicInteger? = AtomicInteger(0)
    private var columncount = 0
    private var exeTime: Long = 0
    private var cacheType: String? = null
    private var name: String? = null
    private var updateCount = 0
    private var generatedKeys: QueryImpl? = null
    private var templateLine: TemplateLine? = null
    private var datasourceName: String? = null
    private var indexName: Collection.Key? = null
    private var indexes // = new
            : Map<Collection.Key?, Integer?>? = null

    companion object {
        private const val serialVersionUID = 1035795427320192551L // do not chnage
        val GENERATED_KEYS: Collection.Key? = KeyImpl.getInstance("GENERATED_KEYS")
        val GENERATEDKEYS: Collection.Key? = KeyImpl.getInstance("GENERATEDKEYS")
        private var useMSSQLModern = false
        @Throws(PageException::class)
        fun toStruct(pc: PageContext?, dc: DatasourceConnection?, sql: SQL?, keyName: Collection.Key?, maxrow: Int, fetchsize: Int, timeout: TimeSpan?, name: String?,
                     templateLine: TemplateLine?, createUpdateData: Boolean, allowToCachePreperadeStatement: Boolean): QueryStruct? {
            val sct = QueryStruct(name, sql, templateLine)
            sct.setDatasourceName(dc.getDatasource().getName())
            execute(pc, dc, sql, maxrow, fetchsize, timeout, createUpdateData, allowToCachePreperadeStatement, null, sct, keyName)
            return sct
        }

        @Throws(PageException::class)
        fun toArray(pc: PageContext?, dc: DatasourceConnection?, sql: SQL?, maxrow: Int, fetchsize: Int, timeout: TimeSpan?, name: String?, templateLine: TemplateLine?,
                    createUpdateData: Boolean, allowToCachePreperadeStatement: Boolean): QueryArray? {
            val arr = QueryArray(name, sql, templateLine)
            arr.setDatasourceName(dc.getDatasource().getName())
            execute(pc, dc, sql, maxrow, fetchsize, timeout, createUpdateData, allowToCachePreperadeStatement, null, arr, null)
            return arr
        }

        @Throws(PageException::class)
        private fun execute(pc: PageContext?, dc: DatasourceConnection?, sql: SQL?, maxrow: Int, fetchsize: Int, timeout: TimeSpan?, createUpdateData: Boolean,
                            allowToCachePreperadeStatement: Boolean, qry: QueryImpl?, qr: QueryResult?, keyName: Collection.Key?) {

            // MSSQL is handled separatly
            if (useMSSQLModern && DataSourceUtil.isMSSQLDriver(dc)) {
                executeMSSQL(pc, dc, sql, maxrow, fetchsize, timeout, createUpdateData, allowToCachePreperadeStatement, qry, qr, keyName)
                return
            }
            val tz: TimeZone = ThreadLocalPageContext.getTimeZone(pc)

            // check if datasource support Generated Keys
            var createGeneratedKeys = createUpdateData
            if (createUpdateData) {
                if (!dc.supportsGetGeneratedKeys()) createGeneratedKeys = false
            }

            // check SQL Restrictions
            if (dc.getDatasource().hasSQLRestriction()) {
                QueryUtil.checkSQLRestriction(dc, sql)
            }
            var stat: Statement? = null
            // Stopwatch stopwatch=new Stopwatch();
            val start: Long = System.nanoTime()
            // stopwatch.start();
            var hasResult = false
            // boolean closeStatement=true;
            try {
                val items: Array<SQLItem?> = sql.getItems()
                if (items.size == 0) {
                    stat = dc.getConnection().createStatement()
                    setAttributes(stat, maxrow, fetchsize, timeout)
                    // some driver do not support second argument
                    // hasResult=createGeneratedKeys?stat.execute(sql.getSQLString(),Statement.RETURN_GENERATED_KEYS):stat.execute(sql.getSQLString());
                    hasResult = QueryUtil.execute(pc, stat, createGeneratedKeys, sql)
                } else {
                    // some driver do not support second argument
                    val preStat: PreparedStatement = dc.getPreparedStatement(sql, createGeneratedKeys, allowToCachePreperadeStatement)
                    // closeStatement=false;
                    stat = preStat
                    setAttributes(preStat, maxrow, fetchsize, timeout)
                    setItems(pc, ThreadLocalPageContext.getTimeZone(pc), preStat, items)
                    hasResult = QueryUtil.execute(pc, preStat)
                }
                var uc: Int
                // ResultSet res;
                do {
                    if (hasResult) {
                        // res=stat.getResultSet();
                        // if(fillResult(dc,res, maxrow, true,createGeneratedKeys,tz))break;
                        if (fillResult(qry, qr, keyName, dc, stat.getResultSet(), maxrow, true, createGeneratedKeys, tz)) break
                    } else if (setUpdateCount(qry ?: qr, stat).also { uc = it } != -1) {
                        if (uc > 0 && createGeneratedKeys && qry != null) qry.setGeneratedKeys(dc, stat, tz)
                    } else break
                    hasResult = try {
                        // hasResult=stat.getMoreResults(Statement.CLOSE_CURRENT_RESULT);
                        stat.getMoreResults()
                    } catch (t: Throwable) {
                        ExceptionUtil.rethrowIfNecessary(t)
                        break
                    }
                } while (true)
            } catch (e: SQLException) {
                throw DatabaseException(e, sql, dc)
            } catch (e: Throwable) {
                ExceptionUtil.rethrowIfNecessary(e)
                throw Caster.toPageException(e)
            } finally {
                // if(closeStatement)
                DBUtil.closeEL(stat)
            }
            if (qry != null) {
                qry.exeTime = System.nanoTime() - start
                if (qry.columncount == 0) {
                    if (qry.columnNames == null) qry.columnNames = arrayOfNulls<Collection.Key?>(0)
                    if (qry.columns == null) qry.columns = arrayOfNulls<QueryColumnImpl?>(0)
                }
            } else {
                qr.setExecutionTime(System.nanoTime() - start)
            }
        }

        @Throws(PageException::class)
        private fun executeMSSQL(pc: PageContext?, dc: DatasourceConnection?, sql: SQL?, maxrow: Int, fetchsize: Int, timeout: TimeSpan?, createUpdateData: Boolean,
                                 allowToCachePreperadeStatement: Boolean, qry: QueryImpl?, qr: QueryResult?, keyName: Collection.Key?) {
            val tz: TimeZone = ThreadLocalPageContext.getTimeZone(pc)

            // check if datasource support Generated Keys
            var createGeneratedKeys = createUpdateData
            if (createUpdateData) {
                if (!dc.supportsGetGeneratedKeys()) createGeneratedKeys = false
            }

            // check SQL Restrictions
            if (dc.getDatasource().hasSQLRestriction()) {
                QueryUtil.checkSQLRestriction(dc, sql)
            }
            var stat: Statement? = null
            // Stopwatch stopwatch=new Stopwatch();
            val start: Long = System.nanoTime()
            // stopwatch.start();
            var hasResult = false
            var hasPossibleGeneratedKeys = false
            // boolean closeStatement=true;
            try {
                val items: Array<SQLItem?> = sql.getItems()
                if (items.size == 0) {
                    stat = dc.getConnection().createStatement()
                    setAttributes(stat, maxrow, fetchsize, timeout)
                    // some driver do not support second argument
                    // hasResult=createGeneratedKeys?stat.execute(sql.getSQLString(),Statement.RETURN_GENERATED_KEYS):stat.execute(sql.getSQLString());
                    hasResult = QueryUtil.execute(pc, stat, createGeneratedKeys, sql)
                } else {
                    // some driver do not support second argument
                    val preStat: PreparedStatement = dc.getPreparedStatement(sql, createGeneratedKeys, allowToCachePreperadeStatement)
                    // closeStatement=false;
                    stat = preStat
                    setAttributes(preStat, maxrow, fetchsize, timeout)
                    setItems(pc, ThreadLocalPageContext.getTimeZone(pc), preStat, items)
                    hasResult = QueryUtil.execute(pc, preStat)
                }
                var uc: Int
                var resultsetCount = 0
                // ResultSet res;
                do {
                    resultsetCount++
                    if (hasResult) {
                        if (fillResult(qry, qr, keyName, dc, stat.getResultSet(), maxrow, true, createGeneratedKeys, tz)) {
                            /*
						 * Some SQL implementations (e.g. SQL Server) allow both a resultset *and* keys to be generated in a
						 * single statement. For example:
						 *
						 * insert into XXXX (col1, col2) OUTPUT INSERTED.* values (1, 'a'), (2, 'b'), (3, 'c')
						 *
						 * In the above, the "OUTPUT INSERTED.*" will return a recordset of all the changes.
						 */
                            if (resultsetCount == 1 && !hasPossibleGeneratedKeys) {
                                // we need to attempt to get the generated keys, because an exception might be getting throw
                                try {
                                    /*
								 * The Microsoft SQL Server driver can advance the resultset when the getGeneratedKeys() method is
								 * called. If the next resultset happens to have an exception, we have to be able to track that
								 * exception so we can report it.
								 */
                                    val rs: ResultSet = stat.getGeneratedKeys()
                                    /*
								 * We should check for any generated keys now, because in the MSSQL driver if we have a single
								 * resultset (INSERT or UPDATE) that has an OUTPUT clause, the generated keys are only available
								 * right now.
								 *
								 * If we get back false, then an exception happened. However, we will still want to check back later
								 * in care there are other statements executed later that could return a resultset.
								 */if (qry != null) {
                                        hasPossibleGeneratedKeys = qry != null && !qry.setGeneratedKeys(dc, rs, tz)
                                    }
                                } catch (se: SQLException) {
                                    /*
								 * We can ignore when SQL Server driver throws the "statement must be executed" message as this is
								 * the standard exception which happens when there are no keys available.
								 *
								 * However, we should report other exceptions because they could becoming from a RAISERROR or other
								 * exception being generated by SQL Server.
								 */
                                    // TODO we need a change here that less depends on the actual MSSQL JDBC driver
                                    hasPossibleGeneratedKeys = if (se.getMessage() !== "The statement must be executed before any results can be obtained.") {
                                        throw se
                                    } else {
                                        true
                                    }
                                }
                            }
                            /*
						 * When using the MSSQL driver, we need to make sure to go through all the recordset objects because
						 * there may be exceptions or additional SQL recordsets that need to be processed.
						 */while (stat.getMoreResults()) {
                                // we just need to advance through all the resultsets, we can ignore
                                // the results since we only return the first resultset
                            }
                            break
                        }
                    } else if (setUpdateCount(qry ?: qr, stat).also { uc = it } != -1) {
                        if (uc > 0) {
                            // since we had some updates, we need to flag that the generated keys need to be checked
                            hasPossibleGeneratedKeys = true
                        }
                    } else break

                    // try {
                    // hasResult=stat.getMoreResults(Statement.CLOSE_CURRENT_RESULT);
                    hasResult = stat.getMoreResults()
                    // }
                    // catch (SQLException e) {
                    /*
				 * The JTDS driver (and possibly other drivers) throw exceptions as soon as the query is executed,
				 * however the MSSQL driver will delay throwing the execution until you try to request the
				 * recordset.
				 *
				 * So we need to check if a SQLException is being thrown and if so, we need to rethrow it.
				 *
				 */
                    // if (DataSourceUtil.isMSSQLDriver(dc)) {
                    // throw e;
                    // }
                    // break;
                    // }
                } while (true)
            } catch (e: SQLException) {
                throw DatabaseException(e, sql, dc)
            } catch (e: Throwable) {
                ExceptionUtil.rethrowIfNecessary(e)
                throw Caster.toPageException(e)
            } finally {
                // we need to look for any possible generated keys from the query
                if (createGeneratedKeys && hasPossibleGeneratedKeys && qry != null) {
                    /*
				 * The MSSQL driver recommends always checking for generated keys after all recordsets have been
				 * parsed. This will prevent the Statement.getGeneratedKeys() from advancing the recordset.
				 *
				 * See the following link for more information:
				 * https://social.technet.microsoft.com/Forums/ie/en-US/a91f8aa2-6ec0-447d-8b95-9e99e1da56fb/the-
				 * statement-must-be-executed-before-any-results-can-be-obtained-error-with-jdbc-20?forum=
				 * sqldataaccess
				 */
                    qry.setGeneratedKeys(dc, stat, tz)
                }
                // if(closeStatement)
                DBUtil.closeEL(stat)
            }
            if (qry != null) {
                qry.exeTime = System.nanoTime() - start
                if (qry.columncount == 0) {
                    if (qry.columnNames == null) qry.columnNames = arrayOfNulls<Collection.Key?>(0)
                    if (qry.columns == null) qry.columns = arrayOfNulls<QueryColumnImpl?>(0)
                }
            } else {
                qr.setExecutionTime(System.nanoTime() - start)
            }
        }

        private fun setUpdateCount(qr: QueryResult?, stat: Statement?): Int {
            try {
                val uc: Int = stat.getUpdateCount()
                if (uc > -1) {
                    qr.setUpdateCount(qr.getUpdateCount() + uc)
                    return uc
                }
            } catch (t: Throwable) {
                ExceptionUtil.rethrowIfNecessary(t)
            }
            return -1
        }

        @Throws(DatabaseException::class, PageException::class, SQLException::class)
        private fun setItems(pc: PageContext?, tz: TimeZone?, preStat: PreparedStatement?, items: Array<SQLItem?>?) {
            for (i in items.indices) {
                SQLCaster.setValue(pc, tz, preStat, i + 1, items!![i])
            }
        }

        @Throws(SQLException::class)
        private fun setAttributes(stat: Statement?, maxrow: Int, fetchsize: Int, timeout: TimeSpan?) {
            if (maxrow > -1) stat.setMaxRows(maxrow)
            if (fetchsize > 0) stat.setFetchSize(fetchsize)
            val to = getSeconds(timeout)
            if (to > 0) DataSourceUtil.setQueryTimeoutSilent(stat, to)
        }

        fun getSeconds(timeout: TimeSpan?): Int {
            if (timeout == null) return 0
            if (timeout.getSeconds() > 0) return Caster.toIntValue(timeout.getSeconds())
            return if (timeout.getMillis() > 0) 1 else 0
        }

        @Throws(SQLException::class, IOException::class, PageException::class)
        private fun fillResult(qry: QueryImpl?, qr: QueryResult?, keyName: Collection.Key?, dc: DatasourceConnection?, result: ResultSet?, maxrow: Int, closeResult: Boolean,
                               createGeneratedKeys: Boolean, tz: TimeZone?): Boolean {
            if (result == null) return false
            var recordcount = 0
            var columncount = 0
            var columnNames: Array<Collection.Key?>? = null
            var columns: Array<QueryColumnImpl?>? = null
            try {
                val meta: ResultSetMetaData = result.getMetaData()
                columncount = meta.getColumnCount()

                // set header arrays
                val tmpColumnNames: Array<Collection.Key?> = arrayOfNulls<Collection.Key?>(columncount)
                var count = 0
                var key: Collection.Key
                var columnName: String
                for (i in 0 until columncount) {
                    columnName = QueryUtil.getColumnName(meta, i + 1)
                    if (StringUtil.isEmpty(columnName)) columnName = "column_$i"
                    key = KeyImpl.init(columnName)
                    val index = getIndexFrom(tmpColumnNames, key, 0, i)
                    if (index == -1) {
                        tmpColumnNames[i] = key
                        count++
                    }
                }
                columncount = count
                columnNames = arrayOfNulls<Collection.Key?>(columncount)
                if (qry != null) columns = arrayOfNulls<QueryColumnImpl?>(columncount)
                val casts: Array<Cast?> = arrayOfNulls<Cast?>(columncount)

                // get all used ints
                val usedColumns = IntArray(columncount)
                count = 0
                for (i in tmpColumnNames.indices) {
                    if (tmpColumnNames[i] != null) {
                        usedColumns[count++] = i
                    }
                }

                // set used column names
                var type: Int
                for (i in usedColumns.indices) {
                    columnNames!![i] = tmpColumnNames[usedColumns[i]]
                    type = meta.getColumnType(usedColumns[i] + 1)
                    if (qry != null) columns!![i] = QueryColumnImpl(qry, columnNames[i], type)
                    casts[i] = QueryUtil.toCast(result, type)
                }
                if (createGeneratedKeys && columncount == 1 && columnNames!![0].equals(GENERATED_KEYS) && dc != null && DataSourceUtil.isMSSQLDriver(dc)) {
                    columncount = 0
                    columnNames = null
                    if (qry != null) {
                        columns = null
                        qry.setGeneratedKeys(dc, result, tz)
                    }
                    return false
                }

                // fill QUERY
                if (qry != null) {
                    qry.populating = true
                    var index = -1
                    if (qry.indexName != null) {
                        qry.indexes = ConcurrentHashMap<Collection.Key?, Integer?>()
                        for (i in columnNames.indices) {
                            if (columnNames!![i].equalsIgnoreCase(qry.indexName)) {
                                index = i
                                break
                            }
                        }
                    }
                    if (index != -1) {
                        var o: Object
                        while (result.next()) {
                            if (maxrow > -1 && recordcount >= maxrow) {
                                break
                            }
                            for (i in usedColumns.indices) {
                                o = casts[i].toCFType(tz, result, usedColumns[i] + 1)
                                if (index == i) {
                                    qry.indexes.put(Caster.toKey(o), recordcount + 1)
                                }
                                columns!![i]!!.add(o)
                            }
                            ++recordcount
                        }
                    } else {
                        while (result.next()) {
                            if (maxrow > -1 && recordcount >= maxrow) {
                                break
                            }
                            for (i in usedColumns.indices) {
                                columns!![i]!!.add(casts[i].toCFType(tz, result, usedColumns[i] + 1))
                            }
                            ++recordcount
                        }
                    }
                } else {
                    var sct: Struct?
                    val qa: QueryArray? = if (qr is QueryArray) qr as QueryArray? else null
                    val qs: QueryStruct? = if (qa == null) qr as QueryStruct? else null
                    var k: Object
                    val full: Boolean = NullSupportHelper.full()
                    while (result.next()) {
                        if (maxrow > -1 && recordcount >= maxrow) {
                            break
                        }
                        sct = StructImpl(Struct.TYPE_LINKED)
                        var `val`: Object
                        for (i in usedColumns.indices) {
                            `val` = casts[i].toCFType(tz, result, usedColumns[i] + 1)
                            if (`val` == null && !full) `val` = ""
                            sct.set(columnNames!![i], `val`)
                        }
                        if (qa != null) qa.appendEL(sct) else {
                            // QueryStruct
                            if (keyName == null) {
                                // single record struct
                                if (recordcount > 0) {
                                    throw ApplicationException("Attribute [keyColumn] is required when return type is set to Struct and more than one record is returned")
                                }
                                qs.setSingleRecord(true)
                                for (tmp in columnNames!!) {
                                    qs.set(tmp, sct.get(tmp))
                                }
                            } else {
                                // struct of structs with keyName column as key
                                k = sct.get(keyName, CollectionUtil.NULL)
                                if (k === CollectionUtil.NULL) {
                                    val keys: Struct = StructImpl()
                                    for (tmp in columnNames!!) {
                                        keys.set(tmp, "")
                                    }
                                    throw StructSupport.invalidKey(null, keys, keyName, "resultset")
                                }
                                if (k == null) k = ""
                                qs.set(KeyImpl.toKey(k), sct)
                            }
                        }
                        ++recordcount
                    }
                }
            } finally {
                if (qry != null) {
                    qry.populating = false
                    qry.columncount = columncount
                    qry.recordcount.set(recordcount)
                    qry.columnNames = columnNames
                    qry.columns = columns
                } else {
                    qr.setColumnNames(columnNames)
                }
                if (closeResult) IOUtil.close(result)
            }
            return true
        }

        @Throws(IOException::class, SQLException::class)
        private fun toString(clob: Clob?): Object? {
            return IOUtil.toString(clob.getCharacterStream())
        }

        private fun getIndexFrom(tmpColumnNames: Array<Collection.Key?>?, key: Collection.Key?, from: Int, to: Int): Int {
            for (i in from until to) {
                if (tmpColumnNames!![i] != null && tmpColumnNames[i].equalsIgnoreCase(key)) return i
            }
            return -1
        }

        @Throws(DatabaseException::class)
        private fun validateColumnNames(columnNames: Array<Key?>?) {
            val testMap: Set<String?> = HashSet<String?>()
            for (i in columnNames.indices) {

                // Only allow column names that are valid variable name
                // if(!Decision.isSimpleVariableName(columnNames[i]))
                // throw new DatabaseException("invalid column name ["+columnNames[i]+"] for query",
                // "column names
                // must start with a letter and can be followed by
                // letters numbers and underscores [_]. RegExp:[a-zA-Z][a-zA-Z0-9_]*",null,null,null);
                if (testMap.contains(columnNames!![i].getLowerString())) throw DatabaseException("invalid parameter for query, ambiguous column name " + columnNames[i],
                        "columnNames: " + ListUtil.arrayToListTrim(_toStringKeys(columnNames), ","), null, null)
                testMap.add(columnNames[i].getLowerString())
            }
        }

        private fun _toStringKeys(columnNames: Array<Collection.Key?>?): Array<String?>? {
            val strColumnNames = arrayOfNulls<String?>(columnNames!!.size)
            for (i in strColumnNames.indices) {
                strColumnNames[i] = columnNames[i].getString()
            }
            return strColumnNames
        }

        private fun toCollKeyArr(strColumnList: Array<String?>?): Array<Collection.Key?>? {
            val columnList: Array<Collection.Key?> = arrayOfNulls<Collection.Key?>(strColumnList!!.size)
            for (i in columnList.indices) {
                columnList[i] = KeyImpl.init(strColumnList!![i].trim())
            }
            return columnList
        }

        /**
         *
         * @param type
         * @return return String represetation of a Type from int type
         */
        fun getColumTypeName(type: Int): String? {
            return when (type) {
                Types.ARRAY -> "OBJECT"
                Types.BIGINT -> "BIGINT"
                Types.BINARY -> "BINARY"
                Types.BIT -> "BIT"
                Types.BLOB -> "OBJECT"
                Types.BOOLEAN -> "BOOLEAN"
                Types.CHAR -> "CHAR"
                Types.NCHAR -> "NCHAR"
                Types.CLOB -> "OBJECT"
                Types.NCLOB -> "OBJECT"
                Types.DATALINK -> "OBJECT"
                Types.DATE -> "DATE"
                Types.DECIMAL -> "DECIMAL"
                Types.DISTINCT -> "OBJECT"
                Types.DOUBLE -> "DOUBLE"
                Types.FLOAT -> "DOUBLE"
                Types.INTEGER -> "INTEGER"
                Types.JAVA_OBJECT -> "OBJECT"
                Types.LONGVARBINARY -> "LONGVARBINARY"
                Types.LONGVARCHAR -> "LONGVARCHAR"
                Types.NULL -> "OBJECT"
                Types.NUMERIC -> "NUMERIC"
                Types.OTHER -> "OBJECT"
                Types.REAL -> "REAL"
                Types.REF -> "OBJECT"
                Types.SMALLINT -> "SMALLINT"
                Types.STRUCT -> "OBJECT"
                Types.TIME -> "TIME"
                Types.TIMESTAMP -> "TIMESTAMP"
                Types.TINYINT -> "TINYINT"
                Types.VARBINARY -> "VARBINARY"
                Types.NVARCHAR -> "NVARCHAR"
                Types.SQLXML -> "SQLXML"
                Types.VARCHAR -> "VARCHAR"
                else -> "VARCHAR"
            }
        }

        fun cloneQuery(qry: Query?, deepCopy: Boolean): QueryImpl? {
            val newResult = QueryImpl()
            val inside: Boolean = ThreadLocalDuplication.set(qry, newResult)
            return try {
                val tmp: Array<Key?> = qry.getColumnNames()
                if (tmp != null) {
                    newResult.columnNames = arrayOfNulls<Collection.Key?>(tmp.size)
                    newResult.columns = arrayOfNulls<QueryColumnImpl?>(tmp.size)
                    var col: QueryColumn
                    for (i in tmp.indices) {
                        newResult.columnNames!![i] = tmp[i]
                        newResult.columns!![i] = QueryUtil.duplicate2QueryColumnImpl(newResult, qry.getColumn(tmp[i], null), deepCopy)
                    }
                }
                newResult.currRow = ConcurrentHashMap<Integer?, Integer?>()
                newResult.sql = qry.getSql()
                if (qry is QueryImpl) newResult.templateLine = (qry as QueryImpl?)!!.getTemplateLine() else newResult.templateLine = TemplateLine(qry.getTemplate(), 0)
                newResult.recordcount = (qry as QueryImpl?)!!.recordcount
                newResult.columncount = newResult.columnNames!!.size
                newResult.cacheType = qry.getCacheType()
                newResult.name = qry.getName()
                newResult.exeTime = qry.getExecutionTime()
                newResult.updateCount = qry.getUpdateCount()
                if (qry is QueryImpl) newResult.datasourceName = (qry as QueryImpl?)!!.getDatasourceName()
                if (qry.getGeneratedKeys() != null) cloneQuery((qry.getGeneratedKeys() as QueryImpl).also { newResult.generatedKeys = it }, false)
                newResult
            } finally {
                if (!inside) ThreadLocalDuplication.reset()
            }
        }

        init {
            useMSSQLModern = Caster.toBooleanValue(SystemUtil.getSystemPropOrEnvVar("tachyon.datasource.mssql.modern", null), false)
        }
    }

    @Override
    fun getTemplate(): String? {
        return if (templateLine == null) null else templateLine.template
    }

    @Override
    fun getTemplateLine(): TemplateLine? { // FUTURE add to interface
        return templateLine
    }

    fun getDatasourceName(): String? {
        return datasourceName
    }

    fun setTemplateLine(templateLine: TemplateLine?) {
        this.templateLine = templateLine
    }

    @Override
    fun executionTime(): Int {
        return exeTime.toInt()
    }

    /**
     * create a QueryImpl from a SQL Resultset
     *
     * @param result SQL Resultset
     * @param maxrow
     * @param name
     * @throws PageException
     */
    constructor(result: ResultSet?, maxrow: Int, name: String?, tz: TimeZone?) {
        this.name = name
        // Stopwatch stopwatch=new Stopwatch();
        // stopwatch.start();
        val start: Long = System.nanoTime()
        try {
            fillResult(this, null, null, null, result, maxrow, true, false, tz)
        } catch (e: SQLException) {
            throw DatabaseException(e, null)
        } catch (e: IOException) {
            throw Caster.toPageException(e)
        }
        exeTime = System.nanoTime() - start
    }

    /**
     * Constructor of the class only for internal usage (cloning/deserialize)
     */
    constructor() {}
    constructor(result: ResultSet?, name: String?, tz: TimeZone?) {
        this.name = name
        try {
            fillResult(this, null, null, null, result, -1, true, false, tz)
        } catch (e: SQLException) {
            throw DatabaseException(e, null)
        } catch (e: Exception) {
            throw Caster.toPageException(e)
        }
    }

    /**
     * constructor of the class, to generate a resultset from a sql query
     *
     * @param dc Connection to a database
     * @param name
     * @param sql sql to execute
     * @param maxrow maxrow for the resultset
     * @throws PageException
     */
    constructor(pc: PageContext?, dc: DatasourceConnection?, sql: SQL?, maxrow: Int, fetchsize: Int, timeout: TimeSpan?, name: String?) : this(pc, dc, sql, maxrow, fetchsize, timeout, name, null, false, true, null) {}
    constructor(pc: PageContext?, dc: DatasourceConnection?, sql: SQL?, maxrow: Int, fetchsize: Int, timeout: TimeSpan?, name: String?, templateLine: TemplateLine?,
                createUpdateData: Boolean, allowToCachePreperadeStatement: Boolean, indexName: Collection.Key?) {
        this.name = name
        this.templateLine = templateLine
        datasourceName = dc.getDatasource().getName()
        this.indexName = indexName
        this.sql = sql
        execute(pc, dc, sql, maxrow, fetchsize, timeout, createUpdateData, allowToCachePreperadeStatement, this, null, null)
    }

    private fun setGeneratedKeys(dc: DatasourceConnection?, stat: Statement?, tz: TimeZone?): Boolean {
        return try {
            val rs: ResultSet = stat.getGeneratedKeys()
            setGeneratedKeys(dc, rs, tz)
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
            false
        }
    }

    @Throws(PageException::class)
    private fun setGeneratedKeys(dc: DatasourceConnection?, rs: ResultSet?, tz: TimeZone?): Boolean {
        generatedKeys = if (DataSourceUtil.isMSSQL(dc)) {
            val results = QueryImpl(rs, "", tz)
            val columnCount = results.getColumnCount()
            // ACF compatibility action
            if (columnCount == 1) {
                results.renameEL(GENERATED_KEYS, KeyConstants._IDENTITYCOL)
                results.renameEL(GENERATEDKEYS, KeyConstants._IDENTITYCOL)
                results.renameEL(KeyConstants._ID, KeyConstants._IDENTITYCOL)
            }
            if (DataSourceUtil.isMSSQLDriver(dc) && (columnCount > 1 || results.getIndexFromKey(KeyConstants._IDENTITYCOL) == -1)) {
                return false
            }
            /*
			 * The SQL Server driver can end up advancing to a new recordset that is not part of the resultset
			 * for a INSERT/UPDATE operation. So if we do not find the identity column or we have more than once
			 * column, we should just ignore the results.
			 */

            // save the results
            results
        } else {
            QueryImpl(rs, "", tz)
        }
        return true
    }

    @Override
    fun getUpdateCount(): Int {
        return updateCount
    }

    @Override
    fun setUpdateCount(updateCount: Int) {
        this.updateCount = updateCount
    }

    @Override
    fun getGeneratedKeys(): Query? {
        return generatedKeys
    }

    @Throws(IOException::class, SQLException::class)
    private fun toBytes(blob: Blob?): Object? {
        return IOUtil.toBytes(blob.getBinaryStream())
    }

    /**
     * constructor of the class, to generate an empty resultset (no database execution)
     *
     * @param strColumns columns for the resultset
     * @param rowNumber count of rows to generate (empty fields)
     * @param name
     */
    @Deprecated
    @Deprecated("""use instead
	              <code>QueryImpl(Collection.Key[] columnKeys, int rowNumber,String name)</code>""")
    constructor(strColumns: Array<String?>?, rowNumber: Int, name: String?) {
        this.name = name
        columncount = strColumns!!.size
        recordcount.set(rowNumber)
        columnNames = arrayOfNulls<Collection.Key?>(columncount)
        columns = arrayOfNulls<QueryColumnImpl?>(columncount)
        for (i in strColumns.indices) {
            columnNames!![i] = KeyImpl.init(strColumns[i].trim())
            columns!![i] = QueryColumnImpl(this, columnNames!![i], Types.OTHER, getRecordcount())
        }
    }

    /**
     * constructor of the class, to generate an empty resultset (no database execution)
     *
     * @param strColumns columns for the resultset
     * @param rowNumber count of rows to generate (empty fields)
     * @param name
     */
    constructor(columnKeys: Array<Collection.Key?>?, rowNumber: Int, name: String?) : this(columnKeys, rowNumber, name, null) {}
    constructor(columnKeys: Array<Collection.Key?>?, rowNumber: Int, name: String?, sql: SQL?) {
        this.name = name
        columncount = columnKeys!!.size
        recordcount.set(rowNumber)
        columnNames = arrayOfNulls<Collection.Key?>(columncount)
        columns = arrayOfNulls<QueryColumnImpl?>(columncount)
        for (i in columnKeys.indices) {
            columnNames!![i] = columnKeys[i]
            columns!![i] = QueryColumnImpl(this, columnNames!![i], Types.OTHER, getRecordcount())
        }
        validateColumnNames(columnNames)
        this.sql = sql
    }

    /**
     * constructor of the class, to generate an empty resultset (no database execution)
     *
     * @param strColumns columns for the resultset
     * @param strTypes array of the types
     * @param rowNumber count of rows to generate (empty fields)
     * @param name
     * @throws DatabaseException
     */
    constructor(strColumns: Array<String?>?, strTypes: Array<String?>?, rowNumber: Int, name: String?) {
        this.name = name
        columncount = strColumns!!.size
        if (strTypes!!.size != columncount) throw DatabaseException("columns and types has not the same count", null, null, null)
        recordcount.set(rowNumber)
        columnNames = arrayOfNulls<Collection.Key?>(columncount)
        columns = arrayOfNulls<QueryColumnImpl?>(columncount)
        for (i in strColumns.indices) {
            columnNames!![i] = KeyImpl.init(strColumns[i].trim())
            columns!![i] = QueryColumnImpl(this, columnNames!![i], SQLCaster.toSQLType(strTypes[i]), getRecordcount())
        }
    }

    /**
     * constructor of the class, to generate an empty resultset (no database execution)
     *
     * @param strColumns columns for the resultset
     * @param strTypes array of the types
     * @param rowNumber count of rows to generate (empty fields)
     * @param name
     * @throws DatabaseException
     */
    constructor(columnNames: Array<Collection.Key?>?, strTypes: Array<String?>?, rowNumber: Int, name: String?) {
        this.name = name
        this.columnNames = columnNames
        columncount = columnNames!!.size
        if (strTypes!!.size != columncount) throw DatabaseException("columns and types has not the same count", null, null, null)
        recordcount.set(rowNumber)
        columns = arrayOfNulls<QueryColumnImpl?>(columncount)
        for (i in columnNames.indices) {
            columns!![i] = QueryColumnImpl(this, columnNames[i], SQLCaster.toSQLType(strTypes[i]), getRecordcount())
        }
        validateColumnNames(columnNames)
    }

    /**
     * constructor of the class, to generate an empty resultset (no database execution)
     *
     * @param arrColumns columns for the resultset
     * @param rowNumber count of rows to generate (empty fields)
     * @param name
     * @throws PageException
     */
    constructor(arrColumns: Array?, rowNumber: Int, name: String?) {
        this.name = name
        columncount = arrColumns.size()
        recordcount.set(rowNumber)
        columnNames = arrayOfNulls<Collection.Key?>(columncount)
        columns = arrayOfNulls<QueryColumnImpl?>(columncount)
        for (i in 0 until columncount) {
            columnNames!![i] = KeyImpl.init(Caster.toString(arrColumns.get(i + 1, "")).trim())
            columns!![i] = QueryColumnImpl(this, columnNames!![i], Types.OTHER, getRecordcount())
        }
        validateColumnNames(columnNames)
    }

    /**
     * constructor of the class, to generate an empty resultset (no database execution)
     *
     * @param arrColumns columns for the resultset
     * @param arrTypes type of the columns
     * @param rowNumber count of rows to generate (empty fields)
     * @param name
     * @throws PageException
     */
    constructor(arrColumns: Array?, arrTypes: Array?, rowNumber: Int, name: String?) {
        this.name = name
        columncount = arrColumns.size()
        if (arrTypes.size() !== columncount) throw DatabaseException("columns and types has not the same count", null, null, null)
        recordcount.set(rowNumber)
        columnNames = arrayOfNulls<Collection.Key?>(columncount)
        columns = arrayOfNulls<QueryColumnImpl?>(columncount)
        for (i in 0 until columncount) {
            columnNames!![i] = KeyImpl.init(arrColumns.get(i + 1, "").toString().trim())
            columns!![i] = QueryColumnImpl(this, columnNames!![i], SQLCaster.toSQLType(Caster.toString(arrTypes.get(i + 1, ""))), getRecordcount())
        }
        validateColumnNames(columnNames)
    }

    /*
	 * public QueryImpl(Collection.Key[] columnNames, QueryColumn[] columns, String name,long exeTime,
	 * boolean isCached,SQL sql) throws DatabaseException { this.columnNames=columnNames;
	 * this.columns=columns; this.exeTime=exeTime; this.isCached=isCached; this.name=name;
	 * this.columncount=columnNames.length; this.recordcount=columns.length==0?0:columns[0].size();
	 * this.sql=sql;
	 *
	 * }
	 */
    constructor(columnNames: Array<Collection.Key?>?, arrColumns: Array<Array?>?, name: String?) {
        this.name = name
        if (columnNames!!.size != arrColumns!!.size) throw DatabaseException("invalid parameter for query, not the same count from names and columns",
                "names:" + columnNames.size + ";columns:" + arrColumns.size, null, null)
        var len = 0
        columns = arrayOfNulls<QueryColumnImpl?>(arrColumns.size)
        if (arrColumns.size > 0) {
            // test columns
            len = arrColumns[0].size()
            for (i in arrColumns.indices) {
                if (arrColumns[i].size() !== len) throw DatabaseException("invalid parameter for query, all columns must have the same size",
                        "column[1]:" + len + "<>column[" + (i + 1) + "]:" + arrColumns[i].size(), null, null)
                // columns[i]=new QueryColumnTypeFlex(arrColumns[i]);
                columns!![i] = QueryColumnImpl(this, columnNames[i], arrColumns[i], Types.OTHER)
            }
            // test keys
            validateColumnNames(columnNames)
        }
        columncount = columns!!.size
        recordcount.set(len)
        this.columnNames = columnNames
    }

    /**
     * constructor of the class
     *
     * @param columnList
     * @param data
     * @param name
     * @throws DatabaseException
     */
    constructor(strColumnList: Array<String?>?, data: Array<Array<Object?>?>?, name: String?) : this(toCollKeyArr(strColumnList), data!!.size, name) {
        for (iRow in data.indices) {
            val row: Array<Object?>? = data!![iRow]
            for (iCol in row.indices) {
                // print.ln(columnList[iCol]+":"+iRow+"="+row[iCol]);
                setAtEL(columnNames!![iCol], iRow + 1, row!![iCol])
            }
        }
    }

    @Override
    fun size(): Int {
        return columncount
    }

    @Override
    fun keys(): Array<Collection.Key?>? {
        return columnNames
    }

    @Override
    fun removeEL(key: Collection.Key?): Object? {
        return setEL(key, null)
    }

    @Override
    @Throws(PageException::class)
    fun remove(key: Collection.Key?): Object? {
        return set(key, null)
    }

    @Override
    fun remove(key: Collection.Key?, defaultValue: Object?): Object? {
        return try {
            set(key, null)
        } catch (e: PageException) {
            defaultValue
        }
    }

    @Override
    fun clear() {
        for (i in columns.indices) {
            columns!![i]!!.clear()
        }
        recordcount.set(0)
    }

    @Override
    operator fun get(key: String?, defaultValue: Object?): Object? {
        return getAt(key, currRow.getOrDefault(getPid(), 1), defaultValue)
    }

    // private static int pidc=0;
    private fun getPid(): Int {
        var pc: PageContext = ThreadLocalPageContext.get()
        if (pc == null) {
            pc = CFMLEngineFactory.getInstance().getThreadPageContext()
            if (pc == null) throw RuntimeException("cannot get pid for current thread")
        }
        return pc.getId()
    }

    @Override
    operator fun get(key: Collection.Key?, defaultValue: Object?): Object? {
        return getAt(key, currRow.getOrDefault(getPid(), 1), defaultValue)
    }

    @Override
    @Throws(PageException::class)
    operator fun get(key: String?): Object? {
        return getAt(key, currRow.getOrDefault(getPid(), 1))
    }

    @Override
    @Throws(PageException::class)
    operator fun get(key: Collection.Key?): Object? {
        return getAt(key, currRow.getOrDefault(getPid(), 1))
    }

    private fun getKeyCase(pc: PageContext?): Boolean {
        var pc: PageContext? = pc
        pc = ThreadLocalPageContext.get(pc)
        return pc != null && pc.getCurrentTemplateDialect() === CFMLEngine.DIALECT_CFML && !(pc.getConfig() as ConfigWebPro).preserveCase()
    }

    @Override
    fun getAt(key: String?, row: Int, defaultValue: Object?): Object? {
        return getAt(KeyImpl.init(key), row, defaultValue)
    }

    @Override
    fun getAt(key: Collection.Key?, row: Int, defaultValue: Object?): Object? {
        val index: Int = getIndexFromKey(key)
        if (index != -1) {
            // we only return default value if row exists
            // LDEV-1201
            return if (row > 0 && row <= getRecordcount()) {
                val `val`: Object = columns!![index].get(row, CollectionUtil.NULL)
                if (`val` !== CollectionUtil.NULL) return `val`
                if (NullSupportHelper.full()) null else ""
            } else defaultValue
            // */
            // return columns[index].get(row,defaultValue);
        }
        if (key.length() >= 10) {
            if (key.equals(KeyConstants._RECORDCOUNT)) return Double.valueOf(getRecordcount())
            if (key.equals(KeyConstants._CURRENTROW)) return Double.valueOf(row)
            if (key.equals(KeyConstants._COLUMNLIST)) return getColumnlist(getKeyCase(ThreadLocalPageContext.get()))
        }
        return defaultValue
    }

    @Override
    @Throws(PageException::class)
    fun getAt(key: String?, row: Int): Object? {
        return getAt(KeyImpl.init(key), row)
    }

    @Override
    @Throws(PageException::class)
    fun getAt(key: Collection.Key?, row: Int): Object? {
        val index: Int = getIndexFromKey(key)
        if (index != -1) {
            val `val`: Object = columns!![index].get(row, CollectionUtil.NULL)
            if (`val` !== CollectionUtil.NULL) return `val`
            return if (NullSupportHelper.full()) null else ""
        }
        if (key.length() >= 10) {
            if (key.equals(KeyConstants._RECORDCOUNT)) return Double.valueOf(getRecordcount())
            if (key.equals(KeyConstants._CURRENTROW)) return Double.valueOf(row)
            if (key.equals(KeyConstants._COLUMNLIST)) return getColumnlist(getKeyCase(ThreadLocalPageContext.get()))
        }
        throw DatabaseException("Column [$key] not found in query", "available columns are [" + getColumnlist(getKeyCase(ThreadLocalPageContext.get()), ", ") + "]", sql,
                null)
    }

    @Override
    @Synchronized
    @Throws(PageException::class)
    fun removeRow(row: Int): Int {
        // disconnectCache();
        for (i in columns.indices) {
            columns!![i]!!.removeRow(row)
        }
        return recordcount.decrementAndGet()
    }

    @Override
    fun removeRowEL(row: Int): Int {
        // disconnectCache();
        return try {
            removeRow(row)
        } catch (e: PageException) {
            getRecordcount()
        }
    }

    @Override
    @Throws(DatabaseException::class)
    fun removeColumn(key: String?): QueryColumn? {
        return removeColumn(KeyImpl.init(key))
    }

    @Override
    @Throws(DatabaseException::class)
    fun removeColumn(key: Collection.Key?): QueryColumn? {
        // disconnectCache();
        val removed: QueryColumn = removeColumnEL(key)
        if (removed == null) {
            if (key.equals(KeyConstants._RECORDCOUNT) || key.equals(KeyConstants._CURRENTROW) || key.equals(KeyConstants._COLUMNLIST)) throw DatabaseException("Cannot remove [$key], it is not a column",
                    "available columns are [" + getColumnlist(getKeyCase(ThreadLocalPageContext.get()), ", ") + "]", null, null)
            throw DatabaseException("Cannot remove column [$key], it doesn't exist",
                    "available columns are [" + getColumnlist(getKeyCase(ThreadLocalPageContext.get()), ", ") + "]", null, null)
        }
        return removed
    }

    @Override
    fun removeColumnEL(key: String?): QueryColumn? {
        return removeColumnEL(KeyImpl.init(key))
    }

    @Override
    @Synchronized
    fun removeColumnEL(key: Collection.Key?): QueryColumn? {
        // TODO should in that case not all method accessing columnNames,columns been locked down?
        val index: Int = getIndexFromKey(key)
        if (index != -1) {
            var current = 0
            var removed: QueryColumn? = null
            val newColumnNames: Array<Collection.Key?> = arrayOfNulls<Collection.Key?>(columnNames!!.size - 1)
            val newColumns: Array<QueryColumnImpl?> = arrayOfNulls<QueryColumnImpl?>(columns!!.size - 1)
            for (i in columns.indices) {
                if (i == index) {
                    removed = columns!![i]
                } else {
                    newColumnNames[current] = columnNames!![i]
                    newColumns[current++] = columns!![i]
                }
            }
            columnNames = newColumnNames
            columns = newColumns
            columncount--
            return removed
        }
        return null
    }

    @Override
    fun setEL(key: String?, value: Object?): Object? {
        return setEL(KeyImpl.init(key), value)
    }

    @Override
    fun setEL(key: Collection.Key?, value: Object?): Object? {
        return setAtEL(key, currRow.getOrDefault(getPid(), 1), value)
    }

    @Override
    @Throws(PageException::class)
    operator fun set(key: String?, value: Object?): Object? {
        return set(KeyImpl.init(key), value)
    }

    @Override
    @Throws(PageException::class)
    operator fun set(key: Collection.Key?, value: Object?): Object? {
        return setAt(key, currRow.getOrDefault(getPid(), 1), value)
    }

    @Override
    @Throws(PageException::class)
    fun setAt(key: String?, row: Int, value: Object?): Object? {
        return setAt(KeyImpl.init(key), row, value)
    }

    @Override
    @Throws(PageException::class)
    fun setAt(key: Collection.Key?, row: Int, value: Object?): Object? {
        return setAt(key, row, value, false)
    }

    // Pass trustType=true to optimize operations such as QoQ where lots of values are being moved
    // around between query objects but we know the types are already fine and don't need to
    // redefine them every time
    @Throws(PageException::class)
    fun setAt(key: Collection.Key?, row: Int, value: Object?, trustType: Boolean): Object? {
        val index: Int = getIndexFromKey(key)
        if (index != -1) {
            return columns!![index].set(row, value, trustType)
        }
        throw DatabaseException("Column [$key] does not exist", "columns are [" + getColumnlist(getKeyCase(ThreadLocalPageContext.get()), ", ") + "]", sql, null)
    }

    @Override
    fun setAtEL(key: String?, row: Int, value: Object?): Object? {
        return setAtEL(KeyImpl.init(key), row, value)
    }

    @Override
    fun setAtEL(key: Collection.Key?, row: Int, value: Object?): Object? {
        val index: Int = getIndexFromKey(key)
        return if (index != -1) {
            columns!![index].setEL(row, value)
        } else null
    }

    @Override
    operator fun next(): Boolean {
        return next(getPid())
    }

    @Override
    fun next(pid: Int): Boolean {
        if (getRecordcount() >= currRow.put(pid, currRow.getOrDefault(pid, 0) + 1)) {
            return true
        }
        currRow.put(pid, 0)
        return false
    }

    @Override
    fun reset() {
        reset(getPid())
    }

    @Override
    fun reset(pid: Int) {
        currRow.remove(pid)
        // arrCurrentRow.set(pid, 0);
    }

    @Override
    fun getRecordcount(): Int {
        return recordcount.get()
    }

    protected fun getRecordcountObj(): AtomicInteger? {
        return recordcount
    }

    @Override
    fun getColumncount(): Int {
        return columncount
    }

    @Override
    fun getCurrentrow(pid: Int): Int {
        return currRow.getOrDefault(pid, 1)
    }

    /**
     * return a string list of all columns
     *
     * @return string list
     */
    fun getColumnlist(upperCase: Boolean): String? {
        return getColumnlist(upperCase, ",")
    }

    fun getColumnlist(upperCase: Boolean, delim: String?): String? {
        val sb = StringBuilder()
        for (i in columnNames.indices) {
            if (i > 0) sb.append(delim)
            sb.append(columnNames!![i].getString())
        }
        return if (upperCase) sb.toString().toUpperCase() else sb.toString()
    }

    /*
	 * public String getColumnlist() { return getColumnlist(true); }
	 */
    fun go(index: Int): Boolean {
        return go(index, getPid())
    }

    @Override
    fun go(index: Int, pid: Int): Boolean {
        if (index > 0 && index <= getRecordcount()) {
            currRow.put(pid, index)
            return true
        }
        currRow.put(pid, 0)
        return false
    }

    @Override
    fun isEmpty(): Boolean {
        return getRecordcount() + columncount == 0
    }

    @Override
    fun toDumpData(pageContext: PageContext?, maxlevel: Int, dp: DumpProperties?): DumpData? {
        return QueryUtil.toDumpData(this, pageContext, maxlevel, dp)
    }

    /**
     * sorts a query by a column
     *
     * @param column colun to sort
     * @throws PageException
     */
    @Override
    @Throws(PageException::class)
    fun sort(column: String?) {
        sort(column, Query.ORDER_ASC)
    }

    @Override
    @Throws(PageException::class)
    fun sort(column: Collection.Key?) {
        sort(column, Query.ORDER_ASC)
    }

    /**
     * sorts a query by a column
     *
     * @param strColumn column to sort
     * @param order sort type (Query.ORDER_ASC or Query.ORDER_DESC)
     * @throws PageException
     */
    @Override
    @Synchronized
    @Throws(PageException::class)
    fun sort(strColumn: String?, order: Int) {
        // disconnectCache();
        sort(getColumn(strColumn), order)
    }

    @Override
    @Synchronized
    @Throws(PageException::class)
    fun sort(keyColumn: Collection.Key?, order: Int) {
        // disconnectCache();
        sort(getColumn(keyColumn), order)
    }

    @Throws(PageException::class)
    fun sort(rows: IntArray?) {
        if (rows!!.size != getRecordcount()) throw ApplicationException("Query Sort row count is invalid, [" + rows.size + "] is not [" + getRecordcount() + "]")
        for (i in columns.indices) {
            columns!![i]!!.sort(rows)
        }
    }

    @Throws(PageException::class)
    private fun sort(column: QueryColumn?, order: Int) {
        var column: QueryColumn? = column
        val type: Int = column.getType()
        val arr: Array<SortRegister?> = ArrayUtil.toSortRegisterArray(column)
        Arrays.sort(arr, if (type == Types.BIGINT || type == Types.BIT || type == Types.INTEGER || type == Types.SMALLINT || type == Types.TINYINT || type == Types.DECIMAL || type == Types.DOUBLE || type == Types.NUMERIC || type == Types.REAL) NumberSortRegisterComparator(order == ORDER_ASC) as Comparator? else SortRegisterComparator(null, order == ORDER_ASC, false, false) as Comparator?)
        for (i in columns.indices) {
            column = columns!![i]
            val len: Int = column.size()
            val newCol = QueryColumnImpl(this, columnNames!![i], columns!![i]!!.getType(), len)
            for (y in 1..len) {
                newCol.set(y, column.get(arr[y - 1].getOldPosition() + 1, null))
            }
            columns!![i] = newCol
        }
    }

    @Override
    fun addRow(count: Int): Boolean {
        addRowAndGet(count)
        return true
    }

    @Override
    fun addRow(): Int {
        return addRowAndGet(1)
    }

    fun addRowAndGet(count: Int): Int {
        for (i in columns.indices) {
            val column: QueryColumnPro? = columns!![i]
            column.addRow(count)
        }
        return recordcount.addAndGet(count)
    }

    @Override
    @Throws(DatabaseException::class)
    fun addColumn(columnName: String?, content: Array?): Boolean {
        return addColumn(columnName, content, Types.OTHER)
    }

    @Override
    @Throws(PageException::class)
    fun addColumn(columnName: Collection.Key?, content: Array?): Boolean {
        return addColumn(columnName, content, Types.OTHER)
    }

    @Override
    @Synchronized
    @Throws(DatabaseException::class)
    fun addColumn(columnName: String?, content: Array?, type: Int): Boolean {
        return addColumn(KeyImpl.init(columnName.trim()), content, type)
    }

    @Override
    @Throws(DatabaseException::class)
    fun addColumn(columnName: Collection.Key?, content: Array?, type: Int): Boolean {
        // disconnectCache();
        // TODO Meta type
        var content: Array? = content
        if (content == null) content = ArrayImpl() else content = Duplicator.duplicate(content, false)
        if (getIndexFromKey(columnName) != -1) throw DatabaseException("Column name [" + columnName.getString().toString() + "] already exists", null, sql, null)
        if (content.size() !== getRecordcount()) {
            // throw new DatabaseException("array for the new column has not the same size like the
            // query
            // (arrayLen!=query.recordcount)");
            if (content.size() > getRecordcount()) addRow(content.size() - getRecordcount()) else content.setEL(getRecordcount(), "")
        }
        val newColumns: Array<QueryColumnImpl?> = arrayOfNulls<QueryColumnImpl?>(columns!!.size + 1)
        val newColumnNames: Array<Collection.Key?> = arrayOfNulls<Collection.Key?>(columns!!.size + 1)
        var logUsage = false
        for (i in columns.indices) {
            newColumns[i] = columns!![i]
            newColumnNames[i] = columnNames!![i]
            if (!logUsage && columns!![i] is DebugQueryColumn) logUsage = true
        }
        newColumns[columns!!.size] = QueryColumnImpl(this, columnName, content, type)
        newColumnNames[columns!!.size] = columnName
        columns = newColumns
        columnNames = newColumnNames
        columncount++
        if (logUsage) enableShowQueryUsage()
        return true
    }

    /*
	 * * if this query is still connected with cache (same query also in cache) it will disconnetd from
	 * cache (clone object and add clone to cache)
	 */
    // protected void disconnectCache() {}
    @Override
    fun clone(): Object {
        return cloneQuery(this, true)
    }

    @Override
    fun duplicate(deepCopy: Boolean): Collection? {
        return cloneQuery(this, deepCopy)
    }

    @Override
    @Synchronized
    fun getTypes(): IntArray? {
        val types = IntArray(columns!!.size)
        for (i in columns.indices) {
            types[i] = columns!![i]!!.getType()
        }
        return types
    }

    @Override
    @Synchronized
    fun getTypesAsMap(): Map<Collection.Key?, String?>? {
        val map: Map<Collection.Key?, String?> = HashMap<Collection.Key?, String?>()
        for (i in columns.indices) {
            map.put(columnNames!![i], columns!![i]!!.getTypeAsString())
        }
        return map
    }

    @Override
    @Throws(DatabaseException::class)
    fun getColumn(key: String?): QueryColumn? {
        return getColumn(KeyImpl.init(key.trim()))
    }

    @Override
    @Throws(DatabaseException::class)
    fun getColumn(key: Collection.Key?): QueryColumn? {
        val index: Int = getIndexFromKey(key)
        if (index != -1) return columns!![index]
        if (key.length() >= 10) {
            if (key.equals(KeyConstants._RECORDCOUNT)) return QueryColumnRef(this, key, Types.INTEGER)
            if (key.equals(KeyConstants._CURRENTROW)) return QueryColumnRef(this, key, Types.INTEGER)
            if (key.equals(KeyConstants._COLUMNLIST)) return QueryColumnRef(this, key, Types.INTEGER)
        }
        throw DatabaseException("Column [" + key.getString().toString() + "] not found in query, Columns are [" + getColumnlist(getKeyCase(ThreadLocalPageContext.get()), ", ").toString() + "]",
                null, sql, null)
    }

    private fun renameEL(src: Collection.Key?, trg: Collection.Key?) {
        val index: Int = getIndexFromKey(src)
        if (index != -1) {
            columnNames!![index] = trg
            columns!![index]!!.setKey(trg)
        }
    }

    @Override
    @Synchronized
    @Throws(ExpressionException::class)
    fun rename(columnName: Collection.Key?, newColumnName: Collection.Key?) {
        val index: Int = getIndexFromKey(columnName)
        if (index == -1) {
            throw ExpressionException("Cannot rename Column [" + columnName.getString().toString() + "] to [" + newColumnName.getString().toString() + "], original column doesn't exist")
        }
        columnNames!![index] = newColumnName
        columns!![index]!!.setKey(newColumnName)
    }

    @Override
    fun getColumn(key: String?, defaultValue: QueryColumn?): QueryColumn? {
        return getColumn(KeyImpl.init(key.trim()), defaultValue)
    }

    @Override
    fun getColumn(key: Collection.Key?, defaultValue: QueryColumn?): QueryColumn? {
        val index: Int = getIndexFromKey(key)
        if (index != -1) return columns!![index]
        if (key.length() >= 10) {
            if (key.equals(KeyConstants._RECORDCOUNT)) return QueryColumnRef(this, key, Types.INTEGER)
            if (key.equals(KeyConstants._CURRENTROW)) return QueryColumnRef(this, key, Types.INTEGER)
            if (key.equals(KeyConstants._COLUMNLIST)) return QueryColumnRef(this, key, Types.INTEGER)
        }
        return defaultValue
    }

    @Override
    override fun toString(): String {
        val keys: Array<Collection.Key?>? = keys()
        val sb = StringBuffer()
        sb.append("| Query: ").append(name).append("\tRecordCount: ").append(getRecordcount()).append('\n')
        if (sql != null) {
            sb.append(sql.toString() + "\n")
            sb.append("---------------------------------------------------\n")
        }
        if (exeTime > 0) {
            sb.append("Execution Time (ns): $exeTime\n")
            sb.append("---------------------------------------------------\n")
        }
        var trenner = ""
        for (i in keys.indices) {
            trenner += "+---------------------"
        }
        trenner += "+\n"
        sb.append(trenner)

        // Header
        for (i in keys.indices) {
            sb.append(getToStringField(keys!![i].getString()))
        }
        sb.append("|\n")
        sb.append(trenner.replace('-', '='))

        // body
        for (i in 0 until getRecordcount()) {
            for (y in keys.indices) {
                try {
                    val o: Object = getAt(keys!![y], i + 1)
                    if (o is String) sb.append(getToStringField(o.toString())) else if (o is Number) sb.append(getToStringField(Caster.toString(o as Number))) else if (o is Clob) sb.append(getToStringField(Caster.toString(o))) else sb.append(getToStringField(if (o == null) "[null]" else o.toString()))
                } catch (e: PageException) {
                    sb.append(getToStringField("[empty]"))
                }
            }
            sb.append("|\n")
            sb.append(trenner)
        }
        return sb.toString()
    }

    private fun getToStringField(str: String?): String? {
        return if (str == null) "|                    " else if (str.length() < 21) {
            var s = "|$str"
            for (i in str.length()..20) s += " "
            s
        } else if (str.length() === 21) "|$str" else "|" + str.substring(0, 18).toString() + "..."
    }

    private fun getIndexFromKey(key: String?): Int {
        val lc: String = StringUtil.toLowerCase(key)
        for (i in columnNames.indices) {
            if (columnNames!![i].getLowerString().equals(lc)) return i
        }
        return -1
    }

    private fun getIndexFromKey(key: Collection.Key?): Int {
        for (i in columnNames.indices) {
            if (columnNames!![i].equalsIgnoreCase(key)) return i
        }
        return -1
    }

    @Override
    fun setExecutionTime(exeTime: Long) {
        this.exeTime = exeTime
    }

    /**
     * @param maxrows
     * @return has cutted or not
     */
    @Synchronized
    fun cutRowsTo(maxrows: Int): Boolean {
        // disconnectCache();
        if (maxrows > -1 && maxrows < getRecordcount()) {
            for (i in columns.indices) {
                val column: QueryColumn? = columns!![i]
                column.cutRowsTo(maxrows)
            }
            recordcount.set(maxrows)
            return true
        }
        return false
    }

    @Override
    fun setCacheType(cacheType: String?) {
        this.cacheType = cacheType
    }

    @Override
    fun getCacheType(): String? {
        return cacheType
    }

    @Override
    fun setCached(isCached: Boolean) {
        throw RuntimeException("method no longer supported")
    }

    @Override
    fun isCached(): Boolean {
        return cacheType != null
    }

    fun getColumnName(columnIndex: Int): Key? {
        val keys: Array<Key?>? = keys()
        return if (columnIndex < 1 || columnIndex > keys!!.size) null else keys[columnIndex - 1]
    }

    @Override
    fun getColumnIndex(coulmnName: String?): Int {
        val keys: Array<Collection.Key?>? = keys()
        for (i in keys.indices) {
            if (keys!![i].getString().equalsIgnoreCase(coulmnName)) return i + 1
        }
        return -1
    }

    @Override
    fun getColumns(): Array<String?>? {
        return getColumnNamesAsString()
    }

    @Override
    fun getColumnNames(): Array<Collection.Key?>? {
        val keys: Array<Collection.Key?>? = keys()
        val rtn: Array<Collection.Key?> = arrayOfNulls<Collection.Key?>(keys!!.size)
        System.arraycopy(keys, 0, rtn, 0, keys!!.size)
        return rtn
    }

    @Override
    @Throws(PageException::class)
    fun setColumnNames(trg: Array<Collection.Key?>?) {
        columncount = trg!!.size
        var src: Array<Collection.Key?>? = keys()

        // target < source
        if (trg.size < src!!.size) {
            columnNames = arrayOfNulls<Collection.Key?>(trg.size)
            val tmp: Array<QueryColumnImpl?> = arrayOfNulls<QueryColumnImpl?>(trg.size)
            for (i in trg.indices) {
                columnNames!![i] = trg[i]
                tmp[i] = columns!![i]
                tmp[i]!!.setKey(trg[i])
            }
            columns = tmp
            return
        }
        if (trg.size > src.size) {
            val recordcount = getRecordcount()
            for (i in src.size until trg.size) {
                val arr: Array = ArrayImpl()
                for (r in 1..recordcount) {
                    arr.setE(i, "")
                }
                addColumn(trg[i], arr)
            }
            src = keys()
        }
        for (i in trg.indices) {
            columnNames!![i] = trg[i]
            columns!![i]!!.setKey(trg[i])
        }
    }

    @Override
    fun getColumnNamesAsString(): Array<String?>? {
        return CollectionUtil.keysAsString(this)
    }

    @Override
    fun getColumnCount(): Int {
        return columncount
    }

    fun getIndexes(): Map<Key?, Integer?>? {
        return indexes
    }

    @Override
    @Throws(IndexOutOfBoundsException::class)
    fun getData(row: Int, col: Int): String? {
        val keys: Array<Collection.Key?>? = keys()
        if (col < 1 || col > keys!!.size) {
            IndexOutOfBoundsException("invalid column index to retrieve Data from query, valid index goes from 1 to " + keys!!.size)
        }
        val fns: Boolean = NullSupportHelper.full()
        val _null: Object = NullSupportHelper.NULL(fns)
        val o: Object = getAt(keys!![col - 1], row, _null)
        if (o === _null) throw IndexOutOfBoundsException("invalid row index to retrieve Data from query, valid index goes from 1 to " + getRecordcount())
        return Caster.toString(o, if (fns) null else "")
    }

    @Override
    fun getName(): String? {
        return name
    }

    @Override
    fun getRowCount(): Int {
        return getRecordcount()
    }

    @Override
    @Throws(IndexOutOfBoundsException::class)
    fun setData(row: Int, col: Int, value: String?) {
        val keys: Array<Collection.Key?>? = keys()
        if (col < 1 || col > keys!!.size) {
            IndexOutOfBoundsException("invalid column index to retrieve Data from query, valid index goes from 1 to " + keys!!.size)
        }
        try {
            setAt(keys!![col - 1], row, value)
        } catch (e: PageException) {
            throw IndexOutOfBoundsException("invalid row index to retrieve Data from query, valid index goes from 1 to " + getRecordcount())
        }
    }

    @Override
    fun containsKey(key: String?): Boolean {
        return getColumn(key, null) != null
    }

    @Override
    fun containsKey(key: Collection.Key?): Boolean {
        return getColumn(key, null) != null
    }

    @Override
    @Throws(ExpressionException::class)
    fun castToString(): String? {
        throw ExpressionException("Can't cast Complex Object Type Query to String", "Use Built-In-Function \"serialize(Query):String\" to create a String from Query")
    }

    @Override
    fun castToString(defaultValue: String?): String? {
        return defaultValue
    }

    @Override
    @Throws(ExpressionException::class)
    fun castToBooleanValue(): Boolean {
        throw ExpressionException("Can't cast Complex Object Type Query to a boolean value")
    }

    @Override
    fun castToBoolean(defaultValue: Boolean?): Boolean? {
        return defaultValue
    }

    @Override
    @Throws(ExpressionException::class)
    fun castToDoubleValue(): Double {
        throw ExpressionException("Can't cast Complex Object Type Query to a number value")
    }

    @Override
    fun castToDoubleValue(defaultValue: Double): Double {
        return defaultValue
    }

    @Override
    @Throws(ExpressionException::class)
    fun castToDateTime(): DateTime? {
        throw ExpressionException("Can't cast Complex Object Type Query to a Date")
    }

    @Override
    fun castToDateTime(defaultValue: DateTime?): DateTime? {
        return defaultValue
    }

    @Override
    @Throws(ExpressionException::class)
    operator fun compareTo(b: Boolean): Int {
        throw ExpressionException("can't compare Complex Object Type Query with a boolean value")
    }

    @Override
    @Throws(PageException::class)
    operator fun compareTo(dt: DateTime?): Int {
        throw ExpressionException("can't compare Complex Object Type Query with a DateTime Object")
    }

    @Override
    @Throws(PageException::class)
    operator fun compareTo(d: Double): Int {
        throw ExpressionException("can't compare Complex Object Type Query with a numeric value")
    }

    @Override
    @Throws(PageException::class)
    operator fun compareTo(str: String?): Int {
        throw ExpressionException("can't compare Complex Object Type Query with a String")
    }

    @Override
    @Synchronized
    fun getMetaDataSimple(): Array? {
        val cols: Array = ArrayImpl()
        var column: Struct?
        for (i in columns.indices) {
            column = StructImpl()
            column.setEL(KeyConstants._name, columnNames!![i].getString())
            column.setEL("isCaseSensitive", Boolean.FALSE)
            column.setEL("typeName", columns!![i]!!.getTypeAsString())
            cols.appendEL(column)
        }
        return cols
    }

    /**
     * @return the sql
     */
    @Override
    fun getSql(): SQL? {
        return sql
    }

    @Override
    @Throws(SQLException::class)
    fun getObject(columnName: String?): Object? {
        var currentrow: Int
        return if (currRow.getOrDefault(getPid(), 0).also { currentrow = it } == 0) null else getAt(columnName, currentrow, null)
    }

    @Override
    @Throws(SQLException::class)
    fun getObject(columnIndex: Int): Object? {
        return if (columnIndex > 0 && columnIndex <= columncount) getObject(columnNames!![columnIndex - 1].getString()) else null
    }

    @Override
    @Throws(SQLException::class)
    fun getString(columnIndex: Int): String? {
        val rtn: Object = getObject(columnIndex) ?: return null
        if (Decision.isCastableToString(rtn)) return Caster.toString(rtn, null)
        throw SQLException("can't cast value to string")
    }

    @Override
    @Throws(SQLException::class)
    fun getString(columnName: String?): String? {
        val rtn: Object = getObject(columnName) ?: return null
        if (Decision.isCastableToString(rtn)) return Caster.toString(rtn, null)
        throw SQLException("can't cast value to string")
    }

    @Override
    @Throws(SQLException::class)
    fun getBoolean(columnIndex: Int): Boolean {
        val rtn: Object = getObject(columnIndex) ?: return false
        if (Decision.isCastableToBoolean(rtn)) return Caster.toBooleanValue(rtn, false)
        throw SQLException("can't cast value to boolean")
    }

    @Override
    @Throws(SQLException::class)
    fun getBoolean(columnName: String?): Boolean {
        val rtn: Object = getObject(columnName) ?: return false
        if (Decision.isCastableToBoolean(rtn)) return Caster.toBooleanValue(rtn, false)
        throw SQLException("can't cast value to boolean")
    }

    // ---------------------------------------
    @Override
    @Throws(PageException::class)
    fun call(pc: PageContext?, methodName: Key?, arguments: Array<Object?>?): Object? {
        return MemberUtil.call(pc, this, methodName, arguments, shortArrayOf(tachyon.commons.lang.CFTypes.TYPE_QUERY), arrayOf<String?>("query"))
        // return Reflector.callMethod(this,methodName,arguments);
    }

    @Override
    @Throws(PageException::class)
    fun callWithNamedValues(pc: PageContext?, methodName: Key?, args: Struct?): Object? {
        return MemberUtil.callWithNamedValues(pc, this, methodName, args, tachyon.commons.lang.CFTypes.TYPE_QUERY, "query")
    }

    @Override
    operator fun get(pc: PageContext?, key: Key?, defaultValue: Object?): Object? {
        return getAt(key, currRow.getOrDefault(pc.getId(), 1), defaultValue)
    }

    @Override
    @Throws(PageException::class)
    operator fun get(pc: PageContext?, key: Key?): Object? {
        return getAt(key, currRow.getOrDefault(pc.getId(), 1))
    }

    fun isInitalized(): Boolean {
        return true
    }

    @Override
    @Throws(PageException::class)
    operator fun set(pc: PageContext?, propertyName: Key?, value: Object?): Object? {
        return setAt(propertyName, currRow.getOrDefault(pc.getId(), 1), value)
    }

    @Override
    fun setEL(pc: PageContext?, propertyName: Key?, value: Object?): Object? {
        return setAtEL(propertyName, currRow.getOrDefault(pc.getId(), 1), value)
    }

    @Override
    fun wasNull(): Boolean {
        throw PageRuntimeException(ApplicationException("method [wasNull] is not supported"))
    }

    @Override
    @Throws(SQLException::class)
    fun absolute(row: Int): Boolean {
        if (getRecordcount() == 0) {
            if (row != 0) throw SQLException("invalid row [$row], query is Empty")
            return false
        }
        // row=row%recordcount;
        if (row > 0) currRow.put(getPid(), row) else currRow.put(getPid(), getRecordcount() + 1 + row)
        return true
    }

    @Override
    @Throws(SQLException::class)
    fun afterLast() {
        currRow.put(getPid(), getRecordcount() + 1)
    }

    @Override
    @Throws(SQLException::class)
    fun beforeFirst() {
        currRow.put(getPid(), 0)
    }

    @Override
    @Throws(SQLException::class)
    fun cancelRowUpdates() {
        // ignored
    }

    @Override
    @Throws(SQLException::class)
    fun clearWarnings() {
        // ignored
    }

    @Override
    @Throws(SQLException::class)
    fun close() {
        // ignored
    }

    @Override
    @Throws(SQLException::class)
    fun deleteRow() {
        try {
            removeRow(currRow!![getPid()])
        } catch (e: Exception) {
            throw SQLException(e.getMessage())
        }
    }

    @Override
    @Throws(SQLException::class)
    fun findColumn(columnName: String?): Int {
        val index = getColumnIndex(columnName)
        if (index == -1) throw SQLException("invald column definitions [$columnName]")
        return index
    }

    @Override
    @Throws(SQLException::class)
    fun first(): Boolean {
        return absolute(1)
    }

    @Override
    @Throws(SQLException::class)
    fun getArray(i: Int): Array? {
        throw SQLException("method is not implemented")
    }

    @Override
    @Throws(SQLException::class)
    fun getArray(colName: String?): Array? {
        throw SQLException("method is not implemented")
    }

    @Override
    @Throws(SQLException::class)
    fun getAsciiStream(columnIndex: Int): InputStream? {
        val res = getString(columnIndex) ?: return null
        return ByteArrayInputStream(res.getBytes())
    }

    @Override
    @Throws(SQLException::class)
    fun getAsciiStream(columnName: String?): InputStream? {
        val res = getString(columnName) ?: return null
        return ByteArrayInputStream(res.getBytes())
    }

    @Override
    @Throws(SQLException::class)
    fun getBigDecimal(columnIndex: Int): BigDecimal? {
        return Caster.toBigDecimal(getDouble(columnIndex))
    }

    @Override
    @Throws(SQLException::class)
    fun getBigDecimal(columnName: String?): BigDecimal? {
        return Caster.toBigDecimal(getDouble(columnName))
    }

    @Override
    @Throws(SQLException::class)
    fun getBigDecimal(columnIndex: Int, scale: Int): BigDecimal? {
        return Caster.toBigDecimal(getDouble(columnIndex))
    }

    @Override
    @Throws(SQLException::class)
    fun getBigDecimal(columnName: String?, scale: Int): BigDecimal? {
        return Caster.toBigDecimal(getDouble(columnName))
    }

    @Override
    @Throws(SQLException::class)
    fun getBinaryStream(columnIndex: Int): InputStream? {
        val obj: Object = getObject(columnIndex) ?: return null
        return try {
            Caster.toInputStream(obj, null as Charset?)
        } catch (e: Exception) {
            throw SQLException(e.getMessage())
        }
    }

    @Override
    @Throws(SQLException::class)
    fun getBinaryStream(columnName: String?): InputStream? {
        val obj: Object = getObject(columnName) ?: return null
        return try {
            Caster.toInputStream(obj, null as Charset?)
        } catch (e: Exception) {
            throw SQLException(e.getMessage())
        }
    }

    @Override
    @Throws(SQLException::class)
    fun getBlob(i: Int): Blob? {
        val bytes = getBytes(i) ?: return null
        return try {
            BlobImpl.toBlob(bytes)
        } catch (e: PageException) {
            throw SQLException(e.getMessage())
        }
    }

    @Override
    @Throws(SQLException::class)
    fun getBlob(colName: String?): Blob? {
        val bytes = getBytes(colName) ?: return null
        return try {
            BlobImpl.toBlob(bytes)
        } catch (e: PageException) {
            throw SQLException(e.getMessage())
        }
    }

    @Override
    @Throws(SQLException::class)
    fun getByte(columnIndex: Int): Byte {
        val obj: Object = getObject(columnIndex) ?: return 0.toByte()
        return try {
            Caster.toByteValue(obj)
        } catch (e: PageException) {
            throw SQLException(e.getMessage())
        }
    }

    @Override
    @Throws(SQLException::class)
    fun getByte(columnName: String?): Byte {
        val obj: Object = getObject(columnName) ?: return 0.toByte()
        return try {
            Caster.toByteValue(obj)
        } catch (e: PageException) {
            throw SQLException(e.getMessage())
        }
    }

    @Override
    @Throws(SQLException::class)
    fun getBytes(columnIndex: Int): ByteArray? {
        val obj: Object = getObject(columnIndex) ?: return null
        return try {
            Caster.toBytes(obj, null as Charset?)
        } catch (e: Exception) {
            throw SQLException(e.getMessage())
        }
    }

    @Override
    @Throws(SQLException::class)
    fun getBytes(columnName: String?): ByteArray? {
        val obj: Object = getObject(columnName) ?: return null
        return try {
            Caster.toBytes(obj, null as Charset?)
        } catch (e: Exception) {
            throw SQLException(e.getMessage())
        }
    }

    @Override
    @Throws(SQLException::class)
    fun getCharacterStream(columnIndex: Int): Reader? {
        val str = getString(columnIndex) ?: return null
        return StringReader(str)
    }

    @Override
    @Throws(SQLException::class)
    fun getCharacterStream(columnName: String?): Reader? {
        val str = getString(columnName) ?: return null
        return StringReader(str)
    }

    @Override
    @Throws(SQLException::class)
    fun getClob(i: Int): Clob? {
        val str = getString(i) ?: return null
        return ClobImpl.toClob(str)
    }

    @Override
    @Throws(SQLException::class)
    fun getClob(colName: String?): Clob? {
        val str = getString(colName) ?: return null
        return ClobImpl.toClob(str)
    }

    @Override
    @Throws(SQLException::class)
    fun getConcurrency(): Int {
        return 0
    }

    @Override
    @Throws(SQLException::class)
    fun getCursorName(): String? {
        return null
    }

    @Override
    @Throws(SQLException::class)
    fun getDate(columnIndex: Int): java.sql.Date? {
        val obj: Object = getObject(columnIndex) ?: return null
        return try {
            Date(Caster.toDate(obj, false, null).getTime())
        } catch (e: PageException) {
            throw SQLException(e.getMessage())
        }
    }

    @Override
    @Throws(SQLException::class)
    fun getDate(columnName: String?): java.sql.Date? {
        val obj: Object = getObject(columnName) ?: return null
        return try {
            Date(Caster.toDate(obj, false, null).getTime())
        } catch (e: PageException) {
            throw SQLException(e.getMessage())
        }
    }

    @Override
    @Throws(SQLException::class)
    fun getDate(columnIndex: Int, cal: Calendar?): java.sql.Date? {
        return getDate(columnIndex) // TODO impl
    }

    @Override
    @Throws(SQLException::class)
    fun getDate(columnName: String?, cal: Calendar?): java.sql.Date? {
        return getDate(columnName) // TODO impl
    }

    @Override
    @Throws(SQLException::class)
    fun getDouble(columnIndex: Int): Double {
        val obj: Object = getObject(columnIndex) ?: return 0
        return try {
            Caster.toDoubleValue(obj)
        } catch (e: PageException) {
            throw SQLException(e.getMessage())
        }
    }

    @Override
    @Throws(SQLException::class)
    fun getDouble(columnName: String?): Double {
        val obj: Object = getObject(columnName) ?: return 0
        return try {
            Caster.toDoubleValue(obj)
        } catch (e: PageException) {
            throw SQLException(e.getMessage())
        }
    }

    @Override
    @Throws(SQLException::class)
    fun getFetchDirection(): Int {
        return 1000
    }

    @Override
    @Throws(SQLException::class)
    fun getFetchSize(): Int {
        return 0
    }

    @Override
    @Throws(SQLException::class)
    fun getFloat(columnIndex: Int): Float {
        val obj: Object = getObject(columnIndex) ?: return 0
        return try {
            Caster.toFloatValue(obj)
        } catch (e: PageException) {
            throw SQLException(e.getMessage())
        }
    }

    @Override
    @Throws(SQLException::class)
    fun getFloat(columnName: String?): Float {
        val obj: Object = getObject(columnName) ?: return 0
        return try {
            Caster.toFloatValue(obj)
        } catch (e: PageException) {
            throw SQLException(e.getMessage())
        }
    }

    @Override
    @Throws(SQLException::class)
    fun getInt(columnIndex: Int): Int {
        val obj: Object = getObject(columnIndex) ?: return 0
        return try {
            Caster.toIntValue(obj)
        } catch (e: PageException) {
            throw SQLException(e.getMessage())
        }
    }

    @Override
    @Throws(SQLException::class)
    fun getInt(columnName: String?): Int {
        val obj: Object = getObject(columnName) ?: return 0
        return try {
            Caster.toIntValue(obj)
        } catch (e: PageException) {
            throw SQLException(e.getMessage())
        }
    }

    @Override
    @Throws(SQLException::class)
    fun getLong(columnIndex: Int): Long {
        val obj: Object = getObject(columnIndex) ?: return 0
        return try {
            Caster.toLongValue(obj)
        } catch (e: PageException) {
            throw SQLException(e.getMessage())
        }
    }

    @Override
    @Throws(SQLException::class)
    fun getLong(columnName: String?): Long {
        val obj: Object = getObject(columnName) ?: return 0
        return try {
            Caster.toLongValue(obj)
        } catch (e: PageException) {
            throw SQLException(e.getMessage())
        }
    }

    @Override
    @Throws(SQLException::class)
    fun getObject(i: Int, map: Map?): Object? {
        throw SQLException("method is not implemented")
    }

    @Override
    @Throws(SQLException::class)
    fun getObject(colName: String?, map: Map?): Object? {
        throw SQLException("method is not implemented")
    }

    // used only with java 7, do not set @Override
    @Override
    @Throws(SQLException::class)
    fun <T> getObject(columnIndex: Int, type: Class<T?>?): T? {
        return QueryUtil.getObject(this, columnIndex, type)
    }

    // used only with java 7, do not set @Override
    @Override
    @Throws(SQLException::class)
    fun <T> getObject(columnLabel: String?, type: Class<T?>?): T? {
        return QueryUtil.getObject(this, columnLabel, type)
    }

    @Override
    @Throws(SQLException::class)
    fun getRef(i: Int): Ref? {
        throw SQLException("method is not implemented")
    }

    @Override
    @Throws(SQLException::class)
    fun getRef(colName: String?): Ref? {
        throw SQLException("method is not implemented")
    }

    @Override
    @Throws(SQLException::class)
    fun getRow(): Int {
        return currRow.getOrDefault(getPid(), 0)
    }

    @Override
    @Throws(SQLException::class)
    fun getShort(columnIndex: Int): Short {
        val obj: Object = getObject(columnIndex) ?: return 0
        return try {
            Caster.toShortValue(obj)
        } catch (e: PageException) {
            throw SQLException(e.getMessage())
        }
    }

    @Override
    @Throws(SQLException::class)
    fun getShort(columnName: String?): Short {
        val obj: Object = getObject(columnName) ?: return 0
        return try {
            Caster.toShortValue(obj)
        } catch (e: PageException) {
            throw SQLException(e.getMessage())
        }
    }

    @Override
    @Throws(SQLException::class)
    fun getStatement(): Statement? {
        throw SQLException("method is not implemented")
    }

    @Override
    @Throws(SQLException::class)
    fun getTime(columnIndex: Int): Time? {
        val obj: Object = getObject(columnIndex) ?: return null
        return try {
            Time(DateCaster.toTime(null, obj).getTime())
        } catch (e: PageException) {
            throw SQLException(e.getMessage())
        }
    }

    @Override
    @Throws(SQLException::class)
    fun getTime(columnName: String?): Time? {
        val obj: Object = getObject(columnName) ?: return null
        return try {
            Time(DateCaster.toTime(null, obj).getTime())
        } catch (e: PageException) {
            throw SQLException(e.getMessage())
        }
    }

    @Override
    @Throws(SQLException::class)
    fun getTime(columnIndex: Int, cal: Calendar?): Time? {
        return getTime(columnIndex) // TODO impl
    }

    @Override
    @Throws(SQLException::class)
    fun getTime(columnName: String?, cal: Calendar?): Time? {
        return getTime(columnName) // TODO impl
    }

    @Override
    @Throws(SQLException::class)
    fun getTimestamp(columnIndex: Int): Timestamp? {
        val obj: Object = getObject(columnIndex) ?: return null
        return try {
            Timestamp(DateCaster.toTime(null, obj).getTime())
        } catch (e: PageException) {
            throw SQLException(e.getMessage())
        }
    }

    @Override
    @Throws(SQLException::class)
    fun getTimestamp(columnName: String?): Timestamp? {
        val obj: Object = getObject(columnName) ?: return null
        return try {
            Timestamp(DateCaster.toTime(null, obj).getTime())
        } catch (e: PageException) {
            throw SQLException(e.getMessage())
        }
    }

    @Override
    @Throws(SQLException::class)
    fun getTimestamp(columnIndex: Int, cal: Calendar?): Timestamp? {
        return getTimestamp(columnIndex) // TODO impl
    }

    @Override
    @Throws(SQLException::class)
    fun getTimestamp(columnName: String?, cal: Calendar?): Timestamp? {
        return getTimestamp(columnName) // TODO impl
    }

    @Override
    @Throws(SQLException::class)
    fun getType(): Int {
        return 0
    }

    @Override
    @Throws(SQLException::class)
    fun getURL(columnIndex: Int): URL? {
        throw SQLException("method is not implemented")
    }

    @Override
    @Throws(SQLException::class)
    fun getURL(columnName: String?): URL? {
        throw SQLException("method is not implemented")
    }

    @Override
    @Throws(SQLException::class)
    fun getUnicodeStream(columnIndex: Int): InputStream? {
        val str = getString(columnIndex) ?: return null
        return try {
            ByteArrayInputStream(str.getBytes("UTF-8"))
        } catch (e: UnsupportedEncodingException) {
            throw SQLException(e.getMessage())
        }
    }

    @Override
    @Throws(SQLException::class)
    fun getUnicodeStream(columnName: String?): InputStream? {
        val str = getString(columnName) ?: return null
        return try {
            ByteArrayInputStream(str.getBytes("UTF-8"))
        } catch (e: UnsupportedEncodingException) {
            throw SQLException(e.getMessage())
        }
    }

    @Override
    @Throws(SQLException::class)
    fun getWarnings(): SQLWarning? {
        throw SQLException("method is not implemented")
    }

    @Override
    @Throws(SQLException::class)
    fun insertRow() {
        throw SQLException("method is not implemented")
    }

    @Override
    @Throws(SQLException::class)
    fun isAfterLast(): Boolean {
        return getCurrentrow(ThreadLocalPageContext.get().getId()) > getRecordcount()
    }

    @Override
    @Throws(SQLException::class)
    fun isBeforeFirst(): Boolean {
        return currRow.getOrDefault(getPid(), 0) === 0
    }

    @Override
    @Throws(SQLException::class)
    fun isFirst(): Boolean {
        return currRow.getOrDefault(getPid(), 0) === 1
    }

    @Override
    @Throws(SQLException::class)
    fun isLast(): Boolean {
        return currRow.getOrDefault(getPid(), 0) === getRecordcount()
    }

    @Override
    @Throws(SQLException::class)
    fun last(): Boolean {
        return absolute(getRecordcount())
    }

    @Override
    @Throws(SQLException::class)
    fun moveToCurrentRow() {
        // ignore
    }

    @Override
    @Throws(SQLException::class)
    fun moveToInsertRow() {
        // ignore
    }

    @Override
    fun previous(): Boolean {
        return previous(getPid())
    }

    @Override
    fun previous(pid: Int): Boolean {
        if (0 < currRow.put(pid, currRow.getOrDefault(pid, 0) - 1)) {
            return true
        }
        currRow.put(pid, 0)
        return false
    }

    @Override
    @Throws(SQLException::class)
    fun refreshRow() {
        // ignore
    }

    @Override
    @Throws(SQLException::class)
    fun relative(rows: Int): Boolean {
        return absolute(getRow() + rows)
    }

    @Override
    @Throws(SQLException::class)
    fun rowDeleted(): Boolean {
        return false
    }

    @Override
    @Throws(SQLException::class)
    fun rowInserted(): Boolean {
        return false
    }

    @Override
    @Throws(SQLException::class)
    fun rowUpdated(): Boolean {
        return false
    }

    @Override
    @Throws(SQLException::class)
    fun setFetchDirection(direction: Int) {
        // ignore
    }

    @Override
    @Throws(SQLException::class)
    fun setFetchSize(rows: Int) {
        // ignore
    }

    @Override
    @Throws(SQLException::class)
    fun updateArray(columnIndex: Int, x: Array?) {
        updateObject(columnIndex, x.getArray())
    }

    @Override
    @Throws(SQLException::class)
    fun updateArray(columnName: String?, x: Array?) {
        updateObject(columnName, x.getArray())
    }

    @Override
    @Throws(SQLException::class)
    fun updateAsciiStream(columnIndex: Int, x: InputStream?, length: Int) {
        updateBinaryStream(columnIndex, x, length)
    }

    @Override
    @Throws(SQLException::class)
    fun updateAsciiStream(columnName: String?, x: InputStream?, length: Int) {
        updateBinaryStream(columnName, x, length)
    }

    @Override
    @Throws(SQLException::class)
    fun updateBigDecimal(columnIndex: Int, x: BigDecimal?) {
        updateObject(columnIndex, x.toString())
    }

    @Override
    @Throws(SQLException::class)
    fun updateBigDecimal(columnName: String?, x: BigDecimal?) {
        updateObject(columnName, x.toString())
    }

    @Override
    @Throws(SQLException::class)
    fun updateBinaryStream(columnIndex: Int, x: InputStream?, length: Int) {
        try {
            updateObject(columnIndex, IOUtil.toBytesMax(x, length))
        } catch (e: IOException) {
            throw SQLException(e.getMessage())
        }
    }

    @Override
    @Throws(SQLException::class)
    fun updateBinaryStream(columnName: String?, x: InputStream?, length: Int) {
        try {
            updateObject(columnName, IOUtil.toBytesMax(x, length))
        } catch (e: IOException) {
            throw SQLException(e.getMessage())
        }
    }

    @Override
    @Throws(SQLException::class)
    fun updateBlob(columnIndex: Int, x: Blob?) {
        try {
            updateObject(columnIndex, toBytes(x))
        } catch (e: IOException) {
            throw SQLException(e.getMessage())
        }
    }

    @Override
    @Throws(SQLException::class)
    fun updateBlob(columnName: String?, x: Blob?) {
        try {
            updateObject(columnName, toBytes(x))
        } catch (e: IOException) {
            throw SQLException(e.getMessage())
        }
    }

    @Override
    @Throws(SQLException::class)
    fun updateBoolean(columnIndex: Int, x: Boolean) {
        updateObject(columnIndex, Caster.toBoolean(x))
    }

    @Override
    @Throws(SQLException::class)
    fun updateBoolean(columnName: String?, x: Boolean) {
        updateObject(columnName, Caster.toBoolean(x))
    }

    @Override
    @Throws(SQLException::class)
    fun updateByte(columnIndex: Int, x: Byte) {
        updateObject(columnIndex, Byte.valueOf(x))
    }

    @Override
    @Throws(SQLException::class)
    fun updateByte(columnName: String?, x: Byte) {
        updateObject(columnName, Byte.valueOf(x))
    }

    @Override
    @Throws(SQLException::class)
    fun updateBytes(columnIndex: Int, x: ByteArray?) {
        updateObject(columnIndex, x)
    }

    @Override
    @Throws(SQLException::class)
    fun updateBytes(columnName: String?, x: ByteArray?) {
        updateObject(columnName, x)
    }

    @Override
    @Throws(SQLException::class)
    fun updateCharacterStream(columnIndex: Int, reader: Reader?, length: Int) {
        try {
            updateObject(columnIndex, IOUtil.toString(reader))
        } catch (e: Exception) {
            throw SQLException(e.getMessage())
        }
    }

    @Override
    @Throws(SQLException::class)
    fun updateCharacterStream(columnName: String?, reader: Reader?, length: Int) {
        try {
            updateObject(columnName, IOUtil.toString(reader))
        } catch (e: Exception) {
            throw SQLException(e.getMessage())
        }
    }

    @Override
    @Throws(SQLException::class)
    fun updateClob(columnIndex: Int, x: Clob?) {
        try {
            updateObject(columnIndex, toString(x))
        } catch (e: IOException) {
            throw SQLException(e.getMessage())
        }
    }

    @Override
    @Throws(SQLException::class)
    fun updateClob(columnName: String?, x: Clob?) {
        try {
            updateObject(columnName, toString(x))
        } catch (e: IOException) {
            throw SQLException(e.getMessage())
        }
    }

    @Override
    @Throws(SQLException::class)
    fun updateDate(columnIndex: Int, x: java.sql.Date?) {
        updateObject(columnIndex, Caster.toDate(x, false, null, null))
    }

    @Override
    @Throws(SQLException::class)
    fun updateDate(columnName: String?, x: java.sql.Date?) {
        updateObject(columnName, Caster.toDate(x, false, null, null))
    }

    @Override
    @Throws(SQLException::class)
    fun updateDouble(columnIndex: Int, x: Double) {
        updateObject(columnIndex, Caster.toDouble(x))
    }

    @Override
    @Throws(SQLException::class)
    fun updateDouble(columnName: String?, x: Double) {
        updateObject(columnName, Caster.toDouble(x))
    }

    @Override
    @Throws(SQLException::class)
    fun updateFloat(columnIndex: Int, x: Float) {
        updateObject(columnIndex, Caster.toDouble(x))
    }

    @Override
    @Throws(SQLException::class)
    fun updateFloat(columnName: String?, x: Float) {
        updateObject(columnName, Caster.toDouble(x))
    }

    @Override
    @Throws(SQLException::class)
    fun updateInt(columnIndex: Int, x: Int) {
        updateObject(columnIndex, Caster.toDouble(x))
    }

    @Override
    @Throws(SQLException::class)
    fun updateInt(columnName: String?, x: Int) {
        updateObject(columnName, Caster.toDouble(x))
    }

    @Override
    @Throws(SQLException::class)
    fun updateLong(columnIndex: Int, x: Long) {
        updateObject(columnIndex, Caster.toDouble(x))
    }

    @Override
    @Throws(SQLException::class)
    fun updateLong(columnName: String?, x: Long) {
        updateObject(columnName, Caster.toDouble(x))
    }

    @Override
    @Throws(SQLException::class)
    fun updateNull(columnIndex: Int) {
        updateObject(columnIndex, null)
    }

    @Override
    @Throws(SQLException::class)
    fun updateNull(columnName: String?) {
        updateObject(columnName, null)
    }

    @Override
    @Throws(SQLException::class)
    fun updateObject(columnIndex: Int, x: Object?) {
        try {
            set(getColumnName(columnIndex), x)
        } catch (e: PageException) {
            throw SQLException(e.getMessage())
        }
    }

    @Override
    @Throws(SQLException::class)
    fun updateObject(columnName: String?, x: Object?) {
        try {
            set(KeyImpl.init(columnName), x)
        } catch (e: PageException) {
            throw SQLException(e.getMessage())
        }
    }

    @Override
    @Throws(SQLException::class)
    fun updateObject(columnIndex: Int, x: Object?, scale: Int) {
        updateObject(columnIndex, x)
    }

    @Override
    @Throws(SQLException::class)
    fun updateObject(columnName: String?, x: Object?, scale: Int) {
        updateObject(columnName, x)
    }

    @Override
    @Throws(SQLException::class)
    fun updateRef(columnIndex: Int, x: Ref?) {
        updateObject(columnIndex, x.getObject())
    }

    @Override
    @Throws(SQLException::class)
    fun updateRef(columnName: String?, x: Ref?) {
        updateObject(columnName, x.getObject())
    }

    @Override
    @Throws(SQLException::class)
    fun updateRow() {
        throw SQLException("method is not implemented")
    }

    @Override
    @Throws(SQLException::class)
    fun updateShort(columnIndex: Int, x: Short) {
        updateObject(columnIndex, Caster.toDouble(x))
    }

    @Override
    @Throws(SQLException::class)
    fun updateShort(columnName: String?, x: Short) {
        updateObject(columnName, Caster.toDouble(x))
    }

    @Override
    @Throws(SQLException::class)
    fun updateString(columnIndex: Int, x: String?) {
        updateObject(columnIndex, x)
    }

    @Override
    @Throws(SQLException::class)
    fun updateString(columnName: String?, x: String?) {
        updateObject(columnName, x)
    }

    @Override
    @Throws(SQLException::class)
    fun updateTime(columnIndex: Int, x: Time?) {
        updateObject(columnIndex, DateTimeImpl(x.getTime(), false))
    }

    @Override
    @Throws(SQLException::class)
    fun updateTime(columnName: String?, x: Time?) {
        updateObject(columnName, DateTimeImpl(x.getTime(), false))
    }

    @Override
    @Throws(SQLException::class)
    fun updateTimestamp(columnIndex: Int, x: Timestamp?) {
        updateObject(columnIndex, DateTimeImpl(x.getTime(), false))
    }

    @Override
    @Throws(SQLException::class)
    fun updateTimestamp(columnName: String?, x: Timestamp?) {
        updateObject(columnName, DateTimeImpl(x.getTime(), false))
    }

    @Override
    @Throws(SQLException::class)
    fun getMetaData(): ResultSetMetaData? {
        throw SQLException("method is not implemented")
    }

    @Override
    fun keyIterator(): Iterator<Collection.Key?>? {
        return KeyIterator(keys())
    }

    @Override
    fun keysAsStringIterator(): Iterator<String?>? {
        return StringIterator(keys())
    }

    @Override
    fun entryIterator(): Iterator<Entry<Key?, Object?>?>? {
        return EntryIterator(this, keys())
    }

    @Override
    fun valueIterator(): Iterator<Object?>? {
        return CollectionIterator(keys(), this)
    }

    @Throws(IOException::class)
    fun readExternal(`in`: ObjectInput?) {
        try {
            val other = CFMLExpressionInterpreter(false).interpret(ThreadLocalPageContext.get(), `in`.readUTF()) as QueryImpl
            currRow = other.currRow
            columncount = other.columncount
            columnNames = other.columnNames
            columns = other.columns
            exeTime = other.exeTime
            generatedKeys = other.generatedKeys
            cacheType = other.cacheType
            name = other.name
            recordcount = other.recordcount
            sql = other.sql
            updateCount = other.updateCount
        } catch (e: PageException) {
            throw IOException(e.getMessage())
        }
    }

    fun writeExternal(out: ObjectOutput?) {
        try {
            out.writeUTF(ScriptConverter().serialize(this))
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
        }
    }

    @Override
    @Throws(SQLException::class)
    fun getHoldability(): Int {
        throw notSupported()
    }

    @Override
    @Throws(SQLException::class)
    fun isClosed(): Boolean {
        return false
    }

    @Override
    @Throws(SQLException::class)
    fun updateNString(columnIndex: Int, nString: String?) {
        updateString(columnIndex, nString)
    }

    @Override
    @Throws(SQLException::class)
    fun updateNString(columnLabel: String?, nString: String?) {
        updateString(columnLabel, nString)
    }

    @Override
    @Throws(SQLException::class)
    fun getNString(columnIndex: Int): String? {
        return getString(columnIndex)
    }

    @Override
    @Throws(SQLException::class)
    fun getNString(columnLabel: String?): String? {
        return getString(columnLabel)
    }

    @Override
    @Throws(SQLException::class)
    fun getNCharacterStream(columnIndex: Int): Reader? {
        return getCharacterStream(columnIndex)
    }

    @Override
    @Throws(SQLException::class)
    fun getNCharacterStream(columnLabel: String?): Reader? {
        return getCharacterStream(columnLabel)
    }

    @Override
    @Throws(SQLException::class)
    fun updateNCharacterStream(columnIndex: Int, x: Reader?, length: Long) {
        updateCharacterStream(columnIndex, x, length)
    }

    @Override
    @Throws(SQLException::class)
    fun updateNCharacterStream(columnLabel: String?, reader: Reader?, length: Long) {
        throw notSupported()
    }

    @Override
    @Throws(SQLException::class)
    fun updateAsciiStream(columnIndex: Int, x: InputStream?, length: Long) {
        throw notSupported()
    }

    @Override
    @Throws(SQLException::class)
    fun updateBinaryStream(columnIndex: Int, x: InputStream?, length: Long) {
        throw notSupported()
    }

    @Override
    @Throws(SQLException::class)
    fun updateCharacterStream(columnIndex: Int, x: Reader?, length: Long) {
        throw notSupported()
    }

    @Override
    @Throws(SQLException::class)
    fun updateAsciiStream(columnLabel: String?, x: InputStream?, length: Long) {
        throw notSupported()
    }

    @Override
    @Throws(SQLException::class)
    fun updateBinaryStream(columnLabel: String?, x: InputStream?, length: Long) {
        throw notSupported()
    }

    @Override
    @Throws(SQLException::class)
    fun updateCharacterStream(columnLabel: String?, reader: Reader?, length: Long) {
        throw notSupported()
    }

    @Override
    @Throws(SQLException::class)
    fun updateBlob(columnIndex: Int, inputStream: InputStream?, length: Long) {
        throw notSupported()
    }

    @Override
    @Throws(SQLException::class)
    fun updateBlob(columnLabel: String?, inputStream: InputStream?, length: Long) {
        throw notSupported()
    }

    @Override
    @Throws(SQLException::class)
    fun updateClob(columnIndex: Int, reader: Reader?, length: Long) {
        throw notSupported()
    }

    @Override
    @Throws(SQLException::class)
    fun updateClob(columnLabel: String?, reader: Reader?, length: Long) {
        throw notSupported()
    }

    @Override
    @Throws(SQLException::class)
    fun updateNClob(columnIndex: Int, reader: Reader?, length: Long) {
        updateClob(columnIndex, reader, length)
    }

    @Override
    @Throws(SQLException::class)
    fun updateNClob(columnLabel: String?, reader: Reader?, length: Long) {
        updateClob(columnLabel, reader, length)
    }

    @Override
    @Throws(SQLException::class)
    fun updateNCharacterStream(columnIndex: Int, x: Reader?) {
        updateCharacterStream(columnIndex, x)
    }

    @Override
    @Throws(SQLException::class)
    fun updateNCharacterStream(columnLabel: String?, reader: Reader?) {
        throw notSupported()
    }

    @Override
    @Throws(SQLException::class)
    fun updateAsciiStream(columnIndex: Int, x: InputStream?) {
        throw notSupported()
    }

    @Override
    @Throws(SQLException::class)
    fun updateBinaryStream(columnIndex: Int, x: InputStream?) {
        throw notSupported()
    }

    @Override
    @Throws(SQLException::class)
    fun updateCharacterStream(columnIndex: Int, x: Reader?) {
        throw notSupported()
    }

    @Override
    @Throws(SQLException::class)
    fun updateAsciiStream(columnLabel: String?, x: InputStream?) {
        throw notSupported()
    }

    @Override
    @Throws(SQLException::class)
    fun updateBinaryStream(columnLabel: String?, x: InputStream?) {
        throw notSupported()
    }

    @Override
    @Throws(SQLException::class)
    fun updateCharacterStream(columnLabel: String?, reader: Reader?) {
        throw notSupported()
    }

    @Override
    @Throws(SQLException::class)
    fun updateBlob(columnIndex: Int, inputStream: InputStream?) {
        throw notSupported()
    }

    @Override
    @Throws(SQLException::class)
    fun updateBlob(columnLabel: String?, inputStream: InputStream?) {
        throw notSupported()
    }

    @Override
    @Throws(SQLException::class)
    fun updateClob(columnIndex: Int, reader: Reader?) {
        throw notSupported()
    }

    @Override
    @Throws(SQLException::class)
    fun updateClob(columnLabel: String?, reader: Reader?) {
        throw notSupported()
    }

    @Override
    @Throws(SQLException::class)
    fun updateNClob(columnIndex: Int, reader: Reader?) {
        updateClob(columnIndex, reader)
    }

    @Override
    @Throws(SQLException::class)
    fun updateNClob(columnLabel: String?, reader: Reader?) {
        updateClob(columnLabel, reader)
    }

    @Override
    @Throws(SQLException::class)
    fun <T> unwrap(iface: Class<T?>?): T? {
        throw notSupported()
    }

    @Override
    @Throws(SQLException::class)
    fun isWrapperFor(iface: Class<*>?): Boolean {
        throw notSupported()
    }

    // JDK6: uncomment this for compiling with JDK6
    @Override
    @Throws(SQLException::class)
    fun updateNClob(columnIndex: Int, nClob: NClob?) {
        throw notSupported()
    }

    @Override
    @Throws(SQLException::class)
    fun updateNClob(columnLabel: String?, nClob: NClob?) {
        throw notSupported()
    }

    @Override
    @Throws(SQLException::class)
    fun getNClob(columnIndex: Int): NClob? {
        throw notSupported()
    }

    @Override
    @Throws(SQLException::class)
    fun getNClob(columnLabel: String?): NClob? {
        throw notSupported()
    }

    @Override
    @Throws(SQLException::class)
    fun getSQLXML(columnIndex: Int): SQLXML? {
        throw notSupported()
    }

    @Override
    @Throws(SQLException::class)
    fun getSQLXML(columnLabel: String?): SQLXML? {
        throw notSupported()
    }

    @Override
    @Throws(SQLException::class)
    fun updateSQLXML(columnIndex: Int, xmlObject: SQLXML?) {
        throw notSupported()
    }

    @Override
    @Throws(SQLException::class)
    fun updateSQLXML(columnLabel: String?, xmlObject: SQLXML?) {
        throw notSupported()
    }

    @Override
    @Throws(SQLException::class)
    fun getRowId(columnIndex: Int): RowId? {
        throw notSupported()
    }

    @Override
    @Throws(SQLException::class)
    fun getRowId(columnLabel: String?): RowId? {
        throw notSupported()
    }

    @Override
    @Throws(SQLException::class)
    fun updateRowId(columnIndex: Int, x: RowId?) {
        throw notSupported()
    }

    @Override
    @Throws(SQLException::class)
    fun updateRowId(columnLabel: String?, x: RowId?) {
        throw notSupported()
    }

    @Throws(PageException::class)
    fun removeRows(index: Int, count: Int) {
        QueryUtil.removeRows(this, index, count)
    }

    private fun notSupported(): SQLException? {
        return SQLException("this feature is not supported")
    }

    @Override
    @Synchronized
    fun enableShowQueryUsage() {
        if (columns != null) for (i in columns.indices) {
            columns!![i] = columns!![i]!!._toDebugColumn()
        }
    }

    @Override
    fun getExecutionTime(): Long {
        return exeTime
    }

    @Override
    fun getIterator(): Iterator<*>? {
        return ForEachQueryIterator(null, this, ThreadLocalPageContext.get().getId())
    }

    @Override
    override fun equals(obj: Object?): Boolean {
        return if (obj !is Collection) false else CollectionUtil.equals(this, obj as Collection?)
    }

    fun disableIndex() {
        if (!populating) {
            indexes = null
            indexName = null
        }
    }
}