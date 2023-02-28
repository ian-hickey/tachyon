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
package lucee.runtime.type.scope.storage.db

import java.io.Serializable

class Ansi92 : SQLExecutorSupport() {
    @Override
    @Throws(PageException::class)
    override fun select(config: Config?, cfid: String?, applicationName: String?, dc: DatasourceConnection?, type: Int, log: Log?, createTableIfNotExist: Boolean): Query? {
        val scopeName: String = VariableInterpreter.scopeInt2String(type)
        val tableName = PREFIX.toString() + "_" + scopeName + "_data"
        var query: Query? = null
        val sqlSelect: SQL = SQLImpl("SELECT data FROM $tableName WHERE cfid=? AND name=? AND expires > ?", arrayOf<SQLItem?>(SQLItemImpl(cfid, Types.VARCHAR), SQLItemImpl(applicationName, Types.VARCHAR), SQLItemImpl(now(config), Types.VARCHAR)))
        val pc: PageContext = ThreadLocalPageContext.get()
        try {
            query = QueryImpl(pc, dc, sqlSelect, -1, -1, null, scopeName + "_storage")
        } catch (de: DatabaseException) {
            if (dc == null || !createTableIfNotExist) throw de

            // create table for storage
            var sql: SQL?
            try {
                sql = createStorageTableSql(dc, scopeName, null)
                ScopeContext.info(log, sql.toString())
                // execute create table
                QueryImpl(pc, dc, sql, -1, -1, null, scopeName + "_storage")
            } catch (_de: DatabaseException) {
                // failed, try text
                try {
                    sql = createStorageTableSql(dc, scopeName, "text")
                    ScopeContext.info(log, sql.toString())
                    QueryImpl(pc, dc, sql, -1, -1, null, scopeName + "_storage")
                } catch (__de: DatabaseException) {
                    // failed, try "memo"
                    try {
                        sql = createStorageTableSql(dc, scopeName, "memo")
                        ScopeContext.info(log, sql.toString())
                        QueryImpl(pc, dc, sql, -1, -1, null, scopeName + "_storage")
                    } catch (___de: DatabaseException) {
                        // failed, try clob
                        try {
                            sql = createStorageTableSql(dc, scopeName, "clob")
                            ScopeContext.info(log, sql.toString())
                            QueryImpl(pc, dc, sql, -1, -1, null, scopeName + "_storage")
                        } catch (____de: DatabaseException) {
                            ___de.initCause(__de)
                            __de.initCause(_de)
                            _de.initCause(de)
                            // we could not create the table, so there seem to be an other exception we cannot solve
                            val exp = DatabaseException("Unable to select $scopeName information from database, and/or to create the table.", null, null,
                                    dc)
                            exp.initCause(de)
                            throw exp
                        }
                    }
                }
            }

            // database table created, now create index
            try {
                sql = SQLImpl("CREATE UNIQUE INDEX ix_$tableName ON $tableName(cfid, name, expires)")
                QueryImpl(pc, dc, sql, -1, -1, null, scopeName + "_storage")
            } catch (_de: DatabaseException) {
                throw DatabaseException("Failed to create unique index on $tableName", null, sql, dc)
            }
            query = QueryImpl(pc, dc, sqlSelect, -1, -1, null, scopeName + "_storage")
        }
        ScopeContext.debug(log, sqlSelect.toString())
        return query
    }

    @Override
    @Throws(PageException::class, SQLException::class)
    override fun update(config: Config?, cfid: String?, applicationName: String?, dc: DatasourceConnection?, type: Int, data: Object?, timeSpan: Long, log: Log?) {
        val strType: String = VariableInterpreter.scopeInt2String(type)
        val tz: TimeZone = ThreadLocalPageContext.getTimeZone()
        var recordsAffected = _update(config, dc.getConnection(), cfid, applicationName, "UPDATE " + PREFIX + "_" + strType + "_data SET expires=?, data=? WHERE cfid=? AND name=?",
                data, timeSpan, log, tz)
        if (recordsAffected > 1) {
            delete(config, cfid, applicationName, dc, type, log)
            recordsAffected = 0
        }
        if (recordsAffected == 0) {
            _update(config, dc.getConnection(), cfid, applicationName, "INSERT INTO " + PREFIX + "_" + strType + "_data (expires, data, cfid, name) VALUES(?, ?, ?, ?)", data,
                    timeSpan, log, tz)
        }
    }

    @Override
    @Throws(PageException::class, SQLException::class)
    override fun delete(config: Config?, cfid: String?, applicationName: String?, dc: DatasourceConnection?, type: Int, log: Log?) {
        val strType: String = VariableInterpreter.scopeInt2String(type)
        val strSQL = "DELETE FROM " + PREFIX + "_" + strType + "_data WHERE cfid=? AND name=?"
        val sql = SQLImpl(strSQL, arrayOf<SQLItem?>(SQLItemImpl(cfid, Types.VARCHAR), SQLItemImpl(applicationName, Types.VARCHAR)))
        execute(null, dc.getConnection(), sql, ThreadLocalPageContext.getTimeZone())
        ScopeContext.debug(log, sql.toString())
    }

    @Override
    @Throws(PageException::class)
    override fun clean(config: Config?, dc: DatasourceConnection?, type: Int, engine: StorageScopeEngine?, cleaner: DatasourceStorageScopeCleaner?, listener: StorageScopeListener?, log: Log?) {
        val strType: String = VariableInterpreter.scopeInt2String(type)
        // select
        val sqlSelect: SQL = SQLImpl("SELECT cfid, name FROM " + PREFIX + "_" + strType + "_data WHERE expires <= ?", arrayOf<SQLItem?>(SQLItemImpl(System.currentTimeMillis(), Types.VARCHAR)))
        val query: Query?
        try {
            query = QueryImpl(ThreadLocalPageContext.get(), dc, sqlSelect, -1, -1, null, strType + "_storage")
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
            // possible that the table not exist, if not there is nothing to clean
            return
        }
        val recordcount: Int = query.getRecordcount()
        var cfid: String
        var name: String
        for (row in 1..recordcount) {
            cfid = Caster.toString(query.getAt(KeyConstants._cfid, row, null), null)
            name = Caster.toString(query.getAt(KeyConstants._name, row, null), null)
            if (listener != null) listener.doEnd(engine, cleaner, name, cfid)
            ScopeContext.info(log, "remove " + strType + "/" + name + "/" + cfid + " from datasource " + dc.getDatasource().getName())
            engine.remove(type, name, cfid)
            val sql = SQLImpl("DELETE FROM " + PREFIX + "_" + strType + "_data WHERE cfid=? and name=?", arrayOf<SQLItem?>(SQLItemImpl(cfid, Types.VARCHAR), SQLItemImpl(name, Types.VARCHAR)))
            QueryImpl(ThreadLocalPageContext.get(), dc, sql, -1, -1, null, strType + "_storage")
        }
    }

    companion object {
        val PREFIX: String? = "cf"
        @Throws(SQLException::class, PageException::class)
        private fun _update(config: Config?, conn: Connection?, cfid: String?, applicationName: String?, strSQL: String?, data: Object?, timeSpan: Long, log: Log?, tz: TimeZone?): Int {
            val sql = SQLImpl(strSQL, arrayOf<SQLItem?>(SQLItemImpl(createExpires(config, timeSpan), Types.VARCHAR),
                    SQLItemImpl(serialize(data, ignoreSet), Types.VARCHAR), SQLItemImpl(cfid, Types.VARCHAR), SQLItemImpl(applicationName, Types.VARCHAR)))
            ScopeContext.debug(log, sql.toString())
            return execute(null, conn, sql, tz)
        }

        @Throws(PageException::class)
        private fun serialize(data: Object?, ignoreSet: Set<Key?>?): Object? {
            return try {
                if (data is Struct) {
                    "struct:" + ScriptConverter().serializeStruct(data as Struct?, ignoreSet)
                } else JavaConverter.serialize(data as Serializable?)
            } catch (e: Exception) {
                throw Caster.toPageException(e)
            }
        }

        @Throws(SQLException::class, PageException::class)
        private fun execute(pc: PageContext?, conn: Connection?, sql: SQLImpl?, tz: TimeZone?): Int {
            val preStat: PreparedStatement = conn.prepareStatement(sql.getSQLString())
            var count = 0
            count = try {
                val items: Array<SQLItem?> = sql.getItems()
                for (i in items.indices) {
                    SQLCaster.setValue(pc, tz, preStat, i + 1, items[i])
                }
                preStat.executeUpdate()
            } finally {
                preStat.close()
            }
            return count
        }

        private fun createStorageTableSql(dc: DatasourceConnection?, scopeName: String?, textSqlType: String?): SQL? {
            var textSqlType = textSqlType
            if (textSqlType == null) textSqlType = DataSourceUtil.getLargeTextSqlTypeName(dc)
            val sb = StringBuilder(256)
            sb.append("CREATE TABLE ")
            if (DataSourceUtil.isMSSQL(dc)) // TODO: why set schema for MSSQL but not other DBMSs?
                sb.append("dbo.")
            sb.append(PREFIX.toString() + "_" + scopeName + "_data (")
            sb.append("expires VARCHAR(64) NOT NULL, ") // TODO: why expires is VARCHAR and not BIGINT?
            sb.append("cfid VARCHAR(64) NOT NULL, ")
            sb.append("name VARCHAR(255) NOT NULL, ")
            sb.append("data ")
            sb.append("$textSqlType ")
            sb.append(" NOT NULL")
            sb.append(")")
            return SQLImpl(sb.toString())
        }
    }
}