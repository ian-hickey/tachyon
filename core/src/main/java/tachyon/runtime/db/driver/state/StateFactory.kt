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
package tachyon.runtime.db.driver.state

import java.sql.CallableStatement

class StateFactory : Factory {
    @Override
    fun createStatementProxy(conn: ConnectionProxy?, stat: Statement?): StatementProxy {
        return StateStatement(conn, stat)
    }

    @Override
    fun createPreparedStatementProxy(conn: ConnectionProxy?, stat: PreparedStatement?, sql: String?): PreparedStatementProxy {
        return StatePreparedStatement(conn, stat, sql)
    }

    @Override
    fun createCallableStatementProxy(conn: ConnectionProxy?, stat: CallableStatement?, sql: String?): CallableStatementProxy {
        return StateCallableStatement(conn, stat, sql)
    }
}