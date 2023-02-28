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

import java.io.File

object DeployHandler {
    private val ALL_EXT: ResourceFilter? = ExtensionResourceFilter(arrayOf<String?>(".lex", ".lar", ".lco"))

    /**
     * deploys all files found
     *
     * @param config
     */
    fun deploy(config: Config?, log: Log?, force: Boolean) {
        if (!contextIsValid(config)) return
        synchronized(config) {
            val dir: Resource = config.getDeployDirectory()
            if (!dir.exists()) dir.mkdirs()

            // check deploy directory
            val children: Array<Resource?> = dir.listResources(ALL_EXT)
            var child: Resource?
            var ext: String
            for (i in children.indices) {
                child = children[i]
                try {
                    // Lucee archives
                    ext = ResourceUtil.getExtension(child, null)
                    if ("lar".equalsIgnoreCase(ext)) {
                        // deployArchive(config,child,true);
                        ConfigAdmin.updateArchive(config as ConfigPro?, child, true)
                    } else if ("lex".equalsIgnoreCase(ext)) ConfigAdmin._updateRHExtension(config as ConfigPro?, child, true, force) else if (config is ConfigServer && "lco".equalsIgnoreCase(ext)) ConfigAdmin.updateCore(config as ConfigServerImpl?, child, true)
                } catch (e: Exception) {
                    log.log(Log.LEVEL_ERROR, "deploy handler", e)
                }
            }

            // check env var for change
            if (config is ConfigServer) {
                var extensionIds: String = StringUtil.unwrap(SystemUtil.getSystemPropOrEnvVar("lucee-extensions", null)) // old no longer used
                if (StringUtil.isEmpty(extensionIds, true)) extensionIds = StringUtil.unwrap(SystemUtil.getSystemPropOrEnvVar("lucee.extensions", null))
                val engine: CFMLEngineImpl = ConfigWebUtil.getEngine(config) as CFMLEngineImpl
                if (engine != null && !StringUtil.isEmpty(extensionIds, true) && !extensionIds.equals(engine.getEnvExt())) {
                    try {
                        engine.setEnvExt(extensionIds)
                        val extensions: List<ExtensionDefintion?> = RHExtension.toExtensionDefinitions(extensionIds)
                        val configDir: Resource = CFMLEngineImpl.getSeverContextConfigDirectory(engine.getCFMLEngineFactory())
                        val sucess: Boolean = deployExtensions(config, extensions.toArray(arrayOfNulls<ExtensionDefintion?>(extensions.size())), log, force, false)
                        if (sucess && configDir != null) ConfigFactory.updateRequiredExtension(engine, configDir, log)
                        log.log(Log.LEVEL_INFO, "deploy handler",
                                (if (sucess) "Successfully installed" else "Failed to install") + " extensions: [" + ListUtil.listToList(extensions, ", ") + "]")
                    } catch (e: Exception) {
                        log.log(Log.LEVEL_ERROR, "deploy handler", e)
                    }
                }
            }
        }
    }

    private fun contextIsValid(config: Config?): Boolean {
        // this test is not very good but it works
        val webs: Array<ConfigWeb?>?
        if (config is ConfigWeb) webs = arrayOf<ConfigWeb?>(config as ConfigWeb?) else webs = (config as ConfigServer?).getConfigWebs()
        for (i in webs.indices) {
            try {
                ReqRspUtil.getRootPath(webs!![i].getServletContext())
            } catch (t: Throwable) {
                ExceptionUtil.rethrowIfNecessary(t)
                return false
            }
        }
        return true
    }

    fun moveToFailedFolder(deployDirectory: Resource?, res: Resource?) {
        val dir: Resource = deployDirectory.getRealResource("failed-to-deploy")
        val dst: Resource = dir.getRealResource(res.getName())
        dir.mkdirs()
        try {
            if (dst.exists()) dst.remove(true)
            ResourceUtil.moveTo(res, dst, true)
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
        }

        // TODO Auto-generated method stub
    }

    @Throws(PageException::class)
    fun deployExtensions(config: Config?, eds: Array<ExtensionDefintion?>?, log: Log?, force: Boolean, throwOnError: Boolean): Boolean {
        var allSucessfull = true
        if (!ArrayUtil.isEmpty(eds)) {
            var ed: ExtensionDefintion?
            var sucess: Boolean
            for (i in eds.indices) {
                ed = eds!![i]
                if (StringUtil.isEmpty(ed.getId(), true)) continue
                sucess = try {
                    deployExtension(config, ed, log, i + 1 == eds.size, force, throwOnError)
                } catch (e: PageException) {
                    if (throwOnError) throw e
                    if (log != null) log.error("deploy-extension", e) else LogUtil.log("deploy-extension", e)
                    false
                }
                if (!sucess) allSucessfull = false
            }
        }
        return allSucessfull
    }

    @Throws(PageException::class)
    fun deployExtensions(config: Config?, eds: List<ExtensionDefintion?>?, log: Log?, force: Boolean, throwOnError: Boolean): Boolean {
        var allSucessfull = true
        if (eds != null && eds.size() > 0) {
            var ed: ExtensionDefintion
            val it: Iterator<ExtensionDefintion?> = eds.iterator()
            var sucess: Boolean
            var count = 0
            while (it.hasNext()) {
                count++
                ed = it.next()
                if (StringUtil.isEmpty(ed.getId(), true)) continue
                sucess = try {
                    deployExtension(config, ed, log, count == eds.size(), force, throwOnError)
                } catch (e: PageException) {
                    if (throwOnError) throw e
                    if (log != null) log.error("deploy-extension", e) else LogUtil.log("deploy-extension", e)
                    false
                }
                if (!sucess) allSucessfull = false
            }
        }
        return allSucessfull
    }

    /**
     * install an extension based on the given id and version
     *
     * @param config
     * @param id the id of the extension
     * @param version pass null if you don't need a specific version
     * @return
     * @throws IOException
     * @throws PageException
     */
    @Throws(PageException::class)
    fun deployExtension(config: Config?, ed: ExtensionDefintion?, log: Log?, reload: Boolean, force: Boolean, throwOnError: Boolean): Boolean {
        val ci: ConfigPro? = config

        // is the extension already installed
        try {
            if (ConfigAdmin.hasRHExtensions(ci, ed) != null) return false
        } catch (e: Exception) {
            if (throwOnError) throw Caster.toPageException(e)
            if (log != null) log.error("extension", e) else LogUtil.log("extension", e)
        }

        // check if a local extension is matching our id
        val it: Iterator<ExtensionDefintion?> = getLocalExtensions(config, false)!!.iterator()
        var ext: ExtensionDefintion? = null
        var tmp: ExtensionDefintion?
        if (log != null) log.info("extension", "installing the extension $ed")
        while (it.hasNext()) {
            tmp = it.next()
            if (ed.equals(tmp)) {
                ext = tmp
                break
            }
        }

        // if we have one and also the defined version matches, there is no need to check online
        do {
            if (ext != null && ed.getVersion() != null) {
                var res: Resource? = null
                try {
                    if (log != null) log.info("extension", "Installing extension [$ed] from local provider")
                    res = SystemUtil.getTempDirectory().getRealResource(ed.getId().toString() + "-" + ed.getVersion() + ".lex")
                    ResourceUtil.touch(res)
                    IOUtil.copy(ext.getSource(), res)
                    ConfigAdmin._updateRHExtension(config as ConfigPro?, res, reload, force)
                    return true
                } catch (e: Exception) {
                    e.printStackTrace()
                    // check if the zip is valid
                    if (res is File) {
                        if (!IsZipFile.invoke(res as File?)) {
                            val engine: CFMLEngineImpl = CFMLEngineImpl.toCFMLEngineImpl(ConfigWebUtil.getEngine(config))
                            engine.deployBundledExtension(true)
                            if (IsZipFile.invoke(res as File?)) {
                                continue  // we start over that part
                            }
                        }
                    }
                    ext = null
                    LogUtil.log(config, DeployHandler::class.java.getName(), e)
                }
            }
            break
        } while (true)
        val id: Identification = config.getIdentification()
        val apiKey: String? = if (id == null) null else id.getApiKey()
        val providers: Array<RHExtensionProvider?> = ci!!.getRHExtensionProviders()
        var url: URL?

        // if we have a local version, we look if there is a newer remote version
        if (ext != null) {
            var content: String
            for (i in providers.indices) {
                var rsp: HTTPResponse? = null
                try {
                    url = providers[i].getURL()
                    val qs = StringBuilder()
                    qs.append("?withLogo=false")
                    if (ed.getVersion() != null) qs.append("&version=").append(ed.getVersion())
                    if (apiKey != null) qs.append("&ioid=").append(apiKey)
                    url = URL(url, "/rest/extension/provider/info/" + ed.getId() + qs)
                    if (log != null) log.info("extension", "Check for a newer version at [$url]")
                    rsp = HTTPEngine.get(url, null, null, 5000, false, "UTF-8", "", null, arrayOf<Header?>(HeaderImpl("accept", "application/json")))
                    if (rsp.getStatusCode() !== 200) continue
                    content = rsp.getContentAsString()
                    val sct: Struct = Caster.toStruct(DeserializeJSON.call(null, content))
                    val remoteVersion: String = Caster.toString(sct.get(KeyConstants._version))

                    // the local version is as good as the remote
                    if (remoteVersion != null && remoteVersion.compareTo(ext.getVersion()) <= 0) {
                        if (log != null) log.info("extension", "Installing extension [$ed] from local provider")

                        // avoid that the exzension from provider get removed
                        val res: Resource = SystemUtil.getTempDirectory().getRealResource(ed.getId().toString() + "-" + ed.getVersion() + ".lex")
                        ResourceUtil.touch(res)
                        IOUtil.copy(ext.getSource(), res)
                        ConfigAdmin._updateRHExtension(config as ConfigPro?, res, reload, force)
                        return true
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    if (log != null) log.error("extension", e)
                } finally {
                    HTTPEngine.closeEL(rsp)
                }
            }
        }

        // if we have an ext at this stage this mean the remote providers was not acessible or have not this
        // extension
        if (ext != null) {
            try {
                if (log != null) log.info("extension", "Installing extension [$ed] from local provider")
                val res: Resource = SystemUtil.getTempDirectory().getRealResource(ext.getSource().getName())
                ResourceUtil.touch(res)
                IOUtil.copy(ext.getSource(), res)
                ConfigAdmin._updateRHExtension(config as ConfigPro?, res, reload, force)
                return true
            } catch (e: Exception) {
                e.printStackTrace()
                if (log != null) log.error("extension", e)
            }
        }

        // if not we try to download it
        if (log != null) log.info("extension", "Installing extension [$ed] from remote extension provider")
        val res: Resource? = downloadExtension(ci, ed, log)
        if (res != null) {
            try {
                ConfigAdmin._updateRHExtension(config as ConfigPro?, res, reload, force)
                return true
            } catch (e: Exception) {
                e.printStackTrace()
                if (log != null) log.error("extension", e) else throw Caster.toPageException(e)
            }
        }
        throw ApplicationException("Failed to install extension [" + ed.getId().toString() + "]")
    }

    fun downloadExtension(config: Config?, ed: ExtensionDefintion?, log: Log?): Resource? {
        val id: Identification = config.getIdentification()
        val apiKey: String? = if (id == null) null else id.getApiKey()
        var url: URL?
        val providers: Array<RHExtensionProvider?> = (config as ConfigPro?)!!.getRHExtensionProviders()
        for (i in providers.indices) {
            var rsp: HTTPResponse? = null
            try {
                url = providers[i].getURL()
                val qs = StringBuilder()
                if (apiKey != null) addQueryParam(qs, "ioid", apiKey)
                addQueryParam(qs, "version", ed.getVersion())
                url = URL(url, "/rest/extension/provider/full/" + ed.getId() + qs)
                if (log != null) log.info("main", "Check for extension at [$url]")
                rsp = HTTPEngine.get(url, null, null, 5000, true, "UTF-8", "", null, arrayOf<Header?>(HeaderImpl("accept", "application/cfml")))

                // If status code indicates success
                if (rsp.getStatusCode() >= 200 && rsp.getStatusCode() < 300) {

                    // copy it locally
                    val res: Resource = SystemUtil.getTempDirectory().getRealResource(ed.getId().toString() + "-" + ed.getVersion() + ".lex")
                    ResourceUtil.touch(res)
                    IOUtil.copy(rsp.getContentAsStream(), res, true)
                    if (log != null) log.info("main", "Downloaded extension [$ed] to [$res]")
                    return res
                } else {
                    if (log != null) log.warn("main", "Failed (" + rsp.getStatusCode().toString() + ") to load extension: [" + ed.toString() + "] from [" + url.toString() + "]")
                }
            } catch (e: Exception) {
                if (log != null) log.error("extension", e)
            } finally {
                HTTPEngine.closeEL(rsp)
            }
        }
        return null
    }

    private fun addQueryParam(qs: StringBuilder?, name: String?, value: String?) {
        if (StringUtil.isEmpty(value)) return
        qs.append(if (qs.length() === 0) "?" else "&").append(name).append("=").append(value)
    }

    fun getExtension(config: Config?, ed: ExtensionDefintion?, log: Log?): Resource? {
        // local
        val ext: ExtensionDefintion? = getLocalExtension(config, ed, null)
        if (ext != null) {
            try {
                val src: Resource = ext.getSource()
                if (src.exists()) {
                    val res: Resource = SystemUtil.getTempDirectory().getRealResource(ed.getId().toString() + "-" + ed.getVersion() + ".lex")
                    ResourceUtil.touch(res)
                    IOUtil.copy(ext.getSource(), res)
                    return res
                }
            } catch (e: Exception) {
            }
        }
        // remote
        return downloadExtension(config, ed, log)
    }

    fun getLocalExtension(config: Config?, ed: ExtensionDefintion?, defaultValue: ExtensionDefintion?): ExtensionDefintion? {
        val it: Iterator<ExtensionDefintion?> = getLocalExtensions(config, false)!!.iterator()
        var ext: ExtensionDefintion?
        while (it.hasNext()) {
            ext = it.next()
            if (ed.equals(ext)) {
                return ext
            }
        }
        return defaultValue
    }

    fun getLocalExtensions(config: Config?, validate: Boolean): List<ExtensionDefintion?>? {
        return (config as ConfigPro?)!!.loadLocalExtensions(validate)
    }

    @Throws(PageException::class)
    fun deployExtension(config: ConfigPro?, ext: Resource?) {
        ConfigAdmin._updateRHExtension(config, ext, true, true)
    }
}