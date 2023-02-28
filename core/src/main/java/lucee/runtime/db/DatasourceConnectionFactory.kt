package lucee.runtime.db

import java.io.IOException

class DatasourceConnectionFactory(config: Config, datasource: DataSource, username: String?, password: String?, logName: String?) : BasePooledObjectFactory<DatasourceConnection?>() {
    private var pool: DatasourceConnPool? = null
    private val config: Config
    private val datasource: DataSource
    val username: String? = null
    val password: String? = null
    private val logName: String?
    fun setPool(pool: DatasourceConnPool?) {
        this.pool = pool
    }

    @Override
    @Throws(IOException::class)
    fun create(): DatasourceConnection {
        LogUtil.log(config, Log.LEVEL_DEBUG, logName, "connection", "create datasource connection: " + datasource.getName())
        var conn: Connection? = null
        conn = try {
            (datasource as DataSourcePro).getConnection(config, username, password)
        } catch (e: SQLException) {
            throw IOException(e)
        } catch (e: Exception) {
            throw ExceptionUtil.toIOException(e)
        }
        return DatasourceConnectionImpl(pool, conn, datasource as DataSourcePro, username, password)
    }

    /**
     * Use the default PooledObject implementation.
     */
    @Override
    fun wrap(dc: DatasourceConnection?): PooledObject<DatasourceConnection> {
        return DefaultPooledObject<DatasourceConnection>(dc)
    }

    @Override
    fun validateObject(p: PooledObject<DatasourceConnection?>): Boolean {
        LogUtil.log(config, Log.LEVEL_DEBUG, logName, "connection", "validate datasource connection: " + datasource.getName())
        val dc: DatasourceConnection = p.getObject()
        val dsp: DataSourcePro = dc.getDatasource()
        if (dc.isTimeout()) {
            LogUtil.log(config, Log.LEVEL_DEBUG, logName, "connection", "reached idle timeout for datasource connection: " + datasource.getName())
            return false
        }
        if (dc.isLifecycleTimeout()) {
            LogUtil.log(config, Log.LEVEL_DEBUG, logName, "connection", "reached life timeout for datasource connection: " + datasource.getName())
            return false
        }
        try {
            if (dc.getConnection().isClosed()) return false
        } catch (e: Exception) {
            LogUtil.log(config, logName, "connection", e, Log.LEVEL_ERROR)
            return false
        }
        try {
            if (dc.getDatasource().validate() && !DataSourceUtil.isValid(dc, 1000)) return false
        } catch (e: Exception) {
            LogUtil.log(config, logName, "connection", e, Log.LEVEL_ERROR)
        }
        return true
    }

    @Override
    @Throws(PageException::class)
    fun activateObject(p: PooledObject<DatasourceConnection?>) {
        LogUtil.log(config, Log.LEVEL_DEBUG, logName, "connection", "activate datasource connection: " + datasource.getName())
        (p.getObject() as DatasourceConnectionImpl).using()
    }

    @Override
    @Throws(PageException::class)
    fun destroyObject(p: PooledObject<DatasourceConnection?>) {
        LogUtil.log(config, Log.LEVEL_DEBUG, logName, "connection", "destroy datasource connection: " + datasource.getName())
        var dc: DatasourceConnection? = null
        try {
            dc = p.getObject()
            dc.close()
        } catch (e: SQLException) {
            throw DatabaseException(e, dc)
        }
    }

    fun getDatasource(): DataSource {
        return datasource
    }

    companion object {
        private val tokens: ConcurrentHashMap<String, String> = ConcurrentHashMap<String, String>()
        fun createId(datasource: DataSource, user: String?, pass: String?): String? {
            val str: String = StringBuilder().append(datasource.id()).append("::").append(user).append(":").append(pass).toString()
            var lock: String = tokens.putIfAbsent(str, str)
            if (lock == null) {
                lock = str
            }
            return lock
        }
    }

    init {
        this.config = config
        this.datasource = datasource
        if (StringUtil.isEmpty(username)) {
            this.username = datasource.getUsername()
            this.password = datasource.getPassword()
        } else {
            this.username = username
            this.password = password ?: ""
        }
        this.logName = if (StringUtil.isEmpty(logName)) null else logName
        // TODO use socketTimeout
    }
}