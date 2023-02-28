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
package tachyon.runtime.engine

import java.io.ByteArrayInputStream

/**
 * The CFMl Engine
 */
class CFMLEngineImpl private constructor(factory: CFMLEngineFactory?, bc: BundleCollection?) : CFMLEngine {
    companion object {
        val CONSOLE_ERR: PrintStream? = System.err
        val CONSOLE_OUT: PrintStream? = System.out
        private val initContextes: Map<String?, CFMLFactory?>? = MapFactory.< String, CFMLFactory>getConcurrentMap<String?, CFMLFactory?>()
        private val contextes: Map<String?, CFMLFactory?>? = MapFactory.< String, CFMLFactory>getConcurrentMap<String?, CFMLFactory?>()
        private var engine: CFMLEngineImpl? = null
        private fun checkInvalidExtensions(eng: CFMLEngineImpl?, config: ConfigPro?, extensionsToInstall: Set<ExtensionDefintion?>?, extensionsToRemove: Set<String?>?) {
            val extensions: Array<RHExtension?> = config.getRHExtensions()
            if (extensions != null) {
                val info: InfoImpl? = eng!!.getInfo()
                var valid: Boolean
                for (ext in extensions) {
                    try {
                        ext.validate(info)
                        valid = true
                    } catch (ae: ApplicationException) {
                        valid = false
                        LogUtil.log("debug", "check-invalid-extension", ae)
                    }
                    if (!valid) {
                        try {
                            val ed: ExtensionDefintion? = getRequiredExtension(info, ext.getId())
                            if (ed != null) {
                                extensionsToInstall.add(ed)
                                LogUtil.log(Log.LEVEL_INFO, "debug", "check-invalid-extension",
                                        "Installed extension [$ext] is invalid and get removed and replaced by [$ed]")
                            } else {
                                extensionsToRemove.add(ext.toExtensionDefinition().getId())
                                LogUtil.log(Log.LEVEL_INFO, "debug", "check-invalid-extension", "Installed extension [$ext] is invalid and was removed.")
                            }
                        } catch (e: Exception) {
                            LogUtil.log("debug", ConfigWebFactory::class.java.getName(), e)
                        }
                    }
                }
            }
        }

        private fun getRequiredExtension(info: InfoImpl?, id: String?): ExtensionDefintion? {
            val reqExt: List<ExtensionDefintion?> = info!!.getRequiredExtension()
            if (reqExt != null) {
                for (ed in reqExt) {
                    if (ed.getId().equals(id)) return ed
                }
            }
            return null
        }

        fun toSet(set: Set<ExtensionDefintion?>?, list: List<ExtensionDefintion?>?): Set<ExtensionDefintion?>? {
            val map: LinkedHashMap<String?, ExtensionDefintion?> = LinkedHashMap<String?, ExtensionDefintion?>()
            var ed: ExtensionDefintion

            // set > map
            if (set != null) {
                val it: Iterator<ExtensionDefintion?> = set.iterator()
                while (it.hasNext()) {
                    ed = it.next()
                    map.put(ed.toString(), ed)
                }
            }

            // list > map
            if (list != null) {
                val it: Iterator<ExtensionDefintion?> = list.iterator()
                while (it.hasNext()) {
                    ed = it.next()
                    map.put(ed.toString(), ed)
                }
            }

            // to Set
            val rtn: LinkedHashSet<ExtensionDefintion?> = LinkedHashSet<ExtensionDefintion?>()
            val it: Iterator<ExtensionDefintion?> = map.values().iterator()
            while (it.hasNext()) {
                ed = it.next()
                rtn.add(ed)
            }
            return rtn
        }

        fun toList(coll: Collection<ExtensionDefintion?>?): String? {
            val sb = StringBuilder()
            val it: Iterator<ExtensionDefintion?> = coll!!.iterator()
            var ed: ExtensionDefintion?
            while (it.hasNext()) {
                ed = it.next()
                if (sb.length() > 0) sb.append(", ")
                sb.append(ed.toString())
            }
            return sb.toString()
        }

        /**
         * get singelton instance of the CFML Engine
         *
         * @param factory
         * @return CFMLEngine
         */
        @Synchronized
        fun getInstance(factory: CFMLEngineFactory?, bc: BundleCollection?): CFMLEngine? {
            if (engine == null) {
                if (SystemUtil.getLoaderVersion() < 6.0) {
                    // windows needs 6.0 because restart is not working with older versions
                    if (SystemUtil.isWindows()) throw RuntimeException("You need to update a newer tachyon.jar to run this version, you can download the latest jar from https://download.tachyon.org.") else if (SystemUtil.getLoaderVersion() < 5.8) throw RuntimeException("You need to update your tachyon.jar to run this version, you can download the latest jar from https://download.tachyon.org.") else if (SystemUtil.getLoaderVersion() < 5.9) LogUtil.log(Log.LEVEL_INFO, "startup",
                            "To use all features Tachyon provides, you need to update your tachyon.jar, you can download the latest jar from https://download.tachyon.org.")
                }
                engine = CFMLEngineImpl(factory, bc)
            }
            return engine
        }

        /**
         * get singelton instance of the CFML Engine, throwsexception when not already init
         *
         * @param factory
         * @return CFMLEngine
         */
        @Synchronized
        @Throws(ServletException::class)
        fun getInstance(): CFMLEngine? {
            if (engine != null) return engine
            throw ServletException("CFML Engine is not loaded")
        }

        @Throws(IOException::class)
        fun getSeverContextConfigDirectory(factory: CFMLEngineFactory?): Resource? {
            val frp: ResourceProvider = ResourcesImpl.getFileResourceProvider()
            return frp.getResource(factory.getResourceRoot().getAbsolutePath()).getRealResource("context")
        }

        @Throws(IOException::class)
        private fun copyRecursiveAndRename(src: Resource?, trg: Resource?) {
            var trg: Resource? = trg
            if (!src.exists()) return
            if (src.isDirectory()) {
                if (!trg.exists()) trg.mkdirs()
                val files: Array<Resource?> = src.listResources()
                for (i in files.indices) {
                    copyRecursiveAndRename(files[i], trg.getRealResource(files[i].getName()))
                }
            } else if (src.isFile()) {
                if (trg.getName().endsWith(".rc") || trg.getName().startsWith(".")) {
                    return
                }
                if (trg.getName().equals("railo-web.xml.cfm")) {
                    trg = trg.getParentResource().getRealResource("tachyon-web.xml.cfm")
                    // cfTachyonConfiguration
                    val `is`: InputStream = src.getInputStream()
                    val os: OutputStream = trg.getOutputStream()
                    try {
                        var str: String? = Util.toString(`is`)
                        str = str.replace("<cfRailoConfiguration", "<!-- copy from Railo context --><cfTachyonConfiguration")
                        str = str.replace("</cfRailoConfiguration", "</cfTachyonConfiguration")
                        str = str.replace("<railo-configuration", "<tachyon-configuration")
                        str = str.replace("</railo-configuration", "</tachyon-configuration")
                        str = str.replace("{railo-config}", "{tachyon-config}")
                        str = str.replace("{railo-server}", "{tachyon-server}")
                        str = str.replace("{railo-web}", "{tachyon-web}")
                        str = str.replace("\"railo.commons.", "\"tachyon.commons.")
                        str = str.replace("\"railo.runtime.", "\"tachyon.runtime.")
                        str = str.replace("\"railo.cfx.", "\"tachyon.cfx.")
                        str = str.replace("/railo-context.ra", "/tachyon-context.lar")
                        str = str.replace("/railo-context", "/tachyon")
                        str = str.replace("railo-server-context", "tachyon-server")
                        str = str.replace("http://www.getrailo.org", "https://update.tachyon.org")
                        str = str.replace("http://www.getrailo.com", "https://update.tachyon.org")
                        val bais = ByteArrayInputStream(str.getBytes())
                        try {
                            Util.copy(bais, os)
                            bais.close()
                        } finally {
                            Util.closeEL(`is`, os)
                        }
                    } finally {
                        Util.closeEL(`is`, os)
                    }
                    return
                }
                val `is`: InputStream = src.getInputStream()
                val os: OutputStream = trg.getOutputStream()
                try {
                    Util.copy(`is`, os)
                } finally {
                    Util.closeEL(`is`, os)
                }
            }
        }

        fun releaseCache(config: Config?) {
            CacheUtil.releaseAll(config)
            if (config is ConfigServer) CacheUtil.releaseAllApplication()
        }

        @Throws(CasterException::class)
        fun toCFMLEngineImpl(e: CFMLEngine?): CFMLEngineImpl? {
            if (e is CFMLEngineImpl) return e
            if (e is CFMLEngineWrapper) return toCFMLEngineImpl((e as CFMLEngineWrapper?).getEngine())
            throw CasterException(e, CFMLEngineImpl::class.java)
        }

        fun toCFMLEngineImpl(e: CFMLEngine?, defaultValue: CFMLEngineImpl?): CFMLEngineImpl? {
            if (e is CFMLEngineImpl) return e
            return if (e is CFMLEngineWrapper) toCFMLEngineImpl((e as CFMLEngineWrapper?).getEngine(), defaultValue) else defaultValue
        }

        fun quick(engine: CFMLEngine?): Boolean {
            var engine: CFMLEngine? = engine
            while (engine is CFMLEngineWrapper) {
                engine = (engine as CFMLEngineWrapper?).getEngine()
            }
            return if (engine is CFMLEngineImpl) (engine as CFMLEngineImpl?)!!.quick else false
        }

        init {
            System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.NoOpLog")
        }
    }

    private var configServer: ConfigServerImpl? = null
    private val factory: CFMLEngineFactory?
    private val controlerState: ControllerStateImpl? = ControllerStateImpl(true)
    private var allowRequestTimeout = true
    private var monitor: Monitor? = null
    private val servletConfigs: List<ServletConfig?>? = ArrayList<ServletConfig?>()
    private val uptime: Long
    private val info: InfoImpl?
    private var bundleCollection: BundleCollection?
    private var cfmlScriptEngine: ScriptEngineFactory? = null
    private var cfmlTagEngine: ScriptEngineFactory? = null
    private var tachyonScriptEngine: ScriptEngineFactory? = null
    private var tachyonTagEngine: ScriptEngineFactory? = null
    private val controler: Controler?
    private var scl: CFMLServletContextListener? = null
    private var asyncReqHandle: Boolean? = null
    private var envExt: String?
    private var quick = false
    fun deployBundledExtension(validate: Boolean): Int {
        return deployBundledExtension(getConfigServerImpl(), validate)
    }

    private fun deployBundledExtension(cs: ConfigServerImpl?, validate: Boolean): Int {
        var count = 0
        val dir: Resource = cs.getLocalExtensionProviderDirectory()
        val existing: List<ExtensionDefintion?> = DeployHandler.getLocalExtensions(cs, validate)
        val existingMap: Map<String?, ExtensionDefintion?> = HashMap<String?, ExtensionDefintion?>()
        run {
            val it: Iterator<ExtensionDefintion?> = existing.iterator()
            var ed: ExtensionDefintion?
            while (it.hasNext()) {
                ed = it.next()
                try {
                    existingMap.put(ed.getSource().getName(), ed)
                } catch (e: ApplicationException) {
                }
            }
        }
        val log: Log = cs.getLog("deploy")

        // get the index
        val cl: ClassLoader = CFMLEngineFactory.getInstance().getCFMLEngineFactory().getClass().getClassLoader()
        var `is`: InputStream = cl.getResourceAsStream("extensions/.index")
        if (`is` == null) `is` = cl.getResourceAsStream("/extensions/.index")
        if (`is` == null) `is` = SystemUtil.getResourceAsStream(null, "/extensions/.index")
        if (`is` == null) {
            log.error("extract-extension", "Could not find [/extensions/.index] defined in the index of the tachyon.jar")
            return count
        }
        try {
            val index: String = IOUtil.toString(`is`, CharsetUtil.UTF8)
            // log.info("extract-extension", "the following extensions are bundled with the tachyon.jar [" + index
            // + "]");
            val names: Array<String?> = tachyon.runtime.type.util.ListUtil.listToStringArray(index, ';')
            var name: String?
            var temp: Resource? = null
            var rhe: RHExtension?
            var exist: ExtensionDefintion
            var it: Iterator<ExtensionDefintion?>
            for (i in names.indices) {
                name = names[i]
                if (StringUtil.isEmpty(name, true)) continue
                name = name.trim()

                // does it already exist?
                if (existingMap.containsKey(name)) {
                    continue
                }
                log.info("extract-extension", "Extract the extension [$name] from the tachyon.jar to the local extension folder [$dir]")
                `is` = cl.getResourceAsStream("extensions/$name")
                if (`is` == null) `is` = cl.getResourceAsStream("/extensions/$name")
                if (`is` == null) {
                    log.error("extract-extension", "Could not find extension [$name] defined in the index in the tachyon.jar")
                    continue
                }
                try {
                    temp = SystemUtil.getTempDirectory().getRealResource(name)
                    log.info("extract-extension", "Copy extension [$name] to temp directory [$temp]")
                    ResourceUtil.touch(temp)
                    Util.copy(`is`, temp.getOutputStream(), false, true)
                    rhe = RHExtension(cs, temp, false)
                    rhe.validate()
                    var alreadyExists: ExtensionDefintion? = null
                    it = existing.iterator()
                    while (it.hasNext()) {
                        exist = it.next()
                        if (exist.equals(rhe)) {
                            alreadyExists = exist
                            break
                        }
                    }
                    var trgName: String = rhe.getId().toString() + "-" + rhe.getVersion() + ".lex"
                    if (alreadyExists == null) {
                        temp.moveTo(dir.getRealResource(trgName))
                        count++
                        log.debug("extract-extension", "Added [$name] to [$dir]")
                    } else if (!alreadyExists.getSource().getName().equals(trgName)) {
                        log.debug("extract-extension", "Rename [" + alreadyExists.getSource().toString() + "] to [" + trgName + "]")
                        alreadyExists.getSource().moveTo(alreadyExists.getSource().getParentResource().getRealResource(trgName))
                    } else {
                        log.info("extract-extension", "Extension  [$name] already exists in local extension directory")
                    }

                    // now we check all extension name (for extension no longer delivered by tachyon)
                    it = existing.iterator()
                    while (it.hasNext()) {
                        exist = it.next()
                        trgName = exist.getId().toString() + "-" + exist.getVersion() + ".lex"
                        if (!trgName.equals(exist.getSource().getName())) {
                            exist.getSource().moveTo(exist.getSource().getParentResource().getRealResource(trgName))
                            log.debug("extract-extension", "Rename [" + exist.getSource().toString() + "] to [" + trgName + "]")
                        }
                    }
                } catch (e: Exception) {
                    log.error("extract-extension", e)
                } finally {
                    if (temp != null && temp.exists()) temp.delete()
                }
            }
        } catch (e: Exception) {
            log.error("extract-extension", e)
        }
        return count
    }

    private fun deployBundledExtensionZip(cs: ConfigServerImpl?) {
        val dir: Resource = cs.getLocalExtensionProviderDirectory()
        val existing: List<ExtensionDefintion?> = DeployHandler.getLocalExtensions(cs, false)
        val sub = "extensions/"
        // MUST this does not work on windows! we need to add an index
        var entry: ZipEntry?
        var zis: ZipInputStream? = null
        try {
            val src: CodeSource = CFMLEngineFactory::class.java.getProtectionDomain().getCodeSource() ?: return
            val loc: URL = src.getLocation()
            zis = ZipInputStream(loc.openStream())
            var path: String
            var name: String?
            var index: Int
            var temp: Resource?
            var rhe: RHExtension?
            var it: Iterator<ExtensionDefintion?>
            var exist: ExtensionDefintion?
            while (zis.getNextEntry().also { entry = it } != null) {
                path = entry.getName()
                if (path.startsWith(sub) && path.endsWith(".lex")) { // ignore non lex files or file from else where
                    index = path.lastIndexOf('/') + 1
                    if (index == sub.length()) { // ignore sub directories
                        name = path.substring(index)
                        temp = null
                        try {
                            temp = SystemUtil.getTempDirectory().getRealResource(name)
                            ResourceUtil.touch(temp)
                            Util.copy(zis, temp.getOutputStream(), false, true)
                            rhe = RHExtension(cs, temp, false)
                            rhe.validate()
                            var alreadyExists = false
                            it = existing.iterator()
                            while (it.hasNext()) {
                                exist = it.next()
                                if (exist.equals(rhe)) {
                                    alreadyExists = true
                                    break
                                }
                            }
                            if (!alreadyExists) {
                                temp.moveTo(dir.getRealResource(name))
                            }
                        } finally {
                            if (temp != null && temp.exists()) temp.delete()
                        }
                    }
                }
                zis.closeEntry()
            }
        } catch (e: Exception) {
            LogUtil.log(cs, "deploy-bundle-extension", e)
        } finally {
            Util.closeEL(zis)
        }
        return
    }

    fun touchMonitor(cs: ConfigServerImpl?) {
        if (monitor != null && monitor.isAlive()) return
        monitor = Monitor(cs, controlerState)
        monitor.setDaemon(true)
        monitor.setPriority(Thread.MIN_PRIORITY)
        monitor.start()
    }

    @Override
    @Throws(ServletException::class)
    fun addServletConfig(config: ServletConfig?) {
        if (PageSourceImpl.logAccessDirectory == null) {
            val str: String = config.getInitParameter("tachyon-log-access-directory")
            if (!StringUtil.isEmpty(str)) {
                val file = File(str.trim())
                file.mkdirs()
                if (file.isDirectory()) {
                    PageSourceImpl.logAccessDirectory = file
                }
            }
        }

        // FUTURE remove and add a new method for it (search:FUTURE add exeServletContextEvent)
        if ("TachyonServletContextListener".equals(config.getServletName())) {
            try {
                val status: String = config.getInitParameter("status")
                if ("release".equalsIgnoreCase(status)) reset()
            } catch (e: Exception) {
                LogUtil.log(configServer, "startup", e)
            }
            return
        }

        // add EventListener
        if (scl == null) {
            addEventListener(config.getServletContext())
        }
        servletConfigs.add(config)
        val real: String = ReqRspUtil.getRootPath(config.getServletContext())
        if (!initContextes!!.containsKey(real)) {
            val jspFactory: CFMLFactory? = loadJSPFactory(getConfigServerImpl(), config, initContextes.size())
            initContextes.put(real, jspFactory)
        }
    }

    private fun filter(req: ServletRequest?, rsp: ServletResponse?, fc: FilterChain?) {
        // TODO get filter defined in Config
    }

    @Throws(PageException::class)
    private fun _get(obj: Object?, msg: String?): Object? {
        return try {
            val m: Method = obj.getClass().getMethod(msg, arrayOfNulls<Class?>(0))
            m.invoke(obj, arrayOfNulls<Object?>(0))
        } catch (e: Exception) {
            throw Caster.toPageException(e)
        }
    }

    private fun addEventListener(sc: ServletContext?) {
        // TOMCAT
        if ("org.apache.catalina.core.ApplicationContextFacade".equals(sc.getClass().getName())) {
            var obj: Object? = extractServletContext(sc)
            obj = extractServletContext(obj)
            if ("org.apache.catalina.core.StandardContext".equals(obj.getClass().getName())) {
                var m: Method? = null
                try {
                    // TODO check if we already have a listener (tachyon.loader.servlet.TachyonServletContextListener), if
                    // so we do nothing
                    // sc.getApplicationLifecycleListeners();
                    m = obj.getClass().getMethod("addApplicationLifecycleListener", arrayOf<Class?>(Object::class.java))
                    var tmp: CFMLServletContextListener?
                    m.invoke(obj, arrayOf<Object?>(CFMLServletContextListener(this).also { tmp = it }))
                    scl = tmp
                    return
                } catch (e: Exception) {
                    // because this is optional and not all servlet engine do support this, we keep the log level on
                    // info
                    LogUtil.log(configServer, "application", "add-event-listener", e, Log.LEVEL_INFO)
                }
            }
        }

        // GENERAL try add Event method directly (does not work with tomcat)
        if (!ServletContextImpl::class.java.getName().equals(sc.getClass().getName())) { // ServletContextImpl does not support addListener
            try {
                val tmp = CFMLServletContextListener(this)
                sc.addListener(tmp)
                scl = tmp
                return
            } catch (e: Exception) {
                // because this is optional and not all servlet engine do support this, we keep the log level on
                // info
                LogUtil.log(configServer, "application", "add-event-listener", e, Log.LEVEL_INFO)
            }
        }
        LogUtil.log(configServer, Log.LEVEL_INFO, "startup", "Tachyon was not able to register an event listener with " + if (sc == null) "null" else sc.getClass().getName())
    }

    private fun extractServletContext(sc: Object?): Object? {
        val clazz: Class<*> = sc.getClass()
        var f: Field? = null
        try {
            f = clazz.getDeclaredField("context")
        } catch (e: Exception) {
            LogUtil.log(configServer, "extract-servlet-context", e)
        }
        if (f != null) {
            f.setAccessible(true)
            var obj: Object? = null
            try {
                obj = f.get(sc)
            } catch (e: Exception) {
                LogUtil.log(configServer, "extract-servlet-context", e)
            }
            return obj
        }
        return null
    }

    @Override
    @Throws(PageException::class)
    fun getConfigServer(password: Password?): ConfigServer? {
        getConfigServerImpl().checkAccess(password)
        return configServer
    }

    @Override
    @Throws(PageException::class)
    fun getConfigServer(key: String?, timeNonce: Long): ConfigServer? {
        getConfigServerImpl().checkAccess(key, timeNonce)
        return configServer
    }

    fun setConfigServerImpl(cs: ConfigServerImpl?) {
        configServer = cs
    }

    private fun getConfigServerImpl(): ConfigServerImpl? {
        return getConfigServerImpl(null, false)
    }

    private fun getConfigServerImpl(existing: ConfigServerImpl?, essentialOnly: Boolean): ConfigServerImpl? {
        if (configServer == null) {
            try {
                val context: Resource? = getSeverContextConfigDirectory(factory)
                val tmp: ConfigServerImpl = ConfigServerFactory.newInstance(this, initContextes, contextes, context, existing, essentialOnly)
                if (essentialOnly) {
                    return tmp
                }
                configServer = tmp
            } catch (e: Exception) {
                e.printStackTrace()
                LogUtil.log(configServer, "startup", e)
            }
        }
        return configServer
    }

    @Throws(ServletException::class)
    private fun loadJSPFactory(configServer: ConfigServerImpl?, sg: ServletConfig?, countExistingContextes: Int): CFMLFactoryImpl? {
        return try {
            val factory = CFMLFactoryImpl(this, sg)
            if (ConfigWebFactory.LOG) LogUtil.log(configServer, Log.LEVEL_INFO, "startup", "Init factory")
            val multi = configServer.getAdminMode() === ConfigImpl.ADMINMODE_MULTI
            val config: ConfigWebPro
            config = if (multi) {
                val isCustomSetting: RefBoolean = RefBooleanImpl()
                val configDir: Resource? = getConfigDirectory(sg, configServer, countExistingContextes, isCustomSetting)
                ConfigWebFactory.newInstanceMulti(this, factory, configServer, configDir, isCustomSetting.toBooleanValue(), sg)
            } else {
                ConfigWebFactory.newInstanceSingle(this, factory, configServer, sg)
            }
            if (ConfigWebFactory.LOG) LogUtil.log(configServer, Log.LEVEL_INFO, "startup", "Loaded config")
            factory.setConfig(config)
            factory
        } catch (e: Exception) {
            val se = ServletException(e.getMessage())
            se.setStackTrace(e.getStackTrace())
            throw se
        }
    }

    /**
     * loads Configuration File from System, from init Parameter from web.xml
     *
     * @param sg
     * @param configServer
     * @param countExistingContextes
     * @return return path to directory
     */
    @Throws(PageServletException::class)
    private fun getConfigDirectory(sg: ServletConfig?, configServer: ConfigServerImpl?, countExistingContextes: Int, isCustomSetting: RefBoolean?): Resource? {
        isCustomSetting.setValue(true)
        val sc: ServletContext = sg.getServletContext()
        var strConfig: String = sg.getInitParameter("configuration")
        if (StringUtil.isEmpty(strConfig)) strConfig = sg.getInitParameter("tachyon-web-directory")
        if (StringUtil.isEmpty(strConfig)) strConfig = System.getProperty("tachyon.web.dir")
        if (StringUtil.isEmpty(strConfig)) {
            isCustomSetting.setValue(false)
            strConfig = "{web-root-directory}/WEB-INF/tachyon/"
        } else if (strConfig.startsWith("/WEB-INF/tachyon/")) strConfig = "{web-root-directory}$strConfig"
        strConfig = StringUtil.removeQuotes(strConfig, true)

        // static path is not allowed
        if (countExistingContextes > 1 && strConfig != null && strConfig.indexOf('{') === -1) {
            val text = "Static path [$strConfig] for servlet init param [tachyon-web-directory] is not allowed, path must use a web-context specific placeholder."
            LogUtil.log(configServer, Log.LEVEL_ERROR, CFMLEngineImpl::class.java.getName(), text)
            throw PageServletException(ApplicationException(text))
        }
        strConfig = SystemUtil.parsePlaceHolder(strConfig, sc, configServer.getLabels())
        val frp: ResourceProvider = ResourcesImpl.getFileResourceProvider()
        val root: Resource = frp.getResource(ReqRspUtil.getRootPath(sc))
        var res: Resource?
        var configDir: Resource = ResourceUtil.createResource(root.getRealResource(strConfig).also { res = it }, FileUtil.LEVEL_PARENT_FILE, FileUtil.TYPE_DIR)
        if (configDir == null) {
            configDir = ResourceUtil.createResource(frp.getResource(strConfig).also { res = it }, FileUtil.LEVEL_GRAND_PARENT_FILE, FileUtil.TYPE_DIR)
        }
        if (configDir == null && !isCustomSetting.toBooleanValue()) {
            configDir = try {
                res.createDirectory(true)
                res
            } catch (e: IOException) {
                throw PageServletException(Caster.toPageException(e))
            }
        }
        if (configDir == null) {
            throw PageServletException(ApplicationException("path [$strConfig] is invalid"))
        }
        if (!configDir.exists() || ResourceUtil.isEmptyDirectory(configDir, null)) {
            var railoRoot: Resource?
            // there is a railo directory
            if (configDir.getName().equals("tachyon") && configDir.getParentResource().getRealResource("railo").also { railoRoot = it }.isDirectory()) {
                try {
                    copyRecursiveAndRename(railoRoot, configDir)
                } catch (e: IOException) {
                    try {
                        if (!configDir.isDirectory()) configDir.createDirectory(true)
                    } catch (ioe: IOException) {
                        LogUtil.log(configServer, "config-directory", ioe)
                    }
                    return configDir
                }
                // zip the railo-server di and delete it (optional)
                try {
                    val p: Resource = railoRoot.getParentResource()
                    CompressUtil.compress(CompressUtil.FORMAT_ZIP, railoRoot, p.getRealResource("railo-web-context-old.zip"), false, -1)
                    ResourceUtil.removeEL(railoRoot, true)
                } catch (e: Exception) {
                    LogUtil.log(configServer, "controller", e)
                }
            } else {
                try {
                    configDir.createDirectory(true)
                } catch (e: IOException) {
                    LogUtil.log(configServer, "controller", e)
                }
            }
        }
        return configDir
    }

    private fun getDirectoryByProp(name: String?): File? {
        val value: String = System.getProperty(name)
        if (Util.isEmpty(value, true)) return null
        val dir = File(value)
        dir.mkdirs()
        return if (dir.isDirectory()) dir else null
    }

    @Override
    @Throws(ServletException::class)
    fun getCFMLFactory(srvConfig: ServletConfig?, req: HttpServletRequest?): CFMLFactory? {
        return getCFMLFactory(null, srvConfig, req)
    }

    @Throws(ServletException::class)
    fun getCFMLFactory(cs: ConfigServerImpl?, srvConfig: ServletConfig?, req: HttpServletRequest?): CFMLFactory? {
        var cs: ConfigServerImpl? = cs
        val srvContext: ServletContext = srvConfig.getServletContext()
        val real: String = ReqRspUtil.getRootPath(srvContext)
        if (cs == null) cs = getConfigServerImpl()

        // Load JspFactory
        var factory: CFMLFactory? = contextes!![real]
        if (factory == null) {
            factory = initContextes!![real]
            if (factory == null) {
                factory = loadJSPFactory(cs, srvConfig, initContextes.size())
                initContextes.put(real, factory)
            }
            contextes.put(real, factory)
            try {
                var cp: String = req.getContextPath()
                if (cp == null) cp = ""
                (factory as CFMLFactoryImpl?).setURL(URL(req.getScheme(), req.getServerName(), req.getServerPort(), cp))
            } catch (e: MalformedURLException) {
                LogUtil.log(cs, "startup", e)
            }
        }
        return factory
    }

    @Override
    @Throws(ServletException::class, IOException::class)
    fun service(servlet: HttpServlet?, req: HttpServletRequest?, rsp: HttpServletResponse?) {
        _service(servlet, req, rsp, Request.TYPE_LUCEE)
    }

    @Override
    @Throws(ServletException::class, IOException::class)
    fun serviceCFML(servlet: HttpServlet?, req: HttpServletRequest?, rsp: HttpServletResponse?) {
        _service(servlet, req, rsp, Request.TYPE_CFML)
    }

    @Override
    @Throws(ServletException::class, IOException::class)
    fun serviceRest(servlet: HttpServlet?, req: HttpServletRequest?, rsp: HttpServletResponse?) {
        _service(servlet, HTTPServletRequestWrap(req), rsp, Request.TYPE_REST)
    }

    @Throws(ServletException::class, IOException::class)
    private fun _service(servlet: HttpServlet?, req: HttpServletRequest?, rsp: HttpServletResponse?, type: Short) {
        val factory: CFMLFactoryImpl? = getCFMLFactory(servlet.getServletConfig(), req) as CFMLFactoryImpl?
        // is Tachyon dialect enabled?
        if (type == Request.TYPE_LUCEE) {
            if (!(factory.getConfig() as ConfigPro).allowTachyonDialect()) {
                try {
                    PageContextImpl.notSupported()
                } catch (e: ApplicationException) {
                    throw PageServletException(e)
                }
            }
        }
        val exeReqAsync = exeRequestAsync()
        val pc: PageContextImpl = factory.getPageContextImpl(servlet, req, rsp, null, false, -1, false, !exeReqAsync, false, -1, true, false, false, null)
        try {
            val r = Request(pc, type)
            if (exeReqAsync) {
                r.start()
                var ended: Long = -1
                do {
                    SystemUtil.wait(Thread.currentThread(), 1000)
                    // done?
                    if (r!!.isDone()) {
                        // print.e("mas-done:"+System.currentTimeMillis());
                        break
                    } else if (ended == -1L && pc.getStartTime() + pc.getRequestTimeout() < System.currentTimeMillis()) {
                        // print.e("req-time:"+System.currentTimeMillis());
                        CFMLFactoryImpl.terminate(pc, false)
                        ended = System.currentTimeMillis()
                        // break; we do not break here, we give the thread itself the chance to end we need the exception
                        // output
                    } else if (ended > -1 && ended + 10000 <= System.currentTimeMillis()) {
                        // print.e("give-up:"+System.currentTimeMillis());
                        break
                    }
                } while (true)
            } else {
                try {
                    Request.exe(pc, type, true, false)
                } catch (rte: RequestTimeoutException) {
                    if (rte.getThreadDeath() != null) throw rte.getThreadDeath()
                } catch (ne: NativeException) {
                    if (ne.getCause() is ThreadDeath) throw ne.getCause() as ThreadDeath
                } catch (td: ThreadDeath) {
                    throw td
                } catch (t: Throwable) {
                    if (t is Exception && !Abort.isSilentAbort(t)) LogUtil.log(configServer, "application", "controller", t, if (t is MissingIncludeException) Log.LEVEL_WARN else Log.LEVEL_ERROR)
                }
            }
        } finally {
            factory.releaseTachyonPageContext(pc, !exeReqAsync)
        }
    }

    @Override
    @Throws(ServletException::class, IOException::class)
    fun serviceFile(servlet: HttpServlet?, req: HttpServletRequest?, rsp: HttpServletResponse?) {
        var req: HttpServletRequest? = req
        req = HTTPServletRequestWrap(req)
        val factory: CFMLFactory? = getCFMLFactory(servlet.getServletConfig(), req)
        val config: ConfigWeb = factory.getConfig()
        val ps: PageSource = config.getPageSourceExisting(null, null, req.getServletPath(), false, true, true, false)
        if (ps == null) {
            rsp.sendError(404)
        } else {
            val res: Resource = ps.getResource()
            if (res == null) {
                rsp.sendError(404)
            } else {
                ReqRspUtil.setContentLength(rsp, res.length())
                val mt: String = servlet.getServletContext().getMimeType(req.getServletPath())
                if (!StringUtil.isEmpty(mt)) ReqRspUtil.setContentType(rsp, mt)
                IOUtil.copy(res, rsp.getOutputStream(), true)
            }
        }
    }

    /*
	 * private String getContextList() { return
	 * List.arrayToList((String[])contextes.keySet().toArray(new String[contextes.size()]),", "); }
	 */
    @Override
    fun getVersion(): String? {
        return info!!.getVersion().toString()
    }

    @Override
    fun getInfo(): Info? {
        return info
    }

    @Override
    fun getUpdateType(): String? {
        return getConfigServerImpl().getUpdateType()
    }

    @Override
    fun getUpdateLocation(): URL? {
        return getConfigServerImpl().getUpdateLocation()
    }

    @Override
    fun getIdentification(): Identification? {
        return getConfigServerImpl().getIdentification()
    }

    @Override
    fun can(type: Int, password: Password?): Boolean {
        return getConfigServerImpl().passwordEqual(password)
    }

    @Override
    fun getCFMLEngineFactory(): CFMLEngineFactory? {
        return factory
    }

    @Override
    @Throws(ServletException::class, IOException::class)
    fun serviceAMF(servlet: HttpServlet?, req: HttpServletRequest?, rsp: HttpServletResponse?) {
        throw ServletException("AMFServlet is no longer supported, use BrokerServlet instead.")
        // req=new HTTPServletRequestWrap(req);
        // getCFMLFactory(servlet.getServletConfig(), req).getConfig().getAMFEngine().service(servlet,new
        // HTTPServletRequestWrap(req),rsp);
    }

    @Override
    fun reset() {
        reset(null)
    }

    @Override
    fun reset(configId: String?) {
        if (!controlerState!!.active()) return
        try {
            LogUtil.log(configServer, Log.LEVEL_INFO, "startup", "Reset CFML Engine")
            RetireOutputStreamFactory.close()
            val cntr: Controler? = getControler()
            if (cntr != null) cntr.close()

            // release HTTP Pool
            HTTPEngine4Impl.releaseConnectionManager()
            releaseCache(getConfigServerImpl())
            var cfmlFactory: CFMLFactoryImpl
            // ScopeContext scopeContext;
            val it: Iterator<Entry<String?, CFMLFactory?>?> = initContextes.entrySet().iterator()
            var e: Entry<String?, CFMLFactory?>?
            var config: ConfigWeb
            while (it.hasNext()) {
                e = it.next()
                try {
                    cfmlFactory = e.getValue() as CFMLFactoryImpl
                    config = cfmlFactory.getConfig()
                    if (configId != null && !configId.equals(config.getIdentification().getId())) continue

                    // scheduled tasks
                    (config.getScheduler() as SchedulerImpl).stop()

                    // scopes
                    try {
                        cfmlFactory.getScopeContext().clear()
                    } catch (ee: Exception) {
                        LogUtil.log(configServer, "controller", ee)
                    }

                    // PageContext
                    try {
                        cfmlFactory.resetPageContext()
                    } catch (ee: Exception) {
                        LogUtil.log(configServer, "controller", ee)
                    }

                    // Query Cache
                    try {
                        val pc: PageContext = ThreadLocalPageContext.get()
                        if (pc != null) {
                            pc.getConfig().getCacheHandlerCollection(Config.CACHE_TYPE_QUERY, null).clear(pc)
                            pc.getConfig().getCacheHandlerCollection(Config.CACHE_TYPE_FUNCTION, null).clear(pc)
                            pc.getConfig().getCacheHandlerCollection(Config.CACHE_TYPE_INCLUDE, null).clear(pc)
                        }
                        // cfmlFactory.getDefaultQueryCache().clear(null);
                    } catch (ee: Exception) {
                        LogUtil.log(configServer, "controller", ee)
                    }

                    // Gateway
                    try {
                        ((cfmlFactory.getConfig() as ConfigWebPro).getGatewayEngine() as GatewayEngineImpl).reset(false)
                    } catch (ee: Exception) {
                        LogUtil.log(configServer, "controller", ee)
                    }

                    // Cache
                    releaseCache(cfmlFactory.getConfig())
                } catch (ex: Exception) {
                    LogUtil.log(configServer, "controller", ex)
                }
            }

            // release felix itself
            shutdownFelix()
        } catch (ee: Exception) {
            LogUtil.logGlobal(configServer, "reset-engine", ee)
        } finally {
            // Controller
            controlerState!!.setActive(false)
        }
    }

    /*
	 * private void dump() { Iterator<Entry<Thread, StackTraceElement[]>> it =
	 * Thread.getAllStackTraces().entrySet().iterator(); while (it.hasNext()) { Entry<Thread,
	 * StackTraceElement[]> e = it.next(); print.e(e.getKey().getContextClassLoader());
	 * print.e(e.getValue()); }
	 * 
	 * }
	 */
    private fun shutdownFelix() {
        val f: CFMLEngineFactory? = getCFMLEngineFactory()
        try {
            val m: Method = f.getClass().getMethod("shutdownFelix", arrayOfNulls<Class?>(0))
            m.invoke(f, arrayOfNulls<Object?>(0))
        } // FUTURE do not use reflection
        // this will for sure fail if CFMLEngineFactory does not have this method
        catch (e: Exception) {
            LogUtil.log(configServer, "controller", e)
        }
    }

    @Override
    fun getCastUtil(): Cast? {
        return CastImpl.getInstance()
    }

    @Override
    fun getOperatonUtil(): Operation? {
        return OperationImpl.getInstance()
    }

    @Override
    fun getDecisionUtil(): Decision? {
        return DecisionImpl.getInstance()
    }

    @Override
    fun getExceptionUtil(): Excepton? {
        return ExceptonImpl.getInstance()
    }

    @Override
    fun getJavaProxyUtil(): Object? { // FUTURE return JavaProxyUtil
        return JavaProxyUtilImpl()
    }

    @Override
    fun getCreationUtil(): Creation? {
        return CreationImpl.getInstance(this)
    }

    @Override
    fun getIOUtil(): IO? {
        return IOImpl.getInstance()
    }

    @Override
    fun getStringUtil(): Strings? {
        return StringsImpl.getInstance()
    }

    @Override
    fun getFDController(): Object? {
        engine!!.allowRequestTimeout(false)
        return FDControllerImpl(engine, engine!!.getConfigServerImpl().getSerialNumber())
    }

    fun getCFMLFactories(): Map<String?, CFMLFactory?>? {
        return initContextes
    }

    @Override
    fun getResourceUtil(): tachyon.runtime.util.ResourceUtil? {
        return ResourceUtilImpl.getInstance()
    }

    @Override
    fun getHTTPUtil(): tachyon.runtime.util.HTTPUtil? {
        return HTTPUtilImpl.getInstance()
    }

    @Override
    fun getThreadPageContext(): PageContext? {
        return ThreadLocalPageContext.get()
    }

    @Override
    fun getThreadConfig(): Config? {
        return ThreadLocalPageContext.getConfig()
    }

    @Override
    fun registerThreadPageContext(pc: PageContext?) {
        ThreadLocalPageContext.register(pc)
    }

    @Override
    fun getVideoUtil(): VideoUtil? {
        return VideoUtilImpl.getInstance()
    }

    @Override
    fun getZipUtil(): ZipUtil? {
        return ZipUtilImpl.getInstance()
    }

    /*
	 * public String getState() { return info.getStateAsString(); }
	 */
    fun allowRequestTimeout(allowRequestTimeout: Boolean) {
        this.allowRequestTimeout = allowRequestTimeout
    }

    fun allowRequestTimeout(): Boolean {
        return allowRequestTimeout
    }

    fun isRunning(): Boolean {
        try {
            val other: CFMLEngine = CFMLEngineFactory.getInstance()
            // FUTURE patch, do better impl when changing loader
            if (other !== this && controlerState!!.active() && other !is CFMLEngineWrapper) {
                LogUtil.log(configServer, Log.LEVEL_INFO, "startup", "CFMLEngine is still set to true but no longer valid, " + tachyon.runtime.config.Constants.NAME.toString() + " disable this CFMLEngine.")
                controlerState!!.setActive(false)
                reset()
                return false
            }
        } catch (e: Exception) {
            LogUtil.log(configServer, "controller", e)
        }
        return controlerState!!.active()
    }

    fun active(): Boolean {
        return controlerState!!.active()
    }

    fun getControllerState(): ControllerState? {
        return controlerState
    }

    @Override
    @Throws(IOException::class, JspException::class, ServletException::class)
    fun cli(config: Map<String?, String?>?, servletConfig: ServletConfig?) {
        val servletContext: ServletContext = servletConfig.getServletContext()
        val servlet = HTTPServletImpl(servletConfig, servletContext, servletConfig.getServletName())

        // webroot
        val strWebroot = config!!["webroot"]
        if (StringUtil.isEmpty(strWebroot, true)) throw IOException("Missing webroot configuration")
        val root: Resource = ResourcesImpl.getFileResourceProvider().getResource(strWebroot)
        root.mkdirs()

        // serverName
        var serverName = config["server-name"]
        if (StringUtil.isEmpty(serverName, true)) serverName = "localhost"

        // uri
        val strUri = config["uri"]
        if (StringUtil.isEmpty(strUri, true)) throw IOException("Missing uri configuration")
        val uri: URI
        uri = try {
            tachyon.commons.net.HTTPUtil.toURI(strUri)
        } catch (e: URISyntaxException) {
            throw Caster.toPageException(e)
        }

        // cookie
        val cookies: Array<Cookie?>?
        val strCookie = config["cookie"]
        if (StringUtil.isEmpty(strCookie, true)) cookies = arrayOfNulls<Cookie?>(0) else {
            val mapCookies: Map<String?, String?> = HTTPUtil.parseParameterList(strCookie, false, null)
            var index = 0
            cookies = arrayOfNulls<Cookie?>(mapCookies.size())
            var entry: Entry<String?, String?>
            val it: Iterator<Entry<String?, String?>?> = mapCookies.entrySet().iterator()
            var c: Cookie
            while (it.hasNext()) {
                entry = it.next()
                c = ReqRspUtil.toCookie(entry.getKey(), entry.getValue(), null)
                if (c != null) cookies!![index++] = c else throw IOException("Cookie name [" + entry.getKey().toString() + "] is invalid")
            }
        }

        // header
        val headers: Array<Pair?> = arrayOfNulls<Pair?>(0)

        // parameters
        val parameters: Array<Pair?> = arrayOfNulls<Pair?>(0)

        // attributes
        val attributes = StructImpl()
        val os = ByteArrayOutputStream()
        val req = HttpServletRequestDummy(root, serverName, uri.getPath(), uri.getQuery(), cookies, headers, parameters, attributes, null, null)
        req.setProtocol("CLI/1.0")
        val rsp: HttpServletResponse = HttpServletResponseDummy(os)
        serviceCFML(servlet, req, rsp)
        val res: String = os.toString(ReqRspUtil.getCharacterEncoding(null, rsp).name())
        // System. out.println(res);
    }

    @Override
    fun getServletConfigs(): Array<ServletConfig?>? {
        return servletConfigs.toArray(arrayOfNulls<ServletConfig?>(servletConfigs!!.size()))
    }

    @Override
    fun uptime(): Long {
        return uptime
    }

    /*
	 * public Bundle getCoreBundle() { return bundle; }
	 */
    @Override
    fun getBundleCollection(): BundleCollection? {
        return bundleCollection
    }

    @Override
    fun getBundleContext(): BundleContext? {
        return bundleCollection.getBundleContext()
    }

    @Override
    fun getClassUtil(): ClassUtil? {
        return ClassUtilImpl()
    }

    @Override
    fun getListUtil(): ListUtil? {
        return ListUtilImpl()
    }

    @Override
    fun getDBUtil(): DBUtil? {
        return DBUtilImpl()
    }

    @Override
    fun getORMUtil(): ORMUtil? {
        return ORMUtilImpl()
    }

    @Override
    fun getTemplateUtil(): TemplateUtil? {
        return TemplateUtilImpl()
    }

    @Override
    fun getHTMLUtil(): HTMLUtil? {
        return HTMLUtilImpl()
    }

    @Override
    fun getScriptEngineFactory(dialect: Int): ScriptEngineFactory? {
        if (dialect == CFMLEngine.DIALECT_CFML) {
            if (cfmlScriptEngine == null) cfmlScriptEngine = ScriptEngineFactoryImpl(this, false, dialect)
            return cfmlScriptEngine
        }
        if (tachyonScriptEngine == null) tachyonScriptEngine = ScriptEngineFactoryImpl(this, false, dialect)
        return tachyonScriptEngine
    }

    @Override
    fun getTagEngineFactory(dialect: Int): ScriptEngineFactory? {
        if (dialect == CFMLEngine.DIALECT_CFML) {
            if (cfmlTagEngine == null) cfmlTagEngine = ScriptEngineFactoryImpl(this, true, dialect)
            return cfmlTagEngine
        }
        if (tachyonTagEngine == null) tachyonTagEngine = ScriptEngineFactoryImpl(this, true, dialect)
        return tachyonTagEngine
    }

    @Override
    @Throws(ServletException::class)
    fun createPageContext(contextRoot: File?, host: String?, scriptName: String?, queryString: String?, cookies: Array<Cookie?>?, headers: Map<String?, Object?>?,
                          parameters: Map<String?, String?>?, attributes: Map<String?, Object?>?, os: OutputStream?, timeout: Long, register: Boolean): PageContext? {
        // FUTURE add first 2 arguments to interface
        return PageContextUtil.getPageContext(null, null, contextRoot, host, scriptName, queryString, cookies, headers, parameters, attributes, os, register, timeout, false)
    }

    @Override
    @Throws(ServletException::class)
    fun createConfig(contextRoot: File?, host: String?, scriptName: String?): ConfigWeb? {
        // TODO do a mored rect approach
        var pc: PageContext? = null
        return try {
            // FUTURE add first 2 arguments to interface
            pc = PageContextUtil.getPageContext(null, null, contextRoot, host, scriptName, null, null, null, null, null, null, false, -1, false)
            pc.getConfig()
        } finally {
            pc.getConfig().getFactory().releaseTachyonPageContext(pc, false)
        }
    }

    @Override
    fun releasePageContext(pc: PageContext?, unregister: Boolean) {
        PageContextUtil.releasePageContext(pc, unregister)
    }

    @Override
    fun getSystemUtil(): tachyon.runtime.util.SystemUtil? {
        return SystemUtilImpl()
    }

    @Override
    fun getThreadTimeZone(): TimeZone? {
        return ThreadLocalPageContext.getTimeZone()
    }

    @Override
    fun getInstrumentation(): Instrumentation? {
        return InstrumentationFactory.getInstrumentation(ThreadLocalPageContext.getConfig())
    }

    fun getControler(): Controler? {
        return controler
    }

    fun onStart(config: ConfigPro?, reload: Boolean) {
        val isWeb = config is ConfigWeb
        val context = if (isWeb) "Web" else "Server"
        if ((isWeb || config.getAdminMode() === ConfigImpl.ADMINMODE_SINGLE)
                && SystemUtil.getSystemPropOrEnvVar("tachyon.enable.warmup", "").equalsIgnoreCase("true")) {
            val msg = "Tachyon warmup completed. Shutting down."
            CONSOLE_ERR.println(msg)
            LogUtil.log(config, Log.LEVEL_ERROR, "application", msg)
            shutdownFelix()
            System.exit(0)
        }
        if (!ThreadLocalPageContext.callOnStart.get()) return
        val listenerTemplateCFML: Resource = config.getConfigDir().getRealResource("context/" + context + "." + tachyon.runtime.config.Constants.getCFMLComponentExtension())
        val listenerTemplateTachyon: Resource = config.getConfigDir().getRealResource("context/" + context + "." + tachyon.runtime.config.Constants.getTachyonComponentExtension())
        var listenerTemplateCFMLWebRoot: Resource? = null
        var listenerTemplateTachyonWebRoot: Resource? = null
        if (isWeb) {
            try {
                val rootdir: Resource = config.getRootDirectory()
                listenerTemplateCFMLWebRoot = rootdir.getRealResource(context + "." + tachyon.runtime.config.Constants.getCFMLComponentExtension())
                listenerTemplateTachyonWebRoot = rootdir.getRealResource(context + "." + tachyon.runtime.config.Constants.getTachyonComponentExtension())
            } catch (e: Exception) {
            }
        }

        // dialect
        val dialect: Int
        val inWebRoot: Boolean
        if (listenerTemplateCFMLWebRoot != null && listenerTemplateCFMLWebRoot.isFile()) {
            inWebRoot = true
            dialect = CFMLEngine.DIALECT_CFML
        } else if (listenerTemplateTachyonWebRoot != null && listenerTemplateTachyonWebRoot.isFile()) {
            inWebRoot = true
            dialect = CFMLEngine.DIALECT_LUCEE
        } else if (listenerTemplateCFML.isFile()) {
            inWebRoot = false
            dialect = CFMLEngine.DIALECT_CFML
        } else if (listenerTemplateTachyon.isFile()) {
            inWebRoot = false
            dialect = CFMLEngine.DIALECT_LUCEE
        } else return
        if (!StringUtil.emptyIfNull(Thread.currentThread().getName()).startsWith("on-start-")) {
            var timeout: Long = config.getRequestTimeout().getMillis()
            if (timeout <= 0) timeout = 50000L
            val thread: OnStart = OnStart(config, dialect, context, reload, inWebRoot)
            thread.setName("on-start-" + CreateUniqueId.invoke())
            val start: Long = System.currentTimeMillis()
            thread.start()
            try {
                thread.join(timeout)
            } catch (e: Exception) {
                LogUtil.log(config, "on-start", e)
            }
            if (thread.isAlive()) {
                LogUtil.log(config, Log.LEVEL_ERROR, "on-start", "killing on-start")
                SystemUtil.stop(thread)
            }
            LogUtil.log(config, Log.LEVEL_INFO, "on-start", "on-start executed in " + (System.currentTimeMillis() - start).toString() + "ms")
        }
    }

    /**
     * process Startup Listeners, i.e. Server.cfc and Web.cfc
     */
    private inner class OnStart(config: ConfigPro?, dialect: Int, context: String?, reload: Boolean, inWebRoot: Boolean) : Thread() {
        private val config: ConfigPro?
        private val dialect: Int
        private val reload: Boolean
        private val context: String?
        private val inWebRoot: Boolean
        @Override
        fun run() {
            val isWeb = config is ConfigWeb
            val id: String = CreateUniqueId.invoke()
            val requestURI = ((if (inWebRoot) "" else "/" + if (isWeb) "tachyon" else "tachyon-server") + "/" + context + "."
                    + if (dialect == CFMLEngine.DIALECT_LUCEE) tachyon.runtime.config.Constants.getTachyonComponentExtension() else tachyon.runtime.config.Constants.getCFMLComponentExtension())

            // PageContext oldPC = ThreadLocalPageContext.get();
            var pc: PageContext? = null
            try {
                val remotePersisId: String
                remotePersisId = try {
                    Md5.getDigestAsString(requestURI + id)
                } catch (e: IOException) {
                    throw Caster.toPageException(e)
                }
                val queryString = "method=on" + context + "Start&reload=" + reload + "&" + ComponentPageImpl.REMOTE_PERSISTENT_ID + "=" + remotePersisId
                pc = if (config is ConfigWeb) {
                    val headers: Array<Pair?> = arrayOf<Pair?>(Pair<String?, Object?>("AMF-Forward", "true"))
                    val attrs: Struct = StructImpl()
                    attrs.setEL(KeyConstants._client, "tachyon-listener-1-0")
                    ThreadUtil.createPageContext(config as ConfigWeb?, DevNullOutputStream.DEV_NULL_OUTPUT_STREAM, "localhost", requestURI, queryString, arrayOfNulls<Cookie?>(0), headers,
                            null, arrayOfNulls<Pair?>(0), attrs, true, Long.MAX_VALUE)
                } else {
                    val headers: Map<String?, Object?> = HashMap<String?, Object?>()
                    headers.put("AMF-Forward", "true")
                    val attrs: Map<String?, Object?> = HashMap<String?, Object?>()
                    attrs.put("client", "tachyon-listener-1-0")
                    val root = File(config.getRootDirectory().getAbsolutePath())
                    val cr: CreationImpl = CreationImpl.getInstance(engine) as CreationImpl
                    val sc: ServletConfig = cr.createServletConfig(root, null, null)
                    PageContextUtil.getPageContext(config, sc, root, "localhost", requestURI, queryString, arrayOfNulls<Cookie?>(0), headers, null, attrs,
                            DevNullOutputStream.DEV_NULL_OUTPUT_STREAM, true, Long.MAX_VALUE,
                            Caster.toBooleanValue(SystemUtil.getSystemPropOrEnvVar("tachyon.ignore.scopes", null), false))
                }
                (pc as PageContextImpl?).setListenerContext(true)
                if (dialect == CFMLEngine.DIALECT_LUCEE) pc.execute(requestURI, true, false) else pc.executeCFML(requestURI, true, false)
                (pc as PageContextImpl?).setListenerContext(false)
            } catch (e: Exception) {
                e.printStackTrace()
                // we simply ignore exceptions, if the template itself throws an error it will be handled by the
                // error listener
            } finally {
                val f: CFMLFactory = pc.getConfig().getFactory()
                f.releaseTachyonPageContext(pc, true)
                // ThreadLocalPageContext.register(oldPC);
            }
        }

        init {
            this.config = config
            this.dialect = dialect
            this.context = context
            this.reload = reload
            this.inWebRoot = inWebRoot
        }
    }

    /*
	 * execute request coming from the servlet engine in a separate thread or not
	 */
    fun exeRequestAsync(): Boolean {
        if (asyncReqHandle == null) asyncReqHandle = Caster.toBoolean(SystemUtil.getSystemPropOrEnvVar("tachyon.async.request.handle", null), Boolean.FALSE)
        return asyncReqHandle!!
    }

    fun getEnvExt(): Object? {
        return envExt
    }

    fun setEnvExt(envExt: String?) {
        this.envExt = envExt
    }

    // private static CFMLEngineImpl engine=new CFMLEngineImpl();
    init {
        this.factory = factory
        bundleCollection = bc
        allowRequestTimeout = Caster.toBooleanValue(SystemUtil.getSystemPropOrEnvVar("tachyon.requesttimeout", null), true)
        // log the startup process
        val logDir: String = SystemUtil.getSystemPropOrEnvVar("startlogdirectory", null) // "/Users/mic/Tmp/");
        if (logDir != null) {
            val f = File(logDir)
            if (f.isDirectory()) {
                val logName: String = SystemUtil.getSystemPropOrEnvVar("logName", "stacktrace")
                val timeRange: Int = Caster.toIntValue(SystemUtil.getSystemPropOrEnvVar("timeRange", "stacktrace"), 1)
                LogST._do(f, logName, timeRange)
            }
        }

        // happen when Tachyon is loaded directly
        if (bundleCollection == null) {
            try {
                val prop: Properties = InfoImpl.getDefaultProperties(null)

                // read the config from default.properties
                val config: Map<String?, Object?> = HashMap<String?, Object?>()
                val it: Iterator<Entry<Object?, Object?>?> = prop.entrySet().iterator()
                var e: Entry<Object?, Object?>?
                var k: String
                while (it.hasNext()) {
                    e = it.next()
                    k = e.getKey()
                    if (!k.startsWith("org.") && !k.startsWith("felix.")) continue
                    config.put(k, CFMLEngineFactorySupport.removeQuotes(e.getValue() as String, true))
                }
                config.put(Constants.FRAMEWORK_BOOTDELEGATION, "tachyon.*")
                val felix: Felix = factory.getFelix(factory.getResourceRoot(), config)
                bundleCollection = BundleCollection(felix, felix, null)
                // bundleContext=bundleCollection.getBundleContext();
            } catch (e: Exception) {
                throw Caster.toPageRuntimeException(e)
            }
        }
        info = InfoImpl(if (bundleCollection == null) null else bundleCollection.core)
        Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader()) // MUST better location for this
        val updateInfo: UpdateInfo
        var configDir: Resource? = null
        try {
            configDir = getSeverContextConfigDirectory(factory)
            updateInfo = ConfigFactory.getNew(this, configDir, true)
        } catch (e: Exception) {
            throw Caster.toPageRuntimeException(e)
        }
        CFMLEngineFactory.registerInstance(this) // patch, not really good but it works
        var cs: ConfigServerImpl? = getConfigServerImpl(null, true.also { quick = it })
        val isRe = if (configDir == null) false else ConfigFactory.isRequiredExtension(this, configDir, null)
        val installExtensions: Boolean = Caster.toBooleanValue(SystemUtil.getSystemPropOrEnvVar("tachyon.extensions.install", null), true)

        // copy bundled extension to local extension directory (if never done before)
        if (installExtensions && updateInfo.updateType !== ConfigFactory.NEW_NONE) {
            val count = deployBundledExtension(cs, false)
            LogUtil.log(Log.LEVEL_INFO, "deploy", "controller",
                    if (count == 0) "No new extension available to add to local extension directory" else "Copied [$count] bundled extension(s) to local extension directory")
        }
        // required extensions

        // if we have a "fresh" install
        var extensions: Set<ExtensionDefintion?>?
        var extensionsToRemove: Set<String?>? = null
        if (installExtensions && (updateInfo.updateType === ConfigFactory.NEW_FRESH || updateInfo.updateType === ConfigFactory.NEW_FROM4)) {
            val ext: List<ExtensionDefintion?> = info.getRequiredExtension()
            extensions = toSet(null, ext)
            LogUtil.log(Log.LEVEL_INFO, "deploy", "controller", "Found Extensions to install (new;" + updateInfo.getUpdateTypeAsString().toString() + "):" + toList(extensions))
        } else if (installExtensions && (updateInfo.updateType === ConfigFactory.NEW_MINOR || !isRe)) {
            extensions = HashSet<ExtensionDefintion?>()
            extensionsToRemove = HashSet<String?>()
            checkInvalidExtensions(this, cs, extensions, extensionsToRemove)
            val it: Iterator<ExtensionDefintion?> = info.getRequiredExtension()!!.iterator()
            var ed: ExtensionDefintion?
            var rhe: RHExtension
            var edVersion: Version
            var rheVersion: Version?
            while (it.hasNext()) {
                ed = it.next()
                edVersion = OSGiUtil.toVersion(ed.getVersion(), null)
                if (ed.getVersion() == null) {
                    continue  // no version definition no update
                }
                try {
                    rhe = ConfigAdmin.hasRHExtensions(cs, ExtensionDefintion(ed.getId()))
                    if (rhe == null) {
                        rheVersion = null
                        val since: Version = ed.getSince()
                        if (since == null || updateInfo.oldVersion == null || !Util.isNewerThan(since, updateInfo.oldVersion)) continue  // not installed we do not update
                        LogUtil.log(Log.LEVEL_INFO, "deploy", "controller", "Detected newer [" + since + ":" + updateInfo.oldVersion + "] Extension version [" + ed + "]")
                        extensions.add(ed)
                    } else rheVersion = OSGiUtil.toVersion(rhe.getVersion(), null)
                    // if the installed is older than the one defined in the manifest we update (if possible)
                    if (rheVersion != null && OSGiUtil.isNewerThan(edVersion, rheVersion)) { // TODO do none OSGi version number comparsion
                        LogUtil.log(Log.LEVEL_INFO, "deploy", "controller", "Detected newer [$edVersion:$rheVersion] Extension version [$ed]")
                        extensions.add(ed)
                    }
                } catch (e: Exception) {
                    LogUtil.log("deploy", "controller", e)
                    extensions.add(ed)
                }
            }
            if (!extensions!!.isEmpty()) {
                LogUtil.log(Log.LEVEL_INFO, "deploy", "controller", "Detected Extensions to install (minor;" + updateInfo.getUpdateTypeAsString().toString() + "):" + toList(extensions))
            }
        } else {
            LogUtil.log(Log.LEVEL_INFO, "deploy", "controller", "No extension(s) found to add/install")
            extensions = HashSet<ExtensionDefintion?>()
        }

        // install extension defined
        var extensionIds: String = StringUtil.unwrap(SystemUtil.getSystemPropOrEnvVar("tachyon-extensions", null)) // old no longer used
        if (StringUtil.isEmpty(extensionIds, true)) extensionIds = StringUtil.unwrap(SystemUtil.getSystemPropOrEnvVar("tachyon.extensions", null))
        envExt = null
        if (!StringUtil.isEmpty(extensionIds, true)) {
            envExt = extensionIds
            LogUtil.log(Log.LEVEL_INFO, "deploy", "controller", "Extensions to install defined in env variable or system property:$extensionIds")
            val _extensions: List<ExtensionDefintion?> = RHExtension.toExtensionDefinitions(extensionIds)
            extensions = toSet(extensions, _extensions)
        }
        if (extensions!!.size() > 0) {
            val sucess: Boolean
            sucess = try {
                DeployHandler.deployExtensions(cs, extensions.toArray(arrayOfNulls<ExtensionDefintion?>(extensions.size())), null, false, false)
            } catch (e: PageException) {
                LogUtil.log("deploy", "controller", e)
                false
            }
            if (sucess && configDir != null) ConfigFactory.updateRequiredExtension(this, configDir, null)
            LogUtil.log(Log.LEVEL_INFO, "deploy", "controller", (if (sucess) "Successfully" else "Unsuccessfully") + " installed extensions :" + toList(extensions))
        } else if (configDir != null) ConfigFactory.updateRequiredExtension(this, configDir, null)

        // extension to remove (we only have to remove in case we did not install an other version)
        if (extensionsToRemove != null) {
            for (ed in extensions) {
                extensionsToRemove.remove(ed.getId())
            }
            if (!extensionsToRemove.isEmpty()) {
                // remove extension that are not valid (to new for current version)
                LogUtil.log(Log.LEVEL_ERROR, "deploy", ConfigWebFactory::class.java.getName(), ("Uninstall extension(s) ["
                        + tachyon.runtime.type.util.ListUtil.toList(extensionsToRemove, ", ")) + "] because they are not supported for the current Tachyon version.")
                try {
                    ConfigAdmin.removeRHExtensions(null, null, tachyon.runtime.type.util.ListUtil.toStringArray(extensionsToRemove), false)
                    if (configDir != null) ConfigFactory.updateRequiredExtension(this, configDir, null)
                } catch (e: Exception) {
                    LogUtil.log("debug", ConfigWebFactory::class.java.getName(), e)
                }
            }
        }
        cs = getConfigServerImpl(cs, false.also { quick = it })
        var log: Log? = null
        if (cs != null) {
            try {
                log = cs.getLog("deploy", true)
            } catch (e: PageException) {
            }
        }
        touchMonitor(cs)
        LogUtil.log(cs, Log.LEVEL_INFO, "startup", "Touched monitors")
        uptime = System.currentTimeMillis()

        // check deploy folder
        try {
            DeployHandler.deploy(cs, log, false)
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
        }
        controler = Controler(cs, initContextes, 5 * 1000, controlerState)
        controler.setDaemon(true)
        controler.setPriority(Thread.MIN_PRIORITY)
        val disabled: Boolean = Caster.toBooleanValue(SystemUtil.getSystemPropOrEnvVar(SystemUtil.SETTING_CONTROLLER_DISABLED, null), false)
        if (!disabled) {
            // start the controller
            LogUtil.log(cs, Log.LEVEL_INFO, "startup", "Start CFML Controller")
            controler.start()
        }

        // remove old log4j bundles FUTURE remove
        try {
            OSGiUtil.removeLocalBundle("log4j", OSGiUtil.toVersion("1.2.16"), null, true, true)
            OSGiUtil.removeLocalBundle("log4j", OSGiUtil.toVersion("1.2.17"), null, true, true)
        } catch (e: Exception) {
            LogUtil.log(cs, "startup", e)
        }
    }
}