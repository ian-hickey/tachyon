package lucee.runtime.config

import java.io.IOException

//FUTURE add to Config
interface ConfigWebPro : ConfigWeb, ConfigPro {
    fun getDefaultServerTagMapping(): Mapping?
    fun getApplicationMapping(type: String?, virtual: String?, physical: String?, archive: String?, physicalFirst: Boolean, ignoreVirtual: Boolean): Mapping?
    fun getServerFunctionMappings(): Collection<Mapping?>?
    fun getServerFunctionMapping(mappingName: String?): Mapping?
    fun getServerTagMappings(): Collection<Mapping?>?
    fun getServerTagMapping(mappingName: String?): Mapping?
    fun getAllLabels(): Map<String?, String?>?
    fun isDefaultPassword(): Boolean
    fun getServerPasswordType(): Int
    fun getServerPasswordSalt(): String?
    fun getServerPasswordOrigin(): Int

    @Throws(PageException::class)
    fun getWSHandler(): WSHandler?
    fun getGatewayEngine(): GatewayEngine?
    fun getCompiler(): CFMLCompilerImpl?
    fun getApplicationMapping(type: String?, virtual: String?, physical: String?, archive: String?, physicalFirst: Boolean, ignoreVirtual: Boolean,
                              checkPhysicalFromWebroot: Boolean, checkArchiveFromWebroot: Boolean): Mapping?

    fun getApplicationMappings(): Array<Mapping?>?
    fun isApplicationMapping(mapping: Mapping?): Boolean

    @Throws(PageException::class)
    fun getBaseComponentPage(dialect: Int, pc: PageContext?): CIPage?
    fun resetBaseComponentPage()
    fun getActionMonitorCollector(): ActionMonitorCollector?
    fun getContextLock(): KeyLock<String?>?
    fun releaseCacheHandlers(pc: PageContext?)
    fun getDebuggerPool(): DebuggerPool?
    fun getCFMLWriter(pc: PageContext?, req: HttpServletRequest?, rsp: HttpServletResponse?): CFMLWriter?
    fun getTagHandlerPool(): TagHandlerPool?
    fun getHash(): String?

    @Throws(PageException::class, IOException::class, SAXException::class, BundleException::class)
    fun updatePassword(server: Boolean, passwordOld: String?, passwordNew: String?)
    fun updatePasswordIfNecessary(server: Boolean, passwordRaw: String?): Password?
    fun isServerPasswordEqual(password: String?): Password?
    fun hasIndividualSecurityManager(): Boolean
    fun getPasswordSource(): Short
    fun resetServerFunctionMappings()

    companion object {
        const val PASSWORD_ORIGIN_DEFAULT: Short = 1
        const val PASSWORD_ORIGIN_SERVER: Short = 2
        const val PASSWORD_ORIGIN_WEB: Short = 3
    }
}