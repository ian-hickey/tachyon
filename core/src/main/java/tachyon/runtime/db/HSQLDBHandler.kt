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
package tachyon.runtime.db

import tachyon.runtime.db.DatasourceManagerImpl.QOQ_DATASOURCE_NAME

/**
 * class to reexecute queries on the resultset object inside the cfml environment
 */
class HSQLDBHandler
/**
 * constructor of the class
 */
{
    var executer: Executer = Executer()
    var qoq: QoQ = QoQ()

    companion object {
        private const val STRING = 0
        private const val INT = 1
        private const val DOUBLE = 2
        private const val DATE = 3
        private const val TIME = 4
        private const val TIMESTAMP = 5
        private const val BINARY = 6
        private val lock: Object = SerializableObject()
        private var hsqldbDisable = false
        private var hsqldbDebug = false

        /**
         * adds a table to the memory database
         *
         * @param conn
         * @param pc
         * @param name name of the new table
         * @param query data source for table
         * @throws SQLException
         * @throws PageException
         */
        @Throws(SQLException::class, PageException::class)
        private fun addTable(conn: Connection?, pc: PageContext, name: String, query: Query, doSimpleTypes: Boolean, usedTables: ArrayList<String>) {
            val stat: Statement
            usedTables.add(name)
            stat = conn.createStatement()
            val keys: Array<Key> = CollectionUtil.keys(query)
            val types: IntArray = query.getTypes()
            val innerTypes = toInnerTypes(types)
            // CREATE STATEMENT
            var comma = ""
            val create = StringBuilder("CREATE TABLE $name (")
            val insert = StringBuilder("INSERT INTO  $name (")
            val values = StringBuilder("VALUES (")
            for (i in keys.indices) {
                val key: String = keys[i].getString()
                val type = if (doSimpleTypes) "VARCHAR_IGNORECASE" else toUsableType(types[i])
                create.append(comma + key)
                create.append(" ")
                create.append(type)
                insert.append(comma + key)
                values.append("$comma?")
                comma = ","
            }
            create.append(")")
            insert.append(")")
            values.append(")")
            stat.execute(create.toString())
            val prepStat: PreparedStatement = conn.prepareStatement(insert.toString() + values.toString())

            // INSERT STATEMENT
            // HashMap integerTypes=getIntegerTypes(types);
            val count: Int = query.getRecordcount()
            val columns: Array<QueryColumn?> = arrayOfNulls<QueryColumn>(keys.size)
            for (i in keys.indices) {
                columns[i] = query.getColumn(keys[i])
            }
            for (y in 0 until count) {
                for (i in keys.indices) {
                    val type = innerTypes[i]
                    val value: Object = columns[i].get(y + 1, null)

                    // print.out("*** "+type+":"+Caster.toString(value));
                    if (doSimpleTypes) {
                        prepStat.setObject(i + 1, Caster.toString(value))
                    } else {
                        if (value == null) prepStat.setNull(i + 1, types[i]) else if (type == BINARY) prepStat.setBytes(i + 1, Caster.toBinary(value)) else if (type == DATE) {
                            // print.out(new java.util.Date(new
                            // Date(DateCaster.toDateAdvanced(value,pc.getTimeZone()).getTime()).getTime()));
                            prepStat.setTimestamp(i + 1, if (value.equals("")) null else Timestamp(DateCaster.toDateAdvanced(query.getAt(keys[i], y + 1), pc.getTimeZone()).getTime()))
                            // prepStat.setObject(i+1,Caster.toDate(value,null));
                            // prepStat.setDate(i+1,(value==null || value.equals(""))?null:new
                            // Date(DateCaster.toDateAdvanced(value,pc.getTimeZone()).getTime()));
                        } else if (type == TIME) prepStat.setTime(i + 1, if (value.equals("")) null else Time(DateCaster.toDateAdvanced(query.getAt(keys[i], y + 1), pc.getTimeZone()).getTime())) else if (type == TIMESTAMP) prepStat.setTimestamp(i + 1, if (value.equals("")) null else Timestamp(DateCaster.toDateAdvanced(query.getAt(keys[i], y + 1), pc.getTimeZone()).getTime())) else if (type == DOUBLE) prepStat.setDouble(i + 1, if (value.equals("")) 0 else Caster.toDoubleValue(query.getAt(keys[i], y + 1))) else if (type == INT) prepStat.setLong(i + 1, if (value.equals("")) 0 else Caster.toLongValue(query.getAt(keys[i], y + 1))) else if (type == STRING) prepStat.setObject(i + 1, Caster.toString(value))
                    }
                }
                prepStat.execute()
            }
        }

        private fun toInnerTypes(types: IntArray): IntArray {
            val innerTypes = IntArray(types.size)
            for (i in types.indices) {
                val type = types[i]
                if (type == Types.BIGINT || type == Types.BIT || type == Types.INTEGER || type == Types.SMALLINT || type == Types.TINYINT) innerTypes[i] = INT else if (type == Types.DECIMAL || type == Types.DOUBLE || type == Types.NUMERIC || type == Types.REAL) innerTypes[i] = DOUBLE else if (type == Types.DATE) innerTypes[i] = DATE else if (type == Types.TIME) innerTypes[i] = TIME else if (type == Types.TIMESTAMP) innerTypes[i] = TIMESTAMP else if (type == Types.BINARY || type == Types.LONGVARBINARY || type == Types.VARBINARY) innerTypes[i] = BINARY else innerTypes[i] = STRING
            }
            return innerTypes
        }

        private fun toUsableType(type: Int): String {
            if (type == Types.NCHAR) return "CHAR"
            if (type == Types.NCLOB) return "CLOB"
            if (type == Types.NVARCHAR) return "VARCHAR_IGNORECASE"
            if (type == Types.VARCHAR) return "VARCHAR_IGNORECASE"
            return if (type == Types.JAVA_OBJECT) "VARCHAR_IGNORECASE" else QueryImpl.getColumTypeName(type)
        }

        /**
         * remove a table from the memory database
         *
         * @param conn
         * @param name
         * @throws DatabaseException
         */
        @Throws(SQLException::class)
        private fun removeTable(conn: Connection, name: String) {
            var name = name
            name = name.replace('.', '_')
            val stat: Statement = conn.createStatement()
            stat.execute("DROP TABLE $name")
            DBUtil.commitEL(conn)
        }

        /**
         * remove all table inside the memory database
         *
         * @param conn
         */
        private fun removeAll(conn: Connection, usedTables: ArrayList<String>) {
            val len: Int = usedTables.size()
            for (i in 0 until len) {
                val tableName: String = usedTables.get(i).toString()
                // print.out("remove:"+tableName);
                try {
                    removeTable(conn, tableName)
                } catch (t: Throwable) {
                    ExceptionUtil.rethrowIfNecessary(t)
                }
            }
        }

        @Throws(PageException::class)
        fun __execute(pc: PageContext, sql: SQL, maxrows: Int, fetchsize: Int, timeout: TimeSpan?, stopwatch: Stopwatch, tables: Set<String>?, doSimpleTypes: Boolean): QueryImpl? {
            var sql: SQL = sql
            val usedTables: ArrayList<String> = ArrayList<String>()
            synchronized(lock) {
                var nqr: QueryImpl? = null
                val config: ConfigPro = pc.getConfig() as ConfigPro
                var dc: DatasourceConnection? = null
                var conn: Connection? = null
                try {
                    val pool: DatasourceConnPool = config.getDatasourceConnectionPool(config.getDataSource(QOQ_DATASOURCE_NAME), "sa", "")
                    dc = pool.borrowObject()
                    conn = dc.getConnection()
                    DBUtil.setAutoCommitEL(conn, false)

                    // sql.setSQLString(HSQLUtil.sqlToZQL(sql.getSQLString(),false));
                    try {
                        val it = tables!!.iterator()
                        // int len=tables.size();
                        while (it.hasNext()) {
                            val tableName = it.next() // tables.get(i).toString();
                            val modTableName: String = tableName.replace('.', '_')
                            val modSql: String = StringUtil.replace(sql.getSQLString(), tableName, modTableName, false)
                            sql.setSQLString(modSql)
                            if (sql.getItems() != null && sql.getItems().length > 0) sql = SQLImpl(sql.toString())
                            addTable(conn, pc, modTableName, Caster.toQuery(pc.getVariable(tableName)), doSimpleTypes, usedTables)
                        }
                        DBUtil.setReadOnlyEL(conn, true)
                        try {
                            nqr = QueryImpl(pc, dc, sql, maxrows, fetchsize, timeout, "query", null, false, false, null)
                        } finally {
                            DBUtil.setReadOnlyEL(conn, false)
                            DBUtil.commitEL(conn)
                            DBUtil.setAutoCommitEL(conn, true)
                        }
                    } catch (e: SQLException) {
                        val de = DatabaseException("QoQ HSQLDB: error executing sql statement on query", null, sql, null)
                        de.setDetail(e.getMessage())
                        throw de
                    }
                } finally {
                    if (conn != null) {
                        removeAll(conn, usedTables)
                        DBUtil.setAutoCommitEL(conn, true)
                    }
                    if (dc != null) (dc as DatasourceConnectionPro).release()

                    // manager.releaseConnection(dc);
                }
                nqr.setExecutionTime(stopwatch.time())
                return nqr
            }
        }

        init {
            hsqldbDisable = Caster.toBooleanValue(SystemUtil.getSystemPropOrEnvVar("tachyon.qoq.hsqldb.disable", "false"), false)
            hsqldbDebug = Caster.toBooleanValue(SystemUtil.getSystemPropOrEnvVar("tachyon.qoq.hsqldb.debug", "false"), false)
        }
    }

    /**
     * executes a query on the queries inside the cfml environment
     *
     * @param pc Page Context
     * @param sql
     * @param maxrows
     * @return result as Query
     * @throws PageException
     * @throws PageException
     */
    @Throws(PageException::class)
    fun execute(pc: PageContext, sql: SQL, maxrows: Int, fetchsize: Int, timeout: TimeSpan): QueryImpl? {
        val stopwatch = Stopwatch(Stopwatch.UNIT_NANO)
        stopwatch.start()
        var prettySQL: String? = null
        var selects: Selects? = null
        var qoqException: Exception? = null

        // First Chance
        try {
            val parser = SelectParser()
            selects = parser.parse(sql.getSQLString())
            val q: QueryImpl = qoq.execute(pc, sql, selects, maxrows) as QueryImpl
            q.setExecutionTime(stopwatch.time())
            return q
        } catch (spe: SQLParserException) {
            qoqException = spe
            if (spe.getCause() != null && spe.getCause() is IllegalQoQException) {
                throw Caster.toPageException(spe)
            }
            prettySQL = SQLPrettyfier.prettyfie(sql.getSQLString())
            try {
                val query: QueryImpl = executer.execute(pc, sql, prettySQL, maxrows)
                query.setExecutionTime(stopwatch.time())
                return query
            } catch (ex: PageException) {
            }
        } catch (e: PageException) {
            qoqException = e
        }

        // Debugging option to completely disable HyperSQL for testing
        // Or if it's an IllegalQoQException that means, stop trying and throw the original message.
        if (qoqException != null && (hsqldbDisable || qoqException is IllegalQoQException)) {
            throw Caster.toPageException(qoqException)
        }

        // Debugging option to to log all QoQ that fall back on hsqldb in the datasource log
        if (qoqException != null && hsqldbDebug) {
            ThreadLocalPageContext.getLog(pc, "datasource").error("QoQ [" + sql.getSQLString().toString() + "] errored and is falling back to HyperSQL.", qoqException)
        }

        // SECOND Chance with hsqldb
        return try {
            var isUnion = false
            var tables: Set<String>? = null
            if (selects != null) {
                val hsql2 = HSQLUtil2(selects)
                isUnion = hsql2.isUnion()
                tables = hsql2.getInvokedTables()
            } else {
                if (prettySQL == null) prettySQL = SQLPrettyfier.prettyfie(sql.getSQLString())
                val hsql = HSQLUtil(prettySQL)
                tables = hsql.getInvokedTables()
                isUnion = hsql.isUnion()
            }
            var strSQL: String = StringUtil.replace(sql.getSQLString(), "[", "", false)
            strSQL = StringUtil.replace(strSQL, "]", "", false)
            sql.setSQLString(strSQL)
            _execute(pc, sql, maxrows, fetchsize, timeout, stopwatch, tables, isUnion)
        } catch (e: ParseException) {
            throw DatabaseException(e.getMessage(), null, sql, null)
        }
    }

    @Throws(PageException::class)
    private fun _execute(pc: PageContext, sql: SQL, maxrows: Int, fetchsize: Int, timeout: TimeSpan, stopwatch: Stopwatch, tables: Set<String>?, isUnion: Boolean): QueryImpl? {
        return try {
            __execute(pc, SQLImpl.duplicate(sql), maxrows, fetchsize, timeout, stopwatch, tables, false)
        } catch (pe: PageException) {
            if (isUnion || StringUtil.indexOf(pe.getMessage(), "NumberFormatException:") !== -1) {
                return __execute(pc, sql, maxrows, fetchsize, timeout, stopwatch, tables, true)
            }
            throw pe
        }
    }
}