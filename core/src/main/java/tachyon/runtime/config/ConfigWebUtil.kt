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

import java.io.IOException

/**
 *
 */
object ConfigWebUtil {
    private var enckey: String? = null

    /**
     * default encryption for configuration (not very secure)
     *
     * @param str
     * @return
     */
    fun decrypt(str: String?): String? {
        var str = str
        if (StringUtil.isEmpty(str) || !StringUtil.startsWithIgnoreCase(str, "encrypted:")) return str
        str = str.substring(10)
        return BlowfishEasy(getEncKey()).decryptString(str)
    }

    /**
     * default encryption for configuration (not very secure)
     *
     * @param str
     * @return
     */
    fun encrypt(str: String?): String? {
        if (StringUtil.isEmpty(str)) return ""
        return if (StringUtil.startsWithIgnoreCase(str, "encrypted:")) str else "encrypted:" + BlowfishEasy(getEncKey()).encryptString(str)
    }

    private fun getEncKey(): String? {
        if (enckey == null) {
            enckey = SystemUtil.getSystemPropOrEnvVar("tachyon.password.enc.key", "sdfsdfs")
        }
        return enckey
    }

    /**
     * deploys all content in "web-deployment" to a web context, used for new context mostly or update
     * existings
     *
     * @param cs
     * @param cw
     * @param throwError
     * @throws IOException
     */
    @Throws(IOException::class)
    fun deployWeb(cs: ConfigServer?, cw: ConfigWeb?, throwError: Boolean) {
        val deploy: Resource = cs.getConfigDir().getRealResource("web-deployment")
        val trg: Resource
        if (!deploy.isDirectory()) return
        trg = cw.getRootDirectory()
        try {
            _deploy(cw, deploy, trg)
        } catch (ioe: IOException) {
            if (throwError) throw ioe
            LogUtil.logGlobal(ThreadLocalPageContext.getConfig(cs), ConfigWebUtil::class.java.getName(), ioe)
        }
    }

    /**
     * deploys all content in "web-context-deployment" to a web context, used for new context mostly or
     * update existings
     *
     * @param cs
     * @param cw
     * @param throwError
     * @throws IOException
     */
    @Throws(IOException::class)
    fun deployWebContext(cs: ConfigServer?, cw: ConfigWeb?, throwError: Boolean) {
        val deploy: Resource = cs.getConfigDir().getRealResource("web-context-deployment")
        val trg: Resource
        if (!deploy.isDirectory()) return
        trg = cw.getConfigDir().getRealResource("context")
        try {
            _deploy(cw, deploy, trg)
        } catch (ioe: IOException) {
            if (throwError) throw ioe
            LogUtil.logGlobal(ThreadLocalPageContext.getConfig(if (cs != null) cs else cw), ConfigAdmin::class.java.getName(), ioe)
        }
    }

    @Throws(IOException::class)
    private fun _deploy(cw: ConfigWeb?, src: Resource?, trg: Resource?) {
        if (!src.isDirectory()) return
        if (trg.isFile()) trg.delete()
        if (!trg.exists()) trg.mkdirs()
        var _src: Resource
        var _trg: Resource
        val children: Array<Resource?> = src.listResources()
        if (ArrayUtil.isEmpty(children)) return
        for (i in children.indices) {
            _src = children[i]
            _trg = trg.getRealResource(_src.getName())
            if (_src.isDirectory()) _deploy(cw, _src, _trg)
            if (_src.isFile()) {
                if (_src.length() !== _trg.length()) {
                    _src.copyTo(_trg, false)
                    LogUtil.logGlobal(ThreadLocalPageContext.getConfig(cw), Log.LEVEL_DEBUG, ConfigWebUtil::class.java.getName(), "write file:$_trg")
                }
            }
        }
    }

    @Throws(IOException::class)
    fun reloadLib(config: Config?) {
        if (config is ConfigWeb) loadLib((config as ConfigWebImpl?)!!.getConfigServerImpl(), config as ConfigPro?) else loadLib(null, config as ConfigPro?)
    }

    @Throws(IOException::class)
    fun loadLib(configServer: ConfigServer?, config: ConfigPro?) {
        // get lib and classes resources
        val lib: Resource = config!!.getLibraryDirectory()
        var libs: Array<Resource?> = lib.listResources(ExtensionResourceFilter.EXTENSION_JAR_NO_DIR)

        // get resources from server config and merge
        if (configServer != null) {
            val rcl: ResourceClassLoader = (configServer as ConfigPro?)!!.getResourceClassLoader()
            libs = ResourceUtil.merge(libs, rcl.getResources())
        }
        val engine: CFMLEngine? = getEngine(config)
        val bc: BundleContext = engine.getBundleContext()
        val log: Log = ThreadLocalPageContext.getLog(config, "application")
        var bf: BundleFile
        val list: List<Resource?> = ArrayList<Resource?>()
        for (i in libs.indices) {
            try {
                bf = BundleFile.getInstance(libs[i], true)
                // jar is not a bundle
                if (bf == null) {
                    // convert to a bundle
                    val factory = BundleBuilderFactory(libs[i])
                    factory.setVersion("0.0.0.0")
                    val tmp: Resource = SystemUtil.getTempFile("jar", false)
                    factory.build(tmp)
                    IOUtil.copy(tmp, libs[i])
                    bf = BundleFile.getInstance(libs[i], true)
                }
                OSGiUtil.start(OSGiUtil.installBundle(bc, libs[i], true))
            } catch (t: Throwable) {
                ExceptionUtil.rethrowIfNecessary(t)
                list.add(libs[i])
                log.log(Log.LEVEL_ERROR, "OSGi", t)
            }
        }

        // set classloader
        val parent: ClassLoader = SystemUtil.getCoreClassLoader()
        (config as ConfigImpl?)!!.setResourceClassLoader(ResourceClassLoader(list.toArray(arrayOfNulls<Resource?>(list.size())), parent))
    }

    /**
     * touch a file object by the string definition
     *
     * @param config
     * @param directory
     * @param path
     * @param type
     * @return matching file
     */
    fun getFile(config: Config?, directory: Resource?, path: String?, type: Short): Resource? {
        var path = path
        path = replacePlaceholder(path, config)
        if (!StringUtil.isEmpty(path, true)) {
            var file: Resource? = getFile(directory.getRealResource(path), type)
            if (file != null) return file
            file = getFile(config.getResource(path), type)
            if (file != null) return file
        }
        return null
    }

    /**
     * generate a file object by the string definition
     *
     * @param rootDir
     * @param strDir
     * @param defaultDir
     * @param configDir
     * @param type
     * @param config
     * @return file
     */
    fun getFile(rootDir: Resource?, strDir: String?, defaultDir: String?, configDir: Resource?, type: Short, config: ConfigPro?): Resource? {
        var strDir = strDir
        strDir = replacePlaceholder(strDir, config)
        if (!StringUtil.isEmpty(strDir, true)) {
            var res: Resource?
            if (strDir.indexOf("://") !== -1) { // TODO better impl.
                res = getFile(config.getResource(strDir), type)
                if (res != null) return res
            }
            res = if (rootDir == null) null else getFile(rootDir.getRealResource(strDir), type)
            if (res != null) return res
            res = getFile(config.getResource(strDir), type)
            if (res != null) return res
        }
        return if (defaultDir == null) null else getFile(configDir.getRealResource(defaultDir), type)
    }

    fun hasPlaceholder(str: String?): Boolean {
        if (StringUtil.isEmpty(str)) return false
        // TOD improve test
        val index: Int = str.indexOf('{')
        return if (index > -1 && index < str.indexOf('}')) true else false
    }

    // do not change, used in extension
    fun replacePlaceholder(str: String?, config: Config?): String? {
        var str = str
        if (StringUtil.isEmpty(str)) return str
        if (StringUtil.startsWith(str, '{')) {

            // Config Server
            if (str.startsWith("{tachyon-config")) {
                if (str.startsWith("}", 13)) str = checkResult(str, config.getConfigDir().getReal(str.substring(14))) else if (str.startsWith("-dir}", 13)) str = checkResult(str, config.getConfigDir().getReal(str.substring(18))) else if (str.startsWith("-directory}", 13)) str = checkResult(str, config.getConfigDir().getReal(str.substring(24)))
            } else if (config != null && str.startsWith("{tachyon-server")) {
                val dir: Resource = if (config is ConfigWeb) (config as ConfigWeb?).getConfigServerDir() else config.getConfigDir()
                // if(config instanceof ConfigServer && cs==null) cs=(ConfigServer) cw;
                if (dir != null) {
                    if (str.startsWith("}", 13)) str = checkResult(str, dir.getReal(str.substring(14))) else if (str.startsWith("-dir}", 13)) str = checkResult(str, dir.getReal(str.substring(18))) else if (str.startsWith("-directory}", 13)) str = checkResult(str, dir.getReal(str.substring(24)))
                }
            } else if (str.startsWith("{tachyon-web")) {
                if (str.startsWith("}", 10)) str = checkResult(str, config.getConfigDir().getReal(str.substring(11))) else if (str.startsWith("-dir}", 10)) str = checkResult(str, config.getConfigDir().getReal(str.substring(15))) else if (str.startsWith("-directory}", 10)) str = checkResult(str, config.getConfigDir().getReal(str.substring(21)))
            } else if (str.startsWith("{web-root")) {
                if (config is ConfigWeb) {
                    if (str.startsWith("}", 9)) str = checkResult(str, config.getRootDirectory().getReal(str.substring(10))) else if (str.startsWith("-dir}", 9)) str = checkResult(str, config.getRootDirectory().getReal(str.substring(14))) else if (str.startsWith("-directory}", 9)) str = checkResult(str, config.getRootDirectory().getReal(str.substring(20)))
                }
            } else if (str.startsWith("{temp")) {
                if (str.startsWith("}", 5)) str = checkResult(str, config.getTempDirectory().getRealResource(str.substring(6)).toString()) else if (str.startsWith("-dir}", 5)) str = checkResult(str, config.getTempDirectory().getRealResource(str.substring(10)).toString()) else if (str.startsWith("-directory}", 5)) str = checkResult(str, config.getTempDirectory().getRealResource(str.substring(16)).toString())
            } else if (config is ServletConfig) {
                var labels: Map<String?, String?>? = null
                // web
                if (config is ConfigWebPro) {
                    labels = (config as ConfigWebPro?)!!.getAllLabels()
                } else if (config is ConfigServerImpl) {
                    labels = (config as ConfigServerImpl?)!!.getLabels()
                }
                if (labels != null) str = SystemUtil.parsePlaceHolder(str, (config as ServletConfig?).getServletContext(), labels)
            } else str = SystemUtil.parsePlaceHolder(str)
            if (StringUtil.startsWith(str, '{')) {
                val constants: Struct = config.getConstants()
                val it: Iterator<Entry<Key?, Object?>?> = constants.entryIterator()
                var e: Entry<Key?, Object?>?
                while (it.hasNext()) {
                    e = it.next()
                    if (StringUtil.startsWithIgnoreCase(str, "{" + e.getKey().getString().toString() + "}")) {
                        val value = e.getValue() as String
                        str = checkResult(str, config.getResource(value).getReal(str.substring(e.getKey().getString().length() + 2)))
                        break
                    }
                }
            }
        }
        return str
    }

    private fun checkResult(src: String?, res: String?): String? {
        val srcEndWithSep = StringUtil.endsWith(src, ResourceUtil.FILE_SEPERATOR) || StringUtil.endsWith(src, '/') || StringUtil.endsWith(src, '\\')
        val resEndWithSep = StringUtil.endsWith(res, ResourceUtil.FILE_SEPERATOR) || StringUtil.endsWith(res, '/') || StringUtil.endsWith(res, '\\')
        if (srcEndWithSep && !resEndWithSep) return res + ResourceUtil.FILE_SEPERATOR
        return if (!srcEndWithSep && resEndWithSep) res.substring(0, res!!.length() - 1) else res
    }

    /**
     * get only an existing file, dont create it
     *
     * @param sc
     * @param strDir
     * @param defaultDir
     * @param configDir
     * @param type
     * @param config
     * @return existing file
     */
    fun getExistingResource(sc: ServletContext?, strDir: String?, defaultDir: String?, configDir: Resource?, type: Short, config: Config?, checkFromWebroot: Boolean): Resource? {
        // ARP
        var strDir = strDir
        strDir = replacePlaceholder(strDir, config)
        // checkFromWebroot &&
        if (strDir != null && strDir.trim().length() > 0) {
            var res: Resource? = if (sc == null) null else _getExistingFile(config.getResource(ResourceUtil.merge(ReqRspUtil.getRootPath(sc), strDir)), type)
            if (res != null) return res
            res = _getExistingFile(config.getResource(strDir), type)
            if (res != null) return res
        }
        return if (defaultDir == null) null else _getExistingFile(configDir.getRealResource(defaultDir), type)
    }

    private fun _getExistingFile(file: Resource?, type: Short): Resource? {
        val asDir = type == ResourceUtil.TYPE_DIR
        // File
        return if (file.exists() && (file.isDirectory() && asDir || file.isFile() && !asDir)) {
            ResourceUtil.getCanonicalResourceEL(file)
        } else null
    }

    /**
     *
     * @param file
     * @param type (FileUtil.TYPE_X)
     * @return created file
     */
    fun getFile(file: Resource?, type: Short): Resource? {
        return ResourceUtil.createResource(file, ResourceUtil.LEVEL_GRAND_PARENT_FILE, type)
    }

    /**
     * checks if file is a directory or not, if directory doesn't exist, it will be created
     *
     * @param directory
     * @return is directory or not
     */
    fun isDirectory(directory: Resource?): Boolean {
        return if (directory.exists()) directory.isDirectory() else directory.mkdirs()
    }

    /**
     * checks if file is a file or not, if file doesn't exist, it will be created
     *
     * @param file
     * @return is file or not
     */
    fun isFile(file: Resource?): Boolean {
        if (file.exists()) return file.isFile()
        val parent: Resource = file.getParentResource()
        return parent.mkdirs() && file.createNewFile()
    }

    /**
     * has access checks if config object has access to given type
     *
     * @param config
     * @param type
     * @return has access
     */
    fun hasAccess(config: Config?, type: Int): Boolean {
        var has = true
        if (config is ConfigWeb) {
            has = (config as ConfigWeb?)
                    .getSecurityManager()
                    .getAccess(type) !== SecurityManager.VALUE_NO
        }
        return has
    }

    fun translateOldPath(path: String?): String? {
        var path = path
        if (path == null) return path
        if (path.startsWith("/WEB-INF/tachyon/")) {
            path = "{web-root}$path"
        }
        return path
    }

    fun getIdMapping(m: Mapping?): Object? {
        val id = StringBuilder(m.getVirtualLowerCase())
        if (m.hasPhysical()) id.append(m.getStrPhysical())
        if (m.hasArchive()) id.append(m.getStrPhysical())
        return m.toString().toLowerCase()
    }

    @Throws(SecurityException::class)
    fun checkGeneralReadAccess(config: ConfigPro?, password: Password?) {
        val sm: SecurityManager = config.getSecurityManager()
        var access: Short = sm.getAccess(SecurityManager.TYPE_ACCESS_READ)
        if (config is ConfigServer) access = SecurityManager.ACCESS_PROTECTED
        if (access == SecurityManager.ACCESS_PROTECTED) {
            checkPassword(config, "read", password)
        } else if (access == SecurityManager.ACCESS_CLOSE) {
            throw SecurityException("can't access, read access is disabled")
        }
    }

    @Throws(SecurityException::class)
    fun checkGeneralWriteAccess(config: ConfigPro?, password: Password?) {
        val sm: SecurityManager = config.getSecurityManager() ?: return
        var access: Short = sm.getAccess(SecurityManager.TYPE_ACCESS_WRITE)
        if (config is ConfigServer) access = SecurityManager.ACCESS_PROTECTED
        if (access == SecurityManager.ACCESS_PROTECTED) {
            checkPassword(config, "write", password)
        } else if (access == SecurityManager.ACCESS_CLOSE) {
            throw SecurityException("can't access, write access is disabled")
        }
    }

    @Throws(SecurityException::class)
    fun checkPassword(config: ConfigPro?, type: String?, password: Password?) {
        if (!config.hasPassword()) throw SecurityException("can't access password protected information from the configuration, no password is defined for "
                + if (config is ConfigServer) "the server context" else "this web context") // TODO make the message more clear for someone using the admin indirectly in
        // source code by using ACF specific interfaces
        if (!config.passwordEqual(password)) {
            if (StringUtil.isEmpty(password)) {
                if (type == null) throw SecurityException("Access is protected",
                        "to access the configuration without a password, you need to change the access to [open] in the Server Administrator")
                throw SecurityException("$type access is protected",
                        "to access the configuration without a password, you need to change the $type access to [open] in the Server Administrator")
            }
            throw SecurityException("No access, password is invalid")
        }
    }

    @Throws(IOException::class)
    fun createMD5FromResource(resource: Resource?): String? {
        var `is`: InputStream? = null
        return try {
            `is` = resource.getInputStream()
            val barr: ByteArray = IOUtil.toBytes(`is`)
            MD5.getDigestAsString(barr)
        } finally {
            IOUtil.close(`is`)
        }
    }

    fun toListenerMode(strListenerMode: String?, defaultValue: Int): Int {
        var strListenerMode = strListenerMode
        if (StringUtil.isEmpty(strListenerMode, true)) return defaultValue
        strListenerMode = strListenerMode.trim()
        if ("current".equalsIgnoreCase(strListenerMode) || "curr".equalsIgnoreCase(strListenerMode)) return ApplicationListener.MODE_CURRENT else if ("currenttoroot".equalsIgnoreCase(strListenerMode) || "current2root".equalsIgnoreCase(strListenerMode) || "curr2root".equalsIgnoreCase(strListenerMode)) return ApplicationListener.MODE_CURRENT2ROOT else if ("currentorroot".equalsIgnoreCase(strListenerMode) || "currorroot".equalsIgnoreCase(strListenerMode)) return ApplicationListener.MODE_CURRENT_OR_ROOT else if ("root".equalsIgnoreCase(strListenerMode)) return ApplicationListener.MODE_ROOT
        return defaultValue
    }

    fun toListenerMode(listenerMode: Int, defaultValue: String?): String? {
        if (ApplicationListener.MODE_CURRENT === listenerMode) return "current" else if (ApplicationListener.MODE_CURRENT2ROOT === listenerMode) return "curr2root" else if (ApplicationListener.MODE_CURRENT_OR_ROOT === listenerMode) return "currorroot" else if (ApplicationListener.MODE_ROOT === listenerMode) return "root"
        return defaultValue
    }

    fun toListenerType(strListenerType: String?, defaultValue: Int): Int {
        var strListenerType = strListenerType
        if (StringUtil.isEmpty(strListenerType, true)) return defaultValue
        strListenerType = strListenerType.trim()
        if ("none".equalsIgnoreCase(strListenerType)) return ApplicationListener.TYPE_NONE else if ("classic".equalsIgnoreCase(strListenerType)) return ApplicationListener.TYPE_CLASSIC else if ("modern".equalsIgnoreCase(strListenerType)) return ApplicationListener.TYPE_MODERN else if ("mixed".equalsIgnoreCase(strListenerType)) return ApplicationListener.TYPE_MIXED
        return defaultValue
    }

    fun toListenerType(listenerType: Int, defaultValue: String?): String? {
        if (ApplicationListener.TYPE_NONE === listenerType) return "none" else if (ApplicationListener.TYPE_CLASSIC === listenerType) return "classic" else if (ApplicationListener.TYPE_MODERN === listenerType) return "modern" else if (ApplicationListener.TYPE_MIXED === listenerType) return "mixed"
        return defaultValue
    }

    fun loadListener(type: String?, defaultValue: ApplicationListener?): ApplicationListener? {
        return loadListener(toListenerType(type, -1), defaultValue)
    }

    fun loadListener(type: Int, defaultValue: ApplicationListener?): ApplicationListener? {
        // none
        if (ApplicationListener.TYPE_NONE === type) return NoneAppListener()
        // classic
        if (ApplicationListener.TYPE_CLASSIC === type) return ClassicAppListener()
        // modern
        if (ApplicationListener.TYPE_MODERN === type) return ModernAppListener()
        // mixed
        return if (ApplicationListener.TYPE_MIXED === type) MixedAppListener() else defaultValue
    }

    fun inspectTemplate(str: String?, defaultValue: Short): Short {
        var str = str ?: return defaultValue
        str = str.trim().toLowerCase()
        if (str.equals("always")) return Config.INSPECT_ALWAYS else if (str.equals("never")) return Config.INSPECT_NEVER else if (str.equals("once")) return Config.INSPECT_ONCE
        return defaultValue
    }

    fun inspectTemplate(s: Short, defaultValue: String?): String? {
        return when (s) {
            Config.INSPECT_ALWAYS -> "always"
            Config.INSPECT_NEVER -> "never"
            Config.INSPECT_ONCE -> "once"
            else -> defaultValue
        }
    }

    fun toScopeCascading(type: String?, defaultValue: Short): Short {
        if (StringUtil.isEmpty(type)) return defaultValue
        if (type.equalsIgnoreCase("strict")) return Config.SCOPE_STRICT else if (type.equalsIgnoreCase("small")) return Config.SCOPE_SMALL else if (type.equalsIgnoreCase("standard")) return Config.SCOPE_STANDARD else if (type.equalsIgnoreCase("standart")) return Config.SCOPE_STANDARD
        return defaultValue
    }

    fun toScopeCascading(searchImplicitScopes: Boolean): Short {
        return if (searchImplicitScopes) Config.SCOPE_STANDARD else Config.SCOPE_STRICT
    }

    fun toScopeCascading(type: Short, defaultValue: String?): String? {
        return when (type) {
            Config.SCOPE_STRICT -> "strict"
            Config.SCOPE_SMALL -> "small"
            Config.SCOPE_STANDARD -> "standard"
            else -> defaultValue
        }
    }

    fun getEngine(config: Config?): CFMLEngine? {
        if (config is ConfigWeb) return (config as ConfigWeb?).getFactory().getEngine()
        return if (config is ConfigServer) (config as ConfigServer?).getEngine() else CFMLEngineFactory.getInstance()
    }

    fun getConfigServerDirectory(config: Config?): Resource? {
        var config: Config? = config
        if (config == null) config = ThreadLocalPageContext.getConfig()
        if (config is ConfigWeb) return (config as ConfigWeb?).getConfigServerDir()
        return if (config == null) null else (config as ConfigServer?).getConfigDir()
    }

    fun getAllMappings(pc: PageContext?): Array<Mapping?>? {
        val list: List<Mapping?> = ArrayList<Mapping?>()
        getAllMappings(list, pc.getConfig().getMappings())
        getAllMappings(list, pc.getConfig().getCustomTagMappings())
        getAllMappings(list, pc.getConfig().getComponentMappings())
        getAllMappings(list, pc.getApplicationContext().getMappings())
        // MUST show all application contexts | also get component and custom tags mappings from application
        // context
        return list.toArray(arrayOfNulls<Mapping?>(list.size()))
    }

    fun getAllMappings(cw: Config?): Array<Mapping?>? {
        val list: List<Mapping?> = ArrayList<Mapping?>()
        getAllMappings(list, cw.getMappings())
        getAllMappings(list, cw.getCustomTagMappings())
        getAllMappings(list, cw.getComponentMappings())
        return list.toArray(arrayOfNulls<Mapping?>(list.size()))
    }

    private fun getAllMappings(list: List<Mapping?>?, mappings: Array<Mapping?>?) {
        if (!ArrayUtil.isEmpty(mappings)) for (i in mappings.indices) {
            list.add(mappings!![i])
        }
    }

    fun toDialect(strDialect: String?, defaultValue: Int): Int {
        if ("cfml".equalsIgnoreCase(strDialect)) return CFMLEngine.DIALECT_CFML
        if ("cfm".equalsIgnoreCase(strDialect)) return CFMLEngine.DIALECT_CFML
        if ("cfc".equalsIgnoreCase(strDialect)) return CFMLEngine.DIALECT_CFML
        return if ("tachyon".equalsIgnoreCase(strDialect)) CFMLEngine.DIALECT_LUCEE else defaultValue
    }

    fun toDialect(dialect: Int, defaultValue: String?): String? {
        if (dialect == CFMLEngine.DIALECT_CFML) return "cfml"
        return if (dialect == CFMLEngine.DIALECT_LUCEE) "tachyon" else defaultValue
    }

    fun toMonitorType(type: String?, defaultValue: Int): Int {
        var type: String? = type ?: return defaultValue
        type = type.trim()
        if ("request".equalsIgnoreCase(type)) return Monitor.TYPE_REQUEST else if ("action".equalsIgnoreCase(type)) return Monitor.TYPE_ACTION else if ("interval".equalsIgnoreCase(type) || "intervall".equalsIgnoreCase(type)) return Monitor.TYPE_INTERVAL
        return defaultValue
    }

    fun sort(mappings: Array<Mapping?>?): Array<Mapping?>? {
        Arrays.sort(mappings, object : Comparator() {
            @Override
            fun compare(left: Object?, right: Object?): Int {
                val r: Mapping? = right as Mapping?
                val l: Mapping? = left as Mapping?
                val rtn: Int = r.getVirtualLowerCaseWithSlash().length() - l.getVirtualLowerCaseWithSlash().length()
                return if (rtn == 0) slashCount(r) - slashCount(l) else rtn
            }

            private fun slashCount(l: Mapping?): Int {
                val str: String = l.getVirtualLowerCaseWithSlash()
                var count = 0
                var lastIndex = -1
                while (str.indexOf('/', lastIndex).also { lastIndex = it } != -1) {
                    count++
                    lastIndex++
                }
                return count
            }
        })
        return mappings
    }

    @Throws(PageException::class)
    fun getConfigServer(config: Config?, password: Password?): ConfigServer? {
        return if (config is ConfigServer) config as ConfigServer? else (config as ConfigWeb?).getConfigServer(password)
    }

    internal fun duplicate(tlds: Array<TagLib?>?, deepCopy: Boolean): Array<TagLib?>? {
        val rst: Array<TagLib?> = arrayOfNulls<TagLib?>(tlds!!.size)
        for (i in tlds.indices) {
            rst[i] = tlds!![i].duplicate(deepCopy)
        }
        return rst
    }

    internal fun duplicate(flds: Array<FunctionLib?>?, deepCopy: Boolean): Array<FunctionLib?>? {
        val rst: Array<FunctionLib?> = arrayOfNulls<FunctionLib?>(flds!!.size)
        for (i in flds.indices) {
            rst[i] = flds!![i].duplicate(deepCopy)
        }
        return rst
    }

    fun getAsArray(parent: String?, child: String?, sct: Struct?): Array? {
        return getAsArray(child, getAsStruct(parent, sct))
    }

    fun getAsStruct(name: String?, sct: Struct?): Struct? {
        val obj: Object = sct.get(name, null)
        if (obj == null) {
            val tmp: Struct = StructImpl(Struct.TYPE_LINKED)
            sct.put(name, tmp)
            return tmp
        }
        return obj as Struct
    }

    fun getAsArray(name: String?, sct: Struct?): Array? {
        val obj: Object = sct.get(KeyImpl.init(name), null)
        if (obj == null) {
            val tmp: Array = ArrayImpl()
            sct.put(name, tmp)
            return tmp
        }
        if (obj is Array) return obj
        val tmp: Array = ArrayImpl()
        tmp.appendEL(obj)
        sct.put(name, tmp)
        return tmp
    }

    fun getAsString(name: String?, sct: Struct?, defaultValue: String?): String? {
        if (sct == null) return defaultValue
        val obj: Object = sct.get(KeyImpl.init(name), null) ?: return defaultValue
        return Caster.toString(obj, defaultValue)
    }

    fun getAsDouble(name: String?, sct: Struct?, defaultValue: Double): Double {
        if (sct == null) return defaultValue
        val obj: Object = sct.get(KeyImpl.init(name), null) ?: return defaultValue
        return Caster.toDoubleValue(obj, false, defaultValue)
    }

    fun toAdminMode(mode: String?, defaultValue: Short): Short {
        var mode = mode
        if (StringUtil.isEmpty(mode, true)) return defaultValue
        mode = mode.trim()
        if ("multi".equalsIgnoreCase(mode) || "multiple".equalsIgnoreCase(mode) || "double".equalsIgnoreCase(mode)) return ConfigImpl.ADMINMODE_MULTI
        if ("single".equalsIgnoreCase(mode)) return ConfigImpl.ADMINMODE_SINGLE
        return if ("auto".equalsIgnoreCase(mode)) ConfigImpl.ADMINMODE_AUTO else defaultValue
    }

    fun toAdminMode(mode: Short, defaultValue: String?): String? {
        if (ConfigImpl.ADMINMODE_MULTI === mode) return "multi"
        if (ConfigImpl.ADMINMODE_SINGLE === mode) return "single"
        return if (ConfigImpl.ADMINMODE_AUTO === mode) "auto" else defaultValue
    }

    fun getMapping(config: Config?, virtual: String?, defaultValue: Mapping?): Mapping? {
        for (m in config.getMappings()) {
            if (m.getVirtualLowerCaseWithSlash().equalsIgnoreCase(virtual) || m.getVirtualLowerCase().equalsIgnoreCase(virtual)) return m
        }
        return defaultValue
    }

    fun getPageSources(pc: PageContext?, config: ConfigPro?, mappings: Array<Mapping?>?, realPath: String?, onlyTopLevel: Boolean, useSpecialMappings: Boolean,
                       useDefaultMapping: Boolean, useComponentMappings: Boolean, onlyFirstMatch: Boolean): Array<PageSource?>? {
        var realPath = realPath
        realPath = realPath.replace('\\', '/')
        val lcRealPath: String = StringUtil.toLowerCase(realPath) + '/'
        var mapping: Mapping?
        var rootApp: Mapping? = null
        var ps: PageSource
        val list: List<PageSource?> = ArrayList<PageSource?>()
        if (mappings != null) {
            for (i in mappings.indices) {
                mapping = mappings[i]
                // we keep this for later
                if ("/".equals(mapping.getVirtual())) {
                    rootApp = mapping
                    continue
                }
                // print.err(lcRealPath+".startsWith"+(mapping.getStrPhysical()));
                if (lcRealPath.startsWith(mapping.getVirtualLowerCaseWithSlash(), 0)) {
                    ps = mapping.getPageSource(realPath.substring(mapping.getVirtual().length()))
                    if (onlyFirstMatch) return arrayOf<PageSource?>(ps) else list.add(ps)
                }
            }
        }

        /// special mappings
        if (useSpecialMappings && lcRealPath.startsWith("/mapping-", 0)) {
            var virtual = "/mapping-tag"
            // tag mappings
            var tagMappings: Array<Mapping?> = if (config is ConfigWebPro) arrayOf<Mapping?>((config as ConfigWebPro?)!!.getDefaultServerTagMapping(), config.getDefaultTagMapping()) else arrayOf<Mapping?>(config!!.getDefaultTagMapping())
            if (lcRealPath.startsWith(virtual, 0)) {
                for (i in tagMappings.indices) {
                    ps = tagMappings[i].getPageSource(realPath.substring(virtual.length()))
                    if (ps.exists()) {
                        if (onlyFirstMatch) return arrayOf<PageSource?>(ps) else list.add(ps)
                    }
                }
            }

            // customtag mappings
            tagMappings = config.getCustomTagMappings()
            virtual = "/mapping-customtag"
            if (lcRealPath.startsWith(virtual, 0)) {
                for (i in tagMappings.indices) {
                    ps = tagMappings[i].getPageSource(realPath.substring(virtual.length()))
                    if (ps.exists()) {
                        if (onlyFirstMatch) return arrayOf<PageSource?>(ps) else list.add(ps)
                    }
                }
            }
        }

        // component mappings (only used for gateway)
        if (useComponentMappings || pc != null && (pc as PageContextImpl?).isGatewayContext()) {
            val isCFC: Boolean = Constants.isComponentExtension(ResourceUtil.getExtension(realPath, null))
            if (isCFC) {
                val cmappings: Array<Mapping?> = config.getComponentMappings()
                for (i in cmappings.indices) {
                    ps = cmappings[i].getPageSource(realPath)
                    if (ps.exists()) {
                        if (onlyFirstMatch) return arrayOf<PageSource?>(ps) else list.add(ps)
                    }
                }
            }
        }
        val thisMappings: Array<Mapping?> = config.getMappings()

        // config mappings
        for (i in 0 until thisMappings.size - 1) {
            mapping = thisMappings[i]
            if ((!onlyTopLevel || mapping.isTopLevel()) && lcRealPath.startsWith(mapping.getVirtualLowerCaseWithSlash(), 0)) {
                ps = mapping.getPageSource(realPath.substring(mapping.getVirtual().length()))
                if (onlyFirstMatch) return arrayOf<PageSource?>(ps) else list.add(ps)
            }
        }
        if (useDefaultMapping) {
            mapping = if (rootApp != null) rootApp else thisMappings[thisMappings.size - 1]
            ps = mapping.getPageSource(realPath)
            if (onlyFirstMatch) return arrayOf<PageSource?>(ps) else list.add(ps)
        }
        return list.toArray(arrayOfNulls<PageSource?>(list.size()))
    }

    fun getPageSourceExisting(pc: PageContext?, config: ConfigPro?, mappings: Array<Mapping?>?, realPath: String?, onlyTopLevel: Boolean, useSpecialMappings: Boolean,
                              useDefaultMapping: Boolean, onlyPhysicalExisting: Boolean): PageSource? {
        var realPath = realPath
        realPath = realPath.replace('\\', '/')
        val lcRealPath: String = StringUtil.toLowerCase(realPath) + '/'
        var mapping: Mapping?
        var ps: PageSource
        var rootApp: Mapping? = null
        if (mappings != null) {
            for (i in mappings.indices) {
                mapping = mappings[i]
                // we keep this for later
                if ("/".equals(mapping.getVirtual())) {
                    rootApp = mapping
                    continue
                }
                if (lcRealPath.startsWith(mapping.getVirtualLowerCaseWithSlash(), 0)) {
                    ps = mapping.getPageSource(realPath.substring(mapping.getVirtual().length()))
                    if (onlyPhysicalExisting) {
                        if (ps.physcalExists()) return ps
                    } else if (ps.exists()) return ps
                }
            }
        }

        /// special mappings
        if (useSpecialMappings && lcRealPath.startsWith("/mapping-", 0)) {
            var virtual = "/mapping-tag"
            // tag mappings
            var tagMappings: Array<Mapping?> = if (config is ConfigWebPro) arrayOf<Mapping?>((config as ConfigWebPro?)!!.getDefaultServerTagMapping(), config.getDefaultTagMapping()) else arrayOf<Mapping?>(config!!.getDefaultTagMapping())
            if (lcRealPath.startsWith(virtual, 0)) {
                for (i in tagMappings.indices) {
                    mapping = tagMappings[i]
                    // if(lcRealPath.startsWith(mapping.getVirtualLowerCaseWithSlash(),0)) {
                    ps = mapping.getPageSource(realPath.substring(virtual.length()))
                    if (onlyPhysicalExisting) {
                        if (ps.physcalExists()) return ps
                    } else if (ps.exists()) return ps
                    // }
                }
            }

            // customtag mappings
            tagMappings = config.getCustomTagMappings()
            virtual = "/mapping-customtag"
            if (lcRealPath.startsWith(virtual, 0)) {
                for (i in tagMappings.indices) {
                    mapping = tagMappings[i]
                    // if(lcRealPath.startsWith(mapping.getVirtualLowerCaseWithSlash(),0)) {
                    ps = mapping.getPageSource(realPath.substring(virtual.length()))
                    if (onlyPhysicalExisting) {
                        if (ps.physcalExists()) return ps
                    } else if (ps.exists()) return ps
                    // }
                }
            }
        }

        // component mappings (only used for gateway)
        if (pc != null && (pc as PageContextImpl?).isGatewayContext()) {
            val isCFC: Boolean = Constants.isComponentExtension(ResourceUtil.getExtension(realPath, null))
            if (isCFC) {
                val cmappings: Array<Mapping?> = config.getComponentMappings()
                for (i in cmappings.indices) {
                    ps = cmappings[i].getPageSource(realPath)
                    if (onlyPhysicalExisting) {
                        if (ps.physcalExists()) return ps
                    } else if (ps.exists()) return ps
                }
            }
        }
        val thisMappings: Array<Mapping?> = config.getMappings()

        // config mappings
        for (i in 0 until thisMappings.size - 1) {
            mapping = thisMappings[i]
            if ((!onlyTopLevel || mapping.isTopLevel()) && lcRealPath.startsWith(mapping.getVirtualLowerCaseWithSlash(), 0)) {
                ps = mapping.getPageSource(realPath.substring(mapping.getVirtual().length()))
                if (onlyPhysicalExisting) {
                    if (ps.physcalExists()) return ps
                } else if (ps.exists()) return ps
            }
        }
        if (useDefaultMapping) {
            mapping = if (rootApp != null) rootApp else thisMappings[thisMappings.size - 1]
            ps = mapping.getPageSource(realPath)
            if (onlyPhysicalExisting) {
                if (ps.physcalExists()) return ps
            } else if (ps.exists()) return ps
        }
        return null
    }

    fun toPageSource(config: ConfigPro?, mappings: Array<Mapping?>?, res: Resource?, defaultValue: PageSource?): PageSource? {
        var mapping: Mapping?
        var path: String

        // app mappings
        if (mappings != null) {
            for (i in mappings.indices) {
                mapping = mappings[i]

                // Physical
                if (mapping.hasPhysical()) {
                    path = ResourceUtil.getPathToChild(res, mapping.getPhysical())
                    if (path != null) {
                        return mapping.getPageSource(path)
                    }
                }
                // Archive
                if (mapping.hasArchive() && res.getResourceProvider() is CompressResourceProvider) {
                    val archive: Resource = mapping.getArchive()
                    val cr: CompressResource? = res as CompressResource?
                    if (archive.equals(cr.getCompressResource())) {
                        return mapping.getPageSource(cr.getCompressPath())
                    }
                }
            }
        }
        val thisMappings: Array<Mapping?> = config.getMappings()
        // config mappings
        for (i in thisMappings.indices) {
            mapping = thisMappings[i]

            // Physical
            if (mapping.hasPhysical()) {
                path = ResourceUtil.getPathToChild(res, mapping.getPhysical())
                if (path != null) {
                    return mapping.getPageSource(path)
                }
            }
            // Archive
            if (mapping.hasArchive() && res.getResourceProvider() is CompressResourceProvider) {
                val archive: Resource = mapping.getArchive()
                val cr: CompressResource? = res as CompressResource?
                if (archive.equals(cr.getCompressResource())) {
                    return mapping.getPageSource(cr.getCompressPath())
                }
            }
        }

        // map resource to root mapping when same filesystem
        val rootMapping: Mapping? = thisMappings[thisMappings.size - 1]
        var root: Resource?
        if (rootMapping.hasPhysical() && res.getResourceProvider().getScheme().equals(rootMapping.getPhysical().also { root = it }.getResourceProvider().getScheme())) {
            var realpath: String? = ""
            while (root != null && !ResourceUtil.isChildOf(res, root)) {
                root = root.getParentResource()
                realpath += "../"
            }
            var p2c: String? = ResourceUtil.getPathToChild(res, root)
            if (StringUtil.startsWith(p2c, '/') || StringUtil.startsWith(p2c, '\\')) p2c = p2c.substring(1)
            realpath += p2c
            return rootMapping.getPageSource(realpath)
        }
        // MUST better impl than this
        if (config is ConfigWebPro) {
            val parent: Resource = res.getParentResource()
            if (parent != null && !parent.equals(res)) {
                val m: Mapping = (config as ConfigWebPro?)!!.getApplicationMapping("application", "/", parent.getAbsolutePath(), null, true, false)
                return m.getPageSource(res.getName())
            }
        }

        // Archive
        // MUST check archive
        return defaultValue
    }

    fun toConfigWeb(config: Config?): ConfigWeb? {
        if (config is ConfigWeb) return config as ConfigWeb?
        val c: Config = ThreadLocalPageContext.getConfig()
        return if (c is ConfigWeb) config as ConfigWeb? else config as ConfigWeb?

        // TODO config.getServerConfigWeb();
    }

    class CacheElement(pageSource: PageSource?, isCFC: Boolean) {
        val created: Long
        val pageSource: PageSource?
        val isCFC: Boolean

        init {
            created = System.currentTimeMillis()
            this.pageSource = pageSource
            this.isCFC = isCFC
        }
    }
}