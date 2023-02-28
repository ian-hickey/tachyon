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
package lucee.runtime.listener

import java.util.HashMap

/**
 * This is a base class for ModernApplicationSupport and ClassicApplicationSupport. It contains code
 * that is shared between the subclasses.
 */
abstract class ApplicationContextSupport(config: ConfigWeb?) : ApplicationContext {
    protected var idletimeout = 1800
    protected var cookiedomain: String? = null
    protected var applicationtoken: String? = null
    private var tagDefaultAttributeValues: Map<Collection.Key?, Map<Collection.Key?, Object?>?>? = null
    protected var cachedWithinFunction: Object?
    protected var cachedWithinInclude: Object?
    protected var cachedWithinQuery: Object?
    protected var cachedWithinResource: Object?
    protected var cachedWithinHTTP: Object?
    protected var cachedWithinFile: Object?
    protected var cachedWithinWS: Object?
    protected var config: ConfigWeb?
    protected fun _duplicate(other: ApplicationContextSupport?) {
        idletimeout = other!!.idletimeout
        cookiedomain = other.cookiedomain
        applicationtoken = other.applicationtoken
        if (other.tagDefaultAttributeValues != null) {
            tagDefaultAttributeValues = HashMap<Collection.Key?, Map<Collection.Key?, Object?>?>()
            val it: Iterator<Entry<Collection.Key?, Map<Collection.Key?, Object?>?>?> = other.tagDefaultAttributeValues.entrySet().iterator()
            var e: Entry<Collection.Key?, Map<Collection.Key?, Object?>?>?
            var iit: Iterator<Entry<Collection.Key?, Object?>?>
            var ee: Entry<Collection.Key?, Object?>?
            var map: Map<Collection.Key?, Object?>?
            while (it.hasNext()) {
                e = it.next()
                iit = e.getValue().entrySet().iterator()
                map = HashMap<Collection.Key?, Object?>()
                while (iit.hasNext()) {
                    ee = iit.next()
                    map.put(ee.getKey(), ee.getValue())
                }
                tagDefaultAttributeValues.put(e.getKey(), map)
            }
        }
        other.cachedWithinFile = Duplicator.duplicate(cachedWithinFile, true)
        other.cachedWithinFunction = Duplicator.duplicate(cachedWithinFunction, true)
        other.cachedWithinHTTP = Duplicator.duplicate(cachedWithinHTTP, true)
        other.cachedWithinInclude = Duplicator.duplicate(cachedWithinInclude, true)
        other.cachedWithinQuery = Duplicator.duplicate(cachedWithinQuery, true)
        other.cachedWithinResource = Duplicator.duplicate(cachedWithinResource, true)
        other.cachedWithinWS = Duplicator.duplicate(cachedWithinWS, true)
    }

    @Override
    fun setSecuritySettings(applicationtoken: String?, cookiedomain: String?, idletimeout: Int) {
        this.applicationtoken = applicationtoken
        this.cookiedomain = cookiedomain
        this.idletimeout = idletimeout
    }

    @Override
    fun getSecurityApplicationToken(): String? {
        return if (StringUtil.isEmpty(applicationtoken, true)) getName() else applicationtoken
    }

    @Override
    fun getSecurityCookieDomain(): String? {
        return if (StringUtil.isEmpty(applicationtoken, true)) null else cookiedomain
    }

    @Override
    fun getSecurityIdleTimeout(): Int {
        return if (idletimeout < 1) 1800 else idletimeout
    }

    @Override
    fun getDataSource(dataSourceName: String?, defaultValue: DataSource?): DataSource? {
        var dataSourceName: String? = dataSourceName ?: return defaultValue
        dataSourceName = dataSourceName.trim()
        val sources: Array<DataSource?> = getDataSources()
        if (!ArrayUtil.isEmpty(sources)) {
            for (i in sources.indices) {
                if (sources[i].getName().equalsIgnoreCase(dataSourceName)) return sources[i]
            }
        }
        return defaultValue
    }

    @Override
    @Throws(ApplicationException::class)
    fun getDataSource(dataSourceName: String?): DataSource? {
        return getDataSource(dataSourceName, null)
                ?: throw ApplicationException("there is no datasource with name [$dataSourceName]")
    }

    @Override
    fun getTagAttributeDefaultValues(pc: PageContext?): Map<Collection.Key?, Map<Collection.Key?, Object?>?>? {
        return tagDefaultAttributeValues
    }

    @Override
    fun getTagAttributeDefaultValues(pc: PageContext?, fullname: String?): Map<Collection.Key?, Object?>? {
        return if (tagDefaultAttributeValues == null) null else tagDefaultAttributeValues!![KeyImpl.init(fullname)]
    }

    @Override
    fun setTagAttributeDefaultValues(pc: PageContext?, sct: Struct?) {
        if (tagDefaultAttributeValues == null) tagDefaultAttributeValues = HashMap<Collection.Key?, Map<Collection.Key?, Object?>?>()
        initTagDefaultAttributeValues(config, tagDefaultAttributeValues, sct, pc.getCurrentTemplateDialect())
    }

    @Override
    fun setCachedWithin(type: Int, value: Object?) {
        if (StringUtil.isEmpty(value)) return
        when (type) {
            Config.CACHEDWITHIN_FUNCTION -> cachedWithinFunction = value
            Config.CACHEDWITHIN_INCLUDE -> cachedWithinInclude = value
            Config.CACHEDWITHIN_QUERY -> cachedWithinQuery = value
            Config.CACHEDWITHIN_RESOURCE -> cachedWithinResource = value
            Config.CACHEDWITHIN_HTTP -> cachedWithinHTTP = value
            Config.CACHEDWITHIN_FILE -> cachedWithinFile = value
            Config.CACHEDWITHIN_WEBSERVICE -> cachedWithinWS = value
        }
    }

    @Override
    fun getCachedWithin(type: Int): Object? {
        when (type) {
            Config.CACHEDWITHIN_FUNCTION -> return cachedWithinFunction
            Config.CACHEDWITHIN_INCLUDE -> return cachedWithinInclude
            Config.CACHEDWITHIN_QUERY -> return cachedWithinQuery
            Config.CACHEDWITHIN_RESOURCE -> return cachedWithinResource
            Config.CACHEDWITHIN_HTTP -> return cachedWithinHTTP
            Config.CACHEDWITHIN_FILE -> return cachedWithinFile
            Config.CACHEDWITHIN_WEBSERVICE -> return cachedWithinWS
        }
        return null
    }

    // FUTURE add to interface
    abstract fun getAntiSamyPolicyResource(): Resource?
    abstract fun setAntiSamyPolicyResource(res: Resource?)
    abstract fun getCacheConnection(cacheName: String?, defaultValue: CacheConnection?): CacheConnection?
    abstract fun getCacheConnectionNames(): Array<Key?>?
    abstract fun setCacheConnection(cacheName: String?, value: CacheConnection?)
    abstract fun getSessionCookie(): SessionCookieData?
    abstract fun setSessionCookie(data: SessionCookieData?)
    abstract fun getAuthCookie(): AuthCookieData?
    abstract fun setAuthCookie(data: AuthCookieData?)
    abstract fun getMailServers(): Array<lucee.runtime.net.mail.Server?>?
    abstract fun setMailServers(servers: Array<lucee.runtime.net.mail.Server?>?)
    abstract fun setLoggers(logs: Map<Key?, Pair<Log?, Struct?>?>?)
    @Throws(PageException::class)
    abstract fun getLogNames(): Collection<Collection.Key?>?
    @Throws(PageException::class)
    abstract fun getLog(name: String?): Log?
    @Throws(PageException::class)
    abstract fun getLogMetaData(string: String?): Struct?
    abstract fun getMailListener(): Object?
    abstract fun setMailListener(mailListener: Object?)
    abstract fun getWSMaintainSession(): Boolean // used in extension Axis1
    abstract fun setWSMaintainSession(maintainSession: Boolean)
    abstract fun getFTP(): FTPConnectionData?
    abstract fun setFTP(ftp: FTPConnectionData?)
    abstract fun getFullNullSupport(): Boolean
    abstract fun setFullNullSupport(fullNullSupport: Boolean)
    abstract fun getQueryListener(): TagListener?
    abstract fun setQueryListener(listener: TagListener?)
    abstract fun getSerializationSettings(): SerializationSettings?
    abstract fun setSerializationSettings(settings: SerializationSettings?)
    abstract fun getFunctionDirectories(): List<Resource?>?
    abstract fun setFunctionDirectories(resources: List<Resource?>?)
    abstract fun getQueryPSQ(): Boolean
    abstract fun setQueryPSQ(psq: Boolean)
    abstract fun getQueryVarUsage(): Int
    abstract fun setQueryVarUsage(varUsage: Int)
    abstract fun getQueryCachedAfter(): TimeSpan?
    abstract fun setQueryCachedAfter(ts: TimeSpan?)
    abstract fun getProxyData(): ProxyData?
    abstract fun setProxyData(data: ProxyData?)
    abstract fun getBlockedExtForFileUpload(): String?
    abstract fun setJavaSettings(javaSettings: JavaSettings?)
    abstract fun getXmlFeatures(): Struct?
    abstract fun setXmlFeatures(xmlFeatures: Struct?)
    abstract fun getAllowImplicidQueryCall(): Boolean
    abstract fun setAllowImplicidQueryCall(allowImplicidQueryCall: Boolean)
    abstract fun getRegex(): Regex?
    abstract fun setRegex(regex: Regex?)
    abstract fun getPreciseMath(): Boolean
    abstract fun setPreciseMath(preciseMath: Boolean)

    companion object {
        private const val serialVersionUID = 1384678713928757744L
        private val _loggers: Map<Collection.Key?, LoggerAndSourceData?>? = ConcurrentHashMap<Collection.Key?, LoggerAndSourceData?>()
        fun initTagDefaultAttributeValues(config: Config?, tagDefaultAttributeValues: Map<Collection.Key?, Map<Collection.Key?, Object?>?>?, sct: Struct?, dialect: Int) {
            if (sct.size() === 0) return
            val ci: ConfigPro? = config as ConfigPro?

            // first check the core lib without namespace
            val lib: TagLib = ci.getCoreTagLib(dialect)
            _initTagDefaultAttributeValues(config, lib, tagDefaultAttributeValues, sct, false)
            if (sct.size() === 0) return

            // then all the other libs including the namespace
            val tlds: Array<TagLib?> = ci.getTLDs(dialect)
            for (i in tlds.indices) {
                _initTagDefaultAttributeValues(config, tlds[i], tagDefaultAttributeValues, sct, true)
                if (sct.size() === 0) return
            }
        }

        private fun _initTagDefaultAttributeValues(config: Config?, lib: TagLib?, tagDefaultAttributeValues: Map<Collection.Key?, Map<Collection.Key?, Object?>?>?, sct: Struct?,
                                                   checkNameSpace: Boolean) {
            if (sct == null) return
            val it: Iterator<Entry<Key?, Object?>?> = sct.entryIterator()
            // loop tags
            var attrs: Struct
            var tag: TagLibTag?
            var iit: Iterator<Entry<Key?, Object?>?>
            var e: Entry<Key?, Object?>?
            var map: Map<Collection.Key?, Object?>?
            var attr: TagLibTagAttr
            var name: String?
            while (it.hasNext()) {
                e = it.next()
                attrs = Caster.toStruct(e.getValue(), null)
                if (attrs != null) {
                    tag = null
                    if (checkNameSpace) {
                        name = e.getKey().getLowerString()
                        if (StringUtil.startsWithIgnoreCase(name, lib.getNameSpaceAndSeparator())) {
                            name = name.substring(lib.getNameSpaceAndSeparator().length())
                            tag = lib.getTag(name)
                        }
                    } else tag = lib.getTag(e.getKey().getLowerString())
                    if (tag != null) {
                        sct.removeEL(e.getKey())
                        map = HashMap<Collection.Key?, Object?>()
                        iit = attrs.entryIterator()
                        while (iit.hasNext()) {
                            e = iit.next()
                            map.put(KeyImpl.init(e.getKey().getLowerString()), e.getValue())
                        }
                        tagDefaultAttributeValues.put(KeyImpl.init(tag.getFullName()), map)
                    }
                }
            }
        }

        @Throws(PageException::class)
        fun initLog(config: Config?, sct: Struct?): Map<Collection.Key?, Pair<Log?, Struct?>?>? {
            val rtn: Map<Collection.Key?, Pair<Log?, Struct?>?> = ConcurrentHashMap<Collection.Key?, Pair<Log?, Struct?>?>()
            if (sct == null) return rtn
            val it: Iterator<Entry<Key?, Object?>?> = sct.entryIterator()
            var e: Entry<Key?, Object?>?
            var v: Struct
            var k: Int
            var name: Collection.Key
            var las: LoggerAndSourceData
            while (it.hasNext()) {
                e = it.next()
                name = e.getKey()
                v = Caster.toStruct(e.getValue(), null)
                if (v == null) continue

                // appender
                var cdApp: ClassDefinition?
                val sctApp: Struct = Caster.toStruct(v.get("appender", null), null)
                val ac: String = AppListenerUtil.toClassName(sctApp)
                val abn: String = AppListenerUtil.toBundleName(sctApp)
                val abv: Version = AppListenerUtil.toBundleVersion(sctApp)
                if (StringUtil.isEmpty(abn)) cdApp = (config as ConfigPro?).getLogEngine().appenderClassDefintion(ac) else cdApp = ClassDefinitionImpl(config.getIdentification(), ac, abn, abv)

                // layout
                var cdLay: ClassDefinition?
                val sctLay: Struct = Caster.toStruct(v.get("layout", null), null)
                val lc: String = AppListenerUtil.toClassName(sctLay)
                val lbn: String = AppListenerUtil.toBundleName(sctLay)
                val lbv: Version = AppListenerUtil.toBundleVersion(sctLay)
                if (StringUtil.isEmpty(lbn)) cdLay = (config as ConfigPro?).getLogEngine().layoutClassDefintion(lc) else cdLay = ClassDefinitionImpl(config.getIdentification(), lc, lbn, lbv)
                if (cdApp != null && cdApp.hasClass()) {
                    // level
                    val strLevel: String = Caster.toString(v.get("level", null), null)
                    if (StringUtil.isEmpty(strLevel, true)) Caster.toString(v.get("loglevel", null), null)
                    val level: Int = LogUtil.toLevel(StringUtil.trim(strLevel, ""), Log.LEVEL_ERROR)
                    val sctAppArgs: Struct = Caster.toStruct(sctApp.get("arguments", null), null)
                    val sctLayArgs: Struct = Caster.toStruct(sctLay.get("arguments", null), null)
                    val readOnly: Boolean = Caster.toBooleanValue(v.get("readonly", null), false)

                    // ignore when no appender/name is defined
                    if (!StringUtil.isEmpty(name)) {
                        val appArgs = toMap(sctAppArgs)
                        las = if (cdLay != null && cdLay.hasClass()) {
                            val layArgs = toMap(sctLayArgs)
                            addLogger(name, level, cdApp, appArgs, cdLay, layArgs, readOnly)
                        } else addLogger(name, level, cdApp, appArgs, null, null, readOnly)
                        rtn.put(name, Pair<Log?, Struct?>(las.getLog(false), v))
                    }
                }
            }
            return rtn
        }

        private fun toMap(sct: Struct?): Map<String?, String?>? {
            val it: Iterator<Entry<Key?, Object?>?> = sct.entryIterator()
            val map: Map<String?, String?> = HashMap<String?, String?>()
            var e: Entry<Key?, Object?>?
            while (it.hasNext()) {
                e = it.next()
                map.put(e.getKey().getLowerString(), Caster.toString(e.getValue(), null))
            }
            return map
        }

        @Throws(PageException::class)
        private fun addLogger(name: Collection.Key?, level: Int, appender: ClassDefinition?, appenderArgs: Map<String?, String?>?, layout: ClassDefinition?,
                              layoutArgs: Map<String?, String?>?, readOnly: Boolean): LoggerAndSourceData? {
            val existing: LoggerAndSourceData? = _loggers!![name]
            val id: String = LoggerAndSourceData.id(name.getLowerString(), appender, appenderArgs, layout, layoutArgs, level, readOnly)
            if (existing != null) {
                if (existing.id().equals(id)) {
                    return existing
                }
                existing.close()
            }
            val las = LoggerAndSourceData(null, id, name.getLowerString(), appender, appenderArgs, layout, layoutArgs, level, readOnly, true)
            _loggers.put(name, las)
            return las
        }
    }

    init {
        this.config = config
        tagDefaultAttributeValues = (config as ConfigPro?).getTagDefaultAttributeValues()
        cachedWithinFunction = config.getCachedWithin(Config.CACHEDWITHIN_FUNCTION)
        cachedWithinInclude = config.getCachedWithin(Config.CACHEDWITHIN_INCLUDE)
        cachedWithinQuery = config.getCachedWithin(Config.CACHEDWITHIN_QUERY)
        cachedWithinResource = config.getCachedWithin(Config.CACHEDWITHIN_RESOURCE)
        cachedWithinHTTP = config.getCachedWithin(Config.CACHEDWITHIN_HTTP)
        cachedWithinFile = config.getCachedWithin(Config.CACHEDWITHIN_FILE)
        cachedWithinWS = config.getCachedWithin(Config.CACHEDWITHIN_WEBSERVICE)
    }
}