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

import java.io.File

/**
 * represent a cfml file on the runtime system
 */
class PageSourceImpl : PageSource {
    // private byte load=LOAD_NONE;
    private val mapping: MappingImpl?
    private var isOutSide = false
    private var relPath: String?
    private var packageName: String? = null
    private var javaName: String? = null
    private var className: String? = null
    private var fileName: String? = null
    private var physcalSource: Resource? = null
    private var archiveSource: Resource? = null
    private var compName: String? = null
    private val pcn: PageAndClassName? = PageAndClassName()
    private var lastAccess: Long = 0
    private val accessCount: RefIntegerSync? = RefIntegerSync()
    private var flush = false

    private constructor() {
        mapping = null
        relPath = null
    }

    private class PageAndClassName {
        var page: Page? = null
        var className: String? = null
        fun reset() {
            page = null
            className = null
        }

        fun set(page: Page?) {
            this.page = page
            if (page != null) className = page.getClass().getName()
        }
    }

    /**
     * constructor of the class
     *
     * @param mapping
     * @param realPath
     */
    internal constructor(mapping: MappingImpl?, realPath: String?) {
        var realPath = realPath
        this.mapping = mapping
        realPath = realPath.replace('\\', '/')
        if (realPath.indexOf("//") !== -1) {
            realPath = StringUtil.replace(realPath, "//", "/", false)
        }
        if (realPath.indexOf('/') !== 0) {
            if (realPath.startsWith("../")) {
                isOutSide = true
            } else if (realPath.startsWith("./")) {
                realPath = realPath.substring(1)
            } else {
                realPath = "/$realPath"
            }
        }
        relPath = realPath
        if (logAccessDirectory != null) dump()
    }

    /**
     * private constructor of the class
     *
     * @param mapping
     * @param realPath
     * @param isOutSide
     */
    internal constructor(mapping: MappingImpl?, realPath: String?, isOutSide: Boolean) {
        // recompileAlways=mapping.getConfig().getCompileType()==Config.RECOMPILE_ALWAYS;
        // recompileAfterStartUp=mapping.getConfig().getCompileType()==Config.RECOMPILE_AFTER_STARTUP ||
        // recompileAlways;
        var realPath = realPath
        this.mapping = mapping
        this.isOutSide = isOutSide
        if (realPath.indexOf("//") !== -1) {
            realPath = StringUtil.replace(realPath, "//", "/", false)
        }
        relPath = realPath
        if (logAccessDirectory != null) dump()
    }

    private fun dump() {
        val res: Resource? = getResource()
        if (res != null && res.isFile()) {
            try {
                val file: File? = createPath()
                IOUtil.write(file, DateTimeImpl().toString() + " " + res.getAbsolutePath() + "\n", "UTF-8", true)
            } catch (ioe: IOException) {
                ioe.printStackTrace()
            }
        }
    }

    /**
     * return page when already loaded, otherwise null
     *
     * @param pc
     * @param config
     * @return
     * @throws PageException
     */
    fun getPage(): Page? {
        return pcn!!.page
    }

    fun getParent(): PageSource? {
        if (relPath!!.equals("/")) return null
        return if (StringUtil.endsWith(relPath, '/')) PageSourceImpl(mapping, GetDirectoryFromPath.invoke(relPath.substring(0, relPath!!.length() - 1))) else PageSourceImpl(mapping, GetDirectoryFromPath.invoke(relPath))
    }

    @Override
    @Synchronized
    @Throws(PageException::class)
    fun loadPage(pc: PageContext?, forceReload: Boolean): Page? {
        if (forceReload) pcn!!.reset()
        var page: Page? = pcn!!.page
        if (mapping!!.isPhysicalFirst()) {
            page = loadPhysical(pc, page)
            if (page == null) page = loadArchive(page)
            if (page != null) return page
        } else {
            page = loadArchive(page)
            if (page == null) page = loadPhysical(pc, page)
            if (page != null) return page
        }
        throw MissingIncludeException(this)
    }

    @Override
    @Synchronized
    @Throws(TemplateException::class)
    fun loadPageThrowTemplateException(pc: PageContext?, forceReload: Boolean, defaultValue: Page?): Page? {
        if (forceReload) pcn!!.reset()
        var page: Page? = pcn!!.page
        if (mapping!!.isPhysicalFirst()) {
            page = loadPhysical(pc, page)
            if (page == null) page = loadArchive(page)
            if (page != null) return page
        } else {
            page = loadArchive(page)
            if (page == null) page = loadPhysical(pc, page)
            if (page != null) return page
        }
        return defaultValue
    }

    @Override
    @Synchronized
    fun loadPage(pc: PageContext?, forceReload: Boolean, defaultValue: Page?): Page? {
        if (forceReload) pcn!!.reset()
        var page: Page? = pcn!!.page
        if (mapping!!.isPhysicalFirst()) {
            page = try {
                loadPhysical(pc, page)
            } catch (e: TemplateException) {
                null
            }
            if (page == null) page = loadArchive(page)
            if (page != null) return page
        } else {
            page = loadArchive(page)
            if (page == null) {
                try {
                    page = loadPhysical(pc, page)
                } catch (e: TemplateException) {
                }
            }
            if (page != null) return page
        }
        return defaultValue
    }

    private fun loadArchive(page: Page?): Page? {
        var page: Page? = page
        if (!mapping!!.hasArchive()) return null
        return if (page != null && page.getLoadType() === LOAD_ARCHIVE) page else try {
            val clazz: Class = mapping!!.getArchiveClass(getClassName())
            page = newInstance(clazz)
            page.setPageSource(this)
            page.setLoadType(LOAD_ARCHIVE)
            pcn!!.set(page)
            page
        } catch (e: Exception) {
            // MUST print.e(e); is there a better way?
            null
        }
    }

    /**
     * throws only an exception when compilation fails
     *
     * @param pc
     * @param page
     * @return
     * @throws PageException
     */
    @Throws(TemplateException::class)
    private fun loadPhysical(pc: PageContext?, page: Page?): Page? {
        var page: Page? = page
        if (!mapping!!.hasPhysical()) return null
        val config: ConfigWeb = pc.getConfig()
        val pci: PageContextImpl? = pc
        if ((mapping!!.getInspectTemplate() === Config.INSPECT_NEVER || pci!!.isTrusted(page)) && isLoad(LOAD_PHYSICAL)) return page
        val srcFile: Resource? = getPhyscalFile()
        val srcLastModified: Long = srcFile.lastModified()
        if (srcLastModified == 0L) return null

        // Page exists
        if (page != null) {
            // if(page!=null && !recompileAlways) {
            if (srcLastModified != page.getSourceLastModified() || page is PagePro && (page as PagePro?)!!.getSourceLength() !== srcFile.length()) {
                // same size, maybe the content has not changed?
                var same = false
                if (page is PagePro && (page as PagePro?)!!.getSourceLength() === srcFile.length()) {
                    try {
                        same = page!!.getHash() === PageSourceCode.toString(this, config.getTemplateCharset()).hashCode()
                    } catch (e: IOException) {
                    }
                }
                if (!same) {
                    LogUtil.log(pc, Log.LEVEL_DEBUG, "compile", "recompile [" + getDisplayPath() + "] because loaded page has changed")
                    pcn!!.set(compile(config, mapping!!.getClassRootDirectory(), page, false, pc.ignoreScopes()).also { page = it })
                    page.setPageSource(this)
                }
            }
            page.setLoadType(LOAD_PHYSICAL)
        } else {
            val classRootDir: Resource = mapping!!.getClassRootDirectory()
            val classFile: Resource = classRootDir.getRealResource(getJavaName().toString() + ".class")
            var isNew = false
            // new class
            if (flush || !classFile.exists()) {
                LogUtil.log(pc, Log.LEVEL_DEBUG, "compile", "compile [" + getDisplayPath() + "] no previous class file or flush")
                pcn!!.set(compile(config, classRootDir, null, false, pc.ignoreScopes()).also { page = it })
                flush = false
                isNew = true
            } else {
                try {
                    val cn = pcn!!.className
                    var done = false
                    if (cn != null) {
                        try {
                            LogUtil.log(pc, Log.LEVEL_DEBUG, "compile", "load class from ClassLoader  [" + getDisplayPath() + "]")
                            pcn.set(newInstance(mapping!!.getPhysicalClass(cn)).also { page = it })
                            done = true
                        } catch (cnfe: ClassNotFoundException) {
                            LogUtil.log(pc, "compile", cnfe)
                        }
                    }
                    if (!done) {
                        LogUtil.log(pc, Log.LEVEL_DEBUG, "compile", "load class from binary  [" + getDisplayPath() + "]")
                        val bytes: ByteArray = IOUtil.toBytes(classFile)
                        if (ClassUtil.isBytecode(bytes)) pcn.set(newInstance(mapping.getPhysicalClass(getClassName(), bytes)).also { page = it })
                    }
                } catch (cfe: ClassFormatError) {
                    LogUtil.log(pc, Log.LEVEL_ERROR, "compile", "size of the class file:" + classFile.length())
                    LogUtil.log(pc, "compile", cfe)
                    pcn!!.reset()
                } catch (t: Throwable) {
                    ExceptionUtil.rethrowIfNecessary(t)
                    LogUtil.log(pc, "compile", t)
                    pcn!!.reset()
                }
                if (page == null) {
                    LogUtil.log(pc, Log.LEVEL_DEBUG, "compile", "compile  [" + getDisplayPath() + "] in case loading of the class fails")
                    pcn!!.set(compile(config, classRootDir, null, false, pc.ignoreScopes()).also { page = it })
                    isNew = true
                }
            }

            // check if version changed or lasMod
            if (!isNew && (srcLastModified != page.getSourceLastModified() || page.getVersion() !== pc.getConfig().getFactory().getEngine().getInfo().getFullVersionInfo())) {
                isNew = true
                LogUtil.log(pc, Log.LEVEL_DEBUG, "compile", "recompile [" + getDisplayPath() + "] because unloaded page has changed")
                pcn!!.set(compile(config, classRootDir, page, false, pc.ignoreScopes()).also { page = it })
            }
            page.setPageSource(this)
            page.setLoadType(LOAD_PHYSICAL)
        }
        pci!!.setPageUsed(page)
        return page
    }

    fun flush() {
        pcn!!.page = null
        flush = true
    }

    private fun isLoad(load: Byte): Boolean {
        val page: Page? = pcn!!.page
        return page != null && load == page.getLoadType()
    }

    @Throws(TemplateException::class)
    private fun compile(config: ConfigWeb?, classRootDir: Resource?, existing: Page?, returnValue: Boolean, ignoreScopes: Boolean): Page? {
        return try {
            _compile(config, classRootDir, existing, returnValue, ignoreScopes, false)
        } catch (re: RuntimeException) {
            val msg: String = StringUtil.emptyIfNull(re.getMessage())
            if (StringUtil.indexOfIgnoreCase(msg, "Method code too large!") !== -1) {
                throw TemplateException("There is too much code inside the template [" + getDisplayPath() + "], " + Constants.NAME
                        + " was not able to break it into pieces, move parts of your code to an include or an external component/function", msg)
            }
            throw re
        } catch (e: ClassFormatError) {
            val msg: String = StringUtil.emptyIfNull(e.getMessage())
            if (StringUtil.indexOfIgnoreCase(msg, "Invalid method Code length") !== -1) {
                throw TemplateException("There is too much code inside the template [" + getDisplayPath() + "], " + Constants.NAME
                        + " was not able to break it into pieces, move parts of your code to an include or an external component/function", msg)
            }
            throw TemplateException("ClassFormatError:" + e.getMessage())
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
            if (t is TemplateException) throw t as TemplateException
            throw PageRuntimeException(Caster.toPageException(t))
        }
    }

    @Throws(IOException::class, SecurityException::class, IllegalArgumentException::class, PageException::class)
    private fun _compile(config: ConfigWeb?, classRootDir: Resource?, existing: Page?, returnValue: Boolean, ignoreScopes: Boolean, split: Boolean): Page? {
        val cwi: ConfigWebPro? = config as ConfigWebPro?
        val dialect = getDialect()
        var now: Long
        if (getPhyscalFile().lastModified() + 10000 > System.currentTimeMillis().also { now = it }) cwi.getCompiler().watch(this, now) // SystemUtil.get
        val result: Result
        result = cwi.getCompiler().compile(cwi, this, cwi.getTLDs(dialect), cwi.getFLDs(dialect), classRootDir, returnValue, ignoreScopes)
        return try {
            val clazz: Class<*> = mapping.getPhysicalClass(getClassName(), result.barr)
            // make sure all children are updated
            if (result.javaFunctions != null && !result.javaFunctions.isEmpty()) {
                for (jf in result.javaFunctions) {
                    mapping.getPhysicalClass(jf.getClassName(), jf.byteCode)
                }
            }
            newInstance(clazz)
        } catch (re: RuntimeException) {
            val msg: String = StringUtil.emptyIfNull(re.getMessage())
            if (!split && StringUtil.indexOfIgnoreCase(msg, "Method code too large!") !== -1) {
                _compile(config, classRootDir, existing, returnValue, ignoreScopes, true)
            } else throw re
        } catch (cfe: ClassFormatError) {
            val msg: String = StringUtil.emptyIfNull(cfe.getMessage())
            if (!split && StringUtil.indexOfIgnoreCase(msg, "Invalid method Code length") !== -1) {
                _compile(config, classRootDir, existing, returnValue, ignoreScopes, true)
            } else throw cfe
        } catch (e: Exception) {
            val pe: PageException = Caster.toPageException(e)
            pe.setExtendedInfo("failed to load template " + getDisplayPath())
            throw pe
        }
    }

    @Throws(SecurityException::class, IllegalArgumentException::class, InstantiationException::class, IllegalAccessException::class, InvocationTargetException::class, NoSuchMethodException::class)
    private fun newInstance(clazz: Class?): Page? {
        val c: Constructor<*> = clazz.getConstructor(arrayOf<Class?>(PageSource::class.java))
        return c.newInstance(arrayOf(this)) as Page
    }

    /**
     * return source path as String
     *
     * @return source path as String
     */
    @Override
    fun getDisplayPath(): String? {
        return if (!mapping!!.hasArchive()) {
            StringUtil.toString(getPhyscalFile(), null)
        } else if (isLoad(LOAD_PHYSICAL)) {
            StringUtil.toString(getPhyscalFile(), null)
        } else if (isLoad(LOAD_ARCHIVE)) {
            StringUtil.toString(getArchiveSourcePath(), null)
        } else {
            val pse = physcalExists()
            val ase = archiveExists()
            if (mapping!!.isPhysicalFirst()) {
                if (pse) return getPhyscalFile().toString() else if (ase) return getArchiveSourcePath()
                return getPhyscalFile().toString()
            }
            if (ase) return getArchiveSourcePath() else if (pse) return getPhyscalFile().toString()
            getArchiveSourcePath()
        }
    }

    fun isComponent(): Boolean {
        val ext: String = ResourceUtil.getExtension(getRealpath(), "")
        return if (getDialect() == CFMLEngine.DIALECT_CFML) Constants.isCFMLComponentExtension(ext) else Constants.isLuceeComponentExtension(ext)
    }

    /**
     * return file object, based on physical path and realpath
     *
     * @return file Object
     */
    private fun getArchiveSourcePath(): String? {
        return "zip://" + mapping!!.getArchive().getAbsolutePath().toString() + "!" + relPath
    }

    /**
     * return file object, based on physical path and realpath
     *
     * @return file Object
     */
    @Override
    fun getPhyscalFile(): Resource? {
        if (physcalSource == null) {
            if (!mapping!!.hasPhysical()) {
                return null
            }
            val tmp: Resource = mapping!!.getPhysical().getRealResource(relPath)
            physcalSource = ResourceUtil.toExactResource(tmp)
            // fix if the case not match
            if (!tmp.getAbsolutePath().equals(physcalSource.getAbsolutePath())) {
                val relpath = extractRealpath(relPath, physcalSource.getAbsolutePath())
                // just a security!
                if (relPath.equalsIgnoreCase(relpath)) {
                    relPath = relpath
                    createClassAndPackage()
                }
            }
        }
        return physcalSource
    }

    fun getArchiveFile(): Resource? {
        if (archiveSource == null) {
            if (!mapping!!.hasArchive()) return null
            val path = "zip://" + mapping!!.getArchive().getAbsolutePath().toString() + "!" + relPath
            archiveSource = ThreadLocalPageContext.getConfig().getResource(path)
        }
        return archiveSource
    }

    @Override
    fun getRealpath(): String? {
        return relPath
    }

    @Override
    fun getRealpathWithVirtual(): String? {
        return if (mapping!!.getVirtual()!!.length() === 1 || mapping!!.ignoreVirtual()) relPath else mapping!!.getVirtual() + relPath
    }

    private fun _getClassName(): String? {
        if (className == null) createClassAndPackage()
        return className
    }

    @Override
    fun getClassName(): String? {
        if (className == null) createClassAndPackage()
        return if (packageName!!.length() === 0) className else packageName.concat(".").concat(className)
    }

    @Override
    fun getFileName(): String? {
        if (fileName == null) createClassAndPackage()
        return fileName
    }

    @Override
    fun getJavaName(): String? {
        if (javaName == null) createClassAndPackage()
        return javaName
    }

    private fun _getPackageName(): String? {
        if (packageName == null) createClassAndPackage()
        return packageName
    }

    @Override
    fun getComponentName(): String? {
        if (compName == null) createComponentName()
        return compName
    }

    private fun createClassAndPackage() {
        val str = relPath
        val packageName = StringBuilder()
        val javaName = StringBuilder()
        val arr: Array<String?> = ListUtil.toStringArrayEL(ListUtil.listToArrayRemoveEmpty(str, '/'))
        var varName: String
        var className: String? = null
        var fileName: String? = null
        for (i in arr.indices) {
            if (i == arr.size - 1) {
                val index: Int = arr[i].lastIndexOf('.')
                varName = if (index != -1) {
                    val ext: String = arr[i].substring(index + 1)
                    StringUtil.toVariableName(arr[i].substring(0, index).toString() + "_" + ext)
                } else StringUtil.toVariableName(arr[i])
                varName = varName + if (getDialect() == CFMLEngine.DIALECT_CFML) Constants.CFML_CLASS_SUFFIX else Constants.LUCEE_CLASS_SUFFIX
                className = varName.toLowerCase()
                fileName = arr[i]
            } else {
                varName = StringUtil.toVariableName(arr[i])
                if (i != 0) {
                    packageName.append('.')
                }
                packageName.append(varName)
            }
            javaName.append('/')
            javaName.append(varName)
        }
        this.packageName = packageName.toString().toLowerCase()
        this.javaName = javaName.toString().toLowerCase()
        this.fileName = fileName
        this.className = className
    }

    private fun createComponentName() {
        val res: Resource? = getPhyscalFile()
        var str: String? = null
        val relPath = relPath
        if (res != null) {
            str = res.getAbsolutePath()
            val begin: Int = str.length() - relPath!!.length()
            if (begin < 0) { // TODO patch, analyze the complete functionality and improve
                str = ListUtil.last(str, "\\/", true)
            } else {
                str = str.substring(begin)
                if (!str.equalsIgnoreCase(relPath)) {
                    str = relPath
                }
            }
        } else str = relPath
        val compName = StringBuilder()
        var arr: Array<String?>

        // virtual part
        if (!mapping!!.ignoreVirtual()) {
            arr = ListUtil.toStringArrayEL(ListUtil.listToArrayRemoveEmpty(mapping!!.getVirtual(), "\\/"))
            for (i in arr.indices) {
                if (compName.length() > 0) compName.append('.')
                compName.append(arr[i])
            }
        }

        // physical part
        arr = ListUtil.toStringArrayEL(ListUtil.listToArrayRemoveEmpty(str, '/'))
        for (i in arr.indices) {
            if (compName.length() > 0) compName.append('.')
            if (i == arr.size - 1) {
                compName.append(ResourceUtil.removeExtension(arr[i], arr[i]))
            } else compName.append(arr[i])
        }
        this.compName = compName.toString()
    }

    @Override
    fun getMapping(): Mapping? {
        return mapping
    }

    @Override
    fun exists(): Boolean {
        return if (mapping!!.isPhysicalFirst()) physcalExists() || archiveExists() else archiveExists() || physcalExists()
    }

    @Override
    fun physcalExists(): Boolean {
        return ResourceUtil.exists(getPhyscalFile())
    }

    private fun archiveExists(): Boolean {
        return if (!mapping!!.hasArchive()) false else try {
            val clazz = getClassName() ?: return getArchiveFile().exists()
            mapping!!.getArchiveClass(clazz)
            true
        } catch (cnfe: ClassNotFoundException) {
            false
        } catch (e: Exception) {
            getArchiveFile().exists()
        }
    }

    /**
     * return the inputstream of the source file
     *
     * @return return the inputstream for the source from physical or archive
     * @throws FileNotFoundException
     */
    @Throws(IOException::class)
    private fun getSourceAsInputStream(): InputStream? {
        return if (!mapping!!.hasArchive()) IOUtil.toBufferedInputStream(getPhyscalFile().getInputStream()) else if (isLoad(LOAD_PHYSICAL)) IOUtil.toBufferedInputStream(getPhyscalFile().getInputStream()) else if (isLoad(LOAD_ARCHIVE)) {
            val name = StringBuffer(_getPackageName().replace('.', '/'))
            if (name.length() > 0) name.append("/")
            name.append(getFileName())
            mapping!!.getArchiveResourceAsStream(name.toString())
        } else {
            null
        }
    }

    @Override
    @Throws(IOException::class)
    fun getSource(): Array<String?>? {
        // if(source!=null) return source;
        val `is`: InputStream = getSourceAsInputStream() ?: return null
        return try {
            IOUtil.toStringArray(IOUtil.getReader(`is`, getMapping().getConfig().getTemplateCharset()))
        } finally {
            IOUtil.closeEL(`is`)
        }
    }

    @Override
    override fun equals(obj: Object?): Boolean {
        if (this === obj) return true
        if (obj is PageSourceImpl) return _getClassName()!!.equals((obj as PageSourceImpl?)!!._getClassName())
        return if (obj is PageSource) _getClassName()!!.equals(ClassUtil.extractName((obj as PageSource?).getClassName())) else false
    }

    /**
     * is given object equal to this
     *
     * @param other
     * @return is same
     */
    override fun equals(ps: PageSource?): Boolean {
        if (this === ps) return true
        return if (ps is PageSourceImpl) _getClassName()!!.equals((ps as PageSourceImpl?)!!._getClassName()) else _getClassName()!!.equals(ClassUtil.extractName(ps.getClassName()))
    }

    @Override
    fun getRealPage(realPath: String?): PageSource? {
        var realPath = realPath
        if (realPath!!.equals(".") || realPath.equals("..")) realPath += '/' else realPath = realPath.replace('\\', '/')
        val _isOutSide: RefBoolean = RefBooleanImpl(isOutSide)
        if (realPath.indexOf('/') === 0) {
            _isOutSide.setValue(false)
        } else if (realPath.startsWith("./")) {
            realPath = mergeRealPathes(mapping, relPath, realPath.substring(2), _isOutSide)
        } else {
            realPath = mergeRealPathes(mapping, relPath, realPath, _isOutSide)
        }
        return mapping!!.getPageSource(realPath, _isOutSide.toBooleanValue())
    }

    @Override
    fun setLastAccessTime(lastAccess: Long) {
        this.lastAccess = lastAccess
    }

    @Override
    fun getLastAccessTime(): Long {
        return lastAccess
    }

    @Override
    fun setLastAccessTime() {
        accessCount.plus(1)
        lastAccess = System.currentTimeMillis()
    }

    @Override
    fun getAccessCount(): Int {
        return accessCount.toInt()
    }

    @Override
    fun getResource(): Resource? {
        val p: Resource? = getPhyscalFile()
        val a: Resource? = getArchiveFile()
        if (mapping!!.isPhysicalFirst()) {
            if (a == null) return p
            if (p == null) return a
            if (p.exists()) return p
            return if (a.exists()) a else p
        }
        if (p == null) return a
        if (a == null) return p
        if (a.exists()) return a
        return if (p.exists()) p else a

        // return getArchiveFile();
    }

    @Override
    @Throws(ExpressionException::class)
    fun getResourceTranslated(pc: PageContext?): Resource? {
        var res: Resource? = null
        if (!isLoad(LOAD_ARCHIVE)) res = getPhyscalFile()

        // there is no physical resource
        if (res == null) {
            var path = getDisplayPath()
            if (path != null) {
                if (path.startsWith("ra://")) path = "zip://" + path.substring(5)
                res = ResourceUtil.toResourceNotExisting(pc, path, false, false)
            }
        }
        return res
    }

    fun clear() {
        pcn!!.page = null
    }

    /**
     * clear page, but only when page use the same classloader as provided
     *
     * @param cl
     */
    fun clear(cl: ClassLoader?) {
        val page: Page? = pcn!!.page
        if (page != null && page.getClass().getClassLoader().equals(cl)) {
            pcn.page = null
        }
    }

    fun isLoad(): Boolean {
        return pcn!!.page != null //// load!=LOAD_NONE;
    }

    @Override
    override fun toString(): String {
        return getDisplayPath()!!
    }

    @Override
    fun getDialect(): Int {
        var c: Config = getMapping().getConfig()
        if (!(c as ConfigPro).allowLuceeDialect()) return CFMLEngine.DIALECT_CFML
        // MUST improve performance on this
        var cw: ConfigWeb? = null
        val ext: String = ResourceUtil.getExtension(relPath, Constants.getCFMLComponentExtension())
        if (c is ConfigWeb) cw = c as ConfigWeb else {
            c = ThreadLocalPageContext.getConfig()
            if (c is ConfigWeb) cw = c as ConfigWeb
        }
        return if (cw != null) {
            (cw.getFactory() as CFMLFactoryImpl)!!.toDialect(ext, CFMLEngine.DIALECT_CFML)
        } else ConfigWebUtil.toDialect(ext, CFMLEngine.DIALECT_CFML)
    }

    @Override
    fun executable(): Boolean {
        return getMapping().getInspectTemplate() === Config.INSPECT_NEVER && isLoad() || exists()
    }

    fun resetLoaded() {
        val p: Page? = pcn!!.page
        if (p != null) p.setLoadType(0.toByte())
    }

    companion object {
        private const val serialVersionUID = -7661676586215092539L

        // public static final byte LOAD_NONE=1;
        const val LOAD_ARCHIVE: Byte = 2
        const val LOAD_PHYSICAL: Byte = 3
        private const val MAX = (1024 * 1024 * 100).toLong()
        var logAccessDirectory: File? = null
        @Throws(IOException::class)
        private fun createPath(): File? {
            var log: File? = File(logAccessDirectory, "access.log")
            if (log.isFile()) {
                if (log.length() > MAX) {
                    var backup: File?
                    var count = 0
                    do {
                        backup = File(logAccessDirectory, "access-" + ++count + ".log")
                    } while (backup.isFile())
                    log.renameTo(backup)
                    File(logAccessDirectory, "access.log").also { log = it }.createNewFile()
                }
            } else log.createNewFile()
            return log
        }

        private fun extractRealpath(relapth: String?, newPath: String?): String? {
            val len1 = relapth?.length() ?: 0
            val len2: Int = newPath!!.length()
            var pos: Int
            var c1: Char
            var c2: Char
            val sb = StringBuilder()
            var done = false
            for (i in 0 until len1) {
                c1 = relapth.charAt(len1 - 1 - i)
                pos = len2 - 1 - i
                c2 = if (pos < 0) c1 else newPath.charAt(pos)
                if (!done && Character.toLowerCase(c1) === Character.toLowerCase(c2)) sb.insert(0, c2) else {
                    done = true
                    sb.insert(0, c1)
                }
            }
            return sb.toString()
        }

        /**
         * merge to realpath to one
         *
         * @param mapping
         * @param parentRealPath
         * @param newRealPath
         * @param isOutSide
         * @return merged realpath
         */
        private fun mergeRealPathes(mapping: Mapping?, parentRealPath: String?, newRealPath: String?, isOutSide: RefBoolean?): String? {
            var parentRealPath = parentRealPath
            var newRealPath = newRealPath
            parentRealPath = pathRemoveLast(parentRealPath, isOutSide)
            while (newRealPath.startsWith("../")) {
                parentRealPath = pathRemoveLast(parentRealPath, isOutSide)
                newRealPath = newRealPath.substring(3)
            }

            // check if come back
            var path: String? = parentRealPath.concat("/").concat(newRealPath)
            if (path.startsWith("../")) {
                var count = 0
                do {
                    count++
                    path = path.substring(3)
                } while (path.startsWith("../"))
                var strRoot: String = mapping.getPhysical().getAbsolutePath().replace('\\', '/')
                if (!StringUtil.endsWith(strRoot, '/')) {
                    strRoot += '/'
                }
                val rootLen: Int = strRoot.length()
                val arr: Array<String?> = ListUtil.toStringArray(ListUtil.listToArray(path, '/'), "") // path.split("/");
                var tmpLen: Int
                for (i in count downTo 1) {
                    if (arr.size > i) {
                        val tmp: String = '/' + list(arr, 0, i)
                        tmpLen = rootLen - tmp.length()
                        if (strRoot.lastIndexOf(tmp) === tmpLen && tmpLen >= 0) {
                            val rtn = StringBuffer()
                            while (i < count - i) {
                                count--
                                rtn.append("../")
                            }
                            isOutSide.setValue(rtn.length() !== 0)
                            return (if (rtn.length() === 0) "/" else rtn.toString()) + list(arr, i, arr.size)
                        }
                    }
                }
            }
            return parentRealPath.concat("/").concat(newRealPath)
        }

        /**
         * convert a String array to a string list, but only part of it
         *
         * @param arr String Array
         * @param from start from here
         * @param len how many element
         * @return String list
         */
        private fun list(arr: Array<String?>?, from: Int, len: Int): String? {
            val sb = StringBuffer()
            for (i in from until len) {
                sb.append(arr!![i])
                if (i + 1 != arr.size) sb.append('/')
            }
            return sb.toString()
        }

        /**
         * remove the last elemtn of a path
         *
         * @param path path to remove last element from it
         * @param isOutSide
         * @return path with removed element
         */
        private fun pathRemoveLast(path: String?, isOutSide: RefBoolean?): String? {
            if (path!!.length() === 0) {
                isOutSide.setValue(true)
                return ".."
            } else if (path.endsWith("..")) {
                isOutSide.setValue(true)
                return path.concat("/..") // path+"/..";
            }
            return path.substring(0, path.lastIndexOf('/'))
        }

        fun best(arr: Array<PageSource?>?): PageSource? {
            if (ArrayUtil.isEmpty(arr)) return null
            if (arr!!.size == 1) return arr[0]
            for (i in arr.indices) {
                if (pageExist(arr[i])) return arr[i]
            }
            return arr[0]
        }

        fun pageExist(ps: PageSource?): Boolean {
            return ps.getMapping().isTrusted() && (ps as PageSourceImpl?)!!.isLoad() || ps.exists()
        }

        @Throws(PageException::class)
        fun loadPage(pc: PageContext?, arr: Array<PageSource?>?, defaultValue: Page?): Page? {
            if (ArrayUtil.isEmpty(arr)) return null
            var p: Page
            for (i in arr.indices) {
                p = arr!![i].loadPageThrowTemplateException(pc, false, null as Page?)
                if (p != null) return p
            }
            return defaultValue
        }

        @Throws(PageException::class)
        fun loadPage(pc: PageContext?, arr: Array<PageSource?>?): Page? {
            if (ArrayUtil.isEmpty(arr)) return null
            var p: Page
            for (i in arr.indices) {
                p = arr!![i].loadPageThrowTemplateException(pc, false, null as Page?)
                if (p != null) return p
            }
            throw MissingIncludeException(arr!![0])
        }

        /**
         * return if the PageSource represent a template (no component,no interface)
         *
         * @param pc
         * @param ps
         * @return
         * @throws PageException
         */
        fun isTemplate(pc: PageContext?, ps: PageSource?, defaultValue: Boolean): Boolean {
            return try {
                ps.loadPage(pc, false) !is CIPage
            } catch (e: PageException) {
                LogUtil.log(pc, PageSourceImpl::class.java.getName(), e)
                defaultValue
            }
        }
    }
}