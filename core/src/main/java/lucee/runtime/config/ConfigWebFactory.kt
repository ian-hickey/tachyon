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
//import lucee.runtime.config.ajax.AjaxFactory;
import lucee.runtime.db.DatasourceManagerImpl.QOQ_DATASOURCE_NAME

/**
 *
 */
object ConfigWebFactory : ConfigFactory() {
    private val TEMPLATE_EXTENSION: String? = "cfm"
    private val COMPONENT_EXTENSION: String? = "cfc"
    private val COMPONENT_EXTENSION_LUCEE: String? = "lucee"
    private const val GB1 = (1024 * 1024 * 1024).toLong()
    const val LOG = true
    private const val DEFAULT_MAX_CONNECTION = 100
    val STRING_CACHE_TYPES: Array<String?>? = arrayOf("function", "include", "query", "resource", "http", "file", "webservice")
    val CACHE_TYPES: IntArray? = intArrayOf(Config.CACHEDWITHIN_FUNCTION, Config.CACHEDWITHIN_INCLUDE, Config.CACHEDWITHIN_QUERY, Config.CACHEDWITHIN_RESOURCE,
            Config.CACHEDWITHIN_HTTP, Config.CACHEDWITHIN_FILE, Config.CACHEDWITHIN_WEBSERVICE)

    // TODO can we merge with aove?
    val STRING_CACHE_TYPES_MAX: Array<String?>? = arrayOf("resource", "function", "include", "query", "template", "object", "file", "http", "webservice")
    val CACHE_TYPES_MAX: IntArray? = intArrayOf(ConfigPro.CACHE_TYPE_RESOURCE, ConfigPro.CACHE_TYPE_FUNCTION, ConfigPro.CACHE_TYPE_INCLUDE, ConfigPro.CACHE_TYPE_QUERY,
            ConfigPro.CACHE_TYPE_TEMPLATE, ConfigPro.CACHE_TYPE_OBJECT, ConfigPro.CACHE_TYPE_FILE, ConfigPro.CACHE_TYPE_HTTP, ConfigPro.CACHE_TYPE_WEBSERVICE)

    /**
     * creates a new ServletConfig Impl Object
     *
     * @param configServer
     * @param configDir
     * @param servletConfig
     * @return new Instance
     * @throws SAXException
     * @throws ClassNotFoundException
     * @throws PageException
     * @throws IOException
     * @throws TagLibException
     * @throws FunctionLibException
     * @throws NoSuchAlgorithmException
     * @throws BundleException
     * @throws ConverterException
     */
    @Throws(SAXException::class, ClassException::class, PageException::class, IOException::class, TagLibException::class, FunctionLibException::class, NoSuchAlgorithmException::class, BundleException::class, ConverterException::class)
    fun newInstanceMulti(engine: CFMLEngine?, factory: CFMLFactoryImpl?, configServer: ConfigServerImpl?, configDir: Resource?, isConfigDirACustomSetting: Boolean,
                         servletConfig: ServletConfig?): ConfigWebPro? {

        // boolean multi = configServer.getAdminMode() == ConfigImpl.ADMINMODE_MULTI;

        // make sure the web context does not point to the same directory as the server context
        if (configDir.equals(configServer!!.getConfigDir())) {
            throw ApplicationException(
                    "the web context [" + createLabel(configServer, servletConfig) + "] has defined the same configuration directory [" + configDir + "] as the server context")
        }
        val webs: Array<ConfigWeb?> = configServer!!.getConfigWebs()
        if (!ArrayUtil.isEmpty(webs)) {
            for (i in webs.indices) {
                // not sure this is necessary if(hash.equals(((ConfigWebImpl)webs[i]).getHash())) continue;
                if (configDir.equals(webs[i].getConfigDir())) throw ApplicationException("the web context [" + createLabel(configServer, servletConfig)
                        + "] has defined the same configuration directory [" + configDir + "] as the web context [" + webs[i].getLabel() + "]")
            }
        }
        LogUtil.logGlobal(configServer, Log.LEVEL_INFO, ConfigWebFactory::class.java.getName(),
                """
                    ===================================================================
                    WEB CONTEXT (${createLabel(configServer, servletConfig)})
                    -------------------------------------------------------------------
                    - config:$configDir${if (isConfigDirACustomSetting) " (custom setting)" else ""}
                    - webroot:${ReqRspUtil.getRootPath(servletConfig.getServletContext())}
                    - label:${createLabel(configServer, servletConfig)}
                    ===================================================================
                    
                    """.trimIndent()
        )
        val doNew = getNew(engine, configDir, false, UpdateInfo.NEW_NONE).updateType !== NEW_NONE
        val configWeb: ConfigWebPro?
        val configFileOld: Resource = configDir.getRealResource("lucee-web.xml." + TEMPLATE_EXTENSION)
        val configFileNew: Resource = configDir.getRealResource(".CFConfig.json")
        val strPath: String = servletConfig.getServletContext().getRealPath("/WEB-INF")
        val path: Resource = ResourcesImpl.getFileResourceProvider().getResource(strPath)
        var hasConfigOld = false
        var hasConfigNew = configFileNew.exists() && configFileNew.length() > 0
        if (!hasConfigNew) {
            hasConfigOld = configFileOld.exists() && configFileOld.length() > 0
        }
        configWeb = ConfigWebImpl(factory, configServer, servletConfig, configDir, configFileNew)

        // translate to new
        var root: Struct? = null
        if (!hasConfigNew) {
            if (hasConfigOld) {
                translateConfigFile(configWeb, configFileOld, configFileNew, "", false)
            } else {
                createConfigFile("web", configFileNew)
                hasConfigNew = true
            }
        }
        root = loadDocumentCreateIfFails(configFileNew, "web")

        // htaccess
        if (path.exists()) createHtAccess(path.getRealResource(".htaccess"))
        if (configDir.exists()) createHtAccess(configDir.getRealResource(".htaccess"))
        createContextFiles(configDir, servletConfig, doNew)
        load(configServer, configWeb as ConfigWebImpl?, root, false, doNew, false)
        createContextFilesPost(configDir, configWeb, servletConfig, false, doNew)

        // call web.cfc for this context
        (ConfigWebUtil.getEngine(configWeb) as CFMLEngineImpl).onStart(configWeb, false)
        (configWeb.getGatewayEngine() as GatewayEngineImpl).autoStart()
        return configWeb
    }

    @Throws(SAXException::class, ClassException::class, PageException::class, IOException::class, TagLibException::class, FunctionLibException::class, NoSuchAlgorithmException::class, BundleException::class, ConverterException::class)
    fun newInstanceSingle(engine: CFMLEngine?, factory: CFMLFactoryImpl?, configServer: ConfigServerImpl?, servletConfig: ServletConfig?): ConfigWebPro? {
        val configDir: Resource = configServer!!.getConfigDir()
        LogUtil.logGlobal(configServer, Log.LEVEL_INFO, ConfigWebFactory::class.java.getName(),
                """
                    ===================================================================
                    WEB CONTEXT (SINGLE) (${createLabel(configServer, servletConfig)})
                    -------------------------------------------------------------------
                    - config:$configDir
                    - webroot:${ReqRspUtil.getRootPath(servletConfig.getServletContext())}
                    - label:${createLabel(configServer, servletConfig)}
                    ===================================================================
                    
                    """.trimIndent()
        )
        val doNew = configServer!!.getUpdateInfo().updateType !== NEW_NONE
        val configWeb: ConfigWebPro = SingleContextConfigWeb(factory, configServer, servletConfig)
        createContextFiles(configDir, servletConfig, doNew)
        createContextFilesPost(configDir, configWeb, servletConfig, false, doNew)
        return configWeb
    }

    private fun createLabel(configServer: ConfigServerImpl?, servletConfig: ServletConfig?): String? {
        val hash: String = SystemUtil.hash(servletConfig.getServletContext())
        val labels: Map<String?, String?> = configServer!!.getLabels()
        var label: String? = null
        if (labels != null) {
            label = labels[hash]
        }
        if (label == null) label = hash
        return label
    }

    private fun createHtAccess(htAccess: Resource?) {
        if (!htAccess.exists()) {
            htAccess.createNewFile()
            val content = """
                AuthName "WebInf Folder"
                AuthType Basic
                <Limit GET POST>
                order deny,allow
                deny from all
                </Limit>
                """.trimIndent()
            try {
                IOUtil.copy(ByteArrayInputStream(content.getBytes()), htAccess, true)
            } catch (t: Throwable) {
                ExceptionUtil.rethrowIfNecessary(t)
            }
        }
    }

    /**
     * reloads the Config Object
     *
     * @param cs
     * @param force
     * @throws SAXException
     * @throws ClassNotFoundException
     * @throws PageException
     * @throws IOException
     * @throws TagLibException
     * @throws FunctionLibException
     * @throws BundleException
     * @throws NoSuchAlgorithmException
     */
    // MUST
    @Throws(ClassException::class, PageException::class, IOException::class, TagLibException::class, FunctionLibException::class, BundleException::class)
    fun reloadInstance(engine: CFMLEngine?, cs: ConfigServerImpl?, cw: ConfigWebImpl?, force: Boolean) {
        val configFile: Resource = cw!!.getConfigFile()
        val configDir: Resource = cw!!.getConfigDir()
        val iDoNew: Int = getNew(engine, configDir, false, UpdateInfo.NEW_NONE).updateType
        val doNew = iDoNew != NEW_NONE
        if (configFile == null) return
        if (second(cw!!.getLoadTime()) > second(configFile.lastModified()) && !force) return
        val root: Struct = loadDocument(configFile)
        createContextFiles(configDir, null, doNew)
        cw!!.reset()
        load(cs, cw, root, true, doNew, false)
        createContextFilesPost(configDir, cw, null, false, doNew)
        (ConfigWebUtil.getEngine(cw) as CFMLEngineImpl).onStart(cw, true)
        (cw!!.getGatewayEngine() as GatewayEngineImpl).autoStart()
    }

    private fun second(ms: Long): Long {
        return ms / 1000
    }

    /**
     * @param cs
     * @param config
     * @param doc
     * @throws ClassNotFoundException
     * @throws IOException
     * @throws FunctionLibException
     * @throws TagLibException
     * @throws PageException
     * @throws BundleException
     */
    @Synchronized
    @Throws(IOException::class)
    fun load(cs: ConfigServerImpl?, config: ConfigImpl?, root: Struct?, isReload: Boolean, doNew: Boolean, essentialOnly: Boolean) {
        var root: Struct? = root
        if (LOG) LogUtil.logGlobal(ThreadLocalPageContext.getConfig(if (cs == null) config else cs), Log.LEVEL_INFO, ConfigWebFactory::class.java.getName(), "start reading config")
        ThreadLocalConfig.register(config)
        var reload = false
        // load PW
        try {
            if (createSaltAndPW(root, config, essentialOnly)) reload = true
            if (LOG) LogUtil.logGlobal(ThreadLocalPageContext.getConfig(if (cs == null) config else cs), Log.LEVEL_DEBUG, ConfigWebFactory::class.java.getName(), "fixed salt")

            // delete to big felix.log (there is also code in the loader to do this, but if the loader is not
            // updated ...)
            if (!essentialOnly && config is ConfigServerImpl) {
                try {
                    val rr: File = config!!.getCFMLEngine().getCFMLEngineFactory().getResourceRoot()
                    val log = File(rr, "context/logs/felix.log")
                    if (log.isFile() && log.length() > GB1) {
                        if (log.delete()) ResourceUtil.touch(log)
                    }
                } catch (t: Throwable) {
                    ExceptionUtil.rethrowIfNecessary(t)
                    log(config, null, t)
                }
            }
            // reload when an old version of xml got updated
            if (reload) {
                root = reload(root, config, cs)
                reload = false
            }
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
            log(config, null, t)
        }
        config!!.setLastModified()
        if (config is ConfigWeb) {
            ConfigWebUtil.deployWebContext(cs, config as ConfigWeb?, false)
            ConfigWebUtil.deployWeb(cs, config as ConfigWeb?, false)
            if (LOG) LogUtil.logGlobal(ThreadLocalPageContext.getConfig(if (cs == null) config else cs), Log.LEVEL_DEBUG, ConfigWebFactory::class.java.getName(), "deploy web context")
        }
        if (config is ConfigServerImpl) _loadAdminMode(config as ConfigServerImpl?, root)
        if (LOG) LogUtil.logGlobal(ThreadLocalPageContext.getConfig(if (cs == null) config else cs), Log.LEVEL_DEBUG, ConfigWebFactory::class.java.getName(), "loaded admin mode")
        _loadConfig(cs, config, root)
        val mode: Int = config!!.getMode()
        if (LOG) LogUtil.logGlobal(ThreadLocalPageContext.getConfig(if (cs == null) config else cs), Log.LEVEL_DEBUG, ConfigWebFactory::class.java.getName(), "loaded config")
        if (!essentialOnly) {
            _loadConstants(cs, config, root)
            if (LOG) LogUtil.logGlobal(ThreadLocalPageContext.getConfig(if (cs == null) config else cs), Log.LEVEL_DEBUG, ConfigWebFactory::class.java.getName(), "loaded constants")
        }
        _loadLoggers(cs, config, root, isReload)
        val log: Log = ThreadLocalPageContext.getLog(config, "application")
        // loadServerLibDesc(cs, config, doc,log);
        if (LOG) LogUtil.logGlobal(ThreadLocalPageContext.getConfig(if (cs == null) config else cs), Log.LEVEL_DEBUG, ConfigWebFactory::class.java.getName(), "loaded loggers")
        _loadTempDirectory(cs, config, root, isReload, log)
        if (LOG) LogUtil.logGlobal(ThreadLocalPageContext.getConfig(if (cs == null) config else cs), Log.LEVEL_DEBUG, ConfigWebFactory::class.java.getName(), "loaded temp dir")
        _loadId(cs, config, root, log)
        if (LOG) LogUtil.logGlobal(ThreadLocalPageContext.getConfig(if (cs == null) config else cs), Log.LEVEL_DEBUG, ConfigWebFactory::class.java.getName(), "loaded id")
        _loadVersion(config, root, log)
        if (LOG) LogUtil.logGlobal(ThreadLocalPageContext.getConfig(if (cs == null) config else cs), Log.LEVEL_DEBUG, ConfigWebFactory::class.java.getName(), "loaded version")
        if (!essentialOnly) {
            _loadSecurity(cs, config, root, log)
            if (LOG) LogUtil.logGlobal(ThreadLocalPageContext.getConfig(if (cs == null) config else cs), Log.LEVEL_DEBUG, ConfigWebFactory::class.java.getName(), "loaded security")
        }
        try {
            ConfigWebUtil.loadLib(cs, config)
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
            log(config, log, t)
        }
        if (LOG) LogUtil.logGlobal(ThreadLocalPageContext.getConfig(if (cs == null) config else cs), Log.LEVEL_DEBUG, ConfigWebFactory::class.java.getName(), "loaded lib")
        if (!essentialOnly) {
            _loadSystem(cs, config, root, log)
            if (LOG) LogUtil.logGlobal(ThreadLocalPageContext.getConfig(if (cs == null) config else cs), Log.LEVEL_DEBUG, ConfigWebFactory::class.java.getName(), "loaded system")
            _loadResourceProvider(cs, config, root, log)
            if (LOG) LogUtil.logGlobal(ThreadLocalPageContext.getConfig(if (cs == null) config else cs), Log.LEVEL_DEBUG, ConfigWebFactory::class.java.getName(), "loaded resource providers")
        }
        _loadFilesystem(cs, config, root, doNew, log) // load this before execute any code, what for example loadxtension does (json)
        if (LOG) LogUtil.logGlobal(ThreadLocalPageContext.getConfig(if (cs == null) config else cs), Log.LEVEL_DEBUG, ConfigWebFactory::class.java.getName(), "loaded filesystem")
        if (!essentialOnly) {
            _loadExtensionBundles(cs, config, root, log)
            if (LOG) LogUtil.logGlobal(ThreadLocalPageContext.getConfig(if (cs == null) config else cs), Log.LEVEL_DEBUG, ConfigWebFactory::class.java.getName(), "loaded extension bundles")
            _loadWS(cs, config, root, log)
            if (LOG) LogUtil.logGlobal(ThreadLocalPageContext.getConfig(if (cs == null) config else cs), Log.LEVEL_DEBUG, ConfigWebFactory::class.java.getName(), "loaded webservice")
            _loadORM(cs, config, root, log)
            if (LOG) LogUtil.logGlobal(ThreadLocalPageContext.getConfig(if (cs == null) config else cs), Log.LEVEL_DEBUG, ConfigWebFactory::class.java.getName(), "loaded orm")
            _loadCacheHandler(cs, config, root, log)
            if (LOG) LogUtil.logGlobal(ThreadLocalPageContext.getConfig(if (cs == null) config else cs), Log.LEVEL_DEBUG, ConfigWebFactory::class.java.getName(), "loaded cache handlers")
            _loadCharset(cs, config, root, log)
            if (LOG) LogUtil.logGlobal(ThreadLocalPageContext.getConfig(if (cs == null) config else cs), Log.LEVEL_DEBUG, ConfigWebFactory::class.java.getName(), "loaded charset")
        }
        _loadApplication(cs, config, root, mode, log)
        if (LOG) LogUtil.logGlobal(ThreadLocalPageContext.getConfig(if (cs == null) config else cs), Log.LEVEL_DEBUG, ConfigWebFactory::class.java.getName(), "loaded application")
        if (!essentialOnly) {
            _loadMappings(cs, config, root, mode, log) // it is important this runs after
            if (LOG) LogUtil.logGlobal(ThreadLocalPageContext.getConfig(if (cs == null) config else cs), Log.LEVEL_DEBUG, ConfigWebFactory::class.java.getName(), "loaded mappings")
            _loadRest(cs, config, root, log)
            if (LOG) LogUtil.logGlobal(ThreadLocalPageContext.getConfig(if (cs == null) config else cs), Log.LEVEL_DEBUG, ConfigWebFactory::class.java.getName(), "loaded rest")
            _loadExtensionProviders(cs, config, root, log)
            if (LOG) LogUtil.logGlobal(ThreadLocalPageContext.getConfig(if (cs == null) config else cs), Log.LEVEL_DEBUG, ConfigWebFactory::class.java.getName(), "loaded extensions")
        }
        if (!essentialOnly) {
            _loadDataSources(cs, config, root, log)
            if (LOG) LogUtil.logGlobal(ThreadLocalPageContext.getConfig(if (cs == null) config else cs), Log.LEVEL_DEBUG, ConfigWebFactory::class.java.getName(), "loaded datasources")
            _loadCache(cs, config, root, log)
            if (LOG) LogUtil.logGlobal(ThreadLocalPageContext.getConfig(if (cs == null) config else cs), Log.LEVEL_DEBUG, ConfigWebFactory::class.java.getName(), "loaded cache")
            _loadCustomTagsMappings(cs, config, root, mode, log)
            if (LOG) LogUtil.logGlobal(ThreadLocalPageContext.getConfig(if (cs == null) config else cs), Log.LEVEL_DEBUG, ConfigWebFactory::class.java.getName(), "loaded custom tag mappings")
            // loadFilesystem(cs, config, doc, doNew); // load tlds
        }
        if (!essentialOnly) {
            _loadTag(cs, config, root, log) // load tlds
            if (LOG) LogUtil.logGlobal(ThreadLocalPageContext.getConfig(if (cs == null) config else cs), Log.LEVEL_DEBUG, ConfigWebFactory::class.java.getName(), "loaded tags")
            _loadRegional(cs, config, root, log)
            if (LOG) LogUtil.logGlobal(ThreadLocalPageContext.getConfig(if (cs == null) config else cs), Log.LEVEL_DEBUG, ConfigWebFactory::class.java.getName(), "loaded regional")
            _loadCompiler(cs, config, root, mode, log)
            if (LOG) LogUtil.logGlobal(ThreadLocalPageContext.getConfig(if (cs == null) config else cs), Log.LEVEL_DEBUG, ConfigWebFactory::class.java.getName(), "loaded compiler")
            _loadScope(cs, config, root, mode, log)
            if (LOG) LogUtil.logGlobal(ThreadLocalPageContext.getConfig(if (cs == null) config else cs), Log.LEVEL_DEBUG, ConfigWebFactory::class.java.getName(), "loaded scope")
            _loadMail(cs, config, root, log)
            if (LOG) LogUtil.logGlobal(ThreadLocalPageContext.getConfig(if (cs == null) config else cs), Log.LEVEL_DEBUG, ConfigWebFactory::class.java.getName(), "loaded mail")
            _loadSearch(cs, config, root, log)
            if (LOG) LogUtil.logGlobal(ThreadLocalPageContext.getConfig(if (cs == null) config else cs), Log.LEVEL_DEBUG, ConfigWebFactory::class.java.getName(), "loaded search")
            _loadScheduler(cs, config, root, log)
            if (LOG) LogUtil.logGlobal(ThreadLocalPageContext.getConfig(if (cs == null) config else cs), Log.LEVEL_DEBUG, ConfigWebFactory::class.java.getName(), "loaded scheduled tasks")
            _loadDebug(cs, config, root, log)
            if (LOG) LogUtil.logGlobal(ThreadLocalPageContext.getConfig(if (cs == null) config else cs), Log.LEVEL_DEBUG, ConfigWebFactory::class.java.getName(), "loaded debug")
            _loadError(cs, config, root, log)
            if (LOG) LogUtil.logGlobal(ThreadLocalPageContext.getConfig(if (cs == null) config else cs), Log.LEVEL_DEBUG, ConfigWebFactory::class.java.getName(), "loaded error")
            _loadRegex(cs, config, root, log)
            if (LOG) LogUtil.logGlobal(ThreadLocalPageContext.getConfig(if (cs == null) config else cs), Log.LEVEL_DEBUG, ConfigWebFactory::class.java.getName(), "loaded regex")
            _loadCFX(cs, config, root, log)
            if (LOG) LogUtil.logGlobal(ThreadLocalPageContext.getConfig(if (cs == null) config else cs), Log.LEVEL_DEBUG, ConfigWebFactory::class.java.getName(), "loaded cfx")
            _loadComponent(cs, config, root, mode, log)
            if (LOG) LogUtil.logGlobal(ThreadLocalPageContext.getConfig(if (cs == null) config else cs), Log.LEVEL_DEBUG, ConfigWebFactory::class.java.getName(), "loaded component")
            _loadUpdate(cs, config, root, log)
            if (LOG) LogUtil.logGlobal(ThreadLocalPageContext.getConfig(if (cs == null) config else cs), Log.LEVEL_DEBUG, ConfigWebFactory::class.java.getName(), "loaded update")
            _loadJava(cs, config, root, log) // define compile type
            if (LOG) LogUtil.logGlobal(ThreadLocalPageContext.getConfig(if (cs == null) config else cs), Log.LEVEL_DEBUG, ConfigWebFactory::class.java.getName(), "loaded java")
            _loadSetting(cs, config, root, log)
            if (LOG) LogUtil.logGlobal(ThreadLocalPageContext.getConfig(if (cs == null) config else cs), Log.LEVEL_DEBUG, ConfigWebFactory::class.java.getName(), "loaded setting")
            _loadProxy(cs, config, root, log)
            if (LOG) LogUtil.logGlobal(ThreadLocalPageContext.getConfig(if (cs == null) config else cs), Log.LEVEL_DEBUG, ConfigWebFactory::class.java.getName(), "loaded proxy")
            _loadRemoteClient(cs, config, root, log)
            if (LOG) LogUtil.logGlobal(ThreadLocalPageContext.getConfig(if (cs == null) config else cs), Log.LEVEL_DEBUG, ConfigWebFactory::class.java.getName(), "loaded remote clients")
            settings(config, log)
            if (LOG) LogUtil.logGlobal(ThreadLocalPageContext.getConfig(if (cs == null) config else cs), Log.LEVEL_DEBUG, ConfigWebFactory::class.java.getName(), "loaded settings2")
            _loadListener(cs, config, root, log)
            if (LOG) LogUtil.logGlobal(ThreadLocalPageContext.getConfig(if (cs == null) config else cs), Log.LEVEL_DEBUG, ConfigWebFactory::class.java.getName(), "loaded listeners")
            _loadDumpWriter(cs, config, root, log)
            if (LOG) LogUtil.logGlobal(ThreadLocalPageContext.getConfig(if (cs == null) config else cs), Log.LEVEL_DEBUG, ConfigWebFactory::class.java.getName(), "loaded dump writers")
            _loadGatewayEL(cs, config, root, log)
            if (LOG) LogUtil.logGlobal(ThreadLocalPageContext.getConfig(if (cs == null) config else cs), Log.LEVEL_DEBUG, ConfigWebFactory::class.java.getName(), "loaded gateways")
            _loadExeLog(cs, config, root, log)
            if (LOG) LogUtil.logGlobal(ThreadLocalPageContext.getConfig(if (cs == null) config else cs), Log.LEVEL_DEBUG, ConfigWebFactory::class.java.getName(), "loaded exe log")
            _loadQueue(cs, config, root, log)
            if (LOG) LogUtil.logGlobal(ThreadLocalPageContext.getConfig(if (cs == null) config else cs), Log.LEVEL_DEBUG, ConfigWebFactory::class.java.getName(), "loaded queue")
            _loadMonitors(cs, config, root, log)
            if (LOG) LogUtil.logGlobal(ThreadLocalPageContext.getConfig(if (cs == null) config else cs), Log.LEVEL_DEBUG, ConfigWebFactory::class.java.getName(), "loaded monitors")
            _loadLogin(cs, config, root, log)
            if (LOG) LogUtil.logGlobal(ThreadLocalPageContext.getConfig(if (cs == null) config else cs), Log.LEVEL_DEBUG, ConfigWebFactory::class.java.getName(), "loaded login")
            _loadStartupHook(cs, config, root, log)
            if (LOG) LogUtil.logGlobal(ThreadLocalPageContext.getConfig(if (cs == null) config else cs), Log.LEVEL_DEBUG, ConfigWebFactory::class.java.getName(), "loaded startup hook")
        }
        config!!.setLoadTime(System.currentTimeMillis())
        if (config is ConfigWebImpl) {
            TagUtil.addTagMetaData(config as ConfigWebImpl?, log)
            if (LOG) LogUtil.logGlobal(ThreadLocalPageContext.getConfig(if (cs == null) config else cs), Log.LEVEL_DEBUG, ConfigWebFactory::class.java.getName(), "added tag meta data")
        }
    }

    private fun createSaltAndPW(root: Struct?, config: Config?, essentialOnly: Boolean): Boolean {
        if (root == null) return false

        // salt
        var salt = getAttr(root, "adminSalt")
        if (StringUtil.isEmpty(salt, true)) salt = getAttr(root, "salt")
        var rtn = false
        if (StringUtil.isEmpty(salt, true) || !Decision.isUUId(salt)) {
            // create salt
            root.setEL("salt", CreateUUID.invoke().also { salt = it })
            rtn = true
        }

        // no password yet
        if (!essentialOnly && config is ConfigServer && StringUtil.isEmpty(root.get("hspw", ""), true) && StringUtil.isEmpty(root.get("adminhspw", ""), true)
                && StringUtil.isEmpty(root.get("pw", ""), true) && StringUtil.isEmpty(root.get("adminpw", ""), true) && StringUtil.isEmpty(root.get("password", ""), true)
                && StringUtil.isEmpty(root.get("adminpassword", ""), true)) {
            val cs: ConfigServer? = config as ConfigServer?
            val pwFile: Resource = cs.getConfigDir().getRealResource("password.txt")
            if (pwFile.isFile()) {
                try {
                    var pw: String? = IOUtil.toString(pwFile, null as Charset?)
                    if (!StringUtil.isEmpty(pw, true)) {
                        pw = pw.trim()
                        val hspw: String = PasswordImpl(Password.ORIGIN_UNKNOW, pw, salt).getPassword()
                        root.setEL("hspw", hspw)
                        pwFile.delete()
                        rtn = true
                    }
                } catch (e: IOException) {
                    LogUtil.logGlobal(cs, "application", e)
                }
            } else {
                LogUtil.log(config, Log.LEVEL_ERROR, "application", "no password set and no password file found at [$pwFile]")
            }
        }
        return rtn
    }

    @Throws(PageException::class, IOException::class, ConverterException::class)
    private fun reload(root: Struct?, config: ConfigImpl?, cs: ConfigServerImpl?): Struct? {
        // store as json
        var root: Struct? = root
        val json = JSONConverter(true, CharsetUtil.UTF8, JSONDateFormat.PATTERN_CF, true, true)
        val str: String = json.serialize(null, root, SerializationSettings.SERIALIZE_AS_ROW)
        IOUtil.write(config!!.getConfigFile(), str, CharsetUtil.UTF8, false)
        root = loadDocument(config!!.getConfigFile())
        if (LOG) LogUtil.logGlobal(ThreadLocalPageContext.getConfig(if (cs == null) config else cs), Log.LEVEL_INFO, ConfigWebFactory::class.java.getName(), "reloading configuration")
        return root
    }

    private fun _loadResourceProvider(configServer: ConfigServerImpl?, config: ConfigImpl?, root: Struct?, log: Log?) {
        try {
            val hasCS = configServer != null
            config!!.clearResourceProviders()
            val providers: Array = ConfigWebUtil.getAsArray("resourceProviders", root)
            val defaultProviders: Array = ConfigWebUtil.getAsArray("defaultResourceProvider", root)

            // Default Resource Provider
            if (hasCS) config!!.setDefaultResourceProvider(configServer!!.getDefaultResourceProvider())
            if (defaultProviders != null && defaultProviders.size() > 0) {
                val defaultProvider: Struct = Caster.toStruct(defaultProviders.getE(defaultProviders.size()))
                val defProv: ClassDefinition? = getClassDefinition(defaultProvider, "", config.getIdentification())
                var strDefaultProviderComponent = getAttr(defaultProvider, "component")
                if (StringUtil.isEmpty(strDefaultProviderComponent)) strDefaultProviderComponent = getAttr(defaultProvider, "class")

                // class
                if (defProv.hasClass()) {
                    config!!.setDefaultResourceProvider(defProv.getClazz(), toArguments(getAttr(defaultProvider, "arguments"), true))
                } else if (!StringUtil.isEmpty(strDefaultProviderComponent)) {
                    strDefaultProviderComponent = strDefaultProviderComponent.trim()
                    val args = toArguments(getAttr(defaultProvider, "arguments"), true)
                    args.put("component", strDefaultProviderComponent)
                    config!!.setDefaultResourceProvider(CFMLResourceProvider::class.java, args)
                }
            }
            // Resource Provider
            if (hasCS) config!!.setResourceProviderFactories(configServer!!.getResourceProviderFactories())
            if (providers != null && providers.size() > 0) {
                var prov: ClassDefinition?
                var strProviderCFC: String?
                var strProviderScheme: String?
                var httpClass: ClassDefinition? = null
                var httpArgs: Map? = null
                var hasHTTPs = false
                val pit: Iterator<*> = providers.getIterator()
                var provider: Struct
                while (pit.hasNext()) {
                    provider = Caster.toStruct(pit.next(), null)
                    if (provider == null) continue
                    try {
                        prov = getClassDefinition(provider, "", config.getIdentification())
                        strProviderCFC = getAttr(provider, "component")
                        if (StringUtil.isEmpty(strProviderCFC)) strProviderCFC = getAttr(provider, "class")

                        // ignore OLD S3 extension from 4.0
                        // lucee.commons.io.res.type.s3.S3ResourceProvider
                        if ("lucee.extension.io.resource.type.s3.S3ResourceProvider".equals(prov.getClassName())
                                || "lucee.commons.io.res.type.s3.S3ResourceProvider".equals(prov.getClassName())) continue
                        // prov=new ClassDefinitionImpl(S3ResourceProvider.class);
                        strProviderScheme = getAttr(provider, "scheme")
                        // class
                        if (prov.hasClass() && !StringUtil.isEmpty(strProviderScheme)) {
                            strProviderScheme = strProviderScheme.trim().toLowerCase()
                            config!!.addResourceProvider(strProviderScheme, prov, toArguments(getAttr(provider, "arguments"), true))

                            // patch for user not having
                            if (strProviderScheme.equalsIgnoreCase("http")) {
                                httpClass = prov
                                httpArgs = toArguments(getAttr(provider, "arguments"), true)
                            } else if (strProviderScheme.equalsIgnoreCase("https")) hasHTTPs = true
                        } else if (!StringUtil.isEmpty(strProviderCFC) && !StringUtil.isEmpty(strProviderScheme)) {
                            strProviderCFC = strProviderCFC.trim()
                            strProviderScheme = strProviderScheme.trim().toLowerCase()
                            val args = toArguments(getAttr(provider, "arguments"), true)
                            args.put("component", strProviderCFC)
                            config!!.addResourceProvider(strProviderScheme, ClassDefinitionImpl(CFMLResourceProvider::class.java), args)
                        }
                    } catch (t: Throwable) {
                        ExceptionUtil.rethrowIfNecessary(t)
                        log(config, log, t)
                    }
                }

                // adding https when not exist
                if (!hasHTTPs && httpClass != null) {
                    config!!.addResourceProvider("https", httpClass, httpArgs)
                }
                // adding s3 when not exist

                // we make sure we have the default on server level
                if (!hasCS && !config!!.hasResourceProvider("s3")) {
                    val s3Class: ClassDefinition = ClassDefinitionImpl(DummyS3ResourceProvider::class.java)
                    config!!.addResourceProvider("s3", s3Class, toArguments("lock-timeout:10000;", false))
                }
            }
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
            log(config, log, t)
        }
    }

    private fun getClassDefinition(data: Struct?, prefix: String?, id: Identification?): ClassDefinition? {
        var prefix = prefix
        var cn: String?
        val bn: String?
        val bv: String?
        if (StringUtil.isEmpty(prefix)) {
            cn = getAttr(data, "class")
            bn = getAttr(data, "bundleName")
            bv = getAttr(data, "bundleVersion")
        } else {
            if (prefix.endsWith("-")) prefix = prefix.substring(0, prefix!!.length() - 1)
            cn = getAttr(data, prefix.toString() + "Class")
            bn = getAttr(data, prefix.toString() + "BundleName")
            bv = getAttr(data, prefix.toString() + "BundleVersion")
        }

        // proxy jar libary no longer provided, so if still this class name is used ....
        if ("com.microsoft.jdbc.sqlserver.SQLServerDriver".equals(cn)) {
            cn = "com.microsoft.sqlserver.jdbc.SQLServerDriver"
        }
        // if(!StringUtil.isEmpty(cd.className,true))cd.getClazz();
        return ClassDefinitionImpl(cn, bn, bv, id)
    }

    private fun _loadCacheHandler(configServer: ConfigServerImpl?, config: ConfigImpl?, root: Struct?, log: Log?) {
        try {
            val hasCS = configServer != null
            // !!!! config.clearResourceProviders();

            // first of all we make sure we have a request and timespan cachehandler
            if (!hasCS) {
                config.addCacheHandler("request", ClassDefinitionImpl(RequestCacheHandler::class.java))
                config.addCacheHandler("timespan", ClassDefinitionImpl(TimespanCacheHandler::class.java))
            }

            // add CacheHandlers from server context to web context
            if (hasCS) {
                val it: Iterator<Entry<String?, Class<CacheHandler?>?>?> = configServer!!.getCacheHandlers()
                if (it != null) {
                    var entry: Entry<String?, Class<CacheHandler?>?>?
                    while (it.hasNext()) {
                        entry = it.next()
                        try {
                            config.addCacheHandler(entry.getKey(), entry.getValue())
                        } catch (t: Throwable) {
                            ExceptionUtil.rethrowIfNecessary(t)
                            log(config, log, t)
                        }
                    }
                }
            }
            val handlers: Struct = ConfigWebUtil.getAsStruct("cacheHandlers", root)
            if (handlers != null) {
                var cd: ClassDefinition?
                var strId: String
                val it: Iterator<Entry<Key?, Object?>?> = handlers.entryIterator()
                var entry: Entry<Key?, Object?>?
                var handler: Struct
                while (it.hasNext()) {
                    try {
                        entry = it.next()
                        handler = Caster.toStruct(entry.getValue(), null)
                        if (handler == null) continue
                        cd = getClassDefinition(handler, "", config.getIdentification())
                        strId = entry.getKey().getString()
                        if (cd.hasClass() && !StringUtil.isEmpty(strId)) {
                            strId = strId.trim().toLowerCase()
                            try {
                                config.addCacheHandler(strId, cd)
                            } catch (t: Throwable) {
                                ExceptionUtil.rethrowIfNecessary(t)
                                log.error("Cache-Handler", t)
                            }
                        }
                    } catch (t: Throwable) {
                        ExceptionUtil.rethrowIfNecessary(t)
                        log(config, log, t)
                    }
                }
            }
        } catch (th: Throwable) {
            ExceptionUtil.rethrowIfNecessary(th)
            log(config, log, th)
        }
    }

    private fun _loadDumpWriter(configServer: ConfigServerImpl?, config: ConfigImpl?, root: Struct?, log: Log?) {
        try {
            val hasCS = configServer != null
            val writers: Array = ConfigWebUtil.getAsArray("dumpWriters", root)
            val sct: Struct = StructImpl()
            var hasPlain = false
            var hasRich = false
            if (hasCS) {
                val entries: Array<DumpWriterEntry?> = configServer!!.getDumpWritersEntries()
                if (entries != null) {
                    for (i in entries.indices) {
                        try {
                            if (entries[i].getDefaultType() === HTMLDumpWriter.DEFAULT_PLAIN) hasPlain = true
                            if (entries[i].getDefaultType() === HTMLDumpWriter.DEFAULT_RICH) hasRich = true
                            sct.put(entries[i].getName(), entries[i])
                        } catch (t: Throwable) {
                            ExceptionUtil.rethrowIfNecessary(t)
                            log(config, log, t)
                        }
                    }
                }
            }
            if (writers != null && writers.size() > 0) {
                var cd: ClassDefinition?
                var strName: String?
                var strDefault: String?
                var clazz: Class
                var def: Int = HTMLDumpWriter.DEFAULT_NONE
                val it: Iterator<*> = writers.getIterator()
                var writer: Struct
                while (it.hasNext()) {
                    try {
                        writer = Caster.toStruct(it.next(), null)
                        if (writer == null) continue
                        cd = getClassDefinition(writer, "", config.getIdentification())
                        strName = getAttr(writer, "name")
                        strDefault = getAttr(writer, "default")
                        clazz = cd.getClazz(null)
                        if (clazz != null && !StringUtil.isEmpty(strName)) {
                            if (StringUtil.isEmpty(strDefault)) def = HTMLDumpWriter.DEFAULT_NONE else if ("browser".equalsIgnoreCase(strDefault)) def = HTMLDumpWriter.DEFAULT_RICH else if ("console".equalsIgnoreCase(strDefault)) def = HTMLDumpWriter.DEFAULT_PLAIN
                            sct.put(strName, DumpWriterEntry(def, strName, ClassUtil.loadInstance(clazz) as DumpWriter))
                        }
                    } catch (t: Throwable) {
                        ExceptionUtil.rethrowIfNecessary(t)
                        log(config, log, t)
                    }
                }
            } else {
                // print.err("yep");
                if (!hasRich) sct.setEL(KeyConstants._html, DumpWriterEntry(HTMLDumpWriter.DEFAULT_RICH, "html", HTMLDumpWriter()))
                if (!hasPlain) sct.setEL(KeyConstants._text, DumpWriterEntry(HTMLDumpWriter.DEFAULT_PLAIN, "text", TextDumpWriter()))
                sct.setEL(KeyConstants._classic, DumpWriterEntry(HTMLDumpWriter.DEFAULT_NONE, "classic", ClassicHTMLDumpWriter()))
                sct.setEL(KeyConstants._simple, DumpWriterEntry(HTMLDumpWriter.DEFAULT_NONE, "simple", SimpleHTMLDumpWriter()))
            }
            val it: Iterator<Object?> = sct.valueIterator()
            val entries: MutableList<DumpWriterEntry?> = ArrayList<DumpWriterEntry?>()
            while (it.hasNext()) {
                entries.add(it.next() as DumpWriterEntry?)
            }
            config!!.setDumpWritersEntries(entries.toArray(arrayOfNulls<DumpWriterEntry?>(entries.size())))
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
            log(config, log, t)
        }
    }

    fun toArguments(attributes: String?, decode: Boolean): Map<String?, String?>? {
        return cssStringToMap(attributes, decode, false)
    }

    fun cssStringToMap(attributes: String?, decode: Boolean, lowerKeys: Boolean): Map<String?, String?>? {
        val map: Map<String?, String?> = HashMap<String?, String?>()
        if (StringUtil.isEmpty(attributes, true)) return map
        val arr: Array<String?> = ListUtil.toStringArray(ListUtil.listToArray(attributes, ';'), null)
        var index: Int
        var str: String?
        for (i in arr.indices) {
            str = arr[i].trim()
            if (StringUtil.isEmpty(str)) continue
            index = str.indexOf(':')
            if (index == -1) map.put(if (lowerKeys) str.toLowerCase() else str, "") else {
                var k = dec(str.substring(0, index).trim(), decode)
                if (lowerKeys) k = k.toLowerCase()
                map.put(k, dec(str.substring(index + 1).trim(), decode))
            }
        }
        return map
    }

    private fun dec(str: String?, decode: Boolean): String? {
        return if (!decode) str else URLDecoder.decode(str, false)
    }

    private fun _loadListener(configServer: ConfigServerImpl?, config: ConfigImpl?, root: Struct?, log: Log?) {
        try {
            if (config is ConfigServer) {
                val cs: ConfigServer? = config as ConfigServer?
                val listener: Struct = ConfigWebUtil.getAsStruct("listener", root)
                val cd: ClassDefinition? = if (listener != null) getClassDefinition(listener, "", config.getIdentification()) else null
                var strArguments = getAttr(listener, "arguments")
                if (strArguments == null) strArguments = ""
                if (cd != null && cd.hasClass()) {
                    try {
                        val obj: Object = ClassUtil.loadInstance(cd.getClazz(), arrayOf(strArguments), null)
                        if (obj is ConfigListener) {
                            val cl: ConfigListener = obj as ConfigListener
                            cs.setConfigListener(cl)
                        }
                    } catch (t: Throwable) {
                        ExceptionUtil.rethrowIfNecessary(t)
                        t.printStackTrace(config!!.getErrWriter())
                    }
                }
            } else if (configServer != null) {
                val listener: ConfigListener = configServer.getConfigListener()
                if (listener != null) listener.onLoadWebContext(configServer, config as ConfigWeb?)
            }
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
            log(config, log, t)
        }
    }

    private fun settings(config: ConfigImpl?, log: Log?) {
        try {
            doCheckChangesInLibraries(config)
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
            log(config, log, t)
        }
    }

    private fun _loadVersion(config: ConfigImpl?, root: Struct?, log: Log?) {
        try {
            val strVersion = getAttr(root, "version")
            config!!.setVersion(Caster.toDoubleValue(strVersion, 5.0)) // cfconfig started with version 5
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
            log(config, log, t)
        }
    }

    private fun _loadId(configServer: ConfigServerImpl?, config: ConfigImpl?, root: Struct?, log: Log?) {
        try {
            if (root == null && configServer != null) {
                val id: Identification = configServer.getIdentification()
                (config as ConfigWebImpl?)!!.setIdentification(IdentificationWebImpl(config, id.getSecurityKey(), id.getApiKey()))
                return
            }

            // Security key
            val res: Resource = config!!.getConfigDir().getRealResource("id")
            var securityKey: String? = null
            try {
                if (!res.exists()) {
                    res.createNewFile()
                    IOUtil.write(res, UUID.randomUUID().toString().also { securityKey = it }, SystemUtil.getCharset(), false)
                } else {
                    securityKey = IOUtil.toString(res, SystemUtil.getCharset())
                }
            } catch (ioe: Exception) {
                log(config, log, ioe)
            }
            if (StringUtil.isEmpty(securityKey)) securityKey = UUID.randomUUID().toString()

            // API Key
            var apiKey: String? = null
            val str = if (root != null) getAttr(root, "apiKey") else null
            if (!StringUtil.isEmpty(str, true)) apiKey = str.trim() else if (configServer != null) apiKey = configServer.getIdentification().getApiKey() // if there is no web api key the server api key is used
            if (config is ConfigWebImpl) (config as ConfigWebImpl?)!!.setIdentification(IdentificationWebImpl(config as ConfigWebImpl?, securityKey, apiKey)) else (config as ConfigServerImpl?)!!.setIdentification(IdentificationServerImpl(config as ConfigServerImpl?, securityKey, apiKey))
            config.getIdentification().getId()
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
            log(config, log, t)
        }
    }

    private fun equal(srcs: Array<Resource?>?, trgs: Array<Resource?>?): Boolean {
        if (srcs!!.size != trgs!!.size) return false
        var src: Resource?
        outer@ for (i in srcs.indices) {
            src = srcs[i]
            for (y in trgs.indices) {
                if (src.equals(trgs[y])) continue@outer
            }
            return false
        }
        return true
    }

    private fun getNewResources(srcs: Array<Resource?>?, trgs: Array<Resource?>?): Array<Resource?>? {
        var trg: Resource?
        val list: MutableList<Resource?> = ArrayList<Resource?>()
        outer@ for (i in trgs.indices) {
            trg = trgs!![i]
            for (y in srcs.indices) {
                if (trg.equals(srcs!![y])) continue@outer
            }
            list.add(trg)
        }
        return list.toArray(arrayOfNulls<Resource?>(list.size()))
    }

    /**
     * @param configServer
     * @param config
     * @param doc
     */
    private fun _loadSecurity(configServer: ConfigServerImpl?, config: ConfigImpl?, root: Struct?, log: Log?) {
        try {
            // Security Manger
            var securityManager: SecurityManager? = null
            if (config is ConfigServerImpl) {
                val cs: ConfigServerImpl? = config
                val security: Struct = ConfigWebUtil.getAsStruct("security", root)

                // Default SecurityManager
                var sm: SecurityManagerImpl? = _toSecurityManager(security)

                // additional file access directories
                var elFileAccesses: Array = ConfigWebUtil.getAsArray("fileAccess", security)
                sm.setCustomFileAccess(_loadFileAccess(config, elFileAccesses, log))
                cs!!.setDefaultSecurityManager(sm)

                // Web SecurityManager
                val accessors: Array = ConfigWebUtil.getAsArray("", security)
                val it: Iterator<*> = accessors.getIterator()
                var ac: Struct
                while (it.hasNext()) {
                    try {
                        ac = Caster.toStruct(it.next(), null)
                        if (ac == null) continue
                        val id = getAttr(ac, "id")
                        if (id != null) {
                            sm = _toSecurityManager(ac)
                            elFileAccesses = ConfigWebUtil.getAsArray("fileAccess", ac)
                            sm.setCustomFileAccess(_loadFileAccess(config, elFileAccesses, log))
                            cs!!.setSecurityManager(id, sm)
                        }
                    } catch (t: Throwable) {
                        ExceptionUtil.rethrowIfNecessary(t)
                        log(config, log, t)
                    }
                }
            } else if (configServer != null) {
                securityManager = configServer.getSecurityManager(config.getIdentification().getId())
            }
            if (config is ConfigWebImpl) {
                if (securityManager == null) securityManager = SecurityManagerImpl.getOpenSecurityManager()
                (config as ConfigWebImpl?)!!.setSecurityManager(securityManager)
            }
            val security: Struct = ConfigWebUtil.getAsStruct("security", root)
            var vu: Int = ConfigImpl.QUERY_VAR_USAGE_UNDEFINED
            if (security != null) {
                vu = AppListenerUtil.toVariableUsage(getAttr(security, "variableUsage"), ConfigImpl.QUERY_VAR_USAGE_UNDEFINED)
                if (vu == ConfigImpl.QUERY_VAR_USAGE_UNDEFINED) vu = AppListenerUtil.toVariableUsage(getAttr(security, "varUsage"), ConfigImpl.QUERY_VAR_USAGE_UNDEFINED)
            }
            if (vu == ConfigImpl.QUERY_VAR_USAGE_UNDEFINED) {
                vu = if (configServer != null) {
                    configServer.getQueryVarUsage()
                } else ConfigImpl.QUERY_VAR_USAGE_IGNORE
            }
            config!!.setQueryVarUsage(vu)
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
            t.printStackTrace()
            log(config, log, t)
        }
    }

    private fun _loadFileAccess(config: Config?, fileAccesses: Array?, log: Log?): Array<Resource?>? {
        if (fileAccesses.size() === 0) return arrayOfNulls<Resource?>(0)
        val reses: MutableList<Resource?> = ArrayList<Resource?>()
        var path: String?
        var res: Resource
        val it: Iterator<*> = fileAccesses.getIterator()
        var fa: Struct
        while (it.hasNext()) {
            try {
                fa = Caster.toStruct(it.next(), null)
                if (fa == null) continue
                path = getAttr(fa, "path")
                if (!StringUtil.isEmpty(path)) {
                    res = config.getResource(path)
                    if (res.isDirectory()) reses.add(res)
                }
            } catch (t: Throwable) {
                ExceptionUtil.rethrowIfNecessary(t)
                log(config, log, t)
            }
        }
        return reses.toArray(arrayOfNulls<Resource?>(reses.size()))
    }

    private fun _toSecurityManager(el: Struct?): SecurityManagerImpl? {
        return SecurityManagerImpl(_attr(el, "setting", SecurityManager.VALUE_YES), _attr(el, "file", SecurityManager.VALUE_ALL),
                _attr(el, "direct_java_access", SecurityManager.VALUE_YES), _attr(el, "mail", SecurityManager.VALUE_YES), _attr(el, "datasource", SecurityManager.VALUE_YES),
                _attr(el, "mapping", SecurityManager.VALUE_YES), _attr(el, "remote", SecurityManager.VALUE_YES), _attr(el, "custom_tag", SecurityManager.VALUE_YES),
                _attr(el, "cfx_setting", SecurityManager.VALUE_YES), _attr(el, "cfx_usage", SecurityManager.VALUE_YES), _attr(el, "debugging", SecurityManager.VALUE_YES),
                _attr(el, "search", SecurityManager.VALUE_YES), _attr(el, "scheduled_task", SecurityManager.VALUE_YES), _attr(el, "tag_execute", SecurityManager.VALUE_YES),
                _attr(el, "tag_import", SecurityManager.VALUE_YES), _attr(el, "tag_object", SecurityManager.VALUE_YES), _attr(el, "tag_registry", SecurityManager.VALUE_YES),
                _attr(el, "cache", SecurityManager.VALUE_YES), _attr(el, "gateway", SecurityManager.VALUE_YES), _attr(el, "orm", SecurityManager.VALUE_YES),
                _attr2(el, "access_read", SecurityManager.ACCESS_PROTECTED), _attr2(el, "access_write", SecurityManager.ACCESS_PROTECTED))
    }

    private fun _attr(el: Struct?, attr: String?, _default: Short): Short {
        return SecurityManagerImpl.toShortAccessValue(getAttr(el, attr), _default)
    }

    private fun _attr2(el: Struct?, attr: String?, _default: Short): Short {
        var strAccess = getAttr(el, attr)
        if (StringUtil.isEmpty(strAccess)) return _default
        strAccess = strAccess.trim().toLowerCase()
        if ("open".equals(strAccess)) return SecurityManager.ACCESS_OPEN
        if ("protected".equals(strAccess)) return SecurityManager.ACCESS_PROTECTED
        return if ("close".equals(strAccess)) SecurityManager.ACCESS_CLOSE else _default
    }

    @Throws(IOException::class)
    fun createMD5FromResource(resource: String?): String? {
        var `is`: InputStream? = null
        return try {
            `is` = InfoImpl::class.java.getResourceAsStream(resource)
            val barr: ByteArray = IOUtil.toBytes(`is`)
            MD5.getDigestAsString(barr)
        } finally {
            IOUtil.close(`is`)
        }
    }

    @Throws(IOException::class)
    fun createContentFromResource(resource: Resource?): String? {
        return IOUtil.toString(resource, null as Charset?)
    }

    fun createFileFromResourceCheckSizeDiffEL(resource: String?, file: Resource?) {
        try {
            createFileFromResourceCheckSizeDiff(resource, file)
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
            LogUtil.logGlobal(ThreadLocalPageContext.getConfig(), Log.LEVEL_ERROR, ConfigWebFactory::class.java.getName(), resource)
            LogUtil.logGlobal(ThreadLocalPageContext.getConfig(), Log.LEVEL_ERROR, ConfigWebFactory::class.java.getName(), file.toString() + "")
            LogUtil.logGlobal(ThreadLocalPageContext.getConfig(), ConfigWebFactory::class.java.getName(), t)
        }
    }

    /**
     * creates a File and his content froma a resurce
     *
     * @param resource
     * @param file
     * @throws IOException
     */
    @Throws(IOException::class)
    fun createFileFromResourceCheckSizeDiff(resource: String?, file: Resource?) {
        val baos = ByteArrayOutputStream()
        IOUtil.copy(InfoImpl::class.java.getResourceAsStream(resource), baos, true, false)
        val barr: ByteArray = baos.toByteArray()
        if (file.exists()) {
            val trgSize: Long = file.length()
            val srcSize = barr.size.toLong()
            if (srcSize == trgSize) return
            LogUtil.logGlobal(ThreadLocalPageContext.getConfig(), Log.LEVEL_DEBUG, ConfigWebFactory::class.java.getName(), "update file:$file")
            LogUtil.logGlobal(ThreadLocalPageContext.getConfig(), Log.LEVEL_DEBUG, ConfigWebFactory::class.java.getName(), " - source:$srcSize")
            LogUtil.logGlobal(ThreadLocalPageContext.getConfig(), Log.LEVEL_DEBUG, ConfigWebFactory::class.java.getName(), " - target:$trgSize")
        } else file.createNewFile()
        IOUtil.copy(ByteArrayInputStream(barr), file, true)
    }

    /**
     * Creates all files for Lucee Context
     *
     * @param configDir
     * @throws IOException
     * @throws IOException
     */
    @Throws(IOException::class)
    private fun createContextFiles(configDir: Resource?, servletConfig: ServletConfig?, doNew: Boolean) {
        // NICE dies muss dynamisch erstellt werden, da hier der admin hinkommt
        // und dieser sehr viele files haben wird
        val contextDir: Resource = configDir.getRealResource("context")
        if (!contextDir.exists()) contextDir.mkdirs()

        // custom locale files
        if (doNew) {
            val dir: Resource = configDir.getRealResource("locales")
            if (!dir.exists()) dir.mkdirs()
            val file: Resource = dir.getRealResource("pt-PT-date.df")
            if (!file.exists()) createFileFromResourceEL("/resource/locales/pt-PT-date.df", file)
        }
        // bin
        val binDir: Resource = configDir.getRealResource("bin")
        if (!binDir.exists()) binDir.mkdirs()
        val ctDir: Resource = configDir.getRealResource("customtags")
        if (!ctDir.exists()) ctDir.mkdirs()

        // Jacob
        if (SystemUtil.isWindows()) {
            val name = if (SystemUtil.getJREArch() === SystemUtil.ARCH_64) "jacob-x64.dll" else "jacob-i586.dll"
            val jacob: Resource = binDir.getRealResource(name)
            if (!jacob.exists()) {
                createFileFromResourceEL("/resource/bin/windows" + (if (SystemUtil.getJREArch() === SystemUtil.ARCH_64) "64" else "32") + "/" + name, jacob)
            }
        }
        val storDir: Resource = configDir.getRealResource("storage")
        if (!storDir.exists()) storDir.mkdirs()
        val compDir: Resource = configDir.getRealResource("components")
        if (!compDir.exists()) compDir.mkdirs()

        // remove old cacerts files, they are now only in the server context
        val secDir: Resource = configDir.getRealResource("security")
        var f: Resource? = null
        if (!secDir.exists()) secDir.mkdirs()
        f = secDir.getRealResource("antisamy-basic.xml")
        if (!f.exists() || doNew) createFileFromResourceEL("/resource/security/antisamy-basic.xml", f)

        // lucee-context
        f = contextDir.getRealResource("lucee-context.lar")
        if (!f.exists() || doNew) createFileFromResourceEL("/resource/context/lucee-context.lar", f) else createFileFromResourceCheckSizeDiffEL("/resource/context/lucee-context.lar", f)

        // lucee-admin
        f = contextDir.getRealResource("lucee-admin.lar")
        if (!f.exists() || doNew) createFileFromResourceEL("/resource/context/lucee-admin.lar", f) else createFileFromResourceCheckSizeDiffEL("/resource/context/lucee-admin.lar", f)

        // lucee-doc
        f = contextDir.getRealResource("lucee-doc.lar")
        if (!f.exists() || doNew) createFileFromResourceEL("/resource/context/lucee-doc.lar", f) else createFileFromResourceCheckSizeDiffEL("/resource/context/lucee-doc.lar", f)
        f = contextDir.getRealResource("component-dump." + TEMPLATE_EXTENSION)
        if (!f.exists()) createFileFromResourceEL("/resource/context/component-dump." + TEMPLATE_EXTENSION, f)

        // Base Component
        val badContent = "<cfcomponent displayname=\"Component\" hint=\"This is the Base Component\">\n</cfcomponent>"
        val badVersion = "704b5bd8597be0743b0c99a644b65896"
        f = contextDir.getRealResource("Component." + COMPONENT_EXTENSION)
        if (!f.exists()) createFileFromResourceEL("/resource/context/Component." + COMPONENT_EXTENSION, f) else if (doNew && badVersion.equals(ConfigWebUtil.createMD5FromResource(f))) {
            createFileFromResourceEL("/resource/context/Component." + COMPONENT_EXTENSION, f)
        } else if (doNew && badContent.equals(createContentFromResource(f).trim())) {
            createFileFromResourceEL("/resource/context/Component." + COMPONENT_EXTENSION, f)
        }

        // Component.lucee
        f = contextDir.getRealResource("Component." + COMPONENT_EXTENSION_LUCEE)
        if (!f.exists() || doNew) createFileFromResourceEL("/resource/context/Component." + COMPONENT_EXTENSION_LUCEE, f)
        f = contextDir.getRealResource(Constants.CFML_APPLICATION_EVENT_HANDLER)
        if (!f.exists()) createFileFromResourceEL("/resource/context/Application." + COMPONENT_EXTENSION, f)
        f = contextDir.getRealResource("form." + TEMPLATE_EXTENSION)
        if (!f.exists() || doNew) createFileFromResourceEL("/resource/context/form." + TEMPLATE_EXTENSION, f)
        f = contextDir.getRealResource("graph." + TEMPLATE_EXTENSION)
        if (!f.exists() || doNew) createFileFromResourceEL("/resource/context/graph." + TEMPLATE_EXTENSION, f)
        f = contextDir.getRealResource("wddx." + TEMPLATE_EXTENSION)
        if (!f.exists()) createFileFromResourceEL("/resource/context/wddx." + TEMPLATE_EXTENSION, f)

        // f=new BinaryFile(contextDir,"lucee_context.ra");
        // if(!f.exists())createFileFromResource("/resource/context/lucee_context.ra",f);
        f = contextDir.getRealResource("admin." + TEMPLATE_EXTENSION)
        if (!f.exists()) createFileFromResourceEL("/resource/context/admin." + TEMPLATE_EXTENSION, f)
        val adminDir: Resource = contextDir.getRealResource("admin")
        if (!adminDir.exists()) adminDir.mkdirs()

        // Plugin
        val pluginDir: Resource = adminDir.getRealResource("plugin")
        if (!pluginDir.exists()) pluginDir.mkdirs()
        f = pluginDir.getRealResource("Plugin." + COMPONENT_EXTENSION)
        if (!f.exists()) createFileFromResourceEL("/resource/context/admin/plugin/Plugin." + COMPONENT_EXTENSION, f)

        // Plugin Note
        val note: Resource = pluginDir.getRealResource("Note")
        if (!note.exists()) note.mkdirs()
        f = note.getRealResource("language.xml")
        if (!f.exists()) createFileFromResourceEL("/resource/context/admin/plugin/Note/language.xml", f)
        f = note.getRealResource("overview." + TEMPLATE_EXTENSION)
        if (!f.exists()) createFileFromResourceEL("/resource/context/admin/plugin/Note/overview." + TEMPLATE_EXTENSION, f)
        f = note.getRealResource("Action." + COMPONENT_EXTENSION)
        if (!f.exists()) createFileFromResourceEL("/resource/context/admin/plugin/Note/Action." + COMPONENT_EXTENSION, f)

        // gateway
        val componentsDir: Resource = configDir.getRealResource("components")
        if (!componentsDir.exists()) componentsDir.mkdirs()
        val gwDir: Resource = componentsDir.getRealResource("lucee/extension/gateway/")
        create("/resource/context/gateway/", arrayOf<String?>("TaskGateway." + COMPONENT_EXTENSION, "DummyGateway." + COMPONENT_EXTENSION, "DirectoryWatcher." + COMPONENT_EXTENSION,
                "DirectoryWatcherListener." + COMPONENT_EXTENSION, "WatchService." + COMPONENT_EXTENSION, "MailWatcher." + COMPONENT_EXTENSION,
                "MailWatcherListener." + COMPONENT_EXTENSION, "AsynchronousEvents." + COMPONENT_EXTENSION, "AsynchronousEventsListener." + COMPONENT_EXTENSION),
                gwDir, doNew)

        // resources/language
        val langDir: Resource = adminDir.getRealResource("resources/language")
        create("/resource/context/admin/resources/language/", arrayOf<String?>("en.xml", "de.xml"), langDir, doNew)

        // add Debug
        val debug: Resource = adminDir.getRealResource("debug")
        create("/resource/context/admin/debug/", arrayOf<String?>("Debug." + COMPONENT_EXTENSION, "Field." + COMPONENT_EXTENSION, "Group." + COMPONENT_EXTENSION), debug, doNew)

        // add Cache Drivers
        val cDir: Resource = adminDir.getRealResource("cdriver")
        create("/resource/context/admin/cdriver/", arrayOf<String?>("Cache." + COMPONENT_EXTENSION, "Field." + COMPONENT_EXTENSION, "Group." + COMPONENT_EXTENSION), cDir, doNew)

        // add DB Drivers types
        val dbDir: Resource = adminDir.getRealResource("dbdriver")
        val typesDir: Resource = dbDir.getRealResource("types")
        create("/resource/context/admin/dbdriver/types/", arrayOf<String?>("IDriver." + COMPONENT_EXTENSION, "Driver." + COMPONENT_EXTENSION, "IDatasource." + COMPONENT_EXTENSION,
                "IDriverSelector." + COMPONENT_EXTENSION, "Field." + COMPONENT_EXTENSION), typesDir, doNew)

        // add Gateway Drivers
        val gDir: Resource = adminDir.getRealResource("gdriver")
        create("/resource/context/admin/gdriver/", arrayOf<String?>("Gateway." + COMPONENT_EXTENSION, "Field." + COMPONENT_EXTENSION, "Group." + COMPONENT_EXTENSION), gDir, doNew)

        // add Logging/appender
        val app: Resource = adminDir.getRealResource("logging/appender")
        create("/resource/context/admin/logging/appender/", arrayOf<String?>("Appender." + COMPONENT_EXTENSION, "Field." + COMPONENT_EXTENSION, "Group." + COMPONENT_EXTENSION), app,
                doNew)

        // Logging/layout
        val lay: Resource = adminDir.getRealResource("logging/layout")
        create("/resource/context/admin/logging/layout/", arrayOf<String?>("Layout." + COMPONENT_EXTENSION, "Field." + COMPONENT_EXTENSION, "Group." + COMPONENT_EXTENSION), lay,
                doNew)
        val templatesDir: Resource = contextDir.getRealResource("templates")
        if (!templatesDir.exists()) templatesDir.mkdirs()
        val errorDir: Resource = templatesDir.getRealResource("error")
        if (!errorDir.exists()) errorDir.mkdirs()
        f = errorDir.getRealResource("error." + TEMPLATE_EXTENSION)
        if (!f.exists() || doNew) createFileFromResourceEL("/resource/context/templates/error/error." + TEMPLATE_EXTENSION, f)
        f = errorDir.getRealResource("error-neo." + TEMPLATE_EXTENSION)
        if (!f.exists() || doNew) createFileFromResourceEL("/resource/context/templates/error/error-neo." + TEMPLATE_EXTENSION, f)
        f = errorDir.getRealResource("error-public." + TEMPLATE_EXTENSION)
        if (!f.exists() || doNew) createFileFromResourceEL("/resource/context/templates/error/error-public." + TEMPLATE_EXTENSION, f)
        val displayDir: Resource = templatesDir.getRealResource("display")
        if (!displayDir.exists()) displayDir.mkdirs()
    }

    private fun createContextFilesPost(configDir: Resource?, config: ConfigWebPro?, servletConfig: ServletConfig?, isEventGatewayContext: Boolean, doNew: Boolean) {
        val contextDir: Resource = configDir.getRealResource("context")
        if (!contextDir.exists()) contextDir.mkdirs()
        val adminDir: Resource = contextDir.getRealResource("admin")
        if (!adminDir.exists()) adminDir.mkdirs()

        // Plugin
        val pluginDir: Resource = adminDir.getRealResource("plugin")
        if (!pluginDir.exists()) pluginDir.mkdirs()

        // deploy org.lucee.cfml components
        if (config is ConfigWeb) {
            val _import: ImportDefintion = config!!.getComponentDefaultImport()
            val path: String = _import.getPackageAsPath()
            val components: Resource = config.getConfigDir().getRealResource("components")
            val dir: Resource = components.getRealResource(path)
            dir.mkdirs()
            // print.o(dir);
            ComponentFactory.deploy(dir, doNew)
        }
    }

    private fun doCheckChangesInLibraries(config: ConfigImpl?) {
        // create current hash from libs
        val ctlds: Array<TagLib?> = config!!.getTLDs(CFMLEngine.DIALECT_CFML)
        val ltlds: Array<TagLib?> = config!!.getTLDs(CFMLEngine.DIALECT_LUCEE)
        val cflds: Array<FunctionLib?> = config!!.getFLDs(CFMLEngine.DIALECT_CFML)
        val lflds: Array<FunctionLib?> = config!!.getFLDs(CFMLEngine.DIALECT_LUCEE)
        val sb = StringBuilder()

        // version
        if (config is ConfigWebImpl) {
            val info: Info = (config as ConfigWebImpl?)!!.getFactory().getEngine().getInfo()
            sb.append(info.getVersion().toString()).append(';')
        }

        // charset
        sb.append(config!!.getTemplateCharset().name()).append(';')

        // dot notation upper case
        _getDotNotationUpperCase(sb, config!!.getMappings())
        _getDotNotationUpperCase(sb, config!!.getCustomTagMappings())
        _getDotNotationUpperCase(sb, config!!.getComponentMappings())
        _getDotNotationUpperCase(sb, config!!.getFunctionMappings())
        _getDotNotationUpperCase(sb, config!!.getTagMappings())
        // _getDotNotationUpperCase(sb,config.getServerTagMapping());
        // _getDotNotationUpperCase(sb,config.getServerFunctionMapping());

        // suppress ws before arg
        sb.append(config!!.getSuppressWSBeforeArg())
        sb.append(';')

        // externalize strings
        sb.append(config!!.getExternalizeStringGTE())
        sb.append(';')

        // function output
        sb.append(config!!.getDefaultFunctionOutput())
        sb.append(';')

        // full null support
        // sb.append(config.getFull Null Support()); // no longer a compiler switch
        // sb.append(';');

        // fusiondebug or not (FD uses full path name)
        sb.append(config.allowRequestTimeout())
        sb.append(';')

        // tld
        for (i in ctlds.indices) {
            sb.append(ctlds[i].getHash())
        }
        for (i in ltlds.indices) {
            sb.append(ltlds[i].getHash())
        }
        // fld
        for (i in cflds.indices) {
            sb.append(cflds[i].getHash())
        }
        for (i in lflds.indices) {
            sb.append(lflds[i].getHash())
        }
        if (config is ConfigWeb) {
            var hasChanged = false
            sb.append(";").append((config as ConfigWebImpl?)!!.getConfigServerImpl()!!.getLibHash())
            try {
                val hashValue: String = HashUtil.create64BitHashAsString(sb.toString())
                // check and compare lib version file
                val libHash: Resource = config!!.getConfigDir().getRealResource("lib-hash")
                if (!libHash.exists()) {
                    libHash.createNewFile()
                    IOUtil.write(libHash, hashValue, SystemUtil.getCharset(), false)
                    hasChanged = true
                } else if (!IOUtil.toString(libHash, SystemUtil.getCharset()).equals(hashValue)) {
                    IOUtil.write(libHash, hashValue, SystemUtil.getCharset(), false)
                    hasChanged = true
                }
            } catch (e: IOException) {
            }

            // change Compile type
            if (hasChanged) {
                try {
                    // first we delete the physical classes
                    config!!.getClassDirectory().remove(true)

                    // now we force the pagepools to flush
                    flushPageSourcePool(config!!.getMappings())
                    flushPageSourcePool(config!!.getCustomTagMappings())
                    flushPageSourcePool(config!!.getComponentMappings())
                    flushPageSourcePool(config!!.getFunctionMappings())
                    flushPageSourcePool(config!!.getTagMappings())
                    if (config is ConfigWeb) {
                        flushPageSourcePool((config as ConfigWebImpl?)!!.getApplicationMappings())
                    }
                } catch (e: IOException) {
                    e.printStackTrace(config!!.getErrWriter())
                }
            }
        } else {
            (config as ConfigServerImpl?)!!.setLibHash(HashUtil.create64BitHashAsString(sb.toString()))
        }
    }

    private fun flushPageSourcePool(vararg mappings: Mapping?) {
        for (i in 0 until mappings.size) {
            if (mappings[i] is MappingImpl) (mappings[i] as MappingImpl?).flush() // FUTURE make "flush" part of the interface
        }
    }

    private fun flushPageSourcePool(mappings: Collection<Mapping?>?) {
        val it: Iterator<Mapping?> = mappings!!.iterator()
        var m: Mapping?
        while (it.hasNext()) {
            m = it.next()
            if (m is MappingImpl) (m as MappingImpl?).flush() // FUTURE make "flush" part of the interface
        }
    }

    private fun _getDotNotationUpperCase(sb: StringBuilder?, vararg mappings: Mapping?) {
        for (i in 0 until mappings.size) {
            sb.append((mappings[i] as MappingImpl?).getDotNotationUpperCase()).append(';')
        }
    }

    private fun _getDotNotationUpperCase(sb: StringBuilder?, mappings: Collection<Mapping?>?) {
        val it: Iterator<Mapping?> = mappings!!.iterator()
        var m: Mapping?
        while (it.hasNext()) {
            m = it.next()
            sb.append((m as MappingImpl?).getDotNotationUpperCase()).append(';')
        }
    }

    /**
     * load mappings from XML Document
     *
     * @param configServer
     * @param config
     * @param doc
     * @throws IOException
     */
    @Throws(IOException::class)
    private fun _loadMappings(configServer: ConfigServerImpl?, config: ConfigImpl?, root: Struct?, mode: Int, log: Log?) {
        try {
            val hasAccess: Boolean = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_MAPPING)
            var _mappings: Struct = Caster.toStruct(root.get("mappings", null), null)
            if (_mappings == null) _mappings = Caster.toStruct(root.get("CFMappings", null), null)
            if (_mappings == null) _mappings = ConfigWebUtil.getAsStruct("mappings", root) else {
                root.setEL("mappings", _mappings)
            }

            // alias CFMappings
            val mappings: Map<String?, Mapping?> = MapFactory.< String, Mapping>getConcurrentMap<String?, Mapping?>()
            var tmp: Mapping?
            var finished = false
            if (configServer != null && config is ConfigWeb) {
                val sm: Array<Mapping?> = configServer.getMappings()
                if (sm != null) {
                    for (i in sm.indices) {
                        try {
                            if (!sm[i].isHidden()) {
                                if ("/".equals(sm[i].getVirtual())) finished = true
                                if (sm[i] is MappingImpl) {
                                    tmp = (sm[i] as MappingImpl?).cloneReadOnly(config)
                                    mappings.put(tmp.getVirtualLowerCase(), tmp)
                                } else {
                                    tmp = sm[i]
                                    mappings.put(tmp.getVirtualLowerCase(), tmp)
                                }
                            }
                        } catch (t: Throwable) {
                            ExceptionUtil.rethrowIfNecessary(t)
                            log(config, log, t)
                        }
                    }
                }
            }
            if (hasAccess) {
                var hasServerContext = false
                if (_mappings != null) {
                    val it: Iterator<Entry<Key?, Object?>?> = _mappings.entryIterator()
                    var e: Entry<Key?, Object?>?
                    var el: Struct
                    while (it.hasNext()) {
                        try {
                            e = it.next()
                            el = Caster.toStruct(e.getValue(), null)
                            if (el == null) continue
                            val virtual: String = e.getKey().getString()
                            val physical = getAttr(el, "physical")
                            val archive = getAttr(el, "archive")
                            var listType = getAttr(el, "listenerType")
                            var listMode = getAttr(el, "listenerMode")
                            val readonly = toBoolean(getAttr(el, "readonly"), false)
                            val hidden = toBoolean(getAttr(el, "hidden"), false)
                            var toplevel = toBoolean(getAttr(el, "toplevel"), true)
                            if (config is ConfigServer && (virtual.equalsIgnoreCase("/lucee-server/") || virtual.equalsIgnoreCase("/lucee-server-context/"))) {
                                hasServerContext = true
                            }

                            // lucee
                            if (virtual.equalsIgnoreCase("/lucee/")) {
                                if (StringUtil.isEmpty(listType, true)) listType = "modern"
                                if (StringUtil.isEmpty(listMode, true)) listMode = "curr2root"
                                toplevel = true
                            }
                            var listenerMode: Int = ConfigWebUtil.toListenerMode(listMode, -1)
                            val listenerType: Int = ConfigWebUtil.toListenerType(listType, -1)
                            var listener: ApplicationListener? = ConfigWebUtil.loadListener(listenerType, null)
                            if (listener != null || listenerMode != -1) {
                                // type
                                if (mode == ConfigPro.MODE_STRICT) listener = ModernAppListener() else if (listener == null) listener = ConfigWebUtil.loadListener(ConfigWebUtil.toListenerType(config!!.getApplicationListener().getType(), -1), null)
                                if (listener == null) // this should never be true
                                    listener = ModernAppListener()

                                // mode
                                if (listenerMode == -1) {
                                    listenerMode = config!!.getApplicationListener().getMode()
                                }
                                listener.setMode(listenerMode)
                            }

                            // physical!=null &&
                            if (physical != null || archive != null) {
                                var insTemp = inspectTemplate(el)
                                if ("/lucee/".equalsIgnoreCase(virtual) || "/lucee".equalsIgnoreCase(virtual) || "/lucee-server/".equalsIgnoreCase(virtual)
                                        || "/lucee-server-context".equalsIgnoreCase(virtual)) insTemp = ConfigPro.INSPECT_ONCE
                                val primary = getAttr(el, "primary")
                                val physicalFirst = primary == null || !primary.equalsIgnoreCase("archive")
                                tmp = MappingImpl(config, virtual, physical, archive, insTemp, physicalFirst, hidden, readonly, toplevel, false, false, listener, listenerMode,
                                        listenerType)
                                mappings.put(tmp.getVirtualLowerCase(), tmp)
                                if (virtual.equals("/")) {
                                    finished = true
                                    // break;
                                }
                            }
                        } catch (t: Throwable) {
                            ExceptionUtil.rethrowIfNecessary(t)
                            log(config, log, t)
                        }
                    }
                }

                // set default lucee-server-context
                if (config is ConfigServer && !hasServerContext) {
                    val listener: ApplicationListener = ConfigWebUtil.loadListener(ApplicationListener.TYPE_MODERN, null)
                    listener.setMode(ApplicationListener.MODE_CURRENT2ROOT)
                    tmp = MappingImpl(config, "/lucee-server", "{lucee-server}/context/", null, ConfigPro.INSPECT_ONCE, true, false, true, true, false, false, listener,
                            ApplicationListener.MODE_CURRENT2ROOT, ApplicationListener.TYPE_MODERN)
                    mappings.put(tmp.getVirtualLowerCase(), tmp)
                }
            }
            if (!finished) {
                if (config is ConfigWebImpl && ResourceUtil.isUNCPath(config.getRootDirectory().getPath())) {
                    tmp = MappingImpl(config, "/", config.getRootDirectory().getPath(), null, ConfigPro.INSPECT_UNDEFINED, true, true, true, true, false, false, null, -1, -1)
                } else {
                    tmp = MappingImpl(config, "/", "/", null, ConfigPro.INSPECT_UNDEFINED, true, true, true, true, false, false, null, -1, -1)
                }
                mappings.put("/", tmp)
            }
            val arrMapping: Array<Mapping?> = arrayOfNulls<Mapping?>(mappings.size())
            var index = 0
            val it: Iterator = mappings.keySet().iterator()
            while (it.hasNext()) {
                arrMapping[index++] = mappings[it.next()]
            }
            config!!.setMappings(arrMapping)
            // config.setMappings((Mapping[]) mappings.toArray(new
            // Mapping[mappings.size()]));
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
            log(config, log, t)
        }
    }

    private fun inspectTemplate(data: Struct?): Short {
        var strInsTemp = getAttr(data, "inspectTemplate")
        if (StringUtil.isEmpty(strInsTemp)) strInsTemp = getAttr(data, "inspect")
        if (StringUtil.isEmpty(strInsTemp)) {
            val trusted: Boolean = Caster.toBoolean(getAttr(data, "trusted"), null)
            return if (trusted != null) {
                if (trusted.booleanValue()) ConfigPro.INSPECT_NEVER else ConfigPro.INSPECT_ALWAYS
            } else ConfigPro.INSPECT_UNDEFINED
        }
        return ConfigWebUtil.inspectTemplate(strInsTemp, ConfigPro.INSPECT_UNDEFINED)
    }

    private fun _loadRest(configServer: ConfigServerImpl?, config: ConfigImpl?, root: Struct?, log: Log?) {
        try {
            val hasAccess = true // MUST
            // ConfigWebUtil.hasAccess(config,SecurityManager.TYPE_REST);
            val hasCS = configServer != null
            var el: Struct = ConfigWebUtil.getAsStruct("rest", root)

            // list
            val list: Boolean? = if (el != null) Caster.toBoolean(getAttr(el, "list"), null) else null
            if (list != null) {
                config!!.setRestList(list.booleanValue())
            } else if (hasCS) {
                config!!.setRestList(configServer!!.getRestList())
            }
            val _mappings: Array = ConfigWebUtil.getAsArray("mapping", el)

            // first get mapping defined in server admin (read-only)
            val mappings: Map<String?, lucee.runtime.rest.Mapping?> = HashMap<String?, lucee.runtime.rest.Mapping?>()
            var tmp: lucee.runtime.rest.Mapping?
            if (configServer != null && config is ConfigWeb) {
                val sm: Array<lucee.runtime.rest.Mapping?> = configServer.getRestMappings()
                if (sm != null) {
                    for (i in sm.indices) {
                        try {
                            if (!sm[i]!!.isHidden()) {
                                tmp = sm[i]!!.duplicate(config, Boolean.TRUE)
                                mappings.put(tmp.getVirtual(), tmp)
                            }
                        } catch (t: Throwable) {
                            ExceptionUtil.rethrowIfNecessary(t)
                            log(config, log, t)
                        }
                    }
                }
            }

            // get current mappings
            if (hasAccess && _mappings != null) {
                val it: Iterator<*> = _mappings.getIterator()
                while (it.hasNext()) {
                    try {
                        el = Caster.toStruct(it.next())
                        if (el == null) continue
                        val physical = getAttr(el, "physical")
                        val virtual = getAttr(el, "virtual")
                        val readonly = toBoolean(getAttr(el, "readonly"), false)
                        val hidden = toBoolean(getAttr(el, "hidden"), false)
                        val _default = toBoolean(getAttr(el, "default"), false)
                        if (physical != null) {
                            tmp = Mapping(config, virtual, physical, hidden, readonly, _default)
                            mappings.put(tmp.getVirtual(), tmp)
                        }
                    } catch (t: Throwable) {
                        ExceptionUtil.rethrowIfNecessary(t)
                        log(config, log, t)
                    }
                }
            }
            config!!.setRestMappings(mappings.values().toArray(arrayOfNulls<lucee.runtime.rest.Mapping?>(mappings.size())))
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
            log(config, log, t)
        }
    }

    private fun toAMFEngine(config: Config?, cd: ClassDefinition<AMFEngine?>?, defaultValue: AMFEngine?): AMFEngine? {
        val log: Log = ThreadLocalPageContext.getLog(config, "application")
        try {
            val clazz: Class<AMFEngine?> = cd.getClazz(null)
            if (clazz != null) {
                val obj: Object = ClassUtil.newInstance(clazz)
                if (obj is AMFEngine) return obj as AMFEngine
                log.error("Flex", "object [" + Caster.toClassName(obj).toString() + "] must implement the interface " + AMFEngine::class.java.getName())
            }
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
            log.error("Flex", t)
        }
        return defaultValue
    }

    private fun _loadLoggers(configServer: ConfigServerImpl?, config: ConfigImpl?, root: Struct?, isReload: Boolean) {
        val hasCS = configServer != null
        val existing: Set<String?> = HashSet()
        try {
            val loggers: Struct = ConfigWebUtil.getAsStruct("loggers", root)
            var name: String
            var appenderArgs: String
            var tmp: String
            var layoutArgs: String
            var cdAppender: ClassDefinition?
            var cdLayout: ClassDefinition?
            var level: Int = Log.LEVEL_ERROR
            var readOnly = false
            val itt: Iterator<Entry<Key?, Object?>?> = loggers.entryIterator()
            var entry: Entry<Key?, Object?>?
            var child: Struct
            while (itt.hasNext()) {
                try {
                    entry = itt.next()
                    child = Caster.toStruct(entry.getValue(), null)
                    if (child == null) continue
                    name = entry.getKey().getString()

                    // appender
                    cdAppender = getClassDefinition(child, "appender", config.getIdentification())
                    if (!cdAppender.hasClass()) {
                        tmp = StringUtil.trim(getAttr(child, "appender"), "")
                        cdAppender = config!!.getLogEngine().appenderClassDefintion(tmp)
                    } else if (!cdAppender.isBundle()) {
                        cdAppender = config!!.getLogEngine().appenderClassDefintion(cdAppender.getClassName())
                    }
                    appenderArgs = StringUtil.trim(getAttr(child, "appenderArguments"), "")

                    // layout
                    cdLayout = getClassDefinition(child, "layout", config.getIdentification())
                    if (!cdLayout.hasClass()) {
                        tmp = StringUtil.trim(getAttr(child, "layout"), "")
                        cdLayout = config!!.getLogEngine().layoutClassDefintion(tmp)
                    } else if (!cdLayout.isBundle()) {
                        cdLayout = config!!.getLogEngine().layoutClassDefintion(cdLayout.getClassName())
                    }
                    layoutArgs = StringUtil.trim(getAttr(child, "layoutArguments"), "")
                    var strLevel = getAttr(child, "level")
                    if (StringUtil.isEmpty(strLevel, true)) strLevel = getAttr(child, "logLevel")
                    level = LogUtil.toLevel(StringUtil.trim(strLevel, ""), Log.LEVEL_ERROR)
                    readOnly = Caster.toBooleanValue(getAttr(child, "readOnly"), false)
                    // ignore when no appender/name is defined
                    if (cdAppender.hasClass() && !StringUtil.isEmpty(name)) {
                        val appArgs = cssStringToMap(appenderArgs, true, true)
                        existing.add(name.toLowerCase())
                        if (cdLayout.hasClass()) {
                            val layArgs = cssStringToMap(layoutArgs, true, true)
                            config!!.addLogger(name, level, cdAppender, appArgs, cdLayout, layArgs, readOnly, false)
                        } else config!!.addLogger(name, level, cdAppender, appArgs, null, null, readOnly, false)
                    }
                } catch (t: Throwable) {
                    ExceptionUtil.rethrowIfNecessary(t)
                    log(config, null, t)
                }
            }
            if (hasCS) {
                val it: Iterator<Entry<String?, LoggerAndSourceData?>?> = configServer!!.getLoggers().entrySet().iterator()
                var e: Entry<String?, LoggerAndSourceData?>?
                var data: LoggerAndSourceData
                while (it.hasNext()) {
                    e = it.next()
                    try {
                        // logger only exists in server context
                        if (!existing.contains(e.getKey().toLowerCase())) {
                            data = e.getValue()
                            config!!.addLogger(e.getKey(), data.getLevel(), data.getAppenderClassDefinition(), data.getAppenderArgs(false), data.getLayoutClassDefinition(),
                                    data.getLayoutArgs(false), true, false)
                        }
                    } catch (th: Throwable) {
                        ExceptionUtil.rethrowIfNecessary(th)
                        log(config, null, th)
                    }
                }
            }
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
            log(config, null, t)
        }
    }

    private fun _loadExeLog(configServer: ConfigServerImpl?, config: ConfigImpl?, root: Struct?, log: Log?) {
        try {
            val hasServer = configServer != null
            val el: Struct = ConfigWebUtil.getAsStruct("executionLog", root)

            // enabled
            val bEnabled: Boolean = Caster.toBoolean(getAttr(el, "enabled"), null)
            if (bEnabled == null) {
                if (hasServer) config!!.setExecutionLogEnabled(configServer!!.getExecutionLogEnabled())
            } else config!!.setExecutionLogEnabled(bEnabled.booleanValue())
            var hasChanged = false
            val `val`: String = Caster.toString(config!!.getExecutionLogEnabled())
            try {
                val contextDir: Resource = config!!.getConfigDir()
                val exeLog: Resource = contextDir.getRealResource("exeLog")
                if (!exeLog.exists()) {
                    exeLog.createNewFile()
                    IOUtil.write(exeLog, `val`, SystemUtil.getCharset(), false)
                    hasChanged = true
                } else if (!IOUtil.toString(exeLog, SystemUtil.getCharset()).equals(`val`)) {
                    IOUtil.write(exeLog, `val`, SystemUtil.getCharset(), false)
                    hasChanged = true
                }
            } catch (e: IOException) {
                e.printStackTrace(config!!.getErrWriter())
            }
            if (hasChanged) {
                try {
                    if (config!!.getClassDirectory().exists()) config!!.getClassDirectory().remove(true)
                } catch (e: IOException) {
                    e.printStackTrace(config!!.getErrWriter())
                }
            }

            // class
            val strClass = getAttr(el, "class")
            var clazz: Class?
            if (!StringUtil.isEmpty(strClass)) {
                try {
                    if ("console".equalsIgnoreCase(strClass)) clazz = ConsoleExecutionLog::class.java else if ("debug".equalsIgnoreCase(strClass)) clazz = DebugExecutionLog::class.java else {
                        val cd: ClassDefinition? = if (el != null) getClassDefinition(el, "", config.getIdentification()) else null
                        val c: Class? = if (cd != null) cd.getClazz() else null
                        if (c != null && ClassUtil.newInstance(c) is ExecutionLog) {
                            clazz = c
                        } else {
                            clazz = ConsoleExecutionLog::class.java
                            LogUtil.logGlobal(if (configServer == null) config else configServer, Log.LEVEL_ERROR, ConfigWebFactory::class.java.getName(),
                                    "class [" + strClass + "] must implement the interface " + ExecutionLog::class.java.getName())
                        }
                    }
                } catch (t: Throwable) {
                    ExceptionUtil.rethrowIfNecessary(t)
                    LogUtil.logGlobal(ThreadLocalPageContext.getConfig(if (configServer == null) config else configServer), ConfigWebFactory::class.java.getName(), t)
                    clazz = ConsoleExecutionLog::class.java
                }
                if (clazz != null) LogUtil.logGlobal(ThreadLocalPageContext.getConfig(if (configServer == null) config else configServer), Log.LEVEL_INFO,
                        ConfigWebFactory::class.java.getName(), "loaded ExecutionLog class " + clazz.getName())

                // arguments
                var strArgs = getAttr(el, "arguments")
                if (StringUtil.isEmpty(strArgs)) strArgs = getAttr(el, "classArguments")
                val args = toArguments(strArgs, true)
                config!!.setExecutionLogFactory(ExecutionLogFactory(clazz, args))
            } else {
                if (hasServer) config!!.setExecutionLogFactory(configServer!!.getExecutionLogFactory()) else config!!.setExecutionLogFactory(ExecutionLogFactory(ConsoleExecutionLog::class.java, HashMap<String?, String?>()))
            }
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
            log(config, log, t)
        }
    }

    /**
     * loads datasource settings from XMl DOM
     *
     * @param configServer
     * @param config
     * @param doc
     * @throws BundleException
     * @throws ClassNotFoundException
     */
    private fun _loadDataSources(configServer: ConfigServerImpl?, config: ConfigImpl?, root: Struct?, log: Log?) {
        try {
            // load JDBC Driver definition
            config!!.setJDBCDrivers(_loadJDBCDrivers(configServer, config, root, log))

            // When set to true, makes JDBC use a representation for DATE data that
            // is compatible with the Oracle8i database.
            System.setProperty("oracle.jdbc.V8Compatible", "true")
            val hasCS = configServer != null
            val datasources: Map<String?, DataSource?> = HashMap<String?, DataSource?>()

            // Copy Parent datasources as readOnly
            if (hasCS) {
                val ds: Map<String?, DataSource?> = configServer!!.getDataSourcesAsMap()
                val it: Iterator<Entry<String?, DataSource?>?> = ds.entrySet().iterator()
                var entry: Entry<String?, DataSource?>?
                while (it.hasNext()) {
                    entry = it.next()
                    try {
                        if (!entry.getKey().equals(QOQ_DATASOURCE_NAME)) datasources.put(entry.getKey(), entry.getValue().cloneReadOnly())
                    } catch (t: Throwable) {
                        ExceptionUtil.rethrowIfNecessary(t)
                        log(config, log, t)
                    }
                }
            }

            // Default query of query DB
            try {
                setDatasource(config, datasources, QOQ_DATASOURCE_NAME, ClassDefinitionImpl("org.hsqldb.jdbcDriver", "hsqldb", "1.8.0", config.getIdentification()),
                        "hypersonic-hsqldb", "", -1, "jdbc:hsqldb:.", "sa", "", null, DEFAULT_MAX_CONNECTION, -1, -1, 60000, 0, 0, 0, true, true, DataSource.ALLOW_ALL, false,
                        false, null, StructImpl(), "", ParamSyntax.DEFAULT, false, false, false, false)
            } catch (t: Throwable) {
                ExceptionUtil.rethrowIfNecessary(t)
                log.error("Datasource", t)
            }
            val sm: SecurityManager = config!!.getSecurityManager()
            val access: Short = sm.getAccess(SecurityManager.TYPE_DATASOURCE)
            var accessCount = -1
            if (access == SecurityManager.VALUE_YES) accessCount = -1 else if (access == SecurityManager.VALUE_NO) accessCount = 0 else if (access >= SecurityManager.VALUE_1 && access <= SecurityManager.VALUE_10) {
                accessCount = access - SecurityManager.NUMBER_OFFSET
            }

            // Databases
            // Struct parent = ConfigWebUtil.getAsStruct("dataSources", root);

            // PSQ
            val strPSQ = getAttr(root, "preserveSingleQuote")
            if (access != SecurityManager.VALUE_NO && !StringUtil.isEmpty(strPSQ)) {
                config!!.setPSQL(toBoolean(strPSQ, true))
            } else if (hasCS) config!!.setPSQL(configServer!!.getPSQL())

            // Data Sources
            val dataSources: Struct = ConfigWebUtil.getAsStruct("dataSources", root)
            if (accessCount == -1) accessCount = dataSources.size()
            if (dataSources.size() < accessCount) accessCount = dataSources.size()

            // if(hasAccess) {
            var jdbc: JDBCDriver
            var cd: ClassDefinition?
            var id: String
            val it: Iterator<Entry<Key?, Object?>?> = dataSources.entryIterator()
            var e: Entry<Key?, Object?>?
            var dataSource: Struct
            while (it.hasNext()) {
                e = it.next()
                dataSource = Caster.toStruct(e.getValue(), null)
                if (dataSource == null) continue
                if (dataSource.containsKey("database")) {
                    try {
                        // do we have an id?
                        jdbc = config!!.getJDBCDriverById(getAttr(dataSource, "id"), null)
                        cd = if (jdbc != null && jdbc.cd != null) {
                            jdbc.cd
                        } else getClassDefinition(dataSource, "", config.getIdentification())
                        // we only have a class
                        if (!cd.isBundle()) {
                            jdbc = config!!.getJDBCDriverByClassName(cd.getClassName(), null)
                            if (jdbc != null && jdbc.cd != null && jdbc.cd.isBundle()) cd = jdbc.cd
                        }
                        // still no bundle!
                        if (!cd.isBundle()) cd = patchJDBCClass(config, cd)
                        var idle: Int = Caster.toIntValue(getAttr(dataSource, "idleTimeout"), -1)
                        if (idle == -1) idle = Caster.toIntValue(getAttr(dataSource, "connectionTimeout"), -1)
                        var defLive = 15
                        if (idle > 0) defLive = idle * 5 // for backward compatibility
                        setDatasource(config, datasources, e.getKey().getString(), cd, getAttr(dataSource, "host"), getAttr(dataSource, "database"),
                                Caster.toIntValue(getAttr(dataSource, "port"), -1), getAttr(dataSource, "dsn"), getAttr(dataSource, "username"),
                                ConfigWebUtil.decrypt(getAttr(dataSource, "password")), null, Caster.toIntValue(getAttr(dataSource, "connectionLimit"), DEFAULT_MAX_CONNECTION),
                                idle, Caster.toIntValue(getAttr(dataSource, "liveTimeout"), defLive), Caster.toIntValue(getAttr(dataSource, "minIdle"), 0),
                                Caster.toIntValue(getAttr(dataSource, "maxIdle"), 0), Caster.toIntValue(getAttr(dataSource, "maxTotal"), 0),
                                Caster.toLongValue(getAttr(dataSource, "metaCacheTimeout"), 60000), toBoolean(getAttr(dataSource, "blob"), true),
                                toBoolean(getAttr(dataSource, "clob"), true), Caster.toIntValue(getAttr(dataSource, "allow"), DataSource.ALLOW_ALL),
                                toBoolean(getAttr(dataSource, "validate"), false), toBoolean(getAttr(dataSource, "storage"), false), getAttr(dataSource, "timezone"),
                                toStruct(getAttr(dataSource, "custom")), getAttr(dataSource, "dbdriver"), ParamSyntax.toParamSyntax(dataSource, ParamSyntax.DEFAULT),
                                toBoolean(getAttr(dataSource, "literalTimestampWithTSOffset"), false), toBoolean(getAttr(dataSource, "alwaysSetTimeout"), false),
                                toBoolean(getAttr(dataSource, "requestExclusive"), false), toBoolean(getAttr(dataSource, "alwaysResetConnections"), false)
                        )
                    } catch (th: Throwable) {
                        ExceptionUtil.rethrowIfNecessary(th)
                        log.error("Datasource", th)
                    }
                }
            }
            config!!.setDataSources(datasources)
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
            log(config, log, t)
        }
    }

    private fun patchJDBCClass(config: ConfigImpl?, cd: ClassDefinition?): ClassDefinition? {
        // PATCH for MySQL driver that did change the className within the same extension, JDBC extension
        // expect that the className does not change.
        if ("org.gjt.mm.mysql.Driver".equals(cd.getClassName()) || "com.mysql.jdbc.Driver".equals(cd.getClassName()) || "com.mysql.cj.jdbc.Driver".equals(cd.getClassName())) {
            var jdbc: JDBCDriver = config!!.getJDBCDriverById("mysql", null)
            if (jdbc != null && jdbc.cd != null && jdbc.cd.isBundle()) return jdbc.cd
            jdbc = config!!.getJDBCDriverByClassName("com.mysql.cj.jdbc.Driver", null)
            if (jdbc != null && jdbc.cd != null && jdbc.cd.isBundle()) return jdbc.cd
            jdbc = config!!.getJDBCDriverByClassName("com.mysql.jdbc.Driver", null)
            if (jdbc != null && jdbc.cd != null && jdbc.cd.isBundle()) return jdbc.cd
            jdbc = config!!.getJDBCDriverByClassName("org.gjt.mm.mysql.Driver", null)
            if (jdbc != null && jdbc.cd != null && jdbc.cd.isBundle()) return jdbc.cd
            var tmp: ClassDefinitionImpl? = ClassDefinitionImpl("com.mysql.cj.jdbc.Driver", "com.mysql.cj", null, config.getIdentification())
            if (tmp.getClazz(null) != null) return tmp
            tmp = ClassDefinitionImpl("com.mysql.jdbc.Driver", "com.mysql.jdbc", null, config.getIdentification())
            if (tmp.getClazz(null) != null) return tmp
        }
        if ("com.microsoft.jdbc.sqlserver.SQLServerDriver".equals(cd.getClassName())) {
            var jdbc: JDBCDriver = config!!.getJDBCDriverById("mssql", null)
            if (jdbc != null && jdbc.cd != null && jdbc.cd.isBundle()) return jdbc.cd
            jdbc = config!!.getJDBCDriverByClassName("com.microsoft.sqlserver.jdbc.SQLServerDriver", null)
            if (jdbc != null && jdbc.cd != null && jdbc.cd.isBundle()) return jdbc.cd
            val tmp = ClassDefinitionImpl("com.microsoft.sqlserver.jdbc.SQLServerDriver", cd.getName(), cd.getVersionAsString(), config.getIdentification())
            if (tmp.getClazz(null) != null) return tmp
        }
        return cd
    }

    fun _loadJDBCDrivers(configServer: ConfigServerImpl?, config: ConfigImpl?, root: Struct?, log: Log?): Array<JDBCDriver?>? {
        val map: Map<String?, JDBCDriver?> = HashMap<String?, JDBCDriver?>()
        try {
            // first add the server drivers, so they can be overwritten
            if (configServer != null) {
                val sds: Array<JDBCDriver?> = configServer.getJDBCDrivers()
                if (sds != null) {
                    for (sd in sds) {
                        try {
                            map.put(sd.cd.toString(), sd)
                        } catch (t: Throwable) {
                            ExceptionUtil.rethrowIfNecessary(t)
                            log(config, log, t)
                        }
                    }
                }
            }

            // jdbcDrivers
            val jdbcDrivers: Struct = ConfigWebUtil.getAsStruct("jdbcDrivers", root)
            val it: Iterator<Entry<Key?, Object?>?> = jdbcDrivers.entryIterator()
            var e: Entry<Key?, Object?>?
            var cd: ClassDefinition?
            val label: String?
            val id: String?
            val connStr: String?
            while (it.hasNext()) {
                try {
                    e = it.next()
                    val driver: Struct = Caster.toStruct(e.getValue(), null) ?: continue

                    // class definition
                    driver.setEL(KeyConstants._class, e.getKey().getString())
                    cd = getClassDefinition(driver, "", config.getIdentification())
                    if (StringUtil.isEmpty(cd.getClassName()) && !StringUtil.isEmpty(cd.getName())) {
                        try {
                            val bundle: Bundle = OSGiUtil.loadBundle(cd.getName(), cd.getVersion(), config.getIdentification(), null, false)
                            val cn: String = JDBCDriver.extractClassName(bundle)
                            cd = ClassDefinitionImpl(config.getIdentification(), cn, cd.getName(), cd.getVersion())
                        } catch (t: Throwable) {
                            ExceptionUtil.rethrowIfNecessary(t)
                        }
                    }
                    label = getAttr(driver, "label")
                    id = getAttr(driver, "id")
                    connStr = getAttr(driver, "connectionString")
                    // check if label exists
                    if (StringUtil.isEmpty(label)) {
                        if (log != null) log.error("Datasource", "missing label for jdbc driver [" + cd.getClassName().toString() + "]")
                        continue
                    }

                    // check if it is a bundle
                    if (!cd.isBundle()) {
                        if (log != null) log.error("Datasource", "jdbc driver [$label] does not describe a bundle")
                        continue
                    }
                    map.put(cd.toString(), JDBCDriver(label, id, connStr, cd))
                } catch (t: Throwable) {
                    ExceptionUtil.rethrowIfNecessary(t)
                    log(config, log, t)
                }
            }
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
            log(config, log, t)
        }
        return map.values().toArray(arrayOfNulls<JDBCDriver?>(map.size()))
    }

    private fun _loadCache(configServer: ConfigServerImpl?, config: ConfigImpl?, root: Struct?, log: Log?) {
        try {
            val hasCS = configServer != null

            // load cache defintions
            run {
                val map: Map<String?, ClassDefinition?> = HashMap<String?, ClassDefinition?>()

                // first add the server drivers, so they can be overwritten
                if (configServer != null) {
                    val cds: Map<String?, ClassDefinition?> = configServer.getCacheDefinitions()
                    if (cds != null) {
                        val values: Collection<ClassDefinition?> = cds.values()
                        if (values != null) {
                            val it: Iterator<ClassDefinition?> = values.iterator()
                            var cd: ClassDefinition?
                            while (it.hasNext()) {
                                cd = it.next()
                                map.put(cd.getClassName(), cd)
                            }
                        }
                    }
                }
                var cd: ClassDefinition?
                val caches: Array = ConfigWebUtil.getAsArray("cacheClasses", root)
                if (caches != null) {
                    val it: Iterator<*> = caches.getIterator()
                    var cache: Struct
                    while (it.hasNext()) {
                        try {
                            cache = Caster.toStruct(it.next())
                            if (cache == null) continue
                            cd = getClassDefinition(cache, "", config.getIdentification())

                            // check if it is a bundle
                            if (!cd.isBundle()) {
                                log.error("Cache", "[$cd] does not have bundle info")
                                continue
                            }
                            map.put(cd.getClassName(), cd)
                        } catch (t: Throwable) {
                            ExceptionUtil.rethrowIfNecessary(t)
                            log(config, log, t)
                        }
                    }
                }
                config!!.setCacheDefinitions(map)
            }
            val caches: Map<String?, CacheConnection?> = HashMap<String?, CacheConnection?>()
            val hasAccess: Boolean = ConfigWebUtil.hasAccess(config, SecurityManagerImpl.TYPE_CACHE)

            // default cache
            for (i in CACHE_TYPES_MAX.indices) {
                try {
                    val def = getAttr(root, "default" + StringUtil.ucFirst(STRING_CACHE_TYPES_MAX!![i]))
                    if (hasAccess && !StringUtil.isEmpty(def)) {
                        config!!.setCacheDefaultConnectionName(CACHE_TYPES_MAX!![i], def)
                    } else if (hasCS) {
                        if (root.containsKey("default" + StringUtil.ucFirst(STRING_CACHE_TYPES_MAX[i]))) config!!.setCacheDefaultConnectionName(CACHE_TYPES_MAX!![i], "") else config!!.setCacheDefaultConnectionName(CACHE_TYPES_MAX!![i], configServer!!.getCacheDefaultConnectionName(CACHE_TYPES_MAX[i]))
                    } else config!!.setCacheDefaultConnectionName(+CACHE_TYPES_MAX!![i], "")
                } catch (t: Throwable) {
                    ExceptionUtil.rethrowIfNecessary(t)
                    log(config, log, t)
                }
            }
            run {
                val eCaches: Struct = ConfigWebUtil.getAsStruct("caches", root)

                // check if we have an update or not
                val sb = StringBuilder()
                for (e in config!!.getCacheDefinitions().entrySet()) {
                    sb.append(e.getKey()).append(':').append(e.getValue().toString()).append(';')
                }
                val md5 = if (eCaches != null) getMD5(eCaches, sb.toString(), if (hasCS) configServer!!.getCacheMD5() else "") else ""
                if (md5!!.equals(config!!.getCacheMD5())) {
                    return
                }
                config!!.setCacheMD5(md5)
            }

            // cache connections
            val conns: Struct = ConfigWebUtil.getAsStruct("caches", root)

            // if(hasAccess) {
            var cd: ClassDefinition?
            var name: Key
            var cc: CacheConnection?
            // Class cacheClazz;
            // caches
            if (hasAccess) {
                val it: Iterator<Entry<Key?, Object?>?> = conns.entryIterator()
                var entry: Entry<Key?, Object?>?
                var data: Struct
                while (it.hasNext()) {
                    try {
                        entry = it.next()
                        name = entry.getKey()
                        data = Caster.toStruct(entry.getValue(), null)
                        cd = getClassDefinition(data, "", config.getIdentification())
                        if (!cd.isBundle()) {
                            val _cd: ClassDefinition = config!!.getCacheDefinition(cd.getClassName())
                            if (_cd != null) cd = _cd
                        }
                        run {
                            val custom: Struct? = toStruct(getAttr(data, "custom"))

                            // Workaround for old EHCache class definitions
                            if (cd.getClassName() != null && cd.getClassName().endsWith(".EHCacheLite")) {
                                cd = ClassDefinitionImpl("org.lucee.extension.cache.eh.EHCache")
                                if (!custom.containsKey("distributed")) custom.setEL("distributed", "off")
                                if (!custom.containsKey("asynchronousReplicationIntervalMillis")) custom.setEL("asynchronousReplicationIntervalMillis", "1000")
                                if (!custom.containsKey("maximumChunkSizeBytes")) custom.setEL("maximumChunkSizeBytes", "5000000")
                            } //
                            else if (cd.getClassName() != null
                                    && (cd.getClassName().endsWith(".extension.io.cache.eh.EHCache") || cd.getClassName().endsWith("lucee.runtime.cache.eh.EHCache"))) {
                                cd = ClassDefinitionImpl("org.lucee.extension.cache.eh.EHCache")
                            }
                            cc = CacheConnectionImpl(config, name.getString(), cd, custom, Caster.toBooleanValue(getAttr(data, "readOnly"), false),
                                    Caster.toBooleanValue(getAttr(data, "storage"), false))
                            if (!StringUtil.isEmpty(name)) {
                                caches.put(name.getLowerString(), cc)
                            } else LogUtil.logGlobal(ThreadLocalPageContext.getConfig(if (configServer == null) config else configServer), Log.LEVEL_ERROR,
                                    ConfigWebFactory::class.java.getName(), "missing cache name")
                        }
                    } catch (t: Throwable) {
                        ExceptionUtil.rethrowIfNecessary(t)
                        log(config, log, t)
                    }
                }
            }
            // }

            // call static init once per driver
            run {

                // group by classes
                val _caches: Map<ClassDefinition?, List<CacheConnection?>?> = HashMap<ClassDefinition?, List<CacheConnection?>?>()
                {
                    val it: Iterator<Entry<String?, CacheConnection?>?> = caches.entrySet().iterator()
                    var entry: Entry<String?, CacheConnection?>?
                    var list: List<CacheConnection?>?
                    while (it.hasNext()) {
                        try {
                            entry = it.next()
                            cc = entry.getValue()
                            if (cc == null) continue  // Jira 3196 ?!
                            list = _caches.get(cc.getClassDefinition())
                            if (list == null) {
                                list = ArrayList<CacheConnection?>()
                                _caches.put(cc.getClassDefinition(), list)
                            }
                            list.add(cc)
                        } catch (t: Throwable) {
                            ExceptionUtil.rethrowIfNecessary(t)
                            log(config, log, t)
                        }
                    }
                }
                // call
                val it: Iterator<Entry<ClassDefinition?, List<CacheConnection?>?>?> = _caches.entrySet().iterator()
                var entry: Entry<ClassDefinition?, List<CacheConnection?>?>?
                var list: List<CacheConnection?>
                var _cd: ClassDefinition
                while (it.hasNext()) {
                    entry = it.next()
                    list = entry.getValue()
                    _cd = entry.getKey()
                    try {
                        val m: Method = _cd.getClazz().getMethod("init", arrayOf<Class?>(Config::class.java, Array<String>::class.java, Array<Struct>::class.java))
                        if (Modifier.isStatic(m.getModifiers())) m.invoke(null, arrayOf<Object?>(config, _toCacheNames(list), _toArguments(list))) else LogUtil.logGlobal(ThreadLocalPageContext.getConfig(if (configServer == null) config else configServer), Log.LEVEL_ERROR, ConfigWebFactory::class.java.getName(), "method [init(Config,String[],Struct[]):void] for class [" + _cd.toString().toString() + "] is not static")
                    } catch (e: InvocationTargetException) {
                        log.error("Cache", e.getTargetException())
                    } catch (e: RuntimeException) {
                        log.error("Cache", e)
                    } catch (e: NoSuchMethodException) {
                        log.error("Cache", "missing method [public static init(Config,String[],Struct[]):void] for class [" + _cd.toString().toString() + "] ")
                    } catch (e: Throwable) {
                        ExceptionUtil.rethrowIfNecessary(e)
                        log.error("Cache", e)
                    }
                }
            }

            // Copy Parent caches as readOnly
            if (hasCS) {
                val ds: Map<String?, CacheConnection?> = configServer!!.getCacheConnections()
                val it: Iterator<Entry<String?, CacheConnection?>?> = ds.entrySet().iterator()
                var entry: Entry<String?, CacheConnection?>?
                while (it.hasNext()) {
                    try {
                        entry = it.next()
                        cc = entry.getValue()
                        if (!caches.containsKey(entry.getKey())) caches.put(entry.getKey(), ServerCacheConnection(configServer, cc))
                    } catch (t: Throwable) {
                        ExceptionUtil.rethrowIfNecessary(t)
                        log(config, log, t)
                    }
                }
            }
            config!!.setCaches(caches)
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
            log(config, log, t)
        }
    }

    private fun getMD5(data: Struct?, cacheDef: String?, parentMD5: String?): String? {
        return try {
            MD5.getDigestAsString(StringBuilder().append(data.toString()).append(':').append(cacheDef).append(':').append(parentMD5).toString())
        } catch (e: IOException) {
            ""
        }
    }

    private fun _loadGatewayEL(configServer: ConfigServerImpl?, config: ConfigImpl?, root: Struct?, log: Log?) {
        try {
            _loadGateway(configServer, config, root, log)
        } catch (e: Exception) {
            log(config, log, e)
        }
    }

    private fun _loadGateway(configServer: ConfigServerImpl?, config: ConfigImpl?, root: Struct?, log: Log?) {
        val hasCS = configServer != null
        // MUSST
        val engine: GatewayEngineImpl? = if (hasCS) (config as ConfigWebPro?)!!.getGatewayEngine() as GatewayEngineImpl else null
        val mapGateways: Map<String?, GatewayEntry?> = HashMap<String?, GatewayEntry?>()

        // get from server context
        if (hasCS) {
            val entries: Map<String?, GatewayEntry?> = configServer!!.getGatewayEntries()
            if (entries != null && !entries.isEmpty()) {
                val it: Iterator<Entry<String?, GatewayEntry?>?> = entries.entrySet().iterator()
                var e: Entry<String?, GatewayEntry?>?
                while (it.hasNext()) {
                    try {
                        e = it.next()
                        mapGateways.put(e.getKey(), (e.getValue() as GatewayEntryImpl).duplicateReadOnly(engine))
                    } catch (th: Throwable) {
                        ExceptionUtil.rethrowIfNecessary(th)
                        log(config, log, th)
                    }
                }
            }
        }
        val hasAccess: Boolean = ConfigWebUtil.hasAccess(config, SecurityManagerImpl.TYPE_GATEWAY)
        var ge: GatewayEntry?
        // cache connections
        val gateways: Struct = ConfigWebUtil.getAsStruct("gateways", root)

        // if(hasAccess) {
        var id: String
        // engine.reset();

        // caches
        if (hasAccess) {
            try {
                val it: Iterator<Entry<Key?, Object?>?> = gateways.entryIterator()
                var e: Entry<Key?, Object?>?
                var eConnection: Struct
                while (it.hasNext()) {
                    try {
                        e = it.next()
                        eConnection = Caster.toStruct(e.getValue(), null)
                        if (eConnection == null) continue
                        id = e.getKey().getLowerString()
                        ge = GatewayEntryImpl(engine, id, getClassDefinition(eConnection, "", config.getIdentification()), getAttr(eConnection, "cfcPath"),
                                getAttr(eConnection, "listenerCFCPath"), getAttr(eConnection, "startupMode"), toStruct(getAttr(eConnection, "custom")),
                                Caster.toBooleanValue(getAttr(eConnection, "readOnly"), false))
                        if (!StringUtil.isEmpty(id)) {
                            mapGateways.put(id.toLowerCase(), ge)
                        } else {
                            LogUtil.logGlobal(ThreadLocalPageContext.getConfig(if (configServer == null) config else configServer), Log.LEVEL_ERROR, ConfigWebFactory::class.java.getName(),
                                    "missing id")
                        }
                    } catch (t: Throwable) {
                        ExceptionUtil.rethrowIfNecessary(t)
                        log(config, log, t)
                    }
                }
            } catch (t: Throwable) {
                ExceptionUtil.rethrowIfNecessary(t)
                log(config, log, t)
            }
        }
    }

    private fun _toArguments(list: List<CacheConnection?>?): Array<Struct?>? {
        val it: Iterator<CacheConnection?> = list!!.iterator()
        val args: Array<Struct?> = arrayOfNulls<Struct?>(list.size())
        var index = 0
        while (it.hasNext()) {
            args[index++] = it.next().getCustom()
        }
        return args
    }

    private fun _toCacheNames(list: List<CacheConnection?>?): Array<String?>? {
        val it: Iterator<CacheConnection?> = list!!.iterator()
        val names = arrayOfNulls<String?>(list.size())
        var index = 0
        while (it.hasNext()) {
            names[index++] = it.next().getName()
        }
        return names
    }

    private fun toStruct(str: String?): Struct? {
        val sct: Struct = StructImpl()
        try {
            val arr: Array<String?> = ListUtil.toStringArray(ListUtil.listToArrayRemoveEmpty(str, '&'))
            var item: Array<String?>
            for (i in arr.indices) {
                item = ListUtil.toStringArray(ListUtil.listToArrayRemoveEmpty(arr[i], '='))
                if (item.size == 2) sct.setEL(KeyImpl.init(URLDecoder.decode(item[0], true).trim()), URLDecoder.decode(item[1], true)) else if (item.size == 1) sct.setEL(KeyImpl.init(URLDecoder.decode(item[0], true).trim()), "")
            }
        } catch (ee: PageException) {
        }
        return sct
    }

    @Throws(BundleException::class, ClassException::class, SQLException::class)
    private fun setDatasource(config: ConfigImpl?, datasources: Map<String?, DataSource?>?, datasourceName: String?, cd: ClassDefinition?, server: String?, databasename: String?,
                              port: Int, dsn: String?, user: String?, pass: String?, listener: TagListener?, connectionLimit: Int, idleTimeout: Int, liveTimeout: Int, minIdle: Int, maxIdle: Int, maxTotal: Int,
                              metaCacheTimeout: Long, blob: Boolean, clob: Boolean, allow: Int, validate: Boolean, storage: Boolean, timezone: String?, custom: Struct?, dbdriver: String?, ps: ParamSyntax?,
                              literalTimestampWithTSOffset: Boolean, alwaysSetTimeout: Boolean, requestExclusive: Boolean, alwaysResetConnections: Boolean) {
        datasources.put(datasourceName.toLowerCase(),
                DataSourceImpl(config, datasourceName, cd, server, dsn, databasename, port, user, pass, listener, connectionLimit, idleTimeout, liveTimeout, minIdle, maxIdle,
                        maxTotal, metaCacheTimeout, blob, clob, allow, custom, false, validate, storage,
                        if (StringUtil.isEmpty(timezone, true)) null else TimeZoneUtil.toTimeZone(timezone, null), dbdriver, ps, literalTimestampWithTSOffset, alwaysSetTimeout,
                        requestExclusive, alwaysResetConnections, ThreadLocalPageContext.getLog(config, "application")))
    }

    /**
     * @param configServer
     * @param config
     * @param doc
     * @throws IOException
     */
    private fun _loadCustomTagsMappings(configServer: ConfigServerImpl?, config: ConfigImpl?, root: Struct?, mode: Int, log: Log?) {
        try {
            val hasAccess: Boolean = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_CUSTOM_TAG)
            val hasCS = configServer != null

            // do patch cache
            val strDoPathcache = getAttr(root, "customTagUseCachePath")
            if (hasAccess && !StringUtil.isEmpty(strDoPathcache, true)) {
                config!!.setUseCTPathCache(Caster.toBooleanValue(strDoPathcache.trim(), true))
            } else if (hasCS) {
                config!!.setUseCTPathCache(configServer!!.useCTPathCache())
            }

            // do custom tag local search
            if (mode == ConfigPro.MODE_STRICT) {
                config!!.setDoLocalCustomTag(false)
            } else {
                val strDoCTLocalSearch = getAttr(root, "customTagLocalSearch")
                if (hasAccess && !StringUtil.isEmpty(strDoCTLocalSearch)) {
                    config!!.setDoLocalCustomTag(Caster.toBooleanValue(strDoCTLocalSearch.trim(), true))
                } else if (hasCS) {
                    config!!.setDoLocalCustomTag(configServer!!.doLocalCustomTag())
                }
            }

            // do custom tag deep search
            if (mode == ConfigPro.MODE_STRICT) {
                config!!.setDoCustomTagDeepSearch(false)
            } else {
                val strDoCTDeepSearch = getAttr(root, "customTagDeepSearch")
                if (hasAccess && !StringUtil.isEmpty(strDoCTDeepSearch)) {
                    config!!.setDoCustomTagDeepSearch(Caster.toBooleanValue(strDoCTDeepSearch.trim(), false))
                } else if (hasCS) {
                    config!!.setDoCustomTagDeepSearch(configServer!!.doCustomTagDeepSearch())
                }
            }

            // extensions
            if (mode == ConfigPro.MODE_STRICT) {
                config!!.setCustomTagExtensions(Constants.getComponentExtensions())
            } else {
                val strExtensions = getAttr(root, "customTagExtensions")
                if (hasAccess && !StringUtil.isEmpty(strExtensions)) {
                    try {
                        val arr: Array<String?> = ListUtil.toStringArray(ListUtil.listToArrayRemoveEmpty(strExtensions, ","))
                        config!!.setCustomTagExtensions(ListUtil.trimItems(arr))
                    } catch (e: PageException) {
                    }
                } else if (hasCS) {
                    config!!.setCustomTagExtensions(configServer!!.getCustomTagExtensions())
                }
            }

            // Struct customTag = ConfigWebUtil.getAsStruct("customTag", root);
            val ctMappings: Array = ConfigWebUtil.getAsArray("customTagMappings", root)

            // Web Mapping
            var hasSet = false
            var mappings: Array<Mapping?>? = null
            if (hasAccess && ctMappings.size() > 0) {
                val it: Iterator<Object?> = ctMappings.valueIterator()
                val list: List<Mapping?> = ArrayList()
                var ctMapping: Struct
                while (it.hasNext()) {
                    try {
                        ctMapping = Caster.toStruct(it.next(), null)
                        if (ctMapping == null) continue
                        val virtual: String = createVirtual(ctMapping)
                        val physical = getAttr(ctMapping, "physical")
                        val archive = getAttr(ctMapping, "archive")
                        val readonly = toBoolean(getAttr(ctMapping, "readonly"), false)
                        val hidden = toBoolean(getAttr(ctMapping, "hidden"), false)
                        val inspTemp = inspectTemplate(ctMapping)
                        val primary = getAttr(ctMapping, "primary")
                        val physicalFirst = archive == null || !primary.equalsIgnoreCase("archive")
                        hasSet = true
                        list.add(MappingImpl(config, virtual, physical, archive, inspTemp, physicalFirst, hidden, readonly, true, false, true, null, -1, -1))
                    } catch (t: Throwable) {
                        ExceptionUtil.rethrowIfNecessary(t)
                        log(config, log, t)
                    }
                }
                mappings = list.toArray(arrayOfNulls<Mapping?>(list.size()))
                config!!.setCustomTagMappings(mappings)
            }

            // Server Mapping
            if (hasCS) {
                var originals: Array<Mapping?>? = configServer!!.getCustomTagMappings()
                if (originals == null) originals = arrayOfNulls<Mapping?>(0)
                var clones: Array<Mapping?>? = arrayOfNulls<Mapping?>(originals!!.size)
                val map = LinkedHashMap()
                var m: Mapping?
                for (i in clones.indices) {
                    try {
                        m = (originals!![i] as MappingImpl?).cloneReadOnly(config)
                        map.put(toKey(m), m)
                    } catch (t: Throwable) {
                        ExceptionUtil.rethrowIfNecessary(t)
                        log(config, log, t)
                    }
                }
                if (mappings != null) {
                    for (i in mappings.indices) {
                        m = mappings[i]
                        map.put(toKey(m), m)
                    }
                }
                if (originals!!.size > 0) {
                    clones = arrayOfNulls<Mapping?>(map.size())
                    val it: Iterator = map.entrySet().iterator()
                    var entry: Map.Entry
                    var index = 0
                    while (it.hasNext()) {
                        try {
                            entry = it.next() as Entry
                            clones!![index++] = entry.getValue() as Mapping
                        } catch (t: Throwable) {
                            ExceptionUtil.rethrowIfNecessary(t)
                            log(config, log, t)
                        }
                    }
                    hasSet = true
                    // print.err("set:"+clones.length);
                    config!!.setCustomTagMappings(clones)
                }
            }
            if (!hasSet) {
                // MappingImpl m=new
                // MappingImpl(config,"/default-customtags/","{lucee-web}/customtags/",null,false,true,false,false,true,false,true);
                // config.setCustomTagMappings(new
                // Mapping[]{m.cloneReadOnly(config)});
            }
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
            log(config, log, t)
        }
    }

    private fun toKey(m: Mapping?): Object? {
        return if (!StringUtil.isEmpty(m.getStrPhysical(), true)) m.getVirtual().toString() + ":" + m.getStrPhysical().toLowerCase().trim() else (m.getVirtual().toString() + ":" + m.getStrPhysical() + ":" + m.getStrArchive()).toLowerCase()
    }

    /**
     * @param configServer
     * @param config
     * @param doc
     * @param serverPW
     * @throws IOException
     * @throws NoSuchAlgorithmException
     */
    private fun _loadConfig(configServer: ConfigServerImpl?, config: ConfigImpl?, root: Struct?) {
        var salt: String? = null
        var pw: Password? = null
        salt = getAttr(root, "salt")
        if (StringUtil.isEmpty(salt, true)) salt = getAttr(root, "adminSalt")
        // salt (every context need to have a salt)
        if (StringUtil.isEmpty(salt, true)) throw RuntimeException("context is invalid, there is no salt!")
        config!!.setSalt(salt.trim().also { salt = it })
        // password
        pw = PasswordImpl.readFromStruct(root, salt, false)
        if (pw != null) {
            config!!.setPassword(pw)
            if (config is ConfigWebImpl) (config as ConfigWebImpl?)!!.setPasswordSource(ConfigWebImpl.PASSWORD_ORIGIN_WEB)
        } else if (configServer != null) {
            (config as ConfigWebImpl?).setPasswordSource(if (configServer.hasCustomDefaultPassword()) ConfigWebImpl.PASSWORD_ORIGIN_DEFAULT else ConfigWebImpl.PASSWORD_ORIGIN_SERVER)
            if (configServer.getDefaultPassword() != null) config!!.setPassword(configServer.getDefaultPassword())
        }
        if (config is ConfigServerImpl) {
            val keyList = getAttr(root, "authKeys")
            if (!StringUtil.isEmpty(keyList)) {
                val keys: Array<String?> = ListUtil.trimItems(ListUtil.toStringArray(ListUtil.toListRemoveEmpty(keyList, ',')))
                for (i in keys.indices) {
                    try {
                        keys[i] = URLDecoder.decode(keys[i], "UTF-8", true)
                    } catch (e: UnsupportedEncodingException) {
                    }
                }
                config!!.setAuthenticationKeys(keys)
            }
        }

        // default password
        if (config is ConfigServerImpl) {
            pw = PasswordImpl.readFromStruct(root, salt, true)
            if (pw != null) (config as ConfigServerImpl?)!!.setDefaultPassword(pw)
        }

        // mode
        var mode = getAttr(root, "mode")
        if (!StringUtil.isEmpty(mode, true)) {
            mode = mode.trim()
            if ("custom".equalsIgnoreCase(mode)) config!!.setMode(ConfigPro.MODE_CUSTOM)
            if ("strict".equalsIgnoreCase(mode)) config!!.setMode(ConfigPro.MODE_STRICT)
        } else if (configServer != null) {
            config!!.setMode(configServer.getMode())
        }

        // check config file for changes
        val cFc = getAttr(root, "checkForChanges")
        if (!StringUtil.isEmpty(cFc, true)) {
            config!!.setCheckForChangesInConfigFile(Caster.toBooleanValue(cFc.trim(), false))
        } else if (configServer != null) {
            config!!.setCheckForChangesInConfigFile(configServer.checkForChangesInConfigFile())
        }
    }

    private fun _loadTag(configServer: ConfigServerImpl?, config: ConfigImpl?, root: Struct?, log: Log?) {
        try {
            run {
                val tags: Array = ConfigWebUtil.getAsArray("tags", root)
                var tag: Struct
                var cd: ClassDefinition?
                var nss: String?
                var ns: String?
                var n: String?
                if (tags != null) {
                    val it: Iterator<*> = tags.getIterator()
                    while (it.hasNext()) {
                        try {
                            tag = Caster.toStruct(it.next(), null)
                            if (tag == null) continue
                            ns = getAttr(tag, "namespace")
                            nss = getAttr(tag, "namespaceSeperator")
                            n = getAttr(tag, "name")
                            cd = getClassDefinition(tag, "", config.getIdentification())
                            config!!.addTag(ns, nss, n, CFMLEngine.DIALECT_BOTH, cd)
                        } catch (t: Throwable) {
                            ExceptionUtil.rethrowIfNecessary(t)
                            log(config, log, t)
                        }
                    }
                }
            }

            // set tag default values
            val defaults: Array = ConfigWebUtil.getAsArray("tagDefaults", root)
            if (defaults.size() > 0) {
                var def: Struct
                var tagName: String?
                var attrName: String?
                var attrValue: String?
                val tags: Struct = StructImpl()
                var tag: Struct?
                val it: Iterator<*> = defaults.getIterator()
                val trg: Map<Key?, Map<Key?, Object?>?> = HashMap<Key?, Map<Key?, Object?>?>()
                while (it.hasNext()) {
                    try {
                        def = Caster.toStruct(it.next(), null)
                        if (def == null) continue
                        tagName = getAttr(def, "tag")
                        attrName = getAttr(def, "attributeName")
                        attrValue = getAttr(def, "attributeValue")
                        if (StringUtil.isEmpty(tagName) || StringUtil.isEmpty(attrName) || StringUtil.isEmpty(attrValue)) continue
                        tag = tags.get(tagName, null) as Struct
                        if (tag == null) {
                            tag = StructImpl()
                            tags.setEL(tagName, tag)
                        }
                    } catch (t: Throwable) {
                        ExceptionUtil.rethrowIfNecessary(t)
                        log(config, log, t)
                    }
                }

                // initTagDefaultAttributeValues
            }
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
            log(config, log, t)
        }
    }

    private fun _loadTempDirectory(configServer: ConfigServerImpl?, config: ConfigImpl?, root: Struct?, isReload: Boolean, log: Log?) {
        try {
            if (configServer != null && root == null) {
                config.setTempDirectory(configServer.getTempDirectory(), !isReload)
                return
            }
            val configDir: Resource = config!!.getConfigDir()
            val hasCS = configServer != null
            val strTempDirectory: String = ConfigWebUtil.translateOldPath(getAttr(root, "tempDirectory"))
            var cst: Resource? = null
            // Temp Dir
            if (!StringUtil.isEmpty(strTempDirectory)) cst = ConfigWebUtil.getFile(configDir, strTempDirectory, null, configDir, FileUtil.TYPE_DIR, config)
            if (cst == null && hasCS) cst = configServer!!.getTempDirectory()
            if (cst == null) cst = ConfigWebUtil.getFile(configDir, "temp", null, configDir, FileUtil.TYPE_DIR, config)
            config.setTempDirectory(cst, !isReload)
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
            log(config, log, t)
        }
    }

    /**
     * @param configServer
     * @param config
     * @param doc
     * @throws ExpressionException
     * @throws TagLibException
     * @throws FunctionLibException
     */
    private fun _loadFilesystem(configServer: ConfigServerImpl?, config: ConfigImpl?, root: Struct?, doNew: Boolean, log: Log?) {
        try {
            if (configServer != null) {
                val src: Resource = configServer.getConfigDir().getRealResource("distribution")
                val trg: Resource = config!!.getConfigDir().getRealResource("context/")
                copyContextFiles(src, trg)
            }
            val configDir: Resource = config!!.getConfigDir()
            val hasCS = configServer != null
            var strAllowRealPath: String? = null
            var strDeployDirectory: String? = null
            // String strTempDirectory=null;

            // system.property or env var
            var strDefaultFLDDirectory: String? = null
            var strDefaultTLDDirectory: String? = null
            var strDefaultFuncDirectory: String? = null
            var strDefaultTagDirectory: String? = null
            var strFuncDirectory: String? = null
            var strTagDirectory: String? = null

            // only read in server context
            if (!hasCS) {
                strDefaultFLDDirectory = SystemUtil.getSystemPropOrEnvVar("lucee.library.fld", null)
                strDefaultTLDDirectory = SystemUtil.getSystemPropOrEnvVar("lucee.library.tld", null)
                strDefaultFuncDirectory = SystemUtil.getSystemPropOrEnvVar("lucee.library.function", null)
                strDefaultTagDirectory = SystemUtil.getSystemPropOrEnvVar("lucee.library.tag", null)
                if (StringUtil.isEmpty(strDefaultFLDDirectory)) strDefaultFLDDirectory = SystemUtil.getSystemPropOrEnvVar("lucee.library.default.fld", null)
                if (StringUtil.isEmpty(strDefaultTLDDirectory)) strDefaultTLDDirectory = SystemUtil.getSystemPropOrEnvVar("lucee.library.default.tld", null)
                if (StringUtil.isEmpty(strDefaultFuncDirectory)) strDefaultFuncDirectory = SystemUtil.getSystemPropOrEnvVar("lucee.library.default.function", null)
                if (StringUtil.isEmpty(strDefaultTagDirectory)) strDefaultTagDirectory = SystemUtil.getSystemPropOrEnvVar("lucee.library.default.tag", null)
                strFuncDirectory = SystemUtil.getSystemPropOrEnvVar("lucee.library.additional.function", null)
                strTagDirectory = SystemUtil.getSystemPropOrEnvVar("lucee.library.additional.tag", null)
            }
            val fileSystem: Struct = ConfigWebUtil.getAsStruct("fileSystem", root)

            // get library directories
            if (fileSystem != null) {
                strAllowRealPath = getAttr(fileSystem, "allowRealpath")
                strDeployDirectory = ConfigWebUtil.translateOldPath(getAttr(fileSystem, "deployDirectory"))
                if (StringUtil.isEmpty(strDefaultTLDDirectory)) strDefaultTLDDirectory = ConfigWebUtil.translateOldPath(getAttr(fileSystem, "tldDirectory"))
                if (StringUtil.isEmpty(strDefaultFLDDirectory)) strDefaultFLDDirectory = ConfigWebUtil.translateOldPath(getAttr(fileSystem, "flddirectory"))
                if (StringUtil.isEmpty(strDefaultTagDirectory)) strDefaultTagDirectory = ConfigWebUtil.translateOldPath(getAttr(fileSystem, "tagDirectory"))
                if (StringUtil.isEmpty(strDefaultFuncDirectory)) strDefaultFuncDirectory = ConfigWebUtil.translateOldPath(getAttr(fileSystem, "functionDirectory"))
                if (StringUtil.isEmpty(strDefaultTLDDirectory)) strDefaultTLDDirectory = ConfigWebUtil.translateOldPath(getAttr(fileSystem, "tldDefaultDirectory"))
                if (StringUtil.isEmpty(strDefaultFLDDirectory)) strDefaultFLDDirectory = ConfigWebUtil.translateOldPath(getAttr(fileSystem, "fldDefaultDirectory"))
                if (StringUtil.isEmpty(strDefaultTagDirectory)) strDefaultTagDirectory = ConfigWebUtil.translateOldPath(getAttr(fileSystem, "tagDefaultDirectory"))
                if (StringUtil.isEmpty(strDefaultFuncDirectory)) strDefaultFuncDirectory = ConfigWebUtil.translateOldPath(getAttr(fileSystem, "functionDefaultDirectory"))
                if (StringUtil.isEmpty(strTagDirectory)) strTagDirectory = ConfigWebUtil.translateOldPath(getAttr(fileSystem, "tagAddionalDirectory"))
                if (StringUtil.isEmpty(strFuncDirectory)) strFuncDirectory = ConfigWebUtil.translateOldPath(getAttr(fileSystem, "functionAddionalDirectory"))
            }

            // set default directories if necessary
            if (StringUtil.isEmpty(strDefaultFLDDirectory)) strDefaultFLDDirectory = "{lucee-config}/library/fld/"
            if (StringUtil.isEmpty(strDefaultTLDDirectory)) strDefaultTLDDirectory = "{lucee-config}/library/tld/"
            if (StringUtil.isEmpty(strDefaultFuncDirectory)) strDefaultFuncDirectory = "{lucee-config}/library/function/"
            if (StringUtil.isEmpty(strDefaultTagDirectory)) strDefaultTagDirectory = "{lucee-config}/library/tag/"

            // Deploy Dir
            val dd: Resource = ConfigWebUtil.getFile(configDir, strDeployDirectory, "cfclasses", configDir, FileUtil.TYPE_DIR, config)
            config.setDeployDirectory(dd)

            // TAG

            // init TLDS
            if (hasCS) {
                config!!.setTLDs(ConfigWebUtil.duplicate(configServer!!.getTLDs(CFMLEngine.DIALECT_CFML), false), CFMLEngine.DIALECT_CFML)
                config!!.setTLDs(ConfigWebUtil.duplicate(configServer!!.getTLDs(CFMLEngine.DIALECT_LUCEE), false), CFMLEngine.DIALECT_LUCEE)
            } else {
                val cs: ConfigServerImpl? = config as ConfigServerImpl?
                config!!.setTLDs(ConfigWebUtil.duplicate(arrayOf<TagLib?>(cs!!.cfmlCoreTLDs), false), CFMLEngine.DIALECT_CFML)
                config!!.setTLDs(ConfigWebUtil.duplicate(arrayOf<TagLib?>(cs!!.luceeCoreTLDs), false), CFMLEngine.DIALECT_LUCEE)
            }

            // TLD Dir
            if (!StringUtil.isEmpty(strDefaultTLDDirectory)) {
                val tld: Resource = ConfigWebUtil.getFile(config, configDir, strDefaultTLDDirectory, FileUtil.TYPE_DIR)
                if (tld != null) config!!.setTldFile(tld, CFMLEngine.DIALECT_BOTH)
            }

            // Tag Directory
            val listTags: List<Path?> = ArrayList<Path?>()
            if (!StringUtil.isEmpty(strDefaultTagDirectory)) {
                val dir: Resource = ConfigWebUtil.getFile(config, configDir, strDefaultTagDirectory, FileUtil.TYPE_DIR)
                createTagFiles(config, configDir, dir, doNew)
                listTags.add(Path(strDefaultTagDirectory, dir))
            }
            // addional tags
            val mapTags: Map<String?, String?> = LinkedHashMap<String?, String?>()
            if (hasCS) {
                val mappings: Collection<Mapping?> = configServer!!.getTagMappings()
                if (mappings != null && !mappings.isEmpty()) {
                    val it: Iterator<Mapping?> = mappings.iterator()
                    var m: Mapping?
                    while (it.hasNext()) {
                        m = it.next()
                        if ((m.getPhysical() == null || !m.getPhysical().exists()) && ConfigWebUtil.hasPlaceholder(m.getStrPhysical())) {
                            mapTags.put(m.getStrPhysical(), "")
                        }
                    }
                }
            }
            if (!StringUtil.isEmpty(strTagDirectory) || !mapTags.isEmpty()) {
                val arr: Array<String?> = ListUtil.listToStringArray(strTagDirectory, ',')
                for (str in arr) {
                    mapTags.put(str, "")
                }
                for (str in mapTags.keySet()) {
                    try {
                        str = str.trim()
                        if (StringUtil.isEmpty(str)) continue
                        val dir: Resource = ConfigWebUtil.getFile(config, configDir, str, FileUtil.TYPE_DIR)
                        listTags.add(Path(str, dir))
                    } catch (t: Throwable) {
                        ExceptionUtil.rethrowIfNecessary(t)
                        log(config, log, t)
                    }
                }
            }
            config!!.setTagDirectory(listTags)

            // allow realpath
            if (hasCS) {
                config!!.setAllowRealPath(configServer!!.allowRealPath())
            }
            if (!StringUtil.isEmpty(strAllowRealPath, true)) {
                config!!.setAllowRealPath(Caster.toBooleanValue(strAllowRealPath, true))
            }

            // FUNCTIONS

            // Init flds
            if (hasCS) {
                config!!.setFLDs(ConfigWebUtil.duplicate(configServer!!.getFLDs(CFMLEngine.DIALECT_CFML), false), CFMLEngine.DIALECT_CFML)
                config!!.setFLDs(ConfigWebUtil.duplicate(configServer!!.getFLDs(CFMLEngine.DIALECT_LUCEE), false), CFMLEngine.DIALECT_LUCEE)
            } else {
                val cs: ConfigServerImpl? = config as ConfigServerImpl?
                config!!.setFLDs(ConfigWebUtil.duplicate(arrayOf<FunctionLib?>(cs!!.cfmlCoreFLDs), false), CFMLEngine.DIALECT_CFML)
                config!!.setFLDs(ConfigWebUtil.duplicate(arrayOf<FunctionLib?>(cs!!.luceeCoreFLDs), false), CFMLEngine.DIALECT_LUCEE)
            }

            // FLDs
            if (!StringUtil.isEmpty(strDefaultFLDDirectory)) {
                val fld: Resource = ConfigWebUtil.getFile(config, configDir, strDefaultFLDDirectory, FileUtil.TYPE_DIR)
                if (fld != null) config!!.setFldFile(fld, CFMLEngine.DIALECT_BOTH)
            }

            // Function files (CFML)
            val listFuncs: List<Path?> = ArrayList<Path?>()
            if (!StringUtil.isEmpty(strDefaultFuncDirectory)) {
                val dir: Resource = ConfigWebUtil.getFile(config, configDir, strDefaultFuncDirectory, FileUtil.TYPE_DIR)
                createFunctionFiles(config, configDir, dir, doNew)
                listFuncs.add(Path(strDefaultFuncDirectory, dir))
                // if (dir != null) config.setFunctionDirectory(dir);
            }
            // function additonal
            val mapFunctions: Map<String?, String?> = LinkedHashMap<String?, String?>()
            if (hasCS) {
                val mappings: Collection<Mapping?> = configServer!!.getFunctionMappings()
                if (mappings != null && !mappings.isEmpty()) {
                    val it: Iterator<Mapping?> = mappings.iterator()
                    var m: Mapping?
                    while (it.hasNext()) {
                        m = it.next()
                        if ((m.getPhysical() == null || !m.getPhysical().exists()) && ConfigWebUtil.hasPlaceholder(m.getStrPhysical())) {
                            mapFunctions.put(m.getStrPhysical(), "")
                        }
                    }
                }
            }
            if (!StringUtil.isEmpty(strFuncDirectory) || !mapFunctions.isEmpty()) {
                val arr: Array<String?> = ListUtil.listToStringArray(strFuncDirectory, ',')
                for (str in arr) {
                    mapFunctions.put(str, "")
                }
                for (str in mapFunctions.keySet()) {
                    try {
                        str = str.trim()
                        if (StringUtil.isEmpty(str)) continue
                        val dir: Resource = ConfigWebUtil.getFile(config, configDir, str, FileUtil.TYPE_DIR)
                        listFuncs.add(Path(str, dir))
                    } catch (t: Throwable) {
                        ExceptionUtil.rethrowIfNecessary(t)
                        log(config, log, t)
                    }
                }
            }
            config!!.setFunctionDirectory(listFuncs)
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
            log(config, log, t)
        }
    }

    private fun createTagFiles(config: Config?, configDir: Resource?, dir: Resource?, doNew: Boolean) {
        if (config is ConfigServer) {

            // Dump
            create("/resource/library/tag/", arrayOf<String?>("Dump." + COMPONENT_EXTENSION), dir, doNew)

            /*
			 * Resource sub = dir.getRealResource("lucee/dump/skins/");
			 * create("/resource/library/tag/lucee/dump/skins/",new String[]{
			 * "text."+CFML_TEMPLATE_MAIN_EXTENSION ,"simple."+CFML_TEMPLATE_MAIN_EXTENSION
			 * ,"modern."+CFML_TEMPLATE_MAIN_EXTENSION ,"classic."+CFML_TEMPLATE_MAIN_EXTENSION
			 * ,"pastel."+CFML_TEMPLATE_MAIN_EXTENSION },sub,doNew);
			 */
            var f: Resource
            val build: Resource = dir.getRealResource("build")
            // /resource/library/tag/build/jquery
            val jquery: Resource = build.getRealResource("jquery")
            if (!jquery.isDirectory()) jquery.mkdirs()
            val names = arrayOf<String?>("jquery-1.12.4.min.js")
            for (i in names.indices) {
                try {
                    f = jquery.getRealResource(names[i])
                    if (!f.exists() || doNew) createFileFromResourceEL("/resource/library/tag/build/jquery/" + names[i], f)
                } catch (t: Throwable) {
                    ExceptionUtil.rethrowIfNecessary(t)
                    log(config, null, t)
                }
            }

            // AJAX
            // AjaxFactory.deployTags(dir, doNew);
        }
    }

    private fun createFunctionFiles(config: Config?, configDir: Resource?, dir: Resource?, doNew: Boolean) {
        if (config is ConfigServer) {
            var f: Resource = dir.getRealResource("writeDump." + TEMPLATE_EXTENSION)
            if (!f.exists() || doNew) createFileFromResourceEL("/resource/library/function/writeDump." + TEMPLATE_EXTENSION, f)
            f = dir.getRealResource("dump." + TEMPLATE_EXTENSION)
            if (!f.exists() || doNew) createFileFromResourceEL("/resource/library/function/dump." + TEMPLATE_EXTENSION, f)
            f = dir.getRealResource("location." + TEMPLATE_EXTENSION)
            if (!f.exists() || doNew) createFileFromResourceEL("/resource/library/function/location." + TEMPLATE_EXTENSION, f)
            f = dir.getRealResource("threadJoin." + TEMPLATE_EXTENSION)
            if (!f.exists() || doNew) createFileFromResourceEL("/resource/library/function/threadJoin." + TEMPLATE_EXTENSION, f)
            f = dir.getRealResource("threadTerminate." + TEMPLATE_EXTENSION)
            if (!f.exists() || doNew) createFileFromResourceEL("/resource/library/function/threadTerminate." + TEMPLATE_EXTENSION, f)
            f = dir.getRealResource("throw." + TEMPLATE_EXTENSION)
            if (!f.exists() || doNew) createFileFromResourceEL("/resource/library/function/throw." + TEMPLATE_EXTENSION, f)
            f = dir.getRealResource("trace." + TEMPLATE_EXTENSION)
            if (!f.exists() || doNew) createFileFromResourceEL("/resource/library/function/trace." + TEMPLATE_EXTENSION, f)
            f = dir.getRealResource("queryExecute." + TEMPLATE_EXTENSION)
            // if (!f.exists() || doNew)
            // createFileFromResourceEL("/resource/library/function/queryExecute."+TEMPLATE_EXTENSION, f);
            if (f.exists()) // FUTURE add this instead if(updateType=NEW_FRESH || updateType=NEW_FROM4)
                delete(dir, "queryExecute." + TEMPLATE_EXTENSION)
            f = dir.getRealResource("transactionCommit." + TEMPLATE_EXTENSION)
            if (!f.exists() || doNew) createFileFromResourceEL("/resource/library/function/transactionCommit." + TEMPLATE_EXTENSION, f)
            f = dir.getRealResource("transactionRollback." + TEMPLATE_EXTENSION)
            if (!f.exists() || doNew) createFileFromResourceEL("/resource/library/function/transactionRollback." + TEMPLATE_EXTENSION, f)
            f = dir.getRealResource("transactionSetsavepoint." + TEMPLATE_EXTENSION)
            if (!f.exists() || doNew) createFileFromResourceEL("/resource/library/function/transactionSetsavepoint." + TEMPLATE_EXTENSION, f)
            f = dir.getRealResource("writeLog." + TEMPLATE_EXTENSION)
            if (!f.exists() || doNew) createFileFromResourceEL("/resource/library/function/writeLog." + TEMPLATE_EXTENSION, f)

            // AjaxFactory.deployFunctions(dir, doNew);
        }
    }

    private fun copyContextFiles(src: Resource?, trg: Resource?) {
        // directory
        if (src.isDirectory()) {
            if (trg.exists()) trg.mkdirs()
            val children: Array<Resource?> = src.listResources()
            for (i in children.indices) {
                copyContextFiles(children[i], trg.getRealResource(children[i].getName()))
            }
        } else if (src.isFile()) {
            if (src.lastModified() > trg.lastModified()) {
                try {
                    if (trg.exists()) trg.remove(true)
                    trg.createFile(true)
                    src.copyTo(trg, false)
                } catch (e: IOException) {
                    LogUtil.logGlobal(ThreadLocalPageContext.getConfig(), ConfigWebFactory::class.java.getName(), e)
                }
            }
        }
    }

    /**
     * @param configServer
     * @param config
     * @param doc
     */
    private fun _loadUpdate(configServer: ConfigServer?, config: Config?, root: Struct?, log: Log?) {
        try {
            // Server
            if (config is ConfigServer && root != null) {
                val cs: ConfigServer? = config as ConfigServer?
                cs.setUpdateType(getAttr(root, "updateType"))
                var location = getAttr(root, "updateLocation")
                if (location != null) {
                    location = location.trim()
                    if ("http://snapshot.lucee.org".equals(location) || "https://snapshot.lucee.org".equals(location)) location = "https://update.lucee.org"
                    if ("http://release.lucee.org".equals(location) || "https://release.lucee.org".equals(location)) location = "https://update.lucee.org"
                }
            }
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
            log(config, log, t)
        }
    }

    /**
     * @param configServer
     * @param config
     * @param doc
     */
    private fun _loadAdminMode(config: ConfigServerImpl?, root: Struct?) {
        config!!.setAdminMode(ConfigWebUtil.toAdminMode(getAttr(root, "mode"), ConfigImpl.ADMINMODE_SINGLE))
    }

    private fun _loadSetting(configServer: ConfigServerImpl?, config: ConfigImpl?, root: Struct?, log: Log?) {
        try {
            val hasAccess: Boolean = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_SETTING)
            val hasCS = configServer != null

            // suppress whitespace
            var str = getAttr(root, "suppressContent")
            if (!StringUtil.isEmpty(str) && hasAccess) {
                config!!.setSuppressContent(toBoolean(str, false))
            } else if (hasCS) config!!.setSuppressContent(configServer!!.isSuppressContent())

            // CFML Writer
            str = SystemUtil.getSystemPropOrEnvVar("lucee.cfml.writer", null)
            if (StringUtil.isEmpty(str)) {
                str = getAttr(root, "cfmlWriter")
            }
            if (!StringUtil.isEmpty(str) && hasAccess) {
                if ("white-space".equalsIgnoreCase(str)) config!!.setCFMLWriterType(ConfigPro.CFML_WRITER_WS) else if ("white-space-pref".equalsIgnoreCase(str)) config!!.setCFMLWriterType(ConfigPro.CFML_WRITER_WS_PREF) else if ("regular".equalsIgnoreCase(str)) config!!.setCFMLWriterType(ConfigPro.CFML_WRITER_REFULAR)
                // FUTURE add support for classes implementing CFMLWriter interface
            } else if (hasCS) config!!.setCFMLWriterType(configServer!!.getCFMLWriterType())

            // show version
            str = getAttr(root, "showVersion")
            if (!StringUtil.isEmpty(str) && hasAccess) {
                config!!.setShowVersion(toBoolean(str, false))
            } else if (hasCS) config!!.setShowVersion(configServer!!.isShowVersion())

            // close connection
            str = getAttr(root, "closeConnection")
            if (!StringUtil.isEmpty(str) && hasAccess) {
                config!!.setCloseConnection(toBoolean(str, false))
            } else if (hasCS) config!!.setCloseConnection(configServer!!.closeConnection())

            // content-length
            str = getAttr(root, "contentLength")
            if (!StringUtil.isEmpty(str) && hasAccess) {
                config!!.setContentLength(toBoolean(str, true))
            } else if (hasCS) config!!.setContentLength(configServer!!.contentLength())
            str = getAttr(root, "bufferTagBodyOutput")
            val b: Boolean = Caster.toBoolean(str, null)
            if (b != null && hasAccess) {
                config!!.setBufferOutput(b.booleanValue())
            } else if (hasCS) config!!.setBufferOutput(configServer!!.getBufferOutput())

            // allow-compression
            str = SystemUtil.getSystemPropOrEnvVar("lucee.allow.compression", null)
            if (StringUtil.isEmpty(str)) {
                str = getAttr(root, "allowCompression")
            }
            if (!StringUtil.isEmpty(str) && hasAccess) {
                config!!.setAllowCompression(toBoolean(str, true))
            } else if (hasCS) config!!.setAllowCompression(configServer!!.allowCompression())

            // mode
            val developMode = getAttr(root, "developMode")
            if (!StringUtil.isEmpty(developMode) && hasAccess) {
                config!!.setDevelopMode(toBoolean(developMode, false))
            } else if (hasCS) config!!.setDevelopMode(configServer!!.isDevelopMode())
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
            log(config, log, t)
        }
    }

    private fun _loadRemoteClient(configServer: ConfigServerImpl?, config: ConfigImpl?, root: Struct?, log: Log?) {
        try {
            val hasAccess: Boolean = ConfigWebUtil.hasAccess(config, SecurityManagerImpl.TYPE_REMOTE)

            // SNSN
            // RemoteClientUsage
            val _clients: Struct = ConfigWebUtil.getAsStruct("remoteClients", root)

            // usage
            val strUsage = getAttr(_clients, "usage")
            val sct: Struct?
            if (!StringUtil.isEmpty(strUsage)) sct = toStruct(strUsage) // config.setRemoteClientUsage(toStruct(strUsage));
            else sct = StructImpl()
            // TODO make this generic
            if (configServer != null) {
                val sync: String = Caster.toString(configServer.getRemoteClientUsage().get("synchronisation", ""), "")
                if (!StringUtil.isEmpty(sync)) {
                    sct.setEL("synchronisation", sync)
                }
            }
            config!!.setRemoteClientUsage(sct)

            // max-threads
            var maxThreads: Int = Caster.toIntValue(getAttr(_clients, "maxThreads"), -1)
            if (maxThreads < 1 && configServer != null) {
                val engine: SpoolerEngineImpl = configServer.getSpoolerEngine() as SpoolerEngineImpl
                if (engine != null) maxThreads = engine.getMaxThreads()
            }
            if (maxThreads < 1) maxThreads = 20

            // directory
            var strDir: String? = SystemUtil.getSystemPropOrEnvVar("lucee.task.directory", null)
            if (StringUtil.isEmpty(strDir)) strDir = if (_clients != null) getAttr(_clients, "directory") else null
            val file: Resource = ConfigWebUtil.getFile(config!!.getRootDirectory(), strDir, "client-task", config!!.getConfigDir(), FileUtil.TYPE_DIR, config)
            config!!.setRemoteClientDirectory(file)
            var clients: Array? = null
            var client: Struct
            if (hasAccess && _clients != null) clients = ConfigWebUtil.getAsArray("remoteClient", _clients)
            val list: MutableList<RemoteClient?> = ArrayList<RemoteClient?>()
            if (clients != null) {
                val it: Iterator<*> = clients.getIterator()
                while (it.hasNext()) {
                    try {
                        client = Caster.toStruct(it.next(), null)
                        if (client == null) continue

                        // type
                        var type = getAttr(client, "type")
                        if (StringUtil.isEmpty(type)) type = "web"
                        // url
                        val url = getAttr(client, "url")
                        var label = getAttr(client, "label")
                        if (StringUtil.isEmpty(label)) label = url
                        val sUser = getAttr(client, "serverUsername")
                        val sPass: String = ConfigWebUtil.decrypt(getAttr(client, "serverPassword"))
                        val aPass: String = ConfigWebUtil.decrypt(getAttr(client, "adminPassword"))
                        val aCode: String = ConfigWebUtil.decrypt(getAttr(client, "securityKey"))
                        // if(aCode!=null && aCode.indexOf('-')!=-1)continue;
                        var usage = getAttr(client, "usage")
                        if (usage == null) usage = ""
                        val pUrl = getAttr(client, "proxyServer")
                        val pPort: Int = Caster.toIntValue(getAttr(client, "proxyPort"), -1)
                        val pUser = getAttr(client, "proxyUsername")
                        val pPass: String = ConfigWebUtil.decrypt(getAttr(client, "proxyPassword"))
                        var pd: ProxyData? = null
                        if (!StringUtil.isEmpty(pUrl, true)) {
                            pd = ProxyDataImpl()
                            pd.setServer(pUrl)
                            if (!StringUtil.isEmpty(pUser)) {
                                pd.setUsername(pUser)
                                pd.setPassword(pPass)
                            }
                            if (pPort > 0) pd.setPort(pPort)
                        }
                        list.add(RemoteClientImpl(label, type, url, sUser, sPass, aPass, pd, aCode, usage))
                    } catch (t: Throwable) {
                        ExceptionUtil.rethrowIfNecessary(t)
                        log(config, log, t)
                    }
                }
            }
            if (list.size() > 0) config!!.setRemoteClients(list.toArray(arrayOfNulls<RemoteClient?>(list.size()))) else config.setRemoteClients(arrayOfNulls<RemoteClient?>(0))

            // init spooler engine
            val dir: Resource = config!!.getRemoteClientDirectory()
            if (dir != null && !dir.exists()) dir.mkdirs()
            var se: SpoolerEngineImpl? = config!!.getSpoolerEngine() as SpoolerEngineImpl
            if (se == null) {
                config!!.setSpoolerEngine(SpoolerEngineImpl(dir, "Remote Client Spooler", ThreadLocalPageContext.getLog(config, "remoteclient"), maxThreads).also { se = it })
            } else {
                se.setLog(ThreadLocalPageContext.getLog(config, "remoteclient"))
                se.setPersisDirectory(dir)
                se.setMaxThreads(maxThreads)
            }
            if (config is ConfigWeb) {
                se.init(config as ConfigWeb?)
            }
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
            log(config, log, t)
        }
    }

    private fun _loadSystem(configServer: ConfigServerImpl?, config: ConfigImpl?, root: Struct?, log: Log?) {
        try {
            val hasAccess: Boolean = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_SETTING)
            // Struct sys = ConfigWebUtil.getAsStruct("system", root);
            val hasCS = configServer != null

            // web context
            if (hasCS) {
                config!!.setOut(config!!.getOutWriter())
                config!!.setErr(config!!.getErrWriter())
                return
            }
            var out: String? = null
            var err: String? = null
            // sys prop or env var
            out = SystemUtil.getSystemPropOrEnvVar("lucee.system.out", null)
            err = SystemUtil.getSystemPropOrEnvVar("lucee.system.err", null)
            if (StringUtil.isEmpty(out)) out = getAttr(root, "systemOut")
            if (StringUtil.isEmpty(err)) err = getAttr(root, "systemErr")

            // OUT
            var ps: PrintStream? = toPrintStream(config, out, false)
            config!!.setOut(PrintWriter(ps))
            System.setOut(ps)

            // ERR
            ps = toPrintStream(config, err, true)
            config!!.setErr(PrintWriter(ps))
            System.setErr(ps)
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
            log(config, log, t)
        }
    }

    private fun toPrintStream(config: Config?, streamtype: String?, iserror: Boolean): PrintStream? {
        var streamtype = streamtype
        if (!StringUtil.isEmpty(streamtype)) {
            streamtype = streamtype.trim()
            // null
            if (streamtype.equalsIgnoreCase("null")) {
                return PrintStream(DevNullOutputStream.DEV_NULL_OUTPUT_STREAM)
            } else if (StringUtil.startsWithIgnoreCase(streamtype, "class:")) {
                val classname: String = streamtype.substring(6)
                try {
                    return ClassUtil.loadInstance(classname) as PrintStream
                } catch (t: Throwable) {
                    ExceptionUtil.rethrowIfNecessary(t)
                }
            } else if (StringUtil.startsWithIgnoreCase(streamtype, "file:")) {
                var strRes: String = streamtype.substring(5)
                try {
                    strRes = ConfigWebUtil.translateOldPath(strRes)
                    val res: Resource = ConfigWebUtil.getFile(config, config.getConfigDir(), strRes, ResourceUtil.TYPE_FILE)
                    if (res != null) return PrintStream(res.getOutputStream(), true)
                } catch (t: Throwable) {
                    ExceptionUtil.rethrowIfNecessary(t)
                }
            } else if (StringUtil.startsWithIgnoreCase(streamtype, "log")) {
                try {
                    val engine: CFMLEngine = ConfigWebUtil.getEngine(config)
                    val root: Resource = ResourceUtil.toResource(engine.getCFMLEngineFactory().getResourceRoot())
                    val log: Resource = root.getRealResource("context/logs/" + (if (iserror) "err" else "out") + ".log")
                    if (!log.isFile()) {
                        log.getParentResource().mkdirs()
                        log.createNewFile()
                    }
                    return PrintStream(RetireOutputStream(log, true, 5, null))
                } catch (t: Throwable) {
                    ExceptionUtil.rethrowIfNecessary(t)
                }
            }
        }
        return if (iserror) CFMLEngineImpl.CONSOLE_ERR else CFMLEngineImpl.CONSOLE_OUT
    }

    /**
     * @param configServer
     * @param config
     * @param doc
     */
    private fun _loadCharset(configServer: ConfigServer?, config: ConfigImpl?, root: Struct?, log: Log?) {
        try {
            val hasAccess: Boolean = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_SETTING)
            val hasCS = configServer != null

            // template
            var template: String? = SystemUtil.getSystemPropOrEnvVar("lucee.template.charset", null)
            if (StringUtil.isEmpty(template)) template = getAttr(root, "templateCharset")
            if (!StringUtil.isEmpty(template)) config.setTemplateCharset(template) else if (hasCS) config.setTemplateCharset(configServer.getTemplateCharset())

            // web
            var web: String? = SystemUtil.getSystemPropOrEnvVar("lucee.web.charset", null)
            if (StringUtil.isEmpty(web)) web = getAttr(root, "webCharset")
            if (!StringUtil.isEmpty(web)) config.setWebCharset(web) else if (hasCS) config.setWebCharset(configServer.getWebCharset())

            // resource
            var resource: String? = null
            resource = SystemUtil.getSystemPropOrEnvVar("lucee.resource.charset", null)
            if (StringUtil.isEmpty(resource)) resource = getAttr(root, "resourceCharset")
            if (!StringUtil.isEmpty(resource)) config.setResourceCharset(resource) else if (hasCS) config.setResourceCharset(configServer.getResourceCharset())
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
            log(config, log, t)
        }
    }

    private fun _loadQueue(configServer: ConfigServerImpl?, config: ConfigImpl?, root: Struct?, log: Log?) {
        try {

            // Server
            if (config is ConfigServerImpl) {

                // max
                var max: Integer = Caster.toInteger(SystemUtil.getSystemPropOrEnvVar("lucee.queue.max", null), null)
                if (max == null) max = Caster.toInteger(getAttr(root, "requestQueueMax"), null)
                config.setQueueMax(Caster.toIntValue(max, 100))

                // timeout
                var timeout: Long = Caster.toLong(SystemUtil.getSystemPropOrEnvVar("lucee.queue.timeout", null), null)
                if (timeout == null) timeout = Caster.toLong(getAttr(root, "requestQueueTimeout"), null)
                config.setQueueTimeout(Caster.toLongValue(timeout, 0L))

                // enable
                var enable: Boolean = Caster.toBoolean(SystemUtil.getSystemPropOrEnvVar("lucee.queue.enable", null), null)
                if (enable == null) enable = Caster.toBoolean(getAttr(root, "requestQueueEnable"), null)
                config.setQueueEnable(Caster.toBooleanValue(enable, false))
                (config as ConfigServerImpl?).setThreadQueue(if (config.getQueueEnable()) ThreadQueueImpl() else ThreadQueueNone())
            } else {
                config!!.setQueueMax(configServer!!.getQueueMax())
                config!!.setQueueTimeout(configServer!!.getQueueTimeout())
                config!!.setQueueEnable(configServer!!.getQueueEnable())
            }
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
            log(config, log, t)
        }
    }

    /**
     * @param configServer
     * @param config
     * @param doc
     */
    private fun _loadRegional(configServer: ConfigServer?, config: ConfigImpl?, root: Struct?, log: Log?) {
        try {
            val hasAccess: Boolean = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_SETTING)
            val hasCS = configServer != null

            // timeZone
            var strTimeZone: String? = null
            strTimeZone = getAttr(root, arrayOf("timezone", "thisTimezone"))
            if (!StringUtil.isEmpty(strTimeZone)) config!!.setTimeZone(TimeZone.getTimeZone(strTimeZone)) else if (hasCS) config!!.setTimeZone(configServer.getTimeZone()) else {
                var def: TimeZone = TimeZone.getDefault()
                if (def == null) {
                    def = TimeZoneConstants.EUROPE_LONDON
                }
                config!!.setTimeZone(def)
            }

            // this is necessary, otherwise travis has no default
            if (TimeZone.getDefault() == null) TimeZone.setDefault(config!!.getTimeZone())

            // timeserver
            var strTimeServer: String? = if (hasCS) null else SystemUtil.getSystemPropOrEnvVar("lucee.timeserver", null)
            var useTimeServer: Boolean? = null
            if (!StringUtil.isEmpty(strTimeServer)) useTimeServer = Boolean.TRUE
            if (StringUtil.isEmpty(strTimeServer)) strTimeServer = getAttr(root, "timeserver")
            if (useTimeServer == null) useTimeServer = Caster.toBoolean(getAttr(root, "useTimeserver"), null)
            if (!StringUtil.isEmpty(strTimeServer)) config!!.setTimeServer(strTimeServer) else if (hasCS) config!!.setTimeServer(configServer.getTimeServer())
            if (useTimeServer != null) config!!.setUseTimeServer(useTimeServer.booleanValue()) else if (hasCS) config!!.setUseTimeServer((configServer as ConfigPro?)!!.getUseTimeServer())

            // locale
            val strLocale = getAttr(root, arrayOf("locale", "thisLocale"))
            if (!StringUtil.isEmpty(strLocale)) config.setLocale(strLocale) else if (hasCS) config.setLocale(configServer.getLocale()) else config.setLocale(Locale.US)
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
            log(config, log, t)
        }
    }

    private fun _loadWS(configServer: ConfigServerImpl?, config: ConfigImpl?, root: Struct?, log: Log?) {
        try {
            val ws: Struct = ConfigWebUtil.getAsStruct("webservice", root)
            val cd: ClassDefinition? = if (ws != null) getClassDefinition(ws, "", config.getIdentification()) else null
            if (cd != null && !StringUtil.isEmpty(cd.getClassName())) {
                config!!.setWSHandlerClassDefinition(cd)
            } else if (configServer != null) {
                config!!.setWSHandlerClassDefinition(configServer.getWSHandlerClassDefinition())
            }
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
            log(config, log, t)
        }
    }

    private fun _loadORM(configServer: ConfigServerImpl?, config: ConfigImpl?, root: Struct?, log: Log?) {
        try {
            val hasAccess: Boolean = ConfigWebUtil.hasAccess(config, SecurityManagerImpl.TYPE_ORM)
            val orm: Struct = ConfigWebUtil.getAsStruct("orm", root)
            val hasCS = configServer != null

            // engine
            val cdDefault: ClassDefinition = ClassDefinitionImpl(DummyORMEngine::class.java)
            var cd: ClassDefinition? = null
            if (orm != null) {
                cd = getClassDefinition(orm, "engine", config.getIdentification())
                if (cd == null || cd.isClassNameEqualTo(DummyORMEngine::class.java.getName()) || cd.isClassNameEqualTo("lucee.runtime.orm.hibernate.HibernateORMEngine")) cd = getClassDefinition(orm, "", config.getIdentification())
                if (cd != null && (cd.isClassNameEqualTo(DummyORMEngine::class.java.getName()) || cd.isClassNameEqualTo("lucee.runtime.orm.hibernate.HibernateORMEngine"))) cd = null
            }
            if (cd == null || !cd.hasClass()) {
                cd = if (configServer != null) configServer.getORMEngineClass() else cdDefault
            }

            // load class (removed because this unnecessary loads the orm engine)
            /*
			 * try { cd.getClazz(); // TODO check interface as well } catch (Exception e) { log.error("ORM", e);
			 * cd=cdDefault; }
			 */config!!.setORMEngineClass(cd)

            // config
            val def: ORMConfiguration? = if (hasCS) configServer!!.getORMConfig() else null
            val ormConfig: ORMConfiguration = if (root == null) def else ORMConfigurationImpl.load(config, null, orm, config!!.getRootDirectory(), def)
            config!!.setORMConfig(ormConfig)
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
            log(config, log, t)
        }
    }

    /**
     * @param configServer
     * @param config
     * @param doc
     * @throws PageException
     * @throws IOException
     */
    private fun _loadScope(configServer: ConfigServerImpl?, config: ConfigImpl?, root: Struct?, mode: Int, log: Log?) {
        try {
            val hasAccess: Boolean = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_SETTING)
            val hasCS = configServer != null

            // Local Mode
            if (mode == ConfigPro.MODE_STRICT) {
                config.setLocalMode(Undefined.MODE_LOCAL_OR_ARGUMENTS_ALWAYS)
            } else {
                val strLocalMode = getAttr(root, "localMode")
                if (hasAccess && !StringUtil.isEmpty(strLocalMode)) {
                    config!!.setLocalMode(strLocalMode)
                } else if (hasCS) config!!.setLocalMode(configServer!!.getLocalMode())
            }

            // CGI readonly
            val strCGIReadonly = getAttr(root, "cgiReadonly")
            if (hasAccess && !StringUtil.isEmpty(strCGIReadonly)) {
                config!!.setCGIScopeReadonly(Caster.toBooleanValue(strCGIReadonly, true))
            } else if (hasCS) config!!.setCGIScopeReadonly(configServer!!.getCGIScopeReadonly())

            // Session-Type
            val strSessionType = getAttr(root, "sessionType")
            if (hasAccess && !StringUtil.isEmpty(strSessionType)) {
                config!!.setSessionType(AppListenerUtil.toSessionType(strSessionType, if (hasCS) configServer!!.getSessionType() else Config.SESSION_TYPE_APPLICATION))
            } else if (hasCS) config!!.setSessionType(configServer!!.getSessionType())

            // Cascading
            if (mode == ConfigPro.MODE_STRICT) {
                config!!.setScopeCascadingType(Config.SCOPE_STRICT)
            } else {
                val strScopeCascadingType = getAttr(root, "scopeCascading")
                if (hasAccess && !StringUtil.isEmpty(strScopeCascadingType)) {
                    config!!.setScopeCascadingType(ConfigWebUtil.toScopeCascading(strScopeCascadingType, Config.SCOPE_STANDARD))
                } else if (hasCS) config!!.setScopeCascadingType(configServer!!.getScopeCascadingType())
            }

            // cascade-to-resultset
            if (mode == ConfigPro.MODE_STRICT) {
                config!!.setAllowImplicidQueryCall(false)
            } else {
                var allowImplicidQueryCall: Boolean = Caster.toBoolean(SystemUtil.getSystemPropOrEnvVar("lucee.cascade.to.resultset", null), null)
                if (allowImplicidQueryCall == null) allowImplicidQueryCall = Caster.toBoolean(getAttr(root, "cascadeToResultset"), null)
                if (hasAccess && allowImplicidQueryCall != null) {
                    config!!.setAllowImplicidQueryCall(allowImplicidQueryCall.booleanValue())
                } else if (hasCS) config!!.setAllowImplicidQueryCall(configServer!!.allowImplicidQueryCall())
            }

            // Merge url and Form
            val strMergeFormAndURL = getAttr(root, "mergeUrlForm")
            if (hasAccess && !StringUtil.isEmpty(strMergeFormAndURL)) {
                config!!.setMergeFormAndURL(toBoolean(strMergeFormAndURL, false))
            } else if (hasCS) config!!.setMergeFormAndURL(configServer!!.mergeFormAndURL())

            // Client-Storage
            run {
                val clientStorage = getAttr(root, "clientStorage")
                if (hasAccess && !StringUtil.isEmpty(clientStorage)) {
                    config!!.setClientStorage(clientStorage)
                } else if (hasCS) config!!.setClientStorage(configServer!!.getClientStorage())
            }

            // Session-Storage
            run {
                val sessionStorage = getAttr(root, "sessionStorage")
                if (hasAccess && !StringUtil.isEmpty(sessionStorage)) {
                    config!!.setSessionStorage(sessionStorage)
                } else if (hasCS) config!!.setSessionStorage(configServer!!.getSessionStorage())
            }

            // Client Timeout
            val clientTimeout = getAttr(root, "clientTimeout")
            if (hasAccess && !StringUtil.isEmpty(clientTimeout)) {
                config.setClientTimeout(clientTimeout)
            } else if (hasCS) config.setClientTimeout(configServer!!.getClientTimeout())

            // Session Timeout
            val sessionTimeout = getAttr(root, "sessionTimeout")
            if (hasAccess && !StringUtil.isEmpty(sessionTimeout)) {
                config.setSessionTimeout(sessionTimeout)
            } else if (hasCS) config.setSessionTimeout(configServer!!.getSessionTimeout())

            // App Timeout
            val appTimeout = getAttr(root, "applicationTimeout")
            if (hasAccess && !StringUtil.isEmpty(appTimeout)) {
                config!!.setApplicationTimeout(appTimeout)
            } else if (hasCS) config!!.setApplicationTimeout(configServer!!.getApplicationTimeout())

            // Client Type
            val strClientType = getAttr(root, "clientType")
            if (hasAccess && !StringUtil.isEmpty(strClientType)) {
                config!!.setClientType(strClientType)
            } else if (hasCS) config!!.setClientType(configServer!!.getClientType())

            // Client
            val configDir: Resource = config!!.getConfigDir()
            var strClientDirectory = getAttr(root, "clientDirectory")
            if (hasAccess && !StringUtil.isEmpty(strClientDirectory)) {
                strClientDirectory = ConfigWebUtil.translateOldPath(strClientDirectory)
                val res: Resource = ConfigWebUtil.getFile(configDir, strClientDirectory, "client-scope", configDir, FileUtil.TYPE_DIR, config)
                config!!.setClientScopeDir(res)
            } else {
                config!!.setClientScopeDir(configDir.getRealResource("client-scope"))
            }
            val strMax = getAttr(root, "clientDirectoryMaxSize")
            if (hasAccess && !StringUtil.isEmpty(strMax)) {
                config!!.setClientScopeDirSize(ByteSizeParser.parseByteSizeDefinition(strMax, config!!.getClientScopeDirSize()))
            } else if (hasCS) config!!.setClientScopeDirSize(configServer!!.getClientScopeDirSize())

            // Session Management
            val strSessionManagement = getAttr(root, "sessionManagement")
            if (hasAccess && !StringUtil.isEmpty(strSessionManagement)) {
                config!!.setSessionManagement(toBoolean(strSessionManagement, true))
            } else if (hasCS) config!!.setSessionManagement(configServer!!.isSessionManagement())

            // Client Management
            val strClientManagement = getAttr(root, "clientManagement")
            if (hasAccess && !StringUtil.isEmpty(strClientManagement)) {
                config!!.setClientManagement(toBoolean(strClientManagement, false))
            } else if (hasCS) config!!.setClientManagement(configServer!!.isClientManagement())

            // Client Cookies
            val strClientCookies = getAttr(root, "clientCookies")
            if (hasAccess && !StringUtil.isEmpty(strClientCookies)) {
                config!!.setClientCookies(toBoolean(strClientCookies, true))
            } else if (hasCS) config!!.setClientCookies(configServer!!.isClientCookies())

            // Domain Cookies
            val strDomainCookies = getAttr(root, "domainCookies")
            if (hasAccess && !StringUtil.isEmpty(strDomainCookies)) {
                config!!.setDomainCookies(toBoolean(strDomainCookies, false))
            } else if (hasCS) config!!.setDomainCookies(configServer!!.isDomainCookies())
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
            log(config, log, t)
        }
    }

    private fun _loadJava(configServer: ConfigServerImpl?, config: ConfigImpl?, root: Struct?, log: Log?) {
        try {
            val hasCS = configServer != null
            val strInspectTemplate = getAttr(root, "inspectTemplate")
            if (!StringUtil.isEmpty(strInspectTemplate, true)) {
                config!!.setInspectTemplate(ConfigWebUtil.inspectTemplate(strInspectTemplate, ConfigPro.INSPECT_ONCE))
            } else if (hasCS) {
                config!!.setInspectTemplate(configServer!!.getInspectTemplate())
            }
            var strCompileType = getAttr(root, "compileType")
            if (!StringUtil.isEmpty(strCompileType)) {
                strCompileType = strCompileType.trim().toLowerCase()
                if (strCompileType.equals("after-startup")) {
                    config!!.setCompileType(Config.RECOMPILE_AFTER_STARTUP)
                } else if (strCompileType.equals("always")) {
                    config!!.setCompileType(Config.RECOMPILE_ALWAYS)
                }
            } else if (hasCS) {
                config!!.setCompileType(configServer!!.getCompileType())
            }
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
            log(config, log, t)
        }
    }

    private fun _loadConstants(configServer: ConfigServerImpl?, config: ConfigImpl?, root: Struct?) {
        try {
            val hasCS = configServer != null
            val constants: Struct = ConfigWebUtil.getAsStruct("constants", root)

            // Constants
            var sct: Struct? = null
            if (hasCS) {
                sct = configServer!!.getConstants()
                if (sct != null) sct = sct.duplicate(false) as Struct
            }
            if (sct == null) sct = StructImpl()
            var name: Key
            if (constants != null) {
                val it: Iterator<Entry<Key?, Object?>?> = constants.entryIterator()
                var con: Struct
                var e: Entry<Key?, Object?>?
                while (it.hasNext()) {
                    try {
                        e = it.next()
                        con = Caster.toStruct(it.next(), null)
                        if (con == null) continue
                        name = e.getKey()
                        if (StringUtil.isEmpty(name)) continue
                        sct.setEL(name, e.getValue())
                    } catch (t: Throwable) {
                        ExceptionUtil.rethrowIfNecessary(t)
                        log(config, null, t)
                    }
                }
            }
            config!!.setConstants(sct)
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
            log(config, null, t)
        }
    }

    fun log(config: Config?, log: Log?, e: Throwable?) {
        try {
            if (log != null) log.error("configuration", e) else {
                LogUtil.logGlobal(config, ConfigWebFactory::class.java.getName(), e)
            }
        } catch (th: Throwable) {
            ExceptionUtil.rethrowIfNecessary(th)
            th.printStackTrace()
        }
    }

    private fun _loadLogin(configServer: ConfigServerImpl?, config: ConfigImpl?, root: Struct?, log: Log?) {
        try {
            // server context
            if (config is ConfigServer) {
                val captcha: Boolean = Caster.toBooleanValue(getAttr(root, "loginCaptcha"), false)
                val rememberme: Boolean = Caster.toBooleanValue(getAttr(root, "loginRememberme"), true)
                val delay: Int = Caster.toIntValue(getAttr(root, "loginDelay"), 1)
                val cs: ConfigServerImpl? = config as ConfigServerImpl?
                cs!!.setLoginDelay(delay)
                cs!!.setLoginCaptcha(captcha)
                cs!!.setRememberMe(rememberme)
            }
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
            log(config, log, t)
        }
    }

    private fun _loadStartupHook(configServer: ConfigServerImpl?, config: ConfigImpl?, root: Struct?, log: Log?) {
        try {
            val children: Array = ConfigWebUtil.getAsArray("startupHooks", root)
            if (children == null || children.size() === 0) return
            val it: Iterator<*> = children.getIterator()
            var child: Struct
            while (it.hasNext()) {
                try {
                    child = Caster.toStruct(it.next())
                    if (child == null) continue
                    val cd: ClassDefinition? = getClassDefinition(child, "", config.getIdentification())
                    val existing: ConfigBase.Startup = config!!.getStartups()!!.get(cd.getClassName())
                    if (existing != null) {
                        if (existing.cd.equals(cd)) continue
                        try {
                            val fin: Method = Reflector.getMethod(existing.instance.getClass(), "finalize", arrayOfNulls<Class?>(0), null)
                            if (fin != null) {
                                fin.invoke(existing.instance, arrayOfNulls<Object?>(0))
                            }
                        } catch (t: Throwable) {
                            ExceptionUtil.rethrowIfNecessary(t)
                        }
                    }
                    val clazz: Class = cd.getClazz()
                    val constr: Constructor = Reflector.getConstructor(clazz, arrayOf<Class?>(Config::class.java), null)
                    if (constr != null) config!!.getStartups().put(cd.getClassName(), Startup(cd, constr.newInstance(arrayOf<Object?>(config)))) else config!!.getStartups().put(cd.getClassName(), Startup(cd, ClassUtil.loadInstance(clazz)))
                } catch (t: Throwable) {
                    ExceptionUtil.rethrowIfNecessary(t)
                    log(config, log, t)
                }
            }
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
            log(config, log, t)
        }
    }

    /**
     * @param configServer
     * @param config
     * @param doc
     * @throws IOException
     */
    private fun _loadMail(configServer: ConfigServerImpl?, config: ConfigImpl?, root: Struct?, log: Log?) { // does no init values
        try {
            val hasAccess: Boolean = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_MAIL)
            val hasCS = configServer != null

            // Send partial
            run {
                val strSendPartial = getAttr(root, "mailSendPartial")
                if (!StringUtil.isEmpty(strSendPartial) && hasAccess) {
                    config!!.setMailSendPartial(toBoolean(strSendPartial, false))
                } else if (hasCS) config!!.setMailSendPartial(configServer!!.isMailSendPartial())
            }
            // User set
            run {
                val strUserSet = getAttr(root, "mailUserSet")
                if (!StringUtil.isEmpty(strUserSet) && hasAccess) {
                    config!!.setUserSet(toBoolean(strUserSet, false))
                } else if (hasCS) config!!.setUserSet(configServer!!.isUserset())
            }

            // Spool Interval
            val strSpoolInterval = getAttr(root, "mailSpoolInterval")
            if (!StringUtil.isEmpty(strSpoolInterval) && hasAccess) {
                config!!.setMailSpoolInterval(Caster.toIntValue(strSpoolInterval, 30))
            } else if (hasCS) config!!.setMailSpoolInterval(configServer!!.getMailSpoolInterval())
            val strEncoding = getAttr(root, "mailDefaultEncoding")
            if (!StringUtil.isEmpty(strEncoding) && hasAccess) config.setMailDefaultEncoding(strEncoding) else if (hasCS) config.setMailDefaultEncoding(configServer!!.getMailDefaultCharset())

            // Spool Enable
            val strSpoolEnable = getAttr(root, "mailSpoolEnable")
            if (!StringUtil.isEmpty(strSpoolEnable) && hasAccess) {
                config!!.setMailSpoolEnable(toBoolean(strSpoolEnable, false))
            } else if (hasCS) config!!.setMailSpoolEnable(configServer!!.isMailSpoolEnable())

            // Timeout
            val strTimeout = getAttr(root, "mailConnectionTimeout")
            if (!StringUtil.isEmpty(strTimeout) && hasAccess) {
                config!!.setMailTimeout(Caster.toIntValue(strTimeout, 60))
            } else if (hasCS) config!!.setMailTimeout(configServer!!.getMailTimeout())

            // Servers
            var index = 0
            // Server[] servers = null;
            val elServers: Array = ConfigWebUtil.getAsArray("mailServers", root)
            val servers: List<Server?> = ArrayList<Server?>()
            if (hasCS) {
                val readOnlyServers: Array<Server?> = configServer!!.getMailServers()
                if (readOnlyServers != null) {
                    for (i in readOnlyServers.indices) {
                        try {
                            servers.add(readOnlyServers[index++].cloneReadOnly())
                        } catch (t: Throwable) {
                            ExceptionUtil.rethrowIfNecessary(t)
                            log(config, log, t)
                        }
                    }
                }
            }
            // TODO get mail servers from env var
            if (hasAccess) {
                val it: Iterator<*> = elServers.getIterator()
                var el: Struct
                var i = -1
                while (it.hasNext()) {
                    try {
                        el = Caster.toStruct(it.next(), null)
                        if (el == null) continue
                        i++
                        servers.add(i,
                                ServerImpl(Caster.toIntValue(getAttr(el, "id"), i + 1), getAttr(el, "smtp"), Caster.toIntValue(getAttr(el, "port"), 25),
                                        getAttr(el, "username"), ConfigWebUtil.decrypt(getAttr(el, "password")), toLong(getAttr(el, "life"), (1000 * 60 * 5).toLong()),
                                        toLong(getAttr(el, "idle"), (1000 * 60 * 1).toLong()), toBoolean(getAttr(el, "tls"), false), toBoolean(getAttr(el, "ssl"), false),
                                        toBoolean(getAttr(el, "reuseConnection"), true), if (hasCS) ServerImpl.TYPE_LOCAL else ServerImpl.TYPE_GLOBAL))
                    } catch (t: Throwable) {
                        ExceptionUtil.rethrowIfNecessary(t)
                        log(config, log, t)
                    }
                }
            }
            config!!.setMailServers(servers.toArray(arrayOfNulls<Server?>(servers.size())))
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
            log(config, log, t)
        }
    }

    private fun _loadMonitors(configServer: ConfigServerImpl?, config: ConfigImpl?, root: Struct?, log: Log?) {
        var configServer: ConfigServerImpl? = configServer
        try {
            // only load in server context
            if (configServer != null) return
            configServer = config as ConfigServerImpl?
            val parent: Struct = ConfigWebUtil.getAsStruct("monitoring", root)
            val enabled: Boolean = Caster.toBoolean(getAttr(parent, "enabled"), null)
            if (enabled != null) configServer!!.setMonitoringEnabled(enabled.booleanValue())
            val children: Array = ConfigWebUtil.getAsArray("monitor", parent)
            val intervalls: MutableList<IntervallMonitor?> = ArrayList<IntervallMonitor?>()
            val requests: MutableList<RequestMonitor?> = ArrayList<RequestMonitor?>()
            val actions: MutableList<MonitorTemp?> = ArrayList<MonitorTemp?>()
            var strType: String?
            var name: String?
            var cd: ClassDefinition?
            var _log: Boolean
            var async: Boolean
            var type: Short
            val it: Iterator<*> = children.getIterator()
            var el: Struct
            while (it.hasNext()) {
                try {
                    el = Caster.toStruct(it.next(), null)
                    if (el == null) continue
                    cd = getClassDefinition(el, "", config!!.getIdentification())
                    strType = getAttr(el, "type")
                    name = getAttr(el, "name")
                    async = Caster.toBooleanValue(getAttr(el, "async"), false)
                    _log = Caster.toBooleanValue(getAttr(el, "log"), true)
                    type = if ("request".equalsIgnoreCase(strType)) IntervallMonitor.TYPE_REQUEST else if ("action".equalsIgnoreCase(strType)) Monitor.TYPE_ACTION else IntervallMonitor.TYPE_INTERVAL
                    if (cd.hasClass() && !StringUtil.isEmpty(name)) {
                        name = name.trim()
                        try {
                            val clazz: Class = cd.getClazz()
                            var obj: Object
                            val constr: ConstructorInstance = Reflector.getConstructorInstance(clazz, arrayOf<Object?>(configServer), null)
                            obj = if (constr != null) constr.invoke() else ClassUtil.newInstance(clazz)
                            LogUtil.logGlobal(ThreadLocalPageContext.getConfig(if (configServer == null) config else configServer), Log.LEVEL_INFO, ConfigWebFactory::class.java.getName(),
                                    "loaded " + strType + " monitor [" + clazz.getName() + "]")
                            if (type == IntervallMonitor.TYPE_INTERVAL) {
                                val m: IntervallMonitor = if (obj is IntervallMonitor) obj as IntervallMonitor else IntervallMonitorWrap(obj)
                                m.init(configServer, name, _log)
                                intervalls.add(m)
                            } else if (type == Monitor.TYPE_ACTION) {
                                val am: ActionMonitor = if (obj is ActionMonitor) obj as ActionMonitor else ActionMonitorWrap(obj)
                                actions.add(MonitorTemp(am, name, _log))
                            } else {
                                var m: RequestMonitorPro? = RequestMonitorProImpl(if (obj is RequestMonitor) obj as RequestMonitor else RequestMonitorWrap(obj))
                                if (async) m = AsyncRequestMonitor(m)
                                m.init(configServer, name, _log)
                                LogUtil.logGlobal(ThreadLocalPageContext.getConfig(if (configServer == null) config else configServer), Log.LEVEL_INFO, ConfigWebFactory::class.java.getName(),
                                        "initialize " + strType + " monitor [" + clazz.getName() + "]")
                                requests.add(m)
                            }
                        } catch (t: Throwable) {
                            ExceptionUtil.rethrowIfNecessary(t)
                            LogUtil.logGlobal(ThreadLocalPageContext.getConfig(if (configServer == null) config else configServer), ConfigWebFactory::class.java.getName(), t)
                        }
                    }
                } catch (t: Throwable) {
                    ExceptionUtil.rethrowIfNecessary(t)
                    log(config, log, t)
                }
            }
            configServer!!.setRequestMonitors(requests.toArray(arrayOfNulls<RequestMonitor?>(requests.size())))
            configServer!!.setIntervallMonitors(intervalls.toArray(arrayOfNulls<IntervallMonitor?>(intervalls.size())))
            val actionMonitorCollector: ActionMonitorCollector = ActionMonitorFatory.getActionMonitorCollector(configServer, actions.toArray(arrayOfNulls<MonitorTemp?>(actions.size())))
            configServer!!.setActionMonitorCollector(actionMonitorCollector)
            (configServer!!.getCFMLEngine() as CFMLEngineImpl).touchMonitor(configServer)
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
            log(config, log, t)
        }
    }

    /**
     * @param configServer
     * @param config
     * @param doc
     * @throws PageException
     */
    private fun _loadSearch(configServer: ConfigServer?, config: ConfigImpl?, root: Struct?, log: Log?) {
        try {
            val search: Struct = ConfigWebUtil.getAsStruct("search", root)

            // class
            var cd: ClassDefinition<SearchEngine?>? = if (search != null) getClassDefinition(search, "engine", config.getIdentification()) else null
            if (cd == null || !cd.hasClass() || "lucee.runtime.search.lucene.LuceneSearchEngine".equals(cd.getClassName())) {
                if (configServer != null) cd = (configServer as ConfigPro?).getSearchEngineClassDefinition() else cd = ClassDefinitionImpl(DummySearchEngine::class.java)
            }

            // directory
            var dir = if (search != null) getAttr(search, "directory") else null
            if (StringUtil.isEmpty(dir)) {
                dir = if (configServer != null) (configServer as ConfigPro?).getSearchEngineDirectory() else "{lucee-web}/search/"
            }
            config!!.setSearchEngine(cd, dir)
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
            log(config, log, t)
        }
    }

    /**
     * @param configServer
     * @param config
     * @param doc
     * @param isEventGatewayContext
     * @throws IOException
     * @throws PageException
     */
    private fun _loadScheduler(configServer: ConfigServer?, config: ConfigImpl?, root: Struct?, log: Log?) {
        try {
            if (config is ConfigServer) {
                val mode: Short = (config as ConfigServerImpl?)!!.getAdminMode()
                // short mode = ConfigWebUtil.toAdminMode(getAttr(root, "mode"), ConfigImpl.ADMINMODE_SINGLE);
                if (mode == ConfigImpl.ADMINMODE_MULTI) return
            }
            val configDir: Resource = config!!.getConfigDir()
            val scheduledTasks: Array = ConfigWebUtil.getAsArray("scheduledTasks", root)
            config.setScheduler(if (configServer != null) configServer.getCFMLEngine() else (config as ConfigServer?).getCFMLEngine(), scheduledTasks)
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
            log(config, log, t)
        }
    }

    /**
     * @param configServer
     * @param config
     * @param doc
     */
    private fun _loadDebug(configServer: ConfigServerImpl?, config: ConfigImpl?, root: Struct?, log: Log?) {
        try {
            val hasCS = configServer != null
            val hasAccess: Boolean = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_DEBUGGING)

            // Entries
            // Struct debugging = ConfigWebUtil.getAsStruct("debugging", root);
            val entries: Array = ConfigWebUtil.getAsArray("debugTemplates", root)
            val list: Map<String?, DebugEntry?> = HashMap<String?, DebugEntry?>()
            if (hasCS) {
                val _entries: Array<DebugEntry?> = (configServer as ConfigPro?)!!.getDebugEntries()
                for (i in _entries.indices) {
                    try {
                        list.put(_entries[i]!!.getId(), _entries[i]!!.duplicate(true))
                    } catch (t: Throwable) {
                        ExceptionUtil.rethrowIfNecessary(t)
                        log(config, log, t)
                    }
                }
            }
            var id: String?
            if (entries != null) {
                val it: Iterator<*> = entries.getIterator()
                var e: Struct
                while (it.hasNext()) {
                    try {
                        e = Caster.toStruct(it.next(), null)
                        if (e == null) continue
                        id = getAttr(e, "id")
                        list.put(id, DebugEntry(id, getAttr(e, "type"), getAttr(e, "iprange"), getAttr(e, "label"), getAttr(e, "path"), getAttr(e, "fullname"),
                                toStruct(getAttr(e, "custom"))))
                    } catch (t: Throwable) {
                        ExceptionUtil.rethrowIfNecessary(t)
                        log(config, log, t)
                    }
                }
            }
            config!!.setDebugEntries(list.values().toArray(arrayOfNulls<DebugEntry?>(list.size())))

            // debug
            val strDebug = getAttr(root, "debuggingEnabled")
            if (hasAccess && !StringUtil.isEmpty(strDebug)) {
                config.setDebug(if (toBoolean(strDebug, false)) ConfigImpl.CLIENT_BOOLEAN_TRUE else ConfigImpl.CLIENT_BOOLEAN_FALSE)
            } else if (hasCS) config.setDebug(if (configServer!!.debug()) ConfigImpl.SERVER_BOOLEAN_TRUE else ConfigImpl.SERVER_BOOLEAN_FALSE)

            // debug-log-output
            val strDLO = getAttr(root, "debuggingLogOutput")
            if (hasAccess && !StringUtil.isEmpty(strDLO)) {
                config.setDebugLogOutput(if (toBoolean(strDLO, false)) ConfigImpl.CLIENT_BOOLEAN_TRUE else ConfigImpl.CLIENT_BOOLEAN_FALSE)
            } else if (hasCS) config.setDebugLogOutput(if (configServer!!.debugLogOutput()) ConfigImpl.SERVER_BOOLEAN_TRUE else ConfigImpl.SERVER_BOOLEAN_FALSE)

            // debug options
            val strDebugOption: String? = if (hasCS) null else SystemUtil.getSystemPropOrEnvVar("lucee.debugging.options", null)
            val debugOptions: Array<String?>? = if (StringUtil.isEmpty(strDebugOption)) null else ListUtil.listToStringArray(strDebugOption, ',')
            var options = 0
            var str = getAttr(root, "debuggingDatabase")
            if (StringUtil.isEmpty(str)) str = getAttr(root, "debuggingShowDatabase")
            if (hasAccess && !StringUtil.isEmpty(str)) {
                if (toBoolean(str, false)) options += ConfigPro.DEBUG_DATABASE
            } else if (debugOptions != null && extractDebugOption("database", debugOptions)) options += ConfigPro.DEBUG_DATABASE else if (hasCS && configServer!!.hasDebugOptions(ConfigPro.DEBUG_DATABASE)) options += ConfigPro.DEBUG_DATABASE
            str = getAttr(root, "debuggingException")
            if (StringUtil.isEmpty(str)) str = getAttr(root, "debuggingShowException")
            if (hasAccess && !StringUtil.isEmpty(str)) {
                if (toBoolean(str, false)) options += ConfigPro.DEBUG_EXCEPTION
            } else if (debugOptions != null && extractDebugOption("exception", debugOptions)) options += ConfigPro.DEBUG_EXCEPTION else if (hasCS && configServer!!.hasDebugOptions(ConfigPro.DEBUG_EXCEPTION)) options += ConfigPro.DEBUG_EXCEPTION
            str = getAttr(root, "debuggingTemplate")
            if (StringUtil.isEmpty(str)) str = getAttr(root, "debuggingShowTemplate")
            if (hasAccess && !StringUtil.isEmpty(str)) {
                if (toBoolean(str, false)) options += ConfigPro.DEBUG_TEMPLATE
            } else if (debugOptions != null && extractDebugOption("template", debugOptions)) options += ConfigPro.DEBUG_TEMPLATE else if (hasCS && configServer!!.hasDebugOptions(ConfigPro.DEBUG_TEMPLATE)) options += ConfigPro.DEBUG_TEMPLATE else options += ConfigPro.DEBUG_TEMPLATE
            str = getAttr(root, "debuggingDump")
            if (StringUtil.isEmpty(str)) str = getAttr(root, "debuggingShowDump")
            if (hasAccess && !StringUtil.isEmpty(str)) {
                if (toBoolean(str, false)) options += ConfigPro.DEBUG_DUMP
            } else if (debugOptions != null && extractDebugOption("dump", debugOptions)) options += ConfigPro.DEBUG_DUMP else if (hasCS && configServer!!.hasDebugOptions(ConfigPro.DEBUG_DUMP)) options += ConfigPro.DEBUG_DUMP
            str = getAttr(root, "debuggingTracing")
            if (StringUtil.isEmpty(str)) str = getAttr(root, "debuggingShowTracing")
            if (StringUtil.isEmpty(str)) str = getAttr(root, "debuggingShowTrace")
            if (hasAccess && !StringUtil.isEmpty(str)) {
                if (toBoolean(str, false)) options += ConfigPro.DEBUG_TRACING
            } else if (debugOptions != null && extractDebugOption("tracing", debugOptions)) options += ConfigPro.DEBUG_TRACING else if (hasCS && configServer!!.hasDebugOptions(ConfigPro.DEBUG_TRACING)) options += ConfigPro.DEBUG_TRACING
            str = getAttr(root, "debuggingTimer")
            if (StringUtil.isEmpty(str)) str = getAttr(root, "debuggingShowTimer")
            if (hasAccess && !StringUtil.isEmpty(str)) {
                if (toBoolean(str, false)) options += ConfigPro.DEBUG_TIMER
            } else if (debugOptions != null && extractDebugOption("timer", debugOptions)) options += ConfigPro.DEBUG_TIMER else if (hasCS && configServer!!.hasDebugOptions(ConfigPro.DEBUG_TIMER)) options += ConfigPro.DEBUG_TIMER
            str = getAttr(root, "debuggingImplicitAccess")
            if (StringUtil.isEmpty(str)) str = getAttr(root, "debuggingShowImplicitAccess")
            if (hasAccess && !StringUtil.isEmpty(str)) {
                if (toBoolean(str, false)) options += ConfigPro.DEBUG_IMPLICIT_ACCESS
            } else if (debugOptions != null && extractDebugOption("implicit-access", debugOptions)) options += ConfigPro.DEBUG_IMPLICIT_ACCESS else if (hasCS && configServer!!.hasDebugOptions(ConfigPro.DEBUG_IMPLICIT_ACCESS)) options += ConfigPro.DEBUG_IMPLICIT_ACCESS
            str = getAttr(root, "debuggingQueryUsage")
            if (StringUtil.isEmpty(str)) str = getAttr(root, "debuggingShowQueryUsage")
            if (hasAccess && !StringUtil.isEmpty(str)) {
                if (toBoolean(str, false)) options += ConfigPro.DEBUG_QUERY_USAGE
            } else if (debugOptions != null && extractDebugOption("queryUsage", debugOptions)) options += ConfigPro.DEBUG_QUERY_USAGE else if (hasCS && configServer!!.hasDebugOptions(ConfigPro.DEBUG_QUERY_USAGE)) options += ConfigPro.DEBUG_QUERY_USAGE
            str = getAttr(root, "debuggingThread")
            if (hasAccess && !StringUtil.isEmpty(str)) {
                if (toBoolean(str, false)) options += ConfigPro.DEBUG_THREAD
            }

            // max records logged
            val strMax = getAttr(root, "debuggingMaxRecordsLogged")
            if (StringUtil.isEmpty(str)) str = getAttr(root, "debuggingShowMaxRecordsLogged")
            if (hasAccess && !StringUtil.isEmpty(strMax)) {
                config!!.setDebugMaxRecordsLogged(Caster.toIntValue(strMax, 10))
            } else if (hasCS) config!!.setDebugMaxRecordsLogged(configServer!!.getDebugMaxRecordsLogged())
            config!!.setDebugOptions(options)
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
            log(config, log, t)
        }
    }

    private fun extractDebugOption(name: String?, values: Array<String?>?): Boolean {
        for (`val` in values!!) {
            if (`val`.trim().equalsIgnoreCase(name)) return true
        }
        return false
    }

    /**
     * @param configServer
     * @param config
     * @param doc
     */
    private fun _loadCFX(configServer: ConfigServerImpl?, config: ConfigImpl?, root: Struct?, log: Log?) {
        try {
            val hasAccess: Boolean = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_CFX_SETTING)
            val map: Map<String?, CFXTagClass?> = MapFactory.< String, CFXTagClass>getConcurrentMap<String?, CFXTagClass?>()
            if (configServer != null) {
                try {
                    if (configServer.getCFXTagPool() != null) {
                        val classes: Map<String?, CFXTagClass?> = configServer.getCFXTagPool().getClasses()
                        val it: Iterator<Entry<String?, CFXTagClass?>?> = classes.entrySet().iterator()
                        var e: Entry<String?, CFXTagClass?>?
                        while (it.hasNext()) {
                            e = it.next()
                            map.put(e.getKey(), e.getValue().cloneReadOnly())
                        }
                    }
                } catch (e: SecurityException) {
                }
            }
            if (hasAccess) {
                if (configServer == null) {
                    System.setProperty("cfx.bin.path", config!!.getConfigDir().getRealResource("bin").getAbsolutePath())
                }

                // Java CFX Tags
                val cfxs: Struct = ConfigWebUtil.getAsStruct("cfx", root)
                val it: Iterator<Entry<Key?, Object?>?> = cfxs.entryIterator()
                var cfxTag: Struct
                var entry: Entry<Key?, Object?>?
                while (it.hasNext()) {
                    try {
                        entry = it.next()
                        cfxTag = Caster.toStruct(entry.getValue(), null)
                        if (cfxTag == null) continue
                        val type = getAttr(cfxTag, "type")
                        if (type != null) {
                            // Java CFX Tags
                            if (type.equalsIgnoreCase("java")) {
                                val name: String = entry.getKey().getString()
                                val cd: ClassDefinition? = getClassDefinition(cfxTag, "", config.getIdentification())
                                if (!StringUtil.isEmpty(name) && cd.hasClass()) {
                                    map.put(name.toLowerCase(), JavaCFXTagClass(name, cd))
                                }
                            }
                        }
                    } catch (t: Throwable) {
                        ExceptionUtil.rethrowIfNecessary(t)
                        log(config, log, t)
                    }
                }
            }
            config.setCFXTagPool(map)
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
            log(config, log, t)
        }
    }

    /**
     * loads the bundles defined in the extensions
     *
     * @param cs
     * @param config
     * @param doc
     * @param log
     */
    private fun _loadExtensionBundles(cs: ConfigServerImpl?, config: ConfigImpl?, root: Struct?, log: Log?) {
        try {
            val children: Array = ConfigWebUtil.getAsArray("extensions", root)
            var strBundles: String
            val extensions: List<RHExtension?> = ArrayList<RHExtension?>()
            var rhe: RHExtension?
            val it: Iterator<Object?> = children.valueIterator()
            var e: Entry<Key?, Object?>
            var child: Struct
            var id: String
            while (it.hasNext()) {
                child = Caster.toStruct(it.next(), null)
                if (child == null) continue
                id = Caster.toString(child.get(KeyConstants._id, null), null)
                if (StringUtil.isEmpty(id)) continue
                var bfsq: Array<BundleInfo?>
                try {
                    var res: String = Caster.toString(child.get(KeyConstants._resource, null), null)
                    if (StringUtil.isEmpty(res)) res = Caster.toString(child.get(KeyConstants._path, null), null)
                    if (StringUtil.isEmpty(res)) res = Caster.toString(child.get(KeyConstants._url, null), null)
                    rhe = RHExtension(config, id, Caster.toString(child.get(KeyConstants._version, null), null), res, true)
                    if (rhe.getStartBundles()) rhe.deployBundles(config)
                    extensions.add(rhe)
                } catch (t: Throwable) {
                    ExceptionUtil.rethrowIfNecessary(t)
                    log(config, log, t)
                    continue
                }
            }
            config!!.setExtensions(extensions.toArray(arrayOfNulls<RHExtension?>(extensions.size())))
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
            log(config, log, t)
        }
    }

    private fun _loadExtensionProviders(configServer: ConfigServerImpl?, config: ConfigImpl?, root: Struct?, log: Log?) {
        try {

            // RH Providers
            run {

                // providers
                val xmlProviders: Array = ConfigWebUtil.getAsArray("extensionProviders", root)
                var strProvider: String
                val providers: Map<RHExtensionProvider?, String?> = LinkedHashMap<RHExtensionProvider?, String?>()
                for (i in 0 until Constants.RH_EXTENSION_PROVIDERS.length) {
                    providers.put(Constants.RH_EXTENSION_PROVIDERS!!.get(i), "")
                }
                if (xmlProviders != null) {
                    val it: Iterator<*> = xmlProviders.valueIterator()
                    var url: String
                    while (it.hasNext()) {
                        url = Caster.toString(it.next(), null)
                        if (StringUtil.isEmpty(url, true)) continue
                        try {
                            providers.put(RHExtensionProvider(url.trim(), false), "")
                        } catch (e: MalformedURLException) {
                            LogUtil.logGlobal(ThreadLocalPageContext.getConfig(if (configServer == null) config else configServer), ConfigWebFactory::class.java.getName(), e)
                        } catch (t: Throwable) {
                            ExceptionUtil.rethrowIfNecessary(t)
                            log(config, log, t)
                        }
                    }
                }
                config!!.setRHExtensionProviders(providers.keySet().toArray(arrayOfNulls<RHExtensionProvider?>(providers.size())))
            }
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
            log(config, log, t)
        }
    }

    /**
     * @param configServer
     * @param config
     * @param doc
     * @throws IOException
     */
    private fun _loadComponent(configServer: ConfigServer?, config: ConfigImpl?, root: Struct?, mode: Int, log: Log?) {
        try {
            val hasAccess: Boolean = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_SETTING)
            var hasSet = false
            val hasCS = configServer != null
            if (hasAccess) {

                // component-default-import
                var strCDI = getAttr(root, "componentAutoImport")
                if (StringUtil.isEmpty(strCDI, true) && configServer != null) {
                    strCDI = (configServer as ConfigServerImpl?)!!.getComponentDefaultImport().toString()
                }
                if (!StringUtil.isEmpty(strCDI, true)) config!!.setComponentDefaultImport(strCDI)

                // Base CFML
                var strBase = getAttr(root, "componentBase")
                if (StringUtil.isEmpty(strBase, true)) {
                    strBase = if (configServer != null) configServer.getBaseComponentTemplate(CFMLEngine.DIALECT_CFML) else "/lucee/Component.cfc"
                }
                config!!.setBaseComponentTemplate(CFMLEngine.DIALECT_CFML, strBase)

                // Base Lucee
                strBase = getAttr(root, "componentBaseLuceeDialect")
                if (StringUtil.isEmpty(strBase, true)) {
                    strBase = if (configServer != null) configServer.getBaseComponentTemplate(CFMLEngine.DIALECT_LUCEE) else "/lucee/Component.lucee"
                }
                config!!.setBaseComponentTemplate(CFMLEngine.DIALECT_LUCEE, strBase)

                // deep search
                if (mode == ConfigPro.MODE_STRICT) {
                    config!!.setDoComponentDeepSearch(false)
                } else {
                    val strDeepSearch = getAttr(root, "componentDeepSearch")
                    if (!StringUtil.isEmpty(strDeepSearch)) {
                        config!!.setDoComponentDeepSearch(Caster.toBooleanValue(strDeepSearch.trim(), false))
                    } else if (hasCS) {
                        config!!.setDoComponentDeepSearch((configServer as ConfigServerImpl?)!!.doComponentDeepSearch())
                    }
                }

                // Dump-Template
                var strDumpRemplate = getAttr(root, "componentDumpTemplate")
                if ((strDumpRemplate == null || strDumpRemplate.trim().length() === 0) && configServer != null) {
                    strDumpRemplate = configServer.getComponentDumpTemplate()
                }
                config!!.setComponentDumpTemplate(strDumpRemplate)

                // data-member-default-access
                if (mode == ConfigPro.MODE_STRICT) {
                    config!!.setComponentDataMemberDefaultAccess(Component.ACCESS_PRIVATE)
                } else {
                    var strDmda = getAttr(root, "componentDataMemberAccess")
                    if (!StringUtil.isEmpty(strDmda, true)) {
                        strDmda = strDmda.toLowerCase().trim()
                        if (strDmda.equals("remote")) config!!.setComponentDataMemberDefaultAccess(Component.ACCESS_REMOTE) else if (strDmda.equals("public")) config!!.setComponentDataMemberDefaultAccess(Component.ACCESS_PUBLIC) else if (strDmda.equals("package")) config!!.setComponentDataMemberDefaultAccess(Component.ACCESS_PACKAGE) else if (strDmda.equals("private")) config!!.setComponentDataMemberDefaultAccess(Component.ACCESS_PRIVATE)
                    } else if (configServer != null) {
                        config!!.setComponentDataMemberDefaultAccess(configServer.getComponentDataMemberDefaultAccess())
                    }
                }

                // trigger-properties
                if (mode == ConfigPro.MODE_STRICT) {
                    config!!.setTriggerComponentDataMember(true)
                } else {
                    val tp: Boolean = Caster.toBoolean(getAttr(root, "componentImplicitNotation"), null)
                    if (tp != null) config!!.setTriggerComponentDataMember(tp.booleanValue()) else if (configServer != null) {
                        config!!.setTriggerComponentDataMember(configServer.getTriggerComponentDataMember())
                    }
                }

                // local search
                if (mode == ConfigPro.MODE_STRICT) {
                    config!!.setComponentLocalSearch(false)
                } else {
                    val ls: Boolean = Caster.toBoolean(getAttr(root, "componentLocalSearch"), null)
                    if (ls != null) config!!.setComponentLocalSearch(ls.booleanValue()) else if (configServer != null) {
                        config!!.setComponentLocalSearch((configServer as ConfigServerImpl?)!!.getComponentLocalSearch())
                    }
                }

                // use cache path
                val ucp: Boolean = Caster.toBoolean(getAttr(root, "componentUseCachePath"), null)
                if (ucp != null) config!!.setUseComponentPathCache(ucp.booleanValue()) else if (configServer != null) {
                    config!!.setUseComponentPathCache((configServer as ConfigServerImpl?)!!.useComponentPathCache())
                }

                // use component shadow
                if (mode == ConfigPro.MODE_STRICT) {
                    config!!.setUseComponentShadow(false)
                } else {
                    val ucs: Boolean = Caster.toBoolean(getAttr(root, "componentUseVariablesScope"), null)
                    if (ucs != null) config!!.setUseComponentShadow(ucs.booleanValue()) else if (configServer != null) {
                        config!!.setUseComponentShadow(configServer.useComponentShadow())
                    }
                }
            } else if (configServer != null) {
                config!!.setBaseComponentTemplate(CFMLEngine.DIALECT_CFML, configServer.getBaseComponentTemplate(CFMLEngine.DIALECT_CFML))
                config!!.setBaseComponentTemplate(CFMLEngine.DIALECT_LUCEE, configServer.getBaseComponentTemplate(CFMLEngine.DIALECT_LUCEE))
                config!!.setComponentDumpTemplate(configServer.getComponentDumpTemplate())
                if (mode == ConfigPro.MODE_STRICT) {
                    config!!.setComponentDataMemberDefaultAccess(Component.ACCESS_PRIVATE)
                    config!!.setTriggerComponentDataMember(true)
                } else {
                    config!!.setComponentDataMemberDefaultAccess(configServer.getComponentDataMemberDefaultAccess())
                    config!!.setTriggerComponentDataMember(configServer.getTriggerComponentDataMember())
                }
            }
            if (mode == ConfigPro.MODE_STRICT) {
                config!!.setDoComponentDeepSearch(false)
                config!!.setComponentDataMemberDefaultAccess(Component.ACCESS_PRIVATE)
                config!!.setTriggerComponentDataMember(true)
                config!!.setComponentLocalSearch(false)
                config!!.setUseComponentShadow(false)
            }

            // Web Mapping
            val compMappings: Array = ConfigWebUtil.getAsArray("componentMappings", root)
            hasSet = false
            var mappings: Array<Mapping?>? = null
            if (hasAccess && compMappings.size() > 0) {
                val it: Iterator<Object?> = compMappings.valueIterator()
                val list: List<Mapping?> = ArrayList()
                var cMapping: Struct
                while (it.hasNext()) {
                    try {
                        cMapping = Caster.toStruct(it.next(), null)
                        if (cMapping == null) continue
                        val virtual: String = createVirtual(cMapping)
                        val physical = getAttr(cMapping, "physical")
                        val archive = getAttr(cMapping, "archive")
                        val readonly = toBoolean(getAttr(cMapping, "readonly"), false)
                        val hidden = toBoolean(getAttr(cMapping, "hidden"), false)
                        val listMode: Int = ConfigWebUtil.toListenerMode(getAttr(cMapping, "listenerMode"), -1)
                        val listType: Int = ConfigWebUtil.toListenerType(getAttr(cMapping, "listenerType"), -1)
                        val inspTemp = inspectTemplate(cMapping)
                        val primary = getAttr(cMapping, "primary")
                        val physicalFirst = archive == null || !primary.equalsIgnoreCase("archive")
                        hasSet = true
                        list.add(MappingImpl(config, virtual, physical, archive, inspTemp, physicalFirst, hidden, readonly, true, false, true, null, listMode, listType))
                    } catch (t: Throwable) {
                        ExceptionUtil.rethrowIfNecessary(t)
                        log(config, log, t)
                    }
                }
                mappings = list.toArray(arrayOfNulls<Mapping?>(list.size()))
                config!!.setComponentMappings(mappings)
            }

            // Server Mapping
            if (hasCS) {
                val originals: Array<Mapping?> = (configServer as ConfigServerImpl?)!!.getComponentMappings()
                var clones: Array<Mapping?>? = arrayOfNulls<Mapping?>(originals.size)
                val map = LinkedHashMap()
                var m: Mapping?
                for (i in clones.indices) {
                    try {
                        m = (originals[i] as MappingImpl?).cloneReadOnly(config)
                        map.put(toKey(m), m)
                    } catch (t: Throwable) {
                        ExceptionUtil.rethrowIfNecessary(t)
                        log(config, log, t)
                    }
                }
                if (mappings != null) {
                    for (i in mappings.indices) {
                        m = mappings[i]
                        map.put(toKey(m), m)
                    }
                }
                if (originals.size > 0) {
                    clones = arrayOfNulls<Mapping?>(map.size())
                    val it: Iterator = map.entrySet().iterator()
                    var entry: Map.Entry
                    var index = 0
                    while (it.hasNext()) {
                        entry = it.next() as Entry
                        clones!![index++] = entry.getValue() as Mapping
                        // print.out("c:"+clones[index-1]);
                    }
                    hasSet = true
                    config!!.setComponentMappings(clones)
                }
            }
            if (!hasSet) {
                val m = MappingImpl(config, "/default", "{lucee-web}/components/", null, ConfigPro.INSPECT_UNDEFINED, true, false, false, true, false, true, null, -1,
                        -1)
                config.setComponentMappings(arrayOf<Mapping?>(m.cloneReadOnly(config)))
            }
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
            log(config, log, t)
        }
    }

    private fun _loadProxy(configServer: ConfigServerImpl?, config: ConfigImpl?, root: Struct?, log: Log?) {
        try {
            val hasCS = configServer != null
            val hasAccess: Boolean = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_SETTING)
            val proxy: Struct = ConfigWebUtil.getAsStruct("proxy", root)

            // proxy server
            val enabled: Boolean = Caster.toBooleanValue(getAttr(proxy, "enabled"), true)
            val server = getAttr(proxy, "server")
            val username = getAttr(proxy, "username")
            val password = getAttr(proxy, "password")
            val port: Int = Caster.toIntValue(getAttr(proxy, "port"), -1)

            // includes/excludes
            val includes: Set<String?>? = if (proxy != null) ProxyDataImpl.toStringSet(getAttr(proxy, "includes")) else null
            val excludes: Set<String?>? = if (proxy != null) ProxyDataImpl.toStringSet(getAttr(proxy, "excludes")) else null
            if (enabled && hasAccess && !StringUtil.isEmpty(server)) {
                val pd: ProxyDataImpl = ProxyDataImpl.getInstance(server, port, username, password) as ProxyDataImpl
                pd.setExcludes(excludes)
                pd.setIncludes(includes)
                config!!.setProxyData(pd)
            } else if (hasCS) config!!.setProxyData(configServer!!.getProxyData())
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
            log(config, log, t)
        }
    }

    private fun _loadError(configServer: ConfigServerImpl?, config: ConfigImpl?, root: Struct?, log: Log?) {
        try {
            // Struct error = ConfigWebUtil.getAsStruct("error", root);
            val hasCS = configServer != null
            val hasAccess: Boolean = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_DEBUGGING)

            // 500
            var template500 = getAttr(root, "errorGeneralTemplate")
            if (StringUtil.isEmpty(template500)) template500 = getAttr(root, "generalErrorTemplate")
            if (hasAccess && !StringUtil.isEmpty(template500)) {
                config!!.setErrorTemplate(500, template500)
            } else if (hasCS) config!!.setErrorTemplate(500, configServer!!.getErrorTemplate(500)) else config!!.setErrorTemplate(500, "/lucee/templates/error/error." + TEMPLATE_EXTENSION)

            // 404
            var template404 = getAttr(root, "errorMissingTemplate")
            if (StringUtil.isEmpty(template404)) template404 = getAttr(root, "missingErrorTemplate")
            if (hasAccess && !StringUtil.isEmpty(template404)) {
                config!!.setErrorTemplate(404, template404)
            } else if (hasCS) config!!.setErrorTemplate(404, configServer!!.getErrorTemplate(404)) else config!!.setErrorTemplate(404, "/lucee/templates/error/error." + TEMPLATE_EXTENSION)

            // status code
            var bStausCode: Boolean = Caster.toBoolean(SystemUtil.getSystemPropOrEnvVar("lucee.status.code", null), null)
            if (bStausCode == null) bStausCode = Caster.toBoolean(getAttr(root, "errorStatusCode"), null)
            if (bStausCode != null && hasAccess) {
                config!!.setErrorStatusCode(bStausCode.booleanValue())
            } else if (hasCS) config!!.setErrorStatusCode(configServer!!.getErrorStatusCode())
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
            log(config, log, t)
        }
    }

    private fun _loadRegex(configServer: ConfigServerImpl?, config: ConfigImpl?, root: Struct?, log: Log?) {
        try {
            // Struct regex = ConfigWebUtil.getAsStruct("regex", root);
            val hasCS = configServer != null
            val hasAccess: Boolean = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_SETTING)
            val strType = getAttr(root, "regexType")
            val type: Int = if (StringUtil.isEmpty(strType)) RegexFactory.TYPE_UNDEFINED else RegexFactory.toType(strType, RegexFactory.TYPE_UNDEFINED)
            if (hasAccess && type != RegexFactory.TYPE_UNDEFINED) {
                config!!.setRegex(RegexFactory.toRegex(type, null))
            } else if (hasCS) config!!.setRegex(configServer!!.getRegex()) else config!!.setRegex(RegexFactory.toRegex(RegexFactory.TYPE_PERL, null))
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
            log(config, log, t)
        }
    }

    private fun _loadCompiler(configServer: ConfigServerImpl?, config: ConfigImpl?, root: Struct?, mode: Int, log: Log?) {
        try {
            val hasCS = configServer != null

            // suppress WS between cffunction and cfargument
            if (mode == ConfigPro.MODE_STRICT) {
                config!!.setSuppressWSBeforeArg(true)
            } else {
                //
                var suppress: String? = SystemUtil.getSystemPropOrEnvVar("lucee.suppress.ws.before.arg", null)
                if (StringUtil.isEmpty(suppress, true)) suppress = getAttr(root, arrayOf("suppressWhitespaceBeforeArgument", "suppressWhitespaceBeforecfargument"))
                if (!StringUtil.isEmpty(suppress, true)) {
                    config!!.setSuppressWSBeforeArg(Caster.toBooleanValue(suppress, true))
                } else if (hasCS) {
                    config!!.setSuppressWSBeforeArg(configServer!!.getSuppressWSBeforeArg())
                }
            }

            // do dot notation keys upper case
            if (mode == ConfigPro.MODE_STRICT) {
                config!!.setDotNotationUpperCase(false)
            } else {
                // Env Var
                if (!hasCS) {
                    val tmp: Boolean = Caster.toBoolean(SystemUtil.getSystemPropOrEnvVar("lucee.preserve.case", null), null)
                    if (tmp != null) {
                        config!!.setDotNotationUpperCase(!tmp.booleanValue())
                    }
                }
                val _case = getAttr(root, "dotNotationUpperCase")
                if (!StringUtil.isEmpty(_case, true)) {
                    config!!.setDotNotationUpperCase(Caster.toBooleanValue(_case, true))
                } else if (hasCS) {
                    config!!.setDotNotationUpperCase(configServer!!.getDotNotationUpperCase())
                }
            }

            // full null support
            // if (!hasCS) {
            var fns = if (hasCS) configServer!!.getFullNullSupport() else false
            if (mode == ConfigPro.MODE_STRICT) {
                fns = true
            } else {
                var str = getAttr(root, arrayOf("nullSupport", "fullNullSupport"))
                if (StringUtil.isEmpty(str, true)) str = SystemUtil.getSystemPropOrEnvVar("lucee.full.null.support", null)
                if (!StringUtil.isEmpty(str, true)) {
                    fns = Caster.toBooleanValue(str, if (hasCS) configServer!!.getFullNullSupport() else false)
                }
            }
            // when FNS is true or the lucee dialect is disabled we have no flip flop within a request. FNS is
            // always the same
            config!!.setFullNullSupport(fns)

            // precise math
            var pm = if (hasCS) configServer!!.getPreciseMath() else true
            if (mode == ConfigPro.MODE_STRICT) {
                pm = true
            } else {
                var str = getAttr(root, "preciseMath")
                if (StringUtil.isEmpty(str, true)) str = SystemUtil.getSystemPropOrEnvVar("lucee.precise.math", null)
                if (!StringUtil.isEmpty(str, true)) {
                    pm = Caster.toBooleanValue(str, if (hasCS) configServer!!.getPreciseMath() else true)
                }
            }
            config!!.setPreciseMath(pm)

            // default output setting
            val output = getAttr(root, "defaultFunctionOutput")
            if (!StringUtil.isEmpty(output, true)) {
                config!!.setDefaultFunctionOutput(Caster.toBooleanValue(output, true))
            } else if (hasCS) {
                config!!.setDefaultFunctionOutput(configServer!!.getDefaultFunctionOutput())
            }

            // suppress WS between cffunction and cfargument
            var str = getAttr(root, "externalizeStringGte")
            if (Decision.isNumber(str)) {
                config!!.setExternalizeStringGTE(Caster.toIntValue(str, -1))
            } else if (hasCS) {
                config!!.setExternalizeStringGTE(configServer!!.getExternalizeStringGTE())
            }

            // allow-lucee-dialect
            if (!hasCS) {
                str = getAttr(root, "allowLuceeDialect")
                if (str == null || !Decision.isBoolean(str)) str = SystemUtil.getSystemPropOrEnvVar("lucee.enable.dialect", null)
                if (str != null && Decision.isBoolean(str)) {
                    config!!.setAllowLuceeDialect(Caster.toBooleanValue(str, false))
                }
            } else {
                config!!.setAllowLuceeDialect(configServer!!.allowLuceeDialect())
            }

            // Handle Unquoted Attribute Values As String
            if (mode == ConfigPro.MODE_STRICT) {
                config!!.setHandleUnQuotedAttrValueAsString(false)
            } else {
                str = getAttr(root, "handleUnquotedAttributeValueAsString")
                if (str != null && Decision.isBoolean(str)) {
                    config!!.setHandleUnQuotedAttrValueAsString(Caster.toBooleanValue(str, true))
                } else if (hasCS) {
                    config!!.setHandleUnQuotedAttrValueAsString(configServer!!.getHandleUnQuotedAttrValueAsString()!!)
                }
            }
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
            log(config, log, t)
        }
    }

    /**
     * @param configServer
     * @param config
     * @param doc
     * @throws IOException
     * @throws PageException
     */
    private fun _loadApplication(configServer: ConfigServerImpl?, config: ConfigImpl?, root: Struct?, mode: Int, log: Log?) {
        try {
            val hasCS = configServer != null
            val hasAccess: Boolean = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_SETTING)

            // Listener type
            var listener: ApplicationListener?
            if (mode == ConfigPro.MODE_STRICT) {
                listener = ModernAppListener()
            } else {
                var strLT: String? = SystemUtil.getSystemPropOrEnvVar("lucee.listener.type", null)
                if (StringUtil.isEmpty(strLT)) strLT = SystemUtil.getSystemPropOrEnvVar("lucee.application.listener", null)
                if (StringUtil.isEmpty(strLT)) strLT = getAttr(root, arrayOf("listenerType", "applicationListener"))
                listener = ConfigWebUtil.loadListener(strLT, null)
                if (listener == null) {
                    if (hasCS && configServer!!.getApplicationListener() != null) listener = ConfigWebUtil.loadListener(configServer!!.getApplicationListener().getType(), null)
                    if (listener == null) listener = MixedAppListener()
                }
            }

            // cachedwithin
            for (i in CACHE_TYPES.indices) {
                try {
                    val cw = getAttr(root, "cachedWithin" + StringUtil.ucFirst(STRING_CACHE_TYPES!![i]))
                    if (!StringUtil.isEmpty(cw, true)) config!!.setCachedWithin(CACHE_TYPES!![i], cw) else if (hasCS) config!!.setCachedWithin(CACHE_TYPES!![i], configServer!!.getCachedWithin(CACHE_TYPES[i]))
                } catch (t: Throwable) {
                    ExceptionUtil.rethrowIfNecessary(t)
                    log(config, log, t)
                }
            }

            // Type Checking
            var typeChecking: Boolean = Caster.toBoolean(SystemUtil.getSystemPropOrEnvVar("lucee.type.checking", null), null)
            if (typeChecking == null) typeChecking = Caster.toBoolean(SystemUtil.getSystemPropOrEnvVar("lucee.udf.type.checking", null), null)
            if (typeChecking == null) typeChecking = Caster.toBoolean(getAttr(root, arrayOf("typeChecking", "UDFTypeChecking")), null)
            if (typeChecking != null) config!!.setTypeChecking(typeChecking.booleanValue()) else if (hasCS) config!!.setTypeChecking(configServer!!.getTypeChecking())

            // cached after
            var ts: TimeSpan? = null
            if (hasAccess) {
                val ca = getAttr(root, "cachedAfter")
                if (!StringUtil.isEmpty(ca)) ts = Caster.toTimespan(ca)
            }
            if (ts != null) config!!.setCachedAfterTimeRange(ts) else if (hasCS) config!!.setCachedAfterTimeRange(configServer!!.getCachedAfterTimeRange()) else config!!.setCachedAfterTimeRange(null)

            // Listener Mode
            var strLM: String? = SystemUtil.getSystemPropOrEnvVar("lucee.listener.mode", null)
            if (StringUtil.isEmpty(strLM)) strLM = SystemUtil.getSystemPropOrEnvVar("lucee.application.mode", null)
            if (StringUtil.isEmpty(strLM)) strLM = getAttr(root, arrayOf("listenerMode", "applicationMode"))
            var listenerMode: Int = ConfigWebUtil.toListenerMode(strLM, -1)
            if (listenerMode == -1) {
                listenerMode = if (hasCS) if (configServer!!.getApplicationListener() == null) ApplicationListener.MODE_CURRENT2ROOT else configServer!!.getApplicationListener().getMode() else ApplicationListener.MODE_CURRENT2ROOT
            }
            listener.setMode(listenerMode)
            config!!.setApplicationListener(listener)

            // Req Timeout URL
            if (mode == ConfigPro.MODE_STRICT) {
                config!!.setAllowURLRequestTimeout(false)
            } else {
                val allowURLReqTimeout = getAttr(root, arrayOf("requestTimeoutInURL", "allowUrlRequesttimeout"))
                if (hasAccess && !StringUtil.isEmpty(allowURLReqTimeout)) {
                    config!!.setAllowURLRequestTimeout(Caster.toBooleanValue(allowURLReqTimeout, false))
                } else if (hasCS) config!!.setAllowURLRequestTimeout(configServer!!.isAllowURLRequestTimeout())
            }

            // Req Timeout
            ts = null
            if (hasAccess) {
                var reqTimeout: String? = SystemUtil.getSystemPropOrEnvVar("lucee.requesttimeout", null)
                if (reqTimeout == null) reqTimeout = getAttr(root, "requesttimeout")
                if (!StringUtil.isEmpty(reqTimeout)) ts = Caster.toTimespan(reqTimeout)
            }
            if (ts != null && ts.getMillis() > 0) config.setRequestTimeout(ts) else if (hasCS) config.setRequestTimeout(configServer!!.getRequestTimeout())

            // application Path Timeout
            ts = null
            if (hasAccess) {
                var reqTimeout: String? = SystemUtil.getSystemPropOrEnvVar("lucee.application.path.cache.timeout", null)
                if (reqTimeout == null) reqTimeout = getAttr(root, "applicationPathTimeout")
                if (!StringUtil.isEmpty(reqTimeout)) ts = Caster.toTimespan(reqTimeout)
            }
            if (ts != null && ts.getMillis() > 0) config!!.setApplicationPathCacheTimeout(ts.getMillis()) else if (hasCS) config!!.setApplicationPathCacheTimeout(configServer!!.getApplicationPathCacheTimeout())

            // script-protect
            var strScriptProtect: String? = SystemUtil.getSystemPropOrEnvVar("lucee.script.protect", null)
            if (StringUtil.isEmpty(strScriptProtect)) strScriptProtect = getAttr(root, "scriptProtect")
            if (hasAccess && !StringUtil.isEmpty(strScriptProtect)) {
                config!!.setScriptProtect(AppListenerUtil.translateScriptProtect(strScriptProtect))
            } else if (hasCS) config!!.setScriptProtect(configServer!!.getScriptProtect())

            // classic-date-parsing
            if (config is ConfigServer) {
                if (mode == ConfigPro.MODE_STRICT) {
                    DateCaster.classicStyle = true
                } else {
                    val strClassicDateParsing = getAttr(root, "classicDateParsing")
                    if (!StringUtil.isEmpty(strClassicDateParsing)) {
                        DateCaster.classicStyle = Caster.toBooleanValue(strClassicDateParsing, false)
                    }
                }
            }

            // Cache
            val configDir: Resource = config!!.getConfigDir()
            var strCacheDirectory = getAttr(root, "cacheDirectory")
            if (hasAccess && !StringUtil.isEmpty(strCacheDirectory)) {
                strCacheDirectory = ConfigWebUtil.translateOldPath(strCacheDirectory)
                val res: Resource = ConfigWebUtil.getFile(configDir, strCacheDirectory, "cache", configDir, FileUtil.TYPE_DIR, config)
                config!!.setCacheDir(res)
            } else {
                config!!.setCacheDir(configDir.getRealResource("cache"))
            }

            // cache dir max size
            val strMax = getAttr(root, "cacheDirectoryMaxSize")
            if (hasAccess && !StringUtil.isEmpty(strMax)) {
                config!!.setCacheDirSize(ByteSizeParser.parseByteSizeDefinition(strMax, config!!.getCacheDirSize()))
            } else if (hasCS) config!!.setCacheDirSize(configServer!!.getCacheDirSize())

            // admin sync
            var asc: ClassDefinition? = getClassDefinition(root, "adminSync", config.getIdentification())
            if (!asc.hasClass()) asc = getClassDefinition(root, "adminSynchronisation", config.getIdentification())
            if (hasAccess && asc.hasClass()) {
                try {
                    val clazz: Class = asc.getClazz()
                    if (!Reflector.isInstaneOf(clazz, AdminSync::class.java, false)) throw ApplicationException("class [" + clazz.getName().toString() + "] does not implement interface [" + AdminSync::class.java.getName().toString() + "]")
                    config!!.setAdminSyncClass(clazz)
                } catch (t: Throwable) {
                    ExceptionUtil.rethrowIfNecessary(t)
                    LogUtil.logGlobal(if (configServer == null) config else configServer, ConfigWebFactory::class.java.getName(), t)
                }
            } else if (hasCS) config!!.setAdminSyncClass(configServer!!.getAdminSyncClass())
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
            log(config, log, t)
        }
    }

    /**
     * cast a string value to a boolean
     *
     * @param value String value represent a booolean ("yes", "no","true" aso.)
     * @param defaultValue if can't cast to a boolean is value will be returned
     * @return boolean value
     */
    private fun toBoolean(value: String?, defaultValue: Boolean): Boolean {
        return if (value == null || value.trim().length() === 0) defaultValue else try {
            Caster.toBooleanValue(value.trim())
        } catch (e: PageException) {
            defaultValue
        }
    }

    fun toLong(value: String?, defaultValue: Long): Long {
        if (value == null || value.trim().length() === 0) return defaultValue
        val longValue: Long = Caster.toLongValue(value.trim(), Long.MIN_VALUE)
        return if (longValue == Long.MIN_VALUE) defaultValue else longValue
    }

    fun getAttr(data: Struct?, name: String?): String? {
        val v: String = ConfigWebUtil.getAsString(name, data, null) ?: return null
        return if (StringUtil.isEmpty(v)) "" else replaceConfigPlaceHolder(v)
    }

    fun getAttr(data: Struct?, name: String?, alias: String?): String? {
        var v: String = ConfigWebUtil.getAsString(name, data, null)
        if (v == null) v = ConfigWebUtil.getAsString(alias, data, null)
        if (v == null) return null
        return if (StringUtil.isEmpty(v)) "" else replaceConfigPlaceHolder(v)
    }

    fun getAttr(data: Struct?, names: Array<String?>?): String? {
        var v: String
        for (name in names!!) {
            v = ConfigWebUtil.getAsString(name, data, null)
            if (!StringUtil.isEmpty(v)) return replaceConfigPlaceHolder(v)
        }
        return null
    }

    fun replaceConfigPlaceHolder(v: String?): String? {
        var v = v
        if (StringUtil.isEmpty(v) || v.indexOf('{') === -1) return v
        var s = -1
        var e = -1
        var d = -1
        var prefixLen: Int
        var start = -1
        var end: Int
        var _name: String?
        var _prop: String
        while ((v.indexOf("{system:", start).also { s = it } != -1) or ( /* don't change */
                        v.indexOf("{env:", start).also { e = it } != -1) or ( /* don't change */
                        v.indexOf("\${", start).also { d = it } != -1)) {
            var isSystem = false
            var isDollar = false
            // system
            if (s > -1 && (e == -1 || e > s)) {
                start = s
                prefixLen = 8
                isSystem = true
            } else if (e > -1) {
                start = e
                prefixLen = 5
            } else {
                start = d
                prefixLen = 2
                isDollar = true
            }
            end = v.indexOf('}', start)
            /*
			 * print.e("----------------"); print.e(s+"-"+e); print.e(v); print.e(start); print.e(end);
			 */if (end > prefixLen) {
                _name = v.substring(start + prefixLen, end)
                // print.e(_name);
                _prop = if (isDollar) {
                    val _parts: Array<String?> = _name.split(":")
                    SystemUtil.getSystemPropOrEnvVar(_parts[0], if (_parts.size > 1) _parts[1] else null)
                } else {
                    if (isSystem) System.getProperty(_name) else System.getenv(_name)
                }
                if (_prop != null) {
                    v = StringBuilder().append(v.substring(0, start)).append(_prop).append(v.substring(end + 1)).toString()
                    start += _prop.length()
                } else start = end
            } else start = end // set start to end for the next round
            s = -1
            e = -1 // reset index
            d = -1 // I don't think we need this?
        }
        return v
    }

    class Path(val str: String?, res: Resource?) {
        val res: Resource?
        fun isValidDirectory(): Boolean {
            return res.isDirectory()
        }

        init {
            this.res = res
        }
    }

    class MonitorTemp(am: ActionMonitor?, name: String?, log: Boolean) {
        val am: ActionMonitor?
        val name: String?
        val log: Boolean

        init {
            this.am = am
            this.name = name
            this.log = log
        }
    }
}