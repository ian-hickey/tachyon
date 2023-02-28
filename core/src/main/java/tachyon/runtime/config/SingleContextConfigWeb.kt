package tachyon.runtime.config

import java.io.IOException

class SingleContextConfigWeb(factory: CFMLFactoryImpl?, cs: ConfigServerImpl?, config: ServletConfig?) : ConfigBase(), ConfigWebPro {
    private val cs: ConfigServerImpl?
    protected var password: Password? = null
    private val helper: ConfigWebHelper?
    private val config: ServletConfig?
    private val factory: CFMLFactoryImpl?
    private var id: SCCWIdentificationWeb? = null
    private var rootDir: Resource?
    private var mappings: Array<Mapping?>?
    fun getConfigServerImpl(): ConfigServerImpl? {
        return cs
    }

    @Override
    override fun isAllowURLRequestTimeout(): Boolean {
        return cs!!.isAllowURLRequestTimeout()
    }

    @Override
    fun getCompileType(): Short {
        return cs!!.getCompileType()
    }

    @Override
    fun reloadTimeServerOffset() {
        cs!!.reloadTimeServerOffset()
    }

    @Override
    override fun lastModified(): Long {
        return cs!!.lastModified()
    }

    @Override
    fun getScopeCascadingType(): Short {
        return cs!!.getScopeCascadingType()
    }

    @Override
    override fun getFLDs(dialect: Int): Array<FunctionLib?>? {
        return cs!!.getFLDs(dialect)
    }

    @Override
    override fun getCombinedFLDs(dialect: Int): FunctionLib? {
        return cs!!.getCombinedFLDs(dialect)
    }

    @Override
    override fun getTLDs(dialect: Int): Array<TagLib?>? {
        return cs!!.getTLDs(dialect)
    }

    @Override
    fun allowImplicidQueryCall(): Boolean {
        return cs!!.allowImplicidQueryCall()
    }

    @Override
    fun mergeFormAndURL(): Boolean {
        return cs!!.mergeFormAndURL()
    }

    @Override
    fun getApplicationTimeout(): TimeSpan? {
        return cs!!.getApplicationTimeout()
    }

    @Override
    fun getSessionTimeout(): TimeSpan? {
        return cs!!.getSessionTimeout()
    }

    @Override
    fun getClientTimeout(): TimeSpan? {
        return cs!!.getClientTimeout()
    }

    @Override
    fun getRequestTimeout(): TimeSpan? {
        return cs!!.getRequestTimeout()
    }

    @Override
    fun isClientCookies(): Boolean {
        return cs!!.isClientCookies()
    }

    @Override
    override fun isDevelopMode(): Boolean {
        return cs!!.isDevelopMode()
    }

    @Override
    fun isClientManagement(): Boolean {
        return cs!!.isClientManagement()
    }

    @Override
    fun isDomainCookies(): Boolean {
        return cs!!.isDomainCookies()
    }

    @Override
    fun isSessionManagement(): Boolean {
        return cs!!.isSessionManagement()
    }

    @Override
    fun isMailSpoolEnable(): Boolean {
        return cs!!.isMailSpoolEnable()
    }

    @Override
    override fun isMailSendPartial(): Boolean {
        return cs!!.isMailSendPartial()
    }

    @Override
    override fun isUserset(): Boolean {
        return cs!!.isUserset()
    }

    @Override
    fun getMailServers(): Array<Server?>? {
        return cs!!.getMailServers()
    }

    @Override
    fun getMailTimeout(): Int {
        return cs!!.getMailTimeout()
    }

    @Override
    fun getPSQL(): Boolean {
        return cs!!.getPSQL()
    }

    @Override
    override fun getQueryVarUsage(): Int {
        return cs!!.getQueryVarUsage()
    }

    @Override
    fun getClassLoader(): ClassLoader? {
        return cs!!.getClassLoader()
    }

    @Override
    override fun getClassLoaderEnv(): ClassLoader? {
        return cs!!.getClassLoaderEnv()
    }

    @Override
    override fun getClassLoaderCore(): ClassLoader? {
        return cs!!.getClassLoaderCore()
    }

    @Override
    override fun getResourceClassLoader(): ResourceClassLoader? {
        return cs!!.getResourceClassLoader()
    }

    @Override
    override fun getResourceClassLoader(defaultValue: ResourceClassLoader?): ResourceClassLoader? {
        return cs!!.getResourceClassLoader(defaultValue)
    }

    @Override
    fun getLocale(): Locale? {
        return cs!!.getLocale()
    }

    @Override
    fun debug(): Boolean {
        return cs!!.debug()
    }

    @Override
    override fun debugLogOutput(): Boolean {
        return cs!!.debugLogOutput()
    }

    @Override
    fun getTempDirectory(): Resource? {
        return cs!!.getTempDirectory()
    }

    @Override
    fun getMailSpoolInterval(): Int {
        return cs!!.getMailSpoolInterval()
    }

    @Override
    fun getTimeZone(): TimeZone? {
        return cs!!.getTimeZone()
    }

    @Override
    fun getTimeServerOffset(): Long {
        return cs!!.getTimeServerOffset()
    }

    @Override
    fun getScheduler(): Scheduler? {
        return cs!!.getScheduler()
    }

    @Override
    override fun isPasswordEqual(password: String?): Password? {
        return cs!!.isPasswordEqual(password)
    }

    @Override
    fun hasPassword(): Boolean {
        return cs!!.hasPassword()
    }

    @Override
    fun passwordEqual(password: Password?): Boolean {
        return cs!!.passwordEqual(password)
    }

    @Override
    fun getMappings(): Array<Mapping?>? {
        if (mappings == null) {
            synchronized(this) { if (mappings == null) createMapping() }
        }
        return mappings // cs.getMappings();
    }

    @Override
    override fun getRestMappings(): Array<tachyon.runtime.rest.Mapping?>? {
        return cs!!.getRestMappings()
    }

    @Override
    fun getPageSource(mappings: Array<Mapping?>?, realPath: String?, onlyTopLevel: Boolean): PageSource? {
        throw PageRuntimeException(DeprecatedException("method not supported"))
    }

    @Override
    fun getPageSourceExisting(pc: PageContext?, mappings: Array<Mapping?>?, realPath: String?, onlyTopLevel: Boolean, useSpecialMappings: Boolean, useDefaultMapping: Boolean,
                              onlyPhysicalExisting: Boolean): PageSource? {
        return ConfigWebUtil.getPageSourceExisting(pc, this, mappings, realPath, onlyTopLevel, useSpecialMappings, useDefaultMapping, onlyPhysicalExisting)
    }

    @Override
    fun getPageSources(pc: PageContext?, mappings: Array<Mapping?>?, realPath: String?, onlyTopLevel: Boolean, useSpecialMappings: Boolean, useDefaultMapping: Boolean): Array<PageSource?>? {
        return ConfigWebUtil.getPageSources(pc, this, mappings, realPath, onlyTopLevel, useSpecialMappings, useDefaultMapping, false, onlyFirstMatch)
    }

    @Override
    fun getPageSources(pc: PageContext?, mappings: Array<Mapping?>?, realPath: String?, onlyTopLevel: Boolean, useSpecialMappings: Boolean, useDefaultMapping: Boolean,
                       useComponentMappings: Boolean): Array<PageSource?>? {
        return ConfigWebUtil.getPageSources(pc, this, mappings, realPath, onlyTopLevel, useSpecialMappings, useDefaultMapping, useComponentMappings, onlyFirstMatch)
    }

    @Override
    fun getPhysical(mappings: Array<Mapping?>?, realPath: String?, alsoDefaultMapping: Boolean): Resource? {
        throw PageRuntimeException(DeprecatedException("method not supported"))
    }

    @Override
    fun getPhysicalResources(pc: PageContext?, mappings: Array<Mapping?>?, realPath: String?, onlyTopLevel: Boolean, useSpecialMappings: Boolean, useDefaultMapping: Boolean): Array<Resource?>? {
        throw PageRuntimeException(DeprecatedException("method not supported"))
    }

    @Override
    fun getPhysicalResourceExisting(pc: PageContext?, mappings: Array<Mapping?>?, realPath: String?, onlyTopLevel: Boolean, useSpecialMappings: Boolean, useDefaultMapping: Boolean): Resource? {
        throw PageRuntimeException(DeprecatedException("method not supported"))
    }

    @Override
    override fun toPageSource(mappings: Array<Mapping?>?, res: Resource?, defaultValue: PageSource?): PageSource? {
        return ConfigWebUtil.toPageSource(this, mappings, res, defaultValue)
    }

    @Override
    fun getConfigDir(): Resource? {
        return cs!!.getConfigDir()
    }

    @Override
    fun getConfigFile(): Resource? {
        return cs!!.getConfigFile()
    }

    @Override
    override fun getCoreTagLib(dialect: Int): TagLib? {
        return cs!!.getCoreTagLib(dialect)
    }

    @Override
    fun getCustomTagMappings(): Array<Mapping?>? {
        return cs!!.getCustomTagMappings()
    }

    @Override
    fun getLoadTime(): Long {
        return cs!!.getLoadTime()
    }

    @Override
    @Throws(SecurityException::class)
    fun getCFXTagPool(): CFXTagPool? {
        return cs!!.getCFXTagPool()
    }

    @Override
    fun getBaseComponentTemplate(dialect: Int): String? {
        return cs!!.getBaseComponentTemplate(dialect)
    }

    @Override
    fun getBaseComponentPageSource(dialect: Int): PageSource? {
        return cs!!.getBaseComponentPageSource(dialect)
    }

    @Override
    override fun getBaseComponentPageSource(dialect: Int, pc: PageContext?): PageSource? {
        return cs!!.getBaseComponentPageSource(dialect, pc)
    }

    @Override
    override fun getRestList(): Boolean {
        return cs!!.getRestList()
    }

    @Override
    fun getClientType(): Short {
        return cs!!.getClientType()
    }

    @Override
    fun getSearchEngineClassDefinition(): ClassDefinition<SearchEngine?>? {
        return cs!!.getSearchEngineClassDefinition()
    }

    @Override
    fun getSearchEngineDirectory(): String? {
        return cs!!.getSearchEngineDirectory()
    }

    @Override
    fun getComponentDataMemberDefaultAccess(): Int {
        return cs!!.getComponentDataMemberDefaultAccess()
    }

    @Override
    fun getTimeServer(): String? {
        return cs!!.getTimeServer()
    }

    @Override
    fun getComponentDumpTemplate(): String? {
        return cs!!.getComponentDumpTemplate()
    }

    @Override
    fun getDebugTemplate(): String? {
        return cs!!.getDebugTemplate()
    }

    @Override
    fun getErrorTemplate(statusCode: Int): String? {
        return cs!!.getErrorTemplate(statusCode)
    }

    @Override
    fun getSessionType(): Short {
        return cs!!.getSessionType()
    }

    @Override
    fun getUpdateType(): String? {
        return null
    }

    @Override
    fun getUpdateLocation(): URL? {
        return null
    }

    @Override
    fun getClassDirectory(): Resource? {
        return cs!!.getClassDirectory()
    }

    @Override
    override fun getLibraryDirectory(): Resource? {
        return cs!!.getLibraryDirectory()
    }

    @Override
    override fun getEventGatewayDirectory(): Resource? {
        return cs!!.getEventGatewayDirectory()
    }

    @Override
    override fun getClassesDirectory(): Resource? {
        return cs!!.getClassesDirectory()
    }

    @Override
    fun getRootDirectory(): Resource? {
        return rootDir
    }

    @Override
    fun isSuppressWhitespace(): Boolean {
        return cs!!.isSuppressWhitespace()
    }

    @Override
    override fun isSuppressContent(): Boolean {
        return cs!!.isSuppressContent()
    }

    @Override
    fun getDefaultEncoding(): String? {
        return cs!!.getDefaultEncoding()
    }

    @Override
    fun getTemplateCharset(): Charset? {
        return cs!!.getTemplateCharset()
    }

    @Override
    fun getWebCharset(): Charset? {
        return cs!!.getWebCharset()
    }

    @Override
    override fun getWebCharSet(): CharSet? {
        return cs!!.getWebCharSet()
    }

    @Override
    fun getResourceCharset(): Charset? {
        return cs!!.getResourceCharset()
    }

    @Override
    override fun getResourceCharSet(): CharSet? {
        return cs!!.getResourceCharSet()
    }

    @Override
    fun getSecurityManager(): SecurityManager? {
        return cs!!.getSecurityManager()
    }

    @Override
    fun getFldFile(): Resource? {
        return cs!!.getFldFile()
    }

    @Override
    fun getTldFile(): Resource? {
        return cs!!.getTldFile()
    }

    @Override
    fun getDataSources(): Array<DataSource?>? {
        return cs!!.getDataSources()
    }

    @Override
    fun getDataSourcesAsMap(): Map<String?, DataSource?>? {
        return cs!!.getDataSourcesAsMap()
    }

    @Override
    fun getMailDefaultCharset(): Charset? {
        return cs!!.getMailDefaultCharset()
    }

    @Override
    fun getDefaultResourceProvider(): ResourceProvider? {
        return cs!!.getDefaultResourceProvider()
    }

    @Override
    override fun getCacheHandlers(): Iterator<Entry<String?, Class<CacheHandler?>?>?>? {
        return cs!!.getCacheHandlers()
    }

    @Override
    fun getResourceProviders(): Array<ResourceProvider?>? {
        return cs!!.getResourceProviders()
    }

    @Override
    override fun getResourceProviderFactories(): Array<ResourceProviderFactory?>? {
        return cs!!.getResourceProviderFactories()
    }

    @Override
    override fun hasResourceProvider(scheme: String?): Boolean {
        return cs!!.hasResourceProvider(scheme)
    }

    @Override
    fun getResource(path: String?): Resource? {
        return cs!!.getResource(path)
    }

    @Override
    fun getApplicationListener(): ApplicationListener? {
        return cs!!.getApplicationListener()
    }

    @Override
    fun getScriptProtect(): Int {
        return cs!!.getScriptProtect()
    }

    @Override
    fun getProxyData(): ProxyData? {
        return cs!!.getProxyData()
    }

    @Override
    fun isProxyEnableFor(host: String?): Boolean {
        return cs!!.isProxyEnableFor(host)
    }

    @Override
    fun getTriggerComponentDataMember(): Boolean {
        return cs!!.getTriggerComponentDataMember()
    }

    @Override
    fun getClientScopeDir(): Resource? {
        return cs!!.getClientScopeDir()
    }

    @Override
    override fun getSessionScopeDir(): Resource? {
        return cs!!.getSessionScopeDir()
    }

    @Override
    fun getClientScopeDirSize(): Long {
        return cs!!.getClientScopeDirSize()
    }

    @Override
    @Throws(IOException::class)
    fun getRPCClassLoader(reload: Boolean): ClassLoader? {
        return cs!!.getRPCClassLoader(reload)
    }

    @Override
    @Throws(IOException::class)
    override fun getRPCClassLoader(reload: Boolean, parents: Array<ClassLoader?>?): ClassLoader? {
        return cs!!.getRPCClassLoader(reload, parents)
    }

    @Override
    fun getCacheDir(): Resource? {
        return cs!!.getCacheDir()
    }

    @Override
    fun getCacheDirSize(): Long {
        return cs!!.getCacheDirSize()
    }

    @Override
    fun getDefaultDumpWriter(defaultType: Int): DumpWriter? {
        return cs!!.getDefaultDumpWriter(defaultType)
    }

    @Override
    @Throws(DeprecatedException::class)
    fun getDumpWriter(name: String?): DumpWriter? {
        return cs!!.getDumpWriter(name)
    }

    @Override
    @Throws(ExpressionException::class)
    fun getDumpWriter(name: String?, defaultType: Int): DumpWriter? {
        return cs!!.getDumpWriter(name, defaultType)
    }

    @Override
    fun useComponentShadow(): Boolean {
        return cs!!.useComponentShadow()
    }

    @Override
    override fun useComponentPathCache(): Boolean {
        return cs!!.useComponentPathCache()
    }

    @Override
    override fun useCTPathCache(): Boolean {
        return cs!!.useCTPathCache()
    }

    @Override
    @Throws(DatabaseException::class)
    fun getDataSource(datasource: String?): DataSource? {
        return cs!!.getDataSource(datasource)
    }

    @Override
    fun getDataSource(datasource: String?, defaultValue: DataSource?): DataSource? {
        return cs!!.getDataSource(datasource, defaultValue)
    }

    @Override
    fun getErrWriter(): PrintWriter? {
        return cs!!.getErrWriter()
    }

    @Override
    fun getOutWriter(): PrintWriter? {
        return cs!!.getOutWriter()
    }

    @Override
    override fun getDatasourceConnectionPool(ds: DataSource?, user: String?, pass: String?): DatasourceConnPool? {
        return cs!!.getDatasourceConnectionPool(ds, user, pass)
    }

    @Override
    override fun getDatasourceConnectionPools(): Collection<DatasourceConnPool?>? {
        return cs!!.getDatasourceConnectionPools()
    }

    @Override
    fun doLocalCustomTag(): Boolean {
        return cs!!.doLocalCustomTag()
    }

    @Override
    fun getCustomTagExtensions(): Array<String?>? {
        return cs!!.getCustomTagExtensions()
    }

    @Override
    override fun doComponentDeepSearch(): Boolean {
        return cs!!.doComponentDeepSearch()
    }

    @Override
    fun doCustomTagDeepSearch(): Boolean {
        return cs!!.doCustomTagDeepSearch()
    }

    @Override
    fun getVersion(): Double {
        return cs!!.getVersion()
    }

    @Override
    override fun contentLength(): Boolean {
        return cs!!.contentLength()
    }

    @Override
    override fun allowCompression(): Boolean {
        return cs!!.allowCompression()
    }

    @Override
    fun getConstants(): Struct? {
        return cs!!.getConstants()
    }

    @Override
    fun isShowVersion(): Boolean {
        return cs!!.isShowVersion()
    }

    @Override
    fun getRemoteClients(): Array<RemoteClient?>? {
        return cs!!.getRemoteClients()
    }

    @Override
    fun getSpoolerEngine(): SpoolerEngine? {
        return cs!!.getSpoolerEngine()
        /*
		 * if (spoolerEngine == null) { Resource dir = getRemoteClientDirectory(); if (dir != null &&
		 * !dir.exists()) dir.mkdirs(); SpoolerEngineImpl se = (SpoolerEngineImpl) cs.getSpoolerEngine();
		 * spoolerEngine = new SpoolerEngineImpl(this, dir, "Remote Client Spooler", getLog("remoteclient"),
		 * se.getMaxThreads()); } return spoolerEngine;
		 */
    }

    @Override
    fun getRemoteClientDirectory(): Resource? {
        return cs!!.getRemoteClientDirectory()
        /*
		 * if (remoteClientDirectory == null) { return remoteClientDirectory =
		 * ConfigWebUtil.getFile(getRootDirectory(), "client-task", "client-task", getConfigDir(),
		 * FileUtil.TYPE_DIR, this); } return remoteClientDirectory;
		 */
    }

    @Override
    fun getErrorStatusCode(): Boolean {
        return cs!!.getErrorStatusCode()
    }

    @Override
    fun getLocalMode(): Int {
        return cs!!.getLocalMode()
    }

    @Override
    fun getVideoDirectory(): Resource? {
        return cs!!.getVideoDirectory()
    }

    @Override
    fun getExtensionDirectory(): Resource? {
        return cs!!.getExtensionDirectory()
    }

    @Override
    fun getExtensionProviders(): Array<ExtensionProvider?>? {
        return cs!!.getExtensionProviders()
    }

    @Override
    override fun getRHExtensionProviders(): Array<RHExtensionProvider?>? {
        return cs!!.getRHExtensionProviders()
    }

    @Override
    fun getExtensions(): Array<Extension?>? {
        return cs!!.getExtensions()
    }

    @Override
    override fun getRHExtensions(): Array<RHExtension?>? {
        return cs!!.getRHExtensions()
    }

    @Override
    fun isExtensionEnabled(): Boolean {
        return cs!!.isExtensionEnabled()
    }

    @Override
    fun allowRealPath(): Boolean {
        return cs!!.allowRealPath()
    }

    @Override
    fun getClusterClass(): Class? {
        return cs!!.getClusterClass()
    }

    @Override
    fun getRemoteClientUsage(): Struct? {
        return cs!!.getRemoteClientUsage()
    }

    @Override
    fun getAdminSyncClass(): Class<AdminSync?>? {
        return cs!!.getAdminSyncClass()
    }

    @Override
    @Throws(ClassException::class)
    override fun getAdminSync(): AdminSync? {
        return cs!!.getAdminSync()
    }

    @Override
    fun getVideoExecuterClass(): Class? {
        return cs!!.getVideoExecuterClass()
    }

    @Override
    override fun getUseTimeServer(): Boolean {
        return cs!!.getUseTimeServer()
    }

    @Override
    override fun getTagMappings(): Collection<Mapping?>? {
        return cs!!.getTagMappings()
    }

    @Override
    override fun getTagMapping(mappingName: String?): Mapping? {
        return cs!!.getTagMapping(mappingName)
    }

    @Override
    override fun getDefaultTagMapping(): Mapping? {
        return cs!!.getDefaultTagMapping()
    }

    @Override
    override fun getFunctionMapping(mappingName: String?): Mapping? {
        return cs!!.getFunctionMapping(mappingName)
    }

    @Override
    override fun getDefaultFunctionMapping(): Mapping? {
        return cs!!.getDefaultFunctionMapping()
    }

    @Override
    override fun getFunctionMappings(): Collection<Mapping?>? {
        return cs!!.getFunctionMappings()
    }

    @Override
    fun getDefaultDataSource(): String? {
        return cs!!.getDefaultDataSource()
    }

    @Override
    fun getInspectTemplate(): Short {
        return cs!!.getInspectTemplate()
    }

    @Override
    override fun getTypeChecking(): Boolean {
        return cs!!.getTypeChecking()
    }

    @Override
    override fun getSerialNumber(): String? {
        return cs!!.getSerialNumber()
    }

    @Override
    fun getCacheConnections(): Map<String?, CacheConnection?>? {
        return cs!!.getCacheConnections()
    }

    @Override
    fun getCacheDefaultConnection(type: Int): CacheConnection? {
        return cs!!.getCacheDefaultConnection(type)
    }

    @Override
    fun getCacheDefaultConnectionName(type: Int): String? {
        return cs!!.getCacheDefaultConnectionName(type)
    }

    @Override
    override fun getExecutionLogEnabled(): Boolean {
        return cs!!.getExecutionLogEnabled()
    }

    @Override
    override fun getExecutionLogFactory(): ExecutionLogFactory? {
        return cs!!.getExecutionLogFactory()
    }

    @Override
    @Throws(PageException::class)
    override fun resetORMEngine(pc: PageContext?, force: Boolean): ORMEngine? {
        return cs!!.resetORMEngine(pc, force)
    }

    @Override
    @Throws(PageException::class)
    fun getORMEngine(pc: PageContext?): ORMEngine? {
        return cs!!.getORMEngine(pc)
    }

    @Override
    override fun getORMEngineClassDefintion(): ClassDefinition<out ORMEngine?>? {
        return cs!!.getORMEngineClassDefintion()
    }

    @Override
    fun getComponentMappings(): Array<Mapping?>? {
        return cs!!.getComponentMappings()
    }

    @Override
    override fun getORMConfig(): ORMConfiguration? {
        return cs!!.getORMConfig()
    }

    @Override
    @Throws(TemplateException::class)
    override fun getCachedPage(pc: PageContext?, pathWithCFC: String?): CIPage? {
        return cs!!.getCachedPage(pc, pathWithCFC)
    }

    @Override
    override fun putCachedPageSource(pathWithCFC: String?, ps: PageSource?) {
        cs!!.putCachedPageSource(pathWithCFC, ps)
    }

    @Override
    override fun getCTInitFile(pc: PageContext?, key: String?): InitFile? {
        return cs!!.getCTInitFile(pc, key)
    }

    @Override
    override fun putCTInitFile(key: String?, initFile: InitFile?) {
        cs!!.putCTInitFile(key, initFile)
    }

    @Override
    override fun listCTCache(): Struct? {
        return cs!!.listCTCache()
    }

    @Override
    override fun clearCTCache() {
        cs!!.clearCTCache()
    }

    @Override
    override fun clearFunctionCache() {
        cs!!.clearFunctionCache()
    }

    @Override
    override fun getFromFunctionCache(key: String?): UDF? {
        return cs!!.getFromFunctionCache(key)
    }

    @Override
    override fun putToFunctionCache(key: String?, udf: UDF?) {
        cs!!.putToFunctionCache(key, udf)
    }

    @Override
    override fun listComponentCache(): Struct? {
        return cs!!.listComponentCache()
    }

    @Override
    override fun clearComponentCache() {
        cs!!.clearComponentCache()
    }

    @Override
    override fun clearApplicationCache() {
        cs!!.clearApplicationCache()
    }

    @Override
    override fun getComponentDefaultImport(): ImportDefintion? {
        return cs!!.getComponentDefaultImport()
    }

    @Override
    override fun getComponentLocalSearch(): Boolean {
        return cs!!.getComponentLocalSearch()
    }

    @Override
    override fun getComponentRootSearch(): Boolean {
        return cs!!.getComponentRootSearch()
    }

    @Override
    @Throws(IOException::class)
    override fun getCompressInstance(zipFile: Resource?, format: Int, caseSensitive: Boolean): Compress? {
        return cs!!.getCompressInstance(zipFile, format, caseSensitive)
    }

    @Override
    fun getSessionCluster(): Boolean {
        return cs!!.getSessionCluster()
    }

    @Override
    fun getClientCluster(): Boolean {
        return cs!!.getClientCluster()
    }

    @Override
    override fun getClientStorage(): String? {
        return cs!!.getClientStorage()
    }

    @Override
    override fun getSessionStorage(): String? {
        return cs!!.getSessionStorage()
    }

    @Override
    override fun getDebugEntries(): Array<DebugEntry?>? {
        return cs!!.getDebugEntries()
    }

    @Override
    override fun getDebugEntry(ip: String?, defaultValue: DebugEntry?): DebugEntry? {
        return cs!!.getDebugEntry(ip, defaultValue)
    }

    @Override
    override fun getDebugMaxRecordsLogged(): Int {
        return cs!!.getDebugMaxRecordsLogged()
    }

    @Override
    override fun getDotNotationUpperCase(): Boolean {
        return cs!!.getDotNotationUpperCase()
    }

    @Override
    override fun preserveCase(): Boolean {
        return cs!!.preserveCase()
    }

    @Override
    override fun getDefaultFunctionOutput(): Boolean {
        return cs!!.getDefaultFunctionOutput()
    }

    @Override
    override fun getSuppressWSBeforeArg(): Boolean {
        return cs!!.getSuppressWSBeforeArg()
    }

    @Override
    fun getRestSetting(): RestSettings? {
        return cs!!.getRestSetting()
    }

    @Override
    override fun getCFMLWriterType(): Int {
        return cs!!.getCFMLWriterType()
    }

    @Override
    override fun getBufferOutput(): Boolean {
        return cs!!.getBufferOutput()
    }

    @Override
    override fun hasDebugOptions(debugOption: Int): Boolean {
        return cs!!.hasDebugOptions(debugOption)
    }

    @Override
    override fun checkForChangesInConfigFile(): Boolean {
        return cs!!.checkForChangesInConfigFile()
    }

    @Override
    override fun getExternalizeStringGTE(): Int {
        return cs!!.getExternalizeStringGTE()
    }

    @Override
    override fun getLoggers(): Map<String?, LoggerAndSourceData?>? {
        return cs!!.getLoggers()
    }

    @Override
    fun getLog(name: String?): Log? {
        return cs!!.getLog(name)
    }

    @Override
    @Throws(PageException::class)
    override fun getLog(name: String?, createIfNecessary: Boolean): Log? {
        return cs!!.getLog(name, createIfNecessary)
    }

    @Override
    override fun getTagDefaultAttributeValues(): Map<Key?, Map<Key?, Object?>?>? {
        return cs!!.getTagDefaultAttributeValues()
    }

    @Override
    fun getHandleUnQuotedAttrValueAsString(): Boolean? {
        return cs!!.getHandleUnQuotedAttrValueAsString()
    }

    @Override
    fun getCachedWithin(type: Int): Object? {
        return cs!!.getCachedWithin(type)
    }

    @Override
    override fun getPluginDirectory(): Resource? {
        return cs!!.getPluginDirectory()
    }

    @Override
    override fun getLogDirectory(): Resource? {
        return cs!!.getLogDirectory()
    }

    @Override
    override fun getSalt(): String? {
        return cs!!.getSalt()
    }

    @Override
    override fun getPasswordType(): Int {
        return cs!!.getPasswordType()
    }

    @Override
    override fun getPasswordSalt(): String? {
        return cs!!.getPasswordSalt()
    }

    @Override
    override fun getPasswordOrigin(): Int {
        return cs!!.getPasswordOrigin()
    }

    @Override
    override fun getExtensionBundleDefintions(): Collection<BundleDefinition?>? {
        return cs!!.getExtensionBundleDefintions()
    }

    @Override
    override fun getJDBCDrivers(): Array<JDBCDriver?>? {
        return cs!!.getJDBCDrivers()
    }

    @Override
    override fun getJDBCDriverByClassName(className: String?, defaultValue: JDBCDriver?): JDBCDriver? {
        return cs!!.getJDBCDriverByClassName(className, defaultValue)
    }

    @Override
    override fun getJDBCDriverById(id: String?, defaultValue: JDBCDriver?): JDBCDriver? {
        return cs!!.getJDBCDriverById(id, defaultValue)
    }

    @Override
    override fun getJDBCDriverByBundle(bundleName: String?, version: Version?, defaultValue: JDBCDriver?): JDBCDriver? {
        return cs!!.getJDBCDriverByBundle(bundleName, version, defaultValue)
    }

    @Override
    override fun getJDBCDriverByCD(cd: ClassDefinition?, defaultValue: JDBCDriver?): JDBCDriver? {
        return cs!!.getJDBCDriverByCD(cd, defaultValue)
    }

    @Override
    override fun getQueueMax(): Int {
        return cs!!.getQueueMax()
    }

    @Override
    override fun getQueueTimeout(): Long {
        return cs!!.getQueueTimeout()
    }

    @Override
    override fun getQueueEnable(): Boolean {
        return cs!!.getQueueEnable()
    }

    @Override
    override fun getCGIScopeReadonly(): Boolean {
        return cs!!.getCGIScopeReadonly()
    }

    @Override
    fun getDeployDirectory(): Resource? {
        return cs!!.getDeployDirectory()
    }

    @Override
    override fun allowTachyonDialect(): Boolean {
        return cs!!.allowTachyonDialect()
    }

    @Override
    override fun getCacheDefinitions(): Map<String?, ClassDefinition?>? {
        return cs!!.getCacheDefinitions()
    }

    @Override
    override fun getCacheDefinition(className: String?): ClassDefinition? {
        return cs!!.getCacheDefinition(className)
    }

    @Override
    override fun getAntiSamyPolicy(): Resource? {
        return cs!!.getAntiSamyPolicy()
    }

    @Override
    override fun getLogEngine(): LogEngine? {
        return cs!!.getLogEngine()
    }

    @Override
    override fun getCachedAfterTimeRange(): TimeSpan? {
        return cs!!.getCachedAfterTimeRange()
    }

    @Override
    override fun getStartups(): Map<String?, Startup?>? {
        return cs!!.getStartups()
    }

    @Override
    override fun getRegex(): Regex? {
        return cs!!.getRegex()
    }

    @Override
    override fun getServerRHExtensions(): Array<RHExtension?>? {
        return cs!!.getRHExtensions()
    }

    @Override
    @Throws(PageException::class)
    override fun createClusterScope(): Cluster? {
        return cs!!.createClusterScope()
    }

    @Override
    override fun getApplicationPageSource(pc: PageContext?, path: String?, filename: String?, mode: Int, isCFC: RefBoolean?): PageSource? {
        return cs!!.getApplicationPageSource(pc, path, filename, mode, isCFC)
    }

    @Override
    override fun putApplicationPageSource(path: String?, ps: PageSource?, filename: String?, mode: Int, isCFC: Boolean) {
        cs!!.putApplicationPageSource(path, ps, filename, mode, isCFC)
    }

    @Override
    override fun getAllExtensionBundleDefintions(): Collection<BundleDefinition?>? {
        return cs!!.getAllExtensionBundleDefintions()
    }

    @Override
    @Throws(PageException::class)
    override fun checkPassword() {
        cs!!.checkPassword()
    }

    @Override
    override fun loadLocalExtensions(validate: Boolean): List<ExtensionDefintion?>? {
        return cs!!.loadLocalExtensions(validate)
    }

    @Override
    override fun getAllRHExtensions(): Collection<RHExtension?>? {
        return cs!!.getAllRHExtensions()
    }

    @Override
    fun allowRequestTimeout(): Boolean {
        return cs!!.allowRequestTimeout()
    }

    @Override
    override fun closeConnection(): Boolean {
        return cs!!.closeConnection()
    }

    @Override
    fun checkPermGenSpace(check: Boolean) {
        cs!!.checkPermGenSpace(check)
    }

    @Override
    @Throws(PageException::class)
    fun getActionMonitor(arg0: String?): ActionMonitor? {
        return cs!!.getActionMonitor(arg0)
    }

    @Override
    @Throws(PageException::class)
    fun getConfigServer(arg0: String?): ConfigServer? {
        return cs!!.getConfigServer(arg0)
    }

    @Override
    @Throws(PageException::class)
    fun getConfigServer(arg0: String?, arg1: Long): ConfigServer? {
        return cs!!.getConfigServer(arg0, arg1)
    }

    @Override
    fun getFullNullSupport(): Boolean {
        return cs!!.getFullNullSupport()
    }

    @Override
    fun getIdentification(): IdentificationWeb? {
        if (id == null) id = SCCWIdentificationWeb(cs!!.getIdentification())
        return id
    }

    @Override
    @Throws(PageException::class)
    fun getIntervallMonitor(arg0: String?): IntervallMonitor? {
        return cs!!.getIntervallMonitor(arg0)
    }

    @Override
    fun getIntervallMonitors(): Array<IntervallMonitor?>? {
        return cs!!.getIntervallMonitors()
    }

    @Override
    fun getLocalExtensionProviderDirectory(): Resource? {
        return cs!!.getLocalExtensionProviderDirectory()
    }

    @Override
    fun getLoginCaptcha(): Boolean {
        return cs!!.getLoginCaptcha()
    }

    @Override
    fun getLoginDelay(): Int {
        return cs!!.getLoginDelay()
    }

    @Override
    fun getRememberMe(): Boolean {
        return cs!!.getRememberMe()
    }

    @Override
    @Throws(PageException::class)
    fun getRequestMonitor(arg0: String?): RequestMonitor? {
        return cs!!.getRequestMonitor(arg0)
    }

    @Override
    fun getRequestMonitors(): Array<RequestMonitor?>? {
        return cs!!.getRequestMonitors()
    }

    @Override
    fun getSecurityDirectory(): Resource? {
        return cs!!.getSecurityDirectory()
    }

    @Override
    fun getThreadQueue(): ThreadQueue? {
        return cs!!.getThreadQueue()
    }

    @Override
    fun hasServerPassword(): Boolean {
        return cs!!.hasServerPassword()
    }

    @Override
    fun isMonitoringEnabled(): Boolean {
        return cs!!.isMonitoringEnabled()
    }

    @Override
    fun getAMFEngine(): AMFEngine? {
        return helper!!.getAMFEngine()
    }

    @Override
    @Throws(PageException::class)
    fun getConfigServer(password: Password?): ConfigServer? {
        cs!!.checkAccess(password)
        return cs
    }

    @Override
    fun getConfigServerDir(): Resource? {
        return cs!!.getConfigDir()
    }

    @Override
    fun getFactory(): CFMLFactory? {
        return factory
    }

    @Override
    fun getLabel(): String? {
        return helper!!.getLabel()
    }

    @Override
    fun getLockManager(): LockManager? {
        return helper!!.getLockManager()
    }

    @Override
    @Throws(PageException::class)
    fun getSearchEngine(pc: PageContext?): SearchEngine? {
        return helper!!.getSearchEngine(pc)
    }

    @Override
    fun getWriter(pc: PageContext?, req: HttpServletRequest?, rsp: HttpServletResponse?): JspWriter? {
        return getCFMLWriter(pc, req, rsp)
    }

    @Override
    fun getInitParameter(name: String?): String? {
        return config.getInitParameter(name)
    }

    @Override
    fun getInitParameterNames(): Enumeration<String?>? {
        return config.getInitParameterNames()
    }

    @Override
    fun getServletContext(): ServletContext? {
        return config.getServletContext()
    }

    @Override
    override fun getApplicationPathCacheTimeout(): Long {
        return cs!!.getApplicationPathCacheTimeout()
    }

    @Override
    fun getServletName(): String? {
        return config.getServletName()
    }

    @Override
    override fun getDefaultServerTagMapping(): Mapping? {
        return cs!!.defaultTagMapping
    }

    // FYI used by Extensions, do not remove
    fun getApplicationMapping(virtual: String?, physical: String?): Mapping? {
        return getApplicationMapping("application", virtual, physical, null, true, false)
    }

    @Override
    override fun getApplicationMapping(type: String?, virtual: String?, physical: String?, archive: String?, physicalFirst: Boolean, ignoreVirtual: Boolean): Mapping? {
        return getApplicationMapping(type, virtual, physical, archive, physicalFirst, ignoreVirtual, true, true)
    }

    @Override
    override fun getServerFunctionMappings(): Collection<Mapping?>? {
        return helper!!.getServerFunctionMappings()
    }

    @Override
    override fun getServerFunctionMapping(mappingName: String?): Mapping? {
        return helper!!.getServerFunctionMapping(mappingName)
    }

    @Override
    override fun getServerTagMappings(): Collection<Mapping?>? {
        return helper!!.getServerTagMappings()
    }

    @Override
    override fun getServerTagMapping(mappingName: String?): Mapping? {
        return helper!!.getServerTagMapping(mappingName)
    }

    @Override
    override fun getAllLabels(): Map<String?, String?>? {
        return cs!!.getLabels()
    }

    @Override
    override fun isDefaultPassword(): Boolean {
        // TODO no sure about this
        return false
    }

    @Override
    override fun getAdminMode(): Short {
        return cs!!.getAdminMode()
    }

    @Override
    override fun getServerPasswordType(): Int {
        return cs!!.getPasswordType()
    }

    @Override
    override fun getServerPasswordSalt(): String? {
        return cs!!.getPasswordSalt()
    }

    @Override
    override fun getServerPasswordOrigin(): Int {
        return cs!!.getPasswordOrigin()
    }

    @Override
    override fun getGatewayEngine(): GatewayEngine? {
        return helper!!.getGatewayEngineImpl()
    }

    @Override
    @Throws(PageException::class)
    override fun getWSHandler(): WSHandler? {
        return helper!!.getWSHandler()
    }

    @Override
    override fun getCompiler(): CFMLCompilerImpl? {
        return helper!!.getCompiler()
    }

    @Override
    override fun getApplicationMapping(type: String?, virtual: String?, physical: String?, archive: String?, physicalFirst: Boolean, ignoreVirtual: Boolean,
                                       checkPhysicalFromWebroot: Boolean, checkArchiveFromWebroot: Boolean): Mapping? {
        return helper!!.getApplicationMapping(type, virtual, physical, archive, physicalFirst, ignoreVirtual, checkPhysicalFromWebroot, checkArchiveFromWebroot)
    }

    @Override
    override fun getApplicationMappings(): Array<Mapping?>? {
        return helper!!.getApplicationMappings()
    }

    @Override
    override fun isApplicationMapping(mapping: Mapping?): Boolean {
        return helper!!.isApplicationMapping(mapping)
    }

    @Override
    @Throws(PageException::class)
    override fun getBaseComponentPage(dialect: Int, pc: PageContext?): CIPage? {
        return helper!!.getBaseComponentPage(dialect, pc)
    }

    @Override
    override fun resetBaseComponentPage() {
        helper!!.resetBaseComponentPage()
    }

    @Override
    override fun getActionMonitorCollector(): ActionMonitorCollector? {
        return cs!!.getActionMonitorCollector()
    }

    @Override
    override fun getContextLock(): KeyLock<String?>? {
        return helper!!.getContextLock()
    }

    @Override
    fun getCacheHandlerCollection(type: Int, defaultValue: CacheHandlerCollection?): CacheHandlerCollection? {
        return helper!!.getCacheHandlerCollection(type, defaultValue)
    }

    @Override
    override fun releaseCacheHandlers(pc: PageContext?) {
        helper!!.releaseCacheHandlers(pc)
    }

    @Override
    override fun getDebuggerPool(): DebuggerPool? {
        return helper!!.getDebuggerPool()
    }

    @Override
    override fun getCFMLWriter(pc: PageContext?, req: HttpServletRequest?, rsp: HttpServletResponse?): CFMLWriter? {
        return helper!!.getCFMLWriter(pc, req, rsp)
    }

    @Override
    override fun getTagHandlerPool(): TagHandlerPool? {
        return helper!!.getTagHandlerPool()
    }

    @Override
    override fun getHash(): String? {
        return SystemUtil.hash(getServletContext())
    }

    @Override
    @Throws(PageException::class)
    override fun updatePassword(server: Boolean, passwordOld: String?, passwordNew: String?) {
        try {
            PasswordImpl.updatePassword(cs, passwordOld, passwordNew)
        } catch (e: Exception) {
            throw Caster.toPageException(e)
        }
    }

    @Override
    override fun updatePasswordIfNecessary(server: Boolean, passwordRaw: String?): Password? {
        return PasswordImpl.updatePasswordIfNecessary(cs, cs!!.password, passwordRaw)
    }

    @Override
    override fun isServerPasswordEqual(password: String?): Password? {
        return cs!!.isPasswordEqual(password)
    }

    @Override
    override fun hasIndividualSecurityManager(): Boolean {
        return false
    }

    @Override
    override fun getPasswordSource(): Short {
        return ConfigWebImpl.PASSWORD_ORIGIN_SERVER
    }

    @Override
    fun reset() {
        helper!!.reset()
    }

    @Override
    override fun setPassword(pw: Password?) {
        cs!!.setPassword(pw)
    }

    private class SCCWIdentificationWeb(id: IdentificationServer?) : IdentificationWeb, Serializable {
        private val id: IdentificationServer?
        @Override
        fun getApiKey(): String? {
            return id.getApiKey()
        }

        @Override
        fun getId(): String? {
            return id.getId()
        }

        @Override
        fun getSecurityKey(): String? {
            return id.getSecurityKey()
        }

        @Override
        fun getSecurityToken(): String? {
            return id.getSecurityToken()
        }

        @Override
        fun toQueryString(): String? {
            return id.toQueryString()
        }

        @Override
        fun getServerIdentification(): IdentificationServer? {
            return id
        }

        companion object {
            private const val serialVersionUID = -9020697769127921035L
        }

        init {
            this.id = id
        }
    }

    fun reload() {
        synchronized(this) { createMapping() }
    }

    private fun createMapping() {
        val existing: Map<String?, Mapping?>? = getExistingMappings()

        // Mapping
        val mappings: Map<String?, Mapping?> = MapFactory.< String, Mapping>getConcurrentMap<String?, Mapping?>()
        var tmp: Mapping
        var finished = false
        var ex: Mapping?
        val sm: Array<Mapping?> = cs!!.getMappings()
        if (sm != null) {
            for (i in sm.indices) {
                if (!sm[i].isHidden()) {
                    if ("/".equals(sm[i].getVirtual())) finished = true
                    ex = existing!![sm[i].getVirtualLowerCase()]
                    if (ex != null && ex.equals(sm[i])) {
                        mappings.put(ex.getVirtualLowerCase(), ex)
                    } else if (sm[i] is MappingImpl) {
                        tmp = (sm[i] as MappingImpl?).cloneReadOnly(this)
                        mappings.put(tmp.getVirtualLowerCase(), tmp)
                    } else {
                        tmp = sm[i]
                        mappings.put(tmp.getVirtualLowerCase(), tmp)
                    }
                }
            }
        }
        if (!finished) {
            var m: Mapping?
            if (ResourceUtil.isUNCPath(getRootDirectory().getPath())) {
                m = MappingImpl(this, "/", getRootDirectory().getPath(), null, ConfigPro.INSPECT_UNDEFINED, true, true, true, true, false, false, null, -1, -1)
            } else {
                m = MappingImpl(this, "/", "/", null, ConfigPro.INSPECT_UNDEFINED, true, true, true, true, false, false, null, -1, -1, true, true)
            }
            ex = existing!!["/"]
            if (ex != null && ex.equals(m)) {
                m = ex
            }
            mappings.put("/", m)
        }
        this.mappings = ConfigWebUtil.sort(mappings.values().toArray(arrayOfNulls<Mapping?>(mappings.size())))
    }

    private fun getExistingMappings(): Map<String?, Mapping?>? {
        val mappings: Map<String?, Mapping?> = MapFactory.< String, Mapping>getConcurrentMap<String?, Mapping?>()
        if (this.mappings != null) {
            for (m in this.mappings!!) {
                mappings.put(m.getVirtualLowerCase(), m)
            }
        }
        return mappings
    }

    @Override
    override fun removeDatasourceConnectionPool(ds: DataSource?) {
        cs!!.removeDatasourceConnectionPool(ds)
    }

    @Override
    override fun getDatasourceConnectionPool(): MockPool? {
        return cs!!.getDatasourceConnectionPool()
    }

    @Override
    override fun getPreciseMath(): Boolean {
        return cs!!.getPreciseMath()
    }

    @Override
    override fun resetServerFunctionMappings() {
    }

    // private Resource remoteClientDirectory;
    // private SpoolerEngineImpl spoolerEngine;
    init {
        factory.setConfig(this)
        this.factory = factory
        this.cs = cs
        this.config = config
        val frp: ResourceProvider = ResourcesImpl.getFileResourceProvider()
        rootDir = frp.getResource(ReqRspUtil.getRootPath(config.getServletContext()))

        // Fix for tomcat
        if (rootDir.getName().equals(".") || rootDir.getName().equals("..")) rootDir = rootDir.getParentResource()
        helper = ConfigWebHelper(cs, this)
        reload()
    }
}