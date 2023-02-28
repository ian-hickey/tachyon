/**
 * Copyright (c) 2023, TachyonCFML.org
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
package tachyon.runtime.tag

import java.util.HashMap

/**
 * Defines scoping for a CFML application, enables or disables storing client variables, and
 * specifies a client variable storage mechanism. By default, client variables are disabled. Also,
 * enables session variables and sets timeouts for session and application variables. Session and
 * application variables are stored in memory.
 *
 *
 *
 */
class Application : TagImpl(), DynamicAttributes {
    private var setClientCookies: Boolean? = null
    private var setDomainCookies: Boolean? = null
    private var setSessionManagement: Boolean? = null
    private var clientstorage: String? = null
    private var sessionstorage: String? = null
    private var setClientManagement: Boolean? = null
    private var applicationTimeout: TimeSpan? = null
    private var sessionTimeout: TimeSpan? = null
    private var clientTimeout: TimeSpan? = null
    private var requestTimeout: TimeSpan? = null
    private var mappings: Array<Mapping?>?
    private var customTagMappings: Array<Mapping?>?
    private var componentMappings: Array<Mapping?>?
    private var secureJsonPrefix: String? = null
    private var bufferOutput: Boolean? = null
    private var secureJson: Boolean? = null
    private var scriptrotect: String? = null
    private var typeChecking: Boolean? = null
    private var datasource: Object? = null
    private var defaultdatasource: Object? = null
    private var loginstorage: Int = Scope.SCOPE_UNDEFINED

    // ApplicationContextImpl appContext;
    private var name: String? = ""
    private var action = ACTION_CREATE
    private var localMode = -1
    private var mailListener: Object? = null
    private var queryListener: TagListener? = null
    private var serializationSettings: SerializationSettings? = null
    private var locale: Locale? = null
    private var timeZone: TimeZone? = null
    private var nullSupport: Boolean? = null
    private var enableNULLSupport: Boolean? = null
    private var queryPSQ: Boolean? = null
    private var queryVarUsage = 0
    private var queryCachedAfter: TimeSpan? = null
    private var webCharset: CharSet? = null
    private var resourceCharset: CharSet? = null
    private var sessionType: Short = -1
    private var wsType: Short = -1
    private var sessionCluster: Boolean? = null
    private var clientCluster: Boolean? = null
    private var compression: Boolean? = null
    private var ormenabled: Boolean? = null
    private var ormsettings: Struct? = null
    private var tag: Struct? = null
    private var s3: Struct? = null
    private var ftp: Struct? = null
    private var triggerDataMember: Boolean? = null
    private var cacheFunction: String? = null
    private var cacheQuery: String? = null
    private var cacheTemplate: String? = null
    private var cacheInclude: String? = null
    private var cacheObject: String? = null
    private var cacheResource: String? = null
    private var cacheHTTP: String? = null
    private var cacheFile: String? = null
    private var cacheWebservice: String? = null
    private var antiSamyPolicyResource: Resource? = null
    private var datasources: Struct? = null
    private var logs: Struct? = null
    private var mails: Array? = null
    private var caches: Struct? = null
    private var onmissingtemplate: UDF? = null
    private var scopeCascading: Short = -1
    private var searchQueries: Boolean? = null
    private var suppress: Boolean? = null
    private var cgiReadOnly: Boolean? = null
    private var preciseMath: Boolean? = null
    private var sessionCookie: SessionCookieData? = null
    private var authCookie: AuthCookieData? = null
    private var functionpaths: Object? = null
    private var proxy: Struct? = null
    private var blockedExtForFileUpload: String? = null
    private var javaSettings: Struct? = null
    private var xmlFeatures: Struct? = null
    private var dynAttrs: Map<Key?, Object?>? = null
    private var regex: Regex? = null
    @Override
    fun release() {
        super.release()
        setClientCookies = null
        setDomainCookies = null
        setSessionManagement = null
        clientstorage = null
        sessionstorage = null
        setClientManagement = null
        sessionTimeout = null
        clientTimeout = null
        requestTimeout = null
        applicationTimeout = null
        mappings = null
        customTagMappings = null
        componentMappings = null
        bufferOutput = null
        secureJson = null
        secureJsonPrefix = null
        typeChecking = null
        suppress = null
        loginstorage = Scope.SCOPE_UNDEFINED
        scriptrotect = null
        functionpaths = null
        proxy = null
        datasource = null
        defaultdatasource = null
        datasources = null
        logs = null
        mails = null
        caches = null
        name = ""
        action = ACTION_CREATE
        localMode = -1
        mailListener = null
        queryListener = null
        serializationSettings = null
        locale = null
        timeZone = null
        nullSupport = null
        enableNULLSupport = null
        queryPSQ = null
        queryVarUsage = 0
        queryCachedAfter = null
        webCharset = null
        resourceCharset = null
        sessionType = -1
        wsType = -1
        sessionCluster = null
        clientCluster = null
        compression = null
        ormenabled = null
        ormsettings = null
        tag = null
        s3 = null
        ftp = null
        // appContext=null;
        triggerDataMember = null
        cgiReadOnly = null
        preciseMath = null
        cacheFunction = null
        cacheQuery = null
        cacheTemplate = null
        cacheObject = null
        cacheResource = null
        cacheInclude = null
        cacheHTTP = null
        cacheFile = null
        cacheWebservice = null
        antiSamyPolicyResource = null
        onmissingtemplate = null
        scopeCascading = -1
        searchQueries = null
        authCookie = null
        sessionCookie = null
        blockedExtForFileUpload = null
        javaSettings = null
        xmlFeatures = null
        dynAttrs = null
        regex = null
    }

    @Override
    fun setDynamicAttribute(uri: String?, localName: String?, value: Object?) {
        setDynamicAttribute(uri, KeyImpl.init(localName), value)
    }

    fun setDynamicAttribute(uri: String?, localName: Key?, value: Object?) {
        if (dynAttrs == null) dynAttrs = HashMap<Key?, Object?>()
        dynAttrs.put(localName, value)
    }

    /**
     * set the value setclientcookies Yes or No. Yes enables client cookies. Default is Yes. If you set
     * this attribute to "No", CFML does not automatically send the CFID and CFTOKEN cookies to the
     * client browser; you must manually code CFID and CFTOKEN on the URL for every page that uses
     * Session or Client variables.
     *
     * @param setClientCookies value to set
     */
    fun setSetclientcookies(setClientCookies: Boolean) {
        this.setClientCookies = if (setClientCookies) Boolean.TRUE else Boolean.FALSE
        // getAppContext().setSetClientCookies(setClientCookies);
    }

    /**
     * set the value setdomaincookies Yes or No. Sets the CFID and CFTOKEN cookies for a domain, not
     * just a single host. Applications that are running on clusters must set this value to Yes. The
     * default is No.
     *
     * @param setDomainCookies value to set
     */
    fun setSetdomaincookies(setDomainCookies: Boolean) {
        this.setDomainCookies = if (setDomainCookies) Boolean.TRUE else Boolean.FALSE
        // getAppContext().setSetDomainCookies(setDomainCookies);
    }

    /**
     * set the value sessionmanagement Yes or No. Yes enables session variables. Default is No.
     *
     * @param setSessionManagement value to set
     */
    fun setSessionmanagement(setSessionManagement: Boolean) {
        this.setSessionManagement = if (setSessionManagement) Boolean.TRUE else Boolean.FALSE
        // getAppContext().setSetSessionManagement(setSessionManagement);
    }

    fun setSessioncookie(data: Struct?) {
        sessionCookie = AppListenerUtil.toSessionCookie(pageContext.getConfig(), data)
    }

    fun setAuthcookie(data: Struct?) {
        authCookie = AppListenerUtil.toAuthCookie(pageContext.getConfig(), data)
    }

    fun setBlockedextforfileupload(blockedExt: String?) {
        blockedExtForFileUpload = blockedExt
    }

    fun setSearchresults(searchQueries: Boolean) {
        this.searchQueries = searchQueries
    }

    /**
     * @param datasource the datasource to set
     * @throws PageException
     */
    @Throws(PageException::class)
    fun setDatasource(datasource: Object?) {
        this.datasource = AppListenerUtil.toDefaultDatasource(pageContext.getConfig(), datasource, ThreadLocalPageContext.getLog(pageContext, "application"))
    }

    @Throws(PageException::class)
    fun setDefaultdatasource(defaultdatasource: Object?) {
        this.defaultdatasource = AppListenerUtil.toDefaultDatasource(pageContext.getConfig(), defaultdatasource, ThreadLocalPageContext.getLog(pageContext, "application"))
    }

    fun setDatasources(datasources: Struct?) {
        this.datasources = datasources
    }

    fun setLogs(logs: Struct?) {
        this.logs = logs
    }

    fun setMails(mails: Array?) {
        this.mails = mails
    }

    fun setCaches(caches: Struct?) {
        this.caches = caches
    }

    @Throws(ApplicationException::class)
    fun setLocalmode(strLocalMode: String?) {
        localMode = AppListenerUtil.toLocalMode(strLocalMode)
    }

    @Throws(ApplicationException::class)
    fun setMaillistener(mailListener: Object?) {
        this.mailListener = mailListener
    }

    @Throws(ApplicationException::class)
    fun setQuerylistener(listener: Object?) {
        queryListener = Query.toTagListener(listener)
    }

    @Throws(ApplicationException::class)
    fun setSerializationsettings(sct: Struct?) {
        if (sct == null) return
        serializationSettings = SerializationSettings.toSerializationSettings(sct)
    }

    fun setTimezone(tz: TimeZone?) {
        if (tz == null) return
        timeZone = tz
    }

    fun setNullsupport(nullSupport: Boolean) {
        this.nullSupport = nullSupport
    }

    fun setEnablenullsupport(enableNULLSupport: Boolean) {
        this.enableNULLSupport = enableNULLSupport
    }

    @Throws(ApplicationException::class)
    fun setVariableusage(varUsage: String?) {
        queryVarUsage = AppListenerUtil.toVariableUsage(varUsage)
    }

    @Throws(ApplicationException::class)
    fun setCachedafter(ts: TimeSpan?) {
        queryCachedAfter = ts
    }

    fun setPsq(psq: Boolean) {
        queryPSQ = psq
    }

    @Throws(ApplicationException::class)
    fun setScopecascading(scopeCascading: String?) {
        if (StringUtil.isEmpty(scopeCascading)) return
        val NULL: Short = -1
        val tmp: Short = ConfigWebUtil.toScopeCascading(scopeCascading, NULL)
        if (tmp == NULL) throw ApplicationException("invalid value ($scopeCascading) for attribute [ScopeCascading], valid values are [strict,small,standard]")
        this.scopeCascading = tmp
    }

    @Throws(ApplicationException::class)
    fun setSearchQueries(searchQueries: Boolean) {
        this.searchQueries = searchQueries
    }

    @Throws(ApplicationException::class)
    fun setSearchimplicitscopes(searchImplicitScopes: Boolean) {
        val tmp: Short = ConfigWebUtil.toScopeCascading(searchImplicitScopes)
        scopeCascading = tmp
    }

    fun setWebcharset(charset: String?) {
        if (StringUtil.isEmpty(charset)) return
        webCharset = CharsetUtil.toCharSet(charset)
    }

    fun setResourcecharset(charset: String?) {
        if (StringUtil.isEmpty(charset)) return
        resourceCharset = CharsetUtil.toCharSet(charset)
    }

    fun setLocale(locale: Locale?) {
        if (locale == null) return
        this.locale = locale
    }

    /**
     * set the value clientstorage Specifies how the engine stores client variables
     *
     * @param clientstorage value to set
     */
    fun setClientstorage(clientstorage: String?) {
        this.clientstorage = clientstorage
    }

    fun setSessionstorage(sessionstorage: String?) {
        this.sessionstorage = sessionstorage
    }

    /**
     * set the value clientmanagement Yes or No. Enables client variables. Default is No.
     *
     * @param setClientManagement value to set
     */
    fun setClientmanagement(setClientManagement: Boolean) {
        this.setClientManagement = if (setClientManagement) Boolean.TRUE else Boolean.FALSE
        // getAppContext().setSetClientManagement(setClientManagement);
    }

    /**
     * set the value sessiontimeout Enter the CreateTimeSpan function and values in days, hours,
     * minutes, and seconds, separated by commas, to specify the lifespan of session variables.
     *
     * @param sessionTimeout value to set
     */
    fun setSessiontimeout(sessionTimeout: TimeSpan?) {
        this.sessionTimeout = sessionTimeout
    }

    @Throws(ApplicationException::class)
    fun setSessiontype(sessionType: String?) {
        this.sessionType = AppListenerUtil.toSessionType(sessionType)
    }

    @Throws(ApplicationException::class)
    fun setWstype(wstype: String?) {
        wsType = AppListenerUtil.toWSType(wstype)
    }

    fun setClientcluster(clientCluster: Boolean) {
        this.clientCluster = clientCluster
    }

    fun setSessioncluster(sessionCluster: Boolean) {
        this.sessionCluster = sessionCluster
    }

    fun setClienttimeout(clientTimeout: TimeSpan?) {
        this.clientTimeout = clientTimeout
    }

    fun setRequesttimeout(requestTimeout: TimeSpan?) {
        this.requestTimeout = requestTimeout
    }

    fun setCachefunction(cacheFunction: String?) {
        if (StringUtil.isEmpty(cacheFunction, true)) return
        this.cacheFunction = cacheFunction.trim()
    }

    fun setCachequery(cacheQuery: String?) {
        if (StringUtil.isEmpty(cacheQuery, true)) return
        this.cacheQuery = cacheQuery.trim()
    }

    fun setCachetemplate(cacheTemplate: String?) {
        if (StringUtil.isEmpty(cacheTemplate, true)) return
        this.cacheTemplate = cacheTemplate.trim()
    }

    fun setCacheinclude(cacheInclude: String?) {
        if (StringUtil.isEmpty(cacheInclude, true)) return
        this.cacheInclude = cacheInclude.trim()
    }

    fun setCacheobject(cacheObject: String?) {
        if (StringUtil.isEmpty(cacheObject, true)) return
        this.cacheObject = cacheObject.trim()
    }

    fun setCacheresource(cacheResource: String?) {
        if (StringUtil.isEmpty(cacheResource, true)) return
        this.cacheResource = cacheResource.trim()
    }

    fun setCachehttp(cacheHTTP: String?) {
        if (StringUtil.isEmpty(cacheHTTP, true)) return
        this.cacheHTTP = cacheHTTP.trim()
    }

    fun setCachefile(cacheFile: String?) {
        if (StringUtil.isEmpty(cacheFile, true)) return
        this.cacheFile = cacheFile.trim()
    }

    fun setCachewebservice(cacheWebservice: String?) {
        if (StringUtil.isEmpty(cacheWebservice, true)) return
        this.cacheWebservice = cacheWebservice.trim()
    }

    fun setCompression(compress: Boolean) {
        compression = compress
    }

    @Throws(ExpressionException::class)
    fun setAntiSamyPolicyResource(strAntiSamyPolicyResource: String?) {
        antiSamyPolicyResource = ResourceUtil.toResourceExisting(pageContext, strAntiSamyPolicyResource)
    }

    fun setTriggerdatamember(triggerDataMember: Boolean) {
        this.triggerDataMember = if (triggerDataMember) Boolean.TRUE else Boolean.FALSE
    }

    fun setInvokeimplicitaccessor(invokeimplicitaccessor: Boolean) {
        setTriggerdatamember(invokeimplicitaccessor)
    }

    /**
     * @param ormenabled the ormenabled to set
     */
    fun setOrmenabled(ormenabled: Boolean) {
        this.ormenabled = ormenabled
    }

    /**
     * @param ormsettings the ormsettings to set
     */
    fun setOrmsettings(ormsettings: Struct?) {
        this.ormsettings = ormsettings
    }

    fun setTag(tag: Struct?) {
        this.tag = tag
    }

    /**
     * @param s3 the s3 to set
     */
    fun setS3(s3: Struct?) {
        this.s3 = s3
    }

    /**
     * @param s3 the s3 to set
     */
    fun setFtp(ftp: Struct?) {
        this.ftp = ftp
    }

    /**
     * set the value applicationtimeout Enter the CreateTimeSpan function and values in days, hours,
     * minutes, and seconds, separated by commas, to specify the lifespan of application variables.
     *
     * @param applicationTimeout value to set
     */
    fun setApplicationtimeout(applicationTimeout: TimeSpan?) {
        this.applicationTimeout = applicationTimeout
        // getAppContext().setApplicationTimeout(applicationTimeout);
    }

    /**
     * set the value name The name of your application. This name can be up to 64 characters long.
     * Required for application and session variables, optional for client variables
     *
     * @param name value to set
     */
    fun setName(name: String?) {
        this.name = name
    }

    @Throws(ApplicationException::class)
    fun setAction(strAction: String?) {
        var strAction = strAction
        strAction = strAction.toLowerCase()
        action = if (strAction!!.equals("create")) ACTION_CREATE else if (strAction.equals("update")) ACTION_UPDATE else throw ApplicationException("invalid action definition [$strAction] for tag application, valid values are [create,update]")
    }

    @Throws(PageException::class)
    fun setMappings(mappings: Struct?) {
        this.mappings = AppListenerUtil.toMappings(pageContext.getConfig(), mappings, source)
        // getAppContext().setMappings(AppListenerUtil.toMappings(pageContext, mappings));
    }

    @Throws(PageException::class)
    fun setCustomtagpaths(mappings: Object?) {
        customTagMappings = AppListenerUtil.toCustomTagMappings(pageContext.getConfig(), mappings, source)
    }

    @Throws(PageException::class)
    fun setComponentpaths(mappings: Object?) {
        componentMappings = AppListenerUtil.toComponentMappings(pageContext.getConfig(), mappings, source)
    }

    fun setFunctionpaths(functionpaths: Object?) {
        this.functionpaths = functionpaths
    }

    fun setJavasettings(javaSettings: Struct?) {
        this.javaSettings = javaSettings
    }

    fun setSecurejsonprefix(secureJsonPrefix: String?) {
        this.secureJsonPrefix = secureJsonPrefix
        // getAppContext().setSecureJsonPrefix(secureJsonPrefix);
    }

    fun setSecurejson(secureJson: Boolean) {
        this.secureJson = if (secureJson) Boolean.TRUE else Boolean.FALSE
        // getAppContext().setSecureJson(secureJson);
    }

    fun setBufferoutput(bufferOutput: Boolean) {
        this.bufferOutput = if (bufferOutput) Boolean.TRUE else Boolean.FALSE
        // getAppContext().setSecureJson(secureJson);
    }

    /**
     * @param loginstorage The loginstorage to set.
     * @throws ApplicationException
     */
    @Throws(ApplicationException::class)
    fun setLoginstorage(loginstorage: String?) {
        var loginstorage = loginstorage
        loginstorage = loginstorage.toLowerCase()
        if (loginstorage!!.equals("session")) this.loginstorage = Scope.SCOPE_SESSION else if (loginstorage.equals("cookie")) this.loginstorage = Scope.SCOPE_COOKIE else throw ApplicationException("invalid loginStorage definition [$loginstorage] for tag application, valid values are [session,cookie]")
    }

    /**
     * @param scriptrotect the scriptrotect to set
     */
    fun setScriptprotect(strScriptrotect: String?) {
        scriptrotect = strScriptrotect
    }

    fun setProxy(proxy: Struct?) {
        this.proxy = proxy
    }

    fun setTypechecking(typeChecking: Boolean) {
        this.typeChecking = typeChecking
    }

    fun setSuppressremotecomponentcontent(suppress: Boolean) {
        this.suppress = suppress
    }

    @Throws(PageException::class)
    fun setOnmissingtemplate(oUDF: Object?) {
        onmissingtemplate = Caster.toFunction(oUDF)
    }

    fun setCgireadonly(cgiReadOnly: Boolean) {
        this.cgiReadOnly = cgiReadOnly
    }

    fun setPrecisemath(preciseMath: Boolean) {
        this.preciseMath = preciseMath
    }

    fun setXmlfeatures(xmlFeatures: Struct?) {
        this.xmlFeatures = xmlFeatures
    }

    @Throws(PageException::class)
    fun setRegex(data: Object?) {
        if (Decision.isSimpleValue(data)) {
            regex = RegexFactory.toRegex(RegexFactory.toType(Caster.toString(data)), null)
        } else {
            val sct: Struct = Caster.toStruct(data)
            var o: Object = sct.get(KeyConstants._type, null)
            if (o == null) o = sct.get("engine", null)
            if (o == null) o = sct.get("dialect", null)
            if (o != null) {
                regex = RegexFactory.toRegex(RegexFactory.toType(Caster.toString(o)), null)
            }
        }
    }

    @Override
    @Throws(PageException::class)
    fun doStartTag(): Int {
        var ac: ApplicationContext? = null
        var initORM = false
        if (action == ACTION_UPDATE) {
            ac = pageContext.getApplicationContext()
            // no update because the current context has a different name
            if (!StringUtil.isEmpty(name) && !name.equalsIgnoreCase(ac.getName())) ac = null else {
                initORM = set(ac, true)
                pageContext.setApplicationContext(ac) // we need to make this, so Tachyon does not miss any change
            }
        }
        // if we do not update we have to create a new one
        if (ac == null) {
            val ps: PageSource = pageContext.getCurrentPageSource(null)
            ac = ClassicApplicationContext(pageContext.getConfig(), name, false, if (ps == null) null else ps.getResourceTranslated(pageContext))
            initORM = set(ac, false)
            pageContext.setApplicationContext(ac)
        }

        // scope cascading
        if ((pageContext.undefinedScope() as UndefinedImpl).getScopeCascadingType() !== ac.getScopeCascading()) {
            pageContext.undefinedScope().initialize(pageContext)
        }

        // ORM
        if (initORM) ORMUtil.resetEngine(pageContext, false)
        return SKIP_BODY
    }

    @get:Throws(PageException::class)
    private val source: Resource?
        private get() {
            val curr: PageSource = pageContext.getCurrentPageSource() ?: return null
            return ResourceUtil.getResource(pageContext, curr)
        }

    @Throws(PageException::class)
    private operator fun set(ac: ApplicationContext?, update: Boolean): Boolean {
        if (dynAttrs != null && ac is ClassicApplicationContext) {
            val cac: ClassicApplicationContext? = ac as ClassicApplicationContext?
            cac.setCustomAttributes(dynAttrs)
            dynAttrs = null
        }
        if (applicationTimeout != null) ac.setApplicationTimeout(applicationTimeout)
        if (sessionTimeout != null) ac.setSessionTimeout(sessionTimeout)
        if (clientTimeout != null) ac.setClientTimeout(clientTimeout)
        if (requestTimeout != null) ac.setRequestTimeout(requestTimeout)
        if (clientstorage != null) {
            ac.setClientstorage(clientstorage)
        }
        if (sessionstorage != null) {
            ac.setSessionstorage(sessionstorage)
        }
        if (customTagMappings != null) ac.setCustomTagMappings(customTagMappings)
        if (componentMappings != null) ac.setComponentMappings(componentMappings)
        if (mappings != null) ac.setMappings(mappings)
        if (loginstorage != Scope.SCOPE_UNDEFINED) ac.setLoginStorage(loginstorage)
        if (!StringUtil.isEmpty(datasource)) {
            ac.setDefDataSource(datasource)
            ac.setORMDataSource(datasource)
        }
        if (!StringUtil.isEmpty(defaultdatasource)) ac.setDefDataSource(defaultdatasource)
        if (datasources != null) {
            try {
                ac.setDataSources(AppListenerUtil.toDataSources(pageContext.getConfig(), datasources, ThreadLocalPageContext.getLog(pageContext, "application")))
            } catch (e: Exception) {
                throw Caster.toPageException(e)
            }
        }
        if (logs != null) {
            try {
                val acs: ApplicationContextSupport? = ac as ApplicationContextSupport?
                acs.setLoggers(ApplicationContextSupport.initLog(pageContext.getConfig(), logs))
            } catch (e: Exception) {
                throw Caster.toPageException(e)
            }
        }
        if (mails != null) {
            val acs: ApplicationContextSupport? = ac as ApplicationContextSupport?
            try {
                acs.setMailServers(AppListenerUtil.toMailServers(pageContext.getConfig(), mails, null))
            } catch (e: Exception) {
                throw Caster.toPageException(e)
            }
        }
        if (caches != null) {
            try {
                val acs: ApplicationContextSupport? = ac as ApplicationContextSupport?
                val it: Iterator<Entry<Key?, Object?>?> = caches.entryIterator()
                var e: Entry<Key?, Object?>?
                var name: String
                var sct: Struct
                while (it.hasNext()) {
                    e = it.next()
                    // default value by name
                    if (!StringUtil.isEmpty(Caster.toString(e.getValue(), null).also { name = it })) {
                        setDefault(ac, e.getKey(), name)
                    } else if (Caster.toStruct(e.getValue(), null).also { sct = it } != null) {
                        val cc: CacheConnection = ModernApplicationContext.toCacheConnection(pageContext.getConfig(), e.getKey().getString(), sct)
                        if (cc != null) {
                            name = e.getKey().getString()
                            acs.setCacheConnection(name, cc)

                            // key is a cache type
                            setDefault(ac, e.getKey(), name)

                            // default key
                            val def: Key = Caster.toKey(sct.get(KeyConstants._default, null), null)
                            if (def != null) setDefault(ac, def, name)
                        }
                    }
                }
            } catch (e: Exception) {
                throw Caster.toPageException(e)
            }
        }
        if (onmissingtemplate != null && ac is ClassicApplicationContext) {
            (ac as ClassicApplicationContext?).setOnMissingTemplate(onmissingtemplate)
        }
        val acs: ApplicationContextSupport? = ac as ApplicationContextSupport?
        if (scriptrotect != null) ac.setScriptProtect(AppListenerUtil.translateScriptProtect(scriptrotect))
        if (functionpaths != null) acs.setFunctionDirectories(AppListenerUtil.loadResources(pageContext.getConfig(), ac, functionpaths, true))
        if (proxy != null) acs.setProxyData(ProxyDataImpl.toProxyData(proxy))
        if (bufferOutput != null) ac.setBufferOutput(bufferOutput.booleanValue())
        if (secureJson != null) ac.setSecureJson(secureJson.booleanValue())
        if (typeChecking != null) ac.setTypeChecking(typeChecking.booleanValue())
        if (suppress != null) ac.setSuppressContent(suppress.booleanValue())
        if (secureJsonPrefix != null) ac.setSecureJsonPrefix(secureJsonPrefix)
        if (setClientCookies != null) ac.setSetClientCookies(setClientCookies.booleanValue())
        if (setClientManagement != null) ac.setSetClientManagement(setClientManagement.booleanValue())
        if (setDomainCookies != null) ac.setSetDomainCookies(setDomainCookies.booleanValue())
        if (setSessionManagement != null) ac.setSetSessionManagement(setSessionManagement.booleanValue())
        if (localMode != -1) ac.setLocalMode(localMode)
        if (mailListener != null) (ac as ApplicationContextSupport?).setMailListener(mailListener)
        if (queryListener != null) (ac as ApplicationContextSupport?).setQueryListener(queryListener)
        if (serializationSettings != null) (ac as ApplicationContextSupport?).setSerializationSettings(serializationSettings)
        if (locale != null) ac.setLocale(locale)
        if (timeZone != null) ac.setTimeZone(timeZone)
        if (nullSupport != null) (ac as ApplicationContextSupport?).setFullNullSupport(nullSupport)
        if (enableNULLSupport != null) (ac as ApplicationContextSupport?).setFullNullSupport(enableNULLSupport)
        if (queryPSQ != null) (ac as ApplicationContextSupport?).setQueryPSQ(queryPSQ)
        if (queryVarUsage != 0) (ac as ApplicationContextSupport?).setQueryVarUsage(queryVarUsage)
        if (queryCachedAfter != null) (ac as ApplicationContextSupport?).setQueryCachedAfter(queryCachedAfter)
        if (webCharset != null) ac.setWebCharset(webCharset.toCharset())
        if (resourceCharset != null) ac.setResourceCharset(resourceCharset.toCharset())
        if (sessionType.toInt() != -1) ac.setSessionType(sessionType)
        if (wsType.toInt() != -1) ac.setWSType(wsType)
        if (triggerDataMember != null) ac.setTriggerComponentDataMember(triggerDataMember.booleanValue())
        if (compression != null) ac.setAllowCompression(compression.booleanValue())
        if (cacheFunction != null) ac.setDefaultCacheName(Config.CACHE_TYPE_FUNCTION, cacheFunction)
        if (cacheObject != null) ac.setDefaultCacheName(Config.CACHE_TYPE_OBJECT, cacheObject)
        if (cacheQuery != null) ac.setDefaultCacheName(Config.CACHE_TYPE_QUERY, cacheQuery)
        if (cacheResource != null) ac.setDefaultCacheName(Config.CACHE_TYPE_RESOURCE, cacheResource)
        if (cacheTemplate != null) ac.setDefaultCacheName(Config.CACHE_TYPE_TEMPLATE, cacheTemplate)
        if (cacheInclude != null) ac.setDefaultCacheName(Config.CACHE_TYPE_INCLUDE, cacheInclude)
        if (cacheHTTP != null) ac.setDefaultCacheName(Config.CACHE_TYPE_HTTP, cacheHTTP)
        if (cacheFile != null) ac.setDefaultCacheName(Config.CACHE_TYPE_FILE, cacheFile)
        if (cacheWebservice != null) ac.setDefaultCacheName(Config.CACHE_TYPE_WEBSERVICE, cacheWebservice)
        if (antiSamyPolicyResource != null) (ac as ApplicationContextSupport?).setAntiSamyPolicyResource(antiSamyPolicyResource)
        if (sessionCookie != null) acs.setSessionCookie(sessionCookie)
        if (authCookie != null) acs.setAuthCookie(authCookie)
        if (tag != null) ac.setTagAttributeDefaultValues(pageContext, tag)
        if (clientCluster != null) ac.setClientCluster(clientCluster.booleanValue())
        if (sessionCluster != null) ac.setSessionCluster(sessionCluster.booleanValue())
        if (cgiReadOnly != null) ac.setCGIScopeReadonly(cgiReadOnly.booleanValue())
        if (preciseMath != null) (ac as ApplicationContextSupport?).setPreciseMath(preciseMath.booleanValue())
        if (s3 != null) ac.setS3(AppListenerUtil.toS3(s3))
        if (ftp != null) (ac as ApplicationContextSupport?).setFTP(AppListenerUtil.toFTP(ftp))

        // Scope cascading
        if (scopeCascading.toInt() != -1) ac.setScopeCascading(scopeCascading)
        if (blockedExtForFileUpload != null) {
            if (ac is ClassicApplicationContext) {
                (ac as ClassicApplicationContext?).setBlockedextforfileupload(blockedExtForFileUpload)
            }
        }
        if (ac is ApplicationContextSupport) {
            val appContextSup: ApplicationContextSupport? = ac as ApplicationContextSupport?
            if (javaSettings != null) appContextSup.setJavaSettings(JavaSettingsImpl.newInstance(JavaSettingsImpl(), javaSettings))
            if (xmlFeatures != null) appContextSup.setXmlFeatures(xmlFeatures)
            if (searchQueries != null) appContextSup.setAllowImplicidQueryCall(searchQueries.booleanValue())
            if (regex != null) appContextSup.setRegex(regex)
        }

        // ORM
        var initORM = false
        if (!update) {
            if (ormenabled == null) ormenabled = false
            if (ormsettings == null) ormsettings = StructImpl()
        }
        if (ormenabled != null) ac.setORMEnabled(ormenabled)
        if (ac.isORMEnabled()) {
            initORM = true
            if (ormsettings != null) AppListenerUtil.setORMConfiguration(pageContext, ac, ormsettings)
        }
        return initORM
    }

    @Override
    fun doEndTag(): Int {
        return EVAL_PAGE
    }

    companion object {
        private const val ACTION_CREATE = 0
        private const val ACTION_UPDATE = 1
        private fun setDefault(ac: ApplicationContext?, type: Key?, cacheName: String?) {
            if (KeyConstants._function.equals(type)) ac.setDefaultCacheName(Config.CACHE_TYPE_FUNCTION, cacheName) else if (KeyConstants._object.equals(type)) ac.setDefaultCacheName(Config.CACHE_TYPE_OBJECT, cacheName) else if (KeyConstants._query.equals(type)) ac.setDefaultCacheName(Config.CACHE_TYPE_QUERY, cacheName) else if (KeyConstants._resource.equals(type)) ac.setDefaultCacheName(Config.CACHE_TYPE_RESOURCE, cacheName) else if (KeyConstants._template.equals(type)) ac.setDefaultCacheName(Config.CACHE_TYPE_TEMPLATE, cacheName) else if (KeyConstants._include.equals(type)) ac.setDefaultCacheName(Config.CACHE_TYPE_INCLUDE, cacheName) else if (KeyConstants._http.equals(type)) ac.setDefaultCacheName(Config.CACHE_TYPE_HTTP, cacheName) else if (KeyConstants._file.equals(type)) ac.setDefaultCacheName(Config.CACHE_TYPE_FILE, cacheName) else if (KeyConstants._webservice.equals(type)) ac.setDefaultCacheName(Config.CACHE_TYPE_WEBSERVICE, cacheName)
        }
    }
}