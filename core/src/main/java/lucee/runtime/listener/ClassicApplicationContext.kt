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

import java.nio.charset.Charset

/**
 * This class resolves the Application settings that are defined in cfapplication tag attributes,
 * e.g. sessionManagement, localMode, etc.
 */
class ClassicApplicationContext : ApplicationContextSupport {
    private var name: String? = null
    private var setClientCookies = false
    private var setDomainCookies = false
    private var setSessionManagement = false
    private var setClientManagement = false
    private var sessionTimeout: TimeSpan? = null
    private var requestTimeout: TimeSpan? = null
    private var clientTimeout: TimeSpan? = null
    private var applicationTimeout: TimeSpan? = null
    private var loginStorage = -1
    private var clientstorage: String? = null
    private var sessionstorage: String? = null
    private var scriptProtect = 0
    private var typeChecking = false
    private var mappings: Array<Mapping?>?
    private var ctmappings: Array<Mapping?>?
    private var cmappings: Array<Mapping?>?
    private var funcDirs: List<Resource?>? = null
    private var bufferOutput = false
    private var secureJson = false
    private var secureJsonPrefix: String? = "//"
    private var isDefault = false
    private var defaultDataSource: Object? = null
    private var ormEnabled = false
    private var ormdatasource: Object? = null
    private var ormConfig: ORMConfiguration? = null
    private var s3: Properties? = null
    private var ftp: FTPConnectionData? = null
    private var localMode = 0
    private var locale: Locale? = null
    private var timeZone: TimeZone? = null
    private var webCharset: CharSet? = null
    private var resourceCharset: CharSet? = null
    private var sessionType: Short = 0
    private var sessionCluster = false
    private var clientCluster = false
    private var source: Resource? = null
    private var triggerComponentDataMember = false
    private var defaultCaches: Map<Integer?, String?>? = ConcurrentHashMap<Integer?, String?>()
    private var cacheConnections: Map<Collection.Key?, CacheConnection?>? = ConcurrentHashMap<Collection.Key?, CacheConnection?>()
    private var mailServers: Array<Server?>?
    private var sameFieldAsArrays: Map<Integer?, Boolean?>? = ConcurrentHashMap<Integer?, Boolean?>()
    private var restSettings: RestSettings? = null
    private var restCFCLocations: Array<Resource?>?
    private var antiSamyPolicy: Resource? = null
    private var javaSettings: JavaSettings? = null
    private var dataSources: Array<DataSource?>?
    private var onMissingTemplate: UDF? = null
    private var scopeCascading: Short = 0
    private var allowCompression = false
    private var suppressRemoteComponentContent = false
    private var wstype: Short = 0
    private var cgiScopeReadonly = false
    private var sessionCookie: SessionCookieData? = null
    private var authCookie: AuthCookieData? = null
    private var logs: Map<Key?, Pair<Log?, Struct?>?>? = null
    private var mailListener: Object? = null
    private var queryListener: TagListener? = null
    private var wsMaintainSession = false
    private var fullNullSupport = false
    private var serializationSettings: SerializationSettings? = SerializationSettings.DEFAULT
    private var queryPSQ = false
    private var queryVarUsage = 0
    private var proxyData: ProxyData? = null
    private var queryCachedAfter: TimeSpan? = null
    private var blockedExtForFileUpload: String? = null
    private var xmlFeatures: Struct? = null
    private var customAttrs: Map<Key?, Object?>? = null
    private var allowImplicidQueryCall = false
    private var regex: Regex? = null
    private var preciseMath = false

    /**
     * constructor of the class
     *
     * @param config
     */
    constructor(config: ConfigWeb?, name: String?, isDefault: Boolean, source: Resource?) : super(config) {
        this.name = name
        setClientCookies = config.isClientCookies()
        setDomainCookies = config.isDomainCookies()
        setSessionManagement = config.isSessionManagement()
        setClientManagement = config.isClientManagement()
        sessionTimeout = config.getSessionTimeout()
        requestTimeout = config.getRequestTimeout()
        clientTimeout = config.getClientTimeout()
        applicationTimeout = config.getApplicationTimeout()
        loginStorage = Scope.SCOPE_COOKIE
        scriptProtect = config.getScriptProtect()
        typeChecking = (config as ConfigPro?).getTypeChecking()
        allowCompression = (config as ConfigPro?).allowCompression()
        this.isDefault = isDefault
        defaultDataSource = config.getDefaultDataSource()
        localMode = config.getLocalMode()
        queryPSQ = config.getPSQL()
        queryVarUsage = (config as ConfigPro?).getQueryVarUsage()
        queryCachedAfter = (config as ConfigPro?).getCachedAfterTimeRange()
        locale = config.getLocale()
        timeZone = config.getTimeZone()
        fullNullSupport = config.getFullNullSupport()
        scopeCascading = config.getScopeCascadingType()
        allowImplicidQueryCall = config.allowImplicidQueryCall()
        webCharset = (config as ConfigPro?).getWebCharSet()
        resourceCharset = (config as ConfigPro?).getResourceCharSet()
        bufferOutput = (config as ConfigPro?).getBufferOutput()
        suppressRemoteComponentContent = (config as ConfigPro?).isSuppressContent()
        sessionType = config.getSessionType()
        sessionCluster = config.getSessionCluster()
        clientCluster = config.getClientCluster()
        clientstorage = (config as ConfigPro?).getClientStorage()
        sessionstorage = (config as ConfigPro?).getSessionStorage()
        this.source = source
        triggerComponentDataMember = config.getTriggerComponentDataMember()
        restSettings = config.getRestSetting()
        javaSettings = JavaSettingsImpl()
        wstype = WS_TYPE_AXIS1
        cgiScopeReadonly = (config as ConfigPro?).getCGIScopeReadonly()
        antiSamyPolicy = (config as ConfigPro?).getAntiSamyPolicy()
        regex = (config as ConfigPro?).getRegex()
        preciseMath = (config as ConfigPro?).getPreciseMath()
    }

    /**
     * Constructor of the class, only used by duplicate method
     */
    private constructor(config: ConfigWeb?) : super(config) {}

    fun duplicate(): ApplicationContext? {
        val dbl = ClassicApplicationContext(config)
        dbl._duplicate(this)
        dbl.name = name
        dbl.setClientCookies = setClientCookies
        dbl.setDomainCookies = setDomainCookies
        dbl.setSessionManagement = setSessionManagement
        dbl.setClientManagement = setClientManagement
        dbl.sessionTimeout = sessionTimeout
        dbl.requestTimeout = requestTimeout
        dbl.clientTimeout = clientTimeout
        dbl.applicationTimeout = applicationTimeout
        dbl.loginStorage = loginStorage
        dbl.clientstorage = clientstorage
        dbl.sessionstorage = sessionstorage
        dbl.scriptProtect = scriptProtect
        dbl.typeChecking = typeChecking
        dbl.mappings = mappings
        dbl.dataSources = dataSources
        dbl.ctmappings = ctmappings
        dbl.cmappings = cmappings
        dbl.funcDirs = funcDirs
        dbl.bufferOutput = bufferOutput
        dbl.allowCompression = allowCompression
        dbl.suppressRemoteComponentContent = suppressRemoteComponentContent
        dbl.wstype = wstype
        dbl.secureJson = secureJson
        dbl.secureJsonPrefix = secureJsonPrefix
        dbl.isDefault = isDefault
        dbl.defaultDataSource = defaultDataSource
        dbl.applicationtoken = applicationtoken
        dbl.cookiedomain = cookiedomain
        dbl.idletimeout = idletimeout
        dbl.localMode = localMode
        dbl.queryPSQ = queryPSQ
        dbl.queryVarUsage = queryVarUsage
        dbl.queryCachedAfter = queryCachedAfter
        dbl.locale = locale
        dbl.timeZone = timeZone
        dbl.fullNullSupport = fullNullSupport
        dbl.scopeCascading = scopeCascading
        dbl.allowImplicidQueryCall = allowImplicidQueryCall
        dbl.webCharset = webCharset
        dbl.resourceCharset = resourceCharset
        dbl.sessionType = sessionType
        dbl.triggerComponentDataMember = triggerComponentDataMember
        dbl.restSettings = restSettings
        dbl.defaultCaches = Duplicator.duplicateMap(defaultCaches, ConcurrentHashMap<Integer?, String?>(), false)
        dbl.cacheConnections = Duplicator.duplicateMap(cacheConnections, ConcurrentHashMap<Integer?, String?>(), false)
        dbl.mailServers = mailServers
        dbl.cachedWithinFile = Duplicator.duplicate(cachedWithinFile, false)
        dbl.cachedWithinFunction = Duplicator.duplicate(cachedWithinFunction, false)
        dbl.cachedWithinHTTP = Duplicator.duplicate(cachedWithinHTTP, false)
        dbl.cachedWithinInclude = Duplicator.duplicate(cachedWithinInclude, false)
        dbl.cachedWithinQuery = Duplicator.duplicate(cachedWithinQuery, false)
        dbl.cachedWithinResource = Duplicator.duplicate(cachedWithinResource, false)
        dbl.cachedWithinWS = Duplicator.duplicate(cachedWithinWS, false)
        dbl.sameFieldAsArrays = Duplicator.duplicateMap(sameFieldAsArrays, ConcurrentHashMap<Integer?, Boolean?>(), false)
        dbl.ormEnabled = ormEnabled
        dbl.ormConfig = ormConfig
        dbl.ormdatasource = ormdatasource
        dbl.sessionCluster = sessionCluster
        dbl.clientCluster = clientCluster
        dbl.preciseMath = preciseMath
        dbl.source = source
        dbl.cgiScopeReadonly = cgiScopeReadonly
        dbl.antiSamyPolicy = antiSamyPolicy
        dbl.sessionCookie = sessionCookie
        dbl.authCookie = authCookie
        return dbl
    }

    @Override
    fun getApplicationTimeout(): TimeSpan? {
        return applicationTimeout
    }

    /**
     * @param applicationTimeout The applicationTimeout to set.
     */
    @Override
    fun setApplicationTimeout(applicationTimeout: TimeSpan?) {
        this.applicationTimeout = applicationTimeout
    }

    @Override
    fun getLoginStorage(): Int {
        return loginStorage
    }

    /**
     * @param loginStorage The loginStorage to set.
     */
    @Override
    fun setLoginStorage(loginStorage: Int) {
        this.loginStorage = loginStorage
    }

    @Throws(ApplicationException::class)
    fun setLoginStorage(strLoginStorage: String?) {
        setLoginStorage(AppListenerUtil.translateLoginStorage(strLoginStorage))
    }

    @Override
    fun getName(): String? {
        return name
    }

    /**
     * @param name The name to set.
     */
    fun setName(name: String?) {
        this.name = name
    }

    @Override
    fun getSessionTimeout(): TimeSpan? {
        return sessionTimeout
    }

    /**
     * @param sessionTimeout The sessionTimeout to set.
     */
    @Override
    fun setSessionTimeout(sessionTimeout: TimeSpan?) {
        this.sessionTimeout = sessionTimeout
    }

    @Override
    fun getClientTimeout(): TimeSpan? {
        return clientTimeout
    }

    /**
     * @param sessionTimeout The sessionTimeout to set.
     */
    @Override
    fun setClientTimeout(clientTimeout: TimeSpan?) {
        this.clientTimeout = clientTimeout
    }

    @Override
    fun isSetClientCookies(): Boolean {
        return setClientCookies
    }

    /**
     * @param setClientCookies The setClientCookies to set.
     */
    @Override
    fun setSetClientCookies(setClientCookies: Boolean) {
        this.setClientCookies = setClientCookies
    }

    @Override
    fun isSetClientManagement(): Boolean {
        return setClientManagement
    }

    /**
     * @param setClientManagement The setClientManagement to set.
     */
    @Override
    fun setSetClientManagement(setClientManagement: Boolean) {
        this.setClientManagement = setClientManagement
    }

    @Override
    fun isSetDomainCookies(): Boolean {
        return setDomainCookies
    }

    /**
     * @param setDomainCookies The setDomainCookies to set.
     */
    @Override
    fun setSetDomainCookies(setDomainCookies: Boolean) {
        this.setDomainCookies = setDomainCookies
    }

    @Override
    fun isSetSessionManagement(): Boolean {
        return setSessionManagement
    }

    /**
     * @param setSessionManagement The setSessionManagement to set.
     */
    @Override
    fun setSetSessionManagement(setSessionManagement: Boolean) {
        this.setSessionManagement = setSessionManagement
    }

    @Override
    fun getClientstorage(): String? {
        return clientstorage
    }

    @Override
    fun getSessionstorage(): String? {
        return sessionstorage
    }

    /**
     * @param clientstorage The clientstorage to set.
     */
    @Override
    fun setClientstorage(clientstorage: String?) {
        if (StringUtil.isEmpty(clientstorage, true)) return
        this.clientstorage = clientstorage
    }

    @Override
    fun setSessionstorage(sessionstorage: String?) {
        if (StringUtil.isEmpty(sessionstorage, true)) return
        this.sessionstorage = sessionstorage
    }

    @Override
    fun hasName(): Boolean {
        return name != null
    }

    /**
     * @param scriptProtect The scriptProtect to set.
     */
    @Override
    fun setScriptProtect(scriptProtect: Int) {
        this.scriptProtect = scriptProtect
    }

    @Override
    fun getScriptProtect(): Int {
        // if(isDefault)print.err("get:"+scriptProtect);
        return scriptProtect
    }

    /**
     * @param scriptProtect The scriptProtect to set.
     */
    @Override
    fun setTypeChecking(typeChecking: Boolean) {
        this.typeChecking = typeChecking
    }

    @Override
    fun getTypeChecking(): Boolean {
        return typeChecking
    }

    @Override
    fun setMappings(mappings: Array<Mapping?>?) {
        if (mappings!!.size > 0) this.mappings = mappings
    }

    /**
     * @return the mappings
     */
    @Override
    fun getMappings(): Array<Mapping?>? {
        return mappings
    }

    @Override
    fun setCustomTagMappings(ctmappings: Array<Mapping?>?) {
        this.ctmappings = ctmappings
    }

    @Override
    fun getCustomTagMappings(): Array<Mapping?>? {
        return ctmappings
    }

    @Override
    fun setComponentMappings(cmappings: Array<Mapping?>?) {
        this.cmappings = cmappings
    }

    @Override
    fun getComponentMappings(): Array<Mapping?>? {
        return cmappings
    }

    @Override
    fun setSecureJson(secureJson: Boolean) {
        this.secureJson = secureJson
    }

    /**
     * @return the secureJson
     */
    @Override
    fun getSecureJson(): Boolean {
        return secureJson
    }

    @Override
    fun getBufferOutput(): Boolean {
        return bufferOutput
    }

    @Override
    fun setBufferOutput(bufferOutput: Boolean) {
        this.bufferOutput = bufferOutput
    }

    @Override
    fun setSecureJsonPrefix(secureJsonPrefix: String?) {
        this.secureJsonPrefix = secureJsonPrefix
    }

    /**
     * @return the secureJsonPrefix
     */
    @Override
    fun getSecureJsonPrefix(): String? {
        return secureJsonPrefix
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
    fun setDefaultDataSource(defaultDataSource: String?) {
        this.defaultDataSource = defaultDataSource
    }

    @Override
    fun setDefDataSource(defaultDataSource: Object?) {
        this.defaultDataSource = defaultDataSource
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
        return ormdatasource
    }

    @Override
    fun getORMConfiguration(): ORMConfiguration? {
        return ormConfig
    }

    @Override
    fun setORMConfiguration(config: ORMConfiguration?) {
        ormConfig = config
    }

    @Override
    fun setORMEnabled(ormEnabled: Boolean) {
        this.ormEnabled = ormEnabled
    }

    @Override
    fun getS3(): Properties? {
        if (s3 == null) s3 = PropertiesImpl()
        return s3
    }

    @Override
    override fun getFTP(): FTPConnectionData? {
        if (ftp == null) ftp = FTPConnectionData()
        return ftp
    }

    @Override
    fun getLocalMode(): Int {
        return localMode
    }

    @Override
    fun getLocale(): Locale? {
        return locale
    }

    @Override
    fun getTimeZone(): TimeZone? {
        return timeZone
    }

    @Override
    override fun getFullNullSupport(): Boolean {
        return fullNullSupport
    }

    @Override
    fun getWebCharset(): Charset? {
        return CharsetUtil.toCharset(webCharset)
    }

    fun getWebCharSet(): CharSet? {
        return webCharset
    }

    @Override
    fun getResourceCharset(): Charset? {
        return CharsetUtil.toCharset(resourceCharset)
    }

    fun getResourceCharSet(): CharSet? {
        return resourceCharset
    }

    /**
     * @param localMode the localMode to set
     */
    @Override
    fun setLocalMode(localMode: Int) {
        this.localMode = localMode
    }

    @Override
    fun setLocale(locale: Locale?) {
        this.locale = locale
    }

    @Override
    fun setTimeZone(timeZone: TimeZone?) {
        this.timeZone = timeZone
    }

    @Override
    override fun setFullNullSupport(fullNullSupport: Boolean) {
        this.fullNullSupport = fullNullSupport
    }

    @Override
    fun setWebCharset(webCharset: Charset?) {
        this.webCharset = CharsetUtil.toCharSet(webCharset)
    }

    @Override
    fun setResourceCharset(resourceCharset: Charset?) {
        this.resourceCharset = CharsetUtil.toCharSet(resourceCharset)
    }

    /**
     * @return the sessionType
     */
    @Override
    fun getSessionType(): Short {
        return sessionType
    }

    /**
     * @return the sessionType
     */
    @Override
    fun setSessionType(sessionType: Short) {
        this.sessionType = sessionType
    }

    /**
     * @return the sessionCluster
     */
    @Override
    fun getSessionCluster(): Boolean {
        return sessionCluster
    }

    /**
     * @param sessionCluster the sessionCluster to set
     */
    @Override
    fun setSessionCluster(sessionCluster: Boolean) {
        this.sessionCluster = sessionCluster
    }

    /**
     * @return the clientCluster
     */
    @Override
    fun getClientCluster(): Boolean {
        return clientCluster
    }

    /**
     * @param clientCluster the clientCluster to set
     */
    @Override
    fun setClientCluster(clientCluster: Boolean) {
        this.clientCluster = clientCluster
    }

    @Override
    fun setS3(s3: Properties?) {
        this.s3 = s3
    }

    @Override
    override fun setFTP(ftp: FTPConnectionData?) {
        this.ftp = ftp
    }

    @Override
    fun setORMDatasource(ormdatasource: String?) {
        this.ormdatasource = ormdatasource
    }

    @Override
    fun setORMDataSource(ormdatasource: Object?) {
        this.ormdatasource = ormdatasource
    }

    @Override
    @Throws(PageException::class)
    fun reinitORM(pc: PageContext?) {
        // do nothing
    }

    @Override
    fun getSource(): Resource? {
        return source
    }

    @Override
    fun getTriggerComponentDataMember(): Boolean {
        return triggerComponentDataMember
    }

    @Override
    fun setTriggerComponentDataMember(triggerComponentDataMember: Boolean) {
        this.triggerComponentDataMember = triggerComponentDataMember
    }

    @Override
    fun setDefaultCacheName(type: Int, name: String?) {
        if (StringUtil.isEmpty(name, true)) return
        defaultCaches.put(type, name.trim())
    }

    @Override
    fun getDefaultCacheName(type: Int): String? {
        return defaultCaches!![type]
    }

    @Override
    override fun setCacheConnection(cacheName: String?, value: CacheConnection?) {
        if (StringUtil.isEmpty(cacheName, true)) return
        cacheConnections.put(KeyImpl.init(cacheName), value)
    }

    @Override
    override fun getCacheConnection(cacheName: String?, defaultValue: CacheConnection?): CacheConnection? {
        return cacheConnections!![KeyImpl.init(cacheName)]
    }

    @Override
    override fun getCacheConnectionNames(): Array<Key?>? {
        return if (cacheConnections == null) arrayOfNulls<Key?>(0) else cacheConnections.keySet().toArray(arrayOfNulls<Key?>(cacheConnections!!.size()))
    }

    @Override
    override fun setMailServers(servers: Array<Server?>?) {
        mailServers = servers
    }

    @Override
    override fun getMailServers(): Array<Server?>? {
        return mailServers
    }

    fun setSameFieldAsArray(pc: PageContext?, scope: Int, sameFieldAsArray: Boolean) {
        sameFieldAsArrays.put(scope, sameFieldAsArray)
        if (Scope.SCOPE_URL === scope) pc.urlScope().reinitialize(this) else pc.formScope().reinitialize(this)
    }

    @Override
    fun getSameFieldAsArray(scope: Int): Boolean {
        val b = sameFieldAsArrays!![scope] ?: return false
        return b.booleanValue()
    }

    @Override
    fun getRestSettings(): RestSettings? {
        return restSettings
    }

    fun setRestSettings(restSettings: RestSettings?) {
        this.restSettings = restSettings
    }

    fun setRestCFCLocations(restCFCLocations: Array<Resource?>?) {
        this.restCFCLocations = restCFCLocations
    }

    @Override
    fun getRestCFCLocations(): Array<Resource?>? {
        return restCFCLocations
    }

    @Override
    fun getJavaSettings(): JavaSettings? {
        return javaSettings
    }

    @Override
    override fun setJavaSettings(javaSettings: JavaSettings?) {
        this.javaSettings = javaSettings
    }

    @Override
    fun getDataSources(): Array<DataSource?>? {
        return dataSources
    }

    @Override
    fun setDataSources(dataSources: Array<DataSource?>?) {
        if (!ArrayUtil.isEmpty(dataSources)) this.dataSources = dataSources
    }

    fun setOnMissingTemplate(onMissingTemplate: UDF?) {
        this.onMissingTemplate = onMissingTemplate
    }

    fun getOnMissingTemplate(): UDF? {
        return onMissingTemplate
    }

    @Override
    fun getScopeCascading(): Short {
        return scopeCascading
    }

    @Override
    fun setScopeCascading(scopeCascading: Short) {
        this.scopeCascading = scopeCascading
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
    fun getAllowCompression(): Boolean {
        return allowCompression
    }

    @Override
    fun setAllowCompression(allowCompression: Boolean) {
        this.allowCompression = allowCompression
    }

    @Override
    fun getRequestTimeout(): TimeSpan? {
        return requestTimeout
    }

    @Override
    fun setRequestTimeout(requestTimeout: TimeSpan?) {
        this.requestTimeout = requestTimeout
    }

    @Override
    fun getCustomType(strType: String?): CustomType? {
        // not supported
        return null
    }

    @Override
    fun getSuppressContent(): Boolean {
        return suppressRemoteComponentContent
    }

    @Override
    fun setSuppressContent(suppressContent: Boolean) {
        suppressRemoteComponentContent = suppressContent
    }

    @Override
    fun getWSType(): Short {
        return wstype
    }

    @Override
    fun setWSType(wstype: Short) {
        this.wstype = wstype
    }

    @Override
    fun getCGIScopeReadonly(): Boolean {
        return cgiScopeReadonly
    }

    @Override
    fun setCGIScopeReadonly(cgiScopeReadonly: Boolean) {
        this.cgiScopeReadonly = cgiScopeReadonly
    }

    @Override
    override fun getAntiSamyPolicyResource(): Resource? {
        return antiSamyPolicy
    }

    @Override
    override fun setAntiSamyPolicyResource(antiSamyPolicy: Resource?) {
        this.antiSamyPolicy = antiSamyPolicy
    }

    @Override
    override fun getSessionCookie(): SessionCookieData? {
        return sessionCookie
    }

    @Override
    override fun setSessionCookie(data: SessionCookieData?) {
        sessionCookie = data
    }

    @Override
    override fun getAuthCookie(): AuthCookieData? {
        return authCookie
    }

    @Override
    override fun setAuthCookie(data: AuthCookieData?) {
        authCookie = data
    }

    @Override
    override fun getLogNames(): Collection<Key?>? {
        return if (logs == null) HashSet<Collection.Key?>() else logs.keySet()
    }

    @Override
    override fun setLoggers(logs: Map<Key?, Pair<Log?, Struct?>?>?) {
        this.logs = logs
    }

    @Override
    override fun getLog(name: String?): Log? {
        if (logs == null) return null
        val pair: Pair<Log?, Struct?> = logs!![KeyImpl.init(StringUtil.emptyIfNull(name))] ?: return null
        return pair.getName()
    }

    @Override
    override fun getLogMetaData(name: String?): Struct? {
        if (logs == null) return null
        val pair: Pair<Log?, Struct?> = logs!![KeyImpl.init(StringUtil.emptyIfNull(name))] ?: return null
        return pair.getValue().duplicate(false) as Struct
    }

    @Override
    override fun getMailListener(): Object? {
        return mailListener
    }

    @Override
    override fun setMailListener(listener: Object?) {
        mailListener = listener
    }

    @Override
    override fun getQueryListener(): TagListener? {
        return queryListener
    }

    @Override
    override fun setQueryListener(listener: TagListener?) {
        queryListener = listener
    }

    @Override
    override fun getSerializationSettings(): SerializationSettings? {
        return serializationSettings
    }

    @Override
    override fun setSerializationSettings(settings: SerializationSettings?) {
        serializationSettings = settings
    }

    @Override
    override fun getWSMaintainSession(): Boolean {
        return wsMaintainSession
    }

    @Override
    override fun setWSMaintainSession(wsMaintainSession: Boolean) {
        this.wsMaintainSession = wsMaintainSession
    }

    @Override
    override fun getFunctionDirectories(): List<Resource?>? {
        return funcDirs
    }

    @Override
    override fun setFunctionDirectories(resources: List<Resource?>?) {
        funcDirs = resources
    }

    @Override
    override fun getQueryPSQ(): Boolean {
        return queryPSQ
    }

    @Override
    override fun setQueryPSQ(psq: Boolean) {
        queryPSQ = psq
    }

    @Override
    override fun getQueryVarUsage(): Int {
        return queryVarUsage
    }

    @Override
    override fun setQueryVarUsage(varUsage: Int) {
        queryVarUsage = varUsage
    }

    @Override
    override fun getQueryCachedAfter(): TimeSpan? {
        return queryCachedAfter
    }

    @Override
    override fun setQueryCachedAfter(ts: TimeSpan?) {
        queryCachedAfter = ts
    }

    @Override
    override fun getProxyData(): ProxyData? {
        return proxyData
    }

    @Override
    override fun setProxyData(data: ProxyData?) {
        proxyData = data
    }

    fun setBlockedextforfileupload(blockedExtForFileUpload: String?) {
        this.blockedExtForFileUpload = blockedExtForFileUpload
    }

    @Override
    override fun getBlockedExtForFileUpload(): String? {
        return blockedExtForFileUpload
    }

    @Override
    override fun getXmlFeatures(): Struct? {
        return xmlFeatures
    }

    @Override
    override fun setXmlFeatures(xmlFeatures: Struct?) {
        this.xmlFeatures = xmlFeatures
    }

    fun setCustomAttributes(customAttrs: Map<Key?, Object?>?) {
        this.customAttrs = customAttrs
    }

    fun getCustomAttributes(): Map<Key?, Object?>? {
        return customAttrs
    }

    @Override
    override fun getRegex(): Regex? {
        return regex
    }

    @Override
    override fun setRegex(regex: Regex?) {
        this.regex = regex
    }

    @Override
    override fun getPreciseMath(): Boolean {
        return preciseMath
    }

    @Override
    override fun setPreciseMath(preciseMath: Boolean) {
        this.preciseMath = preciseMath
    }

    companion object {
        private const val serialVersionUID = 940663152793150953L
    }
}