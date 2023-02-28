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
package tachyon.runtime.services

import java.io.PrintWriter

class DataSourceImpl(ds: tachyon.runtime.db.DataSource?) : DataSource {
    private val ds: tachyon.runtime.db.DataSource?
    @Override
    @Throws(SQLException::class)
    fun getConnection(): Connection? {
        return getConnection(ds.getUsername(), ds.getPassword())
    }

    @Override
    @Throws(SQLException::class)
    fun getConnection(user: String?, pass: String?): Connection? {
        return try {
            val pc: PageContext = ThreadLocalPageContext.get()
            pc.getDataSourceManager().getConnection(pc, ds.getName(), user, pass).getConnection()
        } catch (e: PageException) {
            throw SQLException(e.getMessage())
        }
    }

    @Override
    fun getDataSourceDef(): DataSourceDef? {
        return DatSourceDefImpl(ds)
    }

    @Override
    @Throws(SQLException::class)
    fun getLogWriter(): PrintWriter? {
        // TODO Auto-generated method stub
        return null
    }

    @Override
    @Throws(SQLException::class)
    fun getLoginTimeout(): Int {
        // TODO Auto-generated method stub
        return 0
    }

    @Override
    fun isDisabled(): Boolean {
        // TODO Auto-generated method stub
        return false
    }

    @Override
    @Throws(SQLException::class)
    fun remove() {
        // TODO Auto-generated method stub
    }

    @Override
    fun setDataSourceDef(dsDef: DataSourceDef?) {
        // TODO Auto-generated method stub
    }

    @Override
    @Throws(SQLException::class)
    fun setLogWriter(pw: PrintWriter?) {
        // TODO Auto-generated method stub
    }

    @Override
    @Throws(SQLException::class)
    fun setLoginTimeout(timeout: Int) {
        // TODO Auto-generated method stub
    }

    @Override
    @Throws(SQLException::class)
    fun <T> unwrap(iface: Class<T?>?): T? {
        // TODO Auto-generated method stub
        return null
    }

    @Override
    @Throws(SQLException::class)
    fun isWrapperFor(iface: Class<*>?): Boolean {
        // TODO Auto-generated method stub
        return false
    }

    // used only with java 7, do not set @Override
    @Throws(SQLFeatureNotSupportedException::class)
    fun getParentLogger(): Logger? {
        throw SQLFeatureNotSupportedException()
    }

    init {
        this.ds = ds
    }
}