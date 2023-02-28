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
package lucee.runtime.exp

import java.sql.DatabaseMetaData

/**
 * Database Exception Object
 */
class DatabaseException : PageExceptionImpl {
    private var sql: SQL? = null
    private var sqlstate: String? = ""
    private var errorcode = -1
    private var datasource: DataSource? = null

    constructor(sqle: SQLException?, dc: DatasourceConnection?) : super(if (sqle.getCause() is SQLException) (sqle.getCause() as SQLException?. also { sqle = it }).getMessage() else sqle.getMessage(), "database") {
        set(sqle)
        set(dc)
        initCause(sqle)
    }

    constructor(message: String?, detail: String?, sql: SQL?, dc: DatasourceConnection?) : super(message, "database") {
        set(sql)
        set(null, detail)
        set(dc)
    }

    /**
     * Constructor of the class
     *
     * @param message error message
     * @param detail detailed error message
     * @param sqle
     * @param sql
     * @param dc
     */
    private constructor(message: String?, detail: String?, sqle: SQLException?, sql: SQL?, dc: DatasourceConnection?) : super(message
            ?: "", "database") {
        set(sql)
        set(sqle, detail)
        set(sqle)
        set(dc)
    }

    private fun set(sql: SQL?) {
        this.sql = sql
        if (sql != null) {
            try {
                setAdditional(KeyConstants._SQL, sql.toString())
            } catch (e: Exception) {
                setAdditional(KeyConstants._SQL, sql.getSQLString())
            }
        }
    }

    private operator fun set(sqle: SQLException?, detail: String?) {
        val sqleMessage = if (sqle != null) sqle.getMessage() else ""
        if (!StringUtil.isEmpty(sqleMessage)) {
            if (detail != null) {
                setDetail("""
    $detail
    $sqleMessage
    """.trimIndent())
            } else {
                setDetail(detail)
            }
        } else {
            setDetail(detail)
        }
    }

    private fun set(sqle: SQLException?) {
        if (sqle != null) {
            sqlstate = sqle.getSQLState()
            errorcode = sqle.getErrorCode()
            this.setStackTrace(sqle.getStackTrace())
        }
    }

    private fun set(dc: DatasourceConnection?) {
        if (dc != null) {
            datasource = dc.getDatasource()
            try {
                val md: DatabaseMetaData = dc.getConnection().getMetaData()
                md.getDatabaseProductName()
                setAdditional(KeyConstants._DatabaseName, md.getDatabaseProductName())
                setAdditional(KeyConstants._DatabaseVersion, md.getDatabaseProductVersion())
                setAdditional(KeyConstants._DriverName, md.getDriverName())
                setAdditional(KeyConstants._DriverVersion, md.getDriverVersion())
                // setAdditional("url",md.getURL());
                if (!"__default__".equals(dc.getDatasource().getName())) setAdditional(KeyConstants._Datasource, dc.getDatasource().getName())
            } catch (e: SQLException) {
            }
        }
    }
    /**
     * Constructor of the class
     *
     * @param message
     * @param sqle
     * @param sql
     *
     * public DatabaseException(String message, SQLException sqle, SQL
     * sql,DatasourceConnection dc) { this(message,null,sqle,sql,dc); }
     */
    /**
     * Constructor of the class
     *
     * @param sqle
     * @param sql
     */
    constructor(sqle: SQLException?, sql: SQL?, dc: DatasourceConnection?) : this(if (sqle != null) sqle.getMessage() else null, null, sqle, sql, dc) {}

    /**
     * Constructor of the class
     *
     * @param sqle
     */
    @Override
    override fun getCatchBlock(config: Config?): CatchBlock? {
        var strSQL = if (sql == null) "" else sql.toString()
        if (StringUtil.isEmpty(strSQL)) strSQL = Caster.toString(getAdditional().get("SQL", ""), "")
        var datasourceName = if (datasource == null) "" else datasource.getName()
        if (StringUtil.isEmpty(datasourceName)) datasourceName = Caster.toString(getAdditional().get("DataSource", ""), "")
        val sct: CatchBlock = super.getCatchBlock(config)
        sct.setEL("NativeErrorCode", Double.valueOf(errorcode))
        sct.setEL("DataSource", datasourceName)
        sct.setEL("SQLState", sqlstate)
        sct.setEL("Sql", strSQL)
        sct.setEL("queryError", strSQL)
        sct.setEL("where", "")
        return sct
    }

    companion object {
        fun notFoundException(pc: PageContext?, datasource: String?): DatabaseException? {
            val list: List<String?> = ArrayList<String?>()

            // application based datasources
            var datasources: Array<DataSource?> = pc.getApplicationContext().getDataSources()
            if (datasources != null) for (i in datasources.indices) {
                list.add(datasources[i].getName())
            }

            // config based datasources
            datasources = pc.getConfig().getDataSources()
            if (datasources != null) for (i in datasources.indices) {
                list.add(datasources[i].getName())
            }

            // create error detail
            val de = DatabaseException("Datasource [$datasource] doesn't exist", null, null, null)
            de.setDetail(ExceptionUtil.createSoundexDetail(datasource, list.iterator(), "datasource names"))
            de.setAdditional(KeyConstants._Datasource, datasource)
            return de
        }
    }
}