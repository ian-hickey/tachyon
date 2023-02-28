/**
 * Copyright (c) 2015, Lucee Assosication Switzerland. All rights reserved.
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
package lucee.runtime.util

import java.sql.Blob

interface DBUtil {
    /**
     *
     * converts the value defined inside a SQLItem to the type defined in stat item
     * @param item item
     * @return Object
     * @throws PageException Page Exception
     */
    @Throws(PageException::class)
    fun toSqlType(item: SQLItem?): Object?

    @Deprecated
    @Throws(PageException::class, SQLException::class)
    operator fun setValue(tz: TimeZone?, stat: PreparedStatement?, parameterIndex: Int, item: SQLItem?)

    /**
     * fill a SQLItem to into a PreparedStatement
     *
     * @param pc Page Context
     * @param tz timezone
     * @param stat statement
     * @param parameterIndex parameter index
     * @param item item
     * @throws PageException Page Exception
     * @throws SQLException SQL Exception
     */
    @Throws(PageException::class, SQLException::class)
    operator fun setValue(pc: PageContext?, tz: TimeZone?, stat: PreparedStatement?, parameterIndex: Int, item: SQLItem?)

    /**
     * Cast a SQL Item to a String (Display) Value
     *
     * @param item Item
     * @return String Value
     */
    fun toString(item: SQLItem?): String?

    /**
     * cast a type defined in java.sql.Types to String SQL Type
     *
     * @param type SQL Type
     * @return SQL Type as String
     * @throws PageException Page Exception
     */
    @Throws(PageException::class)
    fun toStringType(type: Int): String?

    /**
     * cast a String SQL Type to type defined in java.sql.Types
     *
     * @param strType String SQL Type
     * @return SQL Type as int
     * @throws PageException Page Exception
     */
    @Throws(PageException::class)
    fun toSQLType(strType: String?): Int

    /**
     * create a Blob Object
     *
     * @param conn Connection
     * @param value value
     * @return Returns a Blob Object.
     * @throws PageException Page Exception
     * @throws SQLException SQL Exception
     */
    @Throws(PageException::class, SQLException::class)
    fun toBlob(conn: Connection?, value: Object?): Blob?

    /**
     * create a Clob Object
     *
     * @param conn Connection
     * @param value value
     * @return Returns a Clob Object.
     * @throws PageException Page Exception
     * @throws SQLException SQL Exception
     */
    @Throws(PageException::class, SQLException::class)
    fun toClob(conn: Connection?, value: Object?): Clob?

    /**
     * checks if this is an oracle connection
     *
     * @param conn Connection
     * @return Returns a Boolean.
     */
    fun isOracle(conn: Connection?): Boolean

    @Throws(SQLException::class)
    fun getDatabaseName(dc: DatasourceConnection?): String?

    /**
     * close silently a SQL Statement
     *
     * @param stat SQL statement
     */
    fun closeSilent(stat: Statement?)

    /**
     * close silently a SQL Connection
     *
     * @param conn Connection
     */
    fun closeSilent(conn: Connection?)

    /**
     * close silently a SQL ResultSet
     *
     * @param rs Result set
     */
    fun closeSilent(rs: ResultSet?)
    fun toSQLItem(value: Object?, type: Int): SQLItem?
    fun toSQL(sql: String?, items: Array<SQLItem?>?): SQL?
    fun releaseDatasourceConnection(config: Config?, dc: DatasourceConnection?, async: Boolean)

    /*
	 * FUTURE public void releaseDatasourceConnection(PageContext pc, DatasourceConnection dc,boolean
	 * managed); public void releaseDatasourceConnection(Config config, DatasourceConnection dc);
	 */
    @Throws(PageException::class)
    fun getDatasourceConnection(pc: PageContext?, datasource: DataSource?, user: String?, pass: String?): DatasourceConnection?

    /*
	 * FUTURE public DatasourceConnection getDatasourceConnection(PageContext pc,DataSource datasource,
	 * String user, String pass, boolean managed) throws PageException; public DatasourceConnection
	 * getDatasourceConnection(Config config,DataSource datasource, String user, String pass) throws
	 * PageException;
	 * 
	 */
    @Throws(PageException::class)
    fun getDatasourceConnection(pc: PageContext?, datasourceName: String?, user: String?, pass: String?): DatasourceConnection?
    fun getColumnNames(qry: Query?): Array<Key?>?

    @Throws(SQLException::class)
    fun getColumnName(meta: ResultSetMetaData?, column: Int): String?

    @Throws(SQLException::class)
    fun getObject(rs: ResultSet?, columnIndex: Int, type: Class?): Object?

    @Throws(SQLException::class)
    fun getObject(rs: ResultSet?, columnLabel: String?, type: Class?): Object?
}