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
package lucee.runtime.tag

import lucee.runtime.tag.util.FileUtil.NAMECONFLICT_ERROR

/**
 * Handles all interactions with files. The attributes you use with cffile depend on the value of
 * the action attribute. For example, if the action = "write", use the attributes associated with
 * writing a text file.
 *
 *
 *
 */
class FileTag : BodyTagImpl() {
    // private static final String DEFAULT_ENCODING=Charset.getDefault();
    /** Type of file manipulation that the tag performs.  */
    private var action = 0

    /** Absolute pathname of directory or file on web server.  */
    private var strDestination: String? = null

    /** Content of the file to be created.  */
    private var output: Object? = null

    /** Absolute pathname of file on web server.  */
    private var file: Resource? = null

    /**
     * Applies only to Solaris and HP-UX. Permissions. Octal values of UNIX chmod command. Assigned to
     * owner, group, and other, respectively.
     */
    private var mode = -1

    /** Name of variable to contain contents of text file.  */
    private var variable: String? = null

    /** Name of form field used to select the file.  */
    private var filefield: String? = null

    /** Character set name for the file contents.  */
    private var charset: CharSet? = null

    /** Yes: appends newline character to text written to file  */
    private var addnewline = true
    private var fixnewline = true

    /**
     * One attribute (Windows) or a comma-delimited list of attributes (other platforms) to set on the
     * file. If omitted, the file's attributes are maintained.
     */
    private var attributes: String? = null

    /**
     * Absolute pathname of file on web server. On Windows, use backward slashes; on UNIX, use forward
     * slashes.
     */
    private var source: Resource? = null

    /** Action to take if filename is the same as that of a file in the directory.  */
    private var nameconflict: Int = NAMECONFLICT_UNDEFINED

    /**
     * Limits the MIME types to accept. Comma-delimited list. For example, to permit JPG and Microsoft
     * Word file uploads: accept = "image/jpg, application/msword" The browser uses file extension to
     * determine file type.
     */
    private var accept: String? = null
    private var strict = true
    private var createPath = false
    private var result: String? = null
    private var securityManager: lucee.runtime.security.SecurityManager? = null
    private var serverPassword: String? = null
    private var acl: Object? = null
    private var cachedWithin: Object? = null
    private var allowedExtensions: ResourceFilter? = null
    private var blockedExtensions: ResourceFilter? = null
    @Override
    fun release() {
        super.release()
        acl = null
        action = ACTION_UNDEFINED
        actionValue = null
        strDestination = null
        output = null
        file = null
        mode = -1
        variable = null
        filefield = null
        charset = null
        addnewline = true
        fixnewline = true
        attributes = null
        source = null
        nameconflict = NAMECONFLICT_UNDEFINED
        accept = null
        allowedExtensions = null
        blockedExtensions = null
        strict = true
        createPath = false
        securityManager = null
        result = null
        serverPassword = null
        cachedWithin = null
    }

    fun setCachedwithin(cachedwithin: Object?) {
        if (StringUtil.isEmpty(cachedwithin)) return
        cachedWithin = cachedwithin
    }

    /**
     * set the value action Type of file manipulation that the tag performs.
     *
     * @param strAction value to set
     */
    @Throws(ApplicationException::class)
    fun setAction(strAction: String?) {
        var strAction = strAction
        actionValue = strAction
        strAction = strAction.toLowerCase()
        action = if (strAction!!.equals("move") || strAction.equals("rename")) ACTION_MOVE else if (strAction.equals("copy")) ACTION_COPY else if (strAction.equals("delete")) ACTION_DELETE else if (strAction.equals("read")) ACTION_READ else if (strAction.equals("readbinary")) ACTION_READ_BINARY else if (strAction.equals("write")) ACTION_WRITE else if (strAction.equals("append")) ACTION_APPEND else if (strAction.equals("upload")) ACTION_UPLOAD else if (strAction.equals("uploadall")) ACTION_UPLOAD_ALL else if (strAction.equals("info")) ACTION_INFO else if (strAction.equals("touch")) ACTION_TOUCH else throw ApplicationException("Invalid value [$strAction] for attribute action",
                "supported actions are: [info,move,rename,copy,delete,read,readbinary,write,append,upload,uploadall,touch]")
    }

    /**
     * set the value destination Absolute pathname of directory or file on web server.
     *
     * @param destination value to set
     */
    fun setDestination(destination: String?) {
        strDestination = destination // ResourceUtil.toResourceNotExisting(pageContext ,destination);
    }

    /**
     * set the value output Content of the file to be created.
     *
     * @param output value to set
     */
    fun setOutput(output: Object?) {
        if (output == null) this.output = "" else this.output = output
    }

    /**
     * set the value file Absolute pathname of file on web server.
     *
     * @param file value to set
     */
    fun setFile(file: String?) {
        this.file = ResourceUtil.toResourceNotExisting(pageContext, file)
    }

    /**
     * set the value mode Applies only to Solaris and HP-UX. Permissions. Octal values of UNIX chmod
     * command. Assigned to owner, group, and other, respectively.
     *
     * @param mode value to set
     * @throws PageException
     */
    @Throws(PageException::class)
    fun setMode(mode: String?) {
        this.mode = toMode(mode)
    }

    /**
     * set the value variable Name of variable to contain contents of text file.
     *
     * @param variable value to set
     */
    fun setVariable(variable: String?) {
        this.variable = variable
    }

    /**
     * set the value filefield Name of form field used to select the file.
     *
     * @param filefield value to set
     */
    fun setFilefield(filefield: String?) {
        this.filefield = filefield
    }

    /**
     * set the value charset Character set name for the file contents.
     *
     * @param charset value to set
     */
    fun setCharset(charset: String?) {
        if (StringUtil.isEmpty(charset)) return
        this.charset = CharsetUtil.toCharSet(charset.trim())
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

    fun setServerpassword(serverPassword: String?) {
        this.serverPassword = serverPassword
    }

    /**
     * set the value addnewline Yes: appends newline character to text written to file
     *
     * @param addnewline value to set
     */
    fun setAddnewline(addnewline: Boolean) {
        this.addnewline = addnewline
    }

    @Throws(PageException::class)
    fun setAllowedextensions(oExtensions: Object?) {
        if (StringUtil.isEmpty(oExtensions)) return
        allowedExtensions = FileUtil.toExtensionFilter(oExtensions)
    }

    @Throws(PageException::class)
    fun setBlockedextensions(oExtensions: Object?) {
        if (StringUtil.isEmpty(oExtensions)) return
        blockedExtensions = FileUtil.toExtensionFilter(oExtensions)
    }

    /**
     * set the value attributes One attribute (Windows) or a comma-delimited list of attributes (other
     * platforms) to set on the file. If omitted, the file's attributes are maintained.
     *
     * @param attributes value to set
     */
    fun setAttributes(attributes: String?) {
        this.attributes = attributes
    }

    /**
     * set the value source Absolute pathname of file on web server. On Windows, use backward slashes;
     * on UNIX, use forward slashes.
     *
     * @param source value to set
     */
    fun setSource(source: String?) {
        this.source = ResourceUtil.toResourceNotExisting(pageContext, source)
    }

    /**
     * set the value nameconflict Action to take if filename is the same as that of a file in the
     * directory.
     *
     * @param nameconflict value to set
     * @throws ApplicationException
     */
    @Throws(ApplicationException::class)
    fun setNameconflict(nameconflict: String?) {
        this.nameconflict = FileUtil.toNameConflict(nameconflict)
    }

    /**
     * set the value accept Limits the MIME types to accept. Comma-delimited list. For example, to
     * permit JPG and Microsoft Word file uploads: accept = "image/jpg, application/msword" The browser
     * uses file extension to determine file type.
     *
     * @param accept value to set
     */
    fun setAccept(accept: String?) {
        this.accept = accept
    }

    fun setStrict(strict: Boolean) {
        this.strict = strict
    }

    fun setCreatepath(createPath: Boolean) {
        this.createPath = createPath
    }

    /**
     * @param result The result to set.
     */
    fun setResult(result: String?) {
        this.result = result
    }

    @Override
    @Throws(PageException::class)
    fun doStartTag(): Int {
        if (charset == null) charset = CharsetUtil.toCharSet((pageContext as PageContextImpl?).getResourceCharset())
        securityManager = pageContext.getConfig().getSecurityManager()
        when (action) {
            ACTION_MOVE -> actionMove(pageContext, securityManager, source, strDestination, nameconflict, serverPassword, acl, mode, attributes)
            ACTION_COPY -> actionCopy(pageContext, securityManager, source, strDestination, nameconflict, serverPassword, acl, mode, attributes)
            ACTION_DELETE -> actionDelete()
            ACTION_READ -> actionRead(false)
            ACTION_READ_BINARY -> actionRead(true)
            ACTION_UPLOAD -> actionUpload()
            ACTION_UPLOAD_ALL -> actionUploadAll()
            ACTION_INFO -> actionInfo()
            ACTION_TOUCH -> actionTouch(pageContext, securityManager, file, serverPassword, createPath, acl, mode, attributes)
            ACTION_UNDEFINED -> throw ApplicationException("Missing attribute action") // should never happens
            else -> return EVAL_BODY_BUFFERED
        }
        return SKIP_BODY
    }

    @Override
    @Throws(ApplicationException::class)
    fun doAfterBody(): Int {
        if (action == ACTION_APPEND || action == ACTION_WRITE) {
            val body: String = bodyContent.getString()
            if (!StringUtil.isEmpty(body)) {
                if (!StringUtil.isEmpty(output)) throw ApplicationException("If a body is defined for the tag [file], the attribute [output] is not allowed")
                output = body
            }
        }
        return SKIP_BODY
    }

    @Override
    @Throws(PageException::class)
    fun doEndTag(): Int {
        when (action) {
            ACTION_APPEND -> actionAppend()
            ACTION_WRITE -> actionWrite()
        }
        return EVAL_PAGE
    }

    fun hasBody(hasBody: Boolean) {
        if (output == null && hasBody) output = ""
    }

    /**
     * copy source file to destination file or path
     *
     * @throws PageException
     */
    @Throws(PageException::class)
    private fun actionDelete() {
        checkFile(pageContext, securityManager, file, serverPassword, false, false, false, false)
        setACL(pageContext, file, acl)
        try {
            if (!file.delete()) throw ApplicationException("Can't delete file [$file]")
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
            throw ApplicationException(t.getMessage())
        }
    }

    /**
     * read source file
     *
     * @throws PageException
     */
    @Throws(PageException::class)
    private fun actionRead(isBinary: Boolean) {
        if (variable == null) throw ApplicationException("Attribute [variable] is required for tag [file], when the action is [" + actionValue + "]",
                "Required attributes for action [" + actionValue + "] is [file, variable]")
        if (file == null) throw ApplicationException("Attribute [file] is required for tag [file], when the action is [" + actionValue + "]",
                "Required attributes for action [" + actionValue + "] is [file, variable]")

        // check if we can use cache
        if (StringUtil.isEmpty(cachedWithin)) {
            val tmp: Object = (pageContext as PageContextImpl?).getCachedWithin(ConfigWeb.CACHEDWITHIN_FILE)
            if (tmp != null) setCachedwithin(tmp)
        }
        val cacheId = createCacheId(isBinary)
        var cacheHandler: CacheHandler? = null
        if (cachedWithin != null) {
            cacheHandler = pageContext.getConfig().getCacheHandlerCollection(Config.CACHE_TYPE_FILE, null).getInstanceMatchingObject(cachedWithin, null)
            if (cacheHandler is CacheHandlerPro) {
                val cacheItem: CacheItem = (cacheHandler as CacheHandlerPro?).get(pageContext, cacheId, cachedWithin)
                if (cacheItem is FileCacheItem) {
                    pageContext.setVariable(variable, (cacheItem as FileCacheItem).getData())
                    return
                }
            } else if (cacheHandler != null) { // TODO this else block can be removed when all cache handlers implement CacheHandlerPro
                val cacheItem: CacheItem = cacheHandler.get(pageContext, cacheId)
                if (cacheItem is FileCacheItem) {
                    pageContext.setVariable(variable, (cacheItem as FileCacheItem).getData())
                    return
                }
            }
        }

        // cache not found, process and cache result if needed
        checkFile(pageContext, securityManager, file, serverPassword, false, false, true, false)
        try {
            val start: Long = System.nanoTime()
            val data: Object = if (isBinary) IOUtil.toBytes(file) else IOUtil.toString(file, CharsetUtil.toCharset(charset))
            pageContext.setVariable(variable, data)
            if (cacheHandler != null) cacheHandler.set(pageContext, cacheId, cachedWithin, FileCacheItem.getInstance(file.getAbsolutePath(), data, System.nanoTime() - start))
        } catch (e: IOException) {
            throw ApplicationException("Can't read file [" + file.toString().toString() + "]", e.getMessage())
        }
    }

    private fun createCacheId(binary: Boolean): String? {
        return CacheHandlerCollectionImpl.createId(file, binary)
    }

    /**
     * write to the source file
     *
     * @throws PageException
     */
    @Throws(PageException::class)
    private fun actionWrite() {
        if (output == null) throw ApplicationException("Attribute [output] is required for tag [file], when the action is [" + actionValue + "]",
                "Action [" + actionValue + "] requires a tag body or attribute [output]")
        val created = checkFile(pageContext, securityManager, file, serverPassword, createPath, true, false, true)
        if (file.exists() && !created) {
            // Error
            if (nameconflict == NAMECONFLICT_ERROR) throw ApplicationException("Destination file [$file] already exists") else if (nameconflict == NAMECONFLICT_SKIP) return else if (nameconflict == NAMECONFLICT_OVERWRITE) file.delete() else if (nameconflict == NAMECONFLICT_MAKEUNIQUE) file = makeUnique(file)
        }
        try {
            if (output is InputStream) {
                IOUtil.copy(output as InputStream?, file, false)
            } else if (Decision.isCastableToBinary(output, false)) {
                IOUtil.copy(ByteArrayInputStream(Caster.toBinary(output)), file, true)
            } else {
                var content: String? = Caster.toString(output)
                if (fixnewline) content = doFixNewLine(content)
                if (addnewline) content += SystemUtil.getOSSpecificLineSeparator()
                IOUtil.write(file, content, CharsetUtil.toCharset(charset), false)
            }
        } catch (e: UnsupportedEncodingException) {
            throw ApplicationException("Unsupported Charset Definition [$charset]", e.getMessage())
        } catch (e: IOException) {
            throw ApplicationException("Can't write file [" + file.getAbsolutePath().toString() + "]", e.getMessage())
        }
        setMode(file, mode)
        setAttributes(file, attributes)
        setACL(pageContext, file, acl)
    }

    /**
     * append data to source file
     *
     * @throws PageException
     */
    @Throws(PageException::class)
    private fun actionAppend() {
        if (output == null) throw ApplicationException("Attribute [output] is required for tag [file], when the action is [" + actionValue + "]",
                "Action [" + actionValue + "] requires a tag body or attribute [output]")
        checkFile(pageContext, securityManager, file, serverPassword, createPath, true, false, true)
        try {
            if (!file.exists()) file.createNewFile()
            var content: String? = Caster.toString(output)
            if (fixnewline) content = doFixNewLine(content)
            if (addnewline) content += SystemUtil.getOSSpecificLineSeparator()
            IOUtil.write(file, content, CharsetUtil.toCharset(charset), true)
        } catch (e: UnsupportedEncodingException) {
            throw ApplicationException("Unsupported Charset Definition [$charset]", e.getMessage())
        } catch (e: IOException) {
            throw ApplicationException("Can't append file", e.getMessage())
        }
        setMode(file, mode)
        setAttributes(file, attributes)
        setACL(pageContext, file, acl)
    }

    private fun doFixNewLine(content: String?): String? {
        // TODO replace new line with system new line
        return content
    }

    /**
     * list all files and directories inside a directory
     *
     * @throws PageException
     */
    @Throws(PageException::class)
    private fun actionInfo() {
        if (variable == null) throw ApplicationException("Attribute [variable] is required for tag [file], when the action is [" + actionValue + "]",
                "Required attribute for action [" + actionValue + "] is [file, variable]")
        pageContext.setVariable(variable, getInfo(pageContext, file, serverPassword))
    }

    /**
     * read source file
     *
     * @throws PageException
     */
    @Throws(PageException::class)
    fun actionUpload() {
        val item: FormItem? = getFormItem(pageContext, filefield)
        val cffile: Struct? = _actionUpload(pageContext, securityManager, item, strDestination, nameconflict, accept, allowedExtensions, blockedExtensions, strict, mode, attributes, acl,
                serverPassword)
        if (StringUtil.isEmpty(result)) {
            pageContext.undefinedScope().set(KeyConstants._file, cffile)
            pageContext.undefinedScope().set("cffile", cffile)
        } else {
            pageContext.setVariable(result, cffile)
        }
    }

    @Throws(PageException::class)
    fun actionUploadAll() {
        val arr: Array? = actionUploadAll(pageContext, securityManager, strDestination, nameconflict, accept, allowedExtensions, blockedExtensions, strict, mode, attributes, acl,
                serverPassword)
        if (StringUtil.isEmpty(result)) {
            val sct: Struct?
            if (arr != null && arr.size() > 0) sct = arr.getE(1) as Struct else sct = StructImpl()
            pageContext.undefinedScope().set(KeyConstants._file, sct)
            pageContext.undefinedScope().set("cffile", sct)
        } else {
            pageContext.setVariable(result, arr)
        }
    }

    /**
     * @param fixnewline the fixnewline to set
     */
    fun setFixnewline(fixnewline: Boolean) {
        this.fixnewline = fixnewline
    }

    companion object {
        private const val ACTION_UNDEFINED = 0
        private const val ACTION_MOVE = 1
        private const val ACTION_WRITE = 2
        private const val ACTION_APPEND = 3
        private const val ACTION_READ = 4
        private const val ACTION_UPLOAD = 5
        private const val ACTION_UPLOAD_ALL = 6
        private const val ACTION_COPY = 7
        private const val ACTION_INFO = 8
        private const val ACTION_TOUCH = 9
        private const val ACTION_DELETE = 10
        private const val ACTION_READ_BINARY = 11

        // private static final Key SET_ACL = KeyImpl.intern("setACL");
        private val DETAIL: String? = ("You can set a [allowedExtension] and a [blockedExtension] list as an argument/attribute with the tag [cffile] and the functions [fileUpload] and [fileUploadAll]. "
                + "In addition you can configure this via the Application.cfc, [this.blockedExtForFileUpload] property, the [" + SystemUtil.SETTING_UPLOAD_EXT_BLOCKLIST
                + "] System property or the [" + SystemUtil.convertSystemPropToEnvVar(SystemUtil.SETTING_UPLOAD_EXT_BLOCKLIST)
                + "] Environment variable to allow this type of file to be uploaded.")
        private var actionValue: String? = null
        @Throws(PageException::class)
        fun toMode(mode: String?): Int {
            return if (StringUtil.isEmpty(mode, true)) -1 else try {
                ModeUtil.toOctalMode(mode)
            } catch (e: IOException) {
                throw Caster.toPageException(e)
            }
        }

        /**
         * move source file to destination path or file
         *
         * @throws PageException
         */
        @Throws(PageException::class)
        fun actionMove(pageContext: PageContext?, securityManager: lucee.runtime.security.SecurityManager?, source: Resource?, strDestination: String?, nameconflict: Int,
                       serverPassword: String?, acl: Object?, mode: Int, attributes: String?) {
            var nameconflict = nameconflict
            if (nameconflict == NAMECONFLICT_UNDEFINED) nameconflict = NAMECONFLICT_OVERWRITE
            if (source == null) throw ApplicationException("Attribute [source] is required for tag [file], when the action is [" + actionValue + "]",
                    "Required attributes for action [" + actionValue + "] is [source, destination]")
            if (StringUtil.isEmpty(strDestination)) throw ApplicationException("Attribute [destination] is required for tag [file], when the action is [" + actionValue + "]",
                    "Required attributes for action [" + actionValue + "] is [source, destination]")
            var destination: Resource? = toDestination(pageContext, strDestination, source)
            securityManager.checkFileLocation(pageContext.getConfig(), source, serverPassword)
            securityManager.checkFileLocation(pageContext.getConfig(), destination, serverPassword)
            if (source.equals(destination)) return

            // source
            if (!source.exists()) throw ApplicationException("Source file [" + source.toString().toString() + "] doesn't exist") else if (!source.isFile()) throw ApplicationException("Source file [" + source.toString().toString() + "] isn't a file")
            // else if (!source.isReadable() || !source.isWriteable()) throw new ApplicationException("no access
            // to source file [" + source.toString() + "]");

            // destination
            if (destination.isDirectory()) destination = destination.getRealResource(source.getName())
            if (destination.exists()) {
                // SKIP
                if (nameconflict == NAMECONFLICT_SKIP) return else if (nameconflict == NAMECONFLICT_OVERWRITE) destination.delete() else if (nameconflict == NAMECONFLICT_MAKEUNIQUE) destination = makeUnique(destination) else if (nameconflict == NAMECONFLICT_FORCEUNIQUE) destination = forceUnique(destination) else throw ApplicationException("Destination file [" + destination.toString().toString() + "] already exists")
            }
            try {
                source.moveTo(destination)
            } catch (t: Throwable) {
                ExceptionUtil.rethrowIfNecessary(t)
                throw ApplicationException(t.getMessage())
            }
            setMode(destination, mode)
            setAttributes(destination, attributes)
            setACL(pageContext, destination, acl)
        }

        private fun toDestination(pageContext: PageContext?, path: String?, source: Resource?): Resource? {
            if (source != null && path.indexOf(File.separatorChar) === -1 && path.indexOf('/') === -1 && path.indexOf('\\') === -1) {
                val p: Resource = source.getParentResource()
                if (p != null) return p.getRealResource(path)
            }
            return ResourceUtil.toResourceNotExisting(pageContext, path)
        }

        /**
         * copy source file to destination file or path
         *
         * @throws PageException
         */
        @Throws(PageException::class)
        fun actionCopy(pageContext: PageContext?, securityManager: lucee.runtime.security.SecurityManager?, source: Resource?, strDestination: String?, nameconflict: Int,
                       serverPassword: String?, acl: Object?, mode: Int, attributes: String?) {
            var nameconflict = nameconflict
            if (nameconflict == NAMECONFLICT_UNDEFINED) nameconflict = NAMECONFLICT_OVERWRITE
            if (source == null) throw ApplicationException("Attribute [source] is required for tag [file], when the action is [" + actionValue + "]",
                    "Required attributes for action [" + actionValue + "] is [source, destination]")
            if (StringUtil.isEmpty(strDestination)) throw ApplicationException("Attribute [destination] is required for tag [file], when the action is [" + actionValue + "]",
                    "Required attributes for action [" + actionValue + "] is [source, destination]")
            var destination: Resource? = toDestination(pageContext, strDestination, source)
            securityManager.checkFileLocation(pageContext.getConfig(), source, serverPassword)
            securityManager.checkFileLocation(pageContext.getConfig(), destination, serverPassword)

            // source
            if (!source.exists()) throw ApplicationException("Source file [" + source.toString().toString() + "] doesn't exist") else if (!source.isFile()) throw ApplicationException("Source file [" + source.toString().toString() + "] is not a file") else if (!source.canRead()) throw ApplicationException("Access Denied to source file [" + source.toString().toString() + "]")

            // destination
            if (destination.isDirectory()) destination = destination.getRealResource(source.getName())
            if (destination.exists()) {
                // SKIP
                if (nameconflict == NAMECONFLICT_SKIP) return else if (nameconflict == NAMECONFLICT_OVERWRITE) destination.delete() else if (nameconflict == NAMECONFLICT_MAKEUNIQUE) destination = makeUnique(destination) else if (nameconflict == NAMECONFLICT_FORCEUNIQUE) destination = forceUnique(destination) else throw ApplicationException("Destination file [" + destination.toString().toString() + "] already exists")
            }
            try {
                IOUtil.copy(source, destination)
            } catch (e: IOException) {
                val ae = ApplicationException("Can't copy file [$source] to [$destination]", e.getMessage())
                ae.setStackTrace(e.getStackTrace())
                throw ae
            }
            setMode(destination, mode)
            setAttributes(destination, attributes)
            setACL(pageContext, destination, acl)
        }

        @Throws(PageException::class)
        private fun setACL(pc: PageContext?, res: Resource?, acl: Object?) {
            val scheme: String = res.getResourceProvider().getScheme()
            if ("s3".equalsIgnoreCase(scheme)) {
                Directory.setS3Attrs(pc, res, acl, null)
            }
        }

        private fun makeUnique(res: Resource?): Resource? {
            var res: Resource? = res
            val name: String = ResourceUtil.getName(res)
            var ext: String = ResourceUtil.getExtension(res, "")
            if (!StringUtil.isEmpty(ext)) ext = ".$ext"
            while (res.exists()) {
                res = res.getParentResource().getRealResource(name + HashUtil.create64BitHashAsString(CreateUUID.invoke(), Character.MAX_RADIX) + ext)
            }
            return res
        }

        private fun forceUnique(res: Resource?): Resource? {
            var res: Resource? = res
            val name: String = ResourceUtil.getName(res)
            var ext: String = ResourceUtil.getExtension(res, "")
            if (!StringUtil.isEmpty(ext)) ext = ".$ext"
            while (res.exists()) {
                res = res.getParentResource().getRealResource(name + "_" + HashUtil.create64BitHashAsString(CreateUUID.invoke(), Character.MAX_RADIX) + ext)
            }
            return res
        }

        /**
         * write to the source file
         *
         * @param attributes
         * @param mode
         * @param acl
         * @param serverPassword, booleancreatePath
         * @throws PageException
         */
        @Throws(PageException::class)
        fun actionTouch(pageContext: PageContext?, securityManager: SecurityManager?, file: Resource?, serverPassword: String?, createPath: Boolean, acl: Object?, mode: Int,
                        attributes: String?) {
            checkFile(pageContext, securityManager, file, serverPassword, createPath, true, true, true)
            try {
                ResourceUtil.touch(file)
            } catch (e: IOException) {
                throw ApplicationException("Failed to touch file [" + file.getAbsolutePath().toString() + "]", e.getMessage())
            }
            setMode(file, mode)
            setAttributes(file, attributes)
            setACL(pageContext, file, acl)
        }

        @Throws(PageException::class)
        fun getInfo(pc: PageContext?, file: Resource?, serverPassword: String?): Struct? {
            val sm: SecurityManager = pc.getConfig().getSecurityManager()
            checkFile(pc, sm, file, serverPassword, false, false, false, false)
            val files = File(Caster.toString(file))
            val attr: BasicFileAttributes
            val sct: Struct = StructImpl()

            // fill data to query
            sct.setEL(KeyConstants._path, file.getAbsolutePath())
            sct.setEL(KeyConstants._name, file.getName())
            sct.setEL(KeyConstants._size, Long.valueOf(file.length()))
            sct.setEL(KeyConstants._type, if (file.isDirectory()) "Dir" else "File")
            if (file is File) sct.setEL("execute", (file as File?).canExecute())
            sct.setEL("read", file.canRead())
            sct.setEL("write", file.canWrite())
            try {
                attr = Files.readAttributes(files.toPath(), BasicFileAttributes::class.java)
                sct.set("fileCreated", DateTimeImpl(pc, attr.creationTime().toMillis(), false))
            } catch (e: Exception) {
            }
            sct.setEL("dateLastModified", DateTimeImpl(pc, file.lastModified(), false))
            sct.setEL("attributes", getFileAttribute(file))
            if (SystemUtil.isUnix()) sct.setEL(KeyConstants._mode, ModeObjectWrap(file))
            try {
                sct.setEL(KeyConstants._checksum, Hash.md5(file))
            } catch (t: Throwable) {
                ExceptionUtil.rethrowIfNecessary(t)
            }

            /*
		 * try { BufferedImage bi = ImageUtil.toBufferedImage(file, null); if(bi!=null) { Struct img =new
		 * StructImpl(); img.setEL(KeyConstants._width,Double.valueOf(bi.getWidth()));
		 * img.setEL(KeyConstants._height,Double.valueOf(bi.getHeight())); sct.setEL(KeyConstants._img,img); } }
		 * catch(Exception e) {}
		 */return sct
        }

        private fun getFileAttribute(file: Resource?): String? {
            return if (file.exists() && !file.canWrite()) "R".concat(if (file.isHidden()) "H" else "") else if (file.isHidden()) "H" else ""
        }

        @Throws(PageException::class)
        fun actionUpload(pageContext: PageContext?, securityManager: lucee.runtime.security.SecurityManager?, filefield: String?, strDestination: String?, nameconflict: Int,
                         accept: String?, allowedExtensions: ResourceFilter?, blockedExtensions: ResourceFilter?, strict: Boolean, mode: Int, attributes: String?, acl: Object?, serverPassword: String?): Struct? {
            val item: FormItem? = getFormItem(pageContext, filefield)
            return _actionUpload(pageContext, securityManager, item, strDestination, nameconflict, accept, allowedExtensions, blockedExtensions, strict, mode, attributes, acl,
                    serverPassword)
        }

        @Throws(PageException::class)
        fun actionUploadAll(pageContext: PageContext?, securityManager: lucee.runtime.security.SecurityManager?, strDestination: String?, nameconflict: Int, accept: String?,
                            allowedExtensions: ResourceFilter?, blockedExtensions: ResourceFilter?, strict: Boolean, mode: Int, attributes: String?, acl: Object?, serverPassword: String?): Array? {
            val items: Array<FormItem?>? = getFormItems(pageContext)
            var sct: Struct? = null
            val arr: Array = ArrayImpl()
            for (i in items.indices) {
                sct = _actionUpload(pageContext, securityManager, items!![i], strDestination, nameconflict, accept, allowedExtensions, blockedExtensions, strict, mode, attributes, acl,
                        serverPassword)
                arr.appendEL(sct)
            }
            return arr
        }

        @Throws(PageException::class)
        private fun _actionUpload(pageContext: PageContext?, securityManager: lucee.runtime.security.SecurityManager?, formItem: FormItem?, strDestination: String?, nameconflict: Int,
                                  accept: String?, allowedExtensions: ResourceFilter?, blockedExtensions: ResourceFilter?, strict: Boolean, mode: Int, attributes: String?, acl: Object?, serverPassword: String?): Struct? {
            var nameconflict = nameconflict
            if (nameconflict == NAMECONFLICT_UNDEFINED) nameconflict = NAMECONFLICT_ERROR
            var fileWasRenamed = false
            val fileWasAppended = false
            var fileExisted = false
            var fileWasOverwritten = false

            // set cffile struct
            val cffile: Struct = StructImpl()
            val length: Long = formItem.getResource().length()
            cffile.set("timecreated", DateTimeImpl(pageContext.getConfig()))
            cffile.set("timelastmodified", DateTimeImpl(pageContext.getConfig()))
            cffile.set("datelastaccessed", DateImpl(pageContext))
            cffile.set("oldfilesize", Long.valueOf(length))
            cffile.set("filesize", Long.valueOf(length))

            // client file
            var strClientFile: String? = formItem.getName()
            while (strClientFile.indexOf('\\') !== -1) strClientFile = strClientFile.replace('\\', '/')
            val clientFile: Resource = pageContext.getConfig().getResource(strClientFile)
            val clientFileName: String = clientFile.getName()

            // content type
            val contentType: String = ResourceUtil.getMimeType(formItem.getResource(), clientFile.getName(), formItem.getContentType())
            cffile.set("contenttype", ListFirst.call(pageContext, contentType, "/", false, 1))
            cffile.set("contentsubtype", ListLast.call(pageContext, contentType, "/", false, 1))

            // check file type
            checkContentType(contentType, accept, allowedExtensions, blockedExtensions, clientFile, strict, pageContext.getApplicationContext())
            cffile.set("clientdirectory", getParent(clientFile))
            cffile.set("clientfile", clientFile.getName())
            cffile.set("clientfileext", ResourceUtil.getExtension(clientFile, ""))
            cffile.set("clientfilename", ResourceUtil.getName(clientFile))

            // check destination
            if (StringUtil.isEmpty(strDestination)) throw ApplicationException("Attribute [destination] is required for tag [file], when action is [" + actionValue + "]")
            var destination: Resource? = toDestination(pageContext, strDestination, null)
            securityManager.checkFileLocation(pageContext.getConfig(), destination, serverPassword)
            if (destination.isDirectory()) destination = destination.getRealResource(clientFileName) else if (!destination.exists() && (strDestination.endsWith("/") || strDestination.endsWith("\\"))) destination = destination.getRealResource(clientFileName) else if (!clientFileName.equalsIgnoreCase(destination.getName())) {
                if (ResourceUtil.getExtension(destination, null) == null) destination = destination.getRealResource(clientFileName) else fileWasRenamed = true
            }

            // check parent destination -> directory of the desinatrion
            val parentDestination: Resource = destination.getParentResource()
            if (!parentDestination.exists()) {
                val pp: Resource = parentDestination.getParentResource()
                if (pp == null || !pp.exists()) throw ApplicationException("Attribute [destination] has an invalid value [$destination], directory [$parentDestination] doesn't exist")
                try {
                    parentDestination.createDirectory(true)
                } catch (e: IOException) {
                    throw Caster.toPageException(e)
                }
            } else if (!parentDestination.canWrite()) throw ApplicationException("can't write to destination directory [$parentDestination], no access to write")

            // set server variables
            cffile.set("serverdirectory", getParent(destination))
            cffile.set("serverfile", destination.getName())
            cffile.set("serverfileext", ResourceUtil.getExtension(destination, null))
            cffile.set("serverfilename", ResourceUtil.getName(destination))
            cffile.set("attemptedserverfile", destination.getName())

            // check nameconflict
            if (destination.exists()) {
                fileExisted = true
                if (nameconflict == NAMECONFLICT_ERROR) {
                    throw ApplicationException("Destination file [$destination] already exists")
                } else if (nameconflict == NAMECONFLICT_SKIP) {
                    cffile.set("fileexisted", Caster.toBoolean(fileExisted))
                    cffile.set("filewasappended", Boolean.FALSE)
                    cffile.set("filewasoverwritten", Boolean.FALSE)
                    cffile.set("filewasrenamed", Boolean.FALSE)
                    cffile.set("filewassaved", Boolean.FALSE)
                    return cffile
                } else if (nameconflict == NAMECONFLICT_MAKEUNIQUE) {
                    destination = makeUnique(destination)
                    fileWasRenamed = true

                    // if(fileWasRenamed) {
                    cffile.set("serverdirectory", getParent(destination))
                    cffile.set("serverfile", destination.getName())
                    cffile.set("serverfileext", ResourceUtil.getExtension(destination, ""))
                    cffile.set("serverfilename", ResourceUtil.getName(destination))
                    cffile.set("attemptedserverfile", destination.getName())
                    // }
                } else if (nameconflict == NAMECONFLICT_FORCEUNIQUE) {
                    destination = forceUnique(destination)
                    fileWasRenamed = true
                    cffile.set("serverdirectory", getParent(destination))
                    cffile.set("serverfile", destination.getName())
                    cffile.set("serverfileext", ResourceUtil.getExtension(destination, ""))
                    cffile.set("serverfilename", ResourceUtil.getName(destination))
                    cffile.set("attemptedserverfile", destination.getName())
                } else if (nameconflict == NAMECONFLICT_OVERWRITE) {
                    // fileWasAppended=true;
                    fileWasOverwritten = true
                    if (!destination.delete()) if (destination.exists()) throw ApplicationException("Can't delete destination file [$destination]")
                }
                // for "overwrite" no action is neded
            }
            try {
                destination.createNewFile()
                IOUtil.copy(formItem.getResource(), destination)
            } catch (t: Throwable) {
                ExceptionUtil.rethrowIfNecessary(t)
                throw Caster.toPageException(t)
            }

            // Set cffile/file struct
            cffile.set("fileexisted", Caster.toBoolean(fileExisted))
            cffile.set("filewasappended", Caster.toBoolean(fileWasAppended))
            cffile.set("filewasoverwritten", Caster.toBoolean(fileWasOverwritten))
            cffile.set("filewasrenamed", Caster.toBoolean(fileWasRenamed))
            cffile.set("filewassaved", Boolean.TRUE)
            setMode(destination, mode)
            setAttributes(destination, attributes)
            setACL(pageContext, destination, acl)
            return cffile
        }

        /**
         * check if the content type is permitted
         *
         * @param contentType
         * @throws PageException
         */
        @Throws(PageException::class)
        private fun checkContentType(contentType: String?, accept: String?, allowedExtensions: ResourceFilter?, blockedExtensions: ResourceFilter?, clientFile: Resource?, strict: Boolean,
                                     appContext: ApplicationContext?) {
            var ext: String? = ResourceUtil.getExtension(clientFile, "")

            // check extension
            if (!StringUtil.isEmpty(ext, true)) {
                var extensionAccepted = false
                ext = FileUtil.toExtensions(ext)

                // allowed
                if (allowedExtensions != null) {
                    extensionAccepted = if (!allowedExtensions.accept(clientFile)) throw ApplicationException(
                            "Upload of files with extension [" + ext
                                    + "] is not permitted. The tag cffile/function fileUpload[All] only allows the following extensions in this context [" + allowedExtensions + "].",
                            DETAIL) else true
                }

                // blocked (when explicitly allowed we not have to check if blocked)
                if (!extensionAccepted) {
                    if (blockedExtensions != null) {
                        if (blockedExtensions.accept(clientFile)) {
                            throw ApplicationException("Upload of files with extension [" + ext
                                    + "] is not permitted. The tag cffile/function fileUpload[All] does not allow the following extensions in this context [" + blockedExtensions
                                    + "].", DETAIL)
                        } else extensionAccepted = Boolean.TRUE
                    } else {
                        var blocklistedTypes: String = (appContext as ApplicationContextSupport?).getBlockedExtForFileUpload()
                        if (StringUtil.isEmpty(blocklistedTypes)) blocklistedTypes = SystemUtil.getSystemPropOrEnvVar(SystemUtil.SETTING_UPLOAD_EXT_BLACKLIST, SystemUtil.DEFAULT_UPLOAD_EXT_BLOCKLIST)
                        if (StringUtil.isEmpty(blocklistedTypes)) blocklistedTypes = SystemUtil.getSystemPropOrEnvVar(SystemUtil.SETTING_UPLOAD_EXT_BLOCKLIST, SystemUtil.DEFAULT_UPLOAD_EXT_BLOCKLIST)
                        val filter = NotResourceFilter(ExtensionResourceFilter(blocklistedTypes))
                        if (!filter.accept(clientFile)) throw ApplicationException("Upload of files with extension [$ext] is not permitted.", DETAIL)
                    }
                }
            } else ext = null

            // mimetype
            if (StringUtil.isEmpty(accept, true)) return
            val mt: MimeType = MimeType.getInstance(contentType)
            var sub: MimeType
            val whishedTypes: Array = ListUtil.listToArrayRemoveEmpty(accept, ',')
            val len: Int = whishedTypes.size()
            for (i in 1..len) {
                var whishedType: String? = Caster.toString(whishedTypes.getE(i)).trim().toLowerCase()
                if (whishedType!!.equals("*")) return
                // check mimetype
                if (ListUtil.len(whishedType, "/", true) === 2) {
                    sub = MimeType.getInstance(whishedType)
                    if (mt.match(sub)) return
                }

                // check extension
                if (ext != null && !strict) {
                    if (whishedType.startsWith("*.")) whishedType = whishedType.substring(2)
                    if (whishedType.startsWith(".")) whishedType = whishedType.substring(1)
                    if (ext.equals(whishedType)) return
                }
            }
            if (strict && ListUtil.listContainsNoCase(StringUtil.emptyIfNull(accept), ".$ext", ",", false, false) !== -1) throw ApplicationException("When the value of the attribute STRICT is TRUE, only MIME types are allowed in the attribute(s): ACCEPT.",
                    " set [$accept] to MIME type.") else throw ApplicationException("The MIME type of the uploaded file [$contentType] was rejected by the server.", " Only the following type(s) are allowed, [" + StringUtil.emptyIfNull(accept).toString() + "].  Verify that you are uploading a file of the appropriate type. ")
        }

        /**
         * return fileItem matching to filefield definition or throw an exception
         *
         * @return FileItem
         * @throws ApplicationException
         */
        @Throws(PageException::class)
        private fun getFormItem(pageContext: PageContext?, filefield: String?): FormItem? {
            // check filefield
            if (StringUtil.isEmpty(filefield)) {
                val items: Array<FormItem?>? = getFormItems(pageContext)
                if (ArrayUtil.isEmpty(items)) throw ApplicationException("No uploaded files in found in Form")
                return items!![0]
            }
            val pe: PageException = pageContext.formScope().getInitException()
            if (pe != null) throw pe
            val upload: lucee.runtime.type.scope.Form = pageContext.formScope()
            val fileItem: FormItem = upload.getUploadResource(filefield)
            if (fileItem == null) {
                val items: Array<FormItem?> = upload.getFileItems()
                val sb = StringBuilder()
                for (i in items.indices) {
                    if (i != 0) sb.append(", ")
                    sb.append(items[i].getFieldName())
                }
                var add = "."
                if (sb.length() > 0) add = ", valid field names are [$sb]."
                if (pageContext.formScope().get(filefield, null) == null) throw ApplicationException("Form field [$filefield] is not a file field$add")
                throw ApplicationException("Form field [$filefield] doesn't exist or has no content$add")
            }
            return fileItem
        }

        @Throws(PageException::class)
        private fun getFormItems(pageContext: PageContext?): Array<FormItem?>? {
            val pe: PageException = pageContext.formScope().getInitException()
            if (pe != null) throw pe
            val scope: Form = pageContext.formScope()
            return scope.getFileItems()
        }

        private fun getParent(res: Resource?): String? {
            val parent: Resource = res.getParentResource() ?: return ""
            // print.out("res:"+res);
            // print.out("parent:"+parent);
            return ResourceUtil.getCanonicalPathEL(parent)
        }

        @Throws(PageException::class)
        private fun checkFile(pc: PageContext?, sm: SecurityManager?, file: Resource?, serverPassword: String?, createParent: Boolean, create: Boolean, canRead: Boolean,
                              canWrite: Boolean): Boolean {
            var created = false
            if (file == null) throw ApplicationException("Attribute [file] is required for tag [file], when the action is [" + actionValue + "]")
            sm.checkFileLocation(pc.getConfig(), file, serverPassword)
            if (!file.exists()) {
                if (create) {
                    val parent: Resource = file.getParentResource()
                    if (parent != null && !parent.exists()) {
                        if (createParent) parent.mkdirs() else throw ApplicationException("Parent directory for [$file] doesn't exist")
                    }
                    try {
                        created = true
                        file.createFile(false)
                    } catch (e: IOException) {
                        throw ApplicationException("Invalid file [$file]", e.getMessage())
                    }
                } else throw ApplicationException("Source file [" + file.toString().toString() + "] doesn't exist")
            } else if (!file.isFile()) throw ApplicationException("Source file [" + file.toString().toString() + "] is not a file") else if (canRead && !file.canRead()) throw ApplicationException("Read access denied to source file [" + file.toString().toString() + "]") else if (canWrite && !file.canWrite()) throw ApplicationException("Write access denied to source file [" + file.toString().toString() + "]")
            return created
        }

        /**
         * set attributes on file
         *
         * @param file
         * @throws PageException
         */
        @Throws(PageException::class)
        private fun setAttributes(file: Resource?, attributes: String?) {
            if (!SystemUtil.isWindows() || StringUtil.isEmpty(attributes)) return
            try {
                ResourceUtil.setAttribute(file, attributes)
            } catch (e: IOException) {
                throw ApplicationException("Can't change attributes of file [$file]", e.getMessage())
            }
        }

        /**
         * change mode of given file
         *
         * @param file
         * @throws ApplicationException
         */
        @Throws(ApplicationException::class)
        private fun setMode(file: Resource?, mode: Int) {
            if (mode == -1 || SystemUtil.isWindows()) return
            try {
                file.setMode(mode)
                // FileUtil.setMode(file,mode);
            } catch (e: IOException) {
                throw ApplicationException("Can't change mode of file [$file]", e.getMessage())
            }
        }
    }
}