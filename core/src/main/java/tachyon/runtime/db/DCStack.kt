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
package tachyon.runtime.db

import java.sql.Connection

internal class DCStack(datasource: DataSource, user: String, pass: String) {
    private var item: Item? = null
    private val datasource: DataSource
    val username: String
    val password: String
    private val counter: RefInteger

    companion object {
        private const val DEFAULT_TIMEOUT = 0

        init {
            DEFAULT_TIMEOUT = Caster.toIntValue(SystemUtil.getSystemPropOrEnvVar("tachyon.datasource.timeout.validation", null), 5)
        }
    }

    fun getDatasource(): DataSource {
        return datasource
    }

    fun add(dc: DatasourceConnection) {
        // make sure the connection is not already in stack, this can happen when the conn is released twice
        var test = item
        while (test != null) {
            if (test.dc === dc) {
                LogUtil.log(ThreadLocalPageContext.get(), Log.LEVEL_INFO, DCStack::class.java.getName(), "a datasource connection was released twice!")
                return
            }
            test = test.prev
        }
        item = Item(item, dc)
    }

    fun get(): DatasourceConnection? {
        if (item == null) return null
        val rtn: DatasourceConnection = item!!.dc
        item = item!!.prev
        try {
            return if (!rtn.getConnection().isClosed()) {
                rtn
            } else get()
        } catch (e: SQLException) {
        }
        return null
    }

    val isEmpty: Boolean
        get() = item == null

    fun size(): Int {
        var count = 0
        var i = item
        while (i != null) {
            count++
            i = i.prev
        }
        return count
    }

    fun openConnectionsIn(): Int {
        var count = 0
        var i = item
        while (i != null) {
            try {
                if (!i.dc.getConnection().isClosed()) count++
            } catch (e: Exception) {
            }
            i = i.prev
        }
        return count
    }

    fun openConnectionsOut(): Int {
        return counter.toInt()
    }

    fun openConnections(): Int {
        return openConnectionsIn() + openConnectionsOut()
    }

    internal inner class Item(val prev: Item, dc: DatasourceConnection) {
        val dc: DatasourceConnection
        private var count = 1

        @Override
        override fun toString(): String {
            return "($prev)<-$count"
        }

        init {
            this.dc = dc
            if (prev != null) count = prev.count + 1
        }
    }

    fun clear(force: Boolean, validate: Boolean) {
        synchronized(this) { clear(item, null, force, validate) }
    }

    /**
     *
     * @param current
     * @param next
     * @param timeout timeout in seconds used to validate existing connections
     * @throws SQLException
     */
    private fun clear(current: Item?, next: Item?, force: Boolean, validate: Boolean) {
        if (current == null) return

        // timeout or closed
        if (force || current.dc.isTimeout() || current.dc.isLifecycleTimeout() || isClosedEL(current.dc.getConnection())
                || validate && Boolean.FALSE.equals(isValidEL(current.dc.getConnection()))) {

            // when timeout was reached but it is still open, close it
            if (!isClosedEL(current.dc.getConnection())) {
                try {
                    current.dc.close()
                } catch (e: Exception) {
                }
            }

            // remove this connection from chain
            if (next == null) item = current.prev else {
                next.prev = current.prev
            }
            clear(current.prev, next, force, validate)
        } else {
            // make sure that auto commit is true
            try {
                if (!current.dc.getAutoCommit()) current.dc.setAutoCommit(true)
            } catch (e: SQLException) {
            }
            clear(current.prev, current, force, validate)
        }
        counter.setValue(0)
    }

    private fun isClosedEL(conn: Connection): Boolean {
        return try {
            conn.isClosed()
        } catch (se: Exception) {
            datasource.getLog().error("Connection  Pool", se)
            // in case of an exception we see this conn as useless and close the connection
            try {
                conn.close()
            } catch (e: SQLException) {
                datasource.getLog().error("Connection  Pool", e)
            }
            true
        }
    }

    private fun isValidEL(conn: Connection): Boolean? {
        return try {
            // value is in ms but method expect s
            val ms: Int = datasource.getNetworkTimeout()
            var s = DEFAULT_TIMEOUT
            if (ms > 0) s = Math.ceil(ms / 1000)
            if (conn.isValid(s)) Boolean.TRUE else Boolean.FALSE
        } catch (e: Exception) {
            null
        }
    }

    fun getCounter(): RefInteger {
        return counter
    }

    init {
        this.datasource = datasource
        username = user
        password = pass
        counter = RefIntegerImpl(0)
    }
}