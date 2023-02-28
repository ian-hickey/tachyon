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

import java.net.URL

/**
 * Web Context
 */
class ConfigWebImpl internal constructor(factory: CFMLFactoryImpl?, configServer: ConfigServerImpl?, config: ServletConfig?, configDir: Resource?, configFile: Resource?) : ConfigImpl(configDir, configFile), ServletConfig, ConfigWebPro {
    private val config: ServletConfig?
    private val configServer: ConfigServerImpl?
    private var securityManager: SecurityManager? = null
    private var rootDir: Resource?
    private val factory: CFMLFactoryImpl?
    private val helper: ConfigWebHelper?
    private var passwordSource: Short = 0

    @Override
    override fun reset() {
        super.reset()
        factory.resetPageContext()
        helper!!.reset()
    }

    @Override
    fun getServletName(): String? {
        return config.getServletName()
    }

    @Override
    fun getServletContext(): ServletContext? {
        return config.getServletContext()
    }

    @Override
    fun getInitParameter(name: String?): String? {
        return config.getInitParameter(name)
    }

    @Override
    fun getInitParameterNames(): Enumeration? {
        return config.getInitParameterNames()
    }

    fun getConfigServerImpl(): ConfigServerImpl? {
        return configServer
    }

    @Override
    @Throws(ExpressionException::class)
    fun getConfigServer(password: String?): ConfigServer? {
        var pw: Password? = isServerPasswordEqual(password)
        if (pw == null) pw = PasswordImpl.passwordToCompare(this, true, password)
        return getConfigServer(pw)
    }

    @Override
    @Throws(ExpressionException::class)
    fun getConfigServer(password: Password?): ConfigServer? {
        configServer!!.checkAccess(password)
        return configServer
    }

    @Override
    @Throws(PageException::class)
    fun getConfigServer(key: String?, timeNonce: Long): ConfigServer? {
        configServer!!.checkAccess(key, timeNonce)
        return configServer
    }

    fun getServerConfigDir(): Resource? {
        return configServer!!.getConfigDir()
    }

    /**
     * @return Returns the accessor.
     */
    @Override
    override fun getSecurityManager(): SecurityManager? {
        return securityManager
    }

    /**
     * @param securityManager The accessor to set.
     */
    fun setSecurityManager(securityManager: SecurityManager?) {
        (securityManager as SecurityManagerImpl?).setRootDirectory(getRootDirectory())
        this.securityManager = securityManager
    }

    @Override
    @Throws(SecurityException::class)
    override fun getCFXTagPool(): CFXTagPool? {
        if (securityManager.getAccess(SecurityManager.TYPE_CFX_USAGE) === SecurityManager.VALUE_YES) return super.getCFXTagPool()
        throw SecurityException("no access to cfx functionality", "disabled by security settings")
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
        return configServer!!.getUpdateType()
    }

    @Override
    override fun getUpdateLocation(): URL? {
        return configServer!!.getUpdateLocation()
    }

    @Override
    fun getLockManager(): LockManager? {
        return helper!!.getLockManager()
    }

    /**
     * @return the compiler
     */
    @Override
    override fun getCompiler(): CFMLCompilerImpl? {
        return helper!!.getCompiler()
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
    override fun getServerTagMappings(): Collection<Mapping?>? {
        return helper!!.getServerTagMappings()
    }

    @Override
    override fun getDefaultServerTagMapping(): Mapping? {
        return getConfigServerImpl()!!.defaultTagMapping
    }

    @Override
    override fun getServerTagMapping(mappingName: String?): Mapping? {
        return helper!!.getServerTagMapping(mappingName)
    }

    @Override
    override fun getServerFunctionMappings(): Collection<Mapping?>? {
        return helper!!.getServerFunctionMappings()
    }

    @Override
    override fun resetServerFunctionMappings() {
        helper!!.resetServerFunctionMappings()
    }

    @Override
    override fun getServerFunctionMapping(mappingName: String?): Mapping? {
        return helper!!.getServerFunctionMapping(mappingName)
    }

    fun getDefaultServerFunctionMapping(): Mapping? {
        return getConfigServerImpl()!!.defaultFunctionMapping
    }

    // FYI used by Extensions, do not remove
    fun getApplicationMapping(virtual: String?, physical: String?): Mapping? {
        return getApplicationMapping("application", virtual, physical, null, true, false)
    }

    @Override
    override fun isApplicationMapping(mapping: Mapping?): Boolean {
        return helper!!.isApplicationMapping(mapping)
    }

    @Override
    override fun getApplicationMapping(type: String?, virtual: String?, physical: String?, archive: String?, physicalFirst: Boolean, ignoreVirtual: Boolean): Mapping? {
        return getApplicationMapping(type, virtual, physical, archive, physicalFirst, ignoreVirtual, true, true)
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
    fun getLabel(): String? {
        return helper!!.getLabel()
    }

    @Override
    override fun getHash(): String? {
        return SystemUtil.hash(getServletContext())
    }

    @Override
    override fun getContextLock(): KeyLock<String?>? {
        return helper!!.getContextLock()
    }

    @Override
    override fun getGatewayEntries(): Map<String?, GatewayEntry?>? {
        return helper!!.getGatewayEngineImpl().getEntries()
    }

    @Override
    protected override fun setGatewayEntries(gatewayEntries: Map<String?, GatewayEntry?>?) {
        try {
            helper!!.getGatewayEngineImpl().addEntries(this, gatewayEntries)
        } catch (e: Exception) {
            LogUtil.log(this, ConfigWebImpl::class.java.getName(), e)
        }
    }

    @Override
    override fun getGatewayEngine(): GatewayEngine? {
        return helper!!.getGatewayEngineImpl()
    }

    @Override
    override fun getTagHandlerPool(): TagHandlerPool? {
        return helper!!.getTagHandlerPool()
    }

    @Override
    override fun getDebuggerPool(): DebuggerPool? {
        return helper!!.getDebuggerPool()
    }

    @Override
    fun getThreadQueue(): ThreadQueue? {
        return configServer!!.getThreadQueue()
    }

    @Override
    fun getLoginDelay(): Int {
        return configServer!!.getLoginDelay()
    }

    @Override
    fun getLoginCaptcha(): Boolean {
        return configServer!!.getLoginCaptcha()
    }

    @Override
    fun getRememberMe(): Boolean {
        return configServer!!.getRememberMe()
    }

    @Override
    fun getSecurityDirectory(): Resource? {
        return configServer!!.getSecurityDirectory()
    }

    @Override
    fun isMonitoringEnabled(): Boolean {
        return configServer!!.isMonitoringEnabled()
    }

    @Override
    fun getRequestMonitors(): Array<RequestMonitor?>? {
        return configServer!!.getRequestMonitors()
    }

    @Override
    @Throws(PageException::class)
    fun getRequestMonitor(name: String?): RequestMonitor? {
        return configServer!!.getRequestMonitor(name)
    }

    @Override
    fun getIntervallMonitors(): Array<IntervallMonitor?>? {
        return configServer!!.getIntervallMonitors()
    }

    @Override
    @Throws(PageException::class)
    fun getIntervallMonitor(name: String?): IntervallMonitor? {
        return configServer!!.getIntervallMonitor(name)
    }

    @Override
    fun checkPermGenSpace(check: Boolean) {
        configServer!!.checkPermGenSpace(check)
    }

    @Override
    @Throws(PageException::class)
    override fun createClusterScope(): Cluster? {
        return configServer!!.createClusterScope()
    }

    @Override
    fun hasServerPassword(): Boolean {
        return configServer!!.hasPassword()
    }

    @Override
    @Throws(PageException::class)
    override fun updatePassword(server: Boolean, passwordOld: String?, passwordNew: String?) {
        try {
            PasswordImpl.updatePassword(if (server) configServer else this, passwordOld, passwordNew)
        } catch (e: Exception) {
            throw Caster.toPageException(e)
        }
    }

    @Throws(PageException::class)
    override fun updatePassword(server: Boolean, passwordOld: Password?, passwordNew: Password?) {
        try {
            PasswordImpl.updatePassword(if (server) configServer else this, passwordOld, passwordNew)
        } catch (e: Exception) {
            throw Caster.toPageException(e)
        }
    }

    @Override
    override fun updatePasswordIfNecessary(server: Boolean, passwordRaw: String?): Password? {
        val config: ConfigPro = if (server) configServer else this
        return PasswordImpl.updatePasswordIfNecessary(config, (config as ConfigImpl)!!.password, passwordRaw)
    }

    @Override
    fun getConfigServerDir(): Resource? {
        return configServer!!.getConfigDir()
    }

    @Override
    override fun getAllLabels(): Map<String?, String?>? {
        return configServer!!.getLabels()
    }

    @Override
    fun allowRequestTimeout(): Boolean {
        return configServer!!.allowRequestTimeout()
    }

    @Override
    override fun getCFMLWriter(pc: PageContext?, req: HttpServletRequest?, rsp: HttpServletResponse?): CFMLWriter? {
        return helper!!.getCFMLWriter(pc, req, rsp)
    }

    @Override
    fun getWriter(pc: PageContext?, req: HttpServletRequest?, rsp: HttpServletResponse?): JspWriter? {
        return getCFMLWriter(pc, req, rsp)
    }

    @Override
    override fun getActionMonitorCollector(): ActionMonitorCollector? {
        return configServer!!.getActionMonitorCollector()
    }

    @Override
    override fun hasIndividualSecurityManager(): Boolean {
        return helper!!.hasIndividualSecurityManager(this)
    }

    @Override
    fun getFactory(): CFMLFactory? {
        return factory
    }

    @Override
    fun getCacheHandlerCollection(type: Int, defaultValue: CacheHandlerCollection?): CacheHandlerCollection? {
        return helper!!.getCacheHandlerCollection(type, defaultValue)
    }

    @Override
    override fun releaseCacheHandlers(pc: PageContext?) {
        helper!!.releaseCacheHandlers(pc)
    }

    fun setIdentification(id: IdentificationWeb?) {
        helper!!.setIdentification(id)
    }

    @Override
    fun getIdentification(): IdentificationWeb? {
        return helper!!.getIdentification()
    }

    @Override
    override fun getServerPasswordType(): Int {
        return configServer!!.getPasswordType()
    }

    @Override
    override fun getServerPasswordSalt(): String? {
        return configServer!!.getPasswordSalt()
    }

    @Override
    override fun getServerPasswordOrigin(): Int {
        return configServer!!.getPasswordOrigin()
    }

    fun getServerSalt(): String? {
        return configServer!!.getSalt()
    }

    @Override
    override fun isServerPasswordEqual(password: String?): Password? {
        return configServer!!.isPasswordEqual(password)
    }

    @Override
    override fun isDefaultPassword(): Boolean {
        return if (password == null) false else password === configServer!!.defaultPassword
    }

    @Override
    override fun getAllExtensionBundleDefintions(): Collection<BundleDefinition?>? {
        return configServer!!.getAllExtensionBundleDefintions()
    }

    @Override
    override fun getAllRHExtensions(): Collection<RHExtension?>? {
        return configServer!!.getAllRHExtensions()
    }

    @Override
    @Throws(PageException::class)
    fun getSearchEngine(pc: PageContext?): SearchEngine? {
        return helper!!.getSearchEngine(pc)
    }

    @Override
    fun getActionMonitor(name: String?): ActionMonitor? {
        return configServer!!.getActionMonitor(name)
    }

    @Override
    fun getLocalExtensionProviderDirectory(): Resource? {
        return configServer!!.getLocalExtensionProviderDirectory()
    }

    protected fun setAMFEngine(engine: AMFEngine?) {
        helper!!.setAMFEngine(engine)
    }

    @Override
    fun getAMFEngine(): AMFEngine? {
        return helper!!.getAMFEngine()
    }

    /*
	 * public boolean installServerExtension(ExtensionDefintion ed) throws PageException { return
	 * configServer.installExtension(ed); }
	 */
    @Override
    override fun getServerRHExtensions(): Array<RHExtension?>? {
        return configServer!!.getRHExtensions()
    }

    @Override
    override fun loadLocalExtensions(validate: Boolean): List<ExtensionDefintion?>? {
        return configServer!!.loadLocalExtensions(validate)
    }

    @Override
    @Throws(PageException::class)
    override fun getWSHandler(): WSHandler? {
        return helper!!.getWSHandler()
    }

    fun setPasswordSource(passwordSource: Short) {
        this.passwordSource = passwordSource
    }

    @Override
    override fun getPasswordSource(): Short {
        return passwordSource
    }

    @Override
    @Throws(PageException::class)
    override fun checkPassword() {
        configServer!!.checkPassword()
    }

    @Override
    override fun getAdminMode(): Short {
        return configServer!!.getAdminMode()
    }
    // private File deployDirectory;
    /**
     * constructor of the class
     *
     * @param configServer
     * @param config
     * @param configDir
     * @param configFile
     * @param cloneServer
     */
    init {
        this.configServer = configServer
        this.config = config
        this.factory = factory
        factory.setConfig(this)
        val frp: ResourceProvider = ResourcesImpl.getFileResourceProvider()
        rootDir = frp.getResource(ReqRspUtil.getRootPath(config.getServletContext()))

        // Fix for tomcat
        if (rootDir.getName().equals(".") || rootDir.getName().equals("..")) rootDir = rootDir.getParentResource()
        helper = ConfigWebHelper(configServer, this)
    }
}