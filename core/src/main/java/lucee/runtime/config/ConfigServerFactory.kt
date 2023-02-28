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

import java.io.IOException

/**
 *
 */
object ConfigServerFactory : ConfigFactory() {
    /**
     * creates a new ServletConfig Impl Object
     *
     * @param engine
     * @param initContextes
     * @param contextes
     * @param configDir
     * @return new Instance
     * @throws SAXException
     * @throws ClassNotFoundException
     * @throws PageException
     * @throws IOException
     * @throws TagLibException
     * @throws FunctionLibException
     * @throws BundleException
     * @throws ConverterException
     */
    @Throws(SAXException::class, ClassException::class, PageException::class, IOException::class, TagLibException::class, FunctionLibException::class, BundleException::class, ConverterException::class)
    fun newInstance(engine: CFMLEngineImpl?, initContextes: Map<String?, CFMLFactory?>?, contextes: Map<String?, CFMLFactory?>?, configDir: Resource?,
                    existing: ConfigServerImpl?, essentialOnly: Boolean): ConfigServerImpl? {
        val isCLI: Boolean = SystemUtil.isCLICall()
        if (isCLI) {
            val logs: Resource = configDir.getRealResource("logs")
            logs.mkdirs()
            val out: Resource = logs.getRealResource("out")
            val err: Resource = logs.getRealResource("err")
            ResourceUtil.touch(out)
            ResourceUtil.touch(err)
            if (logs is FileResource) {
                SystemUtil.setPrintWriter(SystemUtil.OUT, PrintWriter(out as FileResource))
                SystemUtil.setPrintWriter(SystemUtil.ERR, PrintWriter(err as FileResource))
            } else {
                SystemUtil.setPrintWriter(SystemUtil.OUT, PrintWriter(IOUtil.getWriter(out, "UTF-8")))
                SystemUtil.setPrintWriter(SystemUtil.ERR, PrintWriter(IOUtil.getWriter(err, "UTF-8")))
            }
        }
        LogUtil.logGlobal(ThreadLocalPageContext.getConfig(), Log.LEVEL_INFO, ConfigServerFactory::class.java.getName(),
                """
                    ===================================================================
                    SERVER CONTEXT
                    -------------------------------------------------------------------
                    - config:$configDir
                    - loader-version:${SystemUtil.getLoaderVersion()}
                    - core-version:${engine.getInfo().getVersion()}
                    ===================================================================
                    
                    """.trimIndent()
        )
        val ui: UpdateInfo = getNew(engine, configDir, false, UpdateInfo.NEW_NONE)
        val doNew = ui.updateType !== NEW_NONE
        val configFileOld: Resource = configDir.getRealResource("lucee-server.xml")
        val configFileNew: Resource = configDir.getRealResource(".CFConfig.json")
        var hasConfigOld = false
        var hasConfigNew = configFileNew.exists() && configFileNew.length() > 0
        if (!hasConfigNew) {
            hasConfigOld = configFileOld.exists() && configFileOld.length() > 0
        }
        val config: ConfigServerImpl = if (existing != null) existing else ConfigServerImpl(engine, initContextes, contextes, configDir, configFileNew, ui, essentialOnly)

        // translate to new
        if (!hasConfigNew) {
            if (hasConfigOld) {
                translateConfigFile(config, configFileOld, configFileNew, "multi", true)
            } else {
                createConfigFile("server", configFileNew)
                hasConfigNew = true
            }
        }
        val root: Struct = loadDocumentCreateIfFails(configFileNew, "server")
        load(config, root, false, doNew, essentialOnly)
        if (!essentialOnly) {
            val version: Double = ConfigWebUtil.getAsDouble("version", root, 1.0)
            val cleanupDatasources = version < 5.0
            createContextFiles(configDir, config, doNew, cleanupDatasources)
            (ConfigWebUtil.getEngine(config) as CFMLEngineImpl).onStart(config, false)
        }
        return config
    }

    /**
     * reloads the Config Object
     *
     * @param configServer
     * @throws SAXException
     * @throws ClassNotFoundException
     * @throws PageException
     * @throws IOException
     * @throws TagLibException
     * @throws FunctionLibException
     * @throws BundleException
     */
    @Throws(ClassException::class, PageException::class, IOException::class, TagLibException::class, FunctionLibException::class, BundleException::class)
    fun reloadInstance(engine: CFMLEngine?, configServer: ConfigServerImpl?) {
        val quick: Boolean = CFMLEngineImpl.quick(engine)
        val configFile: Resource = configServer!!.getConfigFile() ?: return
        if (second(configServer!!.getLoadTime()) > second(configFile.lastModified())) {
            if (!configServer!!.getConfigDir().getRealResource("password.txt").isFile()) return
        }
        val iDoNew: Int = getNew(engine, configServer!!.getConfigDir(), quick, UpdateInfo.NEW_NONE).updateType
        val doNew = iDoNew != NEW_NONE
        load(configServer, loadDocument(configFile), true, doNew, quick)
        (ConfigWebUtil.getEngine(configServer) as CFMLEngineImpl).onStart(configServer, true)
    }

    private fun second(ms: Long): Long {
        return ms / 1000
    }

    /**
     * @param configServer
     * @param doc
     * @throws ClassNotFoundException
     * @throws IOException
     * @throws FunctionLibException
     * @throws TagLibException
     * @throws PageException
     * @throws BundleException
     */
    @Throws(ClassException::class, PageException::class, IOException::class, TagLibException::class, FunctionLibException::class, BundleException::class)
    fun load(configServer: ConfigServerImpl?, root: Struct?, isReload: Boolean, doNew: Boolean, essentialOnly: Boolean) {
        ConfigBase.onlyFirstMatch = Caster.toBooleanValue(SystemUtil.getSystemPropOrEnvVar("lucee.mapping.first", null), false)
        ConfigWebFactory.load(null, configServer, root, isReload, doNew, essentialOnly)
        loadLabel(configServer, root)
    }

    private fun loadLabel(configServer: ConfigServerImpl?, root: Struct?) {
        val children: Array = ConfigWebUtil.getAsArray("labels", "label", root)
        val labels: Map<String?, String?> = HashMap<String?, String?>()
        if (children != null) {
            val it: Iterator<*> = children.getIterator()
            var data: Struct
            while (it.hasNext()) {
                data = Caster.toStruct(it.next(), null)
                if (data == null) continue
                val id: String = ConfigWebUtil.getAsString("id", data, null)
                val name: String = ConfigWebUtil.getAsString("name", data, null)
                if (id != null && name != null) {
                    labels.put(id, name)
                }
            }
        }
        configServer!!.setLabels(labels)
    }

    private fun createContextFiles(configDir: Resource?, config: ConfigServer?, doNew: Boolean, cleanupDatasources: Boolean) {
        val contextDir: Resource = configDir.getRealResource("context")
        val adminDir: Resource = contextDir.getRealResource("admin")

        // Debug
        val debug: Resource = adminDir.getRealResource("debug")
        create("/resource/context/admin/debug/", arrayOf<String?>("Debug.cfc", "Field.cfc", "Group.cfc", "Classic.cfc", "Simple.cfc", "Modern.cfc", "Comment.cfc"), debug, doNew)

        // DB Drivers types
        val dbDir: Resource = adminDir.getRealResource("dbdriver")
        val typesDir: Resource = dbDir.getRealResource("types")
        create("/resource/context/admin/dbdriver/types/", arrayOf<String?>("IDriver.cfc", "Driver.cfc", "IDatasource.cfc", "IDriverSelector.cfc", "Field.cfc"), typesDir, doNew)
        create("/resource/context/admin/dbdriver/", arrayOf<String?>("Other.cfc"), dbDir, doNew)

        // Cache Drivers
        val cDir: Resource = adminDir.getRealResource("cdriver")
        create("/resource/context/admin/cdriver/", arrayOf<String?>("Cache.cfc", "RamCache.cfc" // ,"EHCache.cfc"
                , "Field.cfc", "Group.cfc"), cDir, doNew)
        val wcdDir: Resource = configDir.getRealResource("web-context-deployment/admin")
        val cdDir: Resource = wcdDir.getRealResource("cdriver")
        try {
            ResourceUtil.deleteEmptyFolders(wcdDir)
        } catch (e: IOException) {
            LogUtil.logGlobal(ThreadLocalPageContext.getConfig(config), ConfigServerFactory::class.java.getName(), e)
        }

        // Mail Server Drivers
        val msDir: Resource = adminDir.getRealResource("mailservers")
        create("/resource/context/admin/mailservers/", arrayOf<String?>("Other.cfc", "GMail.cfc", "GMX.cfc", "iCloud.cfc", "Yahoo.cfc", "Outlook.cfc", "MailCom.cfc", "MailServer.cfc"), msDir, doNew)

        // Gateway Drivers
        val gDir: Resource = adminDir.getRealResource("gdriver")
        create("/resource/context/admin/gdriver/", arrayOf<String?>("TaskGatewayDriver.cfc", "AsynchronousEvents.cfc", "DirectoryWatcher.cfc", "MailWatcher.cfc", "Gateway.cfc", "Field.cfc", "Group.cfc"), gDir,
                doNew)

        // Logging/appender
        val app: Resource = adminDir.getRealResource("logging/appender")
        create("/resource/context/admin/logging/appender/", arrayOf<String?>("DatasourceAppender.cfc", "ConsoleAppender.cfc", "ResourceAppender.cfc", "Appender.cfc", "Field.cfc", "Group.cfc"), app, doNew)

        // Logging/layout
        val lay: Resource = adminDir.getRealResource("logging/layout")
        create("/resource/context/admin/logging/layout/", arrayOf<String?>("DatadogLayout.cfc", "ClassicLayout.cfc", "HTMLLayout.cfc", "PatternLayout.cfc", "XMLLayout.cfc", "Layout.cfc", "Field.cfc", "Group.cfc"), lay,
                doNew)

        // Security / SSL
        val secDir: Resource = configDir.getRealResource("security")
        if (!secDir.exists()) secDir.mkdirs()
        val res: Resource = create("/resource/security/", "cacerts", secDir, false)
        if (SystemUtil.getSystemPropOrEnvVar("lucee.use.lucee.SSL.TrustStore", "").equalsIgnoreCase("true")) System.setProperty("javax.net.ssl.trustStore", res.toString())
        // Allow using system proxies
        if (!SystemUtil.getSystemPropOrEnvVar("lucee.disable.systemProxies", "").equalsIgnoreCase("true")) System.setProperty("java.net.useSystemProxies", "true") // it defaults
        // to false

        // Jacob
        if (SystemUtil.isWindows()) {
            val binDir: Resource = configDir.getRealResource("bin")
            if (binDir != null) {
                if (!binDir.exists()) binDir.mkdirs()
                val name = if (SystemUtil.getJREArch() === SystemUtil.ARCH_64) "jacob-x64.dll" else "jacob-i586.dll"
                val jacob: Resource = binDir.getRealResource(name)
                if (!jacob.exists()) {
                    createFileFromResourceEL("/resource/bin/windows" + (if (SystemUtil.getJREArch() === SystemUtil.ARCH_64) "64" else "32") + "/" + name, jacob)
                }
                System.setProperty(LibraryLoader.JACOB_DLL_PATH, jacob.getAbsolutePath())
                System.setProperty(LibraryLoader.JACOB_DLL_NAME, name)
            }
        }
    }
}