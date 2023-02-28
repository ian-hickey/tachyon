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
package tachyon.runtime.tag

import java.util.ArrayList

/**
 * Retrieves and deletes e-mail messages from a POP mail server.
 */
abstract class _Mail : TagImpl() {
    inner class Credential

    private var server: String? = null
    private var port = -1
    private var username: String? = null
    private var password: String? = null
    private var action: String? = "getheaderonly"
    private var name: String? = null
    private var messageNumber: String? = null
    private var uid: String? = null
    private var delimiter: String? = null
    private var attachmentPath: Resource? = null
    private var timeout = 60
    private var startrow = 1
    private var maxrows = -1
    private var generateUniqueFilenames = false
    var isSecure = false
    private var folder: String? = null
    private var newfolder: String? = null
    private var recurse = false
    private var connection: String? = null
    private val id: String?
    private val credentials: List<Credential?>? = ArrayList<Credential?>()
    @Override
    fun release() {
        server = null
        port = -1
        username = null
        password = null
        action = "getheaderonly"
        name = null
        messageNumber = null
        uid = null
        delimiter = null
        attachmentPath = null
        timeout = 60
        startrow = 1
        maxrows = -1
        generateUniqueFilenames = false
        isSecure = false
        folder = null
        newfolder = null
        recurse = false
        connection = null
        super.release()
    }

    /**
     * @param server The server to set.
     */
    fun setServer(server: String?) {
        this.server = server
    }

    /**
     * @param port The port to set.
     */
    fun setPort(port: Double) {
        this.port = port.toInt()
    }

    fun setFolder(folder: String?) {
        this.folder = folder
    }

    fun setNewfolder(newfolder: String?) {
        this.newfolder = newfolder
    }

    fun setRecurse(recurse: Boolean) {
        this.recurse = recurse
    }

    fun setConnection(connection: String?) {
        this.connection = connection
    }

    /**
     * @param username The username to set.
     */
    fun setUsername(username: String?) {
        this.username = username
    }

    /**
     * @param password The password to set.
     */
    fun setPassword(password: String?) {
        this.password = password
    }

    /**
     * @param action The action to set.
     */
    fun setAction(action: String?) {
        this.action = action.trim().toLowerCase()
    }

    /**
     * @param name The name to set.
     */
    fun setName(name: String?) {
        this.name = name
    }

    /**
     * @param messageNumber The messageNumber to set.
     * @throws PageException
     */
    @Throws(PageException::class)
    fun setMessagenumber(messageNumber: String?) {
        this.messageNumber = messageNumber
    }

    /**
     * @param uid The uid to set.
     * @throws PageException
     */
    @Throws(PageException::class)
    fun setUid(uid: String?) {
        this.uid = uid
    }

    /**
     * @param delimiter The delimiter to set.
     * @throws PageException
     */
    @Throws(PageException::class)
    fun setDelimiter(delimiter: String?) {
        this.delimiter = delimiter
    }

    /**
     * @param attachmentPath The attachmentPath to set.
     * @throws PageException
     */
    @Throws(PageException::class)
    fun setAttachmentpath(attachmentPath: String?) {
        // try {
        var attachmentDir: Resource = pageContext.getConfig().getResource(attachmentPath)
        if (!attachmentDir.exists() && !attachmentDir.mkdir()) {
            attachmentDir = pageContext.getConfig().getTempDirectory().getRealResource(attachmentPath)
            if (!attachmentDir.exists() && !attachmentDir.mkdir()) throw ApplicationException("Directory [$attachmentPath] doesn't exist and couldn't be created")
        }
        if (!attachmentDir.isDirectory()) throw ApplicationException("File [$attachmentPath] is not a directory")
        pageContext.getConfig().getSecurityManager().checkFileLocation(attachmentDir)
        this.attachmentPath = attachmentDir
        /*
		 * } catch(IOException ioe) { throw Caster.toPageException(ioe); }
		 */
    }

    /**
     * @param maxrows The maxrows to set.
     */
    fun setMaxrows(maxrows: Double) {
        this.maxrows = maxrows.toInt()
    }

    /**
     * @param startrow The startrow to set.
     */
    fun setStartrow(startrow: Double) {
        this.startrow = startrow.toInt()
    }

    /**
     * @param timeout The timeout to set.
     */
    fun setTimeout(timeout: Double) {
        this.timeout = timeout.toInt()
    }

    /**
     * @param generateUniqueFilenames The generateUniqueFilenames to set.
     */
    fun setGenerateuniquefilenames(generateUniqueFilenames: Boolean) {
        this.generateUniqueFilenames = generateUniqueFilenames
    }

    /**
     * @param debug The debug to set.
     */
    fun setDebug(debug: Boolean) {
        // does nothing this.debug = debug;
    }

    @Override
    @Throws(PageException::class)
    fun doStartTag(): Int {
        if (!StringUtil.isEmpty(delimiter) && uid == null) throw ApplicationException("must specify the attribute [uid] when the attribute delimiter is defined")

        // check attrs
        if (port == -1) port = defaultPort
        checkConnection()

        // PopClient client = new PopClient(server,port,username,password);
        val client: MailClient
        client = try {
            MailClient.getInstance(type, server, port, username, password, isSecure, connection, id)
        } catch (e: Exception) {
            throw Caster.toPageException(e)
        }
        // store connection data
        if (!StringUtil.isEmpty(connection) && StringUtil.isEmpty(server)) {
        }
        client.setDelimiter(if (!StringUtil.isEmpty(delimiter, true)) delimiter else ",")
        client.setTimeout(timeout * 1000)
        client.setMaxrows(maxrows)
        if (startrow > 1) client.setStartrow(startrow - 1)
        client.setUniqueFilenames(generateUniqueFilenames)
        if (attachmentPath != null) client.setAttachmentDirectory(attachmentPath)
        if (uid != null) messageNumber = null
        try {
            // client.connect();
            if (action!!.equals("getheaderonly")) {
                required(tagName, action, "name", name)
                pageContext.setVariable(name, client.getMails(messageNumber, uid, false, folder))
            } else if (action!!.equals("getall")) {
                required(tagName, action, "name", name)
                pageContext.setVariable(name, client.getMails(messageNumber, uid, true, folder))
            } else if (action!!.equals("delete")) {
                client.deleteMails(messageNumber, uid)
            } else if (type == MailClient.TYPE_IMAP && action!!.equals("open")) {
                // no action necessary, because getting a client above already does the trick
            } else if (type == MailClient.TYPE_IMAP && action!!.equals("close")) {
                MailClient.removeInstance(client)
                // no action necessary, because getting a client above already does the trick
            } else if (type == MailClient.TYPE_IMAP && action!!.equals("markread")) {
                client.markRead(folder)
            } else if (type == MailClient.TYPE_IMAP && action!!.equals("createfolder")) {
                required(tagName, action, "folder", folder)
                client.createFolder(folder)
            } else if (type == MailClient.TYPE_IMAP && action!!.equals("deletefolder")) {
                required(tagName, action, "folder", folder)
                client.deleteFolder(folder)
            } else if (type == MailClient.TYPE_IMAP && action!!.equals("renamefolder")) {
                required(tagName, action, "folder", folder)
                required(tagName, action, "newfolder", newfolder)
                client.renameFolder(folder, newfolder)
            } else if (type == MailClient.TYPE_IMAP && action!!.equals("listallfolders")) {
                pageContext.setVariable(name, client.listAllFolder(folder, recurse, startrow, maxrows))
            } else if (type == MailClient.TYPE_IMAP && action!!.equals("movemail")) {
                required(tagName, action, "newfolder", newfolder)
                client.moveMail(folder, newfolder, messageNumber, uid)
            } else {
                var actions = "getHeaderOnly,getAll,delete"
                if (type == MailClient.TYPE_IMAP) actions += ",open,close,markread,createfolder,deletefolder,renamefolder,listallfolders,movemail"
                throw ApplicationException("Invalid value for attribute [action], valid values are [$actions]")
            }
        } catch (e: Exception) {
            throw Caster.toPageException(e)
        } finally {
            // client.disconnectEL();
        }
        return SKIP_BODY
    }

    @Throws(ApplicationException::class)
    private fun checkConnection() {
        if (StringUtil.isEmpty(connection) && StringUtil.isEmpty(server)) {
            throw ApplicationException("You need to define the attribute [connection] or [server].")
        }
    }

    protected abstract val type: Int
    protected abstract val defaultPort: Int
    protected abstract val tagName: String?

    init {
        id = CreateUniqueId.invoke()
    }
}