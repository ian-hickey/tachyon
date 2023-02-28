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
package lucee.runtime.tag

import java.sql.DatabaseMetaData

/**
 * Updates existing records in data sources.
 *
 *
 *
 */
class Update : TagImpl() {
    /** If specified, password overrides the password value specified in the ODBC setup.  */
    private var password: String? = null

    /** Name of the data source that contains a table.  */
    private var datasource: DataSource? = null

    /** If specified, username overrides the username value specified in the ODBC setup.  */
    private var username: String? = null

    /**
     * A comma-separated list of form fields to update. If this attribute is not specified, all fields
     * in the form are included in the operation.
     */
    private var formfields: String? = null

    /**
     * For data sources that support table ownership, for example, SQL Server, Oracle, and Sybase SQL
     * Anywhere, use this field to specify the owner of the table.
     */
    private var tableowner: String? = null

    /** Name of the table you want to update.  */
    private var tablename: String? = null

    /**
     * For data sources that support table qualifiers, use this field to specify the qualifier for the
     * table. The purpose of table qualifiers varies across drivers. For SQL Server and Oracle, the
     * qualifier refers to the name of the database that contains the table. For the Intersolv dBase
     * driver, the qualifier refers to the directory where the DBF files are located.
     */
    private var tablequalifier: String? = null
    @Override
    fun release() {
        super.release()
        password = null
        username = null
        formfields = null
        tableowner = null
        tablequalifier = null
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
     * set the value datasource Name of the data source that contains a table.
     *
     * @param datasource value to set
     */
    @Throws(PageException::class)
    fun setDatasource(datasource: String?) { // exist for old bytecode in archives
        this.datasource = lucee.runtime.tag.Query.toDatasource(pageContext, datasource)
    }

    @Throws(PageException::class)
    fun setDatasource(datasource: Object?) {
        this.datasource = lucee.runtime.tag.Query.toDatasource(pageContext, datasource)
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
     * set the value formfields A comma-separated list of form fields to update. If this attribute is
     * not specified, all fields in the form are included in the operation.
     *
     * @param formfields value to set
     */
    fun setFormfields(formfields: String?) {
        this.formfields = formfields
    }

    /**
     * set the value tableowner For data sources that support table ownership, for example, SQL Server,
     * Oracle, and Sybase SQL Anywhere, use this field to specify the owner of the table.
     *
     * @param tableowner value to set
     */
    fun setTableowner(tableowner: String?) {
        this.tableowner = tableowner
    }

    /**
     * set the value tablename Name of the table you want to update.
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
            val meta: Struct = Insert.getMeta(dc, tablequalifier, tableowner, tablename)
            val pKeys = getPrimaryKeys(dc)
            val sql: SQL? = createSQL(dc, pKeys, meta)
            if (sql != null) {
                val query = QueryImpl(pageContext, dc, sql, -1, -1, null, "query")
                if (pageContext.getConfig().debug()) {
                    val dsn: String = if (ds is DataSource) (ds as DataSource).getName() else Caster.toString(ds)
                    val logdb: Boolean = (pageContext.getConfig() as ConfigPro).hasDebugOptions(ConfigPro.DEBUG_DATABASE)
                    if (logdb) {
                        val debugUsage: Boolean = DebuggerUtil.debugQueryUsage(pageContext, query)
                        val di: DebuggerImpl = pageContext.getDebugger() as DebuggerImpl
                        di.addQuery(if (debugUsage) query else null, dsn, "", sql, query.getRecordcount(),
                                Query.toTemplateLine(pageContext.getConfig(), sourceTemplate, pageContext.getCurrentPageSource()), query.getExecutionTime())
                    }
                }

                // log
                val log: Log = ThreadLocalPageContext.getLog(pageContext, "datasource")
                if (log.getLogLevel() >= Log.LEVEL_INFO) {
                    log.info("update tag", "executed [" + sql.toString().trim().toString() + "] in " + DecimalFormat.call(pageContext, query.getExecutionTime() / 1000000.0).toString() + " ms")
                }
            }
            EVAL_PAGE
        } catch (pe: PageException) {
            ThreadLocalPageContext.getLog(pageContext, "datasource").error("update tag", pe)
            throw pe
        } finally {
            manager.releaseConnection(pageContext, dc)
        }
    }

    @Throws(PageException::class)
    private fun getPrimaryKeys(dc: DatasourceConnection?): Array<String?>? {
        val query: lucee.runtime.type.Query? = getPrimaryKeysAsQuery(dc)
        val recCount: Int = query.getRecordcount()
        val pKeys = arrayOfNulls<String?>(recCount)
        if (recCount == 0) throw DatabaseException("can't find primary keys of table [$tablename]", null, null, dc)
        for (row in 1..recCount) {
            pKeys[row - 1] = Caster.toString(query.getAt("column_name", row))
        }
        return pKeys
    }

    @Throws(PageException::class)
    private fun getPrimaryKeysAsQuery(dc: DatasourceConnection?): lucee.runtime.type.Query? {

        // Read Meta Data
        val meta: DatabaseMetaData
        meta = try {
            dc.getConnection().getMetaData()
        } catch (e: SQLException) {
            throw DatabaseException(e, dc)
        }
        return try {
            QueryImpl(meta.getPrimaryKeys(tablequalifier, tableowner, tablename), -1, "query", pageContext.getTimeZone())
        } catch (e: SQLException) {
            try {
                QueryImpl(meta.getBestRowIdentifier(tablequalifier, tableowner, tablename, 0, false), -1, "query", pageContext.getTimeZone())
            } catch (sqle: SQLException) {
                throw DatabaseException("can't find primary keys of table [" + tablename + "] (" + ExceptionUtil.getMessage(sqle) + ")", null, null, dc)
            }
        }
    }

    /**
     * @param keys primary Keys
     * @return return SQL String for update
     * @throws PageException
     */
    @Throws(PageException::class)
    private fun createSQL(dc: DatasourceConnection?, keys: Array<String?>?, meta: Struct?): SQL? {
        var fields: Array<String?>? = null
        val form: Form = pageContext.formScope()
        fields = if (formfields != null) ListUtil.toStringArray(ListUtil.listToArrayRemoveEmpty(formfields, ',')) else CollectionUtil.keysAsString(pageContext.formScope())
        val set = StringBuffer()
        val where = StringBuffer()
        val setItems = ArrayList()
        val whereItems = ArrayList()
        var field: String?
        for (i in fields.indices) {
            field = StringUtil.trim(fields[i], null)
            if (StringUtil.startsWithIgnoreCase(field, "form.")) field = field.substring(5)
            if (!field.equalsIgnoreCase("fieldnames")) {
                if (ArrayUtil.indexOfIgnoreCase(keys, field) === -1) {
                    if (set.length() === 0) set.append(" set ") else set.append(",")
                    set.append(field)
                    set.append("=?")
                    val ci: ColumnInfo = meta.get(field)
                    if (ci != null) setItems.add(SQLItemImpl(form.get(field, null), ci.getType())) else setItems.add(SQLItemImpl(form.get(field, null)))
                } else {
                    if (where.length() === 0) where.append(" where ") else where.append(" and ")
                    where.append(field)
                    where.append("=?")
                    whereItems.add(SQLItemImpl(form.get(field, null)))
                }
            }
        }
        if (setItems.size() + whereItems.size() === 0) return null
        if (whereItems.size() === 0) throw DatabaseException("can't find primary keys [" + ListUtil.arrayToList(keys, ",").toString() + "] of table [" + tablename.toString() + "] in form scope", null, null, dc)
        val sql = StringBuffer()
        sql.append("update ")
        if (tablequalifier != null && tablequalifier!!.length() > 0) {
            sql.append(tablequalifier)
            sql.append('.')
        }
        if (tableowner != null && tableowner!!.length() > 0) {
            sql.append(tableowner)
            sql.append('.')
        }
        sql.append(tablename)
        sql.append(set)
        sql.append(where)
        return SQLImpl(sql.toString(), arrayMerge(setItems, whereItems))
    }

    private fun arrayMerge(setItems: ArrayList?, whereItems: ArrayList?): Array<SQLItem?>? {
        val items: Array<SQLItem?> = arrayOfNulls<SQLItem?>(setItems.size() + whereItems.size())
        var index = 0
        // Item
        var size: Int = setItems.size()
        for (i in 0 until size) {
            items[index++] = setItems.get(i) as SQLItem
        }
        // Where
        size = whereItems.size()
        for (i in 0 until size) {
            items[index++] = whereItems.get(i) as SQLItem
        }
        return items
    }
}