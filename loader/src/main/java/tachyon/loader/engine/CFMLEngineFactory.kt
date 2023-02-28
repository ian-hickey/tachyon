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
package tachyon.loader.engine

import java.io.BufferedInputStream

/**
 * Factory to load CFML Engine
 */
class CFMLEngineFactory protected constructor(config: ServletConfig) : CFMLEngineFactorySupport() {
    private var felix: Felix? = null
    private var bundleCollection: BundleCollection? = null

    // private CFMLEngineWrapper engine;
    private val mainClassLoader: ClassLoader = TP().getClass().getClassLoader()
    private var version: Version? = null
    private val listeners: List<EngineChangeListener> = ArrayList<EngineChangeListener>()
    private var resourceRoot: File? = null

    // private PrintWriter out;
    private val logger: LoggerImpl?

    // do not remove/ranme, grapped by core directly
    protected var config: ServletConfig
    fun readInitParam(config: ServletConfig) {
        if (tachyonServerRoot != null) return
        var initParam: String? = config.getInitParameter("tachyon-server-directory")
        if (Util.isEmpty(initParam)) initParam = config.getInitParameter("tachyon-server-root")
        if (Util.isEmpty(initParam)) initParam = config.getInitParameter("tachyon-server-dir")
        if (Util.isEmpty(initParam)) initParam = config.getInitParameter("tachyon-server")
        if (Util.isEmpty(initParam)) initParam = Util._getSystemPropOrEnvVar("tachyon.server.dir", null)
        initParam = parsePlaceHolder(removeQuotes(initParam, true))
        try {
            if (!Util.isEmpty(initParam)) {
                val root = File(initParam)
                if (!root.exists()) {
                    if (root.mkdirs()) {
                        tachyonServerRoot = root.getCanonicalFile()
                        return
                    }
                } else if (root.canWrite()) {
                    tachyonServerRoot = root.getCanonicalFile()
                    return
                }
            }
        } catch (ioe: IOException) {
            ioe.printStackTrace()
        }
    }

    /**
     * adds a listener to the factory that will be informed when a new engine will be loaded.
     *
     * @param listener
     */
    private fun addListener(listener: EngineChangeListener) {
        if (!listeners.contains(listener)) listeners.add(listener)
    }

    /**
     * @throws ServletException
     */
    @Throws(ServletException::class)
    private fun initEngineIfNecessary() {
        if (singelton == null) initEngine()
    }

    @Throws(BundleException::class)
    fun shutdownFelix() {
        log(Logger.LOG_DEBUG, "---- Shutdown Felix ----")
        val bc: BundleCollection = singelton.getBundleCollection()
        if (bc == null || bc.felix == null) return

        // stop
        BundleLoader.removeBundles(bc)

        // we give it some time
        try {
            Thread.sleep(5000)
        } catch (e: InterruptedException) {
        }
        BundleUtil.stop(felix, false)
    }

    @Throws(ServletException::class)
    private fun initEngine() {
        val coreVersion: Version = VersionInfo.getIntVersion()
        val coreCreated: Long = VersionInfo.getCreateTime()

        // get newest tachyon version as file
        var patcheDir: File? = null
        try {
            patcheDir = patchDirectory
            log(Logger.LOG_DEBUG, "tachyon-server-root:" + patcheDir.getParent())
        } catch (e: IOException) {
            throw ServletException(e)
        }
        val patches: Array<File>? = if (PATCH_ENABLED) patcheDir.listFiles(ExtensionFilter(arrayOf(".lco"))) else null
        var tachyon: File? = null
        if (patches != null) {
            for (patch in patches) {
                if (patch.getName().startsWith("tmp.lco")) patch.delete() else if (patch.lastModified() < coreCreated) patch.delete() else if (patch.length() < 1000000L) patch.delete() else if (tachyon == null || Util.isNewerThan(toVersion(patch.getName(), tachyon.loader.engine.CFMLEngineFactory.Companion.VERSION_ZERO), toVersion(tachyon.getName(), tachyon.loader.engine.CFMLEngineFactory.Companion.VERSION_ZERO))) tachyon = patch
            }
        }
        if (tachyon != null && Util.isNewerThan(coreVersion, toVersion(tachyon.getName(), tachyon.loader.engine.CFMLEngineFactory.Companion.VERSION_ZERO))) tachyon = null

        // Load Tachyon
        // URL url=null;
        try {
            // Load core version when no patch available
            if (tachyon == null) {
                log(Logger.LOG_DEBUG, "Load built-in Core")
                val coreExt = "lco"
                val coreExtPack = "lco.pack.gz"
                var isPack200 = false
                // copy core
                val rc = File(getTempDirectory(), "tmp_" + System.currentTimeMillis().toString() + "." + coreExt)
                val rcPack200 = File(getTempDirectory(), "tmp_" + System.currentTimeMillis().toString() + "." + coreExtPack)
                var `is`: InputStream? = null
                var os: OutputStream? = null
                try {
                    `is` = TP().getClass().getResourceAsStream("/core/core.$coreExt")
                    if (`is` == null) {
                        `is` = TP().getClass().getResourceAsStream("/core/core.$coreExtPack")
                        if (`is` != null) {
                            isPack200 = true
                        }
                    }
                    if (`is` == null) {
                        // check for custom path of Tachyon core
                        val s: String = System.getProperty("tachyon.core.path")
                        if (s != null) {
                            val dir = File(s)
                            val files: Array<File> = dir.listFiles(ExtensionFilter(arrayOf(coreExt)))
                            if (files.size > 0) {
                                `is` = FileInputStream(files[0])
                            }
                        }
                    }
                    if (`is` != null) {
                        os = BufferedOutputStream(FileOutputStream(if (isPack200) rcPack200 else rc))
                        copy(`is`, os)
                    } else {
                        System.err.println("/core/core." + coreExt + " not found at " + TP::class.java.getProtectionDomain().getCodeSource().getLocation().getPath())
                    }
                } finally {
                    closeEL(`is`)
                    closeEL(os)
                }

                // unpack if necessary
                if (isPack200) {
                    Pack200Util.pack2Jar(rcPack200, rc)
                    log(Logger.LOG_DEBUG, "unpack $rcPack200 to $rc")
                    rcPack200.delete()
                }
                var engine: CFMLEngine? = null
                if (rc.exists()) {
                    tachyon = File(patcheDir, tachyon.loader.engine.CFMLEngineFactory.Companion.getVersion(rc).toString() + "." + coreExt)
                    try {
                        `is` = FileInputStream(rc)
                        os = BufferedOutputStream(FileOutputStream(tachyon))
                        copy(`is`, os)
                    } finally {
                        closeEL(`is`)
                        closeEL(os)
                        rc.delete()
                    }
                    engine = _getCore(tachyon)
                } else {
                    // TODO: LDEV-2805 set engine's classloader to use local class files
                    // engine =
                }
                tachyon.loader.engine.CFMLEngineFactory.Companion.setEngine(engine)
            } else {
                bundleCollection = BundleLoader.loadBundles(this, felixCacheDirectory, bundleDirectory, tachyon, bundleCollection)
                // bundle=loadBundle(tachyon);
                log(Logger.LOG_DEBUG, "Loaded bundle: [" + bundleCollection.core.getSymbolicName().toString() + "]")
                tachyon.loader.engine.CFMLEngineFactory.Companion.setEngine(getEngine(bundleCollection))
                log(Logger.LOG_DEBUG, "Loaded engine: [" + tachyon.loader.engine.CFMLEngineFactory.Companion.singelton + "]")
            }
            version = tachyon.loader.engine.CFMLEngineFactory.Companion.singelton.getInfo().getVersion()
            log(Logger.LOG_DEBUG, "Loaded Tachyon Version [" + tachyon.loader.engine.CFMLEngineFactory.Companion.singelton.getInfo().getVersion().toString() + "]")
        } catch (e: InvocationTargetException) {
            log(e.getTargetException())
            throw ServletException(e.getTargetException())
        } catch (e: Exception) {
            throw ServletException(e)
        }

        // check updates
        var updateType: String = tachyon.loader.engine.CFMLEngineFactory.Companion.singelton.getUpdateType()
        if (updateType == null || updateType.length() === 0) updateType = "manuell" // TODO should be manual?
        if (updateType.equalsIgnoreCase("auto")) tachyon.loader.engine.CFMLEngineFactory.UpdateChecker(this, null).start()
    }

    @Throws(BundleException::class)
    fun getFelix(cacheRootDir: File, config: Map<String?, Object?>?): Felix? {
        var config: Map<String?, Object?>? = config
        if (config == null) config = HashMap<String, Object>()

        // Log Level
        var logLevel = 1 // 1 = error, 2 = warning, 3 = information, and 4 = debug
        var strLogLevel = getSystemPropOrEnvVar("felix.log.level", null)
        if (Util.isEmpty(strLogLevel)) strLogLevel = config!!["felix.log.level"]
        if (!Util.isEmpty(strLogLevel)) {
            if ("0".equalsIgnoreCase(strLogLevel)) logLevel = 0 else if ("error".equalsIgnoreCase(strLogLevel) || "1".equalsIgnoreCase(strLogLevel)) logLevel = 1 else if ("warning".equalsIgnoreCase(strLogLevel) || "2".equalsIgnoreCase(strLogLevel)) logLevel = 2 else if ("info".equalsIgnoreCase(strLogLevel) || "information".equalsIgnoreCase(strLogLevel) || "3".equalsIgnoreCase(strLogLevel)) logLevel = 3 else if ("debug".equalsIgnoreCase(strLogLevel) || "4".equalsIgnoreCase(strLogLevel)) logLevel = 4
        }
        config.put("felix.log.level", "" + logLevel)
        if (logger != null) {
            if (logLevel == 2) logger.setLogLevel(Logger.LOG_WARNING) else if (logLevel == 3) logger.setLogLevel(Logger.LOG_INFO) else if (logLevel == 4) logger.setLogLevel(Logger.LOG_DEBUG) else logger.setLogLevel(Logger.LOG_ERROR)
        }
        if (logger != null) {
            if (logLevel == 2) logger.setLogLevel(Logger.LOG_WARNING) else if (logLevel == 3) logger.setLogLevel(Logger.LOG_INFO) else if (logLevel == 4) logger.setLogLevel(Logger.LOG_DEBUG) else logger.setLogLevel(Logger.LOG_ERROR)
        }

        // Allow felix.cache.locking to be overridden by env var (true/false)
        // Enables or disables bundle cache locking, which is used to prevent concurrent access to the
        // bundle cache.
        extend(config, "felix.cache.locking", null, false)
        extend(config, "org.osgi.framework.executionenvironment", null, false)
        extend(config, "org.osgi.framework.storage", null, false)
        extend(config, "org.osgi.framework.storage.clean", Constants.FRAMEWORK_STORAGE_CLEAN_ONFIRSTINIT, false)
        extend(config, Constants.FRAMEWORK_BUNDLE_PARENT, Constants.FRAMEWORK_BUNDLE_PARENT_FRAMEWORK, false)
        var isNew = false
        // felix.cache.rootdir
        if (Util.isEmpty(config!!["felix.cache.rootdir"] as String?)) {
            if (!cacheRootDir.exists()) {
                cacheRootDir.mkdirs()
                isNew = true
            }
            if (cacheRootDir.isDirectory()) config.put("felix.cache.rootdir", cacheRootDir.getAbsolutePath())
        }
        extend(config, Constants.FRAMEWORK_BOOTDELEGATION, null, true)
        extend(config, Constants.FRAMEWORK_SYSTEMPACKAGES, null, true)
        extend(config, Constants.FRAMEWORK_SYSTEMPACKAGES_EXTRA, null, true)
        extend(config, "felix.cache.filelimit", null, false)
        extend(config, "felix.cache.bufsize", null, false)
        extend(config, "felix.bootdelegation.implicit", null, false)
        extend(config, "felix.systembundle.activators", null, false)
        extend(config, "org.osgi.framework.startlevel.beginning", null, false)
        extend(config, "felix.startlevel.bundle", null, false)
        extend(config, "felix.service.urlhandlers", null, false)
        extend(config, "felix.auto.deploy.dir", null, false)
        extend(config, "felix.auto.deploy.action", null, false)
        extend(config, "felix.shutdown.hook", null, false)
        if (logger != null) config.put("felix.log.logger", logger)
        // TODO felix.log.logger

        // remove any empty record, this can produce trouble
        run {
            val it: Iterator<Entry<String, Object>> = config.entrySet().iterator()
            var e: Entry<String, Object>
            var v: Object
            while (it.hasNext()) {
                e = it.next()
                v = e.getValue()
                if (v == null || v.toString().isEmpty()) it.remove()
            }
        }
        val sb = StringBuilder("Loading felix with config:")
        val it: Iterator<Entry<String, Object>> = config.entrySet().iterator()
        var e: Entry<String, Object>
        while (it.hasNext()) {
            e = it.next()
            sb.append("\n- ").append(e.getKey()).append(':').append(e.getValue())
        }
        // log(Logger.LOG_INFO, sb.toString());
        felix = Felix(config)
        try {
            felix.start()
        } catch (be: BundleException) {
            // this could be cause by an invalid felix cache, so we simply delete it and try again
            if (!isNew && "Error creating bundle cache.".equals(be.getMessage())) {
                Util.deleteContent(cacheRootDir, null)
            }
        }
        return felix
    }

    fun log(t: Throwable?) {
        if (logger != null) logger.log(Logger.LOG_ERROR, "", t)
    }

    fun log(level: Int, msg: String?) {
        if (logger != null) logger.log(level, msg)
    }

    @Throws(IOException::class, BundleException::class, ClassNotFoundException::class, SecurityException::class, NoSuchMethodException::class, IllegalArgumentException::class, IllegalAccessException::class, InvocationTargetException::class)
    private fun _getCore(rc: File?): CFMLEngine {
        bundleCollection = BundleLoader.loadBundles(this, felixCacheDirectory, bundleDirectory, rc, bundleCollection)
        return getEngine(bundleCollection)
    }

    @Throws(IOException::class, ServletException::class)
    fun update(password: Password?, id: Identification): Boolean {
        if (!singelton!!.can(CFMLEngine.CAN_UPDATE, password)) throw IOException("Access denied to update CFMLEngine")
        // new RunUpdate(this).start();
        return _update(id)
    }

    @Throws(IOException::class, ServletException::class)
    fun restart(password: Password?): Boolean {
        if (!singelton!!.can(CFMLEngine.CAN_RESTART_ALL, password)) throw IOException("Access denied to restart CFMLEngine")
        return _restart()
    }

    @Throws(IOException::class, ServletException::class)
    fun restart(configId: String, password: Password?): Boolean {
        if (!singelton!!.can(CFMLEngine.CAN_RESTART_CONTEXT, password)) throw IOException("Access denied to restart CFML Context (configId:$configId)")
        return _restart()
    }

    /**
     * restart the cfml engine
     *
     * @param password
     * @return has updated
     * @throws IOException
     * @throws ServletException
     */
    @Synchronized
    @Throws(ServletException::class)
    private fun _restart(): Boolean {
        if (singelton != null) singelton.reset()
        initEngine()
        val cs: ConfigServer? = getConfigServer(singelton)
        if (cs != null) {
            val log: Log = cs.getLog("application")
            log.info("loader", "Tachyon restarted")
        }
        System.gc()
        return true
    }

    /**
     * updates the engine when an update is available
     *
     * @return has updated
     * @throws IOException
     * @throws ServletException
     */
    @Throws(IOException::class, ServletException::class)
    private fun _update(id: Identification): Boolean {
        val newTachyon: File = downloadCore(id) ?: return false
        if (singelton != null) singelton.reset()
        val v: Version? = null
        try {
            bundleCollection = BundleLoader.loadBundles(this, felixCacheDirectory, bundleDirectory, newTachyon, bundleCollection)
            val e: CFMLEngine = getEngine(bundleCollection) ?: throw IOException("Failed to load engine")
            version = e.getInfo().getVersion()
            // engine = e;
            setEngine(e)
            // e.reset();
            callListeners(e)
            val cs: ConfigServer? = getConfigServer(e)
            if (cs != null) {
                val log: Log = cs.getLog("deploy")
                log.info("loader", "Tachyon Version [$v] installed")
            }
        } catch (e: Exception) {
            System.gc()
            try {
                newTachyon.delete()
            } catch (ee: Exception) {
            }
            log(e)
            e.printStackTrace()
            return false
        }
        log(Logger.LOG_DEBUG, "Version ($v)installed")
        return true
    }

    private fun getConfigServer(engine: CFMLEngine?): ConfigServer? {
        var engine: CFMLEngine = engine ?: return null
        if (engine is CFMLEngineWrapper) engine = engine.getEngine()
        try {
            val m: Method = engine.getClass().getDeclaredMethod("getConfigServerImpl", arrayOf<Class>())
            m.setAccessible(true)
            return m.invoke(engine, arrayOf<Object>()) as ConfigServer
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    @Throws(IOException::class)
    fun downloadBundle(symbolicName: String, symbolicVersion: String, id: Identification?): File? {
        var id: Identification? = id
        val jarDir: File? = bundleDirectory

        // before we download we check if we have it bundled
        var jar: File? = deployBundledBundle(jarDir, symbolicName, symbolicVersion)
        if (jar != null && jar.isFile()) return jar
        if (jar != null) {
            log(Logger.LOG_INFO, jar.toString() + " should exist but does not (exist?" + jar.exists() + ";file?" + jar.isFile() + ";hidden?" + jar.isHidden() + ")")
        }
        val str: String = Util._getSystemPropOrEnvVar("tachyon.enable.bundle.download", null)
        if (str != null && ("false".equalsIgnoreCase(str) || "no".equalsIgnoreCase(str))) { // we do not use CFMLEngine to cast, because the engine may not exist yet
            throw RuntimeException("Tachyon is missing the Bundle jar, " + symbolicName + ":" + symbolicVersion
                    + ", and has been prevented from downloading it. If this jar is not a core jar, it will need to be manually downloaded and placed in the {{tachyon-server}}/context/bundles directory.")
        }
        jar = File(jarDir, symbolicName.replace('.', '-').toString() + "-" + symbolicVersion.replace('.', '-') + ".jar")
        val updateProvider: URL? = updateLocation
        if (id == null && singelton != null) id = singelton.getIdentification()
        val updateUrl = URL(updateProvider, "/rest/update/provider/download/" + symbolicName + "/" + symbolicVersion + "/" + (if (id != null) id.toQueryString() else "")
                + (if (id == null) "?" else "&") + "allowRedirect=true&jv=" + System.getProperty("java.version")
        )
        log(Logger.LOG_INFO, "Downloading bundle [$symbolicName:$symbolicVersion] from $updateUrl and copying to $jar")
        var code: Int
        var conn: HttpURLConnection
        try {
            conn = updateUrl.openConnection() as HttpURLConnection
            conn.setRequestMethod("GET")
            conn.setConnectTimeout(10000)
            conn.connect()
            code = conn.getResponseCode()
        } catch (e: UnknownHostException) {
            log(Logger.LOG_ERROR, "Failed to download the bundle  [$symbolicName:$symbolicVersion] from [$updateUrl] and copy to [$jar]") // MUST
            throw IOException("Failed to download the bundle  [$symbolicName:$symbolicVersion] from [$updateUrl] and copy to [$jar]", e)
        }
        // the update provider is not providing a download for this
        if (code != 200) {

            // the update provider can also provide a different (final) location for this
            var count = 1
            while ((code == 302 || code == 301) && count++ <= MAX_REDIRECTS) {
                var location: String = conn.getHeaderField("Location")
                // just in case we check invalid names
                if (location == null) location = conn.getHeaderField("location")
                if (location == null) location = conn.getHeaderField("LOCATION")
                log(Logger.LOG_INFO, "download redirected:$location")
                conn.disconnect()
                val url = URL(location)
                try {
                    conn = url.openConnection() as HttpURLConnection
                    conn.setRequestMethod("GET")
                    conn.setConnectTimeout(10000)
                    conn.connect()
                    code = conn.getResponseCode()
                } catch (e: UnknownHostException) {
                    log(e)
                    throw IOException("Failed to download the bundle  [$symbolicName:$symbolicVersion] from [$location] and copy to [$jar]", e)
                }
            }

            // no download available!
            if (code != 200) {
                val msg = ("Failed to download the bundle for [" + symbolicName + "] in version [" + symbolicVersion + "] from [" + updateUrl
                        + "], please download manually and copy to [" + jarDir + "]")
                log(Logger.LOG_ERROR, msg)
                conn.disconnect()
                throw IOException(msg)
            }
        }

        // if(jar.createNewFile()) {
        copy(conn.getContent() as InputStream, FileOutputStream(jar))
        conn.disconnect()
        return jar
        /*
		 * } else { throw new IOException("File ["+jar.getName()+"] already exists, won't copy new one"); }
		 */
    }

    private fun deployBundledBundle(bundleDirectory: File?, symbolicName: String, symbolicVersion: String): File? {
        val sub = "bundles/"
        val nameAndVersion = "$symbolicName|$symbolicVersion"
        val osgiFileName = "$symbolicName-$symbolicVersion.jar"
        val pack20Ext = ".jar.pack.gz"
        var isPack200 = false

        // first we look for an exact match
        var `is`: InputStream = getClass().getResourceAsStream("bundles/$osgiFileName")
        if (`is` == null) `is` = getClass().getResourceAsStream("/bundles/$osgiFileName")
        if (`is` != null) log(Logger.LOG_DEBUG, "Found ]/bundles/$osgiFileName] in tachyon.jar") else log(Logger.LOG_INFO, "Could not find [/bundles/$osgiFileName] in tachyon.jar")
        if (`is` == null) {
            `is` = getClass().getResourceAsStream("bundles/$osgiFileName$pack20Ext")
            if (`is` == null) `is` = getClass().getResourceAsStream("/bundles/$osgiFileName$pack20Ext")
            isPack200 = true
            if (`is` != null) log(Logger.LOG_DEBUG, "Found [/bundles/$osgiFileName$pack20Ext] in tachyon.jar") else log(Logger.LOG_INFO, "Could not find [/bundles/$osgiFileName$pack20Ext] in tachyon.jar")
        }
        if (`is` != null) {
            var temp: File? = null
            try {
                // copy to temp file
                temp = File.createTempFile("bundle", ".tmp")
                log(Logger.LOG_DEBUG, "Copying [tachyon.jar!/bundles/$osgiFileName$pack20Ext] to [$temp]")
                Util.copy(BufferedInputStream(`is`), FileOutputStream(temp), true, true)
                if (isPack200) {
                    val temp2: File = File.createTempFile("bundle", ".tmp2")
                    Pack200Util.pack2Jar(temp, temp2)
                    log(Logger.LOG_DEBUG, "Upack [$temp] to [$temp2]")
                    temp.delete()
                    temp = temp2
                }

                // adding bundle
                val trg = File(bundleDirectory, osgiFileName)
                fileMove(temp, trg)
                log(Logger.LOG_DEBUG, "Adding bundle [$symbolicName] in version [$symbolicVersion] to [$trg]")
                return trg
            } catch (ioe: IOException) {
                ioe.printStackTrace()
            } finally {
                if (temp != null && temp.exists()) temp.delete()
            }
        }

        // now we search the current jar as an external zip what is slow (we do not support pack200 in this
        // case)
        // this also not works with windows
        if (isWindows) return null
        var entry: ZipEntry
        var temp: File?
        var zis: ZipInputStream? = null
        try {
            val src: CodeSource = CFMLEngineFactory::class.java.getProtectionDomain().getCodeSource()
                    ?: return null
            val loc: URL = src.getLocation()
            zis = ZipInputStream(loc.openStream())
            var path: String
            var name: String?
            var bundleInfo: String
            var index: Int
            while (zis.getNextEntry().also { entry = it } != null) {
                temp = null
                path = entry.getName().replace('\\', '/')
                if (path.startsWith("/")) path = path.substring(1) // some zip path start with "/" some not
                isPack200 = false
                if (path.startsWith(sub) && path.endsWith(".jar") /* || (isPack200=path.endsWith(".jar.pack.gz")) */) { // ignore non jar files or file from elsewhere
                    index = path.lastIndexOf('/') + 1
                    if (index == sub.length()) { // ignore sub directories
                        name = path.substring(index)
                        temp = null
                        try {
                            temp = File.createTempFile("bundle", ".tmp")
                            Util.copy(zis, FileOutputStream(temp), false, true)

                            /*
							 * if(isPack200) { File temp2 = File.createTempFile("bundle", ".tmp2"); Pack200Util.pack2Jar(temp,
							 * temp2); temp.delete(); temp=temp2; name=name.substring(0,name.length()-".pack.gz".length()); }
							 */bundleInfo = BundleLoader.loadBundleInfo(temp)
                            if (bundleInfo != null && nameAndVersion.equals(bundleInfo)) {
                                val trg = File(bundleDirectory, name)
                                temp.renameTo(trg)
                                log(Logger.LOG_DEBUG, "Adding bundle [$symbolicName] in version [$symbolicVersion] to [$trg]")
                                return trg
                            }
                        } finally {
                            if (temp != null && temp.exists()) temp.delete()
                        }
                    }
                }
                zis.closeEntry()
            }
        } catch (t: Throwable) {
            if (t is ThreadDeath) throw t as ThreadDeath
        } finally {
            Util.closeEL(zis)
        }
        return null
    }

    private val isWindows: Boolean
        private get() {
            val os: String = System.getProperty("os.name").toLowerCase()
            return os.startsWith("windows")
        }

    @Throws(IOException::class)
    private fun downloadCore(id: Identification): File? {
        var id: Identification? = id
        val updateProvider: URL? = updateLocation
        if (id == null && singelton != null) id = singelton.getIdentification()

        // only happens when the code runs from the debug project
        if (version == null) version = instance.getInfo().getVersion()
        val infoUrl = URL(updateProvider, "/rest/update/provider/update-for/" + version.toString().toString() + if (id != null) id.toQueryString() else "")
        log(Logger.LOG_DEBUG, "Checking for core update at [$updateProvider]")
        var strAvailableVersion: String = toString(infoUrl.getContent() as InputStream).trim()
        log(Logger.LOG_DEBUG, "Update provider reports an updated core version available [$strAvailableVersion] ")
        strAvailableVersion = CFMLEngineFactorySupport.removeQuotes(strAvailableVersion, true)
        if (strAvailableVersion.length() === 0 || !Util.isNewerThan(toVersion(strAvailableVersion, VERSION_ZERO), version)) {
            log(Logger.LOG_DEBUG, "There is no newer Version available")
            return null
        }
        log(Logger.LOG_INFO, """Found a newer Version 
 - current Version [${version.toString().toString()}]
 - available Version [$strAvailableVersion]""")
        val updateUrl = URL(updateProvider,
                "/rest/update/provider/download/" + strAvailableVersion + (if (id != null) id.toQueryString() else "") + (if (id == null) "?" else "&") + "allowRedirect=true")
        log(Logger.LOG_INFO, "Downloading core update from [$updateUrl]")

        // local resource
        val patchDir: File? = patchDirectory
        val newTachyon = File(patchDir, "$strAvailableVersion.lco")
        ////
        var code: Int
        var conn: HttpURLConnection
        try {
            conn = updateUrl.openConnection() as HttpURLConnection
            conn.setRequestMethod("GET")
            conn.setConnectTimeout(10000)
            conn.connect()
            code = conn.getResponseCode()
        } catch (e: UnknownHostException) {
            log(e)
            throw e
        }

        // the update provider is not providing a download for this
        if (code != 200) {

            // the update provider can also provide a different (final) location for this
            if (code == 302) {
                var location: String = conn.getHeaderField("Location")
                // just in case we check invalid names
                if (location == null) location = conn.getHeaderField("location")
                if (location == null) location = conn.getHeaderField("LOCATION")
                log(Logger.LOG_DEBUG, "download redirected to $location")
                conn.disconnect()
                val url = URL(location)
                try {
                    conn = url.openConnection() as HttpURLConnection
                    conn.setRequestMethod("GET")
                    conn.setConnectTimeout(10000)
                    conn.connect()
                    code = conn.getResponseCode()
                } catch (e: UnknownHostException) {
                    log(e)
                    throw e
                }
            }

            // no download available!
            if (code != 200) {
                val msg = ("Tachyon failed to download the core for version [" + version.toString().toString() + "] from " + updateUrl.toString() + ", please download it manually and copy to ["
                        + patchDir.toString() + "]")
                log(Logger.LOG_ERROR, msg)
                conn.disconnect()
                throw IOException(msg)
            }
        }

        // copy it to local directory
        if (newTachyon.createNewFile()) {
            copy(conn.getContent() as InputStream, FileOutputStream(newTachyon))
            conn.disconnect()

            // when it is a loader extract the core from it
            val tmp: File? = extractCoreIfLoader(newTachyon)
            if (tmp != null) {
                log(Logger.LOG_DEBUG, "Extract core from loader")
                newTachyon.delete()
                tmp.renameTo(newTachyon)
                tmp.delete()
            }
        } else {
            conn.disconnect()
            log(Logger.LOG_DEBUG, "File for new Version already exists, won't copy new one")
            return null
        }
        return newTachyon
    }
    // read location directly from xml

    // if there is no tachyon-server.xml
    @get:Throws(MalformedURLException::class)
    val updateLocation: URL?
        get() {
            var location: URL? = if (singelton == null) null else singelton.getUpdateLocation()

            // read location directly from xml
            if (location == null) {
                val `is`: InputStream? = null
                try {
                    val xml = File(getResourceRoot(), "context/tachyon-server.xml")
                    if (xml.exists() || xml.length() > 0) {
                        val dbFactory: DocumentBuilderFactory = DocumentBuilderFactory.newInstance()
                        val dBuilder: DocumentBuilder = dbFactory.newDocumentBuilder()
                        val doc: Document = dBuilder.parse(xml)
                        val root: Element = doc.getDocumentElement()
                        val children: NodeList = root.getChildNodes()
                        for (i in children.getLength() - 1 downTo 0) {
                            val node: Node = children.item(i)
                            if (node.getNodeType() === Node.ELEMENT_NODE && node.getNodeName().equals("update")) {
                                val loc: String = (node as Element).getAttribute("location")
                                if (!Util.isEmpty(loc)) location = URL(loc)
                            }
                        }
                    }
                } catch (t: Throwable) {
                    t.printStackTrace()
                } finally {
                    CFMLEngineFactorySupport.closeEL(`is`)
                }
            }

            // if there is no tachyon-server.xml
            if (location == null) location = URL(UPDATE_LOCATION)
            return location
        }

    /**
     * method to initialize an update of the CFML Engine. checks if there is a new Version and update it
     * when a new version is available
     *
     * @param password password
     * @return has updated
     * @throws IOException io exception
     * @throws ServletException servlet exception
     */
    @Throws(IOException::class, ServletException::class)
    fun removeUpdate(password: Password?): Boolean {
        if (!singelton!!.can(CFMLEngine.CAN_UPDATE, password)) throw IOException("Access denied to update CFMLEngine")
        return removeUpdate()
    }

    /**
     * method to initialize an update of the CFML Engine. checks if there is a new Version and update it
     * when a new version is available
     *
     * @param password password for tachyon
     * @return has updated
     * @throws IOException io exception
     * @throws ServletException servlet exception
     */
    @Throws(IOException::class, ServletException::class)
    fun removeLatestUpdate(password: Password?): Boolean {
        if (!singelton!!.can(CFMLEngine.CAN_UPDATE, password)) throw IOException("Access denied to update CFMLEngine")
        return removeLatestUpdate()
    }

    /**
     * updates the engine when an update is available
     *
     * @return has updated
     * @throws IOException io exception
     * @throws ServletException servlet exception
     */
    @Throws(IOException::class, ServletException::class)
    private fun removeUpdate(): Boolean {
        val patchDir: File? = patchDirectory
        val patches: Array<File> = patchDir.listFiles(ExtensionFilter(arrayOf("rc", "rcs")))
        for (i in patches.indices) if (!patches[i].delete()) patches[i].deleteOnExit()
        _restart()
        return true
    }

    @Throws(IOException::class, ServletException::class)
    private fun removeLatestUpdate(): Boolean {
        val patchDir: File? = patchDirectory
        val patches: Array<File> = patchDir.listFiles(ExtensionFilter(arrayOf(".lco")))
        var patch: File? = null
        for (patche in patches) if (patch == null || Util.isNewerThan(toVersion(patche.getName(), VERSION_ZERO), toVersion(patch.getName(), VERSION_ZERO))) patch = patche
        if (patch != null && !patch.delete()) patch.deleteOnExit()
        _restart()
        return true
    }

    @get:Throws(ServletException::class, IOException::class)
    val installedPatches: Array<String>
        get() {
            val patchDir: File? = patchDirectory
            val patches: Array<File> = patchDir.listFiles(ExtensionFilter(arrayOf(".lco")))
            val list: List<String> = ArrayList<String>()
            var name: String
            val extLen: Int = "rc".length() + 1
            for (patche in patches) {
                name = patche.getName()
                name = name.substring(0, name.length() - extLen)
                list.add(name)
            }
            val arr: Array<String> = list.toArray(arrayOfNulls<String>(list.size()))
            Arrays.sort(arr)
            return arr
        }

    /**
     * call all registered listener for update of the engine
     *
     * @param engine
     */
    private fun callListeners(engine: CFMLEngine) {
        val it: Iterator<EngineChangeListener> = listeners.iterator()
        while (it.hasNext()) it.next().onUpdate()
    }

    @get:Throws(IOException::class)
    val patchDirectory: File?
        get() {
            var pd: File? = getDirectoryByPropOrEnv("tachyon.patches.dir")
            if (pd != null) return pd
            pd = File(getResourceRoot(), "patches")
            if (!pd.exists()) pd.mkdirs()
            return pd
        }

    @get:Throws(IOException::class)
    val bundleDirectory: File?
        get() {
            var bd: File? = getDirectoryByPropOrEnv("tachyon.bundles.dir")
            if (bd != null) return bd
            bd = File(getResourceRoot(), "bundles")
            if (!bd.exists()) bd.mkdirs()
            return bd
        }

    // File bd = new File(getResourceRoot(),"felix-cache");
    // if(!bd.exists())bd.mkdirs();
    // return bd;
    @get:Throws(IOException::class)
    val felixCacheDirectory: File?
        get() = getResourceRoot()
    // File bd = new File(getResourceRoot(),"felix-cache");
    // if(!bd.exists())bd.mkdirs();
    // return bd;
    /**
     * return directory to tachyon resource root
     *
     * @return tachyon root directory
     * @throws IOException exception thrown
     */
    @Throws(IOException::class)
    fun getResourceRoot(): File? {
        if (resourceRoot == null) {
            resourceRoot = File(_getResourceRoot(), "tachyon-server")
            if (!resourceRoot.exists()) resourceRoot.mkdirs()
        }
        return resourceRoot
    }

    /**
     * @return return running context root
     * @throws IOException
     * @throws IOException
     */
    @Throws(IOException::class)
    private fun _getResourceRoot(): File? {

        // custom configuration
        if (tachyonServerRoot == null) readInitParam(config)
        if (tachyonServerRoot != null) return tachyonServerRoot
        val lbd: File? = getDirectoryByPropOrEnv("tachyon.base.dir") // directory defined by the caller
        var root: File? = lbd
        // get the root directory
        if (root == null) root = getDirectoryByProp("jboss.server.home.dir") // Jboss/Jetty|Tomcat
        if (root == null) root = getDirectoryByProp("jonas.base") // Jonas
        if (root == null) root = getDirectoryByProp("catalina.base") // Tomcat
        if (root == null) root = getDirectoryByProp("jetty.home") // Jetty
        if (root == null) root = getDirectoryByProp("org.apache.geronimo.base.dir") // Geronimo
        if (root == null) root = getDirectoryByProp("com.sun.aas.instanceRoot") // Glassfish
        if (root == null) root = getDirectoryByProp("env.DOMAIN_HOME") // weblogic
        if (root == null) root = getClassLoaderRoot(mainClassLoader).getParentFile().getParentFile()
        val classicRoot: File? = getClassLoaderRoot(mainClassLoader)

        // in case of a war file the server root need to be with the context
        if (lbd == null) {
            val webInf: File? = getWebInfFolder(classicRoot)
            if (webInf != null) {
                root = webInf
                if (!root.exists()) root.mkdir()
                log(Logger.LOG_DEBUG, "war-root-directory:$root")
            }
        }
        log(Logger.LOG_DEBUG, "root-directory:$root")
        if (root == null) throw IOException("Can't locate the root of the servlet container, please define a location (physical path) for the server configuration"
                + " with help of the servlet init param [tachyon-server-directory] in the web.xml where the Tachyon Servlet is defined" + " or the system property [tachyon.base.dir].")
        val modernDir = File(root, "tachyon-server")
        if (true) {
            // there is a server context in the old tachyon location, move that one
            var classicDir: File
            log(Logger.LOG_DEBUG, "classic-root-directory:$classicRoot")
            var had = false
            if (classicRoot.isDirectory() && File(classicRoot, "tachyon-server").also { classicDir = it }.isDirectory()) {
                log(Logger.LOG_DEBUG, "had tachyon-server classic$classicDir")
                moveContent(classicDir, modernDir)
                had = true
            }
            // there is a railo context
            if (!had && classicRoot.isDirectory() && File(classicRoot, "railo-server").also { classicDir = it }.isDirectory()) {
                log(Logger.LOG_DEBUG, "Had railo-server classic$classicDir")
                // check if there is a Railo context
                copyRecursiveAndRename(classicDir, modernDir)
                // zip the railo-server di and delete it (optional)
                try {
                    ZipUtil.zip(classicDir, File(root, "railo-server-context-old.zip"))
                    Util.delete(classicDir)
                } catch (t: Throwable) {
                    t.printStackTrace()
                }
                // moveContent(classicDir,new File(root,"tachyon-server"));
            }
        }
        return root
    }

    @Throws(IOException::class)
    private fun moveContent(src: File, trg: File) {
        if (src.isDirectory()) {
            val children: Array<File> = src.listFiles()
            if (children != null) for (element in children) moveContent(element, File(trg, element.getName()))
            src.delete()
        } else if (src.isFile()) {
            trg.getParentFile().mkdirs()
            src.renameTo(trg)
        }
    }

    private fun getDirectoryByPropOrEnv(name: String): File? {
        val file: File? = getDirectoryByProp(name)
        return if (file != null) file else getDirectoryByEnv(name)
    }

    private fun getDirectoryByProp(name: String): File? {
        return _getDirectoryBy(System.getProperty(name))
    }

    private fun getDirectoryByEnv(name: String): File? {
        return _getDirectoryBy(System.getenv(name))
    }

    private fun _getDirectoryBy(value: String): File? {
        if (Util.isEmpty(value, true)) return null
        val dir = File(value)
        dir.mkdirs()
        return if (dir.isDirectory()) dir else null
    }

    /**
     * Load CFMl Engine Implementation (tachyon.runtime.engine.CFMLEngineImpl) from a Classloader
     *
     * @param bundle
     * @return
     * @throws ClassNotFoundException
     * @throws SecurityException
     * @throws NoSuchMethodException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    @Throws(ClassNotFoundException::class, SecurityException::class, NoSuchMethodException::class, IllegalArgumentException::class, IllegalAccessException::class, InvocationTargetException::class)
    private fun getEngine(bc: BundleCollection?): CFMLEngine {
        log(Logger.LOG_DEBUG, "state: " + BundleUtil.bundleState(bc.core.getState(), ""))
        // bundle.getBundleContext().getServiceReference(CFMLEngine.class.getName());
        log(Logger.LOG_DEBUG, Constants.FRAMEWORK_BOOTDELEGATION.toString() + ":" + bc.getBundleContext().getProperty(Constants.FRAMEWORK_BOOTDELEGATION))
        log(Logger.LOG_DEBUG, "felix.cache.rootdir: " + bc.getBundleContext().getProperty("felix.cache.rootdir"))

        // log(Logger.LOG_DEBUG,bc.master.loadClass(TP.class.getName()).getClassLoader().toString());
        val clazz: Class<*> = bc.core.loadClass("tachyon.runtime.engine.CFMLEngineImpl")
        log(Logger.LOG_DEBUG, "class:" + clazz.getName())
        val m: Method = clazz.getMethod("getInstance", arrayOf<Class>(CFMLEngineFactory::class.java, BundleCollection::class.java))
        return m.invoke(null, arrayOf(this, bc))
    }

    private inner class UpdateChecker private constructor(private val factory: CFMLEngineFactory, id: Identification) : Thread() {
        private val id: Identification
        @Override
        fun run() {
            var time: Long = 10000
            while (true) try {
                sleep(time)
                time = (1000 * 60 * 60 * 24).toLong()
                factory._update(id)
            } catch (e: Exception) {
            }
        }

        init {
            this.id = id
        }
    }

    fun getLogger(): Logger? {
        return logger
    }

    companion object {
        // set to false to disable patch loading, for example in major alpha releases
        private const val PATCH_ENABLED = true
        val VERSION_ZERO: Version = Version(0, 0, 0, "0")
        private const val UPDATE_LOCATION = "https://update.tachyon.org" // MUST from server.xml
        private const val GB1 = (1024 * 1024 * 1024).toLong()
        private const val MB100 = (1024 * 1024 * 100).toLong()
        private const val MAX_REDIRECTS = 5
        private var factory: CFMLEngineFactory? = null

        // private static CFMLEngineWrapper engineListener;
        private var singelton: CFMLEngineWrapper? = null
        private var tachyonServerRoot: File? = null

        /**
         * returns instance of this factory (singelton = always the same instance) do auto update when
         * changes occur
         *
         * @param config servlet config
         * @return Singelton Instance of the Factory
         * @throws ServletException servlet exception
         */
        @Synchronized
        @Throws(ServletException::class)
        fun getInstance(config: ServletConfig): CFMLEngine? {
            if (singelton != null) {
                if (factory == null) factory = singelton.getCFMLEngineFactory() // not sure if this ever is done, but it does not hurt
                return singelton
            }
            if (factory == null) factory = CFMLEngineFactory(config)

            // read init param from config
            factory!!.readInitParam(config)
            factory!!.initEngineIfNecessary()
            singelton!!.addServletConfig(config)

            // add listener for update
            // factory.addListener(singelton);
            return singelton
        }

        /**
         * returns instance of this factory (singelton = always the same instance) do auto update when
         * changes occur
         *
         * @return Singelton Instance of the Factory
         * @throws RuntimeException runtime exception
         */
        @get:Throws(RuntimeException::class)
        val instance: tachyon.loader.engine.CFMLEngine?
            get() {
                if (singelton != null) return singelton
                throw RuntimeException("Engine is not initialized, you must first call getInstance(ServletConfig)")
            }

        fun registerInstance(engine: CFMLEngine) {
            if (engine is CFMLEngineWrapper) throw RuntimeException("That should not happen!")
            setEngine(engine)
        }

        /**
         * returns instance of this factory (singelton always the same instance)
         *
         * @param config servlet config
         * @param listener listener
         * @return Singelton Instance of the Factory
         * @throws ServletException servlet exception
         */
        @Throws(ServletException::class)
        fun getInstance(config: ServletConfig, listener: EngineChangeListener): CFMLEngine? {
            getInstance(config)

            // add listener for update
            factory!!.addListener(listener)

            // read init param from config
            factory!!.readInitParam(config)
            factory!!.initEngineIfNecessary()
            singelton!!.addServletConfig(config)

            // make the FDController visible for the FDClient
            FDControllerFactory.makeVisible()
            return singelton
        }

        @Throws(IOException::class, BundleException::class)
        private fun getVersion(file: File): String {
            val jar = JarFile(file)
            return try {
                val manifest: Manifest = jar.getManifest()
                val attrs: Attributes = manifest.getMainAttributes()
                attrs.getValue("Bundle-Version")
            } finally {
                jar.close()
            }
        }

        private fun setEngine(engine: CFMLEngine): CFMLEngineWrapper? {
            // new RuntimeException("setEngine").printStackTrace();
            if (singelton == null) singelton = CFMLEngineWrapper(engine) else if (!singelton.isIdentical(engine)) {
                singelton.setEngine(engine) // reset of the old is made before
            } else {
                // new RuntimeException("useless call").printStackTrace();
            }
            return singelton
        }

        private fun extend(config: Map<String?, Object?>?, name: String, defaultValue: String?, add: Boolean) {
            var addional = getSystemPropOrEnvVar(name, null)
            if (Util.isEmpty(addional, true)) {
                if (Util.isEmpty(defaultValue, true)) return
                addional = defaultValue.trim()
            }
            if (add) {
                val existing = config!![name] as String?
                if (!Util.isEmpty(existing, true)) config.put(name, existing.trim().toString() + "," + addional.trim()) else config.put(name, addional.trim())
            } else {
                config.put(name, addional.trim())
            }
        }

        protected fun getSystemPropOrEnvVar(name: String, defaultValue: String?): String? {
            // env
            var name = name
            var value: String = System.getenv(name)
            if (!Util.isEmpty(value)) return value

            // prop
            value = System.getProperty(name)
            if (!Util.isEmpty(value)) return value

            // env 2
            name = name.replace('.', '_').toUpperCase()
            value = System.getenv(name)
            return if (!Util.isEmpty(value)) value else defaultValue
        }

        // FUTURE move to Util class
        @Throws(IOException::class)
        private fun fileMove(src: File?, dest: File) {
            val moved: Boolean = src.renameTo(dest)
            if (!moved) {
                val `is` = BufferedInputStream(FileInputStream(src))
                val os = BufferedOutputStream(FileOutputStream(dest))
                try {
                    Util.copy(`is`, os, false, false) // is set false here, because copy does not close in case of an exception
                } finally {
                    closeEL(`is`)
                    closeEL(os)
                }
                if (!src.delete()) src.deleteOnExit()
            }
        }

        fun extractCoreIfLoader(file: File?): File? {
            return try {
                _extractCoreIfLoader(file)
            } catch (e: IOException) {
                e.printStackTrace()
                null
            }
        }

        @Throws(IOException::class)
        fun _extractCoreIfLoader(file: File?): File? {
            val jf = JarFile(file)
            return try {
                // is it a tachyon loader ?
                val value: String = jf.getManifest().getMainAttributes().getValue("Main-Class")
                if (Util.isEmpty(value) || !value.equals("tachyon.runtime.script.Main")) return null

                // get the core file;
                val je: JarEntry = jf.getJarEntry("core/core.lco") ?: return null
                val `is`: InputStream = jf.getInputStream(je)
                val trg: File = File.createTempFile("tachyon", ".lco")
                val os: OutputStream = FileOutputStream(trg)
                try {
                    Util.copy(`is`, os)
                } finally {
                    Util.closeEL(`is`)
                    Util.closeEL(os)
                }
                trg
            } finally {
                jf.close()
            }
        }

        private fun getWebInfFolder(file: File?): File? {
            var file: File? = file
            var parent: File
            while (file != null && !file.getName().equals("WEB-INF")) {
                parent = file.getParentFile()
                if (file.equals(parent)) return null // this should not happen, simply to be sure
                file = parent
            }
            return file
        }

        @Throws(IOException::class)
        private fun copyRecursiveAndRename(src: File, trg: File) {
            var trg: File = trg
            if (!src.exists()) return
            if (src.isDirectory()) {
                if (!trg.exists()) trg.mkdirs()
                val files: Array<File> = src.listFiles()
                for (file in files) copyRecursiveAndRename(file, File(trg, file.getName()))
            } else if (src.isFile()) {
                if (trg.getName().endsWith(".rc") || trg.getName().startsWith(".")) return
                if (trg.getName().equals("railo-server.xml")) {
                    trg = File(trg.getParentFile(), "tachyon-server.xml")
                    // cfTachyonConfiguration
                    val `is` = FileInputStream(src)
                    val os = FileOutputStream(trg)
                    try {
                        var str: String = Util.toString(`is`)
                        str = str.replace("<cfRailoConfiguration", "<!-- copy from Railo context --><cfTachyonConfiguration")
                        str = str.replace("</cfRailoConfiguration", "</cfTachyonConfiguration")
                        str = str.replace("<railo-configuration", "<!-- copy from Railo context --><cfTachyonConfiguration")
                        str = str.replace("</railo-configuration", "</cfTachyonConfiguration")
                        str = str.replace("{railo-config}", "{tachyon-config}")
                        str = str.replace("{railo-server}", "{tachyon-server}")
                        str = str.replace("{railo-web}", "{tachyon-web}")
                        str = str.replace("\"railo.commons.", "\"tachyon.commons.")
                        str = str.replace("\"railo.runtime.", "\"tachyon.runtime.")
                        str = str.replace("\"railo.cfx.", "\"tachyon.cfx.")
                        str = str.replace("/railo-context.ra", "/tachyon-context.lar")
                        str = str.replace("/railo-context", "/tachyon")
                        str = str.replace("railo-server-context", "tachyon-server")
                        str = str.replace("http://www.getrailo.org", "https://release.tachyon.org")
                        str = str.replace("http://www.getrailo.com", "https://release.tachyon.org")
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
                val `is` = FileInputStream(src)
                val os = FileOutputStream(trg)
                try {
                    Util.copy(`is`, os)
                } finally {
                    Util.closeEL(`is`, os)
                }
            }
        }

        /**
         * returns the path where the classloader is located
         *
         * @param cl ClassLoader
         * @return file of the classloader root
         */
        fun getClassLoaderRoot(cl: ClassLoader): File? {
            val path = "tachyon/loader/engine/CFMLEngine.class"
            val res: URL = cl.getResource(path) ?: return null
            // get file and remove all after !
            var strFile: String? = null
            try {
                strFile = URLDecoder.decode(res.getFile().trim(), "iso-8859-1")
            } catch (e: UnsupportedEncodingException) {
            }
            var index: Int = strFile.indexOf('!')
            if (index != -1) strFile = strFile.substring(0, index)

            // remove path at the end
            index = strFile.lastIndexOf(path)
            if (index != -1) strFile = strFile.substring(0, index)

            // remove "file:" at start and tachyon.jar at the end
            if (strFile.startsWith("file:")) strFile = strFile.substring(5)
            if (strFile.endsWith("tachyon.jar")) strFile = strFile.substring(0, strFile!!.length() - 9)
            var file = File(strFile)
            if (file.isFile()) file = file.getParentFile()
            return file
        }
    }

    init {
        System.setProperty("org.apache.commons.logging.LogFactory.HashtableImpl", ConcurrentHashMapAsHashtable::class.java.getName())
        var logFile: File? = null
        this.config = config
        try {
            logFile = File(getResourceRoot(), "context/logs/felix.log")
            if (logFile.isFile()) {
                // more than a GB (from the time we did not control it)
                if (logFile.length() > GB1) {
                    logFile.delete() // we simply delete it
                } else if (logFile.length() > MB100) {
                    val bak = File(logFile.getParentFile(), "felix.1.log")
                    if (bak.isFile()) bak.delete()
                    logFile.renameTo(bak)
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        logFile.getParentFile().mkdirs()
        logger = LoggerImpl(logFile)
    }
}