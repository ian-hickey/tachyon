package lucee.commons.io.log.log4j2.appender

import java.sql.Types

class DatasourceAppender(config: Config, fallback: Appender, name: String?, filter: Filter?, private val datasourceName: String, private val username: String, private val password: String, tableName: String, custom: String?) : AbstractAppender(name, filter, null) {
    // private DatasourceManagerImpl manager;
    private var datasource: DataSource? = null
    val tableName: String
    private val config: Config
    private val custom: String?
    private val fallback: Appender
    private var isInit = false
    private val token: Object = Object()
    private var pool: DatasourceConnPool? = null
    @Override
    fun append(event: LogEvent) {
        if (!isInit) {
            init()
            if (!isInit) {
                fallback.append(event)
                return
            }
        }
        var conn: DatasourceConnection? = null
        try {

            // ththreadId=
            var threadId: String = event.getThreadName()
            if (threadId.length() > 64) threadId = threadId.substring(0, 63)

            // split application->message
            var application: String
            var msg: String = Caster.toString(event.getMessage(), null)
            val index: Int = msg.indexOf("->")
            if (index > -1) {
                application = msg.substring(0, index)
                if (application.length() > 64) application = application.substring(0, 63)
                msg = msg.substring(index + 2)
            } else application = ""
            if (msg.length() > 512) msg = msg.substring(0, 508).toString() + "..."

            // get Exception
            var exception = ""
            val t: Throwable = event.getThrown()
            if (t != null) {
                val em: String = ExceptionUtil.getMessage(t)
                if (StringUtil.isEmpty(msg)) msg = em else msg += ";$em"
                exception = ExceptionUtil.getStacktrace(t, false)
                if (exception == null) exception = "" else if (exception.length() > 2048) exception = exception.substring(0, 2044).toString() + "..."
            }

            // id
            var id = ""
            val c: Config = ThreadLocalPageContext.getConfig()
            if (c != null) {
                id = if (c is ConfigWeb) (c as ConfigWeb).getLabel() else c.getIdentification().getId()
            }
            conn = connection
            val optionalPC: PageContext = ThreadLocalPageContext.get()
            val sql = SQLImpl("INSERT INTO $tableName (id,name,severity,threadid,time,application,message,exception,custom) values(?,?,?,?,?,?,?,?,?)", arrayOf<SQLItem>(
                    SQLItemImpl(id, Types.VARCHAR), SQLItemImpl(getName(), Types.VARCHAR), SQLItemImpl(event.getLevel().name(), Types.VARCHAR), SQLItemImpl(threadId, Types.VARCHAR), SQLItemImpl(DateTimeImpl(event.getTimeMillis(), false), Types.TIMESTAMP), SQLItemImpl(application, Types.VARCHAR), SQLItemImpl(msg, Types.VARCHAR), SQLItemImpl(exception, Types.VARCHAR), SQLItemImpl(custom
                    ?: "", Types.VARCHAR)
            ))
            QueryImpl(optionalPC, conn, sql, -1, -1, null, "query")
        } catch (pe: PageException) {
            LogUtil.logGlobal(config, "log-loading", pe)
        } finally {
            try {
                relConnection(conn)
            } catch (pee: PageException) {
                LogUtil.logGlobal(config, "log-loading", pee)
            }
        }
    }

    private fun init() {
        synchronized(token) {
            if (!isInit) {
                var conn: DatasourceConnection? = null
                val optionalPC: PageContext = ThreadLocalPageContext.get()
                val sql = SQLImpl("select 1 from $tableName where 1=0")
                try {
                    conn = connection
                    isInit = try {
                        QueryImpl(optionalPC, conn, sql, -1, -1, null, "query")
                        true
                    } catch (pe: PageException) {
                        // SystemOut.printDate(pe);
                        try {
                            QueryImpl(optionalPC, conn, createSQL(conn), -1, -1, null, "query")
                            true
                        } catch (e2: Exception) {
                            // SystemOut.printDate(e2);
                            throw pe
                        }
                    } finally {
                        relConnection(conn)
                    }
                } catch (pe: PageException) {
                    LogUtil.logGlobal(config, "log-loading", pe)
                    isInit = false
                }
            }
        }
    }

    private fun createSQL(dc: DatasourceConnection?): SQL {
        val sb = StringBuilder("CREATE TABLE ")
        if (DataSourceUtil.isMSSQL(dc)) sb.append("dbo.")
        sb.append(tableName).append(" ( ")
        if (DataSourceUtil.isMSSQL(dc)) sb.append("pid INT PRIMARY KEY IDENTITY (1, 1), ") else if (DataSourceUtil.isMySQL(dc)) sb.append("pid INT AUTO_INCREMENT PRIMARY KEY, ") else if (DataSourceUtil.isHSQLDB(dc)) sb.append("pid INTEGER IDENTITY PRIMARY KEY, ") else if (DataSourceUtil.isPostgres(dc)) sb.append("id SERIAL PRIMARY KEY, ")
        sb.append("id varchar(32) NOT NULL, ")
        sb.append("name varchar(128) NOT NULL, ")
        sb.append("severity varchar(16) NOT NULL, ")
        sb.append("threadid varchar(64) NOT NULL, ")
        sb.append("time datetime NOT NULL, ")
        sb.append("application varchar(64) NOT NULL, ")
        sb.append("message varchar(512) NOT NULL, ")
        sb.append("exception varchar(2048) NOT NULL, ")
        sb.append("custom varchar(2048) NOT NULL ")
        sb.append(");")
        return SQLImpl(sb.toString())
    }

    @Throws(PageException::class)
    private fun pool(): DatasourceConnPool? {
        if (pool == null) {
            if (datasourceName == null) datasourceName = config.getDataSource(datasourceName)
            pool = (config as ConfigPro).getDatasourceConnectionPool(datasourceName, username, password)
        }
        return pool
    }

    @get:Throws(PageException::class)
    private val connection: DatasourceConnection
        private get() = pool().borrowObject()

    @Throws(PageException::class)
    protected fun relConnection(conn: DatasourceConnection?) {
        pool().returnObject(conn)
    }

    init {
        this.config = config
        this.tableName = tableName
        this.custom = custom
        this.fallback = fallback
    }
}