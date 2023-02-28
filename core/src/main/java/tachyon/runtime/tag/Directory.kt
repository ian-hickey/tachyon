/**
 * Copyright (c) 2014, the Railo Company Ltd. All rights reserved.
 *
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.  If not, see <http:></http:>//www.gnu.org/licenses/>.
 */
package tachyon.runtime.tag

import tachyon.runtime.tag.util.FileUtil.NAMECONFLICT_ERROR

/**
 * Handles interactions with directories.
 */
class Directory : TagImpl() {
    /**
     * Optional for action = "list". Ignored by all other actions. File extension filter applied to
     * returned names. For example: *m. Only one mask filter can be applied at a time.
     */
    // private final ResourceFilter filter=null;
    // private ResourceAndResourceNameFilter nameFilter=null;
    private var filter: ResourceFilter? = null
    private var pattern: String? = null
    private var patternDelimiters: String? = null

    /** The name of the directory to perform the action against.  */
    private var directory: Resource? = null

    /** Defines the action to be taken with directory(ies) specified in directory.  */
    private var action: String? = "list"

    /**
     * Optional for action = "list". Ignored by all other actions. The query columns by which to sort
     * the directory listing. Any combination of columns from query output can be specified in
     * comma-separated list. You can specify ASC (ascending) or DESC (descending) as qualifiers for
     * column names. ASC is the default
     */
    private var sort: String? = null

    /**
     * Used with action = "Create" to define the permissions for a directory on UNIX and Linux
     * platforms. Ignored on Windows. Options correspond to the octal values of the UNIX chmod command.
     * From left to right, permissions are assigned for owner, group, and other.
     */
    private var mode = -1

    /**
     * Required for action = "rename". Ignored by all other actions. The new name of the directory
     * specified in the directory attribute.
     */
    private var strNewdirectory: String? = null

    /**
     * Required for action = "list". Ignored by all other actions. Name of output query for directory
     * listing.
     */
    private var name: String? = null
    private var recurse = false
    private var serverPassword: String? = null
    private var type = TYPE_ALL

    // private boolean listOnlyNames;
    private var listInfo = LIST_INFO_QUERY_ALL

    // private int acl=S3Constants.ACL_UNKNOW;
    private var acl: Object? = null
    private var storage: String? = null
    private var destination: String? = null
    private var nameconflict = NAMECONFLICT_DEFAULT
    private var createPath = true
    @Override
    fun release() {
        super.release()
        acl = null
        storage = null
        type = TYPE_ALL
        // filter=null;
        filter = null
        destination = null
        directory = null
        action = "list"
        sort = null
        mode = -1
        strNewdirectory = null
        name = null
        recurse = false
        serverPassword = null
        listInfo = LIST_INFO_QUERY_ALL
        nameconflict = NAMECONFLICT_DEFAULT
        createPath = true
        pattern = null
        patternDelimiters = null
    }

    fun setCreatepath(createPath: Boolean) {
        this.createPath = createPath
    }

    /**
     * sets a filter
     *
     * @param filter
     * @throws PageException
     */
    @Throws(PageException::class)
    fun setFilter(filter: Object?) {
        if (filter is UDF) this.setFilter(filter as UDF?) else if (filter is String) this.setFilter(filter as String?)
    }

    @Throws(PageException::class)
    fun setFilter(filter: UDF?) {
        this.filter = UDFFilter.createResourceAndResourceNameFilter(filter)
    }

    fun setFilter(pattern: String?) {
        this.pattern = pattern
    }

    fun setFilterdelimiters(patternDelimiters: String?) {
        this.patternDelimiters = patternDelimiters
    }

    /**
     * set the value acl used only for s3 resources, for all others ignored
     *
     * @param acl value to set
     * @throws ApplicationException
     * @Deprecated only exists for backward compatibility to old ra files.
     */
    @Throws(ApplicationException::class)
    fun setAcl(acl: String?) {
        this.acl = acl
    }

    fun setAcl(acl: Object?) {
        this.acl = acl
    }

    fun setStoreacl(acl: Object?) {
        this.acl = acl
    }

    /**
     * set the value storage used only for s3 resources, for all others ignored
     *
     * @param storage value to set
     * @throws PageException
     */
    @Throws(PageException::class)
    fun setStorage(storage: String?) {
        this.storage = improveStorage(storage)
    }

    @Throws(PageException::class)
    fun setStorelocation(storage: String?) {
        setStorage(storage)
    }

    fun setServerpassword(serverPassword: String?) {
        this.serverPassword = serverPassword
    }

    fun setListinfo(strListinfo: String?) {
        var strListinfo = strListinfo
        strListinfo = strListinfo.trim().toLowerCase()
        listInfo = if ("name".equals(strListinfo)) LIST_INFO_QUERY_NAME else LIST_INFO_QUERY_ALL
    }

    /**
     * set the value directory The name of the directory to perform the action against.
     *
     * @param directory value to set
     */
    fun setDirectory(directory: String?) {
        this.directory = ResourceUtil.toResourceNotExisting(pageContext, directory)
        // print.ln(this.directory);
    }

    /**
     * set the value action Defines the action to be taken with directory(ies) specified in directory.
     *
     * @param action value to set
     */
    fun setAction(action: String?) {
        this.action = action.toLowerCase()
    }

    /**
     * set the value sort Optional for action = "list". Ignored by all other actions. The query columns
     * by which to sort the directory listing. Any combination of columns from query output can be
     * specified in comma-separated list. You can specify ASC (ascending) or DESC (descending) as
     * qualifiers for column names. ASC is the default
     *
     * @param sort value to set
     */
    fun setSort(sort: String?) {
        if (sort.trim().length() > 0) this.sort = sort
    }

    /**
     * set the value mode Used with action = "Create" to define the permissions for a directory on UNIX
     * and Linux platforms. Ignored on Windows. Options correspond to the octal values of the UNIX chmod
     * command. From left to right, permissions are assigned for owner, group, and other.
     *
     * @param mode value to set
     * @throws PageException
     */
    @Throws(PageException::class)
    fun setMode(mode: String?) {
        try {
            this.mode = ModeUtil.toOctalMode(mode)
        } catch (e: IOException) {
            throw Caster.toPageException(e)
        }
    }

    /**
     * set the value newdirectory Required for action = "rename". Ignored by all other actions. The new
     * name of the directory specified in the directory attribute.
     *
     * @param newdirectory value to set
     */
    fun setNewdirectory(newdirectory: String?) {
        // this.newdirectory=ResourceUtil.toResourceNotExisting(pageContext ,newdirectory);
        strNewdirectory = newdirectory
    }

    fun setDestination(destination: String?) {
        this.destination = destination
    }

    /**
     * set the value name Required for action = "list". Ignored by all other actions. Name of output
     * query for directory listing.
     *
     * @param name value to set
     */
    fun setName(name: String?) {
        this.name = name
    }

    /**
     * @param recurse The recurse to set.
     */
    fun setRecurse(recurse: Boolean) {
        this.recurse = recurse
    }

    /**
     * set the value nameconflict Action to take if destination directory is the same as that of a file
     * in the directory.
     *
     * @param nameconflict value to set
     * @throws ApplicationException
     */
    @Throws(ApplicationException::class)
    fun setNameconflict(nameconflict: String?) {
        this.nameconflict = FileUtil.toNameConflict(nameconflict, NAMECONFLICT_UNDEFINED or NAMECONFLICT_ERROR or NAMECONFLICT_OVERWRITE or NAMECONFLICT_SKIP, NAMECONFLICT_DEFAULT)
    }

    @Override
    @Throws(PageException::class)
    fun doStartTag(): Int {
        if (filter == null && !StringUtil.isEmpty(pattern)) filter = WildcardPatternFilter(pattern, patternDelimiters)

        // securityManager = pageContext.getConfig().getSecurityManager();
        if (action!!.equals("list")) {
            val res: Object? = actionList(pageContext, directory, serverPassword, type, filter, listInfo, recurse, sort)
            if (!StringUtil.isEmpty(name) && res != null) pageContext.setVariable(name, res)
        } else if (action!!.equals("create")) actionCreate(pageContext, directory, serverPassword, createPath, mode, acl, storage, nameconflict) else if (action!!.equals("delete")) actionDelete(pageContext, directory, recurse, serverPassword) else if (action!!.equals("forcedelete")) actionDelete(pageContext, directory, true, serverPassword) else if (action!!.equals("rename")) {
            val res = actionRename(pageContext, directory, strNewdirectory, serverPassword, createPath, acl, storage)
            if (!StringUtil.isEmpty(name) && res != null) pageContext.setVariable(name, res)
        } else if (action!!.equals("copy")) {
            if (StringUtil.isEmpty(destination, true) && !StringUtil.isEmpty(strNewdirectory, true)) {
                destination = strNewdirectory.trim()
            }
            actionCopy(pageContext, directory, destination, serverPassword, createPath, acl, storage, filter, recurse, nameconflict)
        } else if (action!!.equals("info")) {
            val res: Object? = getInfo(pageContext, directory, null)
            if (!StringUtil.isEmpty(name) && res != null) pageContext.setVariable(name, res)
        } else throw ApplicationException("Invalid action [$action] for the tag [directory]")
        return SKIP_BODY
    }

    @Override
    fun doEndTag(): Int {
        return EVAL_PAGE
    }

    /**
     * @param strType the type to set
     */
    @Throws(ApplicationException::class)
    fun setType(strType: String?) {
        if (StringUtil.isEmpty(strType)) return
        type = toType(strType)
    }

    companion object {
        const val TYPE_ALL = 0
        const val TYPE_FILE = 1
        const val TYPE_DIR = 2
        val DIRECTORY_FILTER: ResourceFilter? = DirectoryResourceFilter()
        val FILE_FILTER: ResourceFilter? = FileResourceFilter()
        private val MODE: Key? = KeyConstants._mode
        private val META: Key? = KeyConstants._meta
        private val DATE_LAST_MODIFIED: Key? = KeyConstants._dateLastModified
        private val ATTRIBUTES: Key? = KeyConstants._attributes
        private val DIRECTORY: Key? = KeyConstants._directory
        const val LIST_INFO_QUERY_ALL = 1
        const val LIST_INFO_QUERY_NAME = 2
        const val LIST_INFO_ARRAY_NAME = 4
        const val LIST_INFO_ARRAY_PATH = 8
        val NAMECONFLICT_DEFAULT: Int = NAMECONFLICT_OVERWRITE // default
        @Throws(ApplicationException::class)
        fun improveStorage(storage: String?): String? {
            var storage = storage
            storage = improveStorage(storage, null)
            if (storage != null) return storage
            throw ApplicationException("Invalid storage value, valid values are [eu, us, us-west]")
        }

        fun improveStorage(storage: String?, defaultValue: String?): String? {
            var storage = storage
            storage = storage.toLowerCase().trim()
            if ("us".equals(storage)) return "us"
            if ("usa".equals(storage)) return "us"
            if ("u.s.".equals(storage)) return "us"
            if ("u.s.a.".equals(storage)) return "us"
            if ("united states of america".equals(storage)) return "us"
            if ("eu".equals(storage)) return "eu"
            if ("europe.".equals(storage)) return "eu"
            if ("european union.".equals(storage)) return "eu"
            if ("euro.".equals(storage)) return "eu"
            if ("e.u.".equals(storage)) return "eu"
            if ("us-west".equals(storage)) return "us-west"
            return if ("usa-west".equals(storage)) "us-west" else defaultValue
        }

        /**
         * list all files and directories inside a directory
         *
         * @throws PageException
         */
        @Throws(PageException::class)
        fun actionList(pageContext: PageContext?, directory: Resource?, serverPassword: String?, type: Int, filter: ResourceFilter?, listInfo: Int, recurse: Boolean, sort: String?): Object? {
            // check directory
            var filter: ResourceFilter? = filter
            val securityManager: SecurityManager = pageContext.getConfig().getSecurityManager()
            securityManager.checkFileLocation(pageContext.getConfig(), directory, serverPassword)
            if (type != TYPE_ALL) {
                val typeFilter: ResourceFilter = if (type == TYPE_DIR) DIRECTORY_FILTER else FILE_FILTER
                if (filter == null) filter = typeFilter else filter = AndResourceFilter(arrayOf<ResourceFilter?>(typeFilter, filter))
            }

            // create query Object
            var names = arrayOf<String?>("name", "size", "type", "dateLastModified", "attributes", "mode", "directory")
            var types = arrayOf<String?>("VARCHAR", "DOUBLE", "VARCHAR", "DATE", "VARCHAR", "VARCHAR", "VARCHAR")
            val hasMeta = directory is ResourceMetaData
            if (hasMeta) {
                names = arrayOf("name", "size", "type", "dateLastModified", "attributes", "mode", "directory", "meta")
                types = arrayOf("VARCHAR", "DOUBLE", "VARCHAR", "DATE", "VARCHAR", "VARCHAR", "VARCHAR", "OBJECT")
            }
            val typeArray = listInfo == LIST_INFO_ARRAY_NAME || listInfo == LIST_INFO_ARRAY_PATH
            val namesOnly = listInfo == LIST_INFO_ARRAY_NAME || listInfo == LIST_INFO_QUERY_NAME || listInfo == LIST_INFO_ARRAY_PATH
            var array: Array? = null
            val rtn: Object?
            val query: Query = QueryImpl(if (namesOnly) arrayOf<String?>("name") else names, if (namesOnly) arrayOf<String?>("VARCHAR") else types, 0, "query")
            if (typeArray) {
                array = ArrayImpl()
                rtn = array
            } else {
                rtn = query
            }
            if (!directory.exists()) {
                if (directory is FileResource) return rtn
                throw ApplicationException("Directory [" + directory.toString().toString() + "] doesn't exist")
            }
            if (!directory.isDirectory()) {
                if (directory is FileResource) return rtn
                throw ApplicationException("File [" + directory.toString().toString() + "] exists, but isn't a directory")
            }
            if (!directory.isReadable()) {
                if (directory is FileResource) return rtn
                throw ApplicationException("No access to read directory [" + directory.toString().toString() + "]")
            }
            val startNS: Long = System.nanoTime()
            try {
                if (namesOnly) {
                    if (typeArray) {
                        _fillArrayPathOrName(array, directory, filter, 0, recurse, listInfo == LIST_INFO_ARRAY_NAME)
                        return array
                    }

                    // Query Name, available via the cfdirectory tag but not via directoryList()
                    if (recurse || type != TYPE_ALL) _fillQueryNamesRec("", query, directory, filter, 0, recurse) else _fillQueryNames(query, directory, filter, 0)
                } else {
                    // Query All
                    _fillQueryAll(query, directory, filter, 0, hasMeta, recurse)
                }
            } catch (e: IOException) {
                throw Caster.toPageException(e)
            }

            // sort
            if (sort != null && query != null) {
                val arr: Array<String?> = sort.toLowerCase().split(",")
                for (i in arr.indices.reversed()) {
                    try {
                        val col: Array<String?> = arr[i].trim().split("\\s+")
                        if (col.size == 1) query.sort(col[0].trim()) else if (col.size == 2) {
                            val order: String = col[1].toLowerCase().trim()
                            if (order.equals("asc")) query.sort(col[0], tachyon.runtime.type.Query.ORDER_ASC) else if (order.equals("desc")) query.sort(col[0], tachyon.runtime.type.Query.ORDER_DESC) else throw ApplicationException("Invalid order type [" + col[1] + "]")
                        }
                    } catch (t: Throwable) {
                        ExceptionUtil.rethrowIfNecessary(t)
                    }
                }
            }
            query.setExecutionTime(System.nanoTime() - startNS)
            if (typeArray) {
                val it: Iterator<*> = query.getIterator()
                while (it.hasNext()) {
                    val row: Struct? = it.next() as Struct?
                    if (namesOnly) array.appendEL(row.get("name")) else array.appendEL(row.get("directory") + tachyon.commons.io.FileUtil.FILE_SEPERATOR_STRING + row.get("name"))
                }
            }
            return rtn
        }

        @Throws(PageException::class)
        fun getInfo(pc: PageContext?, directory: Resource?, serverPassword: String?): Struct? {
            val securityManager: SecurityManager = pc.getConfig().getSecurityManager()
            securityManager.checkFileLocation(pc.getConfig(), directory, serverPassword)
            if (!directory.exists()) throw ApplicationException("Directory [" + directory.toString().toString() + "] doesn't exist")
            if (!directory.isDirectory()) throw ApplicationException("[" + directory.toString().toString() + "] isn't a directory")
            if (!directory.canRead()) throw ApplicationException("No access to read directory [" + directory.toString().toString() + "]")
            securityManager.checkFileLocation(pc.getConfig(), directory, serverPassword)
            val sct: Struct = StructImpl()
            sct.setEL("directoryName", directory.getName())
            sct.setEL(KeyConstants._size, Long.valueOf(directory.length()))
            sct.setEL("isReadable", directory.isReadable())
            sct.setEL(KeyConstants._path, directory.getAbsolutePath())
            sct.setEL("dateLastModified", DateTimeImpl(pc.getConfig()))
            if (SystemUtil.isUnix()) sct.setEL(KeyConstants._mode, ModeObjectWrap(directory))
            val file = File(Caster.toString(directory))
            val attr: BasicFileAttributes
            try {
                attr = Files.readAttributes(file.toPath(), BasicFileAttributes::class.java)
                sct.setEL("directoryCreated", DateTimeImpl(pc, attr.creationTime().toMillis(), false))
            } catch (e: Exception) {
            }
            return sct
        }

        @Throws(PageException::class, IOException::class)
        private fun _fillQueryAll(query: Query?, directory: Resource?, filter: ResourceFilter?, count: Int, hasMeta: Boolean, recurse: Boolean): Int {
            var count = count
            val list: Array<Resource?> = directory.listResources()
            if (list == null || list.size == 0) return count
            val dir: String = directory.getCanonicalPath()
            // fill data to query
            // query.addRow(list.length);
            var isDir: Boolean
            val modeSupported: Boolean = directory.getResourceProvider().isModeSupported()
            for (i in list.indices) {
                isDir = list[i].isDirectory()
                if (filter == null || filter.accept(list[i])) {
                    query.addRow(1)
                    count++
                    query.setAt(KeyConstants._name, count, list[i].getName())
                    query.setAt(KeyConstants._size, count, Double.valueOf(if (isDir) 0 else list[i].length()))
                    query.setAt(KeyConstants._type, count, if (isDir) "Dir" else "File")
                    if (modeSupported) {
                        query.setAt(MODE, count, ModeObjectWrap(list[i]))
                    }
                    query.setAt(DATE_LAST_MODIFIED, count, Date(list[i].lastModified()))
                    // TODO File Attributes are Windows only...
                    // this is slow as it fetches each the attributes one at a time
                    query.setAt(ATTRIBUTES, count, getFileAttribute(list[i], true))
                    if (hasMeta) {
                        query.setAt(META, count, (list[i] as ResourceMetaData?).getMetaData())
                    }
                    query.setAt(DIRECTORY, count, dir)
                }
                if (recurse && isDir) count = _fillQueryAll(query, list[i], filter, count, hasMeta, recurse)
            }
            return count
        }

        // this method only exists for performance reasion
        @Throws(PageException::class)
        private fun _fillQueryNames(query: Query?, directory: Resource?, filter: ResourceFilter?, count: Int): Int {
            var count = count
            if (filter == null || filter is ResourceNameFilter) {
                val rnf: ResourceNameFilter? = if (filter == null) null else filter as ResourceNameFilter?
                val list: Array<String?> = directory.list()
                if (list == null || list.size == 0) return count
                for (i in list.indices) {
                    if (rnf == null || rnf.accept(directory, list[i])) {
                        query.addRow(1)
                        count++
                        query.setAt(KeyConstants._name, count, list[i])
                    }
                }
            } else {
                val list: Array<Resource?> = directory.listResources()
                if (list == null || list.size == 0) return count
                for (i in list.indices) {
                    if (filter == null || filter.accept(list[i])) {
                        query.addRow(1)
                        count++
                        query.setAt(KeyConstants._name, count, list[i].getName())
                    }
                }
            }
            return count
        }

        @Throws(PageException::class)
        private fun _fillQueryNamesRec(parent: String?, query: Query?, directory: Resource?, filter: ResourceFilter?, count: Int, recurse: Boolean): Int {
            var count = count
            val list: Array<Resource?> = directory.listResources()
            if (list == null || list.size == 0) return count
            for (i in list.indices) {
                if (filter == null || filter.accept(list[i])) {
                    query.addRow(1)
                    count++
                    query.setAt(KeyConstants._name, count, parent.concat(list[i].getName()))
                }
                if (recurse && list[i].isDirectory()) count = _fillQueryNamesRec(parent + list[i].getName().toString() + "/", query, list[i], filter, count, recurse)
            }
            return count
        }

        @Throws(PageException::class)
        private fun _fillArrayPathOrName(arr: Array?, directory: Resource?, filter: ResourceFilter?, count: Int, recurse: Boolean, onlyName: Boolean): Int {
            var count = count
            val list: Array<Resource?> = directory.listResources()
            if (list == null || list.size == 0) return count
            for (i in list.indices) {
                if (filter == null || filter.accept(list[i])) {
                    arr.appendEL(if (onlyName) list[i].getName() else list[i].getAbsolutePath())
                    count++
                }
                if (recurse && list[i].isDirectory()) count = _fillArrayPathOrName(arr, list[i], filter, count, recurse, onlyName)
            }
            return count
        }

        // this method only exists for performance reason
        private fun _fillArrayName(arr: Array?, directory: Resource?, filter: ResourceFilter?, count: Int): Int {
            if (filter == null || filter is ResourceNameFilter) {
                val rnf: ResourceNameFilter? = if (filter == null) null else filter as ResourceNameFilter?
                val list: Array<String?> = directory.list()
                if (list == null || list.size == 0) return count
                for (i in list.indices) {
                    if (rnf == null || rnf.accept(directory, list[i])) {
                        arr.appendEL(list[i])
                    }
                }
            } else {
                val list: Array<Resource?> = directory.listResources()
                if (list == null || list.size == 0) return count
                for (i in list.indices) {
                    if (filter.accept(list[i])) {
                        arr.appendEL(list[i].getName())
                    }
                }
            }
            return count
        }

        /**
         * create a directory
         *
         * @throws PageException
         */
        @Throws(PageException::class)
        fun actionCreate(pc: PageContext?, directory: Resource?, serverPassword: String?, createPath: Boolean, mode: Int, acl: Object?, storage: String?, nameConflict: Int) {
            val securityManager: SecurityManager = pc.getConfig().getSecurityManager()
            securityManager.checkFileLocation(pc.getConfig(), directory, serverPassword)
            if (directory.exists()) {
                if (directory.isDirectory()) {
                    if (nameConflict == NAMECONFLICT_SKIP) return
                    throw ApplicationException("Directory [" + directory.toString().toString() + "] already exists")
                } else if (directory.isFile()) throw ApplicationException("Can't create directory [" + directory.toString().toString() + "], a file exists with the same name")
            }
            // if(!directory.mkdirs()) throw new ApplicationException("can't create directory
            // ["+directory.toString()+"]");
            try {
                directory.createDirectory(createPath)
            } catch (ioe: IOException) {
                throw Caster.toPageException(ioe)
            }

            // set S3 stuff
            setS3Attrs(pc, directory, acl, storage)

            // Set Mode
            if (mode != -1) {
                try {
                    directory.setMode(mode)
                    // FileUtil.setMode(directory,mode);
                } catch (e: IOException) {
                    throw Caster.toPageException(e)
                }
            }
        }

        @Throws(PageException::class)
        fun setS3Attrs(pc: PageContext?, res: Resource?, acl: Object?, storage: String?) {
            val scheme: String = res.getResourceProvider().getScheme()
            if ("s3".equalsIgnoreCase(scheme)) {
                // ACL
                if (acl != null) {
                    try {
                        // old way
                        val bif: BIF = CFMLEngineFactory.getInstance().getClassUtil().loadBIF(pc, "StoreSetACL")
                        bif.invoke(pc, arrayOf<Object?>(res.getAbsolutePath(), acl))
                    } catch (e: Exception) {
                        throw Caster.toPageException(e)
                    }
                }
                // STORAGE
                if (storage != null) {
                    Reflector.callMethod(res, "setStorage", arrayOf(storage))
                }
            }
        }

        @Throws(ApplicationException::class)
        fun improveACL(acl: String?): String? {
            var acl = acl
            acl = acl.toLowerCase().trim()
            if ("public-read".equals(acl)) return "public-read"
            if ("publicread".equals(acl)) return "public-read"
            if ("public_read".equals(acl)) return "public-read"
            if ("public-read-write".equals(acl)) return "public-read-write"
            if ("publicreadwrite".equals(acl)) return "public-read-write"
            if ("public_read_write".equals(acl)) return "public-read-write"
            if ("private".equals(acl)) return "private"
            if ("authenticated-read".equals(acl)) return "authenticated-read"
            if ("authenticated_read".equals(acl)) return "authenticated-read"
            if ("authenticatedread".equals(acl)) return "authenticated-read"
            throw ApplicationException("Invalid acl value, valid values are [public-read, private, public-read-write, authenticated-read]")
        }

        /**
         * delete directory
         *
         * @param dir
         * @param forceDelete
         * @throws PageException
         */
        @Throws(PageException::class)
        fun actionDelete(pc: PageContext?, dir: Resource?, forceDelete: Boolean, serverPassword: String?) {
            val securityManager: SecurityManager = pc.getConfig().getSecurityManager()
            securityManager.checkFileLocation(pc.getConfig(), dir, serverPassword)

            // directory doesn't exist
            if (!dir.exists()) {
                if (dir.isDirectory()) throw ApplicationException("Directory [" + dir.toString().toString() + "] doesn't exist") else if (dir.isFile()) throw ApplicationException("File [" + dir.toString().toString() + "] doesn't exist and isn't a directory")
            }

            // check if file
            if (dir.isFile()) throw ApplicationException("Can't delete [" + dir.toString().toString() + "], it isn't a directory, it's a file")

            // check directory is empty
            val dirList: Array<Resource?> = dir.listResources()
            if (dirList != null && dirList.size > 0 && forceDelete == false) throw ApplicationException("Directory [" + dir.toString().toString() + "] is not empty", "set recurse=true to delete sub-directories and files too")

            // delete directory
            try {
                dir.remove(forceDelete)
            } catch (e: IOException) {
                throw Caster.toPageException(e)
            }
        }

        /**
         * rename a directory to a new Name
         *
         * @throws PageException
         */
        @Throws(PageException::class)
        fun actionRename(pc: PageContext?, directory: Resource?, strNewdirectory: String?, serverPassword: String?, createPath: Boolean, acl: Object?, storage: String?): String? {
            // check directory
            val securityManager: SecurityManager = pc.getConfig().getSecurityManager()
            securityManager.checkFileLocation(pc.getConfig(), directory, serverPassword)
            if (!directory.exists()) throw ApplicationException("The directory [" + directory.toString().toString() + "] doesn't exist")
            if (!directory.isDirectory()) throw ApplicationException("The file [" + directory.toString().toString() + "] exists, but it isn't a directory")
            if (!directory.canRead()) throw ApplicationException("No access to read directory [" + directory.toString().toString() + "]")
            if (strNewdirectory == null) throw ApplicationException("The attribute [newDirectory] is not defined")

            // real to source
            val newdirectory: Resource? = toDestination(pc, strNewdirectory, directory)
            securityManager.checkFileLocation(pc.getConfig(), newdirectory, serverPassword)
            if (newdirectory.exists()) throw ApplicationException("New directory [" + newdirectory.toString().toString() + "] already exists")
            if (createPath) {
                newdirectory.getParentResource().mkdirs()
            }
            try {
                directory.moveTo(newdirectory)
            } catch (t: Throwable) {
                ExceptionUtil.rethrowIfNecessary(t)
                throw Caster.toPageException(t)
            }

            // set S3 stuff
            setS3Attrs(pc, newdirectory, acl, storage)
            return newdirectory.toString()
        }

        @Throws(PageException::class)
        fun actionCopy(pc: PageContext?, directory: Resource?, strDestination: String?, serverPassword: String?, createPath: Boolean, acl: Object?, storage: String?,
                       filter: ResourceFilter?, recurse: Boolean, nameconflict: Int) {
            // check directory
            val securityManager: SecurityManager = pc.getConfig().getSecurityManager()
            securityManager.checkFileLocation(pc.getConfig(), directory, serverPassword)
            if (!directory.exists()) throw ApplicationException("Directory [" + directory.toString().toString() + "] doesn't exist")
            if (!directory.isDirectory()) throw ApplicationException("File [" + directory.toString().toString() + "] exists, but isn't a directory")
            if (!directory.canRead()) throw ApplicationException("No access to read directory [" + directory.toString().toString() + "]")
            if (StringUtil.isEmpty(strDestination)) throw ApplicationException("Attribute [destination] is not defined")

            // real to source
            val newdirectory: Resource? = toDestination(pc, strDestination, directory)
            if (nameconflict == NAMECONFLICT_ERROR && newdirectory.exists()) throw ApplicationException("New directory [" + newdirectory.toString().toString() + "] already exists")
            securityManager.checkFileLocation(pc.getConfig(), newdirectory, serverPassword)
            try {
                var clearEmpty = false
                // has already a filter
                var f: ResourceFilter? = null
                if (filter != null) {
                    if (!recurse) {
                        f = AndResourceFilter(arrayOf<ResourceFilter?>(filter, NotResourceFilter(DirectoryResourceFilter.FILTER)))
                    } else {
                        clearEmpty = true
                        f = OrResourceFilter(arrayOf<ResourceFilter?>(filter, DirectoryResourceFilter.FILTER))
                    }
                } else {
                    if (!recurse) f = NotResourceFilter(DirectoryResourceFilter.FILTER)
                }
                if (!createPath) {
                    val p: Resource = newdirectory.getParentResource()
                    if (p != null && !p.exists()) throw ApplicationException("parent directory for [$newdirectory] doesn't exist")
                }
                ResourceUtil.copyRecursive(directory, newdirectory, f)
                if (clearEmpty) ResourceUtil.removeEmptyFolders(newdirectory, if (f == null) null else NotResourceFilter(filter))
            } catch (t: Throwable) {
                ExceptionUtil.rethrowIfNecessary(t)
                throw ApplicationException(t.getMessage())
            }

            // set S3 stuff
            setS3Attrs(pc, newdirectory, acl, storage)
        }

        private fun toDestination(pageContext: PageContext?, path: String?, source: Resource?): Resource? {
            if (source != null && path.indexOf(File.separatorChar) === -1 && path.indexOf('/') === -1 && path.indexOf('\\') === -1) {
                val p: Resource = source.getParentResource()
                if (p != null) return p.getRealResource(path)
            }
            return ResourceUtil.toResourceNotExisting(pageContext, path)
        }

        private fun getFileAttribute(file: Resource?, exists: Boolean): String? {
            // TODO this is slow as it fetches attributes one at a time
            // also Windows only!
            return if (exists && !file.isWriteable()) "R".concat(if (file.isHidden()) "H" else "") else if (file.isHidden()) "H" else ""
        }

        @Throws(ApplicationException::class)
        fun toType(strType: String?): Int {
            var strType = strType
            strType = strType.trim().toLowerCase()
            return if ("all".equals(strType)) TYPE_ALL else if ("dir".equals(strType)) TYPE_DIR else if ("directory".equals(strType)) TYPE_DIR else if ("file".equals(strType)) TYPE_FILE else throw ApplicationException("Invalid type [$strType], valid types are [all, directory, file]")
        }
    }
}