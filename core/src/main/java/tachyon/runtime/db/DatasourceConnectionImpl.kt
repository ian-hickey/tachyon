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
package tachyon.runtime.db

import java.sql.Array

/**
 * wrap for datasorce and connection from it
 */
class DatasourceConnectionImpl(pool: DatasourceConnPool?, connection: Connection?, datasource: DataSourcePro, username: String?, password: String?) : DatasourceConnectionPro, Task {
    private val connection: Connection?
    private val datasource: DataSourcePro
    private var lastUsed: Long
    private val created: Long

    /**
     * @return the username
     */
    @get:Override
    val username: String?

    /**
     * @return the password
     */
    @get:Override
    val password: String?

    /**
     * @return the transactionIsolationLevel
     */
    val transactionIsolationLevel = -1
    var requestId = -1
    private var supportsGetGeneratedKeys: Boolean? = null
    private val pool: DatasourceConnPool?
    private var lastValidation: Long

    @get:Override
    @set:Override
    override var isManaged = false
    @Override
    fun getConnection(): Connection? {
        return connection
    }

    @Override
    fun getDatasource(): DataSource {
        return datasource
    }

    @get:Override
    val isTimeout: Boolean
        get() {
            var timeout: Int = datasource.getIdleTimeout()
            if (timeout <= 0) return false
            timeout *= 60000
            return lastUsed + timeout < System.currentTimeMillis()
        }

    @get:Override
    val isLifecycleTimeout: Boolean
        get() {
            var timeout: Int = datasource.getLiveTimeout()
            if (timeout <= 0) return false
            timeout *= 60000
            return created + timeout < System.currentTimeMillis()
        }

    @Override
    @Throws(PageException::class)
    override fun using(): DatasourceConnection {
        lastUsed = System.currentTimeMillis()
        if (datasource.isAlwaysResetConnections()) {
            try {
                connection.setAutoCommit(true)
                DBUtil.setTransactionIsolationEL(connection, defaultTransactionIsolation)
            } catch (sqle: SQLException) {
                throw Caster.toPageException(sqle)
            }
        }
        return this
    }

    @Override
    override fun equals(obj: Object): Boolean {
        return if (this === obj) true else equals(this, obj as DatasourceConnection)
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

    // private Map<String,PreparedStatement> preparedStatements=new HashMap<String,
    // PreparedStatement>();
    @Override
    @Throws(SQLException::class)
    fun getPreparedStatement(sql: SQL, createGeneratedKeys: Boolean, allowCaching: Boolean): PreparedStatement {
        return if (createGeneratedKeys) getConnection().prepareStatement(sql.getSQLString(), Statement.RETURN_GENERATED_KEYS) else getConnection().prepareStatement(sql.getSQLString())
    }

    @Override
    @Throws(SQLException::class)
    fun getPreparedStatement(sql: SQL, resultSetType: Int, resultSetConcurrency: Int): PreparedStatement {
        return getConnection().prepareStatement(sql.getSQLString(), resultSetType, resultSetConcurrency)
    }

    @Override
    @Throws(PageException::class)
    fun execute(config: Config?): Object? {
        release()
        return null
    }

    @Override
    @Throws(SQLException::class)
    fun createStatement(): Statement {
        return connection.createStatement()
    }

    @Override
    @Throws(SQLException::class)
    fun createStatement(resultSetType: Int, resultSetConcurrency: Int): Statement {
        return connection.createStatement(resultSetType, resultSetConcurrency)
    }

    @Override
    @Throws(SQLException::class)
    fun createStatement(resultSetType: Int, resultSetConcurrency: Int, resultSetHoldability: Int): Statement {
        return connection.createStatement(resultSetType, resultSetConcurrency, resultSetHoldability)
    }

    @Override
    @Throws(SQLException::class)
    fun prepareCall(sql: String?): CallableStatement {
        return connection.prepareCall(sql)
    }

    @Override
    @Throws(SQLException::class)
    fun prepareCall(sql: String?, resultSetType: Int, resultSetConcurrency: Int): CallableStatement {
        return connection.prepareCall(sql, resultSetType, resultSetConcurrency)
    }

    @Override
    @Throws(SQLException::class)
    fun prepareCall(sql: String?, resultSetType: Int, resultSetConcurrency: Int, resultSetHoldability: Int): CallableStatement {
        return connection.prepareCall(sql, resultSetType, resultSetConcurrency, resultSetHoldability)
    }

    @Override
    @Throws(SQLException::class)
    fun prepareStatement(sql: String?): PreparedStatement {
        return connection.prepareStatement(sql)
    }

    @Override
    @Throws(SQLException::class)
    fun prepareStatement(sql: String?, autoGeneratedKeys: Int): PreparedStatement {
        return connection.prepareStatement(sql, autoGeneratedKeys)
    }

    @Override
    @Throws(SQLException::class)
    fun prepareStatement(sql: String?, columnIndexes: IntArray?): PreparedStatement {
        return connection.prepareStatement(sql, columnIndexes)
    }

    @Override
    @Throws(SQLException::class)
    fun prepareStatement(sql: String?, columnNames: Array<String?>?): PreparedStatement {
        return connection.prepareStatement(sql, columnNames)
    }

    @Override
    @Throws(SQLException::class)
    fun prepareStatement(sql: String?, resultSetType: Int, resultSetConcurrency: Int): PreparedStatement {
        return connection.prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetConcurrency)
    }

    @Override
    @Throws(SQLException::class)
    fun prepareStatement(sql: String?, resultSetType: Int, resultSetConcurrency: Int, resultSetHoldability: Int): PreparedStatement {
        return connection.prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability)
    }

    @Override
    @Throws(SQLException::class)
    fun isWrapperFor(iface: Class<*>?): Boolean {
        return connection.isWrapperFor(iface)
    }

    @Override
    @Throws(SQLException::class)
    fun <T> unwrap(iface: Class<T>?): T {
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
    fun createArrayOf(typeName: String?, elements: Array<Object?>?): Array {
        return connection.createArrayOf(typeName, elements)
    }

    @Override
    @Throws(SQLException::class)
    fun createBlob(): Blob {
        return connection.createBlob()
    }

    @Override
    @Throws(SQLException::class)
    fun createClob(): Clob {
        return connection.createClob()
    }

    @Override
    @Throws(SQLException::class)
    fun createNClob(): NClob {
        return connection.createNClob()
    }

    @Override
    @Throws(SQLException::class)
    fun createSQLXML(): SQLXML {
        return connection.createSQLXML()
    }

    @Override
    @Throws(SQLException::class)
    fun createStruct(typeName: String?, attributes: Array<Object?>?): Struct {
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
    var clientInfo: Properties
        get() = connection.getClientInfo()
        set(properties) {
            connection.setClientInfo(properties)
        }

    @Override
    @Throws(SQLException::class)
    fun getClientInfo(name: String?): String {
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
    val metaData: DatabaseMetaData
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
    val typeMap: Map<String, Any>
        get() = connection.getTypeMap()

    @get:Throws(SQLException::class)
    @get:Override
    val warnings: SQLWarning
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
    fun nativeSQL(sql: String?): String {
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
    fun setSavepoint(): Savepoint {
        return connection.setSavepoint()
    }

    @Override
    @Throws(SQLException::class)
    fun setSavepoint(name: String?): Savepoint {
        return connection.setSavepoint(name)
    }

    @Override
    @Throws(SQLException::class)
    fun setTypeMap(map: Map<String?, Class<*>?>?) {
        connection.setTypeMap(map)
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
    override val defaultTransactionIsolation: Int
        get() = datasource.getDefaultTransactionIsolation()

    @Override
    override fun release() {
        isManaged = false
        try {
            pool.returnObject(this)
        } catch (ise: IllegalStateException) {
            // old Hibernate extension cause: Object has already been returned to this pool or is invalid
        }
    }

    @Override
    override fun validate(): Boolean {
        if (getDatasource().validate()) return true
        var now: Long
        if (lastValidation + VALIDATION_TIMEOUT < System.currentTimeMillis().also { now = it }) {
            lastValidation = now
            return true
        }
        return false
    }

    companion object {
        // private static final int MAX_PS = 100;
        private const val VALIDATION_TIMEOUT = 60000
        fun equals(left: DatasourceConnection, right: DatasourceConnection): Boolean {
            return if (!left.getDatasource().equals(right.getDatasource())) false else StringUtil.emptyIfNull(left.getUsername()).equals(StringUtil.emptyIfNull(right.getUsername()))
                    && StringUtil.emptyIfNull(left.getPassword()).equals(StringUtil.emptyIfNull(right.getPassword()))
        }

        fun equals(dc: DatasourceConnection?, ds: DataSource, user: String?, pass: String?): Boolean {
            var user = user
            var pass = pass
            if (StringUtil.isEmpty(user)) {
                user = ds.getUsername()
                pass = ds.getPassword()
            }
            return if (!dc.getDatasource().equals(ds)) false else StringUtil.emptyIfNull(dc.getUsername()).equals(StringUtil.emptyIfNull(user)) && StringUtil.emptyIfNull(dc.getPassword()).equals(StringUtil.emptyIfNull(pass))
        }
    }

    /**
     * @param connection
     * @param datasource
     * @param pass
     * @param user
     */
    init {
        this.pool = pool
        this.connection = connection
        this.datasource = datasource
        created = System.currentTimeMillis()
        lastUsed = created
        this.username = username
        this.password = password
        if (username == null) {
            this.username = datasource.getUsername()
            this.password = datasource.getPassword()
        }
        if (this.password == null) this.password = ""
        lastValidation = System.currentTimeMillis()
    }
}