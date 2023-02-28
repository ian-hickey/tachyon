/**
 * Copyright (c) 2023, TachyonCFML.org
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
package tachyon.commons.db

import java.sql.Connection

/**
 * Utility for db
 */
object DBUtil {
    fun setAutoCommitEL(conn: Connection?, b: Boolean) {
        try {
            if (conn != null) conn.setAutoCommit(b)
        } catch (e: Throwable) {
            ExceptionUtil.rethrowIfNecessary(e)
        }
    }

    fun setReadOnlyEL(conn: Connection?, b: Boolean) {
        try {
            if (conn != null) conn.setReadOnly(b)
        } catch (e: Throwable) {
            ExceptionUtil.rethrowIfNecessary(e)
        }
    }

    fun commitEL(conn: Connection?) {
        try {
            if (conn != null) conn.commit()
        } catch (e: Throwable) {
            ExceptionUtil.rethrowIfNecessary(e)
        }
    }

    fun setTransactionIsolationEL(conn: Connection?, level: Int) {
        try {
            if (conn != null) conn.setTransactionIsolation(level)
        } catch (e: Exception) {
        }
    }

    fun closeEL(stat: Statement?) {
        if (stat != null) {
            try {
                stat.close()
            } catch (t: Throwable) {
                ExceptionUtil.rethrowIfNecessary(t)
            }
        }
    }

    fun closeEL(rs: ResultSet?) {
        if (rs != null) {
            try {
                rs.close()
            } catch (t: Throwable) {
                ExceptionUtil.rethrowIfNecessary(t)
            }
        }
    } /*
	 * public static Connection getConnection(String connStr, String user, String pass) throws
	 * SQLException { try { return new ConnectionProxy(new StateFactory(),
	 * DriverManager.getConnection(connStr, user, pass)); } catch (SQLException e) {
	 * 
	 * if(connStr.indexOf('?')!=-1) { connStr=connStr+"&user="+user+"&password="+pass; return new
	 * ConnectionProxy(new StateFactory(), DriverManager.getConnection(connStr)); } throw e; } }
	 */
}