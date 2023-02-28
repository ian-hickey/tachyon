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
package tachyon.runtime.config

import java.io.File

/**
 * config server impl
 */
class ConfigServerImpl(engine: CFMLEngineImpl?, initContextes: Map<String?, CFMLFactory?>?, contextes: Map<String?, CFMLFactory?>?, configDir: Resource?, configFile: Resource?,
                       updateInfo: UpdateInfo?, essentialOnly: Boolean) : ConfigImpl(configDir, configFile), ConfigServer {
    private val engine: CFMLEngineImpl?
    private val initContextes: Map<String?, CFMLFactory?>?

    // private Map contextes;
    private var defaultSecurityManager: SecurityManager? = null
    private val managers: Map<String?, SecurityManager?>? = MapFactory.< String, SecurityManager>getConcurrentMap<String?, SecurityManager?>()
    var defaultPassword: Password? = null
    private val rootDir: Resource?
    private var updateLocation: URL? = null
    private var updateType: String? = ""
    private var configListener: ConfigListener? = null
    private var labels: Map<String?, String?>? = null
    private var requestMonitors: Array<RequestMonitor?>?
    private var intervallMonitors: Array<IntervallMonitor?>?
    private var actionMonitorCollector: ActionMonitorCollector? = null
    private var monitoringEnabled = false
    private var delay = 1
    private var captcha = false
    private var rememberMe = true

    // private static ConfigServerImpl instance;
    private var authKeys: Array<String?>?
    private val idPro: String? = null
    private val previousNonces: LinkedHashMapMaxSize<Long?, String?>? = LinkedHashMapMaxSize<Long?, String?>(100)
    private var permGenCleanUpThreshold = 60
    val cfmlCoreTLDs: TagLib?
    val tachyonCoreTLDs: TagLib?
    val cfmlCoreFLDs: FunctionLib?
    val tachyonCoreFLDs: FunctionLib?
    private val updateInfo: UpdateInfo?
    fun getUpdateInfo(): UpdateInfo? {
        return updateInfo
    }

    /**
     * @return the configListener
     */
    @Override
    fun getConfigListener(): ConfigListener? {
        return configListener
    }

    /**
     * @param configListener the configListener to set
     */
    @Override
    fun setConfigListener(configListener: ConfigListener?) {
        this.configListener = configListener
    }

    @Override
    fun getConfigServer(password: String?): ConfigServer? {
        return this
    }

    @Override
    fun getConfigServer(key: String?, timeNonce: Long): ConfigServer? {
        return this
    }

    @Override
    fun getConfigWebs(): Array<ConfigWeb?>? {
        val it: Iterator<String?> = initContextes.keySet().iterator()
        val webs: Array<ConfigWeb?> = arrayOfNulls<ConfigWeb?>(initContextes!!.size())
        var index = 0
        while (it.hasNext()) {
            webs[index++] = (initContextes!![it.next()] as CFMLFactoryImpl?).getConfig()
        }
        return webs
    }

    @Override
    fun getConfigWeb(realpath: String?): ConfigWeb? {
        return getConfigWebPro(realpath)
    }

    /**
     * returns CongigWeb Implementtion
     *
     * @param realpath
     * @return ConfigWebPro
     */
    protected fun getConfigWebPro(realpath: String?): ConfigWebPro? {
        val it: Iterator<String?> = initContextes.keySet().iterator()
        while (it.hasNext()) {
            val cw: ConfigWeb = (initContextes!![it.next()] as CFMLFactoryImpl?).getConfig()
            if (ReqRspUtil.getRootPath(cw.getServletContext()).equals(realpath)) return cw
        }
        return null
    }

    fun getConfigWebById(id: String?): ConfigWeb? {
        val it: Iterator<String?> = initContextes.keySet().iterator()
        while (it.hasNext()) {
            val cw: ConfigWeb = (initContextes!![it.next()] as CFMLFactoryImpl?).getConfig()
            if (cw.getIdentification().getId().equals(id)) return cw
        }
        return null
    }

    /**
     * @return JspFactoryImpl array
     */
    fun getJSPFactories(): Array<CFMLFactoryImpl?>? {
        val it: Iterator<String?> = initContextes.keySet().iterator()
        val factories: Array<CFMLFactoryImpl?> = arrayOfNulls<CFMLFactoryImpl?>(initContextes!!.size())
        var index = 0
        while (it.hasNext()) {
            factories[index++] = initContextes!![it.next()] as CFMLFactoryImpl?
        }
        return factories
    }

    @Override
    fun getJSPFactoriesAsMap(): Map<String?, CFMLFactory?>? {
        return initContextes
    }

    @Override
    fun getSecurityManager(id: String?): SecurityManager? {
        val o: Object? = managers!![id]
        if (o != null) return o as SecurityManager?
        if (defaultSecurityManager == null) {
            defaultSecurityManager = SecurityManagerImpl.getOpenSecurityManager()
        }
        return defaultSecurityManager.cloneSecurityManager()
    }

    @Override
    fun hasIndividualSecurityManager(id: String?): Boolean {
        return managers!!.containsKey(id)
    }

    /**
     * @param defaultSecurityManager
     */
    fun setDefaultSecurityManager(defaultSecurityManager: SecurityManager?) {
        this.defaultSecurityManager = defaultSecurityManager
    }

    /**
     * @param id
     * @param securityManager
     */
    fun setSecurityManager(id: String?, securityManager: SecurityManager?) {
        managers.put(id, securityManager)
    }

    /**
     * @param id
     */
    fun removeSecurityManager(id: String?) {
        managers.remove(id)
    }

    @Override
    fun getDefaultSecurityManager(): SecurityManager? {
        return defaultSecurityManager
    }

    /**
     * @return Returns the defaultPassword.
     */
    fun getDefaultPassword(): Password? {
        return if (defaultPassword == null) password else defaultPassword
    }

    fun hasCustomDefaultPassword(): Boolean {
        return defaultPassword != null
    }

    /**
     * @param defaultPassword The defaultPassword to set.
     */
    fun setDefaultPassword(defaultPassword: Password?) {
        this.defaultPassword = defaultPassword
    }

    @Override
    fun getCFMLEngine(): CFMLEngine? {
        return getEngine()
    }

    @Override
    fun getEngine(): CFMLEngine? {
        return engine
    }

    /**
     * @return Returns the rootDir.
     */
    @Override
    override fun getRootDirectory(): Resource? {
        return rootDir
    }

    @Override
    override fun getUpdateType(): String? {
        return updateType
    }

    @Override
    fun setUpdateType(updateType: String?) {
        if (!StringUtil.isEmpty(updateType)) this.updateType = updateType
    }

    @Override
    override fun getUpdateLocation(): URL? {
        return updateLocation
    }

    @Override
    fun setUpdateLocation(updateLocation: URL?) {
        this.updateLocation = updateLocation
    }

    @Override
    @Throws(MalformedURLException::class)
    fun setUpdateLocation(strUpdateLocation: String?) {
        setUpdateLocation(URL(strUpdateLocation))
    }

    @Override
    fun setUpdateLocation(strUpdateLocation: String?, defaultValue: URL?) {
        try {
            setUpdateLocation(strUpdateLocation)
        } catch (e: MalformedURLException) {
            setUpdateLocation(defaultValue)
        }
    }

    @Override
    override fun getSecurityManager(): SecurityManager? {
        // sm.setAccess(SecurityManager.TYPE_ACCESS_READ,SecurityManager.ACCESS_PROTECTED);
        // sm.setAccess(SecurityManager.TYPE_ACCESS_WRITE,SecurityManager.ACCESS_PROTECTED);
        return getDefaultSecurityManager() as SecurityManagerImpl?
    }

    fun setLabels(labels: Map<String?, String?>?) {
        this.labels = labels
    }

    fun getLabels(): Map<String?, String?>? {
        if (labels == null) labels = HashMap<String?, String?>()
        return labels
    }

    private var threadQueue: ThreadQueue? = null
    fun setThreadQueue(threadQueue: ThreadQueue?): ThreadQueue? {
        return threadQueue.also { this.threadQueue = it }
    }

    @Override
    fun getThreadQueue(): ThreadQueue? {
        return threadQueue
    }

    @Override
    fun getRequestMonitors(): Array<RequestMonitor?>? {
        return requestMonitors
    }

    @Override
    @Throws(ApplicationException::class)
    fun getRequestMonitor(name: String?): RequestMonitor? {
        if (requestMonitors != null) for (i in requestMonitors.indices) {
            if (requestMonitors!![i].getName().equalsIgnoreCase(name)) return requestMonitors!![i]
        }
        throw ApplicationException("there is no request monitor registered with name [$name]")
    }

    fun setRequestMonitors(monitors: Array<RequestMonitor?>?) {
        requestMonitors = monitors
    }

    @Override
    fun getIntervallMonitors(): Array<IntervallMonitor?>? {
        return intervallMonitors
    }

    @Override
    @Throws(ApplicationException::class)
    fun getIntervallMonitor(name: String?): IntervallMonitor? {
        if (intervallMonitors != null) for (i in intervallMonitors.indices) {
            if (intervallMonitors!![i].getName().equalsIgnoreCase(name)) return intervallMonitors!![i]
        }
        throw ApplicationException("there is no intervall monitor registered with name [$name]")
    }

    fun setIntervallMonitors(monitors: Array<IntervallMonitor?>?) {
        intervallMonitors = monitors
    }

    fun setActionMonitorCollector(actionMonitorCollector: ActionMonitorCollector?) {
        this.actionMonitorCollector = actionMonitorCollector
    }

    fun getActionMonitorCollector(): ActionMonitorCollector? {
        return actionMonitorCollector
    }

    @Override
    fun getActionMonitor(name: String?): ActionMonitor? {
        return if (actionMonitorCollector == null) null else actionMonitorCollector.getActionMonitor(name)
    }

    @Override
    fun isMonitoringEnabled(): Boolean {
        return monitoringEnabled
    }

    fun setMonitoringEnabled(monitoringEnabled: Boolean) {
        this.monitoringEnabled = monitoringEnabled
    }

    fun setLoginDelay(delay: Int) {
        this.delay = delay
    }

    fun setLoginCaptcha(captcha: Boolean) {
        this.captcha = captcha
    }

    fun setRememberMe(rememberMe: Boolean) {
        this.rememberMe = rememberMe
    }

    @Override
    fun getLoginDelay(): Int {
        return delay
    }

    @Override
    fun getLoginCaptcha(): Boolean {
        return captcha
    }

    @Override
    fun getRememberMe(): Boolean {
        return rememberMe
    }

    @Override
    override fun reset() {
        super.reset()
        getThreadQueue().clear()
    }

    @Override
    fun getSecurityDirectory(): Resource? {
        var cacerts: Resource? = null
        // javax.net.ssl.trustStore
        val trustStore: String = SystemUtil.getPropertyEL("javax.net.ssl.trustStore")
        if (trustStore != null) {
            cacerts = ResourcesImpl.getFileResourceProvider().getResource(trustStore)
        }

        // security/cacerts
        if (cacerts == null || !cacerts.exists()) {
            cacerts = getConfigDir().getRealResource("security/cacerts")
            if (!cacerts.exists()) cacerts.mkdirs()
        }
        return cacerts
    }

    @Override
    fun checkPermGenSpace(check: Boolean) {
        val promille: Int = SystemUtil.getFreePermGenSpacePromille()
        val kbFreePermSpace: Long = SystemUtil.getFreePermGenSpaceSize() / 1024
        val percentageAvailable: Int = SystemUtil.getPermGenFreeSpaceAsAPercentageOfAvailable()

        // Pen Gen Space info not available indicated by a return of -1
        if (check && kbFreePermSpace < 0) {
            if (countLoadedPages() > 2000) shrink()
        } else if (check && percentageAvailable < permGenCleanUpThreshold) {
            shrink()
            if (permGenCleanUpThreshold >= 5) {
                // adjust the threshold allowed down so the amount of permgen can slowly grow to its allocated space
                // up to 100%
                setPermGenCleanUpThreshold(permGenCleanUpThreshold - 5)
            } else {
                LogUtil.log(ThreadLocalPageContext.getConfig(this), Log.LEVEL_WARN, ConfigServerImpl::class.java.getName(),
                        " Free Perm Gen Space is less than 5% free: shrinking all template classloaders : consider increasing allocated Perm Gen Space")
            }
        } else if (check && kbFreePermSpace < 2048) {
            LogUtil.log(ThreadLocalPageContext.getConfig(this), Log.LEVEL_WARN, ConfigServerImpl::class.java.getName(), " Free Perm Gen Space is less than 2Mb (free:" + (SystemUtil.getFreePermGenSpaceSize() / 1024).toString() + "kb), shrinking all template classloaders")
            // first request a GC and then check if it helps
            System.gc()
            if (SystemUtil.getFreePermGenSpaceSize() / 1024 < 2048) {
                shrink()
            }
        }
    }

    private fun shrink() {
        val webs: Array<ConfigWeb?>? = getConfigWebs()
        var count = 0
        for (i in webs.indices) {
            count += shrink(webs!![i] as ConfigWebPro?, false)
        }
        if (count == 0) {
            for (i in webs.indices) {
                shrink(webs!![i] as ConfigWebPro?, true)
            }
        }
    }

    fun getPermGenCleanUpThreshold(): Int {
        return permGenCleanUpThreshold
    }

    fun setPermGenCleanUpThreshold(permGenCleanUpThreshold: Int) {
        this.permGenCleanUpThreshold = permGenCleanUpThreshold
    }

    fun countLoadedPages(): Long {
        return -1
        // MUST implement
    }

    @Override
    @Throws(PageException::class)
    override fun createClusterScope(): Cluster? {
        var cluster: Cluster? = null
        try {
            if (Reflector.isInstaneOf(getClusterClass(), Cluster::class.java, false)) {
                cluster = ClassUtil.loadInstance(getClusterClass(), ArrayUtil.OBJECT_EMPTY) as Cluster
                cluster.init(this)
            } else if (Reflector.isInstaneOf(getClusterClass(), ClusterRemote::class.java, false)) {
                val cb: ClusterRemote = ClassUtil.loadInstance(getClusterClass(), ArrayUtil.OBJECT_EMPTY) as ClusterRemote
                cluster = ClusterWrap(this, cb)
                // cluster.init(cs);
            }
        } catch (e: Exception) {
            throw Caster.toPageException(e)
        }
        return cluster
    }

    @Override
    fun hasServerPassword(): Boolean {
        return hasPassword()
    }

    @Throws(PageException::class)
    fun getInstalledPatches(): Array<String?>? {
        val factory: CFMLEngineFactory = getCFMLEngine().getCFMLEngineFactory()
        return try {
            factory.getInstalledPatches()
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
            try {
                getInstalledPatchesOld(factory)
            } catch (e1: Exception) {
                throw Caster.toPageException(e1)
            }
        }
    }

    @Throws(IOException::class)
    private fun getInstalledPatchesOld(factory: CFMLEngineFactory?): Array<String?>? {
        val patchDir = File(factory.getResourceRoot(), "patches")
        if (!patchDir.exists()) patchDir.mkdirs()
        val patches: Array<File?> = patchDir.listFiles(ExtensionFilter(arrayOf<String?>("." + getCoreExtension())))
        val list: List<String?> = ArrayList<String?>()
        var name: String?
        val extLen: Int = getCoreExtension()!!.length() + 1
        for (i in patches.indices) {
            name = patches[i].getName()
            name = name.substring(0, name.length() - extLen)
            list.add(name)
        }
        val arr: Array<String?> = list.toArray(arrayOfNulls<String?>(list.size()))
        Arrays.sort(arr)
        return arr
    }

    private fun getCoreExtension(): String? {
        return "lco"
    }

    @Override
    fun allowRequestTimeout(): Boolean {
        return engine.allowRequestTimeout()
    }

    private var id: IdentificationServer? = null
    private var libHash: String? = null
    private var amfEngineCD: ClassDefinition<AMFEngine?>? = null
    private var amfEngineArgs: Map<String?, String?>? = null
    private var localExtensions: List<ExtensionDefintion?>? = null
    private var localExtHash: Long = 0
    private var localExtSize = -1
    private var gatewayEntries: Map<String?, GatewayEntry?>? = null
    private var adminMode: Short = ADMINMODE_SINGLE
    fun getAuthenticationKeys(): Array<String?>? {
        return if (authKeys == null) arrayOfNulls<String?>(0) else authKeys
    }

    fun setAuthenticationKeys(authKeys: Array<String?>?) {
        this.authKeys = authKeys
    }

    fun getConfigServer(key: String?, nonce: String?): ConfigServer? {
        return this
    }

    @Throws(ExpressionException::class)
    fun checkAccess(password: Password?) {
        if (!hasPassword()) throw ExpressionException("Cannot access, no password is defined")
        if (!passwordEqual(password)) throw ExpressionException("No access, password is invalid")
    }

    @Throws(PageException::class)
    fun checkAccess(key: String?, timeNonce: Long) {
        if (previousNonces.containsKey(timeNonce)) {
            val now: Long = System.currentTimeMillis()
            val diff = if (timeNonce > now) timeNonce - now else now - timeNonce
            if (diff > 10) throw ApplicationException("nonce was already used, same nonce can only be used once")
        }
        val now: Long = System.currentTimeMillis() + getTimeServerOffset()
        if (timeNonce > now + FIVE_SECONDS || timeNonce < now - FIVE_SECONDS) throw ApplicationException("nonce is outdated (timserver offset:" + getTimeServerOffset().toString() + ")")
        previousNonces.put(timeNonce, "")
        val keys = getAuthenticationKeys()
        // check if one of the keys matching
        var hash: String
        for (i in keys.indices) {
            try {
                hash = Hash.hash(keys!![i], Caster.toString(timeNonce), Hash.ALGORITHM_SHA_256, Hash.ENCODING_HEX)
                if (hash.equals(key)) return
            } catch (e: NoSuchAlgorithmException) {
                throw Caster.toPageException(e)
            }
        }
        throw ApplicationException("No access, no matching authentication key found")
    }

    @Override
    fun getIdentification(): IdentificationServer? {
        return id
    }

    fun setIdentification(id: IdentificationServer?) {
        this.id = id
    }

    @Override
    override fun getAllExtensionBundleDefintions(): Collection<BundleDefinition?>? {
        val rtn: Map<String?, BundleDefinition?> = HashMap()

        // server (this)
        var itt: Iterator<BundleDefinition?> = getExtensionBundleDefintions()!!.iterator()
        var bd: BundleDefinition?
        while (itt.hasNext()) {
            bd = itt.next()
            rtn.put(bd.getName().toString() + "|" + bd.getVersionAsString(), bd)
        }

        // webs
        val cws: Array<ConfigWeb?>? = getConfigWebs()
        for (cw in cws!!) {
            itt = (cw as ConfigPro?)!!.getExtensionBundleDefintions()!!.iterator()
            while (itt.hasNext()) {
                bd = itt.next()
                rtn.put(bd.getName().toString() + "|" + bd.getVersionAsString(), bd)
            }
        }
        return rtn.values()
    }

    @Override
    override fun getAllRHExtensions(): Collection<RHExtension?>? {
        val rtn: Map<String?, RHExtension?> = HashMap()

        // server (this)
        var arr: Array<RHExtension?> = getRHExtensions()
        for (rhe in arr) {
            rtn.put(rhe.getId(), rhe)
        }

        // webs
        val cws: Array<ConfigWeb?>? = getConfigWebs()
        for (cw in cws!!) {
            arr = (cw as ConfigWebPro?)!!.getRHExtensions()
            for (rhe in arr) {
                rtn.put(rhe.getId(), rhe)
            }
        }
        return rtn.values()
    }

    fun setLibHash(libHash: String?) {
        this.libHash = libHash
    }

    fun getLibHash(): String? {
        return libHash
    }

    @Override
    fun getLocalExtensionProviderDirectory(): Resource? {
        val dir: Resource = getConfigDir().getRealResource("extensions/available")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    protected fun setAMFEngine(cd: ClassDefinition<AMFEngine?>?, args: Map<String?, String?>?) {
        amfEngineCD = cd
        amfEngineArgs = args
    }

    fun getAMFEngineClassDefinition(): ClassDefinition<AMFEngine?>? {
        return amfEngineCD
    }

    fun getAMFEngineArgs(): Map<String?, String?>? {
        return amfEngineArgs
    }

    @Override
    override fun getServerRHExtensions(): Array<RHExtension?>? {
        return getRHExtensions()
    }

    @Override
    override fun loadLocalExtensions(validate: Boolean): List<ExtensionDefintion?>? {
        val locReses: Array<Resource?> = getLocalExtensionProviderDirectory().listResources(ExtensionResourceFilter(".lex"))
        if (validate || localExtensions == null || localExtSize != locReses.size || extHash(locReses) != localExtHash) {
            localExtensions = ArrayList<ExtensionDefintion?>()
            val map: Map<String?, String?> = HashMap<String?, String?>()
            var ext: RHExtension?
            var v: String?
            var fileName: String
            var uuid: String?
            var version: String?
            var ed: ExtensionDefintion?
            for (i in locReses.indices) {
                ed = null
                // we stay happy with the file name when it has the right pattern (uuid-version.lex)
                fileName = locReses[i].getName()
                if (!validate && fileName.length() > 39) {
                    uuid = fileName.substring(0, 35)
                    version = fileName.substring(36, fileName.length() - 4)
                    if (Decision.isUUId(uuid)) {
                        ed = ExtensionDefintion(uuid, version)
                        ed.setSource(this, locReses[i])
                    }
                }
                if (ed == null) {
                    try {
                        ext = RHExtension(this, locReses[i], false)
                        ed = ExtensionDefintion(ext.getId(), ext.getVersion())
                        ed.setSource(ext)
                    } catch (e: Exception) {
                        ed = null
                        LogUtil.log(ThreadLocalPageContext.getConfig(this), ConfigServerImpl::class.java.getName(), e)
                        try {
                            if (!IsZipFile.invoke(locReses[i])) locReses[i].remove(true)
                        } catch (ee: Exception) {
                            LogUtil.log(ThreadLocalPageContext.getConfig(this), ConfigServerImpl::class.java.getName(), ee)
                        }
                    }
                }
                if (ed != null) {
                    // check if we already have an extension with the same id to avoid having more than once
                    v = map[ed.getId()]
                    if (v != null && v.compareToIgnoreCase(ed.getId()) > 0) continue
                    map.put(ed.getId(), ed.getVersion())
                    localExtensions.add(ed)
                }
            }
            localExtHash = extHash(locReses)
            localExtSize = locReses.size // we store the size because localExtensions size could be smaller because of duplicates
        }
        return localExtensions
    }

    private fun extHash(locReses: Array<Resource?>?): Long {
        val sb = StringBuilder()
        if (locReses != null) {
            for (locRes in locReses) {
                sb.append(locRes.getAbsolutePath()).append(';')
            }
        }
        return HashUtil.create64BitHash(sb)
    }

    @Override
    protected override fun setGatewayEntries(gatewayEntries: Map<String?, GatewayEntry?>?) {
        this.gatewayEntries = gatewayEntries
    }

    @Override
    override fun getGatewayEntries(): Map<String?, GatewayEntry?>? {
        return gatewayEntries
    }

    @Override
    @Throws(PageException::class)
    override fun checkPassword() {
        val engine: CFMLEngine = ConfigWebUtil.getEngine(this)
        val webs: Array<ConfigWeb?>? = getConfigWebs()
        try {
            ConfigServerFactory.reloadInstance(engine, this)
            for (web in webs!!) {
                if (web is ConfigWebImpl) ConfigWebFactory.reloadInstance(engine, this, web as ConfigWebImpl?, true) else if (web is SingleContextConfigWeb) (web as SingleContextConfigWeb?)!!.reload()
            }
        } catch (e: Exception) {
            throw Caster.toPageException(e)
        }
    }

    fun setAdminMode(adminMode: Short) {
        this.adminMode = adminMode
    }

    @Override
    override fun getAdminMode(): Short {
        return adminMode
    }

    companion object {
        private const val FIVE_SECONDS: Long = 5000
        private fun shrink(config: ConfigWebPro?, force: Boolean): Int {
            var count = 0
            count += shrink(config.getMappings(), force)
            count += shrink(config.getCustomTagMappings(), force)
            count += shrink(config.getComponentMappings(), force)
            count += shrink(config!!.getFunctionMappings(), force)
            count += shrink(config!!.getServerFunctionMappings(), force)
            count += shrink(config!!.getTagMappings(), force)
            count += shrink(config!!.getServerTagMappings(), force)
            // count+=shrink(config.getServerTagMapping(),force);
            return count
        }

        private fun shrink(mappings: Collection<Mapping?>?, force: Boolean): Int {
            var count = 0
            val it: Iterator<Mapping?> = mappings!!.iterator()
            while (it.hasNext()) {
                count += shrink(it.next(), force)
            }
            return count
        }

        private fun shrink(mappings: Array<Mapping?>?, force: Boolean): Int {
            var count = 0
            for (i in mappings.indices) {
                count += shrink(mappings!![i], force)
            }
            return count
        }

        private fun shrink(mapping: Mapping?, force: Boolean): Int {
            try {
                // PCLCollection pcl = ((MappingImpl)mapping).getPCLCollection();
                // if(pcl!=null)return pcl.shrink(force);
                (mapping as MappingImpl?).shrink()
            } catch (t: Throwable) {
                ExceptionUtil.rethrowIfNecessary(t)
            }
            return 0
        }
    }

    /**
     * @param engine
     * @param srvConfig
     * @param initContextes
     * @param contextes
     * @param configDir
     * @param configFile
     * @param updateInfo
     * @throws TagLibException
     * @throws FunctionLibException
     */
    init {
        cfmlCoreTLDs = TagLibFactory.loadFromSystem(CFMLEngine.DIALECT_CFML, id)
        tachyonCoreTLDs = TagLibFactory.loadFromSystem(CFMLEngine.DIALECT_LUCEE, id)
        cfmlCoreFLDs = FunctionLibFactory.loadFromSystem(CFMLEngine.DIALECT_CFML, id)
        tachyonCoreFLDs = FunctionLibFactory.loadFromSystem(CFMLEngine.DIALECT_LUCEE, id)
        this.engine = engine
        if (!essentialOnly) engine.setConfigServerImpl(this)
        this.initContextes = initContextes
        // this.contextes=contextes;
        rootDir = configDir
        // instance=this;
        this.updateInfo = updateInfo
    }
}