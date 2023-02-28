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

import java.sql.Connection

/**
 * this class handle multible db connection, transaction and logging
 */
class DatasourceManagerImpl(c: ConfigPro) : DataSourceManager {
    private val config: ConfigPro

    @get:Override
    var isAutoCommit = true
    private var isolation: Int = Connection.TRANSACTION_NONE
    private var transConnsReg: Map<DataSource, DatasourceConnectionPro>? = HashMap<DataSource, DatasourceConnectionPro>()
    private val transConnsORM: Map<DataSource, ORMDatasourceConnection> = HashMap<DataSource, ORMDatasourceConnection>()
    private var inside = false
    private val savepoints: Map<String, Savepoint> = ConcurrentHashMap()
    @Throws(PageException::class)
    fun getOpenConnections(pc: PageContext?, ds: DataSource?, user: String?, pass: String?): Long {
        return config.getDatasourceConnectionPool(ds, user, pass).getBorrowedCount()
    }

    @Override
    @Throws(PageException::class)
    fun getConnection(pc: PageContext, _datasource: String?, user: String?, pass: String?): DatasourceConnection {
        return getConnection(pc, pc.getDataSource(_datasource), user, pass)
    }

    @Override
    @Throws(PageException::class)
    fun getConnection(pc: PageContext?, ds: DataSource, user: String?, pass: String?): DatasourceConnection? {
        var pc: PageContext? = pc
        if (isAutoCommit && !(ds as DataSourcePro).isRequestExclusive()) {
            return config.getDatasourceConnectionPool(ds, user, pass).borrowObject()
        }
        pc = ThreadLocalPageContext.get(pc)
        // DatasourceConnection newDC = _getConnection(pc,ds,user,pass);
        var existingDC: DatasourceConnectionPro? = null
        return try {
            existingDC = transConnsReg!![ds]

            // first time that datasource is used within this transaction
            if (existingDC == null) {
                synchronized(getToken(ds.id())) {
                    existingDC = transConnsReg!![ds]
                    if (existingDC == null) {
                        val newDC: DatasourceConnectionPro = config.getDatasourceConnectionPool().getDatasourceConnection(config, ds, user, pass)
                        if (!isAutoCommit) {
                            newDC.setAutoCommit(false)
                            if (isolation != Connection.TRANSACTION_NONE) DBUtil.setTransactionIsolationEL(newDC.getConnection(), isolation)
                        }
                        newDC.setManaged(true)
                        transConnsReg.put(ds, newDC)
                        return newDC
                    }
                }
            }

            // we have already the same datasource but with different credentials
            if (!DatasourceConnectionImpl.equals(existingDC, ds, user, pass)) {
                if (QOQ_DATASOURCE_NAME.equalsIgnoreCase(ds.getName())) {
                    if (isAutoCommit) {
                        if (!existingDC.getAutoCommit()) {
                            existingDC.setAutoCommit(true)
                            DBUtil.setTransactionIsolationEL(existingDC.getConnection(), existingDC.getDefaultTransactionIsolation())
                        }
                    } else {
                        if (existingDC.getAutoCommit()) {
                            existingDC.setAutoCommit(false)
                            if (isolation != Connection.TRANSACTION_NONE) existingDC.setTransactionIsolation(isolation)
                        }
                    }
                    return existingDC
                }
                throw DatabaseException("can't use different connections to the same datasource inside a single transaction.", null, null, existingDC)
            }
            if (isAutoCommit) {
                if (!existingDC.getAutoCommit()) {
                    existingDC.setAutoCommit(true)
                    DBUtil.setTransactionIsolationEL(existingDC.getConnection(), existingDC.getDefaultTransactionIsolation())
                }
            } else {
                if (existingDC.getAutoCommit()) {
                    existingDC.setAutoCommit(false)
                    if (isolation != Connection.TRANSACTION_NONE) existingDC.setTransactionIsolation(isolation)
                }
            }
            existingDC
        } catch (e: SQLException) {
            throw DatabaseException(e, null, existingDC)
        }
    }

    @Throws(PageException::class)
    fun add(pc: PageContext, session: ORMSession) {
        if (isAutoCommit || inside) return
        inside = true
        try {
            val sources: Array<DataSource> = session.getDataSources()
            for (i in sources.indices) {
                _add(pc, session, sources[i])
            }
        } finally {
            inside = false
        }
    }

    @Throws(PageException::class)
    private fun _add(pc: PageContext, session: ORMSession, ds: DataSource) {
        var existingDC: DatasourceConnectionPro? = null
        try {
            existingDC = transConnsORM[ds]
            if (existingDC == null) {
                if (isolation == Connection.TRANSACTION_NONE) isolation = Connection.TRANSACTION_SERIALIZABLE
                val newDC = ORMDatasourceConnection(pc, session, ds, isolation)
                transConnsORM.put(ds, newDC)
                return
            }
            if (!DatasourceConnectionImpl.equals(existingDC, ds, null, null)) {
                // releaseConnection(pc,newDC);
                throw DatabaseException("can't use different connections to the same datasource inside a single transaction", null, null, existingDC)
            }
            if (existingDC.isAutoCommit()) {
                existingDC.setAutoCommit(false)
            }
            return
        } catch (e: SQLException) {
            throw DatabaseException(e, null, existingDC)
        }
    }

    @Override
    fun releaseConnection(pc: PageContext?, dc: DatasourceConnection) {
        releaseConnection(pc, dc, false)
    }

    private fun releaseConnection(pc: PageContext?, dc: DatasourceConnection, ignoreRequestExclusive: Boolean) {
        if (!(dc as DatasourceConnectionPro).isManaged() && isAutoCommit && (ignoreRequestExclusive || !(dc.getDatasource() as DataSourcePro).isRequestExclusive())) {
            if (pc != null && (pc as PageContextImpl).getTimeoutStackTrace() != null) {
                IOUtil.closeEL(dc)
            } else {
                (dc as DatasourceConnectionPro).release()
            }
        }
    }

    @Override
    fun begin() {
        isAutoCommit = false
        isolation = Connection.TRANSACTION_NONE
    }

    @Override
    fun begin(isolation: String) {
        isAutoCommit = false
        if (isolation.equalsIgnoreCase("read_uncommitted")) this.isolation = Connection.TRANSACTION_READ_UNCOMMITTED else if (isolation.equalsIgnoreCase("read_committed")) this.isolation = Connection.TRANSACTION_READ_COMMITTED else if (isolation.equalsIgnoreCase("repeatable_read")) this.isolation = Connection.TRANSACTION_REPEATABLE_READ else if (isolation.equalsIgnoreCase("serializable")) this.isolation = Connection.TRANSACTION_SERIALIZABLE else this.isolation = Connection.TRANSACTION_NONE
    }

    @Override
    fun begin(isolation: Int) {
        isAutoCommit = false
        this.isolation = isolation
    }

    @Override
    @Throws(DatabaseException::class)
    fun rollback() {
        rollback(null)
    }

    // FUTURE
    @Throws(DatabaseException::class)
    fun rollback(savePointName: String?) {
        if (isAutoCommit || _size() == 0) return
        var dc: DatasourceConnection? = null
        var pair: Pair<DatasourceConnection, Exception>? = null
        var hasSavePointMatch = false

        // ORM
        run {
            val it: Iterator<ORMDatasourceConnection> = transConnsORM.values().iterator()
            while (it.hasNext()) {
                dc = it.next()
                try {
                    if (savePointName == null) dc.getConnection().rollback() else {
                        val sp: Savepoint? = savepoints[toKey(dc.getDatasource(), savePointName)]
                        if (sp != null) {
                            dc.getConnection().rollback(sp)
                            hasSavePointMatch = true
                        }
                    }
                } catch (e: Exception) {
                    // we only keep the first exception
                    if (pair == null) {
                        pair = Pair<DatasourceConnection, Exception>(dc, e)
                    }
                }
            }
        }
        // Reg
        run {
            val it: Iterator<DatasourceConnectionPro> = transConnsReg!!.values().iterator()
            while (it.hasNext()) {
                dc = it.next()
                try {
                    if (savePointName == null) dc.getConnection().rollback() else {
                        val sp: Savepoint? = savepoints[toKey(dc.getDatasource(), savePointName)]
                        if (sp != null) {
                            dc.getConnection().rollback(sp)
                            hasSavePointMatch = true
                        }
                    }
                } catch (e: Exception) {
                    // we only keep the first exception
                    if (pair == null) {
                        pair = Pair<DatasourceConnection, Exception>(dc, e)
                    }
                }
            }
        }
        throwException(pair)
        if (savePointName != null && !hasSavePointMatch) throw DatabaseException("There are no savepoint with name [$savePointName] set", null, null, null)
    }

    @Override
    @Throws(DatabaseException::class)
    fun savepoint() {
        savepoint(null)
    }

    // FUTURE
    @Throws(DatabaseException::class)
    fun savepoint(savePointName: String?) {
        if (isAutoCommit || _size() == 0) return
        var dc: DatasourceConnection
        var pair: Pair<DatasourceConnection, Exception>? = null
        // ORM
        run {
            val it: Iterator<ORMDatasourceConnection> = transConnsORM.values().iterator()
            while (it.hasNext()) {
                dc = it.next()
                try {
                    if (savePointName == null) dc.getConnection().setSavepoint() else dc.getConnection().setSavepoint(savePointName)
                } catch (e: Exception) {
                    // we only keep the first exception
                    if (pair == null) {
                        pair = Pair<DatasourceConnection, Exception>(dc, e)
                    }
                }
            }
        }
        // Reg
        run {
            val it: Iterator<DatasourceConnectionPro> = transConnsReg!!.values().iterator()
            while (it.hasNext()) {
                dc = it.next()
                try {
                    if (savePointName == null) dc.getConnection().setSavepoint() else savepoints.put(toKey(dc.getDatasource(), savePointName), dc.getConnection().setSavepoint(savePointName))
                } catch (e: Exception) {
                    // we only keep the first exception
                    if (pair == null) {
                        pair = Pair<DatasourceConnection, Exception>(dc, e)
                    }
                }
            }
        }
        throwException(pair)
    }

    private fun toKey(ds: DataSource, savePointName: String): String {
        return HashUtil.create64BitHashAsString(savePointName + ":" + ds.id())
    }

    @Override
    @Throws(DatabaseException::class)
    fun commit() {
        if (isAutoCommit || _size() == 0) return
        var pair: Pair<DatasourceConnection, Exception>? = null
        var dc: DatasourceConnection
        // ORM
        run {
            val it: Iterator<ORMDatasourceConnection> = transConnsORM.values().iterator()
            while (it.hasNext()) {
                dc = it.next()
                try {
                    dc.getConnection().commit()
                } catch (e: Exception) {
                    // we only keep the first exception
                    if (pair == null) {
                        pair = Pair<DatasourceConnection, Exception>(dc, e)
                    }
                }
            }
        }
        // Reg
        run {
            val it: Iterator<DatasourceConnectionPro> = transConnsReg!!.values().iterator()
            while (it.hasNext()) {
                dc = it.next()
                try {
                    dc.getConnection().commit()
                } catch (e: Exception) {
                    // we only keep the first exception
                    if (pair == null) {
                        pair = Pair<DatasourceConnection, Exception>(dc, e)
                    }
                }
            }
        }
        throwException(pair)
    }

    @Override
    fun remove(datasource: DataSource?) {
        config.removeDatasourceConnectionPool(datasource)
    }

    @Override
    fun remove(datasource: String?) {
        throw PageRuntimeException(DeprecatedException("method no longer supported!"))
        // config.getDatasourceConnectionPool().remove(datasource);
    }

    @Override
    fun end() { // FUTURE add DatabaseException
        end(false)
    }

    fun end(onlyORM: Boolean) {
        isAutoCommit = true
        var pair: Pair<DatasourceConnection?, Exception?>? = null
        savepoints.clear()

        // ORM
        if (transConnsORM.size() > 0) {
            val tmp: Map<DataSource, DatasourceConnection>? = null
            val it: Iterator<Entry<DataSource, ORMDatasourceConnection>> = transConnsORM.entrySet().iterator()
            var dc: DatasourceConnection
            var entry: Entry<DataSource, ORMDatasourceConnection>
            while (it.hasNext()) {
                entry = it.next()
                dc = entry.getValue()
                try {
                    dc.setAutoCommit(true)
                    DBUtil.setTransactionIsolationEL(dc.getConnection(), (dc as DatasourceConnectionPro).getDefaultTransactionIsolation())
                } catch (e: Exception) {
                    // we only keep the first exception
                    if (pair == null) {
                        pair = Pair<DatasourceConnection, Exception>(dc, e)
                    }
                    continue
                }
            }
            transConnsORM.clear()
        }

        // Reg
        if (transConnsReg!!.size() > 0) {
            var tmp: Map<DataSource, DatasourceConnectionPro>? = null
            if (onlyORM) tmp = HashMap<DataSource, DatasourceConnectionPro>()
            val it: Iterator<Entry<DataSource, DatasourceConnectionPro>> = transConnsReg.entrySet().iterator()
            var dc: DatasourceConnectionPro
            var entry: Entry<DataSource, DatasourceConnectionPro>
            while (it.hasNext()) {
                entry = it.next()
                dc = entry.getValue()
                try {
                    if (onlyORM && dc.getConnection() !is ORMConnection) {
                        tmp.put(entry.getKey(), entry.getValue())
                        continue
                    }
                    if (dc.isManaged()) {
                        dc.setManaged(false)
                        dc.setAutoCommit(true)
                        DBUtil.setTransactionIsolationEL(dc.getConnection(), dc.getDefaultTransactionIsolation())
                        releaseConnection(null, dc, true)
                    }
                } catch (e: Exception) {
                    // we only keep the first exception
                    if (pair == null) {
                        pair = Pair<DatasourceConnection, Exception>(dc, e)
                    }
                    continue
                }
            }
            transConnsReg.clear()
            if (onlyORM) transConnsReg = tmp
        }
        isolation = Connection.TRANSACTION_NONE
        if (pair != null) {
            if (pair.getValue() is SQLException) {
                throw PageRuntimeException(DatabaseException(pair.getValue() as SQLException, pair.getName()))
            }
            throw PageRuntimeException(pair.getValue())
        }
    }

    @Override
    fun release() {
        end(false)
    }

    fun releaseORM() {
        end(true)
    }

    @Throws(DatabaseException::class)
    private fun throwException(pair: Pair<DatasourceConnection, Exception>?) {
        if (pair != null) {
            if (pair.getValue() is SQLException) {
                throw DatabaseException(pair.getValue() as SQLException, pair.getName())
            }
            throw PageRuntimeException(pair.getValue())
        }
    }

    private fun _size(): Int {
        return transConnsORM.size() + transConnsReg!!.size()
    }

    companion object {
        const val QOQ_DATASOURCE_NAME = "_queryofquerydb"
        private val tokens: ConcurrentHashMap<String, String> = ConcurrentHashMap<String, String>()
        fun getToken(key: String?): String? {
            var lock: String? = tokens.putIfAbsent(key, key)
            if (lock == null) {
                lock = key
            }
            return lock
        }
    }

    init {
        config = c
    }
}