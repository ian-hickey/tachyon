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
package tachyon.runtime.tag

import java.sql.Connection

/**
 * Handles all interactions with files. The attributes you use with cffile depend on the value of
 * the action attribute. For example, if the action = "write", use the attributes associated with
 * writing a text file.
 *
 *
 *
 */
class DBInfo : TagImpl() {
    private var datasource: DataSource? = null
    private var name: String? = null
    private var type = 0
    private var dbname: String? = null
    private var password: String? = null
    private var pattern: String? = null
    private var table: String? = null
    private var procedure: String? = null
    private var username: String? = null
    private var strType: String? = null
    private var filter: String? = null
    @Override
    fun release() {
        super.release()
        datasource = null
        name = null
        type = TYPE_NONE
        dbname = null
        password = null
        pattern = null
        table = null
        procedure = null
        username = null
        filter = null
    }

    /**
     * @param procedure the procedure to set
     */
    fun setProcedure(procedure: String?) {
        this.procedure = procedure
    }

    /**
     * @param datasource the datasource to set
     */
    @Throws(PageException::class)
    fun setDatasource(datasource: String?) { // exist for old bytecode in archives
        this.datasource = tachyon.runtime.tag.Query.toDatasource(pageContext, datasource)
    }

    @Throws(PageException::class)
    fun setDatasource(datasource: Object?) {
        this.datasource = tachyon.runtime.tag.Query.toDatasource(pageContext, datasource)
    }

    /**
     * @param name the name to set
     */
    fun setName(name: String?) {
        this.name = name
    }

    /**
     * @param type the type to set
     * @throws ApplicationException
     */
    @Throws(ApplicationException::class)
    fun setType(strType: String?) {
        var strType = strType
        this.strType = strType
        strType = strType.toLowerCase().trim()
        if ("dbnames".equals(strType)) type = TYPE_DBNAMES else if ("dbname".equals(strType)) type = TYPE_DBNAMES else if ("tables".equals(strType)) type = TYPE_TABLES else if ("table".equals(strType)) type = TYPE_TABLES else if ("columns".equals(strType)) type = TYPE_TABLE_COLUMNS else if ("columns_minimal".equals(strType)) type = TYPE_TABLE_COLUMNS else if ("column".equals(strType)) type = TYPE_TABLE_COLUMNS else if ("version".equals(strType)) type = TYPE_VERSION else if ("procedures".equals(strType)) type = TYPE_PROCEDURES else if ("procedure".equals(strType)) type = TYPE_PROCEDURES else if ("table_columns".equals(strType)) type = TYPE_TABLE_COLUMNS else if ("table_column".equals(strType)) type = TYPE_TABLE_COLUMNS else if ("column_table".equals(strType)) type = TYPE_TABLE_COLUMNS else if ("column_tables".equals(strType)) type = TYPE_TABLE_COLUMNS else if ("tablecolumns".equals(strType)) type = TYPE_TABLE_COLUMNS else if ("tablecolumn".equals(strType)) type = TYPE_TABLE_COLUMNS else if ("columntable".equals(strType)) type = TYPE_TABLE_COLUMNS else if ("columntables".equals(strType)) type = TYPE_TABLE_COLUMNS else if ("procedure_columns".equals(strType)) type = TYPE_PROCEDURE_COLUMNS else if ("procedure_column".equals(strType)) type = TYPE_PROCEDURE_COLUMNS else if ("column_procedure".equals(strType)) type = TYPE_PROCEDURE_COLUMNS else if ("column_procedures".equals(strType)) type = TYPE_PROCEDURE_COLUMNS else if ("procedurecolumns".equals(strType)) type = TYPE_PROCEDURE_COLUMNS else if ("procedurecolumn".equals(strType)) type = TYPE_PROCEDURE_COLUMNS else if ("columnprocedure".equals(strType)) type = TYPE_PROCEDURE_COLUMNS else if ("columnprocedures".equals(strType)) type = TYPE_PROCEDURE_COLUMNS else if ("foreignkeys".equals(strType)) type = TYPE_FOREIGNKEYS else if ("foreignkey".equals(strType)) type = TYPE_FOREIGNKEYS else if ("index".equals(strType)) type = TYPE_INDEX else if ("users".equals(strType)) type = TYPE_USERS else if ("user".equals(strType)) type = TYPE_USERS else if ("term".equals(strType)) type = TYPE_TERMS else if ("terms".equals(strType)) type = TYPE_TERMS else throw ApplicationException("invalid value for attribute type [$strType]",
                "valid values are [dbname,tables,columns,version,procedures,foreignkeys,index,users]")
    }

    /**
     * @param dbname the dbname to set
     */
    fun setDbname(dbname: String?) {
        this.dbname = dbname
    }

    fun setDbnames(dbname: String?) {
        this.dbname = dbname
    }

    /**
     * @param password the password to set
     */
    fun setPassword(password: String?) {
        this.password = password
    }

    /**
     * @param pattern the pattern to set
     */
    fun setPattern(pattern: String?) {
        this.pattern = pattern
    }

    /**
     * @param table the table to set
     */
    fun setTable(table: String?) {
        this.table = table
    }

    /**
     * @param username the username to set
     */
    fun setUsername(username: String?) {
        this.username = username
    }

    /**
     * @param username the username to set
     */
    fun setFilter(filter: String?) {
        this.filter = filter
    }

    @Override
    @Throws(PageException::class)
    fun doStartTag(): Int {
        val ds: Object? = getDatasource(pageContext, datasource)
        val manager: DataSourceManager = pageContext.getDataSourceManager()
        val dc: DatasourceConnection = if (ds is DataSource) manager.getConnection(pageContext, ds as DataSource?, username, password) else manager.getConnection(pageContext, Caster.toString(ds), username, password)
        try {
            if (type == TYPE_TABLE_COLUMNS) typeColumns(dc.getConnection()) else if (type == TYPE_DBNAMES) typeDBNames(dc.getConnection()) else if (type == TYPE_FOREIGNKEYS) typeForeignKeys(dc.getConnection()) else if (type == TYPE_INDEX) typeIndex(dc.getConnection()) else if (type == TYPE_PROCEDURES) typeProcedures(dc.getConnection()) else if (type == TYPE_PROCEDURE_COLUMNS) typeProcedureColumns(dc.getConnection()) else if (type == TYPE_TERMS) typeTerms(dc.getConnection().getMetaData()) else if (type == TYPE_TABLES) typeTables(dc.getConnection()) else if (type == TYPE_VERSION) typeVersion(dc.getConnection().getMetaData()) else if (type == TYPE_USERS) typeUsers(dc.getConnection())
        } catch (sqle: SQLException) {
            throw DatabaseException(sqle, dc)
        } finally {
            manager.releaseConnection(pageContext, dc)
        }
        return SKIP_BODY
    }

    @Throws(PageException::class, SQLException::class)
    private fun typeColumns(conn: Connection?) {
        val _dbName = dbname(conn)
        required("table", table)
        val metaData: DatabaseMetaData = conn.getMetaData()
        val stopwatch = Stopwatch(Stopwatch.UNIT_NANO)
        stopwatch.start()
        table = setCase(metaData, table)
        pattern = setCase(metaData, pattern)
        if (StringUtil.isEmpty(pattern, true)) pattern = null
        var schema: String? = null
        val index: Int = table.indexOf('.')
        if (index > 0) {
            schema = table.substring(0, index)
            table = table.substring(index + 1)
        }
        val qry: Query = QueryImpl(metaData.getColumns(_dbName, schema, table, if (StringUtil.isEmpty(pattern)) "%" else pattern), "query", pageContext.getTimeZone())
        val len: Int = qry.getRecordcount()
        if (len == 0) checkTable(metaData, _dbName) // only check if no columns get returned, otherwise it exists
        if (qry.getColumn(COLUMN_DEF, null) != null) qry.rename(COLUMN_DEF, COLUMN_DEFAULT_VALUE) else if (qry.getColumn(COLUMN_DEFAULT, null) != null) qry.rename(COLUMN_DEFAULT, COLUMN_DEFAULT_VALUE)

        // make sure decimal digits exists
        val col: QueryColumn = qry.getColumn(DECIMAL_DIGITS, null)
        if (col == null) {
            val arr: Array = ArrayImpl()
            for (i in 1..len) {
                arr.append(tachyon.runtime.op.Constants.DOUBLE_ZERO)
            }
            qry.addColumn(DECIMAL_DIGITS, arr)
        }
        if (!"columns_minimal".equals(strType)) {
            // add is primary
            val primaries: Map<String?, Set<String?>?> = HashMap()
            val isPrimary: Array = ArrayImpl()
            var set: Set<String?>?
            var o: Object
            var tblCat: String
            var tblScheme: String
            var tblName: String
            for (i in 1..len) {

                // decimal digits
                o = qry.getAt(DECIMAL_DIGITS, i, null)
                if (o == null) qry.setAtEL(DECIMAL_DIGITS, i, tachyon.runtime.op.Constants.DOUBLE_ZERO)
                tblCat = StringUtil.emptyAsNull(Caster.toString(qry.getAt(TABLE_CAT, i), null), true)
                tblScheme = StringUtil.emptyAsNull(Caster.toString(qry.getAt(TABLE_SCHEM, i), null), true)
                tblName = StringUtil.emptyAsNull(Caster.toString(qry.getAt(TABLE_NAME, i), null), true)
                set = primaries[tblName]
                if (set == null) {
                    try {
                        set = toSet(metaData.getPrimaryKeys(tblCat, tblScheme, tblName), true, "COLUMN_NAME")
                        primaries.put(tblName, set)
                    } catch (e: Exception) {
                    }
                }
                isPrimary.append(if (set != null && set.contains(qry.getAt(COLUMN_NAME, i))) "YES" else "NO")
            }
            qry.addColumn(IS_PRIMARYKEY, isPrimary)

            // add is foreignkey
            val foreigns: Map = HashMap()
            val isForeign: Array = ArrayImpl()
            val refPrim: Array = ArrayImpl()
            val refPrimTbl: Array = ArrayImpl()
            // Map map,inner;
            var map: Map<String?, Map<String?, SVArray?>?>?
            var inner: Map<String?, SVArray?>?
            for (i in 1..len) {
                tblCat = StringUtil.emptyAsNull(Caster.toString(qry.getAt(TABLE_CAT, i), null), true)
                tblScheme = StringUtil.emptyAsNull(Caster.toString(qry.getAt(TABLE_SCHEM, i), null), true)
                tblName = StringUtil.emptyAsNull(Caster.toString(qry.getAt(TABLE_NAME, i), null), true)
                map = foreigns.get(tblName)
                if (map == null) {
                    map = toMap(metaData.getImportedKeys(tblCat, tblScheme, tblName), true, "FKCOLUMN_NAME", arrayOf("PKCOLUMN_NAME", "PKTABLE_NAME"))
                    foreigns.put(tblName, map)
                }
                inner = map!![qry.getAt(COLUMN_NAME, i)]
                if (inner != null) {
                    isForeign.append("YES")
                    refPrim.append(inner["PKCOLUMN_NAME"])
                    refPrimTbl.append(inner["PKTABLE_NAME"])
                } else {
                    isForeign.append("NO")
                    refPrim.append("N/A")
                    refPrimTbl.append("N/A")
                }
            }
            qry.addColumn(IS_FOREIGNKEY, isForeign)
            qry.addColumn(REFERENCED_PRIMARYKEY, refPrim)
            qry.addColumn(REFERENCED_PRIMARYKEY_TABLE, refPrimTbl)
        }
        qry.setExecutionTime(stopwatch!!.time())
        pageContext.setVariable(name, qry)
    }

    @Throws(SQLException::class)
    private fun toMap(result: ResultSet?, closeResult: Boolean, columnName: String?, additional: Array<String?>?): Map<String?, Map<String?, SVArray?>?>? {
        val map: Map<String?, Map<String?, SVArray?>?> = HashMap<String?, Map<String?, SVArray?>?>()
        var inner: Map<String?, SVArray?>?
        var col: String
        var item: SVArray?
        if (result == null) return map
        try {
            while (result.next()) {
                col = result.getString(columnName)
                inner = map[col]
                if (inner != null) {
                    for (i in additional.indices) {
                        item = inner[additional!![i]]
                        item.add(result.getString(additional[i]))
                        item.setPosition(item.size())
                    }
                } else {
                    inner = HashMap<String?, SVArray?>()
                    map.put(col, inner)
                    for (i in additional.indices) {
                        item = SVArray()
                        item.add(result.getString(additional!![i]))
                        inner.put(additional[i], item)
                    }
                }
            }
        } finally {
            if (closeResult) IOUtil.close(result)
        }
        return map
    }

    @Throws(SQLException::class)
    private fun toSet(result: ResultSet?, closeResult: Boolean, columnName: String?): Set<String?>? {
        val set: Set<String?> = HashSet<String?>()
        return if (result == null) set else try {
            while (result.next()) {
                set.add(result.getString(columnName))
            }
            set
        } finally {
            if (closeResult) IOUtil.close(result)
        }
    }

    @Throws(PageException::class, SQLException::class)
    private fun typeDBNames(conn: Connection?) {
        val stopwatch = Stopwatch(Stopwatch.UNIT_NANO)
        stopwatch.start()
        val metaData: DatabaseMetaData = conn.getMetaData()
        val catalogs: tachyon.runtime.type.Query = QueryImpl(metaData.getCatalogs(), "query", pageContext.getTimeZone())
        val scheme: tachyon.runtime.type.Query = QueryImpl(metaData.getSchemas(dbname(conn), null), "query", pageContext.getTimeZone())
        var p: Pattern? = null
        if (pattern != null && !"%".equals(pattern)) p = SQLUtil.pattern(pattern, true)
        val columns = arrayOf<String?>("database_name", "type")
        val types = arrayOf<String?>("VARCHAR", "VARCHAR")
        val qry: tachyon.runtime.type.Query = QueryImpl(columns, types, 0, "query")
        var row = 1
        var len: Int = catalogs.getRecordcount()
        var value: String
        // catalog
        for (i in 1..len) {
            value = catalogs.getAt(TABLE_CAT, i)
            if (!matchPattern(value, p)) continue
            qry.addRow()
            qry.setAt(DATABASE_NAME, row, value)
            qry.setAt(KeyConstants._type, row, "CATALOG")
            row++
        }
        // scheme
        len = scheme.getRecordcount()
        for (i in 1..len) {
            value = scheme.getAt(TABLE_SCHEM, i)
            if (!matchPattern(value, p)) continue
            qry.addRow()
            qry.setAt(DATABASE_NAME, row, value)
            qry.setAt(KeyConstants._type, row, "SCHEMA")
            row++
        }
        qry.setExecutionTime(stopwatch!!.time())
        pageContext.setVariable(name, qry)
    }

    @Throws(PageException::class, SQLException::class)
    private fun typeForeignKeys(conn: Connection?) {
        required("table", table)
        val metaData: DatabaseMetaData = conn.getMetaData()
        val _dbName = dbname(conn)
        val stopwatch = Stopwatch(Stopwatch.UNIT_NANO)
        stopwatch.start()
        table = setCase(metaData, table)
        val index: Int = table.indexOf('.')
        var schema: String? = null
        if (index > 0) {
            schema = table.substring(0, index)
            table = table.substring(index + 1)
        }
        checkTable(metaData, _dbName)
        val qry: tachyon.runtime.type.Query = QueryImpl(metaData.getExportedKeys(_dbName, schema, table), "query", pageContext.getTimeZone())
        qry.setExecutionTime(stopwatch!!.time())
        pageContext.setVariable(name, qry)
    }

    @Throws(SQLException::class, ApplicationException::class)
    private fun checkTable(metaData: DatabaseMetaData?, _dbName: String?) {
        var tables: ResultSet? = null
        if (StringUtil.isEmpty(table)) return
        try {
            tables = metaData.getTables(_dbName, null, setCase(metaData, table), null)
            if (!tables.next()) throw ApplicationException("there is no table that match the following pattern [$table]")
        } finally {
            if (tables != null) tables.close()
        }
    }

    @Throws(SQLException::class)
    private fun setCase(metaData: DatabaseMetaData?, id: String?): String? {
        if (StringUtil.isEmpty(id)) return "%"
        if (metaData.storesLowerCaseIdentifiers()) return id.toLowerCase()
        return if (metaData.storesUpperCaseIdentifiers()) id.toUpperCase() else id
    }

    @Throws(PageException::class, SQLException::class)
    private fun typeIndex(conn: Connection?) {
        required("table", table)
        val metaData: DatabaseMetaData = conn.getMetaData()
        val _dbName = dbname(conn)
        val stopwatch = Stopwatch(Stopwatch.UNIT_NANO)
        stopwatch.start()
        table = setCase(metaData, table)
        val index: Int = table.indexOf('.')
        var schema: String? = null
        if (index > 0) {
            schema = table.substring(0, index)
            table = table.substring(index + 1)
        }
        checkTable(metaData, _dbName)
        val tables: ResultSet = metaData.getIndexInfo(_dbName, schema, table, false, true)
        val qry: tachyon.runtime.type.Query = QueryImpl(tables, "query", pageContext.getTimeZone())

        // type int 2 string
        val rows: Int = qry.getRecordcount()
        var strType: String
        var type: Int
        var card: Int
        for (row in 1..rows) {

            // type
            when (Caster.toIntValue(qry.getAt(KeyConstants._type, row)).also { type = it }) {
                0 -> strType = "Table Statistic"
                1 -> strType = "Clustered Index"
                2 -> strType = "Hashed Index"
                3 -> strType = "Other Index"
                else -> strType = Caster.toString(type)
            }
            qry.setAt(KeyConstants._type, row, strType)

            // CARDINALITY
            card = Caster.toIntValue(qry.getAt(CARDINALITY, row), 0)
            qry.setAt(CARDINALITY, row, Caster.toDouble(card))
        }
        qry.setExecutionTime(stopwatch!!.time())
        pageContext.setVariable(name, qry)
    }

    @Throws(SQLException::class, PageException::class)
    private fun typeProcedures(conn: Connection?) {
        val stopwatch = Stopwatch(Stopwatch.UNIT_NANO)
        stopwatch.start()
        val metaData: DatabaseMetaData = conn.getMetaData()
        val schema: String? = null
        pattern = setCase(metaData, pattern)
        if (StringUtil.isEmpty(pattern, true)) {
            pattern = null
        }
        val qry: tachyon.runtime.type.Query = QueryImpl(metaData.getProcedures(dbname(conn), schema, if (StringUtil.isEmpty(pattern)) "%" else pattern), "query", pageContext.getTimeZone())
        qry.setExecutionTime(stopwatch!!.time())
        pageContext.setVariable(name, qry)
    }

    @Throws(SQLException::class, PageException::class)
    private fun typeProcedureColumns(conn: Connection?) {
        required("procedure", procedure)
        val metaData: DatabaseMetaData = conn.getMetaData()
        val stopwatch = Stopwatch(Stopwatch.UNIT_NANO)
        stopwatch.start()
        procedure = setCase(metaData, procedure)
        pattern = setCase(metaData, pattern)
        if (StringUtil.isEmpty(pattern, true)) pattern = null
        var schema: String? = null
        val index: Int = procedure.indexOf('.')
        if (index > 0) {
            schema = procedure.substring(0, index)
            procedure = procedure.substring(index + 1)
        }
        val qry: tachyon.runtime.type.Query = QueryImpl(
                metaData.getProcedureColumns(dbname(conn), schema, if (StringUtil.isEmpty(procedure)) "%" else procedure, if (StringUtil.isEmpty(pattern)) "%" else pattern), "query",
                pageContext.getTimeZone())
        qry.setExecutionTime(stopwatch!!.time())
        pageContext.setVariable(name, qry)
    }

    @Throws(SQLException::class, PageException::class)
    private fun typeTerms(metaData: DatabaseMetaData?) {
        val sct: Struct = StructImpl()
        sct.setEL(PROCEDURE, metaData.getProcedureTerm())
        sct.setEL(CATALOG, metaData.getCatalogTerm())
        sct.setEL(SCHEMA, metaData.getSchemaTerm())
        pageContext.setVariable(name, sct)
    }

    @Throws(PageException::class, SQLException::class)
    private fun typeTables(conn: Connection?) {
        val metaData: DatabaseMetaData = conn.getMetaData()
        val stopwatch = Stopwatch(Stopwatch.UNIT_NANO)
        stopwatch.start()
        pattern = setCase(metaData, pattern)
        val qry: tachyon.runtime.type.Query = QueryImpl(metaData.getTables(dbname(conn), null, if (StringUtil.isEmpty(pattern)) "%" else pattern,
                if (StringUtil.isEmpty(filter)) null else arrayOf(filter)), "query", pageContext.getTimeZone())
        qry.setExecutionTime(stopwatch!!.time())
        pageContext.setVariable(name, qry)
    }

    private fun dbname(conn: Connection?): String? {
        return if (!StringUtil.isEmpty(dbname, true)) dbname.trim() else try {
            conn.getCatalog()
        } catch (e: SQLException) {
            null
        }
    }

    @Throws(PageException::class, SQLException::class)
    private fun typeVersion(metaData: DatabaseMetaData?) {
        val stopwatch = Stopwatch(Stopwatch.UNIT_NANO)
        stopwatch.start()
        val columns: Array<Key?> = arrayOf<Key?>(DATABASE_PRODUCTNAME, DATABASE_VERSION, DRIVER_NAME, DRIVER_VERSION, JDBC_MAJOR_VERSION, JDBC_MINOR_VERSION)
        val types = arrayOf<String?>("VARCHAR", "VARCHAR", "VARCHAR", "VARCHAR", "DOUBLE", "DOUBLE")
        val qry: tachyon.runtime.type.Query = QueryImpl(columns, types, 1, "query")
        qry.setAt(DATABASE_PRODUCTNAME, 1, metaData.getDatabaseProductName())
        qry.setAt(DATABASE_VERSION, 1, metaData.getDatabaseProductVersion())
        qry.setAt(DRIVER_NAME, 1, metaData.getDriverName())
        qry.setAt(DRIVER_VERSION, 1, metaData.getDriverVersion())
        qry.setAt(JDBC_MAJOR_VERSION, 1, Double.valueOf(metaData.getJDBCMajorVersion()))
        qry.setAt(JDBC_MINOR_VERSION, 1, Double.valueOf(metaData.getJDBCMinorVersion()))
        qry.setExecutionTime(stopwatch!!.time())
        pageContext.setVariable(name, qry)
    }

    @Throws(PageException::class, SQLException::class)
    private fun typeUsers(conn: Connection?) {
        val stopwatch = Stopwatch(Stopwatch.UNIT_NANO)
        stopwatch.start()
        val metaData: DatabaseMetaData = conn.getMetaData()
        checkTable(metaData, dbname(conn))
        val result: ResultSet = metaData.getSchemas()
        val qry: Query = QueryImpl(result, "query", pageContext.getTimeZone())
        qry.rename(TABLE_SCHEM, USER)
        qry.setExecutionTime(stopwatch!!.time())
        pageContext.setVariable(name, qry)
    }

    @Throws(ApplicationException::class)
    private fun required(name: String?, value: String?) {
        if (value == null) throw ApplicationException("Missing attribute [$name]. The type [$strType] requires the attribute [$name].")
    }

    @Override
    fun doEndTag(): Int {
        return EVAL_PAGE
    }

    companion object {
        private val TABLE_NAME: Key? = KeyImpl.getInstance("TABLE_NAME")
        private val COLUMN_NAME: Key? = KeyImpl.getInstance("COLUMN_NAME")
        private val IS_PRIMARYKEY: Key? = KeyImpl.getInstance("IS_PRIMARYKEY")
        private val IS_FOREIGNKEY: Key? = KeyImpl.getInstance("IS_FOREIGNKEY")
        private val COLUMN_DEF: Key? = KeyImpl.getInstance("COLUMN_DEF")
        private val COLUMN_DEFAULT_VALUE: Key? = KeyImpl.getInstance("COLUMN_DEFAULT_VALUE")
        private val COLUMN_DEFAULT: Key? = KeyImpl.getInstance("COLUMN_DEFAULT")
        private val REFERENCED_PRIMARYKEY: Key? = KeyImpl.getInstance("REFERENCED_PRIMARYKEY")
        private val REFERENCED_PRIMARYKEY_TABLE: Key? = KeyImpl.getInstance("REFERENCED_PRIMARYKEY_TABLE")
        private val USER: Key? = KeyConstants._USER
        private val TABLE_SCHEM: Key? = KeyImpl.getInstance("TABLE_SCHEM")
        private val DECIMAL_DIGITS: Key? = KeyImpl.getInstance("DECIMAL_DIGITS")
        private val DATABASE_NAME: Key? = KeyImpl.getInstance("database_name")
        private val TABLE_CAT: Key? = KeyImpl.getInstance("TABLE_CAT")
        private val PROCEDURE: Key? = KeyImpl.getInstance("procedure")
        private val CATALOG: Key? = KeyConstants._catalog
        private val SCHEMA: Key? = KeyConstants._schema
        private val DATABASE_PRODUCTNAME: Key? = KeyImpl.getInstance("DATABASE_PRODUCTNAME")
        private val DATABASE_VERSION: Key? = KeyImpl.getInstance("DATABASE_VERSION")
        private val DRIVER_NAME: Key? = KeyImpl.getInstance("DRIVER_NAME")
        private val DRIVER_VERSION: Key? = KeyImpl.getInstance("DRIVER_VERSION")
        private val JDBC_MAJOR_VERSION: Key? = KeyImpl.getInstance("JDBC_MAJOR_VERSION")
        private val JDBC_MINOR_VERSION: Key? = KeyImpl.getInstance("JDBC_MINOR_VERSION")
        private const val TYPE_NONE = 0
        private const val TYPE_DBNAMES = 1
        private const val TYPE_TABLES = 2
        private const val TYPE_TABLE_COLUMNS = 3
        private const val TYPE_VERSION = 4
        private const val TYPE_PROCEDURES = 5
        private const val TYPE_PROCEDURE_COLUMNS = 6
        private const val TYPE_FOREIGNKEYS = 7
        private const val TYPE_INDEX = 8
        private const val TYPE_USERS = 9
        private const val TYPE_TERMS = 10
        private val CARDINALITY: Collection.Key? = KeyImpl.getInstance("CARDINALITY")
        private fun matchPattern(value: String?, pattern: Pattern?): Boolean {
            return if (pattern == null) true else SQLUtil.match(pattern, value)
        }

        @Throws(ApplicationException::class)
        fun getDatasource(pageContext: PageContext?, datasource: DataSource?): Object? {
            if (datasource == null) {
                val ds: Object = pageContext.getApplicationContext().getDefDataSource()
                if (StringUtil.isEmpty(ds)) {
                    val isCFML = pageContext.getRequestDialect() === CFMLEngine.DIALECT_CFML
                    throw ApplicationException("attribute [datasource] is required, when no default datasource is defined",
                            ("you can define a default datasource as attribute [defaultdatasource] of the tag "
                                    + if (isCFML) Constants.CFML_APPLICATION_TAG_NAME else Constants.LUCEE_APPLICATION_TAG_NAME) + " or as data member of the "
                                    + (if (isCFML) Constants.CFML_APPLICATION_EVENT_HANDLER else Constants.LUCEE_APPLICATION_EVENT_HANDLER).toString() + " (this.defaultdatasource=\"mydatasource\";)")
                }
                return ds
            }
            return datasource
        }
    }
}