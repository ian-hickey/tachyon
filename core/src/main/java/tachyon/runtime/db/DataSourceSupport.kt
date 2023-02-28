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

import java.io.IOException

abstract class DataSourceSupport(config: Config?, @get:Override val name: String, cd: ClassDefinition, username: String?, password: String, listener: TagListener?, blob: Boolean, clob: Boolean,
                                 connectionLimit: Int, idleTimeout: Int, liveTimeout: Int, minIdle: Int, maxIdle: Int, maxTotal: Int, metaCacheTimeout: Long, timezone: TimeZone?, allow: Int, storage: Boolean,
                                 readOnly: Boolean, validate: Boolean, requestExclusive: Boolean, alwaysResetConnections: Boolean, literalTimestampWithTSOffset: Boolean, log: Log?) : DataSourcePro, Cloneable, Serializable, Cloneable {
    @get:Override
    val isBlob: Boolean

    @get:Override
    val isClob: Boolean

    @get:Override
    val connectionLimit: Int

    @get:Override
    val connectionTimeout: Int
        @Override get() = field

    @get:Override
    override val liveTimeout: Int

    @get:Override
    val metaCacheTimeout: Long
    private val timezone: TimeZone?

    @get:Override
    val isStorage: Boolean
    private val validate: Boolean
    protected val allow: Int

    @get:Override
    val isReadOnly: Boolean

    @get:Override
    val username: String?

    @get:Override
    val password: String
    private val cd: ClassDefinition

    @Transient
    var procedureColumnCache: Map<String, SoftReference<ProcMetaCollection>>? = null
        get() {
            if (field == null) field = ConcurrentHashMap<String, SoftReference<ProcMetaCollection>>()
            return field
        }
        private set

    @Transient
    private var driver: Driver? = null

    @Transient
    private var log: Log?
    private override val listener: TagListener?

    @get:Override
    override val isRequestExclusive: Boolean

    // FUTURE add to interface
    val literalTimestampWithTSOffset: Boolean

    @get:Override
    override val isAlwaysResetConnections: Boolean

    @get:Override
    override val minIdle: Int

    @get:Override
    override val maxIdle: Int

    @get:Override
    override val maxTotal: Int

    @get:Override
    override var isMSSQL: Boolean? = null
        private set(isMSSQL) {
            super.isMSSQL = isMSSQL!!
        }

    @Override
    @Throws(ClassException::class, BundleException::class, SQLException::class)
    fun getConnection(config: Config, user: String?, pass: String?): Connection {
        var user = user
        var pass = pass
        return try {
            if (user == null) user = username
            if (pass == null) pass = password
            _getConnection(config, initialize(config), SQLUtil.connectionStringTranslatedPatch(config, getConnectionStringTranslated()), user, pass)
        } catch (e: InstantiationException) {
            throw RuntimeException(e)
        } catch (e: IllegalAccessException) {
            throw RuntimeException(e)
        } catch (e: IOException) {
            throw RuntimeException(e)
        } catch (e: IllegalArgumentException) {
            throw RuntimeException(e)
        } catch (e: InvocationTargetException) {
            throw RuntimeException(e.getTargetException())
        } catch (e: NoSuchMethodException) {
            throw RuntimeException(e)
        } catch (e: SecurityException) {
            throw RuntimeException(e)
        }
    }

    @get:Override
    override val defaultTransactionIsolation: Int
        get() = Companion.defaultTransactionIsolation

    @Throws(BundleException::class, InstantiationException::class, IllegalAccessException::class, IOException::class, IllegalArgumentException::class, InvocationTargetException::class, NoSuchMethodException::class, SecurityException::class)
    private fun initialize(config: Config): Driver {
        return if (driver == null) {
            _initializeDriver(cd, config).also { driver = it }
        } else driver
    }

    @Override
    fun clone(): Object {
        return cloneReadOnly()
    }

    @get:Override
    val timeZone: TimeZone?
        get() = timezone

    @get:Override
    val classDefinition: ClassDefinition
        get() = cd

    @Override
    fun hasAllow(allow: Int): Boolean {
        return this.allow and allow > 0
    }

    @Override
    fun hasSQLRestriction(): Boolean {
        return allow != DataSource.ALLOW_ALL
    }

    @Override
    fun validate(): Boolean {
        return validate
    }

    @Override
    fun setMSSQL(isMSSQL: Boolean) {
        this.isMSSQL = if (isMSSQL) Boolean.TRUE else Boolean.FALSE
    }

    @Override
    fun getLog(): Log? {
        // can be null if deserialized
        if (log == null) log = ThreadLocalPageContext.getLog("application")
        return log
    }

    @Override
    fun getListener(): TagListener? { // FUTURE may add to interface
        return listener
    }

    @Override
    override fun equals(obj: Object): Boolean {
        if (this === obj) return true
        if (obj !is DataSource) return false
        val ds: DataSource = obj as DataSource
        return id().equals(ds.id())
    }

    @Override
    override fun hashCode(): Int {
        return id().hashCode()
    }

    @Override
    fun id(): String {
        return StringBuilder(getConnectionStringTranslated()).append(':').append(connectionLimit).append(':').append(connectionTimeout).append(':')
                .append(liveTimeout).append(':').append(metaCacheTimeout).append(':').append(name.toLowerCase()).append(':').append(username).append(':')
                .append(password).append(':').append(validate()).append(':').append(cd.toString()).append(':').append(if (timeZone == null) "null" else timeZone.getID())
                .append(':').append(isBlob).append(':').append(isClob).append(':').append(isReadOnly).append(':').append(isStorage).append(':').append(isRequestExclusive)
                .append(':').append(isAlwaysResetConnections).toString()
    }

    @Override
    override fun toString(): String {
        return id()
    }

    companion object {
        private const val serialVersionUID = -9111025519905149021L

        @get:Override
        val networkTimeout = 10
            get() = Companion.field
        private var defaultTransactionIsolation = -1
        @Throws(SQLException::class)
        fun _getConnection(config: Config?, driver: Driver, connStrTrans: String?, user: String?, pass: String?): Connection {
            val props: java.util.Properties = Properties()
            if (user != null) props.put("user", user)
            if (pass != null) props.put("password", pass)
            if (defaultTransactionIsolation == -1) {
                val c: Connection = driver.connect(connStrTrans, props)
                defaultTransactionIsolation = getValidTransactionIsolation(c, Connection.TRANSACTION_READ_COMMITTED)
                return c
            }
            return driver.connect(connStrTrans, props)
        }

        private fun getValidTransactionIsolation(conn: Connection, defaultValue: Int): Int {
            try {
                val transactionIsolation: Int = conn.getTransactionIsolation()
                if (transactionIsolation == Connection.TRANSACTION_READ_COMMITTED) return Connection.TRANSACTION_READ_COMMITTED
                if (transactionIsolation == Connection.TRANSACTION_SERIALIZABLE) return Connection.TRANSACTION_SERIALIZABLE
                if (SQLUtil.isOracle(conn)) return defaultValue
                if (transactionIsolation == Connection.TRANSACTION_READ_UNCOMMITTED) return Connection.TRANSACTION_READ_UNCOMMITTED
                if (transactionIsolation == Connection.TRANSACTION_REPEATABLE_READ) return Connection.TRANSACTION_REPEATABLE_READ
            } catch (e: Exception) {
            }
            return defaultValue
        }

        @Throws(ClassException::class, BundleException::class, InstantiationException::class, IllegalAccessException::class, IllegalArgumentException::class, InvocationTargetException::class, NoSuchMethodException::class, SecurityException::class)
        private fun _initializeDriver(cd: ClassDefinition, config: Config): Driver {
            // load the class
            return ClassUtil.newInstance(cd.getClazz()) as Driver
        }

        @Throws(ClassException::class, BundleException::class, SQLException::class)
        fun verify(config: Config, cd: ClassDefinition, connStrTranslated: String?, user: String?, pass: String?) {
            try {
                // Driver driver = _initializeDriver(_initializeCD(jdbc, cd, config),config);
                val driver: Driver = _initializeDriver(cd, config)
                _getConnection(config, driver, connStrTranslated, user, pass)
            } catch (e: InstantiationException) {
                throw RuntimeException(e)
            } catch (e: IllegalAccessException) {
                throw RuntimeException(e)
            } catch (e: IllegalArgumentException) {
                throw RuntimeException(e)
            } catch (e: InvocationTargetException) {
                throw RuntimeException(e.getTargetException())
            } catch (e: NoSuchMethodException) {
                throw RuntimeException(e)
            } catch (e: SecurityException) {
                throw RuntimeException(e)
            }
        }
    }

    init {
        this.cd = cd // _initializeCD(null, cd, config);
        isBlob = blob
        isClob = clob
        this.connectionLimit = connectionLimit
        connectionTimeout = idleTimeout
        this.liveTimeout = liveTimeout
        this.metaCacheTimeout = metaCacheTimeout
        this.timezone = timezone
        this.allow = allow
        isStorage = storage
        isReadOnly = readOnly
        this.username = username
        this.password = password
        this.listener = listener
        this.validate = validate
        isRequestExclusive = requestExclusive
        isAlwaysResetConnections = alwaysResetConnections
        this.log = log
        this.literalTimestampWithTSOffset = literalTimestampWithTSOffset
        this.minIdle = minIdle
        this.maxIdle = maxIdle
        this.maxTotal = maxTotal
    }
}