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
package lucee.runtime

import java.io.IOException

/**
 * Mapping class
 */
class MappingImpl(config: Config?, virtual: String?, strPhysical: String?, strArchive: String?, inspect: Short, physicalFirst: Boolean, hidden: Boolean, readonly: Boolean,
                  topLevel: Boolean, appMapping: Boolean, ignoreVirtual: Boolean, appListener: ApplicationListener?, listenerMode: Int, listenerType: Int, checkPhysicalFromWebroot: Boolean,
                  checkArchiveFromWebroot: Boolean) : Mapping {
    private val virtual: String? = null
    private val lcVirtual: String?
    private val topLevel: Boolean
    private val inspect: Short
    private val physicalFirst: Boolean

    @Transient
    private var pclCFM: PhysicalClassLoader? = null

    @Transient
    private var pclCFC: PhysicalClassLoader? = null

    @Transient
    private var pcoll: PCLCollection? = null
    private var archive: Resource?
    private var hasArchive: Boolean
    private val config: Config?
    private var classRootDirectory: Resource? = null
    private val pageSourcePool: PageSourcePool? = PageSourcePool()
    private val readonly = false
    private val hidden = false
    private val strArchive: String?
    private val strPhysical: String?
    private var physical: Resource?
    private val lcVirtualWithSlash: String?
    private val customTagPath: Map<String?, SoftReference<Object?>?>? = ConcurrentHashMap<String?, SoftReference<Object?>?>()
    private val appMapping: Boolean
    private val ignoreVirtual: Boolean
    private val appListener: ApplicationListener?
    private var archiveBundle: Bundle? = null
    private var archMod: Long = 0
    private val listenerMode: Int
    private val listenerType: Int
    private val checkPhysicalFromWebroot: Boolean
    private val checkArchiveFromWebroot: Boolean

    constructor(config: Config?, virtual: String?, strPhysical: String?, strArchive: String?, inspect: Short, physicalFirst: Boolean, hidden: Boolean, readonly: Boolean,
                topLevel: Boolean, appMapping: Boolean, ignoreVirtual: Boolean, appListener: ApplicationListener?, listenerMode: Int, listenerType: Int) : this(config, virtual, strPhysical, strArchive, inspect, physicalFirst, hidden, readonly, topLevel, appMapping, ignoreVirtual, appListener, listenerMode, listenerType, true,
            true) {
    }

    private fun loadArchive() {
        if (archive == null || archMod == archive.lastModified()) return
        val engine: CFMLEngine = ConfigWebUtil.getEngine(config)
        val bc: BundleContext = engine.getBundleContext()
        try {
            archiveBundle = OSGiUtil.installBundle(bc, archive, true)
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
            archMod = archive.lastModified()
            ThreadLocalPageContext.getLog(config, "application").log(Log.LEVEL_ERROR, "OSGi", t)
            archive = null
        }
    }

    @Override
    @Throws(ClassNotFoundException::class)
    fun getArchiveClass(className: String?): Class<*>? {
        if (archiveBundle != null) {
            return archiveBundle.loadClass(className)
        }
        throw ClassNotFoundException("there is no archive context to load $className from it")
    }

    @Override
    fun getArchiveClass(className: String?, defaultValue: Class<*>?): Class<*>? {
        try {
            if (archiveBundle != null) return archiveBundle.loadClass(className)
            // else if(archiveClassLoader!=null) return archiveClassLoader.loadClass(className);
        } catch (e: ClassNotFoundException) {
        }
        return defaultValue
    }

    @Override
    fun getArchiveResourceAsStream(name: String?): InputStream? {
        // MUST implement
        return null
    }

    fun loadClass(className: String?): Class<*>? {
        var clazz: Class<*>?
        if (isPhysicalFirst()) {
            clazz = getPhysicalClass(className, null as Class<*>?)
            if (clazz != null) return clazz
            clazz = getArchiveClass(className, null)
            if (clazz != null) return clazz
        }
        clazz = getArchiveClass(className, null)
        if (clazz != null) return clazz
        clazz = getPhysicalClass(className, null as Class<*>?)
        return if (clazz != null) clazz else null
    }

    @Throws(IOException::class)
    fun touchClassLoader(): PCLCollection? {
        if (pcoll == null) {
            pcoll = PCLCollection(this, getClassRootDirectory(), getConfig().getClassLoader(), 100)
        }
        return pcoll
    }

    @Throws(IOException::class)
    private fun touchPhysicalClassLoader(forComponent: Boolean): PhysicalClassLoader? {
        if (if (forComponent) pclCFC == null else pclCFM == null) {
            if (forComponent) pclCFC = PhysicalClassLoader(config, getClassRootDirectory()) else pclCFM = PhysicalClassLoader(config, getClassRootDirectory())
        } else if ((if (forComponent) pclCFC else pclCFM).getSize(true) > (if (forComponent) MAX_SIZE_CFC else MAX_SIZE_CFM)) {
            val pcl: PhysicalClassLoader = if (forComponent) pclCFC else pclCFM
            synchronized(pageSourcePool) { pageSourcePool!!.clearPages(pcl) }
            pcl.clear()
            if (forComponent) pclCFC = PhysicalClassLoader(config, getClassRootDirectory()) else pclCFM = PhysicalClassLoader(config, getClassRootDirectory())
        }
        return if (forComponent) pclCFC else pclCFM
    }

    @Override
    @Throws(ClassNotFoundException::class, IOException::class)
    fun getPhysicalClass(className: String?): Class<*>? {
        return touchPhysicalClassLoader(className.contains("_cfc\$cf")).loadClass(className)
        // return touchClassLoader().loadClass(className);
    }

    fun getPhysicalClass(className: String?, defaultValue: Class<*>?): Class<*>? {
        return try {
            getPhysicalClass(className)
        } catch (e: Exception) {
            defaultValue
        }
    }

    @Override
    @Throws(IOException::class)
    fun getPhysicalClass(className: String?, code: ByteArray?): Class<*>? {
        return try {
            touchPhysicalClassLoader(className.contains("_cfc\$cf")).loadClass(className, code)
        } catch (e: UnmodifiableClassException) {
            throw IOException(e)
        }

        // boolean isCFC = className.indexOf("_cfc$")!=-1;//aaaa ResourceUtil.getExtension(ps.getRealpath(),
        // "").equalsIgnoreCase("cfc");
        // return touchClassLoader().loadClass(className,code,isCFC);
    }

    /**
     * remove all Page from Pool using this classloader
     *
     * @param cl
     */
    fun clearPages(cl: ClassLoader?) {
        synchronized(pageSourcePool) { pageSourcePool!!.clearPages(cl) }
    }

    fun clearUnused(config: Config?) {
        synchronized(pageSourcePool) { pageSourcePool!!.clearUnused(config) }
    }

    fun resetPages(cl: ClassLoader?) {
        synchronized(pageSourcePool) { pageSourcePool!!.resetPages(cl) }
    }

    @Override
    fun getPhysical(): Resource? {
        return physical
    }

    @Override
    fun getVirtualLowerCase(): String? {
        return lcVirtual
    }

    @Override
    fun getVirtualLowerCaseWithSlash(): String? {
        return lcVirtualWithSlash
    }

    @Override
    fun getArchive(): Resource? {
        // initArchive();
        return archive
    }

    @Override
    fun hasArchive(): Boolean {
        return hasArchive
    }

    @Override
    fun hasPhysical(): Boolean {
        return physical != null
    }

    @Override
    fun getClassRootDirectory(): Resource? {
        if (classRootDirectory == null) {
            val path: String = if (getPhysical() != null) getPhysical().getAbsolutePath() else getArchive().getAbsolutePath()
            classRootDirectory = config.getClassDirectory().getRealResource(StringUtil.toIdentityVariableName(path))
        }
        return classRootDirectory
    }

    /**
     * clones a mapping and make it readOnly
     *
     * @param config
     * @return cloned mapping
     * @throws IOException
     */
    fun cloneReadOnly(config: Config?): MappingImpl? {
        return MappingImpl(config, virtual, ConfigWebUtil.replacePlaceholder(strPhysical, config), ConfigWebUtil.replacePlaceholder(strArchive, config), inspect, physicalFirst,
                hidden, true, topLevel, appMapping, ignoreVirtual, appListener, listenerMode, listenerType, checkPhysicalFromWebroot, checkArchiveFromWebroot)
    }

    @Override
    fun getInspectTemplate(): Short {
        return if (inspect == Config.INSPECT_UNDEFINED) config.getInspectTemplate() else inspect
    }

    /**
     * inspect template setting (Config.INSPECT_*), if not defined with the mapping,
     * Config.INSPECT_UNDEFINED is returned
     *
     * @return
     */
    fun getInspectTemplateRaw(): Short {
        return inspect
    }

    @Override
    fun getPageSource(realPath: String?): PageSource? {
        var realPath = realPath
        var isOutSide = false
        realPath = realPath.replace('\\', '/')
        if (realPath.indexOf('/') !== 0) {
            if (realPath.startsWith("../")) {
                isOutSide = true
            } else if (realPath.startsWith("./")) {
                realPath = realPath.substring(1)
            } else {
                realPath = "/$realPath"
            }
        }
        return getPageSource(realPath, isOutSide)
    }

    @Override
    fun getPageSource(path: String?, isOut: Boolean): PageSource? {
        synchronized(pageSourcePool) {
            val source: PageSource = pageSourcePool!!.getPageSource(path, true)
            if (source != null) return source
            val newSource = PageSourceImpl(this, path, isOut)
            pageSourcePool!!.setPage(path, newSource)
            return newSource // new PageSource(this,path);
        }
    }

    // to not delete,used for argus monitor!
    fun getPageSourcePool(): PageSourcePool? {
        synchronized(pageSourcePool) { return pageSourcePool }
    }

    @Throws(PageException::class)
    fun getDisplayPathes(arr: Array?): Array? {
        synchronized(pageSourcePool) {
            val keys: Array<String?> = pageSourcePool!!.keys()
            var ps: PageSourceImpl
            for (y in keys.indices) {
                ps = pageSourcePool!!.getPageSource(keys[y], false)
                if (ps != null && ps.isLoad()) arr.append(ps.getDisplayPath())
            }
            return arr
        }
    }

    fun getPageSources(loaded: Boolean): List<PageSource?>? {
        val list: List<PageSource?> = ArrayList()
        synchronized(pageSourcePool) {
            val keys: Array<String?> = pageSourcePool!!.keys()
            var ps: PageSourceImpl
            for (y in keys.indices) {
                ps = pageSourcePool!!.getPageSource(keys[y], false)
                if (ps != null) {
                    if (!loaded || ps.isLoad()) list.add(ps)
                }
            }
        }
        return list
    }

    @Override
    fun check() {
        val cs: ServletContext? = if (config is ConfigWeb) (config as ConfigWeb?).getServletContext() else null

        // Physical
        if (getPhysical() == null && strPhysical != null && strPhysical.length() > 0) {
            physical = ConfigWebUtil.getExistingResource(cs, strPhysical, null, config.getConfigDir(), FileUtil.TYPE_DIR, config, checkPhysicalFromWebroot)
        }
        // Archive
        if (getArchive() == null && strArchive != null && strArchive.length() > 0) {
            archive = ConfigWebUtil.getExistingResource(cs, strArchive, null, config.getConfigDir(), FileUtil.TYPE_FILE, config, checkArchiveFromWebroot)
            loadArchive()
            hasArchive = archive != null
        }
    }

    @Override
    fun getConfig(): Config? {
        return config
    }

    @Override
    fun isHidden(): Boolean {
        return hidden
    }

    @Override
    fun isPhysicalFirst(): Boolean {
        return physicalFirst
    }

    @Override
    fun isReadonly(): Boolean {
        return readonly
    }

    @Override
    fun getStrArchive(): String? {
        return strArchive
    }

    @Override
    fun getStrPhysical(): String? {
        return strPhysical
    }

    @Override
    @Deprecated
    fun isTrusted(): Boolean {
        return getInspectTemplate() == Config.INSPECT_NEVER
    }

    @Override
    fun getVirtual(): String? {
        return virtual
    }

    fun isAppMapping(): Boolean {
        return appMapping
    }

    @Override
    fun isTopLevel(): Boolean {
        return topLevel
    }

    fun getCustomTagPath(name: String?, doCustomTagDeepSearch: Boolean): PageSource? {
        return searchFor(name, name.toLowerCase().trim(), doCustomTagDeepSearch)
    }

    fun ignoreVirtual(): Boolean {
        return ignoreVirtual
    }

    private fun searchFor(filename: String?, lcName: String?, doCustomTagDeepSearch: Boolean): PageSource? {
        var source: PageSource? = getPageSource(filename)
        if (isOK(source)) {
            return source
        }
        customTagPath.remove(lcName)
        if (doCustomTagDeepSearch) {
            source = MappingUtil.searchMappingRecursive(this, filename, false)
            if (isOK(source)) return source
        }
        return null
    }

    @Override
    override fun hashCode(): Int {
        return toString().hashCode()
    }

    @Override
    override fun toString(): String {
        return toString(false)!!
    }

    private fun toString(forCompare: Boolean): String? {
        return StringBuilder().append("StrPhysical:").append(getStrPhysical()).append(";StrArchive:").append(getStrArchive()).append(";Virtual:").append(getVirtual())
                .append(";Archive:").append(getArchive()).append(";Physical:").append(getPhysical()).append(";topLevel:").append(topLevel).append(";inspect:")
                .append(ConfigWebUtil.inspectTemplate(getInspectTemplateRaw(), "")).append(";physicalFirst:").append(physicalFirst).append(";hidden:").append(hidden)
                .append(";readonly:").append(if (forCompare) "" else readonly).append(";").toString()
    }

    @Override
    override fun equals(o: Object?): Boolean {
        if (o === this) return true
        return if (o !is MappingImpl) false else (o as MappingImpl?)!!.toString(true)!!.equals(toString(true))
    }

    fun getApplicationListener(): ApplicationListener? {
        return if (appListener != null) appListener else config.getApplicationListener()
    }

    fun getDotNotationUpperCase(): Boolean {
        return (config as ConfigPro?).getDotNotationUpperCase()
    }

    fun shrink() {
        // MUST implement
    }

    @Override
    fun getListenerMode(): Int {
        return listenerMode
    }

    @Override
    fun getListenerType(): Int {
        return listenerType
    }

    fun flush() {
        synchronized(pageSourcePool) { pageSourcePool!!.clear() }
    }

    fun toSerMapping(): SerMapping? {
        return SerMapping("application", getVirtualLowerCase(), getStrPhysical(), getStrArchive(), isPhysicalFirst(), ignoreVirtual())
    }

    class SerMapping(val type: String?, val virtual: String?, val physical: String?, val archive: String?, val physicalFirst: Boolean, val ignoreVirtual: Boolean) : Serializable {
        fun toMapping(): Mapping? {
            val cwi: ConfigWebPro = ThreadLocalPageContext.getConfig() as ConfigWebPro
            return cwi.getApplicationMapping(type, virtual, physical, archive, physicalFirst, ignoreVirtual)
        }
    }

    companion object {
        private const val serialVersionUID = 6431380676262041196L
        private const val MAX_SIZE_CFC = 3000 // 6783;
        private const val MAX_SIZE_CFM = 2000 // 6783;
        fun isOK(ps: PageSource?): Boolean {
            return if (ps == null) false else ps.executable()
        }

        fun isOK(arr: Array<PageSource?>?): PageSource? {
            if (ArrayUtil.isEmpty(arr)) return null
            for (i in arr.indices) {
                if (isOK(arr!![i])) return arr!![i]
            }
            return null
        }
    }

    /**
     * constructor of the class
     *
     * @param config
     * @param virtual
     * @param strPhysical
     * @param strArchive
     * @param inspect
     * @param physicalFirst
     * @param hidden
     * @param readonly
     * @param topLevel
     * @param appMapping
     * @param ignoreVirtual
     * @param appListener
     */
    init {
        var virtual = virtual
        this.ignoreVirtual = ignoreVirtual
        this.config = config
        this.hidden = hidden
        this.readonly = readonly
        this.strPhysical = if (StringUtil.isEmpty(strPhysical)) null else strPhysical
        this.strArchive = if (StringUtil.isEmpty(strArchive)) null else strArchive
        this.inspect = inspect
        this.topLevel = topLevel
        this.appMapping = appMapping
        this.physicalFirst = physicalFirst
        this.appListener = appListener
        this.listenerMode = listenerMode
        this.listenerType = listenerType
        this.checkPhysicalFromWebroot = checkPhysicalFromWebroot
        this.checkArchiveFromWebroot = checkArchiveFromWebroot

        // virtual
        if (virtual!!.length() === 0) virtual = "/"
        if (!virtual!!.equals("/") && virtual.endsWith("/")) this.virtual = virtual.substring(0, virtual.length() - 1) else this.virtual = virtual
        lcVirtual = this.virtual.toLowerCase()
        lcVirtualWithSlash = if (lcVirtual.endsWith("/")) lcVirtual else lcVirtual + '/'
        val cs: ServletContext? = if (config is ConfigWeb) (config as ConfigWeb?).getServletContext() else null

        // Physical
        physical = ConfigWebUtil.getExistingResource(cs, strPhysical, null, config.getConfigDir(), FileUtil.TYPE_DIR, config, checkPhysicalFromWebroot)
        // Archive
        archive = ConfigWebUtil.getExistingResource(cs, strArchive, null, config.getConfigDir(), FileUtil.TYPE_FILE, config, checkArchiveFromWebroot)
        loadArchive()
        hasArchive = archive != null
        if (archive == null) this.physicalFirst = true else if (physical == null) this.physicalFirst = false else this.physicalFirst = physicalFirst

        // if(!hasArchive && !hasPhysical) throw new IOException("missing physical and archive path, one of
        // them must be defined");
    }
}