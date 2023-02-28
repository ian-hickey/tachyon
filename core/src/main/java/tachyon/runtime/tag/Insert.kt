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

import java.sql.DatabaseMetaData

/**
 * Inserts records in data sources.
 *
 *
 *
 */
class Insert : TagImpl() {
    /** If specified, password overrides the password value specified in the ODBC setup.  */
    private var password: String? = null

    /** Name of the data source that contains your table.  */
    private var datasource: DataSource? = null

    /** If specified, username overrides the username value specified in the ODBC setup.  */
    private var username: String? = null

    /**
     * A comma-separated list of form fields to insert. If this attribute is not specified, all fields
     * in the form are included in the operation.
     */
    private var formfields: String? = null

    /**
     * For data sources that support table ownership such as SQL Server, Oracle, and Sybase SQL
     * Anywhere, use this field to specify the owner of the table.
     */
    private var tableowner: String? = ""

    /** Name of the table you want the form fields inserted in.  */
    private var tablename: String? = null

    /**
     * For data sources that support table qualifiers, use this field to specify the qualifier for the
     * table. The purpose of table qualifiers varies across drivers. For SQL Server and Oracle, the
     * qualifier refers to the name of the database that contains the table. For the Intersolv dBase
     * driver, the qualifier refers to the directory where the DBF files are located.
     */
    private var tablequalifier: String? = ""
    @Override
    fun release() {
        super.release()
        password = null
        username = null
        formfields = null
        tableowner = ""
        tablequalifier = ""
        datasource = null
    }

    /**
     * set the value password If specified, password overrides the password value specified in the ODBC
     * setup.
     *
     * @param password value to set
     */
    fun setPassword(password: String?) {
        this.password = password
    }

    /**
     * set the value datasource Name of the data source that contains your table.
     *
     * @param datasource value to set
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
     * set the value username If specified, username overrides the username value specified in the ODBC
     * setup.
     *
     * @param username value to set
     */
    fun setUsername(username: String?) {
        this.username = username
    }

    /**
     * set the value formfields A comma-separated list of form fields to insert. If this attribute is
     * not specified, all fields in the form are included in the operation.
     *
     * @param formfields value to set
     */
    fun setFormfields(formfields: String?) {
        this.formfields = formfields.toLowerCase().trim()
    }

    /**
     * set the value tableowner For data sources that support table ownership such as SQL Server,
     * Oracle, and Sybase SQL Anywhere, use this field to specify the owner of the table.
     *
     * @param tableowner value to set
     */
    fun setTableowner(tableowner: String?) {
        this.tableowner = tableowner
    }

    /**
     * set the value tablename Name of the table you want the form fields inserted in.
     *
     * @param tablename value to set
     */
    fun setTablename(tablename: String?) {
        this.tablename = tablename
    }

    /**
     * set the value tablequalifier For data sources that support table qualifiers, use this field to
     * specify the qualifier for the table. The purpose of table qualifiers varies across drivers. For
     * SQL Server and Oracle, the qualifier refers to the name of the database that contains the table.
     * For the Intersolv dBase driver, the qualifier refers to the directory where the DBF files are
     * located.
     *
     * @param tablequalifier value to set
     */
    fun setTablequalifier(tablequalifier: String?) {
        this.tablequalifier = tablequalifier
    }

    @Override
    fun doStartTag(): Int {
        return SKIP_BODY
    }

    @Override
    @Throws(PageException::class)
    fun doEndTag(): Int {
        val ds: Object = DBInfo.getDatasource(pageContext, datasource)
        val manager: DataSourceManager = pageContext.getDataSourceManager()
        val dc: DatasourceConnection = if (ds is DataSource) manager.getConnection(pageContext, ds as DataSource, username, password) else manager.getConnection(pageContext, Caster.toString(ds), username, password)
        return try {
            val meta: Struct? = getMeta(dc, tablequalifier, tableowner, tablename)
            val sql: SQL? = createSQL(meta)
            if (sql != null) {
                val query = QueryImpl(pageContext, dc, sql, -1, -1, null, "query")
                if (pageContext.getConfig().debug()) {
                    val dsn: String = if (ds is DataSource) (ds as DataSource).getName() else Caster.toString(ds)
                    val logdb: Boolean = (pageContext.getConfig() as ConfigPro).hasDebugOptions(ConfigPro.DEBUG_DATABASE)
                    if (logdb) {
                        val debugUsage: Boolean = DebuggerImpl.debugQueryUsage(pageContext, query as QueryResult)
                        val di: DebuggerImpl = pageContext.getDebugger() as DebuggerImpl
                        di.addQuery(if (debugUsage) query else null, dsn, "", sql, query.getRecordcount(),
                                Query.toTemplateLine(pageContext.getConfig(), sourceTemplate, pageContext.getCurrentPageSource()), query.getExecutionTime())
                    }
                }

                // log
                val log: Log = ThreadLocalPageContext.getLog(pageContext, "datasource")
                if (log.getLogLevel() >= Log.LEVEL_INFO) {
                    log.info("insert tag", "executed [" + sql.toString().trim().toString() + "] in " + DecimalFormat.call(pageContext, query.getExecutionTime() / 1000000.0).toString() + " ms")
                }
            }
            EVAL_PAGE
        } catch (pe: PageException) {
            ThreadLocalPageContext.getLog(pageContext, "datasource").error("insert tag", pe)
            throw pe
        } finally {
            manager.releaseConnection(pageContext, dc)
        }
    }

    /**
     * @param meta
     * @return return SQL String for insert
     * @throws PageException
     */
    @Throws(PageException::class)
    private fun createSQL(meta: Struct?): SQL? {
        var fields: Array<String?>? = null
        val form: Form = pageContext.formScope()
        fields = if (formfields != null) ListUtil.toStringArray(ListUtil.listToArrayRemoveEmpty(formfields, ',')) else CollectionUtil.keysAsString(pageContext.formScope())
        val names = StringBuffer()
        val values = StringBuffer()
        val items: ArrayList<SQLItem?> = ArrayList<SQLItem?>()
        var field: String?
        for (i in fields.indices) {
            field = StringUtil.trim(fields[i], null)
            if (StringUtil.startsWithIgnoreCase(field, "form.")) field = field.substring(5)
            if (!field.equalsIgnoreCase("fieldnames")) {
                if (names.length() > 0) {
                    names.append(',')
                    values.append(',')
                }
                names.append(field)
                values.append('?')
                val ci = meta.get(field, null) as ColumnInfo
                if (ci != null) items.add(SQLItemImpl(form.get(field, null), ci.type)) else items.add(SQLItemImpl(form.get(field, null)))
            }
        }
        if (items.size() === 0) return null
        val sql = StringBuffer()
        sql.append("insert into ")
        if (tablequalifier!!.length() > 0) {
            sql.append(tablequalifier)
            sql.append('.')
        }
        if (tableowner!!.length() > 0) {
            sql.append(tableowner)
            sql.append('.')
        }
        sql.append(tablename)
        sql.append('(')
        sql.append(names)
        sql.append(")values(")
        sql.append(values)
        sql.append(")")
        return SQLImpl(sql.toString(), items.toArray(arrayOfNulls<SQLItem?>(items.size())))
    }

    companion object {
        @Throws(PageException::class)
        fun getMeta(dc: DatasourceConnection?, tableQualifier: String?, tableOwner: String?, tableName: String?): Struct? {
            var columns: ResultSet? = null
            val sct: Struct = StructImpl()
            try {
                val md: DatabaseMetaData = dc.getConnection().getMetaData()
                columns = md.getColumns(tableQualifier, tableOwner, tableName, null)
                var name: String
                while (columns.next()) {
                    name = columns.getString("COLUMN_NAME")
                    sct.setEL(name, ColumnInfo(name, getInt(columns, "DATA_TYPE"), getBoolean(columns, "IS_NULLABLE")))
                }
            } catch (sqle: SQLException) {
                throw DatabaseException(sqle, dc)
            } finally {
                DBUtil.closeEL(columns)
            }
            return sct
        }

        @Throws(PageException::class, SQLException::class)
        private fun getInt(columns: ResultSet?, columnLabel: String?): Int {
            return try {
                columns.getInt(columnLabel)
            } catch (e: Exception) {
                Caster.toIntValue(columns.getObject(columnLabel))
            }
        }

        @Throws(PageException::class, SQLException::class)
        private fun getBoolean(columns: ResultSet?, columnLabel: String?): Boolean {
            return try {
                columns.getBoolean(columnLabel)
            } catch (e: Exception) {
                Caster.toBooleanValue(columns.getObject(columnLabel))
            }
        }
    }
}

internal class ColumnInfo(
        /**
         * @return the name
         */
        val name: String?,
        /**
         * @return the type
         */
        val type: Int,
        /**
         * @return the nullable
         */
        val isNullable: Boolean) {
    @Override
    override fun toString(): String {
        return name.toString() + "-" + type + "-" + isNullable
    }
}