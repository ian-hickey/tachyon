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
package lucee.runtime.orm

import java.sql.Array

class ORMDatasourceConnection(pc: PageContext?, session: ORMSession?, ds: DataSource?, transactionIsolation: Int) : DatasourceConnectionPro {
    @Override
    @Throws(PageException::class)
    fun using(): DatasourceConnection? {
        return this
    }

    private var datasource: DataSource?
    private val connection: Connection?
    private var supportsGetGeneratedKeys: Boolean? = null

    @get:Override
    @set:Override
    var isManaged = false
    @Override
    fun getConnection(): Connection? {
        return connection
    }

    @Override
    fun getDatasource(): DataSource? {
        return datasource
    }

    @get:Override
    val password: String?
        get() = datasource.getPassword()

    @get:Override
    val username: String?
        get() = datasource.getUsername()

    @get:Override
    val isTimeout: Boolean
        get() = false

    @get:Override
    val isLifecycleTimeout: Boolean
        get() = false

    @Override
    override fun equals(obj: Object?): Boolean {
        return if (this === obj) true else DatasourceConnectionImpl.equals(this, obj as DatasourceConnection?)
        // if(!(obj instanceof ORMDatasourceConnection)) return false;
    }

    @Override
    fun supportsGetGeneratedKeys(): Boolean {
        if (supportsGetGeneratedKeys == null) {
            supportsGetGeneratedKeys = try {
                Caster.toBoolean(getConnection().getMetaData().supportsGetGeneratedKeys())
            } catch (t: Throwable) {
                ExceptionUtil.rethrowIfNecessary(t)
                return false
            }
        }
        return supportsGetGeneratedKeys.booleanValue()
    }

    @Override
    @Throws(SQLException::class)
    fun getPreparedStatement(sql: SQL?, createGeneratedKeys: Boolean, allowCaching: Boolean): PreparedStatement? {
        return if (createGeneratedKeys) getConnection().prepareStatement(sql.getSQLString(), Statement.RETURN_GENERATED_KEYS) else getConnection().prepareStatement(sql.getSQLString())
    }

    @Override
    @Throws(SQLException::class)
    fun getPreparedStatement(sql: SQL?, resultSetType: Int, resultSetConcurrency: Int): PreparedStatement? {
        return getConnection().prepareStatement(sql.getSQLString(), resultSetType, resultSetConcurrency)
    }

    @Override
    @Throws(SQLException::class)
    fun createStatement(): Statement? {
        return connection.createStatement()
    }

    @Override
    @Throws(SQLException::class)
    fun createStatement(resultSetType: Int, resultSetConcurrency: Int): Statement? {
        return connection.createStatement(resultSetType, resultSetConcurrency)
    }

    @Override
    @Throws(SQLException::class)
    fun createStatement(resultSetType: Int, resultSetConcurrency: Int, resultSetHoldability: Int): Statement? {
        return connection.createStatement(resultSetType, resultSetConcurrency, resultSetHoldability)
    }

    @Override
    @Throws(SQLException::class)
    fun prepareCall(sql: String?): CallableStatement? {
        return connection.prepareCall(sql)
    }

    @Override
    @Throws(SQLException::class)
    fun prepareCall(sql: String?, resultSetType: Int, resultSetConcurrency: Int): CallableStatement? {
        return connection.prepareCall(sql, resultSetType, resultSetConcurrency)
    }

    @Override
    @Throws(SQLException::class)
    fun prepareCall(sql: String?, resultSetType: Int, resultSetConcurrency: Int, resultSetHoldability: Int): CallableStatement? {
        return connection.prepareCall(sql, resultSetType, resultSetConcurrency, resultSetHoldability)
    }

    @Override
    @Throws(SQLException::class)
    fun prepareStatement(sql: String?): PreparedStatement? {
        return connection.prepareStatement(sql)
    }

    @Override
    @Throws(SQLException::class)
    fun prepareStatement(sql: String?, autoGeneratedKeys: Int): PreparedStatement? {
        return connection.prepareStatement(sql, autoGeneratedKeys)
    }

    @Override
    @Throws(SQLException::class)
    fun prepareStatement(sql: String?, columnIndexes: IntArray?): PreparedStatement? {
        return connection.prepareStatement(sql, columnIndexes)
    }

    @Override
    @Throws(SQLException::class)
    fun prepareStatement(sql: String?, columnNames: Array<String?>?): PreparedStatement? {
        return connection.prepareStatement(sql, columnNames)
    }

    @Override
    @Throws(SQLException::class)
    fun prepareStatement(sql: String?, resultSetType: Int, resultSetConcurrency: Int): PreparedStatement? {
        return connection.prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetConcurrency)
    }

    @Override
    @Throws(SQLException::class)
    fun prepareStatement(sql: String?, resultSetType: Int, resultSetConcurrency: Int, resultSetHoldability: Int): PreparedStatement? {
        return connection.prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability)
    }

    @Override
    @Throws(SQLException::class)
    fun isWrapperFor(iface: Class<*>?): Boolean {
        return connection.isWrapperFor(iface)
    }

    @Override
    @Throws(SQLException::class)
    fun <T> unwrap(iface: Class<T?>?): T? {
        return connection.unwrap(iface)
    }

    @Override
    @Throws(SQLException::class)
    fun clearWarnings() {
        connection.clearWarnings()
    }

    @Override
    @Throws(SQLException::class)
    fun close() {
        connection.close()
    }

    @Override
    @Throws(SQLException::class)
    fun commit() {
        connection.commit()
    }

    @Override
    @Throws(SQLException::class)
    fun createArrayOf(typeName: String?, elements: Array<Object?>?): Array? {
        return connection.createArrayOf(typeName, elements)
    }

    @Override
    @Throws(SQLException::class)
    fun createBlob(): Blob? {
        return connection.createBlob()
    }

    @Override
    @Throws(SQLException::class)
    fun createClob(): Clob? {
        return connection.createClob()
    }

    @Override
    @Throws(SQLException::class)
    fun createNClob(): NClob? {
        return connection.createNClob()
    }

    @Override
    @Throws(SQLException::class)
    fun createSQLXML(): SQLXML? {
        return connection.createSQLXML()
    }

    @Override
    @Throws(SQLException::class)
    fun createStruct(typeName: String?, attributes: Array<Object?>?): Struct? {
        return connection.createStruct(typeName, attributes)
    }

    @get:Throws(SQLException::class)
    @get:Override
    @set:Throws(SQLException::class)
    @set:Override
    var autoCommit: Boolean
        get() = connection.getAutoCommit()
        set(autoCommit) {
            connection.setAutoCommit(autoCommit)
        }

    @get:Throws(SQLException::class)
    @get:Override
    @set:Throws(SQLException::class)
    @set:Override
    var catalog: String?
        get() = connection.getCatalog()
        set(catalog) {
            connection.setCatalog(catalog)
        }

    @get:Throws(SQLException::class)
    @get:Override
    @set:Throws(SQLClientInfoException::class)
    @set:Override
    var clientInfo: Properties?
        get() = connection.getClientInfo()
        set(properties) {
            connection.setClientInfo(properties)
        }

    @Override
    @Throws(SQLException::class)
    fun getClientInfo(name: String?): String? {
        return connection.getClientInfo(name)
    }

    @get:Throws(SQLException::class)
    @get:Override
    @set:Throws(SQLException::class)
    @set:Override
    var holdability: Int
        get() = connection.getHoldability()
        set(holdability) {
            connection.setHoldability(holdability)
        }

    @get:Throws(SQLException::class)
    @get:Override
    val metaData: DatabaseMetaData?
        get() = connection.getMetaData()

    @get:Throws(SQLException::class)
    @get:Override
    @set:Throws(SQLException::class)
    @set:Override
    var transactionIsolation: Int
        get() = connection.getTransactionIsolation()
        set(level) {
            connection.setTransactionIsolation(level)
        }

    @get:Throws(SQLException::class)
    @get:Override
    @set:Throws(SQLException::class)
    @set:Override
    var typeMap: Map<String?, Any?>?
        get() = connection.getTypeMap()
        set(map) {
            connection.setTypeMap(map)
        }

    @get:Throws(SQLException::class)
    @get:Override
    val warnings: SQLWarning?
        get() = connection.getWarnings()

    @get:Throws(SQLException::class)
    @get:Override
    val isClosed: Boolean
        get() = connection.isClosed()

    @get:Throws(SQLException::class)
    @get:Override
    @set:Throws(SQLException::class)
    @set:Override
    var isReadOnly: Boolean
        get() = connection.isReadOnly()
        set(readOnly) {
            connection.setReadOnly(readOnly)
        }

    @Override
    @Throws(SQLException::class)
    fun isValid(timeout: Int): Boolean {
        return connection.isValid(timeout)
    }

    @Override
    @Throws(SQLException::class)
    fun nativeSQL(sql: String?): String? {
        return connection.nativeSQL(sql)
    }

    @Override
    @Throws(SQLException::class)
    fun releaseSavepoint(savepoint: Savepoint?) {
        connection.releaseSavepoint(savepoint)
    }

    @Override
    @Throws(SQLException::class)
    fun rollback() {
        connection.rollback()
    }

    @Override
    @Throws(SQLException::class)
    fun rollback(savepoint: Savepoint?) {
        connection.rollback(savepoint)
    }

    @Override
    @Throws(SQLClientInfoException::class)
    fun setClientInfo(name: String?, value: String?) {
        connection.setClientInfo(name, value)
    }

    @Override
    @Throws(SQLException::class)
    fun setSavepoint(): Savepoint? {
        return connection.setSavepoint()
    }

    @Override
    @Throws(SQLException::class)
    fun setSavepoint(name: String?): Savepoint? {
        return connection.setSavepoint(name)
    }

    @get:Throws(SQLException::class)
    @get:Override
    @set:Throws(SQLException::class)
    @set:Override
    var schema: String?
        get() = connection.getSchema()
        set(schema) {
            connection.setSchema(schema)
        }

    // used only with java 7, do not set @Override
    @Override
    @Throws(SQLException::class)
    fun abort(executor: Executor?) {
        connection.abort(executor)
    }

    @Override
    @Throws(SQLException::class)
    fun setNetworkTimeout(executor: Executor?, milliseconds: Int) {
        connection.setNetworkTimeout(executor, milliseconds)
    }

    @get:Throws(SQLException::class)
    @get:Override
    val networkTimeout: Int
        get() = connection.getNetworkTimeout()

    @Override
    @Throws(SQLException::class)
    fun isAutoCommit(): Boolean {
        return connection.getAutoCommit()
    }

    @get:Override
    val defaultTransactionIsolation: Int
        get() = (datasource as DataSourcePro?).getDefaultTransactionIsolation()

    @Override
    fun release() {
        IOUtil.closeEL(connection)
    }

    @Override
    fun validate(): Boolean {
        return datasource.validate()
    }

    init {
        datasource = ds
        // this should never happen
        if (datasource == null) {
            datasource = try {
                ORMUtil.getDefaultDataSource(pc)
            } catch (pe: PageException) {
                throw PageRuntimeException(pe)
            }
        }
        connection = ORMConnection(pc, session, datasource, transactionIsolation)
    }
}