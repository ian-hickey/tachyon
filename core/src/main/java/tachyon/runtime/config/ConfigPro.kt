package tachyon.runtime.config

import java.io.IOException

// FUTURE add to Config not necessary all of them!
interface ConfigPro : Config {
    fun getCacheHandlers(): Iterator<Entry<String?, Class<CacheHandler?>?>?>?
    fun getDotNotationUpperCase(): Boolean
    fun getExecutionLogEnabled(): Boolean
    fun getSuppressWSBeforeArg(): Boolean
    fun getDefaultFunctionOutput(): Boolean
    fun getCoreTagLib(dialect: Int): TagLib?
    fun getTLDs(dialect: Int): Array<TagLib?>?
    fun getFLDs(dialect: Int): Array<FunctionLib?>?
    fun getFunctionMappings(): Collection<Mapping?>?
    fun getFunctionMapping(mappingName: String?): Mapping?
    fun getTagMappings(): Collection<Mapping?>?
    fun getDefaultTagMapping(): Mapping?
    fun getTagMapping(mappingName: String?): Mapping?
    fun getRHExtensions(): Array<RHExtension?>?
    fun getPasswordType(): Int
    fun getPasswordSalt(): String?
    fun preserveCase(): Boolean
    fun hasDebugOptions(debugOption: Int): Boolean
    fun getDebugMaxRecordsLogged(): Int
    fun getComponentLocalSearch(): Boolean
    fun getComponentRootSearch(): Boolean
    fun getRestMappings(): Array<tachyon.runtime.rest.Mapping?>?
    fun getFromFunctionCache(key: String?): UDF?
    fun putToFunctionCache(key: String?, udf: UDF?)
    fun getServerRHExtensions(): Array<RHExtension?>?

    // zhis only exists for the hibernate extension that uses this
    fun getDatasourceConnectionPool(): MockPool?
    fun getDatasourceConnectionPool(ds: tachyon.runtime.db.DataSource?, user: String?, pass: String?): DatasourceConnPool?
    fun getDatasourceConnectionPools(): Collection<DatasourceConnPool?>?
    fun removeDatasourceConnectionPool(ds: DataSource?)
    fun clearCTCache()
    fun clearFunctionCache()
    fun getSessionScopeDir(): Resource?
    fun getRegex(): Regex?
    fun closeConnection(): Boolean
    fun getBaseComponentPageSource(dialect: Int, pc: PageContext?): PageSource?
    fun getCachedAfterTimeRange(): TimeSpan?

    @Throws(PageException::class)
    fun getLog(name: String?, createIfNecessary: Boolean): Log?
    fun getLoggers(): Map<String?, LoggerAndSourceData?>?
    fun isSuppressContent(): Boolean
    fun allowCompression(): Boolean
    fun getTypeChecking(): Boolean
    fun getResourceClassLoader(): ResourceClassLoader?

    @Throws(IOException::class)
    fun getRPCClassLoader(reload: Boolean, parents: Array<ClassLoader?>?): ClassLoader?
    fun toPageSource(mappings: Array<Mapping?>?, res: Resource?, defaultValue: PageSource?): PageSource?
    fun getRestList(): Boolean
    fun getExecutionLogFactory(): ExecutionLogFactory?
    fun debugLogOutput(): Boolean
    fun getExternalizeStringGTE(): Int
    fun allowTachyonDialect(): Boolean
    fun getCombinedFLDs(dialect: Int): FunctionLib?

    @Throws(PageException::class)
    fun createClusterScope(): Cluster?
    fun getClassLoaderCore(): ClassLoader?
    fun getLogDirectory(): Resource?
    fun getLogEngine(): LogEngine?
    fun getUseTimeServer(): Boolean
    fun useComponentPathCache(): Boolean
    fun getComponentDefaultImport(): ImportDefintion?
    fun doComponentDeepSearch(): Boolean
    fun isDevelopMode(): Boolean
    fun getCGIScopeReadonly(): Boolean
    fun getSessionStorage(): String?
    fun getClientStorage(): String?
    fun getBufferOutput(): Boolean
    fun getCFMLWriterType(): Int
    fun contentLength(): Boolean
    fun getQueueEnable(): Boolean
    fun getQueueTimeout(): Long
    fun getQueueMax(): Int
    fun isAllowURLRequestTimeout(): Boolean
    fun getPluginDirectory(): Resource?
    fun getDefaultFunctionMapping(): Mapping?
    fun getEventGatewayDirectory(): Resource?
    fun clearComponentCache()
    fun clearApplicationCache()
    fun getStartups(): Map<String?, ConfigBase.Startup?>?

    @Throws(ClassException::class)
    fun getAdminSync(): AdminSync?
    fun isPasswordEqual(password: String?): Password?
    fun getJDBCDrivers(): Array<JDBCDriver?>?

    /**
     * get the extension bundle definition not only from this context, get it from all contexts,
     * including the server context
     *
     * @return
     */
    fun getAllExtensionBundleDefintions(): Collection<BundleDefinition?>?
    fun useCTPathCache(): Boolean
    fun getORMConfig(): ORMConfiguration?
    fun getORMEngineClassDefintion(): ClassDefinition<out ORMEngine?>?
    fun getLibraryDirectory(): Resource?
    fun getClassesDirectory(): Resource?
    fun getRHExtensionProviders(): Array<RHExtensionProvider?>?
    fun getDebugEntries(): Array<DebugEntry?>?
    fun getDebugEntry(ip: String?, defaultValue: DebugEntry?): DebugEntry?
    fun getQueryVarUsage(): Int

    @Throws(PageException::class)
    fun checkPassword()
    fun getSerialNumber(): String?

    @Throws(PageException::class)
    fun resetORMEngine(pc: PageContext?, force: Boolean): ORMEngine?

    // FUTURE add to interface
    fun isMailSendPartial(): Boolean

    // FUTURE add to interface and impl
    fun isUserset(): Boolean
    fun getResourceCharSet(): CharSet?
    fun getWebCharSet(): CharSet?
    fun getCacheDefinitions(): Map<String?, ClassDefinition?>?
    fun getCacheDefinition(className: String?): ClassDefinition?
    fun getAntiSamyPolicy(): Resource?
    fun getTagDefaultAttributeValues(): Map<Key?, Map<Key?, Object?>?>?
    fun getResourceProviderFactories(): Array<ResourceProviderFactory?>?
    fun hasResourceProvider(scheme: String?): Boolean
    fun listCTCache(): Struct?
    fun listComponentCache(): Struct?
    fun getClassLoaderEnv(): ClassLoader?
    fun getJDBCDriverById(id: String?, defaultValue: JDBCDriver?): JDBCDriver?
    fun getJDBCDriverByBundle(bundleName: String?, version: Version?, defaultValue: JDBCDriver?): JDBCDriver?
    fun getJDBCDriverByCD(cd: ClassDefinition?, defaultValue: JDBCDriver?): JDBCDriver?
    fun getJDBCDriverByClassName(className: String?, defaultValue: JDBCDriver?): JDBCDriver?
    fun getCTInitFile(pc: PageContext?, key: String?): InitFile?
    fun putCTInitFile(key: String?, initFile: InitFile?)

    @Throws(TemplateException::class)
    fun getCachedPage(pc: PageContext?, pathWithCFC: String?): CIPage?
    fun putCachedPageSource(pathWithCFC: String?, ps: PageSource?)

    /**
     *
     * @param validate if true Tachyon checks if the file is a valid zip file
     * @return
     */
    fun loadLocalExtensions(validate: Boolean): List<ExtensionDefintion?>?
    fun getResourceClassLoader(defaultValue: ResourceClassLoader?): ResourceClassLoader?

    @Throws(IOException::class)
    fun getCompressInstance(zipFile: Resource?, format: Int, caseSensitive: Boolean): Compress?
    fun getPasswordOrigin(): Int
    fun getSalt(): String?
    fun getExtensionBundleDefintions(): Collection<BundleDefinition?>?
    fun checkForChangesInConfigFile(): Boolean
    fun lastModified(): Long
    fun getAllRHExtensions(): Collection<RHExtension?>?
    fun setPassword(pw: Password?)
    fun getAdminMode(): Short
    fun getApplicationPageSource(pc: PageContext?, path: String?, filename: String?, mode: Int, isCFC: RefBoolean?): PageSource?
    fun putApplicationPageSource(path: String?, ps: PageSource?, filename: String?, mode: Int, isCFC: Boolean)
    fun getApplicationPathCacheTimeout(): Long
    fun getPreciseMath(): Boolean

    companion object {
        const val CLIENT_BOOLEAN_TRUE = 0
        const val CLIENT_BOOLEAN_FALSE = 1
        const val SERVER_BOOLEAN_TRUE = 2
        const val SERVER_BOOLEAN_FALSE = 3
        const val DEBUG_DATABASE = 1
        const val DEBUG_EXCEPTION = 2
        const val DEBUG_TRACING = 4
        const val DEBUG_TIMER = 8
        const val DEBUG_IMPLICIT_ACCESS = 16
        const val DEBUG_QUERY_USAGE = 32
        const val DEBUG_DUMP = 64
        const val DEBUG_TEMPLATE = 128
        const val DEBUG_THREAD = 256
        const val MODE_CUSTOM = 1
        const val MODE_STRICT = 2
        const val CFML_WRITER_REFULAR = 1
        const val CFML_WRITER_WS = 2
        const val CFML_WRITER_WS_PREF = 3
        val DEFAULT_STORAGE_SESSION: String? = "memory"
        val DEFAULT_STORAGE_CLIENT: String? = "cookie"
        const val QUERY_VAR_USAGE_UNDEFINED = 0
        const val QUERY_VAR_USAGE_IGNORE = 1
        const val QUERY_VAR_USAGE_WARN = 2
        const val QUERY_VAR_USAGE_ERROR = 4
    }
}