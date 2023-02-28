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
package lucee.runtime.db

import java.sql.PreparedStatement

object DataSourceUtil {
    fun isHSQLDB(dc: DatasourceConnection?): Boolean {
        return `is`(dc, "HSQL", false, "org.hsqldb.jdbcDriver")
    }

    fun isOracle(dc: DatasourceConnection?): Boolean {
        return `is`(dc, "Oracle", true, "OracleDriver")
    }

    fun isPostgres(dc: DatasourceConnection?): Boolean {
        return `is`(dc, "PostgreSQL", true, "postgresql")
    }

    fun isMySQL(dc: DatasourceConnection?): Boolean {
        return `is`(dc, "MySQL", false, "org.gjt.mm.mysql.Driver")
    }

    fun isMSSQL(dc: DatasourceConnection?): Boolean {
        return `is`(dc, "Microsoft", false, "com.microsoft.jdbc.sqlserver.SQLServerDriver", "com.microsoft.sqlserver.jdbc.SQLServerDriver", "net.sourceforge.jtds.jdbc.Driver")
    }

    fun isMSSQLDriver(dc: DatasourceConnection?): Boolean {
        if (dc == null) return false
        val dsp: DataSourcePro = dc.getDatasource()
        return if (dsp.isMSSQL() == null) {
            try {
                if (dc.getConnection().getMetaData().getDriverName().indexOf("Microsoft SQL Server JDBC Driver") !== -1) {
                    dsp.setMSSQL(true)
                    return true
                }
            } catch (e: SQLException) {
            }
            val className: String = dc.getDatasource().getClassDefinition().getClassName()
            val isMSSQL = className.equals("com.microsoft.jdbc.sqlserver.SQLServerDriver") || className.equals("com.microsoft.sqlserver.jdbc.SQLServerDriver")
            dsp.setMSSQL(isMSSQL)
            isMSSQL
        } else dsp.isMSSQL()
    }

    private fun `is`(dc: DatasourceConnection?, keyword: String, doIndexOf: Boolean, vararg classNames: String): Boolean {
        if (dc == null) return false
        try {
            if (dc.getConnection().getMetaData().getDatabaseProductName().indexOf(keyword) !== -1) return true
        } catch (e: Exception) {
            val className: String = dc.getDatasource().getClassDefinition().getClassName()
            if (doIndexOf) {
                for (cn in classNames) {
                    if (className.indexOf(cn) !== -1) return true
                }
            } else {
                for (cn in classNames) {
                    if (className.equals(cn)) return true
                }
            }
        }
        return false
    }

    @Throws(SQLException::class)
    fun isValid(dc: DatasourceConnection, timeout: Int): Boolean {
        return dc.getConnection().isValid(timeout)
    }

    fun isValid(dc: DatasourceConnection, timeout: Int, defaultValue: Boolean): Boolean {
        return try {
            dc.getConnection().isValid(timeout)
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
            defaultValue
        }
    }

    fun isClosed(ps: PreparedStatement, defaultValue: Boolean): Boolean {
        return try {
            ps.isClosed()
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
            defaultValue
        }
    }

    @Throws(SQLException::class)
    fun getDatabaseName(dc: DatasourceConnection): String? {
        var dbName: String? = null
        try {
            dbName = dc.getDatasource().getDatabase()
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
        }
        if (StringUtil.isEmpty(dbName)) dbName = dc.getConnection().getCatalog() // works on most JDBC drivers (except Oracle )
        return dbName
    }

    fun setQueryTimeoutSilent(stat: Statement, seconds: Int) {
        // some jdbc driver multiply the value by 1000 to get milli second what can end in a negative value,
        // so we have to make sure the given timeout can be
        // multiply by 1000
        var seconds = seconds
        val max: Int = Integer.MAX_VALUE / 1000
        if (max < seconds) seconds = max
        try {
            if (seconds > 0) stat.setQueryTimeout(seconds)
        } catch (e: SQLException) {
        }
    }

    fun getLargeTextSqlTypeName(dc: DatasourceConnection?): String {
        if (isHSQLDB(dc)) return "VARCHAR"
        if (isMySQL(dc)) return "LONGTEXT"
        if (isOracle(dc)) return "CLOB"
        return if (isPostgres(dc)) "TEXT" else "NTEXT"

        // default
    }
}