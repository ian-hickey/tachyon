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

import java.sql.Connection

/**
 * a datasource and connection pair
 */
interface DatasourceConnection : Connection {
    /**
     * @return Returns the connection.
     */
    val connection: Connection?

    /**
     * @return Returns the datasource.
     */
    val datasource: lucee.runtime.db.DataSource?

    /**
     * @return the password
     */
    val password: String?

    /**
     * @return the username
     */
    val username: String?
    fun supportsGetGeneratedKeys(): Boolean

    @Throws(SQLException::class)
    fun getPreparedStatement(sql: SQL?, createGeneratedKeys: Boolean, allowCaching: Boolean): PreparedStatement?

    @Throws(SQLException::class)
    fun getPreparedStatement(sql: SQL?, resultSetType: Int, resultSetConcurrency: Int): PreparedStatement?

    @Override
    @Throws(SQLException::class)
    fun close()

    /**
     * @return is timeout or not
     */
    val isTimeout: Boolean

    /**
     * is life cycle timeout
     *
     * @return Returns if Life Cycle Timeout.
     */
    // FUTURE public boolean isAutoCommit() throws SQLException;
    val isLifecycleTimeout: Boolean
    // FUTURE public void setAutoCommit(boolean setting) throws SQLException;
}