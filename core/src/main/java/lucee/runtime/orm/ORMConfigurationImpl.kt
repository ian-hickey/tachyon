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
package lucee.runtime.orm

import java.io.IOException

class ORMConfigurationImpl private constructor() : ORMConfiguration {
    private var autogenmap = true
    private var cfcLocations: Array<Resource?>?
    private var eventHandling: Boolean? = null
    private var flushAtRequestEnd = true
    private var logSQL = false
    private var saveMapping = false
    private var secondaryCacheEnabled = false
    private var useDBForMapping = true
    private var cacheConfig: Resource? = null

    @get:Override
    var cacheProvider: String? = null
        private set
    private var ormConfig: Resource? = null
    private var eventHandler: String? = null
    private var namingStrategy: String? = null

    @get:Override
    var isDefaultCfcLocation = true
        private set
    private var skipCFCWithError = true
    private var autoManageSession = true
    private var ac: ApplicationContext? = null
    private var dbCreateMap: Map<String?, String?>? = null
    private var dbCreateDefault: String? = ""
    private var dialectMap: Map<String?, String?>? = null

    @get:Override
    var dialect: String? = ""
        private set
    private var schemaMap: Map<String?, String?>? = null

    @get:Override
    var schema: String? = ""
        private set
    private var catalogMap: Map<String?, String?>? = null

    @get:Override
    var catalog: String? = ""
        private set
    private var sqlScriptMap: Map<String?, String?>? = null
    private var sqlScriptDefault: String? = ""
    private var config: Config? = null
    private fun duplicate(): ORMConfigurationImpl? {
        val other = ORMConfigurationImpl()
        other.autogenmap = autogenmap
        other.cfcLocations = cfcLocations
        other.isDefaultCfcLocation = isDefaultCfcLocation
        other.dbCreateMap = dbCreateMap
        other.eventHandler = eventHandler
        other.namingStrategy = namingStrategy
        other.eventHandling = eventHandling
        other.flushAtRequestEnd = flushAtRequestEnd
        other.logSQL = logSQL
        other.saveMapping = saveMapping
        other.secondaryCacheEnabled = secondaryCacheEnabled
        other.useDBForMapping = useDBForMapping
        other.cacheConfig = cacheConfig
        other.cacheProvider = cacheProvider
        other.ormConfig = ormConfig
        other.autoManageSession = autoManageSession
        other.skipCFCWithError = skipCFCWithError
        other.dbCreateDefault = dbCreateDefault
        other.dbCreateMap = dbCreateMap
        other.dialect = dialect
        other.dialectMap = dialectMap
        other.schema = schema
        other.schemaMap = schemaMap
        other.catalog = catalog
        other.catalogMap = catalogMap
        other.sqlScriptDefault = sqlScriptDefault
        other.sqlScriptMap = sqlScriptMap
        return other
    }

    @Override
    fun hash(): String? { // no longer used in Hibernate 3.5.5.72 and above
        var _ac: ApplicationContext? = ac
        if (_ac == null) _ac = ThreadLocalPageContext.get().getApplicationContext()
        val ds: Object = _ac.getORMDataSource()
        val ormConf: ORMConfiguration = _ac.getORMConfiguration()
        val data: StringBuilder = StringBuilder().append(ormConf.autogenmap()).append(':').append(ormConf.getCatalog()).append(':').append(ormConf.isDefaultCfcLocation())
                .append(':').append(ormConf.eventHandling()).append(':').append(ormConf.namingStrategy()).append(':').append(ormConf.eventHandler()).append(':')
                .append(ormConf.flushAtRequestEnd()).append(':').append(ormConf.logSQL()).append(':').append(ormConf.autoManageSession()).append(':')
                .append(ormConf.skipCFCWithError()).append(':').append(ormConf.saveMapping()).append(':').append(ormConf.getSchema()).append(':')
                .append(ormConf.secondaryCacheEnabled()).append(':').append(ormConf.useDBForMapping()).append(':').append(ormConf.getCacheProvider()).append(':').append(ds)
                .append(':')
        append(data, ormConf.getCfcLocations())
        append(data, ormConf.getSqlScript())
        append(data, ormConf.getCacheConfig())
        append(data, ormConf.getOrmConfig())
        append(data, dbCreateDefault, dbCreateMap)
        append(data, catalog, catalogMap)
        append(data, dialect, dialectMap)
        append(data, schema, schemaMap)
        append(data, sqlScriptDefault, sqlScriptMap)
        return CFMLEngineFactory.getInstance().getSystemUtil().hash64b(data.toString())
    }

    private fun append(data: StringBuilder?, reses: Array<Resource?>?) {
        if (reses == null) return
        for (i in reses.indices) {
            append(data, reses[i])
        }
    }

    private fun append(data: StringBuilder?, res: Resource?) {
        if (res == null) return
        if (res.isFile()) {
            val eng: CFMLEngine = CFMLEngineFactory.getInstance()
            try {
                data.append(eng.getSystemUtil().hash64b(eng.getIOUtil().toString(res, null)))
                return
            } catch (e: IOException) {
            }
        }
        data.append(res.getAbsolutePath()).append(':')
    }

    /**
     * @return the autogenmap
     */
    @Override
    fun autogenmap(): Boolean {
        return autogenmap
    }

    /**
     * @return the cfcLocation
     */
    @Override
    fun getCfcLocations(): Array<Resource?>? {
        return cfcLocations
    }

    @get:Override
    val dbCreate: Int
        get() = dbCreateAsInt(dbCreateDefault)

    fun getDbCreate(datasourceName: String?): Int { // FUTURE add to interface
        return dbCreateAsInt(_get(datasourceName, dbCreateDefault, dbCreateMap))
    }

    fun getDialect(datasourceName: String?): String? { // FUTURE add to interface
        return _get(datasourceName, dialect, dialectMap)
    }

    fun getSchema(datasourceName: String?): String? { // FUTURE add to interface
        return _get(datasourceName, schema, schemaMap)
    }

    fun getCatalog(datasourceName: String?): String? { // FUTURE add to interface
        return _get(datasourceName, catalog, catalogMap)
    }

    @get:Override
    val sqlScript: Resource?
        get() = if (StringUtil.isEmpty(sqlScriptDefault)) null else toResEL(config, sqlScriptDefault, true)

    fun getSqlScript(datasourceName: String?): Resource? { // FUTURE add to interface
        val res = _get(datasourceName, sqlScriptDefault, sqlScriptMap)
        return if (StringUtil.isEmpty(res)) null else toResEL(config, res, true)
    }

    @Override
    fun eventHandling(): Boolean {
        return if (eventHandling == null) false else eventHandling.booleanValue()
    }

    @Override
    fun eventHandler(): String? {
        return eventHandler
    }

    @Override
    fun namingStrategy(): String? {
        return namingStrategy
    }

    @Override
    fun flushAtRequestEnd(): Boolean {
        return flushAtRequestEnd
    }

    @Override
    fun logSQL(): Boolean {
        return logSQL
    }

    @Override
    fun saveMapping(): Boolean {
        return saveMapping
    }

    @Override
    fun secondaryCacheEnabled(): Boolean {
        return secondaryCacheEnabled
    }

    @Override
    fun useDBForMapping(): Boolean {
        return useDBForMapping
    }

    @Override
    fun getCacheConfig(): Resource? {
        return cacheConfig
    }

    @Override
    fun getOrmConfig(): Resource? {
        return ormConfig
    }

    @Override
    fun skipCFCWithError(): Boolean {
        return skipCFCWithError
    }

    @Override
    fun autoManageSession(): Boolean {
        return autoManageSession
    }

    @Override
    fun toStruct(): Object? {
        val locs: Array<Resource?>? = getCfcLocations()
        val arrLocs: Array = ArrayImpl()
        if (locs != null) for (i in locs.indices) {
            arrLocs.appendEL(getAbsolutePath(locs[i]))
        }
        val sct: Struct = StructImpl()
        sct.setEL(AUTO_GEN_MAP, autogenmap())
        sct.setEL(CFC_LOCATION, arrLocs)
        sct.setEL(IS_DEFAULT_CFC_LOCATION, isDefaultCfcLocation)
        sct.setEL(EVENT_HANDLING, eventHandling())
        sct.setEL(EVENT_HANDLER, eventHandler())
        sct.setEL(NAMING_STRATEGY, namingStrategy())
        sct.setEL(FLUSH_AT_REQUEST_END, flushAtRequestEnd())
        sct.setEL(LOG_SQL, logSQL())
        sct.setEL(SAVE_MAPPING, saveMapping())
        sct.setEL(SECONDARY_CACHE_ENABLED, secondaryCacheEnabled())
        sct.setEL(USE_DB_FOR_MAPPING, useDBForMapping())
        sct.setEL(CACHE_CONFIG, getAbsolutePath(getCacheConfig()))
        sct.setEL(CACHE_PROVIDER, StringUtil.emptyIfNull(cacheProvider))
        sct.setEL(ORM_CONFIG, getAbsolutePath(getOrmConfig()))
        sct.setEL(CATALOG, externalize(catalogMap, catalog))
        sct.setEL(SCHEMA, externalize(schemaMap, schema))
        sct.setEL(DB_CREATE, externalize(dbCreateMap, dbCreateDefault))
        sct.setEL(DIALECT, externalize(dialectMap, dialect))
        sct.setEL(SQL_SCRIPT, externalize(sqlScriptMap, sqlScriptDefault))
        return sct
    }

    companion object {
        const val DBCREATE_NONE = 0
        const val DBCREATE_UPDATE = 1
        const val DBCREATE_DROP_CREATE = 2
        val AUTO_GEN_MAP: Key? = KeyImpl.getInstance("autogenmap")
        val CATALOG: Key? = KeyConstants._catalog
        val IS_DEFAULT_CFC_LOCATION: Key? = KeyImpl.getInstance("isDefaultCfclocation")
        val DB_CREATE: Key? = KeyImpl.getInstance("dbCreate")
        val DIALECT: Key? = KeyConstants._dialect
        val FLUSH_AT_REQUEST_END: Key? = KeyImpl.getInstance("flushAtRequestEnd")
        val LOG_SQL: Key? = KeyImpl.getInstance("logSql")
        val SAVE_MAPPING: Key? = KeyImpl.getInstance("savemapping")
        val SCHEMA: Key? = KeyConstants._schema
        val SECONDARY_CACHE_ENABLED: Key? = KeyImpl.getInstance("secondarycacheenabled")
        val SQL_SCRIPT: Key? = KeyImpl.getInstance("sqlscript")
        val USE_DB_FOR_MAPPING: Key? = KeyImpl.getInstance("useDBForMapping")
        val CACHE_CONFIG: Key? = KeyImpl.getInstance("cacheconfig")
        val CACHE_PROVIDER: Key? = KeyImpl.getInstance("cacheProvider")
        val ORM_CONFIG: Key? = KeyImpl.getInstance("ormConfig")
        val EVENT_HANDLING: Key? = KeyImpl.getInstance("eventHandling")
        val EVENT_HANDLER: Key? = KeyImpl.getInstance("eventHandler")
        val AUTO_MANAGE_SESSION: Key? = KeyImpl.getInstance("autoManageSession")
        val NAMING_STRATEGY: Key? = KeyImpl.getInstance("namingstrategy")
        val CFC_LOCATION: Key? = KeyConstants._cfcLocation
        fun load(config: Config?, ac: ApplicationContext?, el: Element?, defaultCFCLocation: Resource?, defaultConfig: ORMConfiguration?): ORMConfiguration? {
            return _load(config, ac, _GetElement(el), defaultCFCLocation, defaultConfig)
        }

        fun load(config: Config?, ac: ApplicationContext?, settings: Struct?, defaultCFCLocation: Resource?, defaultConfig: ORMConfiguration?): ORMConfiguration? {
            return _load(config, ac, _GetStruct(settings), defaultCFCLocation, defaultConfig)
        }

        private fun _load(config: Config?, ac: ApplicationContext?, settings: _Get?, defaultCFCLocation: Resource?, _dc: ORMConfiguration?): ORMConfiguration? {
            var dc = _dc as ORMConfigurationImpl?
            if (dc == null) dc = ORMConfigurationImpl()
            val c = dc.duplicate()
            c!!.config = config
            c.cfcLocations = if (defaultCFCLocation == null) arrayOfNulls<Resource?>(0) else arrayOf<Resource?>(defaultCFCLocation)

            // autogenmap
            c.autogenmap = Caster.toBooleanValue(settings!![AUTO_GEN_MAP, dc.autogenmap()], dc.autogenmap())

            // cfclocation
            var obj: Object? = settings[KeyConstants._cfcLocation, null]
            if (obj != null) {
                val list: List<Resource?> = AppListenerUtil.loadResources(config, ac, obj, true)
                if (list != null && list.size() > 0) {
                    c.cfcLocations = list.toArray(arrayOfNulls<Resource?>(list.size()))
                    c.isDefaultCfcLocation = false
                }
            }
            if (c.cfcLocations == null) c.cfcLocations = if (defaultCFCLocation == null) arrayOfNulls<Resource?>(0) else arrayOf<Resource?>(defaultCFCLocation)

            // catalog
            obj = settings[CATALOG, null]
            if (!StringUtil.isEmpty(obj)) {
                val coll = _load(obj)
                c.catalog = StringUtil.emptyIfNull(coll!!.def)
                c.catalogMap = coll.map
            } else {
                c.catalog = StringUtil.emptyIfNull(dc.catalog)
                c.catalogMap = dc.catalogMap
            }

            // dbcreate
            obj = settings[DB_CREATE, null]
            if (!StringUtil.isEmpty(obj)) {
                val coll = _load(obj)
                c.dbCreateDefault = StringUtil.emptyIfNull(coll!!.def)
                c.dbCreateMap = coll.map
            } else {
                c.dbCreateDefault = StringUtil.emptyIfNull(dc.dbCreateDefault)
                c.dbCreateMap = dc.dbCreateMap
            }

            // dialect
            obj = settings[DIALECT, null]
            if (!StringUtil.isEmpty(obj)) {
                val coll = _load(obj)
                c.dialect = StringUtil.emptyIfNull(coll!!.def)
                c.dialectMap = coll.map
            } else {
                c.dialect = StringUtil.emptyIfNull(dc.dialect)
                c.dialectMap = dc.dialectMap
            }

            // sqlscript
            obj = settings[SQL_SCRIPT, null]
            if (!StringUtil.isEmpty(obj)) {
                val coll = _load(obj)
                c.sqlScriptDefault = StringUtil.emptyIfNull(coll!!.def)
                c.sqlScriptMap = coll.map
            } else {
                c.sqlScriptDefault = StringUtil.emptyIfNull(dc.sqlScriptDefault)
                c.sqlScriptMap = dc.sqlScriptMap
            }

            // namingstrategy
            c.namingStrategy = Caster.toString(settings[NAMING_STRATEGY, dc.namingStrategy()], dc.namingStrategy())

            // eventHandler
            c.eventHandler = Caster.toString(settings[EVENT_HANDLER, dc.eventHandler()], dc.eventHandler())

            // eventHandling
            var b: Boolean? = Caster.toBoolean(settings[EVENT_HANDLING, null], null)
            if (b == null) {
                if (dc.eventHandling()) b = Boolean.TRUE else b = !StringUtil.isEmpty(c.eventHandler, true)
            }
            c.eventHandling = b

            // flushatrequestend
            c.flushAtRequestEnd = Caster.toBooleanValue(settings[FLUSH_AT_REQUEST_END, dc.flushAtRequestEnd()], dc.flushAtRequestEnd())

            // logSQL
            c.logSQL = Caster.toBooleanValue(settings[LOG_SQL, dc.logSQL()], dc.logSQL())

            // autoManageSession
            c.autoManageSession = Caster.toBooleanValue(settings[AUTO_MANAGE_SESSION, dc.autoManageSession()], dc.autoManageSession())

            // skipCFCWithError
            c.skipCFCWithError = Caster.toBooleanValue(settings[KeyConstants._skipCFCWithError, dc.skipCFCWithError()], dc.skipCFCWithError())

            // savemapping
            c.saveMapping = Caster.toBooleanValue(settings[SAVE_MAPPING, dc.saveMapping()], dc.saveMapping())

            // schema
            // c.schema = StringUtil.trim(Caster.toString(settings.get(SCHEMA, dc.getSchema()), dc.getSchema()),
            // dc.getSchema());
            obj = settings[SCHEMA, null]
            if (obj != null) {
                val coll = _load(obj)
                c.schema = StringUtil.emptyIfNull(coll!!.def)
                c.schemaMap = coll.map
            } else {
                c.schema = StringUtil.emptyIfNull(dc.schema)
                c.schemaMap = dc.schemaMap
            }

            // secondarycacheenabled
            c.secondaryCacheEnabled = Caster.toBooleanValue(settings[SECONDARY_CACHE_ENABLED, dc.secondaryCacheEnabled()], dc.secondaryCacheEnabled())

            // useDBForMapping
            c.useDBForMapping = Caster.toBooleanValue(settings[USE_DB_FOR_MAPPING, dc.useDBForMapping()], dc.useDBForMapping())

            // cacheconfig
            obj = settings[CACHE_CONFIG, null]
            if (!StringUtil.isEmpty(obj)) {
                try {
                    c.cacheConfig = toRes(config, obj, true)
                } catch (e: ExpressionException) {
                    // print.printST(e);
                }
            }

            // cacheprovider
            c.cacheProvider = StringUtil.trim(Caster.toString(settings[CACHE_PROVIDER, dc.cacheProvider], dc.cacheProvider), dc.cacheProvider)

            // ormconfig
            obj = settings[ORM_CONFIG, null]
            if (!StringUtil.isEmpty(obj)) {
                try {
                    c.ormConfig = toRes(config, obj, true)
                } catch (e: ExpressionException) {
                    // print.printST(e);
                }
            }
            c.ac = ac
            return c
        }

        private fun _load(obj: Object?): Coll? {
            val coll = Coll()
            if (obj != null) {
                // multi
                if (Decision.isStruct(obj)) {
                    val sct: Struct = Caster.toStruct(obj, null)
                    if (sct != null) {
                        val it: Iterator<Entry<Key?, Object?>?> = sct.entryIterator()
                        coll.map = HashMap<String?, String?>()
                        var e: Entry<Key?, Object?>?
                        var k: String
                        var v: String
                        while (it.hasNext()) {
                            e = it.next()
                            k = e.getKey().getLowerString().trim()
                            v = Caster.toString(e.getValue(), "").trim()
                            if ("__default__".equals(k) || "".equals(k)) coll.def = v else coll.map.put(k, v)
                        }
                    }
                } else {
                    coll.def = Caster.toString(obj, "").trim()
                }
            }
            return coll
        }

        @Throws(ExpressionException::class)
        private fun toRes(config: Config?, obj: Object?, existing: Boolean): Resource? {
            val pc: PageContext = ThreadLocalPageContext.get()
            return if (pc != null) Caster.toResource(pc, obj, existing) else Caster.toResource(config, obj, existing)
        }

        private fun toResEL(config: Config?, obj: Object?, existing: Boolean): Resource? {
            val pc: PageContext = ThreadLocalPageContext.get()
            return try {
                if (pc != null) Caster.toResource(pc, obj, existing) else Caster.toResource(config, obj, existing)
            } catch (pe: PageException) {
                null
            }
        }

        private fun append(data: StringBuilder?, def: String?, map: Map<String?, String?>?) {
            data.append(':').append(def)
            if (map != null) {
                val it: Iterator<Entry<String?, String?>?> = map.entrySet().iterator()
                var e: Entry<String?, String?>?
                while (it.hasNext()) {
                    e = it.next()
                    data.append(':').append(e.getKey()).append(':').append(e.getValue())
                }
            }
        }

        private fun _get(datasourceName: String?, def: String?, map: Map<String?, String?>?): String? {
            var datasourceName = datasourceName
            if (map != null && !StringUtil.isEmpty(datasourceName)) {
                datasourceName = datasourceName.toLowerCase().trim()
                val res = map[datasourceName]
                if (!StringUtil.isEmpty(res)) return res
            }
            return def
        }

        private fun getAbsolutePath(res: Resource?): String? {
            return if (res == null) "" else res.getAbsolutePath()
        }

        fun dbCreateAsInt(dbCreate: String?): Int {
            var dbCreate = dbCreate
            dbCreate = dbCreate?.trim()?.toLowerCase() ?: ""
            if ("update".equals(dbCreate)) return DBCREATE_UPDATE
            if ("dropcreate".equals(dbCreate)) return DBCREATE_DROP_CREATE
            return if ("drop-create".equals(dbCreate)) DBCREATE_DROP_CREATE else DBCREATE_NONE
        }

        fun dbCreateAsString(dbCreate: Int): String? {
            when (dbCreate) {
                DBCREATE_DROP_CREATE -> return "dropcreate"
                DBCREATE_UPDATE -> return "update"
            }
            return "none"
        }

        private fun externalize(map: Map<String?, String?>?, def: String?): Object? {
            if (map == null || map.isEmpty()) return StringUtil.emptyIfNull(def)
            val it: Iterator<Entry<String?, String?>?> = map.entrySet().iterator()
            var e: Entry<String?, String?>?
            val sct: Struct = StructImpl()
            while (it.hasNext()) {
                e = it.next()
                if (!StringUtil.isEmpty(e.getValue())) sct.setEL(e.getKey(), e.getValue())
            }
            return sct
        }
    }

    init {
        autogenmap = true
        flushAtRequestEnd = true
        useDBForMapping = true
    }
}

internal interface _Get {
    operator fun get(name: Collection.Key?, defaultValue: Object?): Object?
}

internal class _GetStruct(sct: Struct?) : _Get {
    private val sct: Struct?
    @Override
    override fun get(name: Collection.Key?, defaultValue: Object?): Object? {
        return sct.get(name, defaultValue)
    }

    @Override
    override fun toString(): String {
        return "_GetStruct:" + sct.toString()
    }

    init {
        this.sct = sct
    }
}

internal class _GetElement(el: Element?) : _Get {
    private val el: Element?
    @Override
    override fun get(name: Collection.Key?, defaultValue: Object?): Object? {
        var value = _get(name.getString())
        if (value == null) value = _get(StringUtil.camelToHypenNotation(name.getString()))
        if (value == null) value = _get(name.getLowerString())
        if (value == null) {
            val map: NamedNodeMap = el.getAttributes()
            val len: Int = map.getLength()
            var attr: Attr
            var n: String
            for (i in 0 until len) {
                attr = map.item(i) as Attr
                n = attr.getName()
                n = StringUtil.replace(n, "-", "", false).toLowerCase()
                if (n.equalsIgnoreCase(name.getLowerString())) return attr.getValue()
            }
        }
        return value ?: defaultValue
    }

    private fun _get(name: String?): String? {
        return if (el.hasAttribute(name)) el.getAttribute(name) else null
    }

    init {
        this.el = el
    }
}

internal class Coll {
    var map: Map<String?, String?>? = null
    var def: String? = null
}