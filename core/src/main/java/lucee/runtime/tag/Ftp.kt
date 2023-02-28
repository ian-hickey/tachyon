/**
 *
 * Copyright (c) 2014, the Railo Company Ltd. All rights reserved.
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

import java.io.IOException

/**
 *
 * Lets users implement File Transfer Protocol (FTP) operations.
 *
 *
 *
 */
class Ftp : TagImpl() {
    /*
	 * private static final Key = KeyImpl.getInstance(); private static final Key =
	 * KeyImpl.getInstance(); private static final Key = KeyImpl.getInstance(); private static final Key
	 * = KeyImpl.getInstance(); private static final Key = KeyImpl.getInstance(); private static final
	 * Key = KeyImpl.getInstance();
	 */
    private var pool: FTPPoolImpl? = null
    private var action: String? = null
    private var username: String? = null
    private var password: String? = null
    private var server: String? = null
    private var timeout = 30
    private var port = -1
    private var connectionName: String? = null
    private var retrycount = 1
    private var count = 0
    private var stoponerror = true
    private var passive = false
    private var name: String? = null
    private var directory: String? = null
    private var ASCIIExtensionList = ASCCI_EXT_LIST
    private var transferMode: Short = FTPConstant.TRANSFER_MODE_AUTO
    private var remotefile: String? = null
    private var localfile: String? = null
    private var failifexists = true
    private var existing: String? = null
    private var _new: String? = null
    private var item: String? = null
    private var result: String? = null
    private var proxyserver: String? = null
    private var proxyport = 80
    private var proxyuser: String? = null
    private var proxypassword: String? = ""
    private var fingerprint: String? = null
    private var secure = false
    private var recursive = false
    private var key: String? = null
    private var passphrase: String? = ""

    // private Struct cfftp=new StructImpl();
    @Override
    fun release() {
        super.release()
        pool = null
        action = null
        username = null
        password = null
        server = null
        timeout = 30
        port = -1
        connectionName = null
        proxyserver = null
        proxyport = 80
        proxyuser = null
        proxypassword = ""
        retrycount = 1
        count = 0
        stoponerror = true
        passive = false
        name = null
        directory = null
        ASCIIExtensionList = ASCCI_EXT_LIST
        transferMode = FTPConstant.TRANSFER_MODE_AUTO
        remotefile = null
        localfile = null
        failifexists = true
        existing = null
        _new = null
        item = null
        result = null
        fingerprint = null
        secure = false
        recursive = false
        key = null
        passphrase = ""
    }

    fun setAction(action: String?) {
        this.action = action.trim().toLowerCase()
    }

    /**
     * sets the attribute action
     *
     * @param action
     */
    fun setSecure(secure: Boolean) {
        this.secure = secure
    }

    @Override
    fun doStartTag(): Int {
        return SKIP_BODY
    }

    @Override
    @Throws(PageException::class)
    fun doEndTag(): Int {
        pool = (pageContext as PageContextImpl?).getFTPPool()
        var client: AFTPClient? = null

        // retries
        do {
            client = try {
                if (action!!.equals("open")) actionOpen() else if (action!!.equals("close")) actionClose() else if (action!!.equals("changedir")) actionChangeDir() else if (action!!.equals("createdir")) actionCreateDir() else if (action!!.equals("listdir")) actionListDir() else if (action!!.equals("removedir")) actionRemoveDir() else if (action!!.equals("getfile")) actionGetFile() else if (action!!.equals("putfile")) actionPutFile() else if (action!!.equals("rename")) actionRename() else if (action!!.equals("remove")) actionRemove() else if (action!!.equals("getcurrentdir")) actionGetCurrentDir() else if (action!!.equals("getcurrenturl")) actionGetCurrentURL() else if (action!!.equals("existsdir")) actionExistsDir() else if (action!!.equals("existsfile")) actionExistsFile() else if (action!!.equals("exists")) actionExists() else throw ApplicationException("Attribute [action] has an invalid value [$action]", "valid values are [open, close, listDir, createDir, removeDir, changeDir, getCurrentDir, "
                        + "getCurrentURL, existsFile, existsDir, exists, getFile, putFile, rename, remove]")
            } catch (ioe: IOException) {
                if (count++ < retrycount) continue
                throw Caster.toPageException(ioe)
            }
            if (client == null || !checkCompletion(client)) break
        } while (true)
        return EVAL_PAGE
    }

    /**
     * check if a directory exists or not
     *
     * @return FTPCLient
     * @throws PageException
     * @throws IOException
     */
    @Throws(PageException::class, IOException::class)
    private fun actionExistsDir(): AFTPClient? {
        required("directory", directory)
        val client: AFTPClient? = client
        val res = existsDir(client, directory)
        val cfftp: Struct? = writeCfftp(client)
        cfftp.setEL(RETURN_VALUE, Caster.toBoolean(res))
        cfftp.setEL(SUCCEEDED, Boolean.TRUE)
        stoponerror = false
        return client
    }

    /**
     * check if a file exists or not
     *
     * @return FTPCLient
     * @throws IOException
     * @throws PageException
     */
    @Throws(PageException::class, IOException::class)
    private fun actionExistsFile(): AFTPClient? {
        required("remotefile", remotefile)
        val client: AFTPClient? = client
        val file: FTPFile? = existsFile(client, remotefile, true)
        val cfftp: Struct? = writeCfftp(client)
        cfftp.setEL(RETURN_VALUE, Caster.toBoolean(file != null && file.isFile()))
        cfftp.setEL(SUCCEEDED, Boolean.TRUE)
        stoponerror = false
        return client
    }

    /**
     * check if a file or directory exists
     *
     * @return FTPCLient
     * @throws PageException
     * @throws IOException
     */
    @Throws(PageException::class, IOException::class)
    private fun actionExists(): AFTPClient? {
        required("item", item)
        val client: AFTPClient? = client
        val file: FTPFile? = existsFile(client, item, false)
        val cfftp: Struct? = writeCfftp(client)
        cfftp.setEL(RETURN_VALUE, Caster.toBoolean(file != null))
        cfftp.setEL(SUCCEEDED, Boolean.TRUE)
        return client
    }

    /*
	 * * check if file or directory exists if it exists return FTPFile otherwise null
	 * 
	 * @param client
	 * 
	 * @param strPath
	 * 
	 * @return FTPFile or null
	 * 
	 * @throws IOException
	 * 
	 * @throws PageException / private FTPFile exists(FTPClient client, String strPath) throws
	 * PageException, IOException { strPath=strPath.trim();
	 * 
	 * // get parent path FTPPath path=new FTPPath(client.printWorkingDirectory(),strPath); String
	 * name=path.getName(); print.out("path:"+name);
	 * 
	 * // when directory FTPFile[] files=null; try { files = client.listFiles(path.getPath()); } catch
	 * (IOException e) {}
	 * 
	 * if(files!=null) { for(int i=0;i<files.length;i++) { if(files[i].getName().equalsIgnoreCase(name))
	 * { return files[i]; } }
	 * 
	 * } return null; }
	 */
    @Throws(PageException::class, IOException::class)
    private fun existsFile(client: AFTPClient?, strPath: String?, isFile: Boolean): FTPFile? {
        var strPath = strPath
        strPath = strPath.trim()
        if (strPath!!.equals("/")) {
            val file = FTPFile()
            file.setName("/")
            file.setType(FTPFile.DIRECTORY_TYPE)
            return file
        }

        // get parent path
        val path = FTPPath(client, strPath)
        val p: String = path.getPath()
        val n: String = path.getName()
        strPath = p
        if ("//".equals(p)) strPath = "/"
        if (isFile) strPath += n

        // when directory
        var files: Array<FTPFile?>? = null
        try {
            files = client.listFiles(p)
        } catch (e: IOException) {
        }
        if (files != null) {
            for (i in files.indices) {
                if (files[i].getName().equalsIgnoreCase(n)) {
                    return files[i]
                }
            }
        }
        return null
    }

    @Throws(PageException::class, IOException::class)
    private fun existsDir(client: AFTPClient?, strPath: String?): Boolean {
        var strPath = strPath
        strPath = strPath.trim()

        // get parent path
        val path = FTPPath(client, strPath)
        val p: String = path.getPath()
        val n: String = path.getName()
        strPath = p + "" + n
        if ("//".equals(p)) strPath = "/$n"
        if (!strPath.endsWith("/")) strPath += "/"
        return client.directoryExists(directory)
    }

    /**
     * removes a file on the server
     *
     * @return FTPCLient
     * @throws IOException
     * @throws PageException
     */
    @Throws(IOException::class, PageException::class)
    private fun actionRemove(): AFTPClient? {
        required("item", item)
        val client: AFTPClient? = client
        client.deleteFile(item)
        writeCfftp(client)
        return client
    }

    /**
     * rename a file on the server
     *
     * @return FTPCLient
     * @throws PageException
     * @throws IOException
     */
    @Throws(PageException::class, IOException::class)
    private fun actionRename(): AFTPClient? {
        required("existing", existing)
        required("new", _new)
        val client: AFTPClient? = client
        client.rename(existing, _new)
        writeCfftp(client)
        return client
    }

    /**
     * copy a local file to server
     *
     * @return FTPClient
     * @throws IOException
     * @throws PageException
     */
    @Throws(IOException::class, PageException::class)
    private fun actionPutFile(): AFTPClient? {
        required("remotefile", remotefile)
        required("localfile", localfile)
        val client: AFTPClient? = client
        val local: Resource = ResourceUtil.toResourceExisting(pageContext, localfile)
        // if(failifexists && local.exists()) throw new ApplicationException("File ["+local+"] already
        // exist, if you want to overwrite, set attribute
        // failIfExists to false");
        var `is`: InputStream? = null
        try {
            `is` = IOUtil.toBufferedInputStream(local.getInputStream())
            client.setFileType(getType(local))
            client.storeFile(remotefile, `is`)
        } finally {
            IOUtil.close(`is`)
        }
        writeCfftp(client)
        return client
    }

    /**
     * gets a file from server and copy it local
     *
     * @return FTPCLient
     * @throws PageException
     * @throws IOException
     */
    @Throws(PageException::class, IOException::class)
    private fun actionGetFile(): AFTPClient? {
        required("remotefile", remotefile)
        required("localfile", localfile)
        val client: AFTPClient? = client
        val local: Resource = ResourceUtil.toResourceExistingParent(pageContext, localfile)
        pageContext.getConfig().getSecurityManager().checkFileLocation(local)
        if (failifexists && local.exists()) throw ApplicationException("FTP File [$local] already exists, if you want to overwrite, set attribute [failIfExists] to false")
        var fos: OutputStream? = null
        client.setFileType(getType(local))
        var success = false
        try {
            fos = IOUtil.toBufferedOutputStream(local.getOutputStream())
            success = client.retrieveFile(remotefile, fos)
        } finally {
            IOUtil.close(fos)
            if (!success) local.delete()
        }
        writeCfftp(client)
        return client
    }

    /**
     * get url of the working directory
     *
     * @return FTPCLient
     * @throws IOException
     * @throws PageException
     */
    @Throws(PageException::class, IOException::class)
    private fun actionGetCurrentURL(): AFTPClient? {
        val client: AFTPClient? = client
        val pwd: String = client.printWorkingDirectory()
        val cfftp: Struct? = writeCfftp(client)
        cfftp.setEL("returnValue", client.getPrefix().toString() + "://" + client.getRemoteAddress().getHostName() + pwd)
        return client
    }

    /**
     * get path from the working directory
     *
     * @return FTPCLient
     * @throws IOException
     * @throws PageException
     */
    @Throws(PageException::class, IOException::class)
    private fun actionGetCurrentDir(): AFTPClient? {
        val client: AFTPClient? = client
        val pwd: String = client.printWorkingDirectory()
        val cfftp: Struct? = writeCfftp(client)
        cfftp.setEL("returnValue", pwd)
        return client
    }

    /**
     * change working directory
     *
     * @return FTPCLient
     * @throws IOException
     * @throws PageException
     */
    @Throws(IOException::class, PageException::class)
    private fun actionChangeDir(): AFTPClient? {
        required("directory", directory)
        val client: AFTPClient? = client
        client.changeWorkingDirectory(directory)
        writeCfftp(client)
        return client
    }

    @get:Throws(PageException::class, IOException::class)
    private val client: AFTPClient?
        private get() = pool.get(_createConnection())

    /**
     * removes a remote directory on server
     *
     * @return FTPCLient
     * @throws IOException
     * @throws PageException
     */
    @Throws(IOException::class, PageException::class)
    private fun actionRemoveDir(): AFTPClient? {
        required("directory", directory)
        val client: AFTPClient? = client
        if (recursive) {
            removeRecursive(client, directory, FTPFile.DIRECTORY_TYPE)
        } else client.removeDirectory(directory)
        writeCfftp(client)
        return client
    }

    /**
     * create a remote directory
     *
     * @return FTPCLient
     * @throws IOException
     * @throws PageException
     */
    @Throws(IOException::class, PageException::class)
    private fun actionCreateDir(): AFTPClient? {
        required("directory", directory)
        val client: AFTPClient? = client
        client.makeDirectory(directory)
        writeCfftp(client)
        return client
    }

    /**
     * List data of a ftp connection
     *
     * @return FTPCLient
     * @throws PageException
     * @throws IOException
     */
    @Throws(PageException::class, IOException::class)
    private fun actionListDir(): AFTPClient? {
        required("name", name)
        required("directory", directory)
        val client: AFTPClient? = client
        var files: Array<FTPFile?>? = client.listFiles(directory)
        if (files == null) files = arrayOfNulls<FTPFile?>(0)
        pageContext.setVariable(name, toQuery(files, "ftp", directory, client.getRemoteAddress().getHostName()))
        writeCfftp(client)
        return client
    }

    /**
     * Opens a FTP Connection
     *
     * @return FTPCLinet
     * @throws IOException
     * @throws PageException
     */
    @Throws(IOException::class, PageException::class)
    private fun actionOpen(): AFTPClient? {
        required("server", server)
        required("username", username)
        // required("password", password);
        val client: AFTPClient? = client
        writeCfftp(client)
        return client
    }

    /**
     * close an existing ftp connection
     *
     * @return FTPCLient
     * @throws PageException
     */
    @Throws(PageException::class)
    private fun actionClose(): AFTPClient? {
        val conn: FTPConnection? = _createConnection()
        val client: AFTPClient = pool.remove(conn)
        val cfftp: Struct? = writeCfftp(client)
        cfftp.setEL("succeeded", Caster.toBoolean(client != null))
        return client
    }

    /**
     * throw an error if the value is empty (null)
     *
     * @param attributeName
     * @param atttributValue
     * @throws ApplicationException
     */
    @Throws(ApplicationException::class)
    private fun required(attributeName: String?, atttributValue: String?) {
        if (atttributValue == null) throw ApplicationException("Invalid combination of attributes for the tag [ftp]", "attribute [$attributeName] is required, if action is [$action]")
    }

    /**
     * writes cfftp variable
     *
     * @param client
     * @return FTPCLient
     * @throws PageException
     */
    @Throws(PageException::class)
    private fun writeCfftp(client: AFTPClient?): Struct? {
        val cfftp: Struct = StructImpl()
        if (result == null) pageContext.variablesScope().setEL(CFFTP, cfftp) else pageContext.setVariable(result, cfftp)
        if (client == null) {
            cfftp.setEL(SUCCEEDED, Boolean.FALSE)
            cfftp.setEL(ERROR_CODE, Double.valueOf(-1))
            cfftp.setEL(ERROR_TEXT, "")
            cfftp.setEL(RETURN_VALUE, "")
            return cfftp
        }
        val repCode: Int = client.getReplyCode()
        val repStr: String = client.getReplyString()
        cfftp.setEL(ERROR_CODE, Double.valueOf(repCode))
        cfftp.setEL(ERROR_TEXT, repStr)
        cfftp.setEL(SUCCEEDED, Caster.toBoolean(client.isPositiveCompletion()))
        cfftp.setEL(RETURN_VALUE, repStr)
        return cfftp
    }

    /**
     * check completion status of the client
     *
     * @param client
     * @return FTPCLient
     * @throws ApplicationException
     */
    @Throws(ApplicationException::class)
    private fun checkCompletion(client: AFTPClient?): Boolean {
        val isPositiveCompletion: Boolean = client.isPositiveCompletion()
        if (isPositiveCompletion) return false
        if (count++ < retrycount) return true
        if (stoponerror) {
            throw FTPException(action, client)
        }
        return false
    }

    /**
     * get FTP. ... _FILE_TYPE
     *
     * @param file
     * @return type
     */
    private fun getType(file: Resource?): Int {
        return if (transferMode == FTPConstant.TRANSFER_MODE_BINARY) AFTPClient.FILE_TYPE_BINARY else if (transferMode == FTPConstant.TRANSFER_MODE_ASCCI) AFTPClient.FILE_TYPE_TEXT else {
            val ext: String = ResourceUtil.getExtension(file, null)
            if (ext == null || ListUtil.listContainsNoCase(ASCIIExtensionList, ext, ";", true, false) === -1) AFTPClient.FILE_TYPE_BINARY else AFTPClient.FILE_TYPE_TEXT
        }
    }

    /**
     * @return return a new FTP Connection Object
     */
    private fun _createConnection(): FTPConnection? {
        return FTPConnectionImpl(connectionName, server, username, password, getPort(), timeout, transferMode, passive, proxyserver, proxyport, proxyuser, proxypassword,
                fingerprint, stoponerror, secure, key, passphrase)
    }

    /**
     * @param password The password to set.
     */
    fun setPassword(password: String?) {
        this.password = password
    }

    /**
     * @param username The username to set.
     */
    fun setUsername(username: String?) {
        this.username = username
    }

    /**
     * The path to the file that contains a private key
     *
     * @param key
     */
    fun setKey(key: String?) {
        this.key = key
    }

    /**
     * The passphrase that protects the private key
     *
     * @param passphrase
     */
    fun setPassphrase(passphrase: String?) {
        this.passphrase = passphrase
    }

    /**
     * @param server The server to set.
     */
    fun setServer(server: String?) {
        this.server = server
    }

    /**
     * @param timeout The timeout to set.
     */
    fun setTimeout(timeout: Double) {
        this.timeout = timeout.toInt()
    }

    /**
     * @param port The port to set.
     */
    fun setPort(port: Double) {
        this.port = port.toInt()
    }

    fun getPort(): Int {
        if (port != -1) return port
        return if (secure) PORT_SFTP else PORT_FTP
    }

    /**
     * @param connection The connection to set.
     */
    fun setConnection(connection: String?) {
        connectionName = connection
    }

    /**
     * @param proxyserver The proxyserver to set.
     */
    fun setProxyserver(proxyserver: String?) {
        this.proxyserver = proxyserver
    }

    /**
     * set the value proxyport The port number on the proxy server from which the object is requested.
     * Default is 80. When used with resolveURL, the URLs of retrieved documents that specify a port
     * number are automatically resolved to preserve links in the retrieved document.
     *
     * @param proxyport value to set
     */
    fun setProxyport(proxyport: Double) {
        this.proxyport = proxyport.toInt()
    }

    /**
     * set the value username When required by a proxy server, a valid username.
     *
     * @param proxyuser value to set
     */
    fun setProxyuser(proxyuser: String?) {
        this.proxyuser = proxyuser
    }

    /**
     * set the value password When required by a proxy server, a valid password.
     *
     * @param proxypassword value to set
     */
    fun setProxypassword(proxypassword: String?) {
        this.proxypassword = proxypassword
    }

    /**
     * @param retrycount The retrycount to set.
     */
    fun setRetrycount(retrycount: Double) {
        this.retrycount = retrycount.toInt()
    }

    /**
     * @param stoponerror The stoponerror to set.
     */
    fun setStoponerror(stoponerror: Boolean) {
        this.stoponerror = stoponerror
    }

    /**
     * @param passive The passive to set.
     */
    fun setPassive(passive: Boolean) {
        this.passive = passive
    }

    /**
     * @param directory The directory to set.
     */
    fun setDirectory(directory: String?) {
        this.directory = directory
    }

    /**
     * @param name The name to set.
     */
    fun setName(name: String?) {
        this.name = name
    }

    fun setRecurse(recursive: Boolean) {
        this.recursive = recursive
    }

    /**
     * @param extensionList The aSCIIExtensionList to set.
     */
    fun setAsciiextensionlist(extensionList: String?) {
        ASCIIExtensionList = extensionList.toLowerCase().trim()
    }

    /**
     * @param transferMode The transferMode to set.
     */
    fun setTransfermode(transferMode: String?) {
        var transferMode = transferMode
        transferMode = transferMode.toLowerCase().trim()
        if (transferMode.equals("binary")) this.transferMode = FTPConstant.TRANSFER_MODE_BINARY else if (transferMode.equals("ascci")) this.transferMode = FTPConstant.TRANSFER_MODE_ASCCI else this.transferMode = FTPConstant.TRANSFER_MODE_AUTO
    }

    /**
     * @param localfile The localfile to set.
     */
    fun setLocalfile(localfile: String?) {
        this.localfile = localfile
    }

    /**
     * @param remotefile The remotefile to set.
     */
    fun setRemotefile(remotefile: String?) {
        this.remotefile = remotefile
    }

    /**
     * @param failifexists The failifexists to set.
     */
    fun setFailifexists(failifexists: Boolean) {
        this.failifexists = failifexists
    }

    /**
     * @param _new The _new to set.
     */
    fun setNew(_new: String?) {
        this._new = _new
    }

    /**
     * @param existing The existing to set.
     */
    fun setExisting(existing: String?) {
        this.existing = existing
    }

    /**
     * @param item The item to set.
     */
    fun setItem(item: String?) {
        this.item = item
    }

    /**
     * @param result The result to set.
     */
    fun setResult(result: String?) {
        this.result = result
    }

    fun setFingerprint(fingerprint: String?) {
        this.fingerprint = fingerprint
    }

    companion object {
        private val ASCCI_EXT_LIST: String? = "txt;htm;html;cfm;cfml;shtm;shtml;css;asp;asa"
        private const val PORT_FTP = 21
        private const val PORT_SFTP = 22
        private val SUCCEEDED: Key? = KeyConstants._succeeded
        private val ERROR_CODE: Key? = KeyImpl.getInstance("errorCode")
        private val ERROR_TEXT: Key? = KeyImpl.getInstance("errorText")
        private val RETURN_VALUE: Key? = KeyImpl.getInstance("returnValue")
        private val CFFTP: Key? = KeyImpl.getInstance("cfftp")
        @Throws(IOException::class)
        private fun removeRecursive(client: AFTPClient?, path: String?, type: Int) {
            // directory
            var path = path
            if (FTPFile.DIRECTORY_TYPE === type) {
                if (!path.endsWith("/")) path += "/"
                // first we remove the children
                val children: Array<FTPFile?> = client.listFiles(path)
                for (child in children) {
                    if (child.getName().equals(".") || child.getName().equals("..")) continue
                    removeRecursive(client, path + child.getName(), child.getType())
                }
                // then the directory itself
                client.removeDirectory(path)
            } else if (FTPFile.FILE_TYPE === type) {
                client.deleteFile(path)
            }
        }

        @Throws(PageException::class)
        fun toQuery(files: Array<FTPFile?>?, prefix: String?, directory: String?, hostName: String?): lucee.runtime.type.Query? {
            var directory = directory
            val cols = arrayOf<String?>("name", "isdirectory", "lastmodified", "length", "mode", "path", "url", "type", "raw", "attributes")
            val types = arrayOf<String?>("VARCHAR", "BOOLEAN", "DATE", "DOUBLE", "VARCHAR", "VARCHAR", "VARCHAR", "VARCHAR", "VARCHAR", "VARCHAR")
            val query: lucee.runtime.type.Query = QueryImpl(cols, types, 0, "query")

            // translate directory path for display
            if (directory!!.length() === 0) directory = "/" else if (directory.startsWith("./")) directory = directory.substring(1) else if (directory.charAt(0) !== '/') directory = '/' + directory
            if (directory.charAt(directory!!.length() - 1) !== '/') directory = "$directory/"
            var row: Int
            for (i in files.indices) {
                val file: FTPFile? = files!![i]
                if (file.getName().equals(".") || file.getName().equals("..")) continue
                row = query.addRow()
                query.setAt("attributes", row, "")
                query.setAt("isdirectory", row, Caster.toBoolean(file.isDirectory()))
                query.setAt("lastmodified", row, DateTimeImpl(file.getTimestamp()))
                query.setAt("length", row, Caster.toDouble(file.getSize()))
                query.setAt("mode", row, FTPConstant.getPermissionASInteger(file))
                query.setAt("type", row, FTPConstant.getTypeAsString(file.getType()))
                // query.setAt("permission",row,FTPConstant.getPermissionASInteger(file));
                query.setAt("raw", row, file.getRawListing())
                query.setAt("name", row, file.getName())
                query.setAt("path", row, directory + file.getName())
                query.setAt("url", row, prefix.toString() + "://" + hostName + "" + directory + file.getName())
            }
            return query
        }
    }
}