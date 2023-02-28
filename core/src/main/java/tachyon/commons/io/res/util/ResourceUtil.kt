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
package tachyon.commons.io.res.util

import java.io.File

object ResourceUtil {
    const val MIMETYPE_CHECK_EXTENSION = 1
    const val MIMETYPE_CHECK_HEADER = 2

    /**
     * Field `FILE_SEPERATOR`
     */
    val FILE_SEPERATOR: Char = File.separatorChar

    /**
     * Field `FILE_ANTI_SEPERATOR`
     */
    val FILE_ANTI_SEPERATOR = if (FILE_SEPERATOR == '/') '\\' else '/'

    /**
     * Field `TYPE_DIR`
     */
    const val TYPE_DIR: Short = 0

    /**
     * Field `TYPE_FILE`
     */
    const val TYPE_FILE: Short = 1

    /**
     * Field `LEVEL_FILE`
     */
    const val LEVEL_FILE: Short = 0

    /**
     * Field `LEVEL_PARENT_FILE`
     */
    const val LEVEL_PARENT_FILE: Short = 1

    /**
     * Field `LEVEL_GRAND_PARENT_FILE`
     */
    const val LEVEL_GRAND_PARENT_FILE: Short = 2
    val EXT_MT: HashMap<String, String> = HashMap<String, String>()
    // private static Magic mimeTypeParser;
    /**
     * cast a String (argument destination) to a File Object, if destination is not an absolute, file
     * object will be relative to current position (get from PageContext) file must exist otherwise
     * throw exception
     *
     * @param pc Page Context to the current position in filesystem
     * @param path relative or absolute path for file object
     * @return file object from destination
     * @throws ExpressionException
     */
    @Throws(ExpressionException::class)
    fun toResourceExisting(pc: PageContext, path: String?): Resource {
        return toResourceExisting(pc, path, pc.getConfig().allowRealPath())
    }

    fun toResourceExisting(pc: PageContext?, path: String?, allowRealpath: Boolean, defaultValue: Resource): Resource {
        return try {
            toResourceExisting(pc, path, allowRealpath)
        } catch (e: Throwable) {
            ExceptionUtil.rethrowIfNecessary(e)
            defaultValue
        }
    }

    @Throws(ExpressionException::class)
    fun toResourceExisting(pc: PageContext, path: String, allowRealpath: Boolean): Resource? {
        var path = path
        path = path.replace('\\', '/')
        var res: Resource? = pc.getConfig().getResource(path)
        if (res.exists()) return res else if (!allowRealpath) throw ExpressionException("file or directory [$path] does not exist")
        if (res.isAbsolute() && res.exists()) {
            return res
        }
        if (StringUtil.startsWith(path, '/')) {
            val pci: PageContextImpl = pc as PageContextImpl
            val cw: ConfigWeb = pc.getConfig()
            val sources: Array<PageSource> = cw.getPageSources(pci, ExpandPath.mergeMappings(pc.getApplicationContext().getMappings(), pc.getApplicationContext().getComponentMappings()),
                    path, false, pci.useSpecialMappings(), true, false)
            if (!ArrayUtil.isEmpty(sources)) {
                for (i in sources.indices) {
                    if (sources[i].exists()) return sources[i].getResource()
                }
            }
        }
        res = getRealResource(pc, path, res)
        if (res.exists()) return res
        throw ExpressionException("file or directory [$path] does not exist")
    }

    @Throws(ExpressionException::class)
    fun toResourceExisting(config: Config?, path: String): Resource {
        var config: Config? = config
        var path = path
        path = path.replace('\\', '/')
        config = ThreadLocalPageContext.getConfig(config)
        val res: Resource
        res = if (config == null) ResourcesImpl.getFileResourceProvider().getResource(path) else config.getResource(path)
        if (res.exists()) return res
        throw ExpressionException("file or directory [$path] does not exist")
    }

    fun toResourceExisting(config: Config?, path: String, defaultValue: Resource): Resource {
        var config: Config? = config
        var path = path
        path = path.replace('\\', '/')
        config = ThreadLocalPageContext.getConfig(config)
        val res: Resource
        res = if (config == null) ResourcesImpl.getFileResourceProvider().getResource(path) else config.getResource(path)
        return if (res.exists()) res else defaultValue
    }

    fun toResourceNotExisting(config: Config, path: String): Resource {
        var path = path
        val res: Resource
        path = path.replace('\\', '/')
        res = config.getResource(path)
        return res
    }

    /**
     * cast a String (argument destination) to a File Object, if destination is not an absolute, file
     * object will be relative to current position (get from PageContext) at least parent must exist
     *
     * @param pc Page Context to the current position in filesystem
     * @param destination relative or absolute path for file object
     * @return file object from destination
     * @throws ExpressionException
     */
    @Throws(ExpressionException::class)
    fun toResourceExistingParent(pc: PageContext, destination: String): Resource? {
        return toResourceExistingParent(pc, destination, pc.getConfig().allowRealPath())
    }

    @Throws(ExpressionException::class)
    fun toResourceExistingParent(pc: PageContext, destination: String, allowRealpath: Boolean): Resource? {
        var destination = destination
        destination = destination.replace('\\', '/')
        var res: Resource? = pc.getConfig().getResource(destination)

        // not allow realpath
        if (!allowRealpath) {
            if (res.exists() || parentExists(res)) return res
            throw ExpressionException("parent directory [" + res.getParent().toString() + "]  for file [" + destination + "] doesn't exist")
        }

        // allow realpath
        if (res.isAbsolute() && (res.exists() || parentExists(res))) {
            return res
        }
        if (StringUtil.startsWith(destination, '/')) {
            val pci: PageContextImpl = pc as PageContextImpl
            val cw: ConfigWeb = pc.getConfig()
            val sources: Array<PageSource> = cw.getPageSources(pci, ExpandPath.mergeMappings(pc.getApplicationContext().getMappings(), pc.getApplicationContext().getComponentMappings()),
                    destination, false, pci.useSpecialMappings(), true)
            if (!ArrayUtil.isEmpty(sources)) {
                for (i in sources.indices) {
                    if (sources[i].exists() || parentExists(sources[i])) {
                        res = sources[i].getResource()
                        if (res != null) return res
                    }
                }
            }
        }
        res = getRealResource(pc, destination, res)
        if (res != null && (res.exists() || parentExists(res))) return res
        throw ExpressionException("parent directory [" + res.getParent().toString() + "]  for file [" + destination + "] doesn't exist")
    }

    /**
     * cast a String (argument destination) to a File Object, if destination is not an absolute, file
     * object will be relative to current position (get from PageContext) existing file is preferred but
     * dont must exist
     *
     * @param pc Page Context to the current position in filesystem
     * @param destination relative or absolute path for file object
     * @return file object from destination
     */
    fun toResourceNotExisting(pc: PageContext, destination: String): Resource? {
        return toResourceNotExisting(pc, destination, pc.getConfig().allowRealPath(), false)
    }

    fun toResourceNotExisting(pc: PageContext, destination: String, allowRealpath: Boolean, checkComponentMappings: Boolean): Resource? {
        var destination = destination
        destination = destination.replace('\\', '/')
        var res: Resource = pc.getConfig().getResource(destination)
        if (!allowRealpath || res.exists()) {
            return res
        }
        var isUNC: Boolean
        if (!isUNCPath(destination).also { isUNC = it } && StringUtil.startsWith(destination, '/')) {
            val pci: PageContextImpl = pc as PageContextImpl
            val cw: ConfigWeb = pc.getConfig()
            val sources: Array<PageSource> = cw.getPageSources(pci, ExpandPath.mergeMappings(pc.getApplicationContext().getMappings(), pc.getApplicationContext().getComponentMappings()),
                    destination, false, pci.useSpecialMappings(), SystemUtil.isWindows(), checkComponentMappings)
            if (!ArrayUtil.isEmpty(sources)) {
                for (i in sources.indices) {
                    res = sources[i].getResource()
                    if (res != null) return res
                }
            }
            // Resource res2 = pc.getPhysical(destination,SystemUtil.isWindows());
            // if(res2!=null) return res2;
        }
        if (isUNC) {
            res = pc.getConfig().getResource(destination.replace('/', '\\'))
        } else if (!destination.startsWith("..")) res = pc.getConfig().getResource(destination)
        return if (res != null && res.isAbsolute()) res else getRealResource(pc, destination, res)
    }

    private fun getRealResource(pc: PageContext, destination: String, defaultValue: Resource?): Resource? {
        var ps: PageSource = pc.getCurrentPageSource()
        if (ps != null) {
            ps = ps.getRealPage(destination)
            if (ps != null) {
                val res: Resource = ps.getResource()
                if (res != null) return getCanonicalResourceEL(res)
            }
        }
        return defaultValue
    }

    fun isUNCPath(path: String): Boolean {
        return SystemUtil.isWindows() && (path.startsWith("//") || path.startsWith("\\\\"))
    }

    fun isWindowsPath(path: String?): Boolean {
        return SystemUtil.isWindows() && path!!.length() > 1 && path.charAt(1) === ':'
    }

    /**
     * translate the path of the file to an existing file path by changing case of letters Works only on
     * Linux, because
     *
     * Example Unix: we have an existing file with path "/usr/virtual/myFile.txt" now you call this
     * method with path "/Usr/Virtual/myfile.txt" the result of the method will be
     * "/usr/virtual/myFile.txt"
     *
     * if there are more file with rhe same name but different cases Example: /usr/virtual/myFile.txt
     * /usr/virtual/myfile.txt /Usr/Virtual/myFile.txt the nearest case wil returned
     *
     * @param res
     * @return file
     */
    fun toExactResource(res: Resource): Resource {
        var res: Resource = res
        res = getCanonicalResourceEL(res)
        return if (res.getResourceProvider().isCaseSensitive()) {
            if (res.exists()) res else _check(res)
        } else res
    }

    private fun _check(file: Resource): Resource {
        // todo cascade durch while ersetzten
        var file: Resource = file
        var parent: Resource = file.getParentResource() ?: return file
        if (!parent.exists()) {
            val op: Resource = parent
            parent = _check(parent)
            if (op === parent) return file
            if (parent.getRealResource(file.getName()).also { file = it }.exists()) return file
        }
        val files: Array<String> = parent.list() ?: return file
        val name: String = file.getName()
        for (i in files.indices) {
            if (name.equalsIgnoreCase(files[i])) return parent.getRealResource(files[i])
        }
        return file
    }

    /**
     * create a file if possible, return file if ok, otherwise return null
     *
     * @param res file to touch
     * @param level touch also parent and grand parent
     * @param type is file or directory
     * @return file if exists, otherwise null
     */
    fun createResource(res: Resource, level: Short, type: Short): Resource? {
        val asDir = type == TYPE_DIR
        // File
        if (level >= LEVEL_FILE && res.exists() && (res.isDirectory() && asDir || res.isFile() && !asDir)) {
            return getCanonicalResourceEL(res)
        }

        // Parent
        val parent: Resource = res.getParentResource()
        if (level >= LEVEL_PARENT_FILE && parent != null && parent.exists() && canRW(parent) && !ConfigWebUtil.hasPlaceholder(res.getAbsolutePath())) {
            if (asDir) {
                if (res.mkdirs()) return getCanonicalResourceEL(res)
            } else {
                if (createNewResourceEL(res)) return getCanonicalResourceEL(res)
            }
            return getCanonicalResourceEL(res)
        }

        // Grand Parent
        if (level >= LEVEL_GRAND_PARENT_FILE && parent != null) {
            val gparent: Resource = parent.getParentResource()
            if (gparent != null && gparent.exists() && canRW(gparent) && !ConfigWebUtil.hasPlaceholder(res.getAbsolutePath())) {
                if (asDir) {
                    if (res.mkdirs()) return getCanonicalResourceEL(res)
                } else {
                    if (parent.mkdirs() && createNewResourceEL(res)) return getCanonicalResourceEL(res)
                }
            }
        }
        return null
    }

    @Throws(IOException::class)
    fun setAttribute(res: Resource, attributes: String) {
        /*
		 * if(res instanceof File && SystemUtil.isWindows()) { if(attributes.length()>0) {
		 * attributes=ResourceUtil.translateAttribute(attributes);
		 * Runtime.getRuntime().exec("attrib "+attributes+" " + res.getAbsolutePath()); } } else {
		 */
        val flags = strAttrToBooleanFlags(attributes)
        if (flags[READ_ONLY] == YES) res.setWritable(false) else if (flags[READ_ONLY] == NO) res.setWritable(true)
        if (flags[HIDDEN] == YES) res.setAttribute(Resource.ATTRIBUTE_HIDDEN, true) // setHidden(true);
        else if (flags[HIDDEN] == NO) res.setAttribute(Resource.ATTRIBUTE_HIDDEN, false) // res.setHidden(false);
        if (flags[ARCHIVE] == YES) res.setAttribute(Resource.ATTRIBUTE_ARCHIVE, true) // res.setArchive(true);
        else if (flags[ARCHIVE] == NO) res.setAttribute(Resource.ATTRIBUTE_ARCHIVE, false) // res.setArchive(false);
        if (flags[SYSTEM] == YES) res.setAttribute(Resource.ATTRIBUTE_SYSTEM, true) // res.setSystem(true);
        else if (flags[SYSTEM] == NO) res.setAttribute(Resource.ATTRIBUTE_SYSTEM, false) // res.setSystem(false);

        // }
    }

    // private static final int NORMAL=0;
    private const val READ_ONLY = 0
    private const val HIDDEN = 1
    private const val ARCHIVE = 2
    private const val SYSTEM = 3

    // private static final int IGNORE=0;
    private const val NO = 1
    private const val YES = 2
    @Throws(IOException::class)
    private fun strAttrToBooleanFlags(attributes: String): ShortArray {
        val arr: Array<String?>
        arr = try {
            ListUtil.toStringArray(ListUtil.listToArrayRemoveEmpty(attributes.toLowerCase(), ','))
        } catch (e: PageException) {
            arrayOfNulls(0)
        }
        var hasNormal = false
        var hasReadOnly = false
        var hasHidden = false
        var hasArchive = false
        var hasSystem = false
        for (i in arr.indices) {
            val str: String = arr[i].trim().toLowerCase()
            if (str.equals("readonly") || str.equals("read-only") || str.equals("+r")) hasReadOnly = true else if (str.equals("normal") || str.equals("temporary")) hasNormal = true else if (str.equals("hidden") || str.equals("+h")) hasHidden = true else if (str.equals("system") || str.equals("+s")) hasSystem = true else if (str.equals("archive") || str.equals("+a")) hasArchive = true else throw IOException("invalid attribute definition [$str]")
        }
        val flags = ShortArray(4)
        if (hasReadOnly) flags[READ_ONLY] = YES.toShort() else if (hasNormal) flags[READ_ONLY] = NO.toShort()
        if (hasHidden) flags[HIDDEN] = YES.toShort() else if (hasNormal) flags[HIDDEN] = NO.toShort()
        if (hasSystem) flags[SYSTEM] = YES.toShort() else if (hasNormal) flags[SYSTEM] = NO.toShort()
        if (hasArchive) flags[ARCHIVE] = YES.toShort() else if (hasNormal) flags[ARCHIVE] = NO.toShort()
        return flags
    }

    /**
     * sets attributes of a file on Windows system
     *
     * @param res
     * @param attributes
     * @throws PageException
     * @throws IOException
     */
    @Throws(IOException::class)
    fun translateAttribute(attributes: String): String {
        val flags = strAttrToBooleanFlags(attributes)
        val sb = StringBuilder()
        if (flags[READ_ONLY] == YES) sb.append(" +R") else if (flags[READ_ONLY] == NO) sb.append(" -R")
        if (flags[HIDDEN] == YES) sb.append(" +H") else if (flags[HIDDEN] == NO) sb.append(" -H")
        if (flags[SYSTEM] == YES) sb.append(" +S") else if (flags[SYSTEM] == NO) sb.append(" -S")
        if (flags[ARCHIVE] == YES) sb.append(" +A") else if (flags[ARCHIVE] == NO) sb.append(" -A")
        return sb.toString()
    }

    /*
	 * * translate a path in a proper form example susi\petere -> /susi/peter
	 * 
	 * @param path
	 * 
	 * @return path / public static String translatePath(String path) { / *path=prettifyPath(path);
	 * if(path.indexOf('/')!=0)path='/'+path; int index=path.lastIndexOf('/'); // remove slash at the
	 * end if(index==path.length()-1) path=path.substring(0,path.length()-1); return path;* / return
	 * translatePath(path, true, false); }
	 */
    /*
	 * * translate a path in a proper form example susi\petere -> susi/peter/
	 * 
	 * @param path
	 * 
	 * @return path / public static String translatePath2x(String path) { / *path=prettifyPath(path);
	 * if(path.indexOf('/')==0)path=path.substring(1); int index=path.lastIndexOf('/'); // remove slash
	 * at the end if(index!=path.length()-1) path=path+'/';* / return translatePath(path, false, true);
	 * }
	 */
    fun translatePath(path: String?, slashAdBegin: Boolean, slashAddEnd: Boolean): String? {
        var path = path
        path = prettifyPath(path)

        // begin
        if (slashAdBegin && !isWindowsPath(path)) {
            if (path.indexOf('/') !== 0) path = '/' + path
        } else {
            if (path.indexOf('/') === 0) path = path.substring(1)
        }

        // end
        val index: Int = path.lastIndexOf('/')
        if (slashAddEnd) {
            if (index != path!!.length() - 1) path = "$path/"
        } else {
            if (index == path!!.length() - 1 && index > -1) path = path.substring(0, path.length() - 1)
        }
        return path
    }

    /**
     * translate a path in a proper form and cut name away example susi\petere -> /susi/ and peter
     *
     * @param path
     * @return
     */
    fun translatePathName(path: String?): Array<String?> {
        var path = path
        path = prettifyPath(path)
        if (path.indexOf('/') !== 0) path = '/' + path
        var index: Int = path.lastIndexOf('/')
        // remove slash at the end
        if (index == path!!.length() - 1) path = path.substring(0, path.length() - 1)
        index = path.lastIndexOf('/')
        val name: String?
        if (index == -1) {
            name = path
            path = "/"
        } else {
            name = path.substring(index + 1)
            path = path.substring(0, index + 1)
        }
        return arrayOf(path, name)
    }

    fun prettifyPath(path: String?): String? {
        var path: String? = path ?: return null
        path = path.replace('\\', '/')
        return StringUtil.replace(path, "//", "/", false)
        // TODO /aaa/../bbb/
    }

    fun removeScheme(scheme: String, path: String): String {
        var path = path
        if (path.indexOf("://") === scheme.length() && StringUtil.startsWithIgnoreCase(path, scheme)) path = path.substring(3 + scheme.length())
        return path
    }

    /**
     * merge to path parts to one
     *
     * @param parent
     * @param child
     * @return
     */
    fun merge(parent: String?, child: String?): String? {
        var parent = parent
        var child = child
        if (child!!.length() <= 2) {
            if (child.length() === 0) return parent
            if (child.equals(".")) return parent
            if (child.equals("..")) child = "../"
        }
        parent = translatePath(parent, true, false)
        child = prettifyPath(child) // child.replace('\\', '/');
        if (child.startsWith("./")) child = child.substring(2)
        if (StringUtil.startsWith(child, '/')) return parent.concat(child)
        if (!StringUtil.startsWith(child, '.')) return parent.concat("/").concat(child)
        while (child.startsWith("../")) {
            parent = pathRemoveLast(parent)
            child = child.substring(3)
        }
        return if (StringUtil.startsWith(child, '/')) parent.concat(child) else parent.concat("/").concat(child)
    }

    private fun pathRemoveLast(path: String?): String {
        if (path!!.length() === 0) return ".." else if (path.endsWith("..")) {
            return path.concat("/..")
        }
        return path.substring(0, path.lastIndexOf('/'))
    }

    /**
     * Returns the canonical form of this abstract pathname.
     *
     * @param res file to get canonical form from it
     *
     * @return The canonical pathname string denoting the same file or directory as this abstract
     * pathname
     *
     * @throws SecurityException If a required system property value cannot be accessed.
     */
    fun getCanonicalPathEL(res: Resource?): String {
        return try {
            res.getCanonicalPath()
        } catch (e: IOException) {
            res.toString()
        }
    }

    /**
     * Returns the canonical form of this abstract pathname.
     *
     * @param res file to get canonical form from it
     *
     * @return The canonical pathname string denoting the same file or directory as this abstract
     * pathname
     *
     * @throws SecurityException If a required system property value cannot be accessed.
     */
    fun getCanonicalResourceEL(res: Resource): Resource {
        return if (res == null) res else try {
            res.getCanonicalResource()
        } catch (e: IOException) {
            res.getAbsoluteResource()
        }
    }

    /**
     * creates a new File
     *
     * @param res
     * @return was successfull
     */
    fun createNewResourceEL(res: Resource): Boolean {
        return try {
            res.createFile(false)
            true
        } catch (e: IOException) {
            false
        }
    }

    fun exists(res: Resource?): Boolean {
        return res != null && res.exists()
    }

    /**
     * check if file is read and writable
     *
     * @param res
     * @return is or not
     */
    fun canRW(res: Resource): Boolean {
        return res.isReadable() && res.isWriteable()
    }

    /**
     * similar to linux bash function touch, create file if not exist otherwise change last modified
     * date
     *
     * @param res
     * @throws IOException
     */
    @Throws(IOException::class)
    fun touch(res: Resource) {
        if (res.exists()) {
            res.setLastModified(System.currentTimeMillis())
        } else {
            res.createFile(true)
        }
    }

    @Throws(IOException::class)
    fun touch(res: File) {
        if (res.exists()) {
            res.setLastModified(System.currentTimeMillis())
        } else {
            res.mkdirs()
            res.createNewFile()
        }
    }

    @Throws(IOException::class)
    fun clear(res: Resource) {
        if (res.exists()) {
            IOUtil.write(res, ByteArray(0))
        } else {
            res.createFile(true)
        }
    }

    /**
     * return the mime type of a file, dont check extension
     *
     * @param res
     * @param defaultValue
     * @return mime type of the file
     */
    fun getMimeType(res: Resource?, defaultValue: String?): String {
        return IOUtil.getMimeType(res, defaultValue)
    }

    fun getMimeType(res: Resource?, fileName: String?, defaultValue: String?): String {
        return IOUtil.getMimeType(res, fileName, defaultValue)
    }

    /**
     * check if file is a child of given directory
     *
     * @param file file to search
     * @param dir directory to search
     * @return is inside or not
     */
    fun isChildOf(file: Resource?, dir: Resource?): Boolean {
        var file: Resource? = file
        while (file != null) {
            if (file.equals(dir)) return true
            file = file.getParentResource()
        }
        return false
    }

    /**
     * return diffrents of one file to another if first is child of second otherwise return null
     *
     * @param file file to search
     * @param dir directory to search
     */
    fun getPathToChild(file: Resource?, dir: Resource?): String? {
        var file: Resource? = file
        if (dir == null || !file.getResourceProvider().getScheme().equals(dir.getResourceProvider().getScheme())) return null
        val isFile: Boolean = file.isFile()
        var str = "/"
        while (file != null) {
            if (file.equals(dir)) {
                return if (isFile) str.substring(0, str.length() - 1) else str
            }
            str = "/" + file.getName() + str
            file = file.getParentResource()
        }
        return null
    }

    /**
     * get the Extension of a file
     *
     * @param res
     * @return extension of file
     */
    fun getExtension(res: Resource, defaultValue: String?): String {
        return getExtension(res.getName(), defaultValue)
    }

    /**
     * get the Extension of a file
     *
     * @param strFile
     * @return extension of file
     */
    fun getExtension(strFile: String, defaultValue: String): String {
        val pos: Int = strFile.lastIndexOf('.')
        return if (pos == -1) defaultValue else strFile.substring(pos + 1)
    }

    fun getName(strFileName: String): String {
        val pos: Int = strFileName.lastIndexOf('.')
        return if (pos == -1) strFileName else strFileName.substring(0, pos)
    }

    fun getName(res: Resource): String {
        return getName(res.getName())
    }

    /**
     * split a FileName in Parts
     *
     * @param fileName
     * @return new String[]{name[,extension]}
     */
    fun splitFileName(fileName: String): Array<String> {
        val pos: Int = fileName.lastIndexOf('.')
        return if (pos == -1) {
            arrayOf(fileName)
        } else arrayOf(fileName.substring(0, pos), fileName.substring(pos + 1))
    }

    /**
     * change extension of file and return new file
     *
     * @param file
     * @param newExtension
     * @return file with new Extension
     */
    fun changeExtension(file: Resource, newExtension: String): Resource {
        val ext = getExtension(file, null)
                ?: return file.getParentResource().getRealResource(file.getName() + '.' + newExtension)
        val name: String = file.getName()
        return file.getParentResource().getRealResource(name.substring(0, name.length() - ext.length()) + newExtension)
    }

    /**
     * @param res delete the content of a directory
     */
    fun deleteContent(src: Resource, filter: ResourceFilter?) {
        _deleteContent(src, filter, false)
    }

    fun _deleteContent(src: Resource, filter: ResourceFilter?, deleteDirectories: Boolean) {
        if (src.isDirectory()) {
            val files: Array<Resource> = if (filter == null) src.listResources() else src.listResources(filter)
            if (files != null) {
                for (i in files.indices) {
                    _deleteContent(files[i], filter, true)
                    if (deleteDirectories) {
                        try {
                            src.remove(false)
                        } catch (e: IOException) {
                        }
                    }
                }
            }
        } else if (src.isFile()) {
            src.delete()
        }
    }

    /**
     * copy a file or directory recursive (with his content)
     *
     * @param res file or directory to delete
     * @throws IOException
     * @throws FileNotFoundException
     */
    @Throws(IOException::class)
    fun copyRecursive(src: Resource, trg: Resource) {
        copyRecursive(src, trg, null)
    }

    /**
     * copy a file or directory recursive (with his content)
     *
     * @param src
     * @param trg
     * @param filter
     * @throws IOException
     * @throws FileNotFoundException
     */
    @Throws(IOException::class)
    fun copyRecursive(src: Resource, trg: Resource, filter: ResourceFilter?) {
        // print.out(src);
        // print.out(trg);
        if (!src.exists()) return
        if (src.isDirectory()) {
            if (!trg.exists()) trg.createDirectory(true)
            val files: Array<Resource> = if (filter == null) src.listResources() else src.listResources(filter)
            if (files != null) for (i in files.indices) {
                copyRecursive(files[i], trg.getRealResource(files[i].getName()), filter)
            }
        } else if (src.isFile()) {
            touch(trg)
            IOUtil.copy(src, trg)
        }
    }

    @Throws(IOException::class)
    fun copy(src: Resource, trg: Resource) {
        if (src.equals(trg)) return
        checkCopyToOK(src, trg)
        IOUtil.copy(src, trg)
    }

    /**
     * return if parent file exists
     *
     * @param res file to check
     * @return parent exists?
     */
    private fun parentExists(res: Resource): Boolean {
        var res: Resource? = res
        res = res.getParentResource()
        return res != null && res.exists()
    }

    private fun parentExists(ps: PageSource): Boolean {
        val p: PageSource = (ps as PageSourceImpl).getParent()
        return p != null && p.exists()
    }

    @Throws(IOException::class)
    fun removeChildren(res: Resource?) {
        removeChildren(res, null as ResourceFilter?)
    }

    @Throws(IOException::class)
    fun removeChildren(res: Resource, filter: ResourceNameFilter?) {
        val children: Array<Any> = (if (filter == null) res.listResources() else res.listResources(filter)) ?: return
        for (i in children.indices) {
            children[i].remove(true)
        }
    }

    @Throws(IOException::class)
    fun removeChildren(res: Resource, filter: ResourceFilter?) {
        val children: Array<Any> = (if (filter == null) res.listResources() else res.listResources(filter)) ?: return
        for (i in children.indices) {
            children[i].remove(true)
        }
    }

    fun removeChildrenEL(res: Resource?, filter: ResourceNameFilter?) {
        try {
            removeChildren(res, filter)
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
        }
    }

    fun removeChildrenEL(res: Resource?, filter: ResourceFilter?) {
        try {
            removeChildren(res, filter)
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
        }
    }

    fun removeChildrenEL(res: Resource?) {
        try {
            removeChildren(res)
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
        }
    }

    fun removeEL(res: Resource, force: Boolean) {
        try {
            res.remove(force)
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
        }
    }

    fun createFileEL(res: Resource, force: Boolean) {
        try {
            res.createFile(force)
        } catch (e: IOException) {
        }
    }

    fun createDirectoryEL(res: Resource, force: Boolean) {
        try {
            res.createDirectory(force)
        } catch (e: IOException) {
        }
    }

    fun getContentType(resource: Resource): ContentType {
        // TODO make this part of an interface
        if (resource is HTTPResource) {
            try {
                return (resource as HTTPResource).getContentType()
            } catch (e: IOException) {
            }
        }
        var `is`: InputStream? = null
        return try {
            `is` = resource.getInputStream()
            ContentTypeImpl(`is`)
        } catch (e: IOException) {
            ContentTypeImpl.APPLICATION_UNKNOW
        } finally {
            IOUtil.closeEL(`is`)
        }
    }

    fun getContentType(resource: Resource, defaultValue: ContentType): ContentType {
        if (resource is HTTPResource) {
            try {
                return (resource as HTTPResource).getContentType()
            } catch (e: IOException) {
            }
        }
        var `is`: InputStream? = null
        return try {
            `is` = resource.getInputStream()
            ContentTypeImpl(`is`)
        } catch (e: IOException) {
            defaultValue
        } finally {
            IOUtil.closeEL(`is`)
        }
    }

    @Throws(IOException::class)
    fun moveTo(src: Resource, dest: Resource, useResourceMethod: Boolean) {
        var useResourceMethod = useResourceMethod
        checkMoveToOK(src, dest)
        if (src.isFile()) {
            try {
                if (useResourceMethod) src.moveTo(dest)
            } catch (e: IOException) {
                useResourceMethod = false
            }
            if (!useResourceMethod) {
                if (!dest.exists()) dest.createFile(false)
                IOUtil.copy(src, dest)
                src.remove(false)
            }
        } else {
            if (!dest.exists()) dest.createDirectory(false)
            val children: Array<Resource> = src.listResources()
            if (children != null) {
                for (i in children.indices) {
                    moveTo(children[i], dest.getRealResource(children[i].getName()), useResourceMethod)
                }
            }
            src.remove(false)
        }
        dest.setLastModified(System.currentTimeMillis())
    }

    /**
     * return the size of the Resource, other than method length of Resource this method return the size
     * of all files in a directory
     *
     * @param collectionDir
     * @return
     */
    fun getRealSize(res: Resource): Long {
        return getRealSize(res, null)
    }

    /**
     * return the size of the Resource, other than method length of Resource this method return the size
     * of all files in a directory
     *
     * @param collectionDir
     * @return
     */
    fun getRealSize(res: Resource, filter: ResourceFilter?): Long {
        if (res.isFile()) {
            return res.length()
        } else if (res.isDirectory()) {
            var size: Long = 0
            val children: Array<Resource> = if (filter == null) res.listResources() else res.listResources(filter)
            if (children != null) {
                for (i in children.indices) {
                    size += getRealSize(children[i])
                }
            }
            return size
        }
        return 0
    }

    fun getChildCount(res: Resource): Int {
        return getChildCount(res, null)
    }

    fun getChildCount(res: Resource, filter: ResourceFilter?): Int {
        if (res.isFile()) {
            return 1
        } else if (res.isDirectory()) {
            var size = 0
            val children: Array<Resource> = if (filter == null) res.listResources() else res.listResources(filter)
            if (children != null) {
                for (i in children.indices) {
                    size += getChildCount(children[i])
                }
            }
            return size
        }
        return 0
    }

    /**
     * return if Resource is empty, means is directory and has no children or an empty file, if not
     * exist return false.
     *
     * @param res
     * @return
     */
    fun isEmpty(res: Resource): Boolean {
        return isEmptyDirectory(res, null) || isEmptyFile(res)
    }

    /**
     * return Boolean.True when directory is empty, Boolean.FALSE when directory s not empty and null if
     * directory does not exists
     *
     * @param res
     * @return
     */
    fun isEmptyDirectory(res: Resource, filter: ResourceFilter?): Boolean {
        if (res.isDirectory()) {
            val children: Array<Resource> = if (filter == null) res.listResources() else res.listResources(filter)
            if (children == null || children.size == 0) return true
            for (i in children.indices) {
                if (children[i].isFile()) return false
                if (children[i].isDirectory() && !isEmptyDirectory(children[i], filter)) return false
            }
        }
        return true
    }

    fun isEmptyFile(res: Resource): Boolean {
        return if (res.isFile()) {
            res.length() === 0
        } else false
    }

    fun toResource(file: File): Resource {
        return ResourcesImpl.getFileResourceProvider().getResource(file.getPath())
    }

    /**
     * list children of all given resources
     *
     * @param resources
     * @return
     */
    fun listResources(resources: Array<Resource>, filter: ResourceFilter?): Array<Resource?> {
        var count = 0
        var children: Array<Resource?>
        val list: ArrayList<Array<Resource>> = ArrayList<Array<Resource>>()
        for (i in resources.indices) {
            children = if (filter == null) resources[i].listResources() else resources[i].listResources(filter)
            if (children != null) {
                count += children.size
                list.add(children)
            } else list.add(arrayOfNulls<Resource>(0))
        }
        val rtn: Array<Resource?> = arrayOfNulls<Resource>(count)
        var index = 0
        for (i in resources.indices) {
            children = list.get(i)
            for (y in children.indices) {
                rtn[index++] = children[y]
            }
        }
        // print.out(rtn);
        return rtn
    }

    fun listResources(res: Resource, filter: ResourceFilter?): Array<Resource> {
        return if (filter == null) res.listResources() else res.listResources(filter)
    }

    fun deleteFileOlderThan(res: Resource, date: Long, filter: ExtensionResourceFilter?) {
        if (res.isFile()) {
            if (res.lastModified() <= date) res.delete()
        } else if (res.isDirectory()) {
            val children: Array<Resource> = if (filter == null) res.listResources() else res.listResources(filter)
            if (children != null) {
                for (i in children.indices) {
                    deleteFileOlderThan(children[i], date, filter)
                }
            }
        }
    }

    /**
     * check if directory creation is ok with the rules for the Resource interface, to not change this
     * rules.
     *
     * @param resource
     * @param createParentWhenNotExists
     * @throws IOException
     */
    @Throws(IOException::class)
    fun checkCreateDirectoryOK(resource: Resource, createParentWhenNotExists: Boolean) {
        if (resource.exists()) {
            if (resource.isFile()) throw IOException("can't create directory [" + resource.getPath().toString() + "], it already exists as a file")
            if (resource.isDirectory()) throw IOException("can't create directory [" + resource.getPath().toString() + "], directory already exists")
        }
        val parent: Resource = resource.getParentResource()
        // when there is a parent but the parent does not exist
        if (parent != null) {
            if (!parent.exists()) {
                if (createParentWhenNotExists) parent.createDirectory(true) else throw IOException("can't create file [" + resource.getPath().toString() + "], missing parent directory")
            } else if (parent.isFile()) {
                throw IOException("can't create directory [" + resource.getPath().toString() + "], parent is a file")
            }
        }
    }

    /**
     * check if file creating is ok with the rules for the Resource interface, to not change this rules.
     *
     * @param resource
     * @param createParentWhenNotExists
     * @throws IOException
     */
    @Throws(IOException::class)
    fun checkCreateFileOK(resource: Resource, createParentWhenNotExists: Boolean) {
        if (resource.exists()) {
            if (resource.isDirectory()) throw IOException("can't create file [" + resource.getPath().toString() + "], it already exists as a directory")
            if (resource.isFile()) throw IOException("can't create file [" + resource.getPath().toString() + "], file already exists")
        }
        val parent: Resource = resource.getParentResource()
        // when there is a parent but the parent does not exist
        if (parent != null) {
            if (!parent.exists()) {
                if (createParentWhenNotExists) parent.createDirectory(true) else throw IOException("can't create file [" + resource.getPath().toString() + "], missing parent directory")
            } else if (parent.isFile()) {
                throw IOException("can't create file [" + resource.getPath().toString() + "], the specified parent directory is a file")
            }
        }
    }

    /**
     * check if copying a file is ok with the rules for the Resource interface, to not change this
     * rules.
     *
     * @param source
     * @param target
     * @throws IOException
     */
    @Throws(IOException::class)
    fun checkCopyToOK(source: Resource, target: Resource) {
        if (!source.isFile()) {
            if (source.isDirectory()) throw IOException("can't copy [" + source.getPath().toString() + "] to [" + target.getPath().toString() + "], source is a directory")
            throw IOException("can't copy [" + source.getPath().toString() + "] to [" + target.getPath().toString() + "], source file doesn't exist")
        } else if (target.isDirectory()) {
            throw IOException("can't copy [" + source.getPath().toString() + "] to [" + target.getPath().toString() + "], target is a directory")
        }
    }

    /**
     * check if moveing a file is ok with the rules for the Resource interface, to not change this
     * rules.
     *
     * @param source
     * @param target
     * @throws IOException
     */
    @Throws(IOException::class)
    fun checkMoveToOK(source: Resource, target: Resource) {
        if (!source.exists()) {
            throw IOException("can't move [" + source.getPath().toString() + "] to [" + target.getPath().toString() + "], source file doesn't exist")
        }
        if (source.isDirectory() && target.isFile()) throw IOException("can't move [" + source.getPath().toString() + "] directory to [" + target.getPath().toString() + "], target is a file")
        if (source.isFile() && target.isDirectory()) throw IOException("can't move [" + source.getPath().toString() + "] file to [" + target.getPath().toString() + "], target is a directory")
    }

    /**
     * check if getting an inputstream of the file is ok with the rules for the Resource interface, to
     * not change this rules.
     *
     * @param resource
     * @throws IOException
     */
    @Throws(IOException::class)
    fun checkGetInputStreamOK(resource: Resource) {
        if (!resource.exists()) throw IOException("file [" + resource.getPath().toString() + "] does not exist")
        if (resource.isDirectory()) throw IOException("can't read directory [" + resource.getPath().toString() + "] as a file")
    }

    /**
     * check if getting an outputstream of the file is ok with the rules for the Resource interface, to
     * not change this rules.
     *
     * @param resource
     * @throws IOException
     */
    @Throws(IOException::class)
    fun checkGetOutputStreamOK(resource: Resource) {
        if (resource.exists() && !resource.isWriteable()) {
            throw IOException("can't write to file [" + resource.getPath().toString() + "],file is readonly")
        }
        if (resource.isDirectory()) throw IOException("can't write directory [" + resource.getPath().toString() + "] as a file")
        if (!resource.getParentResource().exists()) throw IOException("can't write file [" + resource.getPath().toString() + "] as a file, missing parent directory [" + resource.getParent().toString() + "]")
    }

    /**
     * check if removing the file is ok with the rules for the Resource interface, to not change this
     * rules.
     *
     * @param resource
     * @throws IOException
     */
    @Throws(IOException::class)
    fun checkRemoveOK(resource: Resource) {
        if (!resource.exists()) throw IOException("can't delete resource [$resource], resource does not exist")
        if (!resource.canWrite()) throw IOException("can't delete resource [$resource], no write access")
    }

    @Throws(IOException::class)
    fun deleteEmptyFolders(res: Resource) {
        if (res.isDirectory()) {
            val children: Array<Resource> = res.listResources()
            if (children != null) {
                for (i in children.indices) {
                    deleteEmptyFolders(children[i])
                }
            }
            if (res.listResources().length === 0) {
                res.remove(false)
            }
        }
    }

    /**
     * if the pageSource is based on an archive, translate the source to a zip:// Resource
     *
     * @return return the Resource matching this PageSource
     * @param pc the Page Context Object
     */
    @Deprecated
    @Deprecated("use instead <code>PageSource.getResourceTranslated(PageContext)</code>")
    @Throws(PageException::class)
    fun getResource(pc: PageContext?, ps: PageSource): Resource {
        return ps.getResourceTranslated(pc)
    }

    fun getResource(pc: PageContext?, ps: PageSource, defaultValue: Resource): Resource {
        return try {
            ps.getResourceTranslated(pc)
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
            defaultValue
        }
    }

    fun directrySize(dir: Resource?, filter: ResourceFilter?): Int {
        if (dir == null || !dir.isDirectory()) return 0
        return if (filter == null) dir.list().length else ArrayUtil.size(dir.list(filter))
    }

    fun directrySize(dir: Resource?, filter: ResourceNameFilter?): Int {
        if (dir == null || !dir.isDirectory()) return 0
        return if (filter == null) dir.list().length else ArrayUtil.size(dir.list(filter))
    }

    fun names(resources: Array<Resource>): Array<String?> {
        val names = arrayOfNulls<String>(resources.size)
        for (i in names.indices) {
            names[i] = resources[i].getName()
        }
        return names
    }

    fun merge(srcs: Array<Resource?>?, vararg trgs: Resource?): Array<Resource?> {
        val list: MutableList<Resource?> = ArrayList<Resource>()
        if (srcs != null) {
            for (i in srcs.indices) {
                list.add(srcs[i])
            }
        }
        if (trgs != null) {
            for (i in 0 until trgs.size) {
                if (!list.contains(trgs[i])) list.add(trgs[i])
            }
        }
        return list.toArray(arrayOfNulls<Resource>(list.size()))
    }

    @Throws(IOException::class)
    fun removeEmptyFolders(dir: Resource, filter: ResourceFilter?) {
        if (!dir.isDirectory()) return
        var children: Array<Resource> = dir.listResources(IgnoreSystemFiles.INSTANCE)
        if (!ArrayUtil.isEmpty(children)) {
            var hasFiles = false
            for (i in children.indices) {
                if (children[i].isDirectory()) removeEmptyFolders(children[i], filter) else if (children[i].isFile()) {
                    hasFiles = true
                }
            }
            if (!hasFiles) {
                children = dir.listResources(IgnoreSystemFiles.INSTANCE)
            }
        }
        if (ArrayUtil.isEmpty(children) && (filter == null || filter.accept(dir))) dir.remove(true)
    }

    fun listRecursive(res: Resource?, filter: ResourceFilter?): List<Resource> {
        val list: List<Resource> = ArrayList<Resource>()
        listRecursive(list, res, filter)
        return list
    }

    private fun listRecursive(list: List<Resource>, res: Resource?, filter: ResourceFilter?) {
        if (res == null) return
        if (filter == null || filter.accept(res)) list.add(res)
        if (!res.isDirectory()) return
        val children: Array<Resource> = res.listResources(DirectoryResourceFilter.FILTER)
        if (children != null) for (i in children.indices) {
            listRecursive(children[i], filter)
        }
    }

    fun getSeparator(rp: ResourceProvider): Char {
        return if (rp is ResourceProviderPro) (rp as ResourceProviderPro).getSeparator() else '/'
    }

    fun removeExtension(filename: String, defaultValue: String): String {
        val index: Int = filename.lastIndexOf('.')
        return if (index == -1) defaultValue else filename.substring(0, index)
    }

    @Throws(NoSuchAlgorithmException::class, IOException::class)
    fun checksum(res: Resource?): String {
        return Hash.md5(res)
    }

    @Throws(IOException::class)
    fun toFile(res: Resource): File {
        if (res is File) return res as File
        throw IOException("cannot convert [" + res + "] to a local file from type [" + res.getResourceProvider().getScheme() + "]")
    }

    init {
        EXT_MT.put("ai", "application/postscript")
        EXT_MT.put("aif", "audio/x-aiff")
        EXT_MT.put("aifc", "audio/x-aiff")
        EXT_MT.put("aiff", "audio/x-aiff")
        EXT_MT.put("au", "audio/basic")
        EXT_MT.put("avi", "video/x-msvideo")
        EXT_MT.put("bin", "application/octet-stream")
        EXT_MT.put("bmp", "image/x-ms-bmp")
        EXT_MT.put("cgm", "image/cgm")
        EXT_MT.put("cmx", "image/x-cmx")
        EXT_MT.put("csh", "application/x-csh")
        EXT_MT.put("cfm", "text/html")
        EXT_MT.put("cfml", "text/html")
        EXT_MT.put("css", "text/css")
        EXT_MT.put("doc", "application/msword")
        EXT_MT.put("docx", "application/msword")
        EXT_MT.put("eps", "application/postscript")
        EXT_MT.put("exe", "application/octet-stream")
        EXT_MT.put("gif", "image/gif")
        EXT_MT.put("gtar", "application/x-gtar")
        EXT_MT.put("hqx", "application/mac-binhex40")
        EXT_MT.put("htm", "text/html")
        EXT_MT.put("html", "text/html")
        EXT_MT.put("jpe", "image/jpeg")
        EXT_MT.put("jpeg", "image/jpeg")
        EXT_MT.put("jpg", "image/jpeg")
        EXT_MT.put("js", "text/javascript")
        EXT_MT.put("mmid", "x-music/x-midi")
        EXT_MT.put("mov", "video/quicktime")
        EXT_MT.put("mp2a", "audio/x-mpeg-2")
        EXT_MT.put("mp2v", "video/mpeg-2")
        EXT_MT.put("mp3", "audio/mpeg")
        EXT_MT.put("mp4", "video/mp4")
        EXT_MT.put("mpa", "audio/x-mpeg")
        EXT_MT.put("mpa2", "audio/x-mpeg-2")
        EXT_MT.put("mpeg", "video/mpeg")
        EXT_MT.put("mpega", "audio/x-mpeg")
        EXT_MT.put("mpg", "video/mpeg")
        EXT_MT.put("mpv2", "video/mpeg-2")
        EXT_MT.put("pbm", "image/x-portable-bitmap")
        EXT_MT.put("pcd", "image/x-photo-cd")
        EXT_MT.put("pdf", "application/pdf")
        EXT_MT.put("pgm", "image/x-portable-graymap")
        EXT_MT.put("pict", "image/x-pict")
        EXT_MT.put("pl", "application/x-perl")
        EXT_MT.put("png", "image/png")
        EXT_MT.put("php", "text/html")
        EXT_MT.put("pnm", "image/x-portable-anymap")
        EXT_MT.put("ppm", "image/x-portable-pixmap")
        EXT_MT.put("ppt", "application/vnd.ms-powerpoint")
        EXT_MT.put("pptx", "application/vnd.ms-powerpoint")
        EXT_MT.put("ps", "application/postscript")
        EXT_MT.put("qt", "video/quicktime")
        EXT_MT.put("rgb", "image/rgb")
        EXT_MT.put("rtf", "application/rtf")
        EXT_MT.put("sh", "application/x-sh")
        EXT_MT.put("sit", "application/x-stuffit")
        EXT_MT.put("swf", "application/x-shockwave-flash")
        EXT_MT.put("tar", "application/x-tar")
        EXT_MT.put("tcl", "application/x-tcl")
        EXT_MT.put("tif", "image/tiff")
        EXT_MT.put("tiff", "image/tiff")
        EXT_MT.put("txt", "text/plain")
        EXT_MT.put("wav", "audio/x-wav")
        EXT_MT.put("wma", "audio/x-ms-wma")
        EXT_MT.put("wmv", "video/x-ms-wmv")
        EXT_MT.put("xbm", "image/x-xbitmap")
        EXT_MT.put("xhtml", "application/xhtml+xml")
        EXT_MT.put("xls", "application/vnd.ms-excel")
        EXT_MT.put("xlsx", "application/vnd.ms-excel")
        EXT_MT.put("xpm", "image/x-xpixmap")
        EXT_MT.put("zip", "application/zip")
        val te: Array<String> = Constants.getTemplateExtensions()
        for (i in tachyon.commons.io.res.util.te.indices) {
            EXT_MT.put(tachyon.commons.io.res.util.te.get(i), "text/html")
        }
    }
}