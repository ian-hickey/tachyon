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
package lucee.commons.io.res.type.datasource

import java.io.IOException

/**
 * Resource Provider for ram resource
 */
class DatasourceResourceProvider : ResourceProviderPro {
    @get:Override
    var scheme = "ds"
        private set

    @get:Override
    var isCaseSensitive = true
    private var lockTimeout: Long = 1000
    private val lock: ResourceLockImpl = ResourceLockImpl(lockTimeout, isCaseSensitive)
    private var _manager: DatasourceManagerImpl? = null
    private var defaultPrefix = "rdr"
    private val cores: Map = WeakHashMap()
    private val attrCache: Map<String, SoftReference<Attr>> = ConcurrentHashMap<String, SoftReference<Attr>>()
    private val attrsCache: Map<String, SoftReference<Attr>> = ConcurrentHashMap<String, SoftReference<Attr>>()
    private var arguments: Map? = null

    /**
     * initialize ram resource
     *
     * @param scheme
     * @param arguments
     * @return RamResource
     */
    @Override
    fun init(scheme: String, arguments: Map?): ResourceProvider {
        if (!StringUtil.isEmpty(scheme)) this.scheme = scheme
        if (arguments != null) {
            this.arguments = arguments
            // case-sensitive
            val oCaseSensitive: Object = arguments.get("case-sensitive")
            if (oCaseSensitive != null) {
                isCaseSensitive = Caster.toBooleanValue(oCaseSensitive, true)
            }

            // prefix
            val oPrefix: Object = arguments.get("prefix")
            if (oPrefix != null) {
                defaultPrefix = Caster.toString(oPrefix, defaultPrefix)
            }

            // lock-timeout
            val oTimeout: Object = arguments.get("lock-timeout")
            if (oTimeout != null) {
                lockTimeout = Caster.toLongValue(oTimeout, lockTimeout)
            }
        }
        lock.setLockTimeout(lockTimeout)
        lock.setCaseSensitive(isCaseSensitive)
        return this
    }

    @Override
    fun getResource(path: String): Resource {
        val sb = StringBuilder()
        return DatasourceResource(this, parse(sb, path), sb.toString())
    }

    fun parse(subPath: StringBuilder, path: String): ConnectionData {
        var path = path
        path = ResourceUtil.removeScheme(scheme, path)
        val data: ConnectionData = ConnectionData()
        val atIndex: Int = path.indexOf('@')
        var slashIndex: Int = path.indexOf('/')
        if (slashIndex == -1) {
            slashIndex = path.length()
            path += "/"
        }
        var index: Int

        // username/password
        if (atIndex != -1) {
            index = path.indexOf(':')
            if (index != -1 && index < atIndex) {
                data.username = path.substring(0, index)
                data.password = path.substring(index + 1, atIndex)
            } else data.username = path.substring(0, atIndex)
        }
        // host port
        if (slashIndex > atIndex + 1) {
            data.datasourceName = path.substring(atIndex + 1, slashIndex)
        }
        if (slashIndex > atIndex + 1) {
            index = path.indexOf(':', atIndex + 1)
            if (index != -1 && index > atIndex && index < slashIndex) {
                data.datasourceName = path.substring(atIndex + 1, index)
                data.prefix = path.substring(index + 1, slashIndex)
            } else {
                data.datasourceName = path.substring(atIndex + 1, slashIndex)
                data.prefix = defaultPrefix
            }
        }
        subPath.append(path.substring(slashIndex))
        return data
    }

    @Override
    fun setResources(resources: Resources?) {
        // this.resources=resources;
    }

    @Override
    @Throws(IOException::class)
    fun lock(res: Resource?) {
        lock.lock(res)
    }

    @Override
    fun unlock(res: Resource?) {
        lock.unlock(res)
    }

    @Override
    @Throws(IOException::class)
    fun read(res: Resource?) {
        lock.read(res)
    }

    @get:Override
    val isAttributesSupported: Boolean
        get() = false

    @get:Override
    val isModeSupported: Boolean
        get() = true
    private val manager: DatasourceManagerImpl?
        private get() {
            if (_manager == null) {
                val config: Config = ThreadLocalPageContext.getConfig()
                _manager = DatasourceManagerImpl(config as ConfigPro)
            }
            return _manager
        }

    @Throws(PageException::class)
    private fun getCore(data: ConnectionData): Core {
        var core: Core? = cores.get(data.datasourceName) as Core
        if (core == null) {
            val dc: DatasourceConnection = manager.getConnection(ThreadLocalPageContext.get(), data.datasourceName, data.username, data.password)
            try {
                dc.setAutoCommit(false)
                dc.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED)
                if ("com.microsoft.jdbc.sqlserver.SQLServerDriver".equals(dc.getDatasource().getClassDefinition().getClassName())) core = MSSQL(dc, data.prefix) else if ("com.microsoft.sqlserver.jdbc.SQLServerDriver".equals(dc.getDatasource().getClassDefinition().getClassName())) core = MSSQL(dc, data.prefix) else if ("net.sourceforge.jtds.jdbc.Driver".equals(dc.getDatasource().getClassDefinition().getClassName())) core = MSSQL(dc, data.prefix) else if ("org.gjt.mm.mysql.Driver".equals(dc.getDatasource().getClassDefinition().getClassName())) core = MySQL(dc, data.prefix) else throw ApplicationException("There is no DatasourceResource driver for this database [" + data.prefix + "]")
                cores.put(data.datasourceName, core)
            } catch (e: SQLException) {
                throw DatabaseException(e, dc)
            } finally {
                release(dc)
            }
        }
        return core
    }

    @Throws(PageException::class)
    private fun getDatasourceConnection(data: ConnectionData, autoCommit: Boolean): DatasourceConnection {
        val dc: DatasourceConnection = manager.getConnection(ThreadLocalPageContext.get(), data.datasourceName, data.username, data.password)
        try {
            dc.getConnection().setAutoCommit(autoCommit)
            dc.getConnection().setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED)
        } catch (e: SQLException) {
            throw DatabaseException(e, dc)
        }
        return dc
    }

    @Throws(PageException::class)
    private fun getDatasourceConnection(data: ConnectionData): DatasourceConnection {
        return getDatasourceConnection(data, false)
    }

    fun getAttr(data: ConnectionData, fullPathHash: Int, path: String?, name: String?): Attr? {
        val attr: Attr? = getFromCache(data, path, name)
        return if (attr != null) attr else try {
            _getAttr(data, fullPathHash, path, name)
        } catch (pe: PageException) {
            throw PageRuntimeException(pe)
        }
    }

    @Throws(PageException::class)
    private fun _getAttr(data: ConnectionData, fullPathHash: Int, path: String?, name: String?): Attr? {
        if (!StringUtil.isEmpty(data.datasourceName)) {
            var dc: DatasourceConnection? = null
            try {
                dc = getDatasourceConnection(data)
                val attr: Attr = getCore(data).getAttr(dc, data.prefix, fullPathHash, path, name)
                if (attr != null) return putToCache(data, path, name, attr)
            } catch (e: SQLException) {
                throw DatabaseException(e, dc)
            } finally {
                manager.releaseConnection(ThreadLocalPageContext.get(), dc)
            }
        }
        return putToCache(data, path, name, Attr.notExists(name, path))
    }

    @Throws(PageException::class)
    fun getAttrs(data: ConnectionData, pathHash: Int, path: String?): Array<Attr?>? {
        if (StringUtil.isEmpty(data.datasourceName)) return null
        var dc: DatasourceConnection? = null
        try {
            dc = getDatasourceConnection(data)
            val list: List = getCore(data).getAttrs(dc, data.prefix, pathHash, path)
            if (list != null) {
                val it: Iterator = list.iterator()
                val rtn: Array<Attr?> = arrayOfNulls<Attr>(list.size())
                var index = 0
                while (it.hasNext()) {
                    rtn[index] = it.next() as Attr
                    putToCache(data, rtn[index].getParent(), rtn[index].getName(), rtn[index])
                    index++
                }
                return rtn
            }
        } catch (e: SQLException) {
            throw DatabaseException(e, dc)
        } finally {
            release(dc)
        }
        return null
    }

    @Throws(IOException::class)
    fun create(data: ConnectionData, fullPathHash: Int, pathHash: Int, path: String?, name: String?, type: Int) {
        if (StringUtil.isEmpty(data.datasourceName)) throw IOException("Missing datasource definition")
        removeFromCache(data, path, name)
        var dc: DatasourceConnection? = null
        try {
            dc = getDatasourceConnection(data)
            getCore(data).create(dc, data.prefix, fullPathHash, pathHash, path, name, type)
        } catch (e: SQLException) {
            throw IOException(e.getMessage())
        } catch (e: PageException) {
            throw PageRuntimeException(e)
        } finally {
            release(dc)
        }
    }

    @Throws(IOException::class)
    fun delete(data: ConnectionData, fullPathHash: Int, path: String?, name: String?) {
        val attr: Attr = getAttr(data, fullPathHash, path, name)
                ?: throw IOException("Can't delete resource [$path$name], resource does not exist")
        var dc: DatasourceConnection? = null
        try {
            dc = getDatasourceConnection(data)
            getCore(data).delete(dc, data.prefix, attr)
        } catch (e: SQLException) {
            throw IOException(e.getMessage())
        } catch (e: PageException) {
            throw PageRuntimeException(e)
        } finally {
            removeFromCache(data, path, name)
            release(dc)
            // manager.releaseConnection(CONNECTION_ID,dc);
        }
    }

    @Throws(IOException::class)
    fun getInputStream(data: ConnectionData, fullPathHash: Int, path: String?, name: String?): InputStream {
        val attr: Attr = getAttr(data, fullPathHash, path, name)
                ?: throw IOException("File [$path$name] does not exist")
        var dc: DatasourceConnection? = null
        return try {
            dc = getDatasourceConnection(data)
            getCore(data).getInputStream(dc, data.prefix, attr)
        } catch (e: SQLException) {
            throw IOException(e.getMessage())
        } catch (e: PageException) {
            throw PageRuntimeException(e)
        } finally {
            release(dc)
            // manager.releaseConnection(CONNECTION_ID,dc);
        }
    }

    @Synchronized
    @Throws(IOException::class)
    fun getOutputStream(data: ConnectionData, fullPathHash: Int, pathHash: Int, path: String?, name: String?, append: Boolean): OutputStream {
        var attr: Attr? = getAttr(data, fullPathHash, path, name)
        if (attr.getId() === 0) {
            create(data, fullPathHash, pathHash, path, name, Attr.TYPE_FILE)
            attr = getAttr(data, fullPathHash, path, name)
        }
        val pis = PipedInputStream()
        val pos = PipedOutputStream()
        pis.connect(pos)
        var dc: DatasourceConnection? = null
        // Connection c=null;
        return try {
            dc = getDatasourceConnection(data)
            // Connection c = dc.getConnection();
            val writer = DataWriter(getCore(data), dc, data.prefix, attr, pis, this, append)
            writer.start()
            DatasourceResourceOutputStream(writer, pos)
            // core.getOutputStream(dc, name, attr, pis);
        } catch (e: PageException) {
            throw PageRuntimeException(e)
        } finally {
            removeFromCache(data, path, name)
        }
    }

    fun setLastModified(data: ConnectionData, fullPathHash: Int, path: String?, name: String?, time: Long): Boolean {
        try {
            val attr: Attr? = getAttr(data, fullPathHash, path, name)
            val dc: DatasourceConnection = getDatasourceConnection(data)
            try {
                getCore(data).setLastModified(dc, data.prefix, attr, time)
            } finally {
                removeFromCache(data, path, name)
                release(dc)
            }
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
            return false
        }
        return true
    }

    fun setMode(data: ConnectionData, fullPathHash: Int, path: String?, name: String?, mode: Int): Boolean {
        try {
            val attr: Attr? = getAttr(data, fullPathHash, path, name)
            val dc: DatasourceConnection = getDatasourceConnection(data)
            try {
                getCore(data).setMode(dc, data.prefix, attr, mode)
            } /*
			 * catch (SQLException e) { return false; }
			 */ finally {
                removeFromCache(data, path, name)
                release(dc)
                // manager.releaseConnection(CONNECTION_ID,dc);
            }
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
            return false
        }
        return true
    }

    fun concatSupported(data: ConnectionData): Boolean {
        return try {
            getCore(data).concatSupported()
        } catch (e: PageException) {
            false
        }
    }

    private fun removeFromCache(data: ConnectionData, path: String?, name: String?): Attr? {
        attrsCache.remove(data.key() + path)
        val rtn: SoftReference<Attr> = attrCache.remove(data.key() + path + name)
        return if (rtn == null) null else rtn.get()
    }

    private fun getFromCache(data: ConnectionData, path: String?, name: String?): Attr? {
        val key = data.key() + path + name
        val tmp: SoftReference<Attr>? = attrCache[key]
        val attr: Attr? = if (tmp == null) null else tmp.get()
        if (attr != null && attr.timestamp() + MAXAGE < System.currentTimeMillis()) {
            attrCache.remove(key)
            return null
        }
        return attr
    }

    private fun putToCache(data: ConnectionData, path: String?, name: String?, attr: Attr?): Attr? {
        attrCache.put(data.key() + path + name, SoftReference<Attr>(attr))
        return attr
    }

    inner class ConnectionData {
        /**
         * @return the username
         */
        /**
         * @param username the username to set
         */
        var username: String? = null
        /**
         * @return the password
         */
        /**
         * @param password the password to set
         */
        var password: String? = null
        /**
         * @return the datasourceName
         */
        /**
         * @param datasourceName the datasourceName to set
         */
        var datasourceName: String? = null
        /**
         * @return the prefix
         */
        /**
         * @param prefix the prefix to set
         */
        var prefix: String? = null
        fun key(): String? {
            return if (StringUtil.isEmpty(username)) datasourceName else username.toString() + ":" + password + "@" + datasourceName
        }
    }

    /**
     * release datasource connection
     *
     * @param dc
     * @param autoCommit
     */
    fun release(dc: DatasourceConnection?) {
        if (dc != null) {
            try {
                dc.getConnection().commit()
                dc.getConnection().setAutoCommit(true)
                DBUtil.setTransactionIsolationEL(dc.getConnection(), (dc as DatasourceConnectionPro).getDefaultTransactionIsolation())
            } catch (e: SQLException) {
            }
            manager.releaseConnection(ThreadLocalPageContext.get(), dc)
        }
    }

    @Override
    fun getArguments(): Map? {
        return arguments
    }

    @get:Override
    val separator: Char
        get() = '/'

    companion object {
        const val DBTYPE_ANSI92 = 0
        const val DBTYPE_MSSQL = 1
        const val DBTYPE_MYSQL = 2
        private const val MAXAGE = 5000
    }
}