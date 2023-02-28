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

import java.sql.SQLException

/**
 *
 */
class DataSourceImpl(config: Config?, name: String, cd: ClassDefinition, @get:Override val host: String, @get:Override val connectionString: String, @get:Override val database: String, @get:Override val port: Int, username: String?, password: String?,
                     listener: TagListener?, connectionLimit: Int, idleTimeout: Int, liveTimeout: Int, minIdle: Int, maxIdle: Int, maxTotal: Int, metaCacheTimeout: Long, blob: Boolean, clob: Boolean,
                     allow: Int, custom: Struct?, readOnly: Boolean, validate: Boolean, storage: Boolean, timezone: TimeZone?, dbdriver: String, paramSyntax: ParamSyntax?,
                     literalTimestampWithTSOffset: Boolean, alwaysSetTimeout: Boolean, requestExclusive: Boolean, alwaysResetConnections: Boolean, log: Log?) : DataSourceSupport(config, name, cd, username, ConfigWebUtil.decrypt(password), listener, blob, clob, connectionLimit, idleTimeout, liveTimeout, minIdle, maxIdle, maxTotal,
        metaCacheTimeout, timezone, if (allow < 0) ALLOW_ALL else allow, storage, readOnly, validate, requestExclusive, alwaysResetConnections, literalTimestampWithTSOffset,
        log), Cloneable {

    @get:Override
    var connectionStringTranslated: String
        private set
    private val custom: Struct?
    val dbDriver: String
    private val paramSyntax: ParamSyntax

    // FUTURE add to interface
    val alwaysSetTimeout: Boolean
    private fun translateConnStr() {
        connectionStringTranslated = replace(connectionStringTranslated, "host", host, false, false)
        connectionStringTranslated = replace(connectionStringTranslated, "database", database, false, false)
        connectionStringTranslated = replace(connectionStringTranslated, "port", Caster.toString(port), false, false)
        connectionStringTranslated = replace(connectionStringTranslated, "username", getUsername(), false, false)
        connectionStringTranslated = replace(connectionStringTranslated, "password", getPassword(), false, false)

        // Collection.Key[] keys = custom==null?new Collection.Key[0]:custom.keys();
        if (custom != null) {
            val it: Iterator<Entry<Key, Object>> = custom.entryIterator()
            var e: Entry<Key, Object>
            var leading = true
            while (it.hasNext()) {
                e = it.next()
                connectionStringTranslated = replace(connectionStringTranslated, e.getKey().getString(), Caster.toString(e.getValue(), ""), true, leading)
                leading = false
            }
        }
    }

    private fun replace(src: String, name: String, value: String, doQueryString: Boolean, leading: Boolean): String {
        var src = src
        if (StringUtil.indexOfIgnoreCase(src, "{$name}") !== -1) {
            return StringUtil.replace(connectionStringTranslated, "{$name}", value, false)
        }
        if (!doQueryString) return src

        // FUTURE remove; this is for backward compatibility to old MSSQL driver
        return if (ParamSyntax.DEFAULT.equals(paramSyntax) && getClassDefinition().getClassName().indexOf("microsoft") !== -1 || getClassDefinition().getClassName().indexOf("jtds") !== -1) ';' + name + '='.toInt() + value.let { src += it; src } else (if (leading) paramSyntax.leadingDelimiter else paramSyntax.delimiter) + name + paramSyntax.separator + value.let { src += it; src }
        // return src+=((src.indexOf('?')!=-1)?'&':'?')+name+'='+value;
    }

    @get:Override
    val dsnOriginal: String
        get() = connectionString

    @get:Override
    val dsnTranslated: String
        get() = connectionStringTranslated

    // FUTURE add to interface
    fun getParamSyntax(): ParamSyntax {
        return paramSyntax
    }

    @Override
    override fun clone(): Object {
        return _clone(isReadOnly())
    }

    @Override
    fun cloneReadOnly(): DataSource {
        return _clone(true)
    }

    fun _clone(readOnly: Boolean): DataSource {
        return try {
            DataSourceImpl(ThreadLocalPageContext.getConfig(), getName(), getClassDefinition(), host, connectionString, database, port, getUsername(), getPassword(),
                    getListener(), getConnectionLimit(), getIdleTimeout(), getLiveTimeout(), getMinIdle(), getMaxIdle(), getMaxTotal(), getMetaCacheTimeout(), isBlob(), isClob(),
                    allow, custom, readOnly, validate(), isStorage(), getTimeZone(), dbDriver, getParamSyntax(), getLiteralTimestampWithTSOffset(), alwaysSetTimeout,
                    isRequestExclusive(), isAlwaysResetConnections(), getLog())
        } catch (re: RuntimeException) {
            throw re // this should never happens, because the class was already loaded in this object
        } catch (e: Exception) {
            throw RuntimeException(e) // this should never happens, because the class was already loaded in this object
        }
    }

    @Override
    fun getCustomValue(key: String?): String {
        return Caster.toString(custom.get(KeyImpl.init(key), null), "")
    }

    @get:Override
    val customNames: Array<String>
        get() = CollectionUtil.keysAsString(custom)

    @get:Override
    val customs: Struct
        get() = custom.clone() as Struct

    /**
     *
     * @param config
     * @param name
     * @param cd
     * @param host
     * @param connStr
     * @param database
     * @param port
     * @param username
     * @param password
     * @param listener
     * @param connectionLimit
     * @param idleTimeout
     * @param liveTimeout
     * @param metaCacheTimeout
     * @param blob
     * @param clob
     * @param allow
     * @param custom
     * @param readOnly
     * @param validate
     * @param storage
     * @param timezone
     * @param dbdriver
     * @param paramSyntax
     * @param literalTimestampWithTSOffset
     * @param alwaysSetTimeout
     * @param requestExclusive
     * @param alwaysResetConnections
     * @param log
     * @throws BundleException
     * @throws ClassException
     * @throws SQLException
     */
    init {
        this.custom = custom
        connectionStringTranslated = connectionString
        this.paramSyntax = if (paramSyntax == null) ParamSyntax.DEFAULT else paramSyntax
        this.alwaysSetTimeout = alwaysSetTimeout
        translateConnStr()
        dbDriver = dbdriver
    }
}