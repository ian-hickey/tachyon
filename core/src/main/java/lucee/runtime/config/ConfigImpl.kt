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
package lucee.runtime.config

import lucee.runtime.db.DatasourceManagerImpl.QOQ_DATASOURCE_NAME

/**
 * Hold the definitions of the Lucee configuration.
 */
abstract class ConfigImpl protected constructor(configDir: Resource?, configFile: Resource?) : ConfigBase(), ConfigPro {
    private var mode: Int = MODE_CUSTOM
    private val rpcClassLoaders: Map<String?, PhysicalClassLoader?>? = ConcurrentHashMap<String?, PhysicalClassLoader?>()
    private var datasources: Map<String?, DataSource?>? = HashMap<String?, DataSource?>()
    private var caches: Map<String?, CacheConnection?>? = HashMap<String?, CacheConnection?>()
    private var defaultCacheFunction: CacheConnection? = null
    private var defaultCacheObject: CacheConnection? = null
    private var defaultCacheTemplate: CacheConnection? = null
    private var defaultCacheQuery: CacheConnection? = null
    private var defaultCacheResource: CacheConnection? = null
    private var defaultCacheInclude: CacheConnection? = null
    private var defaultCacheHTTP: CacheConnection? = null
    private var defaultCacheFile: CacheConnection? = null
    private var defaultCacheWebservice: CacheConnection? = null
    private var cacheDefaultConnectionNameFunction: String? = null
    private var cacheDefaultConnectionNameObject: String? = null
    private var cacheDefaultConnectionNameTemplate: String? = null
    private var cacheDefaultConnectionNameQuery: String? = null
    private var cacheDefaultConnectionNameResource: String? = null
    private var cacheDefaultConnectionNameInclude: String? = null
    private var cacheDefaultConnectionNameHTTP: String? = null
    private var cacheDefaultConnectionNameFile: String? = null
    private var cacheDefaultConnectionNameWebservice: String? = null
    private var cfmlTlds: Array<TagLib?>? = arrayOfNulls<TagLib?>(0)
    private var luceeTlds: Array<TagLib?>? = arrayOfNulls<TagLib?>(0)
    private var cfmlFlds: Array<FunctionLib?>? = arrayOfNulls<FunctionLib?>(0)
    private var luceeFlds: Array<FunctionLib?>? = arrayOfNulls<FunctionLib?>(0)
    private var combinedCFMLFLDs: FunctionLib? = null
    private var combinedLuceeFLDs: FunctionLib? = null
    private var type: Short = SCOPE_STANDARD
    private var _allowImplicidQueryCall = true
    private var _mergeFormAndURL = false
    private val loggers: Map<String?, LoggerAndSourceData?>? = HashMap<String?, LoggerAndSourceData?>()
    private var _debug = 0
    private var debugLogOutput: Int = SERVER_BOOLEAN_FALSE
    private var debugOptions = 0
    private var suppresswhitespace = false
    private var suppressContent = false
    private var showVersion = false
    private var tempDirectory: Resource? = null
    private var clientTimeout: TimeSpan? = TimeSpanImpl(90, 0, 0, 0)
    private var sessionTimeout: TimeSpan? = TimeSpanImpl(0, 0, 30, 0)
    private var applicationTimeout: TimeSpan? = TimeSpanImpl(1, 0, 0, 0)
    private var requestTimeout: TimeSpan? = TimeSpanImpl(0, 0, 0, 30)
    private var sessionManagement = true
    private var clientManagement = false
    private var clientCookies = true
    private var developMode = false
    private var domainCookies = false
    private val configFile: Resource?
    private val configDir: Resource?
    private var sessionStorage: String? = DEFAULT_STORAGE_SESSION
    private var clientStorage: String? = DEFAULT_STORAGE_CLIENT
    private var loadTime: Long = 0
    private var spoolInterval = 30
    private var spoolEnable = true
    private var sendPartial = false
    private var userSet = true
    private var mailServers: Array<Server?>?
    private var mailTimeout = 30
    private var timeZone: TimeZone? = null
    private var timeServer: String? = ""
    private var useTimeServer = true
    private var timeOffset: Long = 0
    private var searchEngineClassDef: ClassDefinition<SearchEngine?>? = null
    private var searchEngineDirectory: String? = null
    private var locale: Locale? = null
    private var psq = false
    private val debugShowUsage = false
    private val errorTemplates: Map<String?, String?>? = HashMap<String?, String?>()
    var password: Password? = null
    private var salt: String? = null
    private var mappings: Array<Mapping?>? = arrayOfNulls<Mapping?>(0)
    private var customTagMappings: Array<Mapping?>? = arrayOfNulls<Mapping?>(0)
    private var componentMappings: Array<Mapping?>? = arrayOfNulls<Mapping?>(0)
    private var scheduler: SchedulerImpl? = null
    private var cfxTagPool: CFXTagPool? = null
    private var baseComponentPageSourceCFML: PageSource? = null
    private var baseComponentTemplateCFML: String? = null
    private var baseComponentPageSourceLucee: PageSource? = null
    private var baseComponentTemplateLucee: String? = null
    private var restList = false
    private var clientType: Short = CLIENT_SCOPE_TYPE_COOKIE
    private var componentDumpTemplate: String? = null
    private var componentDataMemberDefaultAccess: Int = Component.ACCESS_PRIVATE
    private var triggerComponentDataMember = false
    private var sessionType: Short = SESSION_TYPE_APPLICATION
    private var deployDirectory: Resource? = null
    private var compileType: Short = RECOMPILE_NEVER
    private var resourceCharset: CharSet? = SystemUtil.getCharSet()
    private var templateCharset: CharSet? = SystemUtil.getCharSet()
    private var webCharset: CharSet? = CharSet.UTF8
    private var mailDefaultCharset: CharSet? = CharSet.UTF8
    private var tldFile: Resource? = null
    private var fldFile: Resource? = null
    private val resources: Resources? = ResourcesImpl()
    private val cacheHandlerClasses: Map<String?, Class<CacheHandler?>?>? = HashMap<String?, Class<CacheHandler?>?>()
    private var applicationListener: ApplicationListener? = null
    private var scriptProtect: Int = ApplicationContext.SCRIPT_PROTECT_ALL
    private var proxy: ProxyData? = null
    private var clientScopeDir: Resource? = null
    private var sessionScopeDir: Resource? = null
    private var clientScopeDirSize = (1024 * 1024 * 10).toLong()
    private val sessionScopeDirSize = (1024 * 1024 * 10).toLong()
    private var cacheDir: Resource? = null
    private var cacheDirSize = (1024 * 1024 * 10).toLong()
    private var useComponentShadow = true
    private var out: PrintWriter? = SystemUtil.getPrintWriter(SystemUtil.OUT)
    private var err: PrintWriter? = SystemUtil.getPrintWriter(SystemUtil.ERR)
    private val pools: Map<String?, DatasourceConnPool?>? = HashMap()
    private var doCustomTagDeepSearch = false
    private var doComponentTagDeepSearch = false
    private var version = 1.0
    private var closeConnection = false
    private var contentLength = true
    private var allowCompression = false
    private var doLocalCustomTag = true
    private var constants: Struct? = null
    private var remoteClients: Array<RemoteClient?>?
    private var remoteClientSpoolerEngine: SpoolerEngine? = null
    private var remoteClientDirectory: Resource? = null
    private var allowURLRequestTimeout = false
    private var errorStatusCode = true
    private var localMode: Int = Undefined.MODE_LOCAL_OR_ARGUMENTS_ONLY_WHEN_EXISTS
    private var rhextensionProviders: Array<RHExtensionProvider?>? = Constants.RH_EXTENSION_PROVIDERS
    private var rhextensions: Array<RHExtension?>? = RHEXTENSIONS_EMPTY
    private var allowRealPath = true
    private var dmpWriterEntries: Array<DumpWriterEntry?>?
    private var clusterClass: Class? = ClusterNotSupported::class.java // ClusterRemoteNotSupported.class;//
    private var remoteClientUsage: Struct? = null
    private var adminSyncClass: Class? = AdminSyncNotSupported::class.java
    private var adminSync: AdminSync? = null
    private var customTagExtensions: Array<String?>? = Constants.getExtensions()
    private var videoExecuterClass: Class? = VideoExecuterNotSupported::class.java
    protected var scriptMapping: MappingImpl? = null

    // private Resource tagDirectory;
    var defaultFunctionMapping: Mapping? = null
    var functionMappings: Map<String?, Mapping?>? = ConcurrentHashMap<String?, Mapping?>()
    var defaultTagMapping: Mapping? = null
    var tagMappings: Map<String?, Mapping?>? = ConcurrentHashMap<String?, Mapping?>()
    private var inspectTemplate: Short = INSPECT_ONCE
    private var typeChecking = true
    private var cacheMD5: String? = null
    private var executionLogEnabled = false
    private var executionLogFactory: ExecutionLogFactory? = null
    private val ormengines: Map<String?, ORMEngine?>? = HashMap<String?, ORMEngine?>()
    private var cdORMEngine: ClassDefinition<out ORMEngine?>? = null
    private var ormConfig: ORMConfiguration? = null
    private var resourceCL: ResourceClassLoader? = null
    private var componentDefaultImport: ImportDefintion? = ImportDefintionImpl(Constants.DEFAULT_PACKAGE, "*")
    private var componentLocalSearch = true
    private var componentRootSearch = true
    private var useComponentPathCache = true
    private var useCTPathCache = true
    private var restMappings: Array<lucee.runtime.rest.Mapping?>?
    protected var writerType: Int = CFML_WRITER_REFULAR
    private var configFileLastModified: Long = 0
    private var checkForChangesInConfigFile = false

    // protected String apiKey=null;
    private val consoleLayouts: List? = ArrayList()
    private val resourceLayouts: List? = ArrayList()
    private var tagDefaultAttributeValues: Map<Key?, Map<Key?, Object?>?>? = null
    private var handleUnQuotedAttrValueAsString = true
    private val cachedWithins: Map<Integer?, Object?>? = HashMap<Integer?, Object?>()
    private var queueMax = 100
    private var queueTimeout: Long = 0
    private var queueEnable = false
    private var varUsage = 0
    private var cachedAfterTimeRange: TimeSpan? = null
    private var regex // TODO add possibility to configure
            : Regex? = null
    private var applicationPathCacheTimeout: Long = Caster.toLongValue(SystemUtil.getSystemPropOrEnvVar("lucee.application.path.cache.timeout", null), 20000)
    private var envClassLoader: ClassLoader? = null
    private var preciseMath = true

    /**
     * @return the allowURLRequestTimeout
     */
    @Override
    override fun isAllowURLRequestTimeout(): Boolean {
        return allowURLRequestTimeout
    }

    /**
     * @param allowURLRequestTimeout the allowURLRequestTimeout to set
     */
    fun setAllowURLRequestTimeout(allowURLRequestTimeout: Boolean) {
        this.allowURLRequestTimeout = allowURLRequestTimeout
    }

    @Override
    fun getCompileType(): Short {
        return compileType
    }

    @Override
    fun reset() {
        timeServer = ""
        componentDumpTemplate = ""
        // resources.reset();
        ormengines.clear()
        compressResources.clear()
        clearFunctionCache()
        clearCTCache()
        clearComponentCache()
        clearApplicationCache()
        // clearComponentMetadata();
    }

    @Override
    fun reloadTimeServerOffset() {
        timeOffset = 0
        if (useTimeServer && !StringUtil.isEmpty(timeServer, true)) {
            val ntp = NtpClient(timeServer)
            timeOffset = ntp.getOffset(0)
        }
    }

    @Override
    override fun lastModified(): Long {
        return configFileLastModified
    }

    fun setLastModified() {
        configFileLastModified = configFile.lastModified()
    }

    @Override
    fun getScopeCascadingType(): Short {
        return type
    }

    /*
	 * @Override public String[] getCFMLExtensions() { return getAllExtensions(); }
	 * 
	 * @Override public String getCFCExtension() { return getComponentExtension(); }
	 * 
	 * @Override public String[] getAllExtensions() { return Constants.ALL_EXTENSION; }
	 * 
	 * @Override public String getComponentExtension() { return Constants.COMPONENT_EXTENSION; }
	 * 
	 * @Override public String[] getTemplateExtensions() { return Constants.TEMPLATE_EXTENSIONS; }
	 */
    fun setFLDs(flds: Array<FunctionLib?>?, dialect: Int) {
        if (dialect == CFMLEngine.DIALECT_CFML) {
            cfmlFlds = flds
            combinedCFMLFLDs = null // TODO improve check (hash)
        } else {
            luceeFlds = flds
            combinedLuceeFLDs = null // TODO improve check (hash)
        }
    }

    /**
     * return all Function Library Deskriptors
     *
     * @return Array of Function Library Deskriptors
     */
    @Override
    override fun getFLDs(dialect: Int): Array<FunctionLib?>? {
        return if (dialect == CFMLEngine.DIALECT_CFML) cfmlFlds else luceeFlds
    }

    @Override
    override fun getCombinedFLDs(dialect: Int): FunctionLib? {
        if (dialect == CFMLEngine.DIALECT_CFML) {
            if (combinedCFMLFLDs == null) combinedCFMLFLDs = FunctionLibFactory.combineFLDs(cfmlFlds)
            return combinedCFMLFLDs
        }
        if (combinedLuceeFLDs == null) combinedLuceeFLDs = FunctionLibFactory.combineFLDs(luceeFlds)
        return combinedLuceeFLDs
    }

    /**
     * return all Tag Library Deskriptors
     *
     * @return Array of Tag Library Deskriptors
     */
    @Override
    override fun getTLDs(dialect: Int): Array<TagLib?>? {
        return if (dialect == CFMLEngine.DIALECT_CFML) cfmlTlds else luceeTlds
    }

    fun setTLDs(tlds: Array<TagLib?>?, dialect: Int) {
        if (dialect == CFMLEngine.DIALECT_CFML) cfmlTlds = tlds else luceeTlds = tlds
    }

    @Override
    fun allowImplicidQueryCall(): Boolean {
        return _allowImplicidQueryCall
    }

    @Override
    fun mergeFormAndURL(): Boolean {
        return _mergeFormAndURL
    }

    @Override
    fun getApplicationTimeout(): TimeSpan? {
        return applicationTimeout
    }

    @Override
    fun getSessionTimeout(): TimeSpan? {
        return sessionTimeout
    }

    @Override
    fun getClientTimeout(): TimeSpan? {
        return clientTimeout
    }

    @Override
    fun getRequestTimeout(): TimeSpan? {
        return requestTimeout
    }

    @Override
    fun isClientCookies(): Boolean {
        return clientCookies
    }

    @Override
    override fun isDevelopMode(): Boolean {
        return developMode
    }

    @Override
    fun isClientManagement(): Boolean {
        return clientManagement
    }

    @Override
    fun isDomainCookies(): Boolean {
        return domainCookies
    }

    @Override
    fun isSessionManagement(): Boolean {
        return sessionManagement
    }

    @Override
    fun isMailSpoolEnable(): Boolean {
        return spoolEnable
    }

    // FUTURE add to interface
    @Override
    override fun isMailSendPartial(): Boolean {
        return sendPartial
    }

    // FUTURE add to interface and impl
    @Override
    override fun isUserset(): Boolean {
        return userSet
    }

    @Override
    fun getMailServers(): Array<Server?>? {
        if (mailServers == null) mailServers = arrayOfNulls<Server?>(0)
        return mailServers
    }

    @Override
    fun getMailTimeout(): Int {
        return mailTimeout
    }

    @Override
    fun getPSQL(): Boolean {
        return psq
    }

    fun setQueryVarUsage(varUsage: Int) {
        this.varUsage = varUsage
    }

    @Override
    override fun getQueryVarUsage(): Int {
        return varUsage
    }

    @Override
    fun getClassLoader(): ClassLoader? {
        val rcl: ResourceClassLoader? = getResourceClassLoader(null)
        return if (rcl != null) rcl else ClassLoaderHelper().getClass().getClassLoader()
    }

    // do not remove, ised in Hibernate extension
    @Override
    override fun getClassLoaderEnv(): ClassLoader? {
        if (envClassLoader == null) envClassLoader = EnvClassLoader(this)
        return envClassLoader
    }

    @Override
    override fun getClassLoaderCore(): ClassLoader? {
        return ClassLoaderHelper().getClass().getClassLoader()
    }

    /*
	 * public ClassLoader getClassLoaderLoader() { return new TP().getClass().getClassLoader(); }
	 */
    @Override
    override fun getResourceClassLoader(): ResourceClassLoader? {
        if (resourceCL == null) throw RuntimeException("no RCL defined yet!")
        return resourceCL
    }

    @Override
    override fun getResourceClassLoader(defaultValue: ResourceClassLoader?): ResourceClassLoader? {
        return if (resourceCL == null) defaultValue else resourceCL
    }

    fun setResourceClassLoader(resourceCL: ResourceClassLoader?) {
        this.resourceCL = resourceCL
    }

    @Override
    fun getLocale(): Locale? {
        return locale
    }

    @Override
    fun debug(): Boolean {
        return if (!(_debug == CLIENT_BOOLEAN_TRUE || _debug == SERVER_BOOLEAN_TRUE)) false else true
    }

    @Override
    override fun debugLogOutput(): Boolean {
        return debug() && debugLogOutput == CLIENT_BOOLEAN_TRUE || debugLogOutput == SERVER_BOOLEAN_TRUE
    }

    @Override
    fun getTempDirectory(): Resource? {
        if (tempDirectory == null) {
            val tmp: Resource = SystemUtil.getTempDirectory()
            if (!tmp.exists()) tmp.mkdirs()
            return tmp
        }
        if (!tempDirectory.exists()) tempDirectory.mkdirs()
        return tempDirectory
    }

    @Override
    fun getMailSpoolInterval(): Int {
        return spoolInterval
    }

    @Override
    fun getTimeZone(): TimeZone? {
        return timeZone
    }

    @Override
    fun getTimeServerOffset(): Long {
        return timeOffset
    }

    /**
     * @return return the Scheduler
     */
    @Override
    fun getScheduler(): Scheduler? {
        return scheduler
    }

    /**
     * @return gets the password as hash
     */
    fun getPassword(): Password? {
        return password
    }

    @Override
    override fun isPasswordEqual(password: String?): Password? {
        return if (this.password == null) null else (this.password as PasswordImpl?)!!.isEqual(this, password)
    }

    @Override
    fun hasPassword(): Boolean {
        return password != null
    }

    @Override
    fun passwordEqual(password: Password?): Boolean {
        return if (this.password == null) false else this.password.equals(password)
    }

    @Override
    fun getMappings(): Array<Mapping?>? {
        return mappings
    }

    @Override
    override fun getRestMappings(): Array<lucee.runtime.rest.Mapping?>? {
        if (restMappings == null) restMappings = arrayOfNulls<lucee.runtime.rest.Mapping?>(0)
        return restMappings
    }

    fun setRestMappings(restMappings: Array<lucee.runtime.rest.Mapping?>?) {

        // make sure only one is default
        var hasDefault = false
        var m: lucee.runtime.rest.Mapping?
        for (i in restMappings.indices) {
            m = restMappings!![i]
            if (m!!.isDefault()) {
                if (hasDefault) m.setDefault(false)
                hasDefault = true
            }
        }
        this.restMappings = restMappings
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
        return getPageSources(pc, mappings, realPath, onlyTopLevel, useSpecialMappings, useDefaultMapping, false, onlyFirstMatch)
    }

    @Override
    fun getPageSources(pc: PageContext?, mappings: Array<Mapping?>?, realPath: String?, onlyTopLevel: Boolean, useSpecialMappings: Boolean, useDefaultMapping: Boolean,
                       useComponentMappings: Boolean): Array<PageSource?>? {
        return getPageSources(pc, mappings, realPath, onlyTopLevel, useSpecialMappings, useDefaultMapping, useComponentMappings, onlyFirstMatch)
    }

    fun getPageSources(pc: PageContext?, appMappings: Array<Mapping?>?, realPath: String?, onlyTopLevel: Boolean, useSpecialMappings: Boolean, useDefaultMapping: Boolean,
                       useComponentMappings: Boolean, onlyFirstMatch: Boolean): Array<PageSource?>? {
        return ConfigWebUtil.getPageSources(pc, this, appMappings, realPath, onlyTopLevel, useSpecialMappings, useDefaultMapping, useComponentMappings, onlyFirstMatch)
    }

    /**
     * @param mappings
     * @param realPath
     * @param alsoDefaultMapping ignore default mapping (/) or not
     * @return physical path from mapping
     */
    @Override
    fun getPhysical(mappings: Array<Mapping?>?, realPath: String?, alsoDefaultMapping: Boolean): Resource? {
        throw PageRuntimeException(DeprecatedException("method not supported"))
    }

    @Override
    fun getPhysicalResources(pc: PageContext?, mappings: Array<Mapping?>?, realPath: String?, onlyTopLevel: Boolean, useSpecialMappings: Boolean, useDefaultMapping: Boolean): Array<Resource?>? {
        // now that archives can be used the same way as physical resources, there is no need anymore to
        // limit to that FUTURE remove
        throw PageRuntimeException(DeprecatedException("method not supported"))
    }

    @Override
    fun getPhysicalResourceExisting(pc: PageContext?, mappings: Array<Mapping?>?, realPath: String?, onlyTopLevel: Boolean, useSpecialMappings: Boolean, useDefaultMapping: Boolean): Resource? {
        // now that archives can be used the same way as physical resources, there is no need anymore to
        // limit to that FUTURE remove
        throw PageRuntimeException(DeprecatedException("method not supported"))
    }

    @Override
    override fun toPageSource(mappings: Array<Mapping?>?, res: Resource?, defaultValue: PageSource?): PageSource? {
        return ConfigWebUtil.toPageSource(this, mappings, res, defaultValue)
    }

    @Override
    fun getConfigDir(): Resource? {
        return configDir
    }

    @Override
    fun getConfigFile(): Resource? {
        return configFile
    }

    /**
     * sets the password
     *
     * @param password
     */
    @Override
    override fun setPassword(password: Password?) {
        this.password = password
    }

    /**
     * set how lucee cascade scopes
     *
     * @param type cascading type
     */
    fun setScopeCascadingType(type: Short) {
        this.type = type
    }

    fun addTag(nameSpace: String?, nameSpaceSeperator: String?, name: String?, dialect: Int, cd: ClassDefinition?) {
        if (dialect == CFMLEngine.DIALECT_BOTH) {
            addTag(nameSpace, nameSpaceSeperator, name, CFMLEngine.DIALECT_CFML, cd)
            addTag(nameSpace, nameSpaceSeperator, name, CFMLEngine.DIALECT_LUCEE, cd)
            return
        }
        val tlds: Array<TagLib?>? = if (dialect == CFMLEngine.DIALECT_CFML) cfmlTlds else luceeTlds
        for (i in tlds.indices) {
            if (tlds!![i].getNameSpaceAndSeparator().equalsIgnoreCase(nameSpace + nameSpaceSeperator)) {
                val tlt = TagLibTag(tlds[i])
                tlt.setAttributeType(TagLibTag.ATTRIBUTE_TYPE_DYNAMIC)
                tlt.setBodyContent("free")
                tlt.setTagClassDefinition(cd)
                tlt.setName(name)
                tlds[i].setTag(tlt)
            }
        }
    }

    /**
     * set the optional directory of the tag library deskriptors
     *
     * @param fileTld directory of the tag libray deskriptors
     * @throws TagLibException
     */
    @Throws(TagLibException::class)
    fun setTldFile(fileTld: Resource?, dialect: Int) {
        if (dialect == CFMLEngine.DIALECT_BOTH) {
            setTldFile(fileTld, CFMLEngine.DIALECT_CFML)
            setTldFile(fileTld, CFMLEngine.DIALECT_LUCEE)
            return
        }
        var tlds: Array<TagLib?>? = if (dialect == CFMLEngine.DIALECT_CFML) cfmlTlds else luceeTlds
        if (fileTld == null) return
        tldFile = fileTld
        var key: String?
        val map: Map<String?, TagLib?> = HashMap<String?, TagLib?>()
        // First fill existing to set
        for (i in tlds.indices) {
            key = getKey(tlds!![i])
            map.put(key, tlds!![i])
        }
        var tl: TagLib

        // now overwrite with new data
        if (fileTld.isDirectory()) {
            val files: Array<Resource?> = fileTld.listResources(ExtensionResourceFilter(arrayOf<String?>("tld", "tldx")))
            for (i in files.indices) {
                try {
                    tl = TagLibFactory.loadFromFile(files[i], getIdentification())
                    key = getKey(tl)
                    if (!map.containsKey(key)) map.put(key, tl) else overwrite(map[key], tl)
                } catch (tle: TagLibException) {
                    LogUtil.log(this, Log.LEVEL_ERROR, "loading", "can't load tld " + files[i])
                    tle.printStackTrace(getErrWriter())
                }
            }
        } else if (fileTld.isFile()) {
            tl = TagLibFactory.loadFromFile(fileTld, getIdentification())
            key = getKey(tl)
            if (!map.containsKey(key)) map.put(key, tl) else overwrite(map[key], tl)
        }

        // now fill back to array
        tlds = arrayOfNulls<TagLib?>(map.size())
        if (dialect == CFMLEngine.DIALECT_CFML) cfmlTlds = tlds else luceeTlds = tlds
        var index = 0
        val it: Iterator<TagLib?> = map.values().iterator()
        while (it.hasNext()) {
            tlds!![index++] = it.next()
        }
    }

    @Override
    override fun getCoreTagLib(dialect: Int): TagLib? {
        val tlds: Array<TagLib?>? = if (dialect == CFMLEngine.DIALECT_CFML) cfmlTlds else luceeTlds
        for (i in tlds.indices) {
            if (tlds!![i].isCore()) return tlds[i]
        }
        throw RuntimeException("no core taglib found") // this should never happen
    }

    fun setTagDirectory(listTagDirectory: List<Path?>?) {
        val it: Iterator<Path?> = listTagDirectory!!.iterator()
        var index = -1
        var mappingName: String
        var path: Path?
        var m: Mapping?
        var isDefault: Boolean
        while (it.hasNext()) {
            path = it.next()
            index++
            isDefault = index == 0
            mappingName = "/mapping-tag" + (if (isDefault) "" else index).toString() + ""
            m = MappingImpl(this, mappingName, if (path.isValidDirectory()) path.res.getAbsolutePath() else path.str, null, ConfigPro.INSPECT_NEVER, true, true, true, true, false,
                    true, null, -1, -1)
            if (isDefault) defaultTagMapping = m
            tagMappings.put(mappingName, m)
            val tlc: TagLib? = getCoreTagLib(CFMLEngine.DIALECT_CFML)
            val tll: TagLib? = getCoreTagLib(CFMLEngine.DIALECT_LUCEE)

            // now overwrite with new data
            if (path.res.isDirectory()) {
                val files: Array<String?> = path.res.list(ExtensionResourceFilter(if (getMode() == ConfigPro.MODE_STRICT) Constants.getComponentExtensions() else Constants.getExtensions()))
                for (i in files.indices) {
                    if (tlc != null) createTag(tlc, files[i], mappingName)
                    if (tll != null) createTag(tll, files[i], mappingName)
                }
            }
        }
    }

    fun createTag(tl: TagLib?, filename: String?, mappingName: String?) { // Jira 1298
        val name = toName(filename) // filename.substring(0,filename.length()-(getCFCExtension().length()+1));
        val tlt = TagLibTag(tl)
        tlt.setName(name)
        tlt.setTagClassDefinition("lucee.runtime.tag.CFTagCore", getIdentification(), null)
        tlt.setHandleExceptions(true)
        tlt.setBodyContent("free")
        tlt.setParseBody(false)
        tlt.setDescription("")
        tlt.setAttributeType(TagLibTag.ATTRIBUTE_TYPE_MIXED)

        // read component and read setting from that component
        val tlts = TagLibTagScript(tlt)
        tlts.setType(TagLibTagScript.TYPE_MULTIPLE)
        tlt.setScript(tlts)
        var tlta: TagLibTagAttr? = TagLibTagAttr(tlt)
        tlta.setName("__filename")
        tlta.setRequired(true)
        tlta.setRtexpr(true)
        tlta.setType("string")
        tlta.setHidden(true)
        tlta.setDefaultValue(filename)
        tlt.setAttribute(tlta)
        tlta = TagLibTagAttr(tlt)
        tlta.setName("__name")
        tlta.setRequired(true)
        tlta.setRtexpr(true)
        tlta.setHidden(true)
        tlta.setType("string")
        tlta.setDefaultValue(name)
        tlt.setAttribute(tlta)
        tlta = TagLibTagAttr(tlt)
        tlta.setName("__isweb")
        tlta.setRequired(true)
        tlta.setRtexpr(true)
        tlta.setHidden(true)
        tlta.setType("boolean")
        tlta.setDefaultValue(if (this is ConfigWeb) "true" else "false")
        tlt.setAttribute(tlta)
        tlta = TagLibTagAttr(tlt)
        tlta.setName("__mapping")
        tlta.setRequired(true)
        tlta.setRtexpr(true)
        tlta.setHidden(true)
        tlta.setType("string")
        tlta.setDefaultValue(mappingName)
        tlt.setAttribute(tlta)
        tl.setTag(tlt)
    }

    fun setFunctionDirectory(listFunctionDirectory: List<Path?>?) {
        val it: Iterator<Path?> = listFunctionDirectory!!.iterator()
        var index = -1
        var mappingName: String
        var path: Path?
        var isDefault: Boolean
        while (it.hasNext()) {
            path = it.next()
            index++
            isDefault = index == 0
            mappingName = "/mapping-function" + (if (isDefault) "" else index).toString() + ""
            val mapping = MappingImpl(this, mappingName, if (path.isValidDirectory()) path.res.getAbsolutePath() else path.str, null, ConfigPro.INSPECT_NEVER, true, true,
                    true, true, false, true, null, -1, -1)
            if (isDefault) defaultFunctionMapping = mapping
            functionMappings.put(mappingName, mapping)
            val flc: FunctionLib? = cfmlFlds!![cfmlFlds!!.size - 1]
            val fll: FunctionLib? = luceeFlds!![luceeFlds!!.size - 1]

            // now overwrite with new data
            if (path.res.isDirectory()) {
                val files: Array<String?> = path.res.list(ExtensionResourceFilter(Constants.getTemplateExtensions()))
                for (file in files) {
                    if (flc != null) createFunction(flc, file, mappingName)
                    if (fll != null) createFunction(fll, file, mappingName)
                }
                combinedCFMLFLDs = null
                combinedLuceeFLDs = null
            }
        }
    }

    fun createFunction(fl: FunctionLib?, filename: String?, mapping: String?) {
        val name = toName(filename) // filename.substring(0,filename.length()-(getCFMLExtensions().length()+1));
        val flf = FunctionLibFunction(fl, true)
        flf.setArgType(FunctionLibFunction.ARG_DYNAMIC)
        flf.setFunctionClass("lucee.runtime.functions.system.CFFunction", null, null)
        flf.setName(name)
        flf.setReturn("object")
        var arg: FunctionLibFunctionArg? = FunctionLibFunctionArg(flf)
        arg.setName("__filename")
        arg.setRequired(true)
        arg.setType("string")
        arg.setHidden(true)
        arg.setDefaultValue(filename)
        flf.setArg(arg)
        arg = FunctionLibFunctionArg(flf)
        arg.setName("__name")
        arg.setRequired(true)
        arg.setHidden(true)
        arg.setType("string")
        arg.setDefaultValue(name)
        flf.setArg(arg)
        arg = FunctionLibFunctionArg(flf)
        arg.setName("__isweb")
        arg.setRequired(true)
        arg.setHidden(true)
        arg.setType("boolean")
        arg.setDefaultValue(if (this is ConfigWeb) "true" else "false")
        flf.setArg(arg)
        arg = FunctionLibFunctionArg(flf)
        arg.setName("__mapping")
        arg.setRequired(true)
        arg.setHidden(true)
        arg.setType("string")
        arg.setDefaultValue(mapping)
        flf.setArg(arg)
        fl.setFunction(flf)
    }

    private fun overwrite(existingTL: TagLib?, newTL: TagLib?) {
        val it: Iterator<TagLibTag?> = newTL.getTags().values().iterator()
        while (it.hasNext()) {
            existingTL.setTag(it.next())
        }
    }

    private fun getKey(tl: TagLib?): String? {
        return tl.getNameSpaceAndSeparator().toLowerCase()
    }

    @Throws(FunctionLibException::class)
    fun setFldFile(fileFld: Resource?, dialect: Int) {
        if (dialect == CFMLEngine.DIALECT_BOTH) {
            setFldFile(fileFld, CFMLEngine.DIALECT_CFML)
            setFldFile(fileFld, CFMLEngine.DIALECT_LUCEE)
            return
        }
        var flds: Array<FunctionLib?>? = if (dialect == CFMLEngine.DIALECT_CFML) cfmlFlds else luceeFlds

        // merge all together (backward compatibility)
        if (flds!!.size > 1) for (i in 1 until flds.size) {
            overwrite(flds[0], flds[i])
        }
        flds = arrayOf<FunctionLib?>(flds[0])
        if (dialect == CFMLEngine.DIALECT_CFML) {
            cfmlFlds = flds
            if (cfmlFlds != flds) combinedCFMLFLDs = null // TODO improve check
        } else {
            luceeFlds = flds
            if (luceeFlds != flds) combinedLuceeFLDs = null // TODO improve check
        }
        if (fileFld == null) return
        fldFile = fileFld

        // overwrite with additional functions
        var fl: FunctionLib
        if (fileFld.isDirectory()) {
            val files: Array<Resource?> = fileFld.listResources(ExtensionResourceFilter(arrayOf<String?>("fld", "fldx")))
            for (i in files.indices) {
                try {
                    fl = FunctionLibFactory.loadFromFile(files[i], getIdentification())
                    overwrite(flds[0], fl)
                } catch (fle: FunctionLibException) {
                    LogUtil.log(this, Log.LEVEL_ERROR, "loading", "can't load fld " + files[i])
                    fle.printStackTrace(getErrWriter())
                }
            }
        } else {
            fl = FunctionLibFactory.loadFromFile(fileFld, getIdentification())
            overwrite(flds[0], fl)
        }
    }

    private fun overwrite(existingFL: FunctionLib?, newFL: FunctionLib?) {
        val it: Iterator<FunctionLibFunction?> = newFL.getFunctions().values().iterator()
        while (it.hasNext()) {
            existingFL.setFunction(it.next())
        }
    }

    private fun getKey(functionLib: FunctionLib?): String? {
        return functionLib.getDisplayName().toLowerCase()
    }

    /**
     * sets if it is allowed to implict query call, call a query member without define name of the
     * query.
     *
     * @param _allowImplicidQueryCall is allowed
     */
    fun setAllowImplicidQueryCall(_allowImplicidQueryCall: Boolean) {
        this._allowImplicidQueryCall = _allowImplicidQueryCall
    }

    /**
     * sets if url and form scope will be merged
     *
     * @param _mergeFormAndURL merge yes or no
     */
    fun setMergeFormAndURL(_mergeFormAndURL: Boolean) {
        this._mergeFormAndURL = _mergeFormAndURL
    }

    /**
     * @param strApplicationTimeout The applicationTimeout to set.
     * @throws PageException
     */
    @Throws(PageException::class)
    fun setApplicationTimeout(strApplicationTimeout: String?) {
        setApplicationTimeout(Caster.toTimespan(strApplicationTimeout))
    }

    /**
     * @param applicationTimeout The applicationTimeout to set.
     */
    protected fun setApplicationTimeout(applicationTimeout: TimeSpan?) {
        this.applicationTimeout = applicationTimeout
    }

    /**
     * @param strSessionTimeout The sessionTimeout to set.
     * @throws PageException
     */
    @Throws(PageException::class)
    protected fun setSessionTimeout(strSessionTimeout: String?) {
        setSessionTimeout(Caster.toTimespan(strSessionTimeout))
    }

    /**
     * @param sessionTimeout The sessionTimeout to set.
     */
    protected fun setSessionTimeout(sessionTimeout: TimeSpan?) {
        this.sessionTimeout = sessionTimeout
    }

    @Throws(PageException::class)
    protected fun setClientTimeout(strClientTimeout: String?) {
        setClientTimeout(Caster.toTimespan(strClientTimeout))
    }

    /**
     * @param clientTimeout The sessionTimeout to set.
     */
    protected fun setClientTimeout(clientTimeout: TimeSpan?) {
        this.clientTimeout = clientTimeout
    }

    /**
     * @param strRequestTimeout The requestTimeout to set.
     * @throws PageException
     */
    @Throws(PageException::class)
    protected fun setRequestTimeout(strRequestTimeout: String?) {
        setRequestTimeout(Caster.toTimespan(strRequestTimeout))
    }

    /**
     * @param requestTimeout The requestTimeout to set.
     */
    protected fun setRequestTimeout(requestTimeout: TimeSpan?) {
        this.requestTimeout = requestTimeout
    }

    /**
     * @param clientCookies The clientCookies to set.
     */
    fun setClientCookies(clientCookies: Boolean) {
        this.clientCookies = clientCookies
    }

    /**
     * @param developMode
     */
    fun setDevelopMode(developMode: Boolean) {
        this.developMode = developMode
    }

    /**
     * @param clientManagement The clientManagement to set.
     */
    fun setClientManagement(clientManagement: Boolean) {
        this.clientManagement = clientManagement
    }

    /**
     * @param domainCookies The domainCookies to set.
     */
    fun setDomainCookies(domainCookies: Boolean) {
        this.domainCookies = domainCookies
    }

    /**
     * @param sessionManagement The sessionManagement to set.
     */
    fun setSessionManagement(sessionManagement: Boolean) {
        this.sessionManagement = sessionManagement
    }

    /**
     * @param spoolEnable The spoolEnable to set.
     */
    fun setMailSpoolEnable(spoolEnable: Boolean) {
        this.spoolEnable = spoolEnable
    }

    fun setMailSendPartial(sendPartial: Boolean) {
        this.sendPartial = sendPartial
    }

    fun setUserSet(userSet: Boolean) {
        this.userSet = userSet
    }

    /**
     * @param mailTimeout The mailTimeout to set.
     */
    fun setMailTimeout(mailTimeout: Int) {
        this.mailTimeout = mailTimeout
    }

    /**
     * @param psq (preserve single quote) sets if sql string inside a cfquery will be preserved for
     * Single Quotes
     */
    fun setPSQL(psq: Boolean) {
        this.psq = psq
    }

    /**
     * set if lucee make debug output or not
     *
     * @param _debug debug or not
     */
    protected fun setDebug(_debug: Int) {
        this._debug = _debug
    }

    protected fun setDebugLogOutput(debugLogOutput: Int) {
        this.debugLogOutput = debugLogOutput
    }

    /**
     * sets the temp directory
     *
     * @param strTempDirectory temp directory
     * @throws ExpressionException
     */
    @Throws(ExpressionException::class)
    protected fun setTempDirectory(strTempDirectory: String?, flush: Boolean) {
        setTempDirectory(resources.getResource(strTempDirectory), flush)
    }

    /**
     * sets the temp directory
     *
     * @param tempDirectory temp directory
     * @throws ExpressionException
     */
    @Throws(ExpressionException::class)
    protected fun setTempDirectory(tempDirectory: Resource?, flush: Boolean) {
        var tempDirectory: Resource? = tempDirectory
        if (!isDirectory(tempDirectory) || !tempDirectory.isWriteable()) {
            LogUtil.log(this, Log.LEVEL_ERROR, "loading",
                    "temp directory [" + tempDirectory + "] is not writable or can not be created, using directory [" + SystemUtil.getTempDirectory() + "] instead")
            tempDirectory = SystemUtil.getTempDirectory()
            if (!tempDirectory.isWriteable()) {
                LogUtil.log(this, Log.LEVEL_ERROR, "loading", "temp directory [$tempDirectory] is not writable")
            }
        }
        if (flush) ResourceUtil.removeChildrenEL(tempDirectory) // start with an empty temp directory
        this.tempDirectory = tempDirectory
    }

    /**
     * sets the Schedule Directory
     *
     * @param scheduleDirectory sets the schedule Directory
     * @param logger
     * @throws PageException
     */
    @Throws(PageException::class)
    protected fun setScheduler(engine: CFMLEngine?, scheduledTasks: Array?) {
        if (scheduledTasks == null) {
            if (scheduler == null) scheduler = SchedulerImpl(engine, this, ArrayImpl())
            return
        }
        try {
            if (scheduler == null) scheduler = SchedulerImpl(engine, this, scheduledTasks)
        } catch (e: Exception) {
            throw Caster.toPageException(e)
        }
    }

    /**
     * @param spoolInterval The spoolInterval to set.
     */
    fun setMailSpoolInterval(spoolInterval: Int) {
        this.spoolInterval = spoolInterval
    }

    /**
     * sets the timezone
     *
     * @param timeZone
     */
    fun setTimeZone(timeZone: TimeZone?) {
        this.timeZone = timeZone
    }

    /**
     * sets the time server
     *
     * @param timeServer
     */
    fun setTimeServer(timeServer: String?) {
        this.timeServer = timeServer
    }

    /**
     * sets the locale
     *
     * @param strLocale
     */
    protected fun setLocale(strLocale: String?) {
        if (strLocale == null) {
            locale = Locale.US
        } else {
            try {
                locale = Caster.toLocale(strLocale)
                if (locale == null) locale = Locale.US
            } catch (e: ExpressionException) {
                locale = Locale.US
            }
        }
    }

    /**
     * sets the locale
     *
     * @param locale
     */
    protected fun setLocale(locale: Locale?) {
        this.locale = locale
    }

    /**
     * @param mappings The mappings to set.
     */
    fun setMappings(mappings: Array<Mapping?>?) {
        this.mappings = ConfigWebUtil.sort(mappings)
    }

    /**
     * @param datasources The datasources to set
     */
    fun setDataSources(datasources: Map<String?, DataSource?>?) {
        this.datasources = datasources
    }

    /**
     * @param customTagMappings The customTagMapping to set.
     */
    fun setCustomTagMappings(customTagMappings: Array<Mapping?>?) {
        this.customTagMappings = customTagMappings
    }

    @Override
    fun getCustomTagMappings(): Array<Mapping?>? {
        return customTagMappings
    }

    /**
     * @param mailServers The mailsServers to set.
     */
    fun setMailServers(mailServers: Array<Server?>?) {
        this.mailServers = mailServers
    }

    /**
     * is file a directory or not, touch if not exist
     *
     * @param directory
     * @return true if existing directory or has created new one
     */
    protected fun isDirectory(directory: Resource?): Boolean {
        if (directory.exists()) return directory.isDirectory()
        try {
            directory.createDirectory(true)
            return true
        } catch (e: IOException) {
            e.printStackTrace(getErrWriter())
        }
        return false
    }

    @Override
    fun getLoadTime(): Long {
        return loadTime
    }

    /**
     * @param loadTime The loadTime to set.
     */
    fun setLoadTime(loadTime: Long) {
        this.loadTime = loadTime
    }

    /**
     * @return Returns the configLogger. / public Log getConfigLogger() { return configLogger; }
     */
    @Override
    @Throws(SecurityException::class)
    fun getCFXTagPool(): CFXTagPool? {
        return cfxTagPool
    }

    /**
     * @param cfxTagPool The customTagPool to set.
     */
    protected fun setCFXTagPool(cfxTagPool: CFXTagPool?) {
        this.cfxTagPool = cfxTagPool
    }

    /**
     * @param cfxTagPool The customTagPool to set.
     */
    protected fun setCFXTagPool(cfxTagPool: Map?) {
        this.cfxTagPool = CFXTagPoolImpl(cfxTagPool)
    }

    @Override
    fun getBaseComponentTemplate(dialect: Int): String? {
        return if (dialect == CFMLEngine.DIALECT_CFML) baseComponentTemplateCFML else baseComponentTemplateLucee
    }

    /**
     * @return pagesource of the base component
     */
    @Override
    fun getBaseComponentPageSource(dialect: Int): PageSource? {
        return getBaseComponentPageSource(dialect, ThreadLocalPageContext.get())
    }

    @Override
    override fun getBaseComponentPageSource(dialect: Int, pc: PageContext?): PageSource? {
        var base: PageSource = if (dialect == CFMLEngine.DIALECT_CFML) baseComponentPageSourceCFML else baseComponentPageSourceLucee
        if (base == null) {
            base = PageSourceImpl.best(getPageSources(pc, null, getBaseComponentTemplate(dialect), false, false, true))
            if (!base.exists()) {
                val baseTemplate = getBaseComponentTemplate(dialect)
                val mod: String = ContractPath.call(pc, baseTemplate, false)
                if (!mod.equals(baseTemplate)) {
                    base = PageSourceImpl.best(getPageSources(pc, null, mod, false, false, true))
                }
            }
            if (dialect == CFMLEngine.DIALECT_CFML) baseComponentPageSourceCFML = base else baseComponentPageSourceLucee = base
        }
        return base
    }

    /**
     * @param template The baseComponent template to set.
     */
    fun setBaseComponentTemplate(dialect: Int, template: String?) {
        if (dialect == CFMLEngine.DIALECT_CFML) {
            baseComponentPageSourceCFML = null
            baseComponentTemplateCFML = template
        } else {
            baseComponentPageSourceLucee = null
            baseComponentTemplateLucee = template
        }
    }

    fun setRestList(restList: Boolean) {
        this.restList = restList
    }

    @Override
    override fun getRestList(): Boolean {
        return restList
    }

    /**
     * @param clientType
     */
    fun setClientType(clientType: Short) {
        this.clientType = clientType
    }

    /**
     * @param strClientType
     */
    fun setClientType(strClientType: String?) {
        var strClientType = strClientType
        strClientType = strClientType.trim().toLowerCase()
        clientType = if (strClientType.equals("file")) Config.CLIENT_SCOPE_TYPE_FILE else if (strClientType.equals("db")) Config.CLIENT_SCOPE_TYPE_DB else if (strClientType.equals("database")) Config.CLIENT_SCOPE_TYPE_DB else Config.CLIENT_SCOPE_TYPE_COOKIE
    }

    @Override
    fun getClientType(): Short {
        return clientType
    }

    /**
     * @param searchEngine The searchEngine to set.
     */
    fun setSearchEngine(cd: ClassDefinition?, directory: String?) {
        searchEngineClassDef = cd
        searchEngineDirectory = directory
    }

    @Override
    fun getSearchEngineClassDefinition(): ClassDefinition<SearchEngine?>? {
        return searchEngineClassDef
    }

    @Override
    fun getSearchEngineDirectory(): String? {
        return searchEngineDirectory
    }

    @Override
    fun getComponentDataMemberDefaultAccess(): Int {
        return componentDataMemberDefaultAccess
    }

    /**
     * @param componentDataMemberDefaultAccess The componentDataMemberDefaultAccess to set.
     */
    fun setComponentDataMemberDefaultAccess(componentDataMemberDefaultAccess: Int) {
        this.componentDataMemberDefaultAccess = componentDataMemberDefaultAccess
    }

    @Override
    fun getTimeServer(): String? {
        return timeServer
    }

    @Override
    fun getComponentDumpTemplate(): String? {
        return componentDumpTemplate
    }

    /**
     * @param template The componentDump template to set.
     */
    fun setComponentDumpTemplate(template: String?) {
        componentDumpTemplate = template
    }

    fun createSecurityToken(): String? {
        return try {
            Md5.getDigestAsString(getConfigDir().getAbsolutePath())
        } catch (e: IOException) {
            null
        }
    }

    @Override
    fun getDebugTemplate(): String? {
        throw PageRuntimeException(DeprecatedException("no longer supported, use instead getDebugEntry(ip, defaultValue)"))
    }

    @Override
    fun getErrorTemplate(statusCode: Int): String? {
        return errorTemplates!![Caster.toString(statusCode)]
    }

    /**
     * @param errorTemplate the errorTemplate to set
     */
    fun setErrorTemplate(statusCode: Int, errorTemplate: String?) {
        errorTemplates.put(Caster.toString(statusCode), errorTemplate)
    }

    @Override
    fun getSessionType(): Short {
        return sessionType
    }

    /**
     * @param sessionType The sessionType to set.
     */
    fun setSessionType(sessionType: Short) {
        this.sessionType = sessionType
    }

    @Override
    abstract fun getUpdateType(): String?
    @Override
    abstract fun getUpdateLocation(): URL?
    @Override
    fun getClassDirectory(): Resource? {
        return deployDirectory
    }

    @Override
    override fun getLibraryDirectory(): Resource? {
        val dir: Resource = getConfigDir().getRealResource("lib")
        if (!dir.exists()) dir.mkdir()
        return dir
    }

    @Override
    override fun getEventGatewayDirectory(): Resource? {
        val dir: Resource = getConfigDir().getRealResource("context/admin/gdriver")
        if (!dir.exists()) dir.mkdir()
        return dir
    }

    @Override
    override fun getClassesDirectory(): Resource? {
        val dir: Resource = getConfigDir().getRealResource("classes")
        if (!dir.exists()) dir.mkdir()
        return dir
    }

    /**
     * set the deploy directory, directory where lucee deploy transalted cfml classes (java and class
     * files)
     *
     * @param strDeployDirectory deploy directory
     * @throws ExpressionException
     */
    @Throws(ExpressionException::class)
    protected fun setDeployDirectory(strDeployDirectory: String?) {
        setDeployDirectory(resources.getResource(strDeployDirectory))
    }

    /**
     * set the deploy directory, directory where lucee deploy transalted cfml classes (java and class
     * files)
     *
     * @param deployDirectory deploy directory
     * @throws ExpressionException
     * @throws ExpressionException
     */
    @Throws(ExpressionException::class)
    protected fun setDeployDirectory(deployDirectory: Resource?) {
        if (!isDirectory(deployDirectory)) {
            throw ExpressionException("deploy directory $deployDirectory doesn't exist or is not a directory")
        }
        this.deployDirectory = deployDirectory
    }

    @Override
    abstract fun getRootDirectory(): Resource?

    /**
     * sets the compileType value.
     *
     * @param compileType The compileType to set.
     */
    fun setCompileType(compileType: Short) {
        this.compileType = compileType
    }

    /**
     * FUTHER Returns the value of suppresswhitespace.
     *
     * @return value suppresswhitespace
     */
    @Override
    fun isSuppressWhitespace(): Boolean {
        return suppresswhitespace
    }

    /**
     * FUTHER sets the suppresswhitespace value.
     *
     * @param suppresswhitespace The suppresswhitespace to set.
     */
    protected fun setSuppressWhitespace(suppresswhitespace: Boolean) {
        this.suppresswhitespace = suppresswhitespace
    }

    @Override
    override fun isSuppressContent(): Boolean {
        return suppressContent
    }

    fun setSuppressContent(suppressContent: Boolean) {
        this.suppressContent = suppressContent
    }

    @Override
    fun getDefaultEncoding(): String? {
        return webCharset.name()
    }

    @Override
    fun getTemplateCharset(): Charset? {
        return CharsetUtil.toCharset(templateCharset)
    }

    fun getTemplateCharSet(): CharSet? {
        return templateCharset
    }

    /**
     * sets the charset to read the files
     *
     * @param templateCharset
     */
    protected fun setTemplateCharset(templateCharset: String?) {
        this.templateCharset = CharsetUtil.toCharSet(templateCharset, this.templateCharset)
    }

    protected fun setTemplateCharset(templateCharset: Charset?) {
        this.templateCharset = CharsetUtil.toCharSet(templateCharset)
    }

    @Override
    fun getWebCharset(): Charset? {
        return CharsetUtil.toCharset(webCharset)
    }

    @Override
    override fun getWebCharSet(): CharSet? {
        return webCharset
    }

    /**
     * sets the charset to read and write resources
     *
     * @param resourceCharset
     */
    protected fun setResourceCharset(resourceCharset: String?) {
        this.resourceCharset = CharsetUtil.toCharSet(resourceCharset, this.resourceCharset)
    }

    protected fun setResourceCharset(resourceCharset: Charset?) {
        this.resourceCharset = CharsetUtil.toCharSet(resourceCharset)
    }

    @Override
    fun getResourceCharset(): Charset? {
        return CharsetUtil.toCharset(resourceCharset)
    }

    @Override
    override fun getResourceCharSet(): CharSet? {
        return resourceCharset
    }

    /**
     * sets the charset for the response stream
     *
     * @param webCharset
     */
    protected fun setWebCharset(webCharset: String?) {
        this.webCharset = CharsetUtil.toCharSet(webCharset, this.webCharset)
    }

    protected fun setWebCharset(webCharset: Charset?) {
        this.webCharset = CharsetUtil.toCharSet(webCharset)
    }

    @Override
    fun getSecurityManager(): SecurityManager? {
        return null
    }

    @Override
    fun getFldFile(): Resource? {
        return fldFile
    }

    @Override
    fun getTldFile(): Resource? {
        return tldFile
    }

    @Override
    fun getDataSources(): Array<DataSource?>? {
        val map: Map<String?, DataSource?>? = getDataSourcesAsMap()
        val it: Iterator<DataSource?> = map!!.values().iterator()
        val ds: Array<DataSource?> = arrayOfNulls<DataSource?>(map.size())
        var count = 0
        while (it.hasNext()) {
            ds[count++] = it.next()
        }
        return ds
    }

    @Override
    fun getDataSourcesAsMap(): Map<String?, DataSource?>? {
        val map: Map<String?, DataSource?> = HashMap<String?, DataSource?>()
        val it: Iterator<Entry<String?, DataSource?>?> = datasources.entrySet().iterator()
        var entry: Entry<String?, DataSource?>?
        while (it.hasNext()) {
            entry = it.next()
            if (!entry.getKey().equals(QOQ_DATASOURCE_NAME)) map.put(entry.getKey(), entry.getValue())
        }
        return map
    }

    /**
     * @return the mailDefaultCharset
     */
    @Override
    fun getMailDefaultCharset(): Charset? {
        return mailDefaultCharset.toCharset()
    }

    fun getMailDefaultCharSet(): CharSet? {
        return mailDefaultCharset
    }

    /**
     * @param mailDefaultEncoding the mailDefaultCharset to set
     */
    protected fun setMailDefaultEncoding(mailDefaultCharset: String?) {
        this.mailDefaultCharset = CharsetUtil.toCharSet(mailDefaultCharset, this.mailDefaultCharset)
    }

    protected fun setMailDefaultEncoding(mailDefaultCharset: Charset?) {
        this.mailDefaultCharset = CharsetUtil.toCharSet(mailDefaultCharset)
    }

    @Throws(ClassException::class)
    fun setDefaultResourceProvider(defaultProviderClass: Class?, arguments: Map?) {
        val o: Object = ClassUtil.loadInstance(defaultProviderClass)
        if (o is ResourceProvider) {
            val rp: ResourceProvider = o as ResourceProvider
            rp.init(null, arguments)
            setDefaultResourceProvider(rp)
        } else throw ClassException("object [" + Caster.toClassName(o).toString() + "] must implement the interface " + ResourceProvider::class.java.getName())
    }

    /**
     * @param defaultResourceProvider the defaultResourceProvider to set
     */
    fun setDefaultResourceProvider(defaultResourceProvider: ResourceProvider?) {
        resources.registerDefaultResourceProvider(defaultResourceProvider)
    }

    /**
     * @return the defaultResourceProvider
     */
    @Override
    fun getDefaultResourceProvider(): ResourceProvider? {
        return resources.getDefaultResourceProvider()
    }

    @Throws(ClassException::class, BundleException::class)
    protected fun addCacheHandler(id: String?, cd: ClassDefinition<CacheHandler?>?) {
        val clazz: Class<CacheHandler?> = cd.getClazz()
        val o: Object = ClassUtil.loadInstance(clazz) // just try to load and forget afterwards
        if (o is CacheHandler) {
            addCacheHandler(id, clazz)
        } else throw ClassException("object [" + Caster.toClassName(o).toString() + "] must implement the interface " + CacheHandler::class.java.getName())
    }

    protected fun addCacheHandler(id: String?, chc: Class<CacheHandler?>?) {
        cacheHandlerClasses.put(id, chc)
    }

    @Override
    override fun getCacheHandlers(): Iterator<Entry<String?, Class<CacheHandler?>?>?>? {
        return cacheHandlerClasses.entrySet().iterator()
    }

    @Throws(ClassException::class, BundleException::class)
    fun addResourceProvider(strProviderScheme: String?, cd: ClassDefinition?, arguments: Map?) {
        (resources as ResourcesImpl?).registerResourceProvider(strProviderScheme, cd, arguments)
    }

    /*
	 * protected void addResourceProvider(ResourceProvider provider) {
	 * ((ResourcesImpl)resources).registerResourceProvider(provider); }
	 */
    fun clearResourceProviders() {
        resources.reset()
    }

    /**
     * @return return the resource providers
     */
    @Override
    fun getResourceProviders(): Array<ResourceProvider?>? {
        return resources.getResourceProviders()
    }

    /**
     * @return return the resource providers
     */
    @Override
    override fun getResourceProviderFactories(): Array<ResourceProviderFactory?>? {
        return (resources as ResourcesImpl?).getResourceProviderFactories()
    }

    @Override
    override fun hasResourceProvider(scheme: String?): Boolean {
        val factories: Array<ResourceProviderFactory?> = (resources as ResourcesImpl?).getResourceProviderFactories()
        for (i in factories.indices) {
            if (factories[i].getScheme().equalsIgnoreCase(scheme)) return true
        }
        return false
    }

    fun setResourceProviderFactories(resourceProviderFactories: Array<ResourceProviderFactory?>?) {
        for (i in resourceProviderFactories.indices) {
            (resources as ResourcesImpl?).registerResourceProvider(resourceProviderFactories!![i])
        }
    }

    @Override
    fun getResource(path: String?): Resource? {
        return resources.getResource(path)
    }

    @Override
    fun getApplicationListener(): ApplicationListener? {
        return applicationListener
    }

    /**
     * @param applicationListener the applicationListener to set
     */
    fun setApplicationListener(applicationListener: ApplicationListener?) {
        this.applicationListener = applicationListener
    }

    /**
     * @return the scriptProtect
     */
    @Override
    fun getScriptProtect(): Int {
        return scriptProtect
    }

    /**
     * @param scriptProtect the scriptProtect to set
     */
    fun setScriptProtect(scriptProtect: Int) {
        this.scriptProtect = scriptProtect
    }

    /**
     * @return the proxyPassword
     */
    @Override
    fun getProxyData(): ProxyData? {
        return proxy
    }

    /**
     * @param proxy the proxyPassword to set
     */
    fun setProxyData(proxy: ProxyData?) {
        this.proxy = proxy
    }

    @Override
    fun isProxyEnableFor(host: String?): Boolean { // FUTURE remove
        return ProxyDataImpl.isProxyEnableFor(getProxyData(), host)
    }

    /**
     * @return the triggerComponentDataMember
     */
    @Override
    fun getTriggerComponentDataMember(): Boolean {
        return triggerComponentDataMember
    }

    /**
     * @param triggerComponentDataMember the triggerComponentDataMember to set
     */
    fun setTriggerComponentDataMember(triggerComponentDataMember: Boolean) {
        this.triggerComponentDataMember = triggerComponentDataMember
    }

    @Override
    fun getClientScopeDir(): Resource? {
        if (clientScopeDir == null) clientScopeDir = getConfigDir().getRealResource("client-scope")
        return clientScopeDir
    }

    @Override
    override fun getSessionScopeDir(): Resource? {
        if (sessionScopeDir == null) sessionScopeDir = getConfigDir().getRealResource("session-scope")
        return sessionScopeDir
    }

    @Override
    fun getClientScopeDirSize(): Long {
        return clientScopeDirSize
    }

    fun getSessionScopeDirSize(): Long {
        return sessionScopeDirSize
    }

    /**
     * @param clientScopeDir the clientScopeDir to set
     */
    fun setClientScopeDir(clientScopeDir: Resource?) {
        this.clientScopeDir = clientScopeDir
    }

    protected fun setSessionScopeDir(sessionScopeDir: Resource?) {
        this.sessionScopeDir = sessionScopeDir
    }

    /**
     * @param clientScopeDirSize the clientScopeDirSize to set
     */
    fun setClientScopeDirSize(clientScopeDirSize: Long) {
        this.clientScopeDirSize = clientScopeDirSize
    }

    @Override
    @Throws(IOException::class)
    fun getRPCClassLoader(reload: Boolean): ClassLoader? {
        return getRPCClassLoader(reload, null)
    }

    @Override
    @Throws(IOException::class)
    override fun getRPCClassLoader(reload: Boolean, parents: Array<ClassLoader?>?): ClassLoader? {
        val key = toKey(parents)
        var rpccl: PhysicalClassLoader? = rpcClassLoaders!![key]
        if (rpccl == null || reload) {
            synchronized(key) {
                rpccl = rpcClassLoaders[key]
                if (rpccl == null || reload) {
                    val dir: Resource = getClassDirectory().getRealResource("RPC/$key")
                    if (!dir.exists()) {
                        ResourceUtil.createDirectoryEL(dir, true)
                    }
                    rpcClassLoaders.put(key, PhysicalClassLoader(this, dir, if (parents != null && parents.size == 0) null else parents, false).also { rpccl = it })
                }
            }
        }
        return rpccl
    }

    private fun toKey(parents: Array<ClassLoader?>?): String? {
        if (parents == null || parents.size == 0) return "orphan"
        val sb = StringBuilder()
        for (parent in parents) {
            sb.append(';').append(System.identityHashCode(parent))
        }
        return HashUtil.create64BitHashAsString(sb.toString())
    }

    fun resetRPCClassLoader() {
        rpcClassLoaders.clear()
    }

    fun setCacheDir(cacheDir: Resource?) {
        this.cacheDir = cacheDir
    }

    @Override
    fun getCacheDir(): Resource? {
        return cacheDir
    }

    @Override
    fun getCacheDirSize(): Long {
        return cacheDirSize
    }

    fun setCacheDirSize(cacheDirSize: Long) {
        this.cacheDirSize = cacheDirSize
    }

    fun setDumpWritersEntries(dmpWriterEntries: Array<DumpWriterEntry?>?) {
        this.dmpWriterEntries = dmpWriterEntries
    }

    fun getDumpWritersEntries(): Array<DumpWriterEntry?>? {
        return dmpWriterEntries
    }

    @Override
    fun getDefaultDumpWriter(defaultType: Int): DumpWriter? {
        val entries: Array<DumpWriterEntry?>? = getDumpWritersEntries()
        if (entries != null) for (i in entries.indices) {
            if (entries[i].getDefaultType() === defaultType) {
                return entries[i].getWriter()
            }
        }
        return HTMLDumpWriter()
    }

    @Override
    @Throws(DeprecatedException::class)
    fun getDumpWriter(name: String?): DumpWriter? {
        throw DeprecatedException("this method is no longer supported")
    }

    @Override
    @Throws(ExpressionException::class)
    fun getDumpWriter(name: String?, defaultType: Int): DumpWriter? {
        if (StringUtil.isEmpty(name)) return getDefaultDumpWriter(defaultType)
        val entries: Array<DumpWriterEntry?>? = getDumpWritersEntries()
        for (i in entries.indices) {
            if (entries!![i].getName().equals(name)) {
                return entries[i].getWriter()
            }
        }

        // error
        val sb = StringBuilder()
        for (i in entries.indices) {
            if (i > 0) sb.append(", ")
            sb.append(entries!![i].getName())
        }
        throw ExpressionException("invalid format definition [$name], valid definitions are [$sb]")
    }

    @Override
    fun useComponentShadow(): Boolean {
        return useComponentShadow
    }

    @Override
    override fun useComponentPathCache(): Boolean {
        return useComponentPathCache
    }

    @Override
    override fun useCTPathCache(): Boolean {
        return useCTPathCache
    }

    fun flushComponentPathCache() {
        if (componentPathCache != null) componentPathCache.clear()
    }

    fun flushApplicationPathCache() {
        if (applicationPathCache != null) applicationPathCache.clear()
    }

    fun flushCTPathCache() {
        if (ctPatchCache != null) ctPatchCache.clear()
    }

    fun setUseCTPathCache(useCTPathCache: Boolean) {
        this.useCTPathCache = useCTPathCache
    }

    fun setUseComponentPathCache(useComponentPathCache: Boolean) {
        this.useComponentPathCache = useComponentPathCache
    }

    /**
     * @param useComponentShadow the useComponentShadow to set
     */
    fun setUseComponentShadow(useComponentShadow: Boolean) {
        this.useComponentShadow = useComponentShadow
    }

    @Override
    @Throws(DatabaseException::class)
    fun getDataSource(datasource: String?): DataSource? {
        val ds: DataSource? = if (datasource == null) null else datasources!![datasource.toLowerCase()] as DataSource?
        if (ds != null) return ds

        // create error detail
        val de = DatabaseException("datasource [$datasource] doesn't exist", null, null, null)
        de.setDetail(ExceptionUtil.createSoundexDetail(datasource, datasources.keySet().iterator(), "datasource names"))
        de.setAdditional(KeyConstants._Datasource, datasource)
        throw de
    }

    @Override
    fun getDataSource(datasource: String?, defaultValue: DataSource?): DataSource? {
        val ds: DataSource? = if (datasource == null) null else datasources!![datasource.toLowerCase()] as DataSource?
        return if (ds != null) ds else defaultValue
    }

    @Override
    fun getErrWriter(): PrintWriter? {
        return err
    }

    /**
     * @param err the err to set
     */
    fun setErr(err: PrintWriter?) {
        this.err = err
    }

    @Override
    fun getOutWriter(): PrintWriter? {
        return out
    }

    /**
     * @param out the out to set
     */
    fun setOut(out: PrintWriter?) {
        this.out = out
    }

    @Override
    override fun getDatasourceConnectionPool(ds: DataSource?, user: String?, pass: String?): DatasourceConnPool? {
        val id: String = DatasourceConnectionFactory.createId(ds, user, pass)
        var pool: DatasourceConnPool? = pools!![id]
        if (pool == null) {
            synchronized(id) {
                pool = pools[id]
                if (pool == null) { // TODO add config but from where?
                    val dsp: DataSourcePro? = ds as DataSourcePro?
                    // MUST merge ConnectionLimit and MaxTotal
                    var mt = 0
                    if (dsp.getMaxTotal() > 0) mt = dsp.getMaxTotal() else {
                        mt = dsp.getConnectionLimit()
                        if (mt <= 0) mt = Integer.MAX_VALUE
                    }
                    pool = DatasourceConnPool(this, ds, user, pass, "datasource",
                            DatasourceConnPool.createPoolConfig(null, null, null, dsp.getMinIdle(), dsp.getMaxIdle(), mt, 0, 0, 0, 0, 0, null))
                    pools.put(id, pool)
                }
            }
        }
        return pool
    }

    @Override
    override fun getDatasourceConnectionPool(): MockPool? {
        return MockPool()
    }

    @Override
    override fun getDatasourceConnectionPools(): Collection<DatasourceConnPool?>? {
        return pools!!.values()
    }

    @Override
    override fun removeDatasourceConnectionPool(ds: DataSource?) {
        for (e in pools.entrySet()) {
            if (e.getValue().getFactory().getDatasource().getName().equalsIgnoreCase(ds.getName())) {
                synchronized(e.getKey()) { pools.remove(e.getKey()) }
                e.getValue().clear()
            }
        }
    }

    @Override
    fun doLocalCustomTag(): Boolean {
        return doLocalCustomTag
    }

    @Override
    fun getCustomTagExtensions(): Array<String?>? {
        return customTagExtensions
    }

    fun setCustomTagExtensions(vararg customTagExtensions: String?) {
        this.customTagExtensions = customTagExtensions
    }

    fun setDoLocalCustomTag(doLocalCustomTag: Boolean) {
        this.doLocalCustomTag = doLocalCustomTag
    }

    @Override
    override fun doComponentDeepSearch(): Boolean {
        return doComponentTagDeepSearch
    }

    fun setDoComponentDeepSearch(doComponentTagDeepSearch: Boolean) {
        this.doComponentTagDeepSearch = doComponentTagDeepSearch
    }

    @Override
    fun doCustomTagDeepSearch(): Boolean {
        return doCustomTagDeepSearch
    }

    /**
     * @param doCustomTagDeepSearch the doCustomTagDeepSearch to set
     */
    fun setDoCustomTagDeepSearch(doCustomTagDeepSearch: Boolean) {
        this.doCustomTagDeepSearch = doCustomTagDeepSearch
    }

    fun setVersion(version: Double) {
        this.version = version
    }

    /**
     * @return the version
     */
    @Override
    fun getVersion(): Double {
        return version
    }

    @Override
    override fun closeConnection(): Boolean {
        return closeConnection
    }

    fun setCloseConnection(closeConnection: Boolean) {
        this.closeConnection = closeConnection
    }

    @Override
    override fun contentLength(): Boolean {
        return contentLength
    }

    @Override
    override fun allowCompression(): Boolean {
        return allowCompression
    }

    fun setAllowCompression(allowCompression: Boolean) {
        this.allowCompression = allowCompression
    }

    fun setContentLength(contentLength: Boolean) {
        this.contentLength = contentLength
    }

    /**
     * @return the constants
     */
    @Override
    fun getConstants(): Struct? {
        return constants
    }

    /**
     * @param constants the constants to set
     */
    fun setConstants(constants: Struct?) {
        this.constants = constants
    }

    /**
     * @return the showVersion
     */
    @Override
    fun isShowVersion(): Boolean {
        return showVersion
    }

    /**
     * @param showVersion the showVersion to set
     */
    fun setShowVersion(showVersion: Boolean) {
        this.showVersion = showVersion
    }

    fun setRemoteClients(remoteClients: Array<RemoteClient?>?) {
        this.remoteClients = remoteClients
    }

    @Override
    fun getRemoteClients(): Array<RemoteClient?>? {
        return if (remoteClients == null) arrayOfNulls<RemoteClient?>(0) else remoteClients
    }

    @Override
    fun getSpoolerEngine(): SpoolerEngine? {
        return remoteClientSpoolerEngine
    }

    fun setRemoteClientDirectory(remoteClientDirectory: Resource?) {
        this.remoteClientDirectory = remoteClientDirectory
    }

    /**
     * @return the remoteClientDirectory
     */
    @Override
    fun getRemoteClientDirectory(): Resource? {
        return if (remoteClientDirectory == null) {
            ConfigWebUtil.getFile(getRootDirectory(), "client-task", "client-task", getConfigDir(), FileUtil.TYPE_DIR, this)
        } else remoteClientDirectory
    }

    fun setSpoolerEngine(spoolerEngine: SpoolerEngine?) {
        remoteClientSpoolerEngine = spoolerEngine
    }
    /*
	 * *
	 * 
	 * @return the structCase / public int getStructCase() { return structCase; }
	 */
    /*
	 * *
	 * 
	 * @param structCase the structCase to set / protected void setStructCase(int structCase) {
	 * this.structCase = structCase; }
	 */
    /**
     * @return if error status code will be returned or not
     */
    @Override
    fun getErrorStatusCode(): Boolean {
        return errorStatusCode
    }

    /**
     * @param errorStatusCode the errorStatusCode to set
     */
    fun setErrorStatusCode(errorStatusCode: Boolean) {
        this.errorStatusCode = errorStatusCode
    }

    @Override
    fun getLocalMode(): Int {
        return localMode
    }

    /**
     * @param localMode the localMode to set
     */
    fun setLocalMode(localMode: Int) {
        this.localMode = localMode
    }

    /**
     * @param strLocalMode the localMode to set
     */
    fun setLocalMode(strLocalMode: String?) {
        localMode = AppListenerUtil.toLocalMode(strLocalMode, localMode)
    }

    @Override
    fun getVideoDirectory(): Resource? {
        // TODO take from tag <video>
        val dir: Resource = getConfigDir().getRealResource("video")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    @Override
    fun getExtensionDirectory(): Resource? {
        // TODO take from tag <extensions>
        val dir: Resource = getConfigDir().getRealResource("extensions/installed")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    @Override
    fun getExtensionProviders(): Array<ExtensionProvider?>? {
        throw RuntimeException("no longer supported, use getRHExtensionProviders() instead.")
    }

    fun setRHExtensionProviders(extensionProviders: Array<RHExtensionProvider?>?) {
        rhextensionProviders = extensionProviders
    }

    @Override
    override fun getRHExtensionProviders(): Array<RHExtensionProvider?>? {
        return rhextensionProviders
    }

    @Override
    fun getExtensions(): Array<Extension?>? {
        throw PageRuntimeException("no longer supported")
    }

    @Override
    override fun getRHExtensions(): Array<RHExtension?>? {
        return rhextensions
    }

    fun setExtensions(extensions: Array<RHExtension?>?) {
        rhextensions = extensions
    }

    @Override
    fun isExtensionEnabled(): Boolean {
        throw PageRuntimeException("no longer supported")
    }

    @Override
    fun allowRealPath(): Boolean {
        return allowRealPath
    }

    fun setAllowRealPath(allowRealPath: Boolean) {
        this.allowRealPath = allowRealPath
    }

    /**
     * @return the classClusterScope
     */
    @Override
    fun getClusterClass(): Class? {
        return clusterClass
    }

    /**
     * @param clusterClass the classClusterScope to set
     */
    protected fun setClusterClass(clusterClass: Class?) {
        this.clusterClass = clusterClass
    }

    @Override
    fun getRemoteClientUsage(): Struct? {
        if (remoteClientUsage == null) remoteClientUsage = StructImpl()
        return remoteClientUsage
    }

    fun setRemoteClientUsage(remoteClientUsage: Struct?) {
        this.remoteClientUsage = remoteClientUsage
    }

    @Override
    fun getAdminSyncClass(): Class<AdminSync?>? {
        return adminSyncClass
    }

    fun setAdminSyncClass(adminSyncClass: Class?) {
        this.adminSyncClass = adminSyncClass
        adminSync = null
    }

    @Override
    @Throws(ClassException::class)
    override fun getAdminSync(): AdminSync? {
        if (adminSync == null) {
            adminSync = ClassUtil.loadInstance(getAdminSyncClass()) as AdminSync
        }
        return adminSync
    }

    @Override
    fun getVideoExecuterClass(): Class? {
        return videoExecuterClass
    }

    protected fun setVideoExecuterClass(videoExecuterClass: Class?) {
        this.videoExecuterClass = videoExecuterClass
    }

    fun setUseTimeServer(useTimeServer: Boolean) {
        this.useTimeServer = useTimeServer
    }

    @Override
    override fun getUseTimeServer(): Boolean {
        return useTimeServer
    }

    /**
     * @return the tagMappings
     */
    @Override
    override fun getTagMappings(): Collection<Mapping?>? {
        return tagMappings!!.values()
    }

    @Override
    override fun getTagMapping(mappingName: String?): Mapping? {
        return tagMappings!![mappingName]
    }

    @Override
    override fun getDefaultTagMapping(): Mapping? {
        return defaultTagMapping
    }

    @Override
    override fun getFunctionMapping(mappingName: String?): Mapping? {
        return functionMappings!![mappingName]
    }

    @Override
    override fun getDefaultFunctionMapping(): Mapping? {
        return defaultFunctionMapping
    }

    @Override
    override fun getFunctionMappings(): Collection<Mapping?>? {
        return functionMappings!!.values()
    }
    /*
	 * *
	 * 
	 * @return the tagDirectory
	 * 
	 * public Resource getTagDirectory() { return tagDirectory; }
	 */
    /**
     * mapping used for script (JSR 223)
     *
     * @return
     */
    fun getScriptMapping(): Mapping? {
        if (scriptMapping == null) {
            // Physical resource TODO make in RAM
            val physical: Resource = getConfigDir().getRealResource("jsr223")
            if (!physical.exists()) physical.mkdirs()
            scriptMapping = MappingImpl(this, "/mapping-script/", physical.getAbsolutePath(), null, ConfigPro.INSPECT_NEVER, true, true, true, true, false, true, null, -1,
                    -1)
        }
        return scriptMapping
    }

    @Override
    fun getDefaultDataSource(): String? {
        // TODO Auto-generated method stub
        return null
    }

    protected fun setDefaultDataSource(defaultDataSource: String?) {
        // this.defaultDataSource=defaultDataSource;
    }

    /**
     * @return the inspectTemplate
     */
    @Override
    fun getInspectTemplate(): Short {
        return inspectTemplate
    }

    @Override
    override fun getTypeChecking(): Boolean {
        return typeChecking
    }

    fun setTypeChecking(typeChecking: Boolean) {
        this.typeChecking = typeChecking
    }

    /**
     * @param inspectTemplate the inspectTemplate to set
     */
    fun setInspectTemplate(inspectTemplate: Short) {
        this.inspectTemplate = inspectTemplate
    }

    @Override
    override fun getSerialNumber(): String? {
        return ""
    }

    fun setCaches(caches: Map<String?, CacheConnection?>?) {
        this.caches = caches
        val it: Iterator<Entry<String?, CacheConnection?>?> = caches.entrySet().iterator()
        var entry: Entry<String?, CacheConnection?>?
        var cc: CacheConnection
        while (it.hasNext()) {
            entry = it.next()
            cc = entry.getValue()
            if (cc.getName().equalsIgnoreCase(cacheDefaultConnectionNameTemplate)) {
                defaultCacheTemplate = cc
            } else if (cc.getName().equalsIgnoreCase(cacheDefaultConnectionNameFunction)) {
                defaultCacheFunction = cc
            } else if (cc.getName().equalsIgnoreCase(cacheDefaultConnectionNameQuery)) {
                defaultCacheQuery = cc
            } else if (cc.getName().equalsIgnoreCase(cacheDefaultConnectionNameResource)) {
                defaultCacheResource = cc
            } else if (cc.getName().equalsIgnoreCase(cacheDefaultConnectionNameObject)) {
                defaultCacheObject = cc
            } else if (cc.getName().equalsIgnoreCase(cacheDefaultConnectionNameInclude)) {
                defaultCacheInclude = cc
            } else if (cc.getName().equalsIgnoreCase(cacheDefaultConnectionNameHTTP)) {
                defaultCacheHTTP = cc
            } else if (cc.getName().equalsIgnoreCase(cacheDefaultConnectionNameFile)) {
                defaultCacheFile = cc
            } else if (cc.getName().equalsIgnoreCase(cacheDefaultConnectionNameWebservice)) {
                defaultCacheWebservice = cc
            }
        }
    }

    @Override
    fun getCacheConnections(): Map<String?, CacheConnection?>? {
        return caches
    }
    // used by argus cache FUTURE add to interface
    /**
     * creates a new RamCache, please make sure to finalize.
     *
     * @param arguments possible arguments are "timeToLiveSeconds", "timeToIdleSeconds" and
     * "controlInterval"
     * @throws IOException
     */
    @Throws(IOException::class)
    fun createRAMCache(arguments: Struct?): Cache? {
        var arguments: Struct? = arguments
        val rc = RamCache()
        if (arguments == null) arguments = StructImpl()
        rc.init(this, "" + CreateUniqueId.invoke(), arguments)
        return rc
    }

    @Override
    fun getCacheDefaultConnection(type: Int): CacheConnection? {
        if (type == CACHE_TYPE_FUNCTION) return defaultCacheFunction
        if (type == CACHE_TYPE_OBJECT) return defaultCacheObject
        if (type == CACHE_TYPE_TEMPLATE) return defaultCacheTemplate
        if (type == CACHE_TYPE_QUERY) return defaultCacheQuery
        if (type == CACHE_TYPE_RESOURCE) return defaultCacheResource
        if (type == CACHE_TYPE_INCLUDE) return defaultCacheInclude
        if (type == CACHE_TYPE_HTTP) return defaultCacheHTTP
        if (type == CACHE_TYPE_FILE) return defaultCacheFile
        return if (type == CACHE_TYPE_WEBSERVICE) defaultCacheWebservice else null
    }

    fun setCacheDefaultConnectionName(type: Int, cacheDefaultConnectionName: String?) {
        if (type == CACHE_TYPE_FUNCTION) cacheDefaultConnectionNameFunction = cacheDefaultConnectionName else if (type == CACHE_TYPE_OBJECT) cacheDefaultConnectionNameObject = cacheDefaultConnectionName else if (type == CACHE_TYPE_TEMPLATE) cacheDefaultConnectionNameTemplate = cacheDefaultConnectionName else if (type == CACHE_TYPE_QUERY) cacheDefaultConnectionNameQuery = cacheDefaultConnectionName else if (type == CACHE_TYPE_RESOURCE) cacheDefaultConnectionNameResource = cacheDefaultConnectionName else if (type == CACHE_TYPE_INCLUDE) cacheDefaultConnectionNameInclude = cacheDefaultConnectionName else if (type == CACHE_TYPE_HTTP) cacheDefaultConnectionNameHTTP = cacheDefaultConnectionName else if (type == CACHE_TYPE_FILE) cacheDefaultConnectionNameFile = cacheDefaultConnectionName else if (type == CACHE_TYPE_WEBSERVICE) cacheDefaultConnectionNameWebservice = cacheDefaultConnectionName
    }

    @Override
    fun getCacheDefaultConnectionName(type: Int): String? {
        if (type == CACHE_TYPE_FUNCTION) return cacheDefaultConnectionNameFunction
        if (type == CACHE_TYPE_OBJECT) return cacheDefaultConnectionNameObject
        if (type == CACHE_TYPE_TEMPLATE) return cacheDefaultConnectionNameTemplate
        if (type == CACHE_TYPE_QUERY) return cacheDefaultConnectionNameQuery
        if (type == CACHE_TYPE_RESOURCE) return cacheDefaultConnectionNameResource
        if (type == CACHE_TYPE_INCLUDE) return cacheDefaultConnectionNameInclude
        if (type == CACHE_TYPE_HTTP) return cacheDefaultConnectionNameHTTP
        if (type == CACHE_TYPE_FILE) return cacheDefaultConnectionNameFile
        return if (type == CACHE_TYPE_WEBSERVICE) cacheDefaultConnectionNameWebservice else null
    }

    fun getCacheMD5(): String? {
        return cacheMD5
    }

    fun setCacheMD5(cacheMD5: String?) {
        this.cacheMD5 = cacheMD5
    }

    @Override
    override fun getExecutionLogEnabled(): Boolean {
        return executionLogEnabled
    }

    fun setExecutionLogEnabled(executionLogEnabled: Boolean) {
        this.executionLogEnabled = executionLogEnabled
    }

    @Override
    override fun getExecutionLogFactory(): ExecutionLogFactory? {
        return executionLogFactory
    }

    fun setExecutionLogFactory(executionLogFactory: ExecutionLogFactory?) {
        this.executionLogFactory = executionLogFactory
    }

    @Override
    @Throws(PageException::class)
    override fun resetORMEngine(pc: PageContext?, force: Boolean): ORMEngine? {
        // String name = pc.getApplicationContext().getName();
        // ormengines.remove(name);
        val e: ORMEngine? = getORMEngine(pc)
        e.reload(pc, force)
        return e
    }

    @Override
    @Throws(PageException::class)
    fun getORMEngine(pc: PageContext?): ORMEngine? {
        val name: String = pc.getApplicationContext().getName()
        var engine: ORMEngine? = ormengines!![name]
        if (engine == null) {
            // try {
            var t: Throwable? = null
            try {
                engine = ClassUtil.loadInstance(cdORMEngine.getClazz()) as ORMEngine
                engine.init(pc)
            } catch (ce: ClassException) {
                t = ce
            } catch (be: BundleException) {
                t = be
            } catch (ncfe: NoClassDefFoundError) {
                t = ncfe
            }
            if (t != null) {
                val ae = ApplicationException("cannot initialize ORM Engine [$cdORMEngine], make sure you have added all the required jar files")
                ae.initCause(t)
                throw ae
            }
            ormengines.put(name, engine)
            /*
			 * } catch (PageException pe) { throw pe; }
			 */
        }
        return engine
    }

    @Override
    override fun getORMEngineClassDefintion(): ClassDefinition<out ORMEngine?>? {
        return cdORMEngine
    }

    @Override
    fun getComponentMappings(): Array<Mapping?>? {
        return componentMappings
    }

    /**
     * @param componentMappings the componentMappings to set
     */
    fun setComponentMappings(componentMappings: Array<Mapping?>?) {
        this.componentMappings = componentMappings
    }

    fun setORMEngineClass(cd: ClassDefinition<out ORMEngine?>?) {
        cdORMEngine = cd
    }

    fun getORMEngineClass(): ClassDefinition<out ORMEngine?>? {
        return cdORMEngine
    }

    fun setORMConfig(ormConfig: ORMConfiguration?) {
        this.ormConfig = ormConfig
    }

    @Override
    override fun getORMConfig(): ORMConfiguration? {
        return ormConfig
    }

    private var componentPathCache: Map<String?, SoftReference<PageSource?>?>? = null // new ArrayList<Page>();
    private var applicationPathCache: Map<String?, SoftReference<ConfigWebUtil.CacheElement?>?>? = null // new ArrayList<Page>();
    private var ctPatchCache: Map<String?, SoftReference<InitFile?>?>? = null // new ArrayList<Page>();
    private val udfCache: Map<String?, SoftReference<UDF?>?>? = ConcurrentHashMap<String?, SoftReference<UDF?>?>()

    @Override
    @Throws(TemplateException::class)
    override fun getCachedPage(pc: PageContext?, pathWithCFC: String?): CIPage? {
        if (componentPathCache == null) return null
        val tmp: SoftReference<PageSource?>? = componentPathCache!![pathWithCFC.toLowerCase()]
        val ps: PageSource = (if (tmp == null) null else tmp.get()) ?: return null
        return try {
            ps.loadPageThrowTemplateException(pc, false, null as Page?) as CIPage
        } catch (pe: PageException) {
            throw pe as TemplateException
        }
    }

    @Override
    override fun putCachedPageSource(pathWithCFC: String?, ps: PageSource?) {
        if (componentPathCache == null) componentPathCache = ConcurrentHashMap<String?, SoftReference<PageSource?>?>() // MUSTMUST new
        // ReferenceMap(ReferenceMap.SOFT,ReferenceMap.SOFT);
        componentPathCache.put(pathWithCFC.toLowerCase(), SoftReference<PageSource?>(ps))
    }

    @Override
    override fun getApplicationPageSource(pc: PageContext?, path: String?, filename: String?, mode: Int, isCFC: RefBoolean?): PageSource? {
        if (applicationPathCache == null) return null
        val id: String = (path.toString() + ":" + filename + ":" + mode).toLowerCase()
        val tmp: SoftReference<CacheElement?>? = if (getApplicationPathCacheTimeout() <= 0) null else applicationPathCache!![id]
        if (tmp != null) {
            val ce: CacheElement = tmp.get()
            if (ce != null && ce.created + getApplicationPathCacheTimeout() >= System.currentTimeMillis()) {
                if (ce.pageSource.loadPage(pc, false, null as Page?) != null) {
                    if (isCFC != null) isCFC.setValue(ce.isCFC)
                    return ce.pageSource
                }
            }
        }
        return null
    }

    @Override
    override fun putApplicationPageSource(path: String?, ps: PageSource?, filename: String?, mode: Int, isCFC: Boolean) {
        if (getApplicationPathCacheTimeout() <= 0) return
        if (applicationPathCache == null) applicationPathCache = ConcurrentHashMap<String?, SoftReference<CacheElement?>?>() // MUSTMUST new
        val id: String = (path.toString() + ":" + filename + ":" + mode).toLowerCase()
        applicationPathCache.put(id, SoftReference<CacheElement?>(CacheElement(ps, isCFC)))
    }

    @Override
    override fun getApplicationPathCacheTimeout(): Long {
        return applicationPathCacheTimeout
    }

    fun setApplicationPathCacheTimeout(applicationPathCacheTimeout: Long) {
        this.applicationPathCacheTimeout = applicationPathCacheTimeout
    }

    @Override
    override fun getCTInitFile(pc: PageContext?, key: String?): InitFile? {
        if (ctPatchCache == null) return null
        val tmp: SoftReference<InitFile?>? = ctPatchCache!![key.toLowerCase()]
        val initFile: InitFile? = if (tmp == null) null else tmp.get()
        if (initFile != null) {
            if (MappingImpl.isOK(initFile.getPageSource())) return initFile
            ctPatchCache.remove(key.toLowerCase())
        }
        return null
    }

    @Override
    override fun putCTInitFile(key: String?, initFile: InitFile?) {
        if (ctPatchCache == null) ctPatchCache = ConcurrentHashMap<String?, SoftReference<InitFile?>?>() // MUSTMUST new ReferenceMap(ReferenceMap.SOFT,ReferenceMap.SOFT);
        ctPatchCache.put(key.toLowerCase(), SoftReference<InitFile?>(initFile))
    }

    @Override
    override fun listCTCache(): Struct? {
        val sct: Struct = StructImpl()
        if (ctPatchCache == null) return sct
        val it: Iterator<Entry<String?, SoftReference<InitFile?>?>?> = ctPatchCache.entrySet().iterator()
        var entry: Entry<String?, SoftReference<InitFile?>?>?
        var v: SoftReference<InitFile?>
        var initFile: InitFile
        while (it.hasNext()) {
            entry = it.next()
            v = entry.getValue()
            if (v != null) {
                initFile = v.get()
                if (initFile != null) sct.setEL(entry.getKey(), initFile.getPageSource().getDisplayPath())
            }
        }
        return sct
    }

    @Override
    override fun clearCTCache() {
        if (ctPatchCache == null) return
        ctPatchCache.clear()
    }

    @Override
    override fun clearFunctionCache() {
        udfCache.clear()
    }

    @Override
    override fun getFromFunctionCache(key: String?): UDF? {
        val tmp: SoftReference<UDF?> = udfCache!![key] ?: return null
        return tmp.get()
    }

    @Override
    override fun putToFunctionCache(key: String?, udf: UDF?) {
        udfCache.put(key, SoftReference<UDF?>(udf))
    }

    @Override
    override fun listComponentCache(): Struct? {
        val sct: Struct = StructImpl()
        if (componentPathCache == null) return sct
        val it: Iterator<Entry<String?, SoftReference<PageSource?>?>?> = componentPathCache.entrySet().iterator()
        var entry: Entry<String?, SoftReference<PageSource?>?>?
        while (it.hasNext()) {
            entry = it.next()
            val k: String = entry.getKey() ?: continue
            val v: SoftReference<PageSource?> = entry.getValue() ?: continue
            val ps: PageSource = v.get() ?: continue
            sct.setEL(KeyImpl.init(k), ps.getDisplayPath())
        }
        return sct
    }

    @Override
    override fun clearComponentCache() {
        if (componentPathCache == null) return
        componentPathCache.clear()
    }

    @Override
    override fun clearApplicationCache() {
        if (applicationPathCache == null) return
        applicationPathCache.clear()
    }

    @Override
    override fun getComponentDefaultImport(): ImportDefintion? {
        return componentDefaultImport
    }

    fun setComponentDefaultImport(str: String?) {
        var str = str
        if (StringUtil.isEmpty(str)) return
        if ("org.railo.cfml.*".equalsIgnoreCase(str)) str = "org.lucee.cfml.*"
        val cdi: ImportDefintion = ImportDefintionImpl.getInstance(str, null)
        if (cdi != null) componentDefaultImport = cdi
    }

    /**
     * @return the componentLocalSearch
     */
    @Override
    override fun getComponentLocalSearch(): Boolean {
        return componentLocalSearch
    }

    /**
     * @param componentLocalSearch the componentLocalSearch to set
     */
    fun setComponentLocalSearch(componentLocalSearch: Boolean) {
        this.componentLocalSearch = componentLocalSearch
    }

    /**
     * @return the componentLocalSearch
     */
    @Override
    override fun getComponentRootSearch(): Boolean {
        return componentRootSearch
    }

    /**
     * @param componentRootSearch the componentLocalSearch to set
     */
    protected fun setComponentRootSearch(componentRootSearch: Boolean) {
        this.componentRootSearch = componentRootSearch
    }

    private val compressResources: Map<String?, SoftReference<Compress?>?>? = ConcurrentHashMap<String?, SoftReference<Compress?>?>()

    @Override
    @Throws(IOException::class)
    override fun getCompressInstance(zipFile: Resource?, format: Int, caseSensitive: Boolean): Compress? {
        val tmp: SoftReference<Compress?>? = compressResources!![zipFile.getPath()]
        var compress: Compress? = if (tmp == null) null else tmp.get()
        if (compress == null) {
            compress = Compress(zipFile, format, caseSensitive)
            compressResources.put(zipFile.getPath(), SoftReference<Compress?>(compress))
        }
        return compress
    }

    @Override
    fun getSessionCluster(): Boolean {
        return false
    }

    @Override
    fun getClientCluster(): Boolean {
        return false
    }

    @Override
    override fun getClientStorage(): String? {
        return clientStorage
    }

    @Override
    override fun getSessionStorage(): String? {
        return sessionStorage
    }

    fun setClientStorage(clientStorage: String?) {
        this.clientStorage = clientStorage
    }

    fun setSessionStorage(sessionStorage: String?) {
        this.sessionStorage = sessionStorage
    }

    private var componentMetaData: Map<String?, ComponentMetaData?>? = null
    fun getComponentMetadata(key: String?): ComponentMetaData? {
        return if (componentMetaData == null) null else componentMetaData!![key.toLowerCase()]
    }

    fun putComponentMetadata(key: String?, data: ComponentMetaData?) {
        if (componentMetaData == null) componentMetaData = HashMap<String?, ComponentMetaData?>()
        componentMetaData.put(key.toLowerCase(), data)
    }

    fun clearComponentMetadata() {
        if (componentMetaData == null) return
        componentMetaData.clear()
    }

    class ComponentMetaData(meta: Struct?, lastMod: Long) {
        val meta: Struct?
        val lastMod: Long

        init {
            this.meta = meta
            this.lastMod = lastMod
        }
    }

    private var debugEntries: Array<DebugEntry?>?
    fun setDebugEntries(debugEntries: Array<DebugEntry?>?) {
        this.debugEntries = debugEntries
    }

    @Override
    override fun getDebugEntries(): Array<DebugEntry?>? {
        if (debugEntries == null) debugEntries = arrayOfNulls<DebugEntry?>(0)
        return debugEntries
    }

    @Override
    override fun getDebugEntry(ip: String?, defaultValue: DebugEntry?): DebugEntry? {
        if (debugEntries!!.size == 0) return defaultValue
        val ia: InetAddress
        ia = try {
            IPRange.toInetAddress(ip)
        } catch (e: IOException) {
            return defaultValue
        }
        for (i in debugEntries.indices) {
            if (debugEntries!![i]!!.getIpRange().inRange(ia)) return debugEntries!![i]
        }
        return defaultValue
    }

    private var debugMaxRecordsLogged = 10
    fun setDebugMaxRecordsLogged(debugMaxRecordsLogged: Int) {
        this.debugMaxRecordsLogged = debugMaxRecordsLogged
    }

    @Override
    override fun getDebugMaxRecordsLogged(): Int {
        return debugMaxRecordsLogged
    }

    private var dotNotationUpperCase = true
    fun setDotNotationUpperCase(dotNotationUpperCase: Boolean) {
        this.dotNotationUpperCase = dotNotationUpperCase
    }

    @Override
    override fun getDotNotationUpperCase(): Boolean {
        return dotNotationUpperCase
    }

    @Override
    override fun preserveCase(): Boolean {
        return !dotNotationUpperCase
    }

    private var defaultFunctionOutput = true
    fun setDefaultFunctionOutput(defaultFunctionOutput: Boolean) {
        this.defaultFunctionOutput = defaultFunctionOutput
    }

    @Override
    override fun getDefaultFunctionOutput(): Boolean {
        return defaultFunctionOutput
    }

    private var getSuppressWSBeforeArg = true
    fun setSuppressWSBeforeArg(getSuppressWSBeforeArg: Boolean) {
        this.getSuppressWSBeforeArg = getSuppressWSBeforeArg
    }

    @Override
    override fun getSuppressWSBeforeArg(): Boolean {
        return getSuppressWSBeforeArg
    }

    private var restSetting: RestSettings? = RestSettingImpl(false, UDF.RETURN_FORMAT_JSON)
    protected fun setRestSetting(restSetting: RestSettings?) {
        this.restSetting = restSetting
    }

    @Override
    fun getRestSetting(): RestSettings? {
        return restSetting
    }

    fun setMode(mode: Int) {
        this.mode = mode
    }

    fun getMode(): Int {
        return mode
    }

    // do not move to Config interface, do instead getCFMLWriterClass
    fun setCFMLWriterType(writerType: Int) {
        this.writerType = writerType
    }

    // do not move to Config interface, do instead setCFMLWriterClass
    @Override
    override fun getCFMLWriterType(): Int {
        return writerType
    }

    private var bufferOutput = false
    private var externalizeStringGTE = -1
    private var extensionBundles: Map<String?, BundleDefinition?>? = null
    private var drivers: Array<JDBCDriver?>?
    private var logDir: Resource? = null

    @Override
    override fun getBufferOutput(): Boolean {
        return bufferOutput
    }

    fun setBufferOutput(bufferOutput: Boolean) {
        this.bufferOutput = bufferOutput
    }

    fun getDebugOptions(): Int {
        return debugOptions
    }

    @Override
    override fun hasDebugOptions(debugOption: Int): Boolean {
        return debugOptions and debugOption > 0
    }

    fun setDebugOptions(debugOptions: Int) {
        this.debugOptions = debugOptions
    }

    fun setCheckForChangesInConfigFile(checkForChangesInConfigFile: Boolean) {
        this.checkForChangesInConfigFile = checkForChangesInConfigFile
    }

    @Override
    override fun checkForChangesInConfigFile(): Boolean {
        return checkForChangesInConfigFile
    }

    fun setExternalizeStringGTE(externalizeStringGTE: Int) {
        this.externalizeStringGTE = externalizeStringGTE
    }

    @Override
    override fun getExternalizeStringGTE(): Int {
        return externalizeStringGTE
    }

    protected fun addConsoleLayout(layout: Object?) {
        consoleLayouts.add(layout)
    }

    protected fun addResourceLayout(layout: Object?) {
        resourceLayouts.add(layout)
    }

    @Throws(PageException::class)
    fun getConsoleLayouts(): Array<Object?>? {
        if (consoleLayouts.isEmpty()) consoleLayouts.add(getLogEngine().getDefaultLayout())
        return consoleLayouts.toArray(arrayOfNulls<Object?>(consoleLayouts.size()))
    }

    @Throws(PageException::class)
    fun getResourceLayouts(): Array<Object?>? {
        if (resourceLayouts.isEmpty()) resourceLayouts.add(getLogEngine().getClassicLayout())
        return resourceLayouts.toArray(arrayOfNulls<Object?>(resourceLayouts.size()))
    }

    protected fun clearLoggers(dyn: Boolean?) {
        if (loggers!!.size() === 0) return
        val list: List<String?>? = if (dyn != null) ArrayList<String?>() else null
        try {
            val it: Iterator<Entry<String?, LoggerAndSourceData?>?> = loggers.entrySet().iterator()
            var e: Entry<String?, LoggerAndSourceData?>?
            while (it.hasNext()) {
                e = it.next()
                if (dyn == null || dyn.booleanValue() === e.getValue().getDyn()) {
                    e.getValue().close()
                    list?.add(e.getKey())
                }
            }
        } catch (e: Exception) {
        }
        if (list == null) loggers.clear() else {
            val it = list.iterator()
            while (it.hasNext()) {
                loggers.remove(it.next())
            }
        }
    }

    @Throws(PageException::class)
    fun addLogger(name: String?, level: Int, appender: ClassDefinition?, appenderArgs: Map<String?, String?>?, layout: ClassDefinition?,
                  layoutArgs: Map<String?, String?>?, readOnly: Boolean, dyn: Boolean): LoggerAndSourceData? {
        val existing: LoggerAndSourceData? = loggers!![name.toLowerCase()]
        val id: String = LoggerAndSourceData.id(name.toLowerCase(), appender, appenderArgs, layout, layoutArgs, level, readOnly)
        if (existing != null) {
            if (existing.id().equals(id)) {
                return existing
            }
            existing.close()
        }
        val las = LoggerAndSourceData(this, id, name.toLowerCase(), appender, appenderArgs, layout, layoutArgs, level, readOnly, dyn)
        loggers.put(name.toLowerCase(), las)
        return las
    }

    @Override
    override fun getLoggers(): Map<String?, LoggerAndSourceData?>? {
        return loggers
    }

    // FUTURE add to interface
    fun getLogNames(): Array<String?>? {
        return loggers.keySet().toArray(arrayOfNulls<String?>(loggers!!.size()))
    }

    @Override
    fun getLog(name: String?): Log? {
        return try {
            getLog(name, true)
        } catch (e: PageException) {
            throw PageRuntimeException(e)
        }
    }

    @Override
    @Throws(PageException::class)
    override fun getLog(name: String?, createIfNecessary: Boolean): Log? {
        val lsd: LoggerAndSourceData = _getLoggerAndSourceData(name, createIfNecessary) ?: return null
        return lsd.getLog(false)
    }

    @Throws(PageException::class)
    private fun _getLoggerAndSourceData(name: String?, createIfNecessary: Boolean): LoggerAndSourceData? {
        return loggers!![name.toLowerCase()]
                ?: return if (!createIfNecessary) null else addLogger(name, Log.LEVEL_ERROR, getLogEngine().appenderClassDefintion("console"), null, getLogEngine().layoutClassDefintion("pattern"), null, true, true)
    }

    @Override
    override fun getTagDefaultAttributeValues(): Map<Key?, Map<Key?, Object?>?>? {
        return if (tagDefaultAttributeValues == null) null else Duplicator.duplicateMap(tagDefaultAttributeValues, HashMap<Key?, Map<Key?, Object?>?>(), true)
    }

    protected fun setTagDefaultAttributeValues(values: Map<Key?, Map<Key?, Object?>?>?) {
        tagDefaultAttributeValues = values
    }

    @Override
    fun getHandleUnQuotedAttrValueAsString(): Boolean? {
        return handleUnQuotedAttrValueAsString
    }

    fun setHandleUnQuotedAttrValueAsString(handleUnQuotedAttrValueAsString: Boolean) {
        this.handleUnQuotedAttrValueAsString = handleUnQuotedAttrValueAsString
    }

    fun setCachedWithin(type: Int, value: Object?) {
        cachedWithins.put(type, value)
    }

    @Override
    fun getCachedWithin(type: Int): Object? {
        return cachedWithins!![type]
    }

    @Override
    override fun getPluginDirectory(): Resource? {
        return getConfigDir().getRealResource("context/admin/plugin")
    }

    @Override
    override fun getLogDirectory(): Resource? {
        if (logDir == null) {
            logDir = getConfigDir().getRealResource("logs")
            logDir.mkdir()
        }
        return logDir
    }

    fun setSalt(salt: String?) {
        this.salt = salt
    }

    @Override
    override fun getSalt(): String? {
        return salt
    }

    @Override
    override fun getPasswordType(): Int {
        return if (password == null) Password.HASHED_SALTED else password.getType() // when there is no password, we will have a HS password
    }

    @Override
    override fun getPasswordSalt(): String? {
        return if (password == null || password.getSalt() == null) salt else password.getSalt()
    }

    @Override
    override fun getPasswordOrigin(): Int {
        return if (password == null) Password.ORIGIN_UNKNOW else password.getOrigin()
    }

    @Override
    override fun getExtensionBundleDefintions(): Collection<BundleDefinition?>? {
        if (extensionBundles == null) {
            val rhes: Array<RHExtension?>? = getRHExtensions()
            val extensionBundles: Map<String?, BundleDefinition?> = HashMap<String?, BundleDefinition?>()
            for (rhe in rhes!!) {
                var bis: Array<BundleInfo?>
                bis = try {
                    rhe.getBundles()
                } catch (e: Exception) {
                    continue
                }
                if (bis != null) {
                    for (bi in bis) {
                        extensionBundles.put(bi.getSymbolicName().toString() + "|" + bi.getVersionAsString(), bi.toBundleDefinition())
                    }
                }
            }
            this.extensionBundles = extensionBundles
        }
        return extensionBundles!!.values()
    }

    fun setJDBCDrivers(drivers: Array<JDBCDriver?>?) {
        this.drivers = drivers
    }

    @Override
    override fun getJDBCDrivers(): Array<JDBCDriver?>? {
        return drivers
    }

    @Override
    override fun getJDBCDriverByClassName(className: String?, defaultValue: JDBCDriver?): JDBCDriver? {
        for (d in drivers!!) {
            if (d.cd.getClassName().equals(className)) return d
        }
        return defaultValue
    }

    @Override
    override fun getJDBCDriverById(id: String?, defaultValue: JDBCDriver?): JDBCDriver? {
        if (!StringUtil.isEmpty(id)) {
            for (d in drivers!!) {
                if (d.id != null && d.id.equalsIgnoreCase(id)) return d
            }
        }
        return defaultValue
    }

    @Override
    override fun getJDBCDriverByBundle(bundleName: String?, version: Version?, defaultValue: JDBCDriver?): JDBCDriver? {
        for (d in drivers!!) {
            if (d.cd.getName().equals(bundleName) && (version == null || version.equals(d.cd.getVersion()))) return d
        }
        return defaultValue
    }

    @Override
    override fun getJDBCDriverByCD(cd: ClassDefinition?, defaultValue: JDBCDriver?): JDBCDriver? {
        for (d in drivers!!) {
            if (d.cd.getId().equals(cd.getId())) return d // TODO comparing cd objects directly?
        }
        return defaultValue
    }

    @Override
    override fun getQueueMax(): Int {
        return queueMax
    }

    fun setQueueMax(queueMax: Int) {
        this.queueMax = queueMax
    }

    @Override
    override fun getQueueTimeout(): Long {
        return queueTimeout
    }

    fun setQueueTimeout(queueTimeout: Long) {
        this.queueTimeout = queueTimeout
    }

    @Override
    override fun getQueueEnable(): Boolean {
        return queueEnable
    }

    fun setQueueEnable(queueEnable: Boolean) {
        this.queueEnable = queueEnable
    }

    private var cgiScopeReadonly = true

    @Override
    override fun getCGIScopeReadonly(): Boolean {
        return cgiScopeReadonly
    }

    fun setCGIScopeReadonly(cgiScopeReadonly: Boolean) {
        this.cgiScopeReadonly = cgiScopeReadonly
    }

    private var deployDir: Resource? = null
    @Override
    fun getDeployDirectory(): Resource? {
        if (deployDir == null) {
            // config web
            if (this is ConfigWeb) {
                deployDir = getConfigDir().getRealResource("deploy")
                if (!deployDir.exists()) deployDir.mkdirs()
            } else {
                try {
                    val file = File(ConfigWebUtil.getEngine(this).getCFMLEngineFactory().getResourceRoot(), "deploy")
                    if (!file.exists()) file.mkdirs()
                    deployDir = ResourcesImpl.getFileResourceProvider().getResource(file.getAbsolutePath())
                } catch (ioe: IOException) {
                    deployDir = getConfigDir().getRealResource("deploy")
                    if (!deployDir.exists()) deployDir.mkdirs()
                }
            }
        }
        return deployDir
    }

    private var allowLuceeDialect = false

    @Override
    override fun allowLuceeDialect(): Boolean {
        return allowLuceeDialect
    }

    fun setAllowLuceeDialect(allowLuceeDialect: Boolean) {
        this.allowLuceeDialect = allowLuceeDialect
    }

    /*
	 * public boolean installExtension(ExtensionDefintion ed) throws PageException { return
	 * DeployHandler.deployExtension(this, ed, getLog("deploy"),true); }
	 */
    private var cacheDefinitions: Map<String?, ClassDefinition?>? = null
    fun setCacheDefinitions(caches: Map<String?, ClassDefinition?>?) {
        cacheDefinitions = caches
    }

    @Override
    override fun getCacheDefinitions(): Map<String?, ClassDefinition?>? {
        return cacheDefinitions
    }

    @Override
    override fun getCacheDefinition(className: String?): ClassDefinition? {
        return cacheDefinitions!![className]
    }

    @Override
    override fun getAntiSamyPolicy(): Resource? {
        return getConfigDir().getRealResource("security/antisamy-basic.xml")
    }

    protected abstract fun setGatewayEntries(gatewayEntries: Map<String?, GatewayEntry?>?)
    abstract fun getGatewayEntries(): Map<String?, GatewayEntry?>?
    private var wsHandlerCD: ClassDefinition? = null
    protected var wsHandler: WSHandler? = null
    fun setWSHandlerClassDefinition(cd: ClassDefinition?) {
        wsHandlerCD = cd
        wsHandler = null
    }

    // public abstract WSHandler getWSHandler() throws PageException;
    fun getWSHandlerClassDefinition(): ClassDefinition? {
        return wsHandlerCD
    }

    fun isEmpty(cd: ClassDefinition?): Boolean {
        return cd == null || StringUtil.isEmpty(cd.getClassName())
    }

    private var fullNullSupport = false
    fun setFullNullSupport(fullNullSupport: Boolean) {
        this.fullNullSupport = fullNullSupport
    }

    @Override
    fun getFullNullSupport(): Boolean {
        return fullNullSupport
    }

    @Override
    override fun getLogEngine(): LogEngine? {
        if (logEngine == null) {
            synchronized(token) {
                if (logEngine == null) {
                    logEngine = LogEngine.newInstance(this)
                }
            }
        }
        return logEngine
    }

    fun setCachedAfterTimeRange(ts: TimeSpan?) {
        cachedAfterTimeRange = ts
    }

    @Override
    override fun getCachedAfterTimeRange(): TimeSpan? {
        if (cachedAfterTimeRange != null && cachedAfterTimeRange.getMillis() <= 0) cachedAfterTimeRange = null
        return cachedAfterTimeRange
    }

    @Override
    override fun getStartups(): Map<String?, Startup?>? {
        if (startups == null) startups = HashMap()
        return startups
    }

    @Override
    override fun getRegex(): Regex? {
        if (regex == null) regex = RegexFactory.toRegex(RegexFactory.TYPE_PERL, null)
        return regex
    }

    fun setRegex(regex: Regex?) {
        this.regex = regex
    }

    @Override
    override fun getPreciseMath(): Boolean {
        return preciseMath
    }

    fun setPreciseMath(preciseMath: Boolean) {
        this.preciseMath = preciseMath
    }

    companion object {
        private val RHEXTENSIONS_EMPTY: Array<RHExtension?>? = arrayOfNulls<RHExtension?>(0)

        // FUTURE add to interface
        const val ADMINMODE_SINGLE: Short = 1
        const val ADMINMODE_MULTI: Short = 2
        const val ADMINMODE_AUTO: Short = 4
        private var startups: Map<String?, Startup?>? = null
        private val token: Object? = Object()
        private fun toName(filename: String?): String? {
            val pos: Int = filename.lastIndexOf('.')
            return if (pos == -1) filename else filename.substring(0, pos)
        }

        private var logEngine: LogEngine? = null
    }

    /**
     * private constructor called by factory method
     *
     * @param configDir - config directory
     * @param configFile - config file
     */
    init {
        this.configDir = configDir
        this.configFile = configFile
    }
}