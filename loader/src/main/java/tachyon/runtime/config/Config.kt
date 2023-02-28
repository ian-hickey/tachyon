/**
 * Copyright (c) 2014, the Railo Company Ltd.
 * Copyright (c) 2015, Tachyon Assosication Switzerland
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
package tachyon.runtime.config

import java.io.IOException

/**
 * interface for Config Object
 */
interface Config {
    val inspectTemplate: Short
    val defaultDataSource: String?

    /**
     * return how tachyon cascade scopes
     *
     * @return type of cascading
     */
    val scopeCascadingType: Short
    // public abstract String[] getCFMLExtensions();
    // public abstract String getCFCExtension();
    // public abstract String getComponentExtension();
    // public abstract String[] getTemplateExtensions();
    // public abstract String[] getAllExtensions();
    /**
     * return the mapping to custom tag directory
     *
     * @return custom tag directory
     */
    val customTagMappings: Array<Any?>?

    /**
     * return if it is allowed to implizid query call, call a query member witot define name of the
     * query.
     *
     * @return is allowed
     */
    fun allowImplicidQueryCall(): Boolean

    /**
     * e merged return if url and form scope will b
     *
     * @return merge or not
     */
    fun mergeFormAndURL(): Boolean

    /**
     * @return Returns the application Timeout.
     */
    val applicationTimeout: TimeSpan?

    /**
     * @return Returns the session Timeout.
     */
    val sessionTimeout: TimeSpan?

    /**
     * @return Returns the client Timeout.
     */
    val clientTimeout: TimeSpan?

    /**
     * @return Returns the request Timeout.
     */
    val requestTimeout: TimeSpan?

    /**
     * @return Returns the clientCookies.
     */
    val isClientCookies: Boolean

    /**
     * @return Returns the clientManagement.
     */
    val isClientManagement: Boolean

    /**
     * @return Returns the domainCookies.
     */
    val isDomainCookies: Boolean

    /**
     * @return Returns the sessionManagement.
     */
    val isSessionManagement: Boolean

    /**
     * @return Returns the spoolEnable.
     */
    val isMailSpoolEnable: Boolean

    /**
     * @return Returns the mailTimeout.
     */
    val mailTimeout: Int

    /**
     * @return preserve single quotes in cfquery tag or not
     */
    val pSQL: Boolean

    /**
     * @return Returns the locale.
     */
    val locale: Locale?

    /**
     * return if debug output will be generated
     *
     * @return debug or not
     */
    fun debug(): Boolean

    /**
     * return the temp directory
     *
     * @return temp directory
     */
    val tempDirectory: Resource?

    /**
     * @return Returns the spoolInterval.
     */
    val mailSpoolInterval: Int

    /**
     * @return returns the time zone for this
     */
    val timeZone: TimeZone?

    /**
     * @return returns the offset from the timeserver to local time
     */
    val timeServerOffset: Long

    /**
     * @return return if a password is set
     */
    fun hasPassword(): Boolean

    /**
     * @param password password
     * @return return if a password is set
     */
    fun passwordEqual(password: Password?): Boolean

    /**
     * @return return if a password is set
     */
    fun hasServerPassword(): Boolean

    /**
     * @return Returns the mappings.
     */
    val mappings: Array<Any?>?

    /**
     * @return Returns the configDir.
     */
    val configDir: Resource?

    /**
     * @return Returns the configFile.
     */
    val configFile: Resource?

    /**
     * @return Returns the loadTime.
     */
    val loadTime: Long

    /**
     * @param dialect dialect
     * @return Returns the baseComponent.
     */
    fun getBaseComponentTemplate(dialect: Int): String?

    /**
     * @return returns the client type
     */
    val clientType: Short

    /**
     * @return Returns the componentDataMemberDefaultAccess.
     */
    val componentDataMemberDefaultAccess: Int

    /**
     * @return Returns the timeServer.
     */
    val timeServer: String?

    /**
     * @return Returns the componentDump.
     */
    val componentDumpTemplate: String?

    /**
     * @return Returns the debug Template.
     */
    @get:Deprecated("use instead <code>getDebugEntry(ip, defaultValue)</code>")
    @get:Deprecated
    val debugTemplate: String?

    /**
     * @param statusCode status code
     * @return Returns the error Template for given status code.
     */
    fun getErrorTemplate(statusCode: Int): String?

    /**
     * @return Returns the sessionType.
     */
    val sessionType: Short

    /**
     * @return returns the charset for the response and request
     */
    val webCharset: Charset?

    /**
     * @return returns the charset used to read cfml files
     */
    val templateCharset: Charset?

    /**
     * @return returns the charset used to read and write resources
     */
    val resourceCharset: Charset?

    /**
     * @return returns the default charset for mail
     */
    val mailDefaultCharset: Charset?

    /**
     * @return returns update type (auto or manual)
     */
    val updateType: String?

    /**
     * @return returns URL for update
     */
    val updateLocation: URL?

    /**
     * return directory, where tachyon deploy translated cfml classes (java and class files)
     *
     * @return deploy directory
     */
    val classDirectory: Resource?

    /**
     * @return Returns the rootDir.
     */
    val rootDirectory: Resource?

    /**
     * @return Returns the accessor.
     */
    val securityManager: SecurityManager?

    /**
     * @return Returns the cfxTagPool.
     * @throws PageException Page Exception
     */
    @get:Throws(PageException::class)
    val cFXTagPool: CFXTagPool?

    /**
     * @param password password
     * @return ConfigServer
     * @throws PageException Page Exception
     */
    @Deprecated
    @Deprecated("use instead ConfigWeb.getConfigServer(Password password)")
    @Throws(PageException::class)
    fun getConfigServer(password: String?): ConfigServer?

    @Throws(PageException::class)
    fun getConfigServer(key: String?, timeNonce: Long): ConfigServer?

    /**
     * reload the time offset to a time server
     */
    fun reloadTimeServerOffset()

    /**
     * reset config
     */
    fun reset()

    /**
     * @return return the search Storage
     */
    val searchEngineClassDefinition: ClassDefinition<SearchEngine?>?
    val searchEngineDirectory: String?

    /**
     * @return return the Scheduler
     */
    val scheduler: Scheduler?

    /**
     * @return return all defined Mail Servers
     */
    val mailServers: Array<Any?>?

    /**
     * return the compile type of this context
     * @return compile type
     */
    val compileType: Short

    /**
     * return the all datasources
     * @return all datasources
     */
    val dataSources: Array<Any?>?

    /**
     * @param path get a resource that match this path
     * @return resource matching path
     */
    fun getResource(path: String?): Resource?

    /**
     * return current application listener
     *
     * @return application listener
     */
    val applicationListener: ApplicationListener?

    /**
     * @return the scriptProtect
     */
    val scriptProtect: Int

    /**
     * return default proxy setting password
     *
     * @return the password for proxy
     */
    val proxyData: ProxyData?

    /**
     * return if proxy is enabled or not
     *
     * @param host Host
     * @return is proxy enabled
     */
    fun isProxyEnableFor(host: String?): Boolean

    /**
     * @return the triggerComponentDataMember
     */
    val triggerComponentDataMember: Boolean
    val restSetting: RestSettings?
    val clientScopeDir: Resource?
    val clientScopeDirSize: Long

    @Throws(IOException::class)
    fun getRPCClassLoader(reload: Boolean): ClassLoader?
    val cacheDir: Resource?
    val cacheDirSize: Long
    val cacheConnections: Map<String?, Any?>?

    /**
     * get default cache connection for a specific type
     *
     * @param type default type, one of the following (CACHE_DEFAULT_NONE, CACHE_DEFAULT_OBJECT,
     * CACHE_DEFAULT_TEMPLATE, CACHE_DEFAULT_QUERY, CACHE_DEFAULT_RESOURCE)
     * @return matching Cache Connection
     */
    fun getCacheDefaultConnection(type: Int): CacheConnection?

    /**
     * get name of a default cache connection for a specific type
     *
     * @param type default type, one of the following (CACHE_DEFAULT_NONE, CACHE_DEFAULT_OBJECT,
     * CACHE_DEFAULT_TEMPLATE, CACHE_DEFAULT_QUERY, CACHE_DEFAULT_RESOURCE)
     * @return name of matching Cache Connection
     */
    fun getCacheDefaultConnectionName(type: Int): String?

    /**
     * returns the default DumpWriter
     *
     * @param defaultType default type
     * @return default DumpWriter
     */
    fun getDefaultDumpWriter(defaultType: Int): DumpWriter?

    /**
     * returns the DumpWriter matching key
     *
     * @param key key for DumpWriter
     * @param defaultType default type
     * @return matching DumpWriter
     * @throws PageException if there is no DumpWriter for this key
     */
    @Throws(PageException::class)
    fun getDumpWriter(key: String?, defaultType: Int): DumpWriter?

    /**
     * returns the DumpWriter matching key
     *
     * @param key key for DumpWriter
     * @return matching DumpWriter
     * @throws PageException if there is no DumpWriter for this key
     */
    @Deprecated
    @Deprecated("""use instead <code>getDumpWriter(String key,int defaultType)</code>
	  """)
    @Throws(PageException::class)
    fun getDumpWriter(key: String?): DumpWriter?

    /**
     * define if components has a "shadow" in the component variables scope or not.
     *
     * @return if the component has a shadow scope.
     */
    fun useComponentShadow(): Boolean

    /*
	 * * return a database connection hold inside by a datasource definition
	 * 
	 * @param datasource definiti0on of the datasource
	 * 
	 * @param user username to connect
	 * 
	 * @param pass password to connect
	 * 
	 * @return datasource connnection
	 * 
	 * @throws PageException
	 */
    // public DatasourceConnection getConnection(String datasource, String user, String pass) throws
    // PageException;
    /*
         * *
         * 
         * @return returns the ConnectionPool
         */
    val componentMappings: Array<Any?>?
    fun doCustomTagDeepSearch(): Boolean

    /**
     * @return returns the error print writer stream
     */
    fun getErrWriter(): PrintWriter?

    /**
     * @return returns the out print writer stream
     */
    fun getOutWriter(): PrintWriter?

    /**
     * define if tachyon search in local directory for custom tags or not
     *
     * @return search in local dir?
     */
    fun doLocalCustomTag(): Boolean
    fun getCustomTagExtensions(): Array<String?>?

    /**
     * @return if error status code will be returned or not
     */
    fun getErrorStatusCode(): Boolean
    fun getLocalMode(): Int

    /**
     * @return return the class defined for the cluster scope
     */
    @Deprecated
    fun getClusterClass(): Class<*>?

    /**
     * @return classloader of ths context
     */
    fun getClassLoader(): ClassLoader? // FUTURE deprecated, use instead getClassLoaderCore

    // public ClassLoader getClassLoaderCore();
    // public ClassLoader getClassLoaderLoader();
    fun getExtensionDirectory(): Resource?
    fun getExtensionProviders(): Array<ExtensionProvider?>?
    fun getExtensions(): Array<Extension?>?
    fun getBaseComponentPageSource(dialect: Int): PageSource?
    fun allowRealPath(): Boolean
    fun getConstants(): Struct?

    @Throws(PageException::class)
    fun getDataSource(datasource: String?): DataSource?
    fun getDataSource(datasource: String?, defaultValue: DataSource?): DataSource?
    fun getDataSourcesAsMap(): Map<String?, DataSource?>?
    fun getDefaultEncoding(): String?
    fun getDefaultResourceProvider(): ResourceProvider?
    fun isExtensionEnabled(): Boolean
    fun getFldFile(): Resource?

    /**
     * @return the tldFile
     */
    fun getTldFile(): Resource?

    /**
     * get PageSource of the first Mapping that match the given criteria
     *
     * @param mappings per application mappings
     * @param realPath path to get PageSource for
     * @param onlyTopLevel checks only toplevel mappings
     * @return Page Source
     */
    @Deprecated
    @Deprecated("use instead getPageSources or getPageSourceExisting")
    fun getPageSource(mappings: Array<Mapping?>?, realPath: String?, onlyTopLevel: Boolean): PageSource?

    /**
     * return existing PageSource that match the given criteria, if there is no PageSource null is
     * returned.
     *
     * @param pc current PageContext
     * @param mappings per application mappings
     * @param realPath path to get PageSource for
     * @param onlyTopLevel checks only toplevel mappings
     * @param useSpecialMappings invoke special mappings like "mapping-tag" or "mapping-customtag"
     * @param useDefaultMapping also invoke the always existing default mapping "/"
     * @param onlyPhysicalExisting only Physical existing
     * @return Page Source
     */
    fun getPageSourceExisting(pc: PageContext?, mappings: Array<Mapping?>?, realPath: String?, onlyTopLevel: Boolean, useSpecialMappings: Boolean, useDefaultMapping: Boolean,
                              onlyPhysicalExisting: Boolean): PageSource?

    /**
     * get all PageSources that match the given criteria
     *
     * @param pc current PageContext
     * @param mappings per application mappings
     * @param realPath path to get PageSource for
     * @param onlyTopLevel checks only toplevel mappings
     * @param useSpecialMappings invoke special mappings like "mapping-tag" or "mapping-customtag"
     * @param useDefaultMapping also invoke the always existing default mapping "/"
     * @return All Page Sources
     */
    @Deprecated
    @Deprecated("use instead")
    fun getPageSources(pc: PageContext?, mappings: Array<Mapping?>?, realPath: String?, onlyTopLevel: Boolean, useSpecialMappings: Boolean, useDefaultMapping: Boolean): Array<PageSource?>?

    /**
     * get all PageSources that match the given criteria
     *
     * @param pc current PageContext
     * @param mappings per application mappings
     * @param realPath path to get PageSource for
     * @param onlyTopLevel checks only toplevel mappings
     * @param useSpecialMappings invoke special mappings like "mapping-tag" or "mapping-customtag"
     * @param useDefaultMapping also invoke the always existing default mapping "/"
     * @param useComponentMappings also invoke component mappings
     * @return All Page Sources
     */
    fun getPageSources(pc: PageContext?, mappings: Array<Mapping?>?, realPath: String?, onlyTopLevel: Boolean, useSpecialMappings: Boolean, useDefaultMapping: Boolean,
                       useComponentMappings: Boolean): Array<PageSource?>?

    /**
     * get Resource of the first Mapping that match the given criteria
     *
     * @param mappings per application mappings
     * @param relPath path to get PageSource for
     * @param alsoDefaultMapping also default mapping
     * @return Resource
     */
    @Deprecated
    @Deprecated("use instead getPhysicalResources or getPhysicalResourceExisting")
    fun getPhysical(mappings: Array<Mapping?>?, relPath: String?, alsoDefaultMapping: Boolean): Resource?

    /**
     * get all Resources that match the given criteria
     *
     * @param pc current PageContext
     * @param mappings per application mappings
     * @param realPath path to get PageSource for
     * @param onlyTopLevel checks only toplevel mappings
     * @param useSpecialMappings invoke special mappings like "mapping-tag" or "mapping-customtag"
     * @param useDefaultMapping also invoke the always existing default mapping "/"
     * @return Resource
     */
    fun getPhysicalResources(pc: PageContext?, mappings: Array<Mapping?>?, realPath: String?, onlyTopLevel: Boolean, useSpecialMappings: Boolean, useDefaultMapping: Boolean): Array<Resource?>?

    /**
     * return existing Resource that match the given criteria, if there is no Resource null is returned.
     *
     * @param pc current PageContext
     * @param mappings per application mappings
     * @param realPath path to get Resource for
     * @param onlyTopLevel checks only toplevel mappings
     * @param useSpecialMappings invoke special mappings like "mapping-tag" or "mapping-customtag"
     * @param useDefaultMapping also invoke the always existing default mapping "/"
     * @return Resource
     */
    fun getPhysicalResourceExisting(pc: PageContext?, mappings: Array<Mapping?>?, realPath: String?, onlyTopLevel: Boolean, useSpecialMappings: Boolean, useDefaultMapping: Boolean): Resource?
    fun getRemoteClientDirectory(): Resource?
    fun getRemoteClients(): Array<RemoteClient?>?
    fun getSpoolerEngine(): SpoolerEngine?
    fun getResourceProviders(): Array<ResourceProvider?>?
    fun getVersion(): Double
    fun getVideoDirectory(): Resource?

    // public String getVideoProviderLocation();
    fun isShowVersion(): Boolean
    fun isSuppressWhitespace(): Boolean

    // public boolean isVideoAgreementAccepted();
    fun getRemoteClientUsage(): Struct?
    fun getAdminSyncClass(): Class<AdminSync?>?
    fun getVideoExecuterClass(): Class<VideoExecuter?>?
    fun getThreadQueue(): ThreadQueue?
    fun getSessionCluster(): Boolean
    fun getClientCluster(): Boolean
    fun getSecurityDirectory(): Resource?
    fun isMonitoringEnabled(): Boolean
    fun getRequestMonitors(): Array<RequestMonitor?>?

    @Throws(PageException::class)
    fun getRequestMonitor(name: String?): RequestMonitor?
    fun getIntervallMonitors(): Array<IntervallMonitor?>?

    @Throws(PageException::class)
    fun getIntervallMonitor(name: String?): IntervallMonitor?

    @Throws(PageException::class)
    fun getActionMonitor(name: String?): ActionMonitor?

    /**
     * if free permspace gen is lower than 10000000 bytes, tachyon shrinks all classloaders
     *
     * @param check check
     */
    fun checkPermGenSpace(check: Boolean)
    fun allowRequestTimeout(): Boolean
    fun getLog(name: String?): Log?
    fun getHandleUnQuotedAttrValueAsString(): Boolean?
    fun getCachedWithin(type: Int): Object?
    fun getIdentification(): Identification?
    fun getLoginDelay(): Int
    fun getLoginCaptcha(): Boolean
    fun getRememberMe(): Boolean
    fun getFullNullSupport(): Boolean

    @Throws(PageException::class)
    fun getORMEngine(pc: PageContext?): ORMEngine?
    fun getLocalExtensionProviderDirectory(): Resource?
    fun getDeployDirectory(): Resource?

    companion object {
        /**
         * Define a strict scope cascading
         */
        const val SCOPE_STRICT: Short = 0

        /**
         * Define a small scope cascading
         */
        const val SCOPE_SMALL: Short = 1

        /**
         * Define a standart scope cascading (like other cf versions)
         */
        const val SCOPE_STANDARD: Short = 2

        /**
         * Field `CLIENT_SCOPE_TYPE_COOKIE`
         */
        const val CLIENT_SCOPE_TYPE_COOKIE: Short = 0

        /**
         * Field `CLIENT_SCOPE_TYPE_FILE`
         */
        const val CLIENT_SCOPE_TYPE_FILE: Short = 1

        /**
         * Field `CLIENT_SCOPE_TYPE_DB`
         */
        const val CLIENT_SCOPE_TYPE_DB: Short = 2

        /**
         * Field `SESSION_TYPE_APPLICATION`
         */
        const val SESSION_TYPE_APPLICATION: Short = 0

        /**
         * Field `SESSION_TYPE_J2EE`
         */
        const val SESSION_TYPE_JEE: Short = 1

        /**
         * Field `RECOMPILE_NEVER`
         */
        const val RECOMPILE_NEVER: Short = 0

        /**
         * Field `RECOMPILE_AT_STARTUP`
         */
        const val RECOMPILE_AFTER_STARTUP: Short = 1

        /**
         * Field `RECOMPILE_ALWAYS`
         */
        const val RECOMPILE_ALWAYS: Short = 2
        const val INSPECT_ALWAYS: Short = 0
        const val INSPECT_ONCE: Short = 1
        const val INSPECT_NEVER: Short = 2

        // Hibernate Extension has hardcoded this 4, do not change!!!!
        const val INSPECT_UNDEFINED: Short = 4

        /*
	 * public static final int CUSTOM_TAG_MODE_NONE = 0; public static final int CUSTOM_TAG_MODE_CLASSIC
	 * = 1; public static final int CUSTOM_TAG_MODE_MODERN = 2; public static final int
	 * CUSTOM_TAG_MODE_CLASSIC_MODERN = 4; public static final int CUSTOM_TAG_MODE_MODERN_CLASSIC = 8;
	 */
        const val CACHE_TYPE_NONE = 0
        const val CACHE_TYPE_OBJECT = 1
        const val CACHE_TYPE_TEMPLATE = 2
        const val CACHE_TYPE_QUERY = 4
        const val CACHE_TYPE_RESOURCE = 8
        const val CACHE_TYPE_FUNCTION = 16
        const val CACHE_TYPE_INCLUDE = 32
        const val CACHE_TYPE_HTTP = 64
        const val CACHE_TYPE_FILE = 128
        const val CACHE_TYPE_WEBSERVICE = 256
        const val CACHEDWITHIN_QUERY = CACHE_TYPE_QUERY
        const val CACHEDWITHIN_RESOURCE = CACHE_TYPE_RESOURCE
        const val CACHEDWITHIN_FUNCTION = CACHE_TYPE_FUNCTION
        const val CACHEDWITHIN_INCLUDE = CACHE_TYPE_INCLUDE
        const val CACHEDWITHIN_HTTP = CACHE_TYPE_HTTP
        const val CACHEDWITHIN_FILE = CACHE_TYPE_FILE
        const val CACHEDWITHIN_WEBSERVICE = CACHE_TYPE_WEBSERVICE
    }
}