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

import java.lang.reflect.Method

/**
 * This class resolves the Application settings that are defined in Application.cfc via the this
 * reference, e.g. this.sessionManagement, this.localMode, etc.
 */
class ModernApplicationContext(pc: PageContext?, cfc: Component?, throwsErrorWhileInit: RefBoolean?) : ApplicationContextSupport(pc.getConfig()) {
    private val component: Component?
    private var name: String? = null
    private var setClientCookies: Boolean
    private var setDomainCookies: Boolean
    private var setSessionManagement: Boolean
    private var setClientManagement: Boolean
    private var applicationTimeout: TimeSpan?
    private var sessionTimeout: TimeSpan?
    private var clientTimeout: TimeSpan?
    private var requestTimeout: TimeSpan?
    private var loginStorage: Int = Scope.SCOPE_COOKIE
    private var scriptProtect: Int
    private var typeChecking: Boolean
    private var allowCompression: Boolean
    private var defaultDataSource: Object?
    private var bufferOutput: Boolean
    private var suppressContent: Boolean
    private var sessionType: Short
    private var wstype: Short
    private var wsMaintainSession = false
    private var sessionCluster: Boolean
    private var clientCluster: Boolean
    private var clientStorage: String?
    private var sessionStorage: String?
    private var secureJsonPrefix: String? = "//"
    private var secureJson = false
    private var ctmappings: Array<Mapping?>?
    private var cmappings: Array<Mapping?>?
    private var dataSources: Array<DataSource?>?
    private var s3: lucee.runtime.net.s3.Properties? = null
    private var ftp: FTPConnectionData? = null
    private var triggerComponentDataMember: Boolean
    private var defaultCaches: Map<Integer?, String?>? = null
    private var cacheConnections: Map<Collection.Key?, CacheConnection?>? = null
    private var sameFormFieldAsArray = false
    private var sameURLFieldAsArray = false
    private var customTypes: Map<String?, CustomType?>? = null
    private var cgiScopeReadonly: Boolean
    private var preciseMath: Boolean
    private var sessionCookie: SessionCookieData? = null
    private var authCookie: AuthCookieData? = null
    private var mailListener: Object? = null
    private var queryListener: TagListener? = null
    private var fullNullSupport: Boolean
    private var serializationSettings: SerializationSettings? = null
    private var queryPSQ: Boolean
    private var queryCachedAfter: TimeSpan?
    private var queryVarUsage: Int
    private var proxyData: ProxyData?
    private var blockedExtForFileUpload: String? = null
    private var mappings: Array<Mapping?>?
    private var initMappings = false
    private var initCustomTypes = false
    private var initMailListener = false
    private var initQueryListener = false
    private var initFullNullSupport = false
    private var initCachedWithins = false
    private var initApplicationTimeout = false
    private var initSessionTimeout = false
    private var initClientTimeout = false
    private var initRequestTimeout = false
    private var initSetClientCookies = false
    private var initSetClientManagement = false
    private var initSetDomainCookies = false
    private var initSetSessionManagement = false
    private var initScriptProtect = false
    private var initTypeChecking = false
    private var initAllowCompression = false
    private var initDefaultAttributeValues = false
    private var initClientStorage = false
    private var initSecureJsonPrefix = false
    private var initSecureJson = false
    private var initSessionStorage = false
    private var initSessionCluster = false
    private var initClientCluster = false
    private var initLoginStorage = false
    private var initSessionType = false
    private var initWS = false
    private var initTriggerComponentDataMember = false
    private var initDataSources = false
    private var initCache = false
    private var initCTMappings = false
    private var initCMappings = false
    private var localMode: Int
    private var initLocalMode = false
    private var initBufferOutput = false
    private var initSuppressContent = false
    private var initS3 = false
    private var initFTP = false
    private var ormEnabled = false
    private var ormConfig: ORMConfiguration? = null
    private var initRestSetting = false
    private var restSetting: RestSettings?
    private var initJavaSettings = false
    private var javaSettings: JavaSettings?
    private var ormDatasource: Object? = null
    private var locale: Locale?
    private var initLocale = false
    private var timeZone: TimeZone?
    private var initTimeZone = false
    private var webCharset: CharSet?
    private var initWebCharset = false
    private var resourceCharset: CharSet?
    private var initResourceCharset = false
    private var initCGIScopeReadonly = false
    private var initPreciseMath = false
    private var initSessionCookie = false
    private var initAuthCookie = false
    private var initSerializationSettings = false
    private var initQueryPSQ = false
    private var initQueryCacheAfter = false
    private var initQueryVarUsage = false
    private var initProxyData = false
    private var initBlockedExtForFileUpload = false
    private var initXmlFeatures = false
    private var initRegex = false
    private var xmlFeatures: Struct? = null
    private var antiSamyPolicyResource: Resource? = null
    private var restCFCLocations: Array<Resource?>?
    private var scopeCascading: Short = -1
    private var mailServers: Array<Server?>?
    private var initMailServer = false
    private var initLog = false
    private var logs: Map<Collection.Key?, Pair<Log?, Struct?>?>? = null
    private var funcDirs: List<Resource?>? = null
    private var initFuncDirs = false
    private var allowImplicidQueryCall: Boolean
    private var regex: Regex?
    fun initScopeCascading() {
        val o: Object? = Companion[component, SCOPE_CASCADING, null]
        if (o != null) {
            scopeCascading = ConfigWebUtil.toScopeCascading(Caster.toString(o, null), (-1).toShort())
        } else {
            val b: Boolean = Caster.toBoolean(Companion[component, SEARCH_IMPLICIT_SCOPES, null], null)
            if (b != null) scopeCascading = ConfigWebUtil.toScopeCascading(b)
        }
    }

    private fun initAllowImplicidQueryCall() {
        var o: Object? = Companion[component, SEARCH_QUERIES, null]
        if (o == null) o = Companion[component, SEARCH_RESULTS, null]
        if (o != null) allowImplicidQueryCall = Caster.toBooleanValue(o, allowImplicidQueryCall)
    }

    @Override
    fun getScopeCascading(): Short {
        return if (scopeCascading.toInt() == -1) config.getScopeCascadingType() else scopeCascading
    }

    @Override
    fun setScopeCascading(scopeCascading: Short) {
        this.scopeCascading = scopeCascading
    }

    @Override
    @Throws(PageException::class)
    fun reinitORM(pc: PageContext?) {

        // datasource
        var o: Object? = Companion[component, KeyConstants._datasource, null]
        if (o != null) {
            defaultDataSource = AppListenerUtil.toDefaultDatasource(pc.getConfig(), o, ThreadLocalPageContext.getLog(pc, "application"))
            ormDatasource = defaultDataSource
        }

        // default datasource
        o = Companion[component, DEFAULT_DATA_SOURCE, null]
        if (o != null) {
            defaultDataSource = AppListenerUtil.toDefaultDatasource(pc.getConfig(), o, ThreadLocalPageContext.getLog(pc, "application"))
        }

        // ormenabled
        o = Companion[component, ORM_ENABLED, null]
        if (o != null && Caster.toBooleanValue(o, false)) {
            ormEnabled = true

            // settings
            o = Companion[component, ORM_SETTINGS, null]
            val settings: Struct?
            if (o is Struct) settings = o as Struct? else settings = StructImpl()
            AppListenerUtil.setORMConfiguration(pc, this, settings)
        }
    }

    @Override
    fun hasName(): Boolean {
        return true // !StringUtil.isEmpty(getName());
    }

    @Override
    fun getName(): String? {
        if (name == null) {
            name = Caster.toString(Companion[component, KeyConstants._name, ""], "")
        }
        return name
    }

    @Override
    fun getLoginStorage(): Int {
        if (!initLoginStorage) {
            var str: String? = null
            val o: Object? = Companion[component, LOGIN_STORAGE, null]
            if (o != null) {
                str = Caster.toString(o, null)
                if (str != null) loginStorage = AppListenerUtil.translateLoginStorage(str, loginStorage)
            }
            initLoginStorage = true
        }
        return loginStorage
    }

    @Override
    fun getApplicationTimeout(): TimeSpan? {
        if (!initApplicationTimeout) {
            val o: Object? = Companion[component, APPLICATION_TIMEOUT, null]
            if (o != null) applicationTimeout = Caster.toTimespan(o, applicationTimeout)
            initApplicationTimeout = true
        }
        return applicationTimeout
    }

    @Override
    fun getSessionTimeout(): TimeSpan? {
        if (!initSessionTimeout) {
            val o: Object? = Companion[component, SESSION_TIMEOUT, null]
            if (o != null) sessionTimeout = Caster.toTimespan(o, sessionTimeout)
            initSessionTimeout = true
        }
        return sessionTimeout
    }

    @Override
    fun getClientTimeout(): TimeSpan? {
        if (!initClientTimeout) {
            val o: Object? = Companion[component, CLIENT_TIMEOUT, null]
            if (o != null) clientTimeout = Caster.toTimespan(o, clientTimeout)
            initClientTimeout = true
        }
        return clientTimeout
    }

    @Override
    fun getRequestTimeout(): TimeSpan? {
        if (!initRequestTimeout) {
            var o: Object? = Companion[component, REQUEST_TIMEOUT, null]
            if (o == null) o = Companion[component, KeyConstants._timeout, null]
            if (o != null) requestTimeout = Caster.toTimespan(o, requestTimeout)
            initRequestTimeout = true
        }
        return requestTimeout
    }

    @Override
    fun setRequestTimeout(requestTimeout: TimeSpan?) {
        this.requestTimeout = requestTimeout
        initRequestTimeout = true
    }

    @Override
    fun isSetClientCookies(): Boolean {
        if (!initSetClientCookies) {
            val o: Object? = Companion[component, SET_CLIENT_COOKIES, null]
            if (o != null) setClientCookies = Caster.toBooleanValue(o, setClientCookies)
            initSetClientCookies = true
        }
        return setClientCookies
    }

    @Override
    fun isSetClientManagement(): Boolean {
        if (!initSetClientManagement) {
            val o: Object? = Companion[component, CLIENT_MANAGEMENT, null]
            if (o != null) setClientManagement = Caster.toBooleanValue(o, setClientManagement)
            initSetClientManagement = true
        }
        return setClientManagement
    }

    @Override
    fun isSetDomainCookies(): Boolean {
        if (!initSetDomainCookies) {
            val o: Object? = Companion[component, SET_DOMAIN_COOKIES, null]
            if (o != null) setDomainCookies = Caster.toBooleanValue(o, setDomainCookies)
            initSetDomainCookies = true
        }
        return setDomainCookies
    }

    @Override
    fun isSetSessionManagement(): Boolean {
        if (!initSetSessionManagement) {
            val o: Object? = Companion[component, SESSION_MANAGEMENT, null]
            if (o != null) setSessionManagement = Caster.toBooleanValue(o, setSessionManagement)
            initSetSessionManagement = true
        }
        return setSessionManagement
    }

    @Override
    fun getClientstorage(): String? {
        if (!initClientStorage) {
            val str: String = Caster.toString(Companion[component, CLIENT_STORAGE, null], null)
            if (!StringUtil.isEmpty(str)) clientStorage = str
            initClientStorage = true
        }
        return clientStorage
    }

    @Override
    fun getScriptProtect(): Int {
        if (!initScriptProtect) {
            var str: String? = null
            val o: Object? = Companion[component, SCRIPT_PROTECT, null]
            if (o != null) {
                str = Caster.toString(o, null)
                if (str != null) scriptProtect = AppListenerUtil.translateScriptProtect(str)
            }
            initScriptProtect = true
        }
        return scriptProtect
    }

    @Override
    fun getTypeChecking(): Boolean {
        if (!initTypeChecking) {
            val b: Boolean = Caster.toBoolean(Companion[component, TYPE_CHECKING, null], null)
            if (b != null) typeChecking = b.booleanValue()
            initTypeChecking = true
        }
        return typeChecking
    }

    @Override
    fun getAllowCompression(): Boolean {
        if (!initAllowCompression) {
            val b: Boolean = Caster.toBoolean(Companion[component, KeyConstants._compression, null], null)
            if (b != null) allowCompression = b.booleanValue()
            initAllowCompression = true
        }
        return allowCompression
    }

    @Override
    fun setAllowCompression(allowCompression: Boolean) {
        this.allowCompression = allowCompression
        initAllowCompression = true
    }

    @Override
    fun getSecureJsonPrefix(): String? {
        if (!initSecureJsonPrefix) {
            val o: Object? = Companion[component, SECURE_JSON_PREFIX, null]
            if (o != null) secureJsonPrefix = Caster.toString(o, secureJsonPrefix)
            initSecureJsonPrefix = true
        }
        return secureJsonPrefix
    }

    @Override
    fun getSecureJson(): Boolean {
        if (!initSecureJson) {
            val o: Object? = Companion[component, SECURE_JSON, null]
            if (o != null) secureJson = Caster.toBooleanValue(o, secureJson)
            initSecureJson = true
        }
        return secureJson
    }

    @Override
    fun getSessionstorage(): String? {
        if (!initSessionStorage) {
            val str: String = Caster.toString(Companion[component, SESSION_STORAGE, null], null)
            if (!StringUtil.isEmpty(str)) sessionStorage = str
            initSessionStorage = true
        }
        return sessionStorage
    }

    @Override
    fun getSessionCluster(): Boolean {
        if (!initSessionCluster) {
            val o: Object? = Companion[component, SESSION_CLUSTER, null]
            if (o != null) sessionCluster = Caster.toBooleanValue(o, sessionCluster)
            initSessionCluster = true
        }
        return sessionCluster
    }

    @Override
    fun getClientCluster(): Boolean {
        if (!initClientCluster) {
            val o: Object? = Companion[component, CLIENT_CLUSTER, null]
            if (o != null) clientCluster = Caster.toBooleanValue(o, clientCluster)
            initClientCluster = true
        }
        return clientCluster
    }

    @Override
    fun getSessionType(): Short {
        if (!initSessionType) {
            var str: String? = null
            val o: Object? = Companion[component, SESSION_TYPE, null]
            if (o != null) {
                str = Caster.toString(o, null)
                if (str != null) sessionType = AppListenerUtil.toSessionType(str, sessionType)
            }
            initSessionType = true
        }
        return sessionType
    }

    @Override
    fun getWSType(): Short {
        initWS()
        return wstype
    }

    @Override
    override fun getWSMaintainSession(): Boolean {
        initWS()
        return wsMaintainSession
    }

    @Override
    override fun setWSMaintainSession(wsMaintainSession: Boolean) {
        initWS = true
        this.wsMaintainSession = wsMaintainSession
    }

    fun initWS() {
        if (!initWS) {
            var o: Object? = Companion[component, WS_SETTINGS, null]
            if (o == null) o = Companion[component, WS_SETTING, null]
            if (o is Struct) {
                val sct: Struct? = o as Struct?

                // type
                o = sct.get(KeyConstants._type, null)
                if (o is String) {
                    wstype = AppListenerUtil.toWSType(Caster.toString(o, null), WS_TYPE_AXIS1)
                }

                // MaintainSession
                o = sct.get("MaintainSession", null)
                if (o != null) {
                    wsMaintainSession = Caster.toBooleanValue(o, false)
                }
            }
            initWS = true
        }
    }

    @Override
    fun setWSType(wstype: Short) {
        initWS = true
        this.wstype = wstype
    }

    @Override
    fun getTriggerComponentDataMember(): Boolean {
        if (!initTriggerComponentDataMember) {
            var b: Boolean? = null
            var o: Object? = Companion[component, INVOKE_IMPLICIT_ACCESSOR, null]
            if (o == null) o = Companion[component, TRIGGER_DATA_MEMBER, null]
            if (o != null) {
                b = Caster.toBoolean(o, null)
                if (b != null) triggerComponentDataMember = b.booleanValue()
            }
            initTriggerComponentDataMember = true
        }
        return triggerComponentDataMember
    }

    @Override
    fun setTriggerComponentDataMember(triggerComponentDataMember: Boolean) {
        initTriggerComponentDataMember = true
        this.triggerComponentDataMember = triggerComponentDataMember
    }

    @Override
    fun getSameFieldAsArray(scope: Int): Boolean {
        return if (Scope.SCOPE_URL === scope) sameURLFieldAsArray else sameFormFieldAsArray
    }

    fun initSameFieldAsArray(pc: PageContext?) {
        val oldForm: Boolean = pc.getApplicationContext().getSameFieldAsArray(Scope.SCOPE_FORM)
        val oldURL: Boolean = pc.getApplicationContext().getSameFieldAsArray(Scope.SCOPE_URL)

        // Form
        var o: Object? = Companion[component, KeyConstants._sameformfieldsasarray, null]
        if (o != null && Decision.isBoolean(o)) sameFormFieldAsArray = Caster.toBooleanValue(o, false)

        // URL
        o = Companion[component, KeyConstants._sameurlfieldsasarray, null]
        if (o != null && Decision.isBoolean(o)) sameURLFieldAsArray = Caster.toBooleanValue(o, false)
        if (oldForm != sameFormFieldAsArray) pc.formScope().reinitialize(this)
        if (oldURL != sameURLFieldAsArray) pc.urlScope().reinitialize(this)
    }

    fun initWebCharset(pc: PageContext?) {
        initCharset()
        val cs: Charset? = getWebCharset()
        // has defined a web charset
        if (cs != null) {
            if (!cs.equals(config.getWebCharset())) {
                ReqRspUtil.setContentType(pc.getHttpServletResponse(), "text/html; charset=" + cs.name())
            }
        }
    }

    @Override
    fun getDefaultCacheName(type: Int): String? {
        initCache()
        return defaultCaches!![type]
    }

    @Override
    override fun getMailServers(): Array<Server?>? {
        initMailServers()
        return mailServers
    }

    private fun initMailServers() {
        if (!initMailServer) {
            var key: Key?
            var oMail: Object? = Companion[component, KeyConstants._mail.also { key = it }, null]
            if (oMail == null) oMail = Companion[component, KeyConstants._mails.also { key = it }, null]
            if (oMail == null) oMail = Companion[component, KeyConstants._mailServer.also { key = it }, null]
            if (oMail == null) oMail = Companion[component, KeyConstants._mailServers.also { key = it }, null]
            if (oMail == null) oMail = Companion[component, KeyConstants._smtpServerSettings.also { key = it }, null]
            var arrMail: Array? = Caster.toArray(oMail, null)
            // we also support a single struct instead of an array of structs
            if (arrMail == null) {
                val sctMail: Struct = Caster.toStruct(Companion[component, key, null], null)
                if (sctMail != null) {
                    arrMail = ArrayImpl()
                    arrMail.appendEL(sctMail)
                }
            }
            if (arrMail != null) {
                mailServers = AppListenerUtil.toMailServers(config, arrMail, null)
            }
            initMailServer = true
        }
    }

    @Override
    override fun setMailServers(servers: Array<Server?>?) {
        mailServers = servers
        initMailServer = true
    }

    @Override
    override fun getCacheConnection(cacheName: String?, defaultValue: CacheConnection?): CacheConnection? {
        initCache()
        return cacheConnections!![KeyImpl.init(cacheName)]
    }

    @Override
    override fun getCacheConnectionNames(): Array<Key?>? {
        initCache()
        val set: Set<Key?> = cacheConnections.keySet()
        return set.toArray(arrayOfNulls<Key?>(set.size()))
    }

    private fun initCache() {
        if (!initCache) {
            var hasResource = false
            if (defaultCaches == null) defaultCaches = ConcurrentHashMap<Integer?, String?>()
            if (cacheConnections == null) cacheConnections = ConcurrentHashMap<Collection.Key?, CacheConnection?>()
            var sctDefCache: Struct = Caster.toStruct(Companion[component, DEFAULT_CACHE, null], null)
            if (sctDefCache == null) sctDefCache = Caster.toStruct(Companion[component, KeyConstants._cache, null], null)

            // Default
            if (sctDefCache != null) {
                // Function
                initDefaultCache(sctDefCache, Config.CACHE_TYPE_FUNCTION, KeyConstants._function)
                // Query
                initDefaultCache(sctDefCache, Config.CACHE_TYPE_QUERY, KeyConstants._query)
                // Template
                initDefaultCache(sctDefCache, Config.CACHE_TYPE_TEMPLATE, KeyConstants._template)
                // Object
                initDefaultCache(sctDefCache, Config.CACHE_TYPE_OBJECT, KeyConstants._object)
                // INCLUDE
                initDefaultCache(sctDefCache, Config.CACHE_TYPE_INCLUDE, KeyConstants._include)
                // Resource
                if (initDefaultCache(sctDefCache, Config.CACHE_TYPE_RESOURCE, KeyConstants._resource)) hasResource = true
                // HTTP
                if (initDefaultCache(sctDefCache, Config.CACHE_TYPE_HTTP, KeyConstants._http)) hasResource = true
                // File
                if (initDefaultCache(sctDefCache, Config.CACHE_TYPE_FILE, KeyConstants._file)) hasResource = true
                // Webservice
                if (initDefaultCache(sctDefCache, Config.CACHE_TYPE_WEBSERVICE, KeyConstants._webservice)) hasResource = true
            }
            // check alias inmemoryfilesystem
            if (!hasResource) {
                val str: String = Caster.toString(Companion[component, IN_MEMORY_FILESYSTEM, null], null)
                if (!StringUtil.isEmpty(str, true)) {
                    defaultCaches.put(Config.CACHE_TYPE_RESOURCE, str.trim())
                }
            }

            // cache definitions
            val sctCache: Struct = Caster.toStruct(Companion[component, KeyConstants._cache, null], null)
            if (sctCache != null) {
                val it: Iterator<Entry<Key?, Object?>?> = sctCache.entryIterator()
                _initCache(cacheConnections, it, false)
            }
            initCache = true
        }
    }

    private fun _initCache(cacheConnections: Map<Key?, CacheConnection?>?, it: Iterator<Entry<Key?, Object?>?>?, sub: Boolean) {
        var e: Entry<Key?, Object?>?
        var sct: Struct
        var cc: CacheConnection?
        while (it!!.hasNext()) {
            e = it.next()
            if (!sub && KeyConstants._function.equals(e.getKey()) || KeyConstants._query.equals(e.getKey()) || KeyConstants._template.equals(e.getKey())
                    || KeyConstants._object.equals(e.getKey()) || KeyConstants._include.equals(e.getKey()) || KeyConstants._resource.equals(e.getKey())
                    || KeyConstants._http.equals(e.getKey()) || KeyConstants._file.equals(e.getKey()) || KeyConstants._webservice.equals(e.getKey())) continue
            if (!sub && KeyConstants._connections.equals(e.getKey())) {
                val _sct: Struct = Caster.toStruct(e.getValue(), null)
                if (_sct != null) _initCache(cacheConnections, _sct.entryIterator(), true)
                continue
            }
            sct = Caster.toStruct(e.getValue(), null)
            if (sct == null) continue
            cc = toCacheConnection(config, e.getKey().getString(), sct, null)
            if (cc != null) {
                cacheConnections.put(e.getKey(), cc)
                val def: Key = Caster.toKey(sct.get(KeyConstants._default, null), null)
                if (def != null) {
                    val n: String = e.getKey().getString().trim()
                    if (KeyConstants._function.equals(def)) defaultCaches.put(Config.CACHE_TYPE_FUNCTION, n) else if (KeyConstants._query.equals(def)) defaultCaches.put(Config.CACHE_TYPE_QUERY, n) else if (KeyConstants._template.equals(def)) defaultCaches.put(Config.CACHE_TYPE_TEMPLATE, n) else if (KeyConstants._object.equals(def)) defaultCaches.put(Config.CACHE_TYPE_OBJECT, n) else if (KeyConstants._include.equals(def)) defaultCaches.put(Config.CACHE_TYPE_INCLUDE, n) else if (KeyConstants._resource.equals(def)) defaultCaches.put(Config.CACHE_TYPE_RESOURCE, n) else if (KeyConstants._http.equals(def)) defaultCaches.put(Config.CACHE_TYPE_HTTP, n) else if (KeyConstants._file.equals(def)) defaultCaches.put(Config.CACHE_TYPE_FILE, n) else if (KeyConstants._webservice.equals(def)) defaultCaches.put(Config.CACHE_TYPE_WEBSERVICE, n)
                }
            }
        }
    }

    private fun initDefaultCache(data: Struct?, type: Int, key: Key?): Boolean {
        val o: Object = data.get(key, null)
        var hasResource = false
        if (o != null) {
            var name: String?
            var sct: Struct?
            val cc: CacheConnection?
            if (!StringUtil.isEmpty(Caster.toString(o, null).also { name = it }, true)) {
                defaultCaches.put(type, name.trim())
                hasResource = true
            } else if (Caster.toStruct(o, null).also { sct = it } != null) {
                cc = toCacheConnection(config, key.getString(), sct, null)
                if (cc != null) {
                    cacheConnections.put(key, cc)
                    defaultCaches.put(type, key.getString())
                    hasResource = true
                }
            }
        }
        return hasResource
    }

    @Override
    fun setDefaultCacheName(type: Int, cacheName: String?) {
        if (StringUtil.isEmpty(cacheName, true)) return
        initCache()
        defaultCaches.put(type, cacheName.trim())
    }

    @Override
    override fun setCacheConnection(cacheName: String?, cc: CacheConnection?) {
        if (StringUtil.isEmpty(cacheName, true)) return
        initCache()
        cacheConnections.put(KeyImpl.init(cacheName), cc)
    }

    @Override
    override fun getMailListener(): Object? {
        if (!initMailListener) {
            val mail: Struct = Caster.toStruct(Companion[component, KeyConstants._mail, null], null)
            if (mail != null) mailListener = mail.get(KeyConstants._listener, null)
            initMailListener = true
        }
        return mailListener
    }

    @Override
    override fun getQueryListener(): TagListener? {
        if (!initQueryListener) {
            val query: Struct = Caster.toStruct(Companion[component, KeyConstants._query, null], null)
            if (query != null) queryListener = Query.toTagListener(query.get(KeyConstants._listener, null), null)
            initQueryListener = true
        }
        return queryListener
    }

    @Override
    override fun getSerializationSettings(): SerializationSettings? {
        if (!initSerializationSettings) {
            val sct: Struct = Caster.toStruct(Companion[component, KeyConstants._serialization, null], null)
            serializationSettings = if (sct != null) {
                SerializationSettings.toSerializationSettings(sct)
            } else SerializationSettings.DEFAULT
            initSerializationSettings = true
        }
        return serializationSettings
    }

    @Override
    override fun setSerializationSettings(settings: SerializationSettings?) {
        serializationSettings = settings
        initSerializationSettings = true
    }

    @Override
    fun getMappings(): Array<Mapping?>? {
        if (!initMappings) {
            val o: Object? = Companion[component, KeyConstants._mappings, null]
            if (o != null) mappings = AppListenerUtil.toMappings(config, o, mappings, getSource())
            initMappings = true
        }
        return mappings
    }

    @Override
    fun getCustomTagMappings(): Array<Mapping?>? {
        if (!initCTMappings) {
            val o: Object? = Companion[component, CUSTOM_TAG_PATHS, null]
            if (o != null) ctmappings = AppListenerUtil.toCustomTagMappings(config, o, getSource(), ctmappings)
            initCTMappings = true
        }
        return ctmappings
    }

    @Override
    fun getComponentMappings(): Array<Mapping?>? {
        if (!initCMappings) {
            val o: Object? = Companion[component, COMPONENT_PATHS, null]
            if (o != null) cmappings = AppListenerUtil.toComponentMappings(config, o, getSource(), cmappings)
            initCMappings = true
        }
        return cmappings
    }

    @Override
    override fun getFunctionDirectories(): List<Resource?>? {
        if (!initFuncDirs) {
            val o: Object? = Companion[component, FUNCTION_PATHS, null]
            if (o != null) funcDirs = AppListenerUtil.loadResources(config, null, o, true)
            initFuncDirs = true
        }
        return funcDirs
    }

    @Override
    override fun setFunctionDirectories(resources: List<Resource?>?) {
        funcDirs = resources
        initFuncDirs = true
    }

    @Override
    fun getLocalMode(): Int {
        if (!initLocalMode) {
            val o: Object? = Companion[component, LOCAL_MODE, null]
            if (o != null) localMode = AppListenerUtil.toLocalMode(o, localMode)
            initLocalMode = true
        }
        return localMode
    }

    @Override
    fun getLocale(): Locale? {
        if (!initLocale) {
            val o: Object? = Companion[component, KeyConstants._locale, null]
            if (o != null) {
                val str: String = Caster.toString(o, null)
                if (!StringUtil.isEmpty(str)) locale = LocaleFactory.getLocale(str, locale)
            }
            initLocale = true
        }
        return locale
    }

    @Override
    fun getTimeZone(): TimeZone? {
        if (!initTimeZone) {
            val o: Object? = Companion[component, KeyConstants._timezone, null]
            if (o != null) {
                val str: String = Caster.toString(o, null)
                if (!StringUtil.isEmpty(str)) timeZone = TimeZoneUtil.toTimeZone(str, timeZone)
            }
            initTimeZone = true
        }
        return timeZone
    }

    @Override
    fun getWebCharset(): Charset? {
        if (!initWebCharset) initCharset()
        return CharsetUtil.toCharset(webCharset)
    }

    fun getWebCharSet(): CharSet? {
        if (!initWebCharset) initCharset()
        return webCharset
    }

    @Override
    override fun getAntiSamyPolicyResource(): Resource? {
        return antiSamyPolicyResource
    }

    @Override
    override fun setAntiSamyPolicyResource(res: Resource?) {
        antiSamyPolicyResource = res
    }

    fun initAntiSamyPolicyResource(pc: PageContext?) {
        val sct: Struct = Caster.toStruct(Companion[component, KeyConstants._security, null], null)
        if (sct != null) {
            val tmp: Resource = ResourceUtil.toResourceExisting(pc, Caster.toString(sct.get("antisamypolicy", null), null), true, null)
            if (tmp != null) antiSamyPolicyResource = tmp
        }
    }

    @Override
    fun getResourceCharset(): Charset? {
        if (!initResourceCharset) initCharset()
        return CharsetUtil.toCharset(resourceCharset)
    }

    fun getResourceCharSet(): CharSet? {
        if (!initResourceCharset) initCharset()
        return resourceCharset
    }

    /**
     * @return webcharset if it was defined, otherwise null
     */
    private fun initCharset(): CharSet? {
        val o: Object? = Companion[component, KeyConstants._charset, null]
        if (o != null) {
            val sct: Struct = Caster.toStruct(o, null)
            if (sct != null) {
                val web: CharSet = CharsetUtil.toCharSet(Caster.toString(sct.get(KeyConstants._web, null), null), null)
                if (!initWebCharset && web != null) webCharset = web
                val res: CharSet = CharsetUtil.toCharSet(Caster.toString(sct.get(KeyConstants._resource, null), null), null)
                if (!initResourceCharset && res != null) resourceCharset = res
                initWebCharset = true
                initResourceCharset = true
                return web
            }
        }
        initWebCharset = true
        initResourceCharset = true
        return null
    }

    @Override
    fun getBufferOutput(): Boolean {
        return _getBufferOutput()
    }

    fun _getBufferOutput(): Boolean {
        if (!initBufferOutput) {
            val o: Object? = Companion[component, BUFFER_OUTPUT, null]
            if (o != null) bufferOutput = Caster.toBooleanValue(o, bufferOutput)
            initBufferOutput = true
        }
        return bufferOutput
    }

    @Override
    fun getSuppressContent(): Boolean {
        if (!initSuppressContent) {
            val o: Object? = Companion[component, SUPPRESS_CONTENT, null]
            if (o != null) suppressContent = Caster.toBooleanValue(o, suppressContent)
            initSuppressContent = true
        }
        return suppressContent
    }

    @Override
    fun setSuppressContent(suppressContent: Boolean) {
        this.suppressContent = suppressContent
        initSuppressContent = true
    }

    @Override
    fun getS3(): lucee.runtime.net.s3.Properties? {
        if (!initS3) {
            val o: Object? = Companion[component, KeyConstants._s3, null]
            if (o != null && Decision.isStruct(o)) s3 = AppListenerUtil.toS3(Caster.toStruct(o, null))
            initS3 = true
        }
        return s3
    }

    @Override
    override fun getFTP(): FTPConnectionData? {
        if (!initFTP) {
            val o: Object? = Companion[component, KeyConstants._ftp, null]
            if (o != null && Decision.isStruct(o)) ftp = AppListenerUtil.toFTP(Caster.toStruct(o, null))
            initFTP = true
        }
        return ftp
    }

    @Override
    fun getDefaultDataSource(): String? {
        throw PageRuntimeException(DeprecatedException("this method is no longer supported!"))
    }

    @Override
    fun getDefDataSource(): Object? {
        return defaultDataSource
    }

    @Override
    fun getDataSources(): Array<DataSource?>? {
        if (!initDataSources) {
            val o: Object? = Companion[component, KeyConstants._datasources, null]
            // if "this.datasources" does not exists, check if "this.datasource" exists and contains a struct
            /*
			 * if(o==null){ o = get(component,KeyConstants._datasource,null); if(!Decision.isStruct(o)) o=null;
			 * }
			 */if (o != null) dataSources = AppListenerUtil.toDataSources(config, o, dataSources, ThreadLocalPageContext.getLog(config, "application"))
            initDataSources = true
        }
        return dataSources
    }

    @Override
    fun isORMEnabled(): Boolean {
        return ormEnabled
    }

    @Override
    fun getORMDatasource(): String? {
        throw PageRuntimeException(DeprecatedException("this method is no longer supported!"))
    }

    @Override
    fun getORMDataSource(): Object? {
        return ormDatasource
    }

    @Override
    fun getORMConfiguration(): ORMConfiguration? {
        return ormConfig
    }

    fun getComponent(): Component? {
        return component
    }

    fun getCustom(key: Key?): Object? {
        try {
            val cw: ComponentSpecificAccess = ComponentSpecificAccess.toComponentSpecificAccess(Component.ACCESS_PRIVATE, component)
            return cw.get(key, null)
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
        }
        return null
    }

    //////////////////////// SETTERS /////////////////////////
    @Override
    fun setApplicationTimeout(applicationTimeout: TimeSpan?) {
        initApplicationTimeout = true
        this.applicationTimeout = applicationTimeout
    }

    @Override
    fun setSessionTimeout(sessionTimeout: TimeSpan?) {
        initSessionTimeout = true
        this.sessionTimeout = sessionTimeout
    }

    @Override
    fun setClientTimeout(clientTimeout: TimeSpan?) {
        initClientTimeout = true
        this.clientTimeout = clientTimeout
    }

    @Override
    fun setClientstorage(clientstorage: String?) {
        initClientStorage = true
        clientStorage = clientstorage
    }

    @Override
    fun setSessionstorage(sessionstorage: String?) {
        initSessionStorage = true
        sessionStorage = sessionstorage
    }

    @Override
    fun setCustomTagMappings(customTagMappings: Array<Mapping?>?) {
        initCTMappings = true
        ctmappings = customTagMappings
    }

    @Override
    fun setComponentMappings(componentMappings: Array<Mapping?>?) {
        initCMappings = true
        cmappings = componentMappings
    }

    @Override
    fun setMappings(mappings: Array<Mapping?>?) {
        initMappings = true
        this.mappings = mappings
    }

    @Override
    override fun setMailListener(mailListener: Object?) {
        initMailListener = true
        this.mailListener = mailListener
    }

    @Override
    override fun setQueryListener(listener: TagListener?) {
        initQueryListener = true
        queryListener = listener
    }

    @Override
    fun setDataSources(dataSources: Array<DataSource?>?) {
        initDataSources = true
        this.dataSources = dataSources
    }

    @Override
    fun setLoginStorage(loginStorage: Int) {
        initLoginStorage = true
        this.loginStorage = loginStorage
    }

    @Override
    fun setDefaultDataSource(datasource: String?) {
        defaultDataSource = datasource
    }

    @Override
    fun setDefDataSource(datasource: Object?) {
        defaultDataSource = datasource
    }

    @Override
    fun setScriptProtect(scriptrotect: Int) {
        initScriptProtect = true
        scriptProtect = scriptrotect
    }

    @Override
    fun setTypeChecking(typeChecking: Boolean) {
        initTypeChecking = true
        this.typeChecking = typeChecking
    }

    @Override
    fun setSecureJson(secureJson: Boolean) {
        initSecureJson = true
        this.secureJson = secureJson
    }

    @Override
    fun setSecureJsonPrefix(secureJsonPrefix: String?) {
        initSecureJsonPrefix = true
        this.secureJsonPrefix = secureJsonPrefix
    }

    @Override
    fun setSetClientCookies(setClientCookies: Boolean) {
        initSetClientCookies = true
        this.setClientCookies = setClientCookies
    }

    @Override
    fun setSetClientManagement(setClientManagement: Boolean) {
        initSetClientManagement = true
        this.setClientManagement = setClientManagement
    }

    @Override
    fun setSetDomainCookies(setDomainCookies: Boolean) {
        initSetDomainCookies = true
        this.setDomainCookies = setDomainCookies
    }

    @Override
    fun setSetSessionManagement(setSessionManagement: Boolean) {
        initSetSessionManagement = true
        this.setSessionManagement = setSessionManagement
    }

    @Override
    fun setLocalMode(localMode: Int) {
        initLocalMode = true
        this.localMode = localMode
    }

    @Override
    fun setLocale(locale: Locale?) {
        initLocale = true
        this.locale = locale
    }

    @Override
    fun setTimeZone(timeZone: TimeZone?) {
        initTimeZone = true
        this.timeZone = timeZone
    }

    @Override
    fun setWebCharset(webCharset: Charset?) {
        initWebCharset = true
        this.webCharset = CharsetUtil.toCharSet(webCharset)
    }

    @Override
    fun setResourceCharset(resourceCharset: Charset?) {
        initResourceCharset = true
        this.resourceCharset = CharsetUtil.toCharSet(resourceCharset)
    }

    @Override
    fun setBufferOutput(bufferOutput: Boolean) {
        initBufferOutput = true
        this.bufferOutput = bufferOutput
    }

    @Override
    fun setSessionType(sessionType: Short) {
        initSessionType = true
        this.sessionType = sessionType
    }

    @Override
    fun setClientCluster(clientCluster: Boolean) {
        initClientCluster = true
        this.clientCluster = clientCluster
    }

    @Override
    fun setSessionCluster(sessionCluster: Boolean) {
        initSessionCluster = true
        this.sessionCluster = sessionCluster
    }

    @Override
    fun setS3(s3: Properties?) {
        initS3 = true
        this.s3 = s3
    }

    @Override
    override fun setFTP(ftp: FTPConnectionData?) {
        initFTP = true
        this.ftp = ftp
    }

    @Override
    fun setORMEnabled(ormEnabled: Boolean) {
        this.ormEnabled = ormEnabled
    }

    @Override
    fun setORMConfiguration(ormConfig: ORMConfiguration?) {
        this.ormConfig = ormConfig
    }

    @Override
    fun setORMDatasource(ormDatasource: String?) {
        this.ormDatasource = ormDatasource
    }

    @Override
    fun setORMDataSource(ormDatasource: Object?) {
        this.ormDatasource = ormDatasource
    }

    @Override
    fun getSource(): Resource? {
        return component.getPageSource().getResource()
    }

    @Override
    fun getRestSettings(): RestSettings? {
        initRest()
        return restSetting
    }

    @Override
    fun getRestCFCLocations(): Array<Resource?>? {
        initRest()
        return restCFCLocations
    }

    private fun initRest() {
        if (!initRestSetting) {
            val o: Object? = Companion[component, REST_SETTING, null]
            if (o != null && Decision.isStruct(o)) {
                val sct: Struct = Caster.toStruct(o, null)

                // cfclocation
                var obj: Object = sct.get(KeyConstants._cfcLocation, null)
                if (obj == null) obj = sct.get(KeyConstants._cfcLocations, null)
                val list: List<Resource?> = AppListenerUtil.loadResources(config, null, obj, true)
                restCFCLocations = if (list == null) null else list.toArray(arrayOfNulls<Resource?>(list.size()))

                // skipCFCWithError
                val skipCFCWithError: Boolean = Caster.toBooleanValue(sct.get(KeyConstants._skipCFCWithError, null), restSetting.getSkipCFCWithError())

                // returnFormat
                val returnFormat: Int = Caster.toIntValue(sct.get(KeyConstants._returnFormat, null), restSetting.getReturnFormat())
                restSetting = RestSettingImpl(skipCFCWithError, returnFormat)
            }
            initRestSetting = true
        }
    }

    @Override
    override fun setJavaSettings(javaSettings: JavaSettings?) {
        initJavaSettings = true
        this.javaSettings = javaSettings
    }

    @Override
    fun getJavaSettings(): JavaSettings? {
        initJava()
        return javaSettings
    }

    private fun initJava() {
        if (!initJavaSettings) {
            val o: Object? = Companion[component, JAVA_SETTING, null]
            if (o != null && Decision.isStruct(o)) {
                javaSettings = JavaSettingsImpl.newInstance(javaSettings, Caster.toStruct(o, null))
            }
            initJavaSettings = true
        }
    }

    @Override
    override fun getTagAttributeDefaultValues(pc: PageContext?, tagClassName: String?): Map<Collection.Key?, Object?>? {
        if (!initDefaultAttributeValues) {
            // this.tag.<tagname>.<attribute-name>=<value>
            val sct: Struct = Caster.toStruct(Companion[component, KeyConstants._tag, null], null)
            if (sct != null) {
                setTagAttributeDefaultValues(pc, sct)
            }
            initDefaultAttributeValues = true
        }
        return super.getTagAttributeDefaultValues(pc, tagClassName)
    }

    @Override
    override fun setTagAttributeDefaultValues(pc: PageContext?, sct: Struct?) {
        initDefaultAttributeValues = true
        super.setTagAttributeDefaultValues(pc, sct)
    }

    @Override
    fun getCustomType(strType: String?): CustomType? {
        if (!initCustomTypes) {
            if (customTypes == null) customTypes = HashMap<String?, CustomType?>()

            // this.type.susi=function(any value){};
            val sct: Struct = Caster.toStruct(Companion[component, KeyConstants._type, null], null)
            if (sct != null) {
                val it: Iterator<Entry<Key?, Object?>?> = sct.entryIterator()
                var e: Entry<Key?, Object?>?
                var udf: UDF
                while (it.hasNext()) {
                    e = it.next()
                    udf = Caster.toFunction(e.getValue(), null)
                    if (udf != null) customTypes.put(e.getKey().getLowerString(), UDFCustomType(udf))
                }
            }
            initCustomTypes = true
        }
        return customTypes!![strType.trim().toLowerCase()]
    }

    @Override
    override fun getCachedWithin(type: Int): Object? {
        if (!initCachedWithins) {
            var sct: Struct? = Caster.toStruct(Companion[component, KeyConstants._cachedWithin, null], null)
            if (sct != null) {
                val it: Iterator<Entry<Key?, Object?>?> = sct.entryIterator()
                var e: Entry<Key?, Object?>?
                var v: Object
                var k: Int
                while (it.hasNext()) {
                    e = it.next()
                    k = AppListenerUtil.toCachedWithinType(e.getKey().getString(), -1)
                    v = e.getValue()
                    if (k != -1 && !StringUtil.isEmpty(v)) setCachedWithin(k, v)
                }
            }
            sct = null
            // also support this.tag.include... as second chance
            if (super.getCachedWithin(Config.CACHEDWITHIN_INCLUDE) == null) {
                sct = Caster.toStruct(Companion[component, KeyConstants._tag, null], null)
                if (sct != null) {
                    var obj: Object? = sct.get(KeyConstants._include, null)
                    if (Decision.isCastableToStruct(obj)) {
                        val tmp: Struct = Caster.toStruct(obj, null)
                        obj = if (tmp == null) null else tmp.get("cachedWithin", null)
                        if (!StringUtil.isEmpty(obj)) setCachedWithin(Config.CACHEDWITHIN_INCLUDE, obj)
                    }
                }
            }

            // also support this.tag.function... as second chance
            if (super.getCachedWithin(Config.CACHEDWITHIN_FUNCTION) == null) {
                if (sct == null) sct = Caster.toStruct(Companion[component, KeyConstants._tag, null], null)
                if (sct != null) {
                    var obj: Object? = sct.get(KeyConstants._function, null)
                    if (Decision.isCastableToStruct(obj)) {
                        val tmp: Struct = Caster.toStruct(obj, null)
                        obj = if (tmp == null) null else tmp.get("cachedWithin", null)
                        if (!StringUtil.isEmpty(obj)) setCachedWithin(Config.CACHEDWITHIN_FUNCTION, obj)
                    }
                }
            }
            initCachedWithins = true
        }
        return super.getCachedWithin(type)
    }

    @Override
    fun getCGIScopeReadonly(): Boolean {
        if (!initCGIScopeReadonly) {
            val o: Object? = Companion[component, CGI_READONLY, null]
            if (o != null) cgiScopeReadonly = Caster.toBooleanValue(o, cgiScopeReadonly)
            initCGIScopeReadonly = true
        }
        return cgiScopeReadonly
    }

    @Override
    fun setCGIScopeReadonly(cgiScopeReadonly: Boolean) {
        initCGIScopeReadonly = true
        this.cgiScopeReadonly = cgiScopeReadonly
    }

    @Override
    override fun getBlockedExtForFileUpload(): String? {
        if (!initBlockedExtForFileUpload) {
            val o: Object? = Companion[component, BLOCKED_EXT_FOR_FILE_UPLOAD, null]
            blockedExtForFileUpload = Caster.toString(o, null)
            initBlockedExtForFileUpload = true
        }
        return blockedExtForFileUpload
    }

    @Override
    override fun getSessionCookie(): SessionCookieData? {
        if (!initSessionCookie) {
            val sct: Struct = Caster.toStruct(Companion[component, SESSION_COOKIE, null], null)
            if (sct != null) sessionCookie = AppListenerUtil.toSessionCookie(config, sct)
            initSessionCookie = true
        }
        return sessionCookie
    }

    @Override
    override fun getAuthCookie(): AuthCookieData? {
        if (!initAuthCookie) {
            val sct: Struct = Caster.toStruct(Companion[component, AUTH_COOKIE, null], null)
            if (sct != null) authCookie = AppListenerUtil.toAuthCookie(config, sct)
            initAuthCookie = true
        }
        return authCookie
    }

    @Override
    override fun setSessionCookie(data: SessionCookieData?) {
        sessionCookie = data
        initSessionCookie = true
    }

    @Override
    override fun setAuthCookie(data: AuthCookieData?) {
        authCookie = data
        initAuthCookie = true
    }

    @Override
    override fun setLoggers(logs: Map<Key?, Pair<Log?, Struct?>?>?) {
        this.logs = logs
        initLog = true
    }

    @Override
    override fun getLog(name: String?): Log? {
        if (!initLog) initLog()
        val pair: Pair<Log?, Struct?> = logs!![KeyImpl.init(StringUtil.emptyIfNull(name))] ?: return null
        return pair.getName()
    }

    @Override
    override fun getLogMetaData(name: String?): Struct? {
        if (!initLog) initLog()
        val pair: Pair<Log?, Struct?> = logs!![KeyImpl.init(StringUtil.emptyIfNull(name))] ?: return null
        return pair.getValue().duplicate(false) as Struct
    }

    @Override
    override fun getLogNames(): Collection<Collection.Key?>? {
        if (!initLog) initLog()
        return logs.keySet()
    }

    private fun initLog() {
        try {
            // appender
            var oLogs: Object? = Companion[component, LOGS, null]
            if (oLogs == null) oLogs = Companion[component, LOG, null]
            val sct: Struct = Caster.toStruct(oLogs, null)
            logs = initLog(ThreadLocalPageContext.getConfig(config), sct)
            initLog = true
        } catch (e: PageException) {
            throw PageRuntimeException(e)
        }
    }

    @Override
    override fun getFullNullSupport(): Boolean {
        if (!initFullNullSupport) {
            var b: Boolean = Caster.toBoolean(Companion[component, NULL_SUPPORT, null], null)
            if (b == null) b = Caster.toBoolean(Companion[component, ENABLE_NULL_SUPPORT, null], null)
            if (b != null) fullNullSupport = b.booleanValue()
            initFullNullSupport = true
        }
        return fullNullSupport
    }

    @Override
    override fun setFullNullSupport(fullNullSupport: Boolean) {
        this.fullNullSupport = fullNullSupport
        initFullNullSupport = true
    }

    @Override
    override fun getPreciseMath(): Boolean {
        if (!initPreciseMath) {
            var b: Boolean = Caster.toBoolean(Companion[component, PRECISE_MATH, null], null)
            if (b == null) b = Caster.toBoolean(Companion[component, PRECISION_EVAL, null], null)
            if (b != null) preciseMath = b.booleanValue()
            initPreciseMath = true
        }
        return preciseMath
    }

    @Override
    override fun setPreciseMath(preciseMath: Boolean) {
        this.preciseMath = preciseMath
        initPreciseMath = true
    }

    @Override
    override fun getQueryPSQ(): Boolean {
        if (!initQueryPSQ) {
            val qry: Struct = Caster.toStruct(Companion[component, KeyConstants._query, null], null)
            if (qry != null) {
                var b: Boolean = Caster.toBoolean(qry.get(PSQ, null), null)
                if (b == null) b = Caster.toBoolean(qry.get(PSQ_LONG, null), null)
                if (b != null) queryPSQ = b.booleanValue()
            }
            initQueryPSQ = true
        }
        return queryPSQ
    }

    @Override
    override fun setQueryPSQ(psq: Boolean) {
        queryPSQ = psq
        initQueryPSQ = true
    }

    @Override
    override fun getQueryCachedAfter(): TimeSpan? {
        if (!initQueryCacheAfter) {
            val qry: Struct = Caster.toStruct(Companion[component, KeyConstants._query, null], null)
            if (qry != null) {
                val ts: TimeSpan = Caster.toTimespan(qry.get(CACHED_AFTER, null), null)
                if (ts != null) queryCachedAfter = ts
            }
            initQueryCacheAfter = true
        }
        return queryCachedAfter
    }

    @Override
    override fun setQueryCachedAfter(ts: TimeSpan?) {
        queryCachedAfter = ts
        initQueryCacheAfter = true
    }

    @Override
    override fun getQueryVarUsage(): Int {
        if (!initQueryVarUsage) {
            val qry: Struct = Caster.toStruct(Companion[component, KeyConstants._query, null], null)
            if (qry != null) {
                var str: String = Caster.toString(qry.get(VAR_USAGE, null), null)
                if (StringUtil.isEmpty(str)) str = Caster.toString(qry.get(VARIABLE_USAGE, null), null)
                if (!StringUtil.isEmpty(str)) queryVarUsage = AppListenerUtil.toVariableUsage(str, queryVarUsage)
            }
            initQueryVarUsage = true
        }
        return queryVarUsage
    }

    @Override
    override fun setQueryVarUsage(varUsage: Int) {
        queryVarUsage = varUsage
        initQueryVarUsage = true
    }

    @Override
    override fun getProxyData(): ProxyData? {
        if (!initProxyData) {
            val sct: Struct = Caster.toStruct(Companion[component, KeyConstants._proxy, null], null)
            proxyData = ProxyDataImpl.toProxyData(sct)
            initProxyData = true
        }
        return proxyData
    }

    @Override
    override fun setProxyData(data: ProxyData?) {
        proxyData = data
        initProxyData = true
    }

    @Override
    override fun getXmlFeatures(): Struct? {
        if (!initXmlFeatures) {
            val sct: Struct = Caster.toStruct(Companion[component, XML_FEATURES, null], null)
            if (sct != null) xmlFeatures = sct
            initXmlFeatures = true
        }
        return xmlFeatures
    }

    @Override
    override fun setXmlFeatures(xmlFeatures: Struct?) {
        this.xmlFeatures = xmlFeatures
    }

    @Override
    override fun getAllowImplicidQueryCall(): Boolean {
        return allowImplicidQueryCall
    }

    @Override
    override fun setAllowImplicidQueryCall(allowImplicidQueryCall: Boolean) {
        this.allowImplicidQueryCall = allowImplicidQueryCall
    }

    @Override
    override fun getRegex(): Regex? {
        if (!initRegex) {
            val sct: Struct = Caster.toStruct(Companion[component, REGEX, null], null)
            var has = false
            if (sct != null) {
                var str: String = Caster.toString(sct.get(ENGINE, null), null)
                if (StringUtil.isEmpty(str, true)) str = Caster.toString(sct.get(KeyConstants._type, null), null)
                if (StringUtil.isEmpty(str, true)) str = Caster.toString(sct.get(DIALECT, null), null)
                if (!StringUtil.isEmpty(str, true)) {
                    val type: Int = RegexFactory.toType(str, -1)
                    if (type != -1) {
                        val tmp: Regex = RegexFactory.toRegex(type, null)
                        if (tmp != null) {
                            has = true
                            regex = tmp
                        }
                    }
                }
            }
            if (!has) {
                val res: Boolean = Caster.toBoolean(Companion[component, USE_JAVA_AS_REGEX_ENGINE, null], null)
                if (res != null) regex = RegexFactory.toRegex(res.booleanValue())
            }
            initRegex = true
        }
        return regex
    }

    @Override
    override fun setRegex(regex: Regex?) {
        this.regex = regex
    }

    companion object {
        private const val serialVersionUID = -8230105685329758613L
        private val APPLICATION_TIMEOUT: Collection.Key? = KeyConstants._applicationTimeout
        private val CLIENT_MANAGEMENT: Collection.Key? = KeyConstants._clientManagement
        private val CLIENT_STORAGE: Collection.Key? = KeyImpl.getInstance("clientStorage")
        private val SESSION_STORAGE: Collection.Key? = KeyImpl.getInstance("sessionStorage")
        private val LOGIN_STORAGE: Collection.Key? = KeyImpl.getInstance("loginStorage")
        private val SESSION_TYPE: Collection.Key? = KeyImpl.getInstance("sessionType")
        private val WS_SETTINGS: Collection.Key? = KeyImpl.getInstance("wssettings")
        private val WS_SETTING: Collection.Key? = KeyImpl.getInstance("wssetting")
        private val TRIGGER_DATA_MEMBER: Collection.Key? = KeyImpl.getInstance("triggerDataMember")
        private val INVOKE_IMPLICIT_ACCESSOR: Collection.Key? = KeyImpl.getInstance("InvokeImplicitAccessor")
        private val SESSION_MANAGEMENT: Collection.Key? = KeyImpl.getInstance("sessionManagement")
        private val SESSION_TIMEOUT: Collection.Key? = KeyImpl.getInstance("sessionTimeout")
        private val CLIENT_TIMEOUT: Collection.Key? = KeyImpl.getInstance("clientTimeout")
        private val REQUEST_TIMEOUT: Collection.Key? = KeyImpl.getInstance("requestTimeout")
        private val SET_CLIENT_COOKIES: Collection.Key? = KeyImpl.getInstance("setClientCookies")
        private val SET_DOMAIN_COOKIES: Collection.Key? = KeyImpl.getInstance("setDomainCookies")
        private val SCRIPT_PROTECT: Collection.Key? = KeyImpl.getInstance("scriptProtect")
        private val CUSTOM_TAG_PATHS: Collection.Key? = KeyImpl.getInstance("customtagpaths")
        private val COMPONENT_PATHS: Collection.Key? = KeyImpl.getInstance("componentpaths")
        private val FUNCTION_PATHS: Collection.Key? = KeyImpl.getInstance("functionpaths")
        private val SECURE_JSON_PREFIX: Collection.Key? = KeyImpl.getInstance("secureJsonPrefix")
        private val SECURE_JSON: Collection.Key? = KeyImpl.getInstance("secureJson")
        private val LOCAL_MODE: Collection.Key? = KeyImpl.getInstance("localMode")
        private val BUFFER_OUTPUT: Collection.Key? = KeyImpl.getInstance("bufferOutput")
        private val SESSION_CLUSTER: Collection.Key? = KeyImpl.getInstance("sessionCluster")
        private val CLIENT_CLUSTER: Collection.Key? = KeyImpl.getInstance("clientCluster")
        private val DEFAULT_DATA_SOURCE: Collection.Key? = KeyImpl.getInstance("defaultdatasource")
        private val DEFAULT_CACHE: Collection.Key? = KeyImpl.getInstance("defaultcache")
        private val ORM_ENABLED: Collection.Key? = KeyImpl.getInstance("ormenabled")
        private val ORM_SETTINGS: Collection.Key? = KeyImpl.getInstance("ormsettings")
        private val IN_MEMORY_FILESYSTEM: Collection.Key? = KeyImpl.getInstance("inmemoryfilesystem")
        private val REST_SETTING: Collection.Key? = KeyImpl.getInstance("restsettings")
        private val JAVA_SETTING: Collection.Key? = KeyImpl.getInstance("javasettings")
        private val SCOPE_CASCADING: Collection.Key? = KeyImpl.getInstance("scopeCascading")
        private val SEARCH_IMPLICIT_SCOPES: Collection.Key? = KeyImpl.getInstance("searchImplicitScopes")
        private val TYPE_CHECKING: Collection.Key? = KeyImpl.getInstance("typeChecking")
        private val CGI_READONLY: Collection.Key? = KeyImpl.getInstance("CGIReadOnly")
        private val SUPPRESS_CONTENT: Collection.Key? = KeyImpl.getInstance("suppressRemoteComponentContent")
        private val LOGS: Collection.Key? = KeyImpl.getInstance("logs")
        private val LOG: Collection.Key? = KeyImpl.getInstance("log")
        private val SESSION_COOKIE: Collection.Key? = KeyImpl.getInstance("sessioncookie")
        private val AUTH_COOKIE: Collection.Key? = KeyImpl.getInstance("authcookie")
        private val ENABLE_NULL_SUPPORT: Key? = KeyImpl.getInstance("enableNULLSupport")
        private val NULL_SUPPORT: Key? = KeyImpl.getInstance("nullSupport")
        private val PRECISE_MATH: Key? = KeyImpl.getInstance("preciseMath")
        private val PRECISION_EVAL: Key? = KeyImpl.getInstance("precisionEvaluate")
        private val PSQ: Key? = KeyImpl.getInstance("psq")
        private val PSQ_LONG: Key? = KeyImpl.getInstance("preservesinglequote")
        private val VAR_USAGE: Key? = KeyImpl.getInstance("varusage")
        private val VARIABLE_USAGE: Key? = KeyImpl.getInstance("variableusage")
        private val CACHED_AFTER: Key? = KeyImpl.getInstance("cachedAfter")
        private val BLOCKED_EXT_FOR_FILE_UPLOAD: Key? = KeyImpl.getInstance("blockedExtForFileUpload")
        private val XML_FEATURES: Key? = KeyImpl.getInstance("xmlFeatures")
        private val SEARCH_QUERIES: Key? = KeyImpl.getInstance("searchQueries")
        private val SEARCH_RESULTS: Key? = KeyImpl.getInstance("searchResults")
        private val REGEX: Key? = KeyImpl.getInstance("regex")
        private val ENGINE: Key? = KeyImpl.getInstance("engine")
        private val DIALECT: Key? = KeyConstants._dialect
        private val USE_JAVA_AS_REGEX_ENGINE: Key? = KeyImpl.getInstance("useJavaAsRegexEngine")
        private val initCacheConnections: Map<String?, CacheConnection?>? = ConcurrentHashMap<String?, CacheConnection?>()
        fun toCacheConnection(config: Config?, name: String?, data: Struct?, defaultValue: CacheConnection?): CacheConnection? {
            return try {
                toCacheConnection(config, name, data)
            } catch (e: Exception) {
                defaultValue
            }
        }

        @Throws(ApplicationException::class, CacheException::class, ClassException::class, BundleException::class)
        fun toCacheConnection(config: Config?, name: String?, data: Struct?): CacheConnection? {
            // class definition
            val className: String = Caster.toString(data.get(KeyConstants._class, null), null)
            if (StringUtil.isEmpty(className)) throw ApplicationException("missing key class in struct the defines a cachec connection")
            val cd: ClassDefinition = ClassDefinitionImpl(className, Caster.toString(data.get(KeyConstants._bundleName, null), null),
                    Caster.toString(data.get(KeyConstants._bundleVersion, null), null), config.getIdentification())
            val cc = CacheConnectionImpl(config, name, cd, Caster.toStruct(data.get(KeyConstants._custom, null), null),
                    Caster.toBooleanValue(data.get(KeyConstants._readonly, null), false), Caster.toBooleanValue(data.get(KeyConstants._storage, null), false))
            val id: String = cc.id()
            val icc: CacheConnection? = initCacheConnections!![id]
            if (icc != null) return icc
            try {
                val m: Method = cd.getClazz().getMethod("init", arrayOf<Class?>(Config::class.java, Array<String>::class.java, Array<Struct>::class.java))
                if (Modifier.isStatic(m.getModifiers())) m.invoke(null, arrayOf(config, arrayOf<String?>(cc.getName()), arrayOf(cc.getCustom()))) else LogUtil.log(ThreadLocalPageContext.getConfig(config), Log.LEVEL_ERROR, ModernApplicationContext::class.java.getName(), "method [init(Config,String[],Struct[]):void] for class [" + cd.toString().toString() + "] is not static")
            } catch (e: Exception) {
            }
            initCacheConnections.put(id, cc)
            return cc
        }

        private operator fun get(app: Component?, name: Key?, defaultValue: String?): Object? {
            val mem: Member = app.getMember(Component.ACCESS_PRIVATE, name, true, false) ?: return defaultValue
            return mem.getValue()
        }

        fun releaseInitCacheConnections() {
            if (initCacheConnections != null) {
                for (cc in initCacheConnections.values()) {
                    CacheUtil.releaseEL(cc)
                }
            }
        }
    }

    init {
        val ci: ConfigPro? = config as ConfigPro?
        setClientCookies = config.isClientCookies()
        setDomainCookies = config.isDomainCookies()
        setSessionManagement = config.isSessionManagement()
        setClientManagement = config.isClientManagement()
        sessionTimeout = config.getSessionTimeout()
        clientTimeout = config.getClientTimeout()
        requestTimeout = config.getRequestTimeout()
        applicationTimeout = config.getApplicationTimeout()
        scriptProtect = config.getScriptProtect()
        typeChecking = ci.getTypeChecking()
        allowCompression = ci.allowCompression()
        defaultDataSource = config.getDefaultDataSource()
        localMode = config.getLocalMode()
        locale = config.getLocale()
        timeZone = config.getTimeZone()
        webCharset = ci.getWebCharSet()
        resourceCharset = ci.getResourceCharSet()
        bufferOutput = ci.getBufferOutput()
        suppressContent = ci.isSuppressContent()
        sessionType = config.getSessionType()
        wstype = WS_TYPE_AXIS1
        cgiScopeReadonly = ci.getCGIScopeReadonly()
        fullNullSupport = ci.getFullNullSupport()
        queryPSQ = ci.getPSQL()
        queryCachedAfter = ci.getCachedAfterTimeRange()
        queryVarUsage = ci.getQueryVarUsage()
        proxyData = config.getProxyData()
        sessionCluster = config.getSessionCluster()
        clientCluster = config.getClientCluster()
        sessionStorage = ci.getSessionStorage()
        clientStorage = ci.getClientStorage()
        allowImplicidQueryCall = config.allowImplicidQueryCall()
        triggerComponentDataMember = config.getTriggerComponentDataMember()
        restSetting = config.getRestSetting()
        javaSettings = JavaSettingsImpl()
        component = cfc
        regex = ci.getRegex()
        preciseMath = ci.getPreciseMath()
        initAntiSamyPolicyResource(pc)
        if (antiSamyPolicyResource == null) antiSamyPolicyResource = (config as ConfigPro?).getAntiSamyPolicy()
        // read scope cascading
        initScopeCascading()
        initSameFieldAsArray(pc)
        initWebCharset(pc)
        initAllowImplicidQueryCall()
        pc.addPageSource(component.getPageSource(), true)
        try {

            /////////// ORM /////////////////////////////////
            reinitORM(pc)
            throwsErrorWhileInit.setValue(false)
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
            throwsErrorWhileInit.setValue(true)
            pc.removeLastPageSource(true)
        }
    }
}