/**
 * Copyright (c) 2014, the Railo Company Ltd.
 * Copyright (c) 2015, Lucee Assosication Switzerland
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

import lucee.runtime.PageContext

interface DataSourceManager {
    /**
     * return a database connection matching to datasource name
     *
     * @param pc page context
     * @param datasource datasource name
     * @param user username to datasource
     * @param pass password to datasource
     * @return return a Db Connection Object
     * @throws PageException Page Exception
     */
    @Deprecated
    @Deprecated("""use instead
	              <code>getConnection(PageContext pc,DataSource ds, String user, String pass)</code>""")
    @Throws(PageException::class)
    fun getConnection(pc: PageContext?, datasource: String?, user: String?, pass: String?): DatasourceConnection?

    /**
     * return a database connection matching to datasource name
     *
     * @param pc page context
     * @param ds datasource name
     * @param user username to datasource
     * @param pass password to datasource
     * @return return a Db Connection Object
     * @throws PageException Page Exception
     */
    @Throws(PageException::class)
    fun getConnection(pc: PageContext?, ds: DataSource?, user: String?, pass: String?): DatasourceConnection?

    @Throws(PageException::class)
    fun releaseConnection(pc: PageContext?, dc: DatasourceConnection?)

    /**
     * set state of transaction to begin
     */
    fun begin()

    /**
     * set state of transaction to begin
     *
     * @param isolation isolation level of the transaction
     */
    fun begin(isolation: String?)

    /**
     * set state of transaction to begin
     *
     * @param isolation isolation level of the transaction
     */
    fun begin(isolation: Int)

    /**
     * rollback hanging transaction
     *
     * @throws PageException Page Exception
     */
    @Throws(PageException::class)
    fun rollback()

    @Throws(PageException::class)
    fun savepoint()

    /**
     * commit hanging transaction
     *
     * @throws PageException Page Exception
     */
    @Throws(PageException::class)
    fun commit()

    /**
     * @return return if manager is in autocommit mode or not
     */
    val isAutoCommit: Boolean

    /**
     * ends the manual commit state
     */
    fun end()

    /**
     * @param datasource datasource name
     */
    @Deprecated
    @Deprecated("""use instead <code>remove(DataSource datasource)</code>
	  """)
    fun remove(datasource: String?)
    fun remove(datasource: DataSource?)
    fun release()
}