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
package lucee.runtime.extension

import java.io.ByteArrayInputStream

/**
 * Extension completely handled by the engine and not by the Install/config.xml
 */
class RHExtension : Serializable {
    private var id: String? = null
    private var releaseType = 0
    private var version: String? = null
    private var name: String? = null
    private var symbolicName: String? = null
    private var description: String? = null
    private var trial = false
    private var image: String? = null
    private var startBundles = false
    private var bundles: Array<BundleInfo?>?
    private var jars: Array<String?>?
    private var flds: Array<String?>?
    private var tlds: Array<String?>?
    private var tags: Array<String?>?
    private var functions: Array<String?>?
    private var archives: Array<String?>?
    private var applications: Array<String?>?
    private var components: Array<String?>?
    private var plugins: Array<String?>?
    private var contexts: Array<String?>?
    private var configs: Array<String?>?
    private var webContexts: Array<String?>?
    private var categories: Array<String?>?
    private var gateways: Array<String?>?
    private var caches: List<Map<String?, String?>?>? = null
    private var cacheHandlers: List<Map<String?, String?>?>? = null
    private var orms: List<Map<String?, String?>?>? = null
    private var webservices: List<Map<String?, String?>?>? = null
    private var monitors: List<Map<String?, String?>?>? = null
    private var resources: List<Map<String?, String?>?>? = null
    private var searchs: List<Map<String?, String?>?>? = null
    private var amfs: List<Map<String?, String?>?>? = null
    private var jdbcs: List<Map<String?, String?>?>? = null
    private var startupHooks: List<Map<String?, String?>?>? = null
    private var mappings: List<Map<String?, String?>?>? = null
    private var eventGatewayInstances: List<Map<String?, Object?>?>? = null
    private var extensionFile: Resource? = null
    private var type: String? = null
    private var minCoreVersion: VersionRange? = null
    private var minLoaderVersion = 0.0
    private var amfsJson: String? = null
    private var resourcesJson: String? = null

    // private Config config;
    private var searchsJson: String? = null
    private var ormsJson: String? = null
    private var webservicesJson: String? = null
    private var monitorsJson: String? = null
    private var cachesJson: String? = null
    private var cacheHandlersJson: String? = null
    private var jdbcsJson: String? = null
    private var startupHooksJson: String? = null
    private var mappingsJson: String? = null
    private var eventGatewayInstancesJson: String? = null
    private var loaded = false
    private val config: Config?
    val softLoaded: Boolean

    constructor(config: ConfigPro?, id: String?, version: String?, resource: String?, installIfNecessary: Boolean) {
        this.config = config
        // we have a newer version that holds the Manifest data
        var res: Resource?
        if (installIfNecessary) {
            res = if (StringUtil.isEmpty(version)) null else toResource(config, id, version, null)
            if (res == null) {
                if (!StringUtil.isEmpty(resource) && ResourceUtil.toResourceExisting(config, resource, null).also { res = it } != null) {
                    DeployHandler.deployExtension(config, res)
                } else {
                    DeployHandler.deployExtension(config, ExtensionDefintion(id, version), null, false, true, true)
                    res = toResource(config, id, version)
                }
            }
        } else {
            res = toResource(config, id, version)
        }
        val data: Struct? = getMetaData(config, id, version)
        if (data.containsKey("startBundles")) {
            extensionFile = res
            val _softLoaded: Boolean
            _softLoaded = try {
                readManifestConfig(id, data, extensionFile.getAbsolutePath(), null)
                true
            } catch (iv: InvalidVersion) {
                throw iv
            } catch (ae: ApplicationException) {
                init(res, false)
                false
            }
            softLoaded = _softLoaded
        } else {
            init(res, false)
            softLoaded = false
        }
    }

    constructor(config: Config?, ext: Resource?, moveIfNecessary: Boolean) {
        this.config = config
        init(ext, moveIfNecessary)
        softLoaded = false
    }

    @Throws(PageException::class, IOException::class, BundleException::class, ConverterException::class)
    private fun init(ext: Resource?, moveIfNecessary: Boolean) {
        // make sure the config is registerd with the thread
        if (ThreadLocalPageContext.getConfig() == null) ThreadLocalConfig.register(config)

        // is it a web or server context?
        type = if (config is ConfigWeb) "web" else "server"
        load(ext)
        extensionFile = ext
        if (moveIfNecessary) {
            move(ext)
            val data: Struct = StructImpl(Struct.TYPE_LINKED)
            populate(data, true)
            storeMetaData(config, id, version, data)
        }
    }

    // copy the file to extension dir if it is not already there
    @Throws(PageException::class)
    private fun move(ext: Resource?) {
        val trg: Resource?
        val trgDir: Resource
        try {
            trg = getExtensionFile(config, id, version)
            trgDir = trg.getParentResource()
            trgDir.mkdirs()
            if (!ext.getParentResource().equals(trgDir)) {
                if (trg.exists()) trg.delete()
                ResourceUtil.moveTo(ext, trg, true)
                extensionFile = trg
            }
        } catch (e: Exception) {
            throw Caster.toPageException(e)
        }
    }

    @Throws(IOException::class, BundleException::class, ApplicationException::class)
    private fun load(ext: Resource?) {
        // print.ds(ext.getAbsolutePath());
        loaded = true
        // no we read the content of the zip
        val zis = ZipInputStream(IOUtil.toBufferedInputStream(ext.getInputStream()))
        var entry: ZipEntry?
        var manifest: Manifest? = null
        var _img: String? = null
        var path: String
        var fileName: String?
        var sub: String?
        val bundles: List<BundleInfo?> = ArrayList<BundleInfo?>()
        val jars: List<String?> = ArrayList<String?>()
        val flds: List<String?> = ArrayList<String?>()
        val tlds: List<String?> = ArrayList<String?>()
        val tags: List<String?> = ArrayList<String?>()
        val functions: List<String?> = ArrayList<String?>()
        val contexts: List<String?> = ArrayList<String?>()
        val configs: List<String?> = ArrayList<String?>()
        val webContexts: List<String?> = ArrayList<String?>()
        val applications: List<String?> = ArrayList<String?>()
        val components: List<String?> = ArrayList<String?>()
        val plugins: List<String?> = ArrayList<String?>()
        val gateways: List<String?> = ArrayList<String?>()
        val archives: List<String?> = ArrayList<String?>()
        try {
            while (zis.getNextEntry().also { entry = it } != null) {
                path = entry.getName()
                fileName = fileName(entry)
                sub = subFolder(entry)
                if (!entry.isDirectory() && path.equalsIgnoreCase("META-INF/MANIFEST.MF")) {
                    manifest = toManifest(config, zis, null)
                } else if (!entry.isDirectory() && path.equalsIgnoreCase("META-INF/logo.png")) {
                    _img = toBase64(zis, null)
                } else if (!entry.isDirectory() && (startsWith(path, type, "jars") || startsWith(path, type, "jar") || startsWith(path, type, "bundles")
                                || startsWith(path, type, "bundle") || startsWith(path, type, "lib") || startsWith(path, type, "libs")) && StringUtil.endsWithIgnoreCase(path, ".jar")) {
                    jars.add(fileName)
                    val bi: BundleInfo = BundleInfo.getInstance(fileName, zis, false)
                    if (bi.isBundle()) bundles.add(bi)
                } else if (!entry.isDirectory() && startsWith(path, type, "flds") && (StringUtil.endsWithIgnoreCase(path, ".fld") || StringUtil.endsWithIgnoreCase(path, ".fldx"))) flds.add(fileName) else if (!entry.isDirectory() && startsWith(path, type, "tlds") && (StringUtil.endsWithIgnoreCase(path, ".tld") || StringUtil.endsWithIgnoreCase(path, ".tldx"))) tlds.add(fileName) else if (!entry.isDirectory() && (startsWith(path, type, "archives") || startsWith(path, type, "mappings")) && StringUtil.endsWithIgnoreCase(path, ".lar")) archives.add(fileName) else if (!entry.isDirectory() && (startsWith(path, type, "event-gateways") || startsWith(path, type, "eventGateways"))
                        && (StringUtil.endsWithIgnoreCase(path, "." + Constants.getCFMLComponentExtension())
                                || StringUtil.endsWithIgnoreCase(path, "." + Constants.getLuceeComponentExtension()))) gateways.add(sub) else if (!entry.isDirectory() && startsWith(path, type, "tags")) tags.add(sub) else if (!entry.isDirectory() && startsWith(path, type, "functions")) functions.add(sub) else if (!entry.isDirectory() && startsWith(path, type, "context") && !StringUtil.startsWith(fileName(entry), '.')) contexts.add(sub) else if (!entry.isDirectory() && (startsWith(path, type, "webcontexts") || startsWith(path, type, "web.contexts")) && !StringUtil.startsWith(fileName(entry), '.')) webContexts.add(sub) else if (!entry.isDirectory() && startsWith(path, type, "config") && !StringUtil.startsWith(fileName(entry), '.')) configs.add(sub) else if (!entry.isDirectory() && (startsWith(path, type, "web.applications") || startsWith(path, type, "applications") || startsWith(path, type, "web"))
                        && !StringUtil.startsWith(fileName(entry), '.')) applications.add(sub) else if (!entry.isDirectory() && startsWith(path, type, "components") && !StringUtil.startsWith(fileName(entry), '.')) components.add(sub) else if (!entry.isDirectory() && startsWith(path, type, "plugins") && !StringUtil.startsWith(fileName(entry), '.')) plugins.add(sub)
                zis.closeEntry()
            }
        } finally {
            IOUtil.close(zis)
        }

        // read the manifest
        if (manifest == null) throw ApplicationException("The Extension [$ext] is invalid,no Manifest file was found at [META-INF/MANIFEST.MF].")
        readManifestConfig(manifest, ext.getAbsolutePath(), _img)
        this.jars = jars.toArray(arrayOfNulls<String?>(jars.size()))
        this.flds = flds.toArray(arrayOfNulls<String?>(flds.size()))
        this.tlds = tlds.toArray(arrayOfNulls<String?>(tlds.size()))
        this.tags = tags.toArray(arrayOfNulls<String?>(tags.size()))
        this.gateways = gateways.toArray(arrayOfNulls<String?>(gateways.size()))
        this.functions = functions.toArray(arrayOfNulls<String?>(functions.size()))
        this.archives = archives.toArray(arrayOfNulls<String?>(archives.size()))
        this.contexts = contexts.toArray(arrayOfNulls<String?>(contexts.size()))
        this.configs = configs.toArray(arrayOfNulls<String?>(configs.size()))
        this.webContexts = webContexts.toArray(arrayOfNulls<String?>(webContexts.size()))
        this.applications = applications.toArray(arrayOfNulls<String?>(applications.size()))
        this.components = components.toArray(arrayOfNulls<String?>(components.size()))
        this.plugins = plugins.toArray(arrayOfNulls<String?>(plugins.size()))
        this.bundles = bundles.toArray(arrayOfNulls<BundleInfo?>(bundles.size()))
    }

    @Throws(ApplicationException::class)
    private fun readManifestConfig(manifest: Manifest?, label: String?, _img: String?) {
        var label = label
        var _img = _img
        val isWeb = config is ConfigWeb
        type = if (isWeb) "web" else "server"
        val logger: Log = ThreadLocalPageContext.getLog(config, "deploy")
        val info: Info = ConfigWebUtil.getEngine(config).getInfo()
        val attr: Attributes = manifest.getMainAttributes()
        readSymbolicName(label, StringUtil.unwrap(attr.getValue("symbolic-name")))
        readName(label, StringUtil.unwrap(attr.getValue("name")))
        label = name
        readVersion(label, StringUtil.unwrap(attr.getValue("version")))
        label += " : $version"
        readId(label, StringUtil.unwrap(attr.getValue("id")))
        readReleaseType(label, StringUtil.unwrap(attr.getValue("release-type")), isWeb)
        description = StringUtil.unwrap(attr.getValue("description"))
        trial = Caster.toBooleanValue(StringUtil.unwrap(attr.getValue("trial")), false)
        if (_img == null) _img = StringUtil.unwrap(attr.getValue("image"))
        image = _img
        var cat: String = StringUtil.unwrap(attr.getValue("category"))
        if (StringUtil.isEmpty(cat, true)) cat = StringUtil.unwrap(attr.getValue("categories"))
        readCategories(label, cat)
        readCoreVersion(label, StringUtil.unwrap(attr.getValue("lucee-core-version")), info)
        readLoaderVersion(label, StringUtil.unwrap(attr.getValue("lucee-loader-version")))
        startBundles = Caster.toBooleanValue(StringUtil.unwrap(attr.getValue("start-bundles")), true)
        readAMF(label, StringUtil.unwrap(attr.getValue("amf")), logger)
        readResource(label, StringUtil.unwrap(attr.getValue("resource")), logger)
        readSearch(label, StringUtil.unwrap(attr.getValue("search")), logger)
        readORM(label, StringUtil.unwrap(attr.getValue("orm")), logger)
        readWebservice(label, StringUtil.unwrap(attr.getValue("webservice")), logger)
        readMonitor(label, StringUtil.unwrap(attr.getValue("monitor")), logger)
        readCache(label, StringUtil.unwrap(attr.getValue("cache")), logger)
        readCacheHandler(label, StringUtil.unwrap(attr.getValue("cache-handler")), logger)
        readJDBC(label, StringUtil.unwrap(attr.getValue("jdbc")), logger)
        readStartupHook(label, StringUtil.unwrap(attr.getValue("startup-hook")), logger)
        readMapping(label, StringUtil.unwrap(attr.getValue("mapping")), logger)
        readEventGatewayInstances(label, StringUtil.unwrap(attr.getValue("event-gateway-instance")), logger)
    }

    @Throws(ApplicationException::class)
    private fun readManifestConfig(id: String?, data: Struct?, label: String?, _img: String?) {
        var label = label
        var _img = _img
        val isWeb = config is ConfigWeb
        type = if (isWeb) "web" else "server"
        val logger: Log = ThreadLocalPageContext.getLog(config, "deploy")
        val info: Info = ConfigWebUtil.getEngine(config).getInfo()
        readSymbolicName(label, ConfigWebFactory.getAttr(data, "symbolicName", "symbolic-name"))
        readName(label, ConfigWebFactory.getAttr(data, "name"))
        label = name
        readVersion(label, ConfigWebFactory.getAttr(data, "version"))
        label += " : $version"
        readId(label, if (StringUtil.isEmpty(id)) ConfigWebFactory.getAttr(data, "id") else id)
        readReleaseType(label, ConfigWebFactory.getAttr(data, "releaseType", "release-type"), isWeb)
        description = ConfigWebFactory.getAttr(data, "description")
        trial = Caster.toBooleanValue(ConfigWebFactory.getAttr(data, "trial"), false)
        if (_img == null) _img = ConfigWebFactory.getAttr(data, "image")
        image = _img
        var cat: String = ConfigWebFactory.getAttr(data, "category")
        if (StringUtil.isEmpty(cat, true)) cat = ConfigWebFactory.getAttr(data, "categories")
        readCategories(label, cat)
        readCoreVersion(label, ConfigWebFactory.getAttr(data, "luceeCoreVersion", "lucee-core-version"), info)
        readLoaderVersion(label, ConfigWebFactory.getAttr(data, "luceeLoaderVersion", "lucee-loader-version"))
        startBundles = Caster.toBooleanValue(ConfigWebFactory.getAttr(data, "startBundles", "start-bundles"), true)
        readAMF(label, ConfigWebFactory.getAttr(data, "amf"), logger)
        readResource(label, ConfigWebFactory.getAttr(data, "resource"), logger)
        readSearch(label, ConfigWebFactory.getAttr(data, "search"), logger)
        readORM(label, ConfigWebFactory.getAttr(data, "orm"), logger)
        readWebservice(label, ConfigWebFactory.getAttr(data, "webservice"), logger)
        readMonitor(label, ConfigWebFactory.getAttr(data, "monitor"), logger)
        readCache(label, ConfigWebFactory.getAttr(data, "cache"), logger)
        readCacheHandler(label, ConfigWebFactory.getAttr(data, "cacheHandler", "cache-handler"), logger)
        readJDBC(label, ConfigWebFactory.getAttr(data, "jdbc"), logger)
        readMapping(label, ConfigWebFactory.getAttr(data, "mapping"), logger)
        readEventGatewayInstances(label, ConfigWebFactory.getAttr(data, "eventGatewayInstance", "event-gateway-instance"), logger)
    }

    private fun readMapping(label: String?, str: String?, logger: Log?) {
        if (!StringUtil.isEmpty(str, true)) {
            mappings = toSettings(logger, str)
            mappingsJson = str
        }
        if (mappings == null) mappings = ArrayList<Map<String?, String?>?>()
    }

    private fun readEventGatewayInstances(label: String?, str: String?, logger: Log?) {
        if (!StringUtil.isEmpty(str, true)) {
            eventGatewayInstances = toSettingsObj(logger, str)
            eventGatewayInstancesJson = str
        }
        if (eventGatewayInstances == null) eventGatewayInstances = ArrayList<Map<String?, Object?>?>()
    }

    private fun readJDBC(label: String?, str: String?, logger: Log?) {
        if (!StringUtil.isEmpty(str, true)) {
            jdbcs = toSettings(logger, str)
            jdbcsJson = str
        }
        if (jdbcs == null) jdbcs = ArrayList<Map<String?, String?>?>()
    }

    private fun readStartupHook(label: String?, str: String?, logger: Log?) {
        if (!StringUtil.isEmpty(str, true)) {
            startupHooks = toSettings(logger, str)
            startupHooksJson = str
        }
        if (startupHooks == null) startupHooks = ArrayList<Map<String?, String?>?>()
    }

    private fun readCacheHandler(label: String?, str: String?, logger: Log?) {
        if (!StringUtil.isEmpty(str, true)) {
            cacheHandlers = toSettings(logger, str)
            cacheHandlersJson = str
        }
        if (cacheHandlers == null) cacheHandlers = ArrayList<Map<String?, String?>?>()
    }

    private fun readCache(label: String?, str: String?, logger: Log?) {
        if (!StringUtil.isEmpty(str, true)) {
            caches = toSettings(logger, str)
            cachesJson = str
        }
        if (caches == null) caches = ArrayList<Map<String?, String?>?>()
    }

    private fun readMonitor(label: String?, str: String?, logger: Log?) {
        if (!StringUtil.isEmpty(str, true)) {
            monitors = toSettings(logger, str)
            monitorsJson = str
        }
        if (monitors == null) monitors = ArrayList<Map<String?, String?>?>()
    }

    private fun readORM(label: String?, str: String?, logger: Log?) {
        if (!StringUtil.isEmpty(str, true)) {
            orms = toSettings(logger, str)
            ormsJson = str
        }
        if (orms == null) orms = ArrayList<Map<String?, String?>?>()
    }

    private fun readWebservice(label: String?, str: String?, logger: Log?) {
        if (!StringUtil.isEmpty(str, true)) {
            webservices = toSettings(logger, str)
            webservicesJson = str
        }
        if (webservices == null) webservices = ArrayList<Map<String?, String?>?>()
    }

    private fun readSearch(label: String?, str: String?, logger: Log?) {
        if (!StringUtil.isEmpty(str, true)) {
            searchs = toSettings(logger, str)
            searchsJson = str
        }
        if (searchs == null) searchs = ArrayList<Map<String?, String?>?>()
    }

    private fun readResource(label: String?, str: String?, logger: Log?) {
        if (!StringUtil.isEmpty(str, true)) {
            resources = toSettings(logger, str)
            resourcesJson = str
        }
        if (resources == null) resources = ArrayList<Map<String?, String?>?>()
    }

    private fun readAMF(label: String?, str: String?, logger: Log?) {
        if (!StringUtil.isEmpty(str, true)) {
            amfs = toSettings(logger, str)
            amfsJson = str
        }
        if (amfs == null) amfs = ArrayList<Map<String?, String?>?>()
    }

    @Throws(ApplicationException::class)
    private fun readLoaderVersion(label: String?, str: String?) {
        minLoaderVersion = Caster.toDoubleValue(str, 0)
        /*
		 * if (minLoaderVersion > SystemUtil.getLoaderVersion()) { throw new InvalidVersion(
		 * "The Extension [" + label + "] cannot be loaded, " + Constants.NAME +
		 * " Loader Version must be at least [" + str + "], update the Lucee.jar first."); }
		 */
    }

    @Throws(ApplicationException::class)
    private fun readCoreVersion(label: String?, str: String?, info: Info?) {
        minCoreVersion = if (StringUtil.isEmpty(str, true)) null else VersionRange(str)
        /*
		 * if (minCoreVersion != null && Util.isNewerThan(minCoreVersion, info.getVersion())) { throw new
		 * InvalidVersion("The Extension [" + label + "] cannot be loaded, " + Constants.NAME +
		 * " Version must be at least [" + minCoreVersion.toString() + "], version is [" +
		 * info.getVersion().toString() + "]."); }
		 */
    }

    @JvmOverloads
    @Throws(ApplicationException::class)
    fun validate(info: Info? = ConfigWebUtil.getEngine(config).getInfo()) {
        if (minCoreVersion != null && !minCoreVersion.isWithin(info.getVersion())) {
            throw InvalidVersion("The Extension [" + getName() + "] cannot be loaded, " + Constants.NAME + " Version must be at least [" + minCoreVersion.toString()
                    + "], version is [" + info.getVersion().toString() + "].")
        }
        if (minLoaderVersion > SystemUtil.getLoaderVersion()) {
            throw InvalidVersion("The Extension [" + getName() + "] cannot be loaded, " + Constants.NAME + " Loader Version must be at least [" + minLoaderVersion
                    + "], update the Lucee.jar first.")
        }
    }

    fun isValidFor(info: Info?): Boolean {
        if (minCoreVersion != null && !minCoreVersion.isWithin(info.getVersion())) {
            return false
        }
        return if (minLoaderVersion > SystemUtil.getLoaderVersion()) {
            false
        } else true
    }

    private fun readCategories(label: String?, cat: String?) {
        categories = if (!StringUtil.isEmpty(cat, true)) {
            ListUtil.trimItems(ListUtil.listToStringArray(cat, ","))
        } else null
    }

    @Throws(ApplicationException::class)
    private fun readReleaseType(label: String?, str: String?, isWeb: Boolean) {
        // release type
        var str = str
        var rt = RELEASE_TYPE_ALL
        if (!Util.isEmpty(str)) {
            str = str.trim()
            if ("server".equalsIgnoreCase(str)) rt = RELEASE_TYPE_SERVER else if ("web".equalsIgnoreCase(str)) rt = RELEASE_TYPE_WEB
        }
        if (rt == RELEASE_TYPE_SERVER && isWeb || rt == RELEASE_TYPE_WEB && !isWeb) {
            throw ApplicationException(
                    "Cannot install the Extension [" + label + "] in the " + type + " context, this Extension has the release type [" + toReleaseType(rt, "") + "].")
        }
        releaseType = rt
    }

    @Throws(ApplicationException::class)
    private fun readId(label: String?, id: String?) {
        this.id = StringUtil.unwrap(id)
        if (!Decision.isUUId(id)) {
            throw ApplicationException("The Extension [$label] has no valid id defined ($id),id must be a valid UUID.")
        }
    }

    @Throws(ApplicationException::class)
    private fun readVersion(label: String?, version: String?) {
        this.version = version
        if (StringUtil.isEmpty(version)) {
            throw ApplicationException("cannot deploy extension [$label], this Extension has no version information.")
        }
    }

    @Throws(ApplicationException::class)
    private fun readName(label: String?, str: String?) {
        var str = str
        str = StringUtil.unwrap(str)
        if (StringUtil.isEmpty(str, true)) {
            throw ApplicationException("The Extension [$label] has no name defined, a name is necesary.")
        }
        name = str.trim()
    }

    @Throws(ApplicationException::class)
    private fun readSymbolicName(label: String?, str: String?) {
        var str = str
        str = StringUtil.unwrap(str)
        if (!StringUtil.isEmpty(str, true)) symbolicName = str.trim()
    }

    @Throws(IOException::class, BundleException::class)
    fun deployBundles(config: Config?) {
        // no we read the content of the zip
        val zis = ZipInputStream(IOUtil.toBufferedInputStream(extensionFile.getInputStream()))
        var entry: ZipEntry?
        var path: String
        var fileName: String?
        try {
            while (zis.getNextEntry().also { entry = it } != null) {
                path = entry.getName()
                fileName = fileName(entry)
                // jars
                if (!entry.isDirectory() && (startsWith(path, type, "jars") || startsWith(path, type, "jar") || startsWith(path, type, "bundles")
                                || startsWith(path, type, "bundle") || startsWith(path, type, "lib") || startsWith(path, type, "libs")) && StringUtil.endsWithIgnoreCase(path, ".jar")) {
                    val obj: Object = ConfigAdmin.installBundle(config, zis, fileName, version, false, false)
                    // jar is not a bundle, only a regular jar
                    if (obj !is BundleFile) {
                        val tmp: Resource = obj as Resource
                        val tmpJar: Resource = tmp.getParentResource().getRealResource(ListUtil.last(path, "\\/"))
                        tmp.moveTo(tmpJar)
                        ConfigAdmin.updateJar(config, tmpJar, false)
                    }
                }
                zis.closeEntry()
            }
        } finally {
            IOUtil.close(zis)
        }
    }

    @Throws(PageException::class, IOException::class, BundleException::class)
    private fun getMetaData(config: Config?, id: String?, version: String?): Struct? {
        val file: Resource? = getMetaDataFile(config, id, version)
        if (file.isFile()) return Caster.toStruct(JSONExpressionInterpreter().interpret(null, IOUtil.toString(file, CharsetUtil.UTF8)))
        load(getExtensionFile(config, id, version))
        val data: Struct = StructImpl()
        populate(data, true)
        return data
    }

    fun populate(el: Struct?, full: Boolean) {
        val id = getId()
        var name = getName()
        if (StringUtil.isEmpty(name)) name = id
        el.setEL("id", id)
        el.setEL("name", name)
        el.setEL("version", getVersion())
        if (!full) return

        // newly added
        // start bundles (IMPORTANT:this key is used to reconize a newer entry, so do not change)
        el.setEL("startBundles", Caster.toString(getStartBundles()))

        // release type
        el.setEL("releaseType", toReleaseType(getReleaseType(), "all"))

        // Description
        if (StringUtil.isEmpty(getDescription())) el.setEL("description", toStringForAttr(getDescription())) else el.removeEL(KeyImpl.init("description"))

        // Trial
        el.setEL("trial", Caster.toString(isTrial()))

        // Image
        if (StringUtil.isEmpty(getImage())) el.setEL("image", toStringForAttr(getImage())) else el.removeEL(KeyImpl.init("image"))

        // Categories
        val cats = getCategories()
        if (!ArrayUtil.isEmpty(cats)) {
            val sb = StringBuilder()
            for (cat in cats!!) {
                if (sb.length() > 0) sb.append(',')
                sb.append(toStringForAttr(cat).replace(',', ' '))
            }
            el.setEL("categories", sb.toString())
        } else el.removeEL(KeyImpl.init("categories"))

        // core version
        if (minCoreVersion != null) el.setEL("luceeCoreVersion", toStringForAttr(minCoreVersion.toString())) else el.removeEL(KeyImpl.init("luceeCoreVersion"))

        // loader version
        if (minLoaderVersion > 0) el.setEL("loaderVersion", Caster.toString(minLoaderVersion)) else el.removeEL(KeyImpl.init("loaderVersion"))

        // amf
        if (!StringUtil.isEmpty(amfsJson)) el.setEL("amf", toStringForAttr(amfsJson)) else el.removeEL(KeyImpl.init("amf"))

        // resource
        if (!StringUtil.isEmpty(resourcesJson)) el.setEL("resource", toStringForAttr(resourcesJson)) else el.removeEL(KeyImpl.init("resource"))

        // search
        if (!StringUtil.isEmpty(searchsJson)) el.setEL("search", toStringForAttr(searchsJson)) else el.removeEL(KeyImpl.init("search"))

        // orm
        if (!StringUtil.isEmpty(ormsJson)) el.setEL("orm", toStringForAttr(ormsJson)) else el.removeEL(KeyImpl.init("orm"))

        // webservice
        if (!StringUtil.isEmpty(webservicesJson)) el.setEL("webservice", toStringForAttr(webservicesJson)) else el.removeEL(KeyImpl.init("webservice"))

        // monitor
        if (!StringUtil.isEmpty(monitorsJson)) el.setEL("monitor", toStringForAttr(monitorsJson)) else el.removeEL(KeyImpl.init("monitor"))

        // cache
        if (!StringUtil.isEmpty(cachesJson)) el.setEL("cache", toStringForAttr(cachesJson)) else el.removeEL(KeyImpl.init("cache"))

        // cache-handler
        if (!StringUtil.isEmpty(cacheHandlersJson)) el.setEL("cacheHandler", toStringForAttr(cacheHandlersJson)) else el.removeEL(KeyImpl.init("cacheHandler"))

        // jdbc
        if (!StringUtil.isEmpty(jdbcsJson)) el.setEL("jdbc", toStringForAttr(jdbcsJson)) else el.removeEL(KeyImpl.init("jdbc"))

        // startup-hook
        if (!StringUtil.isEmpty(startupHooksJson)) el.setEL("startupHook", toStringForAttr(startupHooksJson)) else el.removeEL(KeyImpl.init("startupHook"))

        // mapping
        if (!StringUtil.isEmpty(mappingsJson)) el.setEL("mapping", toStringForAttr(mappingsJson)) else el.removeEL(KeyImpl.init("mapping"))

        // event-gateway-instances
        if (!StringUtil.isEmpty(eventGatewayInstancesJson)) el.setEL("eventGatewayInstances", toStringForAttr(eventGatewayInstancesJson)) else el.removeEL(KeyImpl.init("eventGatewayInstances"))
    }

    private fun toStringForAttr(str: String?): String? {
        return str ?: ""
    }

    @Throws(PageException::class, IOException::class, BundleException::class)
    private fun populate(qry: Query?) {
        val row: Int = qry.addRow()
        qry.setAt(KeyConstants._id, row, getId())
        qry.setAt(KeyConstants._name, row, getName())
        qry.setAt(SYMBOLIC_NAME, row, getSymbolicName())
        qry.setAt(KeyConstants._image, row, getImage())
        qry.setAt(KeyConstants._type, row, type)
        qry.setAt(KeyConstants._description, row, description)
        qry.setAt(KeyConstants._version, row, if (getVersion() == null) null else getVersion().toString())
        qry.setAt(TRIAL, row, isTrial())
        qry.setAt(RELEASE_TYPE, row, toReleaseType(getReleaseType(), "all"))
        // qry.setAt(JARS, row,Caster.toArray(getJars()));
        qry.setAt(FLDS, row, Caster.toArray(getFlds()))
        qry.setAt(TLDS, row, Caster.toArray(getTlds()))
        qry.setAt(FUNCTIONS, row, Caster.toArray(getFunctions()))
        qry.setAt(ARCHIVES, row, Caster.toArray(getArchives()))
        qry.setAt(TAGS, row, Caster.toArray(getTags()))
        qry.setAt(CONTEXTS, row, Caster.toArray(getContexts()))
        qry.setAt(WEBCONTEXTS, row, Caster.toArray(getWebContexts()))
        qry.setAt(CONFIG, row, Caster.toArray(getConfigs()))
        qry.setAt(EVENT_GATEWAYS, row, Caster.toArray(getEventGateways()))
        qry.setAt(CATEGORIES, row, Caster.toArray(getCategories()))
        qry.setAt(APPLICATIONS, row, Caster.toArray(getApplications()))
        qry.setAt(COMPONENTS, row, Caster.toArray(getComponents()))
        qry.setAt(PLUGINS, row, Caster.toArray(getPlugins()))
        qry.setAt(START_BUNDLES, row, Caster.toBoolean(getStartBundles()))
        val bfs: Array<BundleInfo?>? = getBundles()
        val qryBundles: Query = QueryImpl(arrayOf<Key?>(KeyConstants._name, KeyConstants._version), bfs?.size
                ?: 0, "bundles")
        if (bfs != null) {
            for (i in bfs.indices) {
                qryBundles.setAt(KeyConstants._name, i + 1, bfs[i].getSymbolicName())
                if (bfs[i].getVersion() != null) qryBundles.setAt(KeyConstants._version, i + 1, bfs[i].getVersionAsString())
            }
        }
        qry.setAt(BUNDLES, row, qryBundles)
    }

    @Throws(PageException::class)
    fun toStruct(): Struct? {
        val sct: Struct = StructImpl()
        sct.set(KeyConstants._id, getId())
        sct.set(SYMBOLIC_NAME, getSymbolicName())
        sct.set(KeyConstants._name, getName())
        sct.set(KeyConstants._image, getImage())
        sct.set(KeyConstants._description, description)
        sct.set(KeyConstants._version, if (getVersion() == null) null else getVersion().toString())
        sct.set(TRIAL, isTrial())
        sct.set(RELEASE_TYPE, toReleaseType(getReleaseType(), "all"))
        // sct.set(JARS, row,Caster.toArray(getJars()));
        try {
            sct.set(FLDS, Caster.toArray(getFlds()))
            sct.set(TLDS, Caster.toArray(getTlds()))
            sct.set(FUNCTIONS, Caster.toArray(getFunctions()))
            sct.set(ARCHIVES, Caster.toArray(getArchives()))
            sct.set(TAGS, Caster.toArray(getTags()))
            sct.set(CONTEXTS, Caster.toArray(getContexts()))
            sct.set(WEBCONTEXTS, Caster.toArray(getWebContexts()))
            sct.set(CONFIG, Caster.toArray(getConfigs()))
            sct.set(EVENT_GATEWAYS, Caster.toArray(getEventGateways()))
            sct.set(CATEGORIES, Caster.toArray(getCategories()))
            sct.set(APPLICATIONS, Caster.toArray(getApplications()))
            sct.set(COMPONENTS, Caster.toArray(getComponents()))
            sct.set(PLUGINS, Caster.toArray(getPlugins()))
            sct.set(START_BUNDLES, Caster.toBoolean(getStartBundles()))
            val bfs: Array<BundleInfo?>? = getBundles()
            val qryBundles: Query = QueryImpl(arrayOf<Key?>(KeyConstants._name, KeyConstants._version), bfs?.size
                    ?: 0, "bundles")
            if (bfs != null) {
                for (i in bfs.indices) {
                    qryBundles.setAt(KeyConstants._name, i + 1, bfs[i].getSymbolicName())
                    if (bfs[i].getVersion() != null) qryBundles.setAt(KeyConstants._version, i + 1, bfs[i].getVersionAsString())
                }
            }
            sct.set(BUNDLES, qryBundles)
        } catch (e: Exception) {
            throw Caster.toPageException(e)
        }
        return sct
    }

    fun getId(): String? {
        return id
    }

    fun getImage(): String? {
        return image
    }

    fun getVersion(): String? {
        return version
    }

    fun getStartBundles(): Boolean {
        return startBundles
    }

    fun getName(): String? {
        return name
    }

    fun getSymbolicName(): String? {
        return if (StringUtil.isEmpty(symbolicName)) id else symbolicName
    }

    fun isTrial(): Boolean {
        return trial
    }

    fun getDescription(): String? {
        return description
    }

    fun getReleaseType(): Int {
        return releaseType
    }

    @Throws(ApplicationException::class, IOException::class, BundleException::class)
    fun getBundles(): Array<BundleInfo?>? {
        if (!loaded) load(extensionFile)
        return bundles
    }

    fun getBundles(defaultValue: Array<BundleInfo?>?): Array<BundleInfo?>? {
        if (!loaded) {
            try {
                load(extensionFile)
            } catch (e: Exception) {
                return defaultValue
            }
        }
        return bundles
    }

    @Throws(ApplicationException::class, IOException::class, BundleException::class)
    fun getFlds(): Array<String?>? {
        if (!loaded) load(extensionFile)
        return if (flds == null) EMPTY else flds
    }

    @Throws(ApplicationException::class, IOException::class, BundleException::class)
    fun getJars(): Array<String?>? {
        if (!loaded) load(extensionFile)
        return if (jars == null) EMPTY else jars
    }

    @Throws(ApplicationException::class, IOException::class, BundleException::class)
    fun getTlds(): Array<String?>? {
        if (!loaded) load(extensionFile)
        return if (tlds == null) EMPTY else tlds
    }

    @Throws(ApplicationException::class, IOException::class, BundleException::class)
    fun getFunctions(): Array<String?>? {
        if (!loaded) load(extensionFile)
        return if (functions == null) EMPTY else functions
    }

    @Throws(ApplicationException::class, IOException::class, BundleException::class)
    fun getArchives(): Array<String?>? {
        if (!loaded) load(extensionFile)
        return if (archives == null) EMPTY else archives
    }

    @Throws(ApplicationException::class, IOException::class, BundleException::class)
    fun getTags(): Array<String?>? {
        if (!loaded) load(extensionFile)
        return if (tags == null) EMPTY else tags
    }

    @Throws(ApplicationException::class, IOException::class, BundleException::class)
    fun getEventGateways(): Array<String?>? {
        if (!loaded) load(extensionFile)
        return if (gateways == null) EMPTY else gateways
    }

    @Throws(ApplicationException::class, IOException::class, BundleException::class)
    fun getApplications(): Array<String?>? {
        if (!loaded) load(extensionFile)
        return if (applications == null) EMPTY else applications
    }

    @Throws(ApplicationException::class, IOException::class, BundleException::class)
    fun getComponents(): Array<String?>? {
        if (!loaded) load(extensionFile)
        return if (components == null) EMPTY else components
    }

    @Throws(ApplicationException::class, IOException::class, BundleException::class)
    fun getPlugins(): Array<String?>? {
        if (!loaded) load(extensionFile)
        return if (plugins == null) EMPTY else plugins
    }

    @Throws(ApplicationException::class, IOException::class, BundleException::class)
    fun getContexts(): Array<String?>? {
        if (!loaded) load(extensionFile)
        return if (contexts == null) EMPTY else contexts
    }

    @Throws(ApplicationException::class, IOException::class, BundleException::class)
    fun getConfigs(): Array<String?>? {
        if (!loaded) load(extensionFile)
        return if (configs == null) EMPTY else configs
    }

    @Throws(ApplicationException::class, IOException::class, BundleException::class)
    fun getWebContexts(): Array<String?>? {
        if (!loaded) load(extensionFile)
        return if (webContexts == null) EMPTY else webContexts
    }

    fun getCategories(): Array<String?>? {
        return if (categories == null) EMPTY else categories
    }

    fun getCaches(): List<Map<String?, String?>?>? {
        return caches
    }

    fun getCacheHandlers(): List<Map<String?, String?>?>? {
        return cacheHandlers
    }

    fun getOrms(): List<Map<String?, String?>?>? {
        return orms
    }

    fun getWebservices(): List<Map<String?, String?>?>? {
        return webservices
    }

    fun getMonitors(): List<Map<String?, String?>?>? {
        return monitors
    }

    fun getSearchs(): List<Map<String?, String?>?>? {
        return searchs
    }

    fun getResources(): List<Map<String?, String?>?>? {
        return resources
    }

    fun getAMFs(): List<Map<String?, String?>?>? {
        return amfs
    }

    fun getJdbcs(): List<Map<String?, String?>?>? {
        return jdbcs
    }

    fun getStartupHooks(): List<Map<String?, String?>?>? {
        return startupHooks
    }

    fun getMappings(): List<Map<String?, String?>?>? {
        return mappings
    }

    fun getEventGatewayInstances(): List<Map<String?, Object?>?>? {
        return eventGatewayInstances
    }

    fun getExtensionFile(): Resource? {
        if (!extensionFile.exists()) {
            val c: Config = ThreadLocalPageContext.getConfig()
            if (c != null) {
                val res: Resource = DeployHandler.getExtension(c, ExtensionDefintion(id, version), null)
                if (res != null && res.exists()) {
                    try {
                        IOUtil.copy(res, extensionFile)
                    } catch (e: IOException) {
                        res.delete()
                    }
                }
            }
        }
        return extensionFile
    }

    @Override
    override fun equals(objOther: Object?): Boolean {
        if (objOther === this) return true
        if (objOther is RHExtension) {
            val other = objOther as RHExtension?
            if (!getId()!!.equals(other!!.getId())) return false
            if (!getName()!!.equals(other.getName())) return false
            if (!getVersion()!!.equals(other.getVersion())) return false
            return if (isTrial() != other.isTrial()) false else true
        }
        if (objOther is ExtensionDefintion) {
            val ed: ExtensionDefintion? = objOther
            if (!ed!!.getId().equalsIgnoreCase(getId())) return false
            return if (ed!!.getVersion() == null || getVersion() == null) true else ed!!.getVersion().equalsIgnoreCase(getVersion())
        }
        return false
    }

    class InvalidVersion(message: String?) : ApplicationException(message) {
        companion object {
            private const val serialVersionUID = 8561299058941139724L
        }
    }

    fun toExtensionDefinition(): ExtensionDefintion? {
        val ed = ExtensionDefintion(getId(), getVersion())
        ed!!.setParam("symbolic-name", getSymbolicName())
        ed!!.setParam("description", getDescription())
        return ed
    }

    @Override
    override fun toString(): String {
        val ed = ExtensionDefintion(getId(), getVersion())
        ed!!.setParam("symbolic-name", getSymbolicName())
        ed!!.setParam("description", getDescription())
        return ed.toString()
    }

    companion object {
        private const val serialVersionUID = 2904020095330689714L
        private val BUNDLES: Key? = KeyImpl.getInstance("bundles")
        private val TLDS: Key? = KeyImpl.getInstance("tlds")
        private val FLDS: Key? = KeyImpl.getInstance("flds")
        private val EVENT_GATEWAYS: Key? = KeyImpl.getInstance("eventGateways")
        private val TAGS: Key? = KeyImpl.getInstance("tags")
        private val FUNCTIONS: Key? = KeyConstants._functions
        private val ARCHIVES: Key? = KeyImpl.getInstance("archives")
        private val CONTEXTS: Key? = KeyImpl.getInstance("contexts")
        private val WEBCONTEXTS: Key? = KeyImpl.getInstance("webcontexts")
        private val CONFIG: Key? = KeyConstants._config
        private val COMPONENTS: Key? = KeyImpl.getInstance("components")
        private val APPLICATIONS: Key? = KeyImpl.getInstance("applications")
        private val CATEGORIES: Key? = KeyImpl.getInstance("categories")
        private val PLUGINS: Key? = KeyImpl.getInstance("plugins")
        private val START_BUNDLES: Key? = KeyImpl.getInstance("startBundles")
        private val TRIAL: Key? = KeyImpl.getInstance("trial")
        private val RELEASE_TYPE: Key? = KeyImpl.getInstance("releaseType")
        private val SYMBOLIC_NAME: Key? = KeyImpl.getInstance("symbolicName")
        private val EMPTY: Array<String?>? = arrayOfNulls<String?>(0)
        private val EMPTY_BD: Array<BundleDefinition?>? = arrayOfNulls<BundleDefinition?>(0)
        const val RELEASE_TYPE_ALL = 0
        const val RELEASE_TYPE_SERVER = 1
        const val RELEASE_TYPE_WEB = 2
        private val LEX_FILTER: ExtensionResourceFilter? = ExtensionResourceFilter("lex")
        @Throws(PageException::class, IOException::class, BundleException::class, ConverterException::class)
        fun isInstalled(config: Config?, id: String?, version: String?): Boolean {
            val res: Resource? = toResource(config, id, version, null)
            return res != null && res.isFile()
        }

        @Throws(ConverterException::class, IOException::class)
        fun storeMetaData(config: Config?, id: String?, version: String?, data: Struct?) {
            val json = JSONConverter(true, CharsetUtil.UTF8, JSONDateFormat.PATTERN_CF, true, true)
            val str: String = json.serialize(null, data, SerializationSettings.SERIALIZE_AS_ROW)
            IOUtil.write(getMetaDataFile(config, id, version), str, CharsetUtil.UTF8, false)
        }

        @Throws(IOException::class, BundleException::class, ApplicationException::class)
        fun getManifestFromFile(config: Config?, file: Resource?): Manifest? {
            val zis = ZipInputStream(IOUtil.toBufferedInputStream(file.getInputStream()))
            var entry: ZipEntry?
            var manifest: Manifest? = null
            try {
                while (zis.getNextEntry().also { entry = it } != null) {
                    if (!entry.isDirectory() && entry.getName().equalsIgnoreCase("META-INF/MANIFEST.MF")) {
                        manifest = toManifest(config, zis, null)
                    }
                    zis.closeEntry()
                    if (manifest != null) return manifest
                }
            } finally {
                IOUtil.close(zis)
            }
            return null
        }

        @Throws(PageException::class)
        fun toResource(config: Config?, id: String?, version: String?): Resource? {
            val fileName: String = HashUtil.create64BitHashAsString(id + version, Character.MAX_RADIX).toString() + ".lex"
            val res: Resource = getExtensionDir(config).getRealResource(fileName)
            if (!res.exists()) throw ApplicationException("Extension [$fileName] was not found at [$res]")
            return res
        }

        @Throws(PageException::class)
        fun toResource(config: Config?, id: String?, version: String?, defaultValue: Resource?): Resource? {
            val res: Resource
            val fileName = toHash(id, version, "lex")
            res = getExtensionDir(config).getRealResource(fileName)
            return if (!res.exists()) defaultValue else res
        }

        fun getExtensionFile(config: Config?, id: String?, version: String?): Resource? {
            val fileName = toHash(id, version, "lex")
            return getExtensionDir(config).getRealResource(fileName)
        }

        fun getMetaDataFile(config: Config?, id: String?, version: String?): Resource? {
            val fileName = toHash(id, version, "mf")
            return getExtensionDir(config).getRealResource(fileName)
        }

        fun toHash(id: String?, version: String?, ext: String?): String? {
            var ext = ext
            if (ext == null) ext = "lex"
            return HashUtil.create64BitHashAsString(id + version, Character.MAX_RADIX).toString() + "." + ext
        }

        private fun getExtensionDir(config: Config?): Resource? {
            return config.getConfigDir().getRealResource("extensions/installed")
        }

        private fun getPhysicalExtensionCount(config: Config?): Int {
            val count: RefInteger = RefIntegerImpl(0)
            getExtensionDir(config).list(object : ResourceNameFilter() {
                @Override
                fun accept(res: Resource?, name: String?): Boolean {
                    if (StringUtil.endsWithIgnoreCase(name, ".lex")) count.plus(1)
                    return false
                }
            })
            return count.toInt()
        }

        @Throws(PageException::class, IOException::class, BundleException::class, ConverterException::class)
        fun correctExtensions(config: Config?) {

            // extension defined in xml
            val xmlArrExtensions: Array<RHExtension?> = (config as ConfigPro?).getRHExtensions()
            if (xmlArrExtensions.size == getPhysicalExtensionCount(config)) return  // all is OK
            var ext: RHExtension?
            val xmlExtensions: Map<String?, RHExtension?> = HashMap()
            for (i in xmlArrExtensions.indices) {
                ext = xmlArrExtensions[i]
                xmlExtensions.put(ext!!.getId(), ext)
            }

            // Extension defined in filesystem
            val resources: Array<Resource?> = getExtensionDir(config).listResources(LEX_FILTER)
            if (resources == null || resources.size == 0) return
            var xmlExt: RHExtension?
            for (i in resources.indices) {
                ext = RHExtension(config, resources[i], false)
                xmlExt = xmlExtensions[ext.getId()]
                if (xmlExt != null && (xmlExt.getVersion().toString() + "").equals(ext.getVersion().toString() + "")) continue
                ConfigAdmin._updateRHExtension(config as ConfigPro?, resources[i], true, true)
            }
        }

        fun toBundleDefinitions(strBundles: String?): Array<BundleDefinition?>? {
            if (StringUtil.isEmpty(strBundles, true)) return EMPTY_BD
            val arrStrs = toArray(strBundles)
            val arrBDs: Array<BundleDefinition?>?
            if (!ArrayUtil.isEmpty(arrStrs)) {
                arrBDs = arrayOfNulls<BundleDefinition?>(arrStrs!!.size)
                var index: Int
                for (i in arrStrs.indices) {
                    index = arrStrs!![i].indexOf(':')
                    if (index == -1) arrBDs!![i] = BundleDefinition(arrStrs[i].trim()) else {
                        try {
                            arrBDs!![i] = BundleDefinition(arrStrs[i].substring(0, index).trim(), arrStrs[i].substring(index + 1).trim())
                        } catch (e: BundleException) {
                            throw PageRuntimeException(e) // should not happen
                        }
                    }
                }
            } else arrBDs = EMPTY_BD
            return arrBDs
        }

        private fun toArray(str: String?): Array<String?>? {
            return if (StringUtil.isEmpty(str, true)) arrayOfNulls<String?>(0) else ListUtil.listToStringArray(str.trim(), ',')
        }

        @Throws(PageException::class)
        fun toQuery(config: Config?, children: List<RHExtension?>?, qry: Query?): Query? {
            var qry: Query? = qry
            val log: Log = ThreadLocalPageContext.getLog(config, "deploy")
            if (qry == null) qry = createQuery()
            val it = children!!.iterator()
            while (it.hasNext()) {
                try {
                    it.next()!!.populate(qry) // ,i+1
                } catch (t: Throwable) {
                    ExceptionUtil.rethrowIfNecessary(t)
                    log.error("extension", t)
                }
            }
            return qry
        }

        @Throws(PageException::class)
        fun toQuery(config: Config?, children: Array<RHExtension?>?, qry: Query?): Query? {
            var qry: Query? = qry
            val log: Log = ThreadLocalPageContext.getLog(config, "deploy")
            if (qry == null) qry = createQuery()
            if (children != null) {
                for (i in children.indices) {
                    try {
                        if (children[i] != null) children[i]!!.populate(qry) // ,i+1
                    } catch (t: Throwable) {
                        ExceptionUtil.rethrowIfNecessary(t)
                        log.error("extension", t)
                    }
                }
            }
            return qry
        }

        @Throws(DatabaseException::class)
        private fun createQuery(): Query? {
            return QueryImpl(arrayOf<Key?>(KeyConstants._id, KeyConstants._version, KeyConstants._name, SYMBOLIC_NAME, KeyConstants._type, KeyConstants._description,
                    KeyConstants._image, RELEASE_TYPE, TRIAL, CATEGORIES, START_BUNDLES, BUNDLES, FLDS, TLDS, TAGS, FUNCTIONS, CONTEXTS, WEBCONTEXTS, CONFIG, APPLICATIONS, COMPONENTS,
                    PLUGINS, EVENT_GATEWAYS, ARCHIVES), 0, "Extensions")
        }

        private fun toManifest(config: Config?, `is`: InputStream?, defaultValue: Manifest?): Manifest? {
            return try {
                val cs: Charset = config.getResourceCharset()
                var str: String = IOUtil.toString(`is`, cs)
                if (StringUtil.isEmpty(str, true)) return defaultValue
                str = str.trim().toString() + "\n"
                Manifest(ByteArrayInputStream(str.getBytes(cs)))
            } catch (t: Throwable) {
                ExceptionUtil.rethrowIfNecessary(t)
                defaultValue
            }
        }

        private fun toBase64(`is`: InputStream?, defaultValue: String?): String? {
            return try {
                val bytes: ByteArray = IOUtil.toBytes(`is`)
                if (ArrayUtil.isEmpty(bytes)) defaultValue else Caster.toB64(bytes, defaultValue)
            } catch (t: Throwable) {
                ExceptionUtil.rethrowIfNecessary(t)
                defaultValue
            }
        }

        fun toClassDefinition(config: Config?, map: Map<String?, *>?, defaultValue: ClassDefinition<*>?): ClassDefinition<*>? {
            val _class: String = Caster.toString(map!!["class"], null)
            var _name: String = Caster.toString(map["bundle-name"], null)
            if (StringUtil.isEmpty(_name)) _name = Caster.toString(map["bundleName"], null)
            if (StringUtil.isEmpty(_name)) _name = Caster.toString(map["bundlename"], null)
            if (StringUtil.isEmpty(_name)) _name = Caster.toString(map["name"], null)
            var _version: String = Caster.toString(map["bundle-version"], null)
            if (StringUtil.isEmpty(_version)) _version = Caster.toString(map["bundleVersion"], null)
            if (StringUtil.isEmpty(_version)) _version = Caster.toString(map["bundleversion"], null)
            if (StringUtil.isEmpty(_version)) _version = Caster.toString(map["version"], null)
            return if (StringUtil.isEmpty(_class)) defaultValue else ClassDefinitionImpl(_class, _name, _version, config.getIdentification())
        }

        private fun toSettings(log: Log?, str: String?): List<Map<String?, String?>?>? {
            val list: List<Map<String?, String?>?> = ArrayList()
            _toSettings(list, log, str, true)
            return list
        }

        private fun toSettingsObj(log: Log?, str: String?): List<Map<String?, Object?>?>? {
            val list: List<Map<String?, Object?>?> = ArrayList()
            _toSettings(list, log, str, false)
            return list
        }

        private fun _toSettings(list: List?, log: Log?, str: String?, valueAsString: Boolean) {
            try {
                val res: Object = DeserializeJSON.call(null, str)
                // only a single row
                if (!Decision.isArray(res) && Decision.isStruct(res)) {
                    _toSetting(list, Caster.toMap(res), valueAsString)
                    return
                }
                // multiple rows
                if (Decision.isArray(res)) {
                    val tmpList: List = Caster.toList(res)
                    val it: Iterator = tmpList.iterator()
                    while (it.hasNext()) {
                        _toSetting(list, Caster.toMap(it.next()), valueAsString)
                    }
                    return
                }
            } catch (t: Throwable) {
                ExceptionUtil.rethrowIfNecessary(t)
                log.error("Extension Installation", t)
            }
            return
        }

        @Throws(PageException::class)
        private fun _toSetting(list: List?, src: Map?, valueAsString: Boolean) {
            var e: Entry
            val it: Iterator<Entry?> = src.entrySet().iterator()
            val map: Map = HashMap()
            while (it.hasNext()) {
                e = it.next()
                map.put(Caster.toString(e.getKey()), if (valueAsString) Caster.toString(e.getValue()) else e.getValue())
            }
            list.add(map)
        }

        private fun startsWith(path: String?, type: String?, name: String?): Boolean {
            return StringUtil.startsWithIgnoreCase(path, name.toString() + "/") || StringUtil.startsWithIgnoreCase(path, type.toString() + "/" + name + "/")
        }

        private fun fileName(entry: ZipEntry?): String? {
            val name: String = entry.getName()
            val index: Int = name.lastIndexOf('/')
            return if (index == -1) name else name.substring(index + 1)
        }

        private fun subFolder(entry: ZipEntry?): String? {
            val name: String = entry.getName()
            val index: Int = name.indexOf('/')
            return if (index == -1) name else name.substring(index + 1)
        }

        @Throws(IOException::class, BundleException::class, ApplicationException::class)
        private fun toBundleDefinition(`is`: InputStream?, name: String?, extensionVersion: String?, closeStream: Boolean): BundleDefinition? {
            val tmp: Resource = SystemUtil.getTempDirectory().getRealResource(name)
            return try {
                IOUtil.copy(`is`, tmp, closeStream)
                val bf: BundleFile = BundleFile.getInstance(tmp)
                if (bf.isBundle()) throw ApplicationException("Jar [$name] is not a valid OSGi Bundle")
                BundleDefinition(bf.getSymbolicName(), bf.getVersion())
            } finally {
                tmp.delete()
            }
        }

        fun toReleaseType(releaseType: Int, defaultValue: String?): String? {
            if (releaseType == RELEASE_TYPE_WEB) return "web"
            if (releaseType == RELEASE_TYPE_SERVER) return "server"
            return if (releaseType == RELEASE_TYPE_ALL) "all" else defaultValue
        }

        fun toReleaseType(releaseType: String?, defaultValue: Int): Int {
            if ("web".equalsIgnoreCase(releaseType)) return RELEASE_TYPE_WEB
            if ("server".equalsIgnoreCase(releaseType)) return RELEASE_TYPE_SERVER
            if ("all".equalsIgnoreCase(releaseType)) return RELEASE_TYPE_ALL
            return if ("both".equalsIgnoreCase(releaseType)) RELEASE_TYPE_ALL else defaultValue
        }

        fun toExtensionDefinitions(str: String?): List<ExtensionDefintion?>? {
            // first we split the list
            val rtn: List<ExtensionDefintion?> = ArrayList<ExtensionDefintion?>()
            if (StringUtil.isEmpty(str)) return rtn
            val arr: Array<String?> = ListUtil.trimItems(ListUtil.listToStringArray(str, ','))
            if (ArrayUtil.isEmpty(arr)) return rtn
            var ed: ExtensionDefintion?
            for (i in arr.indices) {
                ed = toExtensionDefinition(arr[i])
                if (ed != null) rtn.add(ed)
            }
            return rtn
        }

        fun toExtensionDefinition(s: String?): ExtensionDefintion? {
            var s = s
            if (StringUtil.isEmpty(s, true)) return null
            s = s.trim()
            val arrr: Array<String?>
            var index: Int
            arrr = ListUtil.trimItems(ListUtil.listToStringArray(s, ';'))
            val ed = ExtensionDefintion()
            var name: String
            var res: Resource?
            val c: Config = ThreadLocalPageContext.getConfig()
            for (ss in arrr) {
                res = null
                index = ss.indexOf('=')
                if (index != -1) {
                    name = ss.substring(0, index).trim()
                    ed!!.setParam(name, ss.substring(index + 1).trim())
                    if ("path".equalsIgnoreCase(name) && c != null) {
                        res = ResourceUtil.toResourceExisting(c, ss.substring(index + 1).trim(), null)
                    }
                } else if (ed!!.getId() == null || Decision.isUUId(ed!!.getId())) {
                    if (c == null || Decision.isUUId(ss) || ResourceUtil.toResourceExisting(ThreadLocalPageContext.getConfig(), ss.trim(), null).also { res = it } == null) ed!!.setId(ss)
                }
                if (res != null && res.isFile()) {
                    val trgDir: Resource = c.getLocalExtensionProviderDirectory()
                    val trg: Resource = trgDir.getRealResource(res.getName())
                    if (!res.equals(trg) && !trg.isFile()) {
                        try {
                            IOUtil.copy(res, trg)
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    }
                    if (!trg.isFile()) continue
                    try {
                        return RHExtension(c, trg, false).toExtensionDefinition()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
            return ed
        }

        @Throws(PageException::class)
        fun toRHExtensions(eds: List<ExtensionDefintion?>?): List<RHExtension?>? {
            return try {
                val rtn: List<RHExtension?> = ArrayList<RHExtension?>()
                val it: Iterator<ExtensionDefintion?> = eds!!.iterator()
                var ed: ExtensionDefintion?
                while (it.hasNext()) {
                    ed = it.next()
                    if (ed != null) rtn.add(ed.toRHExtension())
                }
                rtn
            } catch (e: Exception) {
                throw Caster.toPageException(e)
            }
        }
    }
}