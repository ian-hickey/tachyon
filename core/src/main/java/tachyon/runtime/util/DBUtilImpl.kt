/**
 * Copyright (c) 2015, Tachyon Assosication Switzerland. All rights reserved.
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
package tachyon.runtime.util

import java.sql.Blob

class DBUtilImpl : DBUtil {
    @Override
    @Throws(PageException::class)
    fun toSqlType(item: SQLItem?): Object? {
        return SQLCaster.toSqlType(item)
    }

    @Override
    @Throws(PageException::class, SQLException::class)
    operator fun setValue(tz: TimeZone?, stat: PreparedStatement?, parameterIndex: Int, item: SQLItem?) {
        SQLCaster.setValue(ThreadLocalPageContext.get(), tz, stat, parameterIndex, item)
    }

    @Override
    @Throws(PageException::class, SQLException::class)
    operator fun setValue(pc: PageContext?, tz: TimeZone?, stat: PreparedStatement?, parameterIndex: Int, item: SQLItem?) {
        SQLCaster.setValue(pc, tz, stat, parameterIndex, item)
    }

    @Override
    fun toString(item: SQLItem?): String? {
        return SQLCaster.toString(item)
    }

    @Override
    @Throws(PageException::class)
    fun toStringType(type: Int): String? {
        return SQLCaster.toStringType(type)
    }

    @Override
    @Throws(PageException::class)
    fun toSQLType(strType: String?): Int {
        return SQLCaster.toSQLType(strType)
    }

    @Override
    @Throws(PageException::class, SQLException::class)
    fun toBlob(conn: Connection?, value: Object?): Blob? {
        return SQLUtil.toBlob(conn, value)
    }

    @Override
    @Throws(PageException::class, SQLException::class)
    fun toClob(conn: Connection?, value: Object?): Clob? {
        return SQLUtil.toClob(conn, value)
    }

    @Override
    fun isOracle(conn: Connection?): Boolean {
        return tachyon.commons.sql.SQLUtil.isOracle(conn)
    }

    @Override
    fun closeSilent(stat: Statement?) {
        tachyon.commons.sql.SQLUtil.closeEL(stat)
    }

    @Override
    fun closeSilent(conn: Connection?) {
        tachyon.commons.sql.SQLUtil.closeEL(conn)
    }

    @Override
    fun closeSilent(rs: ResultSet?) {
        tachyon.commons.sql.SQLUtil.closeEL(rs)
    }

    @Override
    fun toSQLItem(value: Object?, type: Int): SQLItem? {
        return SQLItemImpl(value, type)
    }

    @Override
    fun toSQL(sql: String?, items: Array<SQLItem?>?): SQL? {
        return SQLImpl(sql, items)
    }

    fun releaseDatasourceConnection(config: Config?, dc: DatasourceConnection?) {
        _releaseDatasourceConnection(ThreadLocalPageContext.get(config), dc, null)
    }

    fun releaseDatasourceConnection(pc: PageContext?, dc: DatasourceConnection?, managed: Boolean) {
        _releaseDatasourceConnection(pc, dc, managed)
    }

    private fun _releaseDatasourceConnection(pc: PageContext?, dc: DatasourceConnection?, managed: Boolean?) {
        var pc: PageContext? = pc
        var managed = managed
        pc = ThreadLocalPageContext.get(pc)
        if (managed == null) {
            managed = pc != null
        }
        if (managed) {
            if (pc == null) throw PageRuntimeException(ApplicationException("missing PageContext to access the Database Connection Manager"))
            val manager: DatasourceManagerImpl = pc.getDataSourceManager() as DatasourceManagerImpl
            manager.releaseConnection(pc, dc)
            return
        }
        if (dc != null) (dc as DatasourceConnectionPro?).release()
    }

    @Override
    fun releaseDatasourceConnection(config: Config?, dc: DatasourceConnection?, async: Boolean) {
        releaseDatasourceConnection(config, dc)
    }

    @Override
    @Throws(PageException::class)
    fun getDatasourceConnection(pc: PageContext?, datasource: DataSource?, user: String?, pass: String?): DatasourceConnection? {
        return _getDatasourceConnection(pc, datasource, user, pass, null)
    }

    @Throws(PageException::class)
    fun getDatasourceConnection(pc: PageContext?, datasource: DataSource?, user: String?, pass: String?, managed: Boolean): DatasourceConnection? {
        return _getDatasourceConnection(pc, datasource, user, pass, managed)
    }

    @Throws(PageException::class)
    private fun _getDatasourceConnection(pc: PageContext?, datasource: DataSource?, user: String?, pass: String?, managed: Boolean?): DatasourceConnection? {
        var pc: PageContext? = pc
        var managed = managed
        pc = ThreadLocalPageContext.get(pc)
        if (managed == null) {
            managed = pc != null
        }
        if (managed) {
            if (pc == null) throw ApplicationException("missing PageContext to access the Database Connection Manager")
            val manager: DatasourceManagerImpl = pc.getDataSourceManager() as DatasourceManagerImpl
            return manager.getConnection(pc, datasource, user, pass)
        }
        return getDatasourceConnection(ThreadLocalPageContext.getConfig(pc), datasource, user, pass)
    }

    @Throws(PageException::class)
    fun getDatasourceConnection(config: Config?, datasource: DataSource?, user: String?, pass: String?): DatasourceConnection? {
        val ci: ConfigWebPro = ThreadLocalPageContext.getConfig(config) as ConfigWebPro
        return ci.getDatasourceConnectionPool().getDatasourceConnection(config, datasource, user, pass)
    }

    @Override
    @Throws(PageException::class)
    fun getDatasourceConnection(pc: PageContext?, datasourceName: String?, user: String?, pass: String?): DatasourceConnection? {
        return getDatasourceConnection(pc, datasourceName, user, pass, true)
    }

    @Throws(PageException::class)
    fun getDatasourceConnection(pc: PageContext?, datasourceName: String?, user: String?, pass: String?, managed: Boolean): DatasourceConnection? {
        var pc: PageContext? = pc
        var datasourceName = datasourceName
        var datasource: DataSource? = null
        pc = ThreadLocalPageContext.get(pc)
        if (pc != null) {
            // default datasource
            if ("__default__".equalsIgnoreCase(datasourceName)) {
                val obj: Object = pc.getApplicationContext().getDefDataSource()
                if (obj is String) datasourceName = obj else datasource = obj as DataSource
            }

            // get datasource from application context
            if (datasource == null) datasource = pc.getApplicationContext().getDataSource(datasourceName, null)
        }

        // get datasource from config
        if (datasource == null) {
            val config: Config = ThreadLocalPageContext.getConfig(pc)
            datasource = config.getDataSource(datasourceName)
        }
        return getDatasourceConnection(pc, datasource, user, pass, managed)
    }

    @Override
    @Throws(SQLException::class)
    fun getDatabaseName(dc: DatasourceConnection?): String? {
        return DataSourceUtil.getDatabaseName(dc)
    }

    @Override
    fun getColumnNames(qry: Query?): Array<Key?>? {
        return QueryUtil.getColumnNames(qry)
    }

    @Override
    @Throws(SQLException::class)
    fun getColumnName(meta: ResultSetMetaData?, column: Int): String? {
        return QueryUtil.getColumnName(meta, column)
    }

    @Override
    @Throws(SQLException::class)
    fun getObject(rs: ResultSet?, columnIndex: Int, type: Class?): Object? {
        return QueryUtil.getObject(rs, columnIndex, type)
    }

    @Override
    @Throws(SQLException::class)
    fun getObject(rs: ResultSet?, columnLabel: String?, type: Class?): Object? {
        return QueryUtil.getObject(rs, columnLabel, type)
    }
}