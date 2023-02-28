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
package lucee.runtime.db

import java.util.TimeZone

class ApplicationDataSource private constructor(config: Config, name: String, cd: ClassDefinition, @get:Override val connectionStringTranslated: String, username: String, password: String, listener: TagListener, blob: Boolean,
                                                clob: Boolean, connectionLimit: Int, idleTimeout: Int, liveTimeout: Int, minIdle: Int, maxIdle: Int, maxTotal: Int, metaCacheTimeout: Long, timezone: TimeZone, allow: Int,
                                                storage: Boolean, readOnly: Boolean, validate: Boolean, requestExclusive: Boolean, alwaysResetConnections: Boolean, literalTimestampWithTSOffset: Boolean, log: Log) : DataSourceSupport(config, name, cd, username, ConfigWebUtil.decrypt(password), listener, blob, clob, connectionLimit, idleTimeout, liveTimeout, minIdle, maxIdle, maxTotal,
        metaCacheTimeout, timezone, if (allow < 0) ALLOW_ALL else allow, storage, readOnly, validate, requestExclusive, alwaysResetConnections, literalTimestampWithTSOffset,
        log) {

    @get:Override
    val dsnOriginal: String
        get() {
            throw exp()
        }

    @get:Override
    val connectionString: String
        get() {
            throw exp()
        }

    @get:Override
    val dsnTranslated: String
        get() = connectionStringTranslated

    @get:Override
    val database: String
        get() {
            throw PageRuntimeException(ApplicationException("Datasource defined in the application event handler has no name."))
        }

    @get:Override
    val port: Int
        get() {
            throw exp()
        }

    @get:Override
    val host: String
        get() {
            throw exp()
        }

    @Override
    fun cloneReadOnly(): DataSource {
        return try {
            ApplicationDataSource(ThreadLocalPageContext.getConfig(), getName(), getClassDefinition(), connectionStringTranslated, getUsername(), getPassword(), getListener(), isBlob(),
                    isClob(), getConnectionLimit(), getIdleTimeout(), getLiveTimeout(), getMinIdle(), getMaxIdle(), getMaxTotal(), getMetaCacheTimeout(), getTimeZone(), allow,
                    isStorage(), isReadOnly(), validate(), isRequestExclusive(), isAlwaysResetConnections(), getLiteralTimestampWithTSOffset(), getLog())
        } catch (e: Exception) {
            throw RuntimeException(e) // this should never happens, because the class was already loaded in this object
        }
    }

    @Override
    fun getCustomValue(key: String?): String {
        throw exp()
    }

    @get:Override
    val customNames: Array<String>
        get() {
            throw exp()
        }

    @get:Override
    val customs: Struct
        get() {
            throw exp()
        }

    private fun exp(): PageRuntimeException {
        // return new MethodNotSupportedException();
        throw PageRuntimeException(ApplicationException("method not supported"))
    }

    companion object {
        fun getInstance(config: Config, name: String, cd: ClassDefinition, connStr: String, username: String, password: String, listener: TagListener, blob: Boolean,
                        clob: Boolean, connectionLimit: Int, idleTimeout: Int, liveTimeout: Int, minIdle: Int, maxIdle: Int, maxTotal: Int, metaCacheTimeout: Long, timezone: TimeZone, allow: Int,
                        storage: Boolean, readOnly: Boolean, validate: Boolean, requestExclusive: Boolean, alwaysResetConnections: Boolean, literalTimestampWithTSOffset: Boolean, log: Log): DataSource {
            return ApplicationDataSource(config, name, cd, connStr, username, password, listener, blob, clob, connectionLimit, idleTimeout, liveTimeout, minIdle, maxIdle, maxTotal,
                    metaCacheTimeout, timezone, allow, storage, readOnly, validate, requestExclusive, alwaysResetConnections, literalTimestampWithTSOffset, log)
        }
    }
}